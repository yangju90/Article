<div align=center><font face="黑体" size=6>Kubernetes Tools</font></div>

[toc]



# 1.  yq 工具

yq是便携式命令行YAML处理器.

#### 1.1 Ubuntu 系统安装yq 

```shell
sudo snap install yq
```

#### 1.2 yq 工具简单使用

```shell
# 使用工具查看yaml文件
yq xxx.file

# 通道符展示
cat xxx.file | yq

# k8s pod yaml 文件
kubectl get pod <pod_name> -n <node_name> -oyaml | yq
```

