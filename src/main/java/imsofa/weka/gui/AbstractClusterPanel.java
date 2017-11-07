/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package imsofa.weka.gui;

import imsofa.weka.gui.model.ClusterResultTableModel;
import imsofa.weka.gui.table.ClassPredictionResultTable;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Enumeration;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.SwingUtilities;
import weka.clusterers.ClusterEvaluation;
import weka.clusterers.Clusterer;
import weka.core.Attribute;
import weka.core.Instances;
import weka.filters.Filter;
import weka.filters.unsupervised.attribute.Remove;
import weka.gui.explorer.ClustererAssignmentsPlotInstances;
import weka.gui.explorer.ExplorerDefaults;
import weka.gui.visualize.VisualizePanel;

/**
 *
 * @author lendle
 */
public abstract class AbstractClusterPanel extends javax.swing.JPanel {

    protected Instances instances = null;
    protected Clusterer clusterer=null;
    
    public Instances getInstances() {
        return instances;
    }

    public void setInstances(Instances instances) {
        this.instances = instances;
        /*Enumeration<Attribute> attributes = instances.enumerateAttributes();
        this.comboboxClassAttribute.removeAllItems();
        while (attributes.hasMoreElements()) {
            Attribute attribute = attributes.nextElement();
            if (attribute.isNominal()) {
                this.comboboxClassAttribute.addItem(attribute.name());
            }
        }*/
    }

    /**
     * Creates new form KmeansClusterPanel
     */
    public AbstractClusterPanel() {
        try {
            initComponents();
            tableResults.setModel(new ClusterResultTableModel());

            buttonStart.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    try {
                        startButtonActionPerformed();
                    } catch (Exception ex) {
                        Logger.getLogger(AbstractClusterPanel.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
            });
        } catch (Exception ex) {
            Logger.getLogger(AbstractClusterPanel.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

    protected abstract Instances prepareInstances();
    protected abstract Instances prepareTrainingInstances(Instances inst);
    
    protected void startButtonActionPerformed() throws Exception{
        panelPlot.removeAll();

        Instances inst = this.prepareInstances();
        Instances trainInst = this.prepareTrainingInstances(inst);
        //int classIndex = instances.attribute(comboboxClassAttribute.getSelectedItem().toString()).index();
        /*inst.setClassIndex(-1);
        Instances trainInst = new Instances(inst);
        int classIndex = instances.attribute(comboboxClassAttribute.getSelectedItem().toString()).index();
        trainInst.setClassIndex(classIndex);
        inst.setClassIndex(classIndex);*/

        VisualizePanel vp = new VisualizePanel();
        ClustererAssignmentsPlotInstances plotInstances
                = ExplorerDefaults.getClustererAssignmentsPlotInstances();
        plotInstances.setClusterer(clusterer);
        plotInstances.setInstances(inst);
        
        ClusterEvaluation eval = buildClusterEvaluation(trainInst, createOptions());


        //System.out.println(Arrays.toString(eval.getClusterAssignments()));
        //System.out.println(Arrays.toString(eval.getClassesToClusters()));
        //System.out.println(eval.clusterResultsToString());
        plotInstances.setClusterEvaluation(eval);
        plotInstances.canPlot(true);

        vp.setName("test");
        vp.addPlot(plotInstances.getPlotData("test"));
        panelPlot.add(vp);

        ClusterResultTableModel model = new ClusterResultTableModel();
        model.setInstances(instances);
        model.setEvaluation(eval);
        //model.setClassIndex(classIndex);
        tableResults.setModel(model);

        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                tableResults.updateUI();
                repaint();
            }
        });
        plotInstances.cleanUp();
    }

    protected abstract ClusterEvaluation buildClusterEvaluation(Instances trainInst, Map options) throws Exception;
    
    protected abstract Map createOptions();

    protected Instances removeClass(Instances inst) {
        Remove af = new Remove();
        Instances retI = null;

        try {
            if (inst.classIndex() < 0) {
                retI = inst;
            } else {
                af.setAttributeIndices("" + (inst.classIndex() + 1));
                af.setInvertSelection(false);
                af.setInputFormat(inst);
                retI = Filter.useFilter(inst, af);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return retI;
    }

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
        jSplitPane2 = new javax.swing.JSplitPane();
        jScrollPane1 = new javax.swing.JScrollPane();
        panelPlot = new javax.swing.JPanel();
        scrollpane = new javax.swing.JScrollPane();
        tableResults = new ClassPredictionResultTable();

        setLayout(new java.awt.BorderLayout());

        jSplitPane1.setDividerLocation(300);

        panelLeft.setLayout(new java.awt.BorderLayout());

        panelSettings.setLayout(new java.awt.BorderLayout());

        panelCommonSettings.setPreferredSize(new java.awt.Dimension(299, 100));

        javax.swing.GroupLayout panelCommonSettingsLayout = new javax.swing.GroupLayout(panelCommonSettings);
        panelCommonSettings.setLayout(panelCommonSettingsLayout);
        panelCommonSettingsLayout.setHorizontalGroup(
            panelCommonSettingsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 299, Short.MAX_VALUE)
        );
        panelCommonSettingsLayout.setVerticalGroup(
            panelCommonSettingsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 100, Short.MAX_VALUE)
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
