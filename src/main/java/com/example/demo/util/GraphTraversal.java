package com.example.demo.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.Stack;

import com.example.demo.model.MetroGraph;

public class GraphTraversal {

    // BFS Traversal
    public List<String> bfs(MetroGraph graph, String startStopId, String endStopId) {
        Queue<String> queue = new LinkedList<>();
        Map<String, String> previousStops = new HashMap<>();
        Set<String> visited = new HashSet<>();
        queue.add(startStopId);
        visited.add(startStopId);

        while (!queue.isEmpty()) {
            String currentStopId = queue.poll();

            if (currentStopId.equals(endStopId)) {
                return reconstructPath(previousStops, startStopId, endStopId);
            }

            for (String neighbor : graph.getNeighbors(currentStopId).keySet()) {
                if (!visited.contains(neighbor)) {
                    queue.add(neighbor);
                    visited.add(neighbor);
                    previousStops.put(neighbor, currentStopId);
                }
            }
        }
        return Collections.emptyList(); // Return an empty list if no path is found
    }

    // DFS Traversal
    public List<String> dfs(MetroGraph graph, String startStopId, String endStopId) {
        Stack<String> stack = new Stack<>();
        Map<String, String> previousStops = new HashMap<>();
        Set<String> visited = new HashSet<>();
        stack.push(startStopId);
        visited.add(startStopId);

        while (!stack.isEmpty()) {
            String currentStopId = stack.pop();

            if (currentStopId.equals(endStopId)) {
                return reconstructPath(previousStops, startStopId, endStopId);
            }

            for (String neighbor : graph.getNeighbors(currentStopId).keySet()) {
                if (!visited.contains(neighbor)) {
                    stack.push(neighbor);
                    visited.add(neighbor);
                    previousStops.put(neighbor, currentStopId);
                }
            }
        }
        return Collections.emptyList(); // Return an empty list if no path is found
    }

    // Helper function to reconstruct the path from start to end
    private List<String> reconstructPath(Map<String, String> previousStops, String startStopId, String endStopId) {
        List<String> path = new ArrayList<>();
        for (String at = endStopId; at != null; at = previousStops.get(at)) {
            path.add(at);
        }
        Collections.reverse(path);
        return path;
    }
}
