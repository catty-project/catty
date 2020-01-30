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
        .address("127.0.0.1:20550")
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
        .address("127.0.0.1:20550")
        .build();

    Reference<IService> reference = new Reference<>();
    reference.setClientConfig(clientConfig);
    reference.setInterfaceClass(IService.class);

    IService service = reference.refer();
    CompletableFuture<String> future = service.asyncSay("catty");
    future.whenComplete((value, t) -> {
      System.out.println(value);
    });
  }
}
```

# Project Status
The project is developing (means unstable), and has not a release version yet, but it will come soon!

#*Welcome to join me!*