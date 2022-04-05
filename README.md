# codedream-redis

在微服务开发过程中，不免会用到分布式锁和限流，采用 Redis 进行分布式锁和限流性能也不会影响太大的性能。codedream-redis 其中包含了自定义注解实现方法的分布式锁和限流的功能。

## 使用方法

在你的项目pom.xml引入`codedream-redis`依赖，如下：

```xml

<dependency>
    <groupId>com.code.dream</groupId>
    <artifactId>codedream-redis</artifactId>
    <version>1.0.0-SNAPSHOT</version>
</dependency>
```

## 主要特性

* CodeDreamRedis是redis的操作工具类
* 通过自定义注解实现方法的限流和分布式锁，简化开发流程，并且对限流和分布式进行统一管理。
* 能够通过配置采用不同的序列化方式，提高网络传输效率。
* 自定义`RedisCacheManager` 实现通过 cacheName 控制缓存的过期时间

### 1.redis工具类的使用

```java
@RestController
@AllArgsConstructor
public class DemoController {

  private final CodeDreamRedis codeDreamRedis;

  @GetMapping
  public String get() {
    return codeDreamRedis.get("test");
  }

  @PostMapping
  public Boolean save() {
    codeDreamRedis.set("test", "1111");
    return Boolean.TRUE;
  }
}
```

### 2.分布式锁

- 使用场景
    - 定时任务：此次定时任务还未执行（某种原因延迟）拖到下一次执行时，但此次结果可能会影响到后续的定时任务时，需要通过分布式锁进行让其不可同时执行多次，否则就会出现多次执行现象。
    - 在并发量较高的情况，需要保证获取数据和修改数据时的数据一致，需要在获取并修改时添加分布式锁，让其此段代码只能一个线程在执行。

- 配置-单体

```yaml
codedream:
  redis:
    lock:
      enabled: true
      ## 单体服务 IP+Port
      address: "redis://127.0.0.1:6379"
      password: 123456
      database: 0
      pool-size: 20
      idle-size: 5
      idle-timeout: 60000
      connection-timeout: 3000
      timeout: 10000
      ## 单体模式
      mode: single
```

- 配置-主从

```yaml
codedream:
  redis:
    lock:
      enabled: true
      password: 123456
      database: 0
      pool-size: 20
      idle-size: 5
      idle-timeout: 60000
      connection-timeout: 3000
      timeout: 10000
      ## 主从模式
      mode: master
      ## 主从的主节点
      master-address: 127.0.0.1:6379
      ## 主从的从节点
      slave-address:
        - 127.0.0.1:6379
        - 127.0.0.1:6380
```

- 配置-哨兵

```yaml
codedream:
  redis:
    lock:
      enabled: true
      password: 123456
      database: 0
      pool-size: 20
      idle-size: 5
      idle-timeout: 60000
      connection-timeout: 3000
      timeout: 10000
      ## 哨兵模式
      mode: sentinel
      master-name: myLock
      ## 哨兵节点列表
      sentinel-address:
        - 127.0.0.1:6379
        - 127.0.0.1:6380
```

- 配置-集群

```yaml
codedream:
  redis:
    lock:
      enabled: true
      password: 123456
      database: 0
      pool-size: 20
      idle-size: 5
      idle-timeout: 60000
      connection-timeout: 3000
      timeout: 10000
      ## 集群模式
      mode: cluster
      ## 集群节点列表
      node-address:
        - 127.0.0.1:6379
        - 127.0.0.1:6380
```

- 使用

```java
@RedisLock(lockName = "test", param = "#p0", waitTime = 30, leaseTime = 100, timeUnit = TimeUnit.SECONDS, type = LockType.REENTRANT)
```

| 注解字段 | 解释 | 示例 |
|:---:|:---:|:---:|
| vale | 分布式锁的 key，必须：请保持唯一性 | test|
| param | 分布式锁参数，可选，支持 spring el # 读取方法参数和 @ 读取 spring bean | #p0 |
| waitTime | 等待锁超时时间，默认30 | 30 |
| leaseTime | 自动解锁时间，自动解锁时间一定得大于方法执行时间，否则会导致锁提前释放，默认100 | 100 |
| timeUnit | 时间单位，默认为秒 | TimeUnit.SECONDS |
| type | 锁的类型，默认公平锁 | LockType.REENTRANT |

### 3.限流

- 使用场景
    - 外部恶意的访问（例如：爬虫数据的爬取、自动化的频繁的请求）可以进行限流，从而实现对该类恶意请求的屏蔽。
- 配置

```yaml
codedream:
  redis:
    rate-limiter:
      enabled: true
```

- 使用

```java
@RateLimiter(value = "test", param = "#p0", max = 100, ttl = 1, timeUnit = TimeUnit.MINUTES)
```

| 注解字段 | 解释 | 示例 |
|:---:|:---:|:---:|
| vale | 限流的 key 支持，必须：请保持唯一性 | test|
| param | 分布式锁参数，可选，支持 spring el # 读取方法参数和 @ 读取 spring bean | #p0 |
| max | 支持的最大请求，默认: 100 | 100 |
| ttl | 持续时间，默认: 1 | 1 |
| timeUnit |                      时间单位，默认为分                      | TimeUnit.MINUTES |

### 4.序列化方式

- 使用场景
    - 大量数据缓存传输是可以采用 `ProtoStuff`
    - 想要在 redis 管理端查看可以采用 `json`

- 配置

```yaml
codedream:
  redis:
    serializer-type: protostuff
```

### 4.扩展 Cacheable 的Cache Name

- 使用场景
    - 缓存数据最好都加上缓存过期时间，所以对 Cacheable 进行扩展，实现通过 CacheName 设置缓存的过期时间。

- 使用

```java
@Cacheable(cacheNames = "test#100", key = "test")
```

| 注解字段 | 解释 | 示例 |
|:---:|:---:|:---:|
| cacheNames | #后面为缓存时间，单位为秒 | test#1 |

### demo地址（https://github.com/CodeDreamPlus/codedream-redis-demo）
