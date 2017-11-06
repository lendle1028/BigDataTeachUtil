/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package imsofa;

import java.io.File;
import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.WindowConstants;
import weka.core.converters.CSVLoader;
import weka.gui.arffviewer.ArffSortedTableModel;
import weka.gui.arffviewer.ArffTable;

/**
 *
 * @author lendle
 */
public class TestViewData2 {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) throws Exception {
        // TODO code application logic here
        JFrame frame = new JFrame();
        frame.setSize(800, 500);
        frame.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        ArffTable table=new ArffTable();
        CSVLoader loader=new CSVLoader();
        loader.setFile(new File("03face.csv"));
        ArffSortedTableModel model=new ArffSortedTableModel(loader.getDataSet());
        table.setModel(model);
        JScrollPane pane=new JScrollPane();
        pane.getViewport().add(table);
        frame.add(pane);
        table.setReadOnly(true);
        frame.setVisible(true);
    }

}
