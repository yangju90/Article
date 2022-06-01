<div align=center><font face="黑体" size=6>Kubernetes Pod</font></div>

[toc]

# 1. Pod 生命周期

#### 1.1 Pod 在kubelet管理下的状态机

![image-1-1](resources\pod-1-1.png)

* *create:* 创建Pod请求发送到ApiServer
* *pending:* ApiServer已经接收到Pod创建请求，但是还为进行调度
* *ContainerCreating:* 调度完成，正在Node节点启动Pod
* *running:* Pod启动完成，正在运行中
* *terminating:* 用户删除Pod指令，Pod正在执行终止命令（进程退出、数据导出、网络关闭等）
* *gone:* Pod退出完毕
* *succeeded:* 所有容器执行成功结束(退出码exit为0)，且restartPolicy: Never
* *Unkown:* 节点出现问题，因为网络等原因，无法获取Pod状态
* *Failed:* 所有容器启动失败，且restartPolicy: Never
* *Evicted:* 节点承压，Pod被Kubelet驱逐

#### 1.2 Pod 状态记录查看

(1) 通过yaml文件查看细节

```shell
kubectl get pod -n default -oyaml

# 查看 yaml 文件， status.conditions 下记录了Pod状态变化关键点历史记录
# pod phase Pod 的当前状态
# status 状态是由 pod phase和status.condition一同计算得出
```

(2) 查看Pod相关事件`kubectl describe pod`

#### 1.3 Pod 的可靠保证

* 避免容器进程被终止，避免Pod被驱逐
  * 合理的resources.memory.limits 防止容器被OOMKill
  * 合理的emptydir.sizeLimit，保证数据不超过emptydir的限制，防止被驱逐
* 污点Taint的Pod调度
* 健康检查的探针

##### 1.3.1  Pod 的 Qos分类

* *Guaranteed：* Limits 和 request 的资源要求一致
* *Burstable：* request 指定最少资源，Limits 设置限制超过多少资源 (**大部分场景适应**)
* *BestEffort：* 未指定cpu或内存资源

```shell
# k8s 驱逐Pod优先级 BestEffort ->  Burstable -> Guaranteed

kubectl get pod <pod_name> | grep qosClass
```

##### 1.3.2 Pod taint 和 toleration

Pod 在Node 节点 NotReady 时也会被kublet驱逐，常见情况，节点临时不可达：

* 节点临时不可达（网络问题、kubelet 、containerd 不工作等）

临时不可达可以增大tolerationSeconds来保证Pod在一定时间后才会被驱逐，toleration 中定义了Pod 对应的驱逐行为。

##### 1.3.3 健康检查探针

探针的类型：

* *livenessProbe：* 探活，检查失败时kubelet会终止该容器进程并按照restartPolicy决定是否重启 
* *readinessProbe：* 就绪状态检查，检查失败时代表进程正在运行，但不能提供服务，Pod状态为NotReady
* *startupProbe：* 在初始化阶段前（Ready之前）的健康检查，通常迎来避免过于频繁的检测影响应用启动



> **探针方法包括**，ExecAction（内部命令结果）， TcpSocketAction（kubelet通过Tcp 检查容器Ip和端口），HttpGetAction（返回码为200-400 时，探活成功）



**额外自定义就绪条件（readinessGates）：** 加入readinessGates，所有探针就绪，Pod还必须检测readinessGates，满足后Pod才为Ready



##### 1.3.4 优雅启动和终止

`run Post-start` 优雅启动， `Pre-stop` 优雅终止



##### 1.3.5 Pod 存储

无状态服务（statless），通常采用emptyDir



##### 1.3.6 应用的配置

* 传统方式
  * Environment Varianles
  * Volume Mount
* k8s ConfigMap Secret
* 网络 Download Api



# 2. Pod 服务发现机制

#### 2.1 服务发现的类型

* *ClusterIP：* 集群内部访问服务
* *NodePort：* 服务器节点访问
* *LoadBalancer：* 负载均衡期的IP地址，转入Service
* *ExternalName：* 定义外部服务资源，通过资源名访问外部地址，基于dns做服务发现处理
* *None：* Headless 类型，明确声明不需要IP， 负载均衡是基于dns做处理

#### 2.2 服务发布

* kube-dns：相当与基于域名的负载均衡器
  * DNS TTL：TTL会告诉客户端域名多长时间有效，不会反复询问，导致域名地址更新缓慢
* kube-proxy：支持iptables/ipvs ，问题时规模有限，基于4层负载均衡，gRpc不支持（Service 服务配置）
* 七层：采用Ingress

> 网络地址转换（Network Address Translation， NAT）通常通过修改数据包源地址或目标地址来控制数据包转发行为。

**负载均衡器**  TCP/IP的四层负载均衡器，链路层负载均衡器（通常负载均衡器和目标服务器IP port一致）

#### 2.3 Service

![image-2-1](resources\pod-2-1.png)

Service 中如果定义了Selector，ControllerManager中的EndPointController会同时创建同名的EndPoint对象，EndPoint 维护了Pod的地址。**（核心工作在kube-proxy）**



![image-2-2](resources\pod-2-2.png)

#### 2.4 服务拓扑

Service引入了topologyKeys属性，可以控制流量。[查询官网，版本对应的设置]



# 3. Pod 网络

<font color=red><b>核心kube-proxy：</b></font>每台机器都会运行一个kube-proxy服务，他监听ApiServer中 service和endpoint的变化，并通过iptables等来为服务器配置负载均衡（仅支持TCP和UDP），支持的几种实现（L4层，基于五元组 IP、Port 和协议）：

