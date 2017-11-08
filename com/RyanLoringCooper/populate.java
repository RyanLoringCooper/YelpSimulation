package com.RyanLoringCooper;

import org.json.JSONObject;
import org.json.JSONException;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Map;
import java.util.ArrayList;
import java.util.LinkedList;

public class populate {

	public static final String className = "populate";
    private static final String businessString = "INSERT INTO Business (business_id,full_address,hours,open,city,review_count,name,neighborhoods,longitude,state,stars,latitude,attributes) VALUES (";
    private static final String categoryString = "INSERT INTO Category (id, name, business) VALUES (";
    private static final String userString = "INSERT INTO YelpUser (yelping_since, votes, review_count, name, user_id, fans, average_stars, elite) VALUES (";
    private static final String reviewString = "INSERT INTO Review (votes, user_id, review_id, stars, date_field, text, business_id) VALUES (";
    private static final String[] businessValues = {"business_id", "full_address", "hours", "open", "city", "review_count", "name", "neighborhoods", "longitude", "state", "stars", "latitude", "attributes"};
    private static final String[] userValues = {"yelping_since", "votes", "review_count", "name", "user_id", "fans", "average_stars", "elite"};
    private static final String[] reviewValues = {"votes", "user_id", "review_id", "stars", "date_field", "text", "business_id"};
    private Connection conn;
    private ArgumentParser argParser;
	private FileOutputStream insertLogger;

    private populate(String[] args) {
    	argParser = new ArgumentParser(args, className);
        if(argParser.wasValid()) {
        	if(argParser.debug()) {
        		setupInsertLogger();
        	}
        	conn = Util.setupDatabaseConnection(argParser);
        	if(conn != null) {
				handleInserts(getBusinessInserts(argParser.getBusinesses()));
				handleInserts(getUserInserts(argParser.getUsers()));
				handleInserts(getReviewInserts(argParser.getReviews()));
				try {
					conn.close();
				} catch (SQLException e) {
					Util.handleSQLException(e);
				}
				return;
        	}
        	System.err.println("Could not set up database connection.");
        } 
    }

    private void setupInsertLogger() {
    	try {
    		File insertsFile = new File("insertsGenerated.sql");
    		insertsFile.createNewFile();
			insertLogger = new FileOutputStream(insertsFile, false);
		} catch (IOException e) {
			e.printStackTrace();
		}
    }
    
    private String getArrayInsert(String[] arr) {
        String s = "";
        for(int j = 0; j < arr.length; j++) {
            s += "'" + Util.cleanString(arr[j]) + "'";
            if(j != arr.length-1) {
                s += ",";
            }
        }
        return s;
    }

    private String getSingleAttributeInsert(Object attr) {
		String s = "";
		if(attr instanceof String) {
			s += "'" + Util.cleanString(attr) + "'"; 
		} else if(attr instanceof Boolean) {
			if((boolean)attr) {
				s += "'true'";
			} else {
				s += "'false'";
			}
		} else if(attr instanceof Integer) {
			s += Integer.toString((Integer) attr);
		} else if(attr instanceof Double) {
			s += Double.toString((Double) attr);
    	} else {
			System.err.println("Inserter wasn't prepared for "+attr+" Which is of type " + attr.getClass());
		}
		return s;
    }

    private String getVotesInsert(Map<String, Integer> votes) {
        String s = "votes_type(";
        s += Integer.toString(votes.get("funny")) + ",";
        s += Integer.toString(votes.get("useful")) + ",";
        s += Integer.toString(votes.get("cool")) + ")";
        return s;
    }

    private String getBusinessAttributeMapInsert(Map<String, Object> dataMap, String attrName) {
    	String s = "";
    	String[] dataKeys = Util.toStringArray(dataMap.keySet().toArray());
        if(dataKeys.length > 0) {
            for(int k = 0; k < dataKeys.length; k++) {
                s += "attribute_type('" + Util.cleanString(attrName) + " " + dataKeys[k] + "',";
                Object o = dataMap.get(dataKeys[k]);
                if(o instanceof Boolean || o instanceof String) {
                	s += getSingleAttributeInsert(o) + ")";
                } else if(o instanceof Double) {
                	s += "'" + Double.toString((Double)o) + "'";
                } else if(o instanceof Integer) {
                	s += "'" + Integer.toString((Integer)o) + "'";
                }
                if(k != dataKeys.length-1) {
                    s += ",";
                }
            }
        } else {
        	s += "attribute_type('" + Util.cleanString(attrName) + "', '')";
        }
        return s;
    }
    
    private String getBusinessAttributeArrayInsert(String[] data, String attrName) {
    	String s = "";
        if(data.length > 0) {
            for(int k = 0; k < data.length; k++) {
                s += "attribute_type('" + Util.cleanString(attrName) + "','" + data[k] + "')";
                if(k != data.length-1) {
                    s += ",";
                }
            }
        }
        return s;
    }
    
    private String getBusinessHoursInsert(Map<String, Object> hours) {
    	String s = "hoursTable(";
    	String[] days = Util.toStringArray(hours.keySet().toArray());
        for(int j = 0; j < days.length; j++) {
            s += "hours_type('" + Util.cleanString(days[j]) + "',";
            Map<String, Object> dayMap = (Map<String, Object>) hours.get(days[j]);
            s += "'" + (String) dayMap.get("open") + "','" + (String) dayMap.get("close") + "')";
            if(j != days.length-1) {
                s += ",";
            } 
        }
        s += "),";
        return s;
    }
    
