# Spring Cloud

# 一、服务治理

## 1、基本概念

服务治理的核心组件：

- Provider
- Consumer
- 注册中心

服务治理的两大功能：

- 服务注册：在分布式系统中，每个微服务在启动时，会把自己的信息存储到注册中心

- 服务发现：Consumer从注册中心获取Provider的网络信息，通过该信息调用服务

> Spring Cloud使用Eureka实现服务治理，可以轻松整合Spring Boot微服务应用



## 2、Spring Cloud Eureka

- Eureka Server，注册中心
- Eureka CLient，要注册的微服务都通过Eureka Client连接到 Eureka Server，完成注册



## 3、Eureka Server 代码实现

- 创建一个空的父工程，其pom.xml为

```xml
	<parent>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-parent</artifactId>
        <version>2.0.7.RELEASE</version>
    </parent>

    <dependencies>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-web</artifactId>
        </dependency>
    </dependencies>

    <dependencyManagement>
        <dependencies>
            <dependency>
                <groupId>org.springframework.cloud</groupId>
                <artifactId>spring-cloud-dependencies</artifactId>
                <version>Finchley.SR2</version>
                <type>pom</type>
                <scope>import</scope>
            </dependency>
        </dependencies>
    </dependencyManagement>
```

- 在父工程下创建一个maven的module，pom.xml如下

```xml
 	<artifactId>eurekaserver</artifactId>	

	<dependencies>
        <dependency>
            <groupId>org.springframework.cloud</groupId>
            <artifactId>spring-cloud-starter-netflix-eureka-server</artifactId>
            <version>2.0.2.RELEASE</version>
        </dependency>
    </dependencies>
```

- 创建配置文件 application.yml，需要添加 Eureka Server 相关配置

```yml
server:
  port: 8761
eureka:
  client:
    register-with-eureka: false # 是否将当前的 Eureka Server 服务作为客户端注册
    fetch-registry: false # 是否同步其他注册中心配置
    service-url:
      defaultZone: http://localhost:8761/eureka # 注册中心的访问地址
```

- 创建启动类

```java
@SpringBootApplication
@EnableEurekaServer // 声明该类是一个 Eureka Server 微服务
public class EurekaServerApplication {
    public static void main(String[] args) {
        SpringApplication.run(EurekaServerApplication.class, args);
    }
}
```

>运行启动类，访问 http://localhost:8761/ 即可查看注册相关信息



## 4、Eureka Client 代码实现

>注意：
>
>- Provider和Consumer是人为定义的概念，都是Eureka Client
>- Provider本身在提供服务的同时，也可以作为Consumer调用其他Provider提供的服务
>- 消费者同理，所以一个服务只要在Eureka注册为client并提供了方法，就同时是提供者和消费者

- 创建一个maven的module，pom.xml如下

```xml
	<artifactId>eeurekaclient</artifactId>	

	<dependencies>
        <dependency>
            <groupId>org.springframework.cloud</groupId>
            <artifactId>spring-cloud-starter-netflix-eureka-client</artifactId>
            <version>2.0.2.RELEASE</version>
        </dependency>
    </dependencies>
```

- 创建配置文件 application.yml，添加 Eureka Client 相关配置

```yml
server:
  port: 8010
spring:
  application:
    name: provider
eureka:
  client:
    service-url:
      defaultZone: http://localhost:8761/eureka/
  instance:
    prefer-ip-address: true
```

>配置属性说明

`spring.application.name`：当前服务注册在 Eureka Server 上的名称。

`eureka.client.service.defaultZone`：注册中心的访问地址。

`eureka.instance.prefer-ip-address`：是否将当前服务的 ip 注册到 Eureka Server。

- 创建启动类

```java
@SpringBootApplication
public class ProviderApplication {
    public static void main(String[] args) {
        SpringApplication.run(ProviderApplication.class, args);
    }
}
```

- 实现最简单的业务逻辑

