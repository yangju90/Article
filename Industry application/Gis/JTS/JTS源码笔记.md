#### 一、基础数据样例

##### 1.1 geom基础数据

###### 1.1.1 创建点缓冲为圆

```java
//利用Monte Carlo方法近似计算PI
	公式： ① M/N = PI*R*R/4R*R  (落在圆内点的个数为M，所有点的个数为N)
    	  ② PI = 4M/N
//创建圆
static Geometry createCircle(){
  Geometry centrePt = geomFact.createPoint(new Coordinate(0.5, 0.5));
  return centrePt.buffer(0.5, 20);
}

PreparedGeometry.class 相对于Geometry.class
    1. 优化了对特定几何操作的重复调用性能
    2. 优化了部分方法其他依赖于Geometry
    3. 调用时线程安全的，PreparedGeometry的实现类目标就是供给多线程使用
```

###### 1.1.2 创建点、线，判断相交包含

```java
//1. WKT读取Geometry
Geometry g1 = new WKTReader().read("LINESTRING (0 0, 10 10, 20 20)");
//2.点创建线
Coordinate[] coordinates = new Coordinate[]{new Coordinate(0, 0),
      new Coordinate(10, 10), new Coordinate(20, 20)};
Geometry g2 = new GeometryFactory().createLineString(coordinates);
//3.线相交 g1 intersection g2: MULTILINESTRING ((0 0, 10 10), (10 10, 20 20))
Geometry g3 = g1.intersection(g2);
//4.线包含点 Point within g1: true
Geometry point = new GeometryFactory().createPoint(new Coordinate(1,1));
g1.contains(point)
```

###### 1.1.3 推荐的数据创建格式

```java
//依据GeometryFactory 创建geo数据
GeometryFactory fact = new GeometryFactory();
Point p1 = fact.createPoint(new Coordinate(0,0));

MultiPoint mpt = fact.createMultiPointFromCoords(new Coordinate[] 
				{ new Coordinate(0,0), new Coordinate(1,1) } );
```

###### 1.1.4 基础类扩展

```java
// 基于扩展实现的例子

1. ExtendedCoordinate类是对Coordiante的扩展 extends， (x,y,z) -> (x,y,z,m)
2. ExtendedCoordinateSequence实现CoordianteSequence接口 implements， Coordinate更有效的存储压缩方式
3. ExtendedCoordinateSequenceFactory工厂类

//扩展矩形框，若coordinates中点在框内无反应，若在框外则扩展
// minx = x<minx ? x : min  maxx = x>maxx ? x : maxx
public Envelope expandEnvelope(Envelope env){
    for (int i = 0; i < coordinates.length; i++ ) {
      env.expandToInclude(coordinates[i]);
    }
    return env;
}
```

###### 1.1.5 设置精度计算相交

```java
// Geometry计算精度
// floating 结果double类型
PrecisionModel pm = new PrecisionModel()
// floating-single 结果取小数点后6位
PrecisionModel pm = new PrecisionModel(PrecisionModel.FLOATING_SINGLE)
// fixed类型 结果取定义的精确位， 1为默认值，代表整数
PrecisionModel pm = new PrecisionModel(1)    
    
GeometryFactory fact = new GeometryFactory(pm);
WKTReader wktRdr = new WKTReader(fact);
Geometry C = A.intersection(B);

// 由于精度的不同，图形相交可能会出现不同结果，面有可能退化为点、线
```

###### 1.1.6 WKT数据创建几何

```java
GeometryFactory fact = new GeometryFactory();
WKTReader wktRdr = new WKTReader(fact);

String wktA = "POLYGON((40 100, 40 20, 120 20, 120 100, 40 100))";
String wktB = "LINESTRING(20 80, 80 60, 100 140)";
Geometry A = wktRdr.read(wktA);
Geometry B = wktRdr.read(wktB);
// A相交于B
Geometry C = A.intersection(B);
// 暂时不清楚intersectionMatrix 作用
IntersectionMatrix itersectionMatrix = A.relate(B)
```

##### 1.2 i读取KML数据，处理

