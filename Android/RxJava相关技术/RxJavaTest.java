package com.logistics.pda;

import com.logistics.pda.common.util.EsUtil;
import io.reactivex.*;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import io.reactivex.schedulers.Timed;
import io.reactivex.subjects.BehaviorSubject;
import jdk.swing.interop.SwingInterOpUtils;
import org.junit.jupiter.api.Test;
import org.springframework.data.relational.core.sql.In;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Timer;
import java.util.concurrent.Flow;
import java.util.concurrent.TimeUnit;

public class RxJavaTest {
    /**
     * 创建
     */
    @Test
    public void rxHelloWord() {
        // 创建一个Observable
        Observable<String> observable = Observable.create(new ObservableOnSubscribe<String>() {
            @Override
            public void subscribe(ObservableEmitter<String> emitter) throws Exception {
                System.out.println(Thread.currentThread());
                // 发送事件
                emitter.onNext("Hello World!");
                emitter.onNext("dispose sign");
                emitter.onNext("I'm marvin");
//
                // 发送事件完成
//                emitter.onComplete();
                // 发送失败
                emitter.onNext("Message cannot be sent!");

            }
        });


        observable.subscribeOn(Schedulers.io())
                .unsubscribeOn(Schedulers.newThread())
                .observeOn(Schedulers.single())
                .observeOn(Schedulers.io())
                .subscribe(new Observer<String>() {
                    Disposable d;

                    @Override
                    public void onSubscribe(Disposable d) {
                        this.d = d;
                        System.out.println(Thread.currentThread() + "Observe1: " + d.isDisposed());
                    }

                    @Override
                    public void onNext(String s) {
                        System.out.println("Observe1: " + s + Thread.currentThread());
                        if ("dispose sign".equals(s)) {
                            dispose();
                        }
                    }

                    @Override
                    public void onError(Throwable e) {
                        System.out.println("Observe1: " + e);
                        dispose();
                    }

                    @Override
                    public void onComplete() {
                        System.out.println("Observe1: complete!");
                        dispose();
                    }

                    private void dispose() {
                        if (this.d != null && !this.d.isDisposed()) {
                            d.dispose();
                        }
                    }
                });

//        observable.subscribe(new Observer<String>() {
//            Disposable d;
//
//            @Override
//            public void onSubscribe(Disposable d) {
//                this.d = d;
//                System.out.println("Observe2: " + d.isDisposed());
//            }
//
//            @Override
//            public void onNext(String s) {
//                if ("dispose sign".equals(s)) {
//                    dispose();
//                }
//                System.out.println("Observe2: " + s);
//            }
//
//            @Override
//            public void onError(Throwable e) {
//                System.out.println("Observe2: " + e);
//            }
//
//            @Override
//            public void onComplete() {
//                dispose();
//                System.out.println("Observe2: complete!");
//            }
//
//
//            private void dispose() {
//                if (this.d != null && !this.d.isDisposed()) {
//                    d.dispose();
//                }
//            }
//        });

//        observable.subscribe((String s) -> {
//            System.out.println(s);
//        });


        try {
            Thread.sleep(8000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    /**
     * 线程切换
     */

    @Test
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
        }).subscribeOn(Schedulers.io())
                .observeOn(Schedulers.computation())
                .subscribe(new Observer<String>() {
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

    @Test
    public void rxSimpleTest() {

        int COUNT_BITS = Integer.SIZE - 3;
        System.out.println(-1 << COUNT_BITS);//RUNNING
        System.out.println(0 << COUNT_BITS);//SHUTDOWN
        System.out.println(1 << COUNT_BITS);//STOP
        System.out.println(2 << COUNT_BITS);//TIDYING
        System.out.println(3 << COUNT_BITS);//TERMINATED


        Observable<String> observable = Observable.just(1, 2, 3, 4, 5)
                .map((Integer i) -> {
                    return Thread.currentThread() + " number:" + i;
                })
                .subscribeOn(Schedulers.io())
                .observeOn(Schedulers.io());
        observable.subscribe((String str) -> {
            System.out.println(Thread.currentThread() + " " + str);
        });


        observable.observeOn(Schedulers.io()).subscribe((String str) -> {
            System.out.println(Thread.currentThread() + " " + str);
        });

        observable.subscribe(new Observer<String>() {
            @Override
            public void onSubscribe(Disposable disposable) {
                System.out.println(disposable.isDisposed());
            }

            @Override
            public void onNext(String s) {

            }

            @Override
            public void onError(Throwable throwable) {

            }

            @Override
            public void onComplete() {
                System.out.println("close Disposable");
            }
        });


        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void justAndFromTest() {
        Integer[] item = {0, 1, 2, 3, 4, 5};
        Observable.fromArray(item).subscribe((Integer i) -> {
            System.out.println(i);
        });
        Observable.just(item).subscribe((Integer[] i) -> {
            System.out.println(i);
        });
    }

    @Test
    public void filterTakeDoOnNextTest() {
        Integer[] item = {0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12};
        Observable.fromArray(item)
                // 过滤偶数
                .filter((Integer i) -> i % 2 == 0)
                // 只拿取前两个数据
                .take(2)
                // 调用onNext 之前会被执行
                .doOnNext((Integer i) -> {
                    System.out.println("doOnNext:" + i);
                }).subscribe((Integer i) -> System.out.println("subscribe:" + i));
    }

    @Test
    public void debounceTest() {
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

        while (!disposable.isDisposed()) {
            try {
                Thread.sleep(400);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    @Test
    public void composeTest() {
        Disposable disposable = Observable.create((ObservableEmitter<Integer> emitter) -> {
            System.out.println(Thread.currentThread() + " main emitter ");
            emitter.onNext(1);
            emitter.onComplete();
        }).compose((Observable<Integer> observable) ->
                observable.observeOn(Schedulers.newThread()).subscribeOn(Schedulers.newThread())
        ).subscribeOn(Schedulers.io()).observeOn(Schedulers.computation())
                .subscribe((Integer i) -> {
                    System.out.println(Thread.currentThread() + " subscribe ");
                    System.out.println("subscribe:" + i);
                });

        while (!disposable.isDisposed()) {
            try {
                Thread.sleep(400);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }


    @Test
    public void mergeTest() {
        Disposable disposable = Observable.merge(getObservableList(), 1)
                .subscribeOn(Schedulers.io()).observeOn(Schedulers.computation())
                .subscribe((Integer i) -> {
                    System.out.println(Thread.currentThread() + " subscribe ");
                    System.out.println("subscribe:" + i);
                });

        while (!disposable.isDisposed()) {
            try {
                Thread.sleep(400);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    @Test
    public void concatTest() {
        Disposable disposable = Observable.concat(getObservableList())
                .subscribeOn(Schedulers.single()).observeOn(Schedulers.computation())
                .subscribe((Integer i) -> {
                    System.out.println(Thread.currentThread() + " subscribe ");
                    System.out.println("subscribe:" + i);
                });

        while (!disposable.isDisposed()) {
            try {
                Thread.sleep(400);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }


    public List<Observable<Integer>> getObservableList() {
        List<Observable<Integer>> list = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            final Integer index = i;
            list.add(Observable.create((ObservableEmitter<Integer> emitter) -> {
                System.out.println(Thread.currentThread() + " main emitter ");
                emitter.onNext(index * 10 + 1);
                Thread.sleep(new Random().nextInt(1000));
                emitter.onNext(index * 10 + 2);
                Thread.sleep(new Random().nextInt(1000));

                emitter.onComplete();
            }).subscribeOn(Schedulers.io()).observeOn(Schedulers.newThread()));
        }
        return list;
    }


    @Test
    public void timerTest() {
        Disposable disposable = Observable.create((ObservableEmitter<Integer> emitter) -> {
            emitter.onNext(100);
            emitter.onNext(200);
            emitter.onNext(300);
            emitter.onComplete();
        }).timeInterval().subscribe((Timed<Integer> integerTimed) -> {
            System.out.println(Thread.currentThread());
            System.out.println(integerTimed.time());
            System.out.println(integerTimed.value());
            Thread.sleep(2000);
        });
        while (!disposable.isDisposed()) {
            try {
                Thread.sleep(400);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}

