package com.jun.message.listener;

import org.springframework.context.support.GenericApplicationContext;

import com.jun.message.annotation.MessageController;

public class SpringMessageListener extends MessageListener {
	
	private GenericApplicationContext context;
	
	public SpringMessageListener(String basePackage, GenericApplicationContext context) {
		this.basePackage = basePackage;
		this.context = context;
	}
	
	public void listen() {
		addMessageControllerObject(this.basePackage, this.context);
		handle(this.context);
	}
	
	/**
	 * KafkaController ������̼��� �پ��ִ� Ŭ������ ã�Ƽ� ��ü�� ������ spring application context�� �߰��Ѵ�.
	 * @param basePackage
	 * @param context
	 */
	private void addMessageControllerObject(String basePackage, GenericApplicationContext context) {
		super.addMessageControllerObject(basePackage, obj -> {
			context.registerBean(obj.getClass().getCanonicalName(), Object.class, () -> obj);
		});
	}
	
	private void handle(GenericApplicationContext context) {
		context.getBeansWithAnnotation(MessageController.class)
		.forEach((k,controller) -> {
			MessageController messageController = controller.getClass().getDeclaredAnnotation(MessageController.class);
			super.handle(messageController, controller);
		});
	}
	
}
