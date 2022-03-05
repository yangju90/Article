<div align=center><font face="黑体" size=6>Docke核心(Cgroup.Namespace)</font></div>

> docker核心Linux相关知识

* Docker核心Cgroup
* Docker核心Namespace
  * namespace 概述
  * namespace 作用
  * namespace 命令



# Cgroup

#### 1. 概述

cgroups全称 Control Group 是linux系统内核中资源控制的工具， 可以对CPU、内存、磁盘I/O、网络等进程所需的资源进行限制。





#### 

# Namespace

#### 1. 概述



#### 2. 作用

#### 3. 命令操作

##### 3.1 基本命令

```shell
# 查看linux系统当前存在的namespace 
lsns -t <type>   # 查看对应类型的namespace (net mnt ipc pid uts user)
```



