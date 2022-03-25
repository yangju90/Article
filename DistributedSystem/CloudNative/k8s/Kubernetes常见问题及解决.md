<div align=center><font face="黑体" size=6>Kubernetes常见问题及解决</font></div>

[toc]



# 1. kubenetes 安装

#### 1.1 kubeadm init 出错

##### 1.1.1 问题

```SHELL
[kubelet-check] It seems like the kubelet isn't running or healthy.
[kubelet-check] The HTTP call equal to 'curl -sSL http://localhost:10248/healthz' failed with error: Get "http://localhost:10248/healthz": dial tcp 127.0.0.1:10248: connect: connection refused.
```

![QA-1](resources\QA-1.png)

##### 1.1.2 原因

安装的是Docker容器，kubelet的cgroup driver是cgroupfs，docker的 cgroup driver是systemd，两者不一致导致kubelet启动失败

##### 1.1.3 解决方法

```shell
sudo vim /etc/docker/daemon.json
{
  "exec-opts": ["native.cgroupdriver=systemd"]
}
systemctl daemon-reload
systemctl restart docker

kubeadm reset
kubeadm init
```





#### 1.2 kubectl get po x509错误

##### 1.2.1 问题

输入kubectl 命令提示 Unable to connect to the server: x509: certificate signed by unknown authority 

##### 1.2.2 原因

kubectl 命令模式是在`$HOME/.kube `  路径下寻找config 文件，config文件为json格式，说明了当前执行命令的用户信息和认证信息

##### 1.2.3 解决方法

再`$HOME/.kube `  路径下添加config 文件，最为简单的是拿取admin权限

```shell
$ mkdir -p $HOME/.kube
$ sudo cp -i /etc/kubernetes/admin.conf $HOME/.kube/config
$ sudo chown $(id -u):$(id -g) $HOME/.kube/config
```

