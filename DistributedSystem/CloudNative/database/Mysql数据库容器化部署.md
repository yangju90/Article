#### 1. Mysql在Docker环境中部署

```shell
# 1. 拉取 MySQL 的 Docker 镜像
docker pull mysql:8.0.40
# 2. 创建本地的 `/data/mysql` 目录用于数据持久化存储
mkdir -p /data/mysql
# 3. 运行 Docker 容器，将本地目录挂载到容器内的 MySQL 数据目录，并设置 MySQL 的配置以允许远程访问
docker run --name mysql-docker \
  -v /data/mysql:/var/lib/mysql \
  -e MYSQL_ROOT_PASSWORD=12345 \
  -p 3306:3306 \
  -p 33060:33060\
  -d mysql:8.0.40
# 4. 配置 MySQL 的用户权限以允许远程连接
docker exec -it mysql-docker mysql -uroot -p12345
# 不能连接，则进入容器输入账号密码 docker exec -it mysql-docker /bin/sh

# 5. 在 MySQL 命令行中，允许远程访问（替换 'your_remote_ip' 为你要允许访问的远程 IP 地址） '%' 代表允许全部
GRANT ALL PRIVILEGES ON *.* TO 'root'@'%' WITH GRANT OPTION;
FLUSH PRIVILEGES;

# 6. 修改权限
CREATE USER 'root'@'192.168.8.9' IDENTIFIED BY '12345';

-- 首先，撤销用户在 @'%' 上的所有权限
REVOKE ALL PRIVILEGES ON *.* FROM 'root'@'%';

-- 然后，使用 ALTER USER 修改用户密码
ALTER USER 'root'@'192.168.8.9' IDENTIFIED BY '12345';

-- 接着，授予用户在指定 IP 地址上的权限
GRANT ALL PRIVILEGES ON *.* TO 'root'@'192.168.8.9' WITH GRANT OPTION;

-- 刷新权限，使修改生效
FLUSH PRIVILEGES;

```

#### 2. Mysql在K8s环境中部署

###### 2.1 创建secret，mysql 密码

```shell
echo -n "12345" | base64

---

apiVersion: v1
kind: Secret
metadata:
  name: mysql-secret
  namespace: mysql-namespace
type: Opaque
data:
  password: MTIzNDU=
```

###### 2.2 创建 HostPath 存储卷和 MySQL 的 Deployment

```yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: mysql
  namespace: mysql-namespace
spec:
  replicas: 1
  selector:
    matchLabels:
      app: mysql
  template:
    metadata:
      labels:
        app: mysql
    spec:
      containers:
      - name: mysql
        image: mysql:8.0
        env:
        - name: MYSQL_ROOT_PASSWORD
          value: "your_password"
        ports:
        - containerPort: 3306
        volumeMounts:
        - name: mysql-storage
          mountPath: /var/lib/mysql
      volumes:
      - name: mysql-storage
        hostPath:
          path: /data/mysql  # 主机上的存储路径，可根据需要修改
          type: DirectoryOrCreate
      nodeSelector:
        kubernetes.io/hostname: server  # 节点选择器，将服务固定在 server 节点
        
        
---
apiVersion: v1
kind: Service
metadata:
  name: mysql-service
  namespace: mysql-namespace
spec:
  selector:
    app: mysql
  ports:
  - protocol: TCP
    port: 3306
    targetPort: 3306
    nodePort: 30360
  type: NodePort
```

###### 2.3 创建加密的 Deployment

```yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: mysql
  namespace: mysql-namespace
spec:
  replicas: 1
  selector:
    matchLabels:
      app: mysql
  template:
    metadata:
      labels:
        app: mysql
    spec:
      containers:
      - name: mysql
        image: mysql:8.0.40
        env:
        - name: MYSQL_ROOT_PASSWORD
          valueFrom:
            secretKeyRef:
              name: mysql-secret
              key: password
        - name: MYSQL_TCP_SSL
          value: "false"
        ports:
        - containerPort: 3306
        volumeMounts:
        - name: mysql-storage
          mountPath: /var/lib/mysql
      volumes:
      - name: mysql-storage
        hostPath:
          path: /data/mysql
          type: DirectoryOrCreate
      nodeSelector:
        kubernetes.io/hostname: server
```

#### 3. CloudBeaver在K8s环境中部署

###### 3.1 DBeaver

```shell
jdbc:mysql://localhost:3306/mydatabase?allowPublicKeyRetrieval=true
```

###### 3.2 配置脚本

```shell
# 1.运行
docker run --name cloudbeaver \
  -v /data/cloudbeaver:/opt/cloudbeaver/workspace \
  -p 8978:8978 \
  -d dbeaver/cloudbeaver:24.3.4
  
# 2.后台常驻模式（Daemon mode）
-d --restart unless-stopped 

# 3.k8s部署
apiVersion: apps/v1
kind: Deployment
metadata:
  name: cloudbeaver
  namespace: mysql-namespace
spec:
  replicas: 1
  selector:
    matchLabels:
      app: cloudbeaver
  template:
    metadata:
      labels:
        app: cloudbeaver
    spec:
      containers:
      - name: cloudbeaver
        image: db/cloudbeaver:24.3.4
        ports:
        - containerPort: 8978
        volumeMounts:
        - name: cloudbeaver-storage
          mountPath: /opt/cloudbeaver/workspace
      volumes:
      - name: cloudbeaver-storage
        hostPath:
          path: /data/cloudbeaver  # 主机上的存储路径，可根据需要修改
          type: DirectoryOrCreate
      nodeSelector:
        kubernetes.io/hostname: server  # 节点选择器，将服务固定在 server 节点
        
        
---
apiVersion: v1
kind: Service
metadata:
  name: cloudbeaver-service
  namespace: mysql-namespace
spec:
  selector:
    app: cloudbeaver
  ports:
  - protocol: TCP
    port: 8978
    targetPort: 8978
    nodePort: 31978
  type: NodePort
```







