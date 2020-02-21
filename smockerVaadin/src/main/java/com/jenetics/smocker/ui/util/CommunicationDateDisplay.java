package com.jenetics.smocker.ui.util;

import java.util.logging.Level;

import com.jenetics.smocker.dao.DaoConfig;
import com.jenetics.smocker.jseval.JSEvaluator;
import com.jenetics.smocker.model.Communication;
import com.jenetics.smocker.model.config.JsFilterAndDisplay;
import com.jenetics.smocker.ui.SmockerUI;
import com.jenetics.smocker.ui.component.LoggerPanel;
import com.jenetics.smocker.util.NetworkReaderUtility;
import com.jenetics.smocker.util.SmockerException;

public class CommunicationDateDisplay {
	private Communication communication;
	private String name;

	public CommunicationDateDisplay(Communication communication) {
		super();
		this.communication = communication;
		
		JsFilterAndDisplay first = DaoConfig.findJsDisplayAndFilter(communication.getConnection());
		if (first != null && first.getFunctionComName() != null && !first.getFunctionComName().isEmpty()) {
				SmockerUI.doInBackGround(() -> {
					try {
						this.name = JSEvaluator.commName(first.getFunctionComName(), 
							NetworkReaderUtility.decode(communication.getRequest()));
					} catch (Exception e) {
						SmockerUI.log(Level.WARNING, "Unable to execute comm name function ");
					}
				}, 
				10);
		}
	}

	@Override
	public String toString() {
		return (name == null ? "" : name + " ") + communication.getDateTime().toString();
	}

	public Communication getCommunication() {
		return communication;
	}
}
