package com.HomeGrownProgramming;

import org.json.JSONTokener;
import org.json.JSONObject;
import org.json.JSONException;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

import java.util.Map;
import java.util.LinkedList;

import oracle.jdbc.*;

public class populate {

    public static String businessString = "INSERT INTO Business (business_id,full_address,hours,open,categories,city,review_count,name,neighborhoods,longitude,state,stars,latitude,attributes) VALUES (";
    public static String[] businessValues = {"business_id", "full_address", "hours", "open", "categories", "city", "review_count", "name", "neighborhoods", "longitude", "state", "stars", "latitude", "attributes"};
    public static String userString = "INSERT INTO YelpUsers (yelping_since, votes, review_count, name, user_id, friends, fans, average_stars, elite) VALUES (";
    public static String[] userValues = {"yelping_since", "votes", "review_count", "name", "user_id", "friends", "fans", "average_stars", "elite"};

    public static String[] getJSONStringsFromFile(String filePath) throws FileNotFoundException, IOException {
        LinkedList<String> strings = new LinkedList<String>();
        String temp = "";
        int braces = 0;
        FileInputStream f = new FileInputStream(filePath);
        while(f.available() > 0) {
            int b = f.read();
            if(b == '{') {
                braces++;
            } else if(b == '}') {
                braces--;
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
        return (String[]) strings.toArray();
    }

    public static JSONObject[] createJSONObject(String filePath) {
        LinkedList<JSONObject> jsonObjs = new LinkedList<JSONObject>();
        try {
            String[] objs = getJSONStringsFromFile(filePath);
            for(String obj : objs) {
                jsonObjs.add(new JSONObject(obj)); // TODO
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
        return (JSONObject[]) jsonObjs.toArray();
    }

    public static String getArrayInsert(String[] arr) {
        String s = "";
        for(int j = 0; j < arr.length; j++) {
            s += "'" + arr[j] + "'";
            if(j != arr.length-1) {
                s += ",";
            }
        }
        return s;
    }

    public static String getSingleAttributeInsert(Object attr) {
		String s = "";
		if(attr instanceof String) {
			s += "'" + m.get(value) +"'"; 
		} else if(attr instanceof Boolean) {
			if((boolean)attr) {
				s += "'true'";
			} else {
				s += "'false'";
			}
		} else if(attr instanceof Integer) {
			s += Integer.toString((Integer) attr) + "";
		} else {
			System.err.print("Inserter wasn't prepared for ");
			System.err.println(attr);
		}
		return s;
    }

    public static String[] getBusinessInserts(JSONObject[] businesses) {
        String[] inserts = new String[businesses.length];
        for(int i = 0; i < inserts.length; i++) {
            String s = new String(businessString);
            Map<String, Object> m = businesses[i].toMap();
            for(String value : businessValues) {
                if(value.equals("hours")) {
                    s += "hoursTable(";
                    Map<String, Object> hours = (Map<String, Object>) m.get(value);
                    String[] days = (String[]) hours.keySet().toArray();
                    for(int j = 0; j < days.length; j++) {
                        s += "hours_type('" + days[j] + "',";
                        Map<String, Object> dayMap = (Map<String, Object>) hours.get(days[j]);
                        s += "'" + (String) dayMap.get("open") + "','" + (String) dayMap.get("close") + "')";
                        if(j != days.length-1) {
                            s += ",";
                        } 
                    }
                    s += "),";
                } else if(value.equals("categories")) {
                    s += "categoryTable(";
                    s += getArrayInsert((String[]) m.get(value));
                    s += "),";
                } else if(value.equals("neighborhoods")) {
                    s += "neighborhoodTable(";
                    s += getArrayInsert((String[]) m.get(value));
                    s += "),";
                } else if(value.equals("attributes")) {
                    s += "attributeTable(";
                    Map<String, Object> attrsMap = (Map<String, Object>) m.get(value);
                    String[] attrs = (String[]) attrsMap.keySet().toArray();
                    for(int j = 0; j < attrs.length; j++) {
                        s += "attribute_type('" + attrs[i] + "',";
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

    public static String[] getUserInserts(JSONObject[] users) {
        String[] inserts = new String[users.length];
        for(int i = 0; i < inserts.length; i++) {
            String s = new String(userString);
            Map<String, Object> m = users[i].toMap();
            for(String value : userValues) {
                if(value.equals("votes")) {
                    Map<String, Object> votes = (Map<String, Integer>) m.get(value);
                    s += "votes_type(";
                    s += Integer.toString(votes.get("funny")) + ",";
                    s += Integer.toString(votes.get("useful")) + ",";
                    s += Integer.toString(votes.get("cool")) + "),";
                } else if(value.equals("friends")) {
                    s += "friendsTable(";
                    s += getArrayInsert((String[]) m.get(value));
                    s += "),";
                } else if(value.equals("elite")) {
                    s += "eliteTable(";
                    s += getArrayInsert((String[]) m.get(value));
                    // elite is the last value, so no trailing comma is necessary
                    s += ")";
                } else {
                    s += getSingleAttributeInsert(m.get(value));
					s += ",";
                }
            }
        }
    }

    public static String[] getReviewInserts(JSONObject[] reviews) {

    }

    public static void handleInserts(String[] inserts) {

    }

    public static void insertBusinesses(JSONObject[] businesses) {
        if(businesses == null) {
            System.err.println("No business objects were generated from an input file.");
            return;
        }
        handleInserts(getBusinessInserts(businesses));
    }

    public static void insertUsers(JSONObject[] users) {
        if(users == null) {
            System.err.println("No users objects were generated from an input file.");
            return;
        }
        handleInserts(getUserInserts(users));
    }

    public static void insertReviews(JSONObject[] reviews) {
        if(reviews == null) {
            System.err.println("No reviews objects were generated from an input file.");
            return;
        }
        handleInserts(getReviewInserts(reviews));
    }

    public static void main(String[] args) {
        JSONObject[] business = null, reviews = null, users = null;
        for(String arg : args) {
            JSONObject[] jsonObj = createJSONObject(arg);
            if(jsonObj == null) {
                System.err.println("Could not create a JSON object from " + arg);
                return;
            }
            if(arg.matches("[A-Za-z_]*business[A-Za-z_.]*")) {
                business = jsonObj;
            } else if(arg.matches("[A-Za-z_]*user[A-Za-z_.]*")) {
                users = jsonObj;
            } else if(arg.matches("[A-Za-z_]*review[A-Za-z_.]*")) {
                reviews = jsonObj;
            } else {
                System.out.println(arg + " was not used.");
            }
        }
        insertBusinesses(business);
        insertUsers(users);
        insertReviews(reviews);
    }
}
