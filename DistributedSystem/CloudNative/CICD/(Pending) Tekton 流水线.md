<div align=center><font face="黑体" size=6>Tekton 流水线</font></div>

<font COLOR=RED size=3>**Token 内容Pending , 有时间再处理.....**</font>

<font COLOR=RED size=3>**原因：需要处理的任务多，优先级降低**</font>



[toc]



# 1.Tekton 概述

Tekton 是基于声明式 API 的流水线，对象是高度自定义，可扩展的，具有以下优点：

* **可重用：**Tokten 对象可重用性强，组件只需一次定义，即可以被组织任何流水线重用
* **可扩展性：**Tekton 组件目录（Tekton Catatlog）是社区驱动的组件存储仓库，可以直接服用社区的流水线
* **标准化：**Tekton 作为Kubernetes集群的扩展安装和运行，并使用业界公认的资源模型；Tekton 作业以Kubernetes 容器形态执行
* **规模化支持：**只需增加Kubernetes节点，即可增加作业处理能力，可依照集群规模扩充



> **Tekton 术语**

* *Pipline：* 流水线，一个流水线对象由多个Task对象组成
* *Task：* 一个独立运行的任务，如获取代码、编译、或者推送镜像（流水线运行时，Kubernetes 会为每个Task创建一个Pod， 一个Task 由多个Step组成， 每个Step体现为Pod中的一个容器）



# 2.Tekton 安装



# 3. Tekton 应用