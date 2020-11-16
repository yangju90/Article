## Springboot配置日志配置文件位置
`logging.config: classpath:logging/logback.yml`

## Springboot日志分类
**log4j：** 是apache实现的一个开源日志组件

**logback：** 同样是由log4j的作者设计完成的，拥有更好的特性，用来取代log4j的一个日志框架，是slf4j的原生实现
 
**log4j2：** 是log4j 1.x和logback的改进版，采用了一些新技术（无锁异步、等等），使得日志的吞吐量、性能比log4j 1.x提高10倍，并解决了一些死锁的bug，而且配置更加简单灵活


## slf4j+log4j和直接用log4j的区别

slf4j是对所有日志框架制定的一种规范、标准、接口，并不是一个框架的具体的实现，因为接口并不能独立使用，需要和具体的日志框架实现配合使用（如log4j、logback），使用接口的好处是当项目需要更换日志框架的时候，只需要更换jar和配置，不需要更改相关java代码。

```java
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TestSlf4j {
    //Logger和LoggerFactory导入的是org.slf4j包
    private final static Logger logger = LoggerFactory.getLogger(TestSlf4j.class);
}
```

log4j、logback、log4j2都是一种日志具体实现框架，所以既可以单独使用也可以结合slf4j一起搭配使用


```java
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
 
public class TestLog4j {
    // Logger和LogManager导入的是org.apache.logging包
    private static final Logger LOG = LogManager.getLogger(TestLog4j.class); 
}
```

## 导入需要使用的jar包(slf4j+log4j2)

#### log4j2

如项目中有导入spring-boot-starter-web依赖包记得去掉spring自带的日志依赖spring-boot-starter-logging

```pom
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-web</artifactId>
    <exclusions>
        <exclusion>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-logging</artifactId>
        </exclusion>
    </exclusions>
</dependency>
```

springboot项目中需导入log4j2

```pom
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-log4j2</artifactId>
</dependency>
```
如果要使用log4j，则把log4j2的坐标替换为下面的这个，依然要排除原有的spring-boot-starter-logging。

```pom
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-log4j</artifactId>
    <version>1.3.8.RELEASE</version>
</dependency>
```

## Java引入log4j配置文件

```java
class ImportConfig{ 
    public static void main(String[] args) throws IOException {  
        File file = new File(log4j2);  
        BufferedInputStream in = new BufferedInputStream(new FileInputStream(file));  
        final ConfigurationSource source = new ConfigurationSource(in);  
        Configurator.initialize(null, source);  
     
        Logger logger = LogManager.getLogger("myLogger");  
    }
}
```

## Springboot整合zookeeper log4j2 
```pom
<dependency>
    <groupId>org.apache.zookeeper</groupId>
    <artifactId>zookeeper</artifactId>
    <version>3.4.14</version>

    <exclusions>
        <exclusion>
            <groupId>org.slf4j</groupId>
            <artifactId>slf4j-log4j12</artifactId>
        </exclusion>
        <exclusion>
            <groupId>log4j</groupId>
            <artifactId>log4j</artifactId>
        </exclusion>
    </exclusions>
</dependency>
```

参考文章：
