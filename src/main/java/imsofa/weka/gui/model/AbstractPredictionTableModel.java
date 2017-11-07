/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package imsofa.weka.gui.model;

import javax.swing.table.AbstractTableModel;
import weka.core.Instances;

/**
 *
 * @author lendle
 */
public abstract class AbstractPredictionTableModel extends AbstractTableModel {
    protected Instances instances=null;
    protected String resultColumnName=null;

    public String getResultColumnName() {
        return resultColumnName;
    }

    public void setResultColumnName(String resultColumnName) {
        this.resultColumnName = resultColumnName;
    }
    
    

    public Instances getInstances() {
        return instances;
    }

    public void setInstances(Instances instances) {
        this.instances = instances;
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
            return resultColumnName;
        }
    }
}
