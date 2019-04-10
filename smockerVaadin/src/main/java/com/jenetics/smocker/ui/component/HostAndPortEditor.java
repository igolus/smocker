package com.jenetics.smocker.ui.component;

import java.util.function.Consumer;

import com.jenetics.smocker.ui.SmockerUI;
import com.jenetics.smocker.ui.dialog.Dialog;
import com.jenetics.smocker.ui.util.HostAndPortRange;
import com.vaadin.ui.Component;
import com.vaadin.ui.TextField;
import com.vaadin.ui.VerticalLayout;

import de.steinwedel.messagebox.ButtonOption;
import de.steinwedel.messagebox.MessageBox;

public class HostAndPortEditor extends VerticalLayout implements BoxableItem<HostAndPortRange> {
	
	private transient HostAndPortRange hostAndPort;
	private MessageBox sourceBox;
	private TextField textFieldHost;
	private TextField textFieldPortAndRange;
	private transient Consumer<HostAndPortRange> callBack;

	public HostAndPortEditor(HostAndPortRange hostAndPort) {
		super();
		this.hostAndPort = hostAndPort;
		
		buildComponent();
	}

	private void buildComponent() {
		textFieldHost = new TextField(SmockerUI.getBundleValue("Host"));
		textFieldPortAndRange = new TextField(SmockerUI.getBundleValue("Port_Range"));

		addComponent(textFieldHost);
		addComponent(textFieldPortAndRange);
	}

	@Override
	public HostAndPortRange getItem() {
		return hostAndPort;
	}

	@Override
	public Component getComponent() {
		return this;
	}
	
	public void setBox(MessageBox sourceBox) {
		if (this.sourceBox == null) {
			this.sourceBox = sourceBox;
			sourceBox.withOkButton(this::updatehostAndPort, ButtonOption.closeOnClick(false));
			sourceBox.open();
		}
	}
	
	private void updatehostAndPort() {
		String hostValue = textFieldHost.getValue();
		String portAndRangeValue = textFieldPortAndRange.getValue();
		
		if (check()) {
			hostAndPort.setHost(hostValue);
			hostAndPort.setPortRange(portAndRangeValue);
			if (callBack != null) {
				callBack.accept(hostAndPort);
			}
			sourceBox.close();
		}
	}

	public boolean check() {
		String hostValue = textFieldHost.getValue();
		String portAndRangeValue = textFieldPortAndRange.getValue();
		
		if (hostValue.indexOf(' ') != -1) {
			Dialog.warning(SmockerUI.getBundleValue("Bad_JS_Host_Warning"));
			return false;
		}
		
		int[] portRange = HostAndPortRange.getPortRange(portAndRangeValue);
		if (portRange == null) {
			Dialog.warning(SmockerUI.getBundleValue("Bad_JS_PortRange_Warning"));
			return false;
		}
		
		return true;
	}
	
	public void setCallBAckAfterCheck(Consumer<HostAndPortRange> callBack) {
		this.callBack = callBack;
	}
	
	
	
	
}
