<center><font size = 5 face="黑体">单机版Hadoop hdfs安装搭建记录</font></center>



```markdown
**系统配置**

**规格:** 1vCPUs | 2GB | s6.medium.2

**镜像:** Ubuntu 18.04 server 64bit

**用户:** 在Ubuntu上创建halo用户
```

预备软件：① Hadoop安装包（推荐cdh，cloudera站点） ② Java 1.8 + ③ ssh


#### 1. 安装Java

- 首先下载Linux版JDK jdk-8u161-linux-x64.tar.gz

- 解压安装包

  ```java
  tar -zxvf jdk-8u161-linux-x64.tar.gz -C unzipPath
  ```

- 配置环境变量 /etc/profile 或者 ~/.bash_profile

  ```java
  #set java environment
  JAVA_HOME=/usr/local/java/jdk1.8.0_161
  JRE_HOME=$JAVA_HOME/jre
  CLASS_PATH=.:$JAVA_HOME/lib/dt.jar:$JAVA_HOME/lib/tools.jar:$JRE_HOME/lib/rt.jar
  PATH=$PATH:$JAVA_HOME/bin:$JRE_HOME/bin
  export JAVA_HOME JRE_HOME CLASS_PATH PATH
  ```

- 使配置内容生效 source /etc/profile

- 验证 java -version

#### 2. 安装ssh，并配置免密登录

* 购买的服务器，已经安装了ssh，单机部署的情况下需要检查ssh服务是否安装

  ```java
  ps -e | grep ssh # 查看ssh进行
  systemctl status ssh  # 检测ssh状态
  ```

* 安装ssh

  ```java
  (1)判断是否安装ssh服务，可以通过如下命令进行：
  ssh localhost
  ssh: connect to host localhost port 22: Connection refused
  
  (2)如上所示，表示没有还没有安装，可以通过apt安装，命令如下：
  apt-get install openssh-server
  
  (3)启动服务：
  sudo /etc/init.d/ssh start  
  ```

* ssh免密登录

  ```markdown
  cd ~
  ssh-keygen -t rsa
  cd .ssh
  # 将生成的rsa公钥信息写入authorized_keys文件中
  cat id_rsa.pub >> authorized_keys
  # 修改authorized_keys文件读写权限
  chmod 600 authorized_keys
  ```

  .ssh文件夹结构

  ​		|--- id_rsa  # ssh rsa生成的私钥文件

  ​		|--- id_rsa.pub  # ssh rsa生成的公钥文件

  ​		|--- authorized_keys  # 免密登录文件

  ​		|--- know_hosts  # ssh远程登录记录

  ```java
  当ssh一台没登陆过的机器的时候,往往需要输入yes,确认一下添加know_hosts文件,在一些脚本处理的时候很不方便,可以修改/etc/ssh/ssh_config 文件达到自动添加,注意是ssh_config,不是sshd_config
  
  查找#   StrictHostKeyChecking ask 修改为 StrictHostKeyChecking no
  
  这样就可以达到自动添加know_hosts～～
  ```

#### 3. Hadoop安装

* 解压Hadoop3压缩包

* 添加环境变量.profile

  ```cmd
  export HADOOP_HOME=/home/hadoop0/app/hadoop-3.1.3
  export PATH=$HADOOP_HOME/bin:$PATH
  ```

* hadoop目录说明

  |--- bin                  # hadoop客户端命令 

  |--- etc/hadoop   # 相关配置文件存放目录

  |--- sbin				# 启动hadoop相关进程脚本(Server端)

  |--- share			 # 常见使用例子 ***(share/hadoop/mapreduce)

* 修改配置文件

  ```markdown
  (1) etc/hadoop/hadoop_env.sh
  # 添加
  export JAVA_HOME=/software/java/jdk1.8.0_161
  	
  (2) etc/hadoop/core-site.xml
  # 添加 hadoop0为配置的本地hosts
  <configuration>
          <property>
                  <name>fs.defaultFS</name>
                  <value>hdfs://hadoop0:9000</value>
          </property>
  </configuration>
  
  # 备注
  hadoop0 -> 127.0.0.1 则hadoop-client客户端不能连接，因为在本地监听
  
  改为 hdfs://0.0.0.0:9000，hadoop-client连接正常可以创建文件夹，但不能读写
  Exception：There are 1 datanode(s) running and 1 node(s) are excluded in this operation.
  
  stack overflow 建议更改为本地ip，但是在华为云服务器上或许因为ip转发问题，问题并未解决，有时间在研究。
  (已确定上面的Exeption在单机上部署不存在问题，应该是云服务器有ip转发的关系)
  
  (3) etc/hadoop/hdfs-site.xml
  # 添加
  <configuration>
      <property>
              <name>dfs.replication</name>    # 副本数
              <value>1</value>
      </property>
  
      <property>
       		# 文件blocks存放位置，默认在linux系统tmp文件夹下，重启可能丢失
       		# 所以需要修改存储位置
              <name>hadoop.tmp.dir</name>   
              <value>/home/hadoop0/data/tmp</value>
      </property>
  </configuration>
  
  (4) 修改workers文件
  # 添加配置 ip 或者 映射 name
  hadoop0
  ```

