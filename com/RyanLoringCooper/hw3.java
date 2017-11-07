package com.RyanLoringCooper;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class hw3 implements ActionListener {
	public static final String searchActionString = "Search", closeActionString = "Close";
	private static UserInterface ui = null;
	private static Connection conn = null;
	private String hostname = "192.168.1.151", port = "5002", dbName = "XE";
	private boolean debug = true;

    public hw3() {
        ui = new UserInterface(this);
        conn = Util.setupDatabaseConnection(hostname, port, dbName, debug);
    }
    
    private String getCategoryWhere(List<String> mainCatsSelected, List<String> subCatsSelected) {
    	String query = "categories = categoryTable(";
    	for(int i = 0; i < mainCatsSelected.size(); i++) {
			query += "'" + mainCatsSelected.get(i)+ "'";
			if(i != mainCatsSelected.size()-1) {
				query += ",";
			}
		}
		if(subCatsSelected.size() > 0) {
			query += ",";
		}
		for(int i = 0; i < subCatsSelected.size(); i++) {
			query += "'" + subCatsSelected.get(i) + "'";
			if(i != subCatsSelected.size()-1) {
				query += ",";
			}
		}
		return query;
    }

    private String getDetailsQuery(List<String> mainCatsSelected, List<String> subCatsSelected, List<String> attributesSelected, String dayChose, String fromChose, String toChosen, String searchForChosen) {
    	// TODO this does not hand cases of businesses who's categories aren't totally selected
    	String query = "SELECT b.name, b.city, b.state, b.stars "
    				+  "FROM Business b, table(b.categories) categories, table(b.attributes) attributes "
    				+  "WHERE ";
		query += getCategoryWhere(mainCatsSelected, subCatsSelected);
		query += ") AND attributes = attributeTable(";
		for(int i = 0; i < attributesSelected.size(); i++) {
			String attr = attributesSelected.get(i);
			String[] attrs = attr.split("_");
			query += "attribute_type('" + attrs[0] + "','" + attrs[1] + "')";
			if(i != mainCatsSelected.size()-1) {
				query += ",";
			}
			
		}
		query += ");";
		return query;
    }
    
    private String getAttributesQuery(List<String> mainCatsSelected, List<String> subCatsSelected, String dayChose, String fromChose, String toChosen, String searchForChosen) {
        if(mainCatsSelected > 0 && subCatsSelected > 0) {
            String query = "SELECT DISTINCT b.attributes "
                        +  "FROM Business b, Category c"
                        +  "WHERE b.business_id = c.business AND ";
            for(int i = 0; i < mainCatsSelected.size(); i++) {
                query += "c.name = '" + mainCatsSelected.get(i) + "' ";
                if(i != mainCatsSelected.size()-1) {
                    query += searchForChosen + " ";
                }
            }
            for(int i = 0; i < subCatsSelected.size(); i++) {
                query += "c.name = '" + subCatsSelected.get(i) + "' ";
                if(i != subCatsSelected.size()-1) {
                    query += searchForChosen + " ";
                }
            }
            return query;
        } else {
            return null;
        }
    }
    
    private String getSubCatsQuery(List<String> mainCatsSelected, String dayChose, String fromChose, String toChosen, String searchForChosen) {
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
            query += " AND c.business != cat.business) busWithCats WHERE ";
            for(int i = 0; i < mainCatsSelected.size(); i++) {
                query += "c.name = '" + mainCatsSelected.get(i) + "' ";
                if(i != mainCatsSelected.size()-1) {
                    if(searchForChosen.equals("AND") {
                        query += "OR ";
                    } else {
                        query += "AND ";
                    }
                } 
            }
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
			handleDetailsQuery(getDetailsQuery(mainCatsSelected, subCatsSelected, attributesSelected, dayChosen, fromChosen, toChosen, saerchForChosen));
		} else if(!subCatsSelected.isEmpty()) {
			handleAttributesQuery(getAttributesQuery(mainCatsSelected, subCatsSelected, dayChosen, fromChosen, toChosen, searchForChosen));
		} else if(!mainCatsSelected.isEmpty()) {
			handleSubCatsQuery(getSubCatsQuery(mainCatsSelected, dayChosen, fromChosen, toChosen, searchForChosen));
		}
    }
    
    private ResultSet executeQuery(String query) {
    	Statement statement;
		try {
			statement = conn.createStatement();
			return statement.executeQuery(query);
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return null;
    }

	private void handleDetailsQuery(String detailsQuery) {
        if(detailsQuery == null) {
            return;
        }
		ResultSet rs = executeQuery(detailsQuery);
		if(rs != null) {
			try {
				while(rs.next()) {
					// TODO
				}
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
	}
	
	private void handleAttributesQuery(String attributesQuery) {
        if(attributesQuery == null) {
            return;
        }
		ResultSet rs = executeQuery(attributesQuery);
		if(rs != null) {
			// TODO make sure you replace attributes with _ between values
			try {
				while(rs.next()) {
					// TODO
				}
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
	}

    private void handleSubCatsQuery(String subCatsQuery) {
        if(subCatsQuery == null) {
            return;
        }
    	ResultSet rs = executeQuery(subCatsQuery);
    	if(rs != null) {
    		try {
				while(rs.next()) {
					// TODO
				}
			} catch (SQLException e) {
				e.printStackTrace();
			}
    	}
	}

	static void terminate() {
    	if(conn != null) {
    		try {
				conn.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
    	}
    	System.exit(0);
    }
    
    public void actionPerformed(ActionEvent e) {
        if(searchActionString.equals(e.getActionCommand())) {
            executeSearch();
        } else if(closeActionString.equals(e.getActionCommand())) {
        	terminate();
        }
    }
    
    public static void main(String[] args) {
        new hw3();
    }
}
