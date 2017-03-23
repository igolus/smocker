package com.jenetics.smocker.dao;

import java.io.Serializable;

import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;
import javax.transaction.Transactional;

public class DaoManager<T extends Serializable> implements IDaoManager<T> {
	
	Class<T> typeParameterClass = null;

    public DaoManager(Class<T> typeParameterClass) {
        this.typeParameterClass = typeParameterClass;
    }
    
    public DaoManager() {
		super();
	}



	private EntityManager em;
	
	@Override
	public EntityManager getEm() {
		return em;
	}

	@Override
	public void setEm(EntityManager em) {
		this.em = em;
	}

	@Override
	public T findById(final Long id) {
		return (T) em.find(typeParameterClass, id);
	}

	@Override
	public T create(T entity) {
		// TODO Auto-generated method stub
		EntityTransaction entityTransaction = em.getTransaction();
        entityTransaction.begin();
		em.persist(entity);
		entityTransaction.commit();
		return entity;
	}
	
}
