package ca.qc.bdeb.baldr.ihm;

import ca.qc.bdeb.baldr.utils.Observation;
import static ca.qc.bdeb.baldr.utils.Observation.ANALYSE_TERMINEE;
import ca.qc.bdeb.baldr.ihm.renderers.TreeCellCustomRenderer;
import ca.qc.bdeb.baldr.main.Main;

import ca.qc.bdeb.baldr.noyau.GestionnaireFiltres;
import ca.qc.bdeb.baldr.noyau.GestionnairePreferences;
import ca.qc.bdeb.baldr.noyau.Noyau;
import ca.qc.bdeb.baldr.noyau.Projet;
import ca.qc.bdeb.baldr.noyau.Sommaire;
import ca.qc.bdeb.baldr.noyau.Task;

import ca.qc.bdeb.baldr.utils.ArrayUtil;
import ca.qc.bdeb.baldr.utils.Correspondance;

import ca.qc.bdeb.baldr.utils.Observateur;
import java.awt.Component;
import java.awt.Container;

import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.MouseEvent;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;

import java.net.URL;

import java.util.*;
import javax.swing.AbstractAction;

import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.JProgressBar;
import javax.swing.JTree;
import javax.swing.tree.*;
import org.apache.commons.io.filefilter.WildcardFileFilter;

/**
 *
 * @author Carl
 */
