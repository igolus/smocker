package com.jenetics.smocker.ui.container;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import com.jenetics.smocker.ui.SmockerUI;
import com.vaadin.addon.jpacontainer.JPAContainer;
import com.vaadin.addon.jpacontainer.provider.CachingLocalEntityProvider;

public class GenericJPAContainer<T> extends JPAContainer<T> {

    private final Class<T> parameterClass;
    
    @PersistenceContext(unitName=SmockerUI.PERSISTENCE_UNIT) 
	private EntityManager em;
	
    public GenericJPAContainer(Class<T> parameterClass) {
		super(parameterClass);
		this.parameterClass = parameterClass;
		setEntityProvider(new CachingLocalEntityProvider<T>(parameterClass, em));
        setParentProperty("parent");
	}
}