```java
// StudentRepositoryImpl
@Repository
public class StudentRepositoryImpl implements StudentRepository {

    private static Map<Long, Student> studentMap;

    static {
        studentMap = new HashMap<>();
        studentMap.put(1L, new Student(1L, "张三", 22));
        studentMap.put(2L, new Student(2L, "李四", 23));
        studentMap.put(3L, new Student(3L, "王五四", 24));
    }

    @Override
    public Collection<Student> findALl() {
        return studentMap.values();
    }

    @Override
    public Student findById(long id) {
        return studentMap.get(id);
    }

    @Override
    public void saveOrUpdate(Student student) {
        studentMap.put(student.getId(), student);
    }

    @Override
    public void deleteById(long id) {
        studentMap.remove(id);
    }
}

// StudentHandler
@RestController
@RequestMapping("/student")
public class StudentHandler {

    @Resource
    public StudentRepository studentRepository;

    @GetMapping("/findAll")
    public Collection<Student> findAll() {
        return studentRepository.findALl();
    }

    @GetMapping("/findById/{id}")
    public Student findById(@PathVariable("id") long id) {
        return studentRepository.findById(id);
    }

    @PostMapping("/save")
    public void save(@RequestBody Student student) {
        studentRepository.saveOrUpdate(student);
    }

    @PutMapping("/update")
    public void update(@RequestBody Student student) {
        studentRepository.saveOrUpdate(student);
    }

    @DeleteMapping("/deleteById/{id}")
    public void deleteById(@PathVariable("id") long id) {
        studentRepository.deleteById(id);
    }
}
```





## 5、RestTemplate的使用

-  创建一个maven的module，不需要引入依赖，parent的spring boot 依赖都有

```java
@SpringBootApplication
public class RestTemplateApplication {
    public static void main(String[] args) {
        SpringApplication.run(RestTemplateApplication.class, args);
    }
	// 直接把RestTemplate注入到IOC容器
    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }
}
```

- http接口

```java
@RestController
@RequestMapping("/rest")
public class RestHandler {

    @Resource
    private RestTemplate restTemplate;

    @GetMapping("/findAll")
    public Collection<Student> findAll() {
        return restTemplate.getForEntity("http://localhost:8010/student/findAll", Collection.class).getBody();
    }

    @GetMapping("/findAll2")
    public Collection<Student> findAll2() {
        return restTemplate.getForObject("http://localhost:8010/student/findAll", Collection.class);
    }
    
}
```

>解释：
>
>- 必须有相应的Provider定义的相同实体类
>
>- Consumer服务本身没有任何业务逻辑，只有http接口
>
>- 通过访问consume服务暴露的http接口，能直接获取信息
>- 虽然现在能调用提供者的服务，但是本身未注册，还不是消费者



## 6、服务消费者Consumer

- 创建一个maven的module，其pom.xml如下（其实和上一步几乎一样，就多了注册）

```xml
	<artifactId>eeurekaclient</artifactId>

    <dependencies>
        <dependency>
            <groupId>org.springframework.cloud</groupId>
            <artifactId>spring-cloud-starter-netflix-eureka-client</artifactId>
            <version>2.0.2.RELEASE</version>
        </dependency>
    </dependencies>
```

- 创建配置文件 application.yml

```yml
server:
  port: 8020
spring:
  application:
    name: consumer
eureka:
  client:
    service-url:
      defaultZone: http://localhost:8761/eureka/
  instance:
    prefer-ip-address: true
```

- 创建启动类

```java
@SpringBootApplication
public class ConsumerApplication {
    public static void main(String[] args) {
        SpringApplication.run(ConsumerApplication.class, args);
    }
    
    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }
}
```

- http接口

```java
@RestController
@RequestMapping("/consumer")
public class ConsumerHandler {

    @Resource
    private RestTemplate restTemplate;

    @GetMapping("/findAll")
    public Collection<Student> findAll() {
        return restTemplate.getForEntity("http://localhost:8010/student/findAll", Collection.class).getBody();
    }

    @GetMapping("/findAll2")
    public Collection<Student> findAll2() {
        return restTemplate.getForObject("http://localhost:8010/student/findAll", Collection.class);
    }
}
```



# 二、服务网关

