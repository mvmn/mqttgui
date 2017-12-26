package x.mvmn.util.mqttgui.util;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.Toolkit;

import javax.swing.SwingUtilities;

import x.mvmn.util.mqttgui.ErrorMessageDialog;

public class SwingUtil {

	protected static ErrorMessageDialog errorMessageDialog = new ErrorMessageDialog(null);

	public static <T> void performSafely(final UnsafeOperation operation) {
		new Thread(new Runnable() {
			public void run() {
				try {
					operation.run();
				} catch (final Exception e) {
					e.printStackTrace();
					SwingUtilities.invokeLater(new Runnable() {
						public void run() {
							errorMessageDialog.show(null, "Error occurred: " + e.getClass().getName() + " " + e.getMessage(), StackTraceUtil.toString(e));
						}
					});
				}
			}
		}).start();
	}

	public static interface UnsafeOperation {
		public void run() throws Exception;
	}

	public static void moveToScreenCenter(final Component component) {
		Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
		Dimension componentSize = component.getSize();
		int newComponentX = screenSize.width - componentSize.width;
		if (newComponentX >= 0) {
			newComponentX = newComponentX / 2;
		} else {
			newComponentX = 0;
		}
		int newComponentY = screenSize.height - componentSize.height;
		if (newComponentY >= 0) {
			newComponentY = newComponentY / 2;
		} else {
			newComponentY = 0;
		}
		component.setLocation(newComponentX, newComponentY);
	}
}
