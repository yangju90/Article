###### 备注：

```bash
br_netfilter  # linux 透明防火墙

apt-key  # 存储apt 密钥
apt-mark hold/unhold # 系统更新时 apt-update，是否更新软件包 hold不更新 unhold 取消hold
apt-cache madison <<package name>> # 查看软件版本
```

透明防火墙(Transparent Firewall)又称[桥接](https://so.csdn.net/so/search?q=桥接&spm=1001.2101.3001.7020)模式防火墙（Bridge Firewall）。简单来说，就是在网桥设备上加入防火墙功能。透明防火墙具有部署能力强、隐蔽性好、安全性高的优点。

 journalctl -f -u kubelet
