[toc]



## kafka简介

kafka是一款开源的消息引擎系统（消息队列中间件），消息在不同系统之间的传递核心是要保证信息的无歧义性，同时最大限度提供重用性和通用性。

**Kafka传递消息的两种模型**：

* 点对点模型
* 发布/订阅模型（Publisher/Subscriber）

消息引擎最大的作用，进行流量的**<font color=red >"削峰填谷"</font>**



Kafka 既是消息引擎系统，也是分布式流处理平台。设计之初就旨在提供三个方面的特性：

* 提供一套API实现生产者和消费者
* 降低网络传输和磁盘存储开销
* 实现高伸缩性架构



## Kafka术语

##### 1. 基本术语

* Topic 主题， 发布订阅的对象
* Producer 生产者
* Consumer 消费者
* Clients 客户端，包括生产者，消费者等

##### 2. 服务器端术语

* Broker kafka服务端进程构成，集群由多个Broker组成， 负责接收和处理客户端的请求，并对消息进行持久化
* Replication 备份机制，kafka实现HA的手段
* Replica 副本，数据拷贝，实际的数据备份存储单元；副本数量是可配置的，副本保存着相同数据，有着不同的角色和作用
  * Leader Replica 领导者副本， 与客户端交互，对外提供服务
  * Follower Replica 领导者副本的追随者， 不对外交互，仅做容灾
* Scalability 可扩展性
* Partitioning 分区， 主题可划分多个分区，保证了可扩展性，消息存储的单元，每个分区的offset从0开始

##### 3. 消息持久化

(1) Kafka 使用消息日志（Log）来保存数据，一个日志就是磁盘上一个只能追加写（Append-only）消息的物理文件。因为只能追加写入，故避免了缓慢的随机 I/O 操作，改为性能较好的顺序 I/O 写操作，这也是实现 Kafka 高吞吐量特性的一个重要手段。

(2) Kafka 定期地删除消息日志以回收磁盘。通过日志段（Log Segment）机制，日志又近一步细分成多个日志段，消息被追加写到当前最新的日志段中，当写满了一个日志段后，Kafka 会自动切分出一个新的日志段，并将老的日志段封存起来。Kafka 在后台还有定时任务会定期地检查老的日志段是否能够被删除，从而实现回收磁盘空间的目的。



##### 4. 消费者组（Consumer Group）

**点对点模型(Peer to Peer) ** 和 **发布订阅模型 **： Kafka 通过消费者组实现这两种模型，消费者组内的Consumer不会重复消费数据，消费同一主题下的数据；通过多个消费者组可以实现消息重复消费。

消费者组的设计提供了“瓜分” 数据的作用，也使Consumer“挂掉”后，可以进行Consumer Rebalance。



## Kafka 版本

##### Kafka 三大版本，即维护社区：

* Apache kafka（社区版）
  * 只维护了核心组件，Connect只提供了磁盘读写的连接器
* Confluence Kafka
  * 免费版： 提供了多种扩展组件，还包括Schema注册中心和Rest proxy功能
    * Schema注册中心：提供了 集中管理Kafka消息格式以实现数据向前/向后的兼容
    * Rest proxy： 以Http的方式访问Kafka
  * 商业版： 跨数据中心备份，集群监控工具等
* CDH kafka（Cloudera）
  * 集成大数据平台，包括安装、运维管理和监控功能



##### 版本演进：

版本号：
大 + 小 + patch

0.7版本:
只有基础消息队列功能，无副本；打死也不使用

0.8版本:
增加了副本机制，新的producer API；建议使用0.8.2.2版本；不建议使用0.8.2.0之后的producer API

0.9版本:
增加权限和认证，新的consumer API，Kafka Connect功能；不建议使用consumer API

0.10版本:
引入Kafka Streams功能，bug修复；建议版本0.10.2.2；建议使用新版consumer API

0.11版本:
producer API幂等，事物API，消息格式重构；建议版本0.11.0.3；谨慎对待消息格式变化

1.0和2.0版本:
Kafka Streams改进；建议版本2.0



##### Kafka 常见监控器：

* kafka manager ：多集群监控
* kafka eagle
* JMXTrans + InfluxDB + Grafana
* kafka tools： 可以看到kafka存储结构
* Kafka Offset Monitor ： 单集群监控





##### Kafka exactly once、most once、 least once 问题：

* exactly once : 
  * 分布式快照 / 状态检查点
  * 至少一次事件传递和对重复数据去重
  * 换句话说，**事件的处理可以发生多次，但是该处理的效果只在持久后端状态存储中反映一次**

* produce
  * most once： 消息丢失，acks = 0， 消息无需确认
  * least once ： acks = 1（leader确认） -1（Isr 队列确认）
    * min.insync.replicas  Isr队列中最小个数，与 -1 配合
    * unclean.leader.election.enable  false 则为非isr队列中数据不能参与选举，true isr中若无则选举其它副本，数据丢失
    * 落后时间大于多少，退出Isr队列
  * exactly once ：  幂等配置 enable.idempotence  和 开启提交事物
    * 幂等配置： 实际是使用带key的提交，能够保证同一次会话、同一partition间的exactly once
    * 事物提交：首先开启幂等配置，然后通过事物initTransactions，提交修改
* broker
  * broker 消息丢失，与acks参数有关
  * kafka 内部本身消息处理和流式处理支持了Exactly once的能力
* consumer
  * enable.auto.commit  是否手动提交位移
  * 自动提交会出现问题： 重复消费、丢失数据（offset位移不当）
  * 手动提交，也会出现重复消费问题：offset提交失败时（提交错误处理）
  * 实现Exactly once ，consumer记录offset位置（数据库、持久化等手段）





