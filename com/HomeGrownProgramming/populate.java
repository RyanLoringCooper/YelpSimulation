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

public class Populate {

    public static String businessString = "INSERT INTO Business (business_id,full_address,hours,open,categories,city,review_count,name,neighborhoods,longitude,state,stars,latitude,attributes) VALUES (";
    public static String[] businessValues = {"business_id", "full_address", "hours", "open", "categories", "city", "review_count", "name", "neighborhoods", "longitude", "state", "stars", "latitude", "attributes"};

    public static String[] getJSONStringsFromFile(String filepath) throws FileNotFoundException, IOException {
        LinkedList<String> strings = new LinkedList<String>();
        String temp = "";
        int braces = 0;
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
            FileInputStream f = new FileInputStream(filePath);
            String[] objs = getJSONStringsFromFile(f);
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

    public static String[] getBusinessInserts(JSONObject[] businesses) {
        String[] inserts = new String[businesses.length];
        for(int i = 0; i < inserts.length; i++) {
            String s = new String(businessString);
            Map<String, Object> m = businesses[i].toMap();
            for(String value : businessValues) {
                if(value.equals("hours")) {
                    s += "hoursTable(";
                    Map<String, Object> hours = m.get(value);
                    String[] days = (String[]) hours.keySet().toArray();
                    for(int j = 0; j < days.length; j++) {
                        s += "hours_type('" + days[j] + "',";
                        Map<String, Object> dayMap = hours.get(days[j]);
                        s += "'" + (String) dayMap.get("open") + "','" + (String) dayMap.get("close") + "')";
                        if(j != days.length-1) {
                            s += ",";
                        } 
                    }
                    s += "),";
                } else if(value.equals("categories")) {
                    s += "categoryTable(";
                    String[] cats = (String[]) m.get(value);
                    for(int j = 0; j < cats.length; j++) {
                        s += "'" + cats[j] + "'";
                        if(j != days.length-1) {
                            s += ",";
                        }  
                    }
                    s += "),";
                } else if(value.equals("neighborhoods")) {
                    s += "neighborhoodTable(";
                    String[] hoods = (String[]) m.get(value);
                    for(int j = 0; j < hoods.length; j++) {
                        s += "'" + hoods[j] + "'";
                        if(j != hoods.length-1) {
                            s += ",";
                        }
                    }
                    s += "),";
                } else if(value.equals("attributes")) {
                    s += "attributeTable(";
                    Map<String, Object> attrsMap = m.get(value);
                    String[] attrs = (String[]) attrsMap.keySet().toArray();
                    for(int j = 0; j < attrs.length; j++) {
                        s += "attribute_type('" + attrs[i] + "',";
                        Object attr = attrsMap.get(attrs[i]);
                        if(attr instanceOf Boolean) {
                            if((boolean)attr) {
                                s += "'true'";
                            } else {
                                s += "'false'";
                            }
                        } else if(attr instanceOf String) {
                            s += "'" + (String) attr + "'";
                        } else if(attr instanceOf Integer) {
                            s += Integer.toString((Integer) attr);
                        } else {
                            System.err.print("Inserter wasn't prepared for ");
                            System.err.println(attr);
                        }
                        s += ")";
                        if(j != attrs.length-1) {
                            s += ",";
                        }
                    }
                    // attributes should be the last thing added to s, so no comma needed
                    s += ")"
                } else {
                    Object attr = m.get(value);
                    if(attr instanceOf String) {
                        s += "'" + m.get(value) +"',"; 
                    } else if(attr instanceOf Boolean) {
                        if((boolean)attr) {
                            s += "'true',";
                        } else {
                            s += "'false',";
                        }
                    } else if(attr instanceOf Integer) {
                        s += Integer.toString((Integer) attr) + ",";
                    } else {
                        System.err.print("Inserter wasn't prepared for ");
                        System.err.println(attr);
                    }
                }
            }
            s += ");";
            inserts[i] = s;
        }
        return inserts;
    }

    public static void insertBusinesses(JSONObject[] businesses) {
        String[] inserts = getBusinessInserts(businesses);
        // TODO write jdbc stuff 
    }

    public static void insertUsers(JSONObject[] users) {

    }

    public static void insertReviews(JSONObject[] reviews) {

    }

    public static void main(String[] args) {
        JSONObject[] business, reviews, users;
        for(String arg : args) {
            JSONObject[] jsonObj = createJSONObject(arg);
            if(jsonObj == null) {
                System.err.println("Could not create a JSON object from " + arg);
                return -1;
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
