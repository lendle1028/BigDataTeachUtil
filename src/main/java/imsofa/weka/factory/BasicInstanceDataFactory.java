/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package imsofa.weka.factory;

import imsofa.weka.model.InstanceData;
import java.io.File;
import java.io.FileFilter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 *
 * @author lendle
 */
public class BasicInstanceDataFactory extends InstanceDataFactory {

    @Override
    public List<InstanceData> loadInstanceData(File homeFolder) {
        if (homeFolder == null) {
            //return sample data
            return Arrays.asList(new InstanceData[]{
                new InstanceData("Face Data", new File("03face.csv"), null),
                new InstanceData("Iris", new File("iris.csv"), null)
            });
        }else{
            File [] csvFiles=homeFolder.listFiles(new FileFilter() {
                @Override
                public boolean accept(File pathname) {
                    return pathname.getName().toLowerCase().endsWith(".csv");
                }
            });
            List<InstanceData> ret=new ArrayList<>();
            for(File csvFile : csvFiles){
                ret.add(new InstanceData(csvFile.getName(), csvFile, null));
            }
            return ret;
        }
    }

}
