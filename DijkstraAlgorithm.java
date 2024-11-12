package dijkstrass;
import java.io.*;
import java.util.*;
public class DijkstraAlgorithm {
    // Define the Node class to store city and distance
    private static class Node implements Comparable<Node> {
        String city;
        int distance;

        Node(String city, int distance) {
            this.city = city;
            this.distance = distance;
        }

        @Override
        public int compareTo(Node other) {
            return Integer.compare(this.distance, other.distance);
        }
    }

    private Map<String, Map<String, Integer>> graph; // Adjacency list for graph
    private Set<String> cities; // Set of all cities

    public DijkstraAlgorithm() {
        graph = new HashMap<>();
        cities = new HashSet<>();
    }

    // Method to parse the graph from the text file
    public void parseGraphFromText(String filename) {
        try (BufferedReader br = new BufferedReader(new FileReader(filename))) {
            String line;
            br.readLine(); // Skip header line

            // Read each line and parse the cities and distances
            while ((line = br.readLine()) != null) {
                String[] parts = line.split(",");
                if (parts.length < 3) continue; // Skip malformed lines

                String source = parts[0].trim();
                String destination = parts[1].trim();
                int distance = Integer.parseInt(parts[2].trim());

                // Add cities to set
                cities.add(source);
                cities.add(destination);

                // Add edges to graph (undirected)
                graph.computeIfAbsent(source, k -> new HashMap<>()).put(destination, distance);
                graph.computeIfAbsent(destination, k -> new HashMap<>()).put(source, distance);
            }
        } catch (IOException | NumberFormatException e) {
            e.printStackTrace();
        }
    }

    // Method to find the shortest paths from the starting city to all other cities
    public Map<String, Integer> findShortestPaths(String startCity) {
        Map<String, Integer> distances = new HashMap<>();
        Map<String, String> previousNodes = new HashMap<>();
        PriorityQueue<Node> pq = new PriorityQueue<>();

        // Initialize distances
        for (String city : cities) {
            distances.put(city, Integer.MAX_VALUE);
        }
        distances.put(startCity, 0);
        pq.offer(new Node(startCity, 0));

        while (!pq.isEmpty()) {
            Node current = pq.poll();
            String currentCity = current.city;

            // Skip if we've found a better path
            if (current.distance > distances.get(currentCity)) {
                continue;
            }

            // Check all neighboring cities
            Map<String, Integer> neighbors = graph.getOrDefault(currentCity, new HashMap<>());
            for (Map.Entry<String, Integer> neighbor : neighbors.entrySet()) {
                String nextCity = neighbor.getKey();
                int newDistance = distances.get(currentCity) + neighbor.getValue();

                if (newDistance < distances.get(nextCity)) {
                    distances.put(nextCity, newDistance);
                    previousNodes.put(nextCity, currentCity);
                    pq.offer(new Node(nextCity, newDistance));
                }
            }
        }

        return distances;
    }

    // Method to reconstruct the shortest path from the start city to the end city
    public List<String> getPath(String startCity, String endCity) {
        Map<String, String> previousNodes = new HashMap<>();
        Map<String, Integer> distances = new HashMap<>();
        PriorityQueue<Node> pq = new PriorityQueue<>();

        // Initialize distances
        for (String city : cities) {
            distances.put(city, Integer.MAX_VALUE);
        }
        distances.put(startCity, 0);
        pq.offer(new Node(startCity, 0));

        while (!pq.isEmpty()) {
            Node current = pq.poll();
            String currentCity = current.city;

            if (currentCity.equals(endCity)) {
                break;
            }

            if (current.distance > distances.get(currentCity)) {
                continue;
            }

            Map<String, Integer> neighbors = graph.getOrDefault(currentCity, new HashMap<>());
            for (Map.Entry<String, Integer> neighbor : neighbors.entrySet()) {
                String nextCity = neighbor.getKey();
                int newDistance = distances.get(currentCity) + neighbor.getValue();

                if (newDistance < distances.get(nextCity)) {
                    distances.put(nextCity, newDistance);
                    previousNodes.put(nextCity, currentCity);
                    pq.offer(new Node(nextCity, newDistance));
                }
            }
        }

        // Reconstruct path
        List<String> path = new ArrayList<>();
        String current = endCity;
        while (current != null) {
            path.add(0, current);
            current = previousNodes.get(current);
        }

        return path;
    }

    public static void main(String[] args) {
        DijkstraAlgorithm dijkstra = new DijkstraAlgorithm();
        // Provide the correct file path to your cities.txt
        dijkstra.parseGraphFromText("C:\\Users\\sasis\\Downloads\\large_city_distances.txt");
        String startCity = "NewYork"; // Set your starting city
        String endCity = "Chicago";  // Set your destination city

        // Find shortest paths from the start city
        Map<String, Integer> distances = dijkstra.findShortestPaths(startCity);

        // Print the shortest distances from the start city to all cities
        System.out.println("Distances from " + startCity + " to all cities:");
        for (Map.Entry<String, Integer> entry : distances.entrySet()) {
            if (!entry.getKey().equals(startCity)) {
                System.out.printf("%s: %d miles%n", entry.getKey(), entry.getValue());
            }
        }

        // Print the shortest path from the start city to the end city
        List<String> path = dijkstra.getPath(startCity, endCity);
        System.out.println("\nShortest path from " + startCity + " to " + endCity + ":");
        System.out.println(String.join(" -> ", path));
        System.out.println("Total distance: " + distances.get(endCity) + " miles");
    }
}
