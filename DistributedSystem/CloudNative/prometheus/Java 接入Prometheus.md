## 概述

Prometheus 主要通过 Pull 的方式抓取目标服务暴露的监控接口，因此需要配置对应的抓取任务来请求监控数据。在 Kubernetes 生态中，提供了两种任务配置：

- Pod Monitor：基于 Prometheus Operator 抓取 Pod 上对应的监控数据。
- Service Monitor：基于 Prometheus Operator 抓取 Service 对应 Endpoints 上的监控数据。（我们使用的方式）

## Springboot 接入

Prometheus 监控服务基于 Spring Actuator 机制采集数据，结合配套提供的 Grafana Dashboard 可以方便监控应用的状态。

下面步骤适用于 Springboot Boot 2.0 及以上的版本。

### 步骤一：修改 pom 依赖

项目中已经引用 **Spring-boot-starter-web** 的基础上，在 **pom.xml** 文件中添加 **actuator/prometheus** Maven 依赖项。

```xaml
`<dependency>   <groupId>org.springframework.boot</groupId>   <artifactId>spring-boot-starter-actuator</artifactId> </dependency> <dependency>   <groupId>io.micrometer</groupId>   <artifactId>micrometer-registry-prometheus</artifactId> </dependency>`
```

### 步骤二：修改配置

编辑 **resources** 目录下的 **application.yml** 文件，修改 **actuator** 相关的配置暴露 Prometheus 协议的指标数据。

```yaml
`management:   endpoints:     web:       exposure:         include: prometheus  # 打开 Prometheus 的 Web 访问 Path   metrics:     # 下面选项必须打开，以监控 http 请求的 P50/P99/P95 等，具体的时间分布可以根据实际情况设置     distribution:       slo:   # springboot2.3版本版本以上已经将sla删掉，换为slo，之前版本兼容sla         http:           server:             requests: 5ms,10ms,50ms,100ms,200ms,500ms,1s,5s     # 在 Prometheus 中添加特别的 Labels     tags:       # 必须加上对应的应用名，因为需要以应用的维度来查看对应的监控       application: spring-boot-demo`
```

### 步骤三：本地验证

在项目运行之后，可以通过 **http://localhost:8080/actuator/prometheus** （对应的端口号和路径以实际项目为准）访问到指标数据，说明相关的依赖配置已经正确。

#### 注意：

如果发现 **http_server_requests_seconds_bucket** 指标 **le** 数量定义不是自己定义的，可能是版本问题，加上这个配置尝试：

```yaml
`management.metrics.distribution.percentiles-histogram.http.server.requests=false`
```

## Micrometer

[Micrometer ](https://micrometer.io/)为 Java 平台上的性能数据收集提供了一个通用的API，提供多种指标类型，同时支持接入不同的监控系统。

通过下面数据可以看到每个指标后面都跟着一个 Meter（计量器）类型。

```
`# HELP jvm_memory_used_bytes The amount of used memory # TYPE jvm_memory_used_bytes gauge jvm_memory_used_bytes{application="spring-boot-demo",area="nonheap",id="Metaspace",} 3.716988E7 # HELP jvm_threads_states_threads The current number of threads having NEW state # TYPE jvm_threads_states_threads gauge jvm_threads_states_threads{application="spring-boot-demo",state="new",} 0.0 # HELP jvm_gc_live_data_size_bytes Size of old generation memory pool after a full GC # TYPE jvm_gc_live_data_size_bytes gauge jvm_gc_live_data_size_bytes{application="spring-boot-demo",} 1.2780288E7 # TYPE http_server_requests_seconds histogram http_server_requests_seconds_bucket{application="spring-boot-demo",exception="None",method="GET",outcome="SUCCESS",status="200",uri="/actuator",le="0.001",} 0.0 http_server_requests_seconds_bucket{application="spring-boot-demo",exception="None",method="GET",outcome="SUCCESS",status="200",uri="/actuator",le="0.005",} 0.0 # TYPE logback_events_total counter logback_events_total{application="spring-boot-demo",level="info",} 7.0`
```

### 四种计量器：

1. **Counter**（计数器）：表示收集的数据是按照某个趋势（增加/减少）一直变化的。如接口请求总数、请求错误总数、队列数量变化等。
2. **Gauge**（计量仪）：表示瞬时的数据，可以任意变化的。如cpu、memory使用量、network使用量等。
3. **Timer**（计时器）：用来记录事件的持续时间，这个用的比较少。
4. **Distriution summary**（分布概要）：用来记录事件的分布情况，表示一段时间范围对数据进行采样，可以用于统计网络请求平均延时、请求延迟占比等。



## Kubernetes 探针

Springboot  对 Kubernetes 上常用的**就绪探针**和**存活探针**提供支持。

- springboot 官方文档：[application-availability](https://docs.spring.io/spring-boot/docs/current/reference/html/features.html#features.spring-application.application-availability)
- kubernetes 探针文档：[Setting up health checks with readiness and liveness probes](https://confluence.newegg.org/display/DevOps/Setting+up+health+checks+with+readiness+and+liveness+probes)

### Liveness State

应用程序的存活状态表示内部状态是否允许程序正常工作。不正常的状态意味着程序处于无法恢复的状态，基础设置应该重新启动应用程序。

我们线上统一使用 tcp check，检测端口是否可用。避免依赖外部系统而引发的Pod大规模级联重启。

### Readiness State

就绪状态表示程序是否准备好处理流量。

### Configuration

- /actuator/health/liveness
- /actuator/health/readiness

```yaml
`management:   endpoints:     web:       exposure:         include:           - health   endpoint:     health:       probes:         enabled: true`
```



### Managing the availability state

例如，还可以将应用的 “Readiness” 状态导出到一个文件中，使用 Kubernetes 的 “exec Probe” 查看文件是否存在

```java
`import org.springframework.boot.availability.AvailabilityChangeEvent; import org.springframework.boot.availability.ReadinessState; import org.springframework.context.event.EventListener; import org.springframework.stereotype.Component;  @Component public class MyReadinessStateExporter {      @EventListener     public void onStateChange(AvailabilityChangeEvent<ReadinessState> event) {         switch (event.getState()) {         case ACCEPTING_TRAFFIC:             // create file /tmp/healthy             break;         case REFUSING_TRAFFIC:             // remove file /tmp/healthy             break;         }     }  }   `
```






  