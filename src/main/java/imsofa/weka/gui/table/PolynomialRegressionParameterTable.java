/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package imsofa.weka.gui.table;

import imsofa.weka.gui.model.regression.PolynomialRegressionParameterTableModel;
import javax.swing.JTable;

/**
 *
 * @author lendle
 */
public class PolynomialRegressionParameterTable extends JTable{

    public PolynomialRegressionParameterTable() {
    }

    public PolynomialRegressionParameterTable(PolynomialRegressionParameterTableModel dm) {
        super(dm);
    }

    
}
