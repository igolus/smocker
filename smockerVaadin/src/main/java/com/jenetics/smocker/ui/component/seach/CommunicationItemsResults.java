package com.jenetics.smocker.ui.component.seach;

import java.util.List;

import com.jenetics.smocker.model.Communication;
import com.jenetics.smocker.ui.SmockerUI;
import com.jenetics.smocker.util.NetworkReaderUtility;
import com.vaadin.ui.Button;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.VerticalLayout;

public class CommunicationItemsResults extends VerticalLayout {

	private String searchQuery;

	public CommunicationItemsResults(List<Communication> foundComms, String searchQuery) {
		this.searchQuery = searchQuery;
		foundComms.stream().forEach(this::addFoundComm);
	}
	
	public void addFoundComm(Communication comm) {
		HorizontalLayout layout = new HorizontalLayout();
		String decodedReq = NetworkReaderUtility.decode(comm.getRequest()).trim();
		int indexInReq = decodedReq.indexOf(searchQuery);
		int indexInResp = NetworkReaderUtility.decode(comm.getResponse()).indexOf(searchQuery);
		
		if (indexInReq != -1) {
			String extract = extract (indexInReq, decodedReq);
			layout.addComponent(new Label(extract));
		}
		
		layout.addComponent(new Label("Date : " + comm.getDateTime()));
		layout.addComponent(new Button(SmockerUI.getBundleValue("select")));
		layout.addStyleName("layout-with-border");
		addComponent(layout);
	}

	private String extract(int indexInReq, String request) {
		int startindex = Math.max(0, indexInReq - 10);
		int endIndex = Math.min(request.length(), indexInReq + searchQuery.length() + 5);
		StringBuilder builder = new StringBuilder();
		if (startindex != 0) {
			builder.append("...");
		}
		builder.append(request.substring(startindex, endIndex));
		if (endIndex + searchQuery.length() + 1 != request.length()) {
			builder.append("...");
		}
		return builder.toString();
	}
}
