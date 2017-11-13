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
	public static final String searchActionString = "Search", closeActionString = "Close", resetActionString = "Reset";
	private static UserInterface ui = null;
	private static Connection conn = null;
	private ArgumentParser argParser;
	private int numSearches = 0;

    public hw3(String[] args) {
    	argParser = new ArgumentParser(args, className);
    	if(argParser.wasValid()) {
			ui = new UserInterface(this);
			conn = Util.setupDatabaseConnection(argParser);
    	}
    }

    private String getDetailsQuery(List<String> mainCatsSelected, List<String> subCatsSelected, List<String> attributesSelected, String dayChosen, String fromChosen, String toChosen, String locationChosen, String searchForChosen) {
        if(mainCatsSelected.size() > 0) {
    	    return "SELECT DISTINCT b.name, b.city, b.state, b.stars FROM Business b, table(b.attributes) attrs, table(b.hours) hours"
    	    	 + getDetailsQueryMeat(mainCatsSelected, subCatsSelected, attributesSelected, dayChosen, fromChosen, toChosen, locationChosen, searchForChosen);
    	    
        }
        return null;
    }
    private String getDetailsQueryMeat(List<String> mainCatsSelected, List<String> subCatsSelected, List<String> attributesSelected, String dayChosen, String fromChosen, String toChosen, String locationChosen, String searchForChosen) {
		String query = "";
		if(attributesSelected.size() > 0) {
			query += " AND ";
			for(int i = 0; i < attributesSelected.size(); i++) {
				String[] attrs = attributesSelected.get(i).split("=");
				query += "(attrs.attr = '" + attrs[0] + "' AND attrs.value = '" + attrs[1] + "')";
				if(i != attributesSelected.size()-1) {
					query += " " + searchForChosen + " "; 
				}
			}
		}
		if(!locationChosen.equals(UserInterface.dropdownDefaultString)) {
			String[] loc = locationChosen.split(",");
			query += " AND b.city = '" + loc[0] + "' AND b.state = '" + loc[1] + "'";
		}
		if(!dayChosen.equals(UserInterface.dropdownDefaultString)) {
			query += " AND hours.day = '" + dayChosen + "'";
		}
		if(!fromChosen.equals(UserInterface.dropdownDefaultString)) {
			query += " AND hours.open = '" + fromChosen + "'";
		}
		if(!toChosen.equals(UserInterface.dropdownDefaultString)) {
			query += " AND hours.close = '" + toChosen + "'";
		}
		query += ")";
		return query;
    }
    
    private String[] getOperationsQueries(List<String> mainCatsSelected, List<String> subCatsSelected, List<String> attributesSelected, String locationChosen, String searchForChosen) {
    	if(mainCatsSelected.size() > 0) {
			String whereClause = getDetailsQueryMeat(mainCatsSelected, subCatsSelected, attributesSelected, UserInterface.dropdownDefaultString, UserInterface.dropdownDefaultString, UserInterface.dropdownDefaultString, locationChosen, searchForChosen);
			String days = "SELECT DISTINCT hours.day FROM Business b, table(b.attributes) attrs, table(b.hours) hours" + whereClause;
			String from = "SELECT DISTINCT hours.open FROM Business b, table(b.attributes) attrs, table(b.hours) hours" + whereClause;
			String to = "SELECT DISTINCT hours.close FROM Business b, table(b.attributes) attrs, table(b.hours) hours" + whereClause;
			String[] retval = {days, from, to};
			return retval;
    	} 
    	return null;
    }
    
    private String getLocationsQuery(List<String> mainCatsSelected, List<String> subCatsSelected, List<String> attributesSelected, String searchForChosen) {
    	if(mainCatsSelected.size() > 0) {
    		ArrayList<String> queries = new ArrayList<String>();
    		String start = "SELECT DISTINCT b.city, b.state FROM Business b, table(b.attributes) attrs, Category cMain, Category cSub WHERE ";
            for(int i = 0; i < mainCatsSelected.size(); i++) {
            	String query = start + "b.business_id = cMain.business AND cMain.name = '" + mainCatsSelected.get(i) + "'";
            	if(subCatsSelected.size() > 0) {
					for(int j = 0; j < subCatsSelected.size(); j++) {
						query += " AND b.business_id = cSub.business"
							   + " AND cSub.name = '" + subCatsSelected.get(j) + "' ";
						if(attributesSelected.size() > 0) {
							for(int k = 0; k < attributesSelected.size(); k++) {
								String[] attrs = attributesSelected.get(k).split("=");
								query += " AND (attrs.attr = '" + attrs[0] + "' AND attrs.value = '" + attrs[1] + "') ";
								queries.add(query);
							}
						} else {
							queries.add(query);
						}
					}
            	} else {
					queries.add(query);
            	}
            }
            String query = "";
            for(int i = 0; i < queries.size(); i++) {
            	query += queries.get(i);
            	if(i != queries.size()-1) {
					if(searchForChosen.equals("AND")) {
						query += "INTERSECT ";
					} else {
						query += "UNION ";
					}
            	}
            }
            return query;
    	}
    	return null;
    }

    private String getAttributesQuery(List<String> mainCatsSelected, List<String> subCatsSelected, String searchForChosen) {
        if(mainCatsSelected.size() > 0) {
        	String start = "SELECT DISTINCT attrs.attr, attrs.value FROM Business b, table(b.attributes) attrs, Category cMain, Category cSub WHERE ";
            String query = "";
            for(int i = 0; i < mainCatsSelected.size(); i++) {
                for(int j = 0; j < subCatsSelected.size(); j++) {
                    query += start + "b.business_id = cMain.business AND b.business_id = cSub.business AND cMain.name = '" + mainCatsSelected.get(i) 
                            + "' AND cSub.name = '" + subCatsSelected.get(j) + "' ";
                    if(j != subCatsSelected.size()-1) {
                        if(searchForChosen.equals("AND")) {
                            query += "INTERSECT ";
                        } else {
                            query += "UNION ";
                        }
                    }
                }
                if(i != mainCatsSelected.size()-1) {
                    if(searchForChosen.equals("AND")) {
                        query += "INTERSECT ";
                    } else {
                        query += "UNION ";
                    }
                }
            }
            return query; 
        }
		return null;
    }
    
    private String getSubCatsQuery(List<String> mainCatsSelected, String searchForChosen) {
        if(mainCatsSelected.size() > 0) {
            String query = "";
            for(int k = 0; k < mainCatsSelected.size(); k++) {
            	query += "SELECT DISTINCT busWithCats.name "
                        + "FROM Category c, ( "
                        + "SELECT * " 
                        + "FROM Category cat "
                        + "WHERE cat.name != '" + mainCatsSelected.get(k) + "' "
            			+ ") busWithCats WHERE "
            			+ "c.name = '" + mainCatsSelected.get(k) + "'"
            			+ " AND c.business = busWithCats.business";
                if(k != mainCatsSelected.size()-1) {
                	if(searchForChosen.equals("AND")) {
                        query += " INTERSECT ";
                    } else {
                        query += " UNION ";
					}
                } 
            }
            return query;
        } 
		return null;
    }

    private String getBusinessID(String[] businessChosen) {
        if(businessChosen != null && businessChosen.length == 4) {
            String q = "SELECT b.business_id FROM Business b WHERE " 
                     + "b.name = '" + businessChosen[0] + "' "
                     + "AND b.city = '" + businessChosen[1] + "' "
                     + "AND b.state = '" + businessChosen[2] + "' "
                     + "AND b.stars = '" + businessChosen[3] + "'";
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
                         + "FROM Review r WHERE r.business_id = '" + bid + "'";
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
					return handleOperationsQueries(getOperationsQueries(mainCatsSelected, subCatsSelected, attributesSelected, locationChosen, searchForChosen));
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
	
	private int handleDaysQuery(String daysQuery) {
		ArrayList<String> days = new ArrayList<String>();
		ResultSet rs = executeQuery(daysQuery);
		if(rs != null) {
			try {
				while(rs.next()) {
					days.add(rs.getString(1));
				}
				rs.close();
			} catch (SQLException e) {
				Util.handleSQLException(e);
			}
		}
		ui.fillDays(days.toArray(new String[days.size()]));
		return days.size();
	}
	
	private int handleFromQuery(String fromQuery) {
		ArrayList<String> froms = new ArrayList<String>();
		ResultSet rs = executeQuery(fromQuery);
		if(rs != null) {
			try {
				while(rs.next()) {
					froms.add(rs.getString(1));
				}
				rs.close();
			} catch (SQLException e) {
				Util.handleSQLException(e);
			}
		}
		ui.fillFroms(froms.toArray(new String[froms.size()]));
		return froms.size();
	}
	private int handleToQuery(String toQuery) {
		ArrayList<String> tos = new ArrayList<String>();
		ResultSet rs = executeQuery(toQuery);
		if(rs != null) {
			try {
				while(rs.next()) {
					tos.add(rs.getString(1));
				}
				rs.close();
			} catch (SQLException e) {
				Util.handleSQLException(e);
			}
		}
		ui.fillTos(tos.toArray(new String[tos.size()]));
		return tos.size();
	}
	private int handleOperationsQueries(String[] operationsQueries) {
		if(operationsQueries == null) {
			return 0;
		}
		int days = handleDaysQuery(operationsQueries[0]);
		int froms = handleFromQuery(operationsQueries[1]);
		int tos = handleToQuery(operationsQueries[2]);
		return days*froms*tos;
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
        }
    }

    public int getNumSearches() {
    	return numSearches;
    }
    
    public static void main(String[] args) {
        new hw3(args);
    }
}
