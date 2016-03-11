package x.mvmn.util.mqttgui;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;

import org.eclipse.paho.client.mqttv3.IMqttActionListener;
import org.eclipse.paho.client.mqttv3.IMqttAsyncClient;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttMessage;

import x.mvmn.util.mqttgui.util.StackTraceUtil;
import x.mvmn.util.mqttgui.util.SwingUtil;

public class MqttClientGui extends JPanel {

	private static final long serialVersionUID = -1040358216365429952L;

	protected final IMqttAsyncClient client;
	protected final MqttConnectOptions mqttConnectOptions;

	protected JTextArea txaMainLog = new JTextArea();
	protected JButton btnClear = new JButton("Clear log");

	protected JButton btnConnect = new JButton("Connect");
	protected JButton btnDisconnect = new JButton("Disconnect");

	protected JButton btnSubscribe = new JButton("Subscribe");
	protected JButton btnUnsubscribe = new JButton("Un-subscribe");

	protected JTextField txTopic = new JTextField();
	protected JComboBox<Integer> cbQos = new JComboBox<Integer>(new Integer[] { 0, 1, 2 });

	protected JTextField txPublishTopic = new JTextField("test/topic");
	protected JComboBox<Integer> cbPublishQos = new JComboBox<Integer>(new Integer[] { 0, 1, 2 });
	protected JTextArea txPublishText = new JTextArea("Hello");
	protected final JButton btnPublish = new JButton("Publish");

	public MqttClientGui(final IMqttAsyncClient client, final MqttConnectOptions mqttConnectOptions) {
		super(new BorderLayout());

		this.client = client;
		this.mqttConnectOptions = mqttConnectOptions;

		client.setCallback(new MqttCallback() {
			public void messageArrived(String topic, MqttMessage message) throws Exception {
				MqttClientGui.this.log("[Global] Message arrived - '" + topic + "' (QoS " + message.getQos() + "):\n"
						+ new String(message.getPayload(), "UTF-8") + "\n----");
			}

			public void deliveryComplete(IMqttDeliveryToken token) {
				MqttClientGui.this.log("[Global] Message delivery complete.");
			}

			public void connectionLost(Throwable cause) {
				MqttClientGui.this.log("[Global] Connection lost: \n" + StackTraceUtil.toString(cause));
			}
		});

		txaMainLog.setEditable(false);
		btnClear.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				txaMainLog.setText("");
			}
		});
		JTabbedPane topPanel = new JTabbedPane();
		this.add(topPanel, BorderLayout.NORTH);
		this.add(new JScrollPane(txaMainLog), BorderLayout.CENTER);
		this.add(btnClear, BorderLayout.SOUTH);
		JPanel pnlSubscribe = new JPanel();
		topPanel.addTab("Subscribe", pnlSubscribe);

		pnlSubscribe.setLayout(new GridLayout(3, 2));
		pnlSubscribe.add(btnConnect);
		pnlSubscribe.add(btnDisconnect);
		pnlSubscribe.add(txTopic);
		pnlSubscribe.add(cbQos);
		pnlSubscribe.add(btnSubscribe);
		pnlSubscribe.add(btnUnsubscribe);

		btnSubscribe.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				SwingUtil.performSafely(new SwingUtil.UnsafeOperation() {
					public void run() throws Exception {
						final String topic = txTopic.getText().trim();
						final int qos = ((Integer) cbQos.getSelectedItem()).intValue();
						log("Subscribing to '" + topic + "' with QoS " + qos);
						client.subscribe(topic, qos, null, new IMqttActionListener() {
							public void onSuccess(IMqttToken asyncActionToken) {
								log("Subscribed successfully to '" + topic + "' with QoS " + qos + ".");
							}

							public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
								log("Subscribe to '" + topic + "' with QoS " + qos + " failed:\n"
										+ StackTraceUtil.toString(exception));
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
						log("Un-subscribing from '" + topic + "'...");
						client.unsubscribe(topic, null, new IMqttActionListener() {
							public void onSuccess(IMqttToken asyncActionToken) {
								log("Un-subscribed successfully from '" + topic + "'.");
							}

							public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
								log("Un-subscribe from '" + topic + "' failed:\n" + StackTraceUtil.toString(exception));
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
						log("Connecting...");
						client.connect(mqttConnectOptions, null, new IMqttActionListener() {
							public void onSuccess(IMqttToken asyncActionToken) {
								log("Connected.");
							}

							public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
								log("Connect failed:\n" + StackTraceUtil.toString(exception));
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
						log("Disconnecting... ");
						client.disconnect(null, new IMqttActionListener() {
							public void onSuccess(IMqttToken asyncActionToken) {
								log("Disconnected.");
							}

							public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
								log("Disconnect failed:\n" + StackTraceUtil.toString(exception));
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
		pnlPublishTop.add(cbPublishQos, BorderLayout.EAST);
		pnlPublish.add(new JScrollPane(txPublishText), BorderLayout.CENTER);
		pnlPublish.add(btnPublish, BorderLayout.SOUTH);
		btnPublish.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent e) {
				btnPublish.setEnabled(false);
				SwingUtil.performSafely(new SwingUtil.UnsafeOperation() {
					public void run() throws Exception {
						final String topic = txPublishTopic.getText().trim();
						final int qos = ((Integer) cbPublishQos.getSelectedItem()).intValue();
						byte[] payload = txPublishText.getText().getBytes("UTF-8");
						log("Publishing...");
						try {
							client.publish(topic, payload, qos, false, null, new IMqttActionListener() {
								public void onSuccess(IMqttToken asyncActionToken) {
									btnPublish.setEnabled(true);
									log("Message published successfully.");
								}

								public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
									btnPublish.setEnabled(true);
									log("Message publish failed: " + StackTraceUtil.toString(exception));
								}
							});
						} finally {
							btnPublish.setEnabled(true);
						}
					}
				});
			}
		});
	}

	protected void log(final String text) {
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				txaMainLog.append(text + "\n");
			}
		});
	}
}
