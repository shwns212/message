package com.jun.message.listener;

import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.regex.Pattern;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.core.type.filter.RegexPatternTypeFilter;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jun.message.annotation.MessageController;
import com.jun.message.annotation.MessageMapping;
import com.jun.message.message.Message;

public class MessageListener {
	private static List<Object> kafkaControllers = new ArrayList<>();
	private static Integer consumerCount = 0;
	private final ObjectMapper objectMapper = new ObjectMapper();
	public MessageListener() {}
	
	public MessageListener(String basePackage) {
		listen(basePackage);
	}
	
	public void listen(String basePackage) {
		addKafkaControllerObject(basePackage);
		handle();
	}

	/**
	 * KafkaController 어노테이션이 붙어있는 클래스를 찾아서 객체를 생성후 list에 추가한다.
	 * @param basePackage
	 */
	private void addKafkaControllerObject(String basePackage) {
		ClassPathScanningCandidateComponentProvider provider = new ClassPathScanningCandidateComponentProvider(false);
		provider.addIncludeFilter(new RegexPatternTypeFilter(Pattern.compile(".*")));
		Set<BeanDefinition> beanDefinitions = provider.findCandidateComponents(basePackage);
		for(BeanDefinition beanDefinition : beanDefinitions) {
			Class<?> clazz = null;
			try {
				clazz = Class.forName(beanDefinition.getBeanClassName());
			} catch (ClassNotFoundException e) {
				e.printStackTrace();
			}
			Annotation[] annotations = clazz.getAnnotations();
			for(Annotation annotation : annotations) {
				if(annotation.annotationType().equals(MessageController.class)) {
					try {
						kafkaControllers.add(clazz.newInstance());
					} catch (InstantiationException | IllegalAccessException e) {
						e.printStackTrace();
					}
				}
			}
		}
	}
	
	private void handle() {
		// 클래스 레벨에 KafkaController 어노테이션이 붙어있는 객체들
		for(Object controller : MessageListener.kafkaControllers) {
			Annotation[] cAnnotations = controller.getClass().getAnnotations();
			for(Annotation cAnnotation : cAnnotations) {
				// 클래스 레벨 어노테이션이 KafkaController일 경우 새로운 쓰레드에서 메세지를 받는다.
				if(cAnnotation.annotationType().equals(MessageController.class)) {
					consumerCount++;
					execute(controller, cAnnotation);
				}
			}
		}
	}
	
	/**
	 * 메시지를 받아서 특정 핸들러 메서드에 메시지를 넘겨 실행한다.
	 * @param controller
	 * @param record
	 */
	public void receiveMessageAndInvokeHandleMethod(Object controller, ConsumerRecord<String, String> record) {
		Message message = null;
		try {
			message = objectMapper.readValue(record.value(), Message.class);
		} catch (JsonProcessingException e1) {
			e1.printStackTrace();
		}
		
		Method[] methods = controller.getClass().getDeclaredMethods();
		for(Method method : methods) {
			Annotation[] annotations = method.getDeclaredAnnotations();
			for(Annotation annotation : annotations) {
				if(annotation.annotationType().equals(MessageMapping.class)) {
					String value = ((MessageMapping)annotation).value();
					if(message.getType().equals(value)) {
						try {
							method.invoke(controller, message);
						} catch (IllegalAccessException | IllegalArgumentException
								| InvocationTargetException e) {
							e.printStackTrace();
						}
					}
				}
			}
		}
	}
	
