package me.schiz.jmeter.protocol;

import org.apache.commons.net.SocketClient;

/**
 * Created with IntelliJ IDEA.
 * User: schizophrenia
 * Date: 25.11.13
 * Time: 17:27
 * To change this template use File | Settings | File Templates.
 */
public class Session {
	public enum _encryption {
		PLAIN,
		SSL,
		STARTTLS
	}

	public enum _protocol {
		IMAP, POP3, SMTP
	}

	public SocketClient socketClient;
	public _encryption encryption;
	public _protocol protoctol;

	public void Session() {}
	public void Session(SocketClient socketClient, _encryption encryption, _protocol protoctol) {
		this.socketClient = socketClient;
		this.encryption = encryption;
		this.protoctol = protoctol;
	}
}
