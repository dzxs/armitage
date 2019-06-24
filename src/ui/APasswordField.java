package ui;

import javax.swing.*;
import javax.swing.text.Document;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;


/* A textfield with a popup menu to cut, copy, paste, and clear the textfield */
public class APasswordField extends JPasswordField {
	protected JPopupMenu menu = null;

	public APasswordField(int cols) {
		super(cols);
		createMenu();
	}

	public APasswordField(Document doc, String text, int cols) {
		super(doc, text, cols);
		createMenu();
	}

	public APasswordField(String text, int cols) {
		super(text, cols);
		createMenu();
	}

	public APasswordField() {
		super();
		createMenu();
	}

	public String getPass() {
		return new String(getPassword());
	}

	public void createMenu() {
		if (menu != null)
			return;

		menu = new JPopupMenu();
		JMenuItem cut = new JMenuItem("Cut", 'C');
		JMenuItem copy = new JMenuItem("Copy", 'o');
		JMenuItem paste = new JMenuItem("Paste", 'P');
		JMenuItem clear = new JMenuItem("Clear", 'l');

		cut.addActionListener(ev -> cut());

		copy.addActionListener(ev -> copy());

		paste.addActionListener(ev -> paste());

		clear.addActionListener(ev -> setText(""));

		menu.add(cut);
		menu.add(copy);
		menu.add(paste);
		menu.add(clear);

		addMouseListener(new MouseAdapter() {
			public void handle(MouseEvent ev) {
				if (ev.isPopupTrigger()) {
					menu.show((JComponent)ev.getSource(), ev.getX(), ev.getY());
				}
			}

			public void mousePressed(MouseEvent ev) {
				handle(ev);
			}

			public void mouseClicked(MouseEvent ev) {
				handle(ev);
			}

			public void mouseReleased(MouseEvent ev) {
				handle(ev);
			}
		});
	}
}
