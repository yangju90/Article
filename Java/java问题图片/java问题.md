##### 1. Web项目获取路径

```java
A.class.getResource("/").getPath(); // 获取类编译的绝对路径 classes/ 根下
file:/E:/java/workspace/email_analytics/Hadoop-train/target/classes/

A.class.getResource("").getPath(); // 获取类的绝对路径
file:/E:/java/workspace/email_analytics/Hadoop-train/target/classes/indi/mat/

// 检测web项目的root路径
private static String detectWebRootPath() {
    try {
        String path = A.class.getResource("/").toURI().getPath();
        return (new File(path)).getParentFile().getParentFile().getCanonicalPath();
    } catch (Exception var1) {
        throw new RuntimeException(var1);
    }
}

// 第一种：获取类加载的根路径   D:\git\daotie\daotie\target\classes
File f = new File(this.getClass().getResource("/").getPath());
System.out.println(f);

// 获取当前类的所在工程路径; 如果不加“/”  获取当前类的加载目录  D:\git\daotie\daotie\target\classes\my
File f2 = new File(this.getClass().getResource("").getPath());
System.out.println(f2);

// 第二种：获取项目路径    D:\git\daotie\daotie
File directory = new File("");// 参数为空
String courseFile = directory.getCanonicalPath();
System.out.println(courseFile);


// 第三种：  file:/D:/git/daotie/daotie/target/classes/
URL xmlpath = this.getClass().getClassLoader().getResource("");
System.out.println(xmlpath);


// 第四种： D:\git\daotie\daotie
System.out.println(System.getProperty("user.dir"));
/*
 * 结果： C:\Documents and Settings\Administrator\workspace\projectName
 * 获取当前工程路径
 */

// 第五种：  获取所有的类路径 包括jar包的路径
System.out.println(System.getProperty("java.class.path"));
```

其中，getPath()、getAbsolutePath()、getCanonicalPath()在一般情况下结果一致，getPath返回的时File.class中的path变量；getAbsolutePath、getCanonicalPath是通过系统FileSystem获取绝对路径，getCanonicalPath检查路径有效性，getAbsolutePath不检查。

##### 2. SpringBoot配置文件读取乱码

问题描述：接收一个羡慕，发现项目配置文件中的编码格式为ascii，为了调试方便强行转换为utf-8，通过粘贴复制转换，因此产生了一些问题，在项目调试期间，通过@Value读取中文始终乱码，对项目编码和设置都不起做用。

```java
// 尝试入下：
1. 修改application.properties 没有用（当然没用，只是瞎试！）
    banner.charset=UTF-8
    server.tomcat.uri-encoding=UTF-8
    spring.http.encoding.charset=UTF-8
    spring.http.encoding.enabled=true
    spring.http.encoding.force=true
    spring.messages.encoding=UTF-8
2. 自己读文件，获取，但是怎么挂在@Value里面，还需要研究，不想干
```

最后两种解决：

1. 设置 @PropertySource(value= "", encoding="UTF-8") 在类上面；

2.  将文件回退为最初的ascii编码，IDEA调整环境编码格式。（因为我的配置在application.properties中，所以使用第二种）

<img src='java问题-资源\图2 编码问题.png' style="zoom:60%">

深层原因：

SpringBoot在读取properties文件编码啊，中文使用ISO_8859_1，所以会有问题。也有网友推荐使用yml文件解决问题，并没有尝试。

