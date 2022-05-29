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



# 3. Pod 网络调用关系

