package com.jenetics.smocker.model;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Version;

import com.fasterxml.jackson.annotation.JsonIgnore;

@Entity
public class CommunicationMocked implements EntityWithId {

	private static final long serialVersionUID = 1L;
	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	@Column(name = "id", updatable = false, nullable = false)
	private Long id;
	@Version
	@Column(name = "version")
	private int version;

	@Column(columnDefinition = "TEXT")
	private String request;

	@Column(columnDefinition = "TEXT")
	private String response;

	@JoinColumn(nullable = false)
	@OneToOne
	@JsonIgnore
	private ConnectionMocked connection;

	@Column
	@Temporal(TemporalType.TIME)
	private Date dateTime;

	@Column(columnDefinition = "TEXT")
	private String callerStack;

	public ConnectionMocked getConnection() {
		return connection;
	}

	public void setConnection(ConnectionMocked connection) {
		this.connection = connection;
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
		if (!super.equals(obj)) {
			return false;
		}
		if (!(obj instanceof CommunicationMocked)) {
			return false;
		}
		CommunicationMocked other = (CommunicationMocked) obj;
		if (id != null && !id.equals(other.id)) {
			return false;
		}
		return true;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((id == null) ? 0 : id.hashCode());
		return result;
	}

	public String getRequest() {
		return request;
	}

	public void setRequest(String request) {
		this.request = request;
	}

	public String getResponse() {
		return response;
	}

	public void setResponse(String response) {
		this.response = response;
	}

	public Date getDateTime() {
		return dateTime;
	}

	public void setDateTime(Date dateTime) {
		this.dateTime = dateTime;
	}

	public String getCallerStack() {
		return callerStack;
	}

	public void setCallerStack(String callerStack) {
		this.callerStack = callerStack;
	}

	@Override
	public String toString() {
		String result = getClass().getSimpleName() + " ";
		if (id != null)
			result += "id: " + id;
		result += ", version: " + version;
		if (request != null && !request.trim().isEmpty())
			result += ", request: " + request;
		if (response != null && !response.trim().isEmpty())
			result += ", response: " + response;
		if (connection != null)
			result += ", connection: " + connection.getId();
		if (dateTime != null)
			result += ", dateTime: " + dateTime;
		if (callerStack != null && !callerStack.trim().isEmpty())
			result += ", CallerStack: " + callerStack;
		return result;
	}
}