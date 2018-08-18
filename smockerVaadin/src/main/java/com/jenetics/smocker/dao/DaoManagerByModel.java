package com.jenetics.smocker.dao;

import java.util.HashMap;
import java.util.Map;

import javax.persistence.EntityManager;

import com.jenetics.smocker.model.EntityWithId;
import com.jenetics.smocker.ui.SmockerUI;

public class DaoManagerByModel {
	
	private DaoManagerByModel() {
		super();
	}

	private static Map<Class<?>, DaoManager<?>> daoManagerByClass = new HashMap<>();
	
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

	private static <T extends EntityWithId> boolean isEntityDefinedInMetaModel(EntityManager em, Class<T> typeParameterClass) {
		try {
			em.getMetamodel().entity(typeParameterClass);
		}
		catch (Throwable t) {
			return false;
		}
		return true;
	}

}
