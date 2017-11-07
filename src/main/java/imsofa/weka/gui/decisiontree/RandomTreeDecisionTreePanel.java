/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package imsofa.weka.gui.decisiontree;

import imsofa.weka.gui.AbstractDecisionTreePanel;
import javax.swing.SwingUtilities;
import weka.classifiers.trees.J48;
import weka.classifiers.trees.RandomTree;

/**
 *
 * @author lendle
 */
public class RandomTreeDecisionTreePanel extends AbstractDecisionTreePanel {
    private RandomTree _classifier=new RandomTree();
    private RandomTreeOptionsPanel randomTreeOptionsPanel=new RandomTreeOptionsPanel();
    public RandomTreeDecisionTreePanel() {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                panelSpecificSettings.add(randomTreeOptionsPanel);
            }
        });
        this.classifier=_classifier;
    }
    
    protected void setupClassifier() {
        int k=Integer.valueOf(""+randomTreeOptionsPanel.getSpinnerK().getValue());
        int numObjs=Integer.valueOf(""+randomTreeOptionsPanel.getSpinnerMinObjs().getValue());
        double minVar=Double.valueOf(""+randomTreeOptionsPanel.getSpinnerMinVariance().getValue());
        int maxDepth=Integer.valueOf(""+randomTreeOptionsPanel.getSpinnerMaxDepths().getValue());
        _classifier.setKValue(k);
        _classifier.setMinNum(numObjs);
        _classifier.setMinVarianceProp(minVar);
        _classifier.setMaxDepth(maxDepth);
    }
}
