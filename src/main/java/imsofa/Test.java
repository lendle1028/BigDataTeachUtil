/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package imsofa;

import java.io.PrintWriter;
import java.io.StringWriter;
import javax.swing.JFrame;
import javax.swing.WindowConstants;
import weka.datagenerators.DataGenerator;
import weka.gui.explorer.ClassifierPanel;

/**
 *
 * @author lendle
 */
public class Test {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) throws Exception {
        // TODO code application logic here
        weka.datagenerators.classifiers.regression.Expression generator = new weka.datagenerators.classifiers.regression.Expression();
        DataGenerator.makeData(generator, new String[0]);

        generator.setOutput(new PrintWriter(new StringWriter()));
        //System.out.println(generator.generateExamples());
        ClassifierPanel panel=new ClassifierPanel();
        panel.setInstances(generator.generateExamples());
        JFrame frame=new JFrame();
        frame.setSize(500, 500);
        frame.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        frame.add(panel);
        frame.setVisible(true);
    }

}