# Launcher Lib

## 为什么创建这个项目

因为LunarClient使用JS写的启动器磁盘空间占用过多, 而且不支持自定义, 所以此项目就诞生了

## 如何使用

在build.gradle中添加如下代码

```groovy
repositories {
    maven { url 'https://www.jitpack.io' }
}

dependencies {
    implementation 'com.github.CubeWhy:LauncherLib:master-SNAPSHOT'
}
```

