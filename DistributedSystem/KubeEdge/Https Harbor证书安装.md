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
IP.1 = 192.168.8.212
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

##### 1.3 证书本地可信

```shell
tls: failed to verify certificate: x509: certificate signed by unknown authority 错误时添加证书可信

# 1. 验证自签证书是否可信，推荐用curl
curl --head -v https://<域名>  

# 2. linux添加证书到可信列表，将证书拷贝到 /usr/local/share/ca-certificates，执行更新证书
cp cetcharbor.com.crt /usr/local/share/ca-certificates
sudo update-ca-certificates
 
# 3. 添加后，自签证书可信
```

##### 1.4 ks证书区别

```html
在TLS（传输层安全性协议）中，ca.crt、tls.crt 和 tls.key 是三种关键的文件，它们共同工作以确保数据传输的安全性。以下是每个文件的作用：

ca.crt（证书颁发机构证书）:

作用：ca.crt 是证书颁发机构（CA）的公钥证书。CA 是一个可信的第三方机构，负责签发和管理数字证书。ca.crt 通常包含 CA 的公钥和身份信息，用于验证它签发的证书的真实性。
用途：在客户端和服务器建立TLS连接时，服务器会提供其证书，客户端则使用 ca.crt 来验证该证书是否由受信任的CA签发。如果证书链能够追溯到客户端信任的CA，那么连接就是可信的。
tls.crt（服务器证书）:

作用：tls.crt 是服务器的公钥证书，通常包含服务器的公钥、身份信息（如域名）、证书有效期、签名算法等。这个证书是由CA签发的，用于在TLS握手过程中证明服务器的身份。
用途：当客户端（如浏览器）连接到服务器时，服务器会提供其 tls.crt。客户端使用CA的公钥（ca.crt）来验证服务器证书的有效性。如果验证成功，客户端就可以放心地与服务器通信，因为可以确认服务器的身份。
tls.key（服务器私钥）:

作用：tls.key 是服务器的私钥文件，与 tls.crt 中的公钥配对使用。私钥用于解密客户端发送的加密数据，或用于生成数字签名，以确保数据的完整性和来源的不可否认性。
用途：在TLS握手过程中，服务器可能会使用私钥来对某些信息进行签名，以证明它拥有与公钥匹配的私钥。这增加了通信的安全性，因为只有拥有正确私钥的实体才能生成有效的签名。
这三个文件共同构成了TLS加密通信的基础：

身份验证：通过 ca.crt 和 tls.crt 验证服务器的身份。
加密：使用 tls.key 和 tls.crt 加密数据传输，确保数据在传输过程中的安全性。
完整性：通过私钥生成的数字签名确保数据在传输过程中未被篡改。
在实际部署中，ca.crt 通常被分发到所有需要验证服务器证书的客户端，而 tls.crt 和 tls.key 则存储在服务器上，用于建立安全的TLS连接。
```



