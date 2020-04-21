## 集合Map

Map图

Map相关类说明

### HashMap详解

#### JDK1.7 

##### 1. 类

HashMap继承了AbstractMap抽象类，实现了Map接口，Cloneable接口和序列化Serializable接口。



#### JDK1.8

##### hash计算

```java
// hash值的高16位与低16位做异或运算
static final int hash(Object key) {
    int h;
    return (key == null) ? 0 : (h = key.hashCode()) ^ (h >>> 16);
}
```





