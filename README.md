# spring cloud 微服务搭建过程
## 一、最小化java8基本环境搭建
参考[文档base-docker](base-docker/README.md)
## 二、spring-cloud母工程创建
### 母工程pom文件编写  
参考[spring-cloud-2.0.0.pom](pom.xml)

## 三、Eureka服务注册中心服务器集群搭建
参考[文档eureka-server](eureka-server/README.md)

## 四、Spring Cloud 配置中心
参考[文档config-server](config-server/README.md)

## 五、zipkin服务链路追踪服务中心
参考[文档zipkin-server](zipkin-server/README.md)

## 六、服务网关中心

参考[文档zuul-gateway](zuul-gateway/README.md)

## 七、分布式事务管理
参考[文档txlcn-tm](txlcn-tm/README.md)

## 八、服务与授权中心
参考[文档oauth2-server](oauth2-server/README.md)

## 九、分布式任务调度平台
参考[文档xxl-job-admin](xxl-job-admin/README.md)

## 十、用户微服务
参考[文档user-server](user-server/README.md)

## 各微服务端口

| 序号 |    微服务名称    | 说明                 |  映射端口  |  内部端口  |
| :--: | :--------------: | :------------------- | :--------: | :--------: |
|  1   |      redis       | redis服务器          |    6379    |    6379    |
|  2   |     rabbitmq     | rabbitmq消息中间件   | 5672/15672 | 5672/15672 |
|  3   |   mysql-server   | MySQL服务器          |    3306    |    3306    |
|  4   | fast-dfs-tracker | fastDFS              |   22122    |   22122    |
|  5   | fast-dfs-storage | fastDFS客户端        |            |            |
|  6   |      gitlab      | git服务器            |   80/443   |   80/443   |
|  7   |      eureka      | 注册中心             |    8761    |    8761    |
|  8   |  config-server   | 配置中心             |    3344    |    3344    |
|  9   |   zuul-gateway   | 服务网关             |    8000    |    8000    |
|  10  |  zipkin-server   | 服务链路追踪服务中心 |    9000    |    9000    |
|  11  |     txlcn-tm     | 分布式事务管理       | 7970/8070  | 7970/8070  |
|  12  |  oauth2-server   | 服务与授权中心       |    6000    |    6000    |
|  13  |  xxl-job-admin   | 分布式任务调度平台   |    5000    |    5000    |
|  14  |   user-server    | 用户微服务           |    8001    |    8001    |
|      |                  |                      |            |            |
|      |                  |                      |            |            |

