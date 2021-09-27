

<center><font size=5 face="黑体">Gradle环境配置</font></center>

一、配置系统环境

1. 配置GRADLE_HOME

```groovy
GRADLE_HOME Gradle软件执行路径，配置 opt/gradle-7.2
然后GRADLE_HOME配置到Path中 %GRADLE_HOME%\bin
```

2. 配置GRADLE_USER_HOME

```groovy
GRADLE_USER_HOME Gradle下载项目依赖库文件的地址，可以配置到Maven资源库位置
```

3. build.gradle 文件解释

```groovy
plugins {
    id 'java'
}

group 'indi.mat.tools.groovy'
version '1.0-SNAPSHOT'

sourceCompatibility = 1.8


/**
 * 指定所使用的仓库，mavenCentral() 表示使用中央仓库
 *  mavenLocal()
 *  mavenCentral()
 *  配置有先后顺序，会先从本地查询，从本地可以加快索引速度
 */
repositories {
    mavenLocal()
    mavenCentral()
}

/**
 * gradle 工程所有的jar包坐标都在dependencies属性内放置
 * 每个Jar包的坐标都有三个基本元素组成 group、name、version
 *
 * testCompile 表示该jar包在测试的时候起作用，该属性为jar包的作用域
 *
 * 每一个jar包都需要带上作用域
 */
dependencies {

    testCompile group: 'junit', name: 'junit', version: '4.12'
    // https://mvnrepository.com/artifact/org.springframework.boot/spring-boot-starter-web
    implementation group: 'org.springframework.boot', name: 'spring-boot-starter-web', version: '2.5.3'

}

```

