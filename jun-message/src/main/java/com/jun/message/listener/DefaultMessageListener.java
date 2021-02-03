package com.jun.message.listener;

import com.jun.message.annotation.MessageController;

public class DefaultMessageListener extends MessageListener {
	
	public DefaultMessageListener(String basePackage) {
		this.basePackage = basePackage;
	}
	
	public void listen() {
		addMessageControllerObject(this.basePackage);
		handle();
	}
	/**
	 * KafkaController 어노테이션이 붙어있는 클래스를 찾아서 객체를 생성후 list에 추가한다.
	 * @param basePackage
	 * @param context
	 */
	private void addMessageControllerObject(String basePackage) {
		super.addMessageControllerObject(basePackage, obj -> {
			MessageListener.kafkaControllers.add(obj);
		});
	}
	
	private void handle() {
		// 클래스 레벨에 KafkaController 어노테이션이 붙어있는 객체들
		for(Object controller : MessageListener.kafkaControllers) {
			MessageController messageController = controller.getClass().getDeclaredAnnotation(MessageController.class);
			super.handle(messageController, messageController);
		}
	}
}
