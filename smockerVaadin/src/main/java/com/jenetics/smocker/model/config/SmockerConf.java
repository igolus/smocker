package com.jenetics.smocker.model.config;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;

import java.util.HashSet;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Version;

import com.jenetics.smocker.model.Connection;
import com.jenetics.smocker.model.EntityWithId;

@Entity
public class SmockerConf implements EntityWithId {

	private static final long serialVersionUID = 1L;
	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	@Column(name = "id", updatable = false, nullable = false)
	private Long id;
	@Version
	@Column(name = "version")
	private int version;

	@Column
	private boolean autorefesh = false;

	public boolean isAutorefesh() {
		return autorefesh;
	}

	public void setAutorefesh(boolean autorefesh) {
		this.autorefesh = autorefesh;
	}

	@Column(columnDefinition = "TEXT")
	private String globalJsFunction;

	@Column(columnDefinition = "TEXT")
	private String filterJsFunction;

	@Column(columnDefinition = "TEXT")
	private String formatDisplayJsFunction;
	
	@Column(columnDefinition = "TEXT")
	private String traceFunctionJsFunction;

	@Column(columnDefinition = "TEXT")
	private String defaultMockFunction;

	@Column(columnDefinition = "TEXT")
	private String duplicateHosts;

	@Column(columnDefinition = "TEXT")
	private String excludedHosts;

	@Column(columnDefinition = "TEXT")
	private String includedHosts;

	public String getExcludedHosts() {
		return excludedHosts;
	}

	public void setExcludedHosts(String excludedHosts) {
		this.excludedHosts = excludedHosts;
	}

	public String getIncludedHosts() {
		return includedHosts;
	}

	public void setIncludedHosts(String includedHosts) {
		this.includedHosts = includedHosts;
	}

	public String getDuplicateHosts() {
		return duplicateHosts;
	}

	public void setDuplicateHosts(String duplicateHosts) {
		this.duplicateHosts = duplicateHosts;
	}

	public String getGlobalJsFunction() {
		return globalJsFunction;
	}

	public void setGlobalJsFunction(String globalJsFunction) {
		this.globalJsFunction = globalJsFunction;
	}

	public String getFilterJsFunction() {
		return filterJsFunction;
	}

	public void setFilterJsFunction(String filterJsFunction) {
		this.filterJsFunction = filterJsFunction;
	}

	public String getFormatDisplayJsFunction() {
		return this.formatDisplayJsFunction;
	}

	public void setFormatDisplayJsFunction(String formatDisplayJsFunction) {
		this.formatDisplayJsFunction = formatDisplayJsFunction;
	}

	public String getDefaultMockFunction() {
		return defaultMockFunction;
	}

	public void setDefaultMockFunction(String defaultMockFunction) {
		this.defaultMockFunction = defaultMockFunction;
	}

	public String getTraceFunctionJsFunction() {
		return traceFunctionJsFunction;
	}

	public void setTraceFunctionJsFunction(String traceFunctionJsFunction) {
		this.traceFunctionJsFunction = traceFunctionJsFunction;
	}
	
	public Long getId() {
		return this.id;
	}

	public void setId(final Long id) {
		this.id = id;
	}

	public int getVersion() {
		return this.version;
	}

	public void setVersion(final int version) {
		this.version = version;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (!(obj instanceof SmockerConf)) {
			return false;
		}
		SmockerConf other = (SmockerConf) obj;
		if (id != null) {
			if (!id.equals(other.id)) {
				return false;
			}
		}
		return true;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((id == null) ? 0 : id.hashCode());
		return result;
	}


	@Override
	public String toString() {
		String result = getClass().getSimpleName() + " ";
		if (id != null)
			result += "id: " + id;
		result += ", version: " + version;
		return result;
	}
}