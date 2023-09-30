# thin-spring-boot-maven-plugin
springboot包瘦身插件

# 应用场景

本插件是在spring-boot-maven-plugin基础上做的变更，因此只适用于基于spring-boot-maven-plugin打包的项目。

对于部分无法进行CD的项目（zf相关的），每次全量传输非常的耗费时间。本插件在打包的时候将第三方jar包排除，然后在启动的时候重组，从而减少包的大小，增加效率。


# 使用

1. 将spring-boot-maven-plugin插件替换为thin-maven-plugin

```
<project>
    <build>
        <plugins>
            <plugin>
                <groupId>com.hsq</groupId>
                <artifactId>thin-spring-boot-maven-plugin</artifactId>
                <version>xx</version>
            </plugin>
        </plugins>
    </build>
</project>
```

2. 配置保留的jar，支持正则表达式，配置排除文件根目录(可选)。windows系统排除文件默认根目录为“C:/thinMavenPlugin/ea/”,unix系统默认根目录为“/var/thinMavenPlugin/ea/”。

```
<project>
    <build>
        <plugins>
            <plugin>
                <groupId>com.hsq</groupId>
                <artifactId>thin-spring-boot-maven-plugin</artifactId>
                <configuration>
                    <mainClass>xx</mainClass>
                    <!-- 这里的layout必须为zip-->
                    <layout>ZIP</layout>
                    <includes>
                        <!-- 所有groupId为com.example的包都会保留下来 -->
                        <include>
                            <groupId>com.example</groupId>
                            <artifactId>.*</artifactId>
                        </include>
                        <include>
                            <groupId>com.example</groupId>
                            <artifactId>module2</artifactId>
                        </include>
                        ....
                    </includes>
                </configuration>
                <executions>
                    <execution>
                        <goals>
                            <goal>repackage</goal>
                        </goals>
                        <configuration>
                            <!-- 配置剔除文件存储位置-->
                            <winDir>D:/xx</winDir>
                            <unixDir>/xx</unixDir>
                        </configuration>
                    </execution>
                </executions>
            </plugin>
        </plugins>
    </build>
</project>
```

3. 启动

```
java -jar xx.jar -Dloader.path=libPath
```



# 再瘦身

在后续的开发中，还会引入新的第三方jar包，是不是又得手动将这些jar包以及它们依赖的jar包再重新放到
服务器上呢？

答案是不需要。

插件会自动识别新加入的jar包，并把他们打入的最终的包中，而不是将它们剔除！直到随着时间的推移，新加入的第三方包越来越多，这个时候我们考虑再做一次瘦身。
可以通过命令行参数来再瘦身。

```
mvn clean package -Dspring-boot.rethin=true
```

# 不瘦身

有的时候，我们可能需要spring-boot-maven-plugin打的包，这个时候我们可以修改打包命令来完成。

```
mvn clean package -Dspring-boot.includes=
```

这样就能拿到现在所有的第三方jar包，在做再瘦身的时候是很有用的。


# 写在最后

本人代码水平有限，精力有限，所以解决方案可能不是很完美，希望大家能多提宝贵意见。

# 不爱喝咖啡，豆浆就行:smile:

![zfb微信](https://github.com/wuyueshenggong/thin-spring-boot-maven-plugin/assets/131785606/f5188ada-ad31-4259-9303-171effebc1ce)


