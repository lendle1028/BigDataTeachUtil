/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package imsofa.weka.gui;

import imsofa.weka.factory.InstanceDataFactory;
import imsofa.weka.gui.model.ClassifyResultTableModel;
import imsofa.weka.gui.model.ClusterResultTableModel;
import imsofa.weka.gui.table.ClassPredictionResultTable;
import imsofa.weka.model.InstanceData;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Enumeration;
import java.util.List;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JFrame;
import javax.swing.SwingUtilities;
import javax.swing.WindowConstants;
import weka.classifiers.AbstractClassifier;
import weka.classifiers.Classifier;
import weka.classifiers.CostMatrix;
import weka.classifiers.Evaluation;
import weka.classifiers.evaluation.output.prediction.AbstractOutput;
import weka.classifiers.trees.J48;
import weka.core.Attribute;
import weka.core.BatchPredictor;
import weka.core.Drawable;
import weka.core.Instances;
import weka.core.converters.ConverterUtils;
import weka.filters.Filter;
import weka.filters.unsupervised.attribute.Remove;
import weka.gui.explorer.ClassifierErrorsPlotInstances;
import static weka.gui.explorer.ClassifierPanel.setupEval;
import weka.gui.explorer.ExplorerDefaults;
import weka.gui.treevisualizer.PlaceNode2;
import weka.gui.treevisualizer.TreeVisualizer;

/**
 *
 * @author lendle
 */
public abstract class AbstractDecisionTreePanel extends AbstractModelingPanel {

    protected Classifier classifier = new J48();

    public void setPanelContext(ModelingPanelContext panelContext) {
        super.setPanelContext(panelContext);
        Enumeration<Attribute> attributes = panelContext.getInstances().enumerateAttributes();
        this.comboboxClassAttribute.removeAllItems();
        while (attributes.hasMoreElements()) {
            Attribute attribute = attributes.nextElement();
            if (attribute.isNominal()) {
                this.comboboxClassAttribute.addItem(attribute.name());
            }
        }
    }

