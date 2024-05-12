package com.example.myapplication;

import java.util.Date;

public class Appointment {
    private int year;
    private int month;
    private int dayOfMonth;
    private String timeSlot;

    public Appointment(int year, int month, int dayOfMonth, String timeSlot) {
        this.year = year;
        this.month = month;
        this.dayOfMonth = dayOfMonth;
        this.timeSlot = timeSlot;
    }

    // Getters and setters

    public int getYear() {
        return year;
    }

    public void setYear(int year) {
        this.year = year;
    }

    public int getMonth() {
        return month;
    }

    public void setMonth(int month) {
        this.month = month;
    }

    public int getDayOfMonth() {
        return dayOfMonth;
    }

    public void setDayOfMonth(int dayOfMonth) {
        this.dayOfMonth = dayOfMonth;
    }

    public String getTimeSlot() {
        return timeSlot;
    }

    public void setTimeSlot(String timeSlot) {
        this.timeSlot = timeSlot;
    }

    public String getDate() {
        return String.format("%d-%d-%d %s", year, month, dayOfMonth, timeSlot);
    }
}
