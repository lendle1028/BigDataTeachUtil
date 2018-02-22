/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package imsofa.weka.gui;

import imsofa.weka.gui.model.ClusterResultTableModel;
import imsofa.weka.gui.model.regression.RegressionResultTableModel;
import imsofa.weka.gui.model.regression.RegressionStatisticsTableModel;
import imsofa.weka.gui.model.regression.RegressionParameter;
import imsofa.weka.gui.regression.RegressionResult;
import imsofa.weka.gui.table.ClassPredictionResultTable;
import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.util.Enumeration;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.SwingUtilities;
import net.sourceforge.openforecast.DataSet;
import net.sourceforge.openforecast.Observation;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
import weka.core.Attribute;

/**
 *
 * @author lendle
 */
public abstract class AbstractRegressionPanel extends AbstractModelingPanel {
    public void setPanelContext(ModelingPanelContext panelContext) {
        super.setPanelContext(panelContext);
        Enumeration<Attribute> attributes = panelContext.getInstances().enumerateAttributes();
//        this.comboboxTarget.removeAllItems();
//        while (attributes.hasMoreElements()) {
//            Attribute attribute = attributes.nextElement();
//            if (attribute.isNumeric()) {
//                this.comboboxTarget.addItem(attribute.name());
//            }
//        }
    }
   

    /**
     * Creates new form KmeansClusterPanel
     */
    public AbstractRegressionPanel() {
        try {
            initComponents();
            tableResults.setModel(new ClusterResultTableModel());

            buttonStart.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    try {
                        startButtonActionPerformed();
                    } catch (Exception ex) {
                        Logger.getLogger(AbstractRegressionPanel.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
            });
        } catch (Exception ex) {
            Logger.getLogger(AbstractRegressionPanel.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

    protected abstract RegressionResult performRegression(List<RegressionParameter> parameters);
    protected abstract List<RegressionParameter> getRegressionParameters();
    
    protected void startButtonActionPerformed() throws Exception{
        panelPlot.removeAll();
        
        List<RegressionParameter> parameters=this.getRegressionParameters();
        RegressionResult result=this.performRegression(parameters);
        String leftFieldName=null;
        for(RegressionParameter parameter : parameters){
            if(parameter.isAsLeftField()){
                leftFieldName=parameter.getFieldName();
                break;
            }
        }
        DataSet dataSet=new DataSet();
        double[] y = new double[panelContext.getInstances().numInstances()];
        for (int i = 0; i < y.length; i++) {
            y[i]=panelContext.getInstances().instance(i).value(panelContext.getInstances().attribute(leftFieldName));
            Observation observation=new Observation(y[i]);
            for(int j=0; j<panelContext.getInstances().numAttributes(); j++){
                Attribute attribute=panelContext.getInstances().attribute(j);
                if(attribute.isNumeric() && attribute.name().equals(leftFieldName)==false){
                    observation.setIndependentValue(attribute.name(), panelContext.getInstances().instance(i).value(attribute));
                }
            }
            dataSet.add(observation);
        }
        
        XYSeriesCollection dataset = new XYSeriesCollection();
        XYSeries series1 = new XYSeries("data");
        
        for (int i = 0; i < panelContext.getInstances().numInstances(); i++) {
            double estimatedY=result.getIntercept();
            for(Map.Entry<String,Double> entry: result.getCoefs().entrySet()){
                String attrName=entry.getKey();
                if(attrName.contains("^")){
                    attrName=attrName.substring(0, attrName.indexOf("^"));
                }
                Attribute attribute=panelContext.getInstances().attribute(attrName);
                estimatedY+=panelContext.getInstances().instance(i).value(attribute)*entry.getValue();
            }
            Attribute resultAttribute=panelContext.getInstances().attribute(leftFieldName);
            series1.add(panelContext.getInstances().instance(i).value(resultAttribute), estimatedY);
        }
        dataset.addSeries(series1);
        JFreeChart chart = ChartFactory.createScatterPlot("plot", "X", "Y", dataset, PlotOrientation.HORIZONTAL, true, true, true);
        XYPlot plot = (XYPlot) chart.getPlot();
        plot.setBackgroundPaint(new Color(255, 228, 196));

        // Create Panel
        ChartPanel panel = new ChartPanel(chart);
        
        this.panelPlot.add(panel);
        
        RegressionResultTableModel model=new RegressionResultTableModel();
        model.setInstances(panelContext.getInstances());
        model.setRegressionResult(result);
        tableResults.setModel(model);
        
        RegressionStatisticsTableModel regressionStatisticsTableModel=new RegressionStatisticsTableModel();
        regressionStatisticsTableModel.setCoefs(result.getCoefs());
        regressionStatisticsTableModel.setIntercept(result.getIntercept());
        regressionStatisticsTableModel.setInterceptPValue(result.getInterceptPValue());
        regressionStatisticsTableModel.setMape(result.getMape());
        regressionStatisticsTableModel.setMse(result.getMse());
        regressionStatisticsTableModel.setpValues(result.getPValues());
        tableStatistics.setModel(regressionStatisticsTableModel);
        
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                tableResults.updateUI();
                tableStatistics.updateUI();
                revalidate();
            }
        });
    }
    
