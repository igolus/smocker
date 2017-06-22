package com.jenetics.smocker.model;

import javax.persistence.Entity;
import java.io.Serializable;
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

	@OneToMany(cascade = CascadeType.PERSIST, mappedBy = "javaApplication")
	private Set<Connection> Connections = new HashSet<Connection>();

	@Column
	private String Host;

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

	public String getClassQualifiedName() {
		return classQualifiedName;
	}

	public void setClassQualifiedName(String classQualifiedName) {
		this.classQualifiedName = classQualifiedName;
	}

	public Set<Connection> getConnections() {
		return this.Connections;
	}

	public void setConnections(final Set<Connection> Connections) {
		this.Connections = Connections;
	}

	public String getHost() {
		return Host;
	}

	public void setHost(String Host) {
		this.Host = Host;
	}

	@Override
	public String toString() {
		String result = getClass().getSimpleName() + " ";
		if (id != null)
			result += "id: " + id;
		result += ", version: " + version;
		if (classQualifiedName != null && !classQualifiedName.trim().isEmpty())
			result += ", classQualifiedName: " + classQualifiedName;
		if (Connections != null)
			result += ", Connections: " + Connections;
		if (Host != null && !Host.trim().isEmpty())
			result += ", Host: " + Host;
		return result;
	}

}