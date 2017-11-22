package com.RyanLoringCooper;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class hw3 implements ActionListener {
	
	public static final String className = "hw3";
	public static final String searchActionString = "Search", closeActionString = "Close", resetActionString = "Reset", backActionString = "Back";
	private static UserInterface ui = null;
	private static Connection conn = null;
	private ArgumentParser argParser;
	static int numSearches = 0;

    public hw3(String[] args) {
    	argParser = new ArgumentParser(args, className);
    	if(argParser.wasValid()) {
			ui = new UserInterface(this);
			conn = Util.setupDatabaseConnection(argParser);
    	}
    }

    private String getDetailsQuery(List<String> mainCatsSelected, List<String> subCatsSelected, List<String> attributesSelected, String dayChosen, String fromChosen, String toChosen, String locationChosen, String searchForChosen) {
        if(mainCatsSelected.size() > 0) {
    	    String operationSelect = "SELECT DISTINCT hours.day day, hours.open open, hours.close close, b.business_id business_id ";
    	    String query = getLocationsMeat(mainCatsSelected, subCatsSelected, attributesSelected, searchForChosen)
    	    		+ ", A AS (" + getLocationsMainQuery(attributesSelected, searchForChosen, operationSelect, ", table(b.hours) hours ") + getLocationWhere(locationChosen)
    	    		+ ") SELECT DISTINCT b.name, b.city, b.state, b.stars FROM Business b, A a WHERE b.business_id = a.business_id";
    	    if(!dayChosen.equals("Any")) {
    	    	query += " AND A.day = '" + dayChosen + "'";
    	    }
    	    if(!fromChosen.equals("Any")) {
    	    	query += " AND TO_DATE(A.open, 'HH24:MI') < TO_DATE('" + fromChosen + "', 'HH24:MI')";
    	    }
    	    if(!toChosen.equals("Any")) {
    	    	query += " AND TO_DATE(A.close, 'HH24:MI') > TO_DATE('" + toChosen + "', 'HH24:MI')";
    	    }
    	    return query;
        }
        return null;
    }
    
    private String getLocationWhere(String locationChosen) {
    	String locationWhere = "";
		if(!locationChosen.equals("Any")) {
			String[] loc = locationChosen.split(",");
			locationWhere = " AND b.city = '" + Util.cleanString(loc[0]) + "' AND b.state = '" + Util.cleanString(loc[1]) + "'";
		}
		return locationWhere;
    }
    
    private String getOperationsQuery(List<String> mainCatsSelected, List<String> subCatsSelected, List<String> attributesSelected, String locationChosen, String searchForChosen) {
    	if(mainCatsSelected.size() > 0) {
    		String select = "SELECT DISTINCT hours.day day, hours.open open, hours.close close ";
    		
    		String query = getLocationsMeat(mainCatsSelected, subCatsSelected, attributesSelected, searchForChosen)
    					+ getLocationsMainQuery(attributesSelected, searchForChosen, select, ", table(b.hours) hours ") + getLocationWhere(locationChosen);
			return query;
    	} 
    	return null;
    }
    
    private String getLocationsQuery(List<String> mainCatsSelected, List<String> subCatsSelected, List<String> attributesSelected, String searchForChosen) {
    		String desiredSelect = "SELECT DISTINCT b.city, b.state ";
    		return getLocationsMeat(mainCatsSelected, subCatsSelected, attributesSelected, searchForChosen) 
    				+ getLocationsMainQuery(attributesSelected, searchForChosen, desiredSelect, "");
    }
    
    private String getLocationsMainQuery(List<String> attributesSelected, String searchForChosen, String select, String extraFrom) {
    	String query = "";
		if(attributesSelected.size() > 0) {
			query += select
					+ "FROM Business b, ";
			for(int i = 0; i < attributesSelected.size(); i++) {
				query += "B" + Integer.toString(i) + " b" + Integer.toString(i);
				if(i != attributesSelected.size()-1) {
					query += ", ";
				} 
			}
			query += extraFrom + " WHERE ";
			for(int i = 0; i < attributesSelected.size(); i++) {
				query += "b.business_id = b" + Integer.toString(i) + ".business_id";
				if(i != attributesSelected.size()-1) {
					query += " " + searchForChosen + " ";
				}
			}
		} else {
			query += select
					+ "FROM Business b, Cats c " + extraFrom
					+ "WHERE b.business_id = c.business";
		}
		return query;
    }
    
    private String getLocationsMeat(List<String> mainCatsSelected, List<String> subCatsSelected, List<String> attributesSelected, String searchForChosen) {
    	if(mainCatsSelected.size() > 0) {
    		String query = getCats(mainCatsSelected, subCatsSelected, searchForChosen);
    		if(attributesSelected.size() > 0) {
				query +=  ", Attrs AS ("
						+ "SELECT DISTINCT attrs.attr attr, attrs.value value, b.business_id business_id "
						+ "FROM Business b, table(b.attributes) attrs, Cats c "
						+ "WHERE c.business = b.business_id)";
				for(int i = 0; i < attributesSelected.size(); i++) {
					String[] attrs = attributesSelected.get(i).split("=");
					query +=  ", B" + Integer.toString(i) + " AS ( "
							+ "SELECT DISTINCT b.business_id business_id "
							+ "FROM Business b, Attrs attrs "
							+ "WHERE b.business_id = attrs.business_id "
							+ "AND attrs.attr = '" + Util.cleanString(attrs[0]) + "' AND attrs.value = '" + Util.cleanString(attrs[1]) + "') ";
				}
    		}
			return query;
    	}
    	return null;
    }

    private String getBusinessInCatsQuery(List<String> cats) {
    	String query = "SELECT DISTINCT c0.business business FROM";
    	for(int i = 0; i < cats.size(); i++) {
    		query += " Category c" + Integer.toString(i);
		   if(i != cats.size()-1) {
			   query += ",";
		   }
    	} 
    	query += " WHERE";
    	for(int i = 0; i < cats.size()-1; i++) {
    		query += " c" + Integer.toString(i) + ".business = c" + Integer.toString(i+1) + ".business"
    				+ " AND c" + Integer.toString(i) + ".name = '" + Util.cleanString(cats.get(i)) + "' AND";
		   
    	}
    	query += " c" + Integer.toString(cats.size()-1) + ".name = '" + Util.cleanString(cats.get(cats.size()-1)) +"'";
    	return query;
    }
    
    private ArrayList<ArrayList<String>> getCatPairs(List<String> mainCatsSelected, List<String> subCatsSelected) {
    	ArrayList<ArrayList<String>> catPairs = new ArrayList<ArrayList<String>>();
	   for(int i = 0; i < mainCatsSelected.size(); i++) {
		   for(int j = 0; j < subCatsSelected.size(); j++) {
			   ArrayList<String> pair = new ArrayList<String>();
			   pair.add(mainCatsSelected.get(i));
			   pair.add(subCatsSelected.get(j));
			   catPairs.add(pair);
		   }
	   }
	   return catPairs;
    }
    
    private String getAttributesQuery(List<String> mainCatsSelected, List<String> subCatsSelected, String searchForChosen) {
   		if(mainCatsSelected.size() > 0) {
   			String query = getCats(mainCatsSelected, subCatsSelected, searchForChosen) 
   					+ "SELECT DISTINCT attrs.attr, attrs.value "
   					+ "FROM Business b, table(b.attributes) attrs, Cats c "
   					+ "WHERE c.business = b.business_id";
			return query;
   		}
		return null;
    }
    
    	
    private String getCats(List<String> mainCatsSelected, List<String> subCatsSelected, String searchForChosen) {
    	String query = "WITH Cats AS ( ";
		if(searchForChosen == "AND") {
			mainCatsSelected.addAll(subCatsSelected);
			query += getBusinessInCatsQuery(mainCatsSelected);
		} else {
			if(subCatsSelected.size() == 0) {
				for(int i = 0; i < mainCatsSelected.size(); i++) {
					ArrayList<String> temp = new ArrayList<String>();
					temp.add(mainCatsSelected.get(i));
					query += getBusinessInCatsQuery(temp);
					if(i != mainCatsSelected.size()-1) {
						query += " UNION ";
					}
				}
			} else {
		   ArrayList<ArrayList<String>> catPairs = getCatPairs(mainCatsSelected, subCatsSelected);
		   for(int i = 0; i < catPairs.size(); i++) {
			   query += getBusinessInCatsQuery(catPairs.get(i));
			   if(i != catPairs.size()-1) {
				   query += " UNION ";
			   }
		   }
			}
		}
		query += ") "; 
		return query;
    }
    
    private String getSubCatsQueryAndFrom(List<String> mainCatsSelected) {
        if(mainCatsSelected.size() > 0) {
            String query = "FROM ";
            for(int i = 0; i < mainCatsSelected.size(); i++) {
            	query += "Category c" + Integer.toString(i) + ", ";
            }
            query += "Category subCats WHERE ";
            for(int i = 0; i < mainCatsSelected.size(); i++) {
            	query += "subCats.business = c" + Integer.toString(i) + ".business AND ";
            	query += "subCats.name != c" + Integer.toString(i) + ".name AND "; 
            	query += "c" + Integer.toString(i) + ".name = '" + Util.cleanString(mainCatsSelected.get(i)) + "' ";
            	if(i != mainCatsSelected.size()-1) {
            		query += "AND ";
            	}
            }
            return query;
        } 
		return null;
    }
    
    private String getSubCatsQueryOrFrom(List<String> mainCatsSelected, String select) {
    	String query = "";
    	for(int i = 0; i < mainCatsSelected.size(); i++) {
			ArrayList<String> cat = new ArrayList<String>();
			cat.add(mainCatsSelected.get(i));
			query += select + getSubCatsQueryAndFrom(cat);
			if(i != mainCatsSelected.size()-1) {
				query += " UNION ";
			}
			
		}
    	return query;
    }

    private String getSubCatsQuery(List<String> mainCatsSelected, String searchForChosen) {
    	String select = "SELECT DISTINCT subCats.name ";
    	String query;
    	if(searchForChosen == "AND") {
    		query = select + getSubCatsQueryAndFrom(mainCatsSelected);
    	} else {
    		query = getSubCatsQueryOrFrom(mainCatsSelected, select);
    	}
    	return query;
    }
    
    private String getBusinessID(String[] businessChosen) {
        if(businessChosen != null && businessChosen.length == 4) {
            String q = "SELECT DISTINCT b.business_id FROM Business b WHERE " 
                     + "b.name = '" + Util.cleanString(businessChosen[0]) + "' "
                     + "AND b.city = '" + Util.cleanString(businessChosen[1]) + "' "
                     + "AND b.state = '" + Util.cleanString(businessChosen[2]) + "' "
                     + "AND b.stars = '" + Util.cleanString(businessChosen[3]) + "'";
            ResultSet rs = executeQuery(q);
            String retval = null;
            if(rs != null) {
                try {
                    while(rs.next()) {
                        retval = rs.getString(1);
                    }
				    rs.close();
                } catch (SQLException e) {
                    Util.handleSQLException(e);
                }
            }
            return retval;
        }
        return null;    
    }

    private String getReviewsQuery(String[] businessChosen) {
        String bid = getBusinessID(businessChosen);
        if(bid != null) {
            String query = "SELECT r.date_field, r.stars, r.text, r.user_id, r.votes "
                         + "FROM Review r WHERE r.business_id = '" + Util.cleanString(bid) + "'";
            return query;
        }
        return null;
    }

    public int executeSearch() {
        List<String> mainCatsSelected = ui.mainCategoriesList.getSelectedValuesList();
		if(!mainCatsSelected.isEmpty()) {
			numSearches++;
			List<String> subCatsSelected = ui.subCategoriesList.getSelectedValuesList();
			List<String> attributesSelected = ui.attributesList.getSelectedValuesList();
			String dayChosen = (String) ui.weekDayDropdown.getSelectedItem();
			String fromChosen = (String) ui.fromHoursDropdown.getSelectedItem();
			String toChosen = (String) ui.toHoursDropdown.getSelectedItem();
			String locationChosen = (String) ui.locationDropdown.getSelectedItem();
			String searchForChosen = (String) ui.searchForDropdown.getSelectedItem();
			String[] businessChosen = null;
			if(ui.detailsTable.getSelectedRow() != -1) {
				businessChosen = ui.detailsTableData[ui.detailsTable.getSelectedRow()];
			}
			switch (numSearches) {
				case 1:
					return handleSubCatsQuery(getSubCatsQuery(mainCatsSelected, searchForChosen));
				case 2:
					return handleAttributesQuery(getAttributesQuery(mainCatsSelected, subCatsSelected, searchForChosen));
				case 3:
					return handleLocationsQuery(getLocationsQuery(mainCatsSelected, subCatsSelected, attributesSelected, searchForChosen));
				case 4:
					return handleOperationsQueries(getOperationsQuery(mainCatsSelected, subCatsSelected, attributesSelected, locationChosen, searchForChosen));
				case 5:
					return handleDetailsQuery(getDetailsQuery(mainCatsSelected, subCatsSelected, attributesSelected, dayChosen, fromChosen, toChosen, locationChosen, searchForChosen));
				case 6:
					return handleReviewsQuery(getReviewsQuery(businessChosen));
				default:
					numSearches = 0;
			}
		}
		numSearches = 0;
		return 0;
    }
    
    private ResultSet executeQuery(String query) {
		try {
			Statement statement = conn.createStatement();
			if(argParser.debug()) {
				System.out.println(query);
			}
			return statement.executeQuery(query);
		} catch (SQLException e) {
			Util.handleSQLException(e);
		}
		return null;
    }

    private int handleReviewsQuery(String reviewsQuery) {
        if(reviewsQuery == null) {
            return 0;
        }   
        ArrayList<String[]> reviews = new ArrayList<String[]>();
        ResultSet rs = executeQuery(reviewsQuery);
		if(rs != null) {
			try {
				while(rs.next()) {
                    String[] data = new String[5];
                    data[0] = rs.getString(1);
                    data[1] = rs.getString(2);
                    data[2] = rs.getString(3);
                    data[3] = rs.getString(4);
                    data[4] = rs.getString(5);
                    reviews.add(data);
				}
				rs.close();
			} catch (SQLException e) {
				Util.handleSQLException(e);
			}
		}
        ui.fillReviewsPopup(reviews.toArray(new String[reviews.size()][5]));
        return reviews.size();
    }

	private int handleDetailsQuery(String detailsQuery) {
        if(detailsQuery == null) {
            return 0;
        }
        ArrayList<String[]> details = new ArrayList<String[]>();
		ResultSet rs = executeQuery(detailsQuery);
		if(rs != null) {
			try {
				while(rs.next()) {
					String[] data = new String[4];
					data[0] = rs.getString(1);
		            data[1] = rs.getString(2);
		            data[2] = rs.getString(3);
		            data[3] = rs.getString(4);
                    details.add(data);
				}
				rs.close();
			} catch (SQLException e) {
				Util.handleSQLException(e);
			}
		}
        ui.setDetailsTable(details.toArray(new String[details.size()][4]));
        return details.size();
	}
	
	private int handleOperationsQueries(String operationsQuery) {
		if(operationsQuery == null) {
			return 0;
		}
		ArrayList<String> days = new ArrayList<String>();
		ArrayList<String> froms = new ArrayList<String>();
		ArrayList<String> tos = new ArrayList<String>();
		ResultSet rs = executeQuery(operationsQuery);
		if(rs != null) {
			try {
				while(rs.next()) {
					days.add(rs.getString(1));
					froms.add(rs.getString(2));
					tos.add(rs.getString(3));
				}
				rs.close();
			} catch (SQLException e) {
				Util.handleSQLException(e);
			}
		}
		String[] toResults = Util.removeDuplicates(tos.toArray(new String[tos.size()]));
		String[] fromResults = Util.removeDuplicates(froms.toArray(new String[froms.size()])); 
		String[] daysResults = Util.removeDuplicates(days.toArray(new String[days.size()]));
		ui.fillTos(toResults);
		ui.fillFroms(fromResults);
		ui.fillDays(daysResults);
		return daysResults.length*fromResults.length*toResults.length;
	}
	
	private int handleAttributesQuery(String attributesQuery) {
        if(attributesQuery == null) {
            return 0;
        }
        ArrayList<String> attributes = new ArrayList<String>();
        ResultSet rs = executeQuery(attributesQuery);
		if(rs != null) {
			try {
				while(rs.next()) {
                    attributes.add(rs.getString(1) + "=" + rs.getString(2));
                }
				rs.close();
			} catch (SQLException e) {
				Util.handleSQLException(e);
			}
		}
        String[] attrs = attributes.toArray(new String[attributes.size()]);
        Arrays.sort(attrs);
        ui.fillAttributes(attrs);
        return attrs.length;
	}
	
	private int handleLocationsQuery(String locationsQuery) {
		if(locationsQuery == null) {
            return 0;
        }
        ArrayList<String> locations = new ArrayList<String>();
        ResultSet rs = executeQuery(locationsQuery);
		if(rs != null) {
			try {
				while(rs.next()) {
                    locations.add(rs.getString(1) + "," + rs.getString(2));
                }
				rs.close();
			} catch (SQLException e) {
				Util.handleSQLException(e);
			}
		}
        String[] locs = locations.toArray(new String[locations.size()]);
        Arrays.sort(locs);
        ui.fillLocations(locs);
        return locs.length;
	}

    private int handleSubCatsQuery(String subCatsQuery) {
        if(subCatsQuery == null) {
            return 0;
        }
        ArrayList<String> categories = new ArrayList<String>();
    	ResultSet rs = executeQuery(subCatsQuery);
    	if(rs != null) {
    		try {
				while(rs.next()) {
                    categories.add(rs.getString(1));
				}
				rs.close();
			} catch (SQLException e) {
				Util.handleSQLException(e);
			}
    	}
        String[] cats = categories.toArray(new String[categories.size()]);
        Arrays.sort(cats);
        ui.fillSubcategories(cats);
        return cats.length;
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
    
	@Override
    public void actionPerformed(ActionEvent e) {
        if(searchActionString.equals(e.getActionCommand())) {
        	new LoadingPopup(ui, this);
        } else if(closeActionString.equals(e.getActionCommand())) {
        	terminate();
        } else if(resetActionString.equals(e.getActionCommand())) {
        	numSearches = 0;
        	ui.reset();
        } else if(backActionString.equals(e.getActionCommand())) {
        	numSearches--;
        	ui.setStatusText();
        }
    }

    public int getNumSearches() {
    	return numSearches;
    }
    
    public static void main(String[] args) {
        new hw3(args);
    }
}
