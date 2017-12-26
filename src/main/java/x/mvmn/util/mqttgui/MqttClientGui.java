package x.mvmn.util.mqttgui;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.UnsupportedEncodingException;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.table.DefaultTableModel;

import org.eclipse.paho.client.mqttv3.IMqttActionListener;
import org.eclipse.paho.client.mqttv3.IMqttAsyncClient;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttMessage;

import x.mvmn.util.mqttgui.MqttGui.MqttClientGuiCloseCallback;
import x.mvmn.util.mqttgui.util.StackTraceUtil;
import x.mvmn.util.mqttgui.util.SwingUtil;

public class MqttClientGui extends JPanel {

	private static final long serialVersionUID = -1040358216365429952L;

	protected final IMqttAsyncClient client;

	protected JTextArea txaMainLog = new JTextArea();
	protected JButton btnClearLog = new JButton("Clear log");
	protected JButton btnClearMessages = new JButton("Clear messages");

	protected JButton btnConnect = new JButton("Connect");
	protected JButton btnDisconnect = new JButton("Disconnect");

	protected JButton btnSubscribe = new JButton("Subscribe");
	protected JButton btnUnsubscribe = new JButton("Un-subscribe");

	protected JButton btnClose = new JButton("Close client");

	protected JCheckBox cbRetain = new JCheckBox("Retain", false);

	protected JTextField txTopic = new JTextField();
	protected JComboBox<Integer> cbQos = new JComboBox<Integer>(new Integer[] { 0, 1, 2 });

	protected final JTable tblReceivedMessages;
	protected final DefaultTableModel tableModel;

	protected JTextField txPublishTopic = new JTextField("test/topic");
	protected JComboBox<Integer> cbPublishQos = new JComboBox<Integer>(new Integer[] { 0, 1, 2 });
	protected JTextArea txPublishText = new JTextArea("Hello");
	protected final JButton btnPublish = new JButton("Publish");
	protected final JButton btnPublishMultiple = new JButton("Publish multiple");

	protected JTextField tfUsername = new JTextField("guest");
	protected JPasswordField tfPassword = new JPasswordField("guest");
	protected JCheckBox cbCleanSession = new JCheckBox("Clean session", false);

