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
	 * KafkaController 어노테이션이 붙어있는 클래스를 찾아서 객체를 생성후 spring application context에 추가한다.
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
