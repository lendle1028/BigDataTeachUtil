/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package imsofa.weka.factory;

import imsofa.weka.gui.model.lecture.Lecture;
import imsofa.weka.gui.model.lecture.LectureCategory;
import java.io.File;
import java.io.FileFilter;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author lendle
 */
public class BasicLectureFactory extends LectureFactory{
    /**
     * load lecture definition from folders
     * the folders must be in the following structure:
     * lectures
     * ...category1
     * ......lecture1
     * ......lecture2
     * @return 
     */
    @Override
    public List<LectureCategory> getLectures() {
        File lectures=new File("lectures");
        List<LectureCategory> ret=new ArrayList<>();
        if(lectures.exists() && lectures.isDirectory()){
            File [] catrgories=lectures.listFiles();
            for(File category : catrgories){
                ret.add(this.loadLectureCategory(category));
            }
        }
        return ret;
    }
    
    private LectureCategory loadLectureCategory(File categoryFolder){
        LectureCategory lectureCategory=new LectureCategory();
        lectureCategory.setName(categoryFolder.getName());
        File [] lectureFolders=categoryFolder.listFiles();
        for(File lectureFolder : lectureFolders){
            Lecture lecture=new Lecture();
            lecture.setName(lectureFolder.getName());
            File [] csvFiles=lectureFolder.listFiles(new FileFilter() {
                @Override
                public boolean accept(File pathname) {
                    return pathname.getName().toLowerCase().endsWith(".csv");
                }
            });
            lecture.setSourceFiles(csvFiles);
            lecture.setHomeFolder(lectureFolder);
            lectureCategory.getLectures().add(lecture);
        }
        return lectureCategory;
    }
}
