# Octopus <img src="https://raw.githubusercontent.com/Tarikul-Islam-Anik/Animated-Fluent-Emojis/master/Emojis/Animals/Octopus.png" alt="Octopus" width="30" height="30"/>

基于 Netty 实现的轻量级消息发布 / 订阅中间件，完整实现了 MQTT V3.1.1 协议，支持三种消息 QoS，离线消息持久化，可用于服务与服务之间，客户端与服务之间的通信，支持 Broker 集群部署与服务状态监控。

## 特性

- 基于 Spring Boot 实现服务动态配置、依赖注入与服务组件管理。
- 基于 Netty 实现 MQTT 消息编解码、心跳检测与客户端连接管理。
- 基于 CTrie 实现线程安全的无锁并发主题订阅管理，支持通配符匹配。
- 基于 Spring Data JPA 和 MySQL 实现消息持久化存储。
- 基于 Consul 实现服务注册与发现。
- 基于 gRPC 实现 Broker 集群内部消息通信，支持单一客户端重复登陆检测并实时踢下线。
- 通过 Redis 实现消息缓存，支持 Pub/Sub 通道消息桥接。
- 基于 Spring AOP 埋点实现自定义 Prometheus Exporter，结合 Grafana 实现 Broker 集群状态可视化监控。
- 通过 Nginx 实现 Broker 集群 L4 负载均衡，Docker Compose 实现集群服务快速部署。

<img src="https://github.com/huohaodong/octopus/assets/42486690/1d6199e1-cd3a-457c-bf45-7ffd66a1b8d8" width=50% height=50%>

## 快速开始

### 简单使用

Octopus 默认依赖于 Redis 与 MySQL，请在使用前确保有可用的 Redis 与 MySQL 服务并修改 application.yml 进行配置。此外，还可以使用 Octopus 提供的 `docker-compose.yml` 进行快速部署，具体请参考 [Docker Compose](#Docker-Compose) 小节。

从源码编译（JDK >= 17）：

```shell
mvn clean package -DskipTests
```

启动 Octopus：

```shell
java -jar ./octopus-broker/target/octopus-broker-xxxx.jar # 根据当前编译得到的版本启动程序
```

Octopus 默认监听本机 `20000` 端口，可以根据需要自行修改 `application.yml` 中的相关配置。

### Docker

编译源码：

```shell
mvn clean package -DskipTests
```

构造镜像：

```shell
cd ./octopus-broker
docker build -t octopus:latest .
```

启动 Octopus：

```shell
docker run --name octupos -p 20000:20000 -d octopus:latest
```

## Benchmark

推荐使用 [MQTTX-CLI](https://mqttx.app/cli) 进行基准测试，[MQTTX-Client](https://mqttx.app/) 进行图形化测试。

```bash
# 连接基准测试
mqttx bench conn -c 3000 -h localhost -p 20000

# 订阅基准测试
mqttx bench sub -c 10000 -t bench/%i -h localhost -p 20000 -q 2

# 发布基准测试
mqttx bench pub -c 2000 -t bench/%i -h localhost -p 20000 -q 2
```

## Docker Compose

### 部署 Broker 集群

首先编译源码：

```shell
mvn clean package -DskipTests
```

接着构造 Octopus 镜像：

```shell
cd ./octopus-broker
docker build -t octopus:latest .
```

最后使用 Octopus 提供的 docker-compose 文件来启动 Broker 集群：

```shell
docker-compose -f ./docker-compose.yml up -d
```

`docker-compose.yml` 会默认启动三个 Broker，分别为 Broker1、Broker2 和 Broker3 以及它们运行所依赖的 MySQL 与 Redis 服务。Broker 在将自身服务注册到 Consul 容器后，通过 gRPC 实现 Broker 集群内部通信与消息转发，最后通过 Nginx 服务暴露向外暴露唯一端口实现集群内部的负载均衡。

容器服务及其对应的端口使用情况如下所示：

|     服务名      | 端口号 |                  说明                   |
| :-------------: | :----: | :-------------------------------------: |
|  octopus-mysql  |  3306  |               MySQL 服务                |
|  octopus-redis  |  6379  |               Redis 服务                |
| octopus-consul  |  8500  | Consul 服务|
|  octopus-nginx  | 20000  | Nginx 服务 |
| octopus-broker1 | 20001  |           Octopus Broker 服务           |
| octopus-broker2 | 20002  |           Octopus Broker 服务           |
| octopus-broker3 | 20003  |           Octopus Broker 服务           |

### 服务监控

Octopus 提供了默认的 Prometheus Exporter 实现，可以通过修改 `application.yml` 文件来启用对应的 Exporter 服务，默认端口号为 19998。

Octopus Prometheus Exporter 提供的默认监控数据如下所示：

|  监控数据名   |                 说明                 |
| :------------------------------: | :----------------------------------: |
|    topic_active    |    Broker 当前主题总数      |
| subscription_active |    Broker 当前订阅总数      |
|   retain_message_active   |    Broker 当前保留消息总数    |
|   will_message_active   |    Broker 当前遗嘱消息总数    |
|  connection_active   |   Broker 当前客户端连接总数   |
|     message_sent_total      | 已发送的数据包总数 |
|   message_received_total    | 已接收的数据包总数 |

### 监控示例

![prometheus grafana](https://user-images.githubusercontent.com/42486690/227205714-9218fd04-0998-4626-a499-735b4438ea75.png)

## 协议实现参考

- emqx https://github.com/emqx/emqx
- jmqtt https://github.com/Cicizz/jmqtt

- moquette https://github.com/moquette-io/moquette
- iot-mqtt-server https://gitee.com/recallcode/iot-mqtt-server
