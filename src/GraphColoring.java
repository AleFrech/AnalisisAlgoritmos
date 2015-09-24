import org.jgraph.JGraph;
import org.jgrapht.GraphColorTuple;
import org.jgrapht.ListenableGraph;
import org.jgrapht.UndirectedGraph;
import org.jgrapht.alg.ChromaticNumber;
import org.jgrapht.ext.JGraphModelAdapter;
import org.jgrapht.graph.DefaultWeightedEdge;
import org.jgrapht.graph.ListenableUndirectedWeightedGraph;
import org.jgrapht.graph.SimpleGraph;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;


public class GraphColoring extends JApplet implements ActionListener
{

    private static final Dimension DEFAULT_SIZE = new Dimension(1000, 1000);
    protected JPanel controlsPanel;
    private static final Color BACKGROUND_COLOR= UIManager.getColor ( "controlsPanel.getBackground()" );
    protected JButton NextButton;
    protected JButton PreviousButton;
    protected JButton PlayPauseButton;
    protected JButton SelectFileButton;
    protected File file=null;
    protected JFileChooser fileChooser = new JFileChooser();
    protected ArrayList<GraphColorTuple<String,DefaultWeightedEdge>> graphColorTupleList;
    protected Integer actualPosition=0;
    protected Set<String> vertexSet;
    protected Set<DefaultWeightedEdge> edgeSet;
    protected boolean isPlaying = false;
    protected Timer timer;
    private JGraphModelAdapter<String, DefaultWeightedEdge> jgAdapter;
    protected GraphManager graphManager = new GraphManager();

    public static void main()
    {
        GraphColoring applet = new GraphColoring();
        applet.init();
        JFrame frame = new JFrame();
        frame.getContentPane().add(applet);
        frame.setTitle("Graph Coloring");
        frame.setSize(800,600);
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }

    public void init()
    {
        resize(DEFAULT_SIZE);
        controlsPanel = new JPanel();
        getContentPane().setLayout(new BoxLayout(getContentPane(), BoxLayout.PAGE_AXIS));
        getContentPane().add(controlsPanel);
        NextButton = new JButton("\u2192");
        NextButton.setActionCommand("next");
        NextButton.addActionListener(this);
        PreviousButton = new JButton("\u2190");
        PreviousButton.setActionCommand("previous");
        PreviousButton.addActionListener(this);
        PlayPauseButton = new JButton("play");
        PlayPauseButton.setActionCommand("play");
        PlayPauseButton.addActionListener(this);
        SelectFileButton = new JButton("select file");
        SelectFileButton.setActionCommand("select");
        SelectFileButton.addActionListener(this);
        controlsPanel.add(PreviousButton);
        controlsPanel.add(PlayPauseButton);
        controlsPanel.add(NextButton);
        controlsPanel.add(SelectFileButton);
        JPanel algorithmRadioPanel = new JPanel();
        algorithmRadioPanel.setLayout(new BoxLayout(algorithmRadioPanel, BoxLayout.Y_AXIS));
        controlsPanel.add(algorithmRadioPanel);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if(e.getSource()==SelectFileButton){
            int returnVal = fileChooser.showOpenDialog(this);
            if (returnVal == JFileChooser.APPROVE_OPTION) {
                file = fileChooser.getSelectedFile();
                    executeGraphColoring(file);
            }
        }else if(e.getSource()==NextButton){
            actualPosition++;
            graphManager.paintGraphPosition(actualPosition,graphColorTupleList,vertexSet,edgeSet,jgAdapter);
        }else if(e.getSource()==PreviousButton){
            if(actualPosition>0)
                actualPosition--;
            graphManager.paintGraphPosition(actualPosition,graphColorTupleList,vertexSet,edgeSet,jgAdapter);
        }else if(e.getSource()==PlayPauseButton){
            isPlaying = !isPlaying;
            if(!isPlaying){
                timer.stop();
                return;
            }
            continuousPlay();
        }
    }


    private void continuousPlay(){
        ActionListener a = new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                if(isPlaying&&(graphColorTupleList.size()>actualPosition)) {
                    actualPosition++;
                    graphManager.paintGraphPosition(actualPosition,graphColorTupleList,vertexSet,edgeSet,jgAdapter);
                }else
                    timer.stop();
            }
        };
        timer = new Timer(1000, a);
        timer.start();
    }

    private void executeGraphColoring(File file){
        ListenableGraph<String, DefaultWeightedEdge> g = new ListenableUndirectedWeightedGraph<>(DefaultWeightedEdge.class);
        UndirectedGraph<String, DefaultWeightedEdge> undirectedGraph = new SimpleGraph<>(DefaultWeightedEdge.class);
        jgAdapter = new JGraphModelAdapter<>(g);
        JGraph jgraph = new JGraph(jgAdapter);
        graphManager.adjustDisplaySettings(jgraph,DEFAULT_SIZE,BACKGROUND_COLOR);
        getContentPane().add(jgraph);
        try (BufferedReader reader = Files.newBufferedReader(Paths.get(file.getPath()))){
            String line;
            vertexSet = new HashSet<>();
            edgeSet = new HashSet<>();
            while ((line = reader.readLine()) != null) {
                String splitedVals[] = line.split(",");
                if(g.addVertex(splitedVals[0]))
                    graphManager.positionVertexAt(splitedVals[0],Integer.parseInt(splitedVals[2])*5,Integer.parseInt(splitedVals[3])*5,Color.white,jgAdapter);
                if(g.addVertex(splitedVals[1]))
                    graphManager.positionVertexAt(splitedVals[1],Integer.parseInt(splitedVals[4])*5,Integer.parseInt(splitedVals[5])*5,Color.white,jgAdapter);
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
        graphManager.paintGraphPosition(actualPosition, graphColorTupleList, vertexSet, edgeSet, jgAdapter);
        System.out.println("Finished Algorithm");
    }
}
