/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package imsofa;

import java.awt.GridLayout;
import java.io.File;
import javax.swing.JFrame;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.WindowConstants;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import weka.core.Instances;
import weka.core.converters.CSVLoader;
import weka.gui.AttributeSelectionPanel;
import weka.gui.AttributeSummaryPanel;
import weka.gui.AttributeVisualizationPanel;

/**
 *
 * @author lendle
 */
public class TestViewData {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) throws Exception {
        // TODO code application logic here
        JFrame frame = new JFrame();
        frame.setSize(800, 500);
        frame.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        frame.setLayout(new GridLayout(1, 3));
        AttributeSelectionPanel m_AttPanel = new AttributeSelectionPanel();
        final AttributeSummaryPanel m_AttSummaryPanel = new AttributeSummaryPanel();
        final AttributeVisualizationPanel m_AttVisualizePanel = new AttributeVisualizationPanel();
        frame.add(m_AttPanel);
        frame.add(m_AttSummaryPanel);
        frame.add(m_AttVisualizePanel);

        CSVLoader loader = new CSVLoader();
        loader.setFile(new File("03face.csv"));
        Instances instances = loader.getDataSet();
        m_AttPanel.setInstances(instances);
        m_AttSummaryPanel.setInstances(instances);
        m_AttVisualizePanel.setInstances(instances);

        m_AttPanel.getSelectionModel().setSelectionInterval(0, 0);
        m_AttSummaryPanel.setAttribute(0);
        m_AttVisualizePanel.setAttribute(0);

        m_AttPanel.getSelectionModel().addListSelectionListener(
                new ListSelectionListener() {
            public void valueChanged(ListSelectionEvent e) {
                if (!e.getValueIsAdjusting()) {
                    ListSelectionModel lm = (ListSelectionModel) e.getSource();
                    for (int i = e.getFirstIndex(); i <= e.getLastIndex(); i++) {
                        if (lm.isSelectedIndex(i)) {
                            m_AttSummaryPanel.setAttribute(i);
                            m_AttVisualizePanel.setAttribute(i);
                            break;
                        }
                    }
                }
            }
        });
        frame.setVisible(true);
    }

}
