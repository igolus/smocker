package com.jenetics.smocker.dao;

import java.io.Serializable;

import javax.persistence.EntityManager;

public interface IDaoManager<T extends Serializable> {

	T findById(final Long id);

	T create(T entity);

	void setEm(EntityManager em);

	EntityManager getEm();

}
