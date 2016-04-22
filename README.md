一种主动缓存机制，最大特点是列表结果只缓存ID集合，以提高效率。

### 方案

#### 创建

主动将对象写入缓存，以ID为key，这就保证用户第一次读取时速度也会很快，同时需要相关列表查询缓存

示例：假设新建某Category ID为10的Product 111，则需要牺牲所有Category ID为10的列表查询缓存 (findByCategoryId)，以及Category ID无关的全局查询缓存 (findByAll)。Category ID非10的相关列表缓存都不受影响。

#### 查询

- 单个查询：直接从缓存里以ID为key读取对象
- 列表查询：以列表参数为key，只缓存ID集合。查询时先根据查询参数拿到ID集合，再通过这些ID批处理拿到对应对象。

#### 更新

只用刷新此对象缓存，无其他逻辑

#### 删除

同创建类似，需要牺牲以下两部分缓存：

- 该对象本身缓存
- 该对象关联的列表缓存。

### 优缺点与改进

- 优点：空间和时间效率都很高，更新逻辑简单，所有缓存实现是代码级别，可控易测试
- 缺点：代码实现较注解复杂的多
- 改进：可以参考Spring的几个cache相关注解的实现，以类似的方式实现上述逻辑（AOP + 注解），这样使用起来就更加容易了，但技术挑战较大。

### Demo

1. 为了实现方便使用了spring data jpa和hibernate，实际可替换为mybatis
1. 运行`./gradlew bootRun`
1. 运行redis-server在本地`redis-server`
1. 运行各种命令获得结果
    1. 查询所有products：`curl -H 'Content-Type: application/json'  localhost:8080/products`
    1. 查询指定category的products：`curl -H 'Content-Type: application/json'  localhost:8080/products?categoryId=2`
    1. 创建product：`curl -H 'Content-Type: application/json'  localhost:8080/products -d '{"name":"abc","categoryId":2}'`
    1. 更新product：`curl -H 'Content-Type: application/json'  localhost:8080/products/3 -d '{"name":"aaa"}' -X PUT`
    1. 删除product：`curl -H 'Content-Type: application/json'  localhost:8080/products/3 -X DELETE`
1. 日志会打印此次查询是来自于Cache还是DB
