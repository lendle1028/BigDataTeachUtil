/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package imsofa.weka.gui.wizard;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import javax.swing.ImageIcon;
import javax.swing.JEditorPane;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JWindow;
import javax.swing.text.html.HTMLEditorKit;

/**
 *
 * @author lendle
 */
public class WizardWindow extends JWindow {

    protected final ImageIcon wizardIcon = new ImageIcon(this.getClass().getClassLoader().getResource("imsofa/weka/gui/wizard/merlin.png"));
    protected final JLabel wizardIconLabel;
    protected final JPanel wizardPanel = new JPanel();
    protected final JEditorPane editorPane = new JEditorPane();
    protected final JLabel wizardText = new JLabel();
    protected final JScrollPane scrollPane = new JScrollPane(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);

    public WizardWindow() {
        this.setLayout(new BorderLayout());
        //FlowLayout flowLayout=new FlowLayout(FlowLayout.LEFT);
        wizardPanel.setLayout(new BorderLayout());
        wizardIconLabel = new JLabel(wizardIcon);
        wizardPanel.add(wizardIconLabel);
        wizardPanel.add(wizardText, "East");
        wizardText.setPreferredSize(new Dimension(150, 0));
        wizardText.setText("<html>Don't worry,<br> be <b>happy!</b></html>");
        this.add(wizardPanel, "South");

        this.add(scrollPane);
        editorPane.setEditable(false);
        editorPane.setEditorKit(new HTMLEditorKit());
        editorPane.setText("<b>test11111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111</b>");
        scrollPane.getViewport().add(editorPane);

        javax.swing.SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                scrollPane.getHorizontalScrollBar().setValue(0);
            }
        });
    }

    public void setInstruction(String instruction) {
        this.editorPane.setText(instruction);
        javax.swing.SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                scrollPane.getHorizontalScrollBar().setValue(0);
            }
        });
    }

    public void setWizardText(String text) {
        this.wizardText.setText(text);
    }
}
