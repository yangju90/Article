## Arrays.sort使用

```java
// 降序排列
Arrays.sort(T, new Comparator<T>(){
    public int compare(T o1, T o2){
        return o2.num - o1.num;
    }
});

注意：int[] 数组等需要转换为包装类才可以使用
```



##### 为什么o2 - o1是降序排列？

Arrays.sort使用比较器，当o2-o1 < 0 时，即o2 < o1 两对象进行交换，部分代码：

```java
// 插入排序，寻找有序队列
private static <T> int countRunAndMakeAscending(T[] a, int lo, int hi,
                                                    Comparator<? super T> c) {
        assert lo < hi;
        int runHi = lo + 1;
        if (runHi == hi)
            return 1;

        // Find end of run, and reverse range if descending
        if (c.compare(a[runHi++], a[lo]) < 0) { // Descending
            while (runHi < hi && c.compare(a[runHi], a[runHi - 1]) < 0)
                runHi++;
            reverseRange(a, lo, runHi);
        } else {                              // Ascending
            while (runHi < hi && c.compare(a[runHi], a[runHi - 1]) >= 0)
                runHi++;
        }

        return runHi - lo;
}
```

可以看到o2的下标是小于o1的，所以 o2-o1 是降序。



##### Arrays.sort算法

看到一篇文章[Arrays.sort](https://www.jianshu.com/p/d7ba7d919b80)排序，底层利用插入、快排和归并，可能是jdk1.8版本间的差异，在我本地跟踪的代码算法并不是这样。

通过查看源码，Arrays.sort排序主要使用TimSort排序算法，使排序的时间复杂度更加稳定O(nlogn)，当然也可以根据系统参数，选择使用老式排序算法MergeSort，源码提到后续这个算法会被替代。

###### (1) 传统MergeSort排序

```java
/** To be removed in a future release. */
private static void legacyMergeSort(Object[] a,
                                    int fromIndex, int toIndex) {
    Object[] aux = copyOfRange(a, fromIndex, toIndex);
    mergeSort(aux, a, fromIndex, toIndex, -fromIndex);
}


@SuppressWarnings({"unchecked", "rawtypes"})
private static void mergeSort(Object[] src,
                              Object[] dest,
                              int low,
                              int high,
                              int off) {
    int length = high - low;

    // Insertion sort on smallest arrays
    if (length < INSERTIONSORT_THRESHOLD) {
        for (int i=low; i<high; i++)
            for (int j=i; j>low &&
                 ((Comparable) dest[j-1]).compareTo(dest[j])>0; j--)
                swap(dest, j, j-1);
        return;
    }

    // Recursively sort halves of dest into src
    int destLow  = low;
    int destHigh = high;
    low  += off;
    high += off;
    int mid = (low + high) >>> 1;
    mergeSort(dest, src, low, mid, -off);
    mergeSort(dest, src, mid, high, -off);

    // If list is already sorted, just copy from src to dest.  This is an
    // optimization that results in faster sorts for nearly ordered lists.
    if (((Comparable)src[mid-1]).compareTo(src[mid]) <= 0) {
        System.arraycopy(src, low, dest, destLow, length);
        return;
    }

    // Merge sorted halves (now in src) into dest
    for(int i = destLow, p = low, q = mid; i < destHigh; i++) {
        if (q >= high || p < mid && ((Comparable)src[p]).compareTo(src[q])<=0)
            dest[i] = src[p++];
        else
            dest[i] = src[q++];
    }
}
```

*算法核心：*

1.  当待排区间大小小于**INSERTIONSORT_THRESHOLD == 7** ，使用插入排序；
2.  否则采用递归归并排序。

###### (2) TimSort排序算法

```java
/**
 * Sorts the given range, using the given workspace array slice
 * for temp storage when possible. This method is designed to be
 * invoked from public methods (in class Arrays) after performing
 * any necessary array bounds checks and expanding parameters into
 * the required forms.
 *
 * @param a the array to be sorted
 * @param lo the index of the first element, inclusive, to be sorted
 * @param hi the index of the last element, exclusive, to be sorted
 * @param c the comparator to use
 * @param work a workspace array (slice)
 * @param workBase origin of usable space in work array
 * @param workLen usable size of work array
 * @since 1.8
 */
static <T> void sort(T[] a, int lo, int hi, Comparator<? super T> c,
                     T[] work, int workBase, int workLen) {
    assert c != null && a != null && lo >= 0 && lo <= hi && hi <= a.length;

    int nRemaining  = hi - lo;
    if (nRemaining < 2)
        return;  // Arrays of size 0 and 1 are always sorted

    // If array is small, do a "mini-TimSort" with no merges
    if (nRemaining < MIN_MERGE) {
        int initRunLen = countRunAndMakeAscending(a, lo, hi, c);
        binarySort(a, lo, hi, lo + initRunLen, c);
        return;
    }

    /**
     * March over the array once, left to right, finding natural runs,
     * extending short natural runs to minRun elements, and merging runs
     * to maintain stack invariant.
     */
    TimSort<T> ts = new TimSort<>(a, c, work, workBase, workLen);
    int minRun = minRunLength(nRemaining);
    do {
        // Identify next run
        int runLen = countRunAndMakeAscending(a, lo, hi, c);

        // If run is short, extend to min(minRun, nRemaining)
        if (runLen < minRun) {
            int force = nRemaining <= minRun ? nRemaining : minRun;
            binarySort(a, lo, lo + force, lo + runLen, c);
            runLen = force;
        }

        // Push run onto pending-run stack, and maybe merge
        ts.pushRun(lo, runLen);
        ts.mergeCollapse();

        // Advance to find next run
        lo += runLen;
        nRemaining -= runLen;
    } while (nRemaining != 0);

    // Merge all remaining runs to complete sort
    assert lo == hi;
    ts.mergeForceCollapse();
    assert ts.stackSize == 1;
}
```

*算法核心：*

1.  当待排区间小于**MIN_MERGE == 32**时，使用countRunAndMakeAscending，选找出升序或降序；
2. 调用binarySort进行排序，虽然叫二叉排序，从源代码来看是插入排序（在寻找插入位置时用了二分法）;
3. 定义基本片段长度，调用的是minRunLength，返回的数要么小于16，要么是16，要么介于[16， 32]之间；
4. pushRun、mergeCollapse反复归并一些相邻片段，过程中避免归并长度相差很大的片段，直至整个排序完成，所用分段选择策略可以保证O(n log n)时间复杂性；
5. 什么时候会进行合并呢？之所以进行判断是为了防止这样的情况：1000，10，100，10，10，这是五个分区的长度，最好的情况是先将小的分区合并，最后在和最大的分区合并，这个方法就是这个目的。