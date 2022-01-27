package ca.qc.bdeb.baldr.ihm;

import ca.qc.bdeb.baldr.formattage.ExtrairePDF;
import ca.qc.bdeb.baldr.utils.Observation;
import ca.qc.bdeb.baldr.ihm.ListViewTableModel.Entree;
import static ca.qc.bdeb.baldr.ihm.PanelFiles.win;
import ca.qc.bdeb.baldr.ihm.renderers.ListViewCellCustomRenderer;
import ca.qc.bdeb.baldr.ihm.renderers.TableViewCellCustomRenderer;
import ca.qc.bdeb.baldr.ihm.renderers.TableHeaderCellCustomRenderer;
import ca.qc.bdeb.baldr.noyau.*;
import java.awt.Component;

import java.awt.FlowLayout;
import java.awt.event.ItemEvent;
import java.awt.event.KeyEvent;
import java.io.BufferedReader;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Observable;
import java.util.Observer;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JOptionPane;
import javax.swing.JSpinner;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.table.TableModel;
import javax.swing.table.TableRowSorter;
import javax.swing.RowFilter;
import javax.swing.text.*;
import javax.swing.tree.TreePath;

/**
 *
 * @author Carl
 */
public class PanelResults extends javax.swing.JPanel implements Observer {

    private Task analys;
    private ResourceBundle messages;
    private int spinnerValue;
    private Noyau noyau = null;
    private PanelTab parent;
    private GestionnairePreferences preferences;
    private GestionnaireI18N gestI18N;
    private List<File> fichiersResultats;
    private List<File> fichierCacher;
    private Projet projetCourant;

    /**
     * Vrai si les résultats s'affichent sous forme de tableau, faux sinon.
     */
    private boolean vueTableau;

    public PanelResults(PanelTab parent, Noyau noyau, GestionnaireI18N gestI18N) {
        this.parent = parent;
        this.noyau = noyau;
        projetCourant = noyau.getProjetCourant();
        this.preferences = noyau.getPrefs();

        messages = ResourceBundle.getBundle("i18n/Baldr");
        initComponents();
        String prefDecimal = preferences.readPref("DECIMAL");
        if (prefDecimal.equals("")) {
            prefDecimal = "1";
            preferences.writePref("DECIMAL", prefDecimal);
        }

        initialiserSelecteurDecimal();
        this.selecteurDecimal.setValue(Integer.parseInt(prefDecimal));
        this.spinnerValue = (int) selecteurDecimal.getValue();

        tableResultats.addMouseListener(new ResTableMouseAdapter(tableResultats, this.noyau));

        tableResultats.getTableHeader().setReorderingAllowed(false);

        pnlItemsHaut.setLayout(new FlowLayout(FlowLayout.LEFT));
        this.gestI18N = gestI18N;
        gestI18N.addObserver(this);

        if (this.preferences.prefExists("VUE_TABLEAU")) {
            vueTableau = (Boolean) this.preferences.readPref("VUE_TABLEAU", false);
        } else {
            vueTableau = true;
        }
        noyau.getPrefs().writePref("VUE_COURAN", vueTableau);

        if (vueTableau) {
            cboVue.setSelectedIndex(0);
        } else {
            cboVue.setSelectedIndex(1);
        }
        ChangementTailleTableau((int) selecteurDecimal.getValue());
    }

    public ResourceBundle getMessages() {
        return messages;
    }

    public void setTableauVue(boolean _tableauVue) {
        vueTableau = _tableauVue;
        if (vueTableau) {
            cboVue.setSelectedIndex(0);
        } else {
            cboVue.setSelectedIndex(1);
        }
    }

    private void initialiserSelecteurDecimal() {

        JSpinner.NumberEditor jsEditor = (JSpinner.NumberEditor) selecteurDecimal.getEditor();

        final Document jsDoc = jsEditor.getTextField().getDocument();

        if (jsDoc instanceof PlainDocument) {
            AbstractDocument doc = new PlainDocument() {

                private static final long serialVersionUID = 1L;

                @Override
                public void setDocumentFilter(DocumentFilter filter) {
                    if (filter instanceof MyDocumentFilter) {
                        super.setDocumentFilter(filter);
                    }
                }
            };
            doc.setDocumentFilter(new MyDocumentFilter());
            jsEditor.getTextField().setDocument(doc);
        }
        selecteurDecimal.setValue(0);

    }

