## CheckPoint工作机制

![image-20200327095641408](Hadoop hdfs工作机制\image-20200327095641408.png)

1. 客户端请求建立连接。
2. client发送增删改请求。
3. 写入editlog日志，刷入内存，对应datanode的写流程。
4. SecondNamenode定时向namenode发送询问是否需要checkpoint（默认一分钟）。
5. namenode检查是否需要checkpoint，条件：1H未checkpoint，操作发生了100w次。返回给SecondNamenode需要checkpoint。然后暂停写操作，将editlog翻滚出新文件，用于写入，将旧文件重命名。
6. Secondnamnode发起checkpoint请求，namenode接到请求，将fsimage和editlog发送给secondnamenode，secondnamnode接收到后进行合并文件。
7. 合并完成后生成新文件，copy到namenode，重命名为fsimage，修改原文件文件名。
8. 合并完成。

##### fsimag
记录的是文件信息，每次启动加载fsimage和edit就是完整的namenode的数据。
但是其中没有保存块的具体得datanode信息，由于块信息可以会变化，由datananode通信确定。

##### editlog
记录client的每次操作。请求过来首先写入磁盘然后写入内存，防止数据丢失。

##### SecondNamenode作用
主要提供备份作用，当namenode数据丢失，可以用secondnamenode的备份，但是该数据不全。

##### datanode与namenode交互
他们交互主要采取心跳机制，然后同步块信息，通过cheksum检查数据信息正确性。



## SafeMode

SafeMode安全模式下，不能进行数据读写。启动时、blocks丢失时进入SafeMode模式，一般为30s。