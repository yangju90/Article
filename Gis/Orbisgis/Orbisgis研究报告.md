<center><font size=5 face="黑体">OrbisGis研究报告</font></center>

[toc]

<div STYLE="page-break-after: always;"></div>

#### 一、OrbisGis简介

​	OrbisGis是由Gis、统计学和计算机科学领域的专家合力开发完成的系统，核心决策团队在法国（导致部分资料是用法语写的）。

##### 1.1 OrbisGis 项目组核心成员跟进OGC标准

​	以下为OrbisGis项目中遵循的OGC标准目录：

```markdown
1) Simple Feature Access  SFS - SFS 标准
2) Symbology Encoding (SE)  符号编码标准 ---  进行中
3) Web Map Service Web地图服务标准
  	Web地图服务（WMS）从地理信息动态地生成空间参考数据的地图，猜测代码中会有关于WMS的部分，待求证。
4) Web Processing Service Web处理服务标准 ---  进行中
5) OWS-Context  ---  进行中
```

##### 1.2 OrbisGis项目特色 

```markdown
1. OSGI plateform  OSGI编程；
2. OSGI包含了制作专题图的功能
3. SQL和Groovy语言的控制台支持，支持H2GIS和PostGIS数据库；
4. 支持的数据格式: SHP, OSM, KML, GeoJson, ... 。
```

##### 1.3 官方推荐的三个项目

​	除了OrbisGis项目本身外，官方网站推荐了以下三个项目：

```markdown
1. H2GIS
   模仿PostGIS数据库引擎方式，对H2数据库进行了改造，支持了空间索引。
2. CTS
   利用大地测量学算法开发的坐标转换库。
3. NoiseModelling
   有GIS实现的环境噪声计算模型，OrbisGis插件。（查看源码后，无内容模块可以借鉴）
```

#### 二、官方资料

​	官方用户手册主要介绍了OrbisGis软件本身的如何使用和相关的功能介绍，大部分内容为GUI（图形用户界面），从可以看出以下模块或许有用：

1. 数据导入、导出功能，支持的数据格式SHP、GPX、OSM、GeoJSON、CSV、DBF、TSV
2. PostGis的数据库操作代码

<div STYLE="page-break-after: always;"></div>

#### 三、OrbisGis代码模块分析

OrbisGis代码本身已经没有人维护，各模块的更新时间都在3年前。对于OrgisGis及其插件研究的导图如下图所示：

![Orbisgis项目图片1](Orbisgis-资源\Orbisgis项目图片1.png)

OrbisGis项目可以分为三个部分：

1. 自身开发的源代码功能；
2. 项目依赖的组件；
3. 项目可扩展的插件。

三部又可划分为更细模块。目录树标记为绿色的功能部分有一定价值，本结会对功能详细说明并搭配使用样例代码；白色部分大部分是与Java的OSGI框架和GUI框架相关。

##### 3.1 图层导出工具

###### 3.1.1 工具提供的功能

图层导出工具，提供了地理信息数据导出为图片或PDF的功能，具体格式有PNG、JPEG、TIFF、PDF。工具使用如下所示：

```mermaid
graph LR
id(创建图层上下文) --> id2(根据上下文创建图层) --> id3(获取图层输出流)--> id4(导出)
```

###### 3.1.2 代码使用样例

```java
// 创建图层上下文
MapContext mc = new OwsMapContext(getDataManager());
// 获取地理信息数据，根据上下文创建图层
ILayer layer = mc.createLayer(ExportTest.class.getResource("landcover2000.shp").toURI());
mc.getLayerModel().addLayer(layer);
// 获取图层输出流
MapImageWriter mapImageWriter = new MapImageWriter(mc.getLayerModel());
FileOutputStream out = new FileOutputStream(new File(imagePath));
// 设置转换格式 MapImageWriter.Format.TIFF（TIFF, PNG, JPEG, PDF）
mapImageWriter.setFormat(format);
mapImageWriter.write(out, new NullProgressMonitor());
```

##### 3.2 通用工具类

###### 3.2.1 代码功能

通用工具类提供了三方面能力：

* 一种二级缓存的管理算法实现
* 任务处理进度监控框架
* 提供了URI(jdbc、file...)解析、short、int、float与byte互转

###### 3.2.2 代码使用样例

1. 二级缓存算法

   使用二级缓存管理算法需要继承TwoQueueBuffer类，并实现reclaim和unload方法，算法使用了两种置换算法，一种FIFO一种LRU

