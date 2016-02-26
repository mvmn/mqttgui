package x.mvmn.util.mqttgui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;

import org.eclipse.paho.client.mqttv3.IMqttAsyncClient;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.springframework.integration.mqtt.core.DefaultMqttPahoClientFactory;

import x.mvmn.util.mqttgui.util.SwingUtil;

public class MqttGui implements WindowListener {

	final JFrame mainWindow = new JFrame("MVMn MQTT GUI");
	final JTabbedPane tabPane = new JTabbedPane();
	final JPanel btnPanel = new JPanel();
	final JButton btnCreateClient = new JButton("Create client");

	final ConcurrentHashMap<String, IMqttAsyncClient> clients = new ConcurrentHashMap<String, IMqttAsyncClient>();

	public MqttGui() {
		mainWindow.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		mainWindow.addWindowListener(this);
		mainWindow.getContentPane().setLayout(new BorderLayout());
		mainWindow.add(tabPane, BorderLayout.CENTER);

		btnPanel.setLayout(new BorderLayout());
		mainWindow.getContentPane().add(btnPanel, BorderLayout.SOUTH);
		btnPanel.add(btnCreateClient, BorderLayout.CENTER);

		btnCreateClient.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				new NewClientDialog(mainWindow, new NewClientDialog.NewClientDialogCallback() {
					public void onSuccess(String serverUrl, String username, String password, String clientInstanceId)
							throws Exception {
						createNewClientTab(serverUrl, username, password, clientInstanceId);
					}
				}).setVisible(true);
			}
		});

		mainWindow.setMinimumSize(new Dimension(800, 600));
		mainWindow.pack();
		SwingUtil.moveToScreenCenter(mainWindow);
		mainWindow.setVisible(true);
	}

	public void createNewClientTab(String serverUrl, String username, String password, String clientInstanceId)
			throws Exception {
		DefaultMqttPahoClientFactory factory = new org.springframework.integration.mqtt.core.DefaultMqttPahoClientFactory();
		factory.setUserName(username);
		factory.setPassword(password);
		IMqttAsyncClient client = factory.getAsyncClientInstance(serverUrl, clientInstanceId);
		tabPane.addTab(clientInstanceId, new MqttClientGui(client));
	}

	public void doCleanup() {
		for (Map.Entry<String, IMqttAsyncClient> clientEntry : clients.entrySet()) {
			try {
				clientEntry.getValue().close();
			} catch (MqttException e) {
				e.printStackTrace();
			}
		}
	}

	public static void main(String args[]) {
		new MqttGui();
	}

	public void windowClosing(WindowEvent e) {
		mainWindow.setVisible(false);
		this.doCleanup();
	}

	public void windowOpened(WindowEvent e) {
	}

	public void windowClosed(WindowEvent e) {
	}

	public void windowIconified(WindowEvent e) {
	}

	public void windowDeiconified(WindowEvent e) {
	}

	public void windowActivated(WindowEvent e) {
	}

	public void windowDeactivated(WindowEvent e) {
	}
}
