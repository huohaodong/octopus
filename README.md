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

### 简单使用

从源码编译（JDK >= 11）：

```shell
mvn package -DskipTests
```

启动 Octopus：

```shell
java -jar ./target/octopus-version.jar # 根据当前编译得到的版本启动程序
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

## 监控示例

![prometheus grafana](https://user-images.githubusercontent.com/42486690/227205714-9218fd04-0998-4626-a499-735b4438ea75.png)

## 协议实现参考

- mosquitto https://github.com/eclipse/mosquitto
- rocketmq-mqtt https://github.com/apache/rocketmq-mqtt
- iot-mqtt-server https://gitee.com/recallcode/iot-mqtt-server
- jmqtt https://github.com/Cicizz/jmqtt
