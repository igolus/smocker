package com.jenetics.smocker.dao;

import java.util.List;

import com.jenetics.smocker.model.config.SmockerConf;

public class DaoConfig {
	
	protected static IDaoManager<SmockerConf> daoManagerSmockerConf = null;
	private static SmockerConf smockerConfSingleItem; 
	
	public static SmockerConf getSingleConfig() {
		List<SmockerConf> listConfig = daoManagerSmockerConf.listAll();
		if (listConfig.size() == 0) {
			smockerConfSingleItem = new SmockerConf();
			daoManagerSmockerConf.create(smockerConfSingleItem);
		}
		else {
			smockerConfSingleItem = listConfig.get(0);
		}
		return smockerConfSingleItem;
	}
	
	public static SmockerConf saveConfig(SmockerConf smockerConf) {
		return daoManagerSmockerConf.update(smockerConf);
	}
}
