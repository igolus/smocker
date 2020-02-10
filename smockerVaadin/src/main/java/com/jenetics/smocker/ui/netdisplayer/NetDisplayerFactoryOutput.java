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
		JsFilterAndDisplay jsDisplayAndFilter = DaoConfig.findJsDisplayAndFilter(comm.getConnection());
		String functionOutputDisplay = jsDisplayAndFilter.getFunctionOutputDisplay();
		if (!StringUtils.isEmpty(functionOutputDisplay) ) {
			return new JSConfigViewer(bundle.getString("Output"), jsDisplayAndFilter, false);
		}

		return new DefaultViewer(bundle.getString("Output"));
	}
}
