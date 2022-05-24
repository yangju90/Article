[toc]

## Kafka参数配置

kafka 相关参数包括：

* Broker端参数（Static Config 静态参数配置，必须重启生效）
* Topic主题参数 （Kafka 提供了Kafka config 配置生效）
* JVM参数



参数生效优先级： Topic 主题参数



#### 1. Broker端参数

##### Broker配置存储信息参数

* log.dirs: 指定了Broker需要使用的若干目录路径。无默认值
* log.dir: 指定Broker log路径，只能单个

生产系统中，使用dirs，指定多目录，最好多磁盘。一方面，以磁盘提高IO和系统throughput；另一方面，能够实现故障转移，即Failover，避免磁盘损坏，broker关闭（同时也是Kafka可以舍弃RAID方案的基础）。

#### 2. Broker与Zookeeper相关配置

Zookeepr在Kafka中作用：1. 分布式协调框架，负责协调管理并保存Kafka集群所有元数据信息，比如集群Broker运行监控，Topic、Partition创建、分区及所在位置。



* zookeeper.connect： kafka 连接zookeeper位置，两套kafka集群使用zookeeper，可以使用chroot，即:

    ```yml
    zookeeper.connect: zk1:2181,zk2:2181,zk3:2181/kafka1
    zookeeper.connect: zk1:2181,zk2:2181,zk3:2181/kafka2
    ```



#### 3. 客户端与Broker连接相关

* listeners : 监听器，告诉外部连接着需要通过什么协议访问指定主机名和端口开放的Kafka服务
* advertised.listeners : Advertised 表示宣称、公布的，对外发布监听器配置（双网卡，内外网访问配置）

监听器配置构成<协议名称://主机号:端口号>，协议包括：

* PLAINTEXT 明文传输
* SSL 加密传输

如果不是明文传输，需要加入配置，listener.security.protocol.map。

listener.security.protocol.map=CONTROLLER:PLAINTEXT  表示CONTROLLER这个自动以协议底层使用明文不加密传输数据。

* delete.topic.enable  可以为true，注意设定权限



#### 4. Topic管理参数

* auto.create.topics.enable : 是否允许自动创建Topic

* unclean.leader.election.enable : 是否允许非ISR节点选举，ISR队列同replica.lag.time.max.ms 配置生效

* auto.leader.rebalance.enable : 是自动进行leader选举，触发与broker上的leader数量有关

  ```yml
  （建议false关闭自动定时选举， 但是会影响 leader自动平衡，如果出现broker挂到重启，不会平衡leader，是一个取舍参数，线上大规模变换leader容易出现危险）通过 kafka-preferred-replica-election.sh 脚本来重新平衡集群中的leader副本。但是我们配置这个参数为true的话，controller角色就会每五分钟（默认）检查一下集群不平衡的状态，进而重新平衡leader副本。
  
  broker数量比率由leader.imbalance.per.broker.percentage参数控制，默认为10% int 10
  ```


* auto.offset.reset :

  ```java
  按照consumer Group 划分：
  earliest: 当各分区下有已提交的offset时，从提交的offset开始消费；无提交的offset时，从头开始消费
  latest: 当各分区下有已提交的offset时，从提交的offset开始消费；无提交的offset时，消费新产生的该分区下的数据
  none: topic各分区都存在已提交的offset时，从offset后开始消费；只要有一个分区不存在已提交的offset，则抛出异常
  ```

  

  consumer读取offset.reset参数的两种情况： 1. Kafka broker未保存位移信息 2. 位移信息对应消息不存在（可能已经被删除）。按照 offset.reset设置参数读取。

* num.partitions ： kafka 创建分区未指定分区数，时的默认分区数

#### 5. 数据存储参数

* log.retention.{hours|minutes|ms}：控制消息保存的时间，优先级顺序 ms > minutes > ms

* log.retention.bytes : 指定Broker为消息保存的总磁盘容量大小（-1时，不限制大小，既有broker端也有topic下的设置，同时参数与log segment大小也有关系，超出阈值的部分必须大于一个segment，才能删除最早的日志段）

  参考： https://www.cnblogs.com/huxi2b/p/8042099.html

  ```yml
  参数生效的范围是单个分区日志，删除的时候要对HW与删除位移上限的比较。如果后者越过了HW自然不允许删除该日志段。
  ```

  ![参数1](Kafka资源\参数1.png)

  LogStartOffset （LSO）: 消费开始

  High Watermark（HW）：ISR同步日志位置

  LogEndOffset（LEO）： 提交的日志

* message.max.bytes : 控制Broker能够接收的最大消息大小（默认1000012,不到1M）[三端参数共同生效]，消息越大，在compact时耗费内存越大，ByteBuffer占用（Log Cleaner，就是为topic执行compact操作的线程）。

  ```yml
  producer: message.max.bytes
  broker： message.max.bytes 
  		replica.fetch.max.bytes
  consumer: fetch.message.max.bytes
  ```

  

  