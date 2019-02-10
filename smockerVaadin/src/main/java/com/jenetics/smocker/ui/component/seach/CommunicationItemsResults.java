package com.jenetics.smocker.ui.component.seach;

import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

import com.jenetics.smocker.model.Communication;
import com.jenetics.smocker.ui.SmockerUI;
import com.jenetics.smocker.ui.util.ButtonWithIEntity;
import com.jenetics.smocker.util.NetworkReaderUtility;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.GridLayout;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;

public class CommunicationItemsResults extends VerticalLayout {

	private String searchQuery;
	private transient Consumer<Communication> selected;
	private Window searchWindow;
	private Runnable callBack;

	public CommunicationItemsResults(Runnable callBack, List<Communication> foundComms, String searchQuery, 
			Consumer<Communication> selected) {
		this.searchQuery = searchQuery;
		this.selected = selected;
		this.callBack = callBack;
		foundComms.stream().forEach(this::addFoundComm);
	}

	public void addFoundComm(Communication comm) {
		HorizontalLayout layout = new HorizontalLayout();

		GridLayout grid = new GridLayout(2, 3);
		grid.setSizeFull();

		String decodedReq = NetworkReaderUtility.decode(comm.getRequest()).trim();
		String decodedResp = NetworkReaderUtility.decode(comm.getRequest()).trim();
		int indexInReq = decodedReq.indexOf(searchQuery);
		int indexInResp = NetworkReaderUtility.decode(comm.getResponse()).indexOf(searchQuery);

		Label labelFound = null;
		if (indexInReq != -1) {
			String extract = extract (indexInReq, decodedReq);
			labelFound = new Label(extract);
			layout.addComponent(new Label(extract));
		}
		else if (indexInResp != -1) {
			String extract = extract (indexInResp, decodedResp);
			labelFound = new Label(extract);
		}
		if (labelFound != null) {
			grid.addComponent(labelFound, 0, 0);
		}

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

	private String extract(int index, String text) {
		int startindex = Math.max(0, index - 10);
		int endIndex = Math.min(text.length(), index + searchQuery.length() + 5);
		StringBuilder builder = new StringBuilder();
		if (startindex != 0) {
			builder.append("...");
		}
		builder.append(text.substring(startindex, endIndex));
		if (endIndex + searchQuery.length() + 1 != text.length()) {
			builder.append("...");
		}
		return builder.toString();
	}

	public void setWindowContainer(Window searchWindow) {
		this.searchWindow = searchWindow;
	}
}
