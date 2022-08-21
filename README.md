## What
This is a simple tester application that loads a Redis cluster with requests.  
It can be configured with: 
- number of threads performing Redis requests
- request random delay (min/max), to create a more realistic load
- retries count and delay in case of error.

The tester can be run as a standalone SpringBoot application, or deployed into K8S as a POD.  
The project contains an example for such K8S deployment (with Deployment and Configmap configuration).  

## Why 
This tester tries to reproduce the problem and help in finding the root cause for the exception mentioned in BouncyCastle issue:  
https://github.com/bcgit/bc-java/issues/1186 

```java
org.springframework.data.redis.RedisSystemException: Unknown redis exception; nested exception is io.netty.handler.codec.DecoderException: javax.net.ssl.SSLException: org.bouncycastle.tls.TlsFatalAlert: bad_record_mac(20)
        at org.springframework.data.redis.FallbackExceptionTranslationStrategy.getFallback(FallbackExceptionTranslationStrategy.java:53) ~[spring-data-redis-2.6.4.jar:2.6.4]
        at org.springframework.data.redis.FallbackExceptionTranslationStrategy.translate(FallbackExceptionTranslationStrategy.java:43) ~[spring-data-redis-2.6.4.jar:2.6.4]
        at org.springframework.data.redis.connection.lettuce.LettuceConnection.convertLettuceAccessException(LettuceConnection.java:272) ~[spring-data-redis-2.6.4.jar:2.6.4]
        at org.springframework.data.redis.connection.lettuce.LettuceConnection.await(LettuceConnection.java:1063) ~[spring-data-redis-2.6.4.jar:2.6.4]
        at org.springframework.data.redis.connection.lettuce.LettuceConnection.lambda$doInvoke$4(LettuceConnection.java:920) ~[spring-data-redis-2.6.4.jar:2.6.4]
        at org.springframework.data.redis.connection.lettuce.LettuceInvoker$Synchronizer.invoke(LettuceInvoker.java:665) ~[spring-data-redis-2.6.4.jar:2.6.4]
        at org.springframework.data.redis.connection.lettuce.LettuceInvoker.just(LettuceInvoker.java:125) ~[spring-data-redis-2.6.4.jar:2.6.4]
        at org.springframework.data.redis.connection.lettuce.LettuceHashCommands.hIncrBy(LettuceHashCommands.java:193) ~[spring-data-redis-2.6.4.jar:2.6.4]
        at org.springframework.data.redis.connection.DefaultedRedisConnection.hIncrBy(DefaultedRedisConnection.java:1380) ~[spring-data-redis-2.6.4.jar:2.6.4]
        at org.springframework.data.redis.core.DefaultHashOperations.lambda$increment$2(DefaultHashOperations.java:81) ~[spring-data-redis-2.6.4.jar:2.6.4]
        at org.springframework.data.redis.core.RedisTemplate.execute(RedisTemplate.java:223) ~[spring-data-redis-2.6.4.jar:2.6.4]
        at org.springframework.data.redis.core.RedisTemplate.execute(RedisTemplate.java:190) ~[spring-data-redis-2.6.4.jar:2.6.4]
        at org.springframework.data.redis.core.AbstractOperations.execute(AbstractOperations.java:97) ~[spring-data-redis-2.6.4.jar:2.6.4]
        at org.springframework.data.redis.core.DefaultHashOperations.increment(DefaultHashOperations.java:81) ~[spring-data-redis-2.6.4.jar:2.6.4]
        at org.redistest.RedistestApplication.lambda$main$0(RedistestApplication.java:76) ~[classes/:na]
        at java.base/java.util.concurrent.FutureTask.run(Unknown Source) ~[na:na]
        at java.base/java.util.concurrent.ThreadPoolExecutor.runWorker(Unknown Source) ~[na:na]
        at java.base/java.util.concurrent.ThreadPoolExecutor$Worker.run(Unknown Source) ~[na:na]
        at java.base/java.lang.Thread.run(Unknown Source) ~[na:na]
Caused by: io.netty.handler.codec.DecoderException: javax.net.ssl.SSLException: org.bouncycastle.tls.TlsFatalAlert: bad_record_mac(20)
        at io.netty.handler.codec.ByteToMessageDecoder.callDecode(ByteToMessageDecoder.java:480) ~[netty-codec-4.1.77.Final.jar:4.1.77.Final]
        at io.netty.handler.codec.ByteToMessageDecoder.channelRead(ByteToMessageDecoder.java:279) ~[netty-codec-4.1.77.Final.jar:4.1.77.Final]
        at io.netty.channel.AbstractChannelHandlerContext.invokeChannelRead(AbstractChannelHandlerContext.java:379) ~[netty-transport-4.1.77.Final.jar:4.1.77.Final]
        at io.netty.channel.AbstractChannelHandlerContext.invokeChannelRead(AbstractChannelHandlerContext.java:365) ~[netty-transport-4.1.77.Final.jar:4.1.77.Final]
        at io.netty.channel.AbstractChannelHandlerContext.fireChannelRead(AbstractChannelHandlerContext.java:357) ~[netty-transport-4.1.77.Final.jar:4.1.77.Final]
        at io.netty.channel.DefaultChannelPipeline$HeadContext.channelRead(DefaultChannelPipeline.java:1410) ~[netty-transport-4.1.77.Final.jar:4.1.77.Final]
        at io.netty.channel.AbstractChannelHandlerContext.invokeChannelRead(AbstractChannelHandlerContext.java:379) ~[netty-transport-4.1.77.Final.jar:4.1.77.Final]
        at io.netty.channel.AbstractChannelHandlerContext.invokeChannelRead(AbstractChannelHandlerContext.java:365) ~[netty-transport-4.1.77.Final.jar:4.1.77.Final]
        at io.netty.channel.DefaultChannelPipeline.fireChannelRead(DefaultChannelPipeline.java:919) ~[netty-transport-4.1.77.Final.jar:4.1.77.Final]
        at io.netty.channel.nio.AbstractNioByteChannel$NioByteUnsafe.read(AbstractNioByteChannel.java:166) ~[netty-transport-4.1.77.Final.jar:4.1.77.Final]
        at io.netty.channel.nio.NioEventLoop.processSelectedKey(NioEventLoop.java:722) ~[netty-transport-4.1.77.Final.jar:4.1.77.Final]
        at io.netty.channel.nio.NioEventLoop.processSelectedKeysOptimized(NioEventLoop.java:658) ~[netty-transport-4.1.77.Final.jar:4.1.77.Final]
        at io.netty.channel.nio.NioEventLoop.processSelectedKeys(NioEventLoop.java:584) ~[netty-transport-4.1.77.Final.jar:4.1.77.Final]
        at io.netty.channel.nio.NioEventLoop.run(NioEventLoop.java:496) ~[netty-transport-4.1.77.Final.jar:4.1.77.Final]
        at io.netty.util.concurrent.SingleThreadEventExecutor$4.run(SingleThreadEventExecutor.java:995) ~[netty-common-4.1.77.Final.jar:4.1.77.Final]
        at io.netty.util.internal.ThreadExecutorMap$2.run(ThreadExecutorMap.java:74) ~[netty-common-4.1.77.Final.jar:4.1.77.Final]
        at io.netty.util.concurrent.FastThreadLocalRunnable.run(FastThreadLocalRunnable.java:30) ~[netty-common-4.1.77.Final.jar:4.1.77.Final]
        ... 1 common frames omitted
Caused by: javax.net.ssl.SSLException: org.bouncycastle.tls.TlsFatalAlert: bad_record_mac(20)
        at org.bouncycastle.jsse.provider.ProvSSLEngine.unwrap(ProvSSLEngine.java:508) ~[bctls-fips-1.0.13.jar:1.0.13]
        at java.base/javax.net.ssl.SSLEngine.unwrap(Unknown Source) ~[na:na]
        at io.netty.handler.ssl.SslHandler$SslEngineType$3.unwrap(SslHandler.java:295) ~[netty-handler-4.1.77.Final.jar:4.1.77.Final]
        at io.netty.handler.ssl.SslHandler.unwrap(SslHandler.java:1342) ~[netty-handler-4.1.77.Final.jar:4.1.77.Final]
        at io.netty.handler.ssl.SslHandler.decodeJdkCompatible(SslHandler.java:1235) ~[netty-handler-4.1.77.Final.jar:4.1.77.Final]
        at io.netty.handler.ssl.SslHandler.decode(SslHandler.java:1284) ~[netty-handler-4.1.77.Final.jar:4.1.77.Final]
        at io.netty.handler.codec.ByteToMessageDecoder.decodeRemovalReentryProtection(ByteToMessageDecoder.java:510) ~[netty-codec-4.1.77.Final.jar:4.1.77.Final]
        at io.netty.handler.codec.ByteToMessageDecoder.callDecode(ByteToMessageDecoder.java:449) ~[netty-codec-4.1.77.Final.jar:4.1.77.Final]
        ... 17 common frames omitted
Caused by: org.bouncycastle.tls.TlsFatalAlert: bad_record_mac(20)
        at org.bouncycastle.tls.crypto.impl.TlsAEADCipher.decodeCiphertext(TlsAEADCipher.java:293) ~[bctls-fips-1.0.13.jar:1.0.13]
        at org.bouncycastle.tls.RecordStream.decodeAndVerify(RecordStream.java:253) ~[bctls-fips-1.0.13.jar:1.0.13]
        at org.bouncycastle.tls.RecordStream.readFullRecord(RecordStream.java:204) ~[bctls-fips-1.0.13.jar:1.0.13]
        at org.bouncycastle.tls.TlsProtocol.safeReadFullRecord(TlsProtocol.java:903) ~[bctls-fips-1.0.13.jar:1.0.13]
        at org.bouncycastle.tls.TlsProtocol.offerInput(TlsProtocol.java:1308) ~[bctls-fips-1.0.13.jar:1.0.13]
        at org.bouncycastle.jsse.provider.ProvSSLEngine.unwrap(ProvSSLEngine.java:464) ~[bctls-fips-1.0.13.jar:1.0.13]
        ... 24 common frames omitted
Caused by: java.lang.IllegalStateException:
        at org.bouncycastle.tls.crypto.impl.jcajce.Exceptions.illegalStateException(Exceptions.java:10) ~[bctls-fips-1.0.13.jar:1.0.13]
        at org.bouncycastle.tls.crypto.impl.jcajce.JceAEADCipherImpl.doFinal(JceAEADCipherImpl.java:139) ~[bctls-fips-1.0.13.jar:1.0.13]
        at org.bouncycastle.tls.crypto.impl.TlsAEADCipher.decodeCiphertext(TlsAEADCipher.java:288) ~[bctls-fips-1.0.13.jar:1.0.13]
        ... 29 common frames omitted
Caused by: javax.crypto.AEADBadTagException: Error finalising cipher data: mac check in GCM failed
        at java.base/jdk.internal.reflect.NativeConstructorAccessorImpl.newInstance0(Native Method) ~[na:na]
        at java.base/jdk.internal.reflect.NativeConstructorAccessorImpl.newInstance(Unknown Source) ~[na:na]
        at java.base/jdk.internal.reflect.DelegatingConstructorAccessorImpl.newInstance(Unknown Source) ~[na:na]
        at java.base/java.lang.reflect.Constructor.newInstanceWithCaller(Unknown Source) ~[na:na]
        at java.base/java.lang.reflect.Constructor.newInstance(Unknown Source) ~[na:na]
        at org.bouncycastle.jcajce.provider.ClassUtil.throwBadTagException(Unknown Source) ~[bc-fips-1.0.2.3.jar:1.0.2.3]
        at org.bouncycastle.jcajce.provider.BaseCipher.engineDoFinal(Unknown Source) ~[bc-fips-1.0.2.3.jar:1.0.2.3]
        at org.bouncycastle.jcajce.provider.BaseCipher.engineDoFinal(Unknown Source) ~[bc-fips-1.0.2.3.jar:1.0.2.3]
        at java.base/javax.crypto.Cipher.doFinal(Unknown Source) ~[na:na]
        at org.bouncycastle.tls.crypto.impl.jcajce.JceAEADCipherImpl.doFinal(JceAEADCipherImpl.java:135) ~[bctls-fips-1.0.13.jar:1.0.13]
        ... 30 common frames omitted

```

