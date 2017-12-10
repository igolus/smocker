package com.jenetics.smocker.ui.util;

import org.vaadin.easyapp.util.VisitableView;

import com.jenetics.smocker.model.EntityWithId;

/**
 * View that can be refreshed
 * 
 * @author igolus
 *
 */
public interface RefreshableView extends VisitableView {

	void refresh(EntityWithId entityWithId);

}
