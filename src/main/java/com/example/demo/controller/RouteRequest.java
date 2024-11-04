
// public class RouteRequest {

//     public String getEndStopId() {
//         // TODO Auto-generated method stub
//         throw new UnsupportedOperationException("Unimplemented method 'getEndStopId'");
//     }

//     public int getChoice() {
//         // TODO Auto-generated method stub
//         throw new UnsupportedOperationException("Unimplemented method 'getChoice'");
//     }

// }
package com.example.demo.controller;

public class RouteRequest {
    private String startStopId;
    private String endStopId;
    private int choice;

    // Getters and Setters
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

