import static java.lang.Math.min;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

public class CourseAllocation {

  private static class Edge {
    public int from, to;
    public Edge residual;
    public double flow;
    public final double capacity;

    public Edge(int from, int to, double capacity) {
      this.from = from;
      this.to = to;
      this.capacity = capacity;
    }

    public boolean isResidual() {
      return capacity == 0;
    }

    public double remainingCapacity() {
      return capacity - flow;
    }

    public void augment(double bottleNeck) {
      flow += bottleNeck;
      residual.flow -= bottleNeck;
    }

    public String toString(int s, int t) {
      String u = (from == s) ? "s" : ((from == t) ? "t" : String.valueOf(from));
      String v = (to == s) ? "s" : ((to == t) ? "t" : String.valueOf(to));
      return String.format(
          "Edge %s -> %s | flow = %.2f",
          u, v, flow);

    }
  }

  private abstract static class NetworkFlowSolverBase {

    // To avoid overflow, set infinity to a value less than double.MAX_VALUE;
    static final double INF = Double.MAX_VALUE / 2;

    // Inputs: n = number of nodes, s = source, t = sink
    final int n, s, t;

    // 'visited' and 'visitedToken' are variables used in graph sub-routines to
    // track whether a node has been visited or not. In particular, node 'i' was
    // recently visited if visited[i] == visitedToken is true. This is handy
    // because to mark all nodes as unvisited simply increment the visitedToken.
    protected int visitedToken = 1;
    protected int[] visited;

    // Indicates whether the network flow algorithm has ran. The solver only
    // needs to run once because it always yields the same result.
    protected boolean solved;

    // The maximum flow. Calculated by calling the {@link #solve} method.
    protected double maxFlow;

    // The adjacency list representing the flow graph.
    protected List<Edge>[] graph;

    /**
     * Creates an instance of a flow network solver. Use the {@link #addEdge} method
     * to add edges to
     * the graph.
     *
     * @param n - The number of nodes in the graph including s and t.
     * @param s - The index of the source node, 0 <= s < n
     * @param t - The index of the sink node, 0 <= t < n and t != s
     */
    public NetworkFlowSolverBase(int n, int s, int t) {
      this.n = n;
      this.s = s;
      this.t = t;
      initializeEmptyFlowGraph();
      visited = new int[n];
    }

    // Constructs an empty graph with n nodes including s and t.
    @SuppressWarnings("unchecked")
    private void initializeEmptyFlowGraph() {
      graph = new List[n];
      for (int i = 0; i < n; i++)
        graph[i] = new ArrayList<Edge>();
    }

    /**
     * Adds a directed edge (and its residual edge) to the flow graph.
     *
     * @param from     - The index of the node the directed edge starts at.
     * @param to       - The index of the node the directed edge ends at.
     * @param capacity - The capacity of the edge
     */
    public void addEdge(int from, int to, double capacity) {
      if (capacity <= 0)
        throw new IllegalArgumentException("Forward edge capacity <= 0");
      Edge e1 = new Edge(from, to, capacity);
      Edge e2 = new Edge(to, from, 0);
      e1.residual = e2;
      e2.residual = e1;
      graph[from].add(e1);
      graph[to].add(e2);
    }

    /**
     * Returns the residual graph after the solver has been executed. This allows
     * you to inspect the
     * {@link Edge#flow} and {@link Edge#capacity} values of each edge. This is
     * useful if you are
     * debugging or want to figure out which edges were used during the max flow.
     */
    public List<Edge>[] getGraph() {
      execute();
      return graph;
    }

    // Returns the maximum flow from the source to the sink.
    public double getMaxFlow() {
      execute();
      return maxFlow;
    }

    // Wrapper method that ensures we only call solve() once
    private void execute() {
      if (solved)
        return;
      solved = true;
      solve();
    }

    // Method to implement which solves the network flow problem.
    public abstract void solve();
  }

  private static class FordFulkersonDfsSolver extends NetworkFlowSolverBase {

    /**
     * Creates an instance of a flow network solver. Use the {@link #addEdge} method
     * to add edges to
     * the graph.
     *
     * @param n - The number of nodes in the graph including s and t.
     * @param s - The index of the source node, 0 <= s < n
     * @param t - The index of the sink node, 0 <= t < n and t != s
     */
    public FordFulkersonDfsSolver(int n, int s, int t) {
      super(n, s, t);
    }

