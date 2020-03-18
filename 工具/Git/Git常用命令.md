<center><font size=5 face="黑体">Gis常用命令</font></center>

![git提交获取基本命令](Git常用命令-资源\git0.png)

#### 一、git基础配置命令

```markdown
1. 版本号
git --version

2. 用户名和邮箱配置
git config --global user.name “xxx”
git config --global user.email “xxx@xxx.com”

3. 查看config信息
git config --global --list

3. 其他命令
ls touch mkdir cp mv rm echo
echo "xx" > a.txt  将xx写入a.txt
echo "xx" >> a.txt 在文件a.txt追加xx

4. Vim相关
Inert模式：
i 在光标当前位置插入 a在光标下一个位置插入 o 表示在新一行插入
I在行首插入 A在行尾插入

5. 命令提示，例如checkout命令
git checkout --help
```

#### 二、 基础操作

##### 2.1 基本操作 

```shell
1. 创建项目
git init   # 当前目录创建

2. 克隆项目
git clone [url] # 克隆到当前目录
git clone <版本库的网址> <本地目录名>

3. 最常用三命令
git status
git add .
git commit -m "提交xxx信息"
git commit -am "add one line"  # 暂存区修改过的文件可以直接提交到本地仓库，越过暂存区

4. 追踪提交人（代码行级别）
git blame index.html
git blame -L 2,5 index.html

5. 查看提交历史记录
git log --pretty=oneline
git show -s --pretty=raw [版本号03d297]
```

##### 2.2 git双横杠与当横杠

```shell
1. 单横杠短选项命令（UNIX风格）：
(1)一个短选项命令，由横杠（-）紧跟单个短选项字符。
git commit -m "第一次提交"
(2)多个短选项命令，由横杠（-）紧跟每个短选项字符。
rm -rf ant
(3)命令和参数之间用空格分隔。
(4)仅作为连字符
git show --name-only
	
2. 双横杠长选项命令（GNU风格）：

(1)长选项命令，有两个（--）紧跟长选项单词（单词不能简写）。

(2)长选项后面跟参数，用空格或等号分隔。
```

##### 2.3 查看指定文件sha-1

```shell
git hash-object readme.txt
```

##### 2.4 git工作区与暂存区

![阿达瓦达瓦达瓦](Git常用命令-资源\git1.jpg)

**版本库：**

工作区根目录下有一个默认隐藏的目录.git，它并不属于工作区，而是版本库（Repository）。

版本库中内容很多，并且都很重要，有两个是我们实际操作中经常要遇到的，那就是暂存区（也可以称之为stage或者index）和分支，HEAD是一个指针，指向当前所在的分支（master）。

#### 三、Git分支操作

##### 3.1 分支基本操作

```shell
(1)创建分支
git branch xxx
git branch -f xxx  # 如果分支存在会强行创建进行覆盖

(2)切换分支
git checkout -b xxx  # 新建分支并切换到分支
git checkout -B xxx  # 同名分支会被替代

(3)分支重命名
git branch #展示分支list
git branch -m xx xxxx # xx分支改为xxxx
mv .git/refs/heads/issue5 .git/refs/heads/newBr # 修改issue5分支到newBr分支


(4)分支切换
git branch -v
git checkout xxx  #切换到xxx分支
git branch --merged # 显示已经合并的分支

(5)根据提交版本创建分支
git log --pretty=online  #查看版本号
git branch [分支名xxx] [版本号xxx]
git checkout -b [分支名xxx] [版本号xxx]  #同理

(6)根据暂存区创建分支
git stash   # 将暂存区状态存储起来
git stash branch xxx

(7)查看分支创建时间
git reflog show --date=iso master

(8)删除分支
git branch -d xxx
git branch -d xxx1 xxx2
git branch -D xxx # 强制删除分支，无论分支是否被合并

(9)回复删除的分支
git branch [分支名xxx] [版本号xxx]  #删除时分支的版本号
通过git reflog查询

(10) 确定当前所在分支
git branch
前面带有星号（*）的分支就是当前所处的分支。如果再深究一下，也就是HEAD指针当指向的那个分支。
cat .git/HEAD  # 也可查询
```

##### 3.2 分支合并与分支管理

1.分支合并

```shell
git checkout master #切换的主分支
git merge hotfix   # 合并hotfix分支

(1)多分支提交，出现‘recursive' strategy，表明是一次三方快照提交。

(2)多分支提交，出现冲突Conflict，需要去查看冲突文件，并合并。
git status

git mergetool #图形化工具来merge

冲突修改完成后，git commit 完成默认合并提交
```

2.分支管理借鉴

