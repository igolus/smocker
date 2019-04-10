package com.jenetics.smocker.ui.component.seach;

import java.util.List;
import java.util.function.Consumer;

import com.jenetics.smocker.model.Communication;
import com.jenetics.smocker.ui.SmockerUI;
import com.jenetics.smocker.ui.util.ButtonWithIEntity;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.GridLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;

public class CommunicationItemsResults extends VerticalLayout {

	private String searchQuery;
	private transient Consumer<Communication> selected;
	private Window searchWindow;
	private transient Runnable callBack;

	public CommunicationItemsResults(Runnable callBack, List<Communication> foundComms, String searchQuery, 
			Consumer<Communication> selected) {
		this.searchQuery = searchQuery;
		this.selected = selected;
		this.callBack = callBack;
		foundComms.stream().forEach(this::addFoundComm);
	}

	public void addFoundComm(Communication comm) {
		GridLayout grid = new GridLayout(2, 3);
		grid.setSizeFull();
		grid.addComponent(new Label("Date : " + comm.getDateTime()), 0, 1);
		ButtonWithIEntity<Communication> buttonSelect = new ButtonWithIEntity<>(comm);
		buttonSelect.setCaption(SmockerUI.getBundleValue("select"));
		buttonSelect.addClickListener( clickEvent -> {
			ButtonWithIEntity<Communication> buttonWithComm = 
					(ButtonWithIEntity<Communication>)clickEvent.getButton();
			if (searchWindow != null) {
				searchWindow.close();
			}
			selected.accept(buttonWithComm.getEntity()); 
			callBack.run();
		});
		grid.addComponent(buttonSelect, 1, 0, 1, 2);
		grid.setComponentAlignment(buttonSelect, Alignment.MIDDLE_RIGHT);
		grid.addStyleName("layout-with-border");
		addComponent(grid);
	}

	public void setWindowContainer(Window searchWindow) {
		this.searchWindow = searchWindow;
	}
}
