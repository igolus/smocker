package com.jenetics.smocker.dao;

import java.util.List;

import com.jenetics.smocker.model.Connection;
import com.jenetics.smocker.model.config.JsFilterAndDisplay;
import com.jenetics.smocker.model.config.SmockerConf;
import com.jenetics.smocker.ui.view.JavaApplicationsView;

public class DaoConfig {
	
	protected static IDaoManager<SmockerConf> daoManagerSmockerConf = 
			DaoManagerByModel.getDaoManager(SmockerConf.class);
	private static SmockerConf smockerConfSingleItem; 
	
	public static SmockerConf getSingleConfig() {
		List<SmockerConf> listConfig = daoManagerSmockerConf.listAll();
		if (listConfig.size() == 0) {
			smockerConfSingleItem = new SmockerConf();
			smockerConfSingleItem = daoManagerSmockerConf.create(smockerConfSingleItem);
		}
		else {
			smockerConfSingleItem = listConfig.get(0);
		}
		return smockerConfSingleItem;
	}
	
	public static SmockerConf saveConfig() {
		return daoManagerSmockerConf.update(smockerConfSingleItem);
	}

	public static JsFilterAndDisplay findJsDisplayAndFilter(Connection conn) {
		List<JsFilterAndDisplay> listDisplayandFilter = JavaApplicationsView.daoManagerJsFilterAndDisplay.queryList("SELECT js FROM JsFilterAndDisplay js WHERE js.host = '" + conn.getHost() + 
				"' and js.port = '" + conn.getPort().toString() + "'");
	
		JsFilterAndDisplay first = listDisplayandFilter.stream().findFirst().orElse(null);
		if (first == null) {
			first = new JsFilterAndDisplay();
			first.setHost(conn.getHost());
			first.setPort(conn.getPort());
			first.setFunctionFilter("");
			first.setFunctionInputDisplay("");
			first.setFunctionOutputDisplay("");
			first = JavaApplicationsView.daoManagerJsFilterAndDisplay.create(first);
		}
		return first;
	}
}
