#### 1. 裴蜀定力 

裴蜀定理（或[贝祖定理](https://baike.baidu.com/item/贝祖定理)）得名于法国数学家艾蒂安·裴蜀，说明了对任何[整数](https://baike.baidu.com/item/整数)a、b和它们的最大公约数d，关于[未知数](https://baike.baidu.com/item/未知数)x和y的线性不定方程（称为裴蜀等式）：若a,b是整数,且gcd(a,b)=d，那么对于任意的整数x,y,ax+by都一定是d的倍数，特别地，一定存在整数x,y，使ax+by=d成立。

它的一个重要推论是：a,b[互质](https://baike.baidu.com/item/互质/577412)的充要条件是存在[整数](https://baike.baidu.com/item/整数)x,y使ax+by=1.



**题目：** Leetcode 365

核心算法：求最大公约数

```java
public int maxCommonDivisor(int x, int y){
    int max, min;
    if(x > y){
        max = x;
        min = y;
    }else{
        max = y;
        min = x;
    }
  
	while(max%min != 0){
  		int temp = max % min;
        max = min;
        min = temp;
	}
    return min;
}
    

 
```

