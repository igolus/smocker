package com.jenetics.smocker.ui.util;

import java.lang.annotation.Annotation;
import java.util.HashMap;

import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.core.type.filter.AnnotationTypeFilter;

import com.jenetics.smocker.annotation.ContentView;

public class AnnotationScanner {
	private static HashMap<String, RefreshableView> viewMap = null;

	public static HashMap<String, RefreshableView> getViewMap() throws ClassNotFoundException, InstantiationException, IllegalAccessException {
		if (viewMap == null) {
			
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
							viewMap = new HashMap<>();
							viewMap.put(contentView.viewName(), (RefreshableView) view);
						}
					}
				}
	        }
	    }
	
		return viewMap;
	}
    
	
	
}
