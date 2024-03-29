## 一、基本概念

​      响应式编程是一种基于 **异步 数据流** 概念的编程模式，数据流可以被观测、过滤、操作，或者为新的消费者与另外一条流合并为新的流。



​      响应式编程的一个核心概念是事件，事件可以被等待、也同样可以触发其他事件。Observable（被观察者）可以发出事件，事件可以是 网络请求、复杂计算、数据库操作、文件读取等等，事件执行结束后交给Subscribers的回调处理。

​      响应式编程实现的四个角色：**Observable**、**Observer**、**Subscriber** 和 **Subject**。响应式流标准中定义的四个接口

- Subscription 接口定义了连接发布者和订阅者的方法；
- Publisher<T> 接口定义了发布者的方法；
- Subscriber<T> 接口定义了订阅者的方法；
- Processor<T,R> 接口定义了处理器；

## 二、基础知识

### 2.1 入门

（1）生产事件

```java
Observable<String> observable = Observable.create(new ObservableOnSubscribe<String>() {             
    @Override             
    public void subscribe(ObservableEmitter<String> emitter) throws Exception {                 
        // 发送事件                 
        emitter.onNext("Hello World!");                 
        emitter.onNext("dispose sign");                 
        emitter.onNext("I'm marvin");                 
        emitter.onError(new Exception());                 
        // 发送事件完成                 
        emitter.onComplete();                 
        // onComplete后的事件，不发送                 
        emitter.onNext("Message cannot be sent!");
    }  
});
```

（2）事件消费

```java
observable.subscribe((String s) ->{       
    System.out.println(s); 
});
```

（3）线程切换

```java
// 设置生产线程 
observable.subscribeOn(Schedulers.io()); 
// 设置消费线程
observable.observeOn(Schedulers.io());
```

### 2.2 线程切换原理

```java
Observable.create((ObservableEmitter<String> emitter) -> {             
    System.out.println(Thread.currentThread() + " emitter " +  "1");             
    emitter.onNext("1");         
}).map((String s) -> {             
    Integer i = Integer.valueOf(s) + 1;             
    System.out.println(Thread.currentThread() + "Map: String -> Integer" + i);             
    return i;         
}).flatMap((Integer i) -> {             
    System.out.println(Thread.currentThread() + "FlatMap: Integer -> Observable<String + 1>" + i + 1);             
    String s = String.valueOf(i + 1);             
    return Observable.create((ObservableEmitter<String> emitter) -> {                 
        System.out.println(Thread.currentThread() + " emitter " +  s + "1");                 
        emitter.onNext(s+ "1");             
    }).subscribeOn(Schedulers.single()).observeOn(Schedulers.newThread());         
}).map((String s) -> {             
    Integer i = Integer.valueOf(s) + 1;             
    System.out.println(Thread.currentThread() + "Map: String -> Integer" + i);             
    return i;         
}).subscribeOn(Schedulers.io()).observeOn(Schedulers.single())         
    .subscribe(new Observer<Integer>() {             
        @Override             
        public void onSubscribe(Disposable d) {              
        }              
        @Override             
        public void onNext(Integer integer) {                 
            System.out.println(Thread.currentThread() + " Last Print" + integer);             
        }             
        @Override             
        public void onError(Throwable e) {              
        }              
        @Override             
        public void onComplete() {              
        }         
    }); 

输出结果: 
Thread[RxCachedThreadScheduler-1,5,main] emitter 1 
Thread[RxCachedThreadScheduler-1,5,main]Map: String -> Integer2 
Thread[RxCachedThreadScheduler-1,5,main]FlatMap: Integer -> Observable<String + 1>21 
Thread[RxSingleScheduler-1,5,main] emitter 31 
Thread[RxNewThreadScheduler-1,5,main]Map: String -> Integer32 
Thread[RxSingleScheduler-1,5,main] Last Print32
```



