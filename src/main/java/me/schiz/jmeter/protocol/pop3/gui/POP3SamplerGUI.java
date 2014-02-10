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
package me.schiz.jmeter.protocol.pop3.gui;

import me.schiz.jmeter.protocol.pop3.sampler.POP3Sampler;
import org.apache.jmeter.samplers.gui.AbstractSamplerGui;
import org.apache.jmeter.testelement.TestElement;

import javax.swing.*;
import java.awt.*;

/**
 * @author Epikhin Mikhail (epikhinm@gmail.com)
 * @version 0.1
 * GUI for low-level POP3 Sampler based on Apache Commons Net
 */

public class POP3SamplerGUI extends AbstractSamplerGui {
	private JPanel          jpGeneralPanel;
	private JTextField      tfName;
	private JTextField      tfComments;

	//General panel
	private JTextField      tfClient;
	private JComboBox		cbOperation;

	//Command panel
	private JPanel          jpCommandPanel;
	private JTextArea       taCommand;
	private JCheckBox		cbAdditionalReply;

	//Server settings
	private JPanel          jpServerPanel;
	private JTextField      tfHostname;
	private JTextField      tfPort;
	private JTextField      tfSoTimeout;
	private JTextField      tfConnectionTimeout;
	private JCheckBox       cbUseSSL;
	//private JCheckBox       cbUseSTARTTLS;
	private JCheckBox       cbTcpNoDelay;

	public POP3SamplerGUI() {
		super();
		init();
		initFields();
	}

	public String getStaticLabel() {
		return "POP3 low-level Sampler";
	}

	public String getStaticLabelResource() {
		return getStaticLabel();
	}

	@Override
	public TestElement createTestElement() {
		POP3Sampler sampler = new POP3Sampler();
		modifyTestElement(sampler);
		return sampler;
	}

	@Override
	public String getLabelResource() {
		return this.getClass().getSimpleName();
	}

	public void configure(TestElement te) {
		super.configure(te);
		if(te instanceof POP3Sampler) {
			POP3Sampler cs = (POP3Sampler)te;
			this.tfName.setText(cs.getName());
			this.tfComments.setText(cs.getComment());

			//Client panel
			this.tfClient.setText(cs.getClient());
			this.cbOperation.setSelectedItem(cs.getOperation());

			//Command panel
			this.taCommand.setText(cs.getCommand());
			this.cbAdditionalReply.setSelected(cs.getAdditionalReply());

			//Server settings
			this.tfHostname.setText(cs.getHostname());
			this.tfPort.setText(String.valueOf(cs.getPort()));
			this.tfSoTimeout.setText(String.valueOf(cs.getSoTimeout()));
			this.tfConnectionTimeout.setText(String.valueOf(cs.getConnectionTimeout()));
			this.cbUseSSL.setSelected(cs.getUseSSL());
			//this.cbUseSTARTTLS.setSelected(cs.getUseSTARTTLS());
			this.cbTcpNoDelay.setSelected(cs.getTcpNoDelay());
		}
	}

