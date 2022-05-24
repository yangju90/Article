## KafkaClient消费Ack

#### 1. KafkaClient异步提交

* AckMode.MANUAL_IMMEDIATE

  (1) Batch获取数据，处理过程中，Consumer group 失活或者rebalance，异步提交失败。

  问题为subsequent calls to poll() was longer than the configured max.poll.interval.ms

  ```java
  具体表现：
      Listener会将这一批数据全部处理完成，统一会抛出异常。然后再次从成功提交的位置开始消费。
  解决方案：因为MANUAL_IMMEDIATE异步提交，是逐条消费,逐条提交，如果出现这种异常，异常情况下，应该增大max.poll.interval.ms
  ```

  

* AckMode.MANUAL