## ByteOrder

ByteOrder BIG_ENDIAN    代表大字节序的ByteOrder。
ByteOrder LITTLE_ENDIAN 代表小字节序的ByteOrder。
ByteOrder nativeOrder()    返回当前硬件平台的字节序。

```java
ByteBuffer buf = ByteBuffer.allocate(1024);
buf.order(ByteOrder.LITTLE_ENDIAN);
buf.putInt(0, 509);
// 小字节序，低位先入数组，buf.get(0) == -3  buf.get(1) == 1 buf.get(2) == 0 buf.get(3) == 0

buf.order(ByteOrder.BIG_ENDIAN);
buf.putInt(0, 509);
// 大字节序，高位先入数组，buf.get(3) == -3  buf.get(2) == 1 buf.get(1) == 0  buf.get(0) == 0
```

ByteBuffer.remaining() 

ByteBuffer.compact()

```java
（1）clear是把position=0，limit=capcity等，也就是说，除了内部数组，其他属性都还原到buffer创建时的初始值，而内部数组的数据虽然没赋为null，但只要不在clear之后误用buffer.get就不会有问题，正确用法是使用buffer.put从头开始写入数据;

（2）而compcat是把buffer中内部数组剩余未读取的数据复制到该数组从索引为0开始，然后position设置为复制剩余数据后的最后一位元素的索引+1，limit设置为capcity，此时在0~position之间是未读数据，而position~limit之间是buffer的剩余空间，可以put数据。
```

## RandomAccessFile

#####  修改读写位置

```java
RandomAccessFile file;
file.seek(n);  // 定位读写位置到n处，以byte计算

FileChannel fc =  file.getChannel();
fc.position(); // 返回当前读写位置
fc.position(n); // 定位读写位置到n处，同file.seek;
```

##### 内存映射修改文件

```java
// 使用MappedByteBuffer会产生关闭文件后不可自动释放的句柄，需要显示关闭
MappedByteBuffer mbb = fc.map(FileChannel.MapMode.READ_WRITE, 0, f.length()); 
mbb.order(ByteOrder.LITTLE_ENDIAN);
mbb.putInt(5);
// 因为在Java8中这个问题没有有效的解决办法，可参考Kafka的读写问题	https://blog.csdn.net/chouzhunle5574/article/details/100946496

mbb.force(); // 尽量不要使用，会强制内存中的内容刷入硬盘

// 临时的办法
((DirectBuffer)mbb).cleaner().clean();
// 或者

public final class DirectByteBufferCleaner {
  private DirectByteBufferCleaner() {}

  public static void clean(final ByteBuffer byteBuffer) {
    if (!byteBuffer.isDirect()) return;
    try {
      Object cleaner = invoke(byteBuffer, "cleaner");
      invoke(cleaner, "clean");
    } catch (Exception e) { /* ignore */ }
  }

  private static Object invoke(final Object target, String methodName) throws Exception {
    final Method method = target.getClass().getMethod(methodName);
    return AccessController.doPrivileged(new PrivilegedAction<Object>() {
      @Override
      public Object run() {
        try {
          return method.invoke(target);
        } catch (Exception e) {
          throw new RuntimeException(e);
        }
      }
    });
  }
}
```

-  Java语言通过java.nio包支持内存映射文件和IO。
- 内存映射文件用于对性能要求高的系统中，如繁忙的电子交易系统
- 使用内存映射IO你可以将文件的一部分加载到内存中
- 如果被请求的页面不在内存中，内存映射文件会导致页面错误
- 将一个文件区间映射到内存中的能力取决于内存的可寻址范围。在32位机器中，不能超过4GB，即2^32比特。
- Java中的内存映射文件比流IO要快(译注：对于大文件而言是对的，小文件则未必）
- 用于加载文件的内存在Java的堆内存之外，存在于共享内存中，允许两个不同进程访问文件。顺便说一下，这依赖于你用的是direct还是non-direct字节缓存。
- 读写内存映射文件是操作系统来负责的，因此，即使你的Java程序在写入内存后就挂掉了，只要操作系统工作正常，数据就会写入磁盘。
- Direct字节缓存比non-direct字节缓存性能要好
- 不要经常调用MappedByteBuffer.force()方法，这个方法强制操作系统将内存中的内容写入硬盘，所以如果你在每次写内存映射文件后都调用force()方法，你就不能真正从内存映射文件中获益，而是跟disk IO差不多。
- 如果电源故障或者主机瘫痪，有可能内存映射文件还没有写入磁盘，意味着可能会丢失一些关键数据。
- MappedByteBuffer和文件映射在缓存被GC之前都是有效的。sun.misc.Cleaner可能是清除内存映射文件的唯一选择。
- 初始化 DirectByteBuffer对象时，如果当前堆外内存的条件很苛刻时，会主动调用 System.gc()强制执行FGC。所以一般建议在使用netty时开启XX:+DisableExplicitGC

