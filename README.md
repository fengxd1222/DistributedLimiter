# DistributedLimiter

```
DistributedLimiter
├─ LimiterClient
│  ├─ pom.xml
│  ├─ src
│  │  ├─ main
│  │  │  ├─ java
│  │  │  │  └─ com
│  │  │  │     └─ example
│  │  │  │        └─ limiter
│  │  │  │           ├─ controller
│  │  │  │           │  └─ TestController.java
│  │  │  │           ├─ limiter
│  │  │  │           │  ├─ DistributedQPSLimiter.java
│  │  │  │           │  ├─ LimiterConfig.java
│  │  │  │           │  ├─ LimiterInfo.java
│  │  │  │           │  └─ LimiterScanner.java
│  │  │  │           ├─ LimiterClientApplication.java
│  │  │  │           └─ netty
│  │  │  │              ├─ Client.java
│  │  │  │              ├─ ClientChannel.java
│  │  │  │              ├─ handler
│  │  │  │              │  ├─ AbstractKryoSerializerEncoder.java
│  │  │  │              │  ├─ ClientLimiterEncoderAbstract.java
│  │  │  │              │  ├─ ClientLimiterRequestExecuteHandler.java
│  │  │  │              │  ├─ HeartbeatHandler.java
│  │  │  │              │  └─ KryoSerializerDecoder.java
│  │  │  │              ├─ LimiterNettyClient.java
│  │  │  │              ├─ remote
│  │  │  │              │  ├─ ClientLimiterRequest.java
│  │  │  │              │  ├─ ClientLimiterResponse.java
│  │  │  │              │  └─ LimiterDefinition.java
│  │  │  │              ├─ serializer
│  │  │  │              │  ├─ AbstractKryoPoolSerializerFactory.java
│  │  │  │              │  └─ AbstractKryoSerializerFactory.java
│  │  │  │              └─ util
│  │  │  │                 ├─ ClientConstant.java
│  │  │  │                 ├─ EventLoopGroupBuilder.java
│  │  │  │                 └─ JwtUtils.java
│  │  │  └─ resources
│  │  │     ├─ application.properties
│  │  │     ├─ static
│  │  │     └─ templates
│  │  └─ test
│  │     └─ java
│  │        └─ com
│  │           └─ example
│  │              └─ limiter
│  │                 ├─ LimiterClientApplicationTests.java
│  │                 └─ Test.http
├─ LimiterServer
│  ├─ pom.xml
│  ├─ src
│  │  ├─ main
│  │  │  ├─ java
│  │  │  │  └─ com
│  │  │  │     └─ example
│  │  │  │        └─ limiter
│  │  │  │           ├─ limiter
│  │  │  │           │  ├─ AbstractLimiter.java
│  │  │  │           │  ├─ config
│  │  │  │           │  │  ├─ LimiterMethodConfig.java
│  │  │  │           │  │  └─ LimiterRemoteConfig.java
│  │  │  │           │  ├─ counter
│  │  │  │           │  │  ├─ AtomicLongCounter.java
│  │  │  │           │  │  ├─ Counter.java
│  │  │  │           │  │  ├─ CounterFactory.java
│  │  │  │           │  │  ├─ DefaultCounter.java
│  │  │  │           │  │  └─ LongAdderCounter.java
│  │  │  │           │  ├─ DistributedQPSLimiter.java
│  │  │  │           │  ├─ LimiterConfig.java
│  │  │  │           │  ├─ LimiterInfo.java
│  │  │  │           │  ├─ QPSLimiter.java
│  │  │  │           │  └─ strategy
│  │  │  │           │     ├─ ChannelReadHandlerStrategy.java
│  │  │  │           │     ├─ ChannelReadHolder.java
│  │  │  │           │     ├─ LimiterChannelReadHandler.java
│  │  │  │           │     └─ LimiterConfigChannelReadHandler.java
│  │  │  │           ├─ LimiterServerApplication.java
│  │  │  │           └─ netty
│  │  │  │              ├─ authorization
│  │  │  │              │  ├─ AuthorizationCache.java
│  │  │  │              │  └─ LocalAuthorizationCache.java
│  │  │  │              ├─ handler
│  │  │  │              │  ├─ AuthorizationHandler.java
│  │  │  │              │  ├─ ClientLimiterEncoder.java
│  │  │  │              │  ├─ ClientLimiterResponseEncoder.java
│  │  │  │              │  ├─ KryoSerializerDecoder.java
│  │  │  │              │  ├─ KryoSerializerEncoder.java
│  │  │  │              │  └─ LimiterHandler.java
│  │  │  │              ├─ LimiterServer.java
│  │  │  │              ├─ remote
│  │  │  │              │  ├─ ClientLimiterRequest.java
│  │  │  │              │  ├─ ClientLimiterResponse.java
│  │  │  │              │  └─ LimiterDefinition.java
│  │  │  │              ├─ serializer
│  │  │  │              │  ├─ KryoPoolSerializerFactory.java
│  │  │  │              │  └─ KryoSerializerFactory.java
│  │  │  │              └─ util
│  │  │  │                 ├─ ClientConstant.java
│  │  │  │                 ├─ EventLoopGroupBuilder.java
│  │  │  │                 └─ JwtUtils.java
│  │  │  └─ resources
│  │  │     ├─ application.properties
│  │  │     ├─ log4j2.xml
│  │  │     ├─ static
│  │  │     └─ templates
│  │  └─ test
│  │     └─ java
│  │        └─ com
│  │           └─ example
│  │              └─ limiter
│  │                 └─ LimiterServerApplicationTests.java
└─ README.md

```