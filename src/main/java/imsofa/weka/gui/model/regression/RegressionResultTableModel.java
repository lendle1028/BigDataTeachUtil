/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package imsofa.weka.gui.model.regression;

import imsofa.weka.gui.model.AbstractPredictionTableModel;
import imsofa.weka.gui.regression.RegressionResult;
import java.util.Map;
import weka.core.Attribute;

/**
 *
 * @author lendle
 */
public class RegressionResultTableModel extends AbstractPredictionTableModel{
    protected RegressionResult regressionResult=null;

    public RegressionResult getRegressionResult() {
        return regressionResult;
    }

    public void setRegressionResult(RegressionResult regressionResult) {
        this.regressionResult = regressionResult;
    }
    
    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        if(columnIndex!=instances.numAttributes()){
            Attribute attribute=instances.instance(rowIndex).attribute(columnIndex);
            if(attribute.isNominal()){
                return instances.instance(rowIndex).stringValue(attribute);
            }else{
                return instances.instance(rowIndex).value(attribute);
            }
        }else{
            double estimatedY=regressionResult.getIntercept();
            for(Map.Entry<String,Double> entry: regressionResult.getCoefs().entrySet()){
                String attrName=entry.getKey();
                Attribute attribute=instances.attribute(attrName);
                estimatedY+=instances.instance(rowIndex).value(attribute)*entry.getValue();
            }
            return estimatedY;
        }
    }
    
}
