/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package imsofa.weka.gui.model.lecture;

import java.io.File;

/**
 *
 * @author lendle
 */
public class Lecture {
    private String name=null;
    private File [] sourceFiles=null;
    private File homeFolder=null;

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

    public File[] getSourceFiles() {
        return sourceFiles;
    }

    public void setSourceFiles(File[] sourceFiles) {
        this.sourceFiles = sourceFiles;
    }
    
    
}
