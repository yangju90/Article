##### 1. 二进制正负转换

 负数的二进制 =  正数二进制取反 +1

```shell
-6 转换为二进制
① 转换为对应正数二进制
  0000 0110
② 取反
  1111 1001
③ +1
  1111 1010
```



##### 2. 运算符

| <<   | 二进制左移               |
| ---- | ------------------------ |
| >>   | 二进制右移               |
| >>>  | 二进制无符号右移         |
| &    | 与  (全为真才为真)       |
| \|   | 或（有真则为真）         |
| ^    | 异或（相同为假不同为真） |

**与、或和异或的妙用**

```java
(1)十六进制转换，例如51转换成十六进制
	x+1:
    51&15;
	51>>>4;
	goto x(x<=8);
(2)异或
    c = x^y;
    x == c^y; //true
```





##### 3.标号循环

```java
exter:for(int i=10; i>0; i--){
    for(int j=0; j<10; j++){
        System.out.println("i=" + i + "j=" + j);
        if(i == j) break exter;
    }
}

exter:for(int i=10; i>0; i--){
    for(int j=0; j<10; j++){
        if(i%2 == 0) continue exter;
        System.out.println("i=" + i + "j=" + j);
    }
}
```



##### 4. 哈希碰撞

所谓哈希（hash），就是将不同的输入映射成独一无二的、固定长度的值（又称"哈希值"）。它是最常见的软件运算之一。

如果不同的输入得到了同一个哈希值，就发生了"哈希碰撞"（collision）。

###### (1)计算哈希方法：

- 直接定址法 
- 数字分析法 
- 折叠法 
- 平方取中法 
- 减去法 
- 字符串数值哈希法 
- 旋转法

可参考文章推荐：[Hash算法原理](https://blog.csdn.net/tanggao1314/article/details/51457585)、

###### (2)解决哈希碰撞的方法：

- 开放定址法（线性探测再散列，二次探测再散列，伪随机探测再散列）
- 再哈希法
- **<font color=red>链地址法</font>**(HashMap用的方法)
- 建立一个公共溢出区

哈希相关文章推荐：[哈希碰撞与生日攻击](http://www.ruanyifeng.com/blog/2018/09/hash-collision-and-birthday-attack.html)、[哈希碰撞](https://blog.csdn.net/kjfcpua/article/details/44238757)、[文章](https://blog.csdn.net/u010739551/article/details/81182794)

