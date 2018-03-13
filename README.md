# mqrpc

mqrpc 是一个利用rabbitmq、protostuff实现的简易RPC框架。


**如果觉得不错，请先在这个仓库上点个 star 吧**，这也是对我的肯定和鼓励，谢谢了。

目前实现了基本的RPC功能，后续会持续进行优化和完善，需要关注更新的请 watch、star、fork

---

chat-server 的主要特色：

- 基于rabbitmq的高性能与可靠性进行通信
- 使用protostuff序列化

## 仓库目录

- [server](/server):RPC服务端，提供服务 
- [mq-core](/mq-core):核心代码，提供相关注解以及服务端与客户端的相关实现
- [client](/client): RPC客户端

## 环境要求

- JDK 1.8+
- Maven 3.3+



## 快速开始



1.修改配置

服务端可客户端的配置文件配置了相应的端口号与rabbitmq相关配置，如需改动请修改：



2.启动节点

先运行运行 `server`，然后启动`client` 



3.添加方法

在`/mq-core/client`提供相关接口、`service`服务端添加实现，并添加@RpcService注解



## TODO

* [x] 利用rabbitmq实现基础的通信功能
* [x] 客户端提供了相应的测试方法
* [ ] 后续优化


-----




## Lisence

Lisenced under [Apache 2.0 lisence](http://opensource.org/licenses/Apache-2.0)

