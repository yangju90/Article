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
        connectTimeout: 10000   # 超时时间connectTimeout和ReadTimeout需要同时开启
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
##### 1.3 fegin 请求异常处理
<font color=red><b>ErrorDecoder</b></font>

```java
@Component
public class FeignDecoder implements ErrorDecoder {

    private static final Logger logger = LoggerFactory.getLogger(FeignDecoder.class);

    @Override
    public Exception decode(String s, Response response) {
        try {
            StringBuilder sb = new StringBuilder();
            sb.append("message: " + "Response status" + response.status() +", ");
            sb.append("url: " + response.request().url() +", ");
            byte[] messageByte =response.body().asInputStream().readAllBytes();
            String message = new String(messageByte,"UTF-8");
            sb.append("body: " + message +". ");
            return new BusinessException(sb.toString());
        } catch (Exception e) {
            if(e instanceof  BusinessException){
                return e;
            }else{
                return new BusinessException("feign client request error ", e);
            }
        }
    }
}
```
