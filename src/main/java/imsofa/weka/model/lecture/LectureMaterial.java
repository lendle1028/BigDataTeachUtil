/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package imsofa.weka.model.lecture;

import com.google.gson.Gson;
import java.io.File;

/**
 *
 * @author lendle
 */
public class LectureMaterial {
    private File dataFile=null;
    private String comment=null;

    public File getDataFile() {
        return dataFile;
    }

    public void setDataFile(File dataFile) {
        this.dataFile = dataFile;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }
    
    public static void main(String [] args) throws Exception{
        LectureMaterial lectureMaterial=new LectureMaterial();
        Gson gson=new Gson();
        lectureMaterial.setDataFile(new File("test.csv"));
        lectureMaterial.setComment("comment");
        System.out.println(gson.toJson(lectureMaterial));
    }
}
