package com.jenetics.smocker.ui.util;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

public class DuplicateHost {
	private static final String SEP_GROUP = "_";
	private static final String SEP_HOST = ";";
	private List<String> listDupHost = new LinkedList<>();
	
	public DuplicateHost(String mainHost) {
		listDupHost.add(mainHost);
	}
	
	public DuplicateHost() {
		super();
	}
	
	public void addHost(String host) {
		listDupHost.add(host);
	}
	
	public void removeHost(String host) {
		listDupHost.remove(host);
	}
	
	public List<String> getListDupHost() {
		return listDupHost;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		String[] listHost = new String[listDupHost.size()];
		listDupHost.toArray(listHost);
		for (int i = 0; i < listHost.length; i++) {
			sb.append(listHost[i]);
			if (i < listHost.length - 1) {
				sb.append(SEP_HOST);
			}
		}
		return sb.toString();
	}
	
	public static String getDbValueFromList(List<DuplicateHost> dupHosts) {
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < dupHosts.size(); i++) {
			DuplicateHost duplicateHost = dupHosts.get(i);
			for (int j = 0; j < duplicateHost.getListDupHost().size(); j++) {
				String host = duplicateHost.getListDupHost().get(j);
				sb.append(host);
				if (j < duplicateHost.getListDupHost().size() - 1) {
					sb.append(SEP_HOST);
				}
			}
			if (i < dupHosts.size() - 1) {
				sb.append(SEP_GROUP);
			}
		}
		return sb.toString();
	}
	
	public static List<DuplicateHost> getListFromDbValue(String dbValue) {
		List<DuplicateHost> ret = new ArrayList<>();
		if (dbValue == null) {
			return ret; 
		}
		String[] groupHosts = dbValue.split(SEP_GROUP);
		for (String groupHost : groupHosts) {
			DuplicateHost duplicateHost = new DuplicateHost();
			String[] hosts = groupHost.split(SEP_HOST);
			for (String host : hosts) {
				duplicateHost.addHost(host);
			}
			ret.add(duplicateHost);
		}
		return ret;
	}
	
	
	
}
