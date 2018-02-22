/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package imsofa.weka.gui.dialogs;

import imsofa.weka.gui.ModelingPanelContext;

/**
 *
 * @author lendle
 */
public class ModelingDialog extends javax.swing.JDialog {

    /**
     * Creates new form ModelingDialog
     */
    public ModelingDialog(java.awt.Frame parent, ModelingPanelContext panelContext, boolean modal) {
        super(parent, modal);
        initComponents();
        this.panelLinearRegression.setPanelContext(panelContext);
        this.panelPolynomialRegression.setPanelContext(panelContext);
        this.panelJ48DecisionTree.setPanelContext(panelContext);
        this.panelKmeansCluster.setPanelContext(panelContext);
        this.panelRandomTreeDecisionTree.setPanelContext(panelContext);
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jTabbedPane1 = new javax.swing.JTabbedPane();
        panelRegression = new javax.swing.JTabbedPane();
        panelLinearRegression = new imsofa.weka.gui.regression.LinearRegressionPanel();
        panelPolynomialRegression = new imsofa.weka.gui.regression.PolynomialRegressionPanel();
        panelDecisionTree = new javax.swing.JTabbedPane();
        panelJ48DecisionTree = new imsofa.weka.gui.decisiontree.J48DecisionTreePanel();
        panelRandomTreeDecisionTree = new imsofa.weka.gui.decisiontree.RandomTreeDecisionTreePanel();
        panelClustersOriginal = new javax.swing.JTabbedPane();
        panelKmeansCluster = new imsofa.weka.gui.clusterer.KmeansClusterPanel();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);

        panelRegression.addTab("線性迴歸", panelLinearRegression);
        panelRegression.addTab("多項式迴歸", panelPolynomialRegression);

        jTabbedPane1.addTab("迴歸", panelRegression);

        panelDecisionTree.addTab("J48", panelJ48DecisionTree);
        panelDecisionTree.addTab("Random Tree", panelRandomTreeDecisionTree);

        jTabbedPane1.addTab("決策樹", panelDecisionTree);

        panelClustersOriginal.addTab("K-means", panelKmeansCluster);

        jTabbedPane1.addTab("分群", panelClustersOriginal);

        getContentPane().add(jTabbedPane1, java.awt.BorderLayout.CENTER);

        pack();
    }// </editor-fold>//GEN-END:initComponents

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        /* Set the Nimbus look and feel */
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
         * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html 
         */
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ClassNotFoundException ex) {
            java.util.logging.Logger.getLogger(ModelingDialog.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(ModelingDialog.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(ModelingDialog.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(ModelingDialog.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        /* Create and display the dialog */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                ModelingDialog dialog = new ModelingDialog(new javax.swing.JFrame(), null, true);
                dialog.addWindowListener(new java.awt.event.WindowAdapter() {
                    @Override
                    public void windowClosing(java.awt.event.WindowEvent e) {
                        System.exit(0);
                    }
                });
                dialog.setVisible(true);
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JTabbedPane jTabbedPane1;
    private javax.swing.JTabbedPane panelClustersOriginal;
    private javax.swing.JTabbedPane panelDecisionTree;
    private imsofa.weka.gui.decisiontree.J48DecisionTreePanel panelJ48DecisionTree;
    private imsofa.weka.gui.clusterer.KmeansClusterPanel panelKmeansCluster;
    private imsofa.weka.gui.regression.LinearRegressionPanel panelLinearRegression;
    private imsofa.weka.gui.regression.PolynomialRegressionPanel panelPolynomialRegression;
    private imsofa.weka.gui.decisiontree.RandomTreeDecisionTreePanel panelRandomTreeDecisionTree;
    private javax.swing.JTabbedPane panelRegression;
    // End of variables declaration//GEN-END:variables
}
