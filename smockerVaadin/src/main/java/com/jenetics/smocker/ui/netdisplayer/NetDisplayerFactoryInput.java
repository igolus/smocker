package com.jenetics.smocker.ui.netdisplayer;

import java.util.ResourceBundle;

import com.jenetics.smocker.ui.netdisplayer.implementation.DefaultViewer;

public class NetDisplayerFactoryInput {
	
	
	private static ResourceBundle bundle = ResourceBundle.getBundle("BundleUI");
	
	public static ComponentWithDisplayChange getComponent(String socketOutput) {
		return new DefaultViewer(bundle.getString("Input"));
	}
}