## 1、基本概念 

为什么要有服务网关：

- 一个客户端会调用多个微服务，比如用户信息的微服务，商品微服务，订单微服务
- 客户端编写代码需要区分很多不同的host和端口，很麻烦
- 每个微服务都需要针对请求进行安全验证，很麻烦
- 跨域请求处理起来也比较复杂，尤其是多客户端多微服务的情况

>所有外部请求访问服务只需要与服务网关交互，不暴露服务细节
>
>所有外部请求只需要访问一个相同的地址，跨域也由网关进行统一处理
>
>还可以抽离所有公共业务逻辑到网关，由网关进行统一安全验证



spring cloud 集成了 Zuul组件，实现服务网关：

- 具备反向代理的功能（通过拦截请求并转发解决了跨域）-> 只有浏览器由于同源策略会跨域
- 网管内部实现了动态路由、身份认证、IP过滤、数据监控等



## 2、Zuul网关代码实现

- 创建一个maven的module，pom.xml如下

```xml
	<artifactId>zuul</artifactId>

    <dependencies>
        <dependency>
            <groupId>org.springframework.cloud</groupId>
            <artifactId>spring-cloud-starter-netflix-eureka-client</artifactId>
            <version>2.0.2.RELEASE</version>
        </dependency>

        <dependency>
            <groupId>org.springframework.cloud</groupId>
            <artifactId>spring-cloud-starter-netflix-zuul</artifactId>
            <version>2.0.2.RELEASE</version>
        </dependency>
    </dependencies>
```

- 创建application.yml

```yml
server:
  port: 8030
spring:
  application:
    name: gateway
eureka:
  client:
    service-url:
      defaultZone: http://localhost:8761/eureka/
zuul:
  routes:
    provider: /p/**
```

>属性说明：

`zuul.route.provider`：给服务提供者 provider 设置映射

- 创建启动类

```java
@EnableZuulProxy
@EnableAutoConfiguration
public class ZuulApplication {
    public static void main(String[] args) {
        SpringApplication.run(ZuulApplication.class, args);
    }
}
```

>注解说明：

`@EnableZuulProxy`：包含了`@EnableZuulServer`，设置该类是网络的启动类

`@EnableAutoConfiguration`：可以帮助Spring Boot应用将所有符合条件的`@Configuration`配置加载到当前SpringBoot创建并使用的IOC容器中

- 现在通过http://localhost:8030/p/student/findAll 即可访问到provider提供的服务
- 以后所有的服务都可以收口到这一个`localhost:8030`地址进行转发

- 同时，zuul针对访问同一服务的请求通过集成 ribbon 实现负载均衡



## 3、Ribbon负载均衡

基于eureka注册的所有服务，选择一种负载均衡算法（可以自定义），对访问该微服务的请求进行负载均衡

- 创建module，pom.xml如下

```xml
    <dependencies>
        <dependency>
            <groupId>org.springframework.cloud</groupId>
            <artifactId>spring-cloud-starter-netflix-eureka-client</artifactId>
            <version>2.0.2.RELEASE</version>
        </dependency>
    </dependencies>
```

- 创建配置文件

```yml
server:
  port: 8040
spring:
  application:
    name: ribbon
eureka:
  client:
    service-url:
      defaultZone: http://localhost:8761/eureka/
  instance:
    prefer-ip-address: true
```

- 创建启动类

```java
@SpringBootApplication
public class RibbonApplication {
    public static void main(String[] args) {
        SpringApplication.run(RibbonApplication.class, args);
    }

    @Bean
    @LoadBalanced
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }
}
```

`@LoadBalanced`：声明一个基于Ribbon的负载均衡

- 定义逻辑（需要与Provider保持相同的Student实体类）

```java
@RestController
@RequestMapping("/ribbon")
public class RibbonHandler {

    @Resource
    private RestTemplate restTemplate;

    public Collection<Student> findAll() {
        return restTemplate.getForObject("http://provider/student/findAll", Collection.class);
    }
}
```

