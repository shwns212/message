package com.jun.message.sender;

import java.util.Properties;
import java.util.function.BiConsumer;

import org.apache.kafka.clients.producer.Callback;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.StringSerializer;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jun.message.exception.FailedMessageSerializeException;
import com.jun.message.listener.MessageListener;
import com.jun.message.listener.SpringMessageListener;
import com.jun.message.message.Message;

public class MessageSender {
	
	private String topic;
	private Properties properties;
	
	/**
	 * 사용자가 세부 설정을 직접 선택하는 방식의 생성자
	 * @param topic
	 * @param properties
	 */
	public MessageSender(String topic, Properties properties) {
		this.topic = topic;
		this.properties = properties;
	}

	/**
	 * 기본 설정을 선택하는 방식의 생성자
	 * @param topic
	 * @param bootstrapServers
	 */
	public MessageSender(String topic, String... bootstrapServers) {
		this.topic = topic;
		this.properties = new Properties();
		this.properties.put("bootstrap.servers", String.join(",", bootstrapServers));
		this.properties.put("key.serializer", StringSerializer.class);
		this.properties.put("value.serializer", StringSerializer.class);
	}

	public void sendWithCallback(Message message, Callback callback) {
		send(message, (producer, record) -> {
			producer.send(record, callback);
		});
		
	}
	
	public void send(Message message) {
		send(message, (producer, record) -> {
			producer.send(record);
		});
	}
	
	private void send(Message message, BiConsumer<KafkaProducer<String, String>, ProducerRecord<String, String>> consumer) {
		try(KafkaProducer<String, String> producer = new KafkaProducer<String, String>(this.properties)) {
			message.bodyToMap(); // Deserialize가 가능한지 확인 불가능할 경우 예외
			ProducerRecord<String, String> record = new ProducerRecord<String, String>(topic, new ObjectMapper().writeValueAsString(message));
			consumer.accept(producer, record);
		}catch(JsonProcessingException e) {
			throw new FailedMessageSerializeException(e);
		}
	}

}
