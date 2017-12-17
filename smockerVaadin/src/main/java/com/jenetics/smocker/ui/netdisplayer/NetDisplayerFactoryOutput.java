package com.jenetics.smocker.ui.netdisplayer;

import java.util.ResourceBundle;

import com.jenetics.smocker.ui.netdisplayer.implementation.DefaultViewer;
import com.jenetics.smocker.ui.netdisplayer.implementation.JsonViewer;
import com.jenetics.smocker.util.NetworkReaderUtility;

public class NetDisplayerFactoryOutput {
	
	private NetDisplayerFactoryOutput() {
		super();
	}

	private static ResourceBundle bundle = ResourceBundle.getBundle("BundleUI");

	public static ComponentWithDisplayChange getComponent(String socketOutput) {
		if (NetworkReaderUtility.readHeaderValue(socketOutput, NetworkReaderUtility.HEADER_CONTENT_TYPE) != null && 
				NetworkReaderUtility.readHeaderValue(socketOutput, NetworkReaderUtility.HEADER_CONTENT_TYPE).trim()
				.startsWith(NetworkReaderUtility.CONTENT_TYPE_JSON)) {
			return new JsonViewer(bundle.getString("Output"));
		}
		return new DefaultViewer(bundle.getString("Output"));
	}
}
