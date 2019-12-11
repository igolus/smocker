package com.jenetics.smocker.dao;

import java.util.List;

import com.jenetics.smocker.model.Connection;
import com.jenetics.smocker.model.config.JsFilterAndDisplay;
import com.jenetics.smocker.model.config.SmockerConf;
import com.jenetics.smocker.ui.view.JavaApplicationsView;

public class DaoConfig {
	
	private DaoConfig() {
		super();
	}

	protected static IDaoManager<SmockerConf> daoManagerSmockerConf = 
			DaoManagerByModel.getDaoManager(SmockerConf.class);
	private static SmockerConf smockerConfSingleItem; 
	
	public static SmockerConf getSingleConfig() {
		List<SmockerConf> listConfig = daoManagerSmockerConf.listAll();
		if (listConfig.isEmpty()) {
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
		
		if (conn != null && conn.getHost() != null && conn.getPort() != null) {
			List<JsFilterAndDisplay> listDisplayandFilter = JavaApplicationsView.daoManagerJsFilterAndDisplay.queryList("SELECT js FROM JsFilterAndDisplay js WHERE js.host = '" + conn.getHost() + 
					"' and js.port = '" + conn.getPort().toString() + "'");

			if (listDisplayandFilter != null) {
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
		return null;
		
	}
}
