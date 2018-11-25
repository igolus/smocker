package com.jenetics.smocker.ui.netdisplayer;

import java.util.ResourceBundle;

import org.apache.commons.lang3.StringUtils;

import com.jenetics.smocker.dao.DaoConfig;
import com.jenetics.smocker.jseval.JSEvaluator;
import com.jenetics.smocker.model.Communication;
import com.jenetics.smocker.model.config.JsFilterAndDisplay;
import com.jenetics.smocker.ui.netdisplayer.implementation.DefaultViewer;
import com.jenetics.smocker.ui.netdisplayer.implementation.JSConfigViewer;
import com.jenetics.smocker.ui.netdisplayer.implementation.JsonViewer;
import com.jenetics.smocker.util.NetworkReaderUtility;

public class NetDisplayerFactoryOutput {
	
	private NetDisplayerFactoryOutput() {
		super();
	}

	private static ResourceBundle bundle = ResourceBundle.getBundle("BundleUI");

	public static ComponentWithDisplayChange getComponent(String socketOutput, Communication comm) {
		
		//check if we get a mapper for the display 
		JsFilterAndDisplay jsDisplayAndFilter = DaoConfig.findJsDisplayAndFilter(comm.getConnection());
		if (!StringUtils.isEmpty(jsDisplayAndFilter.getFunctionDisplay())) {
			return new JSConfigViewer(bundle.getString("Output"), jsDisplayAndFilter);
		}
		
		if (NetworkReaderUtility.readHeaderValue(socketOutput, NetworkReaderUtility.HEADER_CONTENT_TYPE) != null && 
				NetworkReaderUtility.readHeaderValue(socketOutput, NetworkReaderUtility.HEADER_CONTENT_TYPE).trim()
				.startsWith(NetworkReaderUtility.CONTENT_TYPE_JSON)) {
			return new JsonViewer(bundle.getString("Output"));
		}
		return new DefaultViewer(bundle.getString("Output"));
	}
}