```java
class TestAddClear2Q extends TwoQueueBuffer<Integer, Integer> {

    boolean passed;

    public TestAddClear2Q(int maxSize) {
        super(maxSize);
    }

    @Override
    protected Integer reclaim(Integer id) {
        passed = !passed;

        return id;
    }

    @Override
    protected void unload(Integer b) {
    }
}
```

2. 任务状态监控框架

```java
// 创建任务监听，设置监控名称，并设置总大小
ProgressMonitor pm = new RootProgressMonitor("open file", 200);
// 获取当前进度，并向下取整
pm.getCurrentProgress()；
// 获取当前进度，double类型
pm.getOverallProgress()；
// 默认进度执行1.0
pm.endTask()
// 自定义进度执行2.3
pm.pushProgression(2.3);
```

3. URI解析(jdbc、file...)

```java
File f = new File("/home/me/toto.shp");
FileUtils.getNameFromURI(f.toURI()); // toto

URI u = URI.create("http://toto.com/wms?hello=toto&tableName=mytable");
FileUtils.getNameFromURI(u.toURI()); // mytable

URI exoticURI = URI.create("pgsql://poulpe.heig-vd.ch:5432/scapdata/g4districts98");
FileUtils.getNameFromURI(exoticURI); // g4districts98
                
URI uJDBC = URI.create("postgresql://127.0.0.1:5432/gisdbuser=postgres&password=postgres&schema=gis_schema&table=bat");
FileUtils.getNameFromURI(uJDBC); // bat

u = URI.create("jdbc://toto.com:4567/mydb?tableName=helloworld");
FileUtils.getNameFromURI(u); // helloworld

u = URI.create("jdbc:h2:/home/user/OrbisGIS/databasecatalog=&schema=&table=LANDCOVER2000");
FileUtils.getNameFromURI(u)); // LANDCOVER2000

u = URI.create("../src/test/resources/data/landcover2000.shp");
FileUtils.getNameFromURI(u); // landcover2000.shp
```

##### 3.3 地理空间坐标与投影转换（cts库）

###### 3.3.1 cts库功能

​	利用大地测量学算法开发的坐标转换库，主要功能是：

```
1) 地理坐标系转换，例如:
	WGS 84 <-> CGCS 2000
	WGS 72BE <-> CGCS 2000
	....
2) 地理坐标系转换投影，例如: 
	WGS 84 / UTM zone 1N 墨卡托投影
	WGS 84 / UTM zone 2N
	...
	CGCS2000 / Gauss-Kruger CM 75E 高斯-克吕格投影
	...
```

###### 3.3.2 cts库使用样例

1. 地理坐标系转换

```java
// 假设我们需要将WGS 84 -> CGCS 2000 
// 1）初始化注册
CRSFactory cRSFactory = new CRSFactory();
registryManager.addRegistry(new IGNFRegistry());
registryManager.addRegistry(new EPSGRegistry());
registryManager.addRegistry(new ESRIRegistry());
registryManager.addRegistry(new Nad27Registry());
registryManager.addRegistry(new Nad83Registry());
registryManager.addRegistry(new WorldRegistry());
// 2）根据数据源类型和目标类型查找EPSG注册表中的代码,可知WGS 84 = EPSG:4326; CGCS 2000 = EPSG:4490
CoordinateReferenceSystem src = cRSFactory.getCRS("EPSG:4326");
CoordinateReferenceSystem dest = cRSFactory.getCRS("EPSG:4490");
// 3) 创建转换集合
Set<CoordinateOperation> ops = null;
ops = CoordinateOperationFactory.
    		createCoordinateOperations((GeodeticCRS)src, (GeodeticCRS)dest1);
// 4) 获取转换操作精度，这里还可以对ops进行其他操作
CoordinateOperation op = CoordinateOperationFactory.getMostPrecise(ops);
double[] doubles2 = op.transform(doubles);
```

2. 地理投影转换

