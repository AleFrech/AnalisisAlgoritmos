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
/* ----------------------
 * Main.java
 * ----------------------
 * (C) Copyright 2003-2008, by Barak Naveh and Contributors.
 *
 * Original Author:  Barak Naveh
 * Contributor(s):   -
 *
 * $Id$
 *
 * Changes
 * -------
 * 03-Aug-2003 : Initial revision (BN);
 * 07-Nov-2003 : Adaptation to JGraph 3.0 (BN);
 *
 */

import org.jgraph.JGraph;
import org.jgraph.graph.AttributeMap;
import org.jgraph.graph.DefaultGraphCell;
import org.jgraph.graph.GraphConstants;
import org.jgrapht.GraphColorTuple;
import org.jgrapht.ListenableGraph;
import org.jgrapht.UndirectedGraph;
import org.jgrapht.alg.ChromaticNumber;
import org.jgrapht.alg.DijkstraShortestPath;
import org.jgrapht.alg.KruskalMinimumSpanningTree;
import org.jgrapht.ext.JGraphModelAdapter;
import org.jgrapht.graph.*;

import javax.swing.*;
import javax.swing.Timer;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.geom.Rectangle2D;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;


public class Main
    extends JApplet implements ActionListener
{

    private static final long serialVersionUID = 3256444702936019250L;
    private static final Color DEFAULT_BG_COLOR = Color.decode("#FAFBFF");
    private static final Dimension DEFAULT_SIZE = new Dimension(1000, 800);
    protected boolean dijkstraFlag=false;
    protected JPanel controlsPanel;
    protected JButton nextStepButton;
    protected String VertexStart;
    protected String VertexEnd;
    protected JButton previousStepButton;
    protected JButton playPauseButton;
    protected JButton SelectFileButton;
    protected JButton makeDijkstra;
    protected   File file=null;
    protected ListenableDirectedWeightedGraph<String,DefaultWeightedEdge> dijkstraGraph;
    protected JRadioButton graphColoringRadioButton;
    protected JRadioButton shortestPathRadioButton;
    protected JFileChooser fc = new JFileChooser();
    protected ArrayList<GraphColorTuple<String,DefaultWeightedEdge>> graphColorTupleList;
    protected Integer actualPosition=0;
    protected Set<String> vertexSet;
    protected Set<DefaultWeightedEdge> edgeSet;
    protected boolean isPlaying = false;
    protected Timer timer;
    private JGraphModelAdapter<String, DefaultWeightedEdge> jgAdapter;



    public static void main(String [] args)
    {
        Main applet = new Main();
        applet.init();

        JFrame frame = new JFrame();
        frame.getContentPane().add(applet);
        frame.setTitle("Proyecto Analisis");
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        frame.pack();
        frame.setVisible(true);
    }

    /**
     * {@inheritDoc}
     */
    public void init()
    {
        resize(DEFAULT_SIZE);

        controlsPanel = new JPanel();
        controlsPanel.setLayout(new BoxLayout(controlsPanel, BoxLayout.X_AXIS));
        getContentPane().setLayout(new BoxLayout(getContentPane(), BoxLayout.PAGE_AXIS));
        getContentPane().add(controlsPanel);

        makeDijkstra= new JButton("Dijkstra");
        makeDijkstra.setActionCommand("Run");
        makeDijkstra.addActionListener(this);

        nextStepButton = new JButton("\u2192");
        nextStepButton.setMnemonic(KeyEvent.VK_RIGHT);
        nextStepButton.setActionCommand("next");
        nextStepButton.addActionListener(this);

        previousStepButton = new JButton("\u2190");
        previousStepButton.setMnemonic(KeyEvent.VK_LEFT);
        previousStepButton.setActionCommand("previous");
        previousStepButton.addActionListener(this);

        playPauseButton = new JButton("play");
        playPauseButton.setMnemonic(KeyEvent.VK_SPACE);
        playPauseButton.setActionCommand("play");
        playPauseButton.addActionListener(this);

        SelectFileButton = new JButton("select file");
        SelectFileButton.setMnemonic(KeyEvent.VK_ENTER);
        SelectFileButton.setActionCommand("select");
        SelectFileButton.addActionListener(this);

        controlsPanel.add(previousStepButton);
        controlsPanel.add(playPauseButton);
        controlsPanel.add(nextStepButton);
        controlsPanel.add(SelectFileButton);
        controlsPanel.add(makeDijkstra);

        shortestPathRadioButton = new JRadioButton("dijkstra shortest path",true);
        shortestPathRadioButton.setActionCommand("dijsktra");
        shortestPathRadioButton.addActionListener(this);
        graphColoringRadioButton = new JRadioButton("graph coloring");
        graphColoringRadioButton.setActionCommand("coloring");
        graphColoringRadioButton.addActionListener(this);

        ButtonGroup algorithmGroup = new ButtonGroup();
        algorithmGroup.add(shortestPathRadioButton);
        algorithmGroup.add(graphColoringRadioButton);

        JPanel algorithmRadioPanel = new JPanel();
        algorithmRadioPanel.setLayout(new BoxLayout(algorithmRadioPanel, BoxLayout.Y_AXIS));
        algorithmRadioPanel.add(shortestPathRadioButton);
        algorithmRadioPanel.add(graphColoringRadioButton);
        controlsPanel.add(algorithmRadioPanel);

    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if(e.getSource()==SelectFileButton){
            int returnVal = fc.showOpenDialog(this);

            if (returnVal == JFileChooser.APPROVE_OPTION) {
                file = fc.getSelectedFile();
                if(graphColoringRadioButton.isSelected()){
                    dijkstraFlag=false;
                    executeGraphColoring(file);
                }else if(shortestPathRadioButton.isSelected()){
                    dijkstraFlag=true;
                    dijkstraGraph=executeDijkstra(file);
                }

            }
        }else if(e.getSource()==nextStepButton){
            actualPosition++;
            paintGraphPosition();
        }else if(e.getSource()==previousStepButton){
            actualPosition--;
            paintGraphPosition();
        }else if(e.getSource()==playPauseButton){
            isPlaying = !isPlaying;
            if(!isPlaying){
                timer.stop();
                return;
            }

            continuousPlay();
        }else if((e.getSource() == makeDijkstra) && (dijkstraFlag == true) && dijkstraGraph!=null){

             VertexStart = JOptionPane.showInputDialog(null,"Ingrese Vertice Origen","Select Vertex",JOptionPane.INFORMATION_MESSAGE);
             VertexEnd =   JOptionPane.showInputDialog(null,"Ingrese Vertice Destino","Select Vertex",JOptionPane.INFORMATION_MESSAGE);
            if(VertexStart.equals("")|| VertexEnd.equals("")|| VertexStart==null|| VertexEnd==null) {
                JOptionPane.showMessageDialog(null,"Porfavor Ingresar Ambos Vertices","Error!!!",JOptionPane.ERROR_MESSAGE);
                return;
            }
            System.out.println("YESSSS");
            executeDijkstraPart2(dijkstraGraph);
        }
    }

    private void continuousPlay(){
        //while (isPlaying&&(graphColorTupleList.size()>actualPosition)){
           //actualPosition++;
           //paintGraphPosition();
        ActionListener a = new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                if(isPlaying&&(graphColorTupleList.size()>actualPosition)){
                    actualPosition++;
                    paintGraphPosition();
                }else{
                    timer.stop();
                }
            }
        };

        timer = new Timer(1000, a);
        timer.start();

        //}
    }

    private ListenableDirectedWeightedGraph<String,DefaultWeightedEdge> executeDijkstra(File file) {
        ListenableDirectedWeightedGraph<String,DefaultWeightedEdge> g= new ListenableDirectedWeightedGraph<String, DefaultWeightedEdge>(DefaultWeightedEdge.class);
        jgAdapter = new JGraphModelAdapter<>(g);
        JGraph jgraph = new JGraph(jgAdapter);
        adjustDisplaySettings(jgraph);
        getContentPane().add(jgraph);
        try (BufferedReader reader = Files.newBufferedReader(Paths.get(file.getPath()))){
            String line;
            int ammountofEdges=-1;
            while ((line = reader.readLine()) != null) {
                String splitedVals[] = line.split(",");
                if(g.addVertex(splitedVals[0])){
                    positionVertexAt(splitedVals[0],Integer.parseInt(splitedVals[3])*5,Integer.parseInt(splitedVals[4])*5,Color.white);
                }
                if(g.addVertex(splitedVals[1])){
                    positionVertexAt(splitedVals[1],Integer.parseInt(splitedVals[5])*5,Integer.parseInt(splitedVals[6])*5,Color.white);
                }

                //DefaultWeightedEdge tempEdge = new DefaultWeightedEdge();
                g.setEdgeWeight(g.addEdge(splitedVals[0], splitedVals[1]), Double.parseDouble(splitedVals[2]));
                ammountofEdges= ammountofEdges+1;
                Iterator<DefaultWeightedEdge> edgeIter = g.edgeSet().iterator();
                for(int i=0;i<ammountofEdges;i++){
                    edgeIter.next();
                }

                //edgeSet.add(tempEdge);
                g.setEdgeWeight(edgeIter.next(), Double.parseDouble(splitedVals[2]));
                System.out.println(line);
            }
        } catch (IOException x) {
            System.err.format("IOException: %s%n", x);
        }
        vertexSet = new HashSet<>();
        edgeSet = new HashSet<>();
        edgeSet =g.edgeSet();
        vertexSet = g.vertexSet();

        return g;

    }

    private void executeDijkstraPart2( ListenableDirectedWeightedGraph<String,DefaultWeightedEdge> g){
        DijkstraShortestPath<String,DefaultWeightedEdge> dijkstraShortestPath = new DijkstraShortestPath<String, DefaultWeightedEdge>(g,VertexStart,VertexEnd);
        for (DefaultWeightedEdge defaultWeightedEdge : dijkstraShortestPath.getPathEdgeList()) {
            System.out.println(defaultWeightedEdge.toString());
        }

        // KruskalMinimumSpanningTree<String,DefaultWeightedEdge> kruskalMinimumSpanningTree = new KruskalMinimumSpanningTree<>(g);
        // kruskalMinimumSpanningTree.getMinimumSpanningTreeEdgeSet();
        // graphColorTupleList = kruskalMinimumSpanningTree.getGraphList();
        actualPosition = 0;
        paintGraphPosition();

        System.out.println("Finished Algorithm");
    }

    private void executeKruskalMinimumSpanning(File file) {
        ListenableUndirectedWeightedGraph<String, DefaultWeightedEdge> g =
                new ListenableUndirectedWeightedGraph<>(
                        DefaultWeightedEdge.class);

        jgAdapter = new JGraphModelAdapter<>(g);

        JGraph jgraph = new JGraph(jgAdapter);


        adjustDisplaySettings(jgraph);
        getContentPane().add(jgraph);


        try (BufferedReader reader = Files.newBufferedReader(Paths.get(file.getPath()))){
            String line;
            int ammountofEdges=-1;


            while ((line = reader.readLine()) != null) {
                String splitedVals[] = line.split(",");
                if(g.addVertex(splitedVals[0])){
                    positionVertexAt(splitedVals[0],Integer.parseInt(splitedVals[3])*5,Integer.parseInt(splitedVals[4])*5,Color.white);
                }
                if(g.addVertex(splitedVals[1])){
                    positionVertexAt(splitedVals[1],Integer.parseInt(splitedVals[5])*5,Integer.parseInt(splitedVals[6])*5,Color.white);
                }

                //DefaultWeightedEdge tempEdge = new DefaultWeightedEdge();
                g.setEdgeWeight(g.addEdge(splitedVals[0], splitedVals[1]), Double.parseDouble(splitedVals[2]));

                ammountofEdges= ammountofEdges+1;
                Iterator<DefaultWeightedEdge> edgeIter = g.edgeSet().iterator();
                for(int i=0;i<ammountofEdges;i++){
                    edgeIter.next();
                }

                ///edgeSet.add(tempEdge);
                g.setEdgeWeight(edgeIter.next(), Double.parseDouble(splitedVals[2]));


                System.out.println(line);
            }
        } catch (IOException x) {
            System.err.format("IOException: %s%n", x);
        }
        vertexSet = new HashSet<>();
        edgeSet = new HashSet<>();
        edgeSet =g.edgeSet();
        vertexSet = g.vertexSet();


        KruskalMinimumSpanningTree<String,DefaultWeightedEdge> kruskalMinimumSpanningTree = new KruskalMinimumSpanningTree<>(g);
        kruskalMinimumSpanningTree.getMinimumSpanningTreeEdgeSet();

        graphColorTupleList = kruskalMinimumSpanningTree.getGraphList();
        actualPosition = 0;
        paintGraphPosition();

        System.out.println("Finished Algorithm");

    }

    private void executeGraphColoring(File file){
        ListenableGraph<String, DefaultWeightedEdge> g =
                new ListenableUndirectedWeightedGraph<>(
                        DefaultWeightedEdge.class);
        UndirectedGraph<String, DefaultWeightedEdge> undirectedGraph =
                new SimpleGraph<>(DefaultWeightedEdge.class);


        jgAdapter = new JGraphModelAdapter<>(g);

        JGraph jgraph = new JGraph(jgAdapter);


        adjustDisplaySettings(jgraph);
        getContentPane().add(jgraph);


        try (BufferedReader reader = Files.newBufferedReader(Paths.get(file.getPath()))){
            String line;
            vertexSet = new HashSet<>();
            edgeSet = new HashSet<>();

            while ((line = reader.readLine()) != null) {
                String splitedVals[] = line.split(",");
                if(g.addVertex(splitedVals[0])){

                    positionVertexAt(splitedVals[0],Integer.parseInt(splitedVals[2])*5,Integer.parseInt(splitedVals[3])*5,Color.white);
                }
                if(g.addVertex(splitedVals[1])){
                    positionVertexAt(splitedVals[1],Integer.parseInt(splitedVals[4])*5,Integer.parseInt(splitedVals[5])*5,Color.white);
                }
                g.addEdge(splitedVals[0],splitedVals[1]);

                vertexSet.add(splitedVals[0]);
                vertexSet.add(splitedVals[1]);
                undirectedGraph.addVertex(splitedVals[0]);
                undirectedGraph.addVertex(splitedVals[1]);
                undirectedGraph.addEdge(splitedVals[0], splitedVals[1]);

                System.out.println(line);
            }
        } catch (IOException x) {
            System.err.format("IOException: %s%n", x);
        }

        ChromaticNumber<String,DefaultWeightedEdge> chromaticNumber = new ChromaticNumber<>();
        Map<Integer, Set<String>> lastOne =  chromaticNumber.findGreedyColoredGroups(undirectedGraph);

        graphColorTupleList = chromaticNumber.getGraphList();
        GraphColorTuple<String,DefaultWeightedEdge> graphColorTuple=new GraphColorTuple<>();
        graphColorTuple.vertexColors.add(new HashSet<>());
        graphColorTuple.vertexColors.add(new HashSet<>());
        graphColorTuple.vertexColors.addAll(lastOne.values());
        graphColorTupleList.add(graphColorTuple);
        actualPosition = 0;
        paintGraphPosition();

        System.out.println("Finished Algorithm");
    }

    private void paintGraphPosition(){
        ArrayList<Color> colors = new ArrayList<>();
        colors.add(Color.red);
        colors.add(Color.green);
        colors.add(Color.blue);
        colors.add(Color.cyan);
        colors.add(Color.magenta);
        colors.add(Color.orange);
        colors.add(Color.yellow);

        if(actualPosition>graphColorTupleList.size()-1||actualPosition<0){
            return;
        }


        for (String aVertexSet : vertexSet) {
            changeVertexColor(aVertexSet, Color.white);
        }

        for (DefaultWeightedEdge anEdgeSet : edgeSet) {
            changeEdgeColor(anEdgeSet, Color.GRAY);
        }

        Iterator<Color> colorIterator = colors.iterator();

        for(Set<String> stringSet:graphColorTupleList.get(actualPosition).vertexColors){
            Color actualColor = colorIterator.next();

            for (String aStringSet : stringSet) {
                changeVertexColor(aStringSet, actualColor);
            }
        }

        colorIterator = colors.iterator();

        for(Set<DefaultWeightedEdge> edgeSet:graphColorTupleList.get(actualPosition).edgeColors){
            Color actualColor = colorIterator.next();

            for (DefaultWeightedEdge anEdgeSet : edgeSet) {
                changeEdgeColor(anEdgeSet, actualColor);
            }
        }

    }

    private void adjustDisplaySettings(JGraph jg)
    {
        jg.setPreferredSize(DEFAULT_SIZE);

        Color c = DEFAULT_BG_COLOR;
        String colorStr = null;

        try {
            colorStr = getParameter("bgcolor");
        } catch (Exception ignored) {
        }

        if (colorStr != null) {
            c = Color.decode(colorStr);
        }

        jg.setBackground(c);
    }

    @SuppressWarnings("unchecked") // FIXME hb 28-nov-05: See FIXME below
    private void positionVertexAt(Object vertex, int x, int y, Color color)
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

    private void changeVertexColor(Object vertex, Color color)
    {
        DefaultGraphCell cell = jgAdapter.getVertexCell(vertex);
        AttributeMap attr = cell.getAttributes();

        GraphConstants.setBackground(attr,color);

        AttributeMap cellAttr = new AttributeMap();
        cellAttr.put(cell, attr);
        jgAdapter.edit(cellAttr, null, null, null);
    }


    private void changeEdgeColor(DefaultWeightedEdge edge, Color color)
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

}
