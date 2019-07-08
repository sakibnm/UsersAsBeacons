package com.example.usersasbeacons;

import java.util.HashMap;
import java.util.Map;

public class User extends HashMap {
    private String name;
    private String message;
    private String instanceID;

    private Map<String, Object> hashMap;

    @Override
    public String toString() {
        return "User{" +
                "name='" + name + '\'' +
                ", message='" + message + '\'' +
                ", instanceID='" + instanceID + '\'' +
                ", hashMap=" + hashMap +
                '}';
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getInstanceID() {
        return instanceID;
    }

    public void setInstanceID(String instanceID) {
        this.instanceID = instanceID;
    }

    public User(String name, String message, String instanceID) {
        this.name = name;
        this.message = message;
        this.instanceID = instanceID;
    }

    public Map toHashMap(){

        this.hashMap = new HashMap<>();

        this.hashMap.put("name", this.name);
        this.hashMap.put("message", this.message);
        this.hashMap.put("instanceID", this.instanceID);

        return this.hashMap;
    }


}
