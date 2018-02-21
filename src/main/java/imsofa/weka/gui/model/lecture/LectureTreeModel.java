/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package imsofa.weka.gui.model.lecture;

import imsofa.weka.model.lecture.LectureCategory;
import java.util.List;
import javax.swing.event.TreeModelListener;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreePath;

/**
 *
 * @author lendle
 */
public class LectureTreeModel implements TreeModel{
    private List<LectureCategory> lectureCategories=null;

    public LectureTreeModel(List<LectureCategory> lectureCategories) {
        this.lectureCategories=lectureCategories;
    }
    
    
    public List<LectureCategory> getLectureCategories() {
        return lectureCategories;
    }

    public void setLectureCategories(List<LectureCategory> lectureCategories) {
        this.lectureCategories = lectureCategories;
    }
    
    private Object [] getChildren(Object parent){
        if(parent==this.lectureCategories){
            return this.lectureCategories.toArray();
        }else if(parent instanceof LectureCategory){
            LectureCategory lectureCategory=(LectureCategory) parent;
            return lectureCategory.getLectures().toArray();
        }else{
            return null;
        }
    }
    
    @Override
    public Object getRoot() {
        return lectureCategories;
    }

    @Override
    public Object getChild(Object parent, int index) {
        Object [] children=this.getChildren(parent);
        if(children!=null){
            return children[index];
        }else{
            return null;
        }
    }

    @Override
    public int getChildCount(Object parent) {
        Object [] children=this.getChildren(parent);
        if(children!=null){
            return children.length;
        }else{
            return 0;
        }
    }

    @Override
    public boolean isLeaf(Object node) {
        return getChildCount(node)==0;
    }

    @Override
    public void valueForPathChanged(TreePath path, Object newValue) {
        return;
    }

    @Override
    public int getIndexOfChild(Object parent, Object child) {
        Object [] children=this.getChildren(parent);
        if(children!=null){
            int index=0;
            for(Object c : children){
                if(c==child){
                    return index;
                }
                index++;
            }
            return -1;
        }else{
            return -1;
        }
    }

    @Override
    public void addTreeModelListener(TreeModelListener l) {
        return;
    }

    @Override
    public void removeTreeModelListener(TreeModelListener l) {
        return;
    }
    
}