#### 4. 启动及验证

* 第一次启动hadoop之前执行系统格式化 

  ```markdown
  # 格式化
  hdfs namenode -format
  ```

* 启动

  ```markdown
  # 启动dfs
  sbin/start-dfs.sh
  启动hadoop dfs日志位置：logs/hadoop-hadoop0-namenode-xxxx.log
  # 停止集群
  sbin/stop-dfs.sh
  # 单个组件进程启动
  sbin/hadoop-daemons.sh stop|start|status xxx
  xxx可以为：
  	NameNode
  	SecondaryNameNode 
  	DataNode
  	
  netstat -ntlp
  ```

* 验证

  ```markdown
  (1)linux命令行jps出现
  NameNode、DataNode、SecondaryNameNode
  
  (2)验证网站-Namenode information
  http://ip:9870 注意防火墙问题 sudo ufw allow 9870 / systemctl stop firewalld
  
  (3)两种永久关闭/开启防火墙的方式
  systemctl disable firewalld
  systemctl enable firewalld
  chkconfig iptables off
  chkconfig iptables on
  ```

  [注：chkconfig与systemctl区别](<https://www.cnblogs.com/loveer/p/11619833.html>)

#### 5. Hadoop常用命令行操作

* 文件系统常见操作：查看、存储、移动、删除

   ```java
    hadoop fs -ls /         # 查看hadoop根目录文件夹
           -cp src dest  # 复制
           -getmerge file1 file2 localdst #合并
           -get          # 获取
           -put          # 提交（本地和hdfs均可以）
           ....
   ```

* -cat和-text的区别 (text命令会对文本进行解码转码，cat不会，所以cat命令输出会乱码)



--------------------------------------------------

#### 安装过程中注意事项及用到的Linux命令

1. Linux hosts文件修改

   ```markdown
   # 打开vi /etc/hosts 文件修改，创建的用户名为hadoop0
   127.0.0.1       localhost
   127.0.0.1       hadoop0 
   
   uname -a # 获取系统信息
   ```

2. 部分Linux 命令

   * ls命令

     ```markdown
     ls -a # 查看所有文件包括隐藏 
     ls -la # 树状展示
     ll -h # 显示数据大小，转换为(K、M ...)
     env  # 查看系统当前环境变量
     ```

   * tar命令

     ```java
     tar -zxvf jdk*.tar.gz -C ~/app # 解压文件到指定目录
     tar -czvf *.tar abc/   # 打包压缩文件 
     
     指令：
     -c  # 创建压缩文件
     -x  # 解压压缩文件
     -t  # 查看压缩包内有哪些文件
     -z  # 用Gzip解压或压缩
     -j  # 用bzip2解压或压缩
     -v  # 显示详细过程
     -f  # 目标文件名
     -P  # 保留原始权限与属性
     -p  # 使用绝对路径压缩
     -C  # 指定解压到的目录
     ```

3. ssh 修改端口

   ```markdown
   (1)修改ssh端口
   ssh默认的端口是22,配置在/etc/ssh/sshd_config
   Port 22
   Port 800
   编辑防火墙配置启用22和800端口。
   sudo /etc/init.d/ssh restart 
   这样ssh端口将同时工作与22和800上。
   
   (2)结果验证
   a.使用 ssh root@localhost -p 800
       
   b.或使用 systemctl status ssh
   出现
   Server listening on 0.0.0.0 port 800.
   Server listening on :: port 800.
   Server listening on 0.0.0.0 port 22.
   Server listening on :: port 22.
   
   如果连接成功了，则再次编辑sshd_config的设置，将里边的Port22删除，即可。
   ```

   