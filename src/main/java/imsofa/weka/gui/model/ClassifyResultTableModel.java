/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package imsofa.weka.gui.model;

import weka.classifiers.Evaluation;
import weka.core.Attribute;

/**
 *
 * @author lendle
 */
public class ClassifyResultTableModel extends AbstractClassPredictionTableModel{
    private Evaluation evaluation=null;

    public Evaluation getEvaluation() {
        return evaluation;
    }

    public void setEvaluation(Evaluation evaluation) {
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
            return instances.attribute(classIndex).value((int)(evaluation.predictions().get(rowIndex).predicted()));
        }
    }
}
