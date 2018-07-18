package com.jenetics.smocker.ui.util;

import java.util.Hashtable;

import org.vaadin.easyapp.util.VisitableView;

import com.jenetics.smocker.model.EntityWithId;
import com.vaadin.ui.Component;
import com.vaadin.ui.TabSheet;
import com.vaadin.ui.TabSheet.SelectedTabChangeEvent;
import com.vaadin.ui.TabSheet.Tab;

/**
 * View that can be refreshed
 * 
 * @author igolus
 *
 */
public interface RefreshableView {

	Hashtable<String, Tab> tabByConnectionKey = new Hashtable<>();

	void refresh(EntityWithId entityWithId);

	boolean always();

	void tabChanged(SelectedTabChangeEvent event);

	void tabClosed(TabSheet tabsheet, Component tabContent);

}