```java
// 地理投影转换和地理坐标转换代码一致，区别点在与注册表准代码不同
// 例如：
CoordinateReferenceSystem src = cRSFactory.getCRS("EPSG:4326");
System.out.println(src.toWKT());
/**
输出为：
GEOGCS["WGS 84",DATUM["World Geodetic System 1984",SPHEROID["WGS 84",6378137.0,298.257223563,AUTHORITY["EPSG","7030"]],TOWGS84[0,0,0,0,0,0,0],AUTHORITY["EPSG","6326"]],PRIMEM["Greenwich",0.0,AUTHORITY["EPSG","8901"]],UNIT["degree",0.017453292519943295,AUTHORITY["EPSG","9122"]],AXIS["Longitude",EAST],AXIS["Latitude",NORTH],AUTHORITY["EPSG","4326"]]
*/
CoordinateReferenceSystem dest = cRSFactory.getCRS("EPSG:32601");
System.out.println(dest.toWKT());
/**
输出：
PROJCS["WGS 84 / UTM zone 1N",GEOGCS["WGS 84",DATUM["World Geodetic System 1984",SPHEROID["WGS 84",6378137.0,298.257223563,AUTHORITY["EPSG","7030"]],TOWGS84[0,0,0,0,0,0,0],AUTHORITY["EPSG","6326"]],PRIMEM["Greenwich",0.0,AUTHORITY["EPSG","8901"]]],PROJECTION["Transverse Mercator Zoned Grid System"],PARAMETER["latitude of origin",0],PARAMETER["central meridian",-177],PARAMETER["scale factor",1],PARAMETER["false easting",0],PARAMETER["false northing",0],UNIT["meter",1,AUTHORITY["EPSG","9001"]],AXIS["Easting",EAST],AXIS["Northing",NORTH],AUTHORITY["EPSG","32601"]]
*/
```

###### 3.3.3 优缺点

<font color="red">**优点：**</font>地理坐标转换和投影转换的类别都非常完善，常见的类型在扩展代码包的扩展表中都可以检索到。

<font color="red">**缺点：**</font>整个数据的转换过程不是自动识别的，如代码所示，需要查找对应的扩展注册表（epsg、esri、ignf、nad27、nad83、world）进行开发转换，没有一定专业知识很难使用。

##### 3.4 地理信息矢量几何运算库（jts库）

###### 3.4.1 jts库功能

JTS拓扑套件使用精度模型与几何算法实现了核心空间数据的操作，提供了几何图形空间数据的计算功能。

* 创建点、线、面
* 空间关系判断（相等、相交、接触、穿过、包含）
* 叠加计算（合取、析取、相减、缓冲）
* 图形距离计算

###### 3.4.2 jts代码使用样例

1. 创建点、线、面

```java
// 创建 点
Geometry centrePt = geomFact.createPoint(new Coordinate(0.5, 0.5));
// 创建 线

// 创建 面

```





##### 3.5 Java图像处理功能扩展（jai库）

###### 3.5.1 jai库功能



###### 3.5.2 jai代码使用样例



##### 3.6 H2GIS插件源代码及功能分析

###### 3.6.1 H2GIS插件功能

​	H2GIS插件模仿PostGIS数据库引擎方式，对H2数据库进行了改造，支持了空间索引。从jar包中分析得出，用到了Luence技术和JTS库。

- luence技术：是一个全文检索引擎工具包，但它不是一个完整的全文检索引擎，而是一个全文检索引擎的架构，提供了完整的查询引擎和索引引擎，部分文本分析引擎。
- JTS库：JTS 是一套用于处理几何要素拓扑关系的函数库。它提供了完整、稳定、可靠的基本二位平面线形图形运算算法实现。
- H2GIS开发的功能：提供了H2数据库上的空间计算函数（最近点、最远点、最大距离、凸面、并集、交集、缓冲区、聚合等）；数据读写功能，包括的格式有asc、csv、geojson、gpx、json、kml、osm、shp。

###### 3.6.2 H2GIS代码功能样例

1. H2GIS数据库创建（等同一般数据库）

```java
// 创建数据库连接
Connection connection = H2GISDBFactory.createSpatialDataBase("BasicTest");
// 声明、建表、插入语句
Statement st = connection.createStatement();   
st.execute("DROP TABLE IF EXISTS dummy;CREATE TABLE dummy(id INTEGER);");
st.execute("INSERT INTO dummy values (1)");
// 断开连接
st.close();
connection.close();
```


2. H2GIS数据库函数**空间查询**使用（等同一般数据库，支持WKT查询）

```java
// 建空间索引表，插入两行数据
st.execute("DROP TABLE IF EXISTS input_table;"
                + "CREATE TABLE input_table(twoDLine Geometry(GEOMETRY, 4326), threeDLine 					Geometry);"
                + "INSERT INTO input_table VALUES("
                + "ST_GeomFromText('LINESTRING(1 2, 4 5)', 4326),"
                + "ST_GeomFromText('LINESTRING(1 2 3, 4 5 6)'));");
// 空间ST_Scale函数查询
ResultSet rs = st.executeQuery("SELECT "
                + "ST_Scale(twoDLine, 0.5, 0.75), ST_Scale(threeDLine, 0.5, 0.75), "
                + "ST_Scale(threeDLine, 0.5, 0.75, 1.2), ST_Scale(threeDLine, 0.0, -1.0, 				   2.0) "
                + "FROM input_table;");
```

