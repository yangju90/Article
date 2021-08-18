@echo off

: p
ping -n 1 %1
IF ERRORLEVEL 1 goto aa
IF ERRORLEVEL 0 goto bb
:aa
echo 网络连接不通...
timeout 30
goto p
:bb
echo Ip连接正常，准备启动软件

echo 软件启动！！！！！
echo 软件启动！！！！！
echo 软件启动！！！！！
echo 软件启动！！！！！
d:
java -jar spring-boot-1.0-SNAPSHOT.jar