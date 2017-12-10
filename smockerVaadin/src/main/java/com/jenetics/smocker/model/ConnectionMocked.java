package com.jenetics.smocker.model;

import java.util.HashSet;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.Version;

import com.fasterxml.jackson.annotation.JsonIgnore;

@Entity
public class ConnectionMocked implements EntityWithId {

	private static final long serialVersionUID = 1L;
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "id", updatable = false, nullable = false)
	private Long id;
	@Version
	@Column(name = "version")
	private int version;

	@Column(nullable = false)
	private String host;

	@Column(nullable = false)
	private Integer port;

	@OneToMany(cascade = CascadeType.ALL, orphanRemoval = true, mappedBy = "connection")
	private Set<CommunicationMocked> communications = new HashSet<>();

	@JoinColumn(nullable = false)
	@OneToOne
	@JsonIgnore
	private JavaApplicationMocked javaApplication;

	@Column
	private Boolean watched = true;

	public JavaApplicationMocked getJavaApplication() {
		return javaApplication;
	}

	public void setJavaApplication(JavaApplicationMocked javaApplication) {
		this.javaApplication = javaApplication;
	}
	
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
		if (!(obj instanceof ConnectionMocked)) {
			return false;
		}
		ConnectionMocked other = (ConnectionMocked) obj;
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

	public Set<CommunicationMocked> getCommunications() {
		return this.communications;
	}

	public void setCommunications(final Set<CommunicationMocked> communications) {
		this.communications = communications;
	}

	public Boolean getWatched() {
		return watched;
	}

	public void setWatched(Boolean watched) {
		this.watched = watched;
	}

	@Override
	public String toString() {
		String result = getClass().getSimpleName() + " ";
		if (id != null)
			result += "id: " + id;
		result += ", version: " + version;
		if (host != null && !host.trim().isEmpty())
			result += ", host: " + host;
		if (port != null)
			result += ", port: " + port;
		if (communications != null)
			result += ", Communications: " + communications;
		if (javaApplication != null)
			result += ", javaApplication: " + javaApplication.getId();
		if (watched != null)
			result += ", watched: " + watched;
		return result;
	}
}