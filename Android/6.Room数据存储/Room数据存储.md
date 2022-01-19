### 1.Room入门

Room 在 SQLite 上提供了一个抽象层，以便在充分利用 SQLite 的强大功能的同时，能够流畅地访问数据库。

同其他ORM框架一样，Room包含3个主要组件：

- 数据库：使用@Database注释，有返回@Dao注释类得抽象方法
- 实体：数据库种得表
- DAO：用于访问数据库得方法

<img src="资源\图1.png" style="zoom:67%;" />

#### 1.1 数据库表映射

##### 1.1.2 常用注解

- @Entity  tableName设置表名，primaryKeys设置多主键；
- @PrimaryKey 主键， autoGenerate设置自增长； 
- @ColumnInfo name列名 
- @Index 索引 unique 唯一索引
- @Ignore 不需要持久化得字段

[[参考]](https://developer.android.google.cn/training/data-storage/room/defining-data)

```java
@Entity
public class User {
    @PrimaryKey
    public int uid;

    @ColumnInfo(name = "first_name")
    public String firstName;

    @ColumnInfo(name = "last_name")
    public String lastName;
}
```

##### 1.1.2 数据库表间关系

- 一对一
- 多对一
- 嵌套

[[参考]](https://developer.android.google.cn/training/data-storage/room/relationships)

#### 1.2 访问方法

数据库访问方法，分为增、删、改、查。

- @Insert 方法只接收 1 个参数，则它可以返回 `long`，这是插入项的新 `rowId`。如果参数是数组或集合，则应返回 `long[]` 或 `List<Long>`，其它返回Void。
- @Delete 便捷方法会从数据库中删除一组以参数形式给出的实体。它使用主键查找要删除的实体，可以返回一个int，表明删除得行数
- @Update 便捷方法会修改数据库中以参数形式给出的一组实体。它使用与每个实体的主键匹配的查询，可以返回一个int 表明更新得行数
- @Query 是 DAO 类中使用的主要注释。它允许您对数据库执行读/写操作。每个 [`@Query`](https://developer.android.google.cn/reference/androidx/room/Query) 方法都会在编译时进行验证，因此如果查询出现问题，则会发生编译错误，而不是运行时失败。Room 还会验证查询的返回值，以确保当返回的对象中的字段名称与查询响应中的对应列名称不匹配时，Room 可以通过以下两种方式之一提醒您：
  - 如果只有部分字段名称匹配，则会发出警告。
  - 如果没有任何字段名称匹配，则会发出错误。

```java
@Dao
public interface UserDao {
    @Query("SELECT * FROM user")
    List<User> getAll();

    @Query("SELECT * FROM user WHERE uid IN (:userIds)")
    List<User> loadAllByIds(int[] userIds);

    @Query("SELECT * FROM user WHERE first_name LIKE :first AND " +
           "last_name LIKE :last LIMIT 1")
    User findByName(String first, String last);

    @Insert
    void insertAll(User... users);

    @Delete
    void delete(User user);
}
```



#### 1.3 数据库持有者

数据库持有对象，必须为抽象类且继承RoomDatabase，抽象类加注解@Database，填写表对应实体对象和版本号（参考第3节版本迁移）

```java
@Database(entities = {User.class}, version = 1)
public abstract class AppDatabase extends RoomDatabase {
    public abstract UserDao userDao();
}
```

#### 1.4 创建数据库连接

创建数据库连接，初始化接口，调用方法

```java
AppDatabase appDatabase = Room.databaseBuilder(context, AppDatabase.class, Constants.DATABASE_NAME).build();

UserDao userDao = appDatabase.userDao();
```

##### 1.4.1 数据库的使用

(1) 主线程使用

如果未使用后备线程,Room启动会报错,所以最简单的方式是启动主线程查询数据库.

<font color=red>注意: 数据库在主线程种查询时间过长,有崩溃的风险</font>

```java
Room.databaseBuilder(context, AppDatabase.class,Constants.DATABASE_NAME)
    .allowMainThreadQueries()
    .build();
```

(2) 常见非主线程使用(AsyncTask)

<font color=red>这种方式已经逐步废弃和不推荐使用</font>

```java
static class InsertAsyncTask extends AsyncTask<Word, Void, Void>{
    private WordDao wordDao;

    public InsertAsyncTask(WordDao wordDao) {
        this.wordDao = wordDao;
    }

    @Override
    protected Void doInBackground(Word... words) {
        wordDao.insertWords(words);
        return null;
    }
}
```



### 2. 异步调用

#### 2.1 LiveData

##### 2.1.1 LiveData整合

(1) 接口

- LiveData 对写数据持久监控,响应SQLite 数据库写变化.
- ListenableFuture 一次性监控, 异步返回处理可以方便放入其他线程

<font color=red>虽然是异步,但数据请求还在主线程调用, </font>

```java
@Dao
public interface UserDao {
    // LiveData lifeCycle
    @Query("SELECT * FROM user")
    LiveData<List<User>> getAll();
	
    //Guava
    @Delete
    ListenableFuture<Integer> delete(User user);
}
```

(2) 使用

LiveData 返回可以当做ViewModel字段返回,在UI层观察使用

```java
public class UserViewModel extends BaseViewModel {

    private UserRepository userRepository = null;

    private LiveData<List<User>> listLiveDataUser = null;

    public RoomViewModel() {
        userRepository = UserRepository.getInstance();
        listLiveDataUser = new MutableLiveData<>();

        listLiveDataUser = getAll();
    }

    public LiveData<List<User>> getListLiveDataUser() {
        return listLiveDataUser;
    }
}

// UI层进行观测变化
userViewModel.getListLiveDataUser().observe()
```



#### 2.2 Rxjava

(1) 接口

- Flowable对写数据持久监控,响应SQLite 数据库写变化.
- Completable\Single一次性监控

<font color=red>RxJava 易于去做线程的切换,提供基础的BaseDatabase对数据库实例进行处理 </font>

```java
@Dao
public interface UserDao {
    @Query("SELECT * FROM user")
    Flowable<List<User>> getAll();

    @Delete
    Completable delete(User user);
    
    @Insert
    Single<Integer> insert(User user);
}
```

(2) 使用

LiveData 返回可以当做ViewModel字段返回,在UI层观察使用

```java
public class UserViewModel extends BaseViewModel {

    private UserRepository userRepository = null;

    private LiveData<List<User>> listLiveDataUser = null;

    public RoomViewModel() {
        userRepository = UserRepository.getInstance();
        listLiveDataUser = new MutableLiveData<>();

        listLiveDataUser = getAll();
    }

    public LiveData<List<User>> getListLiveDataUser() {
        return listLiveDataUser;
    }
}

// UI层进行观测变化
userViewModel.getListLiveDataUser().observe()
```

(3) BaseDatabase

```java
public abstract class BaseDataBase extends RoomDatabase {

    private CompositeDisposable compositeDisposable = new CompositeDisposable();


    public <T> void addDisposable(Flowable<T> flowable, Consumer<T> consumer) {
    	// 线程切换处理
        compositeDisposable.add(flowable
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(consumer));
    }

    public <T> void addDisposable(Completable completable, Action action) {
        compositeDisposable.add(completable
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(action));
    }

    public void dispose() {
        if(!compositeDisposable.isDisposed()){
            LogUtil.d("DISPOSE", this.getClass().getName());
            compositeDisposable.dispose();
        }
    }
}
```



#### 2.3 一次性请求和持久监控

持久监控,是指对数据库表写入变化的监控, 当有Insert Delete Update事件事,持久监控被触发.

|          | 一次性请求                    | 持久监控 | 优势                                            | 缺点                            |
| -------- | ----------------------------- | -------- | ----------------------------------------------- | ------------------------------- |
| Rxjava   | Single/Completable            | Flowable | 易于做线程切换                                  | 对生命周期管理监控,需要自己实现 |
| LiveData | ListenableFuture(利用Guava的) | LiveData | 与Activity LifeCycle 绑定, 无序特别处理生命周期 | 线程切换不方便                  |

### 3. 版本迁移

#### 3.1 忽略数据，强制迁移

忽略数据,版本不一致时,会删除数据SQLite 文件,重新创建(同时数据会丢失).

```java
UserDataBase = Room.databaseBuilder(context, UserDataBase.class, Constants.DATABASE_NAME)
    // 数据版本迁移
    .fallbackToDestructiveMigration()
    .build();
```

#### 3.2 修改数据表，数据迁移

迁移处理,需要在数据库创建时传入, 通过 @Database version字段指定下个版本号:

```java
UserDataBase = Room.databaseBuilder(context, UserDataBase.class, Constants.DATABASE_NAME)
    // 数据版本迁移
    .addMigrations(MIGRATION_2_3, MIGRATION_3_4)
    .build();
```



##### 3.2.1 增加字段

```java
// 版本 2到3的迁移脚本,增加 bar_data字段
Migration MIGRATION_2_3 = new Migration(2, 3) {
    @Override
    public void migrate(@NonNull SupportSQLiteDatabase database) {
        database.execSQL("ALTER TABLE word ADD COLUMN bar_data INTEGER NOT NULL DEFAULT 1");
    }
};
```

##### 3.2.2 删除字段 

SQLite 数据库不支持数据删除功能,所以数据迁移需要先拷贝临时表,然后Rename表名,实现如下:

```java
// 版本3 到4 的迁移脚本
Migration MIGRATION_3_4 = new Migration(3, 4) {
    @Override
    public void migrate(@NonNull SupportSQLiteDatabase database) {
        database.execSQL("CREATE TABLE word_temp (id INTEGER PRIMARY KEY NOT NULL, " +
                         "english_word TEXT, chinese_word TEXT)");
        database.execSQL("INSERT INTO word_temp (id, english_word, chinese_word) " +
                         "SELECT id, english_word, chinese_word FROM word");
        database.execSQL("DROP TABLE word");
        database.execSQL("ALTER TABLE word_temp RENAME TO word");
    }
};
```



### 4. 测试

