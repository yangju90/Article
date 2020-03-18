<center><font size=5 face="黑体">Linux操作系统命令总结</font></center>

#### 一. Linux登录

##### 1.1 Linux用户登录日志

```java
1) last 
    列出当前和曾经登入系统的用户信息，它默认读取的是/var/log/wtmp文件的信息。
2) lastb
    列出失败尝试的登录信息
```

#### 二、查询日志

##### 2.1 查询历史执行命令

```shell
1) history

2) history | grep xxx   # 显示过滤含有xxx的历史命令
```



#### 三、工具使用

##### 3.1 netstat工具使用

```shell
1) netstat -ntlp  #查询正在监听的tcp列表
    -n  以数字的形势显示address和port 
    -t  socket连接为tcp
    -l  表示Listen表示，正在监听的列表
    -p	显示 proto 指定的协议的连接；
    	proto 可以是TCP 、UDP 、TCPv6 或 UDPv6 协议之一
如果与 -s 选项一起使用以显示按协议统计信息，proto 可以是下列协议之一 :
  	IP 、IPv6 、ICMP 、ICMPv6 、TCP 、TCPv6 、UDP 或 UDPv6 。

```

[参考连接](https://www.iteye.com/blog/wsmajunfeng-1222526)

