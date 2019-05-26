package com.jenetics.smocker.model.config;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Column;
import javax.persistence.Version;

import com.jenetics.smocker.model.EntityWithId;

@Entity
public class JsFilterAndDisplay implements EntityWithId {

	private static final long serialVersionUID = 1L;
	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	@Column(name = "id", updatable = false, nullable = false)
	private Long id;
	@Version
	@Column(name = "version")
	private int version;
	
	@Column(nullable = false)
	private String host;

	@Column(nullable = false)
	private Integer port;
	
	@Column(nullable = true)
	private String functionFilter;
	
	@Column(nullable = true)
	private String functionInputDisplay;	
	
	@Column(nullable = true)
	private String functionOutputDisplay;
	
	@Column(nullable = true)
	private String functionMockOutputDisplay;	
	
	@Column(nullable = true)
	private String functionMockInputDisplay;
	
	@Column(nullable = true)
	private String functionTrace;	
	
	public String getHost() {
		return host;
	}

	public void setHost(String host) {
		this.host = host;
	}

	public Integer getPort() {
		return port;
	}

	public void setPort(Integer port) {
		this.port = port;
	}

	public String getFunctionFilter() {
		return functionFilter;
	}

	public void setFunctionFilter(String functionFilter) {
		this.functionFilter = functionFilter;
	}

	public String getFunctionInputDisplay() {
		return functionInputDisplay;
	}

	public void setFunctionInputDisplay(String functionInputDisplay) {
		this.functionInputDisplay = functionInputDisplay;
	}

	public String getFunctionOutputDisplay() {
		return functionOutputDisplay;
	}

	public void setFunctionOutputDisplay(String functionOutputDisplay) {
		this.functionOutputDisplay = functionOutputDisplay;
	}

	public String getFunctionMockOutputDisplay() {
		return functionMockOutputDisplay;
	}

	public void setFunctionMockOutputDisplay(String functionMockOutputDisplay) {
		this.functionMockOutputDisplay = functionMockOutputDisplay;
	}

	public String getFunctionMockInputDisplay() {
		return functionMockInputDisplay;
	}

	public void setFunctionMockInputDisplay(String functionMockInputDisplay) {
		this.functionMockInputDisplay = functionMockInputDisplay;
	}

	public String getFunctionTrace() {
		return functionTrace;
	}

	public void setFunctionTrace(String functionTrace) {
		this.functionTrace = functionTrace;
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
		if (!(obj instanceof JsFilterAndDisplay)) {
			return false;
		}
		JsFilterAndDisplay other = (JsFilterAndDisplay) obj;
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