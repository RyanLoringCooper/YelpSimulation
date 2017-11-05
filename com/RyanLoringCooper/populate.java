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
    private static final String businessString = "INSERT INTO Business (business_id,full_address,hours,open,categories,city,review_count,name,neighborhoods,longitude,state,stars,latitude,attributes) VALUES (";
    private static final String[] businessValues = {"business_id", "full_address", "hours", "open", "categories", "city", "review_count", "name", "neighborhoods", "longitude", "state", "stars", "latitude", "attributes"};
    private static final String userString = "INSERT INTO YelpUser (yelping_since, votes, review_count, name, user_id, friends, fans, average_stars, elite) VALUES (";
    private static final String[] userValues = {"yelping_since", "votes", "review_count", "name", "user_id", "friends", "fans", "average_stars", "elite"};
    private static final String reviewString = "INSERT INTO Review (votes, user_id, review_id, stars, date_field, text, business_id) VALUES (";
    private static final String[] reviewValues = {"votes", "user_id", "review_id", "stars", "date_field", "text", "business_id"};
    private static final String dbName = "XE";
    private String hostname = null, port = null, username = null, password = null, businessFile = null, userFile = null, reviewFile = null; 
    private Connection conn;
    private boolean debug = false, showJson = false;
	private FileOutputStream insertLogger;


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
    		File insertsFile = new File("insertsGenerated.sql"
    				+ "");
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
			s += "'" + Util.cleanString(attr) + "'"; // TODO sanitize 
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
        String[] inserts = new String[businesses.length];
        for(int i = 0; i < inserts.length; i++) {
            String s = new String(businessString);
            Map<String, Object> m = businesses[i].toMap();
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
                } else if(value.equals("categories")) {
                    s += "categoryTable(";
                    ArrayList<String> temp = (ArrayList<String>)m.get(value);
                    s += getArrayInsert(temp.toArray(new String[temp.size()]));
                    s += "),";
                } else if(value.equals("neighborhoods")) {
                    s += "neighborhoodTable(";
                    ArrayList<String> temp = (ArrayList<String>)m.get(value);
                    s += getArrayInsert(temp.toArray(new String[temp.size()]));
                    s += "),";
                } else if(value.equals("attributes")) {
                    s += "attributeTable(";
                    Map<String, Object> attrsMap = (Map<String, Object>) m.get(value);
                    String[] attrs = Util.toStringArray( attrsMap.keySet().toArray());
                    for(int j = 0; j < attrs.length; j++) {
                        s += "attribute_type('" + Util.cleanString(attrs[i]) + "',";
                        s += getSingleAttributeInsert(attrsMap.get(attrs[i]));
                        s += ")";
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
            s += ");";
            inserts[i] = s;
        }
        return inserts;
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
                } else if(value.equals("friends")) {
                    s += "friendsTable(";
                    ArrayList<String> temp = (ArrayList<String>)m.get(value);
                    s += getArrayInsert(temp.toArray(new String[temp.size()]));
                    s += "),";
                } else if(value.equals("elite")) {
                    s += "eliteTable(";
                    ArrayList<Integer> temp = (ArrayList<Integer>)m.get(value);
                    Integer[] tempInts = temp.toArray(new Integer[temp.size()]);
                    s += getArrayInsert(Util.toStringArray(tempInts));
                    // elite is the last value, so no trailing comma is necessary
                    s += ")";
                } else {
                    s += getSingleAttributeInsert(m.get(value));
					s += ",";
                }
            }
            s += ");";
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
            s += ");";
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
						insertLogger.write((insert + "\n").getBytes());
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