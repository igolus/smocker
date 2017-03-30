package com.jenetics.smocker.ui.util;

import java.util.HashMap;

import javax.enterprise.event.Observes;

import com.jenetics.smocker.model.Connection;
import com.jenetics.smocker.ui.SmockerUI;
import com.vaadin.ui.UI;

public class EventManager {
	
	private static final String CONNECTIONS = "Connections";

	public void newConnection(@Observes Connection conn) {
		
		if (SmockerUI.getInstance().getSession() != null) {
			SmockerUI.getInstance().newConnection(CONNECTIONS);
		}
		
//		try {
//			HashMap<String, ViewAndIconContainer> viewMap = AnnotationScanner.getViewMap();
//			if (viewMap.get(CONNECTIONS) != null) {
//				viewMap.get(CONNECTIONS).getRefreshableView().refresh();
//			}
//			//UI.getCurrent().markAsDirty();
//		} catch (ClassNotFoundException | InstantiationException | IllegalAccessException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
	}
}
