package com.example.demo.controller;

public class PathRequest {
    private String startStopId;
    private String endStopId;
    private int choice;

    // Getters and setters
    public String getStartStopId() {
        return startStopId;
    }

    public void setStartStopId(String startStopId) {
        this.startStopId = startStopId;
    }

    public String getEndStopId() {
        return endStopId;
    }

    public void setEndStopId(String endStopId) {
        this.endStopId = endStopId;
    }

    public int getChoice() {
        return choice;
    }

    public void setChoice(int choice) {
        this.choice = choice;
    }
}
