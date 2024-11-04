package com.example.demo;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Scanner;
import java.util.Set;
import java.util.Stack;

import com.example.demo.model.MetroGraph;
import com.example.demo.model.MetroRoute;
import com.example.demo.model.MetroShape;
import com.example.demo.model.MetroStop;
import com.example.demo.model.MetroStopTime;
import com.example.demo.model.MetroTrip;
import com.example.demo.util.CsvParser1;

public class MetroAnalysisGraph {

    public static void main(String[] args) {
        try {
            // Access files from the resources directory
            InputStream routesStream = MetroNetworkAnalysis.class.getClassLoader().getResourceAsStream("route.csv");
            InputStream stopsStream = MetroNetworkAnalysis.class.getClassLoader().getResourceAsStream("stop.csv");
            InputStream tripsStream = MetroNetworkAnalysis.class.getClassLoader().getResourceAsStream("trip.csv");
            InputStream stopTimesStream = MetroNetworkAnalysis.class.getClassLoader().getResourceAsStream("stop_time.csv");
            InputStream shapesStream = MetroNetworkAnalysis.class.getClassLoader().getResourceAsStream("shape.csv");

            // Parse CSV files using InputStream
            Map<String, MetroRoute> routes = CsvParser1.parseRoutes(new BufferedReader(new InputStreamReader(routesStream)));
            Map<String, MetroStop> stops = CsvParser1.parseStops(new BufferedReader(new InputStreamReader(stopsStream)));
            Map<String, MetroTrip> trips = CsvParser1.parseTrips(new BufferedReader(new InputStreamReader(tripsStream)));
            Map<String, MetroStopTime> stopTimes = CsvParser1.parseStopTimes(new BufferedReader(new InputStreamReader(stopTimesStream)));
            Map<String, MetroShape> shapes = CsvParser1.parseShapes(new BufferedReader(new InputStreamReader(shapesStream)));

            // Create the graph
            MetroGraph graph = new MetroGraph();

            // Populate graph with data from parsed CSV files
            Map<String, String> stopIdToName = new HashMap<>();
            for (MetroStop stop : stops.values()) {
                graph.addStop(stop.getStopId(), stop.getStopName(), stop.getLatitude(), stop.getLongitude());
                stopIdToName.put(stop.getStopId(), stop.getStopName());
            }

            // Group shapes by shapeId and sort by sequence
            Map<String, List<MetroShape>> shapeGroups = groupShapesById(shapes);
            for (List<MetroShape> shapeList : shapeGroups.values()) {
                shapeList.sort(Comparator.comparingInt(MetroShape::getSequence));
            }

            // Add edges between consecutive stops based on the shape sequence
            for (Map.Entry<String, List<MetroShape>> entry : shapeGroups.entrySet()) {
                List<MetroShape> shapeSequence = entry.getValue();
                MetroStop previousStop = null;

                for (int i = 0; i < shapeSequence.size(); i++) {
                    MetroShape currentShape = shapeSequence.get(i);
                    MetroStop currentStop = findClosestStop(stops, currentShape.getLatitude(), currentShape.getLongitude(), 3.0);

                    if (currentStop != null) {
                        if (previousStop != null && !previousStop.getStopId().equals(currentStop.getStopId())) {
                            double distance = calculateDistance(previousStop.getLatitude(), previousStop.getLongitude(), currentStop.getLatitude(), currentStop.getLongitude());
                            graph.addEdge(previousStop.getStopId(), currentStop.getStopId(), distance);
                        }
                        previousStop = currentStop;
                    }
                }
            }

            // User input for start and end stop IDs
            Scanner scanner = new Scanner(System.in);
            System.out.println("Enter the start stop ID:");
            String startStopId = scanner.nextLine();
            System.out.println("Enter the end stop ID:");
            String endStopId = scanner.nextLine();
            scanner.close();

            if (!graph.containsStop(startStopId) || !graph.containsStop(endStopId)) {
                System.out.println("Invalid stop IDs entered.");
                return;
            }

            // Perform BFS
            System.out.println("\n--- BFS Result ---");
            List<String> bfsPath = bfs(graph, startStopId, endStopId, stopIdToName);
            if (!bfsPath.isEmpty()) {
                System.out.println("The path found using BFS is: " + String.join(" -> ", bfsPath));
            } else {
                System.out.println("No path found using BFS.");
            }

            // Perform DFS
            System.out.println("\n--- DFS Result ---");
            List<String> dfsPath = dfs(graph, startStopId, endStopId, stopIdToName, new HashSet<>());
            if (!dfsPath.isEmpty()) {
                System.out.println("The path found using DFS is: " + String.join(" -> ", dfsPath));
            } else {
                System.out.println("No path found using DFS.");
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static List<String> bfs(MetroGraph graph, String start, String end, Map<String, String> stopIdToName) {
        Queue<String> queue = new LinkedList<>();
        Map<String, String> previous = new HashMap<>();
        queue.add(start);
        previous.put(start, null);

        while (!queue.isEmpty()) {
            String current = queue.poll();
            if (current.equals(end)) {
                return constructPath(previous, start, end, stopIdToName);
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

    private static List<String> dfs(MetroGraph graph, String start, String end, Map<String, String> stopIdToName, Set<String> visited) {
        Stack<String> stack = new Stack<>();
        Map<String, String> previous = new HashMap<>();
        stack.push(start);
        visited.add(start);
        previous.put(start, null);

        while (!stack.isEmpty()) {
            String current = stack.pop();
            if (current.equals(end)) {
                return constructPath(previous, start, end, stopIdToName);
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

    private static List<String> constructPath(Map<String, String> previous, String start, String end, Map<String, String> stopIdToName) {
        List<String> path = new LinkedList<>();
        for (String at = end; at != null; at = previous.get(at)) {
            path.add(0, stopIdToName.getOrDefault(at, at));
        }
        return path;
    }

    // Additional helper methods: findClosestStop, calculateDistance, groupShapesById
    private static MetroStop findClosestStop(Map<String, MetroStop> stops, double latitude, double longitude, double toleranceKm) {
        MetroStop closestStop = null;
        double minDistance = Double.MAX_VALUE;
        for (MetroStop stop : stops.values()) {
            double distance = calculateDistance(latitude, longitude, stop.getLatitude(), stop.getLongitude());
            if (distance <= toleranceKm && distance < minDistance) {
                minDistance = distance;
                closestStop = stop;
            }
        }
        return closestStop;
    }

    private static double calculateDistance(double lat1, double lon1, double lat2, double lon2) {
        final int R = 6371;
        double latDistance = Math.toRadians(lat2 - lat1);
        double lonDistance = Math.toRadians(lon2 - lon1);

        double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(lonDistance / 2) * Math.sin(lonDistance / 2);

        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return R * c;
    }

    private static Map<String, List<MetroShape>> groupShapesById(Map<String, MetroShape> shapes) {
        Map<String, List<MetroShape>> shapeGroups = new HashMap<>();
        for (MetroShape shape : shapes.values()) {
            shapeGroups.computeIfAbsent(shape.getShapeId(), k -> new ArrayList<>()).add(shape);
        }
        return shapeGroups;
    }
}
