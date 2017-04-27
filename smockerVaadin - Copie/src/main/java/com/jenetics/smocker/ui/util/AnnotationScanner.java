package com.jenetics.smocker.ui.util;

import java.lang.annotation.Annotation;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.reflections.Reflections;

import com.jenetics.smocker.annotation.ContentView;
import com.jenetics.smocker.annotation.RootView;
import com.vaadin.event.ItemClickEvent;
import com.vaadin.event.ItemClickEvent.ItemClickListener;
import com.vaadin.navigator.Navigator;
import com.vaadin.navigator.View;
import com.vaadin.ui.Tree;

public class AnnotationScanner {

	public AnnotationScanner(Navigator navigator) throws InstantiationException, IllegalAccessException {
		super();
		init(navigator);
	}
	
	private AnnotationScanner() {
		// TODO Auto-generated constructor stub
	}
	
	private Map<String, TreeWithIcon> treeMap = new HashMap();
	private Map<String, View> viewMap = new HashMap();
	
	

	public Map<String, TreeWithIcon> getTreeMap() {
		return treeMap;
	}



	public Map<String, View> getViewMap() {
		return viewMap;
	}



	public void init(Navigator navigator)
			throws InstantiationException, IllegalAccessException {
		
		Reflections reflections = new Reflections("com.jenetics.smocker.ui.view");

		Set<Class<?>> annotatedRootView = reflections.getTypesAnnotatedWith(RootView.class);
		for (Class<?> classTarget : annotatedRootView) {
			Annotation[] annotations = classTarget.getDeclaredAnnotations();
			for (Annotation annotation : annotations) {
				if (annotation instanceof RootView) {
					RootView rootView = (RootView) annotation;
					if (treeMap.get(rootView.viewName()) == null) {
						Tree tree = new Tree();
						treeMap.put(rootView.viewName(),
								new TreeWithIcon(tree, rootView.icon(), rootView.useFontAwasome()));
						tree.addItemClickListener(new ItemClickListener() {
							@Override
							public void itemClick(ItemClickEvent event) {
								navigator.navigateTo(event.getItemId().toString());
							}
						});
					}
				}
			}
		}

		Set<Class<?>> annotatedContentView = reflections.getTypesAnnotatedWith(ContentView.class);

		// first Iteration to set the TreeInstance and set the roots
		for (Class<?> classTarget : annotatedContentView) {
			Object view = classTarget.newInstance();
			if (view instanceof RefreshableView) {

				Annotation[] annotations = classTarget.getDeclaredAnnotations();
				for (Annotation annotation : annotations) {
					if (annotation instanceof ContentView) {
						ContentView contentView = (ContentView) annotation;
						if (contentView.homeView()) {
							navigator.addView("" , (View) view);

						}
						navigator.addView(classTarget.toString(), (View) view);
						TreeWithIcon tree = treeMap.get(contentView.accordeonParent());
						if (tree != null) {
							tree.getTree().addItem(contentView.viewName());
							tree.getTree().setChildrenAllowed(contentView.viewName(), false);
							viewMap.put(contentView.viewName(), (View) view);
						}
					}
				}
			}
			
			//set the parents
			
		}
		
		
		for (Class<?> classTarget : annotatedContentView) {
			Object view = classTarget.newInstance();
			if (view instanceof RefreshableView) {
				Annotation[] annotations = classTarget.getDeclaredAnnotations();
				for (Annotation annotation : annotations) {
					if (annotation instanceof ContentView) {
						ContentView contentView = (ContentView) annotation;
						TreeWithIcon tree = treeMap.get(contentView.accordeonParent());
						if (tree != null) {
							if (!contentView.componentParent().equals(ContentView.NOT_SET)) {
								tree.getTree().setChildrenAllowed(contentView.componentParent(), true);
								tree.getTree().setChildrenAllowed(contentView.viewName(), false);
								tree.getTree().setParent(contentView.viewName(), contentView.componentParent());
							}
						}
					}
				}
			}
		}
	}

}
