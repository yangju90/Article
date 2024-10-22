### 1. ABAC 设计

ABAC （Attribute Based Access Control）基于属性的权限访问控制， 又可称做 PBAC (Policy Based Access Control ) 基于策略的权限访问控制。<font color=green>**基于属性**</font>，是指访问资源的实体和被访问资源的实体可以通过属性判断进行权限控制，同时<font color=green>**基于策略，**</font> 是匹配权限的规则，根据一条条策略匹配，执行匹配到的行为，最终判断出是否有权限。

在实现访问控制使用属性这方面来看，RBAC可以看做ABAC的一个特例，RBAC使用的角色可以作为ABAC策略权限判断的一个属性，ABAC的策略是可以通过多种不同维度的属性进行布尔运算得出。

目前ABAC 的实现方式很多，很多云厂商（如：亚马逊的IAM \ 阿里云的RAM \腾讯的CAM）都有自己的一套实现方式，但是大体的概念都是围绕 主体、客体、策略、环境、属性等建立起来的。下面是ABAC 的一些基础术语：

- **subject** ：访问的主体，通常指的是用户，也可以是其他非用户实体
- **object**： 本访问的客体
- **action**：操作行为
- **policy**：访问策略，通常是匹配到策略，执行action，判断
- **attribute**： 泛指各种属性，可以是各个实体
- **pdp**：policy decision point 策略决策点
- **pep**：policy enforce point 策略执行点   
- **pap**：policy administration point 策略管理点  （包括各种策略规则）
- **pip**：policy information point 策略信息点 (包括 属性、环境等信息)

![Abac permssion](resouce\Abac permssion.png)

如上图， ABAC 权限系统中，权限的判断不仅仅依赖于主体 Subject 的分配（如RBAC \ACL 等）， 也同时依赖于客体的描述属性，还有访问规则及策略的设计。简单的描述Abac，就是 主体（Subject）被 允许/ 拒绝 （Allow/Denied）操作（Action）客体（Object） 在环境下（Domian）。

Abac对应的属性操作一般可以包括：

- 主客体属性： 时间、人、标记等，非NPE 服务资源类还可以包括：服务、程序、数据、网络、设备等等描述信息
- Action 包括: 读、写、编辑、删除、执行、修改等等， Url 的Acion 可以包括 Get、POST、DELETE、PUT等等（其实和增删改查是同样的语义）
- Domain包括：上下文、环境、时间条件、分组、域等等

其实上面只是列举了Abac定义过程中通用的一些方式，在具体的实现时是更加灵活的，核心是根据定义的访问规则及策略进行调整。

所以个人认为ABAC的实现远不如RBAC那样，会有一套固定的表结构设计，通用的实现方法， 就比如业务系统如果要通过ABAC来进行权限的判断， 那么他的 sub obj domain 等元素的定义肯定和云服务厂商的设计实现完全不同的， 简单来说对于主体和客体对应的属性就完全不相同，同时上线文环境domain也是完全不同的。

下面就来粗略的的分析下云厂商是如何做的。

#### 1.1 云厂商的ABAC 权限

目前比较主流的访问控制，是OASIS组织基于ABAC提出的一种访问控制策略描述语言, XACML(extensible access control markup language) ,  定义了策略、规则、决策点等概念（如 第上节 ABAC 术语所示）。但在业界，大多云厂商都倾向自行设计一套策略描述语言，进行资源服务的访问控制。

##### 1.1.1 IAM

云厂商亚马逊通过IAM ( identity and access Management) 组件来实施基于身份的访问控制， IAM 为租户提供了 用户、组、角色、权限、属性等概念来表达安全策略，包含了对RBAC和ABAC的支持。

在亚马逊云中，ABAC 的属性被叫做 tags，tags 可以附加到 IAM 实体上（包括 资源、用户、角色），同时资源和主体用户的标记可以同时存在，这些tags可以设计、策略规则进行匹配，完成复杂的权限管理功能。

(1) IAM 主体对象

IAM 主体对象包括 user、account、group、role， 其中group 是 user 的集合，通过赋予group tag ，可以将权限赋予group内的用户，role也是同理。IAM 通过JSON格式描述policy, 如下：相同 access-project 、access-team、cost-center  tags 的主体可以被赋予为access前缀的角色。

