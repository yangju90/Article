<center><font size=5 face="黑体">Halo博客系统搭建记录</font></center>

[TOC]



```markdown
**系统配置**

**规格:** 1vCPUs | 2GB | s6.medium.2

**镜像:** Ubuntu 18.04 server 64bit

**用户:** 在Ubuntu上创建halo用户
```



#### 一、操作步骤

Halo的安装步骤：

1. 安装nginx

   ```java
   sudo apt-get install nginx
   
   如果出现无法定位nginx包，先进行下面操作
   sudo apt-get update
   ```

2. 启动nginx
   ```java
   systemctl start nginx
   systemctl enable nginx  # linux开机自启动ngin
   ```
   
3. 测试nginx启动

   <img src="Halo博客系统搭建记录\1.png" style="zoom:100%">

4. 安装Java

   * 首先下载Linux版JDK jdk-8u161-linux-x64.tar.gz

   * 解压安装包
     ```java
     tar -zxvf jdk-8u161-linux-x64.tar.gz -C unzipPath
     ```
     
   * 配置环境变量 /etc/profile 或者 ~/.bash_profile

     ```java
     #set java environment
     JAVA_HOME=/usr/local/java/jdk1.8.0_161
     JRE_HOME=$JAVA_HOME/jre
     CLASS_PATH=.:$JAVA_HOME/lib/dt.jar:$JAVA_HOME/lib/tools.jar:$JRE_HOME/lib/rt.jar
     PATH=$PATH:$JAVA_HOME/bin:$JRE_HOME/bin
     export JAVA_HOME JRE_HOME CLASS_PATH PATH
     ```
     
   * 使配置内容生效 source /etc/profile

   * 验证 java -version

5. 安装MySql并测试

   ```java
   sudo apt-get install mysql-server
   
   # 测试Mysql
   systemctl status mysql
   # 登录Mysql
   mysql -u root -p
   # 如果回车不能登录，则进入**注意事项**1进行mysql的配置
   # 进入mysql则进入mysql库
   use mysql;
   update user set authentication_string=password("1234") where user="root";
   update user set plugin="mysql_native_password";
   flush privileges;
   ```

6. 安装halo

   ```java
   (1)创建数据库
   create database halodb character set utf8mb4 collate utf8mb4_bin;
   # mb4 utf-8编码为4个字节 MySQL的utf-8编码只支持3字节的数据，而移动端的表情数据是4个字节的字符。如果直接往采用utf-8编码的数据库中插入表情数据，Java程序中将报SQL异常
   
   (2)创建 mkdir .halo
   
   (3)下载application.yaml配置文件
   
   (4)运行启动halo
   nohup java -jar halo.jar &
   ```

7. 设置服务器安全规则

8. 访问网站



#### 二、域名配置和SSL证书 

1. 购买域名并配置IP地址

<img src="Halo博客系统搭建记录\2.png" style="zoom:100%">

2. 访问域名测试

3. 下载certbot并生成证书配置

   ```java
   (1) 第一种方式
   wget https://dl.eff.org/certbot-auto
   chmod a+x certbot-auto
   
   // 生成证书前关闭nginx
   service nginx stop
   ./certbot-auto certonly
   // 输入相关信息 即可在/etc/letsencrypt/archive目录下得到证书文件。
   
   这种方式没有成功
   
   (2)第二种方式
   
   sudo apt-get update
   sudo apt-get install software-properties-common
   sudo add-apt-repository universe
   sudo add-apt-repository ppa:certbot/certbot
   sudo apt-get update
   
   sudo apt-get install certbot python-certbot-nginx
   // 自动配置到nginx
   sudo certbot --nginx
   // 生成证书，手动配置
   sudo certbot certonly --nginx
   
   // 证书自动申请续时间，测试申请
   sudo certbot renew --dry-run  
   
   
   // 续订证书文件配置位置（三者之一）
   /etc/crontab/
   /etc/cron.*/*
   systemctl list-timers
   ```

4. 检测是否配置成功

   ```java
   (1) 访问注册的域名以https
   (2) 在网站中https://tutorials.hostucan.cn/httpsssl，随便找两个测试网站
   ```

   

#### 三、注意事项

1. mysql不能登录问题

   ```java
   (1) 第一种方式
   
   查看Mysql文件 sudo vi /etc/mysql/mysql.conf.d/mysqld.cnf
   进入到这个配置文件，然后在这个配置文件中的[mysqld]这一块中加入skip-grant-tables这句话
   
   [mysqld]
   #
   # * Basic Settings
   #
   user            　 = mysql
   pid-file        　 = /var/run/mysqld/mysqld.pid
   socket        　　 = /var/run/mysqld/mysqld.sock
   port            　 = 3306
   basedir        　　= /usr
   datadir       　　 = /var/lib/mysql
   tmpdir       　　　= /tmp
   lc-messages-dir   = /usr/share/mysql
   skip-external-locking
   character-set-server=utf8
   collation-server=utf8_general_ci
   skip-grant-tables　　　　<-- add here
   
   
   作用：就是让你可以不用密码登录进去mysql。
   保存:wq，退出。输入：service mysql restart，重新启动mysql。
   
   (2)第二种方式
   使用root用户登录，mysql -u root -p 不需要密码
   修改mysql密码后可登录
   
   
   ```

2. Mysql远程登录

   * mysql5.7远程登录

   ```java
   (1) 开启数据库3306端口
     首先，使用如下指令查看3306端口是否对外开放。
         netstat -an | grep 3306
       tcp    0   0 127.0.0.1:3306      0.0.0.0:*         LISTEN
     如果显示如上，说明mysql端口目前只监听本地连接127.0.0.1。然后需要修改mysql的配置文件
     sudo vim /etc/mysql/mysql.conf.d/mysqld.cnf
     将其中bind-address = 127.0.0.1注释掉。
   (2) 授权远程访问
     首先，进入数据库 mysql -u username -p password，username为你的mysql用户名，password为你的mysql密码。
     然后，执行如下sql语句
     mysql> grant all on *.* to username@'%' identified by '1234';  
     #username为你的mysql用户名，password为你的mysql密码。
   (3) 重启mysql服务
     systemctl restart mysql
   (4) 在防火墙中开启3306端口
     如果仍然不能远程访问mysql，那么有可能是防火墙阻止访问，需要开启允许访问。
     sudo ufw allow 3306
   ```
   
* mysql5.8远程登录，<a target="_blank" href="https://www.cnblogs.com/xiaohuomiao/p/10601760.html">参考</a>。
  
3. 创建halo用户并赋权限

   ```java
   # 创建halo用户
   adduser halo
   # 赋予权限，修改/etc/sudoers文件
   
   # User privilege specification
   root    ALL=(ALL:ALL) ALL
   halo    ALL=(ALL:ALL) ALL   <-- add here
   ```

   
