package com.jenetics.smocker.ui.util;

import java.lang.annotation.Annotation;
import java.util.HashMap;

import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.core.type.filter.AnnotationTypeFilter;

import com.jenetics.smocker.annotation.ContentView;

public class AnnotationScanner {
	
	
	private static HashMap<String, ViewAndIconContainer> viewMap = null;

	public static HashMap<String, ViewAndIconContainer> getViewMap() throws ClassNotFoundException, InstantiationException, IllegalAccessException {
		if (viewMap == null) {
			viewMap = new HashMap<>();
			ClassPathScanningCandidateComponentProvider provider = new ClassPathScanningCandidateComponentProvider(false);
			provider.addIncludeFilter(new AnnotationTypeFilter(ContentView.class));
			
			for (BeanDefinition beanDef : provider.findCandidateComponents("com.jenetics.smocker.ui.view")) {
				Class<?> clazz =  Class.forName(beanDef.getBeanClassName());
				Object view = clazz.newInstance();
				//System.out.println(beanDef);
				if (view instanceof RefreshableView) {
					Annotation[] annotations = clazz.getDeclaredAnnotations();
					for (Annotation annotation : annotations) {
						if (annotation instanceof ContentView) {
							ContentView contentView = (ContentView) annotation;
							viewMap.put(contentView.viewName(), new ViewAndIconContainer((RefreshableView)view, contentView.icon()));
						}
					}
				}
				//TODO manage errors
	        }
	    }
	
		return viewMap;
	}
    
	
	
}