>注意：
>
>- 因为已经注册了Provider，可以直接通过`http://provider/student/findAll`访问Provider的服务
>- spring.application.name即provider对应的所有实例会实现负载均衡



## 4、客户端负载均衡

Ribbon不是作为单独的服务部署的：

- 某个服务注册到eureka后，即是Provider（实现了自己的业务逻辑并提供对外接口）又是Consumer（继承了RestTemplate）
- 当其作为消费者调用其他服务的方法（自身为客户端），只要添加`@LoadBalanced`注解即可实现对调用者的负载均衡





# 三、Feign

## 1、基本概念

- 一个声明式、模板化的 web Service 客户端

- 具备可插拔、基于注解、负载均衡、服务熔断等功能
- 整合了 Ribbon
- 整合了 Hystrix

>相比于 Ribbon + RestTemplate 的方式，Feign大大简化了开发



## 2、代码实现

- 创建一个maven的module，pom.xml如下

```xml
	<artifactId>feign</artifactId>

    <dependencies>
        <dependency>
            <groupId>org.springframework.cloud</groupId>
            <artifactId>spring-cloud-starter-netflix-eureka-client</artifactId>
            <version>2.0.2.RELEASE</version>
        </dependency>

        <dependency>
            <groupId>org.springframework.cloud</groupId>
            <artifactId>spring-cloud-starter-openfeign</artifactId>
            <version>2.0.2.RELEASE</version>
        </dependency>
    </dependencies>
```

- application.yml

```yml
server:
  port: 8050
spring:
  application:
    name: feign
eureka:
  client:
    service-url:
      defaultZone: http://localhost:8761/eureka/
  instance:
    prefer-ip-address: true
```

- 创建启动类

```java
@SpringBootApplication
@EnableFeignClients
public class FeignApplication {
    public static void main(String[] args) {
        SpringApplication.run(FeignApplication.class, args);
    }
}
```

- 创建声明式接口

```java
@FeignClient(value = "provider")
public interface FeignProviderClient {

    @GetMapping("/student/findAll")
    Collection<Student> findAll();
}
```

- Handler类（对外的接口）

```java
@RestController
@RequestMapping("/feign")
public class FeignHandler {

    @Resource
    private FeignProviderClient feignProviderClient;

    @GetMapping("/findAll")
    public Collection<Student> findAll() {
        return feignProviderClient.findAll();
    }

}
```

>此时通过http://localhost:8050/feign/findAll即可调用Provider提供的服务



## 3、熔断机制

一个请求可能访问多个微服务，如果某一个微服务出现问题，可能导致整个项目崩溃

- application.yml添加属性，修改为

```yml
server:
  port: 8050
spring:
  application:
    name: feign
eureka:
  client:
    service-url:
      defaultZone: http://localhost:8761/eureka/
  instance:
    prefer-ip-address: true
feign:
  hystrix:
    enabled: true
```

- 创建FeignProviderClient接口的实现类 FeignError，定义容错机制，需要注入IOC

```java
@Component
public class FeignError implements FeignProviderClient {
    
    @Override
    public Collection<Student> findAll() {
        return null;
    }
}
```

- 在FeignProviderClient定义处添加 `@FeignClient` 的 fallback 属性设置映射

```java
@FeignClient(value = "provider", fallback = FeignError.class)
public interface FeignProviderClient {

    @GetMapping("/student/findAll")
    Collection<Student> findAll();
}
```

>- 此时停止Provider的服务，访问http://localhost:8050/feign/findAll
>- 显示为空页面，不为错误页面，因为降级返回了null





# 四、Hystrix容错机制

## 1、基本概念

在不改变微服务调用关系的前提下，针对错误情况进行预先处理

- 设计原则

1、服务隔离机制

2、服务降级机制

3、熔断机制

4、提供实时的监控和报警功能

5、提供实时的配置修改功能

Hystrix 数据监控需要结合 Spring BootActuator 来使用，Actuator提供了对服务的健康监控、数据统计，可以通过hystrix-stream节点获取监控的请求资源，提供了可视化的监控界面



## 2、数据监控代码实现

- 创建maven的module，pom.xml如下

