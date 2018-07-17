# java-cfgs
java config lib supporting for multiple sources and formats

简单的java配置库，支持多种配置源与配置格式

## 定义：
- 配置源（`ConfigSource`）：配置存放的位置，比如jar包里面，文件系统，网络等等
- 配置格式（`ConfigFormat`）：`properties`、`json`等等
- 配置项（`ConfigItem`）：一个存放配置的类，`fields`与配置中的定义通过注解对应起来。
- 配置键（`ConfigKey`）：配置的key

## 配置源：
配置源通过一个路径来定义：`scheme://path`，不同的scheme对应不同的配置源。
支持：
- `classpath`：固定为`cp://`
- `filesystem`：指向文件夹，`file://<path to the configs dir>`
- `http`：http的url，`http(s)://<base url of the configs>`
- `zookeeper`：path形式为：`zk://[user:pass]@<zk connect string>/<chroot path>`
- `etcd`：path形式为：`etcd://[user:pass]@<etcd endpoints list>/<prefix>`
  - `user:pass`、集群待验证 (TODO)

## 配置格式：
支持：
- `properties`(done)
- `json`(todo)
- `yaml`(maybe)

最终配置会被映射到配置类，配置类支持的类型：
- 基本类型，包括`bool`、`int`、`float`、`string`
- 带有`ConfigKey`的类
- Map，需要注明`itemType`
- List，需要注明`itemType`

## 使用：
假设我们有一个`app.properties`的配置文件，内容是这样的：
```
# basic
app_name = appname
debug = true
num = 111
ratio = 0.22

# map
mysql.addr = jdbc:mysql://192.168.1.2:3306/database
mysql.driver = com.mysql.jdbc.Driver
mysql.user = mysql_user
mysql.pass = mysql_paas

# map in map
dbs.mongo_x.addr = 192.168.1.1
dbs.mongo_x.port = 12710
dbs.mongo_x.auth_db = xxdb
dbs.mongo_x.user = xx_user
dbs.mongo_x.pass = xx_paas

dbs.mongo_y.addr = 192.168.1.1
dbs.mongo_y.port = 12720
dbs.mongo_y.auth_db = yydb
dbs.mongo_y.user = yy_user
dbs.mongo_y.pass = yy_paas

# list
users.$0.id = 1
users.$0.name = u1
users.$0.info.email = u1@u1.com
users.$0.info.age = 11

users.$1.id = 2
users.$1.name = u2
users.$1.info.email = u2@u2.com
users.$1.info.age = 22
```
它可以映射到`AppConfig`这个类里面。
```java
public static class User {

    public static class Info {
        @ConfigKey(key = "email")
        String email;
        @ConfigKey(key = "age")
        int age;

        @Override
        public String toString() {
            return "Info{" +
                    "email='" + email + '\'' +
                    ", age=" + age +
                    '}';
        }
    }

    @ConfigKey(key = "id")
    int id;
    @ConfigKey(key = "name")
    String name;
    @ConfigKey(key = "info")
    Info info;

    @Override
    public String toString() {
        return "User{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", info=" + info +
                '}';
    }
}

public static class MongoDB {
    @ConfigKey(key = "addr")
    String addr;
    @ConfigKey(key = "port")
    int port;
    @ConfigKey(key = "auth_db")
    String authDB;
    @ConfigKey(key = "user")
    String user;
    @ConfigKey(key = "pass")
    String pass;

    @Override
    public String toString() {
        return "MongoDB{" +
                "addr='" + addr + '\'' +
                ", port=" + port +
                ", authDB='" + authDB + '\'' +
                ", user='" + user + '\'' +
                ", pass='" + pass + '\'' +
                '}';
    }
}

@ConfigItem(name = "app.properties")
public static class AppConfig {

    @ConfigKey(key = "app_name")
    String appname;

    @ConfigKey(key = "debug")
    boolean debug;

    @ConfigKey(key = "num")
    int num;

    @ConfigKey(key = "ratio")
    float ratio;

    @ConfigKey(key = "optional", optional = true)
    boolean optional = true;

    @ConfigKey(key = "users", itemType = User.class)
    List<User> users;

    @ConfigKey(key = "dbs", itemType = MongoDB.class)
    Map<String, MongoDB> mongos;

    @Override
    public String toString() {
        return "AppConfig{" +
                "appname='" + appname + '\'' +
                ", debug=" + debug +
                ", num=" + num +
                ", ratio=" + ratio +
                ", optional=" + optional +
                ", users=" + users +
                ", mongos=" + mongos +
                '}';
    }
}
```
然后设置配置源与注册`AppConfig`：
```java
ConfigManager configManager = ConfigManager.INSTANCE;
// 默认为`cp://`
configManager.registerDefaultSource("http://localhost:8000");
// 使用默认的ConfigSource可以省略前面的path
configManager.registerConfig("cp://", AppConfig.class);
```

最后获取配置：
```java
AppConfig appConfig = configManager.getConfig(AppConfig.class);
```

see Test.java for detail