    // Performs the Ford-Fulkerson method applying a depth first search as
    // a means of finding an augmenting path.
    @Override
    public void solve() {
      // Find max flow by adding all augmenting path flows.
      for (double f = dfs(s, INF); f != 0; f = dfs(s, INF)) {
        visitedToken++;
        maxFlow += f;
      }
    }

    private double dfs(int node, double flow) {
      // At sink node, return augmented path flow.
      if (node == t)
        return flow;

      // Mark the current node as visited.
      visited[node] = visitedToken;

      List<Edge> edges = graph[node];
      for (Edge edge : edges) {
        if (edge.remainingCapacity() > 0 && visited[edge.to] != visitedToken) {
          double bottleNeck = dfs(edge.to, min(flow, edge.remainingCapacity()));

          // If we made it from s -> t (a.k.a bottleNeck > 0) then
          // augment flow with bottleneck value.
          if (bottleNeck > 0) {
            edge.augment(bottleNeck);
            return bottleNeck;
          }
        }
      }
      return 0;
    }
  }

  public static double generateRandomNumber() {
    Random random = new Random();
    int randomIndex = random.nextInt(3); // Generates a random index (0, 1, or 2)

    // Array of possible values
    double[] values = { 0.5, 1.0, 1.5 };

    // Return the randomly selected value
    return values[randomIndex];
  }

