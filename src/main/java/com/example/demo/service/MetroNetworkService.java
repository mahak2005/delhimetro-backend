package com.example.demo.service;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.Stack;

import com.example.demo.EdgeWeightCalculator;
import com.example.demo.model.MetroGraph;
import com.example.demo.model.MetroRoute;
import com.example.demo.model.MetroShape;
import com.example.demo.model.MetroStop;
import com.example.demo.model.MetroStopTime;
import com.example.demo.model.MetroTrip;
import com.example.demo.util.CsvParser1;
import com.example.demo.util.DijkstraAlgorithm;
import com.example.demo.util.DijkstraAlgorithm2;

public class MetroNetworkService {

    private MetroGraph graph;
    private Map<String, String> stopIdToName;
    private Map<String, MetroStopTime> stopTimes; // Store stop times for edge weight calculations

    public MetroNetworkService(int choice) throws Exception {
        // Initialize the graph with the user's choice
        initializeGraph(choice);
    }

    private void initializeGraph(int choice) throws Exception {
        // Access files from the resources directory
        InputStream routesStream = getClass().getClassLoader().getResourceAsStream("route.csv");
        InputStream stopsStream = getClass().getClassLoader().getResourceAsStream("stop.csv");
        InputStream tripsStream = getClass().getClassLoader().getResourceAsStream("trip.csv");
        InputStream stopTimesStream = getClass().getClassLoader().getResourceAsStream("stop_time.csv");
        InputStream shapesStream = getClass().getClassLoader().getResourceAsStream("shape.csv");

        // Parse CSV files using InputStream
        Map<String, MetroRoute> routes = CsvParser1.parseRoutes(new BufferedReader(new InputStreamReader(routesStream)));
        Map<String, MetroStop> stops = CsvParser1.parseStops(new BufferedReader(new InputStreamReader(stopsStream)));
        Map<String, MetroTrip> trips = CsvParser1.parseTrips(new BufferedReader(new InputStreamReader(tripsStream)));
        stopTimes = CsvParser1.parseStopTimes(new BufferedReader(new InputStreamReader(stopTimesStream))); // Store stop times
        Map<String, MetroShape> shapes = CsvParser1.parseShapes(new BufferedReader(new InputStreamReader(shapesStream)));

        // Create the graph
        graph = new MetroGraph();
        stopIdToName = new HashMap<>(); // Map to store stop ID to stop name

        // Populate graph with data from parsed CSV files
        for (MetroStop stop : stops.values()) {
            graph.addStop(stop.getStopId(), stop.getStopName(), stop.getLatitude(), stop.getLongitude());
            stopIdToName.put(stop.getStopId(), stop.getStopName());
        }

        // Group shapes by shapeId and sort by sequence
        Map<String, List<MetroShape>> shapeGroups = groupShapesById(shapes);
        for (List<MetroShape> shapeList : shapeGroups.values()) {
            shapeList.sort(Comparator.comparingInt(MetroShape::getSequence));
        }

        // Add edges to the graph based on the passed choice
        addEdgesToGraph(shapeGroups, stops, stopTimes, choice);
    }

    private void addEdgesToGraph(Map<String, List<MetroShape>> shapeGroups, Map<String, MetroStop> stops, Map<String, MetroStopTime> stopTimes, int choice) {
        for (Map.Entry<String, List<MetroShape>> entry : shapeGroups.entrySet()) {
            List<MetroShape> shapeSequence = entry.getValue();
            MetroStop previousStop = null;

            for (MetroShape currentShape : shapeSequence) {
                // Find the closest stop to the current shape point
                MetroStop currentStop = findClosestStop(stops, currentShape.getLatitude(), currentShape.getLongitude(), 3.0); // Adjusted tolerance

                // If a valid stop is found
                if (currentStop != null) {
                    // Avoid self-loops by checking if the current stop and previous stop are not the same
                    if (previousStop != null && !previousStop.getStopId().equals(currentStop.getStopId())) {
                        double edgeWeight;

                        // Different edge weight calculation based on user choice
                        if (choice == 1) {
                            // Choice 1: Use distance for edge weight
                            edgeWeight = calculateDistance(previousStop.getLatitude(), previousStop.getLongitude(), currentStop.getLatitude(), currentStop.getLongitude());
                            graph.addEdge(previousStop.getStopId(), currentStop.getStopId(), edgeWeight);
                        } else if (choice == 2) {
                            // Choice 2: Use edge weight from stopTimes
                            edgeWeight = EdgeWeightCalculator.getEdgeWeight(stopTimes, previousStop.getStopId(), currentStop.getStopId());
                            graph.addEdge(previousStop.getStopId(), currentStop.getStopId(), edgeWeight);
                        } else {
                            // Choice 3 and 4: Use distance for edge weight (same logic)
                            edgeWeight = calculateDistance(previousStop.getLatitude(), previousStop.getLongitude(), currentStop.getLatitude(), currentStop.getLongitude());
                            graph.addEdge(previousStop.getStopId(), currentStop.getStopId(), edgeWeight);
                        }
                        
                        // Add the edge to the graph
                        
                    }
                    previousStop = currentStop; // Move to the next stop
                }
            }
        }

        // To add Sarojini (this part is the same for all choices)
        MetroStop stop205 = stops.get("205");
        MetroStop closestNeighbor = findClosestStop(stops, 28.570606, 77.182838, 3.0);  // Example closest neighbor

        if (stop205 != null && closestNeighbor != null) {
            graph.addEdge(stop205.getStopId(), closestNeighbor.getStopId(), calculateDistance(stop205.getLatitude(), stop205.getLongitude(), closestNeighbor.getLatitude(), closestNeighbor.getLongitude()));
        }
    }

