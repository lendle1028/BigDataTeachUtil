/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package imsofa.weka.gui;

import imsofa.weka.gui.model.lecture.Lecture;
import weka.core.Instances;

/**
 *
 * @author lendle
 */
public class ModelingPanelContext {
    private Lecture lecture=null;
    private Instances instances=null;

    public Lecture getLecture() {
        return lecture;
    }

    public void setLecture(Lecture lecture) {
        this.lecture = lecture;
    }

    public Instances getInstances() {
        return instances;
    }

    public void setInstances(Instances instances) {
        this.instances = instances;
    }
    
}
