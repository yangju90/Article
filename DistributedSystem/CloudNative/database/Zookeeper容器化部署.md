#### 1. 在Linux硬件环境中部署

为保证集群高可用，Zookeeper 集群的节点数最好是奇数，最少有三个节点，所以这里演示搭建一个三个节点的集群。这里我使用三台主机进行搭建，主机名分别为 hadoop001，hadoop002，hadoop003。

###### 1.1 修改配置

```shell
# 解压 zookeeper 安装包，修改其配置文件 zoo.cfg
tickTime=2000
initLimit=10
syncLimit=5
dataDir=/usr/local/zookeeper-cluster/data/
dataLogDir=/usr/local/zookeeper-cluster/log/
clientPort=2181

# server.1 这个1是服务器的标识，可以是任意有效数字，标识这是第几个服务器节点，这个标识要写到dataDir目录下面myid文件里
# 指名集群间通讯端口和选举端口
server.1=hadoop001:2888:3888
server.2=hadoop002:2888:3888
server.3=hadoop003:2888:3888
```

###### 1.2 修改标识节点，启动

分别在三台主机的 `dataDir` 目录下新建 `myid` 文件,并写入对应的节点标识。Zookeeper 集群通过 `myid` 文件识别集群节点，并通过上文配置的节点通信端口和选举端口来进行节点通信，选举出 Leader 节点。

```shell
# 1. 创建存储目录：

# 三台主机均执行该命令
mkdir -vp  /usr/local/zookeeper-cluster/data/
# 2. 创建并写入节点标识到 myid 文件：

# hadoop001主机
echo "1" > /usr/local/zookeeper-cluster/data/myid
# hadoop002主机
echo "2" > /usr/local/zookeeper-cluster/data/myid
# hadoop003主机
echo "3" > /usr/local/zookeeper-cluster/data/myid

# 3. 启动
/usr/app/zookeeper-cluster/zookeeper/bin/zkServer.sh start

./zkServer.sh stop

./zkCli.sh -server 192.168.8.212:2181
```

#### 2. Zookeeper在Docker环境中部署

```shell
# 1. 拉取 MySQL 的 Docker 镜像
docker pull zookeeper:3.9.3
# 2. 创建本地的 `/data/zookeeper` 目录用于数据持久化存储
mkdir -p /data/zookeeper
mkdir -p /data/zookeeper/data/
mkdir -p /data/zookeeper/log/
# 3. 创建配置文件
tickTime=2000
initLimit=10
syncLimit=5
dataDir=/data/zookeeper/data/
dataLogDir=/data/zookeeper/log/
clientPort=2181
# 3. 运行 Docker 容器，将本地目录挂载到容器内的 zookeeper 配置文件，配置数据和日志目录
docker run --name zookeeper \
    -v /data/zookeeper/zoo.cfg:/conf/zoo.cfg \
    -v /data/zookeeper/data/:/data/zookeeper/data/ \
    -v /data/zookeeper/log/:/data/zookeeper/log/ \
    -p 2181:2181 \
    --restart always -d zookeeper:3.9.3
```

#### 3. Zookeeper在K8s环境中部署

###### 3.1 配置参数

```shell
tickTime=2000
initLimit=5
syncLimit=2
dataDir=/var/lib/zookeeper/data
dataLogDir=/var/lib/zookeeper/log
clientPort=2181
maxClientCnxns=60
autopurge.snapRetainCount=3
autopurge.purgeInterval=24
server.1=zookeeper-1.zookeeper:2888:3888
server.2=zookeeper-2.zookeeper:2888:3888
server.3=zookeeper-3.zookeeper:2888:3888
  
# autopurge.snapRetainCount 决定了要保留的历史快照文件的数量，以防止存储资源耗尽
# autopurge.purgeInterval 决定了多久进行一次清理操作,单位是小时
```

###### 2. 定义 Zookeeper 的 StatefulSet

```shell
---
apiVersion: apps/v1
kind: Deployment
metadata:
  name: zookeeper-1
  namespace: zookeeper
spec:
  replicas: 1
  selector:
    matchLabels:
      app: zookeeper
      server-id: "1"
  template:
    metadata:
      labels:
        app: zookeeper
        server-id: "1"
    spec:
      initContainers:
      - name: init-config
        image: db/busybox:1.37.0
        command: ['sh', '-c', 'echo "1" > /work/myid']
        volumeMounts:
        - name: zookeeper-data
          mountPath: /work
      containers:
      - name: zookeeper
        image: db/zookeeper:3.9.3
        ports:
        - containerPort: 2181
          name: client
        - containerPort: 2888
          name: follower
        - containerPort: 3888
          name: election
        volumeMounts:
        - name: zookeeper-config
          mountPath: /conf
        - name: zookeeper-data
          mountPath: /var/lib/zookeeper/data
        - name: zookeeper-log
          mountPath: /var/lib/zookeeper/log
      volumes:
      - name: zookeeper-config
        configMap:
          name: zookeeper-config
          items:
          - key: zoo.cfg
            path: zoo.cfg
      - name: zookeeper-data
        hostPath:
          path: /data/zookeeper/data-1 # 请根据实际情况修改主机上的存储路径
          type: DirectoryOrCreate
      - name: zookeeper-log
        hostPath:
          path: /data/zookeeper/log-1 # 请根据实际情况修改主机上的存储路径
          type: DirectoryOrCreate
```

###### 3. 定义 Zookeeper 的 StatefulSet

```shell
apiVersion: v1
kind: Service
metadata:
  name: zookeeper-1
  namespace: zookeeper
spec:
  clusterIP: None
  selector:
    app: zookeeper-1
    server-id: "1"
  ports:
  - port: 2888
    name: follower
  - port: 3888
    name: election

---

apiVersion: v1
kind: Service
metadata:
  name: zookeeper-cli
  namespace: zookeeper
spec:
  selector:
    app: zookeeper
  type: NodePort # 使用 NodePort 类型，以便从外部访问
  ports:
  - name: client
    port: 2181
    targetPort: 2181
    nodePort: 32181 # 将 2181 端口映射到外部的 32181 端口
    protocol: TCP
```

###### 4. 部署web可视化工具

```shell
apiVersion: apps/v1
kind: Deployment
metadata:
  name: zoonavigator
  namespace: zookeeper
spec:
  replicas: 1
  selector:
    matchLabels:
      app: zoonavigator
  template:
    metadata:
      labels:
        app: zoonavigator
    spec:
      containers:
      - name: zoonavigator
        image: db/zoonavigator:latest
        ports:
        - containerPort: 9000
        
---

apiVersion: v1
kind: Service
metadata:
  name: zoonavigator-cli
  namespace: zookeeper
spec:
  selector:
    app: zoonavigator
  type: NodePort
  ports:
  - name: client
    port: 32182
    targetPort: 9000
    nodePort: 32182
    protocol: TCP
```