```java
public void switchThread() {
    Observable.create((ObservableEmitter<String> emitter) -> {
        System.out.println(Thread.currentThread() + " emitter " + "1");
        emitter.onNext("1");
    }).map((String s) -> {
        Integer i = Integer.valueOf(s) + 1;
        System.out.println(Thread.currentThread() + "Map: String -> Integer" + i);
        return i;
    }).flatMap((Integer i) -> {
        System.out.println(Thread.currentThread() + "FlatMap: Integer -> Observable<String + 1>" + i + 1);
        String s = String.valueOf(i + 1);
        return Observable.create((ObservableEmitter<String> emitter) -> {
            System.out.println(Thread.currentThread() + " emitter " + s + "1");
            forEach(i, new CallBack() {
                @Override
                public void call(String s) {
                    emitter.onNext(s + "1");
                }
            });
        });
    }).map((String s) -> {
        Integer i = Integer.valueOf(s) + 1;
        System.out.println(Thread.currentThread() + "Map: String -> Integer" + i);
        return i;
    }).subscribeOn(Schedulers.newThread()).observeOn(Schedulers.single())
        .subscribe(new Observer<Integer>() {
            @Override
            public void onSubscribe(Disposable d) {
            }

            @Override
            public void onNext(Integer integer) {
                System.out.println(Thread.currentThread() + " Last Print" + integer);
            }

            @Override
            public void onError(Throwable e) {
            }

            @Override
            public void onComplete() {
            }
        });
    try {
        Thread.sleep(2000);
    } catch (InterruptedException e) {
        e.printStackTrace();
    }
}

public void forEach(final int integer, final CallBack back) {
    Observable.create((ObservableEmitter<String> emitter) -> {
        System.out.println("===Observable<String> call: " + Thread.currentThread().getName());
        for (int i = 0; i < integer; i++) {
            emitter.onNext(i + "");
        }
    }).subscribeOn(Schedulers.io()).observeOn(Schedulers.computation()).subscribe(new Observer<String>() {
        @Override
        public void onSubscribe(Disposable d) {
        }

        @Override
        public void onNext(String integer) {
            System.out.println(Thread.currentThread() + " Middle Print" + integer);
            back.call(integer);
        }

        @Override
        public void onError(Throwable e) {
        }

        @Override
        public void onComplete() {
        }
    });
}

interface CallBack {
    void call(String s);
}     

输出结果:  
Thread[RxNewThreadScheduler-1,5,main] emitter 1 
Thread[RxNewThreadScheduler-1,5,main]Map: String -> Integer2 
Thread[RxNewThreadScheduler-1,5,main]FlatMap: Integer -> Observable<String + 1>21 
Thread[RxNewThreadScheduler-1,5,main] emitter 31 ===Observable<String> call: RxCachedThreadScheduler-1 Thread[RxComputationThreadPool-1,5,main] Middle Print0 
Thread[RxComputationThreadPool-1,5,main]Map: String -> Integer2 
Thread[RxComputationThreadPool-1,5,main] Middle Print1 
Thread[RxComputationThreadPool-1,5,main]Map: String -> Integer12 
Thread[RxSingleScheduler-1,5,main] Last Print2 
Thread[RxSingleScheduler-1,5,main] Last Print12
```

线程的切换，需要关注Observable和Observer。

Observable运行的线程由subscribeOn 控制，Observable执行过程中的第一个subscribeOn 生效；Observer运行的线程由observeOn控制， observeOn会对链式调用的之后的造成影响。（总结下来，在那个线程运行，取决于**就近原则**）

（1）线程切换调度器

- Schedulers.io():    io密集型调度器，线程池是一个无数量上限的线程池
- Schedulers.newThread():  开启新线池操作，线程池大小为1
- Schedulers.immediate(): 默认指定线程，也就是当前线程
- Schedulers.computation(): Cpu密集型计算

（2）Observer 对象 Disposable 方法执行线程

​     unsubscribeOn(Schedulers.newThread())



下图为RxJava创建处理数据流的流程，根据DI思想，所有的Observable派生对象在Observable抽象类中完成创建。

<img src="资源\图1.jpg" style="zoom:67%;" />



## 三、RxJava 操作符

### 3.1 变换

### 3.2 操作符

（1）just 和 fromArray 区别

```java
public void justAndFromTest() {         
    Integer[] item = {0, 1, 2, 3, 4, 5};         
    Observable.fromArray(item).subscribe((Integer i) -> {             
        System.out.println(i);         
    });         
    Observable.just(item).subscribe((Integer[] i) -> {             
        System.out.println(i);         
    });     
}
```

（2） map 和 flatMap

- 对象转换，map转换对象，创建新的Observable转换结果发送到Subscriber； 
- flatMap转换为Observable 对象，其实是创建一个新的Observable对象，同时flatMap 变换后产生的每一个Observable对象发送的事件，进而发送给Subscriber回调；

（3）filter 、take 和 doOnNext

```java
public void filterTakeDoOnNextTest() {         
	Integer[] item = {0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12};         
    Observable.fromArray(item)                 
    	// 过滤偶数                 
        .filter((Integer i) -> i%2 == 0)                 
        // 只拿取前两个数据                 
        .take(2)                 
        // 调用onNext 之前会被执行                 
        .doOnNext((Integer i) -> {                     
            System.out.println("doOnNext:" + i);                 
        }).subscribe((Integer i) -> System.out.println("subscribe:" + i));     
}  

输出结果： 
    doOnNext:0 subscribe:0 doOnNext:2 subscribe:2
```



