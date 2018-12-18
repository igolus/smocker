package com.jenetics.smocker.model;

import java.util.HashSet;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.Version;

@Entity
public class Scenario implements EntityWithId {

	private static final long serialVersionUID = 1L;
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "id", updatable = false, nullable = false)
	private Long id;
	@Version
	@Column(name = "version")
	private int version;

	@OneToMany(cascade = CascadeType.ALL, orphanRemoval = true, mappedBy = "scenario")
	private Set<CommunicationMocked> communicationsMocked = new HashSet<>();
	
	@Column(nullable = false)
	private String host;
	
	@Column(nullable = false)
	private Integer port;
	
	@Column(nullable = false)
	private String classQualifiedName;
	
	@Column
	private String name;

	@Override
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
		if (!(obj instanceof Scenario)) {
			return false;
		}
		Scenario other = (Scenario) obj;
		if (id != null && !id.equals(other.id)) {
			return false;
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

	public void setCommunicationsMocked(final Set<CommunicationMocked> communicationsMocked) {
		this.communicationsMocked = communicationsMocked;
	}
	
	public Set<CommunicationMocked> getCommunicationsMocked() {
		return this.communicationsMocked;
	}
	
//	public ConnectionMocked getConnectionMocked() {
//		return connectionMocked;
//	}
//
//	public void setConnectionMocked(ConnectionMocked connectionMocked) {
//		this.connectionMocked = connectionMocked;
//	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

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

	public String getClassQualifiedName() {
		return classQualifiedName;
	}

	public void setClassQualifiedName(String classQualifiedName) {
		this.classQualifiedName = classQualifiedName;
	}

	@Override
	public String toString() {
		String result = getClass().getSimpleName() + " ";
		if (id != null)
			result += "id: " + id;
		result += ", version: " + version;
		if (name != null && !name.trim().isEmpty())
			result += ", name: " + name;
		return result;
	}

}