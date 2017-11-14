/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package imsofa.weka.gui.regression;

import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author lendle
 */
public class RegressionResult {
    protected Map<String, Double> coefs=new HashMap<>();
    protected double mse=-1;
    protected double mape=-1;
    protected Map<String, Double> pValues=new HashMap<>();
    protected double intercept=-1;

    public double getIntercept() {
        return intercept;
    }

    public void setIntercept(double intercept) {
        this.intercept = intercept;
    }
    
    

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

    public Map<String, Double> getPValues() {
        return pValues;
    }

    public void setPValues(Map<String, Double> pValues) {
        this.pValues = pValues;
    }

    
}
