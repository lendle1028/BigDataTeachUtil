/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package imsofa.weka.utils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import javax.script.ScriptEngine;
import javax.script.ScriptException;
import org.apache.commons.io.FileUtils;
import org.renjin.script.RenjinScriptEngineFactory;
import weka.core.Instances;
import weka.core.converters.CSVLoader;
import weka.core.converters.CSVSaver;

/**
 *
 * @author lendle
 */
public class RenjinWrapper {

    private ScriptEngine engine = null;

    public RenjinWrapper() {
        RenjinScriptEngineFactory factory = new RenjinScriptEngineFactory();
        // create a Renjin engine:
        engine = factory.getScriptEngine();
    }

    public void loadData(Instances instances, String varName) throws Exception{
        try {
            CSVSaver saver = new CSVSaver();
            File file = File.createTempFile("" + System.currentTimeMillis(), ".csv");
            try (FileOutputStream output = new FileOutputStream(file)) {
                saver.setDestination(output);
                saver.setInstances(instances);
                saver.setDir(".");
                saver.writeBatch();
            }
            String path = file.getCanonicalPath().replace("\\", "/");
            engine.eval(varName+"<-read.csv('" + path + "')");
            FileUtils.deleteQuietly(file);
        }   catch (IOException ex) {
            ex.printStackTrace();
        }
    }
    
    public Object getVar(String var) throws ScriptException{
        return engine.eval(var);
    }
    
    public Object eval(String exp) throws ScriptException{
        return engine.eval(exp);
    }
    
    public void writeData(String varName, File csvFile) throws ScriptException, IOException{
        String path = csvFile.getCanonicalPath().replace("\\", "/");
        engine.eval("write.csv("+varName+", file='"+path+"', row.names=F)");
    }
    
    public Instances writeData(String varName) throws IOException, ScriptException{
        File file=File.createTempFile("temp", ".csv");
        this.writeData(varName, file);
        CSVLoader loader=new CSVLoader();
        loader.setFile(new File("03face.csv"));
        return loader.getDataSet();
    }
}
