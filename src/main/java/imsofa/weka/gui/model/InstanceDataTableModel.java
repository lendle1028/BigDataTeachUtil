/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package imsofa.weka.gui.model;

import imsofa.weka.model.InstanceData;
import java.util.ArrayList;
import java.util.List;
import javax.swing.table.AbstractTableModel;

/**
 *
 * @author lendle
 */
public class InstanceDataTableModel extends AbstractTableModel{
    protected List<InstanceData> instanceDataList=new ArrayList<>();
    @Override
    public int getRowCount() {
        return this.instanceDataList.size();
    }

    public List<InstanceData> getInstanceDataList() {
        return instanceDataList;
    }

    public void setInstanceDataList(List<InstanceData> instanceDataList) {
        this.instanceDataList = instanceDataList;
    }
    
    

    @Override
    public int getColumnCount() {
        return 2;
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        InstanceData data=this.instanceDataList.get(rowIndex);
        if(columnIndex==0){
            return data.getTitle();
        }else if(columnIndex==1){
            return data.getCsvFile().getName();
        }else{
            return "";
        }
    }

    @Override
    public boolean isCellEditable(int rowIndex, int columnIndex) {
        return false;
    }

    @Override
    public Class<?> getColumnClass(int columnIndex) {
        return String.class;
    }

    @Override
    public String getColumnName(int column) {
        switch(column){
            case 0: return "標題";
            case 1: return "資料檔案";
        };
        return null;
    }
    
    
}
