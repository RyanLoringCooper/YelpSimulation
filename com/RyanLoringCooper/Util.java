package com.RyanLoringCooper;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.LinkedList;

import org.json.JSONException;
import org.json.JSONObject;

public class Util {
	
	public static Connection setupDatabaseConnection(String hostname, String port, String username, String password, String dbName, boolean debug) {
        /*
        try {
            Class.forName("oracle.jdbc.driver.OracleDriver");
            Class.forName("com.mysql.jdbc.Driver");
        } catch (ClassNotFoundException cnfe) {
            System.err.println("Error loading driver: " + cnfe);
        }
        */
    	Connection conn = null;
    	String oracleURL = "jdbc:oracle:thin:@" + hostname + ":" + port + ":" + dbName;
        if(debug) {
        	System.out.println(oracleURL);
        }
        //mysqlURL = "jdbc:mysql://" + hostname + ":" + port + "/" + dbName;
        try {
            conn = DriverManager.getConnection(oracleURL, username, password);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return conn;
    }

	public static Connection setupDatabaseConnection(String hostname, String port, String username, String password, String dbName) {
		return setupDatabaseConnection(hostname, port, username, password, dbName, false);
	}
	
	public static Connection setupDatabaseConnection(ArgumentParser argParser) {
		return setupDatabaseConnection(argParser.getHostname(), argParser.getPort(), argParser.getUsername(), argParser.getPassword(), argParser.getDbName(), argParser.debug());
	}
	
	
    public static String[] toStringArray(ArrayList arr) {
    	if(arr.size() > 0) {
			if(arr.get(0) instanceof Integer) {
				return Util.toStringArray(arr.toArray(new Integer[arr.size()]));
			} else if(arr.get(0) instanceof String) {
				return (String[]) arr.toArray(new String[arr.size()]);
			} else {
				return Util.toStringArray(arr);
			}
    	} else {
    		return new String[0];
    	}
    }
	
	public static String[] toStringArray(Object[] arr) {
    	String[] s = new String[arr.length];
    	for(int i = 0; i < arr.length; i++) {
    		s[i] = arr[i].toString();
    	}
    	return s;
    }
    
    public static String[] toStringArray(Integer[] ints) {
    	String[] retval = new String[ints.length];
    	for(int i = 0; i < retval.length; i++) {
    		retval[i] = Integer.toString(ints[i]);
    	}
    	return retval;
    }
    
    public static String thoroughReplace(String s, char badChar, String replacement) {
    	String retval = "";
    	for(int i = 0; i < s.length(); i++) {
    		if(s.charAt(i) == badChar) {
    			retval += replacement;
    		} else {
    			retval += s.charAt(i);
    		}
    	}
    	return retval;
    }
    
    public static String thoroughReplace(String s, char[] badChars, String[] replacements) {
    	String retval = "";
    	boolean replaced;
    	for(int i = 0; i < s.length(); i++) {
    		replaced = false;
    		for(int j = 0; j < badChars.length && !replaced; j++) {
    			if(s.charAt(i) == badChars[j]) {
    				retval += replacements[j];
    				replaced = true;
    			}
    		}
    		if(!replaced) {
    			retval += s.charAt(i);
    		}
    	}
    	return retval;
    }
    
    public static String cleanString(String s) {
    	char[] badChars = {'\n', '\r', '\''};
    	String[] replacements = {" ", " ", "''"};
    	s = thoroughReplace(s, badChars, replacements);
    	return s;
    }
    
    public static String cleanString(Object s) {
    	return cleanString((String) s);
    }
    
    public static String[] getJSONStringsFromFile(String filePath) throws FileNotFoundException, IOException {
    	if(filePath == null) {
    		return new String[0];
    	}
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
                strings.add(temp);
                temp = "";
            }
        }
        f.close();
        return (String[]) strings.toArray(new String[strings.size()]);
    }

    public static JSONObject[] createJSONObjects(String filePath) {
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
        return (JSONObject[]) jsonObjs.toArray(new JSONObject[jsonObjs.size()]);
    }

	public static void handleSQLException(SQLException e) {
		e.printStackTrace();
		System.err.println(e.getSQLState());
		SQLException ex;
		while((ex = e.getNextException()) != null) {
			ex.printStackTrace();
			System.err.println(ex.getSQLState());
		}
	}

    public static ArrayList<String> addIfUnique(String s, ArrayList<String> l) {
        for(int i = 0; i < l.size(); i++) {
            if(s.equals(l.get(i))) {
                return l;
            }
        }
        l.add(s);
        return l;
    }
}
