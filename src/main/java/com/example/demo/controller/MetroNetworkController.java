package com.example.demo.controller;

import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.service.MetroNetworkService;

@RestController
@CrossOrigin("*")
@RequestMapping("/api")
public class MetroNetworkController {

    private MetroNetworkService metroNetworkService;

    // Initialize the service without a choice
    public MetroNetworkController() throws Exception {
        this.metroNetworkService = new MetroNetworkService(1); // Default choice can be set here if needed
    }

    @GetMapping("/findRoute")
    public String findRoute(
            @RequestParam String startStopId,
            @RequestParam String endStopId,
            @RequestParam int choice) {
        // Reinitialize the service with the new choice each time a route is requested
        try {
            this.metroNetworkService = new MetroNetworkService(choice);
            return metroNetworkService.findShortestPath(startStopId, endStopId, choice);
        } catch (Exception e) {
            // Handle the exception appropriately
            return "Error initializing the Metro Network Service: " + e.getMessage();
        }
    }
}