    public Task getAnalys() {
        return analys;
    }

    private void SelectionnerFichier() throws FileNotFoundException, IOException {
        String nomFichier1 = "";
        String nomFichier2 = "";
        String texteFichier1 = "";
        String texteFichier2 = "";

        ExtrairePDF extractor = new ExtrairePDF();
        if (vueTableau) { //rajout pour separr la vue tableau de la vue liste

            //LIGNE
            //DONNEES DU FICHIER DE LA LIGNE
            int[] tab = tableResultats.getSelectedRows();
            String[] nomFichier = new String[tableResultats.getSelectedRowCount()];
            for (int i = 0; i < tab.length; i++) {
                nomFichier[i] = fichiersResultats.get(tab[i]).getName();
            }

            TreePath[] path = new TreePath[tableResultats.getSelectedRowCount()];;

            //System.out.println(nomFichier[0] + "******************");
            for (int i = 0; i < nomFichier.length; i++) {
                path[i] = parent.getPanelFile().getArbreFichiers().getNextMatch(nomFichier[i], 0, Position.Bias.Forward);
            }
            nomFichier1 = nomFichier[0];
            //SORTIE = FICHIER DE LA LIGNE
            if (!nomFichier[0].endsWith("pdf")) {//si le fichier n'est pas un pdf
                texteFichier1 = extraireTexte(new FileReader(fichiersResultats.get(tab[0])));
            } else {
                texteFichier1 = extractor.ExtrairePDF(fichiersResultats.get(tab[0]));
            }
            //COLONNE
            //DONNÉES DU FICHIER LE COLONNE
            tab = tableResultats.getSelectedColumns();
            nomFichier = new String[tableResultats.getSelectedRowCount()];
            for (int i = 0; i < tab.length; i++) {
                try {
                    nomFichier[i] = fichiersResultats.get(tab[i] - 1).getName();            
                } catch (ArrayIndexOutOfBoundsException e) {
                }
            }
            nomFichier2 = nomFichier[0];

            //SORTIE = FICHIER DE LA COLONNE
            try {
                if (!nomFichier[0].endsWith("pdf")) {//si le fichier n'est pas un pdf
                    texteFichier2 = extraireTexte(new FileReader(fichiersResultats.get(tab[0] - 1)));
                } else {
                    texteFichier2 = extractor.ExtrairePDF(fichiersResultats.get(tab[0] - 1));
                }
                panelContentFile1.setGauche(nomFichier[0], texteFichier2);
            } catch (NullPointerException e) {
            }
            //System.out.println(path[0].toString() + "******************"); //Pour voir le nom du fichier
            parent.getPanelFile().getArbreFichiers().setSelectionPaths(path);
            
        } else {
            String fichier1 = (String) tableResultats.getValueAt(tableResultats.getSelectedRow(), 0);
            String fichier2 = (String) tableResultats.getValueAt(tableResultats.getSelectedRow(), 2);

            File[] ancetresCommuns = analys.getCommonAncestors();
            String debutChemin = ancetresCommuns[0].getAbsolutePath() + File.separator;

            //AFFICHER FICHIER 1
            File file1 = new File(debutChemin + fichier1);
            if (!fichier1.endsWith("pdf")) {//si le fichier n'est pas un pdf
                texteFichier1 = extraireTexte(new FileReader(file1));
            } else {//si le fichier est un pdf
                texteFichier1 = extractor.ExtrairePDF(file1);
            }
            nomFichier1 = file1.getName();
            //AFFICHER FICHIER 2
            File file2 = new File(debutChemin + fichier2);
            if (!fichier2.endsWith("pdf")) {//si le fichier n'est pas un pdf
                texteFichier2 = extraireTexte(new FileReader(file2));
            } else {//si le fichier est un pdf
                texteFichier2 = extractor.ExtrairePDF(file2);
            }
            nomFichier2 = file2.getName();

        }
        panelContentFile1.setDroite(nomFichier1, texteFichier1);
        panelContentFile1.setGauche(nomFichier2, texteFichier2);

    }

    private String extraireTexte(FileReader file) throws IOException {
        BufferedReader in = new BufferedReader(file);
        String line, texte = "";
        while ((line = in.readLine()) != null) {
            // Afficher le contenu du fichier
            texte += line + "\n";
        }
        in.close();
        return texte;
    }

