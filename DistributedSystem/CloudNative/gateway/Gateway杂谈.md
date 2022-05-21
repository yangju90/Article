<div align=center><font face="黑体" size=6>Gateway杂谈</font></div>

[TOC]



### 01.如何衡量Gateway性能

Gateway在微服务架构中，是一个请求调用的枢纽和中转。

![image-1](resource\image-1.png)

所以，Gateway很重要的一点就是要保持“通畅”，当然，人也是，通畅才能健康。

扯远了。

Gateway要如何才能保持通畅？三点：

- 请求要能进来。
- 请求要能出去。
- 不能堵。

和上面的图略有区别，我们一次API的调用经过的环节如下：

Client → Netscaler → Gateway → API

除去一头一尾来说，中间还会经过两个环节，一个是netscaler，一个是gateway。

老实说，netscaler出问题的情况比较少，一是它没有太多逻辑，二是它没太多依赖，三是它是经过市场验证的。

那么我们如何发现这几个环节是否有性能问题呢？为了避免大家遇到问题的时候靠猜，我们目前会把各个环节的时间耗时记录下来，如下图：

![image-2](resource\image-2.png)

- N：Netscaler在把请求转给Gateway时会记录一个时间戳在http header上，Gateway接到请求时会用当前时间减去netscaler的时间戳，这段时间表示netscaler把请求转到Gateway所花费的时间。
- G：Gateway把请求转给API之前，会有一些逻辑，比如鉴权，负载均衡，流控等等，这是Gateway自身的消耗。
- A：Gateway把请求转给API，一直到API完成响应的总时间。

所以，从上面可以看出，N表示请求能不能进来。A表示请求能不能出去。G表示自己会不会堵。

后面的一些文章我就会从这几方面来讲一下如何来调优，以及遇到的case。



### 02.请求进入Gateway

我们前面说了，看gateway的性能主要从三个方面来看：能不能进来，能不能出去，不要堵。

今天我们就先看能不能进来的问题。

我们公司的微服务是基于Restful的，所以API的通信主要是基于Http协议的，当然，http也是基于tcp协议的。

所以，请求能不能进来，换句话说就是，当前请求过来（从netscaler过来）的时候，gateway能不能最快的响应并处理。

那么，对于TCP通信来说，Server与Client需要通过三次握手来建立网络连接，当三次握手成功后，这条链路上就可以开始传送数据了。

由此可以看出，要请求能够尽快进来，第一个门槛就是能够快速的建立好TCP连接。当然，这通常都是OS来负责处理的，不过有两个参数值得关注并调整它。