public class PanelFiles extends javax.swing.JPanel
        implements Observateur, Observer{

    private String[] filtres;
    private String[] filtresExclu;
    private ResourceBundle messages;
    private GestionnaireFiltres gestionFiltres;
    private GestionnairePreferences preferences;
    private boolean alteredPrefs = false;

    /**
     * Arbre des fichiers à analyser.
     */
    private DefaultMutableTreeNode structureFichiers;
    //private double[] valLin;

    private Noyau noyau = null;

    private List<File> fichiersApresSuppression;

    private Projet projetCourant;

    public static WindowBaldr win = null;

    private String dernierTempsAnalyseString = null;
    private EtatAnalyse etatAnalyse;

    private PanelTab ongletParent;
    
    private Task tache;
    
    private TreePath[] selectionEmpty;
    
    private TreePath[] backUp;
   
    private boolean resteAssezDeFichiers() {
        return fichiersApresSuppression != null
                && fichiersApresSuppression.size() >= 2;
    }

    private void setInvalideEtGriser() {
        tache.setInvalide(true);
        PanelResults panneauResultat = trouverPanelResult(win);
        panneauResultat.repaint();
    }

    /**
     * Objets qui formeront le modèle de l'arbre de fichiers.
     */
    public class FileTreeElement implements Serializable {

        private final File fichier;

        private FileTreeElement(File fichier) {
            this.fichier = fichier;
        }

        public boolean isDirectory() {
            return fichier.isDirectory();
        }

        public boolean isSource() {
            return getTask().isSource(fichier);
        }

        private boolean hasSubdirectories() {
            return getTask().hasSubdirectoriesInFiles(fichier);
        }

        @Override
        public String toString() {
            String nom = fichier.getName();
            return (nom.isEmpty() ? fichier.getAbsolutePath() : nom);
        }
    }

    enum EtatAnalyse {
        PRET,
        ANNULATION,
        TERMINE
    };

    /**
     * Objets qui formeront le modèle de l'arbre de fichiers.
     */
    public class ConcatenatedFileTreeElement extends FileTreeElement {

        private final ArrayList<FileTreeElement> contatenatedNodes = new ArrayList<>();

        private ConcatenatedFileTreeElement(FileTreeElement node) {
            super(node.fichier);
            this.contatenatedNodes.add(node);
        }

        @Override
        public String toString() {
            StringBuilder result = new StringBuilder();
            for (FileTreeElement node : contatenatedNodes) {
                result.append(node.toString());
                result.append(File.separator);
            }
            return result.length() > 0 ? result.substring(0, result.length() - 1) : "";
        }

        public void addNode(FileTreeElement file) {
            contatenatedNodes.add(file);
        }
    }

    /**
     * Creates new form PanelFiles
     *
     * @param parent
     * @param noyau
     * @param win
     * @param filtre
     */
    public PanelFiles(
            PanelTab parent, Noyau noyau, WindowBaldr win,
            GestionnaireFiltres filtre, Task tache) {
        this.tache = tache;
        this.ongletParent = parent;
        this.win = win;
        this.noyau = noyau;
        this.preferences = noyau.getPrefs();
        projetCourant = noyau.getProjetCourant();
        this.setLayout(new FlowLayout());
        messages = ResourceBundle.getBundle("i18n/Baldr");
        structureFichiers
                = new DefaultMutableTreeNode(messages.getString("Documents"));
        initComponents();
        creerListeActions();
        placerIcones();
        actionInterrompreAnalyse.setEnabled(false);
        itemMenuAjouterSource.setVisible(false);
        itemMenuSupprimerSource.setVisible(false);
        this.gestionFiltres = filtre;
        etatAnalyse = EtatAnalyse.PRET;
        actionRetirerFichiers.setEnabled(false);
        if (parent.estSommaire()) {
            actionAjouterFichiers.setEnabled(false);
            actionRetirerFichiers.setEnabled(false);
            setFileListFromTask(noyau.getProjetCourant().getTasks().get(
                    parent.getTabNumber()));
        }
        win.getGestionnaireI18N().addObserver(this);
        getTask().ajouterObservateur(this);
    }
    
    private void creerListeActions() {
        actionsMenuContextuel = new ArrayList();
        
        actionsMenuContextuel.add(actionExclureFichiers);
        actionsMenuContextuel.add(actionAjouterSource);
        actionsMenuContextuel.add(actionSupprimerSource);
        actionsMenuContextuel.add(actionAjouterSousDossiersSources);
        actionsMenuContextuel.add(actionRetirerSousDossiersSources);
        actionsMenuContextuel.add(actionCopierFichiers);
        actionsMenuContextuel.add(actionDeplacerFichiers);
        actionsMenuContextuel.add(actionCopierFichiersFiltre);
        actionsMenuContextuel.add(actionDeplacerFichiersFiltre);
    }

    private List<AbstractAction> actionsMenuContextuel;
    
    public void SetBtnAnalyseEnabled(boolean newVal) {
        btnDebutAnalyse.setEnabled(newVal);
        //ici on peint en gris si true 
     if(newVal == true){
         
         setInvalideEtGriser();
     }
    }
    public boolean GetBtnAnalyseEnabled() {
        return btnDebutAnalyse.isEnabled();
    }
    
    private final AbstractAction actionAjouterFichiers = new AbstractAction() {

        @Override
        public void actionPerformed(ActionEvent ae) {
            afficherDialogueAjoutFichiers();
            expandFirstTreeRow();
            btnDebutAnalyse.setEnabled(true);
            setInvalideEtGriser();
            //on peint en gris
        }
    };
    
    private final AbstractAction actionRetirerFichiers = new AbstractAction() {

        @Override
        public void actionPerformed(ActionEvent ae) {
            retirerFichiersSelectionnes();
            btnDebutAnalyse.setEnabled(true);
        }
    };
    
    private final AbstractAction actionDemarrerAnalyse = new AbstractAction() {

        @Override
        public void actionPerformed(ActionEvent ae) {
            if (ongletParent.demarrerAnalyse()) {
                actionAjouterFichiers.setEnabled(false);
                actionRetirerFichiers.setEnabled(false);
                btnDebutAnalyse.setEnabled(false);
                tache.setInvalide(false);
                //plus en gris
            }
        }
    };
    
    private final AbstractAction actionInterrompreAnalyse = new AbstractAction() {

        @Override
        public void actionPerformed(ActionEvent ae) {
            ongletParent.arreterAnalyse();
            actionAjouterFichiers.setEnabled(true);
            actionRetirerFichiers.setEnabled(true);
            progressBarAnalyse.setValue(0);
            descrEtat.setText(messages.getString("Analysis_Canceled"));
            etatAnalyse = EtatAnalyse.ANNULATION;
            setEtatBoutonsPeutDebuter(true);
            setEtatMenuContextuel(true);
        }
    };
    
    private final AbstractAction actionNouvelleAnalyse = new AbstractAction() {

        @Override
        public void actionPerformed(ActionEvent ae) {
            win.nouveauProjet = false;
            getTask().aviserObservateurs(Observation.AJOUTEONGLET, null);
            win.modifierSommaire(win);
        }
    };
    
    private final AbstractAction actionFermerAnalyse = new AbstractAction() {

        @Override
        public void actionPerformed(ActionEvent ae) {
            for (PanelTab onglet : win.getListeOnglets()) {
                if (onglet.isShowing() && !onglet.estSommaire()) {
                    activerSuppression();
                    if (projetCourant.getModifie() && !onglet.isFileListEmpty()) {
                        onglet.ExitAndSaveOnglet();
                    } else {
                        win.fermerTab(onglet, false);
                    }
                      
                    break;
                } else {
                    desactiverSuppression();
                }
            }
                
            if (win.getListeOnglets().size() <= 2){
                win.fermerTab(win.getTabSommaire(), false);
            }
        }
    };
    
    private final AbstractAction actionExclureFichiers = new AbstractAction() {

        @Override
        public void actionPerformed(ActionEvent ae) {
            exclureFichiers();
            btnDebutAnalyse.setEnabled(true);
        }
    };
    
    private final AbstractAction actionAjouterSource = new AbstractAction() {

        @Override
        public void actionPerformed(ActionEvent ae) {
            FileTreeElement fichierSelectionne = getFirstSelectedFileTreeElement();
            getTask().ajouterSource(fichierSelectionne.fichier);
            arbreFichiers.updateUI();
            expandAllTreeRows();
            btnDebutAnalyse.setEnabled(true);
        }
    };
    
    private final AbstractAction actionSupprimerSource = new AbstractAction() {

        @Override
        public void actionPerformed(ActionEvent ae) {
            FileTreeElement fichierSelectionne = getFirstSelectedFileTreeElement();
            getTask().supprimerSource(fichierSelectionne.fichier);
            arbreFichiers.updateUI();
        }
    };
    
    private final AbstractAction actionAjouterSousDossiersSources = new AbstractAction() {

        @Override
        public void actionPerformed(ActionEvent ae) {
            DefaultMutableTreeNode dossierSelectionne = getFirstSelectedNode();
            Enumeration contenu = dossierSelectionne.children();

            while (contenu.hasMoreElements()) {
                FileTreeElement element = (FileTreeElement) ((DefaultMutableTreeNode) contenu.nextElement()).getUserObject();
                getTask().ajouterSource(element.fichier);
            }

            arbreFichiers.updateUI();
        }
    };
    
    private final AbstractAction actionRetirerSousDossiersSources = new AbstractAction() {

        @Override
        public void actionPerformed(ActionEvent ae) {
            DefaultMutableTreeNode dossierSelectionne = getFirstSelectedNode();
            Enumeration contenu = dossierSelectionne.children();

            while (contenu.hasMoreElements()) {
                FileTreeElement element = (FileTreeElement) ((DefaultMutableTreeNode) contenu.nextElement()).getUserObject();

                getTask().supprimerSource(element.fichier);
            }

            arbreFichiers.updateUI();
        }
    };
    
    private final AbstractAction actionCopierFichiers = new AbstractAction() {

        @Override
        public void actionPerformed(ActionEvent ae) {
            List<File> fichiers = getFichiersSelectionnes();
            
            if (fichiers.size() > 0) {
                Task tache = demanderChoixAnalyse();
                
                if (tache != null) {
                    win.ajouterFichiers(tache, fichiers);
                }
            }
        }
    };
    
    private final AbstractAction actionDeplacerFichiers = new AbstractAction() {

        @Override
        public void actionPerformed(ActionEvent ae) {
            List<File> fichiers = getFichiersSelectionnes();
            
            if (fichiers.size() > 0) {
                Task tache = demanderChoixAnalyse();
            
                if (tache != null) {
                    win.ajouterFichiers(tache, fichiers);
                    getTask().retirerFichiers(fichiers);
                    setFileListFromTask(getTask());
                }
            }
        }
    };
    
    private final AbstractAction actionCopierFichiersFiltre = new AbstractAction() {

        @Override
        public void actionPerformed(ActionEvent ae) {
            String wildcard = demanderWildcard();
        
            if (wildcard != null) {
                selectionnerNoeudsSelonFiltre(wildcard);
            }
        
            List<File> fichiers = getFichiersSelectionnes();
        
            if (fichiers.size() > 0) {
                Task tache = demanderChoixAnalyse();
            
                if (tache != null) {
                    win.ajouterFichiers(tache, fichiers);
                }
            }
        }
    };
    
    private final AbstractAction actionDeplacerFichiersFiltre = new AbstractAction() {

        @Override
        public void actionPerformed(ActionEvent ae) {
            String wildcard = demanderWildcard();
        
            if (wildcard != null) {
                selectionnerNoeudsSelonFiltre(wildcard);
            }
        
            List<File> fichiers = getFichiersSelectionnes();
        
            if (fichiers.size() > 0) {
                Task tache = demanderChoixAnalyse();
            
                if (tache != null) {
                    win.ajouterFichiers(tache, fichiers);
                    getTask().retirerFichiers(fichiers);
                    setFileListFromTask(getTask());
                }
            }
        }
    };
    
    private void exclureFichiers() {
        if (arbreFichiers.getRowCount() > 1) {
            String wildcard = (String) JOptionPane.showInputDialog(
                    this.getTopLevelAncestor(),
                    messages.getString("exclude_set_filter"),
                    messages.getString("exclude_files"),
                    JOptionPane.QUESTION_MESSAGE,
                    null,
                    null,
                    "*.*");

            if (wildcard != null) {
                selectionnerNoeudsSelonFiltre(wildcard);
                retirerFichiersSelectionnes();
            }
        } else {
            JOptionPane.showMessageDialog(this.getTopLevelAncestor(),
                    messages.getString("exclude_please_add_files"),
                    messages.getString("exclude_no_files"),
                    JOptionPane.ERROR_MESSAGE);
        }
    }
    

    public DefaultMutableTreeNode getFileTree() {
        return structureFichiers;
    }
    
    public void desactiverSuppression(){
        actionFermerAnalyse.setEnabled(false);
    }
    
    public void activerSuppression(){
        actionFermerAnalyse.setEnabled(true);
    }

    private void placerIcones() {
        URL urlImageAjouter = getClass().getResource("/Images/add.png");
        URL urlImageSupprimer = getClass().getResource("/Images/delete.png");
        URL urlImageDebuterAnalyse = getClass().getResource("/Images/sum.png");
        URL urlImageAnnulerAnalyse = getClass().getResource("/Images/cross.png");
        URL urlImageAjouterAnalyse = getClass().getResource("/Images/tab_add.png");
        URL urlImageFermerAnalyse = getClass().getResource("/Images/tab_delete.png");

        itemMenuAjouter.setIcon(new javax.swing.ImageIcon(urlImageAjouter));
        itemMenuSupprimer.setIcon(new javax.swing.ImageIcon(urlImageSupprimer));
        itemMenuLancer.setIcon(new javax.swing.ImageIcon(urlImageDebuterAnalyse));
        btnAjoutFichiers.setIcon(new javax.swing.ImageIcon(urlImageAjouter));
        btnRetirerFichiers.setIcon(new javax.swing.ImageIcon(urlImageSupprimer));
        btnDebutAnalyse.setIcon(new javax.swing.ImageIcon(urlImageDebuterAnalyse));
        btnInterruptAnalyse.setIcon(new javax.swing.ImageIcon(urlImageAnnulerAnalyse));
        btnNouvelleAnalyse.setIcon(new javax.swing.ImageIcon(urlImageAjouterAnalyse));
        btnFermerAnalyse.setIcon(new javax.swing.ImageIcon(urlImageFermerAnalyse));

    }

    public boolean isFileListEmpty() {
        return structureFichiers.isLeaf();
    }

    public DefaultMutableTreeNode getLastSelectedNode() {
        if (arbreFichiers.isSelectionEmpty()) {
            return structureFichiers;
        }

        // Premier fichier sélectionné
        TreePath ins = arbreFichiers.getSelectionPath();
        DefaultMutableTreeNode lro = structureFichiers; // Par défaut : racine.

        // Permet de récupérer le noeud sélectionné.
        if (ins != null) {
            // Tout l'arbre en largeur.
            Enumeration files = structureFichiers.breadthFirstEnumeration();

            while (files.hasMoreElements()) {
                DefaultMutableTreeNode fich
                        = (DefaultMutableTreeNode) files.nextElement();
                if (ins.equals(new TreePath(fich.getPath()))) {
                    lro = fich;
                    break;
                }
            }
        }

        // Si (sélectionné == fichier), retourner le dossier parent.
        if (lro.isLeaf() && !lro.isRoot()
                && !((FileTreeElement) lro.getUserObject()).fichier.isDirectory()) {

            return (DefaultMutableTreeNode) lro.getParent();
        }

        return lro;
    }

    /**
     * @return Liste des fichiers qui se trouvent dans l'arbre.
     */
    public List<File> getFileList() {

        Enumeration files = structureFichiers.depthFirstEnumeration();
        DefaultMutableTreeNode fich;

        if (!structureFichiers.isLeaf()) {
            List<File> fichiers = new ArrayList();

            while (files.hasMoreElements()) {
                fich = (DefaultMutableTreeNode) files.nextElement();

                if (fich.isLeaf()
                        && !((FileTreeElement) fich.getUserObject()).fichier.isDirectory()) {
                    fichiers.add(
                            ((FileTreeElement) fich.getUserObject()).fichier);
                }
            }
            return fichiers;
        }
        return null;
    }

    /**
     * Créée la liste des fichiers à partir d'une tâche.
     *
     * @param analys La tâche d'où tirer les fichiers.
     */
    public final void setFileListFromTask(Task analys) {
        List<File> tabFichiers = analys.getTousFichiers();

        if (tabFichiers != null && analys.getCommonAncestors() != null) {
            File[] commonAncestors = analys.getCommonAncestors();
            
            if (commonAncestors.length == 1) {
                structureFichiers= new DefaultMutableTreeNode("Documents "+commonAncestors[0].getPath());
                for (File fic : tabFichiers) {
                    ajouterFichierANoeud(structureFichiers, commonAncestors[0], fic);
                }
            } else {
                structureFichiers = new DefaultMutableTreeNode(messages.getString("Documents"));
                Map<File, File[]> filesByAncestors = analys.getFilesByRoot();

                for (File ancestor : commonAncestors) {
                    DefaultMutableTreeNode racine = new DefaultMutableTreeNode(new FileTreeElement(ancestor));
                    structureFichiers.add(racine);

                    for (File fic : filesByAncestors.get(ancestor)) {
                        ajouterFichierANoeud(racine, ancestor, fic);
                    }
                }
            }

            simplifierListeFichierNoeud((DefaultMutableTreeNode) structureFichiers);

            arbreFichiers.setModel(new DefaultTreeModel(structureFichiers));
            expandAllTreeRows();
        }
    }

    private void simplifierListeFichierNoeudRec(DefaultMutableTreeNode node, DefaultMutableTreeNode parentNode, ConcatenatedFileTreeElement parent) {
        FileTreeElement fileTreeElement = (FileTreeElement) node.getUserObject();
        // Peut se compresser avec le fils
        if (node.getChildCount() == 1 && fileTreeElement.isDirectory() && !fileTreeElement.isSource()) {
            DefaultMutableTreeNode newNode;
            if (parent == null) {
                parent = new ConcatenatedFileTreeElement(fileTreeElement);
                newNode = new DefaultMutableTreeNode(parent);
                parentNode.add(newNode);
            } else {
                parent.addNode(fileTreeElement);
                newNode = parentNode;
            }
            simplifierListeFichierNoeudRec((DefaultMutableTreeNode) node.getFirstChild(), newNode, parent);
            return;
        }

        if (!fileTreeElement.isDirectory()) {
            parentNode.add(node);
            return;
        }

        ArrayList<MutableTreeNode> oldChildren = new ArrayList<>();

        Enumeration children = node.children();
        while (children.hasMoreElements()) {
            oldChildren.add((MutableTreeNode) children.nextElement());
        }
        node.removeAllChildren();

        if (parent != null) {
            parent.addNode(fileTreeElement);
        } else {
            parentNode.add(node);
            parentNode = node;
        }

        for (MutableTreeNode child : oldChildren) {
            if (child instanceof DefaultMutableTreeNode) {
                FileTreeElement fileTreeElement2 = (FileTreeElement) ((DefaultMutableTreeNode) child).getUserObject();
                if (fileTreeElement2.isDirectory()) {
                    simplifierListeFichierNoeudRec((DefaultMutableTreeNode) child, parentNode, null);
                } else {
                    parentNode.add((MutableTreeNode) ((DefaultMutableTreeNode) child).clone());
                }
            }
        }
        expandAllTreeRows();
    }

    private void simplifierListeFichierNoeud(DefaultMutableTreeNode structureFichiers) {
        ArrayList<MutableTreeNode> oldChildren = new ArrayList<>();

        Enumeration children = structureFichiers.children();
        while (children.hasMoreElements()) {
            oldChildren.add((MutableTreeNode) children.nextElement());
        }
        structureFichiers.removeAllChildren();

        for (MutableTreeNode child : oldChildren) {
            if (child instanceof DefaultMutableTreeNode) {
                simplifierListeFichierNoeudRec((DefaultMutableTreeNode) child, structureFichiers, null);
            } else {
                structureFichiers.add(child);
            }
        }

        //
        if (structureFichiers.getChildCount() == 1) {
            String tab[];
            char backslash = 92;
            String back = "" + backslash;
            String nomRacine = structureFichiers.getRoot().toString();
            //structureFichiers.
            tab = nomRacine.split("//");
            structureFichiers.setUserObject(tab[tab.length - 1]);
        }
    }

    /**
     * Ajoute un fichier à un arbre.
     *
     * @param racine Racine de l'arbre.
     * @param ficAjout Fichier à ajouter.
     */
    private void ajouterFichierANoeud(DefaultMutableTreeNode racine,
            File ficRacine, File ficAjout) {
        // À partir de ficRacine,
        // sous-dossier dont une branche contient ficAjout
        File nextDirToFile = ficAjout;
        while (!nextDirToFile.getParentFile().equals(ficRacine)) {
            nextDirToFile = nextDirToFile.getParentFile();
        }

        if (nextDirToFile.equals(ficAjout)) {
            // S'il a été impossible de trouver un répertoire parent
            racine.add(new DefaultMutableTreeNode(
                    new FileTreeElement(ficAjout)));
        } else {
            // Trouver le dossier référencé par nextDirToFile
            Enumeration contenuDossier = racine.children();
            boolean trouve = false;
            while (contenuDossier.hasMoreElements()) {
                DefaultMutableTreeNode elm
                        = (DefaultMutableTreeNode) contenuDossier.nextElement();

                if (((FileTreeElement) elm.getUserObject()).fichier.equals(nextDirToFile)) {
                    trouve = true;
                    ajouterFichierANoeud(elm, nextDirToFile, ficAjout);
                }
            }

            if (!trouve) {
                racine.add(new DefaultMutableTreeNode(
                        new FileTreeElement(nextDirToFile)));
                ajouterFichierANoeud(racine, ficRacine, ficAjout);
            }
        }
    }

    private List<File> getTreeSelectedFiles() {
        TreePath[] paths = arbreFichiers.getSelectionPaths();
        if (paths == null) {
            return null;
        }
        List<File> fichiers = new ArrayList();
        for (TreePath p : paths) {
            DefaultMutableTreeNode o
                    = (DefaultMutableTreeNode) p.getLastPathComponent();
            if (o.isLeaf()) {
                if (o.getUserObject() instanceof FileTreeElement) {
                    fichiers.add(((FileTreeElement) o.getUserObject()).fichier);
                }
            }
        }
        return fichiers;
    }

    /**
     * Ouvre des fichiers dans l'éditeur spécifié dans les préférences
     * utilisateur.
     */
    private void openEditor(List<File> fichiers) {
        String editor = preferences.readPref("EDITOR");

        if (editor.length() > 1) {
            if (!editor.contains("$1")) {
                editor += " $1";
            }

            if (System.getProperty("os.name").toUpperCase().contains("MAC")
                    && editor.replace("$1", "").trim().endsWith(".app")) {
                editor = "open -a " + editor;
            }

            String[] filenames = new String[fichiers.size()];
            for (int i = 0; i < fichiers.size(); i++) {
                filenames[i] = fichiers.get(i).getAbsolutePath();
            }

            try {
                Runtime.getRuntime().exec(
                        makeEditorArgsFromCmd(editor, filenames));
            } catch (IOException exp) {
                ErrorMessages.cannotExecute();
            }
        } else {
            ErrorMessages.noEditorDefined();
        }
    }

    /**
     * À partir d'une commande "abc$1def", donne un tableau {abc, $1, def}, où
     * $1 est remplacé par les noms ou les chemins d'un ou de plusieurs
     * fichiers.
     *
     * @param cmd
     * @param filenames
     * @return
     */
    private String[] makeEditorArgsFromCmd(String cmd, String[] filenames) {
        String[] arr = cmd.split("\\$1");
        final int nbFiles = filenames.length;

        // «$1» au début ou à la fin
        if (arr.length == 1) {
            String[] newArr = new String[nbFiles + 1];

            if (cmd.endsWith("$1")) {
                newArr[0] = arr[0];
                ArrayUtil.copyRangeInto(newArr, 1, filenames, 0, nbFiles);
            } else {
                newArr[nbFiles] = arr[0];
                ArrayUtil.copyRangeInto(newArr, 0, filenames, 0, nbFiles);
            }

            arr = newArr;
        } // «$1» au milieu
        else {
            String[] newArr = new String[nbFiles + 2];
            ArrayUtil.copyRangeInto(newArr, 1, filenames, 0, nbFiles);
            newArr[0] = arr[0];
            newArr[newArr.length - 1] = arr[1];
            arr = newArr;
        }

        return ArrayUtil.trimStringsInArray(arr);
    }

    /**
     * Function that display a file chooser to add files or directories to the
     * analysis.
     */
    private void afficherDialogueAjoutFichiers() {
        JFileChooser chooser = new JFileChooser();

        chooser.setMultiSelectionEnabled(true); // Rend un tab de files

        chooser.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);

        String lastdir = preferences.readPref("LAST_DIR");
        if (lastdir != null) {
            chooser.setCurrentDirectory(new File(lastdir));
        }

        int res = chooser.showOpenDialog(this);

        /**
         * TODO: Le switch devrait utiliser des méthodes pour clarifier le code
         */
        switch (res) {
            case JFileChooser.APPROVE_OPTION:
                if (chooser.getSelectedFile().isDirectory()) {
                    FenetreFiltreFichiers frameFiltre
                            = new FenetreFiltreFichiers(win, gestionFiltres);
                    frameFiltre.setVisible(true);
                    filtres = frameFiltre.getFiltresAjoutSelectionnes();
                    filtresExclu=frameFiltre.getFiltresExcluSelectionnes();

                    if (filtres == null) {
                        JOptionPane.showMessageDialog(
                                null,
                                messages.getString("Add_File_Cancel"),
                                messages.getString("ERROR"),
                                JOptionPane.CANCEL_OPTION);
                        break;
                    }
                }

                // FIXME : il faudrait ajouter directement les fichiers
                // à la tâche, puis ensuite les récupérer (traitement inutile)
                ajouterFichiersSurNoeud(chooser.getSelectedFiles(),
                        trouverRepertoireParent());

                if (getTask().getTousFichiers().isEmpty()) {
                    ErrorMessages.noFiles();
                    break;
                }

                setFileListFromTask(getTask());
                String curdir = chooser.getCurrentDirectory().toString();
                if (lastdir == null || lastdir.compareTo(curdir) != 0) {
                    preferences.writePref("LAST_DIR", curdir);
                }
                break;
            case JFileChooser.CANCEL_OPTION:
                break;
            case JFileChooser.ERROR_OPTION:
                break;
        }
    }

    /**
     * Dans l'arbre de fichiers, si un élément est sélectionné, retourne le
     * répertoire dans lequel cet élément se trouve (si c'est un fichier) ou le
     * répertoire lui-même. À défaut d'une sélection, retourne la racine.
     */
    private DefaultMutableTreeNode trouverRepertoireParent() {
        DefaultMutableTreeNode repertoireParent = structureFichiers; // Par défaut : racine
        TreePath elementSelectione = arbreFichiers.getSelectionPath();

        if (elementSelectione != null) {

            Enumeration files = structureFichiers.breadthFirstEnumeration();

            DefaultMutableTreeNode treeNodeTemporaire;
            while (files.hasMoreElements()) {
                treeNodeTemporaire = (DefaultMutableTreeNode) files.nextElement();

                if (elementSelectione.equals(new TreePath(treeNodeTemporaire.getPath()))) {
                    repertoireParent = treeNodeTemporaire;
                    break;
                }
            }

            if (repertoireParent.isLeaf() && !repertoireParent.isRoot()) {
                return (DefaultMutableTreeNode) repertoireParent.getParent();
            }
        }

        return repertoireParent;
    }

    /**
     * Ajoute des fichiers à l'analyse.
     *
     * @param nouveauxFichiers Les nouveaux fichiers.
     * @param noeud L'arbre auquel ajouter les fichiers.
     */
    public void ajouterFichiersSurNoeud(File[] nouveauxFichiers, DefaultMutableTreeNode noeud) {
        for (File fichier : nouveauxFichiers) {
            Enumeration enumeration = noeud.preorderEnumeration();
            while (enumeration.hasMoreElements()) {
                String prochainElement = enumeration.nextElement().toString();
                if (!(fichier.getPath().equals(prochainElement))) {
                    noeud.add(construireArbreRepertoire(fichier, noeud));
                    enleverDossierVide(noeud);
                    break;
                }
            }
        }
        arbreFichiers.updateUI();

        getTask().setFichiers(getFileList(), false);

        // Modifie les résultats du sommaire si il existe
        win.modifierSommaire(win); // Ajouter / Enlever des éléments du sommaire
    }

    public boolean verifierExistenceFichier(File fichier) {
        boolean existe = false;
        List<File> fichierTab = getFileList();
        if (fichierTab != null) {
            for (File test : fichierTab) {
                if (test.equals(fichier)) {
                    existe = true;
                    break;
                }
            }
        }
        return existe;
    }

    /**
     * Permet de retirer les feuilles d'un arbre (DefaultMutableTreeNode) qui
     * sont des dossiers.
     *
     * @param arbre reçoit un arbre à modifier
     * @return le DefaultMutableTreeNode sans les dossiers vides
     */
    private DefaultMutableTreeNode enleverDossierVide(
            DefaultMutableTreeNode arbre) {

        int dossierVide;
        DefaultMutableTreeNode fich;
        List<DefaultMutableTreeNode> dossierASupprimer = new ArrayList();

        do {
            Enumeration files = arbre.depthFirstEnumeration();
            dossierVide = 0;

            // On ne travaille pas sur l'arbre si la racine n'a pas de fils.
            if (!arbre.isLeaf()) {
                while (files.hasMoreElements()) {
                    fich = (DefaultMutableTreeNode) files.nextElement();

                    if (fich.isLeaf() && ((FileTreeElement) fich.getUserObject()).fichier.isDirectory()) {

                        dossierASupprimer.add(fich);
                        dossierVide++;
                    }
                }
                for (DefaultMutableTreeNode d : dossierASupprimer) {
                    d.removeFromParent();
                }

                dossierASupprimer.clear();
            }
        } while (dossierVide != 0); // Passe dans l'arbre jusqu'à ce qu'il n'y ait plus de feuille qui soient des répertoires
        return arbre;
    }

    /**
     * Construit l'arbre des fichiers et repertoires que contient le répertoire
     * passé en paramètre.
     *
     * @param fileSystemElement Élément de système de fichiers (fichier ou
     * répertoire) duquel construire l'arbre.
     * @return Racine de l'arbre.
     */
    private DefaultMutableTreeNode construireArbreRepertoire(
            File fileSystemElement, DefaultMutableTreeNode noeud) {

        DefaultMutableTreeNode elm = new DefaultMutableTreeNode(
                new FileTreeElement(fileSystemElement));
        if (fileSystemElement.isDirectory()) {
            for (File fichier : fileSystemElement.listFiles()) {
                WildcardFileFilter regexExclu = new WildcardFileFilter(filtresExclu);
                WildcardFileFilter regexAjout = new WildcardFileFilter(filtres);
                if (fichier.isDirectory()) {
                    elm.add(construireArbreRepertoire(fichier, noeud));
                } else if (!regexExclu.accept(fichier)) {
                    if (regexAjout.accept(fichier)) {
                        if (!verifierExistenceFichier(fichier)) {
                            elm.add(new DefaultMutableTreeNode(
                                    new FileTreeElement(fichier)));
                        }
                    }
                }
            }
        }
        return elm;
    }

    private void retirerFichiersSelectionnes() {
        if (arbreFichiers.isSelectionEmpty()) {
            return;
        }
        
        TreePath[] removeList = arbreFichiers.getSelectionPaths();
        if(removeList == null){
            removeList = backUp;
        }
        supprimerNodes(removeList);

//        if (faitPartieDuSommaire() && !verifierAutresAnalyses(removeList)) {
//            supprimerDuSommaire(removeList);
//        }
        simplifierListeFichierNoeud(structureFichiers);

        mettreAJourFichiers();
        //ongletParent.demarrerAnalyse(); //<-- Enlever pour reparer l'issue 0000155 (Justin)
        mettreAJourUI();
        expandAllTreeRows();
        win.modifierSommaire(win);
        backUp = arbreFichiers.getSelectionPaths();
        arbreFichiers.setSelectionPath(null);
        actionRetirerFichiers.setEnabled(false);
    }


