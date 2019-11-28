package app;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Basic graph data structure
 */
class Graph {

    private Map<Long, Vertex> allVertex;
    private List<Integer> deadlockedVertices;
    private Vertex endOfCycle;

    public Graph() {
        allVertex = new HashMap<Long, Vertex>();
    }

    public void addEdge(long id1, long id2) {
        if(id1 == id2)
            return;
        Vertex vertex1 = null;
        if (allVertex.containsKey(id1)) {
            vertex1 = allVertex.get(id1);
        } else {
            vertex1 = new Vertex(id1);
            allVertex.put(id1, vertex1);
        }
        Vertex vertex2 = null;
        if (allVertex.containsKey(id2)) {
            vertex2 = allVertex.get(id2);
        } else {
            vertex2 = new Vertex(id2);
            allVertex.put(id2, vertex2);
        }
        vertex1.addAdjacentVertex(vertex2);
    }

    public void removeEdge(long id) {
        Vertex done = allVertex.get(id);
        for (Map.Entry<Long, Vertex> entry : allVertex.entrySet()) {
            entry.getValue().removeAdjacentVertex(done);
        }
        allVertex.remove(id);
    }

    public Collection<Vertex> getAllVertex() {
        return allVertex.values();
    }

    public List<Integer> hasCycle() {
        Set<Vertex> whiteSet = new HashSet<>();
        Map<Vertex, Vertex> graySet = new HashMap<>();
        Set<Vertex> blackSet = new HashSet<>();
        this.deadlockedVertices  = new ArrayList<>();

        for (Vertex vertex : this.getAllVertex()) {
            whiteSet.add(vertex);
        }

        while (whiteSet.size() > 0) {
            Vertex current = whiteSet.iterator().next();
            Vertex previous = null;
            if (dfs(current, previous, whiteSet, graySet, blackSet)) {
                Map<Vertex, Vertex> graySetInversed = graySet.entrySet().stream().collect(Collectors.toMap(Map.Entry::getValue, Map.Entry::getKey));
                while(true){
                    System.out.println("Adding to cycle:"+ endOfCycle.getId());
                    deadlockedVertices.add((int)endOfCycle.getId());
                    if(graySetInversed.containsKey(endOfCycle))
                        endOfCycle = graySetInversed.get(endOfCycle);
                    else
                        return deadlockedVertices;
                }
            }
        }

        return deadlockedVertices;
    }

    private boolean dfs(Vertex current, Vertex previous, Set<Vertex> whiteSet, Map<Vertex, Vertex> graySet,
            Set<Vertex> blackSet) {
        // move current to gray set from white set and then explore it.
        whiteSet.remove(current);
        graySet.put(current, previous);
        for (Vertex neighbor : current.getAdjacentVertexes()) {
            // if in black set means already explored so continue.
            if (blackSet.contains(neighbor)) {
                continue;
            }
            // if in gray set then cycle found.
            if (graySet.containsKey(neighbor)) {
                System.out.println("end of cycle:" + neighbor.getId());
                this.endOfCycle = neighbor;
                return true;
            }
            if (dfs(neighbor, current, whiteSet, graySet, blackSet)) {
                return true;
            }
        }
        // move vertex from gray set to black set when done exploring.
        graySet.remove(current);
        blackSet.add(current);
        return false;
    }

    @Override
    public String toString() {
        return "avail vertexes are: " + allVertex;
    }

    public void print() {
        for (Map.Entry<Long, Vertex> entry : allVertex.entrySet()) {
            System.out.print(entry.getKey() + ": ");
            entry.getValue().print();
        }
    }

}

class Vertex {
    long id;
    private List<Vertex> adjacentVertex = new ArrayList<>();

    Vertex(long id) {
        this.id = id;
    }

    public long getId() {
        return id;
    }

    public void addAdjacentVertex(Vertex v) {
        adjacentVertex.add(v);
    }

    public List<Vertex> getAdjacentVertexes() {
        return adjacentVertex;
    }

    public void removeAdjacentVertex(Vertex v) {
        adjacentVertex.remove(v);
    }

    @Override
    public String toString() {
        return String.valueOf(id);
    }

    public void print() {
        System.out.println(adjacentVertex);
    }

}