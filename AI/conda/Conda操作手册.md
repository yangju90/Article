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

#### 2.1 Docker地址

> https://hub.docker.com/  Docker hub 仓库地址

* docker 推送本地镜像到 本地Harbor

  ```shell
  # 本地image由 Dockerhub拉取，
  [dockerhub]/enlin/notebook:v1
  
  # 给镜像打标签，管理和推送镜像到不同的仓库
  docker tag [SOURCE_IMAGE[:TAG]] [TARGET_IMAGE[:TAG]]
  
  TARGET_IMAGE[:TAG] 是你想要设置的目标镜像的名称和标签。这通常包括仓库地址、项目名称、镜像名称和标签。
  
  docker tag [dockerhub]/enlin/notebook:v1 [harbor]/serving/notebook:v1
  # 推送到仓库
  docker push [TARGET_IMAGE[:TAG]]
  
  docker push  [harbor]/serving/notebook:v1
  ```
* docker查看当前容器PID进程

  ```shell
  docker inspect --format '{{.State.Pid}}' Container_id
  ```
  
#### 2.2 安装jupyterlab

```shell
# conda 安装
conda install jupyterlab

# 生成配置文件
jupyter lab --generate-config

# 寻找配置文件位置
jupyter --config-dir

# 启动指定配置文件位置
jupyter lab --config=/path/jupyter_notebook_config.py

# 启动
jupyter lab
```

* jupyterlab 修改配置

  * 命令生成配置文件 `jupyter lab --generate-config` ， 生成配置文件 jupyter_notebook_config.py
  * 配置一般生成在当前用户目录`.jupyter`下

* 端口和地址修改

  ```python
  # 设置 JupyterLab 监听的端口
  c.ServerApp.port = 8888
  
  # 设置 JupyterLab 监听的 IP 地址
  c.ServerApp.ip = 'localhost'  # 本机
  c.ServerApp.ip = '0.0.0.0'  # 任意地址
  ```

* 认证和令牌

  ```python
  # 禁用令牌认证，警告：这会使你的 JupyterLab 实例更容易受到未经授权的访问
  c.ServerApp.token = ''
  
  # 设置是否自动生成新的令牌
  c.ServerApp.open_browser = False
  ```

* 工作目录

  ```python
  # 设置 JupyterLab 启动时的默认工作目录
  c.ServerApp.root_dir = '/path/to/your/working/directory'
  ```

* 自定义url前缀

  ```python
  # 设置 JupyterLab 的 URL 前缀，例如如果你想要通过 `/jlab` 访问 JupyterLab
  c.ServerApp.base_url = '/jlab'
  ```

* 启用/禁用扩展

  ```python
  # 禁用指定的扩展
  c.LabApp.disabled_extensions = ['@jupyterlab/some-extension']
  
  # 启用指定的扩展
  c.LabApp.extra_extensions = ['@jupyterlab/some-other-extension']
  ```

  

> 注意：构建Dockerfile，EntryPOINT["jupyter","lab","--allow-root"]  报错：
>
> > <font color=red>`root_dir` and `file_to_run` are incompatible. They don't share the same subtrees. Make sure `file_to_run` is on the same path as `root_dir`.</font>
>
> 将 jupyter lab --allow-root 转移到sh脚本中，解决错误，应当是环境上下文不同。
>
>  ENTRYPOINT ["/bin/bash", "/root/aaa.sh"] 

#### 2.3 缺少so库

```shell
# 1. 进入docker内部 
kubectl exec  -it  <Pod> -n <NameSpace> /bin/bash

# 2. 更新apt-get 并安装apt-file， apt-file 是一个用于搜索 Debian 和 Ubuntu 软件包中文件的实用工具。它可以帮助你查找包含特定文件的软件包，这对于解决依赖问题、查找缺失的文件或了解哪个软件包提供了某个特定的程序或库时非常有用。
apt-get update && apt-get install -y --no-install-recommends libgl1 && apt-get install -y --no-install-recommends  libglib2.0-0  && rm -rf /var/lib/apt/lists/* # buildkit


# 3. 搜索缺少的依赖
apt-file search libSM.so.6   
# 4. 按照调试安装依赖
apt-get install libsm6
```

