package com.jenetics.smocker.dao;

import java.io.Serializable;
import java.util.List;

import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;
import javax.persistence.Query;
import org.jboss.logging.Logger;


public class DaoManager<T extends Serializable> implements IDaoManager<T> {
	
	@Inject
	private Logger logger = Logger.getLogger(DaoManager.class);
	
	private EntityManager entityManager;
	
	Class<T> typeParameterClass = null;

    public DaoManager(Class<T> typeParameterClass, EntityManager em) {
        this.typeParameterClass = typeParameterClass;
        this.entityManager = em;
    }
    
    private DaoManager() {
		super();
	}



	

	@Override
	public void setEm(EntityManager em) {
		this.entityManager = em;
	}

	@Override
	public T findById(final Long id) {
		return entityManager.find(typeParameterClass, id);
	}

	@Override
	public T create(T entity) {
		EntityTransaction entityTransaction = entityManager.getTransaction();
        entityTransaction.begin();
        try {
        	entityManager.persist(entity);
        	entityTransaction.commit();
        }
        catch (Exception ex) {
        	logger.error("Unable to persist entity", ex);
        	entityTransaction.rollback();
        }
		return entity;
	}
	
	@Override
	public T update(T entity) {
		EntityTransaction entityTransaction = entityManager.getTransaction();
        entityTransaction.begin();
        try {
        	entityManager.merge(entity);
        	entityTransaction.commit();
        }
        catch (Exception ex) {
        	logger.error("Unable to persist entity", ex);
        	entityTransaction.rollback();
        }

		return entity;
	}

	@Override
	public List<T> listAll(Integer startPosition, Integer maxResult) {
		String entityName = typeParameterClass.getSimpleName();
		Query query = entityManager.createQuery("SELECT e FROM " + entityName + " e");
	    return query.getResultList();
	}
	
	@Override
	public List<T> listAll() {
		return listAll(0, -1);
	}

	@Override
	public void deleteById(Long id) {
		T entity = findById(id);
		EntityTransaction entityTransaction = entityManager.getTransaction();
		entityTransaction.begin();
		try {
			entityManager.remove(entity);
			entityTransaction.commit();
		}
		catch (Exception ex) {
        	logger.error("Unable to delete entity", ex);
        	entityTransaction.rollback();
        }
	}

	@Override
	public void deleteAll() {
		String entityName = typeParameterClass.getSimpleName();
		Query query = entityManager.createQuery("DELETE FROM " + entityName + " e");	
		EntityTransaction entityTransaction = entityManager.getTransaction();
		entityTransaction.begin();
		try {
			query.executeUpdate();
			entityTransaction.commit();
		}
		catch (Exception ex) {
        	logger.error("Unable to delete entity", ex);
        	entityTransaction.rollback();
        }
	}

	
	
}