	private void execute(Object controller, Annotation cAnnotation) {
		new Thread(() ->  {
			
			String topic = ((MessageController)cAnnotation).topic();
			
			// 카프카 기본 설정
			Properties properties = kafkaConfigProperties(cAnnotation);
			
			try(KafkaConsumer<String, String> consumer = new KafkaConsumer<String, String>(properties)) {
				// 구독
				consumer.subscribe(Collections.singleton(topic));
				while(true) {
					ConsumerRecords<String, String> records = consumer.poll(Duration.ofMillis(100));
					for(ConsumerRecord<String, String> record : records) {
						receiveMessageAndInvokeHandleMethod(controller, record);
					}
				}
			}
		},"consumer-"+consumerCount).start();
	}
	
private Properties kafkaConfigProperties(Annotation cAnnotation) {
		String[] bootstrapServers = ((MessageController)cAnnotation).bootstrapServers(); 
		String groupId = ((MessageController)cAnnotation).groupId();
//		String allowAutoCreateTopics = ((KafkaController)cAnnotation).allowAutoCreateTopics();
//		String autoCommitIntervalMs = ((KafkaController)cAnnotation).autoCommitIntervalMs();
//		String autoOffsetReset = ((KafkaController)cAnnotation).autoOffsetReset();
//		String checkCrcs = ((KafkaController)cAnnotation).checkCrcs();
//		String clientDnsLookup = ((KafkaController)cAnnotation).clientDnsLookup();
//		String clientId = ((KafkaController)cAnnotation).clientId();
//		String clientRack = ((KafkaController)cAnnotation).clientRack();
//		String connectionsMaxIdleMs = ((KafkaController)cAnnotation).connectionsMaxIdleMs();
//		String defaultApiTimeoutMs = ((KafkaController)cAnnotation).defaultApiTimeoutMs();
//		String enableAutoCommit = ((KafkaController)cAnnotation).enableAutoCommit();
//		String excludeInternalTopics = ((KafkaController)cAnnotation).excludeInternalTopics();
//		String fetchMaxBytes = ((KafkaController)cAnnotation).fetchMaxBytes();
//		String fetchMaxWaitMs = ((KafkaController)cAnnotation).fetchMaxWaitMs();
//		String fetchMinBytes = ((KafkaController)cAnnotation).fetchMinBytes();
//		String groupInstanceId = ((KafkaController)cAnnotation).groupInstanceId();
//		String heartbeatIntervalMs = ((KafkaController)cAnnotation).heartbeatIntervalMs();
//		String interceptorClasses = ((KafkaController)cAnnotation).interceptorClasses();
//		String internalLeaveGroupOnClose = ((KafkaController)cAnnotation).internalLeaveGroupOnClose();
//		String internalThrowOnFetchStableOffsetUnsupported = ((KafkaController)cAnnotation).internalThrowOnFetchStableOffsetUnsupported();
//		String isolationLevel = ((KafkaController)cAnnotation).isolationLevel();
//		String keyDeserializer = ((KafkaController)cAnnotation).keyDeserializer();
//		String maxPartitionFetchBytes = ((KafkaController)cAnnotation).maxPartitionFetchBytes();
//		String maxPollIntervalMs = ((KafkaController)cAnnotation).maxPollIntervalMs();
//		String maxPollRecords = ((KafkaController)cAnnotation).maxPollRecords();
//		String metadataMaxAgeMs = ((KafkaController)cAnnotation).metadataMaxAgeMs();
//		String metricReporters = ((KafkaController)cAnnotation).metricReporters();
//		String metricsNumSamples = ((KafkaController)cAnnotation).metricsNumSamples();
//		String metricsRecordingLevel = ((KafkaController)cAnnotation).metricsRecordingLevel();
//		String metricsSampleWindowMs = ((KafkaController)cAnnotation).metricsSampleWindowMs();
//		String partitionAssignmentStrategy = ((KafkaController)cAnnotation).partitionAssignmentStrategy();
//		String receiveBufferBytes = ((KafkaController)cAnnotation).receiveBufferBytes();
//		String reconnectBackoffMaxMs = ((KafkaController)cAnnotation).reconnectBackoffMaxMs();
//		String reconnectBackoffMs = ((KafkaController)cAnnotation).reconnectBackoffMs();
//		String requestTimeoutMs = ((KafkaController)cAnnotation).requestTimeoutMs();
//		String retryBackoffMs = ((KafkaController)cAnnotation).retryBackoffMs();
//		String saslClientCallbackHandlerClass = ((KafkaController)cAnnotation).saslClientCallbackHandlerClass();
//		String saslJaasConfig = ((KafkaController)cAnnotation).saslJaasConfig();
//		String saslKerberosKinitCmd = ((KafkaController)cAnnotation).saslKerberosKinitCmd();
//		String saslKerberosMinTimeBeforeRelogin = ((KafkaController)cAnnotation).saslKerberosMinTimeBeforeRelogin();
//		String saslKerberosServiceName = ((KafkaController)cAnnotation).saslKerberosServiceName();
//		String saslKerberosTicketRenewJitter = ((KafkaController)cAnnotation).saslKerberosTicketRenewJitter();
//		String saslKerberosTicketRenewWindowFactor = ((KafkaController)cAnnotation).saslKerberosTicketRenewWindowFactor();
//		String saslLoginCallbackHandlerClass = ((KafkaController)cAnnotation).saslLoginCallbackHandlerClass();
//		String saslLoginClass = ((KafkaController)cAnnotation).saslLoginClass();
//		String saslLoginRefreshBufferSeconds = ((KafkaController)cAnnotation).saslLoginRefreshBufferSeconds();
//		String saslLoginRefreshMinPeriodSeconds = ((KafkaController)cAnnotation).saslLoginRefreshMinPeriodSeconds();
//		String saslLoginRefreshWindowFactor = ((KafkaController)cAnnotation).saslLoginRefreshWindowFactor();
//		String saslLoginRefreshWindowJitter = ((KafkaController)cAnnotation).saslLoginRefreshWindowJitter();
//		String saslMechanism = ((KafkaController)cAnnotation).saslMechanism();
//		String securityProtocol = ((KafkaController)cAnnotation).securityProtocol();
//		String securityProviders = ((KafkaController)cAnnotation).securityProviders();
//		String sendBufferBytes = ((KafkaController)cAnnotation).sendBufferBytes();
//		String sessionTimeoutMs = ((KafkaController)cAnnotation).sessionTimeoutMs();
//		String sslCipherSuites = ((KafkaController)cAnnotation).sslCipherSuites();
//		String sslEnabledProtocols = ((KafkaController)cAnnotation).sslEnabledProtocols();
//		String sslEndpointIdentificationAlgorithm = ((KafkaController)cAnnotation).sslEndpointIdentificationAlgorithm();
//		String sslEngineFactoryClass = ((KafkaController)cAnnotation).sslEngineFactoryClass();
//		String sslKeyPassword = ((KafkaController)cAnnotation).sslKeyPassword();
//		String sslKeymanagerAlgorithm = ((KafkaController)cAnnotation).sslKeymanagerAlgorithm();
//		String sslKeystoreLocation = ((KafkaController)cAnnotation).sslKeystoreLocation();
//		String sslKeystorePassword = ((KafkaController)cAnnotation).sslKeystorePassword();
//		String sslKeystoreType = ((KafkaController)cAnnotation).sslKeystoreType();
//		String sslProtocol = ((KafkaController)cAnnotation).sslProtocol();
//		String sslProvider = ((KafkaController)cAnnotation).sslProvider();
//		String sslSecureRandomImplementation = ((KafkaController)cAnnotation).sslSecureRandomImplementation();
//		String sslTrustmanagerAlgorithm = ((KafkaController)cAnnotation).sslTrustmanagerAlgorithm();
//		String sslTruststoreLocation = ((KafkaController)cAnnotation).sslTruststoreLocation();
//		String sslTruststorePassword = ((KafkaController)cAnnotation).sslTruststorePassword();
//		String sslTruststoreType = ((KafkaController)cAnnotation).sslTruststoreType();
//		String valueDeserializer = ((KafkaController)cAnnotation).valueDeserializer();
		
		
		
		// 카프카 기본 설정
		Properties properties = new Properties();
		properties.put("bootstrap.servers",String.join(",", Arrays.asList(bootstrapServers)));
		properties.put("key.deserializer",StringDeserializer.class);
		properties.put("value.deserializer",StringDeserializer.class);
		properties.put("group.id", groupId);
//		properties.put("allow.auto.create.topics", allowAutoCreateTopics);
//		properties.put("auto.commit.interval.ms", autoCommitIntervalMs);
//		properties.put("auto.offset.reset", autoOffsetReset);
//		properties.put("check.crcs", checkCrcs);
//		properties.put("client.dns.lookup", clientDnsLookup);
//		properties.put("client.id", clientId);
//		properties.put("client.rack", clientRack);
//		properties.put("connections.max.idle.ms", connectionsMaxIdleMs);
//		properties.put("default.api.timeout.ms", defaultApiTimeoutMs);
//		properties.put("enable.auto.commit", enableAutoCommit);
//		properties.put("exclude.internal.topics", excludeInternalTopics);
//		properties.put("fetch.max.bytes", fetchMaxBytes);
//		properties.put("fetch.max.wait.ms", fetchMaxWaitMs);
//		properties.put("fetch.min.bytes", fetchMinBytes);
//		properties.put("group.instance.id", groupInstanceId);
//		properties.put("heartbeat.interval.ms", heartbeatIntervalMs);
//		properties.put("interceptor.classes", interceptorClasses);
//		properties.put("internal.leave.group.on.close", internalLeaveGroupOnClose);
//		properties.put("internal.throw.on.fetch.stable.offset.unsupported", internalThrowOnFetchStableOffsetUnsupported);
//		properties.put("isolation.level", isolationLevel);
//		properties.put("key.deserializer", keyDeserializer);
//		properties.put("max.partition.fetch.bytes", maxPartitionFetchBytes);
//		properties.put("max.poll.interval.ms", maxPollIntervalMs);
//		properties.put("max.poll.records", maxPollRecords);
//		properties.put("metadata.max.age.ms", metadataMaxAgeMs);
//		properties.put("metric.reporters", metricReporters);
//		properties.put("metrics.num.samples", metricsNumSamples);
//		properties.put("metrics.recording.level", metricsRecordingLevel);
//		properties.put("metrics.sample.window.ms", metricsSampleWindowMs);
//		properties.put("partition.assignment.strategy", partitionAssignmentStrategy);
//		properties.put("receive.buffer.bytes", receiveBufferBytes);
//		properties.put("reconnect.backoff.max.ms", reconnectBackoffMaxMs);
//		properties.put("reconnect.backoff.ms", reconnectBackoffMs);
//		properties.put("request.timeout.ms", requestTimeoutMs);
//		properties.put("retry.backoff.ms", retryBackoffMs);
//		properties.put("sasl.client.callback.handler.class", saslClientCallbackHandlerClass);
//		properties.put("sasl.jaas.config", saslJaasConfig);
//		properties.put("sasl.kerberos.kinit.cmd", saslKerberosKinitCmd);
//		properties.put("sasl.kerberos.min.time.before.relogin", saslKerberosMinTimeBeforeRelogin);
//		properties.put("sasl.kerberos.service.name", saslKerberosServiceName);
//		properties.put("sasl.kerberos.ticket.renew.jitter", saslKerberosTicketRenewJitter);
//		properties.put("sasl.kerberos.ticket.renew.window.factor", saslKerberosTicketRenewWindowFactor);
//		properties.put("sasl.login.callback.handler.class", saslLoginCallbackHandlerClass);
//		properties.put("sasl.login.class", saslLoginClass);
//		properties.put("sasl.login.refresh.buffer.seconds", saslLoginRefreshBufferSeconds);
//		properties.put("sasl.login.refresh.min.period.seconds", saslLoginRefreshMinPeriodSeconds);
//		properties.put("sasl.login.refresh.window.factor", saslLoginRefreshWindowFactor);
//		properties.put("sasl.login.refresh.window.jitter", saslLoginRefreshWindowJitter);
//		properties.put("sasl.mechanism", saslMechanism);
//		properties.put("security.protocol", securityProtocol);
//		properties.put("security.providers", securityProviders);
//		properties.put("send.buffer.bytes", sendBufferBytes);
//		properties.put("session.timeout.ms", sessionTimeoutMs);
//		properties.put("ssl.cipher.suites", sslCipherSuites);
//		properties.put("ssl.enabled.protocols", sslEnabledProtocols);
//		properties.put("ssl.endpoint.identification.algorithm", sslEndpointIdentificationAlgorithm);
//		properties.put("ssl.engine.factory.class", sslEngineFactoryClass);
//		properties.put("ssl.key.password", sslKeyPassword);
//		properties.put("ssl.keymanager.algorithm", sslKeymanagerAlgorithm);
//		properties.put("ssl.keystore.location", sslKeystoreLocation);
//		properties.put("ssl.keystore.password", sslKeystorePassword);
//		properties.put("ssl.keystore.type", sslKeystoreType);
//		properties.put("ssl.protocol", sslProtocol);
//		properties.put("ssl.provider", sslProvider);
//		properties.put("ssl.secure.random.implementation", sslSecureRandomImplementation);
//		properties.put("ssl.trustmanager.algorithm", sslTrustmanagerAlgorithm);
//		properties.put("ssl.truststore.location", sslTruststoreLocation);
//		properties.put("ssl.truststore.password", sslTruststorePassword);
//		properties.put("ssl.truststore.type", sslTruststoreType);
//		properties.put("value.deserializer", valueDeserializer);
		
		return properties;
	}
}
