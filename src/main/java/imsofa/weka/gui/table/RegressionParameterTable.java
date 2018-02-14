/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package imsofa.weka.gui.table;

import imsofa.weka.gui.model.regression.RegressionParameterTableModel;
import javax.swing.DefaultCellEditor;
import javax.swing.JCheckBox;
import javax.swing.JTable;

/**
 *
 * @author lendle
 */
public class RegressionParameterTable extends JTable{
    private RegressionParameterTableModel regressionParameterTableModel=null;
    private JCheckBox asLeftFieldCheckBox=new JCheckBox();
    private JCheckBox asRightFieldCheckBox=new JCheckBox();

    public RegressionParameterTable(RegressionParameterTableModel dm) {
        super(dm);
        this.regressionParameterTableModel=dm;
        this.getColumnModel().getColumn(2).setCellEditor(new DefaultCellEditor(asLeftFieldCheckBox));
        this.getColumnModel().getColumn(3).setCellEditor(new DefaultCellEditor(asRightFieldCheckBox));
    }
    
}
