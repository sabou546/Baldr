/*
 * WindowBaldr.java
 *
 * Created on 23 mars 2007, 16:11
 *$Id: WindowBaldr.java 256 2007-11-17 17:22:26Z nezetic $
 */
package ca.qc.bdeb.baldr.ihm;

import ca.qc.bdeb.baldr.utils.Observation;
import static ca.qc.bdeb.baldr.ihm.PanelFiles.win;
import ca.qc.bdeb.baldr.ihm.fileFilters.BaldrFileFilter;

import ca.qc.bdeb.baldr.noyau.GestionnaireFiltres;
import ca.qc.bdeb.baldr.noyau.GestionnairePreferences;
import ca.qc.bdeb.baldr.noyau.Noyau;
import ca.qc.bdeb.baldr.noyau.Projet;
import ca.qc.bdeb.baldr.noyau.RienASauvegarderException;
import ca.qc.bdeb.baldr.noyau.SaveAndRestore;
import ca.qc.bdeb.baldr.noyau.Task;

import ca.qc.bdeb.baldr.utils.Extension;
import ca.qc.bdeb.baldr.utils.Observateur;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import java.io.File;
import java.net.URL;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Observable;
import java.util.Observer;
import java.util.ResourceBundle;

import javax.swing.ImageIcon;
import javax.swing.JFileChooser;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.filechooser.FileFilter;

/**
 * La fenêtre principale du programme.
 *
 * @author Baldr Team
 */
public class WindowBaldr extends javax.swing.JFrame implements Observateur, Observer {

    private List<PanelTab> listeOnglets = new ArrayList();
    public PanelTab tabSommaire = null;

    private APropos aProposBaldr;
    private ResourceBundle messages;

    private Noyau noyau = null;
    private Projet projetCourant;
    // Le fichier de sauvegarde de l'analyse.
    private File fichierEnregistrement;

    public static boolean analyseSommaireOuverte;
    public boolean nouveauProjet;

    private ImageIcon iconeAjouterOnglet;

    public GestionnaireFiltres filtre;
    private GestionnairePreferences preferences;
    private GestionnaireI18N gestionnaireI18N;
    private List<PanelTab> listeTab = new ArrayList();

    public Image iconBaldr;

    /**
     * Le constructeur de la classe, qui lie la fenêtre au noyau et au
     * gestionnaire de filtres.
     *
     * @param noyau Le noyau à lier
     * @param filtre Le gestionnaire de filtres à lier
     */
    public WindowBaldr(Noyau noyau, GestionnaireFiltres filtre) {

        this.filtre = filtre;

        fichierEnregistrement = null;

        this.noyau = noyau;
        this.preferences = noyau.getPrefs();

        projetCourant = noyau.getProjetCourant();

        analyseSommaireOuverte = false;

        String loc = preferences.readPref("LOCALE");

        if (loc != null && loc.length() > 1) {
            Locale.setDefault(new Locale(loc));
        }

        messages = ResourceBundle.getBundle("i18n/Baldr");

        verifierPreference();

        gestionnaireI18N = new GestionnaireI18N(preferences);
        gestionnaireI18N.addObserver(this);

        UIManager.put("OptionPane.yesButtonText",
                messages.getString("Yes"));
        UIManager.put("OptionPane.noButtonText",
                messages.getString("No"));
        UIManager.put("OptionPane.cancelButtonText",
                messages.getString("Cancel"));
        UIManager.put("FileChooser.lookInLabelText",
                messages.getString("Look_in"));
        UIManager.put("FileChooser.saveInLabelText",
                messages.getString("Save_in"));
        UIManager.put("FileChooser.fileNameLabelText",
                messages.getString("File_name"));
        UIManager.put("FileChooser.filesOfTypeLabelText",
                messages.getString("File_type"));
        UIManager.put("FileChooser.cancelButtonText",
                messages.getString("Cancel"));
        UIManager.put("FileChooser.openButtonText",
                messages.getString("Open_File"));
        UIManager.put("FileChooser.saveButtonText",
                messages.getString("Save"));
        UIManager.put("FileChooser.chooseButtonText",
                messages.getString("Choose"));

        try {
            UIManager.setLookAndFeel(
                    UIManager.getSystemLookAndFeelClassName());
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | UnsupportedLookAndFeelException ex) {
        }

        GraphicsDevice screen = GraphicsEnvironment.getLocalGraphicsEnvironment().
                getDefaultScreenDevice();
        GraphicsConfiguration config = screen.getDefaultConfiguration();
        setBounds(config.getBounds());

        initComponents();
        updateProjectName();
        placerImages();

//        Dimension screen = java.awt.Toolkit.getDefaultToolkit().getScreenSize();
//        Insets screenInsets = Toolkit.getDefaultToolkit().getScreenInsets(getGraphicsConfiguration());
//        int taskBarHeight = screenInsets.bottom;

        //setResizable(false);

        setIconImage(iconBaldr);

        jTabbedPane.addChangeListener(new TabChangeListener(this));
        jTabbedPane.addMouseListener(new MousePopupListener());

        // Préparer les onglets de la fenêtre par défaut.
        iconeAjouterOnglet
                = new ImageIcon(getClass().getResource("/Images/tab_add.png"));
        ajouterOnglet();

        setExtendedState(MAXIMIZED_BOTH);
        setVisible(true);

        aProposBaldr = new APropos(this);
        aProposBaldr.setIconImage(iconBaldr);

        // On supprime le lien tant que la documentation
        // en ligne n'est pas prête.
        contentsMenuItem.setEnabled(false);
        contentsMenuItem.setVisible(false);
    }

    public List<PanelTab> getListeTab() {
        return listeTab;
    }

    public GestionnaireFiltres getFiltre() {
        return filtre;
    }

    public void setTabSommaire(PanelTab newTab) {
        tabSommaire = newTab;
    }

    public Projet getProjetCourant() {
        return projetCourant;
    }

    public GestionnairePreferences getPreferences() {
        return preferences;
    }

    /**
     * Créée un onglet de sommaire.
     */
    public void creerSommaire(WindowBaldr win) {

        analyseSommaireOuverte = true;
        //makeSummaryMenuItem.setEnabled(false);

        int numeroDuTabLibre = win.nombreOnglets();
        PanelTab newTab = new PanelTab(numeroDuTabLibre,
                projetCourant.creerTacheSommaire(numeroDuTabLibre),
                noyau, win, win.getFiltre());
        listeTab.add(newTab);
        win.setTabSommaire(newTab);

        win.ajouterOnglet(win.getMessages().getString("Summary"), newTab);
    }