```xml
	<artifactId>hystrix</artifactId>

	<dependencies>
        <dependency>
            <groupId>org.springframework.cloud</groupId>
            <artifactId>spring-cloud-starter-netflix-eureka-client</artifactId>
            <version>2.0.2.RELEASE</version>
        </dependency>

        <dependency>
            <groupId>org.springframework.cloud</groupId>
            <artifactId>spring-cloud-openfeign</artifactId>
            <version>2.0.2.RELEASE</version>
        </dependency>

        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-actuator</artifactId>
            <version>2.0.7.RELEASE</version>
        </dependency>

        <dependency>
            <groupId>org.springframework.cloud</groupId>
            <artifactId>spring-cloud-starter-netflix-hystrix</artifactId>
            <version>2.0.2.RELEASE</version>
        </dependency>

        <dependency>
            <groupId>org.springframework.cloud</groupId>
            <artifactId>spring-cloud-starter-netflix-hystrix-dashboard</artifactId>
            <version>2.0.2.RELEASE</version>
        </dependency>
    </dependencies>
```

- 创建配置文件application.yml

```yml
server:
  port: 8060
spring:
  application:
    name: hystrix
eureka:
  client:
    service-url:
      defaultZone: http://localhost:8761/eureka/
  instance:
    prefer-ip-address: true
feign:
  hystrix:
    enabled: true
management:
  endpoints:
    web:
      exposure:
        include: 'hystrix.stream'
```

- 创建启动类

```java
@SpringBootApplication
@EnableFeignClients
@EnableCircuitBreaker
@EnableHystrixDashboard
public class HystrixApplication {
    public static void main(String[] args) {
        SpringApplication.run(HystrixApplication.class, args);
    }
}
```

>注解说明：

`@EnableCircuitBreaker`：声明使用数据监控

`@EnableHystrixDashboard`：声明使用可视化数据监控

- 在拷贝了Student实体类以及FeignProviderClient之后，写Handler

```java
@RestController
@RequestMapping("/hystrix")
public class HystrixHandler {

    @Resource
    private FeignProviderClient feignProviderClient;

    @GetMapping("/findAll")
    public Collection<Student> findAll() {
        return feignProviderClient.findAll();
    }
}
```

>使用：
>
>- 启动成功之后，访问http://localhost:8060/actuator/hystrix.stream可看到数据
>- 通过访问http://localhost:8060/hystrix，并监控http://localhost:8060/actuator/hystrix.stream可看到图形化界面





# 五、Spring Cloud 配置中心

> 配置集中管理，避免一个微服务修改配置，其他消费者都要改



## 1、本地文件配置

### 声明本地配置中心

- 创建maven的模块，其pom.xml如下：

```xml
	<artifactId>nativeconfigserver</artifactId>

	<dependencies>
        <dependency>
            <groupId>org.springframework.cloud</groupId>
            <artifactId>spring-cloud-config-server</artifactId>
            <version>2.0.2.RELEASE</version>
        </dependency>
    </dependencies>
```

- 创建application.yml

```yml
server:
  port: 8762
spring:
  application:
    name: nativeconfigserver
  profiles:
    active: native
  cloud:
    config:
      server:
        native:
          search-locations: classpath:/shared
```

>属性说明

`profiles.active`：配置文件的配置方式，native表示本地

`cloud.config.server.native.search-locations`：本地配置文件存放的路径

- resources路径下创建shared文件夹，并在此目录下创建configclient-dev.yml

```yml
server:
  port: 8070
foo: foo version 1
```

- 创建启动类

```java
@SpringBootApplication
@EnableConfigServer
public class NativeConfigServerApplication {

    public static void main(String[] args) {
        SpringApplication.run(NativeConfigServerApplication.class, args);
    }
}
```

>注解说明

`@EnableConfigServer`：声明配置中心



### 创建客户端读取配置

- 创建 Maven 模块，pom.xml如下：

```xml
    <artifactId>nativeconfigclient</artifactId>

	<dependencies>
        <dependency>
            <groupId>org.springframework.cloud</groupId>
            <artifactId>spring-cloud-starter-config</artifactId>
            <version>2.0.2.RELEASE</version>
        </dependency>
    </dependencies>
```