	@Override
	public void modifyTestElement(TestElement sampler) {
		if(sampler == null) System.err.println("sampler is null");
		super.configureTestElement(sampler);
		if (sampler instanceof POP3Sampler) {
			POP3Sampler s = (POP3Sampler) sampler;
			s.setName(tfName.getText());
			s.setClient(tfClient.getText());
			s.setOperation((String) cbOperation.getSelectedItem());

			//Command panel
			s.setCommand(taCommand.getText());
			s.setAdditionalReply(cbAdditionalReply.isSelected());

			//Server settings
			s.setHostname(tfHostname.getText());
			s.setPort(Integer.parseInt(tfPort.getText()));
			s.setSoTimeout(Integer.parseInt(tfSoTimeout.getText()));
			s.setConnectionTimeout(Integer.parseInt(tfConnectionTimeout.getText()));
			s.setUseSSL(cbUseSSL.isSelected());
//			s.setUseSTARTTLS(cbUseSTARTTLS.isSelected());
			s.setTcpNoDelay(cbTcpNoDelay.isSelected());

			String curOp = cbOperation.getSelectedItem().toString();
			if(curOp.equals("CONNECT")) {
				setEnabledServerPanel(true);
				setEnabledCommandPanel(false);
			} else if (curOp.equals("COMMAND")) {
				setEnabledServerPanel(false);
				setEnabledCommandPanel(true);
			} else {
				setEnabledServerPanel(false);
				setEnabledCommandPanel(false);
			}
		}
	}
	private void initFields() {
		this.tfName.setText("POP3 low-level Sampler");
		this.tfComments.setText("developed by Epikhin Mikhail");

		//General panel
		this.tfClient.setText("${__threadNum}");
		this.cbOperation.setSelectedItem("CONNECT");

		//Command panel
		this.taCommand.setText("NOOP");
		this.cbAdditionalReply.setSelected(false);

		//Server settings
		this.tfHostname.setText("127.0.0.1");
		this.tfPort.setText("110");
		this.tfConnectionTimeout.setText("1000");
		this.tfSoTimeout.setText("1000");
		this.cbUseSSL.setSelected(false);
//		this.cbUseSTARTTLS.setSelected(false);
		this.cbTcpNoDelay.setSelected(true);
	}
	private void init() {
		setLayout(new BorderLayout(0, 5));
		setBorder(makeBorder());

		JPanel mainPanel = new JPanel(new GridBagLayout());


		jpGeneralPanel = new JPanel(new GridBagLayout());
		jpGeneralPanel.setAlignmentX(0);
		jpGeneralPanel.setBorder(BorderFactory.createTitledBorder(
				BorderFactory.createEtchedBorder(),
				""));

		GridBagConstraints labelConstraints = new GridBagConstraints();
		labelConstraints.anchor = GridBagConstraints.FIRST_LINE_END;

		GridBagConstraints editConstraints = new GridBagConstraints();
		editConstraints.anchor = GridBagConstraints.FIRST_LINE_START;
		editConstraints.weightx = 1.0;
		editConstraints.fill = GridBagConstraints.HORIZONTAL;

		editConstraints.insets = new java.awt.Insets(2, 0, 0, 0);
		labelConstraints.insets = new java.awt.Insets(2, 0, 0, 0);

		JPanel jpHeaderPanel = new JPanel(new GridBagLayout());
		addToPanel(jpHeaderPanel, labelConstraints, 0, 0, new JLabel("Name: ", JLabel.LEFT));
		addToPanel(jpHeaderPanel, editConstraints, 1, 0, tfName = new JTextField(20));
		addToPanel(jpHeaderPanel, labelConstraints, 0, 1, new JLabel("Comments: ", JLabel.LEFT));
		addToPanel(jpHeaderPanel, editConstraints, 1, 1, tfComments = new JTextField(20));

		addToPanel(jpGeneralPanel, labelConstraints, 0, 0, new JLabel("Client: ", JLabel.RIGHT));
		addToPanel(jpGeneralPanel, editConstraints, 1, 0, tfClient = new JTextField(32));
		addToPanel(jpGeneralPanel, labelConstraints, 0, 1, new JLabel("Operation: ", JLabel.RIGHT));
		addToPanel(jpGeneralPanel, editConstraints, 1, 1, cbOperation = new JComboBox(POP3Sampler.operations.toArray(new String[]{})));

		jpCommandPanel = new JPanel(new GridBagLayout());
		jpCommandPanel.setBorder(BorderFactory.createTitledBorder(
				BorderFactory.createEtchedBorder(),
				"Command")); // $NON-NLS-1$
		addToPanel(jpCommandPanel, editConstraints, 0, 0, taCommand = new JTextArea());
		addToPanel(jpCommandPanel, editConstraints, 0, 1, cbAdditionalReply = new JCheckBox("Additional Reply"));
		taCommand.setColumns(32);
		taCommand.setRows(4);
		taCommand.setLineWrap(true);
		taCommand.setWrapStyleWord(true);

		jpServerPanel = new JPanel(new GridBagLayout());
		jpServerPanel.setBorder(BorderFactory.createTitledBorder(
				BorderFactory.createEtchedBorder(),
				"Server Settings")); // $NON-NLS-1$

		addToPanel(jpServerPanel, labelConstraints, 0, 0, new JLabel("Server: ", JLabel.LEFT));
		addToPanel(jpServerPanel, editConstraints, 1, 0, tfHostname = new JTextField(20));
		addToPanel(jpServerPanel, labelConstraints, 2, 0, new JLabel("Port: ", JLabel.LEFT));
		addToPanel(jpServerPanel, editConstraints, 3, 0, tfPort = new JTextField(5));
		addToPanel(jpServerPanel, editConstraints, 3, 1, cbUseSSL = new JCheckBox("SSL"));
		addToPanel(jpServerPanel, editConstraints, 3, 2, cbTcpNoDelay = new JCheckBox("TCP_NODELAY"));
//		addToPanel(jpServerPanel, editConstraints, 3, 3, cbUseSTARTTLS = new JCheckBox("STARTTLS"));
//		addToPanel(jpServerPanel, labelConstraints, 0, 1, new JLabel("Default Timeout: ", JLabel.LEFT));
//		addToPanel(jpServerPanel, editConstraints, 1, 1, tfDefaultTimeout = new JTextField(5));
		addToPanel(jpServerPanel, labelConstraints, 0, 1, new JLabel("Socket Timeout: ", JLabel.LEFT));
		addToPanel(jpServerPanel, editConstraints, 1, 1, tfSoTimeout = new JTextField(5));
		addToPanel(jpServerPanel, labelConstraints, 0, 2, new JLabel("Connection Timeout: ", JLabel.LEFT));
		addToPanel(jpServerPanel, editConstraints, 1, 2, tfConnectionTimeout = new JTextField(5));

		// Compilation panels
		addToPanel(mainPanel, editConstraints, 0, 0, jpHeaderPanel);
		addToPanel(mainPanel, editConstraints, 0, 1, jpGeneralPanel);
		addToPanel(mainPanel, editConstraints, 0, 2, jpCommandPanel);
		addToPanel(mainPanel, editConstraints, 0, 3, jpServerPanel);

		JPanel container = new JPanel(new BorderLayout());
		container.add(makeTitlePanel(), BorderLayout.NORTH);
		container.add(mainPanel, BorderLayout.NORTH);
		add(container, BorderLayout.CENTER);
	}
	private void addToPanel(JPanel panel, GridBagConstraints constraints, int col, int row, JComponent component) {
		constraints.gridx = col;
		constraints.gridy = row;
		panel.add(component, constraints);
	}

	private void setEnabledCommandPanel(boolean enabled) {
		jpCommandPanel.setEnabled(enabled);
		taCommand.setEnabled(enabled);
		cbAdditionalReply.setEnabled(enabled);
	}

	private void setEnabledServerPanel(boolean enabled) {
		jpServerPanel.setEnabled(enabled);
		tfHostname.setEnabled(enabled);
		tfPort.setEnabled(enabled);
		tfSoTimeout.setEnabled(enabled);
		tfConnectionTimeout.setEnabled(enabled);
		cbUseSSL.setEnabled(enabled);
//		cbUseSTARTTLS.setEnabled(enabled);
		cbTcpNoDelay.setEnabled(enabled);
	}
}