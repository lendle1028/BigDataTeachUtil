/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package imsofa.weka.factory;

import imsofa.weka.model.InstanceData;
import imsofa.weka.model.lecture.Lecture;
import java.io.File;
import java.util.List;

/**
 *
 * @author lendle
 */
public abstract class InstanceDataFactory {
    public abstract List<InstanceData> loadInstanceData(File homeFolder);
    public abstract List<InstanceData> loadInstanceData(Lecture lecture);
    public static final InstanceDataFactory newInstance(){
        return new BasicInstanceDataFactory();
    }
}
