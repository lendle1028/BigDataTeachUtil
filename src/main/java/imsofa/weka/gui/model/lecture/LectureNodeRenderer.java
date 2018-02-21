/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package imsofa.weka.gui.model.lecture;

import imsofa.weka.model.lecture.LectureCategory;
import imsofa.weka.model.lecture.Lecture;
import java.awt.Component;
import javax.swing.JLabel;
import javax.swing.JTree;
import javax.swing.tree.DefaultTreeCellRenderer;

/**
 *
 * @author lendle
 */
public class LectureNodeRenderer extends DefaultTreeCellRenderer{

    @Override
    public Component getTreeCellRendererComponent(JTree tree, Object value, boolean sel, boolean expanded, boolean leaf, int row, boolean hasFocus) {
        JLabel label=(JLabel) super.getTreeCellRendererComponent(tree, value, sel, expanded, leaf, row, hasFocus); //To change body of generated methods, choose Tools | Templates.
        if(value instanceof LectureCategory){
            LectureCategory category=(LectureCategory) value;
            label.setText(category.getName());
        }else{
            Lecture lecture=(Lecture) value;
            label.setText(lecture.getName());
        }
        return label;
    }
    
}
