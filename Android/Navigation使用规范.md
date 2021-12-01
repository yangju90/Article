### 1.Navigation使用
#### 1.1 相关模块

在Navigtion使用中相关的JetPack组件，包括LifeCycles中的ViewModel、LiveData，根据MVVM开发模式，使用单Activity进行项目开发过程中的问题进行简单说明。

包括了：组件的使用、ViewModel的使用、 项目开发需要遵守的规范、其他注意事项

图1


#### 1.2 组件使用
#####（1）Navigation组件使用
###### 1.NavGraph

   res文件上创建，Android Resource File， 选择Resource Type 为 Navigation,创建。进入navigation中进行操作，为可视化界面。
   
图2



###### 2.NavHost

   layout 资源文件中，使用androidx.fragment.app.FragmentContainerView 组件

图3

###### 3.NavController

   NavController相当于Navigation的控制器，类似于MVC中的C的作用。

**Activity获取NavController**
```java
@Override
protected void onCreate(Bundle savedInstanceState) {
        ....

        NavHostFragment navHostFragment =
                (NavHostFragment) getSupportFragmentManager().findFragmentById(binding.fragmentContainerView.getId());
        NavController navController = navHostFragment.getNavController();
 
        AppBarConfiguration appBarConfiguration =
                new AppBarConfiguration.Builder(R.id.homeFragment).build();
 
        NavigationUI.setupWithNavController(binding.appBar, navController, appBarConfiguration);

}
```

**Fragment获取NavController**
```java
@Override
protected void onCreate(Bundle savedInstanceState) {
       ....

       NavController navController = Navigation.findNavController(view);
}
```


#####（2）ViewModel组件使用
###### 1.ViewModel实例化

**ViewModel实例化**
```java 
// Activity 中
new ViewModelProvider(this).get(T.class);
// Fragment 中
new ViewModelProvider(getActivity()).get(T.class);

// ViewModel 中需要有数据库或者Http请求的
LViewModelProviders.of(this, T.class);
```


###### 2.DataBinding 绑定使用

a. 修改layout xml文件为DataBinding


**activity_main.xml**
```xml
<layout xmlns:android="http://schemas.android.com/apk/res/android"
xmlns:app="http://schemas.android.com/apk/res-auto"
xmlns:tools="http://schemas.android.com/tools">

    <data>
        <!- 设置变量 -->
        <variable name="t" type="T" />
    </data>
 
    <androidx.constraintlayout.widget.ConstraintLayout/>
 
    ...
</layout>
```



b.代码中传入ViewModel

**DataBinding传入对象**
```java
@Override
protected void onCreate(Bundle savedInstanceState) {
super.onCreate(savedInstanceState);

    binding = DataBindingUtil.setContentView(this, R.layout.activity_main);
 
    t = LViewModelProviders.of(this, T.class);
    binding.setT(t);
    binding.setLifecycleOwner(this);
}
```


#### 1.3 项目中需要遵守的规范
开发中请首先参考项目里的ReadMe文件，遵守开发基本规范，下面是Navigation组件中，开发需要遵守的。

#####（1）基类继承


1.采用单Activity开发项目，除MainActivity外不能创建任何Activity，MainActivity需要继承BaseActivity（如有特殊需求，再进行讨论）
2.Fragment创建均需要继承于BaseFragment
3.ViewModel目前使用两种（BaseViewModel和BaseNavModel），下面会进行说名

#####（2）Activity 和 Fragment中的方法

所有基类中继承的方法，不要忘记super方法调用，此次架构并没有采用抽象类作为基类


setObserver
setListener
createNavViewModel
Activity
所有的观察对象在这个方法中定义, ex:

@Override
public void setListener() {
super.setListener();
binding.button.setOnClickListener((View view) -> {
....
});
}
所有的View监听在这个方法中定义,ex:

@Override
public void setObserver() {
super.setObserver();
binding.button.setOnClickListener((View view) -> {
....
});
}
不存在这个方法
Fragment
createNavViewModel(R.id.home_nav_graph, T.class);

##### (3) BaseNavModel 和BaseViewModel类

BaseNavModel	BaseViewModel
o. 共同点
因为采用单页面，Activity中的ViewModel定义，会提前定义好，如果必须要添加，需要讨论。

都在Fragment中使用

a. 区别
ViewModelProvider()；创建

不存在  lifecycleOwner

LViewModelProviders.of 创建

存在 lifecycleOwner

b.使用
主要用于页面间的数据传递，绑定NavGraph生命周期

createNavViewModel(R.id.home_nav_graph, T.class);

采用的是单页面前提，所以仅用于和Fragment绑定，所以创建方法为

LViewModelProviders.of(this, T.class)
c.生命周期
生命周期消亡跟随 Navigtion中的NavGraph栈

生命周期跟随Fragment本身


##### （4） NavGraph使用



所有的顺序路由关系，必须在NavGraph中定义，确保popUpTo弹出栈定义正确，应该在当前的Navigation栈内能够找到，否者页面跳转会出现不正确

a. 一般顺序跳转代码中禁止调用R.id.xxx进行跳转，首先在Navgation 可视化界面创建action

b. 跳转样例
```java 
NavDirections navDirections = MoveTaskSuccessFragmentDirections.actionMoveTaskSuccessFragmentToMoveTaskFragment();
Navigation.findNavController(view).navigate(navDirections);
```

#####（5）Navigation跨页面跳转

a. Navigation 栈中不存在此页面，PendingIntent 和 Deep Linkes 解决，目前项目基本不会存在这种情况，不做讨论，可参考 https://developer.android.google.cn/guide/navigation/navigation-deep-link

b. Navigaion栈中存在此页面，这种情况一般出现在跨nav_graph，无法定义action时

出现这种情况，会定义Enum类型，所有的跳转逻辑均必须写在MianActivity中，通过setObserver() 监控变化，跳转代码

```java
NavOptions navOptions = new NavOptions.Builder().setPopUpTo(R.id.home_nav_graph, true).build();
NavHostFragment navHostFragment =
(NavHostFragment) getSupportFragmentManager().findFragmentById(binding.fragmentContainerView.getId());
navHostFragment.getNavController().navigate(R.id.loginFragment, null, navOptions);
```