	public MqttClientGui(final IMqttAsyncClient client, final MqttClientGuiCloseCallback closeCallback) {
		super(new BorderLayout());
		this.client = client;

		client.setCallback(new MqttCallback() {
			public void messageArrived(String topic, MqttMessage message) throws Exception {
				onMessageArrived(topic, message);
			}

			public void deliveryComplete(IMqttDeliveryToken token) {
				MqttClientGui.this.logOnSwingEdt("[Global] Message delivery complete.");
			}

			public void connectionLost(Throwable cause) {
				MqttClientGui.this.logOnSwingEdt("[Global] Connection lost: \n" + StackTraceUtil.toString(cause));
			}
		});

		txaMainLog.setEditable(false);
		btnClearLog.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				txaMainLog.setText("");
			}
		});
		JTabbedPane topPanel = new JTabbedPane();
		this.add(topPanel, BorderLayout.NORTH);

		JTabbedPane centerTabs = new JTabbedPane();
		JPanel logPanel = new JPanel(new BorderLayout());
		logPanel.add(new JScrollPane(txaMainLog), BorderLayout.CENTER);
		logPanel.add(btnClearLog, BorderLayout.SOUTH);
		centerTabs.add("Log", logPanel);

		tableModel = new DefaultTableModel(new Object[] { "Topic", "QoS", "Body", "Retained" }, 0);
		tblReceivedMessages = new JTable(tableModel);

		JPanel tablePanel = new JPanel(new BorderLayout());
		tablePanel.add(new JScrollPane(tblReceivedMessages), BorderLayout.CENTER);
		tablePanel.add(btnClearMessages, BorderLayout.SOUTH);
		centerTabs.add("Received messages", tablePanel);

		btnClearMessages.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				tableModel.setRowCount(0);
			}
		});

		this.add(centerTabs, BorderLayout.CENTER);
		this.add(btnClose, BorderLayout.SOUTH);

		btnClose.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				SwingUtil.performSafely(new SwingUtil.UnsafeOperation() {
					public void run() throws Exception {
						if (client.isConnected()) {
							try {
								client.disconnect();
							} finally {
								client.close();
							}
						}
						closeCallback.close(MqttClientGui.this);
					}
				});
			}
		});

		JPanel pnlConnect = new JPanel(new BorderLayout());
		topPanel.addTab("Connect", pnlConnect);

		JPanel pnlConnectCenter = new JPanel(new GridLayout(1, 2));
		pnlConnect.add(pnlConnectCenter, BorderLayout.CENTER);
		tfUsername.setBorder(BorderFactory.createTitledBorder("Username"));
		pnlConnectCenter.add(tfUsername);
		tfPassword.setBorder(BorderFactory.createTitledBorder("Password"));
		pnlConnectCenter.add(tfPassword);
		pnlConnect.add(cbCleanSession, BorderLayout.EAST);

		JPanel pnlConnectSouth = new JPanel(new GridLayout(1, 2));
		pnlConnect.add(pnlConnectSouth, BorderLayout.SOUTH);

		pnlConnectSouth.add(btnConnect);
		pnlConnectSouth.add(btnDisconnect);

		JPanel pnlSubscribe = new JPanel();
		topPanel.addTab("Subscribe", pnlSubscribe);

		pnlSubscribe.setLayout(new BorderLayout());
		txTopic.setBorder(BorderFactory.createTitledBorder("Topic"));
		pnlSubscribe.add(txTopic, BorderLayout.CENTER);
		cbQos.setBorder(BorderFactory.createTitledBorder("QoS"));
		pnlSubscribe.add(cbQos, BorderLayout.EAST);
		JPanel pnlSubscribeSouth = new JPanel(new GridLayout(1, 2));
		pnlSubscribe.add(pnlSubscribeSouth, BorderLayout.SOUTH);
		pnlSubscribeSouth.add(btnSubscribe);
		pnlSubscribeSouth.add(btnUnsubscribe);

		btnSubscribe.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				SwingUtil.performSafely(new SwingUtil.UnsafeOperation() {
					public void run() throws Exception {
						final String topic = txTopic.getText().trim();
						final int qos = ((Integer) cbQos.getSelectedItem()).intValue();
						logOnSwingEdt("Subscribing to '" + topic + "' with QoS " + qos);
						client.subscribe(topic, qos, null, new IMqttActionListener() {
							public void onSuccess(IMqttToken asyncActionToken) {
								logOnSwingEdt("Subscribed successfully to '" + topic + "' with QoS " + qos + ".");
							}

							public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
								logOnSwingEdt("Subscribe to '" + topic + "' with QoS " + qos + " failed:\n" + StackTraceUtil.toString(exception));
							}
						});
					}
				});
			}
		});

		btnUnsubscribe.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				SwingUtil.performSafely(new SwingUtil.UnsafeOperation() {
					public void run() throws Exception {
						final String topic = txTopic.getText().trim();
						logOnSwingEdt("Un-subscribing from '" + topic + "'...");
						client.unsubscribe(topic, null, new IMqttActionListener() {
							public void onSuccess(IMqttToken asyncActionToken) {
								logOnSwingEdt("Un-subscribed successfully from '" + topic + "'.");
							}

							public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
								logOnSwingEdt("Un-subscribe from '" + topic + "' failed:\n" + StackTraceUtil.toString(exception));
							}
						});
					}
				});
			}
		});

		btnConnect.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				SwingUtil.performSafely(new SwingUtil.UnsafeOperation() {
					public void run() throws Exception {
						logOnSwingEdt("Connecting...");
						String username = tfUsername.getText();
						String password = new String(tfPassword.getPassword());
						boolean cleanSession = cbCleanSession.isSelected();
						MqttConnectOptions connectionOptions = new MqttConnectOptions();
						if (!username.isEmpty()) {
							connectionOptions.setUserName(username);
							connectionOptions.setPassword(password.toCharArray());
						}
						connectionOptions.setCleanSession(cleanSession);
						client.connect(connectionOptions, null, new IMqttActionListener() {
							public void onSuccess(IMqttToken asyncActionToken) {
								logOnSwingEdt("Connected.");
							}

							public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
								logOnSwingEdt("Connect failed:\n" + StackTraceUtil.toString(exception));
							}
						});
					}
				});
			}
		});

		btnDisconnect.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				SwingUtil.performSafely(new SwingUtil.UnsafeOperation() {
					public void run() throws Exception {
						logOnSwingEdt("Disconnecting... ");
						client.disconnect(null, new IMqttActionListener() {
							public void onSuccess(IMqttToken asyncActionToken) {
								logOnSwingEdt("Disconnected.");
							}

							public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
								logOnSwingEdt("Disconnect failed:\n" + StackTraceUtil.toString(exception));
							}
						});
					}
				});
			}
		});

		JPanel pnlPublish = new JPanel(new BorderLayout());

		topPanel.addTab("Publish", pnlPublish);

		JPanel pnlPublishTop = new JPanel(new BorderLayout());
		pnlPublish.add(pnlPublishTop, BorderLayout.NORTH);
		pnlPublishTop.add(txPublishTopic, BorderLayout.CENTER);
		pnlPublishTop.add(cbPublishQos, BorderLayout.WEST);
		pnlPublishTop.add(cbRetain, BorderLayout.EAST);
		pnlPublish.add(new JScrollPane(txPublishText), BorderLayout.CENTER);
		JPanel pnlSouth = new JPanel(new GridLayout(1, 2));
		pnlSouth.add(btnPublish);
		pnlSouth.add(btnPublishMultiple);
		pnlPublish.add(pnlSouth, BorderLayout.SOUTH);
		btnPublish.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				btnPublish.setEnabled(false);
				SwingUtil.performSafely(new SwingUtil.UnsafeOperation() {
					public void run() throws Exception {
						final String topic = txPublishTopic.getText().trim();
						final int qos = ((Integer) cbPublishQos.getSelectedItem()).intValue();
						final boolean retain = cbRetain.isSelected();
						byte[] payload = txPublishText.getText().getBytes("UTF-8");
						logOnSwingEdt("Publishing...");
						try {
							client.publish(topic, payload, qos, retain, null, new IMqttActionListener() {
								public void onSuccess(IMqttToken asyncActionToken) {
									btnPublish.setEnabled(true);
									logOnSwingEdt("Message published successfully.");
								}

								public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
									btnPublish.setEnabled(true);
									logOnSwingEdt("Message publish failed: " + StackTraceUtil.toString(exception));
								}
							}).waitForCompletion(60000);
						} finally {
							btnPublish.setEnabled(true);
						}
					}
				});
			}
		});
		btnPublishMultiple.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				String input = JOptionPane.showInputDialog("Enter number of publishes", "10");
				int count = 0;
				if (input != null && !input.trim().isEmpty()) {
					try {
						count = Integer.parseInt(input.trim());
					} catch (NumberFormatException nfe) {
						// Ignore
					}
				}
				if (count > 0) {
					final int finalCount = count;
					final int qos = ((Integer) cbPublishQos.getSelectedItem()).intValue();
					final boolean retain = cbRetain.isSelected();
					final String topic = txPublishTopic.getText().trim();
					final String payload = txPublishText.getText();
					SwingUtil.performSafely(new SwingUtil.UnsafeOperation() {
						public void run() throws Exception {
							for (int i = 0; i < finalCount; i++) {
								String topicWithCount = topic.replace("$counter", String.valueOf(i));
								String payloadWithCount = payload.replace("$counter", String.valueOf(i));
								logOnSwingEdt("Publishing message " + (i + 1) + " of " + finalCount + "...");
								client.publish(topicWithCount, payloadWithCount.getBytes("UTF-8"), qos, retain, null, new IMqttActionListener() {
									public void onSuccess(IMqttToken asyncActionToken) {
										logOnSwingEdt("Message published successfully.");
									}

									public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
										logOnSwingEdt("Message publish failed: " + StackTraceUtil.toString(exception));
									}
								});
							}
						}
					});
				}
			}
		});
	}

	protected void onMessageArrived(String topic, MqttMessage message) {
		try {
			String payloadStr = new String(message.getPayload(), "UTF-8");
			MqttClientGui.this.logOnSwingEdt("[Global] Message arrived - '" + topic + "' (QoS " + message.getQos() + "):\n" + payloadStr + "\n----");
			tableModel.addRow(new Object[] { topic, String.valueOf(message.getQos()), payloadStr, String.valueOf(message.isRetained()) });
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
	}

	protected void logOnSwingEdt(final String text) {
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				txaMainLog.append(text + "\n");
			}
		});
	}
}