    private static class MyDocumentFilter extends DocumentFilter {

        @Override
        public void insertString(FilterBypass fb, int offset, String valeurDecimal, AttributeSet attr) throws BadLocationException {
            if (stringContainsOnlyDigits(valeurDecimal)) {
                super.insertString(fb, offset, valeurDecimal, attr);
            }
        }

        @Override
        public void remove(FilterBypass fb, int offset, int length) throws BadLocationException {
            super.remove(fb, offset, length);
        }

        @Override
        public void replace(FilterBypass fb, int offset, int length, String text, AttributeSet attrs) throws BadLocationException {
            if (stringContainsOnlyDigits(text)) {
                super.replace(fb, offset, length, text, attrs);
            }
        }

        private boolean stringContainsOnlyDigits(String valeurDecimal) {
            for (int i = 0; i < valeurDecimal.length(); i++) {
                if (!Character.isDigit(valeurDecimal.charAt(i))) {
                    return false;
                }
            }
            return true;
        }
    }

    /**
     * Destroy the node and their children which are selected in the jTree
     */
    public void regenererResultats() {

        if (analys.getResults() == null
                || analys.getResults().getValues() == null
                || analys.getResults().getValues().length == 0) {
            tableResultats.setModel(new DefaultTableModel());

            return;
        }

        if (!tableResultats.getAutoCreateColumnsFromModel()) {
            tableResultats.setAutoCreateColumnsFromModel(true);
        }

        if (vueTableau) {
            creerTabResultats();
        } else {
            creerListeResultats();
        }

        tableResultats.setRowSelectionAllowed(true);

        ChangementTailleTableau((int) selecteurDecimal.getValue());
        if (tableResultats.getRowCount() <= 0 || !analys.verifierAnalyseFaite()) {
            DefaultTableModel nouveauTable = new DefaultTableModel();
            tableResultats.setModel(nouveauTable);
        }
    }

    private void creerTabResultats() {

        mnuFilterExclude.setVisible(vueTableau);
        tableResultats.setRowSorter(null);

        tableResultats.setRowSelectionAllowed(true);

        Tri.Type typeTrie = ((Tri.TypeI18N) cboOrdre.getSelectedItem()).getType();
        MatriceTriangulaire resultats = analys.getResults();
        fichiersResultats = analys.getFichiersResultats();

        Task.trier(fichiersResultats, resultats, typeTrie);

        TableModel modeleTableau = new BaldrTableModel(fichiersResultats, resultats.getValues(), getMessages());
        tableResultats.setModel(modeleTableau);

        TableCellRenderer columnHeaderRenderer = new TableHeaderCellCustomRenderer();

        float[] minMax = resultats.getMinMaxValues();
        TableCellRenderer individualCellRenderer = new TableViewCellCustomRenderer(
                minMax[0], minMax[1], spinnerValue, analys,
                fichiersResultats, noyau.getPrefs());

        TableColumn colonne;
        for (int i = 0; i <= fichiersResultats.size(); i++) {
            colonne = tableResultats.getColumnModel().getColumn(i);

            colonne.setHeaderRenderer(columnHeaderRenderer);

            // Première colonne : même rendu que les en-têtes
            if (i == 0) {
                colonne.setCellRenderer(columnHeaderRenderer);
            } else {
                colonne.setCellRenderer(individualCellRenderer);
            }
        }
    }

