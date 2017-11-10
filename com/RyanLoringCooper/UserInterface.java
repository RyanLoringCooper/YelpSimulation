package com.RyanLoringCooper;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.BoxLayout;
import javax.swing.JScrollPane;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.table.DefaultTableModel;
import javax.swing.JTable;

import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

public class UserInterface extends JFrame {
    
	private static final long serialVersionUID = -668103667213956930L;
	private static final String windowName = "Yelp Simulation";
	public static final String locationDefaultString = "Any";
    private static final String[] mainCategories = {"Active Life", "Arts & Entertainment", "Automotive", "Car Rental", "Cafes", "Beauty & Spas", "Convenience Stores", "Dentists", "Doctors", "Drugstores", "Department Stores", "Education", "Event Planning & Services", "Flowers & Gifts", "Food", "Health & Medical", "Home Services", "Home & Garden", "Hospitals", "Hotels & Travel", "Hardware Stores", "Grocery", "Medical Centers", "Nurseries & Gardening", "Nightlife", "Restaurants", "Shopping", "Transportation"};
    private static final String[] daysOfTheWeek = {"Any", "Sunday", "Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday"};
    private static final String[] hoursOfTheDay = {"12:00AM", "1:00AM", "2:00AM", "3:00AM", "4:00AM", "5:00AM", "6:00AM", "7:00AM", "8:00AM", "9:00AM", "10:00AM", "11:00AM", "12:00PM", "1:00PM", "2:00PM", "3:00PM", "4:00PM", "5:00PM", "6:00PM", "7:00PM", "8:00PM", "9:00PM", "10:00PM", "11:00PM"};
    protected static final String[] searchForOptions = {"AND", "OR"}; 
    protected static final int detailsHeaderHeight = 12, detailsHeaderMaxWidth = 160, mainSectionHeight = 500;
    // modify these by calling *scroller.setViewportView(new JList<JTextArea>(JTextArea[] info));
    protected JScrollPane subCategoriesScroller, attributesScroller, detailsScroller;
    protected JList<String> mainCategoriesList, subCategoriesList, attributesList;
    protected JTable detailsTable;
    protected String[][] detailsTableData;
    protected String[] detailsColumnNames = {"Business", "City", "State", "Stars"}, locationDefault = {locationDefaultString};
    protected JComboBox<String> weekDayDropdown, fromHoursDropdown, toHoursDropdown, locationDropdown, searchForDropdown;
    private ActionListener al;

    public UserInterface(ActionListener al) {
    	this.al = al;
        add(firstView());
        setupFirstWindow();
    }

    protected class WindowListener extends WindowAdapter {
		public void windowClosing(WindowEvent e) {
			hw3.terminate();
		}
    }
    
    private void setupFirstWindow() {
    	addWindowListener(new WindowListener());
        setName(windowName);
        setTitle(windowName);
        setLocation(10,20);
        setVisible(true);
        pack();
    }

    private JPanel firstView() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS)); 
        panel.add(getCenterPanel());
        panel.add(getDropdownsPanel());
        return panel;
    }

    private JPanel getCenterPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new GridLayout(1, 2));
        panel.add(getListsPanel());
        panel.add(getDetailsPanel());
        return panel;
    }

    // dropdowns at the bottom of the ui used for filtering
    @SuppressWarnings("unchecked")
	private JPanel getDropdownsPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.X_AXIS));
        
        Object[] weekDay = getSelectionDropdown("Day of the week:", daysOfTheWeek);
        weekDayDropdown = (JComboBox<String>)weekDay[1];

        Object[] fromHours = getSelectionDropdown("From:", hoursOfTheDay);
        fromHoursDropdown = (JComboBox<String>)fromHours[1];

        Object[] toHours = getSelectionDropdown("To:", hoursOfTheDay);
        toHoursDropdown = (JComboBox<String>)toHours[1];
        
        Object[] location = getSelectionDropdown("City, State:", locationDefault);
        locationDropdown = (JComboBox<String>)location[1];

        Object[] searchFor = getSelectionDropdown("Search for:", searchForOptions);
        searchForDropdown = (JComboBox<String>)searchFor[1];

        panel.add((JPanel)weekDay[0]);
        panel.add((JPanel)fromHours[0]);
        panel.add((JPanel)toHours[0]);
        panel.add((JPanel)location[0]);
        panel.add((JPanel)searchFor[0]);
        panel.add(getButtons());
        return panel;
    }
    
    private Object[] getSelectionDropdown(String name, String[] options) {
    	JPanel panel = new JPanel();
    	JLabel label = new JLabel(name, JLabel.CENTER);
        JComboBox<String> dropdown = new JComboBox<String>(options);
    	panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
    	panel.add(label);
        panel.add(dropdown);
        Object[] retval = new Object[2];
        retval[0] = panel;
        retval[1] = dropdown;
    	return retval;
    }
    
    private JButton getButton(String name) {
    	JButton button = new JButton(name);
    	button.setActionCommand(name);
    	button.addActionListener(al);
    	return button;
    }
    
    private JPanel getButtons() {
    	JPanel panel = new JPanel();
    	panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
    	panel.add(getButton(hw3.searchActionString));
        panel.add(getButton(hw3.resetActionString));
        panel.add(getButton(hw3.closeActionString));
        return panel;
    }

    // shows the various categories that can be used to narrow things down
    private JPanel getListsPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.X_AXIS));
        panel.add(getListOfMainCategories());
        subCategoriesList = new JList<String>();
        subCategoriesScroller = new JScrollPane(subCategoriesList);
        panel.add(subCategoriesScroller);
        attributesList = new JList<String>();
        attributesScroller = new JScrollPane(attributesList);
        panel.add(attributesScroller);
        return panel;
    }

    private JScrollPane getListOfMainCategories() {
        mainCategoriesList = new JList<String>(mainCategories);
        mainCategoriesList.setLayoutOrientation(JList.VERTICAL);
        mainCategoriesList.setVisibleRowCount(-1);
        JScrollPane listScroller = new JScrollPane(mainCategoriesList);
        listScroller.setPreferredSize(new Dimension(250, mainSectionHeight));
        return listScroller;
    }

    // panel that displays reviews about a selected business
    private JPanel getDetailsPanel() {
        JPanel panel = new JPanel();
        panel.setPreferredSize(new Dimension(detailsHeaderMaxWidth*4, mainSectionHeight));
        detailsScroller = new JScrollPane();
        setDetailsTable(new String[0][0]);
        panel.add(detailsScroller);
        return panel;
    }

    public void setDetailsTable(String[][] data) {
        detailsTableData = data;
        detailsTable = new JTable(new DefaultTableModel(detailsTableData, detailsColumnNames) {
			private static final long serialVersionUID = -5147240233632955602L;

			@Override
        	public boolean isCellEditable(int row, int column) {
        		return false;
        	}
        });
        detailsTable.setFillsViewportHeight(true);
        detailsTable.setColumnSelectionAllowed(false);
        detailsTable.setDragEnabled(false);
        detailsScroller.setViewportView(detailsTable);
    }

    public void fillSubcategories(String[] cats) {
        subCategoriesList = new JList<String>(cats);
        subCategoriesList.setLayoutOrientation(JList.VERTICAL);
        subCategoriesList.setVisibleRowCount(-1);
        subCategoriesScroller.setViewportView(subCategoriesList);
    }

    public void fillAttributes(String[] attrs) {
        attributesList = new JList<String>(attrs);
        attributesList.setLayoutOrientation(JList.VERTICAL);
        attributesList.setVisibleRowCount(-1);
        attributesScroller.setViewportView(attributesList);
    }
    
    public void fillLocations(String[] locs) {
    	locationDropdown.removeAllItems();
    	locationDropdown.addItem(locationDefaultString);
    	for(String loc : locs) {
    		locationDropdown.addItem(loc);
    	}
    }
    
    public void reset() {
    	fillSubcategories(new String[0]);
    	fillAttributes(new String[0]);
    	setDetailsTable(new String[0][0]);
    }
}
