[toc]

#### Kafka Topic 配置

如果配置了Topic参数，Broker针对此Topic的通用配置将被覆盖。

* retention.bytes : 同broker级别log.retention.bytes，这里为Topic级别定义
* retention.ms : 规定了该 Topic 消息被保存的时长。默认是 7 天
* max.message.bytes : 

Topic Level 参数配置在两个阶段：1.创建Topic时指定 2.修改Topic配置是指定

```shell
(1) 创建主题test
bin/kafka-topics.sh --bootstrap-server localhost:9092 --create --topic test --partitions 1 --replication-factor 1 --config retention.ms=15552000000 --config max.message.bytes=5242880

(2) 修改主题test
bin/kafka-configs.sh --zookeeper localhost:2181 --entity-type topics --entity-name test --alter --add-config max.message.bytes=10485760
```



#### JVM配置

Kafka占用JVM堆大小，一般6-8G比较合适，Kafka与客户端交互时会在JVM创建大量的ByteBuffer。



JAVA 7 情况下，CPU资源充足使用CMS合适。 -XX: +UseCurrentMarkSweepGC

​							不充足使用吞吐量收集器ParallelGC，开启方法 -XX:+UseParallelGC 



JAVA 8 使用G1就不错，在没有任何调优的情况下，G1表现优于CMS，Full GC更少，需要调整的参数更少。



Kafka中对应的参数：

*  KAFKA_HEAP_OPTS: 指定堆代销
*  KAFKA_JVM_PERFORMANCE_OPTS: 指定GC参数

```shell

$> export KAFKA_HEAP_OPTS=--Xms6g  --Xmx6g
$> export KAFKA_JVM_PERFORMANCE_OPTS= -server -XX:+UseG1GC -XX:MaxGCPauseMillis=20 -XX:InitiatingHeapOccupancyPercent=35 -XX:+ExplicitGCInvokesConcurrent -Djava.awt.headless=true
$> bin/kafka-server-start.sh config/server.properties
```



#### 操作系统参数

Kafka并不需要设置太多OS参数，与其他软件一样，需要关心几个方向：

* 文件描述符限制     ulimit -n

  ```shell
  ulimit -a 显示当前用户进程限制
  ulimit -u 65536 用户进程数
  ulimit -n 1000000 用户文件描述符
  
  永久生效的方法：
  修改/etc/security/limits.conf文件
  
  * soft nofile 204800
  * hard nofile 204800
  * soft nproc 204800
  * hard nproc 204800
  
  noproc 进程 nofile 文件
  ```

  

* 文件系统类型   XFS > ext4

* swap分区使用，建议设置为接近0的值，例如 swappniess 为1

* 提交时间 （Kafka Page Cache 落盘时间，默认5秒，建议可以调大，提高性能）





#### 性能调优

关于Kafka堆大小设置，可以监控JVM中live data 数量来调整，堆越大触发 full gc时间就会越长，并非越大越好。

监控一下实时的堆大小，特别是GC之后的live data大小，通常将heapsize设置成其1.5~2倍就足以了

上了6g内存确实g1最好。4g cms，3g以下就pg



1、我们生产环境有个__counsumer_offsets-49目录经常会变的很大，把磁盘撑满，要设置什么参数优化一下啊？__

要确定是不是bug，目前这个topic占用磁盘空间过多的问题社区有几个bug，最好去搜搜看。另外用jstack查一下log cleaner线程的状态。最后调优的话就是适当增加log.cleaner.threads的值，前提是你的log cleaner线程是正常工作的

2、手工清理__counsumer_offsets-49目录下的文件有什么影响吗？

手动清理可能会造成部分consumer group的位移提交数据丢失



看集群规模，和partition 总数。

规模一定后，partition 总数多到一个范围后，就等同于随机读写了 （log 文件数非常多)。这样就不能充分利用kafka 顺序读写的好吞吐特性了。
hdd磁盘随机读写的性能，是非常差的，iops只能到 200以下。
具体的范围值，可以google下，有附带压测结果，有个文章讲的很清楚。





注：

**页缓存**属于磁盘缓存（Disk cache）的一种，主要是为了改善系统性能。重复访问磁盘上的磁盘块是常见的操作，把它们保存在内存中可以避免昂贵的磁盘IO操作。

既然叫页缓存，它是根据页（page）来组织的内存结构。每一页包含了很多磁盘上的块数据。Linux使用Radix树实现页缓存，主要是加速特定页的查找速度。另外一般使用LRU策略来淘汰过期页数据。总之它是一个完全由内核来管理的磁盘缓存，用户应用程序通常是无感知的。

如果要详细了解page cache，可以参见《Understanding the Linux Kernel》一书的第15章



**文件系统参数** 系统会根据LRU算法定期将页缓存上的 脏 数据落盘到物理磁盘上. 这个定期就是由提交时间来确定的,默认是5秒。  文件系统的参数，比如ext4就是commit=Nseconds这样设置