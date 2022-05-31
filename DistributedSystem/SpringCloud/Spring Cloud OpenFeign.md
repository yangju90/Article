> https://blog.csdn.net/manzhizhen/article/details/110013311
> https://docs.spring.io/spring-cloud-openfeign/docs/2.2.5.RELEASE/reference/html/#feign-logging

#### 1. 依赖引入

##### 1.1 默认引入
###### 1.1.1 pom 文件
```xml
<dependency>
    <groupId>org.springframework.cloud</groupId>
    <artifactId>spring-cloud-dependencies</artifactId>
    <version>2021.0.0</version>
    <type>pom</type>
    <scope>import</scope>
</dependency>

<dependency>
    <groupId>org.springframework.cloud</groupId>
    <artifactId>spring-cloud-starter-openfeign</artifactId>
</dependency>
```
###### 1.1.2 yaml 文件配置
```yaml
feign:
  client:
    config:
      default:
        connectTimeout: 10000
        readTimeout: 10000
```
###### 1.1.3 FeignConfig类文件
开启Feign日志打印，首先要开启Spring框架的日志为DEBUG, 样例：`logging.level.project.user.UserClient: DEBUG`

```java
@Configuration
public class FeignConfig {

    @Bean
    Logger.Level feignLoggerLevel() {
        return Logger.Level.FULL;
    }


    private class FeignBasicAuthRequestInterceptor implements RequestInterceptor {
        @Override
        public void apply(RequestTemplate requestTemplate) {
            requestTemplate.header("Request_to", "scheduling");
        }
    }

    @Bean
    public RequestInterceptor requestInterceptor() {
        return new FeignBasicAuthRequestInterceptor();
    }
}
```
##### 1.2 fegin 采用OkHttp作为Client
###### 1.1.1 pom 文件
```xml
<dependency>
    <groupId>io.github.openfeign</groupId>
    <artifactId>feign-okhttp</artifactId>
</dependency>
```
###### 1.1.2 yaml 文件配置
```yaml
feign:
  okhttp:
    enabled: true
```
