#### Apache ftpserver集成

---

Apache ftpserver由Java语言开发的Ftp服务器，集成到其它程序，正常运行需要：

- mina-core, 2.0-M3 or later
- slf4j-api
- A SLF4J implementation of your choice, for example slf4j-simple-1.5.3.jar
- ftplet-api
- ftpserver-core

```java
<dependency>
    <groupId>org.slf4j</groupId>
    <artifactId>slf4j-log4j12</artifactId>
    <version>1.7.25 </version>
</dependency>
<dependency>
    <groupId>org.apache.ftpserver</groupId>
    <artifactId>ftpserver-core</artifactId>
    <version>1.1.1</version>
</dependency>
<dependency>
    <groupId>org.apache.ftpserver</groupId>
    <artifactId>ftplet-api</artifactId>
    <version>1.1.1</version>
</dependency>
<dependency>
    <groupId>org.apache.mina</groupId>
    <artifactId>mina-core</artifactId>
    <version>2.0.16</version>
</dependency>
```





#### Springboot应用集成 Apache ftpserver

---

![image-20200324144637183](Apache ftpserver整合springboot-资源\图1.png)



#### 代码

---

```java
@Configuration
public class FtpConfig {
    private final Logger logger = LoggerFactory.getLogger(FtpConfig.class);

    @Bean
    public UserManager getUserManger() throws FtpException {

        FtpServerFactory serverFactory = new FtpServerFactory();
        
        // ftp服务监听的端口号
        ListenerFactory factory = new ListenerFactory();
        factory.setPort(2221);
        serverFactory.addListener("default", factory.createListener());
		
        // 创建用户
        BaseUser user = new BaseUser();
        user.setName("admin");
        user.setPassword("123456");
        user.setHomeDirectory("E:\\FTPServerPath");

        List<Authority> authorities = new ArrayList<Authority>();
        authorities.add(new WritePermission());
        user.setAuthorities(authorities);
        UserManager userManager = serverFactory.getUserManager();
        userManager.save(user);
      
       	serverFactory.setUserManager(userManager);
        
        FtpServer server = serverFactory.createServer();
        server.start();
        logger.info("ftp已经启动！");
 
        return userManager;
    }
}
```

---

```java
@Component
public class SpringUtil implements ApplicationContextAware {
 
    private static ApplicationContext applicationContext;
 
    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
		
        if(SpringUtil.applicationContext == null) {
            SpringUtil.applicationContext = applicationContext;
        }
    }
 	
    //SpringUtils.getApplicationContext()获取applicationContext对象
    public static ApplicationContext getApplicationContext() {
        return applicationContext;
    }
 	
    public static Object getBean(String name){
        return getApplicationContext().getBean(name);
    }
 
    public static <T> T getBean(Class<T> clazz){
        return getApplicationContext().getBean(clazz);
    }
 
    public static <T> T getBean(String name,Class<T> clazz){
        return getApplicationContext().getBean(name, clazz);
    }
 
}
```

