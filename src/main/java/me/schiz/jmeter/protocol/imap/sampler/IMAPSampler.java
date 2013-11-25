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
package me.schiz.jmeter.protocol.imap.sampler;

import me.schiz.jmeter.protocol.SessionStorage;
import org.apache.commons.net.SocketClient;
import org.apache.commons.net.imap.IMAPClient;
import org.apache.commons.net.imap.IMAPCommand;
import org.apache.commons.net.imap.IMAPSClient;
import org.apache.jmeter.samplers.AbstractSampler;
import org.apache.jmeter.samplers.Entry;
import org.apache.jmeter.samplers.SampleResult;
import org.apache.jorphan.logging.LoggingManager;
import org.apache.log.Logger;

import java.io.IOException;
import java.net.SocketException;
import java.util.LinkedList;

/**
 * @author Epikhin Mikhail (epihin-m@yandex.ru)
 * @version 0.2
 * IMAP low-level Sampler based on Apache Commons Net
 */
public class IMAPSampler extends AbstractSampler{
    // TODO add request information
    // TODO add logging
    // TODO buy milk
    // TODO UID support
    private static final Logger log = LoggingManager.getLoggerForClass();
    private static final long serialVersionUID = 2105827624749965416L;

    private static final String RC_ERROR = "500";

    public static final String CLIENT = "IMAPSampler.client";
    public static final String OPERATION = "IMAPSampler.operation";
    public static final String COMMAND = "IMAPSampler.command";
    public static final String COMMAND_ARGS = "IMAPSampler.command_args";
    public static final String HOSTNAME = "IMAPSampler.hostname";
    public static final String PORT = "IMAPSampler.port";
    public static final String DEFAULT_TIMEOUT = "IMAPSampler.default_timeout";
    public static final String SO_TIMEOUT = "IMAPSampler.so_timeout";
    public static final String CONNECTION_TIMEOUT = "IMAPSampler.connection_timeout";
    public static final String USE_SSL = "IMAPSampler.use_ssl";
    public static final String TCP_NODELAY = "IMAPSampler.tcp_nodelay";
    public static final String CLIENT_NAME = "IMAPSampler.client_name";
    public static final String CLIENT_PASSWORD = "IMAPSampler.client_password";
    public static final String CHECK_SUCCESSFUL = "IMAPSampler.check_successful";

    public static final LinkedList<String> operations = new LinkedList<String>();
    public static final LinkedList<String> commands = new LinkedList<String>();

    //Operations
    static {
        operations.push("DISCONNECT");
        operations.push("NOOP");
        operations.push("COMMAND");
        operations.push("LOGOUT");
        operations.push("LOGIN");
        operations.push("CAPABILITY");
        operations.push("CONNECT");
    }

