/**
 * Created by alefr on 9/11/2015.
 */

import org.jgraph.JGraph;
import org.jgraph.graph.AttributeMap;
import org.jgraph.graph.DefaultGraphCell;
import org.jgraph.graph.GraphConstants;
import org.jgrapht.GraphColorTuple;
import org.jgrapht.ext.JGraphModelAdapter;
import org.jgrapht.graph.DefaultWeightedEdge;

import java.awt.*;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Set;

public class GraphManager {

    private boolean VerifyNext=true;
    private boolean VerifPrevt=true;

    public void paintGraphPosition(int actualVertexPosition,ArrayList<GraphColorTuple<String,DefaultWeightedEdge>> graphColorTupleList,Set<String> vertexSet,Set<DefaultWeightedEdge> edgeSet,JGraphModelAdapter<String, DefaultWeightedEdge> jgAdapter){
        ArrayList<Color> colors = new ArrayList<>();
        colors.add(Color.yellow);
        colors.add(Color.cyan);
        colors.add(Color.orange);
        colors.add(Color.green);
        colors.add(Color.blue);
        colors.add(Color.red);
        colors.add(Color.pink);
        colors.add(Color.magenta);



        if(actualVertexPosition>graphColorTupleList.size()-1||actualVertexPosition<0){
            return;
        }

        for (String aVertexSet : vertexSet) {
            changeVertexColor(aVertexSet, Color.white,jgAdapter);
        }

        for (DefaultWeightedEdge anEdgeSet : edgeSet) {
            changeEdgeColor(anEdgeSet, Color.GRAY,jgAdapter);
        }

        Iterator<Color> colorIterator = colors.iterator();

        for(Set<String> stringSet:graphColorTupleList.get(actualVertexPosition).vertexColors){
            Color actualColor = colorIterator.next();

            for (String aStringSet : stringSet) {
                changeVertexColor(aStringSet, actualColor,jgAdapter);
            }
        }

        colorIterator = colors.iterator();

        for(Set<DefaultWeightedEdge> edgeset:graphColorTupleList.get(actualVertexPosition).edgeColors){
            Color actualColor = colorIterator.next();

            for (DefaultWeightedEdge anEdgeSet : edgeset) {
                changeEdgeColor(anEdgeSet, actualColor,jgAdapter);
            }
        }

    }

    public void playNextDijkstra(ArrayList<Object> dijkstraFullpath, int actualPosition,JGraphModelAdapter<String, DefaultWeightedEdge> jgAdapter){


                if(VerifyNext) {
                    DefaultGraphCell cell = jgAdapter.getVertexCell(dijkstraFullpath.get(actualPosition));
                    AttributeMap attr = cell.getAttributes();
                    GraphConstants.setBackground(attr, Color.green);
                    AttributeMap cellAttr = new AttributeMap();
                    cellAttr.put(cell, attr);
                    jgAdapter.edit(cellAttr, null, null, null);
                    VerifyNext=false;
                    VerifPrevt=true;
                    return;
                }
                if(!VerifyNext){
                    DefaultGraphCell cell = jgAdapter.getEdgeCell((DefaultWeightedEdge) dijkstraFullpath.get(actualPosition));
                    AttributeMap attr = cell.getAttributes();
                    GraphConstants.setLineColor(attr, Color.green);
                    GraphConstants.setBackground(attr, Color.green);
                    AttributeMap cellAttr = new AttributeMap();
                    cellAttr.put(cell, attr);
                    jgAdapter.edit(cellAttr, null, null, null);
                    VerifyNext=true;
                    VerifPrevt=false;
                    return;
                }

    }
    public void playPrevDijkstra(ArrayList<Object> dijkstraFullpath, int actualPosition,JGraphModelAdapter<String, DefaultWeightedEdge> jgAdapter){

        if(VerifPrevt) {
            DefaultGraphCell cell = jgAdapter.getVertexCell(dijkstraFullpath.get(actualPosition));
            AttributeMap attr = cell.getAttributes();
            GraphConstants.setBackground(attr, Color.white);
            AttributeMap cellAttr = new AttributeMap();
            cellAttr.put(cell, attr);
            jgAdapter.edit(cellAttr, null, null, null);
            VerifPrevt=false;
            VerifyNext=true;
            return;
        }
        if(!VerifPrevt){
            DefaultGraphCell cell = jgAdapter.getEdgeCell((DefaultWeightedEdge) dijkstraFullpath.get(actualPosition));
            AttributeMap attr = cell.getAttributes();
            GraphConstants.setLineColor(attr, Color.decode("#7eaadb"));
            //GraphConstants.setForeground(attr, Color.decode("#354272"));
            AttributeMap cellAttr = new AttributeMap();
            cellAttr.put(cell, attr);
            jgAdapter.edit(cellAttr, null, null, null);
            VerifPrevt=true;
            VerifyNext=false;
            return;
        }

    }


    @SuppressWarnings("unchecked") // FIXME hb 28-nov-05: See FIXME below
    public void positionVertexAt(Object vertex, int x, int y, Color color,JGraphModelAdapter<String, DefaultWeightedEdge> jgAdapter)
    {
        DefaultGraphCell cell = jgAdapter.getVertexCell(vertex);
        AttributeMap attr = cell.getAttributes();

        Rectangle2D bounds = GraphConstants.getBounds(attr);

        Rectangle2D newBounds = new Rectangle2D.Double(x, y, bounds.getWidth(), bounds.getHeight());

        GraphConstants.setBounds(attr, newBounds);
        GraphConstants.setForeground(attr,Color.BLACK);
        GraphConstants.setBackground(attr, color);

        AttributeMap cellAttr = new AttributeMap();
        cellAttr.put(cell, attr);
        jgAdapter.edit(cellAttr, null, null, null);
    }

    public void changeVertexColor(Object vertex, Color color,JGraphModelAdapter<String, DefaultWeightedEdge> jgAdapter)
    {
        DefaultGraphCell cell = jgAdapter.getVertexCell(vertex);
        AttributeMap attr = cell.getAttributes();

        GraphConstants.setBackground(attr,color);

        AttributeMap cellAttr = new AttributeMap();
        cellAttr.put(cell, attr);
        jgAdapter.edit(cellAttr, null, null, null);
    }


    public void changeEdgeColor(DefaultWeightedEdge edge, Color color,JGraphModelAdapter<String, DefaultWeightedEdge> jgAdapter)
    {
        DefaultGraphCell cell = jgAdapter.getEdgeCell(edge);
        AttributeMap attr = cell.getAttributes();

        GraphConstants.setLineColor(attr,color);
        GraphConstants.setBorderColor(attr, color);
        GraphConstants.setForeground(attr, color);

        AttributeMap cellAttr = new AttributeMap();
        cellAttr.put(cell, attr);
        jgAdapter.edit(cellAttr, null, null, null);
    }

    public void adjustDisplaySettings(JGraph jg,Dimension DEFAULT_SIZE,Color BACKGROUND_COLOR)
    {
        jg.setPreferredSize(DEFAULT_SIZE);

        Color c = BACKGROUND_COLOR;
        String colorStr = null;

        try {
            //colorStr = getParameter("bgcolor");
        } catch (Exception ignored) {
        }

        if (colorStr != null) {
            c = Color.decode(colorStr);
        }

        jg.setBackground(c);
    }
}
