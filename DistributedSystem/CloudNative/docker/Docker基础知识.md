<div align=center><font face="黑体" size=6>Docker基础知识</font></div>

> Docker 学习

* Docker概述
* Docker安装
  * 额外知识：/etc/sysctl.conf 文件详解
* Docker命令
  * 镜像命令
  * 容器命令
  * 命令操作
* *Docker镜像*
* 容器数据卷
* DockerFile
* Docker容器原理
* Docker网络原理
* IDEA整合Docker
* Docker Compose
* Docker Swarm
* CI/CD Jenkins (gitlab)

[toc]



# **1. Docker概述**

解决开发、测试、运维环境的一致性（开发即运维）

Docker 开发、打包、部署、上线，一套流程做完

![image-1](resource\image-1.png)

Docker 通过隔离机制，可以尽可能利用服务器资源！



# 2. Docker 安装

#### 2.1 Docker的基本组成

![image-2](resource\image-2.jpg)

**镜像（image）：**可以创建容器服务

**容器（container）：** 利用容器技术，独立运行一个或者一组应用，通过镜像来创建

**仓库（repository）：**存放镜像的地方



![image-5](D:\work\blog\Article\DistributedSystem\CloudNative\docker\resource\image-5.png)



#### 2.2 安装Docker

系统环境Ubuntu Focal 20.04 (LTS)

##### 2.2.1 配置VirtualBox

配置网络环境，目标 宿主机、虚拟机A 、虚拟机B、外部网络 之间的网络均可以正常访问。

(1) 设置VirtualBox的网络管理器  (<font color=red>注意不要与宿主机同网段，宿主机为192.168.1.3，设置HostOnly网卡在192.168.1.x网段，导致虚拟机双网卡，不能链接互联网</font>)

![image-3](resource\image-3.png)

(2) 设置具体的虚拟服务器网络 **<font color=red>网卡1为NAT 网卡2为Host-Only</font>**(顺序不能颠倒)，选择对应的虚拟网卡配置

(3) 修改Ubuntu操作系统网络配置文件

```yaml
vi /etc/netplan/00-installer-config.yaml

network:
  ethernets:
    enp0s3:
      dhcp4: true
    enp0s8:
      dhcp4: no
      addresses:
        - 192.168.56.100/24
  version: 2
```

(4) 执行netplan

```sh
netplan apply
```



> 其它：
>
> 新版本的Ubuntu 18以上系统，网络使用netplan，同时路由使用route table
>
> - route    查看路由表
> - ip route
>
>  更多信息查询，route路由表信息



(5) sudo 命令相关操作

```shell
使用visudo编辑权限文件， 也可以用vi
# 给用户添加sudo组
su root       -切换到root用户组   (Ubuntu无密码下切换到root， sudo -i )
# 打开sudoers添加sudo权限
visudo /etc/sudoers
# 添加admin用户的权限
%admin ALL=(ALL) ALL
# 设置sudo无密码
%sudo ALL=(ALL:ALL) NOPASSWD:ALL

改完后就会生效，不需要source去执行

注：visudo 操作命令 crtl + x 退出 crtl + o 保存文件 enter 确认保存的文件 
```



##### 2.2.2 安装Docker

> 参考链接： https://docs.docker.com/engine/install/

(1) 卸载旧版本

| 命令       | 功能                             |
| ---------- | -------------------------------- |
| apt remove | 会删除软件包而保留软件的配置文件 |
| apt purge  | 会同时清除软件包和软件的配置文件 |

```shell
# 卸载软件 
sudo apt-get purge docker-ce docker-ce-cli containerd.io

#删除镜像容器卷
 sudo rm -rf /var/lib/docker
 sudo rm -rf /var/lib/containerd
```

(2) 安装docker

```she
sudo apt-get install docker-ce docker-ce-cli containerd.io
```

(3) 测试运行docker

```shel
sudo docker run hello-world
```

(4) 其他命令

```
systemctl status docker 查看docker状态
systemctl start docker 开启docker服务
systemctl stop docker 关闭docker服务、

docker version 查看docker版本
docker images 查看镜像

docker ps   查看正在运行容器
docker ps -a  查看所有容器

docker rmi -f 镜像id
docker rm 容器id
```

(5) 配置镜像仓库地址

```shell
sudo mkdir -p /etc/docker
sudo touch daemon.json
{
  "registry-mirrors": ["https://docker.mirrors.ustc.edu.cn"],
  "insecure-registries": ["host:prt"],
  "live-restore": true
}

#重启服务
sudo systemctl daemon-reload
sudo systemctl restart docker
```

(6) Linux 禁用swap分区

```shell
# 临时禁用吗，重启失效
swapoff -a

#启动交换分区
swapon -a

# 永久禁用
vi /etc/fstab
remove the line with swap keyword

# 查看内存使用情况
free -mh
```



##### 2.2.3 /etc/sysctl.conf 文件详解

使文件立刻生效命令：/sbin/sysctl -p

/proc/sys目录下存放着大多数内核参数，并且可以在系统运行时进行更改，不过重新启动机器就会失效。/etc/sysctl.conf是一个允许改变正在运行中的Linux系统的接口，它包含一些TCP/IP堆栈和虚拟内存系统的高级选项，修改内核参数永久生效。也就是说/proc/sys下内核文件与配置文件sysctl.conf中变量存在着对应关系。