```java
//io.gml2 整体为读取KML数据
1. KMLReader kml格式读取类， 利用XMLReader读取kml文件
2. KMLHandler Xml读取过程中的start
3. FixingGeometryFactory 类，粗暴修复LinearRing（没有闭合的点，直接闭合）

读取KML可参考， 可用dom4j构建
```

##### 1.3 linerref.LinearRefExample<font color=red>(未完，类作用未知)</font>

```java
LengthIndexedLine.class 类的作用未知，等待研究
    1. extractLine()
    2. indicesOf()
    3. extractPoint()
```

##### 1.4 操作运算

###### 1.4.1 几何间距离计算

```java
//Geometry 距离计算
GeometryFactory fact = new GeometryFactory();
WKTReader wktRdr = new WKTReader(fact);

Geometry A = wktRdr.read(wktA);
Geometry B = wktRdr.read(wktB);
// 距离计算类声明
DistanceOp distOp = new DistanceOp(A, B);
// A、B图形间的距离
double distance = distOp.distance();
// A、B图形间最近的线，生成（x1 y1，x2 y2）=> (x1,y1)属于A (x2,y2)属于B
Coordinate[] closestPt = distOp.nearestPoints();
LineString closestPtLine = fact.createLineString(closestPt);
// 计算线的长度 （方法：循环 + 勾股定理）
closestPtLine.getLength();
```

###### 1.4.2 线合并

```java
// 创建linerString线集合
Collection lines = new ArrayList();
LineMerger lineMerger = new LineMerger();
lineMerger.add(lineStrings);
// 集合中的线，合并为新的线集合
Collection mergedLineStrings = lineMerger.getMergedLineStrings();
```

合并入下图，合并规则：

* 合并线数据都是节点化的（它们仅有端点接触，线段不能相互交叉）
* 若要合并的线段方向不同，最终节点方向依据服从多数点的原则

<img src='JTS图片\图1.4.2 LinerMeger 线合并.jpg' style="zoom:50%">

###### 1.4.3 polygonize（多边形器，将线转化为面）

```java
Collection lines = new ArrayList();
lines.add(read("LINESTRING (0 0 , 10 10)")); // isolated edge
lines.add(read("LINESTRING (185 221, 100 100)")); //dangling edge
lines.add(read("LINESTRING (185 221, 88 275, 180 316)"));
lines.add(read("LINESTRING (185 221, 292 281, 180 316)"));
lines.add(read("LINESTRING (189 98, 83 187, 185 221)"));
lines.add(read("LINESTRING (189 98, 325 168, 185 221)"));
polygonizer.add(lines);
// 返回面集合
Collection polys = polygonizer.getPolygons();
// 数据第1,2条均属于dangling lines
Collection dangles = polygonizer.getDangles();
Collection cuts = polygonizer.getCutEdges();
```

<img src="JTS图片/图1.4.3 Polygonize 线合并为面.jpg" style="zoom:70%">

##### 1.5 precision.EnhancedPrecisionOp

```java
// enhanced precision techniques to reduce the likelihood of robustness problems.
// 增强类方法可以增强计算的鲁棒性（极少发生，至少样例在作者电脑上并没有发生错误！！！）
Geometry result = EnhancedPrecisionOp.intersection(g1, g2);
```

##### 1.6 technique

###### 1.6.1 线自相交判断

```java
// 线自相交判断（必须是cross<线段两端穿出> 而非touch<线段一端在线内，一端穿出> LineRing也检测不到），很值得去研究，里面的算法实现

LineString line1 = (LineString) (rdr.read("LINESTRING (0 0, 10 10, 20 20)"));
// 输出结果相交点
showSelfIntersections(line1);

public static Geometry lineStringSelfIntersections(LineString line){
    // getEndPoints 获取线的端点，输出为Geometry中Coordinate大小为 （2*线条数）的点
    // LineString 输出为2 ， MultiLineString 输出为2n
    Geometry lineEndPts = getEndPoints(line);
    // union输出原子化（noded）的线段
    Geometry nodedLine = line.union(lineEndPts);
    Geometry nodedEndPts = getEndPoints(nodedLine);
    Geometry selfIntersections = nodedEndPts.difference(lineEndPts);
    return selfIntersections;
}
```

