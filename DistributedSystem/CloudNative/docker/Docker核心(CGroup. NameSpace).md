<div align=center><font face="黑体" size=6>Docker核心(Cgroup.Namespace)</font></div>

> docker核心Linux相关知识

* Docker核心Cgroup
* Docker核心Namespace
  * namespace 概述
  * namespace 作用
  * namespace 命令
* Docker核心网络



# Cgroup

#### 1. 概述

cgroups全称 Control Group 是linux系统内核中资源控制的工具， 可以对CPU、内存、磁盘I/O、网络等进程所需的资源量进行限制。



# Namespace

#### 1. 概述



#### 2. 作用

Namespace 是一种隔离机制， 主要目的是隔离运行在同一个宿主机上的容器，让这些容器之间不能互访问彼此的资源。主要有两个作用：

* 充分利用系统的资源，可以在同一台宿主机上运行多个容器
* 保证了安全性，不同容器之间不能互相访问


|       分类        |               隔离内容               |
| :---------------: | :----------------------------------: |
|  Mount namespace  |             Mount points             |
|   UTS namespace   |     Hostname and NIS domain name     |
|   IPC namespace   |  System V IPC, POSIX message queues  |
|   PID namespace   |              Process ID              |
| Network namespace | Network devices, stacks, ports, etc. |
|  User namespace   |          User and group ID           |


#### 3. 命令操作

##### 3.1 基本命令

```shell
# 查看linux系统当前存在的namespace 
lsns -t <type>   # 查看对应类型的namespace (net mnt ipc pid uts user)
```



# 网络

#### 1. 单主机网络模式

* Null （--net=None）  
  * 把容放入独立的网络空间但不做任何网络配置 （创建namespace ，但不配置）
  * 用户需要通过独立运行docker network命令来完成网络配置
* Host
  * 使用主机网络名空间，复用主机网络
* Container
  * 重用其他容器网络 （多个进程采用容器部署之间的通信方案，a. 多个进程放入同一个容器中 b. 多个容器运行过程中共享同一个namespace）
* Bridge （--net=bridge）在启动容器时不添加网络参数时，默认的模式
  * 使用Linux网桥和iptables提供容器互联，Docker在每台主机上创建一个名叫docker0的网桥，通过veth pair来链接该主机的每一个EndPoint

#### 2. 跨主机网络模式

* Overlay技术 (libnetwork, libkv)
  * 通过网络封包实现 —— 通过封包解包实现
* Underlay
  * 使用现有底层网络，为每一个容器配置可路由的网络IP (容器与主机网络共用，所以要有合理的网络规划 )
