# 1. Go基础环境

#### 1.1 Go环境变量配置

* **ENV:** go env 查看go配置的环境策略

* **GOPATH:** go语言下载依赖三方包的默认位置
* **GO111MODULE:** 模式使用go module 开关，设置为on打开 (on/auto/off)

(1) 命令`go mod init xxxx` 初始化项目模块生成`go.mod`文件，记录版本信息。

(2) 命令`go mod tidy`加载依赖模块到`go.mod`文件，生成`go.sum`文件，文件是Go语言项目中用于记录项目依赖项的版本和哈希值的一种锁文。

#### 1.2 Go Module

###### (1) 升级/降级依赖版本 

* 查看依赖版本

  ```shell
  go list -m -versions xxxx
  
  example:
  go list -m -versions github.com/sirupsen/logrus
  ```

* 升级/降级依赖版本
  
  ```
  修改go.mod文件 require版本，执行go mod tidy
  ```

###### (2) go module 构建

* 空导入的作用 `import _ "foo"`

  ```
  包的初始化会按照常量->变量->init函数的次序进行,通常实践中空导入意味着期望依赖包的init函数得到执行，这个init函数中有我们需要的逻辑。
  ```

* vender 模式，辅助解决内网打包问题

#### 1.3 Go 语言执行次序

**main.main函数：**main 包的 main 函数来说，它是用户层逻辑的入口函数，但它却不一定是用户层第一个被执行的函数。

**init函数：**Go包的初始化函数，init函数不能被显式的调用，否则会有编译错误，

![go初始化顺序](resources\go初始化顺序.png)

**main 包依赖 pkg1 和 pkg4 两个包**

第一步，Go 会根据包导入的顺序，先去初始化 main 包的第一个依赖包 pkg1。

第二步，Go 在进行包初始化的过程中，会采用“深度优先”的原则，递归初始化各个包的依赖包。在上图里，pkg1 包依赖 pkg2 包，pkg2 包依赖 pkg3 包，pkg3 没有依赖包，于是 Go 在 pkg3 包中按照“常量 -> 变量 -> init 函数”的顺序先对 pkg3 包进行初始化；

第三步，在 pkg3 包初始化完毕后，Go 会回到 pkg2 包并对 pkg2 包进行初始化，接下来再回到 pkg1 包并对 pkg1 包进行初始化。在调用完 pkg1 包的 init 函数后，Go 就完成了 main 包的第一个依赖包 pkg1 的初始化。

第四步，Go 会初始化 main 包的第二个依赖包 pkg4，pkg4 包的初始化过程与 pkg1 包类似，也是先初始化它的依赖包 pkg5，然后再初始化自身；

第五步，当 Go 初始化完 pkg4 包后也就完成了对 main 包所有依赖包的初始化，接下来初始化 main 包自身。

最后，在 main 包中，Go 同样会按照**“常量 -> 变量 -> init 函数”**的顺序进行初始化，执行完这些初始化工作后才正式进入程序的入口函数 main 函数。