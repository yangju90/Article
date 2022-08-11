<center><font size=5 face="黑体">Linux操作系统命令总结</font></center>

#### 一、文件夹权限详解

```markdown
文件夹权限 rwx
* r - 看不到,文件夹中的内容（ls命令，无权限）
* w - 删除、创建文件夹（mkdir、rm无权限）
* x - 控制进入文件夹的权限（cd 无权限）

一般用户创建文件夹权限为 775 （用户User、组Group、其他Other）
```



#### 二、sudo权限作用

sudo权限的作用，sudo权限获取写（w）权限。
sudo su 提权

