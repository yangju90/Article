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

#### 3.系统卸载

```

```







