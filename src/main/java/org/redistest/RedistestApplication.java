package org.redistest;

import io.netty.handler.ssl.SslClosedEngineException;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.bouncycastle.jcajce.provider.BouncyCastleFipsProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.data.redis.RedisConnectionFailureException;
import org.springframework.data.redis.RedisSystemException;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.RedisTemplate;

import javax.annotation.PostConstruct;
import java.security.Provider;
import java.security.Security;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

@Slf4j
@Data
@SpringBootApplication
public class RedistestApplication {

	@PostConstruct
	private void Init() {
		if (isFips) {
			BouncyCastleFipsProvider bc = new BouncyCastleFipsProvider();
			int addProviderIndex = Security.addProvider(bc);
			log.warn("added BC provider at position {}", addProviderIndex);
		}
		printSecurityProvidersInfo();
	}

	@Value("${application.num-of-threads:1}")
	private int numOfThreads;
	@Value("${application.wait-min-millis:1000}")
	private int waitMin;
	@Value("${application.wait-max-millis:10000}")
	private int waitMax;
	@Value("${application.log-period-millis:0}")
	private int logPeriodMillis;
	@Value("${application.sleep-after-error-period-millis:0}")
	private int sleepAfterErrorPeriodMillis;
	@Value("${application.max-retries-on-failure:3}")
	private int maxRetriesOnFailure;
	@Value("${application.max-retry-delay-millis:500}")
	private int retriesDelayMillis;
	@Value("${application.is-fips:true}")
	private boolean isFips;

	public static void main(String[] args) {
		Random random = new Random();
		ConfigurableApplicationContext run = SpringApplication.run(RedistestApplication.class, args);
		RedistestApplication application = run.getBean(RedistestApplication.class);
		log.warn("$$$$$$$$$  starting test application $$$$$$$$$ - fips mode = {}", application.isFips());
		log.warn("$$$$$$$$$  num-of-threads:{} ", application.getNumOfThreads());
		log.warn("$$$$$$$$$  wait-min-millis:{} ", application.getWaitMin());
		log.warn("$$$$$$$$$  wait-max-millis:{} ", application.getWaitMax());
		log.warn("$$$$$$$$$  max-retries-on-failure:{} ", application.getMaxRetriesOnFailure());
		log.warn("$$$$$$$$$  retry-delay-millis:{} ", application.getRetriesDelayMillis());
		RedisTemplate<String,String> redisTemplate = (RedisTemplate<String,String>)run.getBean(RedisTemplate.class);

		HashOperations<String, String, Long> ops = redisTemplate.opsForHash();
		String hashKeyPrefix = "a:testclient";
		String hashKeyFieldName = "c";

		ExecutorService executor = Executors.newFixedThreadPool(application.getNumOfThreads());
		for (int t=0; t < application.getNumOfThreads(); ++t) {
			executor.submit(() -> {
				String threadName = Thread.currentThread().getName();
				String hashKey = hashKeyPrefix + ":" + threadName + ":" + LocalDateTime.now().toString();
				log.warn("starting redis client for thread {} with key {}", threadName, hashKey);
				int curSleepTime = 0;
				Instant totalSleepTime = Instant.now();
				long curErrorCount = 0;
				AtomicInteger curRetryErrorCount = new AtomicInteger();
				int i = 0;
				while (true) {
					try {
						Long counterValue = increment(ops, hashKey, hashKeyFieldName, application.getMaxRetriesOnFailure(), application.getRetriesDelayMillis(), curRetryErrorCount);
						curSleepTime = application.getWaitMax() > 0 ? random.nextInt(application.getWaitMax() - application.getWaitMin()) + application.getWaitMin() : 0;
						if ((application.getLogPeriodMillis() > 0) && (totalSleepTime.isBefore(Instant.now().minusMillis(application.getLogPeriodMillis())))) {
							totalSleepTime = Instant.now();
							log.info("{} {} : {} -> {} (r: {} , e: {})", i++, hashKey, hashKeyFieldName, counterValue, curRetryErrorCount.intValue(), curErrorCount);
						}
						if (curSleepTime > 0){
							try {
								Thread.sleep(curSleepTime);
							} catch (InterruptedException e) {
								log.error("InterruptedException on sleep", e);
							}
						}
					} catch (Exception e) {
						++curErrorCount;
						log.warn("Exception while performing redis command, curErrorCount={}", curErrorCount, e);
						if (application.getSleepAfterErrorPeriodMillis() > 0) {
							Thread.sleep(application.getSleepAfterErrorPeriodMillis());
						}
					}
				}
			});
		}
	}

	private void printSecurityProvidersInfo() {
//		log.warn("****  BouncyCastle approved mode {}  ****", CryptoServicesRegistrar.isInApprovedOnlyMode());
		StringBuilder sb = new StringBuilder();
		Provider[] providers = Security.getProviders();
		for (int i=0; i< providers.length; ++i) {
			sb.append(String.format("PROVIDER[%d] : %s --> %s\n", i+1 , providers[i].getName(), providers[i].getInfo()));
		}
		log.warn(sb.toString());

	}

	private static Long increment(HashOperations<String, String, Long> ops, String redisKey, String hashKey, int maxRetriesOnFailure, int retriesDelayMillis, AtomicInteger curRetryErrorCount) {
		int retryCount = 0;
		Long res = null;

		while (retryCount <= maxRetriesOnFailure && res == null) {
			try {
				res = ops.increment(redisKey, hashKey, 1L);
			} catch (Exception e) {
				if ((e instanceof RedisConnectionFailureException || e instanceof SslClosedEngineException || e instanceof RedisSystemException) && (retryCount < maxRetriesOnFailure)) {
					retryCount++;
					curRetryErrorCount.getAndIncrement();
					try {
						Thread.sleep(retriesDelayMillis);
					} catch (InterruptedException ex) {
						throw new RuntimeException(ex);
					}
					log.warn("Redis client - performing retry {} after caught {}: {}", retryCount, e.getClass(), e.getMessage());
				} else {
					throw e;
				}
			}
		}

		return res;
	}

}

