#### 1. 安装前处理

##### 1.1 环境检查

```shell
# cpu核心数
cat /proc/cpuinfo | grep "processor" | sort | uniq | wc -l
# 检查内存大小
cat /proc/meminfo | grep MemTotal
# 检查磁盘分区
lsblk -f
# 磁盘大小
df -hl
# 服务器节点是否支持虚拟化 （x86）-若无回显则不支持虚拟化
grep -E '(svm|vmx)' /proc/cpuinfo
# Arm架构查看
ls /dev/kvm
```

##### 1.2 依赖项安装

```shell
# 更新依赖源
apt-get update 
# 把缓存文件夹里的deb包清理
apt-get clean
# 下载安装包，并不安装
apt-get install -d xxx
cd /var/cache/apt/archives
dpkg -i *.deb

# 安装socat、conntrack、ebtables 和 ipset
sudo apt install -d socat conntrack ebtables ipset
```

##### 1.3 磁盘处理

(1) 将已有磁盘重新分配

```shell
#!/bin/bash

# 定义磁盘变量
DISK="/dev/sdb"

# 卸载当前分区
sudo umount /dev/sdb1
sudo umount -l /dev/sdb1 (强制卸载)
lsof /home (查看占用)

# 删除现有分区
sudo sgdisk --delete 1 /dev/sdb

# 创建新的分区
sudo sgdisk -n 1:0:+6T $DISK
sudo sgdisk -n 2:0:0 $DISK

# 格式化第一个分区
sudo mkfs.ext4 /dev/sdb1

# 挂载第一个分区
sudo mount /dev/sdb1 /home

# 更新 /etc/fstab
UUID=$(uuidgen)
echo "UUID=$UUID /home ext4 defaults 0 2" | sudo tee -a /etc/fstab
/dev/disk/by-uuid/535e3cb3-1bdd-471e-b3d7-31176aad41a7 /home ext4 defaults 0 2
```

(2) 卸载某个磁盘

```shell
DISK="/dev/sdX"
\# Zap the disk to a fresh, usable state (zap-all is important, b/c MBR has to be clean)
sgdisk --zap-all $DISK
\# Wipe a large portion of the beginning of the disk to remove more LVM metadata that may be present
dd if=/dev/zero of="$DISK" bs=1M count=100 oflag=direct,dsync
\# SSDs may be better cleaned with blkdiscard instead of dd
blkdiscard $DISK
\# Inform the OS of partition table changes
partprobe $DISK
```

#### 2. 系统安装

```shell
# 1.独立安装
./install.sh -a --ratio 2

# 2.卸载
./uninstall.sh -a


# 查看pod中有那些容器
kubectl get pod virt-launcher-i-sdj0cbnp-nn645 -o jsonpath='{.spec.containers[*].name}'
```

#### 3. 其他命令

```shell
# 过滤镜像
ctr --namespace=k8s.io images ls | awk '{print $1}' | awk '!/sha256/'

# 镜像打tag
ctr images tag docker.io/kubespheredev/ksv-apiserver:v1.6.1 cetcharbor.com:5000/kubespheredev/ksv-apiserver:v1.6.1

# 登录,跳过验证
ctr i push -u admin:12345 --plain-http cetcharbor.com:5000/kubespheredev/ksv-apiserver:v1.6.1

ctr i push -u admin:12345 cetcharbor.com:5000/kubespheredev/ksv-apiserver:v1.6.1


# 删除
ctr images rm cetcharbor.com:5000/kubespheredev/ksv-apiserver:v1.6.1

# 镜像不存在bash命令时 --sh
kubectl exec -it ksv-console-5b7f5685f5-mdtgb -n kubesphere-virtualization-system -- sh
```



#### 4. 重新制作镜像

##### 4.1 虚拟机管理平台

```shell
tar -xvf archive_name.tar

# KVS 管理平台
docker build -t myimage:1.0 .

ctr i  rm docker.io/library/myimage:1.0
docker rmi myimage:1.0

docker save -o myimage.tar myimage:1.0

ctr i import myimage.tar

kubectl delete pod ksv-console-66f8cd4d66-g5nss -n kubesphere-virtualization-system


# 本地测试运行，查看效果
docker run --name my-node-app myimage:1.0

docker run -p 8000:8000 -it --name my-node-app myimage:1.0 /bin/sh

docker run -p 8000:8000 -d --name my-node-app myimage:1.0
```

##### 4.2 云平台

```shell
# 1. 修改云平台deployment
# 2. docker build 制作镜像，tag限定名称版本
# 3. 镜像上传harbor平台
# 4. 删除ctr本地缓存镜像
# 5. 删除pod，让k8s平台拉起新镜像
```

#### 5. 依赖安装kvm

 ```shell
# 1. kube-ovn

# 2. multusCNI

# 3. kvm-crds.yaml 

# 4. kvm-deploy.yaml
 ```

#### 6. 其他

```shell
kubectl exec -it ks-installer-6cb88854c9-96vtc -n kubesphere-system -- /bin/bash

# 查看虚拟机列表：
kubectl get vms
# 查看虚拟机详情：
kubectl describe vm <虚拟机名称>

# 查看虚拟机实例：
kubectl get vmi

# 或者获取特定虚拟机实例的详细信息：
kubectl describe vmi <虚拟机实例名称>

# 查看虚拟机日志：
# 每个虚拟机都在一个 Pod 中运行，可以使用 kubectl logs 命令来查看虚拟机的日志：
kubectl logs <virt-launcher-pod名称>

# 停止/启动一个虚拟机
virtctl stop <虚拟机名称>
virtctl start <虚拟机名称>

virctl console 退出控制 Ctrl+]
```

#### 7. 使用

```shell
# 1. 创建虚拟机
kubectl apply -f vm.yaml

kubectl get vms
kubectl get vms -o yaml testvm



virtctl start testvm


kubectl get vmis
kubectl get vmis -o yaml testvm

virtctl console testvm

virtctl stop testvm

kubectl delete vm testvm
```



#### 8. 混合安装

```shell
# 1. ksv-config 修整


sudo mkfs.ext4 /dev/sda


sudo mkdir /data

sudo mount /dev/sda /data

# 获取id
sudo blkid /dev/sda

echo "UUID=your-uuid /data ext4 defaults 0 2" | sudo tee -a /etc/fstab

df -h
```