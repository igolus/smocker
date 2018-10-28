package com.jenetics.smocker.dao;

import java.io.Serializable;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;
import javax.persistence.Query;

import org.jboss.logging.Logger;

import com.jenetics.smocker.model.EntityWithId;

public class DaoManager<T extends EntityWithId> implements IDaoManager<T> {

	private Logger logger = Logger.getLogger(DaoManager.class);
	
	private static Object lock = new Object();
	
	private EntityManager entityManager;

	Class<T> typeParameterClass = null;

	DaoManager(Class<T> typeParameterClass, EntityManager em) {
		this.typeParameterClass = typeParameterClass;
		this.entityManager = em;
	}

	@Override
	public void setEm(EntityManager em) {
		this.entityManager = em;
	}

	@Override
	public T findById(final Long id) {
		synchronized (lock) {
			return entityManager.find(typeParameterClass, id);
		}		
	}

	@Override
	public T create(T entity) {
		synchronized (lock) {
			EntityTransaction entityTransaction = entityManager.getTransaction();
			if(!entityTransaction.isActive()) {
				entityTransaction.begin();
			}
			try {
				entityManager.persist(entity);
				entityTransaction.commit();
			} catch (Exception ex) {
				logger.error("Unable to persist entity", ex);
				entityTransaction.rollback();
			}
			return entity;
		}
	}

	@Override
	public T update(T entity) {
		synchronized (lock) {
			EntityTransaction entityTransaction = entityManager.getTransaction();
			if(!entityTransaction.isActive()) {
				entityTransaction.begin();
			}
			try {
				T lastEntity = findById(entity.getId());
				entity.setVersion(lastEntity.getVersion());
				entityManager.merge(entity);
				entityTransaction.commit();
				
			} catch (Exception ex) {
				logger.error("Unable to persist entity", ex);
				entityTransaction.rollback();
			}

			return entity;
		}
	}

	@Override
	public List<T> listAll(Integer startPosition, Integer maxResult) {
		synchronized (lock) {
			String entityName = typeParameterClass.getSimpleName();
			Query query = entityManager.createQuery("SELECT e FROM " + entityName + " e");
			return query.getResultList();
		}
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
		} catch (Exception ex) {
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
		} catch (Exception ex) {
			logger.error("Unable to delete entity", ex);
			entityTransaction.rollback();
		}
	}
	
	@Override
	public List<T> queryList(String querySql) {
		String entityName = typeParameterClass.getSimpleName();
		Query query = entityManager.createQuery(querySql);
		EntityTransaction entityTransaction = entityManager.getTransaction();
		return query.getResultList();
	}

}
