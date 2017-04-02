package com.jenetics.smocker.ui.util;

import java.lang.annotation.Annotation;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.reflections.Reflections;

import com.jenetics.smocker.annotation.ContentView;
import com.jenetics.smocker.annotation.RootView;
import com.vaadin.event.ItemClickEvent;
import com.vaadin.ui.Tree;
import com.vaadin.ui.UI;
import com.vaadin.ui.Tree.ExpandEvent;
import com.vaadin.ui.Tree.ExpandListener;
import com.vaadin.event.ItemClickEvent.ItemClickListener;
import com.vaadin.navigator.Navigator;
import com.vaadin.navigator.View;

public class AnnotationScanner {

	public static Map<String, TreeWithIcon> getTreeMap(Navigator navigator)
			throws InstantiationException, IllegalAccessException {
		Map<String, TreeWithIcon> treeMap = new HashMap<>();
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
								System.out.println(event);
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
						navigator.addView(contentView.viewName(), (View) view);
						TreeWithIcon tree = treeMap.get(contentView.accordeonParent());
						if (tree != null) {
							tree.getTree().addItem(contentView.viewName());
							tree.getTree().setChildrenAllowed(contentView.viewName(), false);
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
								//tree.getTree().setParent(contentView.viewName(), contentView.componentParent());
								//tree.getTree().setChildrenAllowed(contentView.componentParent(), true);
							}
						}
					}
				}
			}
		}
		
		return treeMap;
	}

}
