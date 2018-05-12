package com.example.sravan.applibrary.objects;

/**
 * Created by Sravan on 1/17/2018.
 */

public class Event{
    public String bookKey;
    public String date;
    public String event;
    public String eventInfo;
    public String key;
    public String location;
    public String userKey;

    //Create an event object to store all information for displaying and updating an event
    //Server public no argument constructor
    public Event () {

    }

    public Event(String bookKey, String date, String event, String eventInfo, String key, String location, String userKey) {
        this.bookKey = bookKey;
        this.date = date;
        this.event = event;
        this.eventInfo = eventInfo;
        this.key = key;
        this.location = location;
        this.userKey = userKey;
    }

    public String getBookKey() {
        return bookKey;
    }

    public void setBookKey(String bookKey) {
        this.bookKey = bookKey;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getEvent() {
        return event;
    }

    public void setEvent(String event) {
        this.event = event;
    }

    public String getEventInfo() {
        return eventInfo;
    }

    public void setEventInfo(String eventInfo) {
        this.eventInfo = eventInfo;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getUserKey() {
        return userKey;
    }

    public void setUserKey(String userKey) {
        this.userKey = userKey;
    }
}