    /**
     * Crée une liste de résultats triée en ordre croissant de valeurs.
     */
    private void creerListeResultats() {
        List<Entree> listeEntrees = new ArrayList();

        mnuFilterExclude.setVisible(vueTableau);

        ListViewTableModel modeleTableau = new ListViewTableModel(messages);
        tableResultats.setModel(modeleTableau);

        float[] minMax = analys.getResults().getMinMaxValues();
        tableResultats.getColumnModel().getColumn(2)
                .setCellRenderer(new ListViewCellCustomRenderer(analys, minMax[0],
                        minMax[1], spinnerValue, noyau.getPrefs()));

        List<File> tabFichiers = analys.getFichiersResultats();
        File[] ancetresCommuns = analys.getCommonAncestors();
        String cheminAncetreCommun;
        if (ancetresCommuns.length == 1) {
            cheminAncetreCommun = ancetresCommuns[0].getAbsolutePath() + File.separator;
        } else {
            cheminAncetreCommun = "";
        }

        for (int i = 0; i < tabFichiers.size(); i++) {
            for (int j = i + 1; j < tabFichiers.size(); j++) {
                if (analys.getResults().getRes(i, j) != 0) {
                    listeEntrees.add(modeleTableau.new Entree(
                            tabFichiers.get(i), tabFichiers.get(j), cheminAncetreCommun,
                            analys.getResults().getRes(i, j)
                    ));

                }
            }
        }

        Collections.sort(listeEntrees);

        for (Entree e : listeEntrees) {
            modeleTableau.addResult(e);
        }
        tableResultats.moveColumn(1, 2);

        tableResultats.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);

