package armitage;

import console.Console;

import javax.swing.*;
import java.awt.*;
import java.util.Properties;

/** A generic multi-feature console for use in the Armitage network attack tool */
public class ActivityConsole extends Console implements Activity {
	protected JLabel label;
	protected Color  original;
	public void registerLabel(JLabel l) {
		label = l;
		original = l.getForeground();
	}

	public void resetNotification() {
		label.setForeground(original);
	}

	protected void appendToConsole(String _text) {
		super.appendToConsole(_text);

		if (_text.length() > 0 && label != null && !isShowing()) {
			label.setForeground(Color.decode(display.getProperty("tab.highlight.color", "#0000ff")));
		}
	}

	public ActivityConsole(Properties preferences) {
		super(preferences);
	}
}
