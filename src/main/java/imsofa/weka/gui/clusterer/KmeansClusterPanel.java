/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package imsofa.weka.gui.clusterer;

import imsofa.weka.Global;
import imsofa.weka.gui.AbstractClusterPanel;
import java.util.HashMap;
import java.util.Map;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingUtilities;
import weka.clusterers.ClusterEvaluation;
import weka.clusterers.SimpleKMeans;
import weka.core.DistanceFunction;
import weka.core.EuclideanDistance;
import weka.core.Instances;
import weka.core.ManhattanDistance;

/**
 *
 * @author lendle
 */
public class KmeansClusterPanel extends AbstractClusterPanel{
    private KmeansOptionsPanel kmeansOptionsPanel=new KmeansOptionsPanel();
    private SimpleKMeans simpleKMeans = new SimpleKMeans();
    
    public KmeansClusterPanel() {
        super();
        this.clusterer=simpleKMeans;
        SwingUtilities.invokeLater(new Runnable(){
            @Override
            public void run() {
                panelSpecificSettings.add(kmeansOptionsPanel);
                revalidate();
            }
        });
        
    }
    
    

    @Override
    public void setInstances(Instances instances) {
        super.setInstances(instances); //To change body of generated methods, choose Tools | Templates.
        ((SpinnerNumberModel) kmeansOptionsPanel.getSpinnerClusters().getModel()).setMaximum(instances.numInstances());
    }
    
    

    @Override
    protected ClusterEvaluation buildClusterEvaluation(Instances trainInst, Map options) throws Exception {
        simpleKMeans.setNumClusters((Integer)options.get("numClusters"));
        simpleKMeans.buildClusterer(removeClass(trainInst));
        String distanceFunctionName=(String) options.get("distanceFunction");
        DistanceFunction distanceFunction=null;
        if(distanceFunctionName.equals("尤拉距離")){
            distanceFunction=new EuclideanDistance(instances);
        }else{
            distanceFunction=new ManhattanDistance(instances);
        }
        simpleKMeans.setDistanceFunction(distanceFunction);
        ClusterEvaluation eval = new ClusterEvaluation();
        eval.setClusterer(simpleKMeans);
        eval.evaluateClusterer(trainInst, "", false);
        return eval;
    }

    @Override
    protected Map createOptions() {
        Map options=new HashMap();
        options.put("numClusters", Integer.valueOf("" + kmeansOptionsPanel.getSpinnerClusters().getValue()));
        options.put("distanceFunction", kmeansOptionsPanel.getComboboxDistanceFunction().getSelectedItem().toString());
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