    //protected abstract Map createOptions();

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jSplitPane1 = new javax.swing.JSplitPane();
        panelLeft = new javax.swing.JPanel();
        panelSettings = new javax.swing.JPanel();
        panelCommonSettings = new javax.swing.JPanel();
        panelSpecificSettings = new javax.swing.JPanel();
        panelActions = new javax.swing.JPanel();
        buttonStart = new javax.swing.JButton();
        buttonSave = new javax.swing.JButton();
        jSplitPane2 = new javax.swing.JSplitPane();
        jScrollPane1 = new javax.swing.JScrollPane();
        panelPlot = new javax.swing.JPanel();
        jTabbedPane1 = new javax.swing.JTabbedPane();
        jScrollPane2 = new javax.swing.JScrollPane();
        tableStatistics = new javax.swing.JTable();
        scrollpane = new javax.swing.JScrollPane();
        tableResults = new ClassPredictionResultTable();

        setLayout(new java.awt.BorderLayout());

        jSplitPane1.setDividerLocation(400);

        panelLeft.setLayout(new java.awt.BorderLayout());

        panelSettings.setLayout(new java.awt.GridLayout(2, 1));

        panelCommonSettings.setBorder(javax.swing.BorderFactory.createTitledBorder("一般設定"));
        panelCommonSettings.setPreferredSize(new java.awt.Dimension(299, 100));
        panelCommonSettings.setLayout(new java.awt.BorderLayout());
        panelSettings.add(panelCommonSettings);

        panelSpecificSettings.setLayout(new java.awt.BorderLayout());
        panelSettings.add(panelSpecificSettings);

        panelLeft.add(panelSettings, java.awt.BorderLayout.CENTER);

        panelActions.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT));

        buttonStart.setText("執行");
        panelActions.add(buttonStart);

        buttonSave.setText("儲存");
        buttonSave.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                buttonSaveActionPerformed(evt);
            }
        });
        panelActions.add(buttonSave);

        panelLeft.add(panelActions, java.awt.BorderLayout.SOUTH);

        jSplitPane1.setLeftComponent(panelLeft);

        jSplitPane2.setDividerLocation(150);
        jSplitPane2.setOrientation(javax.swing.JSplitPane.VERTICAL_SPLIT);

        panelPlot.setLayout(new java.awt.BorderLayout());
        jScrollPane1.setViewportView(panelPlot);

        jSplitPane2.setBottomComponent(jScrollPane1);

        tableStatistics.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {

            }
        ));
        jScrollPane2.setViewportView(tableStatistics);

        jTabbedPane1.addTab("統計", jScrollPane2);

        tableResults.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null}
            },
            new String [] {
                "Title 1", "Title 2", "Title 3", "Title 4"
            }
        ));
        scrollpane.setViewportView(tableResults);

        jTabbedPane1.addTab("資料", scrollpane);

        jSplitPane2.setTopComponent(jTabbedPane1);

        jSplitPane1.setRightComponent(jSplitPane2);

        add(jSplitPane1, java.awt.BorderLayout.CENTER);
    }// </editor-fold>//GEN-END:initComponents

    private void buttonSaveActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_buttonSaveActionPerformed
        // TODO add your handling code here:
        this.saveModelAction();
    }//GEN-LAST:event_buttonSaveActionPerformed


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton buttonSave;
    private javax.swing.JButton buttonStart;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JSplitPane jSplitPane1;
    private javax.swing.JSplitPane jSplitPane2;
    private javax.swing.JTabbedPane jTabbedPane1;
    protected javax.swing.JPanel panelActions;
    protected javax.swing.JPanel panelCommonSettings;
    private javax.swing.JPanel panelLeft;
    private javax.swing.JPanel panelPlot;
    protected javax.swing.JPanel panelSettings;
    protected javax.swing.JPanel panelSpecificSettings;
    private javax.swing.JScrollPane scrollpane;
    private javax.swing.JTable tableResults;
    private javax.swing.JTable tableStatistics;
    // End of variables declaration//GEN-END:variables
}