3. 数据读写功能

H2GIS代码中数据读写功能都针对H2数据库，通过将 （asc、csv、geojson、gpx、json、kml、osm、shp）格式转化为 WKT格式存入。具体代码位置：<font color="red" face="黑体">org.h2gis.functions.io</font> 包下。

###### 3.6.3 优缺点

<font color="red">**优点：**</font>以java的方式，在H2库上实现了空间索引，有借鉴学习意义。

<font color="red">**缺点：**</font>H2GIS项目几乎没有可复用的任何点，包括IO部分，都需要大量的改造工作才可以使用与其他项目。

##### 3.7 噪声模拟分析插件

库主要功能是使用Groovy语言从OSM数据中提取噪声数据，并将其转换为模拟噪声的的地理信息图层。

##### 3.8 WMS插件

###### 3.8.1 WMS插件功能

WMS 遵循OGC标准，主要提供了两个功能：

* 根据url地址获取服务内容
* 生成地图与图层

###### 3.8.2 WMS使用样例

1. 获取服务内容

```java
// 加载xml文件
Document doc = getDocument(CAPABILITIES_1_3_0);
// 创建解析
ParserWMS1_3 parser = new ParserWMS1_3();
// 创建远程WMS服务
WMService service = new WMService("http://dummy.org/wms?");
// 解析服务
Capabilities cap = parser.parseCapabilities(service, doc);
// 获取顶层图层
MapLayer ml = cap.getTopLayer();
```

2. 生成地图图层

```java
// 创建图层窗口
String wgs = "EPSG:4326";
BoundingBox bb = new BoundingBox(s, new Envelope(40,50,40,50));
HashMap<String BoundingBox> hm = new HashMap<String,BoundingBox>();
hm.put(wgs, bb);
//生成图层
Collection<String> srs = new ArrayList<String>();
srs.add(s);
MapLayer c1 = new MapLayer("c1", "c1", srs, new ArrayList<MapLayer>(),bb, hm);
```

##### 3.9 Orbiswps插件功能分析

Orbiswps插件提供了Groovy语言操作数据库的功能，包括：增、删、查、改、数据库连接等功能。项目由Groovy语言开发。

##### 3.10 OrbisData库功能分析

###### 3.10.1 OrbisData功能模块

Orbisdata库分为两个大的模块：process-manager和data-manager。

* data-manager模块专门用于创建、访问和请求数据库。支持H2/H2GIS
* process-manager模块专用于创建具有in/output定义的进程，进程可以使用输入值执行，监听进程

###### 3.10.2 OrbisData代码使用样例

1. data-manager使用样例

```java
// 创建数据库文件，并连接数据库
H2GIS h2 = H2GIS.open("./target/" + UUID.randomUUID().toString());
// 创建表
h2gis.execute("CREATE TABLE toto(col1 int, col2 varchar)");
// 插入数据
h2gis.execute("INSERT INTO toto VALUES (0, 'val0'");
// 创建表明为toto 的select查询， 声明和语句已经组装，但没有进行excute
H2gisTable ijdbcTable = h2gis.getTable("toto")
// 执行语句，并将Result结果集组装
DataFrame df = DataFrame.of(ijdbcTable);
df.ncols(); // 2
df.columnIndex("COL1"); // 获取col1 列的value
df.column(0);  // 获取列name 第1列全部value
```

##### 3.11 Orbisgis Web项目分析

######  3.11.1 项目功能

###### 3.11.2 代码样例

#### 四、OrbisGis可复用点分析



--------------

附录：<font  color ="red" face="黑体">研究的OrbisGis及相关插件代码目录</font>

```markdown
1. OrbisGis项目源代码及功能分析
2. cts库代码功能分析
3. jts库源代码功能库分析
4. H2GIS插件源代码及功能分析
5. orbisanalysis库分析
6. h2gis-geotools项目 ----- 项目定义了H2gis数据库的Dialect关键字和函数，没有其他内容
7. h2gis-geoserve项目 ----- 项目只有连接H2库的启动类，geoserver更有价值
8. orbisdata项目
9. orbisserver项目
10. orbiswps项目
```

