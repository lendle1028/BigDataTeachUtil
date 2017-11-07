/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package imsofa.weka.gui.model;

import weka.clusterers.ClusterEvaluation;
import weka.core.Attribute;

/**
 *
 * @author lendle
 */
public class ClusterResultTableModel extends AbstractPredictionTableModel{
    private ClusterEvaluation evaluation=null;

    public ClusterResultTableModel() {
        super.resultColumnName="組別";
    }
    
    public ClusterEvaluation getEvaluation() {
        return evaluation;
    }

    public void setEvaluation(ClusterEvaluation evaluation) {
        this.evaluation = evaluation;
    }
    
    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        if(columnIndex<instances.numAttributes()){
            Attribute attribute=instances.instance(rowIndex).attribute(columnIndex);
            if(attribute.isNominal()){
                return instances.instance(rowIndex).stringValue(attribute);
            }else{
                return instances.instance(rowIndex).value(attribute);
            }
        }else{
            int [] classToClusters=this.evaluation.getClassesToClusters();
            double cluster=this.evaluation.getClusterAssignments()[rowIndex];
            return ""+((int)cluster);
//            for(int i=0; i<classToClusters.length; i++){
//                if(classToClusters[i]==cluster){
//                    int index=0;
//                    Enumeration en=instances.attribute(classIndex).enumerateValues();
//                    while(en.hasMoreElements()){
//                        String value=(String)en.nextElement();
//                        if(index==i){
//                            return value;
//                        }
//                        index++;
//                    }
//                }
//            }
//            return "---";
        }
    }
}
