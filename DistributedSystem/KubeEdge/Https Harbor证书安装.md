#### 1. Harbor Https处理过程

> 配置DNS，注意修稿host地址
>
> windows -> C:\Windows\System32\drivers\etc\hosts
>
> linux ->  /etc/hosts

##### 1.1 无DNS域名Https配置

```shell
# 证书
# <1> ca.key
openssl genrsa -out ca.key 4096
# <2> ca.crt
openssl req -x509 -new -nodes -sha512 -days 3650 \
 -subj "/C=CN/ST=Shanxi/L=Xian/O=Indi/OU=CETC/CN=CETC Xi'an R&D Center" \
 -key ca.key \
 -out ca.crt
```

##### 1.2 配置定义DNS的Https配置

```shell
# 证书
# <1> ca.key
openssl genrsa -out ca.key 4096
# <2> ca.crt
openssl req -x509 -new -nodes -sha512 -days 3650 \
 -subj "/C=CN/ST=Shanxi/L=Xian/O=Indi/OU=CETC/CN=CETC Xi'an R&D Center" \
 -key ca.key \
 -out ca.crt

# <3> v3.ext 
cat > v3.ext <<-EOF
authorityKeyIdentifier=keyid,issuer
basicConstraints=CA:FALSE
keyUsage = digitalSignature, nonRepudiation, keyEncipherment, dataEncipherment
extendedKeyUsage = serverAuth
subjectAltName = @alt_names

[alt_names]
DNS.1=cetcharbor.com
EOF
 
# <4> 生成域名CA 
openssl genrsa -out cetcharbor.com.key 4096

openssl req -sha512 -new \
    -subj "/C=CN/ST=Shanxi/L=Xian/O=Indi/OU=CETC/CN=cetcharbor.com" \
    -key cetcharbor.com.key \
    -out cetcharbor.com.csr

openssl x509 -req -sha512 -days 3650 \
    -extfile v3.ext \
    -CA ca.crt -CAkey ca.key -CAcreateserial \
    -in cetcharbor.com.csr \
    -out cetcharbor.com.crt

# <5> 移动证书到相应位置
mkdir -p /data/cert
cp cetcharbor.com.crt /data/cert/
cp cetcharbor.com.key /data/cert/
cd  /data/cert

openssl x509 -inform PEM -in cetcharbor.com.crt -out cetcharbor.com.cert

mkdir -p /etc/docker/certs.d/cetcharbor.com/
cp cetcharbor.com.cert /etc/docker/certs.d/cetcharbor.com/
cp cetcharbor.com.key /etc/docker/certs.d/cetcharbor.com/
cp ca.crt /etc/docker/certs.d/cetcharbor.com/

# <6> 修改harbor配置文件 harbor.yml
https:
  port: 443
  certificate: /data/cert/cetcharbor.com.cert
  private_key: /data/cert/cetcharbor.com.key
```

