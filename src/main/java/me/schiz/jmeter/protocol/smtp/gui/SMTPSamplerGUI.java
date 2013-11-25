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
package me.schiz.jmeter.protocol.smtp.gui;

import me.schiz.jmeter.protocol.smtp.sampler.SMTPSampler;
import org.apache.jmeter.samplers.gui.AbstractSamplerGui;
import org.apache.jmeter.testelement.TestElement;

import javax.swing.*;
import java.awt.*;

/**
 * @author Epikhin Mikhail (epihin-m@yandex.ru)
 * @version 0.1
 * GUI for low-level SMTP Sampler based on Apache Commons Net
 */

public class SMTPSamplerGUI extends AbstractSamplerGui {
    // TODO: User friendly interface
    private static final long serialVersionUID = 5291619409498223675L;
    private JPanel          jpGeneralPanel;
    private JTextField      tfName;
    private JTextField      tfComments;

    //General panel
    private JTextField      tfClient;
    private JComboBox   cbOperation;

    //Command panel
    private JPanel          jpCommandPanel;
    private JTextArea       taCommand;

    //Server settings
    private JPanel          jpServerPanel;
    private JTextField      tfHostname;
    private JTextField      tfPort;
    private JTextField      tfDefaultTimeout;
    private JTextField      tfSoTimeout;
    private JTextField      tfConnectionTimeout;
    private JCheckBox       cbUseSSL;
    private JCheckBox       cbUseSTARTTLS;
    private JCheckBox       cbTcpNoDelay;

    public SMTPSamplerGUI() {
        super();
        init();
        initFields();
    }

    public String getStaticLabel() {
        return "SMTP low-level Sampler";
    }

    public String getStaticLabelResource() {
        return getStaticLabel();
    }

    @Override
    public TestElement createTestElement() {
        SMTPSampler sampler = new SMTPSampler();
        modifyTestElement(sampler);
        return sampler;
    }

    @Override
    public String getLabelResource() {
        return this.getClass().getSimpleName();
    }

    public void configure(TestElement te) {
        super.configure(te);
        if(te instanceof SMTPSampler) {
            SMTPSampler cs = (SMTPSampler)te;
            this.tfName.setText(cs.getPropertyAsString(SMTPSampler.NAME));
            this.tfComments.setText(cs.getPropertyAsString(SMTPSampler.COMMENTS));

            //SMTP panel
            this.tfClient.setText(cs.getPropertyAsString(SMTPSampler.CLIENT));
            this.tfClient.setText(cs.getPropertyAsString(SMTPSampler.CLIENT));
            this.cbOperation.setSelectedItem(cs.getPropertyAsString(SMTPSampler.OPERATION));

            //Command panel
            this.taCommand.setText(cs.getPropertyAsString(SMTPSampler.COMMAND));

            //Server settings
            this.tfHostname.setText(cs.getPropertyAsString(SMTPSampler.HOSTNAME));
            this.tfPort.setText("" + cs.getPropertyAsInt(SMTPSampler.PORT));
            this.tfDefaultTimeout.setText("" + cs.getPropertyAsInt(SMTPSampler.DEFAULT_TIMEOUT));
            this.tfSoTimeout.setText("" + cs.getPropertyAsInt(SMTPSampler.SO_TIMEOUT));
            this.tfConnectionTimeout.setText("" + cs.getPropertyAsInt(SMTPSampler.CONNECTION_TIMEOUT));
            //this.cbUseSSL.setSelected(cs.getPropertyAsBoolean(SMTPSampler.USE_SSL));
            //this.cbUseSTARTTLS.setSelected(cs.getPropertyAsBoolean(SMTPSampler.USE_STARTTLS));
            this.cbUseSSL.setSelected(cs.getUseSSL());
            this.cbUseSTARTTLS.setSelected(cs.getUseSTARTTLS());
            this.cbTcpNoDelay.setSelected(cs.getPropertyAsBoolean(SMTPSampler.TCP_NODELAY));
        }
    }

