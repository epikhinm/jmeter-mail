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
package me.schiz.jmeter.protocol.smtp.sampler;

import me.schiz.jmeter.protocol.SessionStorage;
import org.apache.commons.net.SocketClient;
import org.apache.commons.net.smtp.SMTPClient;
import org.apache.commons.net.smtp.SMTPReply;
import org.apache.commons.net.smtp.SMTPSClient;
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
 * @version 0.1
 * low-level SMTP Sampler based on Apache Commons Net
 */
public class SMTPSampler extends AbstractSampler{
    // TODO add request information
    // TODO null timestamp in 404 errors
    // TODO remove client with Socket/IOExceptions

    private static final long serialVersionUID = -1137322402307312366L;

    private static final Logger log = LoggingManager.getLoggerForClass();

    public static final String CLIENT = "SMTPSampler.client";
    public static final String OPERATION = "SMTPSampler.operation";
    public static final String COMMAND = "SMTPSampler.command";
    public static final String HOSTNAME = "SMTPSampler.hostname";
    public static final String PORT = "SMTPSampler.port";
    public static final String DEFAULT_TIMEOUT = "SMTPSampler.default_timeout";
    public static final String SO_TIMEOUT = "SMTPSampler.so_timeout";
    public static final String CONNECTION_TIMEOUT = "SMTPSampler.connection_timeout";
    public static final String USE_SSL = "SMTPSampler.use_ssl";
    public static final String USE_STARTTLS = "SMTPSampler.use_starttls";
    public static final String TCP_NODELAY = "SMTPSampler.tcp_nodelay";


    public static final LinkedList<String> operations = new LinkedList<String>();

    //Operations
    static {
        operations.push("DISCONNECT");
        operations.push("NOOP");
        operations.push("COMMAND");
        operations.push("RESET");
        operations.push("CONNECT");
    }