    public String findShortestPath(String startStopId, String endStopId, int choice) {
        if (!graph.containsStop(startStopId) || !graph.containsStop(endStopId)) {
            return "Invalid stop IDs entered.";
        }
        Map<String, Double> distances;
        List<String> path;

        if (choice == 1) { // Distance-based Dijkstra
            DijkstraAlgorithm dijkstra = new DijkstraAlgorithm();
            distances = dijkstra.dijkstra(graph, startStopId);
            path = dijkstra.getPath(endStopId, DijkstraAlgorithm.previousStops);
        } else if (choice == 2) { // Time-based Dijkstra
            DijkstraAlgorithm2 dijkstra = new DijkstraAlgorithm2();
            distances = dijkstra.dijkstra(graph, startStopId, stopTimes);
            path = dijkstra.getPath(endStopId);
        } else if (choice == 3) { // BFS
            List<String> bfsPath = bfs(graph, startStopId, endStopId);
            return "The path found using BFS is: " + String.join(" -> ", bfsPath);
        } else if (choice == 4) { // DFS
            List<String> dfsPath = dfs(graph, startStopId, endStopId, new HashSet<>());
            return "The path found using DFS is: " + String.join(" -> ", dfsPath);
        } else {
            return "Invalid choice. Please select 1, 2, 3 or 4";
        }

        if (distances.get(endStopId) != Double.MAX_VALUE) {
            // Convert path IDs to stop names
            List<String> pathWithNames = new ArrayList<>();
            for (String stopId : path) {
                String stopName = stopIdToName.get(stopId);
                pathWithNames.add(stopName != null ? stopName : stopId); // Add stop name or ID if name not found
            }
            if(choice==1){
                return "The shortest path from " + stopIdToName.get(startStopId) + " to " + stopIdToName.get(endStopId) + " is:\n" + String.join(" -> ", pathWithNames)+". " + "\nThe shortest distance from " + stopIdToName.get(startStopId) + " to " + stopIdToName.get(endStopId) + " is " + distances.get(endStopId);
            
            } else{
                return "The shortest path from " + stopIdToName.get(startStopId) + " to " + stopIdToName.get(endStopId) + " is:\n" + String.join(" -> ", pathWithNames) +". "+ "\nThe shortest time from " + stopIdToName.get(startStopId) + " to " + stopIdToName.get(endStopId) + " is " + distances.get(endStopId);
            } 
        }
        else {
            return "No path found between " + startStopId + " and " + endStopId;
        }      
    }

    private List<String> bfs(MetroGraph graph, String start, String end) {
        Queue<String> queue = new LinkedList<>();
        Map<String, String> previous = new HashMap<>();
        queue.add(start);
        previous.put(start, null);
    
        while (!queue.isEmpty()) {
            String current = queue.poll();
            if (current.equals(end)) {
                return constructPath(previous, start, end);
            }
    
            for (String neighbor : graph.getNeighbors(current).keySet()) {
                if (!previous.containsKey(neighbor)) {
                    queue.add(neighbor);
                    previous.put(neighbor, current);
                }
            }
        }
        return new ArrayList<>();
    }
    
    private List<String> dfs(MetroGraph graph, String start, String end, Set<String> visited) {
        Stack<String> stack = new Stack<>();
        Map<String, String> previous = new HashMap<>();
        stack.push(start);
        visited.add(start);
        previous.put(start, null);
    
        while (!stack.isEmpty()) {
            String current = stack.pop();
            if (current.equals(end)) {
                return constructPath(previous, start, end);
            }
    
            for (String neighbor : graph.getNeighbors(current).keySet()) {
                if (!visited.contains(neighbor)) {
                    stack.push(neighbor);
                    visited.add(neighbor);
                    previous.put(neighbor, current);
                }
            }
        }
        return new ArrayList<>();
    }
    
    private List<String> constructPath(Map<String, String> previous, String start, String end) {
        List<String> path = new ArrayList<>();
        String step = end;
    
        while (step != null) {
            path.add(step);
            step = previous.get(step);
        }
    
        Collections.reverse(path);
    
        // Convert stop IDs to stop names
        List<String> pathWithNames = new ArrayList<>();
        for (String stopId : path) {
            String stopName = stopIdToName.get(stopId);
            pathWithNames.add(stopName != null ? stopName : stopId); // Add stop name or ID if name not found
        }
    
        return pathWithNames;
    }
    

    private MetroStop findClosestStop(Map<String, MetroStop> stops, double latitude, double longitude, double tolerance) {
        MetroStop closestStop = null;
        double closestDistance = Double.MAX_VALUE;

        for (MetroStop stop : stops.values()) {
            double distance = calculateDistance(latitude, longitude, stop.getLatitude(), stop.getLongitude());
            if (distance < closestDistance && distance <= tolerance) {
                closestDistance = distance;
                closestStop = stop;
            }
        }
        return closestStop;
    }

    private double calculateDistance(double lat1, double lon1, double lat2, double lon2) {
        // Haversine formula to calculate distance between two points on the Earth
        final int EARTH_RADIUS = 6371; // Radius of the Earth in kilometers

        double latDistance = Math.toRadians(lat2 - lat1);
        double lonDistance = Math.toRadians(lon2 - lon1);
        double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2) +
                Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
                        Math.sin(lonDistance / 2) * Math.sin(lonDistance / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

        return EARTH_RADIUS * c; // Distance in kilometers
    }

    private Map<String, List<MetroShape>> groupShapesById(Map<String, MetroShape> shapes) {
        Map<String, List<MetroShape>> shapeGroups = new HashMap<>();
        for (MetroShape shape : shapes.values()) {
            shapeGroups.computeIfAbsent(shape.getShapeId(), k -> new ArrayList<>()).add(shape);
        }
        return shapeGroups;
    }
}
