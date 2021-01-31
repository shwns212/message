package com.jun.message.message;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import java.util.HashMap;
import java.util.Map;

import org.junit.Test;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jun.message.exception.FailedMessageDeserializeException;

public class MessageTest {
	ObjectMapper mapper = new ObjectMapper();
	
	/**
	 * json으로 deserialize 할 수 없는 메세지가 왔을 경우
	 */
	@Test(expected = FailedMessageDeserializeException.class)
	public void bodyToObjectTest_fail() {
		// given
		Map<String, Object> map = new HashMap<String, Object>();
		map.put("type", "test");
		map.put("body", "");
		Message message = new Message((String)map.get("type"), (String)map.get("body"));
		
		// when
		TestObject object = message.bodyToObject(TestObject.class);
	}
	
	/**
	 * json으로 deserialize 할 수 없는 메세지가 왔을 경우
	 */
	@Test(expected = FailedMessageDeserializeException.class)
	public void bodyToMapTest_fail() {
		// given
		Map<String, Object> map = new HashMap<String, Object>();
		map.put("type", "test");
		map.put("body", "");
		Message message = new Message((String)map.get("type"), (String)map.get("body"));
		
		// when
		message.bodyToMap();
	}
	
	@Test
	public void bodyToMapTest_success() throws JsonProcessingException {
		// given
		Map<String, Object> map = new HashMap<String, Object>();
		map.put("type", "test");
		map.put("body", new TestObject("abcd","test"));
		
		Message message = new Message((String)map.get("type"), map.get("body"));
		
		// when
		Map<String, Object> resultMap = message.bodyToMap();
		
		// then
		assertThat(resultMap.get("id"),is("abcd"));
		assertThat(resultMap.get("name"),is("test"));
	}
	
	@Test
	public void bodyToMapObject_success() throws JsonProcessingException {
		// given
		Map<String, Object> map = new HashMap<String, Object>();
		map.put("type", "test");
		map.put("body", new TestObject("abcd","test"));
		
		Message message = new Message((String)map.get("type"), map.get("body"));
		
		// when
		TestObject obj = message.bodyToObject(TestObject.class);
		
		// then
		assertThat(obj.getId(),is("abcd"));
		assertThat(obj.getName(),is("test"));
	}
	
	public static class TestObject {
		private String id;
		private String name;
		public TestObject() {
			
		}
		public TestObject(String id, String name) {
			super();
			this.id = id;
			this.name = name;
		}
		public String getId() {
			return id;
		}
		public String getName() {
			return name;
		}
		
	}
}
