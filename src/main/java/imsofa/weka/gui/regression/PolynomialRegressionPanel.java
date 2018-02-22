/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package imsofa.weka.gui.regression;

import imsofa.weka.gui.AbstractRegressionPanel;
import imsofa.weka.gui.ModelingPanelContext;
import imsofa.weka.gui.model.regression.RegressionParameter;
import imsofa.weka.gui.model.regression.RegressionParameterTableModel;
import imsofa.weka.gui.table.RegressionParameterTable;
import imsofa.weka.utils.RenjinWrapper;
import imsofa.weka.validation.DefaultRenjinRegressionValidatorImpl;
import imsofa.weka.validation.ValidationParameter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.script.ScriptException;
import javax.swing.JScrollPane;
import javax.swing.SwingUtilities;
import org.apache.commons.io.FileUtils;
import org.renjin.sexp.DoubleArrayVector;
import org.renjin.sexp.ListVector;
import weka.core.Attribute;

/**
 *
 * @author lendle
 */
public class PolynomialRegressionPanel extends AbstractRegressionPanel {

    private List<RegressionParameter> parameters = new ArrayList<>();
    private byte[] tempModelFile = null;

    public byte[] getTempModelFile() {
        return tempModelFile;
    }

    @Override
    public void setPanelContext(ModelingPanelContext panelContext) {
        super.setPanelContext(panelContext); //To change body of generated methods, choose Tools | Templates.
        for (int i = 0; i < panelContext.getInstances().numAttributes(); i++) {
            Attribute attr = panelContext.getInstances().attribute(i);
            if (attr.isNumeric()) {
                RegressionParameter parameter = new RegressionParameter();
                parameter.setFieldName(attr.name());
                parameter.setRank(1);
                parameters.add(parameter);
            }
        }

        RegressionParameterTableModel model = new RegressionParameterTableModel(parameters);
        RegressionParameterTable table = new RegressionParameterTable(model);
        JScrollPane scrollPane = new JScrollPane();
        this.panelCommonSettings.add(scrollPane);
        scrollPane.getViewport().add(table);
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                repaint();
            }
        });
    }

    @Override
    protected RegressionResult performRegression(List<RegressionParameter> parameters) {
        try {
            RegressionResult result = new RegressionResult();
            String exp = "~";
            String yVar = null;
            int numXVar = 0;
            for (RegressionParameter parameter : parameters) {
                if (parameter.isAsLeftField()) {
                    yVar = parameter.getFieldName();
                    exp = parameter.getFieldName() + exp;
                } else {
                    numXVar++;
                    if (exp.endsWith("~")) {
                        exp = exp + parameter.getFieldName();
                    } else {
                        exp = exp + "+" + parameter.getFieldName();
                    }
                    for (int i = 2; i <= parameter.getRank(); i++) {
                        numXVar++;
                        exp = exp + "+" + "I(" + parameter.getFieldName() + "^" + i + ")";
                    }
                }
            }
            System.out.println(exp);
            RenjinWrapper renjinWrapper = new RenjinWrapper();
            renjinWrapper.loadData(panelContext.getInstances(), "df");

            renjinWrapper.eval("model=lm(" + exp + ", df)");
            renjinWrapper.eval("s=summary(model)");
            renjinWrapper.eval("print(s)");
            ListVector o = (ListVector) renjinWrapper.getVar("s");
            DoubleArrayVector o1 = (DoubleArrayVector) o.get("coefficients");
            System.out.println(Arrays.toString(o1.toDoubleArray()));
            result.setIntercept(o1.get(0));
            result.setInterceptPValue(o1.get((numXVar+1) * 3));
            Map<String, Double> coefs = new HashMap<>();
            Map<String, Double> pValues = new HashMap<>();
            int index = 1;
            for (RegressionParameter parameter : parameters) {
                if (parameter.isAsRightField()) {
                    for (int i = 1; i <= parameter.getRank(); i++) {
                        String fieldName=(i==1)?parameter.getFieldName():parameter.getFieldName()+"^"+parameter.getRank();
                        coefs.put(fieldName, o1.get(index));
                        pValues.put(fieldName, o1.get((numXVar+1) * 3 + index));
                        System.out.println(fieldName+":"+o1.get((numXVar+1) * 3 + index));
                        index++;
                    }
                }
            }
            result.setCoefs(coefs);
            result.setPValues(pValues);
            result.setMse(((DoubleArrayVector) renjinWrapper.eval("mean(s$residuals^2)")).get(0));
            result.setMape(((DoubleArrayVector) renjinWrapper.eval("mean(abs(model$fitted.values-df$" + yVar + ")/df$" + yVar + ")*100")).get(0));

            File file = File.createTempFile("temp", ".model");
            String path = file.getCanonicalPath().replace("\\", "/");
            renjinWrapper.eval("save(model, file='" + path + "')");
            tempModelFile = FileUtils.readFileToByteArray(file);
//        DataSet dataSet=new DataSet();
//        double[] y = new double[panelContext.getInstances().numInstances()];
//        for (int i = 0; i < y.length; i++) {
//            y[i]=panelContext.getInstances().instance(i).value(panelContext.getInstances().attribute(this.comboboxTarget.getSelectedItem().toString()));
//            Observation observation=new Observation(y[i]);
//            for(int j=0; j<panelContext.getInstances().numAttributes(); j++){
//                Attribute attribute=panelContext.getInstances().attribute(j);
//                if(attribute.isNumeric() && attribute.name().equals(this.comboboxTarget.getSelectedItem())==false){
//                    observation.setIndependentValue(attribute.name(), panelContext.getInstances().instance(i).value(attribute));
//                }
//            }
//            dataSet.add(observation);
//        }
//        MultipleLinearRegressionModel model=new MultipleLinearRegressionModel();
//        model.init(dataSet);
//        
//        result.setCoefs(model.getCoefficients());
//        result.setIntercept(model.getIntercept());
//        result.setMape(model.getMAPE());
//        result.setMse(model.getMSE());
//        result.setPValues(model.getPValues());
            return result;
        } catch (IOException ex) {
            ex.printStackTrace();
        } catch (ScriptException ex) {
            ex.printStackTrace();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return null;
    }

    @Override
    protected List<RegressionParameter> getRegressionParameters() {
        return parameters;
    }

    @Override
    protected void saveModel(File outputFile) throws IOException {
        if (tempModelFile != null) {
            ValidationParameter<byte[]> validationParameter = new ValidationParameter<>();
            validationParameter.setValidator(tempModelFile);
            validationParameter.setValidatorClassName(DefaultRenjinRegressionValidatorImpl.class.getName());
            try (ObjectOutputStream output = new ObjectOutputStream(new FileOutputStream(outputFile))) {
                output.writeObject(validationParameter);
                output.flush();
            }
        }
    }

}
