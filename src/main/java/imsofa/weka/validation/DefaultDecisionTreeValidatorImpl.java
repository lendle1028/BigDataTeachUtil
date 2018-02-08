/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package imsofa.weka.validation;

import java.io.File;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.io.FileUtils;
import weka.classifiers.Classifier;
import weka.core.Attribute;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.converters.CSVLoader;

/**
 *
 * @author lendle
 */
public class DefaultDecisionTreeValidatorImpl implements Validator {

    private ValidationParameter validationParameter = null;

    @Override
    public void init(ValidationParameter validationData) {
        this.validationParameter = validationData;
    }

    @Override
    public Instances validate(Instances instances) {
        Classifier classifier = (Classifier) validationParameter.getValidator();
        instances.setClassIndex((Integer)validationParameter.getParameter("classIndex"));
        try {
            File file = File.createTempFile("testModel", ".csv", new File("."));

            List<String> attributeNames = new ArrayList<>();
            for (int i = 0; i < instances.numAttributes(); i++) {
                attributeNames.add(instances.attribute(i).name());
            }
            attributeNames.add("預測結果");
            CSVFormat cSVFormat = CSVFormat.EXCEL.withHeader(attributeNames.toArray(new String[0]));

            try {
                CSVPrinter cSVPrinter = cSVFormat.print(file, Charset.forName("utf-8"));
                for (int i = 0; i < instances.size(); i++) {
                    List rowData = new ArrayList();
                    for (int j = 0; j < instances.numAttributes(); j++) {
                        Attribute attribute = instances.attribute(j);
                        if (attribute.isString() || attribute.isNominal()) {
                            rowData.add(instances.get(i).stringValue(j));
                        } else {
                            rowData.add(instances.get(i).value(j));
                        }
                    }
                    Instance instance = instances.get(i);
                    double result = classifier.classifyInstance(instance);
                    rowData.add(result);

                    cSVPrinter.printRecord(rowData.toArray());
                }
                cSVPrinter.flush();
                cSVPrinter.close();

            } catch (Exception ex) {
                ex.printStackTrace();
            }
            CSVLoader csvLoader = new CSVLoader();
            csvLoader.setFile(file);
            instances = csvLoader.getDataSet();
            //ArffSortedTableModel model = new ArffSortedTableModel(instances);
            FileUtils.deleteQuietly(file);
            file.deleteOnExit();
            return instances;
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return null;
    }

}
