<div align=center><font face="黑体" size=6>Kubernetes平台组件</font></div>

[TOC]

# 1. Kube-scheduler

# 2. Controller Manager

# 3. Kubelet

# 4. CRI

# 5. CNI

# 6. CSI

#### 6.1 概览

CSI 是 kubernetes 集群容器运行时存储插件，早期Docker主要采用Device Mapper作为运行存储，目前Docker 和contianerd 都默认采用OverlayFs，性能更加优秀。

#### 6.2 CSI 组件

CSI 通过Rpc与存储驱动进行交互，主要有两个和相关模块：

* kube-controller-manager (create Volume)
  * Kube-controller-manager模块用于感知CSI驱动的存在
  * kubernetes的主控模块通过Unix domain socket（不是CSI驱动）或着其他方式进行直接的交互
  * Kubernetes的主控模块只与Kubernetes相关的API交互
  * 因此CSI驱动如有依赖Kubernetes API的操作，例如**卷的创建、卷的attach、卷的快照**等，需要在CSI驱动里面通过Kubernetes的API，来出发相关的CSI操作。
* kubelet （mount Volume）
  * Kubelet 模块用于与CSI驱动进行交互 
  * Kubelet 通过Unix domain socket 向CSI驱动发起调用（如NodeStageVolume、NodePublishVolume等），再发起**mount卷和unmount卷**
  * kubelet通过插件注册机制发现CSI驱动以及用于和驱动交互的Unix domain socket 
  * 所有部署在kubernetes集群中的CSI驱动都要通过Kubelet的插件机制来注册自己

#### 6.3 容器存储操作

##### 6.3.1 临时存储 emptyDir 

当Pod从节点删除时，emptyDir卷中的数据也会被永久删除。但当Pod的容器因为默写原因退出再重启时，emptyDir卷内的数据并不会丢失。**emptyDir可以是本地磁盘或网络存储**

* emptyDir 设计主要是给应用充当缓存空间，或者存储的中间数据，用于快速恢复。

##### 6.3.2 emptyDir 使用

```yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: nginx-deployment
spec:
  replicas: 1
  selector:
    matchLabels:
      app: nginx
  template:
    metadata:
      labels:
        app: nginx
    spec:
      containers:
        - name: nginx
          image: nginx
          # volumeMounts mount 到pod的 cache目录
          volumeMounts:
          - mountPath: /cache
            name: cache-volume
      # volume 声明
      volumes:
      - name: cache-volume
        emptyDir: {}
```



##### 6.3.3 半持久化存储 hostPath

hostPath 是将Pod中的存储mount到主机目录中，同docker -v mount , 不会自动删除

hostPath 可以指定卷类型，支持包换目录、字符设备、块设备等。

> hostPath使用要注意数据磁盘管理，一般不使用

##### 6.3.4 Local PVC hostPath 使用

* 创建文件夹作为挂载目录 `sudo mkdir /mnt/data`

* 创建html `sudo sh -c "echo 'Hello from Kubernetes Local storage' > /mnt/data/index.html"`

* create pv

  ```yaml
  apiVersion: v1
  kind: PersistentVolume
  metadata:
    name: task-pv-volume
    labels:
      type: local
  spec:
    storageClassName: manual
    capacity:
      storage: 100Mi
    accessModes:
      - ReadWriteOnce
    hostPath:
      path: "/mnt/data"
  ```

* create pvc

  ```yaml
  apiVersion: v1
  kind: PersistentVolumeClaim
  metadata:
    name: task-pv-claim
  spec:
    storageClassName: manual
    # ReadWriteOnce (只能在一个节点被Mount) | ReadOnlyMany | ReadWriteMany (可在不同节点被Mount)
    accessModes:
      - ReadWriteOnce
    resources:
      requests:
        storage: 100Mi
  ```

* create pod 创建nignx可以访问index.html

  ```yaml
  apiVersion: v1
  kind: Pod
  metadata:
    name: task-pv-pod
  spec:
    volumes:
      - name: task-pv-storage
        persistentVolumeClaim:
          claimName: task-pv-claim
    containers:
      - name: task-pv-container
        image: nginx
        ports:
          - containerPort: 80
            name: "http-server"
        volumeMounts:
          - mountPath: "/usr/share/nginx/html"
            name: task-pv-storage
  ```

##### 6.3.5 持久化存储 PVC

持久化存储引入了很多概念 StorageClass、Volume、PVC（Persistent Volume Claim）、PV（Persitent Volume），将存储独立于Pod 的声明周期来管理，大致分为网络存储和本地存储两种类型。

* *StorageClass：* 用来指示存储类型，主要包括存储插件provisioner、卷的创建和mount等字段
* *PVC：* 用户对存储需求的声明
* *PV：*真实创建的存储空间

##### 6.3.6 Dynamic Local PVC 使用





# 7. Rook

