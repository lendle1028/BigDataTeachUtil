/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package imsofa;

import java.awt.Color;
import java.io.File;
import java.util.Arrays;
import javax.swing.JFrame;
import javax.swing.WindowConstants;
import org.apache.commons.math3.stat.regression.OLSMultipleLinearRegression;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
import weka.core.Instances;
import weka.core.converters.CSVLoader;

/**
 *
 * @author lendle
 */
public class TestLinearRegression {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) throws Exception {
        // TODO code application logic here
        CSVLoader loader = new CSVLoader();
        loader.setFile(new File("iris.csv"));
        Instances instances = loader.getDataSet();
//        System.out.println(instances.attribute(4));
//        instances.setClass(instances.attribute(4));
//        LinearRegression linearRegression=new LinearRegression();
//        linearRegression.setDoNotCheckCapabilities(true);
//        linearRegression.buildClassifier(instances);
//        System.out.println(linearRegression);
        
        

        OLSMultipleLinearRegression regression = new OLSMultipleLinearRegression();
        //double[] y = new double[]{11.0, 12.0, 13.0, 14.0, 15.0, 16.0};
        double[] y = instances.attributeToDoubleArray(0);
        double[][] x = new double[y.length][];
        for (int i = 0; i < y.length; i++) {
            x[i] = new double[]{instances.instance(i).value(1), instances.instance(i).value(2), instances.instance(i).value(3)};
        }

        regression.newSampleData(y, x);

        System.out.println(Arrays.toString(regression.estimateRegressionParameters()));
        System.out.println(regression.calculateRSquared());
        System.out.println(Arrays.toString(regression.estimateResiduals()));
        System.out.println(regression.calculateResidualSumOfSquares());
        System.out.println(Arrays.toString(regression.estimateRegressionParametersStandardErrors()));
        JFrame frame = new JFrame();
        frame.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        frame.setSize(800, 600);

        XYSeriesCollection dataset = new XYSeriesCollection();
        XYSeries series1 = new XYSeries("data");
        
        double [] parameters=regression.estimateRegressionParameters();
        for (int i = 0; i < instances.numInstances(); i++) {
            series1.add(instances.instance(i).value(0), parameters[0]+parameters[1]*instances.instance(i).value(1)+parameters[2]*instances.instance(i).value(2));
        }
        dataset.addSeries(series1);
        JFreeChart chart = ChartFactory.createScatterPlot("plot", "X", "Y", dataset, PlotOrientation.HORIZONTAL, true, true, true);
        XYPlot plot = (XYPlot) chart.getPlot();
        plot.setBackgroundPaint(new Color(255, 228, 196));

        // Create Panel
        ChartPanel panel = new ChartPanel(chart);
        frame.add(panel);

        frame.setVisible(true);
    }

}
