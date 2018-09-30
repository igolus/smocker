package com.jenetics.smocker.ui.dialog;

import com.vaadin.ui.Alignment;
import com.vaadin.ui.Component;

import de.steinwedel.messagebox.MessageBox;

public class CustomMessageBox extends MessageBox {
	
	
	public MessageBox withMessage(Component messageComponent) {
		if (this.messageComponent != null) {
			contentLayout.removeComponent(this.messageComponent);
		}
		
		this.messageComponent = messageComponent;
		
		if (messageComponent != null) {
			messageComponent.setSizeFull();
			contentLayout.addComponent(messageComponent, contentLayout.getComponentCount());
			contentLayout.setExpandRatio(messageComponent, 1.0f);		
			contentLayout.setComponentAlignment(messageComponent, Alignment.MIDDLE_CENTER);
		}
		return this;
	}
}
