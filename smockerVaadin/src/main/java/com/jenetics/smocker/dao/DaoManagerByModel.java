package com.jenetics.smocker.dao;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.EntityManager;


import com.jenetics.smocker.model.EntityWithId;
import com.jenetics.smocker.model.Scenario;
import com.jenetics.smocker.ui.SmockerUI;

public class DaoManagerByModel {

	private static final String UNDEFINED = "undefined";

	static {
		daoManagerByClass = new HashMap<>();
		checkRootScenarioExist();
	}

	private DaoManagerByModel() {
		super();
	}

	private static Map<Class<?>, DaoManager<?>> daoManagerByClass;
	private static Scenario UNDEFINED_SCENARIO;

	public static Scenario getUNDEFINED_SCENARIO() {
		DaoManager<Scenario> daoManagerScenario = getDaoManager(Scenario.class);
		if (daoManagerScenario != null) {
			return daoManagerScenario.findById(UNDEFINED_SCENARIO.getId());
		}
		return null;
	}

	public static <T extends EntityWithId> DaoManager<T> getDaoManager(Class<T> typeParameterClass) {
		if (daoManagerByClass.containsKey(typeParameterClass)) {
			return (DaoManager<T>) daoManagerByClass.get(typeParameterClass);			
		}
		if (isEntityDefinedInMetaModel( SmockerUI.getEm(), typeParameterClass)) {
			DaoManager<T> daoManager = new DaoManager<>(typeParameterClass, SmockerUI.getEm());
			daoManagerByClass.put(typeParameterClass, daoManager);
			return daoManager;
		}
		else if (isEntityDefinedInMetaModel(SmockerUI.getEmPersitant(), typeParameterClass)) {
			DaoManager<T> daoManager = new DaoManager<>(typeParameterClass, SmockerUI.getEmPersitant());
			daoManagerByClass.put(typeParameterClass, daoManager);
			return daoManager;
		}
		return null;
	}

	private static void checkRootScenarioExist() {
		DaoManager<Scenario> daoManagerScenario = getDaoManager(Scenario.class);
		if (daoManagerScenario != null) {
			if (daoManagerScenario.listAll().isEmpty()) {
				Scenario scenario = new Scenario();
				scenario.setName(SmockerUI.getBundleValue(UNDEFINED));
				scenario.setHost(SmockerUI.getBundleValue(UNDEFINED));
				scenario.setIp(SmockerUI.getBundleValue(UNDEFINED));
				scenario.setPort(0);
				scenario = daoManagerScenario.create(scenario);
				DaoManagerByModel.UNDEFINED_SCENARIO = scenario;
			}
			else {
				List<Scenario> listScenarios = 
						daoManagerScenario.queryList("SELECT s FROM Scenario s WHERE s.name = 'undefined'");
				if (!listScenarios.isEmpty()) {
					DaoManagerByModel.UNDEFINED_SCENARIO = listScenarios.get(0);
				}
			}
		}
	}

	private static <T extends EntityWithId> boolean isEntityDefinedInMetaModel(EntityManager em, Class<T> typeParameterClass) {
		try {
			em.getMetamodel().entity(typeParameterClass);
		}
		catch (Exception e) {
			return false;
		}
		return true;
	}

}
