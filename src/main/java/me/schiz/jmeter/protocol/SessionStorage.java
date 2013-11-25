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
package me.schiz.jmeter.protocol;

import org.apache.commons.net.PrintCommandListener;
import org.apache.commons.net.SocketClient;
import org.apache.jorphan.logging.LoggingManager;
import org.apache.log.Logger;

import java.io.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author Epikhin Mikhail (epihin-m@yandex.ru)
 * @version 0.2
 * Simple Singleton for store all  apache commons socket-sessions
 */
public class SessionStorage {
    private static final Logger log = LoggingManager.getLoggerForClass();
    private static volatile SessionStorage instance = null;
	private ConcurrentHashMap<String, Session>	sessionMap = null;

    public SessionStorage() {
		sessionMap = new ConcurrentHashMap<String, Session>();
    }

    public static SessionStorage getInstance() {
        if(instance == null) {
			synchronized(SessionStorage.class) {
				if(instance == null) instance = new SessionStorage();
			}
		}
		return instance;
    }

	public Session getClient(String client) {
		log.debug("get client `" + client + "`");
		return sessionMap.get(client);
	}

    public void putClient(String client, SocketClient socketClient, Session._encryption encryption, Session._protocol protocol) {
		Session session = new Session();
		session.socketClient = socketClient;
		session.protoctol = protocol;
		session.encryption = encryption;

		sessionMap.put(client, session);
        log.debug("put client `" + client + "` with protocol " + protocol);
    }

    public void removeClient(String client) {

		if(sessionMap.get(client).socketClient.isConnected()) {
			try {
				sessionMap.get(client).socketClient.disconnect();
				log.warn("client `" + client + "`disconnected from session storage" );
			} catch (IOException e) {
				log.warn("can't disconnect client `" + client + "`", e);
			}
		}
        sessionMap.remove(client);
        log.debug("remove client `" + client + "`");
    }

	@Deprecated
    public void installLog(String client, String logPath) {
        if(sessionMap.get(client) == null) {
            log.warn("can't install log, because not found client `" + client + "`");
            return;
        }
        try {
            PrintWriter pw = new PrintWriter(new OutputStreamWriter(new BufferedOutputStream(new FileOutputStream(logPath,
                    false)), "UTF-8"), true);

            //logMap.put(client, pw);

            sessionMap.get(client).socketClient.addProtocolCommandListener(new PrintCommandListener(pw, true));
        } catch (UnsupportedEncodingException e) {
            log.error("UTF-8 not supported", e);
        } catch (FileNotFoundException e) {
            log.error("Not found `" + logPath + "`", e);
        }
    }

}