  public static void main(String[] args) {

    String csvFile = "testcase_1.csv";

    // CSV reading logic
    try (BufferedReader br = new BufferedReader(new FileReader(csvFile))) {
      String line;

      // Skip the header line
      br.readLine();
      int n = 0;
      int s, t;
      int categoryIndex;
      int professorIndex;

      // Count the number of lines in the CSV file
      while (br.readLine() != null) {
        n++;
      }
      br.close();
      BufferedReader br2 = new BufferedReader(new FileReader(csvFile));
      br2.readLine(); // Skip the header line
      n = n + 15;
      s = n - 2;
      t = n - 1;

      NetworkFlowSolverBase solver = new FordFulkersonDfsSolver(n, s, t);

      n = n - 15;

      // First loop: Add edges from source to professors
      while ((line = br2.readLine()) != null) {
        String[] data = line.split(",");
        categoryIndex = getCategoryIndex(data[0]);
        professorIndex = getProfessorIndex(data[1]);
        

        double weight = getWeightForCategory(categoryIndex);


        // if professor is of type x3, then he can have course load = 0.5, 1, or 1.5.
        // so we implemented a random system to ensure we get different suboptimal
        // solutions each time code is run
        if (weight == 1.5) {
          double rando = CourseAllocation.generateRandomNumber();
      
          solver.addEdge(s, professorIndex, rando);
        } else {
      
          solver.addEdge(s, professorIndex, weight);
        }
      }

      br2.close(); // Close the first BufferedReader

      // Second loop: Add edges from professors to courses
      br2 = new BufferedReader(new FileReader(csvFile)); // Create a new BufferedReader
      br2.readLine(); // Skip the header line
      while ((line = br2.readLine()) != null) {
        String[] data = line.split(",");
        professorIndex = getProfessorIndex(data[1]);

        for (int i = 2; i < data.length; i++) {
          String courseName = data[i];
          int courseIndex = getCourseIndex(courseName, n);
          if (courseIndex < n + 7) { // ensuring first only the cdc courses (both first degree and higher degree) are
                                     // allocated
            solver.addEdge(professorIndex, courseIndex, 1);
          }
        }
        for (int i = 2; i < data.length; i++) {
          String courseName = data[i];
          int courseIndex = getCourseIndex(courseName, n);
          if (courseIndex >= n + 7) {// now the electives are allocated
            solver.addEdge(professorIndex, courseIndex, 1);
          }
        }
      }
      br2.close(); // Close the second BufferedReader

      // Third loop: Add edges from courses to sink
      br2 = new BufferedReader(new FileReader(csvFile)); // Create another new BufferedReader
      br2.readLine(); // Skip the header line

      Set<Integer> uniqueCourseIndices = new HashSet<>();
      Set<String> uniqueCourseName = new HashSet<>();
      Map<String,Integer> courses = new HashMap<String,Integer>();
      while ((line = br2.readLine()) != null) {
        String[] data = line.split(",");

        for (int i = 2; i < data.length; i++) {
          String courseName = data[i];
          int courseIndex = getCourseIndex(courseName, n);

          // Check if the courseIndex is not already in the set
          if (!courses.containsValue(courseIndex)) {
            courses.put(courseName, courseIndex);
      //       System.out.println("Unique Course Indices: " + uniqueCourseIndices);
      // System.out.println("Unique Course Name: " + uniqueCourseName);
            solver.addEdge(courseIndex, t, 1);
          }
        }
      }
      // System.out.println("Unique Course Indices: " + uniqueCourseIndices);
      // System.out.println("Unique Course Name: " + uniqueCourseName);
      System.out.println("Unique Courses:  " + courses);
      br2.close(); // Close the BufferedReader

      

      List<Edge>[] resultGraph = solver.getGraph();

      // Displays all edges part of the resulting residual graph.
      boolean allCdc = true;
        for(int i = n+1; i<n+7; i++)
        {
            System.out.println(uniqueCourseIndices.contains(i));
            if(!uniqueCourseIndices.contains(i)) continue;
            double totalFlow = 0;
        List<Edge> edges = resultGraph[i];
        for (Edge edge : edges) {
            if (!edge.isResidual()) {
                totalFlow += edge.flow;
            }
        }
        System.out.println(totalFlow);
        if(totalFlow!=1) 
        {allCdc = false; break;}

    }

      PrintStream originalOut = System.out;

      try {
            // Create a new PrintStream to redirect output
            PrintStream fileOut = new PrintStream(new FileOutputStream("output.txt"));
            
            // Set the System.out to the fileOut
            System.setOut(fileOut);
            System.out.printf("Unique Courses:  " + courses);
            System.out.printf("\nTotal courses alloted : %.2f\n", Math.floor(solver.getMaxFlow()));
            if (allCdc){
              System.out.println("All CDCs alloted successfully!");
            for (List<Edge> edges : resultGraph) {
              for (Edge e : edges) {
                if (e.flow > 0.0 && e.to != t && e.from != s) {
                  System.out.println(e.toString(s, t));
                }
              }

            }
          }
          else System.out.println("Allocation not shown as all CDCs not alloted. CRASH!");

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } finally {
            // Reset the System.out to the originalOut
            System.setOut(originalOut);
        }

    } catch (IOException e) {
      e.printStackTrace();
    }

  }

  private static int getCategoryIndex(String category) {
    // Implementing logic to convert category to index
    // For example, if categories are "x1", "x2", "x3", return 1, 2, 3 respectively.
    switch (category) {
      case "x1":
        return 1;
      case "x2":
        return 2;
      case "x3":
        return 3;
      default:
        return 0; // Handle unknown category if needed
    }
  }

  private static int getProfessorIndex(String professor) {
    // Assuming professor is an integer in the CSV file
    return Integer.parseInt(professor);
  }

  private static double getWeightForCategory(int categoryIndex) {
    // Assign weights based on category index
    switch (categoryIndex) {
      case 1:
        return 0.5;
      case 2:
        return 1.0;
      case 3:
        return 1.5;
      default:
        return 0.0; // Handle unknown category if needed
    }
  }

  private static int getCourseIndex(String courseName, int n) {
    String category = courseName.substring(0, 2);
    int startIndex;
    // to assign vertice number to each course.
    // there are 4 first degree cdc's; 4 first degree electives; 2 higher degre
    // cdc's; and 2 higher degree electives
    switch (category) {
      case "FC":
        startIndex = n;
        break;
      case "HC":
        startIndex = n + 4;
        break;
      case "FE":
        startIndex = n + 6;
        break;
      default:
        startIndex = n + 10;
        break;
    }

    int lastDigit = Character.getNumericValue(courseName.charAt(courseName.length() - 1));
    return startIndex + lastDigit; // ensures a unique vertice number is given to each course
  }
}