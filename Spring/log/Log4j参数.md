## rootLogger与rootCategory
rootLogger是新的使用名称，对应Logger类

rootCategory是旧的使用名称，对应原来的Category类

Logger类是Category类的子类，所以，rootCategory是旧的用法，不推荐使用

```xml
<?xml version="1.0" encoding="UTF-8"?>
<configuration>
    <Properties>
        <Property name="PID">??????</Property>
        <Property name="log4j.skipJansi">false</Property>
        <Property name="LOG_LEVEL_PATTERN">%5p</Property>
        <Property name="LOG_EXCEPTION_CONVERSION_WORD">%xwEx</Property>
        <Property name="LOG_DATEFORMAT_PATTERN">yyyy-MM-dd HH:mm:ss.SSS</Property>
        <!-- %-20.20t 输出线程日志，占位20，最大20， %-20t 占位20,可扩展 -->
        <!-- {1.} 日志输出，报名缩写，只去第一位 -->
        <!-- log4j2 开启彩色打印，需要在VM中设置系统属性值 -Dlog4j.skipJansi=false-->
        <property name="LOG_PATTERN" value="%d{${LOG_DATEFORMAT_PATTERN}} %highlight{${LOG_LEVEL_PATTERN}} ${sys:PID} --- [%-20t] %green{%c{1.}} : %m%n%highlight{${sys:LOG_EXCEPTION_CONVERSION_WORD}}"/>
        <property name="LOG_FILE_PATTERN" value="%d{${LOG_DATEFORMAT_PATTERN}} ${LOG_LEVEL_PATTERN} ${sys:PID} --- [%t] %c{1.} : %m%n${sys:LOG_EXCEPTION_CONVERSION_WORD}"/>
        <property name="FILE_PATH" value="../logs"/>
        <property name="FILE_NAME" value="demo"/>
    </Properties>

    <appenders>

        <console name="Console" target="SYSTEM_OUT">
            <PatternLayout pattern="${LOG_PATTERN}"/>
            <!--控制台只输出level及其以上级别的信息（onMatch），其他的直接拒绝（onMismatch）-->
            <ThresholdFilter level="info" onMatch="ACCEPT" onMismatch="DENY"/>
        </console>

        <!--文件会打印出所有信息，这个log每次运行程序会自动清空，由append属性决定，适合临时测试用-->
        <File name="Filelog" fileName="${FILE_PATH}/${FILE_NAME}.log" append="false">
            <PatternLayout pattern="${LOG_FILE_PATTERN}"/>
        </File>

         <!--这个会打印出所有的info及以下级别的信息，每次大小超过size，则这size大小的日志会自动存入按年份-月份建立的文件夹下面并进行压缩，作为存档-->
        <!-- 日志划分与，日志命名有关系，最后一位为1天时，就按照天划分，最后一位为分钟秒钟时，就以分钟秒钟划分， 如%d{yyyy-MM-dd-HH-mm-ss} -->
        <RollingRandomAccessFile name="RollingFileInfo" fileName="${FILE_PATH}/info.log" append="true"
                     filePattern="${FILE_PATH}/%d{yyyy-MM-dd}/${FILE_NAME}-INFO-%d{yyyy-MM-dd}_%i.log.gz">
            <!--控制台只输出level及以上级别的信息（onMatch），其他的直接拒绝（onMismatch）-->
            <ThresholdFilter level="info" onMatch="ACCEPT" onMismatch="DENY"/>
            <PatternLayout pattern="${LOG_FILE_PATTERN}"/>
            <Policies>
                <!--interval属性用来指定多久滚动一次，默认是1 ，这里为1天1个日志-->
                <TimeBasedTriggeringPolicy interval="1"/>
                <SizeBasedTriggeringPolicy size="1KB"/>
            </Policies>
             <!--DefaultRolloverStrategy属性如不设置，则默认为最多同一文件夹下7个文件开始覆盖-->
            <!-- max='4' 控制压缩日志中的%i, 设定最大值，大于设定值则覆盖 -->
            <DefaultRolloverStrategy max="4"/>

            <!--暂时不知道作用-->
            <!--<DitectRolloverStrategy maxFiles="-1"/>-->
        </RollingRandomAccessFile>
    </appenders>

    <!--Logger节点用来单独指定日志的形式，比如要为指定包下的class指定不同的日志级别等。-->
    <!--然后定义loggers，只有定义了logger并引入的appender，appender才会生效-->
    <loggers>

        <!--过滤掉spring和mybatis的一些无用的DEBUG信息,只在Console中输出-->
        <logger name="org.mybatis" level="info" additivity="false">
            <AppenderRef ref="Console"/>
        </logger>
        <!--监控系统信息-->
        <!--若是additivity设为false，则 子Logger 只会在自己的appender里输出，而不会在 父Logger 的appender里输出。-->
        <Logger name="org.springframework" level="info" additivity="false">
            <AppenderRef ref="Console"/>
        </Logger>

        <root level="info">
            <appender-ref ref="Console"/>
            <appender-ref ref="Filelog"/>
            <appender-ref ref="RollingFileInfo"/>
        </root>
    </loggers>
</configuration>

```

