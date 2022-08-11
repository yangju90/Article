[toc]



## PostgreSQL 基础语法

##### 1. 使用sql语句操作数据库

```sql
create database db_indi_user;

create database db_indi_user with owner=postgres encoding='utf-8';

-- 修改数据库名称
create database db_indi_user rename to db_indi_student;

-- 最大连接数限制
create database db_indi_user connection limit 20;

drop database db_indi_user;
```



##### 2.使用sql语句操作数据表

```sql
(1)创建表
-- user 表是postgresql的关键字表，注意要规避，并且字段尽可能规避关键字
-- VARCHAR 在postgresql中被转为character varying 字符串， 与character区别为 后者会用空格补齐

create table if not exists indi_employee_user(
    user_id BIGINT NOT NULL,
    user_name VARCHAR(30) NULL DEFAULT NULL,
    age INT NULL DEFAULT NULL,
    email VARCHAR(50) NULL DEFAULT NULL,
    favorite int DEFAULT NULL,
    PRIMARY KEY (user_id)
);

COMMENT ON TABLE indi_employee_user IS '用户表';

COMMENT ON COLUMN indi_employee_user.user_id IS '主键ID';

COMMENT ON COLUMN indi_employee_user.user_name IS '姓名';

COMMENT ON COLUMN indi_employee_user.age IS '年龄';

COMMENT ON COLUMN indi_employee_user.email IS '邮件';


（2）修改表
alter table indi_employee_user rename age to vage;

alter table indi_employee_user alter column vage type VARCHAR(30);

alter table indi_employee_user drop column vage;

alter table indi_employee_user add column age int;

drop table indi_employee_user;

drop table if exists indi_employee_user;
```

##### 3.数据插入

```sql
(1)简单数据插入

insert into indi_employee_user values (2,'张三', 31, 'employee@email.com', 3);

insert into indi_employee_user (user_id, user_name) values (3, '李四');

(2)批量数据插入

insert into indi_employee_user (user_id, user_name) values 
		(4, '李四'), (5, '李四'),(6, '李四');

-- Select查询批量插入
insert into indi_employee_user_new select * from indi_employee_user;

(3)数据更新操作

update indi_employee_user_new set user_name = 'lisi'  where user_id = 2;

-- 全表更新
update indi_employee_user_new set user_name = 'lisi'

(4)数据删除操作

delete from indi_employee_user_new where user_id = 2;

delete from indi_employee_user_new where user_id between 2 and 10;

-- 清空表，但是自增字段不会归0 索引也不会删除（DML），记录日志，可回滚
delete from indi_employee_user_new;

-- 所有的数据reset（DDL），不会记录日志
tuncate table indi_employee_user_new;

(5)Postgresql 主键、外键
-- 定义主键
CONSTRAINT indi_employee_user_pkey PRIMARY KEY (user_id)

-- 定义外键
CONSTRAINT indi_employee_user_new_favorite FOREIGN KEY (favorite) REFERENCES indi_employee_user(user_id)

-- 主外键作用在于相互约束，主键 唯一标识、提高检索效率 外键 保证数据的完整性、提高检索效率

(6)Postgresql 数据表非空约束、唯一约束、默认值
-- 非空约束  not null
user_id BIGINT NOT NULL

-- 唯一约束, 可以为空
email VARCHAR(50) UNIQUE

-- 默认值
favorite int DEFAULT NULL,
```



##### 4.数据查询

```sql
(1) 简单查询
select * from indi_employee_user;
(2) like 匹配  % 匹配多个字符， _仅匹配一个
(3) null 值查询 is null | is not null
(4) 排序空值 提前 select * from table order by column asc nulls first;    (默认为nulls last)
(5) offset  (pageNumber - 1) * pageSize;
(6) inner join 不能匹配的条件将不会显示， left join | right join 以左右为主连接，为空的条件也可显示

(7) 子查询操作
-------------------------- exists ----------------------------------------
select * from a where exists (select * from b where xx = xx);
-- 子查询查到任何数据，只要不为空， 则相当于 exists true，即 select * from a
-- example dept_no 为employee 字段，查询雇员信息表中部门为开发部的员工
select * from employee where exists (select dp_no from dept where d_name = '开发部' and d_no = dept_no); 

------------------- exists 相当于在主查询中逐行判断 (not exists) ---------------

--------------------------- in --------------------------------------------
select * from employee where dept_no in (select dp_no from dept where d_name = '开发部'); 

------------------ 标量查询(将一个子查询放在Select查询中显示) ---------------------
select e_no, e_name, (select d_name || '' || d_location from dept where dept_no = d_no) as address from employee; 


(8) 查询结果合并
UNION ALL | UNION 字段必须一致， 若字段不一致，可填充null，占位

-- UNION ALL 合并不会去重 快
select e_no, e_name, dept_no, e_salary, detail from employee_new where dept_no in (10, 20)
UNION ALL
select e_no, e_name, dept_no, e_salary, null from employee where e_salary > 5000;

-- UNION 会去重 慢

```

