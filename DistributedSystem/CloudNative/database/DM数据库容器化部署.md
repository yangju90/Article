#### 1. DM在Docker环境中部署

```shell
# 1. 拉取 MySQL 的 Docker 镜像
docker pull sizx/dm8:1-2-128-22.08.04-166351-20005-CTM
# 2. 创建本地的 `/data/dm8` 目录用于数据持久化存储
mkdir -p /data/dm8
# 3. 运行 Docker 容器，将本地目录挂载到容器内的 DM 数据目录
docker run -d -p 5236:5236 --name dm8 \
--restart=always --privileged=true \
-e PAGE_SIZE=16 \
-e UNICODE_FLAG=1 \
-e LENGTH_IN_CHAR=1 \
-e CASE_SENSITIVE=0 \
-e SYSDBA_PWD='123abc!@#' \
-e LD_LIBRARY_PATH=/opt/dmdbms/bin \
-e INSTANCE_NAME=dm8_instance \
-v /data/dm8:/opt/dmdbms/data \
sizx/dm8:1-2-128-22.08.04-166351-20005-CTM && docker logs -f dm8
```

#### 2. DM在K8s环境中部署

###### 2.1 创建secret，mysql 密码

```shell
echo -n '123abc!@#' | base64

---
apiVersion: v1
kind: Namespace
metadata:
  name: dm-namespace
  
  
---
apiVersion: v1
kind: Secret
metadata:
  name: dm-secret
  namespace: dm-namespace
type: Opaque
data:
  password: MTIzYWJjIUAj
```

###### 2.2 创建 HostPath 存储卷和 MySQL 的 Deployment

```yaml

---
apiVersion: v1
kind: Service
metadata:
  name: dm-service
  namespace: dm-namespace
spec:
  selector:
    app: dm8
  ports:
  - protocol: TCP
    port: 32360
    targetPort: 5236
    nodePort: 32360
  type: NodePort
```

###### 2.3 创建加密的 Deployment

```yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: dm8
  namespace: dm-namespace
spec:
  replicas: 1
  selector:
    matchLabels:
      app: dm8
  template:
    metadata:
      labels:
        app: dm8
    spec:
      containers:
      - name: dm8
        image: sizx/dm8:1-2-128-22.08.04-166351-20005-CTM
        env:
        - name: SYSDBA_PWD
          valueFrom:
            secretKeyRef:
              name: dm-secret
              key: password
        - name: INSTANCE_NAME
          value: "dm8_instance"
        - name: LD_LIBRARY_PATH
          value: "/opt/dmdbms/bin"
        - name: CASE_SENSITIVE
          value: "0"
        - name: LENGTH_IN_CHAR
          value: "1"
        - name: UNICODE_FLAG
          value: "1"
        - name: PAGE_SIZE
          value: "16"
        ports:
        - containerPort: 5236
        volumeMounts:
        - name: dm8-storage
          mountPath: /opt/dmdbms/data
      volumes:
      - name: dm8-storage
        hostPath:
          path: /data/dm8
          type: DirectoryOrCreate
      nodeSelector:
        kubernetes.io/hostname: server
```
