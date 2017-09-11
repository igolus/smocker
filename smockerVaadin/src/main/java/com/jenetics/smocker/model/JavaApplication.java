package com.jenetics.smocker.model;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Version;
import com.jenetics.smocker.model.Connection;
import java.util.Set;
import java.util.HashSet;
import javax.persistence.OneToMany;

@Entity
public class JavaApplication implements EntityWithId {

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
	private Set<Connection> connections = new HashSet<>();

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
		if (!(obj instanceof JavaApplication)) {
			return false;
		}
		JavaApplication other = (JavaApplication) obj;
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

	public Set<Connection> getConnections() {
		return this.connections;
	}

	public void setConnections(final Set<Connection> connections) {
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