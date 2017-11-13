package com.RyanLoringCooper;

import java.awt.Dimension;
import java.awt.Font;
import java.awt.Frame;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;

public class LoadingPopup extends JDialog implements ActionListener {
	private static final long serialVersionUID = -2907349847536636724L;
	private static final String waitingText = "Waiting for search results", closeButtonText = "Close";
	private static Dimension size = new Dimension(600, 200);
	private boolean closeOnX = false;
	private JLabel label;
	private JButton closeButton;
	private Thread t;

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
        setupWindow();
        setLocationRelativeTo(frame);
		t = waitingThread(searcher);
		t.start();
		setVisible(true);
	}
	
	private JPanel getLabelPanel() {
		JPanel labelPanel = new JPanel();
		labelPanel.setLayout(new GridBagLayout());
		Font f = new Font("Default", Font.BOLD, 15);
        label = new JLabel(waitingText, JLabel.CENTER);
        label.setFont(f);
        labelPanel.add(label);
        return labelPanel;
	}
	
	private JPanel getButtonPanel() {
		JPanel buttonPanel = new JPanel();
        closeButton = new JButton(closeButtonText);
        closeButton.setActionCommand(closeButtonText);
    	closeButton.addActionListener(this);
    	closeButton.setVisible(false);
        buttonPanel.add(closeButton);
        return buttonPanel;
	}
	
	private void setupWindow() {
		setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowListener());
		setTitle(waitingText);
		JPanel panel = new JPanel();
		panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.add(getLabelPanel());
        panel.add(getButtonPanel());
        setContentPane(panel);
        setPreferredSize(size);
        pack();
	}

	public void close() {
		setVisible(false);
		dispatchEvent(new WindowEvent(this, WindowEvent.WINDOW_CLOSING));
		try {
			t.interrupt();
			t.join();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
	
	private Thread waitingThread(hw3 searcher) {
		return new Thread() {
			public void run() {
				int rowsSelected = searcher.executeSearch();
				String labelText = "No rows were selected.";
				if(rowsSelected > 0) {
					switch(searcher.getNumSearches()) {
						case 1:
							labelText = Integer.toString(rowsSelected) + " subcategories were selected.";
							break;
						case 2:
							labelText = Integer.toString(rowsSelected) + " attributes were selected.";
							break;
						case 3:
							labelText = Integer.toString(rowsSelected) + " locations were selected.";
							break;
						case 4:
							labelText = Integer.toString(rowsSelected) + " possible operating times combinations were selected.";
							break;
						case 5:
							labelText = Integer.toString(rowsSelected) + " businesses were selected.";
							break;
						case 6:
							labelText = Integer.toString(rowsSelected) + " reviews were selected.";
							break;
						default:
							labelText = "An unknown action was preformed.";
							break;
					}
				} 
				label.setText(labelText);
				setTitle(labelText);
				closeOnX = true;
				closeButton.setVisible(true);
				try {
					sleep(5000);
				} catch (InterruptedException e) {
					
				}
				setVisible(false);
			}
		};
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		if(closeButtonText.equals(e.getActionCommand())) {	
			close();
		}
	}
}
