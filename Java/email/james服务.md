<div align=center><font face="黑体" size=6>apache-james 服务配置信息</font></div>

[toc]



# 1.Docker启动

#### 1.1 IMAP|SMTP 服务

1. 生成keystore `keytool -genkey -alias james -keyalg RSA -keystore /opt/james/conf/keystore`  password `james72laBalle`
2. mkdir -p  /opt/james/var
3. 启动

```shell
docker run -d \
--name james_jpa \
-p "25:25" -p "465:465" -p "587:587" \
-p "143:143" -p "110:110"  -p "993:993" \
--volume "/opt/james/conf/:/root/conf/" \
--volume "/opt/james/var:/root/var/" \
apache/james:jpa-3.8.2
```

#### 1.2 修改配置



#### 1.3 添加域和用户

```shell
james-cli AddDomain james.local
james-cli AddUser user@james.local 123456
james-cli AddUser user01@james.local 123456
james-cli AddUser user02@james.local 123456
james-cli AddUser user03@james.local 123456
james-cli AddUser user04@james.local 123456
```

| 协议 | SSL端口 | 非SSL端口 |
| ---- | ------- | --------- |
| POP3 | 995     | 110       |
| IMAP | 993     | 143       |
| SMTP | 465     | 25        |

