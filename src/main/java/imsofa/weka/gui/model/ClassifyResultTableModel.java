/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package imsofa.weka.gui.model;

import java.awt.datatransfer.SystemFlavorMap;
import javax.swing.table.AbstractTableModel;
import weka.classifiers.Evaluation;
import weka.core.Attribute;
import weka.core.Instances;

/**
 *
 * @author lendle
 */
public class ClassifyResultTableModel extends AbstractTableModel{
    private Instances instances=null;
    private Evaluation evaluation=null;
    private int classIndex=-1;
    
    public Instances getInstances() {
        return instances;
    }

    public void setInstances(Instances instances) {
        this.instances = instances;
    }

    public Evaluation getEvaluation() {
        return evaluation;
    }

    public void setEvaluation(Evaluation evaluation) {
        this.evaluation = evaluation;
    }

    public int getClassIndex() {
        return classIndex;
    }

    public void setClassIndex(int classIndex) {
        this.classIndex = classIndex;
    }
    
    @Override
    public int getRowCount() {
        if(this.instances==null){
            return 0;
        }
        return this.instances.numInstances();
    }

    @Override
    public int getColumnCount() {
        if(this.instances==null){
            return 0;
        }
        return this.instances.numAttributes()+1;
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
    
    @Override
    public boolean isCellEditable(int rowIndex, int columnIndex) {
        return false;
    }

    @Override
    public Class<?> getColumnClass(int columnIndex) {
        if(columnIndex<instances.numAttributes()){
            //System.out.println(columnIndex+":"+instances.numAttributes()+":"+instances.attribute(columnIndex).isNumeric());
            return (instances.attribute(columnIndex).isNominal())?String.class:double.class;
        }else{
            return String.class;
        }
    }

    @Override
    public String getColumnName(int column) {
        if(column<instances.numAttributes()){
            return instances.attribute(column).name();
        }else{
            return "分群結果";
        }
    }
}
