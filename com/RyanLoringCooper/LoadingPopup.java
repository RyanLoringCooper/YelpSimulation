package com.RyanLoringCooper;

import java.awt.Dimension;
import java.awt.Frame;
import java.awt.event.WindowEvent;

import javax.swing.BoxLayout;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.JTextArea;

public class LoadingPopup extends JDialog {
	private static final long serialVersionUID = -2907349847536636724L;
	private static final String text = "Waiting for search results";

	public LoadingPopup(Frame frame) {
        super(frame, true);
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
		setTitle(text);
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        JTextArea t = new JTextArea(text);
        t.setEditable(false);
        panel.add(t);
        panel.setPreferredSize(new Dimension(200, 50));
        setContentPane(panel);
        setLocation(500, 500);
        pack();
		new Thread() {
			public void run() {
				setVisible(true);
			}
		}.start();
	}
	
	public void close() {
		setVisible(false);
		dispatchEvent(new WindowEvent(this, WindowEvent.WINDOW_CLOSING));
	}
}