- [master]([https://git-scm.com/book/zh/v2/Git-%E5%88%86%E6%94%AF-%E5%8F%98%E5%9F%BA](https://git-scm.com/book/zh/v2/Git-分支-变基))
- [develop]([https://git-scm.com/book/zh/v2/Git-%E5%88%86%E6%94%AF-%E5%8F%98%E5%9F%BA](https://git-scm.com/book/zh/v2/Git-分支-变基))
- [hotfix]([https://git-scm.com/book/zh/v2/Git-%E5%88%86%E6%94%AF-%E5%8F%98%E5%9F%BA](https://git-scm.com/book/zh/v2/Git-分支-变基))
- [release]([https://git-scm.com/book/zh/v2/Git-%E5%88%86%E6%94%AF-%E5%8F%98%E5%9F%BA](https://git-scm.com/book/zh/v2/Git-分支-变基))
- [feature]([https://git-scm.com/book/zh/v2/Git-%E5%88%86%E6%94%AF-%E5%8F%98%E5%9F%BA](https://git-scm.com/book/zh/v2/Git-分支-变基))

##### 3.3 Git标签

1.列出已有标签

```shell
git tag
git tag -l
git tag --list
git tag -l "v1.8.5*" # 过滤版本
```

2.git 两种标签: 轻量标签(lightweight)与附属标签(annotated)

**轻量标签:**很像一个不会改变的分支——它只是某个特定提交的引用

```shell
git tag v1.4-lw
```

**附注标签:**是存储在 Git 数据库中的一个完整对象， 它们是可以被校验的，其中包含打标签者的名字、电子邮件地址、日期时间， 此外还有一个标签信息，并且可以使用 GNU Privacy Guard （GPG）签名并验证。 通常会建议创建附注标签，这样你可以拥有以上所有信息。但是如果你只是想用一个临时的标签， 或者因为某些原因不想要保存这些信息，那么也可以用轻量标签。

```shell
# 创建附属标签v1.4/
git tag -a v1.4 -m "my version 1.4"

git show v1.4  # 展示标签信息
```

3.根据版本打标签

```shell
git log --pretty=oneline
git tag -a v1.2 [版本号9fceb02]
```

4.共享标签

```shell
git push origin v1.5    # 推送单个标签
git push origin --tags  # 推送所有本地标签(不能区分轻量标签和附属标签)

git push <remote> --tags    
```

5.删除标签

```shell
git tag -d <tagname>

# 删除远程仓库标签
git push <remote> :refs/tags/<tagname>   #第一种删除
git push origin --delete <tagname>    #第二种删除
```

6.检出标签

```shell
git checkout -b version2 v2.0.0  # 将标签对应版本检出为分支


git checkout 2.0.0  # 会失去HEAD跟踪 ——完全不推荐
```

##### 3.4 Git别名 

```shell
git config --global alias.co checkout  # checkout别名为 co  可用git co
git config --global alias.br branch
git config --global alias.ci commit
git config --global alias.st status

git config --global alias.unstage 'reset HEAD --'   # 暂存区取消
git config --global alias.last 'log -1 HEAD' # 查看上一个版本、
git config --global alias.visual '!gitk'  # 加入！可以执行外部命令，而非git子命令
```

#### 四、Git命令进阶

##### 4.1 合并上一次提交

```shell
git commit -m 'initial commit'
git add forgotten_file
git commit --amend

最终分支只有一次提交结果。
```

##### 4.2 暂存区撤销

```shell
git reset HEAD <file>    #从暂存区撤销
git checkout -- <file>   # 回退到上一个版本，拉去的代码

git rm --cached <file> #暂存区撤销
git rm <file> #暂存区撤销、工作区全部删除
暂存区为.git/index 
git rm .git/index ## 清空暂存区
```

##### 4.3 reset命令

```shell
git log --oneline 查看版本号
git reset [版本号] --hard
git reset HEAD^ --hard  # 返回到分支所指向的前一个提交

(1)--mixed：默认值，当重置分支所指向commit提交位置时，暂存区中的内容会被新指向的commit提交内容所替换，工作区内容不变。

(2)--soft：暂存区和工作区的内容都保持原样，不会被替换。

(3)--hard：暂存区和工作区的内容都会被新指向的commit提交内容所替换；git reset --hard只影响被跟踪的文件，如果工作区有新增的文件，并不会被影响。
```

**最后说明：**

假如commit已经被push到远程仓库上，那么其他开发人员可能会基于对应的commit提交进行开发产生新的commit，如果此时进行reset操作，会造成其他开发人员的提交历史丢失，这可能会产生严重后果。

##### 4.4 stash命令

```shell
切换分支前，可将目前工作储藏，而不是放入暂存区
git stash # 储藏 工作区和暂存区
git stash list # 储藏列表 最新的储藏在最上面

git stash push -a  #所有文件包括忽略文件，储藏
git stash push -k # 不重置暂存区，储藏

# 储藏使用
git stash apply
git stash apply stash@{0}

git stash clear # 清除储藏
```

##### 4.5 远程仓库使用

```shell
git remote    # 列出每一个远程服务器简写
git remote -v # 查看需要读写的远程服务器简写和URL
```

如果你的远程仓库不止一个，该命令会将它们全部列出。 例如，与几个协作者合作的，拥有多个远程仓库:

我们能非常方便地拉取其它用户的贡献。我们还可以拥有向他们推送的权限，[在服务器搭建Git参考](https://git-scm.com/book/zh/v2/%E6%9C%8D%E5%8A%A1%E5%99%A8%E4%B8%8A%E7%9A%84-Git-%E5%9C%A8%E6%9C%8D%E5%8A%A1%E5%99%A8%E4%B8%8A%E6%90%AD%E5%BB%BA-Git#_getting_git_on_a_server)。

```shell
git clone   # 自动添加url对应的远程，默认会将远程仓库设定简写为origin
git remote add <shortname> <url>  #手动定义添加

git fetch [远程地址简写pb]  # 拉取
git fetch <remote>    # 会拥有远程所有的内容，包括分支

如果目前跟踪了远程分支，使用
git pull 命令自动抓取，并合并到当前分支

# 分享推送
git push <remote> <branch>  # git push origin master 推送到远程master

git remote show <remote>  #查看远程仓库的信息

git remote rename pb paul #修改远程仓库名字
git remote remove paul #删除远程仓库
```

#####  4.6 Git远程分支

```shell
(1)获取远程分支信息
git ls-remote <remote>  # 获取远程引用完整列表
git remote show <remote>
或者对远程分支进行跟踪，远程分支显示 origin/master

(2) 远程推送分支
git fetch <remote> #远程拉取
git push <remote> <branch> 

(3)推送本地serverfix到远程的serverfix
git push origin serverfix:serverfix 
   
本地serverfix推送远程awesomebranch
git push origin serverfix:awesomebranch

(4)远程分支合并到当前工作区
git merge origin/serverfix

(5)切换到远程分支工作
git checkout -b serverfix origin/serverfix
git checkout --track origin/serverfix

从远程分支拉取并设置名字为sf
git checkout -b sf origin/serverfix

修改当前branch track 的远程的分支
git branch -u origin/serverfix

(6)查看所有跟踪分支
git branch -vv  
参数解释： ahead 3 本地领先3个版本
		 behind 1 本地分支落后1个版本
		 
git fetch --all; git branch -vv  #同步远程，并显示详细的版本信息
```

<font color="red">注：git pull   == git fetch + git merge</font>

##### 4.7 删出远程分支

```shell
git push origin --delete serverfix
```

##### 4.8 Git分支整合之rebase

```shell
# 当前分支 experiment、变基操作的目标基底分支 master
git checkout experiment
git rebase master  
# 变基后的experiment分支合并了master分支，高于master一个版本

# 合并分支
git checkout master
git merge experiment 

# 可以处理变基执行过程中的大部分问题
git pull --rebase
同下面：
git fetch
git rebase teamone/master

git config --global pull.rebase true #默认git pull 命令调用git pull --rebase


git rebase <basebranch> <topicbranch>
git rebase --onto master server client
```

只对尚未推送或分享给别人的本地修改执行变基操作清理历史， 从不对已推送至别处的提交执行变基操作，这样，你才能享受到两种方式带来的便利。详细内容可[参考]([https://git-scm.com/book/zh/v2/Git-%E5%88%86%E6%94%AF-%E5%8F%98%E5%9F%BA](https://git-scm.com/book/zh/v2/Git-分支-变基))

##### 4.9 远程分支回退的三种方式

```shell
1. reset方式，缺点每个成员都需要去拉取自己的分支并合并到master(自己单人开发使用合适)

git reflog
git reset --hard Obfafd
git push -f

2. revert （多人开发时，如果有多个人revert，易出现版本混乱，所以使用此命令前，需要仔细查看版本树）
git revert HEAD                     // 撤销最近一次提交		
git revert HEAD~1                   // 撤销上上次的提交，注意：数字从0开始
git revert 0ffaacc                  // 撤销0ffaacc这次提交

(1) revert 是撤销一次提交，所以后面的commit id是你需要回滚到的版本的前一次提交;
(2) 使用revert HEAD是撤销最近的一次提交，如果你最近一次提交是用revert命令产生的，那么你再执行一次，就相当于撤销了上次的撤销操作，换句话说，你连续执行两次revert HEAD命令，就跟没执行是一样的;
(3) 使用revert HEAD~1 表示撤销最近2次提交，这个数字是从0开始的，如果你之前撤销过产生了commit id，那么也会计算在内的;
(4) 如果使用 revert 撤销的不是最近一次提交，那么一定会有代码冲突，需要你合并代码，合并代码只需要把当前的代码全部去掉，保留之前版本的代码就可以了。

3. 最实用的方式，将master拉取到本地，手动去修改，在提交
```

#### 五、常用命令

```shell
add     blame    clean   checkout
config  clone    commit  diff
grep    init     log     rebase
reflog  reset    show    show-branch
stash   status

clean 清除未跟踪文件
```





-------------------------------

​	





<font size=3>**参考资源：**</font>


[git-book官方网站]: https://git-scm.com/book/en/v2 "git-book官方网站"
[蚂蚁部落]: https://www.softwhy.com/article-8793-1.html	"蚂蚁部落"




