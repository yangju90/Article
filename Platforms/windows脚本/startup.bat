@echo off

: p
ping -n 1 %1
IF ERRORLEVEL 1 goto aa
IF ERRORLEVEL 0 goto bb
:aa
echo �������Ӳ�ͨ...
timeout 30
goto p
:bb
echo Ip����������׼���������

echo �����������������
echo �����������������
echo �����������������
echo �����������������
d:
java -jar spring-boot-1.0-SNAPSHOT.jar