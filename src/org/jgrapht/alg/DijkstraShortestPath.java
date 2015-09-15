/* ==========================================
 * JGraphT : a free Java graph-theory library
 * ==========================================
 *
 * Project Info:  http://jgrapht.sourceforge.net/
 * Project Creator:  Barak Naveh (http://sourceforge.net/users/barak_naveh)
 *
 * (C) Copyright 2003-2008, by Barak Naveh and Contributors.
 *
 * This program and the accompanying materials are dual-licensed under
 * either
 *
 * (a) the terms of the GNU Lesser General Public License version 2.1
 * as published by the Free Software Foundation, or (at your option) any
 * later version.
 *
 * or (per the licensee's choosing)
 *
 * (b) the terms of the Eclipse Public License v1.0 as published by
 * the Eclipse Foundation.
 */
/* -------------------------
 * DijkstraShortestPath.java
 * -------------------------
 * (C) Copyright 2003-2008, by John V. Sichi and Contributors.
 *
 * Original Author:  John V. Sichi
 * Contributor(s):   Christian Hammer
 *
 * $Id$
 *
 * Changes
 * -------
 * 02-Sep-2003 : Initial revision (JVS);
 * 29-May-2005 : Make non-static and add radius support (JVS);
 * 07-Jun-2005 : Made generic (CH);
 *
 */
package org.jgrapht.alg;

import org.jgrapht.Graph;
import org.jgrapht.GraphColorTuple;
import org.jgrapht.GraphPath;
import org.jgrapht.Graphs;
import org.jgrapht.graph.GraphPathImpl;
import org.jgrapht.traverse.ClosestFirstIterator;

import java.util.*;


/**
 * An implementation of <a
 * href="http://mathworld.wolfram.com/DijkstrasAlgorithm.html">Dijkstra's
 * shortest path algorithm</a> using <code>ClosestFirstIterator</code>.
 *
 * @author John V. Sichi
 * @since Sep 2, 2003
 */
public final class DijkstraShortestPath<V, E>
{
    

    private GraphPath<V, E> path;
    private ArrayList<GraphColorTuple<V, E>> graphList;
    private GraphColorTuple<V,E> newColorTuple= new GraphColorTuple<>();
    private List<V> dijkstraVertexPath=null;





    /**
     * Creates and executes a new DijkstraShortestPath algorithm instance. An
     * instance is only good for a single search; after construction, it can be
     * accessed to retrieve information about the path found.
     *
     * @param graph the graph to be searched
     * @param startVertex the vertex at which the path should start
     * @param endVertex the vertex at which the path should end
     */
    public DijkstraShortestPath(Graph<V, E> graph, V startVertex, V endVertex)
    {
        this(graph, startVertex, endVertex, Double.POSITIVE_INFINITY);
    }

    /**
     * Creates and executes a new DijkstraShortestPath algorithm instance. An
     * instance is only good for a single search; after construction, it can be
     * accessed to retrieve information about the path found.
     *
     * @param graph the graph to be searched
     * @param startVertex the vertex at which the path should start
     * @param endVertex the vertex at which the path should end
     * @param radius limit on weighted path length, or Double.POSITIVE_INFINITY
     * for unbounded search
     */


    public DijkstraShortestPath(Graph<V, E> graph, V startVertex, V endVertex, double radius)
    {

        if (!graph.containsVertex(endVertex)) {
            throw new IllegalArgumentException(
                "graph must contain the end vertex");
        }

        Set<V> vertexStart = new HashSet<>();

        V source = null;
        V target = null;
        Set<V> vertexset =graph.vertexSet();
        ArrayList<V> vertexList= new ArrayList<>();
        vertexList.addAll(vertexset);
        for(int i=0;i<vertexList.size();i++){
            if(vertexList.get(i).equals(startVertex)){
                 source = vertexList.get(i);
               // dijkstraVertexPath.add(vertexList.get(i));
            }
            if(vertexList.get(i).equals(startVertex)){
                target= vertexList.get(i);
            }
        }

        vertexStart.add(source);
        vertexStart.add(target);
        graphList = new ArrayList<>();
        newColorTuple.vertexColors.add(new HashSet<V>());
        newColorTuple.vertexColors.add(vertexStart);


        ClosestFirstIterator<V, E> iter = new ClosestFirstIterator<V, E>(graph, startVertex, radius);

        while (iter.hasNext()) {
            V vertex = iter.next();

            if (vertex.equals(endVertex)) {
                createEdgeList(graph, iter, startVertex, endVertex);
                graphList.add(newColorTuple);
                return;
            }
        }

        path = null;
    }

    

    /**
     * Return the edges making up the path found.
     *
     * @return List of Edges, or null if no path exists
     */
    public List<E> getPathEdgeList()
    {
        if (path == null) {
            return null;
        } else {
            return path.getEdgeList();
        }
    }

    public List<V> getVertexPath(){
        return dijkstraVertexPath;
    }

    /**
     * Return the path found.
     *
     * @return path representation, or null if no path exists
     */
    public GraphPath<V, E> getPath()
    {
        return path;
    }

    /**
     * Return the weighted length of the path found.
     *
     * @return path length, or Double.POSITIVE_INFINITY if no path exists
     */
    public double getPathLength()
    {
        if (path == null) {
            return Double.POSITIVE_INFINITY;
        } else {
            return path.getWeight();
        }
    }

    public ArrayList<GraphColorTuple<V, E>> getGraphList() {
        return graphList;
    }


    /**
     * Convenience method to find the shortest path via a single static method
     * call. If you need a more advanced search (e.g. limited by radius, or
     * computation of the path length), use the constructor instead.
     *
     * @param graph the graph to be searched
     * @param startVertex the vertex at which the path should start
     * @param endVertex the vertex at which the path should end
     *
     * @return List of Edges, or null if no path exists
     */
    public static <V, E> List<E> findPathBetween(Graph<V, E> graph, V startVertex, V endVertex)
    {
        DijkstraShortestPath<V, E> alg =
            new DijkstraShortestPath<V, E>(
                graph,
                startVertex,
                endVertex);

        return alg.getPathEdgeList();
    }



    private void createEdgeList(Graph<V, E> graph, ClosestFirstIterator<V, E> iter, V startVertex, V endVertex)
    {
        List<E> edgeList = new ArrayList<E>();
        Set<V> vertexEnd = new HashSet<>();
        Set<V>  tmpVertex = new HashSet<>();

        V v = endVertex;

        V vert=null;

        while (true) {
            E edge = iter.getSpanningTreeEdge(v);

            if (edge == null) {
                break;
            }
            Set<E> evaluatedEdge = new HashSet<>();
            evaluatedEdge.add(edge);
            newColorTuple.edgeColors.add(evaluatedEdge);
            edgeList.add(edge);
            v = Graphs.getOppositeVertex(graph, edge, v);

           // dijkstraVertexPath.add(v);
            tmpVertex.add(v);
            tmpVertex.add(v);
            newColorTuple.vertexColors.add(tmpVertex);
        }
        vertexEnd.add(endVertex);
        vertexEnd.add(endVertex);
        newColorTuple.vertexColors.add(vertexEnd);
        //dijkstraVertexPath.add(endVertex);



        Collections.reverse(edgeList);
        double pathLength = iter.getShortestPathLength(endVertex);
        path = new GraphPathImpl<V, E>(graph, startVertex, endVertex, edgeList, pathLength);
        dijkstraVertexPath=Graphs.getPathVertexList(path);
    }
}

// End DijkstraShortestPath.java
