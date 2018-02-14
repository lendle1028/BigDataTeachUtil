/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package imsofa.weka.gui.model.regression;

import java.util.ArrayList;
import java.util.List;
import javax.swing.table.AbstractTableModel;

/**
 *
 * @author lendle
 */
public class RegressionParameterTableModel extends AbstractTableModel{
    private List<RegressionParameter> parameters=new ArrayList<>();

    public RegressionParameterTableModel(List<RegressionParameter> parameters) {
        this.parameters=parameters;
    }

    public List<RegressionParameter> getParameters() {
        return parameters;
    }
    
    
    
    @Override
    public boolean isCellEditable(int rowIndex, int columnIndex) {
        return columnIndex!=0;
    }

    @Override
    public Class<?> getColumnClass(int columnIndex) {
        switch(columnIndex){
            case 0: return String.class;
            case 1: return Integer.class;
            case 2: return Boolean.class;
            case 3: return Boolean.class;
        };
        return null;
    }

    @Override
    public String getColumnName(int column) {
        switch(column){
            case 0: return "欄位";
            case 1: return "階數";
            case 2: return "應變數";
            case 3: return "自變數";
        };
        return null;
    }

    
    @Override
    public int getRowCount() {
        return parameters.size();
    }

    @Override
    public int getColumnCount() {
        return 4;
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        RegressionParameter parameter=parameters.get(rowIndex);
        switch(columnIndex){
            case 0: return parameter.getFieldName();
            case 1: return parameter.getRank();
            case 2: return parameter.isAsLeftField();
            case 3: return parameter.isAsRightField();
        };
        return null;
    }

    @Override
    public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
        super.setValueAt(aValue, rowIndex, columnIndex); //To change body of generated methods, choose Tools | Templates.
        RegressionParameter parameter=parameters.get(rowIndex);
        switch(columnIndex){
            case 0: parameter.setFieldName(""+aValue);break;
            case 1: parameter.setRank(Integer.valueOf(""+aValue));break;
            case 2: parameter.setAsLeftField(Boolean.valueOf(""+aValue));break;
            case 3: parameter.setAsRightField(Boolean.valueOf(""+aValue));break;
        };
        return;
    }
    
    
    
}
