/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package imsofa.weka.factory;

import com.google.gson.Gson;
import imsofa.weka.model.lecture.Lecture;
import imsofa.weka.model.lecture.LectureCategory;
import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.apache.commons.io.FileUtils;

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
     * .........csv file1
     * .........csv file2
     * .........lecture.conf
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
            try {
                /*File [] csvFiles=lectureFolder.listFiles(new FileFilter() {
                    @Override
                    public boolean accept(File pathname) {
                        return pathname.getName().toLowerCase().endsWith(".csv");
                    }
                });*/
                File confFile=new File(lectureFolder, "lecture.conf");
                String content=FileUtils.readFileToString(confFile, "utf-8");
                Lecture lecture=new Gson().fromJson(content, Lecture.class);
                
                lecture.setHomeFolder(lectureFolder);
                lectureCategory.getLectures().add(lecture);
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
        return lectureCategory;
    }
}
