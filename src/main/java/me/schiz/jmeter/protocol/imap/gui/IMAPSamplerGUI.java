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
package me.schiz.jmeter.protocol.imap.gui;

import java.awt.BorderLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;

import javax.swing.BorderFactory;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.JTextField;


import me.schiz.jmeter.protocol.imap.sampler.IMAPSampler;

import org.apache.jmeter.samplers.gui.AbstractSamplerGui;
import org.apache.jmeter.testelement.TestElement;

/**
 * @author Epikhin Mikhail (epihin-m@yandex.ru)
 * @version 0.2
 * GUI for low-level IMAP Sampler based on Apache Commons Net
 */

public class IMAPSamplerGUI extends AbstractSamplerGui {

    // TODO User-friendly interface
    private static final long serialVersionUID = 5291619409498223675L;
    private JPanel          jpGeneralPanel;
    private JTextField      tfName;
    private JTextField      tfComments;

    //IMAP panel
    private JTextField      tfClient;
    private JComboBox   cbOperation;

    //Command panel
    private JPanel          jpCommandPanel;
    private JComboBox       cbCommand;
    private JTextArea       taCommandArgs;
    private JCheckBox       cbCheckSuccessful;

    //Server settings
    private JPanel          jpServerPanel;
    private JTextField      tfHostname;
    private JTextField      tfPort;
    private JTextField      tfDefaultTimeout;
    private JTextField      tfSoTimeout;
    private JTextField      tfConnectionTimeout;
    private JCheckBox       cbUseSSL;
    private JCheckBox       cbTcpNoDelay;

    //Client settings
    private JPanel          jpClientPanel;
    private JTextField      tfClientName;
    private JTextField      tfClientPassword;


    public IMAPSamplerGUI() {
        super();
        init();
        initFields();
    }

    public String getStaticLabel() {
        return "IMAP low-level Sampler";
    }

    public String getStaticLabelResource() {
        return getStaticLabel();
    }

    @Override
    public TestElement createTestElement() {
        IMAPSampler sampler = new IMAPSampler();
        modifyTestElement(sampler);
        return sampler;
    }

    @Override
    public String getLabelResource() {
        return this.getClass().getSimpleName();
    }

    public void configure(TestElement te) {
        super.configure(te);
        if(te instanceof IMAPSampler) {
            IMAPSampler cs =(IMAPSampler)te;
            this.tfName.setText(cs.getPropertyAsString(IMAPSampler.NAME));
            this.tfComments.setText(cs.getPropertyAsString(IMAPSampler.COMMENTS));

            //IMAP panel
            this.tfClient.setText(cs.getPropertyAsString(IMAPSampler.CLIENT));
            this.cbOperation.setSelectedItem(cs.getPropertyAsString(IMAPSampler.OPERATION));

            //Command panel
            this.cbCommand.setSelectedItem(cs.getPropertyAsString(IMAPSampler.COMMAND));
            this.taCommandArgs.setText(cs.getPropertyAsString(IMAPSampler.COMMAND_ARGS));
            this.cbCheckSuccessful.setSelected(cs.getPropertyAsBoolean(IMAPSampler.CHECK_SUCCESSFUL));

            //Server settings
            this.tfHostname.setText(cs.getPropertyAsString(IMAPSampler.HOSTNAME));
            this.tfPort.setText("" + cs.getPropertyAsInt(IMAPSampler.PORT));
            this.tfDefaultTimeout.setText("" + cs.getPropertyAsInt(IMAPSampler.DEFAULT_TIMEOUT));
            this.tfSoTimeout.setText("" + cs.getPropertyAsInt(IMAPSampler.SO_TIMEOUT));
            this.tfConnectionTimeout.setText("" + cs.getPropertyAsInt(IMAPSampler.CONNECTION_TIMEOUT));
            this.cbUseSSL.setSelected(cs.getPropertyAsBoolean(IMAPSampler.USE_SSL));
            this.cbTcpNoDelay.setSelected(cs.getPropertyAsBoolean(IMAPSampler.TCP_NODELAY));

            //Client settings
            this.tfClientName.setText(cs.getPropertyAsString(IMAPSampler.CLIENT_NAME));
            this.tfClientPassword.setText(cs.getPropertyAsString(IMAPSampler.CLIENT_PASSWORD));
        }
    }

