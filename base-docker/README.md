# 基本环境搭建
## 包含 redis,mysql,rabbitmq,fast-dfs
详细请参考[docker-compose](base/docker-compose.yml)

* 其中包含了网络的创建 base_net ，之所以叫这个名字是因为使用docker-compose 命令
的时候前面会追加文件所在的目录名称。

#### 构建过程:
* 1.拷贝连带文件夹base一起拷贝到docker服务器上
* 2.在base目录下执行命令,如下:
```shell script
docker-compose up -d 
```

## 制作最小化的java8 image
* 官网下载 jdk-8u191-linux-x64.tar.gz
* 解压拷贝出其中的jre目录,并且拷贝出的目录重新命名为 jre1.8
* 进入jre1.8目录
```shell script
cd jre1.8
```
* 删除其中一些没用的文件
```shell script
rm -rf COPYRIGHT LICENSE plugin/ README THIRDPARTYLICENSEREADME THIRDPARTYLICENSEREADME-JAVAFX.txt Welcome.html
rm -rf lib/plugin.jar \
       lib/ext/jfxrt.jar \
       bin/javaws \
       lib/javaws.jar \
       lib/desktop \
       plugin \
       lib/deploy* \
       lib/*javafx* \
       lib/*jfx* \
       lib/amd64/libdecora_sse.so \
       lib/amd64/libprism_*.so \
       lib/amd64/libfxplugins.so \
       lib/amd64/libglass.so \
       lib/amd64/libgstreamer-lite.so \
       lib/amd64/libjavafx*.so \
       lib/amd64/libjfx*.so
```
* 压缩成压缩包 jre1.8.tar.gz
* Dockerfile 文件编写
```dockerfile
FROM docker.io/jeanblanchard/alpine-glibc
MAINTAINER chen.nan<namchern@aliyun.com>
ADD jre1.8.tar.gz /usr/java/
ENV JAVA_HOME /usr/java/jre1.8/
ENV PATH ${PATH}:${JAVA_HOME}/bin
WORKDIR /opt

```
* 执行 docker build 命令
```shell script
docker build -f Dockerfile -t mini-linux:2.0.0 .
```
