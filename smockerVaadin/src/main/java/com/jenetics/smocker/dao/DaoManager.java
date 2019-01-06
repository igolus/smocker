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

	//private static Object lock = new Object();

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
		DaoSingletonLock.lock();
		T entity = entityManager.find(typeParameterClass, id);
		DaoSingletonLock.unlock();
		return entity;

	}

	@Override
	public T create(T entity) {
		DaoSingletonLock.lock();
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
		finally {
			DaoSingletonLock.unlock();
		}
		return entity;
	}

	@Override
	public T update(T entity) {
		DaoSingletonLock.lock();
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
		finally {
			DaoSingletonLock.unlock();
		}
		return entity;
	}

	@Override
	public void delete(T entity) {
		DaoSingletonLock.lock();
		EntityTransaction entityTransaction = entityManager.getTransaction();
		if(!entityTransaction.isActive()) {
			entityTransaction.begin();
		}
		try {
			T lastEntity = findById(entity.getId());
			entity.setVersion(lastEntity.getVersion());
			entityManager.remove(lastEntity);
			entityTransaction.commit();
		} catch (Exception ex) {
			logger.error("Unable to delete entity", ex);
			entityTransaction.rollback();
		}
		finally {
			DaoSingletonLock.unlock();
		}
	}


	@Override
	public List<T> listAll(Integer startPosition, Integer maxResult) {
		DaoSingletonLock.lock();
		String entityName = typeParameterClass.getSimpleName();
		Query query = entityManager.createQuery("SELECT e FROM " + entityName + " e");
		List result = query.getResultList();
		DaoSingletonLock.unlock();
		return result;
	}

	@Override
	public List<T> listAll() {
		return listAll(0, -1);
	}

	@Override
	public void deleteById(Long id) {
		DaoSingletonLock.lock();
		T entity = findById(id);
		if (entity != null) {
			EntityTransaction entityTransaction = entityManager.getTransaction();
			entityTransaction.begin();
			try {
				entityManager.remove(entity);
				entityTransaction.commit();
			} catch (Exception ex) {
				logger.error("Unable to delete entity", ex);
				entityTransaction.rollback();
			}
			finally {
				DaoSingletonLock.unlock();
			}
		}
	}

	@Override
	public void deleteAll() {
		DaoSingletonLock.lock();
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
		finally {
			DaoSingletonLock.unlock();
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
