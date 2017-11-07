/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package imsofa.weka.gui.table;

import imsofa.weka.gui.model.AbstractClassPredictionTableModel;
import java.awt.Color;
import java.awt.Component;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableModel;

/**
 *
 * @author lendle
 */
public class ClassPredictionResultTable extends JTable {

    public ClassPredictionResultTable() {
    }

    public ClassPredictionResultTable(AbstractClassPredictionTableModel dm) {
        super(dm);
    }

    @Override
    public void updateUI() {
        if (super.getModel() instanceof AbstractClassPredictionTableModel) {
            final AbstractClassPredictionTableModel model = (AbstractClassPredictionTableModel) super.getModel();
            if (model.getColumnCount() > 1) {
                this.getColumnModel().getColumn(this.getColumnCount() - 1).setCellRenderer(new DefaultTableCellRenderer() {
                    @Override
                    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                        JLabel label = (JLabel) super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column); //To change body of generated methods, choose Tools | Templates.
                        if (column == model.getClassIndex() + 1) {
                            if (getValueAt(row, column - 1).equals(value) == false) {
                                label.setBackground(Color.RED);
                                label.setForeground(Color.WHITE);
                            } else {
                                label.setBackground(Color.GREEN);
                                label.setForeground(Color.BLACK);
                            }
                        }
                        return label;
                    }

                });
            }
        }
        super.updateUI(); //To change body of generated methods, choose Tools | Templates.
    }

}
