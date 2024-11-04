package com.example.demo;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

import com.example.demo.model.MetroGraph;
import com.example.demo.model.MetroRoute;
import com.example.demo.model.MetroShape;
import com.example.demo.model.MetroStop;
import com.example.demo.model.MetroStopTime;
import com.example.demo.model.MetroTrip;
import com.example.demo.util.CsvParser1;
import com.example.demo.util.DijkstraAlgorithm2;

public class MetroNetworkAnalysisWeight {

    public static void main(String[] args) {
        try {
            // Access files from the resources directory
            InputStream routesStream = MetroNetworkAnalysisWeight.class.getClassLoader().getResourceAsStream("route.csv");
            InputStream stopsStream = MetroNetworkAnalysisWeight.class.getClassLoader().getResourceAsStream("stop.csv");
            InputStream tripsStream = MetroNetworkAnalysisWeight.class.getClassLoader().getResourceAsStream("trip.csv");
//            InputStream stopTimesStream = MetroNetworkAnalysis.class.getClassLoader().getResourceAsStream("stop_time.csv");
            InputStream stopTimesStream = MetroNetworkAnalysisWeight.class.getClassLoader().getResourceAsStream("stop_time.csv");
            InputStream shapesStream = MetroNetworkAnalysisWeight.class.getClassLoader().getResourceAsStream("shape.csv");

            // Parse CSV files using InputStream
            Map<String, MetroRoute> routes = CsvParser1.parseRoutes(new BufferedReader(new InputStreamReader(routesStream)));
            Map<String, MetroStop> stops = CsvParser1.parseStops(new BufferedReader(new InputStreamReader(stopsStream)));
            Map<String, MetroTrip> trips = CsvParser1.parseTrips(new BufferedReader(new InputStreamReader(tripsStream)));
            Map<String, MetroStopTime> stopTimes = CsvParser1.parseStopTimes(new BufferedReader(new InputStreamReader(stopTimesStream)));
            Map<String, MetroShape> shapes = CsvParser1.parseShapes(new BufferedReader(new InputStreamReader(shapesStream)));

            // Create the graph
            MetroGraph graph = new MetroGraph();

            // Populate graph with data from parsed CSV files
            Map<String, String> stopIdToName = new HashMap<>(); // Map to store stop ID to stop name
            for (MetroStop stop : stops.values()) {
                graph.addStop(stop.getStopId(), stop.getStopName(), stop.getLatitude(), stop.getLongitude());
                stopIdToName.put(stop.getStopId(), stop.getStopName()); // Populate the map
            }

            // Group shapes by shapeId and sort by sequence
            Map<String, List<MetroShape>> shapeGroups = groupShapesById(shapes);

            // Sort the shape points based on their sequence for each shapeId
            for (List<MetroShape> shapeList : shapeGroups.values()) {
                shapeList.sort(Comparator.comparingInt(MetroShape::getSequence));
            }

            // Add edges between consecutive stops based on the shape sequence
            for (Map.Entry<String, List<MetroShape>> entry : shapeGroups.entrySet()) {
                List<MetroShape> shapeSequence = entry.getValue();
                MetroStop previousStop = null;

                for (int i = 0; i < shapeSequence.size(); i++) {
                    MetroShape currentShape = shapeSequence.get(i);

                    // Find the closest stop to the current shape point
                    MetroStop currentStop = findClosestStop(stops, currentShape.getLatitude(), currentShape.getLongitude(), 3.0); // 0.1 km tolerance

                    // If a valid stop is found
                    if (currentStop != null) {
                        // Avoid self-loops by checking if the current stop and previous stop are not the same
                        if (previousStop != null && !previousStop.getStopId().equals(currentStop.getStopId())) {
                            double edgeWeight = EdgeWeightCalculator.getEdgeWeight(stopTimes, previousStop.getStopId(), currentStop.getStopId());
                            graph.addEdge(previousStop.getStopId(), currentStop.getStopId(), edgeWeight);
                            System.out.println("Added edge between: " + previousStop.getStopName() + " and " + currentStop.getStopName() + " with weight: " + edgeWeight);
                        }
                        previousStop = currentStop; // Move to the next stop
                    } else {
                        System.out.println("No stop found for current shape point: lat = " + currentShape.getLatitude() + ", lon = " + currentShape.getLongitude());
                    }
                }
            }
            /// To add Sarojini
            MetroStop stop205 = stops.get("205");
            MetroStop closestNeighbor = findClosestStop(stops, 28.570606, 77.182838, 3.0);  // Example closest neighbor

            if (stop205 != null && closestNeighbor != null) {
                graph.addEdge(stop205.getStopId(), closestNeighbor.getStopId(), calculateDistance(stop205.getLatitude(), stop205.getLongitude(), closestNeighbor.getLatitude(), closestNeighbor.getLongitude()));
                System.out.println("Manually added edge: " + stop205.getStopName() + " -> " + closestNeighbor.getStopName());
            }


            // User input for start and end stop IDs
            Scanner scanner = new Scanner(System.in);
            System.out.println("Enter the start stop ID:");
            String startStopId = scanner.nextLine();
            System.out.println("Enter the end stop ID:");
            String endStopId = scanner.nextLine();
            scanner.close();

            // Check if stops exist
            if (!graph.containsStop(startStopId) || !graph.containsStop(endStopId)) {
                System.out.println("Invalid stop IDs entered.");
                return;
            }

            // Perform Dijkstra's algorithm
            DijkstraAlgorithm2 dijkstra = new DijkstraAlgorithm2();
            Map<String, Double> distances = dijkstra.dijkstra(graph, startStopId, stopTimes);
            List<String> path = dijkstra.getPath(endStopId);

            // Print the result
            if (distances.get(endStopId) != Double.MAX_VALUE) {
                // Convert path IDs to stop names
                List<String> pathWithNames = new ArrayList<>();
                for (String stopId : path) {
                    String stopName = stopIdToName.get(stopId);
                    pathWithNames.add(stopName != null ? stopName : stopId); // Add stop name or ID if name not found
                }

                System.out.println("The shortest time from " + stopIdToName.get(startStopId) + " to " + stopIdToName.get(endStopId) + " is " + distances.get(endStopId));
                System.out.println("The path is: " + String.join(" -> ", pathWithNames));
            } else {
                System.out.println("No path found between " + startStopId + " and " + endStopId);
            }
            // After adding stops and edges



        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static Connection getYourDatabaseConnectionSomehow() {
        return null;
    }

    // Find the closest stop to a given latitude and longitude
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

        if (closestStop == null) {
            System.out.println("No stop found for lat: " + latitude + ", lon: " + longitude + " within tolerance: " + toleranceKm + " km");
        }

        return closestStop;
    }

    // Calculate distance between two points using the Haversine formula
    private static double calculateDistance(double lat1, double lon1, double lat2, double lon2) {
        final int R = 6371; // Radius of the Earth in km
        double latDistance = Math.toRadians(lat2 - lat1);
        double lonDistance = Math.toRadians(lon2 - lon1);

        double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(lonDistance / 2) * Math.sin(lonDistance / 2);

        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

        return R * c; // Distance in km
    }

    // Group shapes by their shape ID
    private static Map<String, List<MetroShape>> groupShapesById(Map<String, MetroShape> shapes) {
        Map<String, List<MetroShape>> shapeGroups = new HashMap<>();
        for (MetroShape shape : shapes.values()) {
            shapeGroups.computeIfAbsent(shape.getShapeId(), k -> new ArrayList<>()).add(shape);
        }
        return shapeGroups;
    }
}
