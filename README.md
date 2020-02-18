[![Build Status](https://www.travis-ci.com/zhengrenjie/catty.svg?branch=master)](https://www.travis-ci.com/zhengrenjie/catty)

# Catty
Whole new RPC framework!

# Features
* High performance.
* Async & reactor oriented.
* Micro-kernel & Easy to customize
* ProtoBuf supported.

# Usage
See example package or test package.

**Maven:**
```xml
<dependency>
    <groupId>pink.catty</groupId>
    <artifactId>catty-all</artifactId>
    <version>0.1.5</version>
</dependency>
```

### Sync:
#### Server:
```java
public class Server {

  public static void main(String[] args) {
    ServerConfig serverConfig = ServerConfig.builder()
        .port(20550)
        .build();

    Exporter exporter = new Exporter(serverConfig);
    exporter.registerService(IService.class, new IServiceImpl());
    exporter.export();
  }
}
```
#### Client:
```java
public class Client {

  public static void main(String[] args) {
    ClientConfig clientConfig = ClientConfig.builder()
        .addAddress("127.0.0.1:20550")
        .build();

    Reference<IService> reference = new Reference<>();
    reference.setClientConfig(clientConfig);
    reference.setInterfaceClass(IService.class);

    IService service = reference.refer();
    System.out.println(service.say0());
    System.out.println(service.say1("catty"));
  }
}
```
### Async:
#### Server:
```java
public class Server {
  
  public static void main(String[] args) {
    ServerConfig serverConfig = ServerConfig.builder()
        .port(20550)
        .build();

    Exporter exporter = new Exporter(serverConfig);
    exporter.registerService(IService.class, new IServiceImpl());
    exporter.export();
  }
}
```
#### Client:
```java
public class Client {

  public static void main(String[] args) {
    ClientConfig clientConfig = ClientConfig.builder()
        .addAddress("127.0.0.1:20550")
        .build();

    Reference<IService> reference = new Reference<>();
    reference.setClientConfig(clientConfig);
    reference.setInterfaceClass(IService.class);

    IService service = reference.refer();
    CompletableFuture<String> future = service.asyncSay("catty");
    future.whenComplete((value, t) -> System.out.println(value));
  }
}
```

# Project Status
Catty has released a few versions, which means you could use Catty in your own project!

But as the version is senior than 1.0.0, which means catty is not stable and has not adequately 
tested yet, I temporarily do not recommend you to use it in any large distribution system. But I do 
recommend you to use it in a smaller system or point-to-point system, in which cases Catty would 
be more easier to use and control.


# *Welcome to join me!*
There are lots of things need todo:
* doc
* benchmark
* test
* more useful extensions
* log & annotation
* code review & refactor
