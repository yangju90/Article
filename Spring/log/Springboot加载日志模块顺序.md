## Springboot加载日志模块

#### Spring jcl
Spring jcl 是Springboot连接SLF4J的基础包，实现了对Log4j 2.x API是否存在，以及Spring框架类路径中的SLF4J 1.7api。

#### ServiceLoader
通过配置寻找接口的实现类，实现类的jar包的META-INF下新建一个文件夹services，并在services下新建一个文件，以接口的全限定名为文件名，内容为实现类的全限定名。

参考链接：  
https://www.jianshu.com/p/7601ba434ff4   
https://blog.csdn.net/shi2huang/article/details/80308531   


#### Log4j获取ConfigurationFactory
核心代码
```java
String ALL_TYPES = "*";
for (final ConfigurationFactory factory : getFactories()) {
    final String[] types = factory.getSupportedTypes();
    if (types != null) {
        for (final String type : types) {
            // 只有 XmlConfigurationFactory 符合条件
            if (type.equals(ALL_TYPES)) {
                final Configuration config = factory.getConfiguration(loggerContext, name, configLocation);
                if (config != null) {
                    return config;
                }
            }
        }
    }
}

```

#### Springboot加载log4j2的流程

Springboot 加载log4j2主要分为两个阶段，第一阶段成为start， 第二阶段为initalize

##### start阶段

![dbf文件头2](resource\start.jpg)

* LogAdapter利用Class.forName加载不同日志的实现，使用内部类进行调用
* log4j 通过PluginProcess对 @Plugin注释识别ConfigurationFactory，具体可参考`Springboot配置log4j2爬坑`

##### initalize阶段

初始化阶段，SpringApplication.run()方法的 SpringApplicationRunListeners

![dbf文件头2](resource\initalize.jpg)

* beforeInitialize()， 判断是否以slf4j桥接。

```java
if (isBridgeJulIntoSlf4j()) {
    removeJdkLoggingBridgeHandler();
    SLF4JBridgeHandler.install();
}

// ClassUtils.isPresent() Sping 实现的类似Class.forName功能
ClassUtils.isPresent(BRIDGE_HANDLER, getClassLoader());


```

* LoggingApplicationListener 代码配置onApplicationEvent

```java
public void onApplicationEvent(ApplicationEvent event) {
    // beforeInitalize 阶段
    if (event instanceof ApplicationStartingEvent) {
        onApplicationStartingEvent((ApplicationStartingEvent) event);
    }
    // 环境配置，即initalizeing阶段
    else if (event instanceof ApplicationEnvironmentPreparedEvent) {
        onApplicationEnvironmentPreparedEvent(
                (ApplicationEnvironmentPreparedEvent) event);
    }
    // 程序运行起来后，调用日志阶段
    else if (event instanceof ApplicationPreparedEvent) {
        onApplicationPreparedEvent((ApplicationPreparedEvent) event);
    }
    
    // 程序关闭
    else if (event instanceof ContextClosedEvent && ((ContextClosedEvent) event)
            .getApplicationContext().getParent() == null) {
        onContextClosedEvent();
    }
    
    // 失败
    else if (event instanceof ApplicationFailedEvent) {
        onApplicationFailedEvent();
    }
}

```

* Log4j2判断日志是否为同一实例，利用了identityHashCoder给AppClassLoader做身份哈希判重

* shutdownhook 学习**(有时间在研究)**
```java
private void registerShutdownHookIfNecessary(Environment environment,
        LoggingSystem loggingSystem) {
    boolean registerShutdownHook = environment
            .getProperty(REGISTER_SHUTDOWN_HOOK_PROPERTY, Boolean.class, false);
    if (registerShutdownHook) {
        Runnable shutdownHandler = loggingSystem.getShutdownHandler();
        if (shutdownHandler != null
                && shutdownHookRegistered.compareAndSet(false, true)) {
            registerShutdownHook(new Thread(shutdownHandler));
        }
    }
}

void registerShutdownHook(Thread shutdownHook) {
    Runtime.getRuntime().addShutdownHook(shutdownHook);
}

```