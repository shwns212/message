package com.jun.message.message;

import java.util.HashMap;
import java.util.Map;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jun.message.exception.FailedMessageDeserializeException;

public class Message {
	private String type;
	private String body;
	
	@SuppressWarnings("unused")
	private Message() {}
	
	public Message(String type, Object body) {
		try {
			this.type = type;
			this.body = new ObjectMapper().writeValueAsString(body);
		} catch (JsonProcessingException e) {
			throw new FailedMessageDeserializeException(e);
		}
	}
	
	/**
	 * json message ������ ���ϴ� ��ü�� ��ȯ�Ͽ� ��ȯ�Ѵ�. 
	 * @param <T>
	 * @param clazz
	 * @return
	 */
	public <T> T bodyToObject(Class<T> clazz) {
		try {
			return new ObjectMapper().readValue(body.toString(), clazz);
		} catch (JsonProcessingException e) {
			throw new FailedMessageDeserializeException(e);
		}
	}
	
	/**
	 * json message ������ map���� ��ȯ�Ͽ� ��ȯ�Ѵ�.
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public Map<String, Object> bodyToMap() {
		try {
			return new ObjectMapper().readValue(body.toString(), HashMap.class);
		} catch (JsonProcessingException e) {
			throw new FailedMessageDeserializeException(e);
		}
	}
	
	public String getType() {
		return type;
	}
	public Object getBody() {
		return body;
	}

}
