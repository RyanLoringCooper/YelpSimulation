package com.RyanLoringCooper;

import java.io.FileInputStream;
import java.io.IOException;

import org.json.JSONObject;

public class ArgumentParser {

    private static final String helpTextFront = "Usage: java -jar ", helpTextBack = ".jar [-h|d|j] -host hostname -port portNum -dbname dbname [-user username -password password] jsonFile1 jsonFile2 jsonFile3 ...\n\t"
                                            +"-h: display this help text\n\t"
                                            +"-d: display debug messages\n\t"
                                            +"-j: display JSON objects as they are being created"
                                            +"-host: specifies that a hostname is the next argument\n\t"
                                            +"-port: specifies that a port number to connect to the host is the next argument\n\t"
                                            +"-dbname: specifies that the name of the database is the next argument\n"
                                            +"-user: specifies that the username to log into the database is the next argument (default is read from ./credentials)\n\t"
                                            +"-password: specifies that the password to log into the database is the next argument (default is read from ./credentials)\n\t";
	private String hostname = null, port = null, username = null, password = null, dbName = null;
	private JSONObject[] businesses, users, reviews;
	private boolean debug = false, showJson = false, isValidArgs = false;
	
    public ArgumentParser(String[] args, String jarName) {
    	isValidArgs = isValidArguments(args);
    	if(!isValidArgs) {
    		invalidate();
    		System.err.println(helpTextFront + jarName + helpTextBack);
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
            } else if(args[i].equals("-dbname")) {
            	if(i+1 < args.length) {
            		dbName = args[++i];
            	} else {
            		System.err.println("You must provide a database name after -dbname flag (ie. '-dbname dbname')");
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
                businesses = getJSONObjects(args[i]);
                
            } else if(args[i].matches("[/A-Za-z_.]*user[A-Za-z_.]*")) {
                users = getJSONObjects(args[i]);
            } else if(args[i].matches("[/A-Za-z_.]*review[A-Za-z_.]*")) {
                reviews = getJSONObjects(args[i]);
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
        if(dbName == null) {
        	System.err.println("You must provide a database name using the -dbname flag.");
        	return false;
        }
        if(username == null || password == null) {
        	if(!getCredentials(debug)) {
        		return false;
        	}
        }
        if(debug) {
            System.out.println("Arguments parsed");
        }
        return true;
    }
    
    public boolean getCredentials(boolean debug) {
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
			return false;
		}       
        if(debug) {
        	System.out.println("Username set to " + credentials[0] + "\nPassword set to " + credentials[1]);
        }
        username = credentials[0];
        password = credentials[1];
        return !username.equals("") && !password.equals("");
    }
    
    private JSONObject[] getJSONObjects(String filePath) {
   		JSONObject[] objs =  Util.createJSONObjects(filePath);
    	if(debug) {
			System.out.println("Created JSON objects from " + filePath);
		}
    	return objs;
    }
    
    private void invalidate() {
    	hostname = port = username = password = null;
    	debug = false;
    	showJson = false;
    }
    
    public boolean wasValid() {
    	return isValidArgs;
    }

    public String getHostname() {
    	return hostname;
    }
    
    public String getPort() {
    	return port;
    }
    
    public String getUsername() {
    	return username;
    }
    
    public String getPassword() {
    	return password;
    }

    public String getDbName() {
    	return dbName;
    }
    
	public boolean debug() {
		return debug;
	}

	public boolean showJson() {
		return showJson;
	}
	
	public JSONObject[] getBusinesses() {
		return businesses;
	}
	
	public JSONObject[] getUsers() {
		return users;
	}
	
	public JSONObject[] getReviews() {
		return reviews;
	}
}
