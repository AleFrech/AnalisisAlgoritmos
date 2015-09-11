/* ==========================================
 * JGraphT : a free Java graph-theory library
 * ==========================================
 *
 * Project Info:  http://jgrapht.sourceforge.net/
 * Project Creator:  Barak Naveh (http://sourceforge.net/users/barak_naveh)
 *
 * (C) Copyright 2003-2010, by Barak Naveh and Contributors.
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
 * KruskalMinimumSpanningTree.java
 * -------------------------
 * (C) Copyright 2010-2010, by Tom Conerly and Contributors.
 *
 * Original Author:  Tom Conerly
 * Contributor(s):
 *
 * Changes
 * -------
 * 02-Feb-2010 : Initial revision (TC);
 *
 */
package org.jgrapht.alg;

import org.jgrapht.Graph;
import org.jgrapht.GraphColorTuple;
import org.jgrapht.alg.interfaces.MinimumSpanningTree;
import org.jgrapht.alg.util.UnionFind;

import java.util.*;
import java.util.stream.Collectors;


/**
 * An implementation of <a
 * href="http://en.wikipedia.org/wiki/Kruskal's_algorithm">Kruskal's minimum
 * spanning tree algorithm</a>. If the given graph is connected it computes the
 * minimum spanning tree, otherwise it computes the minimum spanning forest. The
 * algorithm runs in time O(E log E). This implementation uses the hashCode and
 * equals method of the vertices.
 *
 * @author Tom Conerly
 * @since Feb 10, 2010
 */
public class KruskalMinimumSpanningTree<V, E>
    implements MinimumSpanningTree<V, E>
{
    public ArrayList<GraphColorTuple<V, E>> getGraphList() {
        return graphList;
    }

    private ArrayList<GraphColorTuple<V, E>> graphList;

    private double spanningTreeCost;
    private Set<E> edgeList;

    

    /**
     * Creates and executes a new KruskalMinimumSpanningTree algorithm instance.
     * An instance is only good for a single spanning tree; after construction,
     * it can be accessed to retrieve information about the spanning tree found.
     *
     * @param graph the graph to be searched
     */
    public KruskalMinimumSpanningTree(final Graph<V, E> graph)
    {
        graphList = new ArrayList<>();
        UnionFind<V> forest = new UnionFind<V>(graph.vertexSet());
        ArrayList<E> allEdges = new ArrayList<E>(graph.edgeSet());
        Collections.sort(
            allEdges,
            new Comparator<E>() {
                @Override public int compare(E edge1, E edge2)
                {
                    return Double.valueOf(graph.getEdgeWeight(edge1)).compareTo(
                        graph.getEdgeWeight(edge2));
                }
            });

        spanningTreeCost = 0;
        edgeList = new HashSet<E>();
        Set<E> repeatedEdges = new HashSet<>();

        for (E edge : allEdges) {
            V source = graph.getEdgeSource(edge);
            V target = graph.getEdgeTarget(edge);

            GraphColorTuple<V,E> newColorTuple= new GraphColorTuple<>();
            Set<V> vertexBeingEvaluated = new HashSet<>();
            vertexBeingEvaluated.add(source);
            vertexBeingEvaluated.add(target);
            Set<V> vertexesFirstRanked = new HashSet<>();
            for(E edgeAdded:edgeList){
                vertexesFirstRanked.add(graph.getEdgeSource(edgeAdded));
                vertexesFirstRanked.add(graph.getEdgeTarget(edgeAdded));
            }
            //vertexAlreadyInThisCurrentColor.addAll(forest.getParentMap(forest.));
            newColorTuple.vertexColors.add(new HashSet<V>());
            newColorTuple.vertexColors.add(vertexesFirstRanked);
            newColorTuple.vertexColors.add(vertexBeingEvaluated);

            Set<E> evaluatedEdge = new HashSet<>();
            evaluatedEdge.add(edge);
            Set<E> duplicateRepeatedEdge = repeatedEdges.stream().collect(Collectors.toSet());
            Set<E> duplicateFirstRankedEdge = edgeList.stream().collect(Collectors.toSet());

            newColorTuple.edgeColors.add(duplicateRepeatedEdge);
            newColorTuple.edgeColors.add(duplicateFirstRankedEdge);
            newColorTuple.edgeColors.add(evaluatedEdge);
            graphList.add(newColorTuple);



            if (forest.find(source).equals(forest.find(target))) {
                repeatedEdges.add(edge);
                continue;
            }

            forest.union(source, target);
            edgeList.add(edge);
            spanningTreeCost += graph.getEdgeWeight(edge);
        }

        GraphColorTuple<V,E> newColorTuple= new GraphColorTuple<>();
        Set<V> vertexesFirstRanked = new HashSet<>();
        for(E edgeAdded:edgeList){
            vertexesFirstRanked.add(graph.getEdgeSource(edgeAdded));
            vertexesFirstRanked.add(graph.getEdgeTarget(edgeAdded));
        }

        newColorTuple.vertexColors.add(new HashSet<V>());
        newColorTuple.vertexColors.add(vertexesFirstRanked);

        Set<E> duplicateRepeatedEdge = repeatedEdges.stream().collect(Collectors.toSet());
        Set<E> duplicateFirstRankedEdge = edgeList.stream().collect(Collectors.toSet());

        newColorTuple.edgeColors.add(duplicateRepeatedEdge);
        newColorTuple.edgeColors.add(duplicateFirstRankedEdge);
        graphList.add(newColorTuple);

    }

    

    @Override public Set<E> getMinimumSpanningTreeEdgeSet()
    {
        return edgeList;
    }

    @Override public double getMinimumSpanningTreeTotalWeight()
    {
        return spanningTreeCost;
    }

    /**
     * Returns edges set constituting the minimum spanning tree/forest
     *
     * @return minimum spanning-tree edges set
     */
    @Deprecated public Set<E> getEdgeSet()
    {
        return getMinimumSpanningTreeEdgeSet();
    }

    /**
     * Returns total weight of the minimum spanning tree/forest.
     *
     * @return minimum spanning-tree total weight
     */
    @Deprecated public double getSpanningTreeCost()
    {
        return getMinimumSpanningTreeTotalWeight();
    }
}

// End KruskalMinimumSpanningTree.java