---

## 参数介绍

#### 1.日志级别

> 机制：如果一条日志信息的级别大于等于配置文件的级别，就记录。

* trace：追踪，就是程序推进一下，可以写个trace输出
* debug：调试，一般作为最低级别，trace基本不用。
* info：输出重要的信息，使用较多
* warn：警告，有些信息不是错误信息，但也要给程序员一些提示。
* error：错误信息。用的也很多。
* fatal：致命错误。

#### 2.输出源

* CONSOLE（输出到控制台）
* FILE（输出到文件）

#### 3.格式

* SimpleLayout：以简单的形式显示
* HTMLLayout：以HTML表格显示
* PatternLayout：自定义形式显示

PatternLayout自定义日志布局：

```
%d{yyyy-MM-dd HH:mm:ss, SSS} : 日志生产时间,输出到毫秒的时间
%-5level : 输出日志级别，-5表示左对齐并且固定输出5个字符，如果不足在右边补0
%c : logger的名称(%logger)
%t : 输出当前线程名称
%p : 日志输出格式
%m : 日志内容，即 logger.info("message")
%n : 换行符
%C : Java类名(%F)
%L : 行号
%M : 方法名
%l : 输出语句所在的行数, 包括类名、方法名、文件名、行数
hostName : 本地机器名
hostAddress : 本地ip地址
```

#### 4.Log4j配置详解

##### 1.根节点Configuration
有两个属性:
status  
monitorinterval  
有两个子节点:   
* Appenders
* Loggers(表明可以定义多个Appender和Logger).

status用来指定log4j本身的打印日志的级别.    
monitorinterval用于指定log4j自动重新配置的监测间隔时间，单位是s,最小是5s.

##### 2.Appenders节点

常见的有三种子节点:Console、RollingFile、File

**Console节点用来定义输出到控制台的Appender.**

* name:指定Appender的名字.    
* target:SYSTEM_OUT 或 SYSTEM_ERR,一般只设置默认:SYSTEM_OUT.    
* PatternLayout:输出格式，不设置默认为:%m%n.    

**File节点用来定义输出到指定位置的文件的Appender.**    

* name:指定Appender的名字.
* fileName:指定输出日志的目的文件带全路径的文件名.
* PatternLayout:输出格式，不设置默认为:%m%n.

**RollingFile节点用来定义按照条件存储日志，包括日志大小、日志存储时间、日志存储格式、删除策略等.**

* name:指定Appender的名字.
* fileName:指定输出日志的目的文件带全路径的文件名.
* PatternLayout:输出格式，不设置默认为:%m%n.
* filePattern : 指定当发生Rolling时，文件的转移和重命名规则.
* Policies:指定滚动日志的策略，就是什么时候进行新建日志文件输出日志.
* TimeBasedTriggeringPolicy:Policies子节点，基于时间的滚动策略，interval属性用来指定多久滚动一次，默认是1 hour。modulate=true用来调整时间：比如现在是早上3am，interval是4，那么第一次滚动是在4am，接着是8am，12am...而不是7am.
* SizeBasedTriggeringPolicy:Policies子节点，基于指定文件大小的滚动策略，size属性用来定义每个日志文件的大小.
* DefaultRolloverStrategy:用来指定同一个文件夹下最多有几个日志文件时开始删除最旧的，创建新的(通过max属性)。

**Loggers节点，常见的有两种:Root和Logger.**

Root节点用来指定项目的根日志，如果没有单独指定Logger，那么就会默认使用该Root日志输出

* level:日志输出级别，共有8个级别，按照从低到高为：All < Trace < Debug < Info < Warn < Error < AppenderRef：Root的子节点，用来指定该日志输出到哪个Appender.
* Logger节点用来单独指定日志的形式，比如要为指定包下的class指定不同的日志级别等。
* level:日志输出级别，共有8个级别，按照从低到高为：All < Trace < Debug < Info < Warn < Error < Fatal < OFF.
* name:用来指定该Logger所适用的类或者类所在的包全路径,继承自Root节点.
* AppenderRef：Logger的子节点，用来指定该日志输出到哪个Appender,如果没有指定，就会默认继承自Root.如果指定了，那么会在指定的这个Appender和Root的Appender中都会输出，此时我们可以设置Logger的additivity="false"只在自定义的Appender中进行输出。

参考内容：
https://www.cnblogs.com/keeya/p/10101547.html
http://logging.apache.org/log4j/log4j-2.12.1/
