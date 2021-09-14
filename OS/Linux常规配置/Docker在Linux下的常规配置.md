<center><font size=5 face="黑体">Docker在Linux下的常规配置</font></center>

#### 一、配置docker启动

```shell
echo '# Start Docker daemon automatically when logging in if not running.' >> ~/.bashrc
echo 'RUNNING=`ps aux | grep dockerd | grep -v grep`' >> ~/.bashrc
echo 'if [ -z "$RUNNING" ]; then' >> ~/.bashrc
echo '    sudo dockerd > /dev/null 2>&1 &' >> ~/.bashrc
echo '    disown' >> ~/.bashrc
echo 'fi' >> ~/.bashrc
```



#### 二、用户添加到docker命令组

```shell
sudo usermod -aG docker $USER
```
