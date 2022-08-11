#!/bin/bash

#连接，并连接系统

function netConnection()
{
  echo $(ping -c 1 $1 | grep "ttl" | wc -c)
}

while (($(netConnection $1)==0))
do
	echo "未连接网络"
	sleep 5
done

echo "连接到网络" 

nohup java -jar halo-latest.jar &
