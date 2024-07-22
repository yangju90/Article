<div align=center><font face="黑体" size=6>教程</font></div>

[toc]

#### 1 Hello World

> 代码完成了打开窗口播放固定地址视频的功能，代码中没有延迟管理功能，如果需要解决这个问题，查看教程12。



#### 1.1 代码回顾

#### 1.1.1 核心函数

##### 1. 初始化函数

```c
// gstreamer 初始化函数
gst_init(&argc, &argv);
```

初始化函数是执行gstreamer框架的第一个函数，函数作用:

* 初始化所有的内部结构
* 检查那些插件可用
* 执行gstreamer的任何命令行选项

命令行参数可以通过argc、argv参数传递给gst_int（更多的可以查看教程10，gstreamer工具）。

##### 2. gst_parse_launch函数



#### 1.1.2 插件

