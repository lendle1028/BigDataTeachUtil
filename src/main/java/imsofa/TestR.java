/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package imsofa;

import imsofa.weka.utils.RenjinWrapper;
import java.io.File;
import java.io.FileOutputStream;
import javax.script.ScriptEngine;
import org.renjin.script.RenjinScriptEngineFactory;
import org.renjin.sexp.DoubleArrayVector;
import org.renjin.sexp.ListVector;
import weka.core.Instances;
import weka.core.converters.CSVLoader;
import weka.core.converters.CSVSaver;

/**
 *
 * @author lendle
 */
public class TestR {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) throws Exception {
        CSVLoader loader = new CSVLoader();
        loader.setFile(new File("iris.csv"));
        Instances instances = loader.getDataSet();
        
        RenjinWrapper renjinWrapper=new RenjinWrapper();
        renjinWrapper.loadData(instances, "df");
        
        renjinWrapper.eval("print(df)");
        renjinWrapper.eval("print(summary(lm(Petal.Width ~ Sepal.Length+Sepal.Width+Petal.Length, df)))");
        renjinWrapper.eval("s=summary(lm(Petal.Width ~ Sepal.Length+Sepal.Width+Petal.Length, df))");
        renjinWrapper.eval("model=lm(Petal.Width ~ Sepal.Length+Sepal.Width+Petal.Length, df)");
        renjinWrapper.eval("save(model, file='test.RData')");
        
        ListVector o = (ListVector) renjinWrapper.getVar("s");
        System.out.println(o);
        DoubleArrayVector o1 = (DoubleArrayVector) o.get("coefficients");
        System.out.println(o1.get(12)+", "+o1.get(13)+", "+o1.get(14)+", "+o1.get(15));
        System.out.println(o1.get(0)+", "+o1.get(1)+", "+o1.get(2)+", "+o1.get(3));
        org.renjin.sexp.DoubleArrayVector o2=(org.renjin.sexp.DoubleArrayVector) o.get("residuals");
        System.out.println(((DoubleArrayVector)renjinWrapper.eval("mean(s$residuals^2)")).get(0));
        System.out.println(renjinWrapper.eval("mean(abs(s$residuals)/df$Petal.Width)"));
        
        System.out.println(renjinWrapper.writeData("df"));
        
//        CSVSaver saver = new CSVSaver();
//        //saver.setDestination(new File("/home/lendle/dev/projects/NSC106/bigdatateachutil/test.csv"));
//        File file=File.createTempFile(""+System.currentTimeMillis(), ".csv");
//        try (FileOutputStream output = new FileOutputStream(file)) {
//            saver.setDestination(output);
//            saver.setInstances(instances);
//            saver.setDir(".");
//            saver.writeBatch();
//        }
//
//        // TODO code application logic here
//        RenjinScriptEngineFactory factory = new RenjinScriptEngineFactory();
//        // create a Renjin engine:
//        ScriptEngine engine = factory.getScriptEngine();
//        String path=file.getCanonicalPath().replace("\\", "/");
//        engine.eval("df <- read.csv('"+path+"')");
//        engine.eval("print(df)");
//        engine.eval("print(summary(lm(Petal.Width ~ Sepal.Length+Sepal.Width+Petal.Length, df)))");
//        engine.eval("s=summary(lm(Petal.Width ~ Sepal.Length+Sepal.Width+Petal.Length, df))");
//        engine.eval("model=lm(Petal.Width ~ Sepal.Length+Sepal.Width+Petal.Length, df)");
//        engine.eval("save(model, file='test.RData')");
//        ListVector o = (ListVector) engine.eval("s");
//        System.out.println(o);
//        DoubleArrayVector o1 = (DoubleArrayVector) o.get("coefficients");
//        System.out.println(o1.get(12)+", "+o1.get(13)+", "+o1.get(14)+", "+o1.get(15));
//        System.out.println(o1.get(0)+", "+o1.get(1)+", "+o1.get(2)+", "+o1.get(3));
//        org.renjin.sexp.DoubleArrayVector o2=(org.renjin.sexp.DoubleArrayVector) o.get("residuals");
//        System.out.println(((DoubleArrayVector)engine.eval("mean(s$residuals^2)")).get(0));
//        System.out.println(engine.eval("mean(abs(s$residuals)/df$Petal.Width)"));
//        file.deleteOnExit();
    }

}