    //Commands
    static {
        for(IMAPCommand i : IMAPCommand.values()) {
            commands.push(i.name());
        }
    }
    public String getSOClient() {
        return SessionStorage.PROTOCOL.IMAP + getClient();
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
    public String getCommandArgs() {
        return getPropertyAsString(COMMAND_ARGS);
    }
    public void setCommandArgs(String commandArgs) {
        setProperty(COMMAND_ARGS, commandArgs);
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
    public int getDefaultTimeout() {
        return getPropertyAsInt(DEFAULT_TIMEOUT);
    }
    public void setDefaultTimeout(int defaultTimeout) {
        setProperty(DEFAULT_TIMEOUT, defaultTimeout);
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
    public String getClientName() {
        return getPropertyAsString(CLIENT_NAME);
    }
    public void setClientName(String clientName) {
        setProperty(CLIENT_NAME, clientName);
    }
    public String getClientPassword() {
        return getPropertyAsString(CLIENT_PASSWORD);
    }
    public void setClientPassword(String clientPassword) {
        setProperty(CLIENT_PASSWORD, clientPassword);
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
    public boolean getCheckSuccessful() {
        return getPropertyAsBoolean(CHECK_SUCCESSFUL);
    }
    public void setCheckSuccessful(boolean check) {
        setProperty(CHECK_SUCCESSFUL, check);
    }
    @Override
    public SampleResult sample(Entry e) {
        SampleResult sr = new SampleResult();

        sr.setSampleLabel(getName());

        if(getOperation().equals("CONNECT"))    return sampleConnect(sr);
        if(getOperation().equals("DISCONNECT"))    return sampleDisconnect(sr);
        if(getOperation().equals("NOOP"))   return sampleNoop(sr);
        if(getOperation().equals("LOGIN"))   return sampleLogin(sr);
        if(getOperation().equals("LOGOUT"))   return sampleLogout(sr);
        if(getOperation().equals("COMMAND"))    return sampleCommand(sr);
        if(getOperation().equals("CAPABILITY"))    return sampleCapability(sr);
        return sr;
    }

    private SampleResult sampleConnect(SampleResult sr) {
        IMAPClient client;
        if(getUseSSL()) {
            client = new IMAPSClient(true);
        } else {
            client = new IMAPClient();
        }
        try {
            String request = "CONNECT \n";
            request += "Host : " + getHostname() + ":" + getPort() + "\n";
            request += "Default Timeout : " + getDefaultTimeout() + "\n";
            request += "Connect Timeout : " + getConnectionTimeout() + "\n";
            request += "So Timeout : " + getSoTimeout() + "\n";
            request += "Client : " + getClient() + "\n";

            sr.setRequestHeaders(request);
            sr.sampleStart();
            client.setDefaultTimeout(getDefaultTimeout());
            client.setConnectTimeout(getConnectionTimeout());
            client.connect(getHostname(), getPort());
            if(client.isConnected()) {
                SessionStorage.proto_type protoType = SessionStorage.proto_type.PLAIN;
                if(getUseSSL()) protoType = SessionStorage.proto_type.SSL;
                SessionStorage.getInstance().putClient(getSOClient(), client, protoType);
                client.setSoTimeout(getSoTimeout());
                sr.setSuccessful(true);
                sr.setResponseCodeOK();
                sr.setResponseData(client.getReplyString().getBytes());
            } else {
                client.close();
                sr.setSuccessful(false);
                sr.setResponseCode("java.net.ConnectException");
                sr.setResponseMessage("Not connected");
            }
        } catch (SocketException se) {
            sr.setResponseMessage(se.toString());
            sr.setSuccessful(false);
            sr.setResponseCode(se.getClass().getName());
            log.error("client `" + getClient() + "` ", se);
            removeClient();
        } catch (IOException ioe) {
            sr.setResponseMessage(ioe.toString());
            sr.setSuccessful(false);
            sr.setResponseCode(ioe.getClass().getName());
            log.error("client `" + getClient() + "` ", ioe);
            removeClient();
        } finally {
            sr.sampleEnd();
        }
        return sr;
    }
    private SampleResult sampleDisconnect(SampleResult sr) {
        SocketClient soclient = SessionStorage.getInstance().getClient( getSOClient());
        IMAPClient client = null;
        if(soclient instanceof IMAPClient) client = (IMAPClient) soclient;

        String request = "DISCONNECT \n";
        request += "Client : " + getClient() + "\n";
        sr.setRequestHeaders(request);
        if(client == null) {
            clientNotFound(sr);
            return sr;
        } else {
            synchronized(client) {
                sr.sampleStart();
                try {
                    client.disconnect();
                    SessionStorage.getInstance().removeClient(getSOClient());
                    sr.setSuccessful(true);
                    sr.setResponseCodeOK();
                    sr.setResponseData(client.getReplyString().getBytes());
                } catch (IOException e) {
                    sr.setSuccessful(false);
                    sr.setResponseData(e.toString().getBytes());
                    sr.setResponseCode(e.getClass().getName());
                    log.error("client `" + getClient() + "` ", e);
                    removeClient();
                } finally {
                    sr.sampleEnd();
                }
            }
        }
        return sr;
    }
    private SampleResult sampleNoop(SampleResult sr) {
        SocketClient soclient = SessionStorage.getInstance().getClient(getSOClient());
        IMAPClient client = null;
        if(soclient instanceof IMAPClient) client = (IMAPClient) soclient;

        boolean success = false;
        String request = "NOOP\n";
        request += "Client : " + getClient() + "\n";
        sr.setRequestHeaders(request);
        if(client == null) {
            clientNotFound(sr);
            return sr;
        } else {
            synchronized(client) {
                sr.sampleStart();
                try {
                    success = client.noop();
                    if(getCheckSuccessful()) {
                        sr.setSuccessful(success);
                        if(success) sr.setResponseCodeOK();
                        else    sr.setResponseCode(RC_ERROR);
                    } else {
                        sr.setSuccessful(true);
                        sr.setResponseCodeOK();
                    }
                    sr.setResponseData(client.getReplyString().getBytes());
                } catch (IOException e) {
                    sr.setSuccessful(false);
                    sr.setResponseData(e.toString().getBytes());
                    sr.setResponseCode(e.getClass().getName());
                    log.error("client `" + getClient() + "` io exception", e);
                    removeClient();
                }
                sr.sampleEnd();
            }
        }
        return sr;
    }
    private SampleResult sampleLogin(SampleResult sr) {
        SocketClient soclient = SessionStorage.getInstance().getClient(getSOClient());
        IMAPClient client = null;
        if(soclient instanceof IMAPClient) client = (IMAPClient) soclient;

        boolean success = false;
        String request = "LOGIN\n";
        request += "Client : " + getClient() + "\n";
        request += "Client Name : " + getClientName() + "\n";
        sr.setRequestHeaders(request);
        if(client == null) {
            clientNotFound(sr);
            return sr;
        } else {
            synchronized(client) {
                sr.sampleStart();
                try {
                    success = client.login(getClientName(), getClientPassword());
                    sr.setSuccessful(success);
                    if(getCheckSuccessful()) {
                        sr.setSuccessful(success);
                        if(success) sr.setResponseCodeOK();
                        else    sr.setResponseCode(RC_ERROR);
                    } else sr.setResponseCodeOK();
                    sr.setResponseData(client.getReplyString().getBytes());
                } catch (IOException e) {
                    sr.setSuccessful(false);
                    sr.setResponseData(e.toString().getBytes());
                    sr.setResponseCode(e.getClass().getName());
                    log.error("client `" + getClient() + "` ", e);
                    removeClient();
                }
                sr.sampleEnd();
            }
        }
        return sr;
    }
    private SampleResult sampleLogout(SampleResult sr) {
        SocketClient soclient = SessionStorage.getInstance().getClient(getSOClient());
        IMAPClient client = null;
        if(soclient instanceof IMAPClient) client = (IMAPClient) soclient;

        boolean success = false;
        String request = "LOGOUT\n";
        request += "Client : " + getClient() + "\n";
        request += "Client Name : " + getClientName() + "\n";
        sr.setRequestHeaders(request);
        if(client == null) {
            clientNotFound(sr);
            return sr;
        } else {
            synchronized(client) {
                sr.sampleStart();
                try {
                    success = client.logout();
                    sr.setSuccessful(success);
                    if(getCheckSuccessful()) {
                        sr.setSuccessful(success);
                        if(success) sr.setResponseCodeOK();
                        else    sr.setResponseCode(RC_ERROR);
                    } else sr.setResponseCodeOK();
                    sr.setResponseData(client.getReplyString().getBytes());
                } catch (IOException e) {
                    sr.setSuccessful(false);
                    sr.setResponseData(e.toString().getBytes());
                    sr.setResponseCode(e.getClass().getName());
                    log.error("client `"+ getClient() + "` ", e);
                    removeClient();
                }
                sr.sampleEnd();
            }
        }
        return sr;
    }
    private SampleResult sampleCommand(SampleResult sr) {
        SocketClient soclient = SessionStorage.getInstance().getClient(getSOClient());
        IMAPClient client = null;
        if(soclient instanceof IMAPClient) client = (IMAPClient) soclient;

        boolean success = false;
        String request = "COMMAND\n";
        request += "Client : " + getClient() + "\n";
        request += "Client Name : " + getClientName() + "\n";
        request += "Command : `" + getCommand()+ " " + getCommandArgs() + "`\n";
        sr.setRequestHeaders(request);
        if(client == null) {
            clientNotFound(sr);
            return sr;
        } else {
            synchronized(client) {
                sr.sampleStart();
                try {
                    if(getCommandArgs().isEmpty()) {
                        success = client.doCommand(IMAPCommand.valueOf(getCommand()));
                    } else {
                        success = client.doCommand(IMAPCommand.valueOf(getCommand()), getCommandArgs());
                    }
                    sr.setSuccessful(success);
                    if(getCheckSuccessful()) {
                        sr.setSuccessful(success);
                        if(success) sr.setResponseCodeOK();
                        else    sr.setResponseCode(RC_ERROR);
                    } else sr.setResponseCodeOK();
                    sr.setResponseData(client.getReplyString().getBytes());
                } catch (IOException e) {
                    sr.setSuccessful(false);
                    sr.setResponseData(e.toString().getBytes());
                    sr.setResponseCode(e.getClass().getName());
                    log.error("client `" + getClient() + "` ", e);
                    removeClient();
                }
                sr.sampleEnd();
            }
        }
        return sr;
    }
    private SampleResult sampleCapability(SampleResult sr) {
        SocketClient soclient = SessionStorage.getInstance().getClient(getSOClient());
        IMAPClient client = null;
        if(soclient instanceof IMAPClient) client = (IMAPClient) soclient;

        boolean success = false;
        String request = "CAPABILITY \n";
        request += "Client : " + getClient() + "\n";
        request += "Client Name : " + getClientName() + "\n";
        sr.setRequestHeaders(request);
        if(client == null) {
            clientNotFound(sr);
            return sr;
        } else {
            synchronized(client) {
                sr.sampleStart();
                try {
                    success = client.capability();
                    sr.setSuccessful(success);
                    if(getCheckSuccessful()) {
                        sr.setSuccessful(success);
                        if(success) sr.setResponseCodeOK();
                        else    sr.setResponseCode(RC_ERROR);
                    } else sr.setResponseCodeOK();
                    sr.setResponseData(client.getReplyString().getBytes());
                } catch (IOException e) {
                    sr.setSuccessful(false);
                    sr.setResponseData(e.toString().getBytes());
                    sr.setResponseCode(e.getClass().getName());
                    log.error("client `" + getClient() + "` ", e);
                    removeClient();
                }
                sr.sampleEnd();
            }
        }
        return sr;
    }
    private void clientNotFound(SampleResult sr) {
        sr.sampleStart();
        sr.setResponseCode("404");
        sr.setResponseData(("client `"+getClient()+"` not found").getBytes());
        sr.setSuccessful(false);
        sr.sampleEnd();
    }
    private void removeClient() {
		SocketClient socketClient = SessionStorage.getInstance().getClient(getSOClient());
		try {
			socketClient.disconnect();
		} catch (IOException e) {
			log.error("can't disconnect session `" + getClient() + "`", e);
		}
		finally {
			SessionStorage.getInstance().removeClient(getSOClient());
			log.error("session `" + getClient() + "` removed from pool");
		}
    }
}
 