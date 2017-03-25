package com.jenetics.smocker.ui;

import com.jenetics.smocker.ui.util.AnnotationScanner;
import com.vaadin.annotations.Theme;
import com.vaadin.navigator.Navigator;
import com.vaadin.server.VaadinRequest;
import com.vaadin.ui.UI;

@Theme("smocker")
public class SmockerUI extends UI {

	public static final String PERSISTENCE_UNIT = "smockerLocalData";
	
	Navigator navigator;
    protected static final String MAINVIEW = "main";
	
	@Override
	protected void init(VaadinRequest request) {
		
		
		getPage().setTitle("Smocker");
		setContent(new SmockerMainView());
		
		try {
			AnnotationScanner.getViewMap();
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InstantiationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	


}
