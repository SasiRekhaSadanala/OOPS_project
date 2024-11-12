package java_demo;

import java.io.*;
import java.util.*;

public class GabowSCC {

    private int vertexCount;
    private List<List<Integer>> adjList;
    private List<Integer> indexStack;
    private List<Integer> componentStack;
    private boolean[] onStack;
    private int[] index;
    private int currentIndex;
    private List<List<Integer>> sccList;

    public GabowSCC(int vertices) {
        this.vertexCount = vertices;
        adjList = new ArrayList<>();
        for (int i = 0; i < vertices; i++) {
            adjList.add(new ArrayList<>());
        }
        indexStack = new ArrayList<>();
        componentStack = new ArrayList<>();
        onStack = new boolean[vertices];
        index = new int[vertices];
        Arrays.fill(index, -1);
        sccList = new ArrayList<>();
        currentIndex = 0;
    }

    // Adds an edge to the graph
    public void addEdge(int u, int v) {
        adjList.get(u).add(v);
    }

    // Gabow's algorithm to find SCCs
    public void gabowSCC() {
        for (int v = 0; v < vertexCount; v++) {
            if (index[v] == -1) {
                iterativeDFS(v);  // Use the iterative DFS here
            }
        }
    }

    // Iterative DFS to avoid recursion depth issues
    private void iterativeDFS(int start) {
        Stack<Integer> dfsStack = new Stack<>();
        dfsStack.push(start);

        while (!dfsStack.isEmpty()) {
            int v = dfsStack.peek();
            if (index[v] == -1) {  // If the vertex is not visited
                index[v] = currentIndex++;
                indexStack.add(v);
                componentStack.add(v);
                onStack[v] = true;
            }

            boolean allNeighborsProcessed = true;
            for (int w : adjList.get(v)) {
                if (index[w] == -1) {
                    dfsStack.push(w);  // Add to stack to visit later
                    allNeighborsProcessed = false;
                } else if (onStack[w]) {
                    // Handle the SCC component removal logic
                    while (index[w] < index[componentStack.get(componentStack.size() - 1)]) {
                        componentStack.remove(componentStack.size() - 1);
                    }
                }
            }

            // If all neighbors are processed or the node has no outgoing edges
            if (allNeighborsProcessed) {
                dfsStack.pop();  // Backtrack

                // If v is the root of an SCC
                if (componentStack.get(componentStack.size() - 1) == v) {
                    List<Integer> component = new ArrayList<>();
                    int w;
                    do {
                        w = indexStack.remove(indexStack.size() - 1);
                        onStack[w] = false;
                        component.add(w);
                    } while (w != v);
                    sccList.add(component);
                    componentStack.remove(componentStack.size() - 1);
                }
            }
        }
    }

    // Utility function to print SCCs
    public void printSCCs() {
        System.out.println("Strongly Connected Components:");
        for (List<Integer> scc : sccList) {
            System.out.println(scc);
        }
    }

    // Utility function to read graph data from a text file
    public static GabowSCC readGraphFromFile(String filePath) throws IOException {
        Map<Integer, Integer> vertexMap = new HashMap<>();
        int vertexCounter = 0;

        // First, count unique vertices to initialize the graph
        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] parts = line.split("\\s+");
                int u = Integer.parseInt(parts[0]);
                int v = Integer.parseInt(parts[1]);
                if (!vertexMap.containsKey(u)) vertexMap.put(u, vertexCounter++);
                if (!vertexMap.containsKey(v)) vertexMap.put(v, vertexCounter++);
            }
        }

        // Create graph with counted vertices
        GabowSCC graph = new GabowSCC(vertexCounter);

        // Second pass: add edges to the graph
        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] parts = line.split("\\s+");
                int u = vertexMap.get(Integer.parseInt(parts[0]));
                int v = vertexMap.get(Integer.parseInt(parts[1]));
                graph.addEdge(u, v);
            }
        }
        return graph;
    }

    // Kruskal's Algorithm for MST
    static class Edge implements Comparable<Edge> {
        int u, v, weight;

        Edge(int u, int v, int weight) {
            this.u = u;
            this.v = v;
            this.weight = weight;
        }

        @Override
        public int compareTo(Edge other) {
            return Integer.compare(this.weight, other.weight);
        }
    }

    // Union-Find data structure for Kruskal's MST
    static class UnionFind {
        int[] parent, rank;

        UnionFind(int n) {
            parent = new int[n];
            rank = new int[n];
            for (int i = 0; i < n; i++) parent[i] = i;
        }

        int find(int u) {
            if (parent[u] != u) parent[u] = find(parent[u]);
            return parent[u];
        }

        void union(int u, int v) {
            int rootU = find(u);
            int rootV = find(v);
            if (rootU != rootV) {
                if (rank[rootU] > rank[rootV]) {
                    parent[rootV] = rootU;
                } else if (rank[rootU] < rank[rootV]) {
                    parent[rootU] = rootV;
                } else {
                    parent[rootV] = rootU;
                    rank[rootU]++;
                }
            }
        }
    }

    // Method to create an MST using Kruskal's Algorithm
    public void kruskalMST() {
        List<Edge> edges = new ArrayList<>();
        
        // Create edges between SCCs based on some heuristic (e.g., the first vertex of each SCC)
        for (int i = 0; i < sccList.size(); i++) {
            for (int j = i + 1; j < sccList.size(); j++) {
                // You can modify this to use actual weights, here we assume unit weights between SCCs
                edges.add(new Edge(i, j, 1));
            }
        }

        // Sort edges by weight
        Collections.sort(edges);

        UnionFind uf = new UnionFind(sccList.size());
        List<Edge> mst = new ArrayList<>();

        // Kruskal's algorithm
        for (Edge edge : edges) {
            int u = edge.u;
            int v = edge.v;

            if (uf.find(u) != uf.find(v)) {
                uf.union(u, v);
                mst.add(edge);
            }
        }

        // Print the MST edges
        System.out.println("Minimum Spanning Tree (MST) Edges:");
        for (Edge edge : mst) {
            System.out.println("Edge: " + edge.u + " - " + edge.v + " with weight " + edge.weight);
        }
    }

    public static void main(String[] args) {
        try {
            // Path to the .txt file (regular text file, not gzipped)
            String filePath = "C:\\Users\\sarag\\OneDrive\\Desktop\\new_tweet.txt";
            // Read the graph from the text file
            GabowSCC graph = readGraphFromFile(filePath);
            
            // Run Gabow's SCC algorithm
            graph.gabowSCC();
            
            // Print the strongly connected components
            graph.printSCCs();
            
            // Run Kruskal's MST algorithm on the SCCs
            graph.kruskalMST();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}