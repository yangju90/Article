<div align=center><font face="黑体" size=6>Kubernetes常见问题及解决(一)</font></div>

[toc]



# 1. kubenetes 安装

#### 1.1 kubeadm init 出错

##### 1.1.1 问题

```SHELL
[kubelet-check] It seems like the kubelet isn't running or healthy.
[kubelet-check] The HTTP call equal to 'curl -sSL http://localhost:10248/healthz' failed with error: Get "http://localhost:10248/healthz": dial tcp 127.0.0.1:10248: connect: connection refused.
```

![QA-1](resources\QA-1.png)

##### 1.1.2 原因

安装的是Docker容器，kubelet的cgroup driver是cgroupfs，docker的 cgroup driver是systemd，两者不一致导致kubelet启动失败

##### 1.1.3 解决方法

```shell
sudo vim /etc/docker/daemon.json
{
  "exec-opts": ["native.cgroupdriver=systemd"]
}
systemctl daemon-reload
systemctl restart docker

kubeadm reset
kubeadm init
```





#### 1.2 kubectl get po x509错误

##### 1.2.1 问题

输入kubectl 命令提示 Unable to connect to the server: x509: certificate signed by unknown authority 

##### 1.2.2 原因

kubectl 命令模式是在`$HOME/.kube `  路径下寻找config 文件，config文件为json格式，说明了当前执行命令的用户信息和认证信息

##### 1.2.3 解决方法

再`$HOME/.kube `  路径下添加config 文件，最为简单的是拿取admin权限

```shell
$ mkdir -p $HOME/.kube
$ sudo cp -i /etc/kubernetes/admin.conf $HOME/.kube/config
$ sudo chown $(id -u):$(id -g) $HOME/.kube/config
```



#### 1.3 kubectl get nodes notReady 错误

##### 1.3.1 问题

kubeadm init 成功后，输入kubectl get nodes , node 节点NotReady

![QA-2](resources\QA-2.png)

##### 1.3.2 原因

查询原因：kubectl describe node -n k8s1-virtualbox 

命令查看 node 节点的状态，和Event事件,

![QA-3](resources\QA-3.png)

<font color=red>**network plugin is not ready: cni config uninitialized**</font>

##### 1.3.3 解决方法

安装network plugin， calico  https://projectcalico.docs.tigera.io/getting-started/kubernetes/quickstart

calico 安装-ImagePullBackOff，镜像拉取失败，拉取失败的原因：

- 本地网络不好，拉取失败（国内最长遇见的问题）
- Kubernetes 没有权限去拉那个镜像

具体可以`docker describe pod xxx`查看原因。

(1) 寻找好的替换源后，修改

```yaml
containers:
        - name: tigera-operator
          image: docker.io/marvinyangxian/tigera-operator:v1.25.3   # 修改的源地址
          imagePullPolicy: IfNotPresent  # 拉取策略
```

(2) Kubernetes 权限

参考 https://kubernetes.io/docs/concepts/containers/images/#specifying-imagepullsecrets-on-a-pod

```shell
kubectl create secret docker-registry <name> --docker-server=DOCKER_REGISTRY_SERVER --docker-username=DOCKER_USER --docker-password=DOCKER_PASSWORD --docker-email=DOCKER_EMAIL
```



验证启动：`kubectl get nodes -o wide`



#### 1.4 kubeadm 使用 contianerd 做为cri 

##### 1.4.1 问题

kubernetes 集群使用过程中，替换docker cni ，或者创建集权使用contianerd cni， 会遇到集群创建失败，容器不能正常启动的问题

![QA-4-1](resources\QA-4-1.png)

通过日志创建可以看到 dial tcp 192.168.56.201:6443: connect: connection refused

![QA-4-2](resources\QA-4-2.png)

##### 1.4.2 原因

容器调用失败

##### 1.4.3 解决

参考containd cni 文章

https://kubernetes.io/docs/tasks/administer-cluster/migrating-from-dockershim/change-runtime-containerd/

修改后还是有问题。

参考 https://blog.csdn.net/weixin_40212316/article/details/123321377 运行时更换，也有问题没有成功

![QA-4-3](resources\QA-4-3.png)

再次查看日志，通过系统日志 `journalctl -fu containerd` 发现是，拉取地址问题

![QA-4-4](resources\QA-4-4.png)



**<font color= red size=3>最终解决办法：</font>**

```shell
sudo vim /etc/containerd/config.toml

# 仅仅需要修改 sandbox_image
sandbox_image = "k8s.gcr.io/pause:3.5" 
# 改为
sandbox_image = "registry.aliyuncs.com/google_containers/pause:3.6"

systemctl daemon-reload
systemctl restart containerd
```



#### 1.5 kubectl logs 、exec、port-forward 执行失败问题解决

##### 1.5.1 问题

在`master`节点上执行`kubectl logs、 exec、port-forward`等命令，目标pod在slave节点，出现如下错误：

![QA-5-1](resources\QA-5-1.png)

执行任何pod操作或者查看属于pod的子资源，都会显示`pod`不存在。

##### 1.5.2 原因

原因是因为，虚拟化出了3台 ubuntu 主机搭建的 k8s。在组⽹的过程中，采⽤了双⽹卡VirtualBox⽅案，⽹卡1使⽤NAT地址转换⽤来访问互联⽹，⽹卡2使⽤来实现虚拟机互相访问。⽽ k8s 默认使⽤了⽹卡1的 ip 地址，这就导致了Host-only⼯作节点的 ip 地址使⽤的是⽹卡1的 NAT 地址转换地址(不可以访问其他虚拟机)，从⽽导致的问题的产⽣。



检查确认原因：

执行命令`kubectl logs httpserver-68f9d4cdd6-fml4k -v 9`

![QA-5-2](resources\QA-5-2.png)

从输出图中可以看到，`kubectl`先去访问了本机的`apiserver`来获取⽬标`pod`的信息（红色框1，本机IP为192.168.56.201），返回结果正常，但是进⼀步去请求`pod`的`log`资源的时候就出现了 404 的情况（红框2），仔细查看`pod`的信息时，可以发现它的`hostIP`居然是` NAT `地址转换模式的默认地址
为`10.0.2.15 `!（蓝框） 

##### 1.5.3 解决

因为这个地址是⼯作节点上的`kubelet`⾃动⽣成并发给管理节点的，所以现在我们要登录⼯作节点，然后⼿动指定他的 ip 地址：

在 kubelet 启动时指定 ip，修改⽂件 `/etc/systemd/system/kubelet.service.d/10-kubeadm.conf`

然后添加在`ExecStart`⾏之前新增⼀⾏，内容为：`Environment="KUBELET_EXTRA_ARGS=--node-ip=192.168.56.21"`，注意！这个 ip 地址要填写你当前节点的 ip 地址，这个 ip 是可以正常访问管理节点的⽹卡 ip，如下：

![QA-5-3](resources\QA-5-3.png)



重启kubelet

```shell
systemctl stop kubelet.service
systemctl daemon-reload
systemctl start kubelet.service
```



重启完成后，主节点就可正常访问pod了。

# 2.kubenetes 操作运维

#### 2.1 k8s 批量清楚Evict的Pod的记录

```shell
kubectl -n ${ns}  get pods | grep Evicted |awk '{print$1}'|xargs kubectl -n ${ns} delete pods

kubectl get pods --all-namespaces -o wide | grep Evicted | awk '{print $1,$2}' | xargs -L1 kubectl delete pod -n
```

#### 2.2 K8s 清楚Pod占用的磁盘

```shell
# 查看磁盘占用
sudo du -h --max-depth=1
```

