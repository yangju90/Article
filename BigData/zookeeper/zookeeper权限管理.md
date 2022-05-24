## Zookeeper 权限管理



Zookeeper软件本身没有提供，像mysql式的登录权限管理，登录软件权限管理依靠Kerberos身份认证软件加持，或者使用Iptables防火墙进行隔离。



所以这里只聊Zookeeper中Node节点的访问权限：

* Node节点权限不是递归式的，即父亲节点的权限不能顺延到子节点



#### Zookeeper 命令行操作

Zookeeper内部对digest密码验证进行SHA1、Base64加密，设置密码时使用加密后的密码，验证时未加密密码。

ACL仅仅只是访问权限的控制，如果无访问权限直接删除也是可以的（仅限空目录delete可删除），它和文件系统类似，其实控制的是内部权限，例如如果没有密码，在控制节点内部不能进行操作。

1. 利用Zookeeper官方jar生成加密密码

```cmd
> java -cp ./zookeeper-3.6.0.jar:./log4j-1.2.17.jar:./slf4j-log4j12-1.7.25.jar:./slf4j-api-1.7.25.jar org.apache.zookeeper.server.auth.DigestAuthenticationProvider user:abc
user:abc->user:ds/DOeVDf2SENn42yXzlU/p3LL4=
```

2. 创建带权限的节点

```cmd
create /test_acl

setAcl /test_acl digest:user:ds/DOeVDf2SENn42yXzlU/p3LL4=:crwda

# 或者
create /test content digest:user:ds/DOeVDf2SENn42yXzlU/p3LL4=:crwda
# 若密码位数不对，创建的节点无效
```

3. 命令行验证权限

```cmd
addauth digest user:abc

getAcl /test_acl
```

4. 删除节点

```cmd
delete /test_acl

deleteall /test_acl
```



注意：

zk.setACL为这几个节点设置权限就OK了，千万别忘记根节点"/"了