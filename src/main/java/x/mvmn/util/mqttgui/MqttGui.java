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
import javax.swing.JTabbedPane;
import javax.swing.SwingUtilities;

import org.eclipse.paho.client.mqttv3.IMqttAsyncClient;
import org.eclipse.paho.client.mqttv3.MqttAsyncClient;
import org.eclipse.paho.client.mqttv3.MqttException;

import x.mvmn.util.mqttgui.util.SwingUtil;

public class MqttGui implements WindowListener {

	final JFrame mainWindow = new JFrame("MVMn MQTT GUI");
	final JTabbedPane tabPane = new JTabbedPane();
	final JButton btnCreateClient = new JButton("Add client");

	final ConcurrentHashMap<String, IMqttAsyncClient> clients = new ConcurrentHashMap<String, IMqttAsyncClient>();

	public MqttGui() {
		mainWindow.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		mainWindow.addWindowListener(this);
		mainWindow.getContentPane().setLayout(new BorderLayout());
		mainWindow.add(tabPane, BorderLayout.CENTER);

		mainWindow.getContentPane().add(btnCreateClient, BorderLayout.NORTH);

		btnCreateClient.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				new NewClientDialog(mainWindow, new NewClientDialog.NewClientDialogCallback() {
					public void onSuccess(String serverUrl, String clientInstanceId) throws Exception {
						createNewClientTab(serverUrl, clientInstanceId);
					}
				}).setVisible(true);
			}
		});

		mainWindow.setMinimumSize(new Dimension(800, 600));
		mainWindow.pack();
		SwingUtil.moveToScreenCenter(mainWindow);
		mainWindow.setVisible(true);
	}

	public static interface MqttClientGuiCloseCallback {
		public void close(MqttClientGui mqttClientGui);
	}

	public void createNewClientTab(String serverUrl, String clientInstanceId) throws Exception {
		IMqttAsyncClient client = new MqttAsyncClient(serverUrl, clientInstanceId);
		tabPane.addTab(clientInstanceId, new MqttClientGui(client, new MqttClientGuiCloseCallback() {
			public void close(final MqttClientGui mqttClientGui) {
				SwingUtilities.invokeLater(new Runnable() {
					public void run() {
						tabPane.remove(mqttClientGui);
					}
				});
			}
		}));
		tabPane.setSelectedIndex(tabPane.getTabCount() - 1);
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
