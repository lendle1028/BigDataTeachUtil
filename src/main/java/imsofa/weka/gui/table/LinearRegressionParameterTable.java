/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package imsofa.weka.gui.table;

import imsofa.weka.gui.model.regression.RegressionParameter;
import imsofa.weka.gui.model.regression.RegressionParameterTableModel;

/**
 *
 * @author lendle
 */
public class LinearRegressionParameterTable extends RegressionParameterTable{
    
    public LinearRegressionParameterTable(RegressionParameterTableModel dm) {
        super(dm);
        this.getColumnModel().getColumn(1).setMinWidth(0);
        this.getColumnModel().getColumn(1).setMaxWidth(0);
        this.getColumnModel().getColumn(1).setWidth(0);
        for(RegressionParameter parameter : dm.getParameters()){
            parameter.setRank(1);
        }
    }
    
}
