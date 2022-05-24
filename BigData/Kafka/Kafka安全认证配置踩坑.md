### Kafka安全配置，使用JAAS认证

Kafka安全配置，JAAS认证中文资料非常多，可以轻易的参考并配置下来，但是在过程中也踩到了一些坑，一下就是记录。

#### 1. unable to find LoginModule class：.....PlainLoginModule

(1) 出现类未加载，首先日志排查了 libs下的包kafka-clients*.jar是否有加载，当然正常加载

(2) 排查了很多网络方向的问题，也未解决

(3) 最后发现是jaas配置文件问题，非常坑的是这部分为了节省时间，直接从网页上copy下载，虽然和官网中的配置一模一样，因为空白符的原因，所以加载错误。。。。



注意：

> 服务器中的空白符需要特别注意，容易出现异常错误



#### 2. kafka consumer和producer console端认证配置

(1) 要配置脚本文件kafka-console-consumer/producer.sh

```shell
if [ "x$KAFKA_HEAP_OPTS" = "x" ]; then
    export KAFKA_HEAP_OPTS="-Xmx512M -Djava.security.auth.login.config=/home/halo/data/conf/kafka_topic_jass.conf"
fi
```

(2) 要配置启用验证文件producer/consumer.properties

```shell
// 增加
security.protocol=SASL_PLAINTEXT
sasl.mechanism=PLAIN
```

(3) producer和consumer启用命令加入

```shell
# consumer
./bin/kafka-console-consumer.sh --bootstrap-server localhost:9092 --from-beginning --topic test --consumer.config config/consumer.properties

# producer
./bin/kafka-console-producer.sh --broker-list localhost:9092 --topic test --producer.config config/producer.properties
```

(4) 查看消费者组

```shell
./bin/kafka-consumer-groups.sh --bootstrap-server localhost:9092 --list --command-config config/producer.properties
```

(5) 创建topic

```shell
# Kafka官网并没有查询到认证kafka-topics脚本的方法，但是可以直接绕过kafka操作zookeeper进行创建，同时这里可以添加Zookeeper的认证来控制权限
./bin/kafka-topics.sh --create --zookeeper localhost:2181 --replication-factor 1 --partitions 1 --topic test1
```

(6) 查看topic describe

```shell
bin/kafka-topics.sh --zookeeper 127.0.0.1:2181 --topic test --describe
```

