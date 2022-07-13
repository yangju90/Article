<div align=center><font face="黑体" size=6>镜像仓库 Harbor</font></div>

[toc]



# 1. 镜像仓库

镜像仓库（Docker Registry）负责存储、管理和分发镜像。

同一镜像仓库管理多个Repository，Repository 通过命名来区分。每个Repository 包含一个或多个镜像，镜像通过镜像名称和签名（Tag）来区分。

客户端拉取镜像时，要指定三要素：

* 镜像仓库：要从哪一个镜像仓库拉取镜像，通常通过DNS或者IP地址来确定镜像仓库
* Repository： 子知名
* 镜像名称+Tag

![harbor-1.1](resources\harbor-1.1.png)

**镜像仓库OCI 分发规范**

![harbor-1.2](resources\harbor-1.2.png)

# 2. Harbor 

Harbor 拥有完整的仓库管理，镜像管理、基于角色的权限控制、镜像安全扫描集成、镜像签名等。Harbor 支持级联和主从模式。

![harbor-2.1](resources\harbor-2.1.png)

* *Registry：* push/pull  镜像
* *Replication Job Service：* 支持Harbor 镜像仓库复制



#### 2.1 Harbor 搭建

##### 2.1.1 通过Helm安装Harbor

> helm 工具使用查看 kubernetes tools 章节

1.添加harbor的helm配置，拉取harbor配置

```shell
helm repo add harbor https://helm.goharbor.io
helm fetch harbor/harbor --untar
kubectl create ns harbor
```

2.修改values配置文件

`vim harbor/values.yaml`

```yaml
# 修改为nodePort类型对外暴漏
expose:
  type: nodePort
# 域名  
tls:
  commonName: 'core.harbor.domain'

persistence: false
```

3.安装

`helm install harbor ./harbor -n harbor`

##### 2.1.2 Harbor Helm持久化

1.添加hostpath

```yaml
apiVersion: v1
kind: PersistentVolume
metadata:
  name: registry-pv-volume
  labels:
    type: local
spec:
  storageClassName: registry-manual
  capacity:
    storage: 10Gi
  accessModes:
    - ReadWriteMany
  hostPath:
    path: "/mnt/data/registry"
---    
apiVersion: v1
kind: PersistentVolume
metadata:
  name: chartmuseum-pv-volume
  labels:
    type: local
spec:
  storageClassName: chartmuseum-manual
  capacity:
    storage: 10Gi
  accessModes:
    - ReadWriteMany
  hostPath:
    path: "/mnt/data/chartmuseum"
---    
apiVersion: v1
kind: PersistentVolume
metadata:
  name: jobservice-pv-volume
  labels:
    type: local
spec:
  storageClassName: jobservice-manual
  capacity:
    storage: 2Gi
  accessModes:
    - ReadWriteMany
  hostPath:
    path: "/mnt/data/jobservice"
---    
apiVersion: v1
kind: PersistentVolume
metadata:
  name: database-pv-volume
  labels:
    type: local
spec:
  storageClassName: database-manual
  capacity:
    storage: 2Gi
  accessModes:
    - ReadWriteMany
  hostPath:
    path: "/mnt/data/database"
---
apiVersion: v1
kind: PersistentVolume
metadata:
  name: redis-pv-volume
  labels:
    type: local
spec:
  storageClassName: redis-manual
  capacity:
    storage: 2Gi
  accessModes:
    - ReadWriteMany
  hostPath:
    path: "/mnt/data/redis"
---    
apiVersion: v1
kind: PersistentVolume
metadata:
  name: trivy-pv-volume
  labels:
    type: local
spec:
  storageClassName: trivy-manual
  capacity:
    storage: 10Gi
  accessModes:
    - ReadWriteMany
  hostPath:
    path: "/mnt/data/trivy"
```



2.values.yaml 文件修改

