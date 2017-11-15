/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package imsofa.weka.gui.regression;

import imsofa.weka.gui.AbstractRegressionPanel;
import net.sourceforge.openforecast.DataSet;
import net.sourceforge.openforecast.Observation;
import net.sourceforge.openforecast.models.MultipleLinearRegressionModel;
import weka.core.Attribute;

/**
 *
 * @author lendle
 */
public class LinearRegressionPanel extends AbstractRegressionPanel{

    @Override
    protected RegressionResult performRegression() {
        RegressionResult result=new RegressionResult();
        DataSet dataSet=new DataSet();
        double[] y = new double[instances.numInstances()];
        for (int i = 0; i < y.length; i++) {
            y[i]=instances.instance(i).value(instances.attribute(this.comboboxTarget.getSelectedItem().toString()));
            Observation observation=new Observation(y[i]);
            for(int j=0; j<instances.numAttributes(); j++){
                Attribute attribute=instances.attribute(j);
                if(attribute.isNumeric() && attribute.name().equals(this.comboboxTarget.getSelectedItem())==false){
                    observation.setIndependentValue(attribute.name(), instances.instance(i).value(attribute));
                }
            }
            dataSet.add(observation);
        }
        MultipleLinearRegressionModel model=new MultipleLinearRegressionModel();
        model.init(dataSet);
        
        result.setCoefs(model.getCoefficients());
        result.setIntercept(model.getIntercept());
        result.setMape(model.getMAPE());
        result.setMse(model.getMSE());
        result.setPValues(model.getPValues());
        return result;
    }
    
}