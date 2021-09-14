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