这就是： tcp_max_syn_backlog 和 somaxconn。关于TCP这几个参数的解释，可以看一下[这篇文章](https://zhuanlan.zhihu.com/p/146752547)。

```shell
# 查看内核消息， 如果半连接区满，错误信息 Possible SYN flooding on port xxx. Sending cookies. Check SNMP counters
grep kernel messages* | grep TCP
```

所以，你可能会说，gateway都上线这么多年了，somaxconn一直都是128，居然都没出过问题？

是的，gateway上流量虽然大，但是并发量其实不算特别大，一直到我们开始秒杀后，这个问题才开始凸显，秒杀就是一个大并发的行为。

> #### 什么是秒杀？
>
> 在谈秒杀前，首先要搞清楚，什么是秒杀。
> 简单来说，秒杀就是在同一个时刻有大量的请求争抢购买同一个商品并完成交易的过程。
> 这个解释是对用户的。对技术人员来说，秒杀就是在同一时刻有大量的并发读和并发写。
> 那么，按照这个思路来看一个好的秒杀系统具备的特征就应该是：
> - 在大量并发读的情况下，没挂，且响应很快。
> - 在大量并发写的情况下，没挂，且保证数据一致。
> 所以，本质上来说秒杀系统就是一个满足大并发、高性能和高可用的分布式系统。
>
> 下定义都是很简单的，但是做起来确是很难的。因为并发问题，通常都是困扰程序员的一大难题。
>
> 那么，不管是哪种技术平台，哪种架构方式，我认为在设计秒杀系统的时候，可能都需要考虑以下一些设计原则：
>
> - **数据尽量少**：不管是用户请求的数据，还是服务器回传的数据，都要尽量少。因为数据量大了，首先占用网络带宽，同时网络传输时间也会加长。再一个，网络传输通常都是加密的，数据量大，CPU占用也会增加。
> - **请求数尽量少**：请求数多了，对socket的占用是非常大的，这会带来两个问题：如果是客户端请求，那么通常请求数会受浏览器限制；如果是内部服务间的请求，大量的TCP建立，三次握手，开销也很大。
> - **路径尽量短**：路径是指从客户请求开始，到接到响应，中间经过的环节，包括经过了netscaler，api gateway，各个微服务等。每个环节都有可能不稳定，所以，多一个环节就多一个不确定因数。
> - **依赖尽量少**：这个就不用多解释了，如果一个秒杀系统依赖的系统或者服务过多，那么拖垮这个系统的有可能是一些不必要的依赖。
>
> ####  解决方案？
>
> * 分
>   * 第一个要分的就是：把秒杀系统分离出来单独打造一个系统，这样可以有针对性地做优化，并且在系统部署上也独立做一个集群。这样秒杀的大流量就不会影响到正常的商品购买集群的机器负载。同时有了独立系统后，我们就可以大刀阔斧的优化了，优化方式可以参照上一篇提到的原则，例如尽量少请求，少传输数据等。
>   * 第二个要分离的就是动静分离。这个比较好理解，静态内容比如html，js，css等等，这些内容要放得离用户越近越好。常见的位置，例如浏览器里、CDN和服务端缓存。
>   * 第三个要分离的就是冷热数据。秒杀商品是一个典型的热点商品，它们在很短时间内被大量用户执行访问、添加购物车、下单等操作，这些操作我们就称为“热点操作”。因为是我们自己在掌控秒杀，所以热点商品是可以提前预知的，那么对于热点数据就要做提前的缓存，能不去操作数据库的就不要去操作，甚至库存扣减都可以放到缓存里来做。
> * 削： 削听名字，想到的就是削峰填谷，做这件事肯定不能忘记MQ，使用MQ来使并发消息平滑的进入服务系统。
> * 扣： 关系的问题是，秒杀的物品是否发生超卖问题，防止数据扣减过渡，造成损失。



好的，到这个时候，请求已经进入OS层面了，TCP连接已经建立好，这就行了吗？答案是不一定。

还需要你的程序能够处理。这里又分两个层面。

首先，你需要看看你的web server是否有max connection的设定。通常来说，不管IIS还是tomcat还是其他，一般都会有这个设定。因为请求到达web server后，为了能够并发处理，web server通常会新建一个线程去处理这个请求，在这个线程中去调用你的handler处理业务逻辑。那么由于线程还是比较占资源的，所以不可能无限制的使用，为了不把服务器资源耗尽，一般web server都会控制线程数。当达到这个限制后，新来的请求就会排队处理了。

gateway在用golang重构后，这个问题到还好，由于golang是由协程来处理请求，所以暂时没有这个限制。

其次，在linux系统里，需要注意ulimit的限制，ulimit最初设计是用来限制进程对资源的使用情况的，因为早期的系统系统资源包括内存。CPU都是非常有限的，系统要保持公平，就要限制大家的使用，以达到一个相对公平的环境。（这里注意有些应用程序会获取系统的配置，而不是POD、Container定义的资源）**jdk1.8的某个版本就有这种问题，导致POD启动后占用了超过限定的资源量**

好了，到这里，请求应该是可以进来了，后面的文章我们继续看看另外两个问题。

### 03.不能堵

请求进入到gateway后，gateway在把请求转发给后端服务前，还是有不少逻辑需要处理的。比如：路由匹配，鉴权，负载均衡，流控等等。

这些信息都是大家在developer portal配置的，然后由消息队列推送给gateway所依赖的数据库。

由于gateway自身处理逻辑的时间要尽量短，所以这个地方怎么设计呢？

其实比较简单的做法就是，gateway在启动的时候，把所有API的meta数据全部load到内存，那么后续所有的逻辑处理全部在内存完成，这样就是最快的，并且对后端数据库的选型就没太大要求，因为就是启动时候一次性全部读取即可。目前实际市面上大部分api gateway都这么干的。

不过我没有这样来处理，因为我们现在内部的API数据太多了，全部load到程序中，内存的占用太大。

所以，gateway依赖的数据库，我就选择的redis，redis的读写性能应该算是非常好的了。

不过即使这样，也不可能每个请求都去读几次redis，那么这会导致gateway有几百毫秒的消耗，这是不可接受的。由于大家对API的配置变化不是非常频繁，所以，每次读取redis的数据后，我都会在gateway中缓存一段时间，不用重复去读取。所以，gateway内部会有一个LRU的cache，这个LRU的实现如下。

```go
package common

import (
	"container/list"
	"math/rand"
	"time"
)

var (
	urlCache          *Cache
	unHealthHostCache *Cache
	urlSampleCache    *Cache
	apiSampleCache    *Cache
	dnsCache          *Cache
)

func NewCache() {
	urlCache = New(40000, time.Minute*10)
	unHealthHostCache = New(1000, time.Second*30)
	urlSampleCache = New(20000, time.Minute*5)
	apiSampleCache = New(5000, time.Minute*1)
	dnsCache = New(10000, time.Minute*time.Duration(TerraEnv.DNSCacheTime))
}

// Cache is an LRU cache. It is not safe for concurrent access.
type Cache struct {
	// MaxEntries is the maximum number of cache entries before
	// an item is evicted. Zero means no limit.
	MaxEntries int
	ll         *list.List
	cache      map[interface{}]*list.Element
	expiry     time.Duration
}

// A Key may be any value that is comparable. See http://golang.org/ref/spec#Comparison_operators
type Key interface{}

type entry struct {
	key   Key
	ttl   time.Time
	value interface{}
}

// New creates a new Cache.
// If maxEntries is zero, the cache has no limit and it's assumed
// that eviction is done by the caller.
func New(maxEntries int, expiry time.Duration) *Cache {
	return &Cache{
		MaxEntries: maxEntries,
		ll:         list.New(),
		cache:      make(map[interface{}]*list.Element),
		expiry:     expiry,
	}
}

// Add adds a value to the cache.
func (c *Cache) Add(key Key, value interface{}) {

	if c.cache == nil {
		c.cache = make(map[interface{}]*list.Element)
		c.ll = list.New()
	}
	randomExpire := time.Duration(rand.Int31n(60))*time.Second + c.expiry
	if ee, ok := c.cache[key]; ok {
		c.ll.MoveToFront(ee)
		ee.Value.(*entry).ttl = time.Now().Add(randomExpire)
		ee.Value.(*entry).value = value
		return
	}
	ele := c.ll.PushFront(&entry{key, time.Now().Add(randomExpire), value})
	c.cache[key] = ele
	if c.MaxEntries != 0 && c.ll.Len() > c.MaxEntries {
		c.RemoveOldest()
	}
}

func (c *Cache) Size() int {
	return c.ll.Len()
}

// Get looks up a key's value from the cache.
func (c *Cache) Get(key Key) (value interface{}, ok bool) {

	if c.cache == nil {
		return
	}
	if ele, hit := c.cache[key]; hit {
		c.ll.MoveToFront(ele)
		if time.Now().After(ele.Value.(*entry).ttl) {
			c.Remove(key)
			return
		}
		return ele.Value.(*entry).value, true
	}
	return
}

// Remove removes the provided key from the cache.
func (c *Cache) Remove(key Key) {
	if c.cache == nil {
		return
	}
	if ele, hit := c.cache[key]; hit {
		c.removeElement(ele)
	}
}

// RemoveOldest removes the oldest item from the cache.
func (c *Cache) RemoveOldest() {
	if c.cache == nil {
		return
	}
	ele := c.ll.Back()
	if ele != nil {
		c.removeElement(ele)
	}
}

func (c *Cache) removeElement(e *list.Element) {
	c.ll.Remove(e)
	kv := e.Value.(*entry)
	delete(c.cache, kv.key)
}

// Clear purges all stored items from the cache.
func (c *Cache) Clear() {
	c.ll = nil
	c.cache = nil
}

```



好，讲到这里，我们理一下。gateway唯一的依赖就是一个redis，redis里存储了API的各种配置信息。gateway读取了这个配置后，会放到程序的一个LRU缓存里。

这里问大家一个问题，如果LRU里没有的时候，怎么处理？你一定会说，我会把读取配置封装为一个方法，在这个方法里会先去看LRU有没有，没有就去redis读，读到后放入LRU。

没错，我也是这么干的，那么由于LRU是一个会被并发访问的内存资源，一定需要并发锁对不对？那么程序大概是这样写的：

![image-3](resource\image-3.jpg)

我先做了锁的处理，然后在箭头的地方去读redis。这样有没有问题？

我跟大家说，这其实是标准做法，99.9%的场景都是没有问题的，但是用到gateway上，是有问题的。

gateway在上线后，经常会不定期的出现响应毛刺，症状就是我之前提到的那个G时间，比较大。这个问题困扰了我很长时间，一直到后面我才发现可能和上面这段代码有关。

所有读取meta信息都会调用这个代码，如果meta在cache里，那么是很快的。如果需要读取redis，那这个锁一直会锁到读完redis，并放入cache中。如果redis有点卡顿，那么所有并发的API的处理都会受到影响。

而gateway本来就是一个高并发的程序，所以这个卡顿会成倍的放大。

那么怎么改？改动就是锁的范围不能包含redis的读取，只能锁cache的Get和Add两个方法。当然，这有一个副作用，当一个meta不在cache里的时候，如果这个时候有并发的访问，那么就会重复去redis读几次，然后重复放入cache中。不过这个副作用并不会造成太大的影响，是可以接受的。

经过这次修改，gateway的毛刺基本上解决了。现在，只有一个场景下G花费的时间会比较大：开启body的记录，并且body的size比较大（几十k以上）。这个场景下，gateway的处理可能会出现几百毫秒的情况，当然也不是每次请求都会出现。

好，不能堵的问题也说完了，我们下一篇继续。

### 04.请求从gateway出去

之前我们说了请求能进来，不能堵的问题，最后就来说说请求要出去的问题。

当然，和请求要进来问题一样，我们都需要关注TCP的相关问题，所以，包括TCP的参数设定，以及ulimit的问题是一样的。这几个问题就不再重复描述。

由于TCP建立连接还是比较昂贵的，所以，gateway对后端server的连接都是建立的长连接，也就是keep alive的，尽量复用。

并且在新版本的gateway中，我有一个处理，就是不管你client过来的请求，对connection是如何描述的（即使是connection: close），我对后端的连接都是keep alive的。

目前这个keep alive的时长是30s。

其次，当API的请求量并发很大时，那么一个tcp连接肯定是不够的，需要和后端建立多个TCP连接，多是多少呢？

这个数量肯定是要控制的，因为gateway是一个公用的，不能把所有资源都给其中几个API。

之前是每个host给2000个，结果今年不够了。原因是今年我们web site的架构发生了很大的变化，大量的程序部署到了K8S中，那么gateway和所有K8S内部的service之间的访问都要经过K8S ingress。gateway和ingress之间的TCP的通道就显得非常繁忙。假如有一个service变慢，一直占用tcp不释放，就有可能导致tcp不够用。今年就发生了两次。

![image-3](resource\image-4.jpg)

从上图可以看出，2000已经不够了。目前这个值调整为10000。

不过，你也应该可以意识到，这个值不管怎么大，在极端情况下都有可能不够。

所以，比较合理的方案应该是：K8S内部的service调用，通过K8S内部部署的gateway进行调用，不要到外部进行中转。

![image-5](resource\image-5.png)

### 05. URL路由匹配

最近，Gateway出现了一些性能瓶颈，其中一个就是URL匹配有点慢，所以花了点时间来调整匹配的算法，今天就简单和大家聊一下如何来做一个路由匹配。

#### 原理

首先我们先来回顾一下url是啥？当然，我知道大家都是技术人员，我们只是先做一些约定，便于后面好理解。

那么，url就是以 / 分隔的字符。例如：/resource/v1/key，那么这个url包含了被 / 分隔的三段内容。

这里我们暂时忽略query string和hash之类的元素，因为匹配算法用不上。

好了，我们知道了url是什么，那就可以看一下，通常的匹配方法有哪几种？

**首先，精确匹配。**

这个容易理解，就是一字不差呗。

**其次，前缀匹配。**

就是前面一样，后面可以不同。通常就是前面1到多段一样。

**最后，参数匹配。**

url中包含路由参数。



好了，练习一下，例如我下面定义了几个url（我这里的定义都是按照gateway目前的约定来的，其他技术栈可能有不同的表示方法），我们看看是什么类型的。

- /resouce/v1/faq：精确匹配。
- /resource/v1/*：前缀匹配，因为 * 是一个通配符，表示任意字符和任意段。
- /resource/v1/{location}/{key}：参数匹配，因为这个url限定了段数，但是后面两段是通过花括号定义了两个参数。
- /resource/v1/special/{key}：参数匹配。

讲完上面的内容，剩下就可以看如何来实现一个匹配算法了。

#### 算法讨论

如果只是实现精确匹配，其实非常简单，用一个map就完了，url就是map的一个key，简单高效。

如果再加上前缀呢？情况稍微复杂一点。

首先可以把包含 * 的url做成一个正则。

然后挨个正则匹配一下。

但是这里会有一个问题，匹配顺序的问题。例如有两个规则：/resource/* 和 /resource/v1/* 。请求的url：/resource/v1/key。那么正确的匹配结果应该是后面这个规则。怎么办？

聪明的你应该想到，代码里把规则加到路由表的时候，应该按照长度倒序排列，优先匹配段数多的。没错，就是这样。

简单一点的反向代理，其实就做了以上两种匹配，精确和前缀，做法其实就是我们刚才描述的，例如[Ingress](https://kubernetes.io/docs/concepts/services-networking/ingress/#path-types)。



加上路由参数后，情况会复杂更多。

当然，你也可以说，把参数替换成正则嘛，一样可以做，这也没错，nodejs里大名鼎鼎的express框架就这么干的，它用的匹配库名字就叫：[path-to-regexp](https://github.com/pillarjs/path-to-regexp)。

但是，这里有个问题。例如上面我们提到的两个规则：/resource/v1/{location}/{key} 和 /resource/v1/special/{key}，如果请求的url是：/resource/v1/special/2345，那么应该匹配到谁呢？

可能大部分的web框架就没再继续处理这个问题了，答案就是谁先注册匹配谁。



如果只是一个项目里这么搞应该问题也不大，无非就是把路由注册全部放到一个文件中，这样，便于肉眼识别注册的顺序有没有问题。

但是如果这个规则放到gateway里，就不行了，这谁能说清楚先后顺序呢？

所以，我们需要对url做一个权重的处理。精确匹配权重最高，路由参数次之，前缀匹配最低。其中如果同样都是路由参数，那么参数越少的，权重越高。

#### 算法实现

讲到这里，基本上原理说完了，那么怎么实现呢？你会用什么数据结构？

简单粗暴就全部放一个数组，循环遍历。

当然，脑补一下，这个算法应该是最慢的，但是优点是可以用上正则表达式。



另外一个选择就是树，因为直观上，url的结构和树是最像的。树里面我们可以选择[前缀树](https://zh.wikipedia.org/wiki/Trie)。

树的优点当然就是查找性能比较好，但是，但是，有可能一次查找不能找到最佳匹配规则。

例如有两个规则：/resource/v1/{location}/{key} 和 /resource/{version}/special/faq 。请求的url：/resource/v1/special/faq，那么根据上面我们说的权重原理结合树的特性，多半会匹配到第一个规则。

所以，即使用树，也需要所有可能的路径都查找一遍，最后来决定最佳规则。脑补一下，代码其实还是挺复杂的。



那么，gateway用了哪种呢？我其实结合了一下树和数组。

构建了一个只有一层节点的树，也就是说，取url的第一段作为map的key，每个节点里保存了该分支下的所有url，但是这样循环起来还是会比较慢。

就把分支下的url再分一下组，拥有相同段数的url放一个数组。数据结构如下，一个嵌套的map。

```go
RoutesRules struct {
    Routes map[string]map[int][]*RoutePath
}
```

这样其实已经大大降低了遍历的长度，但是在大并发下依然还是会有问题，还可以怎么做呢？

其实在实际场景中，大部分的请求都是有相同的url，没必要每次都匹配一次，所以，再用一个LRU cache，url做md5后做key，用来保存匹配结果，在短时间内，相同的url进来，如果LRU里有匹配结果，那么就不用匹配了，否则就做一次匹配，匹配上以后将结果写入LRU中。

这还是有一个问题，如果有坏人一直用匹配不上的url来访问你，那不是得拖垮你吗？

答案就是，不管是否匹配上规则，都把匹配结果写入LRU，这样不存在的url过来，立即就可以响应一个404。其实这也是防止缓存穿透的一个常用方法。

好了，对于URL匹配就讲到这里，如果你有补充，欢迎评论。

### 06.物理技能不如虚拟机

绝对没有标题党哈。

最近发现一个有趣的现象，gateway在K8S内，有些POD的GC要80ms。大家知道，一旦GC时间过长，一定会影响程序性能。

但是，从下面截图可以发现，不是所有POD的GC都很高，而是有一些POD。

![image-6](resource\image-6.png)

于是，我去观察了一下这些POD的宿主机分布，结果发现这些POD都位于K8S内的物理机上。

这是什么鬼？物理机的性能还不如虚拟机？这好像有点不科学呢？

进一步观察物理机和虚拟机POD的差异。结果发现他们的线程数不同：

![image-7](resource\image-7.png)

在物理机上的POD都启动了60个以上的线程，而VM的都只有10多个。

回顾一下GO的调度模型：经典的 M-P-G 模型，在 Go Scheduler 模型中：

![image-8](resource\image-8.png)

- G 代表 goroutine，即用户创建的 goroutines
- P 代表 Logical Processor，是类似于 CPU 核心的概念，其用来控制并发的 M 数量
- M 是操作系统线程。在绝大多数时候，P 的数量和 M 的数量是相等的。每创建一个 P, 就会创建一个对应的 M

也就是说M>=P。

GO在1.5以后，为了利用上多核的性能，程序启动的时候设定runtime.GOMAXPROCS等于runtime.NumCPU。物理机上有64个core，所以启动64个以上的线程。好像也说得通啊。

不对，那如果这样，我们设定的CPU limit不是没意义了？（感觉已经找到问题所在了）

归纳一下：因为NumCPU拿到的是宿主机的CPU数量，导致启动了过多的线程，而实际由于我们设定了CPU的配额，所以程序在频繁的切换线程，也导致了做GC的时候无法调度到足够多的CPU资源，从而是GC时间过长。

那怎么破呢？Google一下吧，理论上肯定有人解决过了，果然，找到了Uber的一个方案：[automaxproc](https://github.com/uber-go/automaxprocs)。

添加一句话搞定。

看一下效果：

![image-9](resource\image-9.png)

**所以，在K8S里run的程序，有些时候并不是线程越多越好，K8S的哲学就是用启用更多的POD，而不是在一个POD里起更多的线程。**

### 07.负载均衡还有潜规则

负载均衡（LB）可以说是API Gateway的一个主要功能。最近我在往上添加一些新功能的时候，突然觉得现在的LB规则还是挺复杂了，说它是潜规则也不为过。为了让大家不至于对自己的流量往哪转了产生疑问，我决定给大家梳理一下。

总规则：就近转发到可用的后端服务器。

#### 什么叫就近？

就近是指gateway总会将流量转到离自己最近的后端。

#### 如何判断可用？

这里的可用主要指健康检测（Health Check），包括主动和被动。

主动就是指大家在界面设定的health check，包括自定义规则和TCP检测。

被动是指gateway在发现无法和后端建立连接的时候将该host标记为不可用，30s内不会将流量转到该服务器。这里无法建立连接包含两种情况：connection refused和connection timeout。

由于主动检测是有一定时间间隔的，不能很及时的将有问题的后端下线，所以，即使你在界面配置了主动，但是gateway也会将被动的机制运用上去。

也就是说，gateway对所有后端都会使用被动检测的机制来尽量确保服务的可靠性。

#### 还有什么潜规则？

上面是两个大的规则，那么还有两个潜规则需要你了解：

第一，客户端可以指定你想将请求转到哪个后端，类似于nginx的host匹配。客户端在http请求里使用：x-gateway-host，可以指定你想将该请求转到的后端。举个栗子，假如你的x-gateway-host的值为canary，那么gateway会在所有你配置的后端服务中寻找包含有canary的后端。如果找到多个，那么还是根据LB算法来决定使用哪个。

第二，如果API部署在K8S里，而请求是在K8S外面或者另外一个K8S集群中，那么gateway会做一次流量中转，将流量首先中转到目标集群的gateway，然后再由目标集群的gateway来处理请求。所以，这里也有一个小技巧，如果你能确定你的服务在哪个K8S，你可以直接使用对应K8S的gateway，从而减少一次转发处理，让性能更好一些。

#### LB的算法是什么？

目前LB有两种方式：Round Robin和Hash，从名字就可以知道，一个是轮询，一个是Hash。当然，我们现在绝大部分的服务都是无状态的，所以默认的Round Robin就可以满足。如果需要一个客户端固定访问到一个后端，就可以使用Hash。

不管哪种LB的方式，权重都是有效的。在配置Host的时候，都可以指定该Host的权重，可以配置的数字从1~10。举个栗子：如果有两个Host，权重设定为2和3，那么流量的2/5被分配给第一个，3/5分配给第二个。所以这是一个比例的关系。

这里需要注意一点，在K8S里的服务，权重的设定是针对整个服务（service）的，而service背后的POD，是一个轮询的算法，并且大家权重相同。

以上就是API Gateway在LB时候的一些规则，希望大家了解后可以更好的来管理自己的流量。



### 08.补充知识

#### 调整TCP SYN重试次数引发的问题？

因为调整服务器参数时，遇到了connection timeout。即数据库连接失败(失败常见的两种形式，refuse和timeout)，所有临时调整了如下参数：

```shell
net.ipv4.tcp_synack_retries = 1   // 默认为5
net.ipv4.tcp_syn_retries = 1     // 默认为5
```

影响如下图：

![image-10](resource\image-10.png)

监控TCP连接情况，发现入下图：

![image-11](resource\image-11.png)

更改配置后，发现大量的的SYN_SENT，即和后端的连接请求。但是请求源的量并没有很大的改变，为什么会有这么多大量的连接？分析理论应该是retries次数改小后，造成废弃的连接增多，延迟了很多应该很快建立好的连接请求，造成大量的timeout。客户端也会重复请求导致连接数暴增。

更改降低这个参数，可以加快回收半连接，减少资源消耗，但是网络状况不理想的情况下，如果对方没收到第二个握手包，会导致连接失败。





