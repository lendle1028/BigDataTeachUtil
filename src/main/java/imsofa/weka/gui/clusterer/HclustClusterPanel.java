/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package imsofa.weka.gui.clusterer;

import imsofa.weka.gui.AbstractClusterPanel;
import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JButton;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingUtilities;
import weka.clusterers.ClusterEvaluation;
import weka.clusterers.HierarchicalClusterer;
import weka.core.DistanceFunction;
import weka.core.Drawable;
import weka.core.EuclideanDistance;
import weka.core.Instances;
import weka.core.ManhattanDistance;
import weka.gui.hierarchyvisualizer.HierarchyVisualizer;
import weka.gui.treevisualizer.PlaceNode2;
import weka.gui.treevisualizer.TreeVisualizer;

/**
 *
 * @author lendle
 */
public class HclustClusterPanel extends AbstractClusterPanel {

    private HclustOptionsPanel hclustOptionsPanel = new HclustOptionsPanel();
    private HierarchicalClusterer hierarchicalClusterer = new HierarchicalClusterer();

    public HclustClusterPanel() {
        super();
        this.clusterer = hierarchicalClusterer;
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                panelSpecificSettings.add(hclustOptionsPanel);
                JButton button = new JButton("顯示樹狀圖");
                panelActions.add(button);
                button.addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        try {
                            //throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
                            final javax.swing.JFrame jf
                                    = new javax.swing.JFrame("階層分群結果");
                            jf.setSize(800, 600);
                            jf.getContentPane().setLayout(new BorderLayout());
                            String graphString=((Drawable)clusterer).graph();
                            if (graphString.contains("digraph")) {
                                TreeVisualizer tv
                                        = new TreeVisualizer(null, graphString, new PlaceNode2());
                                jf.getContentPane().add(tv, BorderLayout.CENTER);
                                jf.addWindowListener(new java.awt.event.WindowAdapter() {
                                    @Override
                                    public void windowClosing(java.awt.event.WindowEvent e) {
                                        jf.dispose();
                                    }
                                });
                                jf.setVisible(true);
                                tv.fitToScreen();
                            } else if (graphString.startsWith("Newick:")) {
                                HierarchyVisualizer tv
                                        = new HierarchyVisualizer(graphString.substring(7));
                                jf.getContentPane().add(tv, BorderLayout.CENTER);
                                jf.addWindowListener(new java.awt.event.WindowAdapter() {
                                    @Override
                                    public void windowClosing(java.awt.event.WindowEvent e) {
                                        jf.dispose();
                                    }
                                });
                                jf.setVisible(true);
                                tv.fitToScreen();
                            }
                        } catch (Exception ex) {
                            Logger.getLogger(HclustClusterPanel.class.getName()).log(Level.SEVERE, null, ex);
                        }
                    }
                });
                revalidate();
            }
        });

    }

    @Override
    public void setInstances(Instances instances) {
        super.setInstances(instances); //To change body of generated methods, choose Tools | Templates.
        ((SpinnerNumberModel) hclustOptionsPanel.getSpinnerClusters().getModel()).setMaximum(instances.numInstances());
    }

    @Override
    protected ClusterEvaluation buildClusterEvaluation(Instances trainInst, Map options) throws Exception {
        hierarchicalClusterer.setNumClusters((Integer) options.get("numClusters"));
        hierarchicalClusterer.buildClusterer(removeClass(trainInst));
        String distanceFunctionName = (String) options.get("distanceFunction");
        DistanceFunction distanceFunction = null;
        if (distanceFunctionName.equals("尤拉距離")) {
            distanceFunction = new EuclideanDistance(instances);
        } else {
            distanceFunction = new ManhattanDistance(instances);
        }
        hierarchicalClusterer.setDistanceFunction(distanceFunction);
        ClusterEvaluation eval = new ClusterEvaluation();
        eval.setClusterer(hierarchicalClusterer);
        eval.evaluateClusterer(trainInst, "", false);
        return eval;
    }

    @Override
    protected Map createOptions() {
        Map options = new HashMap();
        options.put("numClusters", Integer.valueOf("" + hclustOptionsPanel.getSpinnerClusters().getValue()));
        options.put("distanceFunction", hclustOptionsPanel.getComboboxDistanceFunction().getSelectedItem().toString());
        options.put("classIndex", instances.attribute(comboboxClassAttribute.getSelectedItem().toString()).index());
        return options;
    }

    @Override
    protected Instances prepareInstances() {
        Instances inst = new Instances(instances);
        inst.setClassIndex(-1);
        int classIndex = instances.attribute(comboboxClassAttribute.getSelectedItem().toString()).index();
        inst.setClassIndex(classIndex);
        return inst;
    }

    @Override
    protected Instances prepareTrainingInstances(Instances inst) {
        Instances trainInst = new Instances(inst);
        int classIndex = instances.attribute(comboboxClassAttribute.getSelectedItem().toString()).index();
        trainInst.setClassIndex(classIndex);
        return trainInst;
    }

}
