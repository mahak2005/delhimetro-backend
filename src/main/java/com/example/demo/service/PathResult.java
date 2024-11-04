package com.example.demo.service;

import java.util.List;

public class PathResult {
    private double distanceOrTime;
    private List<String> path;

    public PathResult(double distanceOrTime, List<String> path) {
        this.distanceOrTime = distanceOrTime;
        this.path = path;
    }

    public double getDistanceOrTime() {
        return distanceOrTime;
    }

    public List<String> getPath() {
        return path;
    }
}
