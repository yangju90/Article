#### 1.RBAC

#### ![image-20240301004042680](resouce\rbac-example.png) 

#### 2.RBAC Role

RBAC 模型由 4 个基础模型组成：
- [x] 基本模型 RBAC0（Core RBAC）
- [x] 角色分层模型 RBAC1（Hierarchal RBAC）
- [ ] 角色限制模型 RBAC2（Constraint RBAC）
- [ ] 统一模型 RBAC3（Combines RBAC）

#### 2.1 RBAC Role 树状结构数据库设计

RBAC Role 树状结构设计，解决 RBAC1 角色继承的上下级关系，上级默认拥有下级角色的全部权限。针对角色树的层级深度应该是有限的，选择路径枚举构建Role 层级表。

##### 2.1.1  表格设计

(1) Base

| ColumnName     | DataType     | isNull | Index |
| -------------- | ------------ | ------ | ----- |
| id             | bigint       | F      |       |
| Deleted        | tinyint(1)   | Y      |       |
| in_User        | varchar(200) | F      |       |
| in_date        | bigint       | F      |       |
| last_edit_user | varchar(200) | Y      |       |
| last_edit_date | bigint       | Y      |       |

(2) User

| ColumnName   | DataType     | isNull | Index                  |
| ------------ | ------------ | ------ | ---------------------- |
| username     | varchar(50)  | F      | UNIQUE(uni_user_name)  |
| password     | varchar(50)  | F      |                        |
| contact_with | varchar(50)  | Y      |                        |
| salt         | varchar(200) | Y      |                        |
| email        | varchar(200) | F      | UNIQUE(uni_user_email) |
| avatar       | varchar(200) | Y      |                        |

(3) Role

| ColumnName  | DataType     | isNull  | Index                       |
| ----------- | ------------ | ------- | --------------------------- |
| name        | varchar(20)  | F       | UNIQUE(uni_role_name)       |
| path_string | varchar(512) | F       | ix_path_string(path_string) |
| node        | int          | F       | UNIQUE(uni_node)            |
| status      | tinyint      | D     1 |                             |

(4) User-Role

| ColumnName | DataType | isNull | Index                             |
| ---------- | -------- | ------ | --------------------------------- |
| user_id    | bigint   | F      | ix_t_user_role(user_id,  role_id) |
| role_id    | bigint   | F      | ix_t_user_role(user_id,  role_id) |

(5) Resource

| ColumnName | DataType     | isNull | Index |
| ---------- | ------------ | ------ | ----- |
| name       | varchar(20)  | F      |       |
| resource   | varchar(255) | F      |       |

 (6) Role-Resource

| ColumnName       | DataType    | isNull | Index                                                      |
| ---------------- | ----------- | ------ | ---------------------------------------------------------- |
| role_id          | bigint      | F      | ix_t_resource_role_operate(resource_id,  role_id, operate) |
| resource_id      | bigint      | F      | ix_t_resource_role_operate(resource_id,  role_id, operate) |
| resource_operate | varchar(40) | F      | ix_t_resource_role_operate(resource_id,  role_id, operate) |
| function         | varchar(40) | F      |                                                            |

##### 2.1.2 路径枚举CRUD

(1) Insert 插入

```mysql
INSERT INTO `role` (name, node, path_string, in_user, in_date) values ('root', 1, '1', 'admin', 1658902386540);

INSERT INTO `role` (name, node, path_string, in_user, in_date ) select 'Client' , max(node)+1, concat('1', '_' , max(node)+1), 'admin', 1658902386540  from `role`; 
```

(2) Delete Update 修改

三种实现方式： 

- 删除全部 子节点
- 修改节点状态：对应的查询页需要添加  status = 1
- 子节点接入父节点，当前节点删除  

```mysql
-- 删除全部子节点 --
DELETE FROM `role` WHERE path_string like '1_%';

update `role` set status = 0 where name = 'Client';

-- 删除节点，将子节点接到父节点上 --
DELETE FROM `role` WHERE node = 2;

UPDATE `role` SET path_string = REPLACE(path_string, '2_', '') WHERE path_string LIKE '%2_%';

```

(3) Select  查询

```mysql
Select * From `role` where path_string like '1_2_%' order by node;
```

#### 2.2 K8S平台下的RBAC设计

K8s 云原生RBAC管理模型，以Role、Subject、RoleBinding组成。

* Role：角色，它其实是一组规则，定义了一组对 Kubernetes API 对象的操作权限。
* Subject：被作用者，既可以是“人”，也可以是“机器”，也可以是你在 Kubernetes 里定义的“用户”。
* RoleBinding：定义了“被作用者”和“角色”的绑定关系。

而这三个概念，其实就是整个 RBAC 体系的核心所在。

（RBAC 核心<Role、Subject（人、机器）、RoleBinding>，  ABAC）

##### 2.2.1 平台Yaml文件结构样例

(1) Role 角色资源权限

```yaml
kind: Role
apiVersion: rbac.authorization.k8s.io/v1
metadata:
  namespace: mynamespace
  name: example-role
rules:
- apiGroups: [""]
  resources: ["pods"]
  verbs: ["get", "watch", "list"]
```

(2) RoleBinding 角色Subject对象绑定

```yaml
kind: RoleBinding
apiVersion: rbac.authorization.k8s.io/v1
metadata:
  name: example-rolebinding
  namespace: mynamespace
subjects:
- kind: User
  name: example-user
  apiGroup: rbac.authorization.k8s.io
roleRef:
  kind: Role
  name: example-role
  apiGroup: rbac.authorization.k8s.io
```
