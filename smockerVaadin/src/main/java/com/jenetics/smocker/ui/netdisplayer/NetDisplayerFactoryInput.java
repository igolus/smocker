package com.jenetics.smocker.ui.netdisplayer;

import java.util.ResourceBundle;

import org.apache.commons.lang3.StringUtils;

import com.jenetics.smocker.dao.DaoConfig;
import com.jenetics.smocker.model.Communication;
import com.jenetics.smocker.model.config.JsFilterAndDisplay;
import com.jenetics.smocker.ui.netdisplayer.implementation.DefaultViewer;
import com.jenetics.smocker.ui.netdisplayer.implementation.JSConfigViewer;

public class NetDisplayerFactoryInput {

	private NetDisplayerFactoryInput() {
		super();
	}

	private static ResourceBundle bundle = ResourceBundle.getBundle("BundleUI");

	public static ComponentWithDisplayChange getComponent(String socketInput, Communication comm) {
		JsFilterAndDisplay jsDisplayAndFilter = DaoConfig.findJsDisplayAndFilter(comm.getConnection());
		String functionInputDisplay = jsDisplayAndFilter.getFunctionInputDisplay();
		if (!StringUtils.isEmpty(functionInputDisplay) ) {
			return new JSConfigViewer(bundle.getString("Input"), jsDisplayAndFilter, true);
		}
		
		return new DefaultViewer(bundle.getString("Input"));
	}
}
