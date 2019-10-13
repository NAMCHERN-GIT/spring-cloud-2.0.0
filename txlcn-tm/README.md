# txLcn-tm 分布式事务管理
## 一、原理
```text
    创建一个事务管理组Tm项目。
    LCN把事务注册到Tm中。然后结束后一起提交事务。
    TCC先把事务提交。然后错误后进入cl方法中对数据进行修改。
    只是简单的使用整理。要深入了解请看官方文档。
```
## 二、参考资料
源码地址：https://github.com/codingapi/tx-lcn
中文文档：http://www.txlcn.org/zh-cn/docs/preface.html

## 三、流程步骤
* 1.创建tx-manage数据库和表
* 2.创建Tm项目。修改配置
* 3.启动tm项目，并查看是否成功
* 4.使用tc并注册到Tm
* 5.使用@LcnTransaction模式
* 6.使用@TccTransaction模式

## 四、搭建
* [txlcn-tm 服务端搭建](txlcn-tm.md)
* [txlcn-tc 客户端搭建](txlcn-tc.md)
