/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package imsofa.weka.gui.decisiontree;

import imsofa.weka.gui.AbstractDecisionTreePanel;
import javax.swing.SwingUtilities;
import weka.classifiers.trees.J48;

/**
 *
 * @author lendle
 */
public class J48DecisionTreePanel extends AbstractDecisionTreePanel {
    private J48 _classifier=new J48();
    private J48OptionsPanel j48OptionsPanel=new J48OptionsPanel();
    public J48DecisionTreePanel() {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                panelSpecificSettings.add(j48OptionsPanel);
            }
        });
        this.classifier=_classifier;
    }
    
    protected void setupClassifier() {
        float confidenceFactor=Float.valueOf(""+j48OptionsPanel.getSpinnerConfidenceFactor().getValue());
        int numObjs=Integer.valueOf(""+j48OptionsPanel.getSpinnerMinObjs().getValue());
        _classifier.setConfidenceFactor(confidenceFactor);
        _classifier.setMinNumObj(numObjs);
    }
}
