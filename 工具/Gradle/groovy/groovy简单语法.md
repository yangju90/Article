<center><font size=5 face="黑体">Groovy简单语法</font></center>

1. 集合

```groovy
(1) list
def list = ['a', 'b']
list << 'c'
println list.get(2)  // c

(2) map
def map = ['key1':'value1', 'key2':'value2']
map.key3='value3'
printkln map.get('key3') // value3
```

2. 闭包

```groovy
(1) groovy 闭包，可以认为是一段代码块，可以把闭包当作参数来使用
def block1 = {
    println 'hello block'
}

// 定义方法，放里面需要闭包类型的参数, 这里的参数不能引入任何包，在groovy中Closure这个单词就代表闭包
def method1(Closure closure){
    closure()
}

// 调用
method1(block1) // hello block

(2) 定义一个闭包，带参数
def block2 = {
    v -> println 'hello ${v}'  
}

def method(Closure closure){
    closure("method2")
}

method2(block2) // hello method2
```