    @Override
    public void modifyTestElement(TestElement sampler) {
        if(sampler == null) System.err.println("sampler is null");
        super.configureTestElement(sampler);
        if (sampler instanceof IMAPSampler) {
            IMAPSampler imapSampler = (IMAPSampler) sampler;
            imapSampler.setName(tfName.getText());
            imapSampler.setClient(tfClient.getText());
            imapSampler.setOperation((String) cbOperation.getSelectedItem());

            //Command panel
            imapSampler.setCommand((String)cbCommand.getSelectedItem());
            imapSampler.setCommandArgs(taCommandArgs.getText());
            imapSampler.setCheckSuccessful(cbCheckSuccessful.isSelected());

            //Server settings
            imapSampler.setHostname(tfHostname.getText());
            imapSampler.setPort(Integer.parseInt(tfPort.getText()));
            imapSampler.setDefaultTimeout(Integer.parseInt(tfDefaultTimeout.getText()));
            imapSampler.setSoTimeout(Integer.parseInt(tfSoTimeout.getText()));
            imapSampler.setConnectionTimeout(Integer.parseInt(tfConnectionTimeout.getText()));
            imapSampler.setUseSSL(cbUseSSL.isSelected());
            imapSampler.setTcpNoDelay(cbTcpNoDelay.isSelected());

            //Client settings
            imapSampler.setClientName(tfClientName.getText());
            imapSampler.setClientPassword(tfClientPassword.getText());

            String curOp = cbOperation.getSelectedItem().toString();
            if(curOp.equals("CONNECT")) {
                setEnabledServerPanel(true);
                setEnabledCommandPanel(false);
                setEnabledClientPanel(false);
            } else if (curOp.equals("LOGIN")) {
                setEnabledServerPanel(false);
                setEnabledCommandPanel(false);
                setEnabledClientPanel(true);
            } else if (curOp.equals("COMMAND")) {
                setEnabledServerPanel(false);
                setEnabledCommandPanel(true);
                setEnabledClientPanel(false);
            } else {
                setEnabledServerPanel(false);
                setEnabledCommandPanel(false);
                setEnabledClientPanel(false);
            }
        }
    }
    private void initFields() {
        this.tfName.setText("IMAP low-level Sampler");
        this.tfComments.setText("");

        //IMAP panel
        this.tfClient.setText("${__threadNum}");
        this.cbOperation.setSelectedItem("CONNECT");

        //Command panel
        this.cbCommand.setSelectedItem((String)"NOOP");
        this.taCommandArgs.setText("");
        this.cbCheckSuccessful.setSelected(false);

        //Server settings
        this.tfHostname.setText("127.0.0.1");
        this.tfPort.setText("143");
        this.tfDefaultTimeout.setText("2000");
        this.tfConnectionTimeout.setText("1000");
        this.tfSoTimeout.setText("2000");
        this.cbUseSSL.setSelected(false);
        this.cbTcpNoDelay.setSelected(true);

        //Client settings
        this.tfClientName.setText("user@example.com");
        this.tfClientPassword.setText("SexyPassord");
    }
    private void init() {
        setLayout(new BorderLayout(0, 5));
        setBorder(makeBorder());

        JPanel mainPanel = new JPanel(new GridBagLayout());


        jpGeneralPanel = new JPanel(new GridBagLayout());
        jpGeneralPanel.setAlignmentX(0);
        jpGeneralPanel.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createEtchedBorder(),
                "")); // $NON-NLS-1$

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
        addToPanel(jpGeneralPanel, editConstraints, 1, 1, cbOperation = new JComboBox(IMAPSampler.operations.toArray(new String[]{})));

        jpCommandPanel = new JPanel(new GridBagLayout());
        jpCommandPanel.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createEtchedBorder(),
                "Command")); // $NON-NLS-1$
        addToPanel(jpCommandPanel, labelConstraints, 0, 0, cbCommand = new JComboBox(IMAPSampler.commands.toArray(new String[]{})));
        addToPanel(jpCommandPanel, labelConstraints, 1, 0, cbCheckSuccessful = new JCheckBox("Check Successful"));
        addToPanel(jpCommandPanel, editConstraints, 1, 0, taCommandArgs = new JTextArea());
        taCommandArgs.setColumns(32);
        taCommandArgs.setRows(4);
        taCommandArgs.setLineWrap(true);
        taCommandArgs.setWrapStyleWord(true);

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
        addToPanel(jpServerPanel, labelConstraints, 0, 1, new JLabel("Default Timeout: ", JLabel.LEFT));
        addToPanel(jpServerPanel, editConstraints, 1, 1, tfDefaultTimeout = new JTextField(5));
        addToPanel(jpServerPanel, labelConstraints, 0, 2, new JLabel("Socket Timeout: ", JLabel.LEFT));
        addToPanel(jpServerPanel, editConstraints, 1, 2, tfSoTimeout = new JTextField(5));
        addToPanel(jpServerPanel, labelConstraints, 0, 3, new JLabel("Connection Timeout: ", JLabel.LEFT));
        addToPanel(jpServerPanel, editConstraints, 1, 3, tfConnectionTimeout = new JTextField(5));


        jpClientPanel = new JPanel(new GridBagLayout());
        jpClientPanel.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createEtchedBorder(),
                "Client Settings")); // $NON-NLS-1$
        addToPanel(jpClientPanel, labelConstraints, 0, 0, new JLabel("User: ", JLabel.LEFT));
        addToPanel(jpClientPanel, editConstraints, 1, 0, tfClientName = new JTextField(16));
        addToPanel(jpClientPanel, labelConstraints, 2, 0, new JLabel("Password: ", JLabel.LEFT));
        addToPanel(jpClientPanel, editConstraints, 3, 0, tfClientPassword = new JTextField(16));

        // Compilation panels
        addToPanel(mainPanel, editConstraints, 0, 0, jpHeaderPanel);
        addToPanel(mainPanel, editConstraints, 0, 1, jpGeneralPanel);
        addToPanel(mainPanel, editConstraints, 0, 2, jpCommandPanel);
        addToPanel(mainPanel, editConstraints, 0, 3, jpServerPanel);
        addToPanel(mainPanel, editConstraints, 0, 4, jpClientPanel);

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
        cbCommand.setEnabled(enabled);
        taCommandArgs.setEnabled(enabled);
    }

    private void setEnabledServerPanel(boolean enabled) {
        jpServerPanel.setEnabled(enabled);
        tfHostname.setEnabled(enabled);
        tfPort.setEnabled(enabled);
        tfDefaultTimeout.setEnabled(enabled);
        tfSoTimeout.setEnabled(enabled);
        tfConnectionTimeout.setEnabled(enabled);
        cbUseSSL.setEnabled(enabled);
        cbTcpNoDelay.setEnabled(enabled);
    }
    private void setEnabledClientPanel(boolean enabled) {
        jpClientPanel.setEnabled(enabled);
        tfClientName.setEnabled(enabled);
        tfClientPassword.setEnabled(enabled);
    }
}