// Méthode servant à mettre à jour les fichiers...du sommaire?
    public void mettreAJourFichiers() {
        this.fichiersApresSuppression = getFileList();
    }


    /**
     *
     *
     * @param removeList
     */
    private void supprimerNodes(TreePath[] removeList) {
        DefaultMutableTreeNode nodeEnumeration;
        Enumeration filesEnumeration;

        for (TreePath treePath : removeList) {

            filesEnumeration = structureFichiers.breadthFirstEnumeration();

            while (filesEnumeration.hasMoreElements()) {

                nodeEnumeration = (DefaultMutableTreeNode) filesEnumeration.nextElement();

                String cheminFichierAEnlever = arrayToPath(treePath.getPath());
                String cheminNode = arrayToPath(nodeEnumeration.getPath());
                
                if (cheminNode.contains(cheminFichierAEnlever)) {

                    nodeEnumeration.removeAllChildren();

                    if (!nodeEnumeration.isRoot()) {
                        DefaultMutableTreeNode parentNode
                                = (DefaultMutableTreeNode) nodeEnumeration.getParent();
                        parentNode.remove(nodeEnumeration);
                    }

                    break;
                }
            }
        }

        enleverDossierVide(structureFichiers);

        win.modifierSommaire(win);
    }

    private String arrayToPath(Object[] tableau) {
        StringBuilder sb = new StringBuilder();
        for (Object element : tableau) {
            sb.append(File.separatorChar);
            sb.append(element);
        }
        return sb.toString();
    }

    private void mettreAJourMatriceAvecNouveauFichiers() {
        //if (resteAssezDeFichiers()) {
        getTask().mettreAJourFichiersEtMettreAJourMatrice(fichiersApresSuppression);
        //}

        //Partie enlevee pour enlever le message d'erreur
//        else {
//            ErrorMessages.notEnoughFiles();
//        }
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        menuContextuel = new javax.swing.JPopupMenu();
        itemMenuAjouter = new javax.swing.JMenuItem(actionAjouterFichiers);
        itemMenuSupprimer = new javax.swing.JMenuItem(actionRetirerFichiers);
        separateurMenu = new javax.swing.JPopupMenu.Separator();
        itemMenuExclure = new javax.swing.JMenuItem(actionExclureFichiers)
        ;
        itemMenuLancer = new javax.swing.JMenuItem(actionDemarrerAnalyse);
        menuSources = new javax.swing.JMenu();
        itemMenuAjouterSource = new javax.swing.JMenuItem(actionAjouterSource)
        ;
        itemMenuSupprimerSource = new javax.swing.JMenuItem(actionSupprimerSource)
        ;
        separateurMenuSources = new javax.swing.JPopupMenu.Separator();
        itemMenuAjouterSousDossiersSources = new javax.swing.JMenuItem(actionAjouterSousDossiersSources)
        ;
        itemMenuRetirerSousDossiersSources = new javax.swing.JMenuItem(actionRetirerSousDossiersSources)
        ;
        menuDéplacementFichiers = new javax.swing.JMenu();
        itemMenuCopierSelection = new javax.swing.JMenuItem(actionCopierFichiers)
        ;
        itemMenuDeplacerSelection = new javax.swing.JMenuItem(actionDeplacerFichiers)
        ;
        itemMenuCopierFiltre = new javax.swing.JMenuItem(actionCopierFichiersFiltre)
        ;
        itemMenuDeplacerFiltre = new javax.swing.JMenuItem(actionDeplacerFichiersFiltre)
        ;
        jScrollPane1 = new javax.swing.JScrollPane();
        arbreFichiers = new javax.swing.JTree();
        descrEtat = new javax.swing.JLabel();
        btnAjoutFichiers = new javax.swing.JButton(actionAjouterFichiers);
        btnRetirerFichiers = new javax.swing.JButton(actionRetirerFichiers);
        btnDebutAnalyse = new javax.swing.JButton(actionDemarrerAnalyse);
        btnInterruptAnalyse = new javax.swing.JButton(actionInterrompreAnalyse);
        btnNouvelleAnalyse = new javax.swing.JButton(actionNouvelleAnalyse)
        ;
        btnFermerAnalyse = new javax.swing.JButton(actionFermerAnalyse)
        ;
        progressBarAnalyse = new JProgressBar (0,10000);

        itemMenuAjouter.setText(messages.getString("Add_Files"));
        itemMenuAjouter.setName("Ajouter"); // NOI18N
        menuContextuel.add(itemMenuAjouter);

        itemMenuSupprimer.setText(messages.getString("Delete"));
        itemMenuSupprimer.setName("Supprimer"); // NOI18N
        menuContextuel.add(itemMenuSupprimer);
        menuContextuel.add(separateurMenu);

        itemMenuExclure.setText(messages.getString("exclude_files"));
        menuContextuel.add(itemMenuExclure);

        itemMenuLancer.setText(messages.getString("Start_Analysis"));
        itemMenuLancer.setName("Lancer l'analyse"); // NOI18N
        menuContextuel.add(itemMenuLancer);

        menuSources.setText(messages.getString("sources"));

        itemMenuAjouterSource.setText(messages.getString("Add_Distinguished_Folder")); // NOI18N
        menuSources.add(itemMenuAjouterSource);

        itemMenuSupprimerSource.setText(messages.getString("Remove_Distinguished_Folder")); // NOI18N
        menuSources.add(itemMenuSupprimerSource);
        menuSources.add(separateurMenuSources);

        itemMenuAjouterSousDossiersSources.setText(messages.getString("Add_Distinguished_Subfolder")); // NOI18N
        menuSources.add(itemMenuAjouterSousDossiersSources);

        itemMenuRetirerSousDossiersSources.setText(messages.getString("Remove_Distinguished_Subfolders")); // NOI18N
        menuSources.add(itemMenuRetirerSousDossiersSources);

        menuContextuel.add(menuSources);

        menuDéplacementFichiers.setText(messages.getString("copy_or_move_files"));

        itemMenuCopierSelection.setText(messages.getString("copy_selected_files"));
        menuDéplacementFichiers.add(itemMenuCopierSelection);

        itemMenuDeplacerSelection.setText(messages.getString("move_selected_files"));
        menuDéplacementFichiers.add(itemMenuDeplacerSelection);

        itemMenuCopierFiltre.setText(messages.getString("copy_multiple_files"));
        menuDéplacementFichiers.add(itemMenuCopierFiltre);

        itemMenuDeplacerFiltre.setText(messages.getString("move_multiple_files"));
        menuDéplacementFichiers.add(itemMenuDeplacerFiltre);

        menuContextuel.add(menuDéplacementFichiers);

        jScrollPane1.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        jScrollPane1.setMinimumSize(new java.awt.Dimension(23, 170));

        arbreFichiers.setModel(new DefaultTreeModel(structureFichiers));
        arbreFichiers.setCellRenderer(new TreeCellCustomRenderer());
        arbreFichiers.setMinimumSize(new java.awt.Dimension(0, 200));
        arbreFichiers.setCellRenderer(new ca.qc.bdeb.baldr.ihm.renderers.TreeCellCustomRenderer());
        arbreFichiers.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                arbreFichiersMouseClicked(evt);
            }
        });
        arbreFichiers.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                arbreFichiersKeyPressed(evt);
            }
        });
        jScrollPane1.setViewportView(arbreFichiers);

        descrEtat.setText(messages.getString("Analysis_Ready"));

        btnAjoutFichiers.setToolTipText(messages.getString("Add_Files"));

        btnRetirerFichiers.setToolTipText(messages.getString("Delete_Selected_File"));
        btnRetirerFichiers.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnRetirerFichiersActionPerformed(evt);
            }
        });

        btnDebutAnalyse.setToolTipText(messages.getString("Start_Analysis"));
        btnDebutAnalyse.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnDebutAnalyseActionPerformed(evt);
            }
        });

        btnInterruptAnalyse.setToolTipText(messages.getString("Stop_Analysis"));

        btnNouvelleAnalyse.setToolTipText(messages.getString("New_Analysis"));

        btnFermerAnalyse.setToolTipText(messages.getString("Close_Tab"));

        progressBarAnalyse.setRequestFocusEnabled(false);
        progressBarAnalyse.setString("43%");

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(progressBarAnalyse, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(descrEtat, javax.swing.GroupLayout.PREFERRED_SIZE, 388, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(0, 33, Short.MAX_VALUE))
                    .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(btnAjoutFichiers, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(btnRetirerFichiers, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(btnDebutAnalyse, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(btnInterruptAnalyse, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(btnNouvelleAnalyse, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(btnFermerAnalyse, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 371, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(btnRetirerFichiers, javax.swing.GroupLayout.PREFERRED_SIZE, 26, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnAjoutFichiers, javax.swing.GroupLayout.PREFERRED_SIZE, 26, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnDebutAnalyse, javax.swing.GroupLayout.PREFERRED_SIZE, 26, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnInterruptAnalyse, javax.swing.GroupLayout.PREFERRED_SIZE, 26, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnNouvelleAnalyse, javax.swing.GroupLayout.PREFERRED_SIZE, 26, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnFermerAnalyse, javax.swing.GroupLayout.PREFERRED_SIZE, 26, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(descrEtat)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(progressBarAnalyse, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );

        getAccessibleContext().setAccessibleParent(this);
    }// </editor-fold>//GEN-END:initComponents
    
    /**
     * Sélectionne les noeuds dont le nom du fichier correspond à un filtre.
     *
     * @param filtre
     */
    private void selectionnerNoeudsSelonFiltre(String filtre) {
        List<Integer> selectList = new ArrayList();
        int nbRows = arbreFichiers.getRowCount();

        for (int i = 0; i < nbRows; i++) {
            Object noeud = ((DefaultMutableTreeNode) arbreFichiers
                    .getPathForRow(i).getLastPathComponent()).getUserObject();

            if (noeud instanceof FileTreeElement) {
                String nomFichier = ((FileTreeElement) noeud).toString();

                if (Correspondance.stringMatchesPattern(((FileTreeElement) noeud).fichier, filtre)) {
                    selectList.add(i);
                }
            }
        }

        arbreFichiers.setSelectionRows(ArrayUtil.toIntArray(
                (ArrayList<Integer>) selectList));
    }

    private Task getTask() {
        return ongletParent.getTask();
    }

    private void arbreFichiersKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_arbreFichiersKeyPressed
        //in the jTree, key Delete can remove selected files
        //and the enter key will open the favorite text editor for the current selected file
        if (!ongletParent.analyseEnCours()
                && evt.getKeyCode() == java.awt.event.KeyEvent.VK_DELETE
                && ongletParent.getTask().estSommaire() == false) {
            //&& !ongletParent.getTask().getTitre().equals(messages.getString("Summary"))
            retirerFichiersSelectionnes();

        } else if (evt.getKeyCode() == java.awt.event.KeyEvent.VK_ENTER) {
            List<File> fcs = getTreeSelectedFiles();
            if (fcs != null && fcs.size() > 0) {
                openEditor(fcs);
            }
        }
    }//GEN-LAST:event_arbreFichiersKeyPressed
    private void GestionArbreMouseClick(MouseEvent evt) {
        if (arbreFichiers.getRowCount() > 1) {
            itemMenuCopierFiltre.setVisible(true);
            itemMenuDeplacerFiltre.setVisible(true);
            menuDéplacementFichiers.setVisible(true);
        } else {
            itemMenuCopierFiltre.setVisible(false);
            itemMenuDeplacerFiltre.setVisible(false);
            menuDéplacementFichiers.setVisible(false);
        }
        FileTreeElement noeudSelectionne
                = getFirstSelectedFileTreeElement();
        if (noeudSelectionne != null
                && noeudSelectionne.isDirectory()
                && arbreFichiers.getSelectionCount() == 1) {

            menuSources.setVisible(true);

            itemMenuAjouterSource.setVisible(
                    !noeudSelectionne.isSource());
            itemMenuSupprimerSource.setVisible(
                    noeudSelectionne.isSource());

            boolean hasDirs = noeudSelectionne.hasSubdirectories();
            separateurMenuSources.setVisible(hasDirs);
            itemMenuAjouterSousDossiersSources.setVisible(hasDirs);
            itemMenuRetirerSousDossiersSources.setVisible(hasDirs);
        } else {
            menuSources.setVisible(false);
        }

        if (getFichiersSelectionnes().size() > 0) {
            itemMenuCopierSelection.setVisible(true);
            itemMenuDeplacerSelection.setVisible(true);
        } else {
            itemMenuCopierSelection.setVisible(false);
            itemMenuDeplacerSelection.setVisible(false);
        }

        menuContextuel.show(evt.getComponent(), evt.getX(), evt.getY());
    }

    private void arbreFichiersMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_arbreFichiersMouseClicked
        if (!ongletParent.estSommaire()) {
            if (evt.getButton() == MouseEvent.BUTTON2
                    || evt.getButton() == MouseEvent.BUTTON3) {

                GestionArbreMouseClick(evt);
            } else if (evt.getButton() == MouseEvent.BUTTON1) {
                if (evt.getClickCount() > 1) {
                    List<File> fs = getTreeSelectedFiles();

                    if (fs != null && fs.size() > 0) {
                        openEditor(fs);
                    }
                }
            }
        }
        if(arbreFichiers.getSelectionPaths() != null){
            actionRetirerFichiers.setEnabled(true);
        }
    }//GEN-LAST:event_arbreFichiersMouseClicked

    private void btnRetirerFichiersActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnRetirerFichiersActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_btnRetirerFichiersActionPerformed

    private void btnDebutAnalyseActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnDebutAnalyseActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_btnDebutAnalyseActionPerformed

    /**
     * Retourne une liste des fichiers sélectionnés
     *
     * @return la liste des fichiers sélectionnés
     */
    private ArrayList<File> getFichiersSelectionnes() {
        ArrayList<File> fichiersARetourner = new ArrayList<>();
        if (arbreFichiers.getSelectionPaths() != null) {
            for (TreePath cheminSelectionne : arbreFichiers.getSelectionPaths()) {
                DefaultMutableTreeNode noeud = (DefaultMutableTreeNode) cheminSelectionne.getLastPathComponent();
                if (noeud != null) {
                    ArrayList<File> sousFichiers = new ArrayList<>();
                    ajouterFichiersRecursif(noeud, sousFichiers);
                    fichiersARetourner.addAll(sousFichiers);
                }
            }
        }
        return fichiersARetourner;
    }

    /**
     * Ajoute à <code>listeARemplir</code> tous les sous-fichiers appartenant à
     * <code>noeud</code>, et lui-même s'il n'est pas un dossier. S'appelle
     * récursivement pour traiter chaque sous-fichier.
     *
     * @param noeud le noeud évalué
     * @param listeARemplir la liste de fichiers qui sera remplie au fur et à
     * mesure
     */
    private void ajouterFichiersRecursif(DefaultMutableTreeNode noeud, ArrayList<File> listeARemplir) {
        //Horrible instanceof, empêche un crash lorsqu'on clique sur le noeud de départ "Documents", 
        //qui n'est pas lié à un fichier.
        if (noeud.getUserObject() instanceof FileTreeElement) {

            FileTreeElement noeudFichier = (FileTreeElement) noeud.getUserObject();
            if (noeudFichier != null && !noeudFichier.isDirectory()) {
                if (!listeARemplir.contains(noeudFichier.fichier)) {
                    listeARemplir.add(noeudFichier.fichier);
                }
            } else {
                int nombreEnfants = noeud.getChildCount();
                for (int i = 0; i < nombreEnfants; ++i) {
                    DefaultMutableTreeNode noeudEnfant = (DefaultMutableTreeNode) noeud.getChildAt(i);
                    if (noeudEnfant != null) {
                        ajouterFichiersRecursif(noeudEnfant, listeARemplir);
                    }
                }
            }
        }
    }

    /**
     * Permet à l'utilisateur de choisir une analyse, ou d'en créer une
     * nouvelle, et la retourne
     *
     * @return l'analyse choisie, ou null si l'utilisateur annule
     */
    private Task demanderChoixAnalyse() {
        List<Task> taches = projetCourant.getTasks();
        //Il est inutile de pouvoir déplacer un fichier dans la même analyse
        taches.remove(getTask());
        String[] titresTaches = new String[taches.size() + 1];
        //On ajoute le nom de chaque analyse
        for (int i = 0; i < titresTaches.length - 1; ++i) {
            titresTaches[i] = taches.get(i).getTitre();
        }
        //On ajoute le choix de créer une nouvelle analyse
        titresTaches[titresTaches.length - 1] = messages.getString("new_analysis...");
        String titreSelectionne = (String) JOptionPane.showInputDialog(
                getTopLevelAncestor(),
                messages.getString("analysis_choice"),
                messages.getString("select_analysis"),
                JOptionPane.QUESTION_MESSAGE,
                null,
                titresTaches,
                titresTaches[0]
        );
        int indexSelectionne = -1;
        //On trouve l'analyse choisie
        for (int i = 0; i < titresTaches.length; ++i) {
            if (titresTaches[i].equals(titreSelectionne)) {
                indexSelectionne = i;
            }
        }
        if (indexSelectionne > -1) {
            if (indexSelectionne == titresTaches.length - 1) {
                return creerNouvelleAnalyse();
            } else {
                return taches.get(indexSelectionne);
            }
        }
        return null;
    }

    /**
     * Permet à l'utilisateur d'entrer un nom et de créer une nouvelle analyse
     *
     * @return la nouvelle analyse
     */
    private Task creerNouvelleAnalyse() {
        String nomAnalyse = (String) JOptionPane.showInputDialog(
                this.getTopLevelAncestor(),
                messages.getString("enter_new_analysis_name"),
                messages.getString("new_analysis"),
                JOptionPane.QUESTION_MESSAGE,
                null,
                null,
                messages.getString("new_analysis"));
        if (nomAnalyse != null) {
            return win.ajouterNouvelleAnalyse(nomAnalyse);
        }
        return null;
    }

    /**
     * Demande à l'utilisateur d'entrer un filtre (wildcard), et le retourne
     *
     * @return le filtre entré par l'utilisateur
     */
    private String demanderWildcard() {
        String wildcard = (String) JOptionPane.showInputDialog(
                this.getTopLevelAncestor(),
                messages.getString("enter_filter"),
                messages.getString("select_files"),
                JOptionPane.QUESTION_MESSAGE,
                null,
                null,
                "*.*");
        return wildcard;
    }

    /**
     * Ajoute plusieurs fichiers à cet arbre, les ajoute à l'analyse et met à
     * jour l'interface graphique pour refléter les changements
     *
     * @param fichiers les fichiers à ajouter
     */
    public void ajouterFichiersEtMettreAJourArbre(List<File> fichiers) {
        File[] tabFichiers = new File[fichiers.size()];
        tabFichiers = fichiers.toArray(tabFichiers);
        ajouterFichiersSurNoeud(tabFichiers, trouverRepertoireParent());
        setFileListFromTask(getTask());
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JTree arbreFichiers;
    private javax.swing.JButton btnAjoutFichiers;
    private javax.swing.JButton btnDebutAnalyse;
    private javax.swing.JButton btnFermerAnalyse;
    private javax.swing.JButton btnInterruptAnalyse;
    private javax.swing.JButton btnNouvelleAnalyse;
    private javax.swing.JButton btnRetirerFichiers;
    private javax.swing.JLabel descrEtat;
    private javax.swing.JMenuItem itemMenuAjouter;
    private javax.swing.JMenuItem itemMenuAjouterSource;
    private javax.swing.JMenuItem itemMenuAjouterSousDossiersSources;
    private javax.swing.JMenuItem itemMenuCopierFiltre;
    private javax.swing.JMenuItem itemMenuCopierSelection;
    private javax.swing.JMenuItem itemMenuDeplacerFiltre;
    private javax.swing.JMenuItem itemMenuDeplacerSelection;
    private javax.swing.JMenuItem itemMenuExclure;
    private javax.swing.JMenuItem itemMenuLancer;
    private javax.swing.JMenuItem itemMenuRetirerSousDossiersSources;
    private javax.swing.JMenuItem itemMenuSupprimer;
    private javax.swing.JMenuItem itemMenuSupprimerSource;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JPopupMenu menuContextuel;
    private javax.swing.JMenu menuDéplacementFichiers;
    private javax.swing.JMenu menuSources;
    private javax.swing.JProgressBar progressBarAnalyse;
    private javax.swing.JPopupMenu.Separator separateurMenu;
    private javax.swing.JPopupMenu.Separator separateurMenuSources;
    // End of variables declaration//GEN-END:variables

    private FileTreeElement getFirstSelectedFileTreeElement() {
        DefaultMutableTreeNode noeudSelectionne = getFirstSelectedNode();

        if (noeudSelectionne != null
                && noeudSelectionne.getUserObject() instanceof FileTreeElement) {
            return (FileTreeElement) noeudSelectionne.getUserObject();
        }

        return null;
    }

    private DefaultMutableTreeNode getFirstSelectedNode() {
        TreePath premierSelectionne = arbreFichiers.getSelectionPath();

        if (premierSelectionne != null) {
            return (DefaultMutableTreeNode) premierSelectionne.getLastPathComponent();
        }

        return null;
    }

    private void mettreAJourBarreProgression() {
        float state = getTask().getStateCount();
        int maximum;
        maximum = progressBarAnalyse.getMaximum();
        try {
            progressBarAnalyse.setValue(Math.round(state * maximum));
        } catch (Exception e) {
        }
    }

    public JTree getArbreFichiers() {
        return arbreFichiers;
    }

    @Override
    public void changementEtat() {
    }

    @Override
    public void changementEtat(Enum<?> property, Object o) {
        switch ((Observation) property) {
            case PROGRESS:
                descrEtat.setText(messages.getString("Analysis_In_Progress"));
                mettreAJourBarreProgression();
                break;
            case DEL_IN_TABLE:
                setFileListFromTask((Task) o);
                break;
            case ANALYSE_TERMINEE:
                dernierTempsAnalyseString = ongletParent.getTempsAnalyse();
                etatAnalyse = EtatAnalyse.TERMINE;
                descrEtat.setText(messages.getString("Analysis_Done")
                        + " (" + dernierTempsAnalyseString + ")");
                mettreAJourBarreProgression();
                setEtatBoutonsPeutDebuter(true);
                setEtatMenuContextuel(true);
                actionAjouterFichiers.setEnabled(true);
                actionRetirerFichiers.setEnabled(false);
                btnDebutAnalyse.setEnabled(false);
                break;
        }
    }

    /**
     * Active/désactive les boutons «Démarrer l'analyse» et «Interrompre
     * l'analyse».
     *
     * @param etat État du bouton «Démarrer». «Interrompre» est à {@code !etat}.
     */
    public void setEtatBoutonsPeutDebuter(boolean etat) {
        actionDemarrerAnalyse.setEnabled(etat);
        actionInterrompreAnalyse.setEnabled(!etat);
    }
    
    /**
     * Active/désactive les éléments du menu contextuel
     * 
     * @param etat État des éléments du menu
     */
    public void setEtatMenuContextuel(boolean etat) {
        for (AbstractAction action : actionsMenuContextuel) {
            action.setEnabled(etat);
        }
    }

    /**
     * S'assure que le premier noeud de l'arbre de fichiers est déplié.
     */
    public void expandFirstTreeRow() {
        arbreFichiers.expandRow(0);
    }

    private PanelTab getTabSommaire() {
        List<PanelTab> listeOnglets = win.getListeOnglets();
        for (PanelTab onglet : listeOnglets) {
            if (onglet.estSommaire()) {
                return onglet;
            }
        }
        return null;
    }

    public void mettreAJourUI() {

        arbreFichiers.updateUI();

        if (getTask().getStateCount() >= 0) {
            mettreAJourMatriceAvecNouveauFichiers();
        }
    }

    private void expandAllTreeRows() {
        for (int i = 0; i < arbreFichiers.getRowCount(); ++i) {
            arbreFichiers.expandRow(i);
        }
    }

    @Override
    public void update(Observable o, Object arg) {
        messages = (ResourceBundle) arg;
        itemMenuAjouter.setText(messages.getString("Add_Files"));
        itemMenuSupprimer.setText(messages.getString("Delete"));
        itemMenuExclure.setText(messages.getString("exclude_files"));
        itemMenuLancer.setText(messages.getString("Start_Analysis"));
        itemMenuAjouterSource.setText(messages.getString("Add_Distinguished_Folder")); // NOI18N
        itemMenuSupprimerSource.setText(messages.getString("Remove_Distinguished_Folder")); // NOI18N
        itemMenuAjouterSousDossiersSources.setText(messages.getString("Add_Distinguished_Subfolder")); // NOI18N
        itemMenuRetirerSousDossiersSources.setText(messages.getString("Remove_Distinguished_Subfolders")); // NOI18N
        menuDéplacementFichiers.setText(messages.getString("copy_or_move_files"));
        itemMenuCopierSelection.setText(messages.getString("copy_selected_files"));
        itemMenuDeplacerSelection.setText(messages.getString("move_selected_files"));
        itemMenuCopierFiltre.setText(messages.getString("copy_multiple_files"));
        itemMenuDeplacerFiltre.setText(messages.getString("move_multiple_files"));
        btnAjoutFichiers.setToolTipText(messages.getString("Add_Files"));
        btnRetirerFichiers.setToolTipText(messages.getString("Delete_Selected_File"));
        btnDebutAnalyse.setToolTipText(messages.getString("Start_Analysis"));
        btnInterruptAnalyse.setToolTipText(messages.getString("Stop_Analysis"));
        btnNouvelleAnalyse.setToolTipText(messages.getString("New_Analysis"));
        btnFermerAnalyse.setToolTipText(messages.getString("Close_Tab"));
        switch (etatAnalyse) {
            case PRET:
                descrEtat.setText(messages.getString("Analysis_Ready"));
                break;
            case TERMINE:
                descrEtat.setText(messages.getString("Analysis_Done")
                        + " (" + dernierTempsAnalyseString + ")");
                break;
            case ANNULATION:
                descrEtat.setText(messages.getString("Analysis_Canceled"));
                break;
        }

    }
    
    private PanelResults trouverPanelResult(WindowBaldr win) {
        return trouverPanelResultRecursif(win);
    }

    private PanelResults trouverPanelResultRecursif(Container contenant) {
        if (contenant instanceof PanelResults) {
            return (PanelResults) contenant;
        } else {
            for (Component composante : contenant.getComponents()) {
                if (composante instanceof Container) {
                    PanelResults panneauTrouve = trouverPanelResultRecursif((Container) composante);
                    if (panneauTrouve != null) {
                        return panneauTrouve;
                    }
                }
            }
            
            }
        return null;
        }
        
}
