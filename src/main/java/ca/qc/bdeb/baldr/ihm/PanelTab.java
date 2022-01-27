/*
 * PanelTab.java
 *
 * Created on 30 mars 2007, 19:03
 *$Id: PanelTab.java 249 2007-08-29 20:57:45Z cedric $
 */
package ca.qc.bdeb.baldr.ihm;

import ca.qc.bdeb.baldr.utils.Observation;
import ca.qc.bdeb.baldr.noyau.GestionnaireFiltres;
import ca.qc.bdeb.baldr.noyau.Noyau;
import ca.qc.bdeb.baldr.noyau.Projet;
import ca.qc.bdeb.baldr.noyau.Task;

import ca.qc.bdeb.baldr.utils.Observateur;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Toolkit;

import java.io.File;
import java.util.Date;
import java.util.List;

import java.util.ResourceBundle;
import java.util.concurrent.TimeUnit;

import javax.swing.JOptionPane;
import javax.swing.tree.DefaultMutableTreeNode;

/**
 * The PanelTab Class is in charge of one tab of the application. It will
 * display 3 panes with which the user can interact
 *
 * @author Baldr Team
 *
 * @see WindowBaldr
 *
 */
public class PanelTab extends javax.swing.JPanel
        implements Observateur {

    /**
     * id number of the tab
     */
    private int tabNumber;
    private String tabTitle;
    private PanelFiles pnlFiles;
    private PanelResults pnlResults;
    private ResourceBundle messages;
    private JReport jReport;
    private GestionnaireFiltres filtre;
    private Dimension screenSize;
    private Dimension windowSize;
    public Date tempsDebutAnalyse;

    /**
     * Analysis Results
     */
    private Task tache = null;
    private Noyau noyau;
    private Projet projetCourant;
    private WindowBaldr win;
    private Thread analyse;

    /**
     * Construct and display a tab
     *
     * @param tabNumber Le numéro de la tab
     * @param tache La nouvelle tache
     * @param noyau De type Noyau
     * @param win Fenêtre parente
     * @param filtre
     */
    public PanelTab(int tabNumber, Task tache, Noyau noyau, WindowBaldr win,
            GestionnaireFiltres filtre) {

        this.win = win;
        this.noyau = noyau;
        this.tache = tache;
        this.tabNumber = tabNumber;
        this.filtre = filtre;
        initVariables();
        initFenetre();
        // TODO : Vérifier l'utilité de ce bout de code... sachant que tache ici est le paramètre, et non l'attribut de PanelTab.
        tache.ajouterObservateur(pnlFiles);
        tache.ajouterObservateur(this);
    }

    public JReport getJReport(){
        return jReport;
    }
    public PanelFiles getPanelFile() {
        return pnlFiles;
    }

    public boolean isFileListEmpty() {
        boolean isEmpty;
        isEmpty = pnlFiles.isFileListEmpty();
        return isEmpty;
    }

    public boolean estSommaire() {
        return tache.estSommaire();
    }

    /**
     * Called on closing request to ask for saving.
     *
     */
    public void ExitAndSaveOnglet() {
        int choix = JOptionPane.showConfirmDialog(
                this, messages.getString("Save_Mods"), "Baldr", 1);
        if (choix == JOptionPane.NO_OPTION) {
            tache.aviserObservateurs(Observation.EXIT, this);
        } else if (choix == JOptionPane.OK_OPTION) {
            tache.aviserObservateurs(Observation.SAUVEGARDER, this);
        }
         
    }

    public DefaultMutableTreeNode getFileList() {
        return pnlFiles.getFileTree();
    }

    public String getJReportText() {
        return jReport.getText();
    }

    public void setTabNumber(int i) {
        tabNumber = i;
    }

    public String getTabTitle() {
        return tabTitle;
    }

    public void setTabTitle(String title) {
        this.tabTitle = title;
    }

    public Projet getProjectCourant() {
        return projetCourant;
    }

    public int getTabNumber() {
        return tabNumber;
    }

    @Override
    public void changementEtat() {
    }

    @Override
    public void changementEtat(Enum<?> property, Object o) {
        switch ((Observation) property) {
            case SAVEANDEXIT:
                ExitAndSaveOnglet();
                break;
            case UPDATEMAT:
            case ANALYSE_TERMINEE:
                pnlResults.updateMat(tache);
                break;
            case EXIT:
                win.fermerTab((PanelTab)o, false);
                break;
            case SAUVEGARDER:
                //if (win.sauver() != null) {
                win.fermerTab((PanelTab)o, false);  
                //}
                break;
        }
    }

    public String getTempsAnalyse() {
        Date tempsFinAnalyse = new Date();
        long millis = tempsFinAnalyse.getTime() - tempsDebutAnalyse.getTime();

        return String.format("%02d:%02d:%05.2f",
                TimeUnit.MILLISECONDS.toHours(millis),
                TimeUnit.MILLISECONDS.toMinutes(millis)
                - TimeUnit.HOURS.toMinutes(TimeUnit.MILLISECONDS.toHours(millis)),
                (double) ((TimeUnit.MILLISECONDS.toSeconds(millis)
                - TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(millis)))
                + (double) (millis - TimeUnit.SECONDS.toMillis(TimeUnit.MILLISECONDS.toSeconds(millis))) / 1000));

    }

    public void afficherCopieAnalyse(Task newTask) {
        pnlFiles.expandFirstTreeRow();
        List<File> files = pnlFiles.getFileList();
        tache = newTask;

        if (files != null && files.size() > 2) {
            newTask.ajouterObservateur(pnlFiles);

            pnlFiles.setEtatBoutonsPeutDebuter(true);
            pnlFiles.setEtatMenuContextuel(true);
        } else if (files != null && files.size() <= 2) {
            ErrorMessages.notEnoughFiles();
        } else {
            ErrorMessages.noFiles();
        }
    }

    /**
     * Démarre une analyse. Méthode appelée juste après l'action de
     * l'utilisateur.
     *
     * @return Si l'analyse a correctement été démarrée.
     */
    public boolean demarrerAnalyse() {
        List<File> files = pnlFiles.getFileList();
        tempsDebutAnalyse = new Date();

        boolean analyseLancee = false;

        // Minimum de 2 fichiers pour faire une analyse
        if (files != null && files.size() > 2) {
            pnlFiles.setEtatBoutonsPeutDebuter(false);
            pnlFiles.setEtatMenuContextuel(false);

            analyse = new Thread() {
                @Override
                public void run() {
                    tache.lancerAnalyse();
                }
            };
            
            analyse.start();
            
            analyseLancee = true;
        } else if (files != null && files.size() <= 2) {
            ErrorMessages.notEnoughFiles();
        } else {
            ErrorMessages.noFiles();
        }

        return analyseLancee;
    }
    
    public void arreterAnalyse() {
        if (analyse != null) {
            analyse.interrupt();
        }
    }
    
    public boolean analyseEnCours() {
        return (analyse != null && analyse.isAlive());
    }

    public Task getTask() {
        return tache;
    }

    void setTask(Task task) {
        tabTitle = task.getTitre();
        jReport.setText(task.getJReport());
        pnlFiles.setFileListFromTask(task);
        tache = task;
        tache.ajouterObservateur(pnlFiles);
        tache.ajouterObservateur(this);
        pnlResults.updateMat(tache);
    }

    private void initFenetre() {

        this.setLayout(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.fill = GridBagConstraints.BOTH;

        initialiserPanelFiles(c);
        this.add(pnlFiles, c);

        int windowX = Math.max(0, (screenSize.width - windowSize.width) - 480);

        intialiserPnlResults(windowX, c);
        this.add(pnlResults, c);
        jReport = new JReport(this, tache, win.getGestionnaireI18N(), win);
        initialiserJReport(c);
        this.add(jReport, c);
    }

    private void intialiserPnlResults(int windowX, GridBagConstraints c) {
        pnlResults = new PanelResults(this, noyau, win.getGestionnaireI18N());
        pnlResults.setBounds(460, 0, windowX, 645);
        c.gridx = 1;
        c.gridheight = 2;
        c.weightx = 1;
        c.weighty = 1;
    }

    private void initialiserJReport(GridBagConstraints c) {
        c.gridx = 0;
        c.gridy = 1;
        c.gridwidth = 1;
        c.gridheight = 1;
        c.weightx = 0;
        c.weighty = 1;
    }

    private void initialiserPanelFiles(GridBagConstraints c) {
        pnlFiles = new PanelFiles(this, noyau, win, filtre, tache);
        pnlFiles.setBounds(0, 0, 420, 645);
        c.gridx = 0;
        c.gridy = 0;
        c.weightx = 0;
        c.weighty = 1;
    }

    private void initVariables() {
       
        projetCourant = noyau.getProjetCourant();

        screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        windowSize = this.getSize();

        messages = ResourceBundle.getBundle("i18n/Baldr");
    }
   
}
