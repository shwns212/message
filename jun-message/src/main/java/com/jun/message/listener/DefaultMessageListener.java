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
	 * KafkaController ������̼��� �پ��ִ� Ŭ������ ã�Ƽ� ��ü�� ������ list�� �߰��Ѵ�.
	 * @param basePackage
	 * @param context
	 */
	private void addMessageControllerObject(String basePackage) {
		super.addMessageControllerObject(basePackage, obj -> {
			MessageListener.kafkaControllers.add(obj);
		});
	}
	
	private void handle() {
		// Ŭ���� ������ KafkaController ������̼��� �پ��ִ� ��ü��
		for(Object controller : MessageListener.kafkaControllers) {
			MessageController messageController = controller.getClass().getDeclaredAnnotation(MessageController.class);
			super.handle(messageController, messageController);
		}
	}
}