```json
{
    "Statement": [
        {
            "Sid": "TutorialAssumeRole",
            "Effect": "Allow",
            "Action": "sts:AssumeRole",
            "Resource": "arn:aws:iam::123456789012:role/access-*",
            "Condition": {
                "StringEquals": {
                    "iam:ResourceTag/access-project": "${aws:PrincipalTag/access-project}",
                    "iam:ResourceTag/access-team": "${aws:PrincipalTag/access-team}",
                    "iam:ResourceTag/cost-center": "${aws:PrincipalTag/cost-center}"
                }
            }
        }
    ]
}
```

(2) 分析IAM Policy

IAM policy 支持 Effect、Action、Resource、Condition 原语，同时支持表达式匹配，Condition 中支持函数计算，Action表明可以做的行为，Effect 表达Allow/Deny。

(3) 总之IAM 是依靠定义各种 tag ，通过 policy 中的策略获取tag 进行 计算得出结果。

##### 1.1.2 IAM

RMA (Resource Access Management) 是阿里云提供的资源访问控制服务，提供了用户管理、用户组管理、角色管理、权限策略管理、单点登录、开放授权、身份权限治理等一系列功能，使用户能够方便集成云服务，并完善自身资源的权限管理。

RMA 允许在账户下建立多个用户，用户可以有唯一的用户名、登录密码和访问秘钥，用户根据设定的用户名域名登录，通过账户分权最小化赋权的方式划分云服务资源的权限，定义了 用户、用户组、角色，通过绑定策略完成权限的赋予，判定权限功能。[[阿里云文档\]](https://help.aliyun.com/document_detail/138809.html)

(1) 权限策略

```json
{
    "Statement": [
        {
            "Effect": "Allow",
            "Action": "ecs:Describe*",
            "Resource": "acs:ecs:cn-qingdao:*:instance/*"
        }
    ],
    "Version": "1"
}
```

这个权限策略可以绑定在 用户、用户组和角色上，提供对应的权限判断功能， 如上  Resource 原语 描述可以查看 cn-qingdao 的所有实例。

### 2. Casbin 访问控制

Casbin 是一个强大而高效的开源访问控制库，它支持各种访问控制模型，由多种语言实现，具体特性支持 （[详细查看](https://casbin.org/docs/overview)）。为了能够灵活的配置自身策略，将访问控制模型与策略规则进行的分离，明确了策略实施的逻辑概念，策略实施逻辑概念其本身反应了策略规则所属的访问模型（即策略规则为数据，策略模型为访问控制模型）， 提出PERM 建模语言，来表达访问控制模型及策略规则。

PERM (policy-effect-request-matcher) 元模型，包含4个基本原语：Request、Policy、Effect、Matcher；1个可选原语：Role；以及若干个特性：如域、桩函数等。

![Casbin model](resouce\Casbin model.png)

#### 2.1 Casbin PREM 原语

(1) Request原语是对访问请求的抽象，其定义了访问请求的语义

- 一般由三元组构成：Subject、Object、action，声明语句

  ```
  r= sub, obj, act
  ```

- 如果不需要控制到具体资源，也可采用二元组，声明语句

  ```
  r= sub, act
  ```

- 若需要填加域相关的额外表示

  ```
  r= sub, domain, obj, act
  ```

(2) Policy原语，是对策略规则的描述语义

- 通常来说Policy原语也由Subject、Object、action三元组成， 其描述的是策略规则的定义，如下：

  ```
  p= sub, obj, act
  ```
  

* 对应的策略规则 ，则是

  ```
  p, alice, data1, read
  ```

​       其中分隔符逗号是casbin默认的语法分隔符，也可以采用数据库等实现策略规则的持久化， Policy原语中的每个元素与规则中的每个元素一一对应。上述规则，就描述了用户alice 具有资源data1 的read权限。 

(3) Matcher原语，定义了策略规则如何与访问请求进行匹配

- Matcher 原语可以理解为，在Request 和 Policy 中定义了关于策略和请求的变量属性，然后将这些属性值带入Matcher中进行布尔求值。

- 其支持 +，- ，*， / 等算数运算符，==，！ = ， > , < 等关系运算符，以及 && 、|| 、！ 等逻辑运算符,。一般定义： 

  ```
  m= r.sub == p.sub && r.obj == p.obj && r.act == p.act
  ```

  表示 请求中的主体、客体、动作必须与策略规则中的主体、客体、动作三元组完全相同。

(4) Effect原语，定义了同时匹配多个规则时统一的决策结果

- 规则匹配结果包括 allow和deny两种，可以在 policy时定义，若无定义 则默认为allow 

  ```
  p= sub, obj, act, eft
  p, alice, data1, read, allow
  ```
  
- Effect 原语支持 some, any 等量词（some 为有一条可以匹配就满足， any 必须全部满足）

- 还支持条件关键字where ，&&， ||， ！等逻辑运算符

  ```
e= some(where(p.eft == allow))
  ```

  表达了允许优先，有一条满足条件，则通过匹配，同样如下为拒绝优先：
  
  ```
  e= !some(where(p.eft==deny))
  ```

(5) Role原语，定义了继承关系，可以表达组或者角色层级的概念

- 原语的定义如下，表示 前项继承后项的角色权限

  ```
  g= _, _
  g, alice, admin_group
  ```
  

如上示例，alice 继承admin_group的所有权限

- Role 原语的定义，不仅支持主体，同时还支持客体

  ```
  g2= _, _
  g2, data1, data_group
  ```
  
- Role原语对应的Matcher原语

  ```
  m= g(r.sub, p.sub)
  ```

  将策略规则 p 和请求 r 中具有 Role 关系的策略进行继承关联

(6) domain 可以定义划分区域租户的概念，实现灵活的授权管理

- domain 对策略模型和规则的影响

  ```
  p= sub, dom, obj, act
  g= _, _, _
  m= g(r.sub, p.sub, r.dom) && r.dom == p.dom && r.obj == p.obj && r.act == p.act
   
  p, admin, domain1, data1, read
  p, admin, domain2, data2, read
  g, alice, admin, domian1
  ```

​      上述策略规则表示alice在domain1域内，可以读取同在domain1内的data1数据，但是不可以读取domain2域内的data2数据，也就是按照了domain进行了权限的划分，当然也可以通过通配符来进行匹配，但是对应的Matcher规则也会有响应的改变。

(7) 桩函数

​         Casbin 的PERM表达式还提供了桩函数来扩充满足更多的需求，如查询外部数据库、进行复杂的数学运算等。

​         桩函数允许开发者自定义函数并注册到PERM中被Matcher原语所调用，注册函数可以带多个参数，并有返回值。 java版 桩函数必须实现 AbstractFunction 抽象类。

#### 2.2 其他

(1) Role 和 domain 原语关联

```
g, group1, admin, domain1
g, user3, admin, domain1
g, group2, admin, domain2
g, user3, admin, domain2
 
g, user1, group1, domain1
g, user2, group1, domain1
g, user4, group2, domain2
g, user5, group2, domain2
```

规则原语的继承关系图，叶子节点继承父节点所有权限。

![Hierarchical Role](resouce\Hierarchical Role.png)


(2) Model 语法

Casbin 语法可以实现多种模型的定义 [[参考\]](https://casbin.org/editor)，以一个简单的ABAC 模型为例（不包含Role），语法定义如下：

```
[request_definition]
r = sub, obj, act
 
[policy_definition]
p = sub, obj, act
 
[policy_effect]
e = some(where (p.eft == allow))
 
[matchers]
m = r.sub == r.obj.Owner
```

包含：[request_definition] 、[policy_definition] 、[policy_effect] 、[matchers]

- **[request_definition] 访问请求的定义：** 其基本定义上节已说明， 如有两个访问实体，还可以定义为 r=sub,sub2,obj,act

- **[policy_definition] 策略模型的定义:**   策略模型也可以定义多个模式 (不推荐多策略模型，官方开发并不完善)

  ```
  [policy_definition]
  p= sub, obj, act
  p2= sub, act
  ```

  规则描述也需要根据策略头(p , p2 ) 进行匹配，如果策略模型元素与规则元素不对等，则会报错

  ```
  p, alice, data1, read
  p2, bob, write-all-objects
  ```

- **[policy_effect] effect 多个规则时统一的决策**

- **[matchers] 匹配器表达式**

(3) 多访问实体策略模

* **Model**

  ```
  [request_definition]
  r = sub,sub2, obj, act
   
  [policy_definition]
  p = sub, act
   
  [policy_effect]
  e = some(where (p.eft == allow))
   
  [matchers]
  m = (r.sub == p.sub || r.sub2 == p.sub || r.sub == p2.sub ) && r.act == p.act
  ```

* **Policy**

  ```
  p, admin, read
  ```

* **Request**

  ```
  (admin, unkown, data1,read) - true
  (unkown, admin, data1,read) - true
  ```

### 3. Casbin 进阶

#### 3.1 Matchers 中的内置函数

(1) Url 匹配函数

![Url Matcher Functions](C:\Users\my69\Desktop\file\resouce\Url Matcher Functions.png)

(2) key-getting 函数

![Key getting Function](C:\Users\my69\Desktop\file\resouce\Key getting Function.png)

(3) 自定义函数（java版）

* 实现 AbstractFuntion的call方法 

  ```java
  public class AlawysTrueFunc extends AbstractFunction {
      @Override
      public AviatorObject call(Map<String, Object> env, AviatorObject arg1, AviatorObject arg2) {
          return AviatorBoolean.valueOf(true);
      }
   
      @Override
      public String getName() {
          return "alwaysTrue";
      }
  }
  ```

* 在FunctionMap中注册Fucntion fm.addFunction("alwaysTrue")

* 在Matcher中使用 注册的 alwaysTrue

#### 3.2 Casbin属性特性

(1) 通过 attribute 构建策略模型

* **Model**

  ```
  [request_definition]
  r = sub, obj, act
   
  [policy_definition]
  p = sub, obj, act
   
  [policy_effect]
  e = some(where (p.eft == allow))
   
  [matchers]
  m = r.sub == r.obj.Owner || (r.obj.url == p.obj && r.sub == p.sub)
  ```

* **Policy**

  ```
  p, alice, /api/ops/query, GET
  ```

* **Request**

  ```
  (alice1, { "Owner": "bob", "url": "/api/ops/query"}, GET)  — true
  (alice, { Owner: "alice", "url": ""}, GET) — true
  ```

通过定义：

- 若 sub 用户 请求的 obj Owner 为自己，则返回true
- 若 sub 用户 和 obj url 相匹配，则返回ture

(2) 通过内置桩函数构建模型策略

* **Model**

  ```
  [request_definition]
  r = sub, obj, act
   
  [policy_definition]
  p = sub_rule, obj, act
   
  [policy_effect]
  e = some(where (p.eft == allow))
   
  [matchers]
  m = eval(p.sub_rule) && r.obj == p.obj && r.act == p.act
  ```

* **Policy**

  ```
  p, r.sub.Age > 18 && r.sub.Age < 60, /data1, read
  ```

* **Request**

  ```
  ({ Age: 30}, /data1, read) — true
  ```

通过桩函数定义策略模型，在策略中可以使用运算符（算数、逻辑、关系）定义简单带有规则的测略，请求 sub 的Age 大于18小于60 返回true.

#### 3.3 复杂的 Effect 原语

(1) 表达不存在任何决策结果为deny的规则匹配，则结果为true，否则为false

```
e=some(where(p.eft==allow))&&!some(where(p,eft==deny))
```

(2) 表达任何决策都必须为allow，则结果为true，否则为false

```
e=any(where(p.eft==allow))
```

#### 3.4 优先级模型

通过定义 Policy 规则的优先级策略，控制权限模型的结果。优先级模型 effect 定义如下：

```
[policy_effect]
e = priority(p.eft) || deny
```

(1) 隐式优先级加载策略

隐式优先级策略，策略的顺序决定了优先级，策略出的越早默认的优先级越高。

(2) 显示优先级加载策略

* 显示优先级策略定义

  ```
  [policy_definition]
  p = customized_priority, sub, obj, act, eft
  ```

* 显示策略规则定义

  ```
  p, 1, alice, data1, write, allow
  p, 2, alice, data2, write, deny
  ```

自定义的优先值越小，优先级越高。

(3) 基于Role的优先级

角色和用户的继承结构只能是多棵树，而不是图。 如果一个用户有多个角色，必须确保用户在不同树上有相同的等级。 如果两种角色具有相同的等级，那么出现早的策略（相应的角色）就会更加优先。树的优先级，默认根节点优先级最低，叶子节点优先级最高。

- 策略模型定义

  ```
  
  [request_definition]
  r = sub, obj, act
   
  [policy_definition]
  p = sub, obj, act, eft
   
  [role_definition]
  g = _, _
   
  [policy_effect]
  e = subjectPriority(p.eft) || deny
   
  [matchers]
  m = g(r.sub, p.sub) && r.obj == p.obj && r.act == p.act
  ```

- 策略规则定义

  ```
  p, root, data1, read, deny
   
  g, admin, root
  g, editor, admin
  g, jane, editor
  ```

优先级， jane > editor > admin > root

