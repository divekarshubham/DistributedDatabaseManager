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
 * @file Graph.java
 * @author Shubham Divekar, Himani Shah (sjd451@nyu.edu, has482@nyu.edu)
 * @brief Used for deadlock detection to create waitForGraph. A graph is a collection of vertices, where the edges are stored as Adjacency list in a Vertex.
 * @version 0.1
 * @date 2019-12-02
 *
 * @copyright Copyright (c) 2019
 *
 */

class Graph {
    private Map<Long, Vertex> allVertex;
    private List<Integer> deadlockedVertices;
    private Vertex endOfCycle;

    public Graph()
    {
        allVertex = new HashMap<Long, Vertex>();
    }

    /**
     * Adds edge to waitforgraph
     * @param id1 Transaction number
     * @param id2 Transaction number
     */
    public void addEdge( long id1,
                         long id2 )
    {
        // Cannot add a self cycle
        if( id1 == id2 )
        {
            return;
        }
        System.out.println("Edge from "+id1+ " to "+id2);
        Vertex vertex1 = null;

        if( allVertex.containsKey( id1 ) )
        {
            vertex1 = allVertex.get( id1 );
        }
        else
        {
            vertex1 = new Vertex( id1 );
            allVertex.put( id1, vertex1 );
        }

        Vertex vertex2 = null;

        if( allVertex.containsKey( id2 ) )
        {
            vertex2 = allVertex.get( id2 );
        }
        else
        {
            vertex2 = new Vertex( id2 );
            allVertex.put( id2, vertex2 );
        }

        // Add the vertex to the adjacency list of the vertex
        vertex1.addAdjacentVertex( vertex2 );
    }

    /**
     * remove all edge from wait for graph for a vertex when the transaction is deleted.
     * @param id
     */
    public void removeEdges( long id )
    {
        Vertex done = allVertex.get( id );

        for( Map.Entry<Long, Vertex> entry : allVertex.entrySet() )
        {
            entry.getValue().removeAdjacentVertex( done );
        }

        allVertex.remove( id );
    }

    public Collection<Vertex> getAllVertex()
    {
        return allVertex.values();
    }

    /**
     * @brief Detects if there is a cycle in the graph using DFS and maintaing
     *        gray(currently being explored), white(unexplored) and black(explored)
     *        sets.
     * @return A list of deadlocked Vertices
     */
    public List<Integer> hasCycle()
    {
        Set<Vertex> whiteSet = new HashSet<>();
        // Gray Set maps each new vertex explored to the vertex it was introduced by
        Map<Vertex, Vertex> graySet = new HashMap<>();
        Set<Vertex> blackSet = new HashSet<>();

        this.deadlockedVertices = new ArrayList<>();

        // Add all vertices to the white set
        for( Vertex vertex : this.getAllVertex() )
        {
            whiteSet.add( vertex );
        }

        // Perform a dfs on each unexplored vertex
        while( whiteSet.size() > 0 )
        {
            Vertex current = whiteSet.iterator().next();
            Vertex previous = null;

            // dfs returns true if a cycle is found
            if( dfs( current, previous, whiteSet, graySet, blackSet ) )
            {
                // Get the mapping of vertices to get the deadlocked vertices and return
                Map<Vertex, Vertex> graySetInversed = graySet.entrySet().stream()
                                                         .collect( Collectors.toMap( Map.Entry::getValue, Map.Entry::getKey ) );

                while( true )
                {
                    /* System.out.println("Adding to cycle:"+ endOfCycle.getId()); */
                    deadlockedVertices.add( ( int ) endOfCycle.getId() );

                    if( graySetInversed.containsKey( endOfCycle ) )
                    {
                        endOfCycle = graySetInversed.get( endOfCycle );
                    }
                    else
                    {
                        return deadlockedVertices;
                    }
                }
            }
        }

        return deadlockedVertices;
    }

    private boolean dfs( Vertex current,
                         Vertex previous,
                         Set<Vertex> whiteSet,
                         Map<Vertex, Vertex> graySet,
                         Set<Vertex> blackSet )
    {
        // Move current to gray set from white set and then explore it.
        whiteSet.remove( current );
        graySet.put( current, previous );

        for( Vertex neighbor : current.getAdjacentVertexes() )
        {
            // If in black set means already explored so continue.
            if( blackSet.contains( neighbor ) )
            {
                continue;
            }

            /* if in gray set then cycle found. */
            if( graySet.containsKey( neighbor ) )
            {
                /* Store the end of the cycle */
                /* System.out.println("end of cycle:" + neighbor.getId()); */
                this.endOfCycle = neighbor;
                return true;
            }

            if( dfs( neighbor, current, whiteSet, graySet, blackSet ) )
            {
                return true;
            }
        }

        /* Move vertex from gray set to black set when done exploring. */
        graySet.remove( current );
        blackSet.add( current );
        return false;
    }

    @Override
    public String toString()
    {
        return "avail vertexes are: " + allVertex;
    }

    public void print()
    {
        for( Map.Entry<Long, Vertex> entry : allVertex.entrySet() )
        {
            System.out.print( entry.getKey() + ": " );
            entry.getValue().print();
        }
    }
}

/**
 * Creates vertex for graph
 */
class Vertex {
    long id;
    /* Edges are stored as adjacency list in each vertex */
    private List<Vertex> adjacentVertex = new ArrayList<>();

    Vertex( long id )
    {
        this.id = id;
    }

    public long getId()
    {
        return id;
    }

    public void addAdjacentVertex( Vertex v )
    {
        adjacentVertex.add( v );
    }

    public List<Vertex> getAdjacentVertexes()
    {
        return adjacentVertex;
    }

    public void removeAdjacentVertex( Vertex v )
    {
        adjacentVertex.remove( v );
    }

    @Override
    public String toString()
    {
        return String.valueOf( id );
    }

    public void print()
    {
        System.out.println( adjacentVertex );
    }
}
