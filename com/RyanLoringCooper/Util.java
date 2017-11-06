package com.RyanLoringCooper;

import java.io.FileInputStream;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

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
	
	public static Connection setupDatabaseConnection(String hostname, String port, String dbName, boolean debug) {
		String[] creds = getCredentials(debug);
		return setupDatabaseConnection(hostname, port, creds[0], creds[1], dbName, debug);
	}
	
	public static Connection setupDatabaseConnection(String hostname, String port, String dbName) {
		return setupDatabaseConnection(hostname, port, dbName, false);
	}
	
	public static String[] getCredentials(boolean debug) {
		System.out.println("Attempting to get database credentials from file.");
		String[] credentials = new String[2];
		credentials[0] = "";
		credentials[1] = "";
		int index = 0;
		try {
			FileInputStream f = new FileInputStream("credentials");
			while(f.available() > 0) {
				int b = f.read();
				if(b == '\n') {
					index++;
				} else {
					credentials[index] += (char)b;
				}
			}
			f.close();
		} catch (IOException e) {
			e.printStackTrace();
			System.err.println("You must either have a file named credentials with the database credentials in it, or provide the credentials as command line arguments.");
			return credentials;
		}       
        if(debug) {
        	System.out.println("Username set to " + credentials[0] + "\nPassword set to " + credentials[1]);
        }
        return credentials;
    }
	
	public static String[] getCredentials() {
		return getCredentials(false);
	}

    public static String[] toStringArray(ArrayList<String> arr) {
        return arr.toArray(new String[arr.size()]);
    }

    public static String[] toStringArray(ArrayList<Integer> arr) {
        return Util.toStringArray(arr.toArray(new Integer[temp.size()]));
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
}
