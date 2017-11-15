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
import net.sourceforge.openforecast.DataSet;
import net.sourceforge.openforecast.Observation;
import net.sourceforge.openforecast.models.MultipleLinearRegressionModel;
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
public class TestLinearRegression2 {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) throws Exception {
        // TODO code application logic here
        CSVLoader loader = new CSVLoader();
        loader.setFile(new File("iris.csv"));
//        System.out.println(instances.attribute(4));
//        instances.setClass(instances.attribute(4));
//        LinearRegression linearRegression=new LinearRegression();
//        linearRegression.setDoNotCheckCapabilities(true);
//        linearRegression.buildClassifier(instances);
//        System.out.println(linearRegression);

        double [] y=new double[100];
        double [][] x=new double[100][2];
        for (int i = 0; i < 100; i++) {
            y[i]=i*i;
            x[i][0]=i-2;
            x[i][1]=(i-2)*(i-2);
        }
        
        DataSet dataSet=new DataSet();
        for (int i = 0; i < 100; i++) {
            Observation observation=new Observation(y[i]);
            observation.setIndependentValue("test1", x[i][0]);
            observation.setIndependentValue("test1^2", x[i][1]);
            dataSet.add(observation);
        }
        
        MultipleLinearRegressionModel model=new MultipleLinearRegressionModel();
        model.init(dataSet);
        
        double [] parameters=new double[]{model.getIntercept(), model.getCoefficients().get("test1"), model.getCoefficients().get("test1^2")};

        JFrame frame = new JFrame();
        frame.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        frame.setSize(800, 600);

        XYSeriesCollection dataset = new XYSeriesCollection();
        XYSeries series1 = new XYSeries("data");
        
        for (int i = 0; i < 100; i++) {
            //series1.add(y[i], parameters[0]+parameters[1]*x[i][0]+parameters[2]*x[i][1]);
            series1.add(y[i], parameters[0]+parameters[1]*x[i][0]);
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
