package org.exilekdingcc.java.cfgs;

import org.exiledkingcc.java.cfgs.ConfigManager;
import org.exiledkingcc.java.cfgs.ann.ConfigItem;
import org.exiledkingcc.java.cfgs.ann.ConfigKey;

import java.util.List;
import java.util.Map;


public class Test {

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

    public static void main(String args[]) {
        ConfigManager configManager = ConfigManager.INSTANCE;
        configManager.registerDefaultSource("http://localhost:8000");
        configManager.registerConfig(AppConfig.class);
        AppConfig appConfig = configManager.getConfig(AppConfig.class);
        System.out.println(appConfig.toString());
    }

}