```yaml
# 持久化存储配置部分
persistence:
  enabled: true   # 开启持久化存储
  resourcePolicy: "keep"
  persistentVolumeClaim:        # 定义Harbor各个组件的PVC持久卷部分
    registry:          # registry组件（持久卷）配置部分
      existingClaim: ""
    storageClass: "registry-manual"           # 前面创建的StorageClass，其它组件同样配置
      subPath: ""
      accessMode: ReadWriteMany          # 卷的访问模式，需要修改为ReadWriteMany，允许多个组件读写，否则有的组件无法读取其它组件的数据
      size: 10Gi
    chartmuseum:     # chartmuseum组件（持久卷）配置部分
      existingClaim: ""
      storageClass: "chartmuseum-manual"
      subPath: ""
      accessMode: ReadWriteMany
      size: 10Gi
    jobservice:    # 异步任务组件（持久卷）配置部分
      existingClaim: ""
      storageClass: "jobservice-manual"    #修改，同上
      subPath: ""
      accessMode: ReadWriteOnce
      size: 2Gi
    database:        # PostgreSQl数据库组件（持久卷）配置部分
      existingClaim: ""
      storageClass: "database-manual"
      subPath: ""
      accessMode: ReadWriteMany
      size: 2Gi
    redis:    # Redis缓存组件（持久卷）配置部分
      existingClaim: ""
      storageClass: "redis-manual"
      subPath: ""
      accessMode: ReadWriteMany
      size: 2Gi
    trivy:         # Trity漏洞扫描插件（持久卷）配置部分
      existingClaim: ""
      storageClass: "trivy-manual"
      subPath: ""
      accessMode: ReadWriteMany
      size: 10Gi
```



##### 2.1.3 Harbor可视化使用

1.登录Harbor页面

网址：`https://192.168.56.201:30003`

账号：`admin/Harbor12345`

![harbor-2.2](resources\harbor-2.2.png)

2.配置hosts

配置`harbor NodePort ClusterIP`到hosts文件中`vim /etc/hosts`

```shell
10.1.83.18 core.harbor.domain

# curl 有html数据返回
curl https://core.harbor.domain -k 
```

3.安装nerdctl

`curl -OL https://github.com/containerd/nerdctl/releases/download/v0.21.0/nerdctl-0.21.0-linux-amd64.tar.gz   `

> 当出现 <font color=red>curl: (56) OpenSSL SSL_read: Connection reset by peer, errno 104</font>  注意添加-L

```shell
mkdir nerd
sudo tar -zxvf nerdctl-0.21.0-linux-amd64.tar.gz -C /usr/local/bin
```

4.Containerd配置ca.crt

从harbor中下载ca.crt：`https://192.168.34.2:30003/harbor/projects/1/repositories`

参考contianerd文档：https://github.com/containerd/containerd/blob/main/docs/hosts.md

```shell
mkdir /etc/containerd/certs.d/core.harbor.domain
copy the ca.crt to this folder
systemctl restart containerd
```

~~修改containerd的配置文件config.toml~~ (<font color=red>注：1.5.9 版本不能添加certs的config_path， 否则会出现 Kubelet不能启动问题，这个步骤可以省略</font>)

错误信息：<font color=red>`"Version from runtime service failed" err="rpc error: code = Unimplemented desc = unknown service runtime.v1alpha2.RuntimeService"`</font>

```shell
version = 2

[plugins."io.containerd.grpc.v1.cri".registry]
   config_path = "/etc/containerd/certs.d"
```

重启contianerd， `systemctl restart containderd` 

5.推送Image到Harbor

```shell
sudo nerdctl login -u admin -p Harbor12345 core.harbor.domain

# 提示错误failed to call rh.Client.Do: Get "https://core.harbor.domain/v2/": x509: certificate signed by unknown authority 
# 返回第4步配置ca.crt
```

推送镜像：

```shell
sudo nerdctl images --namespace k8s.io

# 创建镜像Tag
sudo nerdctl tag xxx core.harbor.domain/library/httpserver:v0.1.0 --namespace k8s.io

# 推送镜像到harbor
sudo nerdctl push core.harbor.domain/library/httpserver:v0.1.0 --namespace k8s.io
```

Harbor 的搭建至此完成，可以通过core.harbor.domain 下载镜像

# 3. 镜像安全

#### 3.1 镜像构建安全

* 构建指令问题：构建指令中添加了密钥、Token等敏感信息
* 应用依赖问题：依赖工具不安全
* 文件问题：镜像构建中存在不安全的文件

#### 3.2 镜像扫描

镜像扫描通过扫描工具或者扫描服务队镜像进行扫描，来确定镜像是否安全。

* 指令、应用、文件、依赖包扫描
* 查询比对CVE库，安全策略是否存在
* 检测镜像是否合规，镜像安全

#### 3.3 镜像准入策略

通过kubernetes控制平面的准入控制器进行pod的准入

#### 3.4 镜像扫描服务

镜像扫描服务，开源仅有Anchore和Clair，Clair对Harbor支持较完善。

![harbor-3.1](resources\harbor-3.1.png)

##### 3.4.1 Clair 启用

// todo

##### 3.4.2 Trivy启用

Harbor helm 安装会默认有Trivy，启用 Trivy 前台仓库配置即可。

![harbor-3.2](resources\harbor-3.2.png)
