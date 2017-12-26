package x.mvmn.util.mqttgui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;

import org.eclipse.paho.client.mqttv3.MqttClient;

import x.mvmn.util.mqttgui.util.StackTraceUtil;
import x.mvmn.util.mqttgui.util.SwingUtil;

public class NewClientDialog extends JDialog {
	private static final long serialVersionUID = 9217842265255294094L;

	protected JTextField tfServerUrl = new JTextField("tcp://127.0.0.1:1883");
	protected JTextField tfInstanceId = new JTextField(MqttClient.generateClientId());

	protected JButton btnCreate = new JButton("Create client");
	protected JButton btnCancel = new JButton("Cancel");

	public static interface NewClientDialogCallback {
		public void onSuccess(String serverUrl, String clientInstanceId) throws Exception;
	}

	public NewClientDialog(JFrame parentFrame, final NewClientDialogCallback callback) {
		super(parentFrame, true);
		this.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);

		JPanel mainPanel = new JPanel(new GridLayout(2, 1));
		this.getContentPane().setLayout(new BorderLayout());
		this.getContentPane().add(mainPanel, BorderLayout.CENTER);

		tfServerUrl.setBorder(BorderFactory.createTitledBorder("Server URL"));
		mainPanel.add(tfServerUrl);

		tfInstanceId.setBorder(BorderFactory.createTitledBorder("Client instance ID"));
		mainPanel.add(tfInstanceId);

		JPanel buttonsPanel = new JPanel(new GridLayout(1, 2));

		buttonsPanel.add(btnCancel);
		buttonsPanel.add(btnCreate);

		this.getContentPane().add(buttonsPanel, BorderLayout.SOUTH);

		btnCreate.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent actEvent) {
				try {
					callback.onSuccess(tfServerUrl.getText(), tfInstanceId.getText());
				} catch (Exception ex) {
					ex.printStackTrace();
					JOptionPane.showMessageDialog(NewClientDialog.this, StackTraceUtil.toString(ex),
							"Error occurred: " + ex.getClass().getName() + " " + ex.getMessage(), JOptionPane.ERROR_MESSAGE);
				} finally {
					NewClientDialog.this.setVisible(false);
					NewClientDialog.this.dispose();
				}
			}
		});

		btnCancel.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				NewClientDialog.this.setVisible(false);
				NewClientDialog.this.dispose();
			}
		});

		this.setMinimumSize(new Dimension(600, 100));
		this.pack();
		SwingUtil.moveToScreenCenter(this);
	}

}
