/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package imsofa;

import java.io.File;
import weka.core.converters.CSVLoader;

/**
 *
 * @author lendle
 */
public class TestCSVLoader {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) throws Exception{
        // TODO code application logic here
        CSVLoader loader=new CSVLoader();
        loader.setFile(new File("03face.csv"));
        System.out.println(loader.getDataSet());
    }
    
}
