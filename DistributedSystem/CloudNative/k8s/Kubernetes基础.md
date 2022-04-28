[toc]



###### 备注：

```bash
br_netfilter  # linux 透明防火墙

apt-key  # 存储apt 密钥
apt-mark hold/unhold # 系统更新时 apt-update，是否更新软件包 hold不更新 unhold 取消hold
apt-cache madison <<package name>> # 查看软件版本
```

透明防火墙(Transparent Firewall)又称[桥接](https://so.csdn.net/so/search?q=桥接&spm=1001.2101.3001.7020)模式防火墙（Bridge Firewall）。简单来说，就是在网桥设备上加入防火墙功能。透明防火墙具有部署能力强、隐蔽性好、安全性高的优点。

 journalctl -f -u kubelet



# 1. kubernetes 概述



# 2. Kubernetes 对象

kubernetes 声明式系统，所有的管理能力都构建在对象的基础上，核心对象包括：

* Node ： 计算节点抽象，用来描述计算节点的资源抽象、健康状态
* NameSpace： 资源隔离的基本单位
* Pod： 用来描述应用实例，包括镜像地址、资源需求等
* Service：服务如何将应用发布成服务，本质上是负载均衡和域名服务的声明



#### 2.1 对象属性

* TypeMeta
* MetaData
* Spec： 对象定义核心属性，是用户的期望状态，由创建对象的用户端定义
* Status： 是对象运行的实际状态，由对应的控制器收集状态并更新

> TypeMeta 和 MetaData 是通用属性，Spec 和 Status 是每个对象独有的



# 3. Kubernetes 安装

安装kubernetes参考官方文档：

https://kubernetes.io/docs/setup/production-environment/tools/kubeadm/install-kubeadm/



安装完成后验证：`kubectl get pod -owide`



# 4. Kubernetes 命令

#### 4.1 Kubernets 架构图



#### 4.2 Kubernetes 命令

##### 4.2.1 kubectl 工具常用命令

- `kubectl describe` kubctl 最终要的工具，查看目标状态及Event
- `kubeclt delete ` kubectl 重要的删除命令
- `kubectl get node -oyaml <node-name> ` 查看node-name节点的信息
- `kubectl get pod -owide` 查看pod信息，以wide形式展示
- `kubectl get pod -l component=etcd -n kube-system` 节点过滤

* `kubectl get ns <ns-name> -oyaml` 查看namespace
* `kubectl logs -f <pod-name>` 查看pod日志

##### 4.2.2 kubectl 创建pod

* `kubectl run --image=nginx nginx ` 简单命令创建

* kubectl 执行 kind 为deployment 文件

  * `kubectl create -f xx.yaml`
  * `kubectl apply -f xx.yaml`

  ```shell
  # 两个命令都可以创建一个deployment，生成相应数量的pod
  
  # 区别
  # kubectl create：
  #   (1)kubectl create命令，是先删除所有现有的东西，重新根据yaml文件生成新的。所以要求yaml文件中的配置必须是完整的
  #   (2)kubectl create命令，用同一个yaml 文件执行替换replace命令，将会不成功，fail掉。
  
  # kubectl apply：
  #   kubectl apply命令，根据配置文件里面列出来的内容，升级现有的。所以yaml文件的内容可以只写需要升级的属性
  ```

* 

##### 4.2.3 kubectl 删除pod

```shell
# 查看pod 列表
kubectl get pod -A | grep <podname>

# 直接使用kubectl delete命令时会失败，因为K8s会误认pod挂掉，而重新拉起，所以应该先删除deployment信息
# 查看deployment信息
kubectl get deployment -n <namespace>

# 删除deployment信息
kubectl delete deployment <deployment名> -n <namespace>

# 删除pod
kubectl delete pod <podname> -n <namespace>

# yaml 删除
kubectl delete -f xxx.yaml

```



##### 4.2.4 kubectl 查看pod

* `kubectl -n kube-system  exec  <pod-name> -it /bin/sh`  进入pod容器内



# 5. kubernetes 应用

#### 5.1 通过Pod对象支撑应用运行

##### 5.1.1 环境变量获取

* 直接设置
* 读取 Pod Spec 默写属性值
* 通过 ConfigMap 读取某个值
* 从 Secret 读取某个值



#### 5.2 ConfigMap 使用

* `kubectl create configmap <config-name> --from-file=<file-name>`创建configmap 通过文件

* `kubectl create configmap <config-name> --from-env-file=<file-name>` 

* `kubectl create configmap <config-name> --from-literal=<key>=<value>` 

  ```shell
  # from-file 和 from-env-file 区别
  # from-file 会将文件名当作key 内容当作value加入configmap 中
  # from-env-file 会将文件内容中的k-v 对放入configmap中，同时会忽略配置中注释（#）掉的内容
  ```

* `kubectl get configmap/<config-name> -oyaml`



#### 5.3 Secret

Secret 使用来保存和传递密码、密钥、认证凭证这些敏感信息的对象（同ConfigMap）可以避免把敏感信息明文写在配置文件中， 在etcd中也是加密的。

#### 5.4 ServiceAccount

kubenetes集群中运行的Pod提供账户标识， 和namespace相关







# 6. Service

#### 6.1 Service命令

* `kubectl expose pod <pod-name> --port <port>`  将一个pod发布成为service
* `kubectl get svc`  查看Serivce list

```yaml
apiVersion: v1
kind: Service
metadata:
  creationTimestamp: "2022-03-28T12:27:52Z"
  labels:      
    run: nginx  
  name: nginx
  namespace: default
  resourceVersion: "130435"
  uid: a32f4e02-0594-40b2-a27a-db46ed7b4351
spec:
  clusterIP: 10.104.66.180
  clusterIPs:
  - 10.104.66.180
  internalTrafficPolicy: Cluster
  ipFamilies:
  - IPv4
  ipFamilyPolicy: SingleStack
  ports:
  - port: 80
    protocol: TCP
    targetPort: 80
  selector:         # 集群选择过滤
    run: nginx      # label
  sessionAffinity: None
  type: ClusterIP
status:
  loadBalancer: {}
```





#### 6.2 集群对象

* Replica Set
* Deployment：Deployment 创建无状态Pod，副本
* StatefulSet：statefulset 创建有状态Pod，副本（例如：Zookeeper）
*  Job ：短时任务支撑
* DaemonSet：每个Node节点都会存在的Pod
* 存储PV、PVC
* CustomResourceDefinition （CRD）: kubernetes 扩展自定义扩展



**<font size=3 color=red>StatefulSet 和 Deployment 的差异 </font>**

* 身份标识: StatefulSet Controller 为每个Pod编号，从0开始，表示唯一身份，Deployment pod没有区别
* 数据存储: StatefulSet 允许用户定义volumeClaimTemplates
* 升级策略：Deployment 滚动升级
  * onDelete  （只有删除老的才会升级新的版本）
  * 滚动升级
  * 分片升级

