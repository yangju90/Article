#### 1. kubeedge安装

```shell
# 1. 安装keadm到bin
mv keadm /usr/local/bin/keadm
chmod +777 -R /usr/local/bin/keadm

# 重置
keadm reset --kube-config=/etc/rancher/rke2/rke2.yaml

# 获取证书token
keadm gettoken --kube-config=/etc/rancher/rke2/rke2.yaml

keadm init --profile version=v1.18.0 --advertise-address="192.168.8.93" --kube-config=/etc/rancher/rke2/rke2.yaml --set cloudCore.modules.dynamicController.enable=true

 
keadm manifest --advertise-address="192.168.56.206"  --kube-config=/etc/rancher/rke2/rke2.yaml > kubeedge-cloudcore.yaml
 
keadm manifest --advertise-address=192.168.56.206  --kube-config=/etc/rancher/rke2/rke2.yaml > kubeedge-cloudcore.yaml
```

#### 2. keadm安装edgecore

##### 2.1 containerd

###### (1) 安装edgecore

```shell
mkdir -p /opt/cni/bin
tar Cxzvf /usr/local containerd-1.7.2-linux-amd64.tar.gz 
cp containerd.service /usr/local/lib/systemd/system/containerd.service
systemctl daemon-reload
systemctl enable --now containerd

install -m 755 runc.amd64 /usr/local/sbin/runc

tar Cxzvf /opt/cni/bin cni-plugins-linux-amd64-v1.5.1.tgz

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


keadm join --cloudcore-ipport=192.168.8.93:10000 --token=<token>  --edgenode-name=edgework2 --remote-runtime-endpoint=unix:///run/containerd/containerd.sock --cgroupdriver=systemd --with-mqtt --image-repository=cetcharbor.com:5000/kubeedge


journalctl -u edgecore.service -xe
```

###### (2) 使用二进制安装edgecore的运行配置

KubeEdge默认使用`cgroupfs` cgroup驱动，如果你需要使用`systemd` cgroup驱动，你需要保证containerd配置了`systemd` cgroup，并且修改`edgecore.yaml`的如下参数”:

```yaml
modules:
  edged:
    tailoredKubeletConfig:
      cgroupDriver: systemd
```

###### (3) 添加允许私有HTTPS连接，跳过证书验证（HTTP采用参数）

新版本containerd，/etc/containerd/config.toml 下的`mirrors`和 `configs.tls`  已经废弃，采用 `config_path` 代替，参考https://github.com/containerd/containerd/blob/release/1.7/docs/hosts.md

```shell
sudo mkdir -p /etc/containerd/certs.d/cetcharbor.com:5000

sudo tee /etc/containerd/certs.d/cetcharbor.com:5000/hosts.toml << EOF
server = "http://cetcharbor.com:5000"

[host."http://cetcharbor.com:5000"]
capabilities = ["pull", "resolve", "push"]
skip_verify = true
EOF

sudo mkdir -p /etc/containerd/certs.d/docker.io
sudo tee /etc/containerd/certs.d/docker.io/hosts.toml << EOF
server = "https://registry-1.docker.io"

[host."http://cetcharbor.com:5000"]
capabilities = ["pull", "resolve", "push"]
skip_verify = true
EOF

sudo mkdir -p /etc/containerd/certs.d/cetcharbor.com
sudo tee /etc/containerd/certs.d/cetcharbor.com/hosts.toml << EOF
server = "https://cetcharbor.com"

[host."http://cetcharbor.com:5000"]
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

install -o root -g root -m 0755 cri-dockerd /usr/local/bin/cri-dockerd
install systemd/* /etc/systemd/system
sed -i -e 's,/usr/bin/cri-dockerd,/usr/local/bin/cri-dockerd,' /etc/systemd/system/cri-docker.service
systemctl daemon-reload
# 开机启动并重启
systemctl enable --now cri-docker.socket

# 服务以文件描述符运行
ExecStart=/usr/local/bin/cri-dockerd --container-runtime-endpoint fd://
# 以套接字运行
--container-runtime-endpoint unix:///var/run/cri-dockerd.sock

# 3. 安装CNI Plugin
mkdir -p /opt/cni/bin
tar Cxzvf /opt/cni/bin cni-plugins-linux-amd64-v1.1.1.tgz

# 4. 查看docker cgroup驱动方式
docker info | grep -i cgroup

# 5. 配置cni
mkdir -p /etc/cni/net.d/

cat >/etc/cni/net.d/10-containerd-net.conflist <<EOF
{
  "cniVersion": "1.0.0",
  "name": "containerd-net",
  "plugins": [
    {
      "type": "bridge",
      "bridge": "cni0",
      "isGateway": true,
      "ipMasq": true,
      "promiscMode": true,
      "ipam": {
        "type": "host-local",
        "ranges": [
          [{
            "subnet": "10.88.0.0/16"
          }],
          [{
            "subnet": "2001:db8:4860::/64"
          }]
        ],
        "routes": [
          { "dst": "0.0.0.0/0" },
          { "dst": "::/0" }
        ]
      }
    },
    {
      "type": "portmap",
      "capabilities": {"portMappings": true}
    }
  ]
}
EOF

# 6.重启containerd
```

