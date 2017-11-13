package com.RyanLoringCooper;

import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.Dimension;

import javax.swing.table.DefaultTableModel;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JScrollPane;
import javax.swing.BoxLayout;
import javax.swing.JButton;

public class ReviewsPopup extends JDialog implements ActionListener {
	private static final long serialVersionUID = 2872961479552207911L;
    private static final String viewButtonText = "View Review Text";
	private String[][] reviews;
	private String[] columnNames = {"Date", "Stars", "Text", "UserID", "Votes"};
    private Dimension size = new Dimension(800, 800);
    private Thread mainThread;
    private JTable reviewsTable;

    public ReviewsPopup(String[][] reviews, Frame frame) {
        super(frame, true);
        this.reviews = reviews;
        setupWindow();
        setLocationRelativeTo(frame);
        setupThread();
    }

    private void setupWindow() {
        setTitle("Reviews Viewer");
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.add(getTable());
        panel.add(getButton());
        setContentPane(panel);
        setPreferredSize(size);
        pack();
    }

    private JScrollPane getTable() {
        reviewsTable = new JTable(new DefaultTableModel(reviews, columnNames){
			private static final long serialVersionUID = -8021583007741092559L;

			@Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        });
        reviewsTable.setFillsViewportHeight(true);
        reviewsTable.setColumnSelectionAllowed(false);
        reviewsTable.setDragEnabled(false);
        JScrollPane reviewsScroller = new JScrollPane();
        reviewsScroller.setPreferredSize(size);
        reviewsScroller.setViewportView(reviewsTable);
        return reviewsScroller;
    }

    private JButton getButton() {
        JButton button = new JButton(viewButtonText);
        button.setActionCommand(viewButtonText);
        button.addActionListener(this);
        return button;
    }

    private void setupThread() {
        mainThread = new Thread() {
            @Override
            public void run() {
                setVisible(true);
            }
        };
        mainThread.start();
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if(viewButtonText.equals(e.getActionCommand())) {
            int row = reviewsTable.getSelectedRow();
            if(row > -1) {
				createTextPopup(reviews[row][2]);
            }
        }
    }

    private void createTextPopup(String text) {
        JDialog textPopup = new JDialog(this, true);
        textPopup.setLocationRelativeTo(this);
        textPopup.setContentPane(new JTextArea(text));
        textPopup.pack();
        textPopup.setVisible(true);
    }
}
