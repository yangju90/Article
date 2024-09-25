#### 1. kubeedge安装

```shell
# 1. 安装keadm到bin
mv keadm /usr/local/bin/keadm
chmod +777 -R /usr/local/bin/keadm

# 重启
keadm reset --kube-config=/etc/rancher/rke2/rke2.yaml

# 获取证书token
keadm gettoken


keadm init --advertise-address="192.168.56.206" --kube-config=/etc/rancher/rke2/rke2.yaml
 
keadm manifest --advertise-address="192.168.56.206"  --kube-config=/etc/rancher/rke2/rke2.yaml > kubeedge-cloudcore.yaml
 
keadm manifest --advertise-address=192.168.56.206  --kube-config=/etc/rancher/rke2/rke2.yaml > kubeedge-cloudcore.yaml
```

#### 2. keadm安装edgecore

##### 2.1 containerd

###### (1) 安装edgecore

```shell
# 如果在/etc/containerd/目录下没有containerd的配置文件，你可以执行如下命令生成配置文件并重启containerd
containerd config default > /etc/containerd/config.toml
systemctl restart containerd

# 如果你需要更新沙箱(pause)镜像，你也可以通过在containerd的配置文件中修改如下设置来实现
[plugins."io.containerd.grpc.v1.cri"]
  sandbox_image = "kubeedge/pause:3.6"
  
# 你还可以通过containerd的配置文件查看或更新containerd的cgroup驱动
[plugins."io.containerd.grpc.v1.cri".containerd.runtimes.runc]
  ...
  [plugins."io.containerd.grpc.v1.cri".containerd.runtimes.runc.options]
    SystemdCgroup = true
    
# 如果你使用Keadm安装EdgeCore时，你需要设置
--remote-runtime-endpoint=unix:///run/containerd/containerd.sock

# KubeEdge默认使用cgroupfs cgroup驱动，如果你需要使用systemd cgroup驱动，你需要保证containerd配置了systemd cgroup，在执行keadm join时设置--cgroupdriver=systemd
```

###### (2) 使用二进制安装edgecore的运行配置

KubeEdge默认使用`cgroupfs` cgroup驱动，如果你需要使用`systemd` cgroup驱动，你需要保证containerd配置了`systemd` cgroup，并且修改`edgecore.yaml`的如下参数”:

```yaml
modules:
  edged:
    tailoredKubeletConfig:
      cgroupDriver: systemd
```

###### (3) 添加允许私有HTTPS连接，跳过证书验证

新版本containerd，/etc/containerd/config.toml 下的`mirrors`和 `configs.tls`  已经废弃，采用 `config_path` 代替，参考https://github.com/containerd/containerd/blob/release/1.7/docs/hosts.md

```shell
sudo mkdir -p /etc/containerd/certs.d/cetcharbor.com:5000

sudo tee /etc/containerd/certs.d/cetcharbor.com:5000/hosts.toml << EOF
server = "http://cetcharbor.com:5000"

[host."https://cetcharbor.com:5000"]
capabilities = ["pull", "resolve", "push"]
skip_verify = true
EOF
```

编辑 `/etc/containerd/config.toml` 文件，添加或修改 `[plugins."io.containerd.grpc.v1.cri".registry]` 部分，使用 `config_path` 指向您创建的配置

```shell
[plugins."io.containerd.grpc.v1.cri".registry]
config_path = "/etc/containerd/certs.d"
```

重启containerd服务

```shell
systemctl restart containerd
```

###### (4) containerd查看日志

```shell
containerd 查看运行日志
```







##### 2.2 docker

###### (1) 安装edgecore

```shell
# 1. docker安装
# 2. 安装cri-dockerd，允许kubernetes控制docker
cri-dockerd
# 3. 安装CNI Plugin
```

如果你使用Keadm安装EdgeCore时，你需要设置--remote-runtime-endpoint=unix:///var/run/cri-dockerd.sock

当使用cri-dockerd时，对应的runtimetype也是remote，而不是dock。

KubeEdge默认使用`cgroupfs` cgroup驱动，如果你使用`systemd` cgroup驱动，你需要保证docker配置了`systemd` cgroup，在执行keadm join时设置--cgroupdriver=systemd。

###### (2) 使用二进制安装EdgeCore的运行时配置

KubeEdge默认使用`cgroupfs` cgroup驱动，如果你使用`systemd` cgroup驱动，你需要保证docker配置了`systemd` cgroup，并且修改`edgecore.yaml`的如下参数”:

```yaml
modules:
  edged:
    tailoredKubeletConfig:
      cgroupDriver: systemd
```

