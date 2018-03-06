package com.jenetics.smocker.ui.util;

import org.vaadin.crudui.crud.CrudOperation;
import org.vaadin.crudui.crud.impl.GridBasedCrudComponent;

public class SmockerGridBasedCrudComponent<T> extends GridBasedCrudComponent<T> {

	public SmockerGridBasedCrudComponent(Class<T> domainType) {
		super(domainType);
		// TODO Auto-generated constructor stub
	}
	
	@Override
	protected void updateButtonClicked() {
        T domainObject = (T) getGrid().getSelectedRow();
        showForm(CrudOperation.UPDATE, domainObject, false, "Item saved", event -> {
            updateOperation.perform(domainObject);
            //getGrid().select(null);
            refreshGrid();
//            if (container.containsId(updatedObject)) {
//            	getGrid().select(updatedObject);
//            	getGrid().scrollTo(updatedObject);
//            }
        });
    }

}
