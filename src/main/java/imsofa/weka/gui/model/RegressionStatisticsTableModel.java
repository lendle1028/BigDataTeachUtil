/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package imsofa.weka.gui.model;

import java.util.HashMap;
import java.util.Map;
import javax.swing.table.AbstractTableModel;

/**
 *
 * @author lendle
 */
public class RegressionStatisticsTableModel extends AbstractTableModel{
    protected Map<String, Double> coefs=new HashMap<>();
    protected double mse=-1;
    protected double mape=-1;
    protected Map<String, Double> pValues=new HashMap<>();
    protected double intercept=-1;

    public Map<String, Double> getCoefs() {
        return coefs;
    }

    public void setCoefs(Map<String, Double> coefs) {
        this.coefs = coefs;
    }

    public double getMse() {
        return mse;
    }

    public void setMse(double mse) {
        this.mse = mse;
    }

    public double getMape() {
        return mape;
    }

    public void setMape(double mape) {
        this.mape = mape;
    }

    public Map<String, Double> getpValues() {
        return pValues;
    }

    public void setpValues(Map<String, Double> pValues) {
        this.pValues = pValues;
    }

    public double getIntercept() {
        return intercept;
    }

    public void setIntercept(double intercept) {
        this.intercept = intercept;
    }
    
    
    
    @Override
    public int getRowCount() {
        return this.coefs.size()+3;
    }

    @Override
    public int getColumnCount() {
        return 2;
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        if(columnIndex==0){
            if(rowIndex==0){
                return "截距";
            }else if(rowIndex==coefs.size()+1+0){
                return "MSE";
            }else if(rowIndex==coefs.size()+1+1){
                return "MAPE";
            }else{
                return coefs.keySet().toArray(new String[0])[rowIndex-1];
            }
        }else if(columnIndex==1){
            if(rowIndex==0){
                return ""+this.intercept;
            }else if(rowIndex==coefs.size()+1+0){
                return ""+this.mse;
            }else if(rowIndex==coefs.size()+1+1){
                return ""+this.mape;
            }else{
                return ""+coefs.get(coefs.keySet().toArray(new String[0])[rowIndex-1]);
            }
        }else{
            if(rowIndex==0){
                return "";
            }else if(rowIndex==coefs.size()+1+0){
                return "";
            }else if(rowIndex==coefs.size()+1+1){
                return "";
            }else{
                return ""+pValues.get(coefs.keySet().toArray(new String[0])[rowIndex-1]);
            }
        }
    }

    @Override
    public boolean isCellEditable(int rowIndex, int columnIndex) {
        return false; //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public String getColumnName(int column) {
        switch(column){
            case 0: return "項目";
            case 1: return "數值";
            case 2: return "P";
        }
        return null;
    }
    
    
    
}
