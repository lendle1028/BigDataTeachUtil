/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package imsofa.weka.model;

import java.io.File;
import java.io.IOException;
import weka.core.Instances;
import weka.core.converters.CSVLoader;

/**
 *
 * @author lendle
 */
public class InstanceData {

    private File csvFile = null;
    private String title = null;
    private Instances instances = null;

    public InstanceData(String title, File csvFile, Instances instances) {
        this.title = title;
        this.csvFile = csvFile;
        this.instances = instances;
    }

    public File getCsvFile() {
        return csvFile;
    }

    public void setCsvFile(File csvFile) {
        this.csvFile = csvFile;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public synchronized Instances getInstances() {
        if (instances == null) {
            CSVLoader loader = new CSVLoader();
            try {
                loader.setFile(this.csvFile);
                instances=loader.getDataSet();
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
        return instances;
    }

    public void setInstances(Instances instances) {
        this.instances = instances;
    }

}