* userspace：监听端口，所有服务转发到服务端口，在其内部负载均衡到实际Pod，（用户态性能瓶颈）
* iptables：目前推荐的方式 
* ipvs：iptables的升级版，增量更新，还保证了更新期间连接的保持不断开
* winuserspace：windows上的userspace方式

> Netfilter框架，Linux网络内核框架

![image-3-1](resources\pod-3-1.png)

#### 3.1 iptables 指令

* `iptables -L` 查看iptables配置的调用规则

* `iptables -L -t nat`   查看iptables配置的调用规则，与nat相关的

* `iptables-save -t nat`   将iptables nat相关的规则dump出来，查看

> iptables 存在的问题：
>
> * 规则复杂，不容易阅读，规则太过复杂
> * 负载均衡算法粗暴（全由几率去判断），不够智能，转发效率差
> * iptables 刷新，不能够增量，必须全量替换，这样导致k8s集群规模受限，太多影响效率
> * svc 生成的Ip没有具体设备响应ping不通，必须使用curl 调用（真实的数据包，通过内核使用dnat匹配转换地址）



#### 3.2 ipvs 

`ipvs` 是Netfilter的另外一种工作模式，LVS中间的一部分，两者几乎等同。`Linux virtual server (LVS)`  更多的为负载均衡服务，ipvs 与 iptables hook 点是不同的，ipvs 主要在`LOCAL_IN` `LOCAL_OUT`，添加规则。如下图 iptables和 ipvs ：

![image-3-2](resources\pod-3-2.png)

![image-3-3](resources\pod-3-3.png)

* `ipvs` 不同于`iptables`，没有`PREROUTING`的规则
* `ipvs` 的规则下发点在`LOCAL_IN`和 `LOCAL_OUT`做nat转换， 因此使用`ipvs`需要将ClusterIp 绑定在当前设备的一个Dummy 设备上

##### 3.2.1 iptables 切换 ipvs 模式

* 编辑kube-proxy 的configmap， `kubectl edit configmap kube-proxy -n -kube-system`  set mode："ipvs"
* 删除节点上的kube-proxy pod，重启后会注册虚拟网卡kube-ipvs0
* iptables --flush

##### 3.2.2 ipvs 命令

* 安装ipvsadm
* `ipvsadm -L -n` 查看ipvs nat 转换

`ipvs` 模式会在本地创建一个kube-ipvs0 的虚拟网卡，上面绑定了ClusterIp地址，所以在集群内部可以ping，易读性非常好，且对负载均衡进行了增强（支持权重、轮询等）

#### 3.3 CoreDNS

CoreDNS 包含一个内存态DNS，以及与其他controller类似的控制器。

CoreDNS 的实现原理是，控制器监听Service 和 EndPoint 的变化并配置DNS，客户端Pod在进行域名解析时，从CoreDNS中查询服务对应的地址记录。

> CoreDNS 中的映射信息并不是落盘存储，而是通过启动时CoreDNS中的Controller扫描集群中的Service和EndPoints 得来，域名通用 svc1.ns1.svc.clusterdomain:VIP1 (默认culsterdomain = cluster.local)



##### 3.3.1 Pod 利用CoreDNS 解析域名

* Pod DNS Policy未指定，默认为ClusterFirst， 会创建 /etc/resolv.conf
* resolv 文件会指定nameserver 指向kube-dns

```shell
/etc # cat resolv.conf 
# 短名的补全列表
search default.svc.cluster.local svc.cluster.local cluster.local
# coredns svc 地址
nameserver 10.1.0.10
# 短名配置，dots数为5的情况下认为短名，短名会按照search提供的后缀，进行补全
options ndots:5
```

不同类型服务的DNS记录额，分为 Service、Headless Service 、ExternalName Service

##### 3.3.2 Pod 利用env 解析域名

Pod中的env 查看，可以利用`kubectl exec -it  <pod-name> bin/sh` 进入pod中，env 查看环境变量配置。

Pod中环境变量的Service信息，是在Pod启动时会将namespace下的service以命令的方式写入，这也也存在Service过多pod启动失败，原因是命令过程被截断。

`spec.enableServiceLinks: true/false`控制着是否环境变量加载Service服务域名信息。

##### 3.3.3 自定义DNSPolicy

```yaml
spec: 
	dnsPolicy: "None"
	dnsConfig:
		nameservices:
			- ip
		searchs: 
			- xx.ns1.svc.cluster.local
			- xx.daemon.com
		options:
			- name: ndots
			  values: "2"
```



#### 3.4 Ingress 七层网络

* 基于七层应用层，提供更多功能
* URL/http header 
* L7 path forwarding
* TLS termination （传输安全）

##### 3.4.1 L4 与L7层服务的对比

| L4层服务                                   | L7层服务                                      |
| ------------------------------------------ | --------------------------------------------- |
| 每个Service应用独占ELB(企业负载均衡)       | 多个Service应用可以共享ELB                    |
| 服务创建会频繁更新DNS                      | 服务创建共享一个Domain                        |
| 支持TCP\UDP，启动https服务需要自己管理证书 | TLS termination 发生在Ingress层，可以几种管理 |
|                                            | 更多的高级功能Gateway                         |

##### 3.4.2 Ingress

Kubernetes Ingress Spec 时转发规则的集合，Ingress Controller 来确保 负载均衡配置、边缘路由配置和DNS配置。

> Ingress 相对与L4 层的功能有了一定的丰富，但对与L7层的流量代理来说，还有很多功能的缺乏，例如： 安全（算法）、基于header 的转发规则、rewriting（header rewriting、Uri rewriting）等，都不能支持，Istio 作为Kubernetes 官方Service Provider提供了一种很好的实现。



虚拟路由与硬件路由直接可以通过BGP协议，完成路由信息的同步。
