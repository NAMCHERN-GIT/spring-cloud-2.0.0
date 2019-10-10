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