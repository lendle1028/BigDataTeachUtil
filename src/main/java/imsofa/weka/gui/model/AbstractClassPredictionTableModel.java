/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package imsofa.weka.gui.model;

/**
 *
 * @author lendle
 */
public abstract class AbstractClassPredictionTableModel extends AbstractPredictionTableModel {
    protected int classIndex=-1;

    public AbstractClassPredictionTableModel() {
        super.resultColumnName="分群結果";
    }
    
    public int getClassIndex() {
        return classIndex;
    }

    public void setClassIndex(int classIndex) {
        this.classIndex = classIndex;
    }

}
