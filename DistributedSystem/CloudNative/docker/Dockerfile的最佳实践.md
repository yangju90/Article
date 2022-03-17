<div align=center><font face="黑体" size=6>Dockerfile最佳实践</font></div>



#### 1. Docker 构建上下文

* 当运行docker build 命令时，当前工作目录被称为构建上下文

* docker build 默认查找当前目录的Dockerfile作为构建输入，也可以通过`-f`指定Dockerfile

  * docker build -f ./Dockerfile

* 当docker build 运行时，首先会把构建上下文传输给docker daemon，把没用的文件包含在构建上下文时，会导致传输时间长，构建需要的资源多，构建出的镜像大的问题

  * 包含很多文件的目录运行命令

  ```shell
  # 时间很长，需要拷贝扫描当前目录上下文的文件
  docker build -f $GOPATH/src/init.mat.com/cncamp/golang/httpserver/Dockerfile .
  
  # 仅扫描httpserver下的文件传输创建镜像
  docker build $GOPATH/src/init.mat.com/cncamp/golang/httpserver/
  
  #可以通过.dockerignore文件从编译上下文排除某些文件
  
  # 所以一般在项目中，需要确保上下文清晰，会创建一个专门的目录放置Dockerfile，并在目录中运行Docker build
  ```



#### 2. 构建缓存 (Build Cache)

构建容器镜像时， Docker 依次读取Dockerfile 中的指令，并按顺序依次执行构建指令。

Docker 读取指令后，会先判断缓存中是否有可用已存在的镜像，只有已存镜像不存在时才会重新构建。

* 通常Dokcer简单判断Dockerfile 中的指令与镜像
* 针对ADD和COPY指令，Docker判断该镜像层每一个文件的内容并生成一个checksum， 与现存的镜像比较时，Docker 比较的二者的checksum
* 其他指令，比如`RUN apt-get -y update`， Docker 简单比较与现存镜像中的指令字符串是否一致
* 当某一层cache失效以后，所有上层级的cache均一并失效，后续指令都重新构建镜像



##### 3. 多段构建（Multi-stage build）

```dockerfile
# 第一段构建，创建基础环境下载源码
FROM golang:1.16-alpine AS build
RUN apk add --no-cache git
RUN go get github.com/golang/dep/cmd/dep

COPY Gopkg.lock Gopkg.toml /go/src/project/
WORKDIR /go/src/project/
RUN dep ensure -vendor-only

COPY ./go/src/project
RUN go build -o /bin/project

# 第二段构建，只将编译好的可以执行文件COPY到scratch空镜像中 (scratch中没有任何工具)
FROM scratch
COPY --from=build /bin/project /bin/project
ENTRYPOINT ["/bin/project"]
CMD ["--help"]
```

