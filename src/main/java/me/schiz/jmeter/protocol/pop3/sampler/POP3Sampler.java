/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * 
 */
package me.schiz.jmeter.protocol.pop3.sampler;

import me.schiz.jmeter.protocol.SessionStorage;
import org.apache.commons.net.SocketClient;
import org.apache.commons.net.pop3.POP3Client;
import org.apache.commons.net.pop3.POP3Reply;
import org.apache.commons.net.pop3.POP3SClient;
import org.apache.jmeter.samplers.AbstractSampler;
import org.apache.jmeter.samplers.Entry;
import org.apache.jmeter.samplers.SampleResult;
import org.apache.jorphan.logging.LoggingManager;
import org.apache.log.Logger;

import java.io.IOException;
import java.net.SocketException;
import java.util.LinkedList;

/**
 * @author Epikhin Mikhail (epikhinm@gmail.com)
 * @version 0.1
 * low-level POP3 Sampler based on Apache Commons Net
 */
public class POP3Sampler extends AbstractSampler{
	//TODO: Support SSL (execTLS)
	//TODO: Support STARTTLS (execTLS)
	//TODO: use String for all jmeter fields

	private static final Logger log = LoggingManager.getLoggerForClass();

	public static final String CLIENT = "POP3Sampler.client";
	public static final String OPERATION = "POP3Sampler.operation";
	public static final String COMMAND = "POP3Sampler.command";
	public static final String ADDITIONAL_REPLY = "POP3Sampler.additional_reply";
	public static final String HOSTNAME = "POP3Sampler.hostname";
	public static final String PORT = "POP3Sampler.port";
	public static final String SO_TIMEOUT = "POP3Sampler.so_timeout";
	public static final String CONNECTION_TIMEOUT = "POP3Sampler.connection_timeout";
	public static final String USE_SSL = "POP3Sampler.use_ssl";
	public static final String TCP_NODELAY = "POP3Sampler.tcp_nodelay";


	private final static String RC_200 = "200";
	private final static String RC_354 = "354";
	private final static String RC_404 = "404";
	private final static String RC_500 = "500";



	public static final LinkedList<String> operations = new LinkedList<String>();

	//Operations
	static {
		operations.push("DISCONNECT");
		operations.push("NOOP");
		operations.push("COMMAND");
		operations.push("RSET");
		operations.push("CONNECT");
	}

	public String getSOClient() {
		return SessionStorage.PROTOCOL.POP3 + getClient();
	}
	public String getClient() {
		return getPropertyAsString(CLIENT);
	}
	public void setClient(String client) {
		setProperty(CLIENT, client);
	}
	public String getOperation() {
		return getPropertyAsString(OPERATION);
	}
	public void setOperation(String operation) {
		setProperty(OPERATION, operation);
	}
	public String getCommand() {
		return getPropertyAsString(COMMAND);
	}
	public void setCommand(String command) {
		setProperty(COMMAND, command);
	}
	public String getHostname() {
		return getPropertyAsString(HOSTNAME);
	}
	public void setHostname(String hostname) {
		setProperty(HOSTNAME, hostname);
	}
	public int getPort() {
		return getPropertyAsInt(PORT);
	}
	public void setPort(int port) {
		setProperty(PORT, port);
	}
	public int getSoTimeout() {
		return getPropertyAsInt(SO_TIMEOUT);
	}
	public void setSoTimeout(int soTimeout) {
		setProperty(SO_TIMEOUT, soTimeout);
	}
	public boolean getTcpNoDelay() {
		return getPropertyAsBoolean(TCP_NODELAY);
	}
	public void setTcpNoDelay(boolean enableNagleAlgorithm) {
		setProperty(TCP_NODELAY, enableNagleAlgorithm);
	}
	public int getConnectionTimeout() {
		return getPropertyAsInt(CONNECTION_TIMEOUT);
	}
	public void setConnectionTimeout(int connectionTimeout) {
		setProperty(CONNECTION_TIMEOUT, connectionTimeout);
	}
	public boolean getUseSSL() {
		return getPropertyAsBoolean(USE_SSL);
	}
	public void setUseSSL(boolean use) {
		setProperty(USE_SSL, use);
	}
	public boolean getAdditionalReply() {
		return getPropertyAsBoolean(ADDITIONAL_REPLY);
	}
	public void setAdditionalReply(boolean use) {
		setProperty(ADDITIONAL_REPLY, use);
	}
	@Override
	public SampleResult sample(Entry e) {
		SampleResult sr = new SampleResult();

		sr.setSampleLabel(getName());

		if(getOperation().equals("CONNECT"))    return sampleConnect(sr);
		if(getOperation().equals("DISCONNECT"))    return sampleDisconnect(sr);
		if(getOperation().equals("NOOP"))   return sampleNoop(sr);
		if(getOperation().equals("RSET"))   return sampleReset(sr);
		if(getOperation().equals("COMMAND"))    return sampleCommand(sr);

		return sr;
	}