        for (int colonne = 0; colonne < tableResultats.getColumnCount(); colonne++) {
            TableColumn tableColonne = tableResultats.getColumnModel().getColumn(colonne);
            int taille_ideal = tableColonne.getMinWidth();
            int taille_Max = tableColonne.getMaxWidth();
            for (int range = 0; range < tableResultats.getRowCount(); range++) {
                TableCellRenderer cellule = tableResultats.getCellRenderer(range, colonne);
                Component comp = tableResultats.prepareRenderer(cellule, range, colonne);
                int taille = comp.getPreferredSize().width + tableResultats.getIntercellSpacing().width;
                taille_ideal = Math.max(taille_ideal, taille);

                if (taille_ideal >= taille_Max) {
                    taille_ideal = taille_Max;
                    break;
                }
            }

            tableColonne.setMinWidth(taille_ideal);
        }
    }

    public void updateMat(Task analys) {
        this.analys = analys;

        regenererResultats();
    }

    private Tri.TypeI18N[] getTableauTypes(Tri.Type[] types) {
        Tri.TypeI18N[] tableauTypes = new Tri.TypeI18N[types.length];
        for (int i = 0; i < tableauTypes.length; ++i) {
            tableauTypes[i] = new Tri.TypeI18N(types[i], messages);
        }
        return tableauTypes;
    }

    private void retirerFichiersTableauParFiltre(float filtre) {
        regenererResultats();
        List<Integer> indexARetirer = new ArrayList<>();
        if (vueTableau) {
            BaldrTableModel modele = (BaldrTableModel) tableResultats.getModel();
            int numRows = modele.getRowCount();
            int numCols = modele.getColumnCount();
            for (int row = 0; row < numRows; ++row) {
                boolean valide = false;
                for (int col = 1; col < numCols; ++col) {
                    float val = ((TableCell) modele.getValueAt(row, col)).value;
                    if (val < filtre && val != 0.0f) {
                        valide = true;
                        break;
                    }
                }
                if (!valide) {
                    indexARetirer.add(row);
                }
            }
            int numIndexRetires = 0;
            ArrayList<RowFilter<Object, Object>> filters = new ArrayList<>();
            for (int rangee : indexARetirer) {
                tableResultats.getColumnModel().removeColumn(tableResultats.getColumnModel().getColumn(rangee - numIndexRetires + 1));
                String regex = "^" + (rangee + 1) + " ";
                RowFilter<Object, Object> rowFilter = RowFilter.notFilter(RowFilter.regexFilter(regex));
                filters.add(rowFilter);
                ++numIndexRetires;
            }
            TableRowSorter<BaldrTableModel> rowSorter = new TableRowSorter<>(modele);
            rowSorter.setRowFilter(RowFilter.andFilter(filters));
            tableResultats.setRowSorter(rowSorter);
            tableResultats.repaint();
        }
    }

    @Override
    public void update(Observable o, Object arg) {
        messages = (ResourceBundle) arg;
        lblNbDecimal.setText(messages.getString("Number_Decimals")); // NOI18N
        lblVue.setText(messages.getString("View_Label")); // NOI18N
        lblOrdre.setText(messages.getString("Sort_By")); // NOI18N
        mnuFilterExclude.setText(messages.getString("filter_results"));
        mnuSuprimmer.setText(messages.getString("Delete"));
        int selectedIndex = cboVue.getSelectedIndex();
        cboVue.setModel(new DefaultComboBoxModel(new String[]{messages.getString("Table_View"), messages.getString("List_View")}));
        cboVue.setSelectedIndex(selectedIndex);
        selectedIndex = cboOrdre.getSelectedIndex();
        cboOrdre.setModel(new DefaultComboBoxModel(getTableauTypes(Tri.Type.values())));
        cboOrdre.setSelectedIndex(selectedIndex);
        if (tableResultats.getTableHeader().getColumnModel().getColumnCount() > 0) {
            if ((Boolean) preferences.readPref("VUE_TABLEAU", false)) {
                tableResultats.getTableHeader().getColumnModel().getColumn(0).setHeaderValue(messages.getString("Files"));
            } else {
                tableResultats.getTableHeader().getColumnModel().getColumn(0).setHeaderValue(messages.getString("File1"));
                tableResultats.getTableHeader().getColumnModel().getColumn(1).setHeaderValue(messages.getString("Result"));
                tableResultats.getTableHeader().getColumnModel().getColumn(2).setHeaderValue(messages.getString("File2"));
            }
            tableResultats.getTableHeader().repaint();
        }
        jMenuCacherFicher.setText(messages.getString("hide_results"));
        jRadioButtonMenuItem2.setText(messages.getString("fixed_color"));
        jRadioButtonMenuItem1.setText(messages.getString("progres_color"));
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
        mnuSuprimmer = new javax.swing.JMenuItem();
        mnuFilterExclude = new javax.swing.JMenuItem();
        jSeparator2 = new javax.swing.JPopupMenu.Separator();
        jMenuCacherFicher = new javax.swing.JMenuItem();
        jMenuRestaurerCaches = new javax.swing.JMenuItem();
        jSeparator1 = new javax.swing.JPopupMenu.Separator();
        jRadioButtonMenuItem1 = new javax.swing.JRadioButtonMenuItem();
        jRadioButtonMenuItem2 = new javax.swing.JRadioButtonMenuItem();
        buttonGroup1 = new javax.swing.ButtonGroup();
        srollPaneTableResults = new javax.swing.JScrollPane();
        tableResultats = new javax.swing.JTable();
        pnlItemsHaut = new javax.swing.JPanel();
        lblNbDecimal = new javax.swing.JLabel();
        selecteurDecimal = new javax.swing.JSpinner();
        filler3 = new javax.swing.Box.Filler(new java.awt.Dimension(15, 0), new java.awt.Dimension(15, 0), new java.awt.Dimension(15, 32767));
        lblVue = new javax.swing.JLabel();
        cboVue = new javax.swing.JComboBox();
        filler4 = new javax.swing.Box.Filler(new java.awt.Dimension(15, 0), new java.awt.Dimension(15, 0), new java.awt.Dimension(15, 32767));
        lblOrdre = new javax.swing.JLabel();
        cboOrdre = new javax.swing.JComboBox();
        panelContentFile1 = new ca.qc.bdeb.baldr.ihm.PanelContentFile();

        java.util.ResourceBundle bundle = java.util.ResourceBundle.getBundle("i18n/Baldr"); // NOI18N
        mnuSuprimmer.setText(bundle.getString("Delete")); // NOI18N
        mnuSuprimmer.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                mnuSuprimmerActionPerformed(evt);
            }
        });
        menuContextuel.add(mnuSuprimmer);

        mnuFilterExclude.setText(messages.getString("filter_results"));
        mnuFilterExclude.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                mnuFilterExcludeActionPerformed(evt);
            }
        });
        menuContextuel.add(mnuFilterExclude);
        menuContextuel.add(jSeparator2);

        jMenuCacherFicher.setText(messages.getString("hide_results"));
        jMenuCacherFicher.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuCacherFicherActionPerformed(evt);
            }
        });
        menuContextuel.add(jMenuCacherFicher);

        jMenuRestaurerCaches.setText(messages.getString("restore_hidden"));
        jMenuRestaurerCaches.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuRestaurerCachesActionPerformed(evt);
            }
        });
        menuContextuel.add(jMenuRestaurerCaches);
        menuContextuel.add(jSeparator1);

        buttonGroup1.add(jRadioButtonMenuItem1);
        jRadioButtonMenuItem1.setText(messages.getString("progres_color"));
        jRadioButtonMenuItem1.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                jRadioButtonMenuItem1ItemStateChanged(evt);
            }
        });
        menuContextuel.add(jRadioButtonMenuItem1);

        buttonGroup1.add(jRadioButtonMenuItem2);
        jRadioButtonMenuItem2.setText(messages.getString("fixed_color"));
        jRadioButtonMenuItem2.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                jRadioButtonMenuItem2ItemStateChanged(evt);
            }
        });
        menuContextuel.add(jRadioButtonMenuItem2);

        srollPaneTableResults.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));

        tableResultats.setAutoCreateColumnsFromModel(false);
        tableResultats.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null}
            },
            new String [] {
                "Title 1", "Title 2", "Title 3", "Title 4"
            }
        ));
        tableResultats.setAutoResizeMode(javax.swing.JTable.AUTO_RESIZE_OFF);
        tableResultats.getTableHeader().setResizingAllowed(false);
        tableResultats.addMouseMotionListener(new java.awt.event.MouseMotionAdapter() {
            public void mouseDragged(java.awt.event.MouseEvent evt) {
                tableResultatsMouseDragged(evt);
            }
        });
        tableResultats.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                tableResultatsMouseClicked(evt);
            }
        });
        tableResultats.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                tableResultatsKeyReleased(evt);
            }
        });
        srollPaneTableResults.setViewportView(tableResultats);

        lblNbDecimal.setText(messages.getString("Number_Decimals")); // NOI18N
        pnlItemsHaut.add(lblNbDecimal);

        selecteurDecimal.setModel(new javax.swing.SpinnerNumberModel(1, 1, 99, 1));
        selecteurDecimal.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                selecteurDecimalStateChanged(evt);
            }
        });
        pnlItemsHaut.add(selecteurDecimal);
        pnlItemsHaut.add(filler3);

        lblVue.setText(messages.getString("View_Label")); // NOI18N
        pnlItemsHaut.add(lblVue);

        cboVue.setModel(new DefaultComboBoxModel(new String[] {messages.getString("Table_View"), messages.getString("List_View")}));
        cboVue.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                cboVueItemStateChanged(evt);
            }
        });
        pnlItemsHaut.add(cboVue);
        pnlItemsHaut.add(filler4);

        lblOrdre.setText(messages.getString("Sort_By")); // NOI18N
        pnlItemsHaut.add(lblOrdre);

        cboOrdre.setMaximumRowCount(6);
        cboOrdre.setModel(new DefaultComboBoxModel(getTableauTypes(Tri.Type.values())));
        cboOrdre.setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
        cboOrdre.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                cboOrdreItemStateChanged(evt);
            }
        });
        pnlItemsHaut.add(cboOrdre);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(pnlItemsHaut, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(layout.createSequentialGroup()
                        .addContainerGap()
                        .addComponent(srollPaneTableResults))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                        .addGap(10, 10, 10)
                        .addComponent(panelContentFile1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(pnlItemsHaut, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(srollPaneTableResults, javax.swing.GroupLayout.DEFAULT_SIZE, 231, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(panelContentFile1, javax.swing.GroupLayout.DEFAULT_SIZE, 173, Short.MAX_VALUE))
        );
    }// </editor-fold>//GEN-END:initComponents

    private void cboOrdreItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_cboOrdreItemStateChanged
        if (evt.getStateChange() == ItemEvent.SELECTED) {
            if (analys != null) {
                regenererResultats();
            }
        }
    }//GEN-LAST:event_cboOrdreItemStateChanged
    private void ChangementTailleTableau(int nbreDecimal) {

        if (tableResultats != null) {

            TableColumnAdjuster ajusteurColonne = new TableColumnAdjuster(tableResultats);
            int index = 1;
            while (index < tableResultats.getColumnCount()) {
                TableColumn a = tableResultats.getColumnModel().getColumn(index);

                a.setMaxWidth(20 + (nbreDecimal * 8));
                index++;
                this.revalidate();
            }
            ajusteurColonne.adjustColumns();
        }
    }
    private void selecteurDecimalStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_selecteurDecimalStateChanged

        int nbreDecimal = (int) selecteurDecimal.getValue();

        if (nbreDecimal == 9) {
            selecteurDecimal.setValue(8);

        } else if (nbreDecimal > 8) {
            selecteurDecimal.setValue(nbreDecimal / 10);
        } else if (nbreDecimal == 0) {
            selecteurDecimal.setValue(1);
        }
        ChangementTailleTableau(nbreDecimal);
        spinnerValue = (int) selecteurDecimal.getValue();

        preferences.writePref("DECIMAL", spinnerValue);
        if (analys != null) {
            regenererResultats();
        }
    }//GEN-LAST:event_selecteurDecimalStateChanged

    public void selecteurDecimalKeyPressed(KeyEvent e) {

        int nbreDecimal = (int) selecteurDecimal.getValue();
        if (nbreDecimal > 8) {
            selecteurDecimal.setValue(8);
        }

    }

    private void cboVueItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_cboVueItemStateChanged
        vueTableau = evt.getItem() == messages.getString("Table_View");

        lblOrdre.setVisible(vueTableau);
        cboOrdre.setVisible(vueTableau);
        noyau.getPrefs().writePref("VUE_COURAN", vueTableau);
        if (analys != null) {
            regenererResultats();
        }
    }//GEN-LAST:event_cboVueItemStateChanged

    private void tableResultatsMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_tableResultatsMouseClicked
        if (!parent.estSommaire()) {
            if (evt.getButton() == java.awt.event.MouseEvent.BUTTON2
                    || evt.getButton() == java.awt.event.MouseEvent.BUTTON3) {
                if (tableResultats.getSelectedRows().length > 0) {
                    if ((Boolean) preferences.readPref("PROGRESSIVE", false)) {
                        jRadioButtonMenuItem1.setSelected(true);
                    } else {
                        jRadioButtonMenuItem2.setSelected(true);
                    }
                    menuContextuel.show(evt.getComponent(), evt.getX(), evt.getY());
                }
            }
        }
        try {
            //if (vueTableau) {
            SelectionnerFichier();
            //}
        } catch (IOException ex) {
            Logger.getLogger(PanelResults.class.getName()).log(Level.SEVERE, null, ex);
        }

    }//GEN-LAST:event_tableResultatsMouseClicked

    private void mnuSuprimmerActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_mnuSuprimmerActionPerformed
