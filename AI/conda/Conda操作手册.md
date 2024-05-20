<div align=center><font face="黑体" size=6>Conda操作手册</font></div>

[toc]

# 1. Conda基础环境

#### **1.1 Conda基础命令**

* **创建环境** `conda create -n <env-name>`

* **查看conda已有环境列表**`conda info --envs`

##### (1) 切换conda环境到`base`

```powershell
conda activate
```

##### (2) 安装环境依赖

```shell
# via environment activation
conda activate <env-name>
conda install matplotlib

# via command line option
conda install --name <env-name> matplotlib
```

##### (3) 升级

```shell
# current version
conda --version

conda update conda
```

##### (4) 依赖包下载配置（Specifying channels）

```shell
Channels 是配置下载包位置的 （可以是本地文件，也可以是互联网仓库地址）

文档地址：https://docs.conda.io/projects/conda/en/stable/user-guide/getting-started.html

配置名字为conda-forge的channels地址：
conda install conda-forge::numpy  
```

> 在任何通道中安装最新版本的包：

```shell
conda config --set channel_priority disabled
```

或者 `Add channel_priority: disabled to your .condarc file.`

> 添加优先级最高的新通道

```shell
conda config --add channels new_channel
# 等同于
conda config --prepend channels new_channel

# 添加通道最低优先级
conda config --append channels new_channel

```

#### **1.2 Conda environment管理 **

##### (1) 创建环境安装依赖包

```shell
# 创建conda环境
conda create --name <my-env>
-- OR
conda create -n <my-env>

# 创建环境并安装依赖包
conda create -n <my-env> python=3.9 scipy
-- OR
conda create -n <my-env> python=3.9 
conda install -n <my-env> scipy
```

##### (2) 从yml文件创建环境

```shell
conda env create -f environment.yml

# 在当前目录envs创建conda环境
conda create --prefix ./envs jupyterlab=3.2 matplotlib=3.5 numpy=1.21
# 使用当前目录envs的conda环境
conda activate ./envs
```

##### (3) 从conda环境中导出配置文件

```shell
conda env export > environment.yml

# 跨平台导出
conda env export --from-history > environment.yml
```

导出样例：

```yml
name: stats2
channels:
  - javascript
dependencies:
  - python=3.9
  - bokeh=2.4.2
  - conda-forge::numpy=1.21.*
  - nodejs=16.13.*
  - flask
  - pip
  - pip:
    - Flask-Testing
```

##### (4) 克隆环境

```shell
conda create --name <new-env> --clone <exists-env>

# 导出环境文件<不会检测操作系统平台架构，所以这种方式要保证原始和目标机器环境一致>
conda list --explicit > spec-file.txt
conda create --name myenv --file spec-file.txt
conda install --name myenv --file spec-file.txt
```

#### **1.3 配置文件**

`.condarc`文件，anaconda运行时配置文件，可以配置默认的基础环境包，安装包软件的下载位置。

#### 1.4 管理依赖包

##### （1）搜索依赖包

```shell
conda search scipy

# 默认的channels  defaults
conda search --override-channels --channel defaults scipy

# 通过地址http://conda.anaconda.org/mutirri 安装
conda search --override-channels --channel http://conda.anaconda.org/mutirri iminuit
```

# 2. Conda