## Conclusions
After running the tester, it is seen that the exception occurs after 1-2 hours, when running with 10 threads and a request rate of ~700 requests/sec.  
- Errors only occur when running with BouncyCastle fips version. When building the application with no BC, it runs without any errors.
- When running with BouncyCastle:
  - Sporadic errors occur once every few hours. Usually, they occur on all thread at once and there is connection drop (and auto re-connect)
  - Errors are not related to connection pool. I tried with and without connection pool.
  - Errors occur even when tester is running with a single thread.
- It is possible to recover from the errors and have no effect on the application logic if we perform retries. However, the retry must have a short delay (I tested with 200 ms) between retries. If we set no delay, then the retry does not help.

## Running Locally
To run locally, make sure to fill the following parameters in `application.yml` :  
1. Redis host and port 
2. `ssl: true` , and make sure the Redis cluster supports TLS. If you have problems with certificate, you can bypass verification by setting `ssl-verify : false`.

## K8S Deployment
The project contains a `redistest-deployment.yml` file.  
Make sure to set the application parameters mentioned above in the Configmap, set the Redis connection parameters and the docker image. 
Then, to deploy it on your cluster, run the command `kubectl apply -f redistest-deployment.yml` .  

To chnge the code and build a new docker image, you should run the `jib` task located in `build.gradle`.  
The task must use a base image with a JDK/JRE. Specifically, I used a hardened image named 'zulu' (`docker pull oridool/zulu`) with a JRE that was configured with BouncyCastle as the security provider (overriding cacerts, java.policy, java.security files).     
Then, set the "to" repository parameters. 
Example (assuming you use AWS ECR for storing docker images): 
```shell
gradlew jib -PdockerPublishRegistryUrl=xxx.dkr.ecr.eu-west-1.amazonaws.com -PawsProfile=<AWS_PROFILE>
```


 