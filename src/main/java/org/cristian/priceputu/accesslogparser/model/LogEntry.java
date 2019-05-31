package org.cristian.priceputu.accesslogparser.model;

import java.util.Date;

public class LogEntry {
	private Date ts;
	private String ip;
	private String method;
	private String response;
	private String userAgent;

	public LogEntry() {
	}

	public Date getTs() {
		return ts;
	}

	public void setTs(Date ts) {
		this.ts = ts;
	}

	public String getIp() {
		return ip;
	}

	public void setIp(String ip) {
		this.ip = ip;
	}

	public String getMethod() {
		return method;
	}

	public void setMethod(String method) {
		this.method = method;
	}

	public String getResponse() {
		return response;
	}

	public void setResponse(String response) {
		this.response = response;
	}

	public String getUserAgent() {
		return userAgent;
	}

	public void setUserAgent(String userAgent) {
		this.userAgent = userAgent;
	}

	@Override
	public String toString() {
		return "LogEntry [date=" + ts + ", ip=" + ip + ", method=" + method + ", response=" + response + ", userAgent=" + userAgent + "]";
	}

}
