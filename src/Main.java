import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * Created by alefr on 9/13/2015.
 */
public class Main extends  JFrame {
    private JPanel panel1;

    private JButton coloreadoButton;
    private JButton dijkstraButton;

    public Main() {
        super("Analisis Algoritmos");
        pack();
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setSize(200, 200);
        panel1 = new JPanel();
        panel1.setLayout(new BoxLayout(panel1, BoxLayout.X_AXIS));
        getContentPane().setLayout(new BoxLayout(getContentPane(), BoxLayout.PAGE_AXIS));
        panel1.setLayout(null);
        getContentPane().add(panel1);

        coloreadoButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                GraphColoring color = new GraphColoring();
                color.main();
            }
        });
        dijkstraButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                DijkstraAlgorithm dijkstra = new DijkstraAlgorithm();
                dijkstra.main();
            }
        });

        Insets insets = panel1.getInsets();
        Dimension size = coloreadoButton.getPreferredSize();
        coloreadoButton.setBounds(100 + insets.left, 30 + insets.top,
                size.width, size.height);
        size = dijkstraButton.getPreferredSize();
        dijkstraButton.setBounds(100 + insets.left, 70 + insets.top,
                size.width + 15, size.height);


        panel1.add(coloreadoButton);
        panel1.add(dijkstraButton);



        setVisible(true);
    }
    public static void main(String[] args) {
        Main main = new Main();
        Insets insets = main.getInsets();
        main.setSize(300 + insets.left + insets.right,
                125 + insets.top + insets.bottom);
    }

}


