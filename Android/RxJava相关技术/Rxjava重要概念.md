Rxjava重要概念

- Observable、Flowable、Single、Completable、Maybe都是被观察者
- Flowable是支持背压的一种观察者
- Single、Completable、Maybe 是简化版的Observable
- 几种被观察者通过toObservable / toFlowable / toSingle / toCompletable / toMaybe相互转换



subscribe 订阅：观察者和被观察者建立关联的操作