    public String getSOClient() {
        return SessionStorage.PROTOCOL.SMTP + getClient();
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
    public boolean getUseSTARTTLS() {
        return getPropertyAsBoolean(USE_STARTTLS);
    }
    public void setUseSTARTTLS(boolean use) {
        setProperty(USE_STARTTLS, use);
    }
    @Override
    public SampleResult sample(Entry e) {
        SampleResult sr = new SampleResult();

        sr.setSampleLabel(getName());

        if(getOperation().equals("CONNECT"))    return sampleConnect(sr);
        if(getOperation().equals("DISCONNECT"))    return sampleDisconnect(sr);
        if(getOperation().equals("NOOP"))   return sampleNoop(sr);
        if(getOperation().equals("RESET"))   return sampleReset(sr);
        if(getOperation().equals("COMMAND"))    return sampleCommand(sr);

        return sr;
    }

    private SampleResult sampleConnect(SampleResult sr) {
        SMTPClient client;

        if(getUseSSL()) {
            client = new SMTPSClient(true);
        } else if(getUseSTARTTLS()) {
            client = new SMTPSClient(false);
        } else {
            client = new SMTPClient();
        }

        try {
            String request = "CONNECT \n";
            request += "Host : " + getHostname() + ":" + getPort() + "\n";
            request += "Default Timeout : " + getDefaultTimeout() + "\n";
            request += "Connect Timeout : " + getConnectionTimeout() + "\n";
            request += "So Timeout : " + getSoTimeout() + "\n";
            request += "Client : " + getClient() + "\n";
            if(getUseSSL()) request += "SSL : true\n";
            else request += "SSL : false\n";
            if(getUseSTARTTLS())    request += "STARTTLS : true\n";
            else request += "STARTTLS : false\n";

            sr.setRequestHeaders(request);
            sr.sampleStart();
            client.setDefaultTimeout(getDefaultTimeout());
            client.setConnectTimeout(getConnectionTimeout());
            client.connect(getHostname(), getPort());
            if(client.isConnected()) {
                SessionStorage.proto_type protoType = SessionStorage.proto_type.PLAIN;
                if(getUseSSL() && !getUseSTARTTLS()) protoType = SessionStorage.proto_type.SSL;
                if(!getUseSSL() && getUseSTARTTLS()) protoType = SessionStorage.proto_type.STARTTLS;

                SessionStorage.getInstance().putClient(getSOClient(), client, protoType);
                client.setSoTimeout(getSoTimeout());
                client.setTcpNoDelay(getTcpNoDelay());
                sr.setResponseCode(String.valueOf(client.getReplyCode()));
                sr.setResponseData(client.getReplyString().getBytes());
                setSuccessfulByResponseCode(sr, client.getReplyCode());
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
        SMTPClient client = null;
        if(soclient instanceof SMTPClient) client = (SMTPClient) soclient;

        String request = "DISCONNECT \n";
        request += "Client : " + getClient() + "\n";
        sr.setRequestHeaders(request);
        if(client == null) {
            clientNotFound(sr);
        } else {
            synchronized(client) {
                sr.sampleStart();
                try {
                    client.disconnect();
                    sr.setResponseCode(String.valueOf(client.getReplyCode()));
                    setSuccessfulByResponseCode(sr, client.getReplyCode());
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
        SMTPClient client = null;
        if(soclient instanceof SMTPClient) client = (SMTPClient) soclient;

        String request = "NOOP\n";
        request += "Client : " + getClient() + "\n";
        sr.setRequestHeaders(request);
        if(client == null) {
            clientNotFound(sr);
        } else {
            synchronized(client) {
                sr.sampleStart();
                try {
                    sr.setSuccessful(client.sendNoOp());
                    sr.setResponseCode(String.valueOf(client.getReplyCode()));
                    sr.setResponseData(client.getReplyString().getBytes());
                    setSuccessfulByResponseCode(sr, client.getReplyCode());
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
        SMTPClient client = null;
        int responseCode = 0;
        if(soclient instanceof SMTPClient) client = (SMTPClient) soclient;

        String request = "COMMAND\n";
        request += "Client : " + getClient() + "\n";
        request += "Command : " + getCommand() + "\n";
        sr.setRequestHeaders(request);
        if(client == null) {
            sr.setResponseCode("404");
            sr.setResponseData(("Client `"+getClient()+"` not found").getBytes());
            sr.setSuccessful(false);
            return sr;
        } else {
            synchronized(client) {
                sr.sampleStart();
                try {
                    responseCode = client.sendCommand(getCommand());
                    sr.setResponseCode(String.valueOf(responseCode));
                    sr.setSuccessful(SMTPReply.isPositiveIntermediate(responseCode));
                    String response = client.getReplyString();
                    setSuccessfulByResponseCode(sr, client.getReplyCode());

                    if(SessionStorage.getInstance().getClientType(getSOClient()) == SessionStorage.proto_type.STARTTLS) {
                        String command;
                        if(getCommand().indexOf(' ') != -1) command = getCommand().substring(0, getCommand().indexOf(' '));
                        else command = getCommand();
                        if((command.equalsIgnoreCase("lhlo") || command.equalsIgnoreCase("ehlo") || command.equalsIgnoreCase("helo")) &&
                                getUseSTARTTLS() && client instanceof SMTPSClient) {
                            SMTPSClient sclient = (SMTPSClient)client;
                            if(sclient.execTLS() == false) {
                                sr.setSuccessful(false);
                                sr.setResponseCode("403");;
                                response += sclient.getReplyString();
                                log.error("client `" + client + "` STARTTLS failed");
                                removeClient();
                            } else {
                                response += "\nSTARTTLS OK";
                            }
                        }
                    }
                    sr.setResponseData(response.getBytes());
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
        SMTPClient client = null;
        if(soclient instanceof SMTPClient) client = (SMTPClient) soclient;

        String request = "COMMAND\n";
        request += "Client : " + getClient() + "\n";
        sr.setRequestHeaders(request);
        if(client == null) {
            sr.setResponseCode("404");
            sr.setResponseData(("Client `"+getClient()+"` not found").getBytes());
            sr.setSuccessful(false);
            return sr;
        } else {
            synchronized(client) {
                sr.sampleStart();
                try {
                    sr.setSuccessful(client.reset());
                    sr.setResponseCode(String.valueOf(client.getReplyCode()));
                    sr.setResponseData(client.getReplyString().getBytes());
                    setSuccessfulByResponseCode(sr, client.getReplyCode());
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
    private void setSuccessfulByResponseCode(SampleResult sr, int replyCode) {
        if(SMTPReply.isPositiveCompletion(replyCode)
                || SMTPReply.isPositiveIntermediate(replyCode)
                || SMTPReply.isPositivePreliminary(replyCode)) {
            sr.setSuccessful(true);
        } else sr.setSuccessful(false);
    }
    private void clientNotFound(SampleResult sr) {
        sr.sampleStart();
        sr.setResponseCode("404");
        sr.setResponseData(("client `"+getClient()+"` not found").getBytes());
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
 