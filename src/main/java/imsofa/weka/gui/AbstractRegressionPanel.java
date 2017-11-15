/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package imsofa.weka.gui;

import imsofa.weka.gui.model.ClusterResultTableModel;
import imsofa.weka.gui.model.RegressionResultTableModel;
import imsofa.weka.gui.regression.RegressionResult;
import imsofa.weka.gui.table.ClassPredictionResultTable;
import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Enumeration;
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
import weka.clusterers.ClusterEvaluation;
import weka.clusterers.Clusterer;
import weka.core.Attribute;
import weka.core.Instances;
import weka.filters.Filter;
import weka.filters.unsupervised.attribute.Remove;

/**
 *
 * @author lendle
 */
public abstract class AbstractRegressionPanel extends javax.swing.JPanel {

    protected Instances instances = null;
    protected Clusterer clusterer=null;
    
    public Instances getInstances() {
        return instances;
    }

    public void setInstances(Instances instances) {
        this.instances = instances;
        Enumeration<Attribute> attributes = instances.enumerateAttributes();
        this.comboboxTarget.removeAllItems();
        while (attributes.hasMoreElements()) {
            Attribute attribute = attributes.nextElement();
            if (attribute.isNumeric()) {
                this.comboboxTarget.addItem(attribute.name());
            }
        }
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

    protected abstract RegressionResult performRegression();
    
    protected void startButtonActionPerformed() throws Exception{
        panelPlot.removeAll();

        RegressionResult result=this.performRegression();
        DataSet dataSet=new DataSet();
        double[] y = new double[instances.numInstances()];
        for (int i = 0; i < y.length; i++) {
            y[i]=instances.instance(i).value(instances.attribute(this.comboboxTarget.getSelectedItem().toString()));
            Observation observation=new Observation(y[i]);
            for(int j=0; j<instances.numAttributes(); j++){
                Attribute attribute=instances.attribute(j);
                if(attribute.isNumeric() && attribute.name().equals(this.comboboxTarget.getSelectedItem())==false){
                    observation.setIndependentValue(attribute.name(), instances.instance(i).value(attribute));
                }
            }
            dataSet.add(observation);
        }
        
        XYSeriesCollection dataset = new XYSeriesCollection();
        XYSeries series1 = new XYSeries("data");
        
        for (int i = 0; i < instances.numInstances(); i++) {
            double estimatedY=result.getIntercept();
            for(Map.Entry<String,Double> entry: result.getCoefs().entrySet()){
                String attrName=entry.getKey();
                Attribute attribute=instances.attribute(attrName);
                estimatedY+=instances.instance(i).value(attribute)*entry.getValue();
            }
            Attribute resultAttribute=instances.attribute(this.comboboxTarget.getSelectedItem().toString());
            series1.add(instances.instance(i).value(resultAttribute), estimatedY);
        }
        dataset.addSeries(series1);
        JFreeChart chart = ChartFactory.createScatterPlot("plot", "X", "Y", dataset, PlotOrientation.HORIZONTAL, true, true, true);
        XYPlot plot = (XYPlot) chart.getPlot();
        plot.setBackgroundPaint(new Color(255, 228, 196));

        // Create Panel
        ChartPanel panel = new ChartPanel(chart);
        
        this.panelPlot.add(panel);
        
        RegressionResultTableModel model=new RegressionResultTableModel();
        model.setInstances(instances);
        model.setRegressionResult(result);
        tableResults.setModel(model);
        
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                tableResults.updateUI();
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
        jLabel1 = new javax.swing.JLabel();
        comboboxTarget = new javax.swing.JComboBox<>();
        panelSpecificSettings = new javax.swing.JPanel();
        panelActions = new javax.swing.JPanel();
        buttonStart = new javax.swing.JButton();
        jSplitPane2 = new javax.swing.JSplitPane();
        jScrollPane1 = new javax.swing.JScrollPane();
        panelPlot = new javax.swing.JPanel();
        scrollpane = new javax.swing.JScrollPane();
        tableResults = new ClassPredictionResultTable();

        setLayout(new java.awt.BorderLayout());

        jSplitPane1.setDividerLocation(300);

        panelLeft.setLayout(new java.awt.BorderLayout());

        panelSettings.setLayout(new java.awt.BorderLayout());

        panelCommonSettings.setBorder(javax.swing.BorderFactory.createTitledBorder("一般設定"));
        panelCommonSettings.setPreferredSize(new java.awt.Dimension(299, 100));

        jLabel1.setText("目標欄位：");

        javax.swing.GroupLayout panelCommonSettingsLayout = new javax.swing.GroupLayout(panelCommonSettings);
        panelCommonSettings.setLayout(panelCommonSettingsLayout);
        panelCommonSettingsLayout.setHorizontalGroup(
            panelCommonSettingsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelCommonSettingsLayout.createSequentialGroup()
                .addComponent(jLabel1)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(comboboxTarget, 0, 207, Short.MAX_VALUE))
        );
        panelCommonSettingsLayout.setVerticalGroup(
            panelCommonSettingsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelCommonSettingsLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(panelCommonSettingsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel1)
                    .addComponent(comboboxTarget, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(38, Short.MAX_VALUE))
        );

        panelSettings.add(panelCommonSettings, java.awt.BorderLayout.NORTH);

        panelSpecificSettings.setLayout(new java.awt.BorderLayout());
        panelSettings.add(panelSpecificSettings, java.awt.BorderLayout.CENTER);

        panelLeft.add(panelSettings, java.awt.BorderLayout.CENTER);

        panelActions.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT));

        buttonStart.setText("執行");
        panelActions.add(buttonStart);

        panelLeft.add(panelActions, java.awt.BorderLayout.SOUTH);

        jSplitPane1.setLeftComponent(panelLeft);

        jSplitPane2.setDividerLocation(150);
        jSplitPane2.setOrientation(javax.swing.JSplitPane.VERTICAL_SPLIT);

        panelPlot.setLayout(new java.awt.BorderLayout());
        jScrollPane1.setViewportView(panelPlot);

        jSplitPane2.setBottomComponent(jScrollPane1);

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

        jSplitPane2.setLeftComponent(scrollpane);

        jSplitPane1.setRightComponent(jSplitPane2);

        add(jSplitPane1, java.awt.BorderLayout.CENTER);
    }// </editor-fold>//GEN-END:initComponents


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton buttonStart;
    protected javax.swing.JComboBox<String> comboboxTarget;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JSplitPane jSplitPane1;
    private javax.swing.JSplitPane jSplitPane2;
    protected javax.swing.JPanel panelActions;
    private javax.swing.JPanel panelCommonSettings;
    private javax.swing.JPanel panelLeft;
    private javax.swing.JPanel panelPlot;
    protected javax.swing.JPanel panelSettings;
    protected javax.swing.JPanel panelSpecificSettings;
    private javax.swing.JScrollPane scrollpane;
    private javax.swing.JTable tableResults;
    // End of variables declaration//GEN-END:variables
}