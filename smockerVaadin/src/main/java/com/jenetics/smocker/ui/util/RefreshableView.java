package com.jenetics.smocker.ui.util;

import com.jenetics.smocker.model.EntityWithId;
import com.vaadin.navigator.View;

/**
 * View that can be refreshed
 * @author igolus
 *
 */
public interface RefreshableView extends View {

	void refresh(EntityWithId entityWithId);

}
