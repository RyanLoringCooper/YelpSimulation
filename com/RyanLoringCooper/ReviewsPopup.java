package com.RyanLoringCooper;

import java.awt.Frame;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.Dimension;

import javax.swing.table.DefaultTableModel;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.JScrollPane;

public class ReviewsPopup extends JDialog implements MouseListener {
	private static final long serialVersionUID = 2872961479552207911L;
	private String[][] reviews;
	private String[] columnNames = {"Date", "Stars", "Text", "UserID", "Votes"};
    private Dimension size = new Dimension(800, 800);
    private Thread mainThread;

    public ReviewsPopup(String[][] reviews, Frame frame) {
        super(frame, true);
        this.reviews = reviews;
        setupWindow();
        setLocationRelativeTo(frame);
        setupThread();
    }

    private void setupWindow() {
        setTitle("Reviews Viewer");
        JTable reviewsTable = new JTable(new DefaultTableModel(reviews, columnNames){
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
        setContentPane(reviewsScroller);
        setPreferredSize(size);
        pack();
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
    public void mouseClicked(MouseEvent e) {
        if(e.getClickCount() == 2) {
            JTable target = (JTable)e.getSource();
            int row = target.getSelectedRow();
            createTextPopup(reviews[row][2]);
        }
    }

    private void createTextPopup(String text) {
        JDialog textPopup = new JDialog(this, true);
        textPopup.setLocationRelativeTo(this);
        textPopup.setContentPane(new JLabel(text));
        textPopup.pack();
        textPopup.setVisible(true);
    }

	@Override
	public void mouseEntered(MouseEvent arg0) {
		
	}

	@Override
	public void mouseExited(MouseEvent arg0) {
		
	}

	@Override
	public void mousePressed(MouseEvent arg0) {
		
	}

	@Override
	public void mouseReleased(MouseEvent arg0) {
		
	}
}