    /*
     Modifier le sommaire lorsqu'une analyse est ajoutée ou retirée
     */
    public void modifierSommaire(WindowBaldr win) {
        // temp (A.Ho)
        //projetCourant.setModifie(true);

        if (analyseSommaireOuverte) {

            // Fermture du sommaire hors-date
            win.fermerTab(win.getTabSommaire(), analyseSommaireOuverte);

            int numeroDuTabLibre = win.nombreOnglets();

            PanelTab newTab = new PanelTab(numeroDuTabLibre,
                    projetCourant.creerTacheSommaire(numeroDuTabLibre),
                    noyau, win, win.filtre);
            listeTab.add(newTab);
            win.setTabSommaire(newTab);
            analyseSommaireOuverte = true;

            //makeSummaryMenuItem.setEnabled(false);
            win.ajouterOnglet(win.getMessages().getString("Summary"), newTab);
        }

    }

    /**
     * Méthode qui permet d'ajouter un onglet dynamiquement dès que
     * l'utilisateur active l'option du sommaire intelligent. Sans cette
     * méthode, l'utilisateur doit manuellement ajouter un onglet pour qu'une
     * vérification à ces fins se fasse.
     */
    public void ajustementSmartSommaire(WindowBaldr win) {
        if (win.getPreferences().prefExists("SOMMAIRE")
                && (Boolean) win.getPreferences().readPref("SOMMAIRE", true)) {
            // Plus que 2 onglets d'ouvert et sommaire n'existe pas
            if (win.nombreOnglets() >= 2 && !win.verifierSiLesPanelEstSommaire()
                    && !win.nouveauProjet) {
                creerSommaire(win);
            }
        }
    }

    /**
     * Place les images de l'application sur les ressources prévues.
     */
    private void placerImages() {
        URL urlTabAdd = getClass().getResource("/Images/tab_add.png");
        URL urlCloseAnalysis = getClass().getResource("/Images/tab_delete.png");
        URL urlOpen = getClass().getResource("/Images/folder_go.png");
        URL urlSave = getClass().getResource("/Images/disk.png");
        URL urlSaveAs = getClass().getResource("/Images/ImgSaveAS.png");
        URL urlAbout = getClass().getResource("/Images/help.png");
        URL urlArchive = getClass().getResource("/Images/archive.png");
        URL urlNewProject = getClass().getResource("/Images/newproject.png");
        URL urlWindowsIcon = getClass().getResource("/Images/baldr.gif");
        URL urlFermerProjet = getClass().getResource("/Images/fermer_projet.png");

        newProjectMenuItem.setIcon(new ImageIcon(urlNewProject));
        menuItemNouvelleAnalyse.setIcon(new ImageIcon(urlTabAdd));
        closeAnalysisMenuItem.setIcon(new ImageIcon(urlCloseAnalysis));
        openMenuItem.setIcon(new ImageIcon(urlOpen));
        saveMenuItem.setIcon(new ImageIcon(urlSave));
        saveAsMenuItem.setIcon(new ImageIcon(urlSaveAs));
        preferencesMenuItem.setIcon(new ImageIcon(urlArchive));
        aboutMenuItem.setIcon(new ImageIcon(urlAbout));
        closeMenuItem.setIcon(new ImageIcon(urlFermerProjet));
        iconBaldr = Toolkit.getDefaultToolkit().getImage(urlWindowsIcon);
    }

    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        contexteMenuTab = new javax.swing.JPopupMenu();
        menuRenommerAnalyse = new javax.swing.JMenuItem();
        menuFermerAnalyse = new javax.swing.JMenuItem();
        menuFermerTout = new javax.swing.JMenuItem();
        menuDupliquerAnalyse = new javax.swing.JMenuItem();
        menuExporterAnalyse = new javax.swing.JMenuItem();
        jTabbedPane = new javax.swing.JTabbedPane();
        menuBar = new javax.swing.JMenuBar();
        fileMenu = new javax.swing.JMenu();
        newProjectMenuItem = new javax.swing.JMenuItem();
        jSeparator3 = new javax.swing.JPopupMenu.Separator();
        openMenuItem = new javax.swing.JMenuItem();
        closeMenuItem = new javax.swing.JMenuItem();
        jSeparator4 = new javax.swing.JPopupMenu.Separator();
        saveMenuItem = new javax.swing.JMenuItem();
        saveAsMenuItem = new javax.swing.JMenuItem();
        jSeparator2 = new javax.swing.JPopupMenu.Separator();
        exitMenuItem = new javax.swing.JMenuItem();
        projectMenu = new javax.swing.JMenu();
        menuItemNouvelleAnalyse = new javax.swing.JMenuItem();
        closeAnalysisMenuItem = new javax.swing.JMenuItem();
        jSeparator1 = new javax.swing.JPopupMenu.Separator();
        editMenu = new javax.swing.JMenu();
        preferencesMenuItem = new javax.swing.JMenuItem();
        helpMenu = new javax.swing.JMenu();
        contentsMenuItem = new javax.swing.JMenuItem();
        aboutMenuItem = new javax.swing.JMenuItem();

        contexteMenuTab.setName(""); // NOI18N

