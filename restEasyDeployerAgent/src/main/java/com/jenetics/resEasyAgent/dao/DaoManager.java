package com.jenetics.resEasyAgent.dao;

import java.io.Serializable;

import javax.enterprise.event.Event;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import com.google.common.reflect.TypeToken;

public class DaoManager<T extends Serializable> implements IDaoManager<T> {
	
	private final TypeToken<T> typeToken = new TypeToken<T>(getClass()) { };
    private final Class clazz = typeToken.getType().getClass(); // or getRawType() to return Class<? super T>

	@PersistenceContext(unitName="smockerLocalData") 
	private EntityManager em;
	
	@Inject
    private Event<T> connectionEventSrc;
	
	@Override
	public T findById(final Long id) {
		return (T) em.find(clazz, id);
	}

	@Override
	public T create(T entity) {
		// TODO Auto-generated method stub
		em.getTransaction().begin();
		em.persist(entity);
		em.getTransaction().commit();
		connectionEventSrc.fire(entity);
		return entity;
	}
	
}
