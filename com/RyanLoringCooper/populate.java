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

    private static final String helpText = "Usage: java -jar populate.jar [-h|d|j] -host hostname -port portNum [-user username -password password] jsonFile1 jsonFile2 jsonFile3 ...\n\t"
                                            +"-h: display this help text\n\t"
                                            +"-d: display debug messages\n\t"
                                            +"-j: display JSON objects as they are being created"
                                            +"-host: specifies that a hostname is the next argument\n\t"
                                            +"-port: specifies that a port number to connect to the host is the next argument\n\t"
                                            +"-user: specifies that the username to log into the database is the next argument\n\t"
                                            +"-password: specifies that the password to log into the database is the next argument\n";
    private static final String businessString = "INSERT INTO Business (business_id,full_address,hours,open,city,review_count,name,neighborhoods,longitude,state,stars,latitude,attributes) VALUES (";
    private static final String categoryString = "INSERT INTO Category (id, name, business) VALUES (";
    private static final String userString = "INSERT INTO YelpUser (yelping_since, votes, review_count, name, user_id, fans, average_stars, elite) VALUES (";
    private static final String reviewString = "INSERT INTO Review (votes, user_id, review_id, stars, date_field, text, business_id) VALUES (";
    private static final String[] businessValues = {"business_id", "full_address", "hours", "open", "city", "review_count", "name", "neighborhoods", "longitude", "state", "stars", "latitude", "attributes"};
    private static final String[] userValues = {"yelping_since", "votes", "review_count", "name", "user_id", "fans", "average_stars", "elite"};
    private static final String[] reviewValues = {"votes", "user_id", "review_id", "stars", "date_field", "text", "business_id"};
    private static final String dbName = "XE";
    private String hostname = null, port = null, username = null, password = null, businessFile = null, userFile = null, reviewFile = null; 
    private Connection conn;
    private boolean debug = false, showJson = false;
	private FileOutputStream insertLogger;

    private class CategoryStruct {
        public String cat;
        public String bid;

        public CategoryStruct(String cat, String bid) {
            this.cat = cat;
            this.bid = bid;
        }

        public boolean sameCategory(String cat) {
            return this.cat.equals(cat);
        }

        public boolean sameBusiness(String bid) {
            return this.bid.equals(bid);
        }
    }

    private populate(String[] args) {
        if(isValidArguments(args)) {
        	if(debug) {
        		setupInsertLogger();
        	}
        	if(username == null && password == null) {
        		conn = Util.setupDatabaseConnection(hostname, port, dbName, debug);
        	} else {
        		conn = Util.setupDatabaseConnection(hostname, port, username, password, dbName, debug);
        	}
        	if(conn != null) {
				handleInserts(getBusinessInserts(createJSONObjects(businessFile)));
				handleInserts(getUserInserts(createJSONObjects(userFile)));
				handleInserts(getReviewInserts(createJSONObjects(reviewFile)));
				try {
					conn.close();
				} catch (SQLException e) {
					e.printStackTrace();
				}
				return;
        	}
        	System.err.println("Could not set up database connection.");
        } 
		System.err.println(helpText);
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
    
    private boolean isValidArguments(String[] args) {
        if(args.length < 4) {
            return false;
        }
        for(int i = 0; i < args.length; i++) {
        	if(args[i].equals("-host")) { 
                if(i+1 < args.length) {
                    hostname = args[++i];
                } else {
                    System.err.println("You must provide a hostname directly after -host flag (ie. '-host hostname')");
                    return false;
                }
            } else if(args[i].equals("-port")) {
                if(i+1 < args.length) {
                    port = args[++i];
                } else {
                    System.err.println("You must provide a port directly after -port flag (ie. '-port portNumber')");
                    return false;
                }
            } else if(args[i].equals("-user")) {
                if(i+1 < args.length) {
                    username = args[++i];
                } else {
                    System.err.println("You must provide a username after -user flag (ie. '-user username')");
                    return false;
                }
            } else if(args[i].equals("-password")) {
                if(i+1 < args.length) {
                    password = args[++i];
                } else {
                    System.err.println("You must provide a password after -password flag (ie. '-password password')");
                    return false;
                }
            } else if(args[i].equals("-d")) {
            	debug = true;
            	System.out.println("Debugging messages will be displayed");
        	} else if(args[i].equals("-h")) {
        		return false;
        	} else if(args[i].equals("-j")) {
        		showJson = true;
        	} else if(args[i].matches("[/A-Za-z_.]*business[A-Za-z_.]*")) {
				businessFile = args[i];
			} else if(args[i].matches("[/A-Za-z_.]*user[A-Za-z_.]*")) {
				userFile = args[i];
			} else if(args[i].matches("[/A-Za-z_.]*review[A-Za-z_.]*")) {
				reviewFile = args[i];
			} else {
				System.out.println(args[i] + " was not used.");
			}
        }
        if(hostname == null) {
            System.err.println("You must provide a hostname using the -host flag.");
            return false;
        }
        if(port == null) {
            System.err.println("You must provide a port number using the -port flag.");
            return false;
        }
        if(debug) {
        	System.out.println("Arguments parsed");
        }
        return true;
    }

    private String[] getJSONStringsFromFile(String filePath) throws FileNotFoundException, IOException {
        LinkedList<String> strings = new LinkedList<String>();
        String temp = "";
        int braces = 0, quotes = 0;
        FileInputStream f = new FileInputStream(filePath);
        while(f.available() > 0) {
            int b = f.read();
            if(b == '{' && quotes == 0) {
                braces++;
            } else if(b == '}' && quotes == 0) {
                braces--;
            } else if(b == '\\') {
            	temp += (char)b;
            	b = f.read();
            } else if(b == '"') {
            	quotes = quotes == 1 ? quotes-1 : quotes+1;
        	} else if(braces == 0 && b == '\n') {
                continue;
            }
            temp += (char)b;
            if(braces == 0) {
            	if(showJson) {
            		System.out.println(temp);
            	}
                strings.add(temp);
                temp = "";
            }
        }
        f.close();
        return (String[]) strings.toArray(new String[strings.size()]);
    }

    private JSONObject[] createJSONObjects(String filePath) {
        LinkedList<JSONObject> jsonObjs = new LinkedList<JSONObject>();
        try {
            String[] objs = getJSONStringsFromFile(filePath);
            for(String obj : objs) {
                jsonObjs.add(new JSONObject(obj)); 
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            System.err.println("The file " + filePath + " could not be found.");
            jsonObjs = null;
        } catch (JSONException e) {
            e.printStackTrace();
            jsonObjs = null;
        } catch (IOException e) {
            e.printStackTrace();
            jsonObjs = null;
        }
        if(jsonObjs == null || jsonObjs.size() == 0) {
			System.err.println("Could not create a JSON object from " + filePath);
			return null;
		}
    	if(debug) {
			System.out.println("Created JSON objects from " + filePath);
		}
        return (JSONObject[]) jsonObjs.toArray(new JSONObject[jsonObjs.size()]);
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
                    s += "hoursTable(";
                    Map<String, Object> hours = (Map<String, Object>) m.get(value);
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
                } else if(value.equals("neighborhoods")) {
                    s += "neighborhoodTable(";
                    s += getArrayInsert(Util.toStringArray((ArrayList<String>)m.get(value)));
                    s += "),";
                } else if(value.equals("attributes")) { 
                    s += "attributeTable(";
                    Map<String, Object> attrsMap = (Map<String, Object>) m.get(value);
                    String[] attrs = Util.toStringArray(attrsMap.keySet().toArray());
                    for(int j = 0; j < attrs.length; j++) {
                        Object a = attrsMap.get(attrs[j]);
                        if(a instanceof ArrayList) {
                            String[] data = Util.toStringArray((ArrayList<String>) a);
                            if(data.length > 0) {
                                for(int k = 0; k < data.length; k++) {
                                    s += "attribute_type('" + Util.cleanString(attrs[j]) + "','" + data[k] + "')";
                                    if(k != data.length-1) {
                                        s += ",";
                                    }
                                }
                            } 
                        } else if(a instanceof Map) {
                            Map<String, Object> dataMap = (Map<String, Object>) a;
                            String[] dataKeys = Util.toStringArray(dataMap.keySet().toArray());
                            if(dataKeys.length > 0) {
                                for(int k = 0; k < dataKeys.length; k++) {
                                    s += "attribute_type('" + Util.cleanString(attrs[j]) + " " + dataKeys[k] + "',";
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
                            	s += "attribute_type('" + Util.cleanString(attrs[j]) + "', '')";
                            }
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

    private String[] getReviewInserts(JSONObject[] reviews) {
    	if(reviews == null) {
    		return null;
    	}
        String[] inserts = new String[reviews.length];
        for(int i = 0; i < inserts.length; i++) {
            String s = new String(reviewString);
            Map<String, Object> m = reviews[i].toMap();
            for(String value : reviewValues) {
                if(value.equals("votes")) {
                    s += getVotesInsert((Map<String, Integer>) m.get(value));
                } else if(value.equals("date_field")) {
                	s += "to_date('" + (String)m.get("date") + "', 'YYYY/MM/DD')";
                } else {
                    s += getSingleAttributeInsert(m.get(value));
                }
                // business_id is the last attribute, so append a comma if it is not business_id
                if(!value.equals("business_id")) {
                    s += ",";
                }
            }
            s += ")";
            inserts[i] = s;
        }
        return inserts;
    }

    private void handleInserts(String[] inserts) {
    	if(inserts != null) {
			try {
				Statement statement = conn.createStatement();
				for(String insert : inserts) {
					if(debug) {
						System.out.println(insert);
						insertLogger.write((insert + ";\n").getBytes());
					}
					statement.executeUpdate(insert);
				}
			} catch (SQLException e) {
				e.printStackTrace();
				System.err.println(e.getSQLState());
				SQLException ex;
				while((ex = e.getNextException()) != null) {
					ex.printStackTrace();
					System.err.println(ex.getSQLState());
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
    	}
    }

    public static void main(String[] args) {
        new populate(args);
    }
}