- 创建 bootstrap.yml，配置读取本地配置中心的相关信息

```yml
spring:
  application:
    name: configclient
  profiles:
    active: dev
  cloud:
    config:
      uri: http://localhost:8762
      fail-fast: true
```

>属性说明

`cloud.config.uri`：本地Config Server的访问路径

`cloud.config.fail-fast`：设置客户端优先判断Config Server获取是否正常

通过`spring.application.name`和`spring.profiles.active`拼接目标配置文件名 -> `configclient-dev.yml`

- 创建启动类

```java
@SpringBootApplication
public class NativeConfigClientApplication {
    
    public static void main(String[] args) {
        SpringApplication.run(NativeConfigClientApplication.class, args);
    }
}
```

- handler

```java
@RestController
@RequestMapping("/native")
public class NativeConfigHandler {

    @Value("${server.port}")
    private String port;

    @Value("${foo}")
    private String foo;

    @GetMapping("/index")
    public String index() {
        return this.port + "-" + this.foo;
    }
}
```

>访问`http://localhost:8070/native/index`，输出：`8070-foo version 1`



## 2、远程配置

### 声明远程配置中心

- 创建配置文件，上传至Github

```yml
server:
  port: 8070
eureka:
  client:
    service-url:
      defaultZone: http://localhost:8761:eureka/
spring:
  application:
    name: configclient
```

- 创建Confgi Server，新建 Maven 工程，pom.xml如下：

```xml
	<artifactId>configserver</artifactId>

    <dependencies>
        <dependency>
            <groupId>org.springframework.cloud</groupId>
            <artifactId>spring-cloud-config-server</artifactId>
            <version>2.0.2.RELEASE</version>
        </dependency>
    </dependencies>
```

- 创建配置文件 application.yml

```yml
server:
  port: 8888
spring:
  application:
    name: configserver
  cloud:
    config:
      server:
        git:
          uri: https://github.com/tjumcw/springcloud.git
          search-paths: config
          username: tjumcw
          password: Miao970508
      label: master
eureka:
  client:
    service-url:
      defaultZone: http://localhost:8761/eureka/
```

- 创建启动类

```java
@SpringBootApplication
@EnableConfigServer
public class ConfigServerApplication {

    public static void main(String[] args) {
        SpringApplication.run(ConfigServerApplication.class, args);
    }
}
```



### 创建客户端读取配置

- 创建 Maven 工程，pom.xml如下：

```xml
	<artifactId>configclient</artifactId>

    <dependencies>
        <dependency>
            <groupId>org.springframework.cloud</groupId>
            <artifactId>spring-cloud-starter-config</artifactId>
            <version>2.0.2.RELEASE</version>
        </dependency>

        <dependency>
            <groupId>org.springframework.cloud</groupId>
            <artifactId>spring-cloud-starter-netflix-eureka-client</artifactId>
            <version>2.0.2.RELEASE</version>
        </dependency>
    </dependencies>
```

- 创建 bootstrap.yml

```yml
spring:
  cloud:
    config:
      name: configclient
      label: master # 分支名
      discovery:
        enabled: true # 开启Config服务发现支持
        service-id: configserver
eureka:
  client:
    service-url:
      defaultZone: http://localhost:8761/eureka/
```

>属性解释：

`config.name`：当前服务注册在 Eureka Server上的名称，与远程仓库配置文件名对应

`discovery.service-id`：配置中心在 Eureka Server 上注册的名称（本地没注册，通过url找的）

- 创建启动类

```java
@SpringBootApplication
public class ConfigClientApplication {

    public static void main(String[] args) {
        SpringApplication.run(ConfigClientApplication.class, args);
    }
}
```

- Handler

```java
@RestController
@RequestMapping("/hello")
public class HelloHandler {

    @Value("${server.port}")
    private String port;

    @GetMapping("/index")
    public String index() {
        return this.port;
    }
}
```

- 之后就可以通过访问该接口获取port信息

