/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package imsofa.weka.gui;

import imsofa.weka.gui.dialogs.ModelingDialog;
import imsofa.weka.Global;
import imsofa.weka.factory.InstanceDataFactory;
import imsofa.weka.gui.dialogs.ValidationDialog;
import imsofa.weka.gui.dialogs.ViewDataDialog;
import imsofa.weka.gui.model.InstanceDataTableModel;
import imsofa.weka.model.lecture.Lecture;
import imsofa.weka.gui.wizard.WizardWindow;
import imsofa.weka.model.InstanceData;
import java.util.List;
import weka.core.Instances;

/**
 *
 * @author lendle
 */
public class Main extends javax.swing.JFrame {
    private Lecture currentLecture=null;
    /**
     * Creates new form Main
     */
    public Main(Lecture currentLecture) {
        this.currentLecture=currentLecture;
        initComponents();
        this.setSize(1000, 700);
        List<InstanceData> list=InstanceDataFactory.newInstance().loadInstanceData(currentLecture);
        InstanceDataTableModel instanceDataTableModel=new InstanceDataTableModel();
        instanceDataTableModel.setInstanceDataList(list);
        this.tableData.setModel(instanceDataTableModel);
        WizardWindow wizardWindow=new WizardWindow();
        wizardWindow.setAlwaysOnTop(true);
        wizardWindow.setSize(300, 700);
        wizardWindow.setLocationRelativeTo(this);
        wizardWindow.setLocation(this.getSize().width+50, 0);
        //wizardWindow.setVisible(true);
        this.setLocationRelativeTo(null);
        
        Global.wizard=wizardWindow;
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jPanel1 = new javax.swing.JPanel();
        buttonLoad = new javax.swing.JButton();
        buttonView = new javax.swing.JButton();
        buttonClean = new javax.swing.JButton();
        buttonTransform = new javax.swing.JButton();
        buttonModeling = new javax.swing.JButton();
        buttonVerify = new javax.swing.JButton();
        jScrollPane1 = new javax.swing.JScrollPane();
        tableData = new javax.swing.JTable();
        jMenuBar1 = new javax.swing.JMenuBar();
        jMenu1 = new javax.swing.JMenu();
        jMenu2 = new javax.swing.JMenu();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);

        jPanel1.setLayout(new java.awt.GridLayout(6, 1));

        buttonLoad.setText("載入資料");
        jPanel1.add(buttonLoad);

        buttonView.setText("檢視資料");
        buttonView.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                buttonViewActionPerformed(evt);
            }
        });
        jPanel1.add(buttonView);

        buttonClean.setText("清理資料");
        jPanel1.add(buttonClean);

        buttonTransform.setText("轉換資料");
        jPanel1.add(buttonTransform);

        buttonModeling.setText("建立模型");
        buttonModeling.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                buttonModelingActionPerformed(evt);
            }
        });
        jPanel1.add(buttonModeling);

        buttonVerify.setText("檢驗模型");
        buttonVerify.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                buttonVerifyActionPerformed(evt);
            }
        });
        jPanel1.add(buttonVerify);

        tableData.setModel(new javax.swing.table.DefaultTableModel(
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
        jScrollPane1.setViewportView(tableData);

        jMenu1.setText("File");
        jMenuBar1.add(jMenu1);

        jMenu2.setText("Edit");
        jMenuBar1.add(jMenu2);

        setJMenuBar(jMenuBar1);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 465, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, 102, javax.swing.GroupLayout.PREFERRED_SIZE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 368, Short.MAX_VALUE)
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void buttonViewActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_buttonViewActionPerformed
        // TODO add your handling code here:
        int rowIndex=this.tableData.getSelectedRow();
        if(rowIndex!=-1){
            InstanceDataTableModel model=(InstanceDataTableModel) this.tableData.getModel();
            Instances instances=model.getInstanceDataList().get(rowIndex).getInstances();
            ViewDataDialog dlg=new ViewDataDialog(this, instances, false);
            dlg.setSize(1000, 700);
            dlg.setLocationRelativeTo(this);
            dlg.setVisible(true);
        }
    }//GEN-LAST:event_buttonViewActionPerformed

    private void buttonModelingActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_buttonModelingActionPerformed
        // TODO add your handling code here:
        int rowIndex=this.tableData.getSelectedRow();
        if(rowIndex!=-1){
            InstanceDataTableModel model=(InstanceDataTableModel) this.tableData.getModel();
            Instances instances=model.getInstanceDataList().get(rowIndex).getInstances();
            ModelingPanelContext modelingPanelContext=new ModelingPanelContext();
            modelingPanelContext.setInstances(instances);
            modelingPanelContext.setLecture(currentLecture);
            ModelingDialog dlg=new ModelingDialog(this, modelingPanelContext, false);
            dlg.setLocationRelativeTo(this);
            dlg.setSize(1000, 700);
            dlg.setVisible(true);
        }
    }//GEN-LAST:event_buttonModelingActionPerformed

    private void buttonVerifyActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_buttonVerifyActionPerformed
        // TODO add your handling code here:
        int rowIndex=this.tableData.getSelectedRow();
        if(rowIndex!=-1){
            InstanceDataTableModel model=(InstanceDataTableModel) this.tableData.getModel();
            Instances instances=model.getInstanceDataList().get(rowIndex).getInstances();
            ModelingPanelContext modelingPanelContext=new ModelingPanelContext();
            modelingPanelContext.setInstances(instances);
            modelingPanelContext.setLecture(currentLecture);
            ValidationDialog dlg=new ValidationDialog(this, true, modelingPanelContext);
            dlg.setLocationRelativeTo(this);
            dlg.setSize(1000, 700);
            dlg.setVisible(true);
        }
    }//GEN-LAST:event_buttonVerifyActionPerformed

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
            java.util.logging.Logger.getLogger(Main.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(Main.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(Main.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(Main.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new Main(null).setVisible(true);
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton buttonClean;
    private javax.swing.JButton buttonLoad;
    private javax.swing.JButton buttonModeling;
    private javax.swing.JButton buttonTransform;
    private javax.swing.JButton buttonVerify;
    private javax.swing.JButton buttonView;
    private javax.swing.JMenu jMenu1;
    private javax.swing.JMenu jMenu2;
    private javax.swing.JMenuBar jMenuBar1;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JTable tableData;
    // End of variables declaration//GEN-END:variables
}
