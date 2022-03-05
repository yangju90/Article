<div align=center><font face="黑体" size=6>Docker基础知识</font></div>

> Docker 学习

* Docker概述
* Docker安装
* Docker命令
  * 镜像命令
  * 容器命令
  * 命令操作
  * ....
* *Docker镜像*
* 容器数据卷
* DockerFile
* Docker容器原理
* Docker网络原理
* IDEA整合Docker
* Docker Compose
* Docker Swarm
* CI/CD Jenkins (gitlab)





# **Docker概述**

解决开发、测试、运维环境的一致性（开发即运维）

Docker 开发、打包、部署、上线，一套流程做完

![image-1](resource\image-1.png)

Docker 通过隔离机制，可以尽可能利用服务器资源！



# Docker 安装

#### 1. Docker的基本组成

![image-2](resource\image-2.jpg)

**镜像（image）：**可以创建容器服务

**容器（container）：** 利用容器技术，独立运行一个或者一组应用，通过镜像来创建

**仓库（repository）：**存放镜像的地方

#### 2.安装Docker

系统环境Ubuntu Focal 20.04 (LTS)

##### 2.1 配置VirtualBox

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



##### 2.2 安装Docker

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

docker rmi 镜像id
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



