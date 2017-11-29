/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package imsofa.weka.gui.model;

import java.util.ArrayList;
import java.util.List;
import javax.swing.table.AbstractTableModel;

/**
 *
 * @author lendle
 */
public class PolynomialRegressionParameterTableModel extends AbstractTableModel{
    private List<PolynomialRegressionParameter> parameters=new ArrayList<>();

    public List<PolynomialRegressionParameter> getParameters() {
        return parameters;
    }

    public void setParameters(List<PolynomialRegressionParameter> parameters) {
        this.parameters = parameters;
    }
    
    
    @Override
    public int getRowCount() {
        return this.parameters.size();
    }

    @Override
    public int getColumnCount() {
        return 2;
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        PolynomialRegressionParameter parameter=this.parameters.get(rowIndex);
        if(columnIndex==0){
            return parameter.getAttribute().name();
        }else{
            return parameter.getDegree();
        }
    }

    @Override
    public boolean isCellEditable(int rowIndex, int columnIndex) {
        return columnIndex==1;
    }

    @Override
    public Class<?> getColumnClass(int columnIndex) {
        if(columnIndex==0){
            return String.class;
        }else{
            return int.class;
        }
    }

    @Override
    public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
        PolynomialRegressionParameter parameter=this.parameters.get(rowIndex);
        if(columnIndex==1){
            parameter.setDegree(Integer.valueOf(""+aValue));
        }
    }
    
    
    
}
