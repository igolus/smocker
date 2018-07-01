package com.jenetics.smocker.dao;

import java.io.Serializable;
import java.util.List;

import javax.persistence.EntityManager;

public interface IDaoManager<T extends Serializable> {

	T findById(final Long id);

	T create(T entity);

	void setEm(EntityManager em);

	List<T> listAll(Integer startPosition, Integer maxResult);

	List<T> listAll();

	void deleteById(Long id);

	T update(T entity);

	void deleteAll();

}
