package com.jenetics.smocker.ui.util;

import com.vaadin.ui.Tree;

public class TreeWithIcon {
	private Tree tree;
	private String icon;
	private boolean fontAwesome;
	public TreeWithIcon(Tree tree, String icon, boolean fontAwesome) {
		super();
		this.tree = tree;
		this.icon = icon;
		this.fontAwesome = fontAwesome;
	}
	public Tree getTree() {
		return tree;
	}
	public String getIcon() {
		return icon;
	}
	public boolean isFontAwesome() {
		return fontAwesome;
	}
	
	
	
}