    private String getBusinessAttributesInsert(Map<String, Object> attrsMap) {
    	String s = "attributeTable(";
        String[] attrs = Util.toStringArray(attrsMap.keySet().toArray());
        for(int j = 0; j < attrs.length; j++) {
            Object a = attrsMap.get(attrs[j]);
            if(a instanceof ArrayList) {
                s += getBusinessAttributeArrayInsert(Util.toStringArray((ArrayList<String>) a), attrs[j]);
            } else if(a instanceof Map) {
            	s += getBusinessAttributeMapInsert((Map<String, Object>) a, attrs[j]);
            } else {
                s += "attribute_type('" + Util.cleanString(attrs[j]) + "',";
                s += getSingleAttributeInsert(a);
                s += ")";
            }
            if(j != attrs.length-1) {
                s += ",";
            }
        }
        // attributes should be the last thing added to s, so no comma needed
        s += ")";
        return s;
    }
    
    private String[] getBusinessInserts(JSONObject[] businesses) {
    	if(businesses == null) {
    		return null;
    	}
        ArrayList<String> inserts = new ArrayList<String>();
        ArrayList<CategoryStruct> categories = new ArrayList<CategoryStruct>();
        for(int i = 0; i < businesses.length; i++) {
            String s = new String(businessString);
            Map<String, Object> m = businesses[i].toMap();
            ArrayList<String> cats = (ArrayList<String>)m.get("categories");
            for(int j = 0; j < cats.size(); j++) {
                categories.add(new CategoryStruct(cats.get(j), (String) m.get("business_id")));
            }
            for(String value : businessValues) {
                if(value.equals("hours")) {
                	s += getBusinessHoursInsert((Map<String, Object>) m.get(value));
                } else if(value.equals("neighborhoods")) {
                    s += "neighborhoodTable(";
                    s += getArrayInsert(Util.toStringArray((ArrayList<String>)m.get(value)));
                    s += "),";
                } else if(value.equals("attributes")) { 
                    s += getBusinessAttributesInsert((Map<String, Object>) m.get(value));
                } else {
                    s += getSingleAttributeInsert(m.get(value));
					s += ",";
                }
            }
            s += ")";
            inserts.add(s);
        }
        // generate Categories Inserts
        for(int i = 0; i < categories.size(); i++) {
            String s = new String(categoryString);
            CategoryStruct cs = categories.get(i);
            s += Integer.toString(i) + ",'" + Util.cleanString(cs.cat) + "','" + Util.cleanString(cs.bid) + "')";
            inserts.add(s);
        }
        return inserts.toArray(new String[inserts.size()]);
    }

    private String[] getUserInserts(JSONObject[] users) {
    	if(users == null) {
    		return null;
    	}
        String[] inserts = new String[users.length];
        for(int i = 0; i < inserts.length; i++) {
            String s = new String(userString);
            Map<String, Object> m = users[i].toMap();
            for(String value : userValues) {
                if(value.equals("votes")) {
                    s += getVotesInsert((Map<String, Integer>) m.get(value));
                    s += ",";
                } else if(value.equals("elite")) {
                    s += "eliteTable(";
                    s += getArrayInsert(Util.toStringArray((ArrayList<Integer>)m.get(value)));
                    // elite is the last value, so no trailing comma is necessary
                    s += ")";
                } else {
                    s += getSingleAttributeInsert(m.get(value));
					s += ",";
                }
            }
            s += ")";
            inserts[i] = s;
        }
        return inserts;
    }

    private ArrayList<String> getTextUpdates(String text, String review_id) {
        ArrayList<String> updates = new ArrayList<String>();
        String start = "SELECT appendToText('";
        for(int i = 0; i < text.length()/2000; i++) { 
            String s = new String(start);
            if(i+2000 < text.length()) {
                s += Util.cleanString(text.substring(i, i+2000));
            } else {
                s += Util.cleanString(text.substring(i, text.length()-1));
            }
            s += "', '" + review_id + "') FROM Review;";
            updates.add(s);
        }
        return updates;
    }

    private String[] getReviewInserts(JSONObject[] reviews) {
    	if(reviews == null) {
    		return null;
    	}
        ArrayList<String> inserts = new ArrayList<String>();
        for(int i = 0; i < reviews.length; i++) {
            String s = new String(reviewString);
            Map<String, Object> m = reviews[i].toMap();
            String text = (String) m.get("text");
            for(String value : reviewValues) {
                if(value.equals("votes")) {
                    s += getVotesInsert((Map<String, Integer>) m.get(value));
                } else if(value.equals("date_field")) {
                	s += "to_date('" + (String)m.get("date") + "', 'YYYY/MM/DD')";
                } else if(value.equals("text")) {
                	s += "' '";
                } else {
                    s += getSingleAttributeInsert(m.get(value));
                }
                // business_id is the last attribute, so append a comma if it is not business_id
                if(!value.equals("business_id")) {
                    s += ",";
                }
            }
            s += ")";
            inserts.add(s);
            inserts.addAll(getTextUpdates(text, (String) m.get("review_id")));
        }
        return inserts.toArray(new String[inserts.size()]);
    }

    private void handleInserts(String[] inserts) {
    	if(inserts != null) {
			try {
				Statement statement = conn.createStatement();
				for(String insert : inserts) {
					if(argParser.debug()) {
						System.out.println(insert);
						insertLogger.write((insert + ";\n").getBytes());
					}
					statement.executeUpdate(insert);
				}
			} catch (SQLException e) {
				Util.handleSQLException(e);
			} catch (IOException e) {
				e.printStackTrace();
			}
    	}
    }

    public static void main(String[] args) {
        new populate(args);
    }
}
