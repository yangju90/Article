#!/bin/bash

# 杀掉进程

# ps -ef | grep i | grep -v grep | awk '{print $2}' | xargs kill -9

kyService="halo-latest.jar"
kyPathShell="/home/halo/halo-start.sh"
msService="msService"
msPathShell=""
tomcatService="tomcatService"
tomcatPathShell=""

num=3
while (($num!=0))
do
tnum=0
for i in $kyService $msService $tomcatService
do
echo $i
pid=$(ps -ef | grep $i | grep -v grep | awk '{print $2}')
echo "pid : $pid"
w=${#pid}
if [ $w -ne 0 ];then
echo "杀掉 $pid ..... "
kill -9 $pid
tnum=$[$tnum+1]
fi
done
num=$tnum
echo "进程数：$tnum !"
echo "杀掉的进程数：$num !"
sleep 5
done

echo "开始重启所有程序...."
for i in $kyPathShell
do
exec $i
done
