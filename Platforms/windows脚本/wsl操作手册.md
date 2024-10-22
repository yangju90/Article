<div align=center><font face="黑体" size=6>Wsl操作命令手册</font></div>



`wsl --list --verbose` 查看已注册的子系统

`wsl --shutdown` 删除具体的发行版

`wsl --export Ubuntu-22.04 D:\SoftTools\subsystem\Ubuntu.tar` 导出

`wsl --unregister Ubuntu-22.04` 删除具体的发行版

`wsl --import Ubuntu-22.04 D:\SoftTools\subsystem D:\SoftTools\subsystem\Ubuntu.tar --version 2` 导入

C:\Users\你的用户名\AppData\Local\Packages\* wsl 安装包





Linux 安装deb包 https://pkgs.org/