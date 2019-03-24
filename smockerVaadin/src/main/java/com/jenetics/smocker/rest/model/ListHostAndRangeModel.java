package com.jenetics.smocker.rest.model;

import java.util.ArrayList;
import java.util.List;

import com.jenetics.smocker.ui.util.HostAndPortRange;

public class ListHostAndRangeModel {
	
	private List<HostAndPortRangeModel> listPortAndRangeList = null;

	public ListHostAndRangeModel(List<HostAndPortRangeModel> listPortAndRangeList) {
		super();
		this.listPortAndRangeList = listPortAndRangeList;
	}
	
	public ListHostAndRangeModel() {
		super();
		this.listPortAndRangeList = new ArrayList<>();
	}
	
	public void addIHostAndPortRangeModel(HostAndPortRangeModel hostAndPortRangeModel) {
		listPortAndRangeList.add(hostAndPortRangeModel);
	}

	public List<HostAndPortRangeModel> getListPortAndRangeList() {
		return listPortAndRangeList;
	}
	
	
}
