package com.jenetics.smocker.ui.view;

import java.util.List;

import org.vaadin.easyapp.util.annotations.ContentView;

import com.jenetics.smocker.dao.DaoManager;
import com.jenetics.smocker.model.Communication;
import com.jenetics.smocker.model.Connection;
import com.jenetics.smocker.model.JavaApplication;
import com.jenetics.smocker.model.config.SmockerConf;
import com.jenetics.smocker.network.ClientCommunicator;
import com.jenetics.smocker.ui.SmockerUI;
import com.vaadin.navigator.View;
import com.vaadin.navigator.ViewChangeListener.ViewChangeEvent;
import com.vaadin.ui.Button;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;

@ContentView(sortingOrder = 1, viewName = "Congiguration", icon = "icons/Java-icon.png", homeView = true, rootViewParent = ConnectionsRoot.class)
public class ConfigurationView extends VerticalLayout implements View {
	
	DaoManager<JavaApplication> javaApplicationDaoManager = new DaoManager<JavaApplication>(JavaApplication.class, SmockerUI.getEm());
	
	public ConfigurationView() {
		Button switchRecordReplay = new Button("REPLAY");
		switchRecordReplay.addClickListener(new ClickListener() {
			
			@Override
			public void buttonClick(ClickEvent event) {
				// TODO Auto-generated method stub
				DaoManager<SmockerConf> daoConf = new DaoManager<SmockerConf>(SmockerConf.class, SmockerUI.getEm());
				SmockerConf conf = daoConf.listAll().iterator().next();
				if (conf.equals("VIEW")) {
					conf.setMode("REPLAY");
					switchRecordReplay.setCaption("VIEW");
				}
				else {
					conf.setMode("VIEW");
					switchRecordReplay.setCaption("VIEW");
				}
				daoConf.update(conf);
				
				
				List<JavaApplication> allJavaApp = javaApplicationDaoManager.listAll();
				if (allJavaApp.size() > 0) {
					ClientCommunicator.sendMode(conf.getMode(), allJavaApp.get(0));
				}
			}
		});
		
	   addComponent(switchRecordReplay);
		// TODO Auto-generated constructor stub
	}

	@Override
	public void enter(ViewChangeEvent event) {
		// TODO Auto-generated method stub
		
	}
	
	
}
