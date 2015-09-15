/**
 * Created by alefr on 9/13/2015.
 */
import org.jgraph.JGraph;
import org.jgrapht.GraphColorTuple;
import org.jgrapht.alg.DijkstraShortestPath;
import org.jgrapht.ext.JGraphModelAdapter;
import org.jgrapht.graph.DefaultWeightedEdge;
import org.jgrapht.graph.ListenableDirectedWeightedGraph;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;


public class DijkstraAlgorithm extends JApplet implements ActionListener
{
    private static final Color BACKGROUND_COLOR= Color.decode("#FAFBFF");
    private static final Dimension DEFAULT_SIZE = new Dimension(1000, 1000);
    protected JPanel controlsPanel;
    protected String StartVertex;
    protected String EndVertex;
    protected JButton NextButton;
    protected JButton PreviousButton;
    protected JButton PlayPauseButton;
    protected JButton SelectFileButton;
    protected JButton MakeDijkstraButton;
    protected File file=null;
    protected ListenableDirectedWeightedGraph<String,DefaultWeightedEdge> dijkstraGraph;
    protected JFileChooser fileChooser = new JFileChooser();
    protected ArrayList<GraphColorTuple<String,DefaultWeightedEdge>> graphColorTupleList;
    protected Integer actualPosition=0;
    protected Set<String> vertexSet;
    protected Set<DefaultWeightedEdge> edgeSet;
    protected boolean isPlaying = false;
    protected boolean dijkstraFlag=false;
    protected java.util.List<DefaultWeightedEdge> dijkstraEdgePath;
    protected java.util.List<String> dijkstraVertexPath;
    protected ArrayList<Object> dijkstraFullPath=new ArrayList<>();
    protected Timer timer;
    private JGraphModelAdapter<String, DefaultWeightedEdge> jgAdapter;
    protected GraphManager graphManager = new GraphManager();


