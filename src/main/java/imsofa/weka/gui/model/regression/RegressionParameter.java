/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package imsofa.weka.gui.model.regression;

/**
 *
 * @author lendle
 */
public class RegressionParameter {
    private String fieldName=null;
    private boolean asLeftField=false;
    private boolean asRightField=false;
    private int rank=0;

    public String getFieldName() {
        return fieldName;
    }

    public void setFieldName(String fieldName) {
        this.fieldName = fieldName;
    }

    public boolean isAsLeftField() {
        return asLeftField;
    }

    public void setAsLeftField(boolean asLeftField) {
        this.asLeftField = asLeftField;
    }

    public boolean isAsRightField() {
        return asRightField;
    }

    public void setAsRightField(boolean asRightField) {
        this.asRightField = asRightField;
    }

    public int getRank() {
        return rank;
    }

    public void setRank(int rank) {
        this.rank = rank;
    }
    
    
}