        menuRenommerAnalyse.setText(messages.getString("Rename_Tab")); // NOI18N
        menuRenommerAnalyse.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                menuRenommerAnalyseActionPerformed(evt);
            }
        });
        contexteMenuTab.add(menuRenommerAnalyse);

        menuFermerAnalyse.setText(messages.getString("Close_Tab")); // NOI18N
        menuFermerAnalyse.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                menuFermerAnalyseActionPerformed(evt);
            }
        });
        contexteMenuTab.add(menuFermerAnalyse);

        menuFermerTout.setText(messages.getString("Close_All")); // NOI18N
        menuFermerTout.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                menuFermerToutActionPerformed(evt);
            }
        });
        contexteMenuTab.add(menuFermerTout);

        menuDupliquerAnalyse.setText(messages.getString("Duplicate_Analysis")); // NOI18N
        menuDupliquerAnalyse.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                menuDupliquerAnalyseActionPerformed(evt);
            }
        });
        contexteMenuTab.add(menuDupliquerAnalyse);

        menuExporterAnalyse.setText(messages.getString("Export_To")); // NOI18N
        menuExporterAnalyse.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                menuExporterAnalyseActionPerformed(evt);
            }
        });
        contexteMenuTab.add(menuExporterAnalyse);

        setDefaultCloseOperation(javax.swing.WindowConstants.DO_NOTHING_ON_CLOSE);
        setTitle("Baldr");
        setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
        addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowClosing(java.awt.event.WindowEvent evt) {
                formWindowClosing(evt);
            }
        });

        jTabbedPane.setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));

        fileMenu.setText(messages.getString("File")); // NOI18N
        fileMenu.addMenuListener(new javax.swing.event.MenuListener() {
            public void menuCanceled(javax.swing.event.MenuEvent evt) {
            }
            public void menuDeselected(javax.swing.event.MenuEvent evt) {
            }
            public void menuSelected(javax.swing.event.MenuEvent evt) {
                fileMenuMenuSelected(evt);
            }
        });

        newProjectMenuItem.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_N, java.awt.event.InputEvent.CTRL_MASK));
        newProjectMenuItem.setText(messages.getString("New_Project")); // NOI18N
        newProjectMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                newProjectMenuItemActionPerformed(evt);
            }
        });
        fileMenu.add(newProjectMenuItem);
        fileMenu.add(jSeparator3);

        openMenuItem.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_O, java.awt.event.InputEvent.CTRL_MASK));
        openMenuItem.setText(messages.getString("Open")); // NOI18N
        openMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                openMenuItemActionPerformed(evt);
            }
        });
        fileMenu.add(openMenuItem);

        closeMenuItem.setText(messages.getString("Close_Project")); // NOI18N
        closeMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                closeMenuItemActionPerformed(evt);
            }
        });
        fileMenu.add(closeMenuItem);
        fileMenu.add(jSeparator4);

        saveMenuItem.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_S, java.awt.event.InputEvent.CTRL_MASK));
        saveMenuItem.setText(messages.getString("Save")); // NOI18N
        saveMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                saveMenuItemActionPerformed(evt);
            }
        });
        fileMenu.add(saveMenuItem);

        saveAsMenuItem.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_S, java.awt.event.InputEvent.SHIFT_MASK | java.awt.event.InputEvent.CTRL_MASK));
        saveAsMenuItem.setText(messages.getString("SaveAs")); // NOI18N
        saveAsMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                saveAsMenuItemActionPerformed(evt);
            }
        });
        fileMenu.add(saveAsMenuItem);
        fileMenu.add(jSeparator2);

        exitMenuItem.setText(messages.getString("Exit")); // NOI18N
        exitMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                exitMenuItemActionPerformed(evt);
            }
        });
        fileMenu.add(exitMenuItem);

        menuBar.add(fileMenu);

        projectMenu.setText(messages.getString("Project_Menu")); // NOI18N

        menuItemNouvelleAnalyse.setText(messages.getString("New_Analysis")); // NOI18N
        menuItemNouvelleAnalyse.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                menuItemNouvelleAnalyseActionPerformed(evt);
            }
        });
        projectMenu.add(menuItemNouvelleAnalyse);

        closeAnalysisMenuItem.setText(messages.getString("Close_Tab")); // NOI18N
        closeAnalysisMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                CloseTabActionPerformed(evt);
            }
        });
        projectMenu.add(closeAnalysisMenuItem);
        projectMenu.add(jSeparator1);

        menuBar.add(projectMenu);

        editMenu.setText(messages.getString("Options")); // NOI18N

        preferencesMenuItem.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_P, java.awt.event.InputEvent.CTRL_MASK));
        preferencesMenuItem.setText(messages.getString("Preference")); // NOI18N
        preferencesMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                Prefev(evt);
            }
        });
        editMenu.add(preferencesMenuItem);

        menuBar.add(editMenu);

        helpMenu.setText(messages.getString("Help")); // NOI18N

        contentsMenuItem.setText(messages.getString("Help_Topics")); // NOI18N
        contentsMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                contentsMenuItemActionPerformed(evt);
            }
        });
        helpMenu.add(contentsMenuItem);

        aboutMenuItem.setText(messages.getString("About")); // NOI18N
        aboutMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                aboutMenuItemActionPerformed(evt);
            }
        });
        helpMenu.add(aboutMenuItem);

        menuBar.add(helpMenu);

        setJMenuBar(menuBar);

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jTabbedPane, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 686, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jTabbedPane, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 428, Short.MAX_VALUE)
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents
/*    */
    private void CloseTabActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_CloseTabActionPerformed
        for (PanelTab onglet : listeOnglets) {
            if (onglet.isShowing()) {
                if (projetCourant.getModifie() && !onglet.isFileListEmpty()) {
                    onglet.ExitAndSaveOnglet();
                } else {
                    fermerTab(onglet, false);
                }
                break;
            }
        }
        modifierSommaire(this);
        if (win.getListeOnglets().size() <= 2) {
            win.fermerTab(win.getTabSommaire(), false);
        }
    }//GEN-LAST:event_CloseTabActionPerformed

    private void contentsMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_contentsMenuItemActionPerformed
    }//GEN-LAST:event_contentsMenuItemActionPerformed

    private void formWindowClosing(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_formWindowClosing
        if (projetCourant.getModifie()) {
            ExitAndSave();
        } else {
            System.exit(0);
        }
    }//GEN-LAST:event_formWindowClosing

    // TODO quand il n'y a pas de fichiers dans analyse, il plante lors du sauvegarde
    /**
     * Affiche une fenêtre de sélection de fichier pour permettre de sauvegarder
     * l'état du programme.
     *
     * @return Le fichier choisi, ou null
     */
    public File sauver() {
        // TODO rendre le FileFilter plus propre
        JFileChooser chooser = new JFileChooser();

        FileFilter baldrfifi = new BaldrFileFilter();

        String lastdir = preferences.readPref("LAST_DIR");
        String curdir;

        File savefile = null;

        int userChoice;

        chooser.setMultiSelectionEnabled(false);
        chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
        chooser.setFileFilter(baldrfifi);

        ecrireDonneesDansTask();

        if (lastdir != null) {
            chooser.setCurrentDirectory(new File(lastdir));
        }

        userChoice = chooser.showSaveDialog(this);
        curdir = chooser.getCurrentDirectory().toString();

        switch (userChoice) {
            case JFileChooser.APPROVE_OPTION:
                File f = chooser.getSelectedFile();

                if (Extension.getExtension(f) == null) {
                    f = new File(chooser.getSelectedFile().getAbsolutePath()
                            + "." + Extension.BALDR);
                }
                if (f.exists()) {
                    int reponse = JOptionPane.showConfirmDialog(null,
                            f.getName() + messages.getString("Overwrite"),
                            "Baldr",
                            JOptionPane.YES_NO_OPTION,
                            JOptionPane.INFORMATION_MESSAGE);

                    if (reponse != JOptionPane.YES_OPTION) {
                        return sauver();
                    }
                }
                fichierEnregistrement = f;
                enregistrer(fichierEnregistrement);
                savefile = f;

                break;
            case JFileChooser.CANCEL_OPTION:
                lastdir = curdir;
                break;
            case JFileChooser.ERROR_OPTION:
                break;
        }

        if (lastdir == null || lastdir.compareTo(curdir) != 0) {
            preferences.writePref("LAST_DIR", curdir);
        }
        updateProjectName();
        return savefile;
    }

    private void enregistrer(File fichier) {
        try {
            projetCourant.save(fichier);
        } catch (RienASauvegarderException e) {
            ErrorMessages.nothingToSave();
        }
    }

    /**
     * Inscrit le rapport, le titre du projet et la liste des fichiers dans les
     * tâches de chaque onglet.
     */
    private void ecrireDonneesDansTask() {
        for (PanelTab onglet : listeOnglets) {
            if (onglet != null) {
                Task task = onglet.getTask();
                task.setJReport(onglet.getJReportText());
                task.setTitre(onglet.getTabTitle());
                task.setFichiers(onglet.getPanelFile().getFileList(), false);
            }
        }
    }

    /**
     * Met à jour le nom de projet lorsqu'il est sauvegardé.
     */
    private void updateProjectName() {
        File lastFile = projetCourant.getLastFile();
        if (lastFile == null) {
            setTitle(messages.getString("Default_Project_Name") + " - Baldr");
        } else {
            setTitle(lastFile.getName() + " - Baldr");
        }
    }

    private void saveAsMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_saveAsMenuItemActionPerformed

        sauver();
    }//GEN-LAST:event_saveAsMenuItemActionPerformed
            private void aboutMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_aboutMenuItemActionPerformed
                aProposBaldr.setLocationRelativeTo(this);
                aProposBaldr.setVisible(true);
                aProposBaldr.startAbout();
    }//GEN-LAST:event_aboutMenuItemActionPerformed
                private void menuItemNouvelleAnalyseActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_menuItemNouvelleAnalyseActionPerformed
                    ajouterOnglet();
    }//GEN-LAST:event_menuItemNouvelleAnalyseActionPerformed
                                private void Prefev(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_Prefev
                                    Prefwin test = new Prefwin(this, noyau);
                                    test.setVisible(true);
    }//GEN-LAST:event_Prefev
	private void exitMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_exitMenuItemActionPerformed
            if (projetCourant.getModifie()) {
                ExitAndSave();
            } else {
                System.exit(0);
            }
    }//GEN-LAST:event_exitMenuItemActionPerformed

    private void menuRenommerAnalyseActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_menuRenommerAnalyseActionPerformed
    {//GEN-HEADEREND:event_menuRenommerAnalyseActionPerformed
        PanelTab panel = trouverOngletOriginePopupMenu(evt);
        String nomCourant = panel.getTabTitle();
        String nouveauNom;

        boolean existe;

        do {
            existe = false;
            do {
                nouveauNom = (String) JOptionPane.showInputDialog(this,
                        messages.getString("Rename_Tab_Label"),
                        messages.getString("Rename_Tab"),
                        JOptionPane.QUESTION_MESSAGE,
                        null, null, panel.getTabTitle());
                if ("".equals(nouveauNom)) {
                    JOptionPane.showMessageDialog(this, messages.getString("analysis_name_empty"),
                            messages.getString("New_Analysis"), JOptionPane.ERROR_MESSAGE);
                }
            } while ("".equals(nouveauNom));

            for (PanelTab onglet : listeOnglets) {
                if (onglet.getTabTitle().equals(nouveauNom)) {
                    if (!nouveauNom.equals(nomCourant)) {
                        existe = true;
                    }
                }
            }

            if (existe || nouveauNom.toUpperCase().equals(messages.getString("Summary").toUpperCase())) {
                JOptionPane.showMessageDialog(this, messages.getString("analysis_name_taken"),
                        messages.getString("New_Analysis"), JOptionPane.ERROR_MESSAGE);
                existe = true;
            }
        } while (existe);

        if (nouveauNom != null) {
            int index = rechercherIndexPanelTab(panel);
            jTabbedPane.setTitleAt(index, nouveauNom);
            panel.setTabTitle(nouveauNom);
            Task tache = projetCourant.getTasks().get(index);
            //La tâche n'était par renommée avant de redémarrer le programme
            tache.setTitre(nouveauNom);
        }
    }//GEN-LAST:event_menuRenommerAnalyseActionPerformed

    private void menuFermerAnalyseActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_menuFermerAnalyseActionPerformed
    {//GEN-HEADEREND:event_menuFermerAnalyseActionPerformed
        PanelTab panel = trouverOngletOriginePopupMenu(evt);
        if (projetCourant.getModifie()
                && !panel.getPanelFile().getFileTree().isLeaf()) {
            panel.ExitAndSaveOnglet();
        } else {
            fermerTab(panel, false);
        }
        if (projetCourant.getTacheSommaire() != null) {
            win.fermerTab(win.getTabSommaire(), false);
            creerSommaire(this);
        }
        if (projetCourant.getTasks().size() <= 2 && projetCourant.getTacheSommaire() != null) {
            win.fermerTab(win.getTabSommaire(), false);
        }
    }//GEN-LAST:event_menuFermerAnalyseActionPerformed

    private void menuFermerToutActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_menuFermerToutActionPerformed
    {//GEN-HEADEREND:event_menuFermerToutActionPerformed
        PanelTab ongletConserve = trouverOngletOriginePopupMenu(evt);
        boolean sauvegarde = false;
        List<PanelTab> duplique = new ArrayList(listeOnglets);
        for (PanelTab pt : duplique) {
            if (ongletConserve != pt) {
                if (projetCourant.getModifie()
                        && !pt.getPanelFile().getFileTree().isLeaf() && !sauvegarde) {
                    pt.ExitAndSaveOnglet();
                    sauvegarde = true;
                } else {
                    fermerTab(pt, true);

                }
                modifierSommaire(this);
            }

        }
        win.fermerTab(win.getTabSommaire(), false);
    }//GEN-LAST:event_menuFermerToutActionPerformed

    private void menuDupliquerAnalyseActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_menuDupliquerAnalyseActionPerformed
    {//GEN-HEADEREND:event_menuDupliquerAnalyseActionPerformed
        PanelTab panelOrigine = trouverOngletOriginePopupMenu(evt);
        Task newTask
                = projetCourant.duplicateAndRegisterTask(panelOrigine.getTask());
        PanelTab newTab
                = new PanelTab(listeOnglets.size(), newTask, noyau, this, filtre);
        listeTab.add(newTab);
        newTab.setTabTitle(newTask.getTitre());
        ajouterOnglet(newTask.getTitre(), newTab);
        newTab.setTask(newTask);
    }//GEN-LAST:event_menuDupliquerAnalyseActionPerformed

    private void saveMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_saveMenuItemActionPerformed
        if (projetCourant.getModifie() && fichierEnregistrement != null) {
            enregistrer(fichierEnregistrement);
        } else if (projetCourant.getModifie()
                && fichierEnregistrement == null) {
            sauver();
        }
    }//GEN-LAST:event_saveMenuItemActionPerformed

    private void newProjectMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_newProjectMenuItemActionPerformed
        nouveauProjet = true;
        creerNouveauProjet();
    }//GEN-LAST:event_newProjectMenuItemActionPerformed

    private void menuExporterAnalyseActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_menuExporterAnalyseActionPerformed
        PanelTab panel = trouverOngletOriginePopupMenu(evt);
        Task analys = panel.getTask();
        if (analys != null && analys.getTousFichiers().size() > 2) {
            JFileChooser chooser = new JFileChooser();
            FileFilter baldrfifi = new BaldrFileFilter();
            String lastdir = preferences.readPref("LAST_DIR");

            chooser.setMultiSelectionEnabled(false);
            chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
            chooser.setFileFilter(baldrfifi);
            chooser.setApproveButtonText(messages.getString("Export")); // ne fonctionne pas? o.o
            chooser.setApproveButtonToolTipText(messages.getString("Export_Tooltip"));

            if (lastdir != null) {
                chooser.setCurrentDirectory(new File(lastdir));
            }
            if (chooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
                SaveAndRestore.exportTo(analys, chooser.getSelectedFile());
            }
        } else {
            //todo: afficher erreur
        }
    }//GEN-LAST:event_menuExporterAnalyseActionPerformed

    private void closeMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_closeMenuItemActionPerformed
        nouveauProjet = true;
        creerNouveauProjet();
        // Fermer le tab de sommaire manuellement
        if (tabSommaire != null) {
            fermerTab(tabSommaire, false);
        }
    }//GEN-LAST:event_closeMenuItemActionPerformed

    private void fileMenuMenuSelected(javax.swing.event.MenuEvent evt) {//GEN-FIRST:event_fileMenuMenuSelected
        if (projetCourant.getModifie()) {
            saveMenuItem.setEnabled(true);
        } else {
            saveMenuItem.setEnabled(false);
        }
    }//GEN-LAST:event_fileMenuMenuSelected

    private void openMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_openMenuItemActionPerformed
        JFileChooser chooser = new JFileChooser();
        FileFilter baldrfifi = new BaldrFileFilter();

        if (preferences.prefExists("PREVIEW")
                && (Boolean) preferences.readPref("PREVIEW", false)) {
        }

        chooser.setMultiSelectionEnabled(false);
        chooser.setFileFilter(baldrfifi);
        chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
        chooser.removeChoosableFileFilter(chooser.getAcceptAllFileFilter());

        String lastdir = preferences.readPref("LAST_DIR");

        if (lastdir != null) {
            chooser.setCurrentDirectory(new File(lastdir));
        }

        int res = chooser.showOpenDialog(this);

        String curdir = chooser.getCurrentDirectory().toString();

        switch (res) {
            case JFileChooser.APPROVE_OPTION:
                File fichierChoisi = chooser.getSelectedFile();
                if (!fichierEstUtilisable(fichierChoisi)) {
                    ErrorMessages.invalidFile();
                    return;
                }
                if (projetCourant.getModifie()) {
                    int choix = JOptionPane.showConfirmDialog(this, // Fenêtre mère
                            messages.getString("Save_Mods"), // Message
                            "Baldr", // Titre
                            JOptionPane.YES_NO_CANCEL_OPTION);
                    if (choix == JOptionPane.OK_OPTION) {
                        sauver();
                    } else if (choix == JOptionPane.CANCEL_OPTION) {
                        lastdir = curdir;
                        break;
                    }
                }

                fermerTout();

                fichierEnregistrement = chooser.getSelectedFile();
                projetCourant.clearTasks();
                projetCourant.restore(fichierEnregistrement);
                boolean erreur = false;
                for (Task task : projetCourant.getTasks()) {
                    task.setPrefs(preferences);
                    if (task.getEtatRestauration() == true) {
                        erreur = true;
                    }
                }
                if (erreur) {
                    JOptionPane.showMessageDialog(this, "Le projet ne peut pas être ouvert car un fichier a été modifié ou supprimé.", "ERREUR", JOptionPane.WARNING_MESSAGE);
                } else {
                    
                miseAJourIHM();
                updateProjectName();
                }
                break;
            case JFileChooser.CANCEL_OPTION:
                lastdir = curdir;
                break;
            case JFileChooser.ERROR_OPTION:
                break;
        }

        if (lastdir == null || lastdir.compareTo(curdir) != 0) {
            preferences.writePref("LAST_DIR", curdir);
        }
        verifierSiNouveauSommaire();
    }//GEN-LAST:event_openMenuItemActionPerformed

    private static boolean fichierEstUtilisable(File fichierChoisi) {
        return fichierChoisi.exists() && Extension.contientExtensionValide(fichierChoisi);
    }

    /* TODO : Nouveau Projet et Fermer Projet utilisent le même ActionPerformed
     * Il faudra peut-être les séparer dans le future si jamais on voudrait que
     * les deux menus réagissent différemment.
     */
    private void creerNouveauProjet() {
        if (projetCourant.getModifie()) {
            int choix = JOptionPane.showConfirmDialog(this, // Fenêtre mère
                    messages.getString("Save_Mods"), // Message
                    "Baldr", // Titre
                    JOptionPane.YES_NO_CANCEL_OPTION);
            if (choix == JOptionPane.OK_OPTION) {
                sauver();
            } else if (choix == JOptionPane.CANCEL_OPTION || choix == JOptionPane.CLOSED_OPTION) {
                return;
            }
        }
        fermerTout();

        projetCourant.setLastFile(null);
    }

    private int rechercherIndexPanelTab(PanelTab panel) {
        return jTabbedPane.indexOfComponent(panel);
    }

    private PanelTab trouverOngletOriginePopupMenu(ActionEvent evt) {
        JMenuItem mnuItem = (JMenuItem) evt.getSource();
        JPopupMenu popupMenu = (JPopupMenu) mnuItem.getParent();
        JTabbedPane jTabbedPane = (JTabbedPane) popupMenu.getInvoker();
        return (PanelTab) jTabbedPane.getSelectedComponent();
    }

    /**
     * Ferme tous les onglets ouverts.
     */
    public void fermerTout() {

        while (listeOnglets.size() > 1) {
            fermerTab(listeOnglets.get(listeOnglets.size() - 1), false);
        }

        fermerTab(listeOnglets.get(0), false);

        analyseSommaireOuverte = false;
        projetCourant.setTacheSommaire(null);
        //makeSummaryMenuItem.setEnabled(true);
    }

    /**
     * Ferme un onglet.
     *
     * @param onglet L'onglet à fermer.
     * @param toutSauf Indique si l'onglet lui-même doit rester ouvert.
     */
    public void fermerTab(PanelTab onglet, boolean toutSauf) {

        if (!toutSauf) {
            if (jTabbedPane.getSelectedIndex() != 0) {
                jTabbedPane.setSelectedIndex(
                        jTabbedPane.getSelectedIndex() - 1);
            } else {
                jTabbedPane.setSelectedIndex(0);
                onglet.setTabTitle("Analyse_1");
            }
        }

        listeOnglets.remove(onglet);
        jTabbedPane.remove(onglet);

        if (onglet.analyseEnCours()) {
            onglet.arreterAnalyse();
        }
        projetCourant.retirerTask(onglet.getTask());

        if (onglet.estSommaire()) {
            analyseSommaireOuverte = false;
            projetCourant.setTacheSommaire(null);
            //makeSummaryMenuItem.setEnabled(true);
        }

        setTabNumbers();
    }

    /**
     * Patch qui remets les numéros de tab
     */
    public void setTabNumbers() {
        int i = 0;
        for (PanelTab pt : listeOnglets) {
            pt.setTabNumber(i++);
        }
    }

    /**
     * Ajoute la liste de fichiers <code>fichiers</code> à l'analyse
     * <code>task</code>
     *
     * @param task la tâche où ajouter les fichiers
     * @param fichiers les fichiers à ajouter
     */
    public void ajouterFichiers(Task task, List<File> fichiers) {
        for (PanelTab tab : listeOnglets) {
            if (tab.getTask().equals(task)) {
                tab.getPanelFile().ajouterFichiersEtMettreAJourArbre(fichiers);
            }
        }
    }

    public PanelTab getTabSommaire() {
        return tabSommaire;
    }

    /**
     * Ajoute un onglet avec le titre d'onglet par défaut, le met en focus.
     */
    public void ajouterOnglet() {
        ajouterOnglet(messages.getString("Analysis"));
    }

    /**
     * Ajoute un onglet avec le titre d'onglet fourni, le met en focus.
     *
     * @param titre Le titre de l'onglet
     */
    private void ajouterOnglet(String titre) {
        ajouterOnglet(titre, null);
    }

    /**
     * Ajoute un onglet avec un nom et un onglet fournis, le met en focus.
     *
     * @param titre Le nom de l'onglet
     * @param newTab Le PanelTab à lier à l'onglet
     */
    public void ajouterOnglet(String titre, PanelTab newTab) {
        /**
         * TODO Refactorer la manière dont les tabs sont ajouté, possiblement
         * enlever la nécéssité du ArrayList listeOnglets, smells bad.
         */
        if (newTab == null) {
            int numeroDuTabLibre = nombreOnglets();

            Task newTask = projetCourant.createTask(numeroDuTabLibre);
            newTask.ajouterObservateur(this);

            newTab = new PanelTab(numeroDuTabLibre, newTask,
                    noyau, this, filtre);
            if (listeTab.size() != 0) {
                listeTab.add((listeTab.size() - 1), newTab);
            } else {
                listeTab.add(newTab);
            }
            titre = changerTitre(titre);
            newTask.setTitre(titre);
        }

        newTab.setTabTitle(titre);

        listeOnglets.add(newTab);

        int index = jTabbedPane.indexOfTab("");

        if (index != -1) {
            if (analyseSommaireOuverte
                    && !newTab.estSommaire()
                    && index != 0) {
                jTabbedPane.setComponentAt(index - 1, newTab);
                jTabbedPane.setIconAt(index - 1, null);
                jTabbedPane.setTitleAt(index - 1, titre);

                jTabbedPane.setComponentAt(index, tabSommaire);
                jTabbedPane.setIconAt(index, null);
                jTabbedPane.setTitleAt(index, tabSommaire.getTabTitle());
            } else {
                jTabbedPane.setComponentAt(index, newTab);
                jTabbedPane.setIconAt(index, null);
                jTabbedPane.setTitleAt(index, titre);
            }
        } else {
            jTabbedPane.addTab(titre, newTab);
        }
        if (!newTab.estSommaire()) {
            jTabbedPane.setSelectedComponent(newTab);
        }

        ajouterOngletNouvelleAnalyse();

        // Plus que 2 onglets d'ouvert et sommaire n'existe pas
        if (listeOnglets.size() >= 2 && !verifierSiLesPanelEstSommaire()
                && !nouveauProjet && !projetCourant.sommaireExiste) {
            creerSommaire(this);
        }

    }

    private String changerTitre(String titre) {
        int compteur = 1;
        String titreInitial = titre;
        boolean existe;

        do {
            existe = false;
            for (PanelTab onglet : listeOnglets) {
                if (onglet.getTabTitle().equals(titre)) {
                    existe = true;
                }
            }
            if (existe) {
                titre = titreInitial + "_" + compteur;
                ++compteur;
            }
        } while (existe);

        return titre;
    }

    /**
     * Ajoute une nouvelle analyse dans l'interface, et retourne l'analyse.
     *
     * @param nomAnalyse le nom de la nouvelle analyse
     * @return l'analyse qui vient d'être créée
     */
    public Task ajouterNouvelleAnalyse(String nomAnalyse) {
        ajouterOnglet(nomAnalyse);
        PanelTab tab = (PanelTab) jTabbedPane.getSelectedComponent();
        tab.getTask().setTitre(nomAnalyse);
        return tab.getTask();
    }

    /**
     * Place un onglet "Créer nouvelle analyse" à la fin des onglets existants.
     */
    private void ajouterOngletNouvelleAnalyse() {
        jTabbedPane.addTab("",
                iconeAjouterOnglet,
                null,
                messages.getString("New_Analysis")
        );
    }

    /**
     * Retourne le nombre d'onglets ouverts.
     *
     * @return Le nombre d'onglets ouverts
     */
    public int nombreOnglets() {
        return listeOnglets.size();
    }

    /**
     * Accesseur de la liste des onglets de la fenêtre.
     *
     * @return La liste des PanelTab (onglets) de la fenêtre
     */
    public List<PanelTab> getListeOnglets() {
        return new ArrayList(listeOnglets);
    }

    /**
     * Méthode déclenchée par la tentative de fermer la fenêtre. Demande à
     * l'utilisateur s'il souhaite sauvegarder, etc., et quitte.
     */
    public void ExitAndSave() {

        int choix = JOptionPane.showConfirmDialog(this, // Fenêtre mère
                messages.getString("Save_Mods"), // Message
                "Baldr", // Titre
                JOptionPane.YES_NO_CANCEL_OPTION);
        if (choix == JOptionPane.NO_OPTION) {
            System.exit(0);
        } else if (choix == JOptionPane.OK_OPTION) {
            if (sauver() != null) {
                System.exit(0);
            }
        }
    }

    /**
     * L'accesseur de l'attribut messages, qui contient les ressources i18n du
     * programme.
     *
     * @return L'attribut messages
     */
    public ResourceBundle getMessages() {
        return messages;
    }

    public GestionnaireI18N getGestionnaireI18N() {
        return gestionnaireI18N;
    }

    /**
     * Lorsqu'un fichier est ouvert, on remet les analyses restaurées dans
     * l'interface.
     */
    private void miseAJourIHM() {

        for (Task task : projetCourant.getTasks()) {
            if (!task.estRestauree()) {
                PanelTab vide = trouverPanelTabVide();

                if (vide == null) {
                    int numeroDuTabLibre = nombreOnglets();

                    PanelTab save = new PanelTab(numeroDuTabLibre,
                            task, noyau, this, filtre);
                    listeTab.add(save);
                    ajouterOnglet(task.getTitre(), save);

                    save.setTask(task);
                } else {
                    int index = rechercherIndexPanelTab(vide);
                    jTabbedPane.setTitleAt(index, task.getTitre());
                    vide.setTask(task);
                }

                task.marquerCommeRestauree();
                task.ajouterObservateur(this);
            }
        }
    }

    private PanelTab trouverPanelTabVide() {
        for (PanelTab onglet : listeOnglets) {
            if (onglet.isFileListEmpty()) {
                return onglet;
            }
        }

        return null;
    }

    @Override
    public void changementEtat() {
    }

    @Override
    public void changementEtat(Enum<?> e, Object o) {
        switch ((Observation) e) {
            case EXIT:
                fermerTab((PanelTab) o, false);
                break;
            case SAUVEGARDER:
                if (sauver() != null) {
                    fermerTab((PanelTab) o, false);
                }
                break;
            case AJOUTEONGLET:
                ajouterOnglet();
                break;
        }

    }

    @Override
    public void update(Observable o, Object arg) {
        messages = (ResourceBundle) arg;
        UIManager.put("OptionPane.yesButtonText",
                messages.getString("Yes"));
        UIManager.put("OptionPane.noButtonText",
                messages.getString("No"));
        UIManager.put("OptionPane.cancelButtonText",
                messages.getString("Cancel"));
        UIManager.put("FileChooser.lookInLabelText",
                messages.getString("Look_in"));
        UIManager.put("FileChooser.saveInLabelText",
                messages.getString("Save_in"));
        UIManager.put("FileChooser.fileNameLabelText",
                messages.getString("File_name"));
        UIManager.put("FileChooser.filesOfTypeLabelText",
                messages.getString("File_type"));
        UIManager.put("FileChooser.cancelButtonText",
                messages.getString("Cancel"));
        UIManager.put("FileChooser.openButtonText",
                messages.getString("Open_File"));
        UIManager.put("FileChooser.saveButtonText",
                messages.getString("Save"));
        UIManager.put("FileChooser.chooseButtonText",
                messages.getString("Choose"));
        menuRenommerAnalyse.setText(messages.getString("Rename_Tab"));
        menuFermerAnalyse.setText(messages.getString("Close_Tab"));
        menuFermerTout.setText(messages.getString("Close_All"));
        menuDupliquerAnalyse.setText(messages.getString("Duplicate_Analysis"));
        menuExporterAnalyse.setText(messages.getString("Export_To")); // NOI18N
        fileMenu.setText(messages.getString("File")); // NOI18N
        newProjectMenuItem.setText(messages.getString("New_Project")); // NOI18N
        openMenuItem.setText(messages.getString("Open")); // NOI18N
        closeMenuItem.setText(messages.getString("Close_Project")); // NOI18N
        saveMenuItem.setText(messages.getString("Save")); // NOI18N
        saveAsMenuItem.setText(messages.getString("SaveAs")); // NOI18N
        exitMenuItem.setText(messages.getString("Exit")); // NOI18N
        projectMenu.setText(messages.getString("Project_Menu")); // NOI18N
        menuItemNouvelleAnalyse.setText(messages.getString("New_Analysis")); // NOI18N
        closeAnalysisMenuItem.setText(messages.getString("Close_Tab")); // NOI18N
        //makeSummaryMenuItem.setText(messages.getString("Make_Summary")); // NOI18N
        editMenu.setText(messages.getString("Options")); // NOI18N
        preferencesMenuItem.setText(messages.getString("Preference")); // NOI18N
        helpMenu.setText(messages.getString("Help")); // NOI18N
        contentsMenuItem.setText(messages.getString("Help_Topics")); // NOI18N
        aboutMenuItem.setText(messages.getString("About")); // NOI18N
        aProposBaldr.getLabelText().setText(messages.getString("About_Text"));
        aProposBaldr.getLabelTitre().setText(messages.getString("About_Title"));
        aProposBaldr.updateStringAbout();

        for (int i = 0; i < listeOnglets.size(); i++) {
            if (jTabbedPane.getTitleAt(i).contains("Analysis") || jTabbedPane.getTitleAt(i).contains("Analyse")) {
                if (i == 0) {
                    listeOnglets.get(i).setTabTitle(messages.getString("Analysis"));
                    jTabbedPane.setTitleAt(i, messages.getString("Analysis"));
                } else {
                    listeOnglets.get(i).setTabTitle(messages.getString("Analysis") + "_" + i);
                    jTabbedPane.setTitleAt(i, messages.getString("Analysis") + "_" + i);
                }
            }
        }
        updateProjectName();
    }

    public boolean verifierSiLesPanelEstSommaire() {
        for (PanelTab Panel : listeOnglets) {
            if (Panel.estSommaire()) {
                return true;
            }
        }
        return false;
    }

    /**
     * Quand un projet est ouvert, la methode verifie s'il contient un sommaire,
     * si oui: grise l'option dans la menu barre
     */
    private void verifierSiNouveauSommaire() {
        for (PanelTab Panel : listeOnglets) {
            if (Panel.estSommaire()) {
                //makeSummaryMenuItem.setEnabled(false);

            }
        }
    }

    private void verifierPreference() {
        if ((Boolean) preferences.readPref("PROGRESSIVE", false)) {
            preferences.writePref("PROGRESSIVE", true);
        }
    }

    private class MousePopupListener extends MouseAdapter {

        public MousePopupListener() {
        }

        @Override
        public void mouseClicked(MouseEvent e) {
            showPopup(e);
            int index = jTabbedPane.getSelectedIndex();
            Task tache = projetCourant.getTasks().get(index);
            int test = listeOnglets.size();
            boolean active = false;
            for (PanelTab onglet : listeOnglets) {
                if (tache.equals(listeOnglets.get(test - 1).getTask()) && test > 2) {
                    onglet.getPanelFile().desactiverSuppression();
                    menuFermerTout.setEnabled(false);
                    menuFermerAnalyse.setEnabled(false);
                    closeMenuItem.setEnabled(false);
                    menuDupliquerAnalyse.setEnabled(false);
                    closeAnalysisMenuItem.setEnabled(false);
                }
                if (!tache.equals(listeOnglets.get(test - 1).getTask())) {
                    onglet.getPanelFile().activerSuppression();
                    menuFermerTout.setEnabled(true);
                    menuFermerAnalyse.setEnabled(true);
                    closeMenuItem.setEnabled(true);
                    menuDupliquerAnalyse.setEnabled(true);
                    closeAnalysisMenuItem.setEnabled(true);
                }

            }

        }

        @Override
        public void mousePressed(MouseEvent e) {
            showPopup(e);
        }

        @Override
        public void mouseReleased(MouseEvent e) {
            showPopup(e);
        }

        private void showPopup(MouseEvent e) {
            if (e.isPopupTrigger()
                    && jTabbedPane.getSelectedComponent() != null) {
                //condition si sommaire alors on disable l'option fermerAnalyse
                if(jTabbedPane.getSelectedComponent() == tabSommaire){
                    menuFermerAnalyse.setEnabled(false);
                    menuDupliquerAnalyse.setEnabled(false);
                    menuFermerTout.setEnabled(false);
                }
                contexteMenuTab.show(e.getComponent(), e.getX(), e.getY());
                
            }
        }
    }

    private class TabChangeListener implements ChangeListener {

        private WindowBaldr wb;

        public TabChangeListener(WindowBaldr wb) {
            this.wb = wb;
        }

        /**
         * Gestion des changements sur le JTabbedPane qui controle visuellement
         * les onglets de l'application.
         *
         * @param e
         */
        @Override
        public void stateChanged(ChangeEvent e) {
            JTabbedPane jTabbedPane = (JTabbedPane) e.getSource();

            // Ajout d'un onglet met le SELECTED sur un NULL
            if (jTabbedPane.getSelectedComponent() == null) {
                nouveauProjet = false;
                wb.ajouterOnglet();
            }
        }
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JMenuItem aboutMenuItem;
    private javax.swing.JMenuItem closeAnalysisMenuItem;
    private javax.swing.JMenuItem closeMenuItem;
    private javax.swing.JMenuItem contentsMenuItem;
    private javax.swing.JPopupMenu contexteMenuTab;
    private javax.swing.JMenu editMenu;
    private javax.swing.JMenuItem exitMenuItem;
    private javax.swing.JMenu fileMenu;
    private javax.swing.JMenu helpMenu;
    private javax.swing.JPopupMenu.Separator jSeparator1;
    private javax.swing.JPopupMenu.Separator jSeparator2;
    private javax.swing.JPopupMenu.Separator jSeparator3;
    private javax.swing.JPopupMenu.Separator jSeparator4;
    private javax.swing.JTabbedPane jTabbedPane;
    private javax.swing.JMenuBar menuBar;
    private javax.swing.JMenuItem menuDupliquerAnalyse;
    private javax.swing.JMenuItem menuExporterAnalyse;
    private javax.swing.JMenuItem menuFermerAnalyse;
    private javax.swing.JMenuItem menuFermerTout;
    private javax.swing.JMenuItem menuItemNouvelleAnalyse;
    private javax.swing.JMenuItem menuRenommerAnalyse;
    private javax.swing.JMenuItem newProjectMenuItem;
    private javax.swing.JMenuItem openMenuItem;
    private javax.swing.JMenuItem preferencesMenuItem;
    private javax.swing.JMenu projectMenu;
    private javax.swing.JMenuItem saveAsMenuItem;
    private javax.swing.JMenuItem saveMenuItem;
    // End of variables declaration//GEN-END:variables
}