    public static void main()
    {
        DijkstraAlgorithm applet = new DijkstraAlgorithm();
        applet.init();
        JFrame frame = new JFrame();
        frame.getContentPane().add(applet);
        frame.setTitle("Dijkstra Algorithm");
        frame.pack();
        frame.setLocationRelativeTo(null);
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

        MakeDijkstraButton= new JButton("Dijkstra");
        MakeDijkstraButton.setActionCommand("Run");
        MakeDijkstraButton.addActionListener(this);

        NextButton = new JButton("\u2192");
        NextButton.setMnemonic(KeyEvent.VK_RIGHT);
        NextButton.setActionCommand("next");
        NextButton.addActionListener(this);

        PreviousButton = new JButton("\u2190");
        PreviousButton.setMnemonic(KeyEvent.VK_LEFT);
        PreviousButton.setActionCommand("previous");
        PreviousButton.addActionListener(this);

        PlayPauseButton = new JButton("play");
        PlayPauseButton.setMnemonic(KeyEvent.VK_SPACE);
        PlayPauseButton.setActionCommand("play");
        PlayPauseButton.addActionListener(this);

        SelectFileButton = new JButton("select file");
        SelectFileButton.setMnemonic(KeyEvent.VK_ENTER);
        SelectFileButton.setActionCommand("select");
        SelectFileButton.addActionListener(this);

        controlsPanel.add(PreviousButton);
        controlsPanel.add(PlayPauseButton);
        controlsPanel.add(NextButton);
        controlsPanel.add(SelectFileButton);
        controlsPanel.add(MakeDijkstraButton);

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
                    dijkstraFlag=true;
                    dijkstraGraph=executeDijkstra(file);
            }
        }else if(e.getSource()==NextButton){
                if(dijkstraFullPath!=null) {
                    if(actualPosition<dijkstraFullPath.size()) {
                        graphManager.playNextDijkstra(dijkstraFullPath,actualPosition,jgAdapter);
                        actualPosition++;
                    }
                }

        }else if(e.getSource()==PreviousButton){
            if(actualPosition>0) {
                actualPosition--;
                System.out.println("Prev");
                if(dijkstraEdgePath!=null) {
                    if(actualPosition<dijkstraFullPath.size()) {
                        graphManager.playPrevDijkstra(dijkstraFullPath,actualPosition,jgAdapter);
                    }
                }
            }


        }else if(e.getSource()==PlayPauseButton){
            isPlaying = !isPlaying;
            if(!isPlaying){
                timer.stop();
                return;
            }
            continuousPlay();
        }else if((e.getSource() == MakeDijkstraButton) && (dijkstraFlag == true) && dijkstraGraph!=null){

            StartVertex = JOptionPane.showInputDialog(null,"Ingrese Vertice Origen","Select Vertex",JOptionPane.INFORMATION_MESSAGE);
            EndVertex =   JOptionPane.showInputDialog(null,"Ingrese Vertice Destino","Select Vertex",JOptionPane.INFORMATION_MESSAGE);
            if(StartVertex==null|| EndVertex==null||StartVertex.equals("")|| EndVertex.equals("") ) {
                JOptionPane.showMessageDialog(null,"Porfavor Ingresar Ambos Vertices","Error!!!",JOptionPane.ERROR_MESSAGE);
                return;
            }
            executeDijkstraPart2(dijkstraGraph);
        }
    }

    private void continuousPlay(){

        ActionListener a = new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                if(dijkstraEdgePath!=null) {
                    if(isPlaying&&(dijkstraFullPath.size()>actualPosition)){
                        if (actualPosition < dijkstraFullPath.size()) {
                            graphManager.playNextDijkstra(dijkstraFullPath, actualPosition, jgAdapter);
                            actualPosition++;
                        }
                    }
                }else{
                    timer.stop();
                }
            }
        };

        timer = new Timer(1000, a);
        timer.start();
    }


    public ListenableDirectedWeightedGraph<String,DefaultWeightedEdge> executeDijkstra(File file) {
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
                    graphManager.positionVertexAt(splitedVals[0],Integer.parseInt(splitedVals[3])*5,Integer.parseInt(splitedVals[4])*5, Color.white,jgAdapter);
                }
                if(g.addVertex(splitedVals[1])){
                    graphManager.positionVertexAt(splitedVals[1],Integer.parseInt(splitedVals[5])*5,Integer.parseInt(splitedVals[6])*5,Color.white,jgAdapter);
                }


                g.setEdgeWeight(g.addEdge(splitedVals[0], splitedVals[1]), Double.parseDouble(splitedVals[2]));
                ammountofEdges= ammountofEdges+1;
                Iterator<DefaultWeightedEdge> edgeIter = g.edgeSet().iterator();
                for(int i=0;i<ammountofEdges;i++){
                    edgeIter.next();

                }

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
        DijkstraShortestPath<String,DefaultWeightedEdge> dijkstraShortestPath = new DijkstraShortestPath<String, DefaultWeightedEdge>(g,StartVertex,EndVertex);
        graphColorTupleList=dijkstraShortestPath.getGraphList();
        dijkstraEdgePath= dijkstraShortestPath.getPathEdgeList();
        dijkstraVertexPath=dijkstraShortestPath.getVertexPath();



        for(int i=0;i<dijkstraVertexPath.size();i++){
            if(dijkstraVertexPath.get(i)!=null) {
                dijkstraFullPath.add(dijkstraVertexPath.get(i));
                System.out.println(dijkstraVertexPath.get(i).toString());
            }
            if(i<dijkstraEdgePath.size()) {
                dijkstraFullPath.add(dijkstraEdgePath.get(i));
                System.out.println(dijkstraEdgePath.get(i).toString());
            }

        }




        for (DefaultWeightedEdge defaultWeightedEdge : dijkstraShortestPath.getPathEdgeList()) {

            System.out.println(defaultWeightedEdge.toString());

        }

        org.jgrapht.GraphColorTuple<String,DefaultWeightedEdge> graphColorTuple=new GraphColorTuple<>();
        graphColorTuple.vertexColors.add(new HashSet<>());
        graphColorTuple.vertexColors.add(new HashSet<>());
        actualPosition = 0;
        System.out.println("Finished Algorithm");


    }
    

    public void adjustDisplaySettings(JGraph jg)
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