	private SampleResult sampleConnect(SampleResult sr) {
		POP3Client client;

		if(getUseSSL()) {
			client = new POP3SClient(true);
//		} else if(getUseSTARTTLS()) {
//			client = new POP3SClient(false);
		} else {
			client = new POP3Client();
		}

		StringBuilder requestBuilder = new StringBuilder();
		try {
			//String request = "CONNECT \n";
			requestBuilder.append("CONNECT\n");
			requestBuilder.append("Host : " + getHostname() + ":" + getPort() + "\n");
			requestBuilder.append("Connect Timeout: " + getConnectionTimeout() + "\n");
			requestBuilder.append("Socket Timeout: " + getSoTimeout() + "\n");
			requestBuilder.append("Client : " + getClient() + "\n");
			if(getUseSSL()) requestBuilder.append("SSL : true\n");
			else requestBuilder.append("SSL : false\n");
//			if(getUseSTARTTLS())    request += "STARTTLS : true\n";
//			else request += "STARTTLS : false\n";

			sr.setRequestHeaders(requestBuilder.toString());
			sr.sampleStart();
			client.setConnectTimeout(getConnectionTimeout());
			client.connect(getHostname(), getPort());
			if(client.isConnected()) {
				SessionStorage.proto_type protoType = SessionStorage.proto_type.PLAIN;
				if(getUseSSL())	protoType = SessionStorage.proto_type.SSL;
//				if(getUseSSL() && !getUseSTARTTLS()) protoType = SessionStorage.proto_type.SSL;
//				if(!getUseSSL() && getUseSTARTTLS()) protoType = SessionStorage.proto_type.STARTTLS;

				SessionStorage.getInstance().putClient(getSOClient(), client, protoType);
				client.setSoTimeout(getSoTimeout());
				client.setTcpNoDelay(getTcpNoDelay());
				sr.setResponseCode(RC_200);
				sr.setResponseData(client.getReplyString().getBytes());
				sr.setSuccessful(true);
			} else {
				sr.setResponseCode(RC_500);
				sr.setSuccessful(false);
			}
		} catch (SocketException se) {
			sr.setResponseMessage(se.toString());
			sr.setSuccessful(false);
			sr.setResponseCode(se.getClass().getName());
			log.error("client `" + client + "` ", se);
		} catch (IOException ioe) {
			sr.setResponseMessage(ioe.toString());
			sr.setSuccessful(false);
			sr.setResponseCode(ioe.getClass().getName());
			log.error("client `" + client + "` ", ioe);
		}
		sr.sampleEnd();
		return sr;
	}
	private SampleResult sampleDisconnect(SampleResult sr) {
		SocketClient soclient = SessionStorage.getInstance().getClient( getSOClient());
		POP3Client client = null;
		if(soclient instanceof POP3Client) client = (POP3Client) soclient;

		String request = "DISCONNECT\n";
		request += "Client : " + getClient() + "\n";
		sr.setRequestHeaders(request);
		if(client == null) {
			clientNotFound(sr);
		} else {
			synchronized(client) {
				sr.sampleStart();
				try {
					client.disconnect();
					if(!client.isConnected()) {
						sr.setResponseCode(RC_200);
						sr.setSuccessful(true);
					} else {
						sr.setResponseCode(RC_500);
						sr.setSuccessful(false);
						log.error("can't disconnect client " +getSOClient());
					}
					SessionStorage.getInstance().removeClient(getSOClient());
				} catch (IOException e) {
					sr.setSuccessful(false);
					sr.setResponseData(e.toString().getBytes());
					sr.setResponseCode(e.getClass().getName());
					log.error("client `" + client + "` ", e);
					removeClient();
				}
				sr.sampleEnd();
			}
		}
		return sr;
	}
	private SampleResult sampleNoop(SampleResult sr) {
		SocketClient soclient = SessionStorage.getInstance().getClient(getSOClient());
		POP3Client client = null;
		if(soclient instanceof POP3Client) client = (POP3Client) soclient;

		String request = "NOOP\n";
		request += "Client : " + getClient() + "\n";
		sr.setRequestHeaders(request);
		if(client == null) {
			clientNotFound(sr);
		} else {
			synchronized(client) {
				sr.sampleStart();
				try {
					boolean noop = client.noop();
					sr.setSuccessful(noop);
					sr.setResponseCode(noop ? RC_200 : RC_500);
				} catch (IOException e) {
					sr.setSuccessful(false);
					sr.setResponseData(e.toString().getBytes());
					sr.setResponseCode(e.getClass().getName());
					log.error("client `" + client + "` io exception", e);
					removeClient();
				}
				sr.sampleEnd();
			}
		}
		return sr;
	}
	private SampleResult sampleCommand(SampleResult sr) {
		SocketClient soclient = SessionStorage.getInstance().getClient(getSOClient());
		POP3Client client = null;
		int responseCode;
		if(soclient instanceof POP3Client) client = (POP3Client) soclient;

		String request = "COMMAND\n";
		request += "Client : " + getClient() + "\n";
		request += "Command : " + getCommand() + "\n";
		sr.setRequestHeaders(request);
		if(client == null) {
			clientNotFound(sr);
		} else {
			synchronized(client) {
				sr.sampleStart();
				try {
					String command, args = null;
					int index = getCommand().indexOf(' ');
					if(index != -1) {
						command = getCommand().substring(0, index);
						if(index + 1 < getCommand().length())
							args = getCommand().substring(index + 1, getCommand().length() - 1);
					}
					else command = getCommand();

					responseCode =
							(args != null ?
								client.sendCommand(command, args) :
								client.sendCommand(command)
							);
					if(getAdditionalReply())    client.getAdditionalReply();
					setSuccessfulByResponseCode(sr, responseCode);
					setResponse(sr, client.getReplyStrings());
				} catch (IOException e) {
					sr.setSuccessful(false);
					sr.setResponseData(e.toString().getBytes());
					sr.setResponseCode(e.getClass().getName());
					log.error("client `" + client + "` ", e);
					removeClient();
				}
				sr.sampleEnd();
			}
		}
		return sr;
	}
	private SampleResult sampleReset(SampleResult sr) {
		SocketClient soclient = SessionStorage.getInstance().getClient(getSOClient());
		POP3Client client = null;
		if(soclient instanceof POP3Client) client = (POP3Client) soclient;

		String request = "COMMAND\n";
		request += "Client : " + getClient() + "\n";
		sr.setRequestHeaders(request);
		if(client == null) {
			clientNotFound(sr);
			return sr;
		} else {
			synchronized(client) {
				sr.sampleStart();
				try {
					boolean reset = client.reset();
					sr.setSuccessful(reset);
					sr.setResponseCode(reset ? RC_200 : RC_500);
				} catch (IOException e) {
					sr.setSuccessful(false);
					sr.setResponseData(e.toString().getBytes());
					sr.setResponseCode(e.getClass().getName());
					log.error("client `" + client + "` ", e);
					removeClient();
				}
				sr.sampleEnd();
			}
		}
		return sr;
	}
	private void setSuccessfulByResponseCode(SampleResult sr, int rc) {
		if(rc == POP3Reply.OK) {
			sr.setResponseCode(RC_200);
			sr.setSuccessful(true);
		} else if(rc == POP3Reply.OK_INT) {
			sr.setResponseCode(RC_354);
			sr.setSuccessful(true);
		} else {
			sr.setResponseCode(RC_500);
			sr.setSuccessful(false);
		}
	}
	private void setResponse(SampleResult sr, String[] replies) {
		if(replies == null)	return;
		if(replies.length == 0)	return;
		StringBuilder builder = new StringBuilder();
		for(String reply : replies) {
			builder.append(reply);
			builder.append(System.lineSeparator());
		}
		sr.setResponseData(builder.toString().getBytes());
	}
	private void clientNotFound(SampleResult sr) {
		sr.sampleStart();
		sr.setResponseCode(RC_404);
		sr.setResponseData(("client `" + getClient() + "` not found").getBytes());
		sr.setSuccessful(false);
		sr.sampleEnd();
	}
	private void removeClient() {
		try {
			SessionStorage.getInstance().getClient(getSOClient()).disconnect();
		} catch (IOException e) {
			log.warn("Cannot disconnect client `" + getSOClient() + "` " , e);
		}
		SessionStorage.getInstance().removeClient(getSOClient());
		log.warn("session `" + getClient() + "` removed from pool");
	}
}
 