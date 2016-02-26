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
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;

import x.mvmn.util.mqttgui.util.StackTraceUtil;
import x.mvmn.util.mqttgui.util.SwingUtil;

public class NewClientDialog extends JDialog {
	private static final long serialVersionUID = 9217842265255294094L;

	protected JTextField tfServerUrl = new JTextField("tcp://127.0.0.1:1883");
	protected JTextField tfUsername = new JTextField("guest");
	protected JPasswordField tfPassword = new JPasswordField("guest");
	protected JTextField tfInstanceId = new JTextField("test");

	protected JButton btnOk = new JButton("Ok");
	protected JButton btnCancel = new JButton("Cancel");

	public static interface NewClientDialogCallback {
		public void onSuccess(String serverUrl, String username, String password, String clientInstanceId)
				throws Exception;
	}

	public NewClientDialog(JFrame parentFrame, final NewClientDialogCallback callback) {
		super(parentFrame, true);
		this.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);

		JPanel mainPanel = new JPanel(new GridLayout(5, 2));
		this.getContentPane().setLayout(new BorderLayout());
		this.getContentPane().add(mainPanel, BorderLayout.CENTER);

		mainPanel.setBorder(BorderFactory.createEmptyBorder(4, 16, 4, 16));

		mainPanel.add(new JLabel("Server URL"));
		mainPanel.add(tfServerUrl);

		mainPanel.add(new JLabel("Username"));
		mainPanel.add(tfUsername);

		mainPanel.add(new JLabel("Password"));
		mainPanel.add(tfPassword);

		mainPanel.add(new JLabel("Client instance ID"));
		mainPanel.add(tfInstanceId);

		mainPanel.add(btnCancel);
		mainPanel.add(btnOk);

		btnOk.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent actEvent) {
				try {
					callback.onSuccess(tfServerUrl.getText(), tfUsername.getText(),
							new String(tfPassword.getPassword()), tfInstanceId.getText());
				} catch (Exception ex) {
					ex.printStackTrace();
					JOptionPane.showMessageDialog(NewClientDialog.this, StackTraceUtil.toString(ex),
							"Error occurred: " + ex.getClass().getName() + " " + ex.getMessage(),
							JOptionPane.ERROR_MESSAGE);
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