``` shell
#  关闭swap分区，kubernetes环境必备
vm.swappiness = 0
# ElasticSearch环境必备
vm.max_map_count=655360
fs.file-max = 999999 
net.ipv4.tcp_tw_reuse = 1 
net.ipv4.tcp_keepalive_time = 60 
net.ipv4.tcp_fin_timeout = 30 
net.ipv4.tcp_max_tw_buckets = 5000 
net.ipv4.ip_local_port_range = 1024 61000 
net.ipv4.tcp_rmem = 4096 32768 262142 
net.ipv4.tcp_wmem = 4096 32768 262142 
net.core.netdev_max_backlog = 8096 
net.core.rmem_default = 262144 
net.core.wmem_default = 262144 
net.core.rmem_max = 2097152 
net.core.wmem_max = 2097152 
net.ipv4.tcp_syncookies = 1 
net.ipv4.tcp_max_syn.backlog = 1024
```



```shell
swapoff -a && swapon -a  清空是swap
然后执行 sysctl -p 命令,使上述修改生效。
```

参考连接：https://www.cnblogs.com/Jtianlin/p/4339931.html

​					https://www.cnblogs.com/soymilk2019/p/13725248.html



# 3. Docker命令

#### 3.1 Docker 架构

![image-4](resource\image-4.png)

#### 3.2 Docker 命令

`docker version`  docker 版本信息

`docker info`  显示docker的系统信息，包括镜像和容器的信息

`docker <command> --help` docker命令帮助信息

参考链接：https://docs.docker.com/engine/reference/commandline/cli/

##### 3.2.1 镜像命令

`docker images` 查看所有镜像信息

`docker search <repository_name>`  从远程docker仓库搜索

`docker search mysql --filter=STARS=3000` 搜索STAR数量大于3000的docker镜像

`docker pull <image_name>`  docker image 镜像下载

<font color=red>`docker pull mysql 相等于 docker pull docker.io/library/mysql:latest`</font>

`docker rmi -f <image_id1> <image_id2> ... ` 删除镜像，递归删除

`docker rmi -f ${docker images -aq}  `  删除所有镜像，递归删除

##### 3.2.2 容器命令

```dockerfile
docker run [可选参数] <image_name> #启动镜像命令

# 参数说明
--name="Name"  #容器名字 区分容器
-d   # 后台运行
-it # 交互方式进入容器
-p  
-P  # dockerfile端口，不和主机link

docker run -it centos /bin/bash   #启动容器，并进入容器，以bash形式
docker ps -aq  # 显示所有容器id

docker exec -it <docker_id> /bin/bash    # busybox /bin/sh 进入容器需要一个进程挂起新的终端，所以有/bin/bash

docker attach <docker_id> # 进入的是正在运行的容器命令行，正在执行代码，不会启动新的进程，所以exit 会退出当前容器

ctrl +p +q # 退出当前容器，后台挂起

docker rm -f <docker_id>  # 强制删除 -f

# 启动和停止
docker start <docker_id>   # 启动
docker restart <docker_id>  # 重启
docker stop <docker_id>   # 停止
docker kill <docker_id> # 强制停止
```

##### 3.3.3 其他命令

**后台启动容器问题**

```shell
(1) 启动后停止
docker run -d centos   # 后台启动容器，当发现容器中无进程运行时会自动关闭
docker run -itd centos /bin/bash  # 有交互的运行，则不会关闭

docker run -itd centos /bin/bash -c "while true;do echo mat;sleep 1;done"

(2)查看docker日志
docker logs -f -t --tail 10 <docker_id>
```

**容器中的信息**

`docker top <docker_id>` 查询docker 容器中的进程

`docker inspect <docker_id>` 查询docker 容器中的元数据

`docker cp /www/runoob <docker_id>:/www `  主机拷贝容器  

`docker cp <docker_id>:/www /www/runoob  `  容器拷贝主机



> docker 还可以通过打同卷的方式同步数据  docker -v  
>
> `docker run -itd -v /test:/soft centos /bin/bash`



##### 3.3.4 实战

```shell
(1) nginx

docker run -d --name nginx01 -p 80:80 nginx

# 测试
curl locahost:80  

whereis nginx

vi /etc/nginx/nginx.connf

docker run -it --rm nginx   # 运行完后，会删除容器， 一般用作测试

(2) 多端口暴漏 elasticsearch

docker run -d --name elasticsearch -p 9200:9200 -p 9300:9300 -e "discovery.type=single-node" -e ES_JAVA_OPTS="-Xms64m -Xmx512m" elasticsearch:7.6.2

# --net somenetwork 连接一个container网络,名字为somenetwork
# -e 对docker环境变量的修改 等同与Dockerfile中的ENV

docker stats  # 查看docker容器 cpu 状态

(3) Docker Portainer 

docker run -d -p 8088:9000 --restart=always -v /var/run/docker.sock:/var/run/docker.sock --privileged=true portainer/portainer

# 验证安装
http://${HOST_IP}:8088/
```