> 常用指令
>
> * FROM 选择基础镜像，推荐alpine (ubuntu这种完备的操作系统会比较大，工具多，相应的安全问题也会多，升级维护麻烦)
>
>   * FROM [--platform=<platform>] <image>[@<digest>] [AS <name>]
>   * 一般除了ARG 变量参数之另外，dockerfile必须以FROM开头
>
>   ```dockerfile
>   ARG NG_VERSION=1.19.3
>   FROM nginx:${NG_VERSION}
>   CMD /bin/bash
>   ```
>
>   * 通过向FROM指令添加AS name，可以选择为新生成阶段指定名称。该名称可以在后续的`FROM`和`COPY --FROM=<name>`指令中使用，以引用在此阶段中构建的镜像
>   * 中文参考链接 `https://blog.csdn.net/securitit/article/details/109503940`
>
> * LABLES 按标签组织项目
>
>   * LABEL multi.label1="value1" multi.label2="value2" other="value3"
>   * 配合 label filter 可过滤镜像查询结果
>   * docker images -f label=multi.label1="value1" 查询
>
> * RUN 执行命令 
>
>   * 最常用的方法 `RUN apt-get update && apt-get install` , 两条命令应该永远用&&连接，如果分开执行，RUN apt-get update 构建层被缓存，构建镜像长时间后运行，可能会导致新的package无法安装（因为update缓存的apt-get 不是最新的）
>
> * CMD 容器镜像中应用的运行命令, 在容器运行时，不能够直接追加参数
>
>   * CMD ["executable", "param1", "param2"]
>
>     ```shell
>     例如:
>     CMD ["ls", "-a"]  在docker运行时不能够直接使用docker run <image> <args> 
>     必须 docker run <image> <command> <args> --- docker run <image> ls -al
>     
>     ENTRYPOINT["ls", "-a"]  在docker运行时可以直接追加参数docker run <image> <args> --- docker run <image> -l
>     ```
>
>     
>
> * EXPOSE 发布端口
>
>   * EXPOSE <port> [<port>/<protocol>]
>   * 是镜像和使用者间的约定， 在docker run -P 时，docker会自动expose端口到主机大端口，主机端口时随机的， 如0.0.0.0:32769 ->80/tcp（EXPOSE 80, -P 运行docker会随机分配一个主机端口与expose端口link）
>   * 作用不是很大，很多程序的默认端口会自动expose出去，更多的是约定和声明
>   * docker run -p 也可以指定端口映射
>   * 参考文章 https://blog.csdn.net/weixin_43944305/article/details/103116557
>
> * ENV 这是环境变量
>
>   * ENV <key>=<value>
>
> * ADD 从源地址（文件，目录或者URL）复制文件到目标路径
>
>   * ADD [--chown=<user>:<group>] <src>.. <dest> 修改权限
>   * ADD [--chown=<user>:<group>] ["<src>",.. "<dest>"] 路径中有空格时使用
>   * src 是本地压缩文件，则ADD的同时会完整解压操作
>   * ADD支持Go风格的通配符 如： ADD check* /testdir/
>   * dest不存在，则会创建
>   * 应尽量减少通过ADD URL添加remote文件，建议使用curl 或者wget、untar等成熟工具会比较好
>
> * COPY 从源地址（文件，目录）复制文件到目标路径
>
>   * COPY [--chown=<user>:<group>] <src>.. <dest> 修改权限
>   * COPY [--chown=<user>:<group>] ["<src>",.. "<dest>"] 路径中有空格时使用
>   * COPY 仅支持本地复制不支持URL
>   * 可用于多段构建编译场景，可以用前一个临时镜像中拷贝文件
>     * COPY --from=build /bin/project /bin/project
>
> * ENTRYPOINT 定义可以执行的容器镜像入口命令
>
>   * ENTRYPOINT ["executable", "param1", "param2"]
>
>   * ENTRYPOINT command param1 param2  // docker run模式使用
>
>   * docker run --entrypoint 可以替换Dockerfile中定义的ENTRYPOINT
>
>   * ENTRYPOINT 的最佳实践是用ENTRYPOINT 定义镜像主命令，并通过CMD 定义主要参数
>
>     ```dockerfile
>     ENTRYPOINT ["s3c"]
>     CMD ["--help"]
>     ```
>
> * VOLUME 定义目录为外挂存储卷，Dockerfile 中的指令在该指令后对同一目录的修改都无效
>
>   ```dockerfile
>   # VOLUME 不推荐使用， docker 对存储卷的管理很弱
>   VOLUME ["/data"]
>   等价于 docker run -v /data ,可通过docker inspect 查看主机的mount point.
>   /var/lib/docker/volumes/<containerid>/_data
>   ```
>
> * USER 切换运行镜像的用户和用户组，因安全行要求，越来越多的场景要求容易应用要以non-root身份运行
>   * USER <user>[:<group>]
>   
> * WORKDIR 等价cd 切换工作目录
>
> * ARG
>
> * ONBUILD
>
> * STOPSIGNAL
>
> * HEALTHCHECK
>
> * SHELL

#### 4. dockerfile 最佳实践

* 不要安装无效软件报
* 镜像中尽可能少的进程数
* 无法避免多进程时，应合初始化进程 （** tini 项目 解决方案**）
* 尽可能少的层数

目标： 易管理、漏洞少、镜像小、层级少、利用缓存加速



```dockerfile
docker save/load  //docker 转tar包或者加载docker tar
docker tag <containerId> <tagName>       // docker <tag> 版本 为容器打标签
docker push/pull <tagName>
```



> 注： 镜像仓库地址，如果不填，则默认为hub.docker.com
>
> ​        创建私有镜像仓库 sudo docker run -d -p 5000:5000 registry  (docker 提供了registry 方便创建镜像仓库)
>
> ​         docker 向远程docker.io推送镜像，首先`docker login` 登录
>
> ​         `docker push ${docker_registry}/${contianer_name}:${tag}`

