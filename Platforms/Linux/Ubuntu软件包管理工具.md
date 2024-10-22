<div align=center><font face="黑体" size=6>Ubuntu软件包管理工具dpkg和apt</font></div>

**dpkg和apt是Linux系统中用于管理软件包的工具，‌它们各自具有不同的功能和用途，‌但同时也存在联系。‌**

- **dpkg的基本功能**：‌

  - **安装软件包**：‌使用`dpkg -i <.deb file name>`命令安装.deb文件

  - **管理软件包**：‌包括安装、‌配置、‌卸载等操作

    ```
    dpkg --install package_file
    dpkg --unpack package_file
    dpkg --configure package
    dpkg -r package 删除软件包（保留其配置信息）
    dpkg -P 删除一个包（包括配置信息

  - **处理本地.deb文件**：‌dpkg主要用于处理本地的.deb软件包文件，‌不会解决软件包的依赖关系，‌也不会咨询软件仓库

- **apt的功能和特点**：‌

  - **解决依赖关系**：‌apt能够解决和安装模块的依赖问题，‌并会咨询软件仓库

  - **客户端/服务器系统**：‌APT是一个客户/服务器系统，‌能够从服务器下载并安装软件包

  - **常用命令**：‌包括`apt-get install`用于下载并安装软件包及其依赖项，‌`apt-get remove`用于卸载软件包，‌以及`apt-get update`用于升级来自Debian镜像的软件包列表

  - **常用工具:** `apt-get`、`apt-cache`和`apt-config` ，apt命令事这三个命令的常用集合

    ```
    apt show xx 查看软件包具体信息
    apt purge xx 卸载指定软件及清除相关的配置  [apt-get purge xx]
    apt autoremove 用来自动清理不在使用的依赖和库文件  [apt-get autoremove]
    -y 自动确认所有提示
    apt list --installed 显示已安装的软件包
    apt list --upgradeable 显示可升级的软件包 
    apt-get clean 清理下载包的临时文件
    ```

    

- **dpkg和apt的联系**：‌

  - **互补关系**：‌dpkg是APT系统的基础组件，‌用于处理.deb文件的安装、‌卸载等操作。‌APT则建立在dpkg之上，‌提供了更高级的功能，‌如解决依赖关系和从远程仓库安装软件包
  - **共同目标**：‌两者共同目标是简化Linux系统中软件包的安装、‌管理和维护过程

综上所述，‌dpkg和apt在Linux软件包管理中扮演着不同的角色。‌dpkg主要处理本地的.deb文件，‌而apt则通过解决依赖关系和访问远程仓库，‌提供了更便捷的软件包管理体验。‌两者相互配合，‌共同构成了Debian和Ubuntu等系统中强大的软件包管理系统