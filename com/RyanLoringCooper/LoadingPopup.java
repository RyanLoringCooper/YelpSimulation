package com.RyanLoringCooper;

import java.awt.Dimension;
import java.awt.Font;
import java.awt.Frame;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.JDialog;
import javax.swing.JLabel;

public class LoadingPopup extends JDialog {
	private static final long serialVersionUID = -2907349847536636724L;
	private static final String text = "Waiting for search results";
	private static Dimension size = new Dimension(500, 100);
	private boolean closeOnX = false;

	protected class WindowListener extends WindowAdapter {
		public void windowClosing(WindowEvent e) {
			if(e.getID() == WindowEvent.WINDOW_CLOSING && closeOnX) {
				closeOnX = false;
				close();
			}
		}
    }
	
	public LoadingPopup(Frame frame, hw3 searcher) {
        super(frame, true);
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowListener());
		setTitle(text);
        Font f = new Font("Default", Font.BOLD, 15);
        JLabel t = new JLabel(text, JLabel.CENTER);
        t.setFont(f);
        setContentPane(t);
        setLocationRelativeTo(frame);
        setPreferredSize(size);
        pack();
		new Thread() {
			public void run() {
				if(searcher.executeSearch() > 0) {
					setVisible(false);
				} else {
					t.setText("No rows were selected");
					closeOnX = true;
					try {
						sleep(5000);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
					setVisible(false);
				}
			}
		}.start();
		setVisible(true);
	}
	
	public void close() {
		setVisible(false);
		dispatchEvent(new WindowEvent(this, WindowEvent.WINDOW_CLOSING));
	}
}
