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
import javax.persistence.Version;

@Entity
public class JavaApplicationMocked implements EntityWithId {

	private static final long serialVersionUID = 1L;
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "id", updatable = false, nullable = false)
	private Long id;
	@Version
	@Column(name = "version")
	private int version;

	@Column(length = 1024)
	private String classQualifiedName;

	@OneToMany(cascade = CascadeType.ALL, orphanRemoval = true, mappedBy = "javaApplication")
	private Set<ConnectionMocked> connections = new HashSet<>();

	@Column
	private int sourcePort;

	@Column
	private String sourceHost;

	@Column
	private String sourceIp;

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
		if (!(obj instanceof JavaApplicationMocked)) {
			return false;
		}
		JavaApplicationMocked other = (JavaApplicationMocked) obj;
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

	public String getClassQualifiedName() {
		return classQualifiedName;
	}

	public void setClassQualifiedName(String classQualifiedName) {
		this.classQualifiedName = classQualifiedName;
	}

	public Set<ConnectionMocked> getConnections() {
		return this.connections;
	}

	public void setConnections(final Set<ConnectionMocked> connections) {
		this.connections = connections;
	}

	public int getSourcePort() {
		return sourcePort;
	}

	public void setSourcePort(int sourcePort) {
		this.sourcePort = sourcePort;
	}

	public String getSourceHost() {
		return sourceHost;
	}

	public void setSourceHost(String sourceHost) {
		this.sourceHost = sourceHost;
	}

	public String getSourceIp() {
		return sourceIp;
	}

	public void setSourceIp(String ip) {
		this.sourceIp = ip;
	}

	@Override
	public String toString() {
		String result = getClass().getSimpleName() + " ";
		if (id != null)
			result += "id: " + id;
		result += ", version: " + version;
		if (classQualifiedName != null && !classQualifiedName.trim().isEmpty())
			result += ", classQualifiedName: " + classQualifiedName;
		if (connections != null)
			result += ", Connections: " + connections;
		if (sourceHost != null && !sourceHost.trim().isEmpty())
			result += ", sourceHost: " + sourceHost;
		if (sourceIp != null && !sourceIp.trim().isEmpty())
			result += ", ip: " + sourceIp;
		return result;
	}

}