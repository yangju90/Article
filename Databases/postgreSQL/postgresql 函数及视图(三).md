[toc]



## PostgreSQL 函数及视图

##### 1.PostgreSQL 常用函数介绍

```sql
(1) 常用数值函数
    avg()    平均值
    count()  行数
    max()    最大值
    min()    最小值
    sum()    求值之和
(2) 常用字符串函数
	length(s)                   字符串长度
	concat(s1,s2,...)           字符串合并
	ltrim(s)/rtrim(s)/trim(s)   删除字符串空格函数
	replace(s, s1, s2)          字符串替换函数 （将字符串s中的s1 替换为s2）
	substring(s,n,len)          获取子字符串
(3) 常用日期函数
  	extract(type from d)       日期指定值函数 extract(day/year/month from d) 
  	current_date               获取当前日期
  	current_time               获取当前时间
	now()                      获取当前日期时间函数
```



##### 2. 自定义函数

```sql
// example 1
create or replace function add(integer, integer) returns integer
 	as 'select $1+$2;'
 	language sql
 	returns null on null input;
 	
// example 2 在postgresql中|| 表示字符串连接
create or replace function concat_test(integer, varchar, date) returns varchar
 	as 'select $1||$2||$3;'
 	language sql
 	returns null on null input;
 	
//删除 函数
drop function add(integer, integer);
```



##### 3. Postgresql数据库索引

| 索引名称   | 使用场景                                   |
| ---------- | ------------------------------------------ |
| B-tree索引 | 适合处理顺序存储的数据（范围查询比较优）   |
| Hash索引   | 只能处理简单的等于比较                     |
| GiST索引   | 一种索引架构（可以灵活扩展自己的索引策略） |
| GIN索引    | 反转索引，处理包含多个值的键（如数组）     |

```sql
create index colum_index_name on table_name(column);
drop index colum_index_name;      -- index 是创建B-tree索引

create index colum_index_name on table_name using hash(column);
```



##### 4. 数据库视图

```sql
-- 视图是从一张表或多张表中组合数据，但不存储数据

create view view_name as select ... 语句;

select * from view_name;

drop view view_name;

-- 视图的作用： 简单化、安全性、逻辑数据独立性
```

