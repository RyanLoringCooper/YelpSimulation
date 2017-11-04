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
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class UserInterface extends JFrame implements ActionListener {
    
    public static final long serialVerisonUID = 7335319432115237346L;
    private static final String[] mainCategories = {"Active Life", "Arts & Entertainment", "Automotive", "Car Rental", "Cafes", "Beauty & Spas", "Conveniece Stores", "Dentists", "Doctors", "Drugstores", "Department Stores", "Education", "Event Planning & Services", "Flowers & Gifts", "Food", "Health & Medical", "Home Services", "Home & Garden", "Hospitals", "Hotels & Travel", "Hardware Stores", "Grocery", "Medical Centers", "Nurseries & Gardening", "Nightlife", "Restaurants", "Shopping", "Transportation"};
    private static final String[] daysOfTheWeek = {"Any", "Sunday", "Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday"};
    private static final String[] hoursOfTheDay = {"12:00AM", "1:00AM", "2:00AM", "3:00AM", "4:00AM", "5:00AM", "6:00AM", "7:00AM", "8:00AM", "9:00AM", "10:00AM", "11:00AM", "12:00PM", "1:00PM", "2:00PM", "3:00PM", "4:00PM", "5:00PM", "6:00PM", "7:00PM", "8:00PM", "9:00PM", "10:00PM", "11:00PM"};
    private static final String[] searchForOptions = {""}; // TODO 
    private static final String searchActionString = "Search", closeActionString = "Close";
    private static final int headerHeight = 12, headerMaxWidth = 60;
    // modify these by calling *scroller.setViewportView(new JList<JTextArea>(JTextArea[] info));
    private JScrollPane subCategoriesScroller, attributesScroller, detailsScroller;
    private JList<JTextArea> mainCategoriesList, subCategoriesList, attributesList, detailsList;
    private JComboBox<JTextArea> weekDayDropdown, fromHoursDropdown, toHoursDropdown, searchForDropdown;

    public UserInterface() {
        setupFirstWindow();
        add(firstView());
    }

    private void setupFirstWindow() {
        setName("Yelp Simulation");
        setSize(1000, 700);
        setLocation(10,20);
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
        weekDayDropdown = getDropdown(daysOfTheWeek);
        weekDay.add(weekDayDropdown); 

        JPanel fromHours = new JPanel();
        fromHours.setLayout(new BoxLayout(fromHours, BoxLayout.Y_AXIS));
        fromHours.add(new JTextArea("From:"));
        fromHoursDropdown = getDropdown(hoursOfTheDay);
        fromHours.add(fromHoursDropdown);

        JPanel toHours = new JPanel();
        toHours.setLayout(new BoxLayout(toHours, BoxLayout.Y_AXIS));
        toHours.add(new JTextArea("To:"));
        List<String> reverseHours = Array.asList(hoursOfTheDay);
        Collections.reverse(reverseHours);
        toHoursDropdown = getDropdown(reverseHours.toArray(new String [reverseHours.size()]));
        toHours.add(toHoursDropdown);

        JPanel searchFor = new JPanel();
        searchFor.setLayout(new BoxLayout(searchFor, BoxLayout.Y_AXIS));
        searchFor.add(new JTextArea("Search for:"));
        searchForDropdown = getDropdown(searchForOptions);
        searchFor.add(searchForDropdown);

        JButton searchButton = new JButton(searchActionString);
        searchButton.setActionCommand(searchActionString);
        searchButton.addActionListener(this);

        JButton closeButton = new JButton(closeActionString);
        closeButton.setActionCommand(closeActionString);
        closeButton.addActionListener(this);

        panel.add(weekDay);
        panel.add(fromHours);
        panel.add(toHours);
        panel.add(searchFor);
        panel.add(searchButton);
        panel.add(closeButton);
        return panel;
    }

    private JComboBox getDropdown(String[] s) {
        JTextArea days = new JTextArea[s.length];
        for(int i = 0; i < s.length; i++) {
            days = new JTextArea(s[i]);
        }
        return new JComboBox<JTextArea>(days);
    }

    // shows the various categories that can be used to narrow things down
    private JPanel getListsPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.X_AXIS));
        panel.add(getListOfMainCategories());
        subCategoriesList = new JList<TextArea>();
        subCategoriesScroller = new JScrollPane(subCategoriesList);
        panel.add(subCategoriesScroller);
        attributesList = new JList<TextArea>();
        attributesScroller = new JScrollPane(attributesList);
        panel.add(attributesScroller);
        return panel;
    }

    private JScrollPane getListOfMainCategories() {
        JTextArea[] textAreas = new JTextArea[mainCategories.length];
        for(int i = 0; i < mainCategories.length; i++) {
            textAreas[i] = new JTextArea(mainCategories[i]);
        }
        mainCategoriesList = new JList<JTextArea>(textAreas);
        mainCategoriesList.setLayoutOrientation(JList.VERTICAL);
        mainCategoriesList.setVisibleRowCount(-1);
        JScrollPane listScroller = new JScrollPane(mainCategoriesList);
        listScroller.setPreferredSize(new Dimension(250, 500));
        return listScroller;
    }

    private JPanel getDetailsHeader() {
        JPanel detailsHeader = new JPanel();
        detailsHeader.setLayout(new BoxLayout(detailsHeader, BoxLayout.X_AXIS));
        JTextArea business = new JTextArea("Business");
        JTextArea city = new JTextArea("City");
        JTextArea state = new JTextArea("State");
        JTextArea stars = new JTextArea("Stars");
        business.setPreferredSize(new Dimension(headerHeight, headerMaxWidth));
        city.setPreferredSize(new Dimension(headerHeight, headerMaxWidth/2));
        state.setPreferredSize(new Dimension(headerHeight, headerMaxWidth/4));
        stars.setPreferredSize(new Dimension(headerHeight, headerMaxWidth/4));
        detailsHeader.add(business);
        detailsHeader.add(city);
        detailsHeader.add(state);
        detailsHeader.add(stars);
        return detailsHeader;
    }

    // panel that displays reviews about a selected business
    private JPanel getDetailsPanel() {
        JPanel panel = new JPanel();
        detailsScroller = new JScrollPane(new JPanel());
        detailsScroller.setColumnHeaderView(getDetailsHeader());
        panel.add(detailsScroller);
        return panel;
    }

    private void executeSearch() {
        List<JTextArea> mainCatsSelected = mainCategoriesList.getSelectedValuesList();
        List<JTextArea> subCatsSelected = subCategoriesList.getSelectedValuesList();
        List<JTextArea> attributesSelected = attributesList.getSelectedValuesList();
        JTextArea dayChosen = weekDayDropdown.getSelectedItem();
        JTextArea fromChosen = fromHoursDropdown.getSelectedItem();
        JTextArea toChose = toHoursDropdown.getSelectedItem();
        // TODO
        if(attributesSelected.isEmpty()) {

        } else if(subCatsSelected.isEmpty()) {

        } else {

        }
    }

    public void actionPerformed(ActionEvent e) {
        if(searchActionString.equals(e.getActionCommand())) {
            executeSearch();
        } else if(closeActionString.equals(e.getActionCommand())) {
            // TODO preform close operation
        }
    }
}
