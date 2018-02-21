/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package imsofa.weka.model.lecture;

import com.google.gson.Gson;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author lendle
 */
public class Lecture {
    private String name=null;
    private List<LectureMaterial> sourceFiles=new ArrayList<>();
    private transient File homeFolder=null;
    private String comment=null;

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }
    
    

    public File getHomeFolder() {
        return homeFolder;
    }

    public void setHomeFolder(File homeFolder) {
        this.homeFolder = homeFolder;
    }
    

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

//    public File[] getSourceFiles() {
//        return sourceFiles;
//    }
//
//    public void setSourceFiles(File[] sourceFiles) {
//        this.sourceFiles = sourceFiles;
//    }

    public List<LectureMaterial> getSourceFiles() {
        return sourceFiles;
    }

    public void setSourceFiles(List<LectureMaterial> sourceFiles) {
        this.sourceFiles = sourceFiles;
    }
    
    public static void main(String [] args) throws Exception{
        Lecture lecture=new Lecture();
        lecture.setComment("comment");
        lecture.setHomeFolder(new File("."));
        LectureMaterial lectureMaterial=new LectureMaterial();
        lectureMaterial.setDataFile(new File("test.csv"));
        lectureMaterial.setComment("comment");
        lecture.getSourceFiles().add(lectureMaterial);
        Gson gson=new Gson();
        String json=gson.toJson(lecture);
        System.out.println(json);
        System.out.println(gson.fromJson(json, Lecture.class));
    }
}