    @Override
    public void modifyTestElement(TestElement sampler) {
        if(sampler == null) System.err.println("sampler is null");
        super.configureTestElement(sampler);
        if (sampler instanceof SMTPSampler) {
            SMTPSampler smtpSampler = (SMTPSampler) sampler;
            smtpSampler.setName(tfName.getText());
            smtpSampler.setClient(tfClient.getText());
            smtpSampler.setOperation((String) cbOperation.getSelectedItem());

            //Command panel
            smtpSampler.setCommand(taCommand.getText());

            //Server settings
            smtpSampler.setHostname(tfHostname.getText());
            smtpSampler.setPort(Integer.parseInt(tfPort.getText()));
            smtpSampler.setDefaultTimeout(Integer.parseInt(tfDefaultTimeout.getText()));
            smtpSampler.setSoTimeout(Integer.parseInt(tfSoTimeout.getText()));
            smtpSampler.setConnectionTimeout(Integer.parseInt(tfConnectionTimeout.getText()));
            smtpSampler.setUseSSL(cbUseSSL.isSelected());
            smtpSampler.setUseSTARTTLS(cbUseSTARTTLS.isSelected());
            smtpSampler.setTcpNoDelay(cbTcpNoDelay.isSelected());

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
        this.tfName.setText("SMTP low-level Sampler");
        this.tfComments.setText("");

        //General panel
        this.tfClient.setText("${__threadNum}");
        this.cbOperation.setSelectedItem("CONNECT");

        //Command panel
        this.taCommand.setText("HELO smtp.example.org");

        //Server settings
        this.tfHostname.setText("127.0.0.1");
        this.tfPort.setText("25");
        this.tfDefaultTimeout.setText("3000");
        this.tfConnectionTimeout.setText("1000");
        this.tfSoTimeout.setText("1000");
        this.cbUseSSL.setSelected(false);
        this.cbUseSTARTTLS.setSelected(false);
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
        addToPanel(jpGeneralPanel, editConstraints, 1, 1, cbOperation = new JComboBox(SMTPSampler.operations.toArray(new String[]{})));

        jpCommandPanel = new JPanel(new GridBagLayout());
        jpCommandPanel.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createEtchedBorder(),
                "Command")); // $NON-NLS-1$
        addToPanel(jpCommandPanel, editConstraints, 0, 0, taCommand = new JTextArea());
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
        addToPanel(jpServerPanel, editConstraints, 3, 3, cbUseSTARTTLS = new JCheckBox("STARTTLS"));
        addToPanel(jpServerPanel, labelConstraints, 0, 1, new JLabel("Default Timeout: ", JLabel.LEFT));
        addToPanel(jpServerPanel, editConstraints, 1, 1, tfDefaultTimeout = new JTextField(5));
        addToPanel(jpServerPanel, labelConstraints, 0, 2, new JLabel("Socket Timeout: ", JLabel.LEFT));
        addToPanel(jpServerPanel, editConstraints, 1, 2, tfSoTimeout = new JTextField(5));
        addToPanel(jpServerPanel, labelConstraints, 0, 3, new JLabel("Connection Timeout: ", JLabel.LEFT));
        addToPanel(jpServerPanel, editConstraints, 1, 3, tfConnectionTimeout = new JTextField(5));

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
    }

    private void setEnabledServerPanel(boolean enabled) {
        jpServerPanel.setEnabled(enabled);
        tfHostname.setEnabled(enabled);
        tfPort.setEnabled(enabled);
        tfDefaultTimeout.setEnabled(enabled);
        tfSoTimeout.setEnabled(enabled);
        tfConnectionTimeout.setEnabled(enabled);
        cbUseSSL.setEnabled(enabled);
        cbUseSTARTTLS.setEnabled(enabled);
        cbTcpNoDelay.setEnabled(enabled);
    }
}