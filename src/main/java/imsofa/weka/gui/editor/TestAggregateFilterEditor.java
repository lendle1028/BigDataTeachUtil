/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package imsofa.weka.gui.editor;

import java.awt.Component;
import java.beans.PropertyEditorSupport;
import javax.swing.JButton;

/**
 *
 * @author lendle
 */
public class TestAggregateFilterEditor extends PropertyEditorSupport{

    @Override
    public boolean supportsCustomEditor() {
        return true; //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public Component getCustomEditor() {
        return new JButton("test");
    }
    
}