    @Override
    protected void saveModel(File outputFile) throws IOException{
        try(ObjectOutputStream output=new ObjectOutputStream(new FileOutputStream(outputFile))){
            output.writeObject(this.classifier);
            output.flush();
        }
    }
   
    
    /**
     * Creates new form KmeansClusterPanel
     */
    public AbstractDecisionTreePanel() {
        try {
            initComponents();
            tableResults.setModel(new ClusterResultTableModel());

            buttonStart.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    try {
                        startButtonActionPerformed();
                    } catch (Exception ex) {
                        Logger.getLogger(AbstractDecisionTreePanel.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
            });
        } catch (Exception ex) {
            Logger.getLogger(AbstractDecisionTreePanel.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

    protected abstract void setupClassifier();

    //protected abstract Instances prepareInstances();
    //protected abstract Instances prepareTrainingInstances(Instances inst);
    protected void startButtonActionPerformed() throws Exception {
        panelPlot.removeAll();
        setupClassifier();
        final int classIndex = panelContext.getInstances().attribute(comboboxClassAttribute.getSelectedItem().toString()).index();

        Thread m_RunThread = new Thread() {
            @Override
            public void run() {
                CostMatrix costMatrix = null;
                Instances inst = new Instances(panelContext.getInstances());
                ConverterUtils.DataSource source = null;
                ClassifierErrorsPlotInstances plotInstances = null;

                String grph = null;

                int testMode = 0;
                int numFolds = 10;
                inst.setClassIndex(classIndex);

                Classifier template = null;
                try {
                    template = AbstractClassifier.makeCopy(classifier);
                } catch (Exception ex) {
                    //m_Log.logMessage("Problem copying classifier: " + ex.getMessage());
                    ex.printStackTrace();
                }
                Classifier fullClassifier = null;
                StringBuffer outBuff = new StringBuffer();
                AbstractOutput classificationOutput = null;

                String name
                        = (new SimpleDateFormat("HH:mm:ss - ")).format(new Date());
                String cname = "";
                Evaluation eval = null;
                try {
                    testMode = 1;
                    numFolds = 10;

                    // set up the structure of the plottable instances for
                    // visualization
                    plotInstances = ExplorerDefaults.getClassifierErrorsPlotInstances();
                    plotInstances.setInstances(inst);
                    plotInstances.setClassifier(classifier);
                    plotInstances.setClassIndex(classIndex);
                    plotInstances.setSaveForVisualization(true);
                    plotInstances.setPointSizeProportionalToMargin(true);
                    classifier.buildClassifier(inst);
                    List<String> m_selectedEvalMetrics = Evaluation.getAllEvaluationMetricNames();

                    switch (testMode) {
                        case 1: // CV mode
                            int rnd = 1;
                            Random random = new Random(rnd);
                            inst.randomize(random);
                            if (inst.attribute(classIndex).isNominal()) {
                                inst.stratify(numFolds);
                            }
                            eval = new Evaluation(inst, costMatrix);

                            // make adjustments if the classifier is an InputMappedClassifier
                            eval
                                    = setupEval(eval, classifier, inst, costMatrix, plotInstances,
                                            classificationOutput, false);
                            eval.setMetricsToDisplay(m_selectedEvalMetrics);

                            // plotInstances.setEvaluation(eval);
                            plotInstances.setUp();

                            // Make some splits and do a CV
                            for (int fold = 0; fold < numFolds; fold++) {
                                Instances train = inst.trainCV(numFolds, fold, random);

                                // make adjustments if the classifier is an
                                // InputMappedClassifier
                                eval
                                        = setupEval(eval, classifier, train, costMatrix, plotInstances,
                                                classificationOutput, true);
                                eval.setMetricsToDisplay(m_selectedEvalMetrics);

                                Classifier current = null;
                                try {
                                    current = AbstractClassifier.makeCopy(template);
                                } catch (Exception ex) {
                                }
                                current.buildClassifier(train);

                                grph = ((Drawable) classifier).graph();
                                Instances test = inst.testCV(numFolds, fold);

                                if (classifier instanceof BatchPredictor
                                        && ((BatchPredictor) classifier)
                                                .implementsMoreEfficientBatchPrediction()) {
                                    Instances toPred = new Instances(test);
                                    for (int i = 0; i < toPred.numInstances(); i++) {
                                        toPred.instance(i).setClassMissing();
                                    }
                                    double[][] predictions
                                            = ((BatchPredictor) current)
                                                    .distributionsForInstances(toPred);
                                    plotInstances.process(test, predictions, eval);
                                } else {
                                    for (int jj = 0; jj < test.numInstances(); jj++) {
                                        plotInstances.process(test.instance(jj), current, eval);
                                    }
                                }
                            }
                            if (inst.attribute(classIndex).isNominal()) {
                                outBuff.append("=== Stratified cross-validation ===\n");
                            } else {
                                outBuff.append("=== Cross-validation ===\n");
                            }
                            break;

                        default:
                            throw new Exception("Test mode not implemented");
                    }

                    try {

                    } catch (Exception ex) {
                        ex.printStackTrace();

                    } finally {
                        try {

                            boolean saveVis = true;
                            boolean outputModel = false;
                            //VisualizePanel m_CurrentVis = null;
                            if (!saveVis && outputModel) {
                                ArrayList<Object> vv = new ArrayList<Object>();
                                vv.add(fullClassifier);
                                Instances trainHeader = new Instances(panelContext.getInstances(), 0);
                                trainHeader.setClassIndex(classIndex);
                                vv.add(trainHeader);
                                if (grph != null) {
                                    vv.add(grph);
                                }
                            } else if (saveVis && plotInstances != null
                                    && plotInstances.canPlot(false)) {
                                /*m_CurrentVis = new VisualizePanel();
                                m_CurrentVis.setName("test");
                                m_CurrentVis.addPlot(plotInstances.getPlotData("test"));
                                // m_CurrentVis.setColourIndex(plotInstances.getPlotInstances().classIndex()+1);
                                m_CurrentVis.setColourIndex(plotInstances.getPlotInstances()
                                        .classIndex());

                                ArrayList<Object> vv = new ArrayList<Object>();
                                vv.add(m_CurrentVis);

                                if ((eval != null) && (eval.predictions() != null)) {
                                    vv.add(eval.predictions());
                                    vv.add(inst.classAttribute());
                                }*/

                                final String _grph = grph;
                                ClassifyResultTableModel model = new ClassifyResultTableModel();
                                model.setInstances(inst);
                                model.setEvaluation(eval);
                                model.setClassIndex(classIndex);
                                tableResults.setModel(model);
                                
                                textResult.setValue((eval.correct()/(double)inst.numInstances()));
                                
                                SwingUtilities.invokeLater(new Runnable() {
                                    @Override
                                    public void run() {
                                        TreeVisualizer tv = new TreeVisualizer(null, _grph, new PlaceNode2());
                                        tv.setM_viewPos(new Dimension(50, 10));
                                        panelPlot.setPreferredSize(tv.getM_viewSize());

                                        panelPlot.add(tv);
                                        tv.setLocation(100, 10);
                                        tableResults.updateUI();
                                        revalidate();
                                    }
                                });
                                //System.out.println(Arrays.toString(eval.evaluateModel(classifier, inst, new Object[0] )));
                                /*ArrayList<Prediction> predictions = eval.predictions();
                                for (Prediction prediction : predictions) {
                                    System.out.println(prediction.actual() + ":" + prediction.predicted() + ":" + inst.classAttribute().value((int) prediction.predicted()));
                                }*/

                                
                                //tv.fitToScreen();

                                /*final javax.swing.JFrame jf
                                        = new javax.swing.JFrame("Weka Classifier Tree Visualizer: ");
                                jf.setSize(500, 400);
                                jf.getContentPane().setLayout(new BorderLayout());
                                
                                TreeVisualizer tv = new TreeVisualizer(null, grph, new PlaceNode2());
                                jf.getContentPane().add(tv, BorderLayout.CENTER);
                                jf.addWindowListener(new java.awt.event.WindowAdapter() {
                                    @Override
                                    public void windowClosing(java.awt.event.WindowEvent e) {
                                        jf.dispose();
                                    }
                                });

                                jf.setVisible(true);
                                tv.fitToScreen();*/
                                //panelPlot.add(m_CurrentVis);
                                plotInstances.cleanUp();

                            }
                        } catch (Exception ex) {
                            ex.printStackTrace();
                        }

                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        };
        m_RunThread.setPriority(Thread.MIN_PRIORITY);
        m_RunThread.start();
    }

    //protected abstract ClusterEvaluation buildClusterEvaluation(Instances trainInst, Map options) throws Exception;
    //protected abstract Map createOptions();
    protected Instances removeClass(Instances inst) {
        Remove af = new Remove();
        Instances retI = null;

        try {
            if (inst.classIndex() < 0) {
                retI = inst;
            } else {
                af.setAttributeIndices("" + (inst.classIndex() + 1));
                af.setInvertSelection(false);
                af.setInputFormat(inst);
                retI = Filter.useFilter(inst, af);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return retI;
    }

    public static void main(String[] args) throws Exception {
        JFrame frame = new JFrame();
        frame.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        frame.setSize(800, 600);

        List<InstanceData> list = InstanceDataFactory.newInstance().loadInstanceData(null);
        /*AbstractDecisionTreePanel panel = new AbstractDecisionTreePanel() {
            @Override
            protected void setupClassifier() {
                
            }
        };

        //panel.setInstances(list.get(1).getInstances());
        frame.add(panel);*/

        frame.setVisible(true);
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jSplitPane1 = new javax.swing.JSplitPane();
        panelLeft = new javax.swing.JPanel();
        panelSettings = new javax.swing.JPanel();
        panelCommonSettings = new javax.swing.JPanel();
        comboboxClassAttribute = new javax.swing.JComboBox<>();
        jLabel1 = new javax.swing.JLabel();
        panelSpecificSettings = new javax.swing.JPanel();
        panelResult = new javax.swing.JPanel();
        jLabel2 = new javax.swing.JLabel();
        textResult = new javax.swing.JFormattedTextField();
        panelActions = new javax.swing.JPanel();
        buttonStart = new javax.swing.JButton();
        jSplitPane2 = new javax.swing.JSplitPane();
        jScrollPane1 = new javax.swing.JScrollPane();
        panelPlot = new javax.swing.JPanel();
        scrollpane = new javax.swing.JScrollPane();
        tableResults = new ClassPredictionResultTable();

        setLayout(new java.awt.BorderLayout());

        jSplitPane1.setDividerLocation(300);

        panelLeft.setLayout(new java.awt.BorderLayout());

        panelSettings.setLayout(new java.awt.BorderLayout());

        panelCommonSettings.setBorder(javax.swing.BorderFactory.createTitledBorder("一般設定"));
        panelCommonSettings.setPreferredSize(new java.awt.Dimension(299, 100));

        jLabel1.setText("群組欄位：");

        javax.swing.GroupLayout panelCommonSettingsLayout = new javax.swing.GroupLayout(panelCommonSettings);
        panelCommonSettings.setLayout(panelCommonSettingsLayout);
        panelCommonSettingsLayout.setHorizontalGroup(
            panelCommonSettingsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, panelCommonSettingsLayout.createSequentialGroup()
                .addComponent(jLabel1)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(comboboxClassAttribute, 0, 207, Short.MAX_VALUE))
        );
        panelCommonSettingsLayout.setVerticalGroup(
            panelCommonSettingsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelCommonSettingsLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(panelCommonSettingsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(comboboxClassAttribute, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel1))
                .addContainerGap(42, Short.MAX_VALUE))
        );

        panelSettings.add(panelCommonSettings, java.awt.BorderLayout.NORTH);

        panelSpecificSettings.setLayout(new java.awt.BorderLayout());
        panelSettings.add(panelSpecificSettings, java.awt.BorderLayout.CENTER);

        panelResult.setBorder(javax.swing.BorderFactory.createTitledBorder("結果"));
        panelResult.setLayout(new java.awt.BorderLayout());

        jLabel2.setText("正確率：");
        panelResult.add(jLabel2, java.awt.BorderLayout.WEST);

        textResult.setEditable(false);
        textResult.setFormatterFactory(new javax.swing.text.DefaultFormatterFactory(new javax.swing.text.NumberFormatter(java.text.NumberFormat.getPercentInstance())));
        textResult.setMargin(new java.awt.Insets(0, 0, 0, 100));
        panelResult.add(textResult, java.awt.BorderLayout.CENTER);

        panelSettings.add(panelResult, java.awt.BorderLayout.SOUTH);

        panelLeft.add(panelSettings, java.awt.BorderLayout.CENTER);

        panelActions.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT));

        buttonStart.setText("執行");
        panelActions.add(buttonStart);

        panelLeft.add(panelActions, java.awt.BorderLayout.SOUTH);

        jSplitPane1.setLeftComponent(panelLeft);

        jSplitPane2.setDividerLocation(150);
        jSplitPane2.setOrientation(javax.swing.JSplitPane.VERTICAL_SPLIT);

        panelPlot.setLayout(new java.awt.BorderLayout());
        jScrollPane1.setViewportView(panelPlot);

        jSplitPane2.setBottomComponent(jScrollPane1);

        tableResults.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null}
            },
            new String [] {
                "Title 1", "Title 2", "Title 3", "Title 4"
            }
        ));
        scrollpane.setViewportView(tableResults);

        jSplitPane2.setLeftComponent(scrollpane);

        jSplitPane1.setRightComponent(jSplitPane2);

        add(jSplitPane1, java.awt.BorderLayout.CENTER);
    }// </editor-fold>//GEN-END:initComponents


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton buttonStart;
    protected javax.swing.JComboBox<String> comboboxClassAttribute;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JSplitPane jSplitPane1;
    private javax.swing.JSplitPane jSplitPane2;
    protected javax.swing.JPanel panelActions;
    private javax.swing.JPanel panelCommonSettings;
    private javax.swing.JPanel panelLeft;
    private javax.swing.JPanel panelPlot;
    private javax.swing.JPanel panelResult;
    protected javax.swing.JPanel panelSettings;
    protected javax.swing.JPanel panelSpecificSettings;
    private javax.swing.JScrollPane scrollpane;
    private javax.swing.JTable tableResults;
    private javax.swing.JFormattedTextField textResult;
    // End of variables declaration//GEN-END:variables
}