（4）debounce、throttleFirst

<img src="资源\图2.gif" style="zoom:67%;" />

```java
Disposable disposable = Observable.create((ObservableEmitter<Integer> emitter) -> {             
    emitter.onNext(1); // skip             
    Thread.sleep(400);             
    emitter.onNext(2); // skip             
    Thread.sleep(400);             
    emitter.onNext(3); // skip             
    Thread.sleep(100);             
    emitter.onNext(4); // deliver             
    Thread.sleep(605);             
    emitter.onNext(5); // deliver             
    Thread.sleep(510);             
    emitter.onComplete();         
}).debounce(500, TimeUnit.MILLISECONDS)                 
    .subscribeOn(Schedulers.io())                 
    .observeOn(Schedulers.io())                 
    .subscribe((Integer i) -> System.out.println(i));  	

输出结果： 	4 	5
```

<img src="资源\图3.gif" style="zoom:67%;" />

（5）merge、concat、compose

- 执行Observable转换，merge 、concat合并Observable， 多个合并为一个Observable；
- concat多个顺序执行（串行），merge publisher端可以多线程运行（并行）；
- compose 可以获取Observable本身，切换数据流线程


```java
Disposable disposable = Observable.create((ObservableEmitter<Integer> emitter) -> {    
    System.out.println(Thread.currentThread() + " main emitter ");             
    emitter.onNext(1);             
    emitter.onComplete();         
}).compose((Observable<Integer> observable) ->  
           observable.observeOn(Schedulers.newThread()).subscribeOn(Schedulers.newThread())         ).subscribeOn(Schedulers.io()).observeOn(Schedulers.computation())                 
    .subscribe((Integer i) -> {                     
        System.out.println(Thread.currentThread() + " subscribe ");                     
        System.out.println("subscribe:" + i);                 
    });  	

输出结果：  	
Thread[RxNewThreadScheduler-1,5,main] main emitter  	
Thread[RxComputationThreadPool-1,5,main] subscribe  	
subscribe:1 
```

（6）timer、interval

  timeInterval()返回Timed包裝對象，可以获取执行时间

（7）buffer

```java
public void bufferTest() {
    Integer[] item = {0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12};
    Observable.fromArray(item)
        // 过滤偶数
        .buffer(3)
        .subscribe((List<Integer> integers) -> {
            System.out.println(integers);
        });
}
输出结果：
[0, 1, 2]
[3, 4, 5]
[6, 7, 8]
[9, 10, 11]
[12]    
```

（8）reduce

```java
public void reduceTest(){
    Integer[] item = {0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12};
    Observable.fromArray(item)
        .reduce((Integer a, Integer b) -> a + b)
        .subscribe((Integer i) -> System.out.println("subscribe:" + i));
}

输出结果：
subscribe:78
```



### 3.3 背压 BackPressure

- Cold Observables：指的是那些在订阅之后才开始发送事件的Observable（每个Subscriber都能接收到完整的事件）。
- Hot Observables:     指的是那些在创建了Observable之后，（不管是否订阅）就开始发送事件的Observable

>
> 注意：背压这个话题本身，就是生产者与消费者速率不等产生的，所以在RxJava中，生产者、消费者必须在不同的线程，如果在同一个线程则为同步调用。RxJava2 Observable本身不支持背压策略，默认使用内存堆积



（1）Observable 内存堆积Demo

```java
Disposable disposable = Observable.create((ObservableEmitter<int[]> emiter) -> {
    emiter.onNext(new int[1024]);
    emiter.onComplete();
})
    .repeat()
    .subscribeOn(Schedulers.newThread())
    .observeOn(Schedulers.io())
    .subscribe((int[] s) -> {
        System.out.println(s.length);
        Thread.sleep(1000);
    });  
 
输出结果：
1024
1024
1024
1024
Exception: java.lang.OutOfMemoryError thrown from the UncaughtExceptionHandler in thread "RxCachedThreadScheduler-1"
 
Exception: java.lang.OutOfMemoryError thrown from the UncaughtExceptionHandler in thread "RxNewThreadScheduler-1"
 
Exception: java.lang.OutOfMemoryError thrown from the UncaughtExceptionHandler in thread "RxSchedulerPurge-1"
```


（2）Flowable



（3）BackpressStrategy

- BackpressStrategy.ERROR   事件堆积，首先会放入缓存池，缓存池满则抛出异常
- BackpressStrategy.BUFFER 事件堆积，放入无限制的缓存池，OOM
- BackpressStrategy.DROP     事件堆积，超出缓存池的，丢弃多余事件
- BackpressStrategy.LATEST  同Drop，区别在于总是会保留最后一个发送的事件
