/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package imsofa.weka.factory;

import imsofa.weka.model.InstanceData;
import java.io.File;
import java.util.Arrays;
import java.util.List;

/**
 *
 * @author lendle
 */
public class SampleInstanceDataFactory extends InstanceDataFactory{

    @Override
    public List<InstanceData> loadInstanceData(File homeFolder) {
        return Arrays.asList(new InstanceData[]{
            new InstanceData("Face Data", new File("03face.csv"), null),
            new InstanceData("Iris", new File("iris.csv"), null)
        });
    }
    
}
