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

##### 2.1.2 Harbor可视化使用





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



