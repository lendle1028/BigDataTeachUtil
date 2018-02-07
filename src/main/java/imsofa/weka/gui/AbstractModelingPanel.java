/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package imsofa.weka.gui;

import java.io.File;
import java.io.IOException;
import javax.swing.JFileChooser;
import javax.swing.JPanel;
import javax.swing.filechooser.FileFilter;
import javax.swing.filechooser.FileNameExtensionFilter;

/**
 *
 * @author lendle
 */
public abstract class AbstractModelingPanel extends JPanel {

    protected ModelingPanelContext panelContext = null;
    protected JFileChooser saveFileChooser = new JFileChooser();

    public AbstractModelingPanel() {
        for (FileFilter filter : this.getSaveFileFilters()) {
            saveFileChooser.addChoosableFileFilter(filter);
        }
    }

    protected FileFilter[] getSaveFileFilters() {
        return new FileFilter[]{new FileNameExtensionFilter("所有檔案", "*.*")};
    }

    public ModelingPanelContext getPanelContext() {
        return panelContext;
    }

    public void setPanelContext(ModelingPanelContext panelContext) {
        this.panelContext = panelContext;
        saveFileChooser.setCurrentDirectory(panelContext.getLecture().getHomeFolder());
    }
    
    protected final void saveModelAction(){
        int ret=saveFileChooser.showSaveDialog(this);
        if(ret==JFileChooser.APPROVE_OPTION){
            try {
                saveModel(saveFileChooser.getSelectedFile());
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
    }
    
    protected abstract void saveModel(File outputFile) throws IOException;
}
