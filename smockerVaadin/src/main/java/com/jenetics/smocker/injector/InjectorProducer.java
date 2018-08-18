package com.jenetics.smocker.injector;

import java.io.Serializable;
import java.lang.reflect.ParameterizedType;
import java.util.ResourceBundle;

import javax.enterprise.inject.Produces;
import javax.enterprise.inject.spi.InjectionPoint;
import javax.inject.Named;
import javax.persistence.EntityManager;

import org.jboss.logging.Logger;

import com.jenetics.smocker.dao.DaoManager;
import com.jenetics.smocker.dao.DaoManagerByModel;
import com.jenetics.smocker.dao.IDaoManager;
import com.jenetics.smocker.model.EntityWithId;
import com.jenetics.smocker.ui.SmockerUI;

public class InjectorProducer {
	/**
	 * @param injectionPoint
	 * @return logger
	 */
	@Produces
	public Logger produceLogger(InjectionPoint injectionPoint) {
		return Logger.getLogger(injectionPoint.getMember().getDeclaringClass().getName());
	}

	@Produces
	@Named
	@Dao
	public <T extends EntityWithId> IDaoManager<T> produceDaoManager(InjectionPoint injectionPoint) {
		final ParameterizedType parameterizedType = (ParameterizedType) injectionPoint.getType();
		final Class<T> genericTypeClass = (Class<T>) parameterizedType.getActualTypeArguments()[0];
		return DaoManagerByModel.getDaoManager(genericTypeClass);
	}

	@Produces
	@Named
	@BundleUI
	public ResourceBundle produceResourceBundle(InjectionPoint injectionPoint) {
		return ResourceBundle.getBundle("BundleUI");
	}

}