如果你使用Keadm安装EdgeCore时，你需要设置`--remote-runtime-endpoint=unix:///var/run/cri-dockerd.sock`

KubeEdge默认使用`cgroupfs` cgroup驱动，如果你使用`systemd` cgroup驱动，你需要保证docker配置了`systemd` cgroup，在执行keadm join时设置--cgroupdriver=systemd。

```shell
keadm join --cloudcore-ipport=192.168.8.93:10000 --token=<token>  --edgenode-name=edgework1 --remote-runtime-endpoint=unix:///var/run/cri-dockerd.sock --cgroupdriver=systemd --with-mqtt --image-repository=cetcharbor.com:5000/kubeedge
```



##### 2.3 edgecore 安装

```shell
mv edgecore /usr/local/bin/edgecore
chmod +777 -R /usr/local/bin/edgecore


mkdir -p /etc/kubeedge/config/
edgecore --defaultconfig > /etc/kubeedge/config/edgecore.yaml
 
 
keadm reset edge
 
 
kubectl taint nodes node1 edgenode.kubernetes.io=work:NoSchedule

kubectl taint nodes node1 edgenode.kubernetes.io-

# 删除ds安装
kubectl label nodes edgework-211 kubernetes.io/os-
# 删除pod


containers:
- name: mycontainer
  image: myimage
tolerations:
- key: "key1"
  operator: "Exists"
  effect: "NoSchedule"
  
  
# 反亲和策略处理不需要的服务
```

##### 2.4 安装crictl

```shell
sudo tar zxvf crictl-v1.28.0-linux-amd64.tar.gz -C /usr/local/bin
rm -f crictl-$VERSION-linux-amd64.tar.gz

sudo tee /etc/crictl.yaml << EOF
runtime-endpoint: unix:///run/containerd/containerd.sock
image-endpoint: unix:///run/containerd/containerd.sock
timeout: 2
debug: true
pull-image-on-create: false
EOF
```

#### 3. EdgeMesh安装

```shell
# 1
kubectl taint nodes --all node-role.kubernetes.io/master-

# 2
kubectl label services kubernetes service.edgemesh.kubeedge.io/service-proxy-name=""

# 3. 启用 KubeEdge 的边缘 Kube-API 端点服务
# 3.1. 开启 dynamicController 模块，安装cloudcore
# 3.2. 打开 metaServer 模块，安装edgecore
metaManager:
    metaServer:
      enable: true
      
      modules:
# 3.3. 在边缘节点，配置 clusterDNS 和 clusterDomain
modules:
  ...
  edged:
    ...
    tailoredKubeletConfig:
      ...
      clusterDNS:
      - 169.254.96.16
      clusterDomain: cluster.local

systemctl restart edgecore

# 3.4. 测试端点是否正常curl 127.0.0.1:10550/api/v1/services


kubectl apply -f edgemesh/build/crds/istio/
# 请根据你的 K8s 集群设置 build/agent/resources/04-configmap.yaml 的 relayNodes，并重新生成 PSK 密码。
kubectl apply -f edgemesh/build/agent/resources/

# 请根据你的 K8s 集群设置 04-deployment.yaml 的 relayNodes，并重新生成 PSK 密码。以及设置 05-deployment.yaml 的 nodeName。
kubectl apply -f edgemesh/build/gateway/resources




kubectl get all -n kubeedge -o wide


# linux 获取系统环境变量
printenv
```

#### 4. UI界面创建(未启动)

```shell
kubectl create serviceaccount curl-user -n kube-system


kubectl apply -f - <<EOF
apiVersion: v1
kind: Secret
metadata:
  name: curl-user
  namespace: kube-system
  annotations:
    kubernetes.io/service-account.name: "curl-user"
type: kubernetes.io/service-account-token
EOF


kubectl create clusterrolebinding curl-user-binding --clusterrole=cluster-admin --serviceaccount=kube-system:curl-user -n kube-system


kubectl -n kube-system describe secret $(kubectl -n kube-system get secret | grep curl-user | awk '{print $1}')

```

