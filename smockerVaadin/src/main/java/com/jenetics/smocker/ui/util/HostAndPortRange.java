package com.jenetics.smocker.ui.util;

import com.jenetics.smocker.ui.component.HostAndPortEditor;

public class HostAndPortRange {
	private static final String SEP = " ";
	private String host;
	private String portRange;
	private HostAndPortEditor source;

	public HostAndPortRange(String host, String portRange) {
		super();
		this.host = host;
		this.portRange = portRange;
	}
	
	public void setSource(HostAndPortEditor source) {
		this.source = source;
	}

	public HostAndPortEditor getSource() {
		return source;
	}


	public String getHost() {
		return host;
	}

	public void setHost(String host) {
		this.host = host;
	}

	public String getPortRange() {
		return portRange;
	}

	public void setPortRange(String portRange) {
		this.portRange = portRange;
	}

	@Override
	public String toString() {
		return host + SEP + portRange;
	}

	/**
	 * get the port range 
	 * @param portAndRangeValue
	 * @return null if error single array in case of single port
	 * an array of two integer representing min port and max port
	 * a single array with -1 for all port
	 */
	public static int[] getPortRange(String portAndRangeValue) {
		if (portAndRangeValue.equals("*")) {
			return new int[] {-1};
		}
		try {
			int singlePort = Integer.parseInt(portAndRangeValue);
			return new int[] {singlePort};
		}
		catch (NumberFormatException e) {
			String[] portRange = portAndRangeValue.split("-");
			if (portRange.length != 2) {
				return null;
			}
			
			int minPort=0;
			int maxPort=0;
			
			try {
				minPort = Integer.parseInt(portRange[0]);
			}
			catch (NumberFormatException e1) {
				return null;
			}
			
			try {
				maxPort = Integer.parseInt(portRange[1]);
			}
			catch (NumberFormatException e2) {
				return null;
			}
			
			if (minPort > maxPort) {
				return null;
			}
			
			return new int[] {minPort, maxPort};
		}
		
	}
	
	
	
}
