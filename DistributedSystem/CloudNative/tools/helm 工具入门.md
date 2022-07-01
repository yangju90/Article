<div align=center><font face="黑体" size=6>Helm 工具指南</font></div>

# 1. 概述

helm 简单介绍：

* Helm chart 是创建应用实例的必要配置组，是一堆Spec
* yaml配置信息被归类为模板（Template）和值（Value），信息经过渲染生成最终的对象
* 所有的配置可以被打包进一个可以发布的对象中
* 一个release就是一个有特定配置的chart实例

# 2. Helm的组件

#### 2.1 Helm client

* 本地chart开发
* 管理repository
* 管理release
* 与helm library交互
  * 发送需要安装chart
  * 请求升级或者卸载存在的release

#### 2.2 Helm library

* 负责与ApiServer交互并提供功能
  * 基于chart 和configuration 创建一个release
  * 把chart 安装进kubernetes，并提供相应的release
  * 升级和卸载
  * Helm 采用Kubernetes 存储所有配置信息，无需自己的数据库



# 3. Helm 使用

* `helm create #{pro_name}` 创建一个helm chart

* `helm repo list` 查看repository

* `helm search repo #{name}` 查看repository 中的chart

* `helm pull gitlab/gitlab  ` 拉取gitlab的chart 资源

* `helm install gitlab ./gitlab/` 安装 gitlab