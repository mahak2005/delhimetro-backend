package com.example.demo.controller;

import java.util.List;

public class PathResponse {
    private String error;
    private double distanceOrTime;
    private List<String> path;

    public PathResponse(String error, double distanceOrTime, List<String> path) {
        this.error = error;
        this.distanceOrTime = distanceOrTime;
        this.path = path;
    }

    // Getters and setters
    public String getError() {
        return error;
    }

    public double getDistanceOrTime() {
        return distanceOrTime;
    }

    public List<String> getPath() {
        return path;
    }
}
