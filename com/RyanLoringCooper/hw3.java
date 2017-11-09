package com.RyanLoringCooper;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.swing.JDialog;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class hw3 implements ActionListener {
	
	public static final String className = "hw3";
	public static final String searchActionString = "Search", closeActionString = "Close";
	private static UserInterface ui = null;
	private static Connection conn = null;
	private ArgumentParser argParser;

    public hw3(String[] args) {
    	argParser = new ArgumentParser(args, className);
    	if(argParser.wasValid()) {
			ui = new UserInterface(this);
			conn = Util.setupDatabaseConnection(argParser);
    	}
    }

    private String getDetailsQuery(List<String> mainCatsSelected, List<String> subCatsSelected, List<String> attributesSelected, String dayChose, String fromChose, String toChosen, String searchForChosen) {
        if(mainCatsSelected.size() > 0 && subCatsSelected.size() > 0 && attributesSelected.size() > 0) {
    	    String query = "SELECT b.name, b.city, b.state, b.stars "
                         + getQueryAttributesMeat(mainCatsSelected, subCatsSelected, dayChose, fromChose, toChosen, searchForChosen);
            for(int i = 0; i < attributesSelected.size(); i++) {
                String[] attrs = attributesSelected.get(i).split("=");
                query += " " + searchForChosen + " attrs.attr = '" + attrs[0] + "' AND attrs.value = '" + attrs[1] + "'";
            }
            return query;
        } else {
            return null;
        }
    }
    
    private String getAttributesQuery(List<String> mainCatsSelected, List<String> subCatsSelected, String dayChose, String fromChose, String toChosen, String searchForChosen) {
        if(mainCatsSelected.size() > 0 && subCatsSelected.size() > 0) {
            return "SELECT DISTINCT attrs.attr, attrs.value " 
                  + getQueryAttributesMeat(mainCatsSelected, subCatsSelected, dayChose, fromChose, toChosen, searchForChosen);
        } else {
            return null;
        }
    }

    private String getQueryAttributesMeat(List<String> mainCatsSelected, List<String> subCatsSelected, String dayChosen, String fromChosen, String toChosen, String searchForChosen) {
        if(mainCatsSelected.size() > 0 && subCatsSelected.size() > 0) {
            String query = "FROM Business b, table(b.attributes) attrs";
            for(int i = 0; i < mainCatsSelected.size()+subCatsSelected.size(); i++) {
                query += ", Category c" + Integer.toString(i);
            }
            query += " WHERE ";
            for(int i = 0; i < mainCatsSelected.size()+subCatsSelected.size(); i++) {
                query += "b.business_id = c" + Integer.toString(i) + ".business AND ";
            }
            int i;
            for(i = 0; i < mainCatsSelected.size(); i++) {
                query += "c" + Integer.toString(i) + ".name = '" + mainCatsSelected.get(i) + "' " + searchForChosen + " ";
            }
            for(; i < subCatsSelected.size()+mainCatsSelected.size(); i++) {
                query += "c" + Integer.toString(i) + ".name = '" + subCatsSelected.get(i-mainCatsSelected.size()) + "' ";
                if(i != mainCatsSelected.size()+subCatsSelected.size()-1) {
                    query += searchForChosen + " ";
                }
            }
            return query;
        } else {
            return null;
        }
    }
    
    private String getSubCatsQuery(List<String> mainCatsSelected, String searchForChosen) {
        if(mainCatsSelected.size() > 0) {
            String query = "SELECT DISTINCT busWithCats.name "
                         + "FROM Category c, ( "
                            + "SELECT * " 
                            + "FROM Category cat "
                            + "WHERE ";
            for(int i = 0; i < mainCatsSelected.size(); i++) {
                query += "cat.name != '" + mainCatsSelected.get(i) + "' ";
                if(i != mainCatsSelected.size()-1) {
                    query += searchForChosen + " ";
                } 
            }
            query += ") busWithCats WHERE ";
            for(int i = 0; i < mainCatsSelected.size(); i++) {
                query += "c.name = '" + mainCatsSelected.get(i) + "' ";
                if(i != mainCatsSelected.size()-1) {
                    if(searchForChosen.equals("AND")) {
                        query += "OR ";
                    } else {
                        query += "AND ";
                    }
                } 
            }
            query += " AND c.business = busWithCats.business";
            return query;
        } else {
            return null;
        }
    }
    
    private void executeSearch() {
        List<String> mainCatsSelected = ui.mainCategoriesList.getSelectedValuesList();
        List<String> subCatsSelected = ui.subCategoriesList.getSelectedValuesList();
        List<String> attributesSelected = ui.attributesList.getSelectedValuesList();
        String dayChosen = (String) ui.weekDayDropdown.getSelectedItem();
        String fromChosen = (String) ui.fromHoursDropdown.getSelectedItem();
        String toChosen = (String) ui.toHoursDropdown.getSelectedItem();
        String searchForChosen = (String) ui.searchForDropdown.getSelectedItem();
		if(!attributesSelected.isEmpty()) {
			handleDetailsQuery(getDetailsQuery(mainCatsSelected, subCatsSelected, attributesSelected, dayChosen, fromChosen, toChosen, searchForChosen));
		} else if(!subCatsSelected.isEmpty()) {
			handleAttributesQuery(getAttributesQuery(mainCatsSelected, subCatsSelected, dayChosen, fromChosen, toChosen, searchForChosen));
		} else if(!mainCatsSelected.isEmpty()) {
			handleSubCatsQuery(getSubCatsQuery(mainCatsSelected, searchForChosen));
		}
    }
    
    private ResultSet executeQuery(String query) {
    	Statement statement;
		try {
			statement = conn.createStatement();
			if(argParser.debug()) {
				System.out.println(query);
			}
			return statement.executeQuery(query);
		} catch (SQLException e) {
			Util.handleSQLException(e);
		}
		return null;
    }

	private void handleDetailsQuery(String detailsQuery) {
        if(detailsQuery == null) {
            return;
        }
        ArrayList<String> names = new ArrayList<String>();
        ArrayList<String> cities = new ArrayList<String>();
        ArrayList<String> states = new ArrayList<String>();
        ArrayList<String> stars = new ArrayList<String>();
		ResultSet rs = executeQuery(detailsQuery);
		if(rs != null) {
			try {
				while(rs.next()) {
					names.add(rs.getString(1));
                    cities.add(rs.getString(2));
                    states.add(rs.getString(3));
                    stars.add(rs.getString(4));
				}
			} catch (SQLException e) {
				Util.handleSQLException(e);
			}
		}
        ui.fillDetails(names, cities, states, stars);
	}
	
	private void handleAttributesQuery(String attributesQuery) {
        if(attributesQuery == null) {
            return;
        }
        ArrayList<String> attributes = new ArrayList<String>();
		ResultSet rs = executeQuery(attributesQuery);
		if(rs != null) {
			try {
				while(rs.next()) {
                    attributes.add(rs.getString(1) + "=" + rs.getString(2));
				}
			} catch (SQLException e) {
				Util.handleSQLException(e);
			}
		}
        String[] attrs = attributes.toArray(new String[attributes.size()]);
        Arrays.sort(attrs);
        ui.fillAttributes(attrs);
	}

    private void handleSubCatsQuery(String subCatsQuery) {
        if(subCatsQuery == null) {
            return;
        }
        ArrayList<String> categories = new ArrayList<String>();
    	ResultSet rs = executeQuery(subCatsQuery);
    	if(rs != null) {
    		try {
				while(rs.next()) {
                    categories.add(rs.getString(1));
				}
			} catch (SQLException e) {
				Util.handleSQLException(e);
			}
    	}
        String[] cats = categories.toArray(new String[categories.size()]);
        Arrays.sort(cats);
        ui.fillSubcategories(cats);
	}

	static void terminate() {
    	if(conn != null) {
    		try {
				conn.close();
			} catch (SQLException e) {
				Util.handleSQLException(e);
			}
    	}
    	System.exit(0);
    }
    
    public void actionPerformed(ActionEvent e) {
        if(searchActionString.equals(e.getActionCommand())) {
        	LoadingPopup lp = new LoadingPopup(ui);
            executeSearch();
            lp.close();
        } else if(closeActionString.equals(e.getActionCommand())) {
        	terminate();
        }
    }
    
    public static void main(String[] args) {
        new hw3(args);
    }
}