//        if (analys.getFichiersResultats().size() == 3) {
//            ErrorMessages.notEnoughFiles();
//            return;
//        }

        if (vueTableau) {
            int[] tab = tableResultats.getSelectedRows();
            String pathFichierRecherche;

            for (int i = tab.length - 1; i >= 0; --i) {
//                if (analys.getFichiersResultats().size() > 3) {
                pathFichierRecherche = tableResultats.getColumnName(tab[i] + 1).split("\n")[1];

                for (int j = 0; j < analys.getFichiersResultats().size(); j++) {
                    if (analys.getFichiersResultats().get(j).toString().equals(pathFichierRecherche)) {
                        analys.retirerFichierEtMettreAJourMatrice(
                                analys.getFichiersResultats().get(j));
                        break;
                    }
                }
//                }
            }
        } else {
            genererResultatsSousFormeListe();
        }

        regenererResultats();
        if (win.getTabSommaire() != null) {
            win.fermerTab(win.getTabSommaire(), false);
            win.creerSommaire(win);
        }
        analys.aviserObservateurs(Observation.DEL_IN_TABLE, analys);
    }//GEN-LAST:event_mnuSuprimmerActionPerformed

    private void mnuFilterExcludeActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_mnuFilterExcludeActionPerformed
        String filter = JOptionPane.showInputDialog(parent, messages.getString("enter_filter"),
                messages.getString("filter_results"), JOptionPane.INFORMATION_MESSAGE);
        try {
            filter = filter.replaceAll(",", ".");
            float threshold = Float.parseFloat(filter);
            retirerFichiersTableauParFiltre(threshold);
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(parent, messages.getString("bad_filter_message"),
                    messages.getString("bad_filter_title"), JOptionPane.ERROR_MESSAGE);
        }
    }//GEN-LAST:event_mnuFilterExcludeActionPerformed

    private void jMenuCacherFicherActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuCacherFicherActionPerformed

        if (vueTableau) {
            int[] tab = tableResultats.getSelectedRows();
            String pathFichierRecherche;

            for (int i = tab.length - 1; i >= 0; --i) {
//                if (analys.getFichiersResultats().size() > 3) {
                pathFichierRecherche = tableResultats.getColumnName(tab[i] + 1).split("\n")[1];

                for (int j = 0; j < analys.getFichiersResultats().size(); j++) {
                    if (analys.getFichiersResultats().get(j).toString().equals(pathFichierRecherche)) {
                        analys.CacherFichierEtMettreAJourMatrice(
                                analys.getFichiersResultats().get(j));
                        break;
                    }
                }
//                }
            }
        } else {
            genererResultatsSousFormeListe();

        }

        regenererResultats();
        win.modifierSommaire(win);
        analys.aviserObservateurs(Observation.DEL_IN_TABLE, analys);
    }//GEN-LAST:event_jMenuCacherFicherActionPerformed

    private void genererResultatsSousFormeListe() {
        int[] indiceColonne = tableResultats.getSelectedColumns();
        int[] indiceLigne = tableResultats.getSelectedRows();
        String fichier = (String) tableResultats.getModel().
                getValueAt(indiceLigne[0], indiceColonne[0]);

        File file = null;
        List<File> fichiers = analys.getFichiersResultats();
        for (int i = 0; i < fichiers.size(); ++i) {
            if (fichiers.get(i).getAbsolutePath().contains(fichier)) {
                file = analys.getFile(i);
            }
        }
        analys.CacherFichierEtMettreAJourMatrice(file);
    }

    private void jRadioButtonMenuItem2ItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_jRadioButtonMenuItem2ItemStateChanged
        // TODO add your handling code here:
        preferences.writePref("PROGRESSIVE", false);
        regenererResultats();
    }//GEN-LAST:event_jRadioButtonMenuItem2ItemStateChanged

    private void jRadioButtonMenuItem1ItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_jRadioButtonMenuItem1ItemStateChanged
        // TODO add your handling code here:
        preferences.writePref("PROGRESSIVE", true);
        regenererResultats();
    }//GEN-LAST:event_jRadioButtonMenuItem1ItemStateChanged

    private void tableResultatsMouseDragged(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_tableResultatsMouseDragged
        try {
            //if (vueTableau) {
            SelectionnerFichier();
            //}
        } catch (IOException ex) {
            Logger.getLogger(PanelResults.class.getName()).log(Level.SEVERE, null, ex);
        }
    }//GEN-LAST:event_tableResultatsMouseDragged

    private void jMenuRestaurerCachesActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuRestaurerCachesActionPerformed
        analys.RestaurerFichiersCaches();
        regenererResultats();
    }//GEN-LAST:event_jMenuRestaurerCachesActionPerformed

    private void tableResultatsKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_tableResultatsKeyReleased
        try {
            //if (vueTableau) {
            SelectionnerFichier();
            //}
        } catch (IOException ex) {
            Logger.getLogger(PanelResults.class.getName()).log(Level.SEVERE, null, ex);
        }
    }//GEN-LAST:event_tableResultatsKeyReleased

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.ButtonGroup buttonGroup1;
    private javax.swing.JComboBox cboOrdre;
    private javax.swing.JComboBox cboVue;
    private javax.swing.Box.Filler filler3;
    private javax.swing.Box.Filler filler4;
    private javax.swing.JMenuItem jMenuCacherFicher;
    private javax.swing.JMenuItem jMenuRestaurerCaches;
    private javax.swing.JRadioButtonMenuItem jRadioButtonMenuItem1;
    private javax.swing.JRadioButtonMenuItem jRadioButtonMenuItem2;
    private javax.swing.JPopupMenu.Separator jSeparator1;
    private javax.swing.JPopupMenu.Separator jSeparator2;
    private javax.swing.JLabel lblNbDecimal;
    private javax.swing.JLabel lblOrdre;
    private javax.swing.JLabel lblVue;
    private javax.swing.JPopupMenu menuContextuel;
    private javax.swing.JMenuItem mnuFilterExclude;
    private javax.swing.JMenuItem mnuSuprimmer;
    private ca.qc.bdeb.baldr.ihm.PanelContentFile panelContentFile1;
    private javax.swing.JPanel pnlItemsHaut;
    private javax.swing.JSpinner selecteurDecimal;
    private javax.swing.JScrollPane srollPaneTableResults;
    private javax.swing.JTable tableResultats;
    // End of variables declaration//GEN-END:variables

}
