/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package imsofa.weka.gui.table;

import java.awt.Color;
import java.awt.Component;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.SwingUtilities;
import weka.gui.arffviewer.ArffTableCellRenderer;

/**
 *
 * @author lendle
 */
public class ArffTableCellRendererEx extends ArffTableCellRenderer{
    private int lastSelectedColumn=0;
    public ArffTableCellRendererEx() {
    }

    public ArffTableCellRendererEx(Color missingColor, Color missingColorSelected) {
        super(missingColor, missingColorSelected);
    }

    public ArffTableCellRendererEx(Color missingColor, Color missingColorSelected, Color highlightColor, Color highlightColorSelected) {
        super(missingColor, missingColorSelected, highlightColor, highlightColorSelected);
    }

    @Override
    public Component getTableCellRendererComponent(final JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
        JLabel label=(JLabel) super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column); //To change body of generated methods, choose Tools | Templates.
        int [] selectedColumns=table.getColumnModel().getSelectedColumns();
        label.setForeground(Color.BLACK);
        label.setBackground(Color.WHITE);
        for(int columnIndex : selectedColumns){
            if(columnIndex==column){
                label.setBackground(Color.yellow);
                break;
            }
        }
        if(table.getSelectedColumn()!=lastSelectedColumn){
            lastSelectedColumn=column;
            SwingUtilities.invokeLater(new Runnable(){
                @Override
                public void run() {
                    table.repaint();
                }
            });
        }
        return label;
    }
    
    
}
