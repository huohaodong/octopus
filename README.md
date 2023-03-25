# Octopus <img src="https://raw.githubusercontent.com/Tarikul-Islam-Anik/Animated-Fluent-Emojis/master/Emojis/Animals/Octopus.png" alt="Octopus" width="30" height="30"/>

基于 Netty 实现的轻量级消息发布 / 订阅中间件，完整实现了 MQTT V3.1.1 协议，支持权限认证，消息单播、多播，可用于服务与服务之间，客户端与服务之间的通信，支持
Redis 消息桥接与 Broker 集群部署，支持 Broker 集群服务状态监控。

## 特性

- 基于 Spring Boot 实现服务动态配置、依赖注入与服务组件管理。
- 基于 Netty 实现 MQTT 消息编解码、心跳检测与客户端连接管理。
- 基于 CTrie 实现线程安全的无锁并发主题订阅管理，支持通配符匹配。
- 通过 Redis Pub / Sub 通道实现 Broker 集群数据桥接、Publish 消息同步与控制命令广播。
- 基于 Redis 实现分布式 Session 管理，支持单一客户端重复登陆检测并通过发布控制命令实时踢下线。
- 基于 Spring AOP 实现埋点并定时 Push 监控数据至 Redis 中，实现 Broker 状态采集与数据统计。
- 通过 Redis Exporter 接入统计数据至 Prometheus 并结合 Grafana 实现 Broker 集群状态可视化监控。
- 通过 Nginx 实现 Broker 集群负载均衡，Docker 实现集群服务快速部署。

## 快速开始

Octopus 默认依赖于 Redis，请先确保有可用的 Redis 服务并修改 `application.yml` 进行配置，或将 `application.yml` 中 storage 部分的配置选项全部修改为 `local` 以使用本地内存。

此外，还可以使用 Octopus 提供的 `docker-compose.yml` 进行快速部署，具体请参考 [Docker Compose](#Docker-Compose) 小节。

### 简单使用

从源码编译（JDK >= 11）：

```shell
mvn package -DskipTests
```

启动 Octopus：

```shell
java -jar ./target/octopus.jar # 根据当前编译得到的版本启动程序
```

Octopus 默认监听本机 `20000` 端口，可以根据需要自行修改 `application.yml` 中的相关配置。

### Docker

编译源码：

```shell
mvn package -DskipTests
```

构造镜像：

```shell
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
mvn package -DskipTests
```

接着构造 Octopus 镜像：

```shell
docker build -t octopus:latest .
```

最后使用 Octopus 提供的 docker-compose 文件来启动 Broker 集群：

```shell
docker-compose -f ./docker-compose.yml up -d
```

`docker-compose.yml` 会默认启动两个处于同一 Broker Group 内的 Broker，分别为 BROKER1 和 BROKER2，与此同时我们还启动了一个用以支撑
Broker 工作的 Redis 服务器。此外，为了实时检测 Broker 集群的运行状况，我们也启动了与之相应的 Prometheus 和 Grafana 服务。

容器服务及其对应的端口情况如下所示：

|                服务名                |  端口号  |                  说明                   |
|:---------------------------------:|:-----:|:-------------------------------------:|
|          octopus-broker1          | 20001 |           Octopus Broker 服务           |
|          octopus-broker2          | 20002 |           Octopus Broker 服务           |
|           octopus-redis           | 6379  |      Octopus Broker 对应的 Redis 服务      |
|        octopus-prometheus         | 9090  |   用于监控 Broker 集群运行情况的 Prometheus 服务   |
| octopus-prometheus-redis_exporter | 9121  | 用于采集 Redis 数据的 Prometheus Exporter 服务 |
|          octopus-grafana          | 3000  |          用于展示数据的 Grafana 服务           |

### 服务监控

Octopus 默认将监控数据存储在 Redis 服务中，并通过 Redis-Exporter 导出监控数据到 Prometheus 中，监控数据在 Redis 中以
Key-Value 的方式存储，命名规则为：`STATS:${BROKER 组名}:${监控数据类型}`，比如 `GROUP1` 中当前客户端连接数对应的 Key
为：`STATS:GROUP1:TOTAL_CONNECTION`。

对于我们刚刚部署的 Broker 集群而言，其对的应监控数据如下：

|       监控数据在 Redis 中对应的 Key       |            说明             |
|:--------------------------------:|:-------------------------:|
|    STATS:GROUP1:TOTAL_TOPICS     |   当前 Broker Group 主题总数    |
| STATS:GROUP1:TOTAL_SUBSCRIPTIONS |   当前 Broker Group 订阅总数    |
|   STATS:GROUP1:TOTAL_RETAINED    |  当前 Broker Group 保留消息总数   |
|     STATS:GROUP1:TOTAL_SENT      | 当前 Broker Group 已发送的数据包总数 |
|   STATS:GROUP1:TOTAL_RECEIVED    | 当前 Broker Group 已接收的数据包总数 |
|  STATS:GROUP1:TOTAL_CONNECTION   |  当前 Broker Group 客户端连接总数  |

接下来只需要参考[Prometheus 官方文档](https://prometheus.io/docs/visualization/grafana/)
，通过访问 `http://localhost:3000` 对我们启动的 Grafana 服务进行配置即可。

### 监控示例

![prometheus grafana](https://user-images.githubusercontent.com/42486690/227205714-9218fd04-0998-4626-a499-735b4438ea75.png)

## 协议实现参考

- mosquitto https://github.com/eclipse/mosquitto
- rocketmq-mqtt https://github.com/apache/rocketmq-mqtt
- iot-mqtt-server https://gitee.com/recallcode/iot-mqtt-server
- jmqtt https://github.com/Cicizz/jmqtt
