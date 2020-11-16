# Version Springboot 2.11.1

## logging.config参数

logging.config 配置在application.yml中有效，指定路径有效

## log4j2
##### log4j2官网支持多种自动配置方式，自动扫描：   
系统属性(Log4j.configuration) -> classpath中的(log4j2-test.properties)
-> classpath中的(log4j2-test.yml/yaml/json/jsn/xml) 
-> classpath中的(log4j2.yml/yaml/json/jsn/xml) -> 默认配置

##### Log4j获取ConfigurationFactory

```java
// 按照测试前缀log4j2-test 和 LoggerContext上下文实例获取ConfigurationFactory
Configuration config = getConfiguration(loggerContext, true, name);
if (config == null) {
    // 按照测试前缀log4j2-test 获取ConfigurationFactory
    config = getConfiguration(loggerContext, true, null);
    if (config == null) {
        // 按照前缀log4j2 和LoggerContext上下文实例获取ConfigurationFactory
        config = getConfiguration(loggerContext, false, name);
        if (config == null) {
            // 按照前缀log4j2 获取ConfigurationFactory
            // Springboot 加载Log4j2的工厂实例在这里获取，指向Springboot包log4j2.springboot中的SpringBootConfigurationFactory
            config = getConfiguration(loggerContext, false, null);
        }
    }
}
```

##### log4j2

核心代码如下：
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

##### springboot logging
springboot使用Common logging组件加载日志模块，提供了Java Util、Log4J2和Logback日志的默认配置，预先配置了控制台输出，还提供可选的文件输出。

springboot默认日志配置文件   

| Logging System          | Customization                                                |
| :---------------------- | :----------------------------------------------------------- |
| Logback                 | `logback-spring.xml`, `logback-spring.groovy`, `logback.xml`, or `logback.groovy` |
| Log4j2                  | `log4j2-spring.xml` or `log4j2.xml`                          |
| JDK (Java Util Logging) | `logging.properties`                                         |

##### Springboot log4j2配置工厂
log4j2 加载log4j-core中的ConfigurationFactory继承类，排序如下：
* PropertiesConfigurationFactory
* YamlConfigurationFactory
* JsonConfigurationFactory
* XmlConfigurationFactory
* SpringBootConfigurationFactory (匹配.springboot) 



## springboot 中修改了log4j2.xml的名称后不生效

log4j2.xml文件修改名称为log4j2_dev.xml同时在appliaction_dev.yml配置文件中指定了`logging.config`路径，但是结果还是走spring默认配置。

##### 原因解释

1. springboot log4j2读取顺序：logging.config —> classpath(`log4j2-spring.xml` or `log4j2.xml`) —>  spring-boot包下的logging.log4j.log4j2.xml

2. springBoot项目中默认有一份日志配置文件，项目启动时先读取到了默认日志配置文件，没有读取resource目录中的配置文件，需要自定义日志信息的话需要在Springboot配置文件中指定读取自定义的配置文件

##### 不能读取Yml配置原因

1. 在Log4J2装载时，会检查jackson中的ObjectMapper、JsonNode、JsonParser和YAMLFactory，装载YAMLFactory会失败，所以无法解析yml配置的yml文件，导致失败
```java
public class YamlConfigurationFactory extends ConfigurationFactory {

    /**
     * The file extensions supported by this factory.
     */
    private static final String[] SUFFIXES = new String[] {".yml", ".yaml"};

    private static final String[] dependencies = new String[] {
            "com.fasterxml.jackson.databind.ObjectMapper",
            "com.fasterxml.jackson.databind.JsonNode",
            "com.fasterxml.jackson.core.JsonParser",
            "com.fasterxml.jackson.dataformat.yaml.YAMLFactory"
    };

    private final boolean isActive;

    public YamlConfigurationFactory() {
        for (final String dependency : dependencies) {
            if (!Loader.isClassAvailable(dependency)) {
                LOGGER.debug("Missing dependencies for Yaml support, ConfigurationFactory {} is inactive", getClass().getName());
                isActive = false;
                return;
            }
        }
        isActive = true;
    }

```

#####  测试过程中

将logging.config配置写入application.yml中发现可以读取配置位置
```java

public class LoggingApplicationListener{
    // logFile 指定日志文件输出位置
    private void initializeSystem(ConfigurableEnvironment environment,
    			LoggingSystem system, LogFile logFile) {
    		LoggingInitializationContext initializationContext = new LoggingInitializationContext(
    				environment);
    		// 可以读取到配置
    		String logConfig = environment.getProperty(CONFIG_PROPERTY);
    		if (ignoreLogConfig(logConfig)) {
    			system.initialize(initializationContext, null, logFile);
    		}
            ...
    	}
}
```


##### Spring支持Log4j2默认配置的代码
```java
public abstract class AbstractLoggingSystem extends LoggingSystem {
    // Log4j 初始化
    public void initialize(LoggingInitializationContext initializationContext,
			String configLocation, LogFile logFile) {
		if (StringUtils.hasLength(configLocation)) {
		    
			initializeWithSpecificConfig(initializationContext, configLocation, logFile);
			return;
		}
		// 无配置，调用默认配置
		initializeWithConventions(initializationContext, logFile);
	}



    private String[] getCurrentlySupportedConfigLocations() {
        List<String> supportedConfigLocations = new ArrayList<>();
        if (isClassAvailable("com.fasterxml.jackson.dataformat.yaml.YAMLParser")) {
            Collections.addAll(supportedConfigLocations, "log4j2.yaml", "log4j2.yml");
        }
        if (isClassAvailable("com.fasterxml.jackson.databind.ObjectMapper")) {
            Collections.addAll(supportedConfigLocations, "log4j2.json", "log4j2.jsn");
        }
        supportedConfigLocations.add("log4j2.xml");
        return StringUtils.toStringArray(supportedConfigLocations);
    }
    
    protected String[] getSpringConfigLocations() {
        String[] locations = getStandardConfigLocations();
        for (int i = 0; i < locations.length; i++) {
            String extension = StringUtils.getFilenameExtension(locations[i]);
            locations[i] = locations[i].substring(0,
                    locations[i].length() - extension.length() - 1) + "-spring."
                    + extension;
        }
        return locations;
    }
}

```

##### Spring最终配置文件位置

Spring通过默认配置查找，classpath路径下的配置文件，如果没有找到，就会加载spring-boot包下的logging.log4j.log4j2.xml，如果配置了日志输出位置，则为log4j2-file.xml