package com.RyanLoringCooper;

import javax.swing.JFrame;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.BoxLayout;
import javax.swing.JScrollPane;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JTextArea;

import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class UserInterface extends JFrame {
    
	private static final long serialVersionUID = -668103667213956930L;
    private static final String[] mainCategories = {"Active Life", "Arts & Entertainment", "Automotive", "Car Rental", "Cafes", "Beauty & Spas", "Conveniece Stores", "Dentists", "Doctors", "Drugstores", "Department Stores", "Education", "Event Planning & Services", "Flowers & Gifts", "Food", "Health & Medical", "Home Services", "Home & Garden", "Hospitals", "Hotels & Travel", "Hardware Stores", "Grocery", "Medical Centers", "Nurseries & Gardening", "Nightlife", "Restaurants", "Shopping", "Transportation"};
    private static final String[] daysOfTheWeek = {"Any", "Sunday", "Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday"};
    private static final String[] hoursOfTheDay = {"12:00AM", "1:00AM", "2:00AM", "3:00AM", "4:00AM", "5:00AM", "6:00AM", "7:00AM", "8:00AM", "9:00AM", "10:00AM", "11:00AM", "12:00PM", "1:00PM", "2:00PM", "3:00PM", "4:00PM", "5:00PM", "6:00PM", "7:00PM", "8:00PM", "9:00PM", "10:00PM", "11:00PM"};
    private static final String[] searchForOptions = {"AND", "OR"}; 
    private static final int detailsHeaderHeight = 12, detailsHeaderMaxWidth = 160, mainSectionHeight = 500;
    // modify these by calling *scroller.setViewportView(new JList<JTextArea>(JTextArea[] info));
    protected JScrollPane subCategoriesScroller, attributesScroller, detailsScroller;
    protected JList<String> mainCategoriesList, subCategoriesList, attributesList;
    protected JList<JPanel> detailsList;
    protected JComboBox<String> weekDayDropdown, fromHoursDropdown, toHoursDropdown, searchForDropdown;
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
        setName("Yelp Simulation");
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
    private JPanel getDropdownsPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.X_AXIS));

        JPanel weekDay = new JPanel();
        weekDay.setLayout(new BoxLayout(weekDay, BoxLayout.Y_AXIS));
        weekDay.add(new JTextArea("Day of the week:"));
        weekDayDropdown = new JComboBox<String>(daysOfTheWeek);
        weekDay.add(weekDayDropdown); 

        JPanel fromHours = new JPanel();
        fromHours.setLayout(new BoxLayout(fromHours, BoxLayout.Y_AXIS));
        fromHours.add(new JTextArea("From:"));
        fromHoursDropdown = new JComboBox<String>(hoursOfTheDay);
        fromHours.add(fromHoursDropdown);

        JPanel toHours = new JPanel();
        toHours.setLayout(new BoxLayout(toHours, BoxLayout.Y_AXIS));
        toHours.add(new JTextArea("To:"));
        List<String> reverseHours = Arrays.asList(hoursOfTheDay);
        Collections.reverse(reverseHours);
        toHoursDropdown = new JComboBox<String>(reverseHours.toArray(new String [reverseHours.size()]));
        toHours.add(toHoursDropdown);

        JPanel searchFor = new JPanel();
        searchFor.setLayout(new BoxLayout(searchFor, BoxLayout.Y_AXIS));
        searchFor.add(new JTextArea("Search for:"));
        searchForDropdown = new JComboBox<String>(searchForOptions);
        searchFor.add(searchForDropdown);

        JButton searchButton = new JButton(hw3.searchActionString);
        searchButton.setActionCommand(hw3.searchActionString);
        searchButton.addActionListener(al);

        JButton closeButton = new JButton(hw3.closeActionString);
        closeButton.setActionCommand(hw3.closeActionString);
        closeButton.addActionListener(al);

        panel.add(weekDay);
        panel.add(fromHours);
        panel.add(toHours);
        panel.add(searchFor);
        panel.add(searchButton);
        panel.add(closeButton);
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

    private JPanel getDetailsRow(String  one, String two, String three, String four) {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.X_AXIS));
        JTextArea business = new JTextArea(one);
        JTextArea city = new JTextArea(two);
        JTextArea state = new JTextArea(three);
        JTextArea stars = new JTextArea(four);
        business.setPreferredSize(new Dimension(detailsHeaderMaxWidth, detailsHeaderHeight));
        city.setPreferredSize(new Dimension(detailsHeaderMaxWidth/2, detailsHeaderHeight));
        state.setPreferredSize(new Dimension(detailsHeaderMaxWidth/4, detailsHeaderHeight));
        stars.setPreferredSize(new Dimension(detailsHeaderMaxWidth/4, detailsHeaderHeight));
        panel.add(business);
        panel.add(city);
        panel.add(state);
        panel.add(stars);
        return panel;
    }

    // panel that displays reviews about a selected business
    private JPanel getDetailsPanel() {
        JPanel panel = new JPanel();
        panel.setPreferredSize(new Dimension(detailsHeaderMaxWidth*4, mainSectionHeight));
        JPanel scrollerPanel = new JPanel();
        scrollerPanel.setPreferredSize(new Dimension(detailsHeaderMaxWidth*4, mainSectionHeight));
        detailsScroller = new JScrollPane(scrollerPanel);
        detailsScroller.setColumnHeaderView(getDetailsRow("Business", "City", "State", "Stars"));
        panel.add(detailsScroller);
        return panel;
    }

    public void fillDetails(ArrayList<String> names, ArrayList<String> cities, ArrayList<String> states, ArrayList<String> stars) {
        detailsList = new JList<JPanel>();
        for(int i = 0; i < names.size(); i++) {
            detailsList.add(getDetailsRow(names.get(i), cities.get(i), states.get(i), stars.get(i)));
        }
        detailsScroller.setViewportView(detailsList);
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
}