###### 1.6.2 ~~PolygonUnionUsingBuffer~~

```java
// 图形取并集计算（合取）
Geometry[] geom = new Geometry[3];
geom[0] = rdr.read("POLYGON (( 100 180, 100 260, 180 260, 180 180, 100 180 ))");
geom[1] = rdr.read("POLYGON (( 80 140, 80 200, 200 200, 200 140, 80 140 ))");
geom[2] = rdr.read("POLYGON (( 160 160, 160 240, 240 240, 240 160, 160 160 ))");

GeometryFactory fact = geom[0].getFactory();
Geometry geomColl = fact.createGeometryCollection(geom);
Geometry union = geomColl.buffer(0.0);

// but 代码中不推荐使用buffer来合取，推荐使用 Geometry.union()， 但是为了增加健壮性，最好使用EnhancedPrecisionOp
// but 文档中说buffer速度会更快
```

###### 1.6.3 使用PreparedGeometry优化空间搜索

```java
// 2. 查询200000个点在 100个圆中的相交
// 1. 在1*1的范围内创建了100个圆平均分配（10*10）
// 创建索引STRtree
SpatialIndex index = new STRtree();
// 圆插入索引
index.insert(geom.getEnvelopeInternal(), PreparedGeometryFactory.prepare(geom));

// 检索
public List intersects(Geometry g){
    List result = new ArrayList();
    List candidates = query(g); //核心，先算索引，然后做相交计算 比直接计算相交快了10倍
    for (Iterator it = candidates.iterator(); it.hasNext(); ) {
        PreparedGeometry prepGeom = (PreparedGeometry) it.next();
        if (prepGeom.intersects(g)) {
            result.add(prepGeom);
        }
    }
    return result;
}

// STRtree查询
public List query(Geometry g){
    return index.query(g.getEnvelopeInternal());
}
```

##### 1.7 其他注意事项

###### 1.7.1 Geometry.union() 不支持 GEOMETRYCOLLECTION类型，

* union支持线与点的计算，输出线、点的数组（核心算法 OverlayOp.class -> computeOverlay() ）
* 支持面与线的计算，输出面、线、点
* 线之间的union不会merge，会生成多线

###### 1.7.2 geom.getEnvelopeInternal() 计算图形外接矩形，初始化的Envelope 

``` java
public void setToNull() {
  minx = 0;
  maxx = -1;
  miny = 0;
  maxy = -1;
}
```


#### 二、数据转换geojson

##### 2.1 GeoJson读写

```java
1. geojson数据格式的七种类型：Point、MultiPoint、LineString、MultiLineString、Polygon、MultiPolygon、GeometryCollection
```

#### 三、 JTS空间库索引

##### 3.1 STRtree

###### 3.1.1 STRtree创建后封底

```java
STRtree strTree =  new STRTree();
strTree.insert(Envelop, Object);

// 如果调用了strTree.size()、query()、item、remove等 STRtree就会封底
// 再次insert会报错， Cannot insert item into an STR packed R-tree after if has been built.
```

#### 四、 JTS库使用注意事项

##### 4.1 WKTReader读取几何形状，与Geometry判断

```java
//WTKReader读取解析矢量数据，只做了简单检查，一下图形都可以构建成功
WKTReader rdr = new WKTReader();
Polygon polygon = (Polygon) (rdr.read("POLYGON ((100 10, -100 10, -100 -10, 100 -10, 100 					10))"));
LineString lineString = (LineString) (rdr.read("LINESTRING (100 10, -100 10, -100 -10,    					 100 -10, 60 20, 100 10)"));
LinearRing linearRing = (LinearRing) (rdr.read("LINEARRING (100 10, -100 10, -100 -10,    					 100 -10, 60 20, 100 10)"));

// 但是很多图形不符合OGC SFS标准，标准验证如下
System.out.println(polygon.isValid()); //true
System.out.println(lineString.isValid()); //true
System.out.println(linearRing.isValid()); //false
```

