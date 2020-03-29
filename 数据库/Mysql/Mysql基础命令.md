####  基础命令

---

##### 客户端登录

```mysql
# 客户端登录
mysql -uroot -p
```

##### 数据库操作

```mysql
# 创建数据库：
create database school;

# 创建带字符集的数据库
create database mydb2 CHARACTER SET=utf8;

# 创建带校验的数据库
create database mydb3 CHARACTER SET=utf8 COLLATE utf8_general_ci;

#显示数据库
show databases;

# 删除数据库
DROP DATABASE shujukuba;

# 修改数据库编码
ALTER DATABASE shujukuba character set gb2312;

# 查看创建数据库的语句
show create database databasename;
```

##### 表操作

```mysql
# 创建表
CREATE TABLE `user` (
    `id` bigint(20) unsigned NOT NULL AUTO_INCREMENT,
    `name` text COLLATE utf8_unicode_ci NOT NULL COMMENT '字段1',
    `sex` varchar(128) COLLATE utf8_unicode_ci NOT NULL DEFAULT '' COMMENT '字段2',
    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8_unicode_ci;

# 表名修改
rename table oldName to newName;

# 显示表格状态详情
show table status like 'tableName';

# 表格内容删除
truncate table 'tableName';   // 相当于  drop table + create table

# 修改表格
# 删除user表sex字段
alter table user drop sex;
# 增加字段image二进制存储图片
alter table user add image blob; 
① TinyBlob 最大255B
② Blob 最大容纳65KB
③ MediumBlob 最大容纳16M数据
④ LongBlob 最大容纳4GB数据

# 修改字段类型
alter table user modify detail varchar(255);

# 修改表的字符集为utf-8
alter table user character set utf-8;

# 删除表
drop table user;

# 查看创建表语句
show create table tablename;

# 查看表结构
desc tablename;
```

关于Mysql COLLATE作用的解释，[可参考](https://www.cnblogs.com/qcloud1001/p/10033364.html)。

##### 行操作

```mysql
select 、 insert 、 delete 、 update

# 删除user表全部数据
delete from user;
```

##### 常见字段

```mysql
自增长：auto_increment
非空：not null
默认值：default
唯一：unique
指定字符集：charset
主键：primary key
```

