package com.jenetics.smocker.dao;

import java.io.Serializable;
import java.util.List;

import javax.persistence.EntityManager;

public interface IDaoManager<T extends Serializable> {

	T findById(final Long id);

	T create(T entity);
	
	void setEm(EntityManager em);

	EntityManager getEm();

	List<T> listAll(Integer startPosition, Integer maxResult);

	void update(Long id, T entity);

	void deleteById(Long id);

}
