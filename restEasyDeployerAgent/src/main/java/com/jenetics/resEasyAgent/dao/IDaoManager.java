package com.jenetics.resEasyAgent.dao;

import java.io.Serializable;

public interface IDaoManager<T extends Serializable> {

	T findById(final Long id);

	T create(T entity);

}
