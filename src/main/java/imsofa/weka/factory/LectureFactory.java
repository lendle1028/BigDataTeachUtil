/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package imsofa.weka.factory;

import imsofa.weka.gui.model.lecture.LectureCategory;
import java.util.List;

/**
 *
 * @author lendle
 */
public abstract class LectureFactory {
    public abstract List<LectureCategory> getLectures();
    public static LectureFactory newInstance(){
        return new BasicLectureFactory();
    }
    
    public static void main(String [] args) throws Exception{
        System.out.println(LectureFactory.newInstance().getLectures().size());
    }
}
