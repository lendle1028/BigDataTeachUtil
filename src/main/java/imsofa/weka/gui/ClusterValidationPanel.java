/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package imsofa.weka.gui;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.ObjectInputStream;
import java.io.Reader;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.csv.CSVRecord;
import weka.clusterers.Clusterer;
import weka.core.Attribute;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.converters.CSVLoader;
import weka.core.converters.CSVSaver;
import weka.gui.arffviewer.ArffSortedTableModel;

/**
 *
 * @author lendle
 */
public class ClusterValidationPanel extends javax.swing.JPanel {

    /**
     * Creates new form ClusterValidationPanel
     */
    public ClusterValidationPanel() {
        initComponents();
    }

    public void verifyInstances(Instances instances) {
        try {
            File file = File.createTempFile("testModel", ".csv", new File("."));

            List<String> attributeNames = new ArrayList<>();
            for (int i = 0; i < instances.numAttributes(); i++) {
                attributeNames.add(instances.attribute(i).name());
            }
            attributeNames.add("預測結果");
            CSVFormat cSVFormat = CSVFormat.EXCEL.withHeader(attributeNames.toArray(new String[0]));

            try (ObjectInputStream input = new ObjectInputStream(new FileInputStream(new File("/home/lendle/dev/projects/NSC106/bigdatateachutil/lectures/測試/iris/test.model")))) {
                int index = 0;
                Clusterer clusterer = (Clusterer) input.readObject();
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
                    Instance instance = instances.get(index);
                    int result = clusterer.clusterInstance(instance);
                    rowData.add(result);

                    cSVPrinter.printRecord(rowData.toArray());
                }
                cSVPrinter.flush();
                cSVPrinter.close();
                
            } catch (Exception ex) {
                ex.printStackTrace();
            }
            CSVLoader csvLoader=new CSVLoader();
            csvLoader.setFile(file);
            instances=csvLoader.getDataSet();
            ArffSortedTableModel model = new ArffSortedTableModel(instances);
            this.dataTable.setModel(model);
            file.deleteOnExit();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jScrollPane1 = new javax.swing.JScrollPane();
        dataTable = new weka.gui.arffviewer.ArffTable();

        jScrollPane1.setViewportView(dataTable);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 400, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 300, Short.MAX_VALUE)
        );
    }// </editor-fold>//GEN-END:initComponents


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private weka.gui.arffviewer.ArffTable dataTable;
    private javax.swing.JScrollPane jScrollPane1;
    // End of variables declaration//GEN-END:variables
}