/*
 * Task.java
 *
 * Created on 14 avril 2007, 18:20
 *$Id: Task.java 254 2007-09-23 15:21:42Z zeta $
 */
package ca.qc.bdeb.baldr.noyau;

import ca.qc.bdeb.baldr.formattage.CommentParser;
import ca.qc.bdeb.baldr.formattage.ExtrairePDF;
import ca.qc.bdeb.baldr.utils.Observation;
import static ca.qc.bdeb.baldr.utils.Observation.*;
import ca.qc.bdeb.baldr.main.Main;
import ca.qc.bdeb.baldr.utils.FilePair;
import ca.qc.bdeb.baldr.utils.Observable;
import ca.qc.bdeb.baldr.utils.Observateur;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStream;
import java.math.BigInteger;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Scanner;
import java.util.Set;
import java.util.zip.GZIPOutputStream;
import org.apache.commons.io.FilenameUtils;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.security.*;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.w3c.dom.Element;

/**
 * Représente une tâche d'analyse dans le programme.
 *
 * @author zeta
 */
public class Task implements Savable, Observable, Cloneable {

    /**
     * Les préférences associées au projet.
     */
    private GestionnairePreferences prefs;
    private boolean bPreview;
    private boolean bConcatenation;
    private boolean bCommentaires;
    private boolean bWhiteSpaces;
    private boolean bExtrairePDF;
    private boolean bExtraireImagePDF;

    private boolean checkBoxCommentaires = false;
    private boolean checkBoxWhitepsaces = true;
    private boolean jCheckBoxPreviewFiles = true;
    private boolean jCheckBoxAnalyseConcatenation = false;
    private boolean pdfExtractor = true;
    private boolean pdfImages = true;
    private double redLimit = 0;
    private String redLimitTxt = "0.0";
    private double yellowLimit = 0;
    private String yellowLimitTxt = "0.0";
    private double greenLimit = 0;
    private String greenLimitTxt = "1.0";
    private boolean isProgressive = true;
    private boolean invalide;

    public double getRedLimit() {
        return redLimit;
    }

    public String getRedLimitTxt() {
        return redLimitTxt;
    }

    public void setRedLimit(double newValue) {
        redLimit = newValue;
    }

    public void setRedLimitTxt(String t) {
        redLimitTxt = t;
    }

    public void setRedLimitTxt(String t, char nouv) {
        redLimitTxt = t + nouv;
    }

    public double getYellowLimit() {
        return yellowLimit;
    }

    public String getYellowLimitTxt() {
        return yellowLimitTxt;
    }

    public void setYellowLimit(double newValue) {
        yellowLimit = newValue;
    }

    public void setYellowLimitTxt(String t) {
        yellowLimitTxt = t;
    }

    public void setYellowLimitTxt(String t, char nouv) {
        yellowLimitTxt = t + nouv;
    }

    public double getGreenLimit() {
        return greenLimit;
    }

    public String getGreenLimitTxt() {
        return greenLimitTxt;
    }

    public void setGreenLimit(double newValue) {
        greenLimit = newValue;
    }

    public void setGreenLimitTxt(String t) {
        greenLimitTxt = t;
    }

    public void setGreenLimitTxt(String t, char nouv) {
        greenLimitTxt = t + nouv;
    }

    public boolean getIsProgressive() {
        return isProgressive;
    }

    public void setIsProgressive(boolean newValue) {
        isProgressive = newValue;
    }

    public boolean getJCheckBoxPreviewFiles() {
        return jCheckBoxPreviewFiles;
    }

    public void setJCheckBoxPreviewFiles(boolean newValue) {
        jCheckBoxPreviewFiles = newValue;
    }

    public boolean getJCheckBoxAnalyseConcatenation() {
        return jCheckBoxAnalyseConcatenation;
    }

    public void setJCheckBoxAnalyseConcatenation(boolean newValue) {
        jCheckBoxAnalyseConcatenation = newValue;
    }

    public boolean getPdfExtractor() {
        return pdfExtractor;
    }

    public void setPdfExtractor(boolean newValue) {
        pdfExtractor = newValue;
    }

    public boolean getPdfImages() {
        return pdfImages;
    }

    public void setPdfImages(boolean newValue) {
        pdfImages = newValue;
    }

    /**
     * Le rapport de l'analyse.
     */
    private String rapport;

    /**
     * Le nom de l'analyse.
     */
    private String titre;

    /**
     * La liste des taches qui composent une analyse sommaire
     */
    protected List<Task> tachesComposantes = null;

    /**
     * Les fichiers de l'analyse.
     */
    protected List<File> fichiersAnalyse;

    /**
     * Les fichiers originaux de l'analyse (la liste de tous les fichiers visés
     * par cette tâche).
     */
    private List<File> fichiersOriginaux;

    /**
     * Les fichiers de l'analyse incluant les fichiers cachés
     */
    private List<File> listeFichiersComplete;
    private String IDTache;

    /**
     * Les résultats de l'analyse.
     */
    private GestionnaireResultats gestionnaireResultats;
    private ListeResultats listeResultats;
    private MatriceTriangulaire resultatsAnalyse;
    private MatriceTriangulaire resultatsComplets;
    private Map<File, Long> precalculatedFiles;
    private Map<FilePair, Long> precalculatedPairs;
    private List<Observateur> observateurs;

    /**
     * Les différentes sources couvertes par cette tâche.
     */
    private Map<File, List<File>> sources;

    /**
     * Indique si des fichiers de l'analyse ont été cachés
     */
    private boolean contientFichiersCaches;

    /**
     * Indique si l'analyse doit se faire par concaténation ou non.
     */
    private boolean analyseConcatenation = false;

    // Préférances sur le type d'analyse
    private boolean enleverCommentaires = false;
    private boolean enleverWhitespaces = false;

    /**
     * S'il n'y a qu'un seul ancêtre commun à tous les fichiers, est un tableau
     * d'un élément qui correspond à cet ancêtre commun. Sinon, contient les
     * multiples ancêtres communs (sous Windows, ce serait les multiples
     * racines, par exemple C:, D:, etc.).
     */
    private File[] filesCommonAncestors = null;

    private float state;
    private float medianeErr;
    /**
     * Vrai si l'interface a restauré cette analyse (par exemple, après un
     * import).
     */
    private boolean estRestauree;

    private boolean modifie = false;

    private boolean analyseEnCours;

    private ExtrairePDF pdfExtractore;

    private boolean erreurRestauration = false;

    public GestionnairePreferences getPrefs() {
        return prefs;
    }

    public List<File> getListeFichiersComplete() {
        return listeFichiersComplete;
    }

    /**
     * Lit dans le fichier xml ouvert si le fichier est sommaire avec l'attribut
     * sommaire de "< titre >"
     *
     * @param node
     * @return
     */
    public boolean xmlEstSommaire(Node node) {
        String sommaire = "";
        boolean xmlSommaire = false;
        for (int i = 0; i < node.getChildNodes().getLength(); i++) {
            if (node.getChildNodes().item(i).getNodeName() != null) {
                switch (node.getChildNodes().item(i).getNodeName()) {
                    case "titre":
                        titre = node.getChildNodes().item(i).getTextContent();
                        sommaire = node.getChildNodes().item(i).getAttributes().getNamedItem("sommaire").getNodeValue();
                        if (sommaire.equals("True")) {
                            xmlSommaire = true;
                        }
                        break;
                    case "analys":
                        NodeList analyse
                                = node.getChildNodes().item(i).getChildNodes();
                        setFileListFromXMLNodeSommaire(analyse);
                        break;
                }
            }
        }
        return xmlSommaire;
    }

    /**
     * Permet de récupérer les options spécifiques /* d'un sommaire /* Auteur:
     * Victor Gontar
    *
     */
    private void setFileListFromXMLNodeSommaire(NodeList analyse) {
        for (int j = 0; j < analyse.getLength(); j++) {
            switch (analyse.item(j).getNodeName()) {
                case "optspecifiques":
                    NodeList optSpec = analyse.item(j).getChildNodes();
                    for (int i = 0; i < optSpec.getLength(); i++) {
                        switch (optSpec.item(i).getNodeName()) {
                            case "preview":
                                jCheckBoxPreviewFiles = Boolean.parseBoolean(optSpec.item(i).getTextContent());
                                break;
                            case "concatenation":
                                jCheckBoxAnalyseConcatenation = Boolean.parseBoolean(optSpec.item(i).getTextContent());
                                break;
                            case "extrairepdf":
                                pdfExtractor = Boolean.parseBoolean(optSpec.item(i).getTextContent());
                                break;
                            case "extraireimages":
                                pdfImages = Boolean.parseBoolean(optSpec.item(i).getTextContent());
                                break;
                            case "commentaires":
                                checkBoxCommentaires = Boolean.parseBoolean(optSpec.item(i).getTextContent());
                                break;
                            case "whitespaces":
                                checkBoxWhitepsaces = Boolean.parseBoolean(optSpec.item(i).getTextContent());
                                break;
                            case "progressive":
                                isProgressive = Boolean.parseBoolean(optSpec.item(i).getTextContent());
                                break;
                            case "redlimit":
                                  try {
                                redLimit = Double.parseDouble(optSpec.item(i).getTextContent());
                                redLimitTxt = optSpec.item(i).getTextContent();
                            } catch (NumberFormatException e) {
                                redLimitTxt = optSpec.item(i).getTextContent();
                            }
                            break;
                            case "yellowlimit":
                                  try {
                                yellowLimit = Double.parseDouble(optSpec.item(i).getTextContent());
                                yellowLimitTxt = optSpec.item(i).getTextContent();
                            } catch (NumberFormatException e) {
                                yellowLimitTxt = optSpec.item(i).getTextContent();
                            }
                            break;
                            case "greenlimit":
                                  try {
                                greenLimit = Double.parseDouble(optSpec.item(i).getTextContent());
                                greenLimitTxt = optSpec.item(i).getTextContent();
                            } catch (NumberFormatException e) {
                                greenLimitTxt = optSpec.item(i).getTextContent();
                            }
                            break;
                        }
                    }
                    break;
            }
        }
    }

    /**
     * Crée une nouvelle instance de Task.
     */
    public Task() {
        titre = "Analyse";
        rapport = "";
        fichiersAnalyse = new ArrayList();
        fichiersOriginaux = new ArrayList();
        precalculatedFiles = new HashMap();
        precalculatedPairs = new HashMap();
        observateurs = new ArrayList();
        sources = new HashMap();
        estRestauree = false;
        analyseEnCours = false;
        pdfExtractore = new ExtrairePDF();
        gestionnaireResultats = new GestionnaireResultats();
        IDTache = "NotSaved";

    }

    public String getIDTache() {
        return IDTache;
    }

    public void setIDTache(String id) {
        IDTache = id;
    }

    public boolean getCheckBoxCommentaires() {
        return checkBoxCommentaires;
    }

    public void setCheckBoxCommentaires(boolean newValue) {
        checkBoxCommentaires = newValue;
    }

    public boolean getCheckBoxWhitepsaces() {
        return checkBoxWhitepsaces;
    }

    public void setCheckBoxWhitepsaces(boolean newValue) {
        checkBoxWhitepsaces = newValue;
    }

    @Override
    public Task clone() throws CloneNotSupportedException {
        super.clone();

        Task copie = new Task();

        copie.rapport = rapport;
        copie.titre = titre + " - copie";

        if (fichiersAnalyse != null) {
            copie.fichiersAnalyse = new ArrayList(fichiersAnalyse);
        }

        if (fichiersOriginaux != null) {
            copie.fichiersOriginaux = new ArrayList(fichiersOriginaux);
        }

        copie.prefs = prefs;

        if (resultatsAnalyse != null) {
            copie.resultatsAnalyse = resultatsAnalyse.clone();
        }

        if (precalculatedFiles != null) {
            copie.precalculatedFiles = new HashMap(precalculatedFiles);
        }

        if (precalculatedPairs != null) {
            copie.precalculatedPairs = new HashMap(precalculatedPairs);
        }

        if (observateurs != null) {
            copie.observateurs = new ArrayList(observateurs);
        }

        if (sources != null) {
            copie.sources = new HashMap(sources);
        }

        if (filesCommonAncestors != null) {
            copie.filesCommonAncestors = filesCommonAncestors.clone();
        }

        copie.state = state;
        copie.medianeErr = medianeErr;
        copie.estRestauree = estRestauree;

        return copie;
    }

    /**
     * Permet de donner un autre gestionnaire de résultats à la tâche que celui
     * qu'elle crée automatiquement
     *
     * @param result Le gestionnaire de résultats à utiliser
     */
    public void setGestionnaireResultats(GestionnaireResultats result) {
        gestionnaireResultats = result;
    }

    /**
     * Permet de modifier les fichiers à partir d'une liste de fichiers
     * existante.
     *
     * @param fichiers
     */
    public void setFichiers(List<File> fichiers) {
        setFichiers(fichiers, true);
    }

    /**
     * Permet de modifier l'attribut files à partir d'un tableau de fichiers
     * existant, en spécifiant si les résultats doivent être réinitialisés ou
     * non.
     *
     * @param nouveauxFichiers
     * @param resetRes Si les résultats doivent être réinitialisés
     */
    public void setFichiers(List<File> nouveauxFichiers, boolean resetRes) {
        if (nouveauxFichiers != null) {
            fichiersAnalyse = new ArrayList(nouveauxFichiers);
            fichiersOriginaux = new ArrayList(nouveauxFichiers);
        } else {
            fichiersAnalyse = new ArrayList();
            fichiersOriginaux = new ArrayList();

            return; // Pas de traitement nécessaire.
        }

        if (fichiersAnalyse != null && fichiersAnalyse.size() > 0) {
            File commonAncestor = extractCommonAncestor();

            if (commonAncestor == null) {
                filesCommonAncestors = findAllRoots();
            } else {
                filesCommonAncestors = new File[]{commonAncestor};
            }

            if (resetRes) {
                resultatsAnalyse
                        = new MatriceTriangulaire(fichiersAnalyse.size());
            }
        }

        modifie = true;
    }

    /**
     * Dans le tableau de fichiers, donne la partie du chemin qui est commune à
     * tous les fichiers.
     *
     * @return Objet {@link java.io.File} représentant ce chemin, ou
     * {@code null} s'il n'en existe pas.
     */
    private File extractCommonAncestor() {
        List<String> pathSimilaire = new ArrayList();
        String[] pathInitial = fichiersAnalyse.get(0).getAbsolutePath()
                .split(Main.regexFileSeparator);
        boolean pareil = true;

        // Vérifier le chemin nom par nom
        for (int i = 0; i < pathInitial.length - 1 && pareil; i++) {
            pareil = true;

            for (File file : fichiersAnalyse) {
                String[] pathTest
                        = file.getAbsolutePath().split(Main.regexFileSeparator);

                if (pathTest.length <= i
                        || !pathInitial[i].equals(pathTest[i])) {
                    pareil = false;
                }
            }

            if (pareil) {
                pathSimilaire.add(pathInitial[i]);
            }
        }

        StringBuilder sb = new StringBuilder();

        if (Main.isUnix || Main.isMac) {
            sb.append(File.separator);
        }

        for (String string : pathSimilaire) {
            sb.append(string);
            sb.append(File.separator);
        }

        if (pathSimilaire.isEmpty()) {
            return null;
        } else {
            return new File(sb.toString());
        }
    }

    private File[] findAllRoots() {
        Set<File> roots = new HashSet();

        for (File file : fichiersOriginaux) {
            roots.add(file.toPath().getRoot().toFile());
        }

        return roots.toArray(new File[0]);
    }

    private OutputStream makeComp(OutputStream compresseurFichier,
            File file) throws IOException {
        FileInputStream lecteurFichier = new FileInputStream(file);
        BufferedInputStream bufferIn = new BufferedInputStream(lecteurFichier);

        // La plupart des systèmes de fichiers fonctionnent par blocs
        // de 4096 ou 8192 octets. On prend une chance avec le bloc supérieur,
        // dans le but d'améliorer les performances par rapport à d'autres
        // tailles, puisque cette étape du traitement est la plus lourde.
        byte[] tampon = new byte[8192];
        int len;

        if (FilenameUtils.getExtension(file.getPath()).equals("pdf")) {
            if ((Boolean) prefs.readPref("EXTRACT_PDF", false)) {
                compresseurFichier.write(pdfExtractore.ExtrairePDF(file).getBytes());
            }

            if ((Boolean) prefs.readPref("EXTRACT_IMG", false)) {
                compresseurFichier.write(pdfExtractore.extraireImages(file).getBytes());
            }
        } else {
            // Enlever commentaires du fichier d'analyse
            if (enleverCommentaires) {

                enleverCommentaires(file, bufferIn, compresseurFichier, tampon);

                // Enlever lignes blanches du fichier d'analyse
            } else if (enleverWhitespaces) {

                enleverWhitespaces(file, bufferIn, compresseurFichier, tampon);

                // Analyse du fichier normal
            } else {

                while ((len = bufferIn.read(tampon)) > 0) {
                    compresseurFichier.write(tampon, 0, len);
                }
            }
        }
        bufferIn.close();

        return compresseurFichier;
    }

    private void enleverCommentaires(File file, BufferedInputStream bufferIn,
            OutputStream compresseurFichier, byte[] tampon) throws IOException {

        int codeChar;
        String output;

        byte[] res;
        int pos = 0;

        CommentParser parser = new CommentParser(file);
        while ((codeChar = bufferIn.read()) != -1) {
            parser.lireCaractere((char) codeChar);
            output = parser.retournerCaractereChaine();

            // temp
            if (codeChar == 10) {
                output = new StringBuilder(output).append("\n").toString();
            }

            if (!output.isEmpty()) {
                res = output.getBytes();

                if (pos + res.length >= tampon.length) {
                    compresseurFichier.write(tampon, 0, pos);
                    compresseurFichier.write(res);

                    pos = 0;
                } else {
                    for (int i = 0; i < res.length; i++) {
                        tampon[pos++] = res[i];
                    }
                }
            }
        }

        if (pos > 0) {
            compresseurFichier.write(tampon, 0, pos + 1);
        }
    }

    private void enleverWhitespaces(File file, BufferedInputStream bufferIn,
            OutputStream compresseurFichier, byte[] tampon) throws IOException {

        String ligne = "";

        Scanner scannerIn = new Scanner(bufferIn);

        while (scannerIn.hasNext()) {

            ligne = scannerIn.nextLine();

            if (!(ligne.trim().isEmpty())) {
                tampon = ligne.getBytes();
                compresseurFichier.write(tampon);
            }
        }
    }

    /**
     * Détermine le poids d'un fichier gunzippé seul.
     *
     * @param file Le fichier à anlyser.
     * @return La taille du fichier gunzippé.
     * @throws IOException
     * @throws FileNotFoundException
     */
    private long calculateGZipSize(File file)
            throws IOException, FileNotFoundException {
        long ret = 0;

        if (!enleverCommentaires && precalculatedFiles.containsKey(file)) {
            return precalculatedFiles.get(file);
        }

        OutputStreamSizer calcTaille = new OutputStreamSizer();
        GZIPOutputStream compresseurFichier = new GZIPOutputStream(calcTaille);

        makeComp(compresseurFichier, file);

        compresseurFichier.close();

        ret = calcTaille.getSize();

        calcTaille.close();

        if (!enleverCommentaires) {
            precalculatedFiles.put(file, ret);
        }

        return ret;
    }

    /**
     * Détermine le poids d'une paire de fichiers gunzippés ensembles.
     *
     * @param file1 Le premier fichier à analyser.
     * @param file2 Le deuxième fichier à analyser.
     * @return Le poids des deux fichiers gunzippés ensembles.
     * @throws IOException
     * @throws FileNotFoundException
     */
    private long calculateGZipSize(File file1, File file2)
            throws IOException, FileNotFoundException {
        long ret = 0;
        FilePair pair = new FilePair(file1, file2);

//        if (!enleverCommentaires && precalculatedPairs.containsKey(pair)) {
//            return precalculatedPairs.get(pair);
//        }
        OutputStreamSizer compression = new OutputStreamSizer();
        BufferedOutputStream fichierBuffer
                = new BufferedOutputStream(compression);
        GZIPOutputStream compresseurFichier
                = new GZIPOutputStream(fichierBuffer);

        makeComp(compresseurFichier, file1);
        makeComp(compresseurFichier, file2);

        compresseurFichier.close();

        ret = compression.getSize();

        compression.close();

        if (!enleverCommentaires) {
            precalculatedPairs.put(pair, ret);
        }

        return ret;
    }

    /**
     * Détermine le poids d'un fichier gunzippé seul.
     *
     * @return Le poids du fichier gunzippé.
     * @throws IOException
     * @throws FileNotFoundException
     */
    private long calculateGZipSize(List<File> source)
            throws IOException, FileNotFoundException {
        long ret = 0;

        OutputStreamSizer compression = new OutputStreamSizer();
        GZIPOutputStream compresseurFichier = new GZIPOutputStream(compression);

        for (File fichier : source) {
            makeComp(compresseurFichier, fichier);
        }

        compresseurFichier.close();

        ret = compression.getSize();

        compression.close();

        return ret;
    }

    /**
     * Détermine le poids d'une paire de fichiers gunzippés ensembles.
     *
     * @return Le poids des deux fichiers gunzippés ensembles.
     * @throws IOException
     * @throws FileNotFoundException
     */
    private long calculateGZipSize(List<File> source1, List<File> source2)
            throws IOException, FileNotFoundException {
        long ret = 0;

        OutputStreamSizer compression = new OutputStreamSizer();
        BufferedOutputStream fichierBuffer
                = new BufferedOutputStream(compression);
        GZIPOutputStream compresseurFichier
                = new GZIPOutputStream(fichierBuffer);

        for (File fichier : source1) {
            makeComp(compresseurFichier, fichier);
        }
        for (File fichier : source2) {
            makeComp(compresseurFichier, fichier);
        }

        compresseurFichier.close();

        ret = compression.getSize();

        compression.close();

        return ret;
    }

    /**
     * Retire plusieurs fichiers de l'analyse
     *
     * @param fichiers les fichiers à retirer
     */
    public void retirerFichiers(List<File> fichiers) {
        for (File fichier : fichiers) {
            retirerFichierEtMettreAJourMatrice(fichier);
        }
    }

    /**
     * L'analyse consiste en la comparaison 1 par 1 chaque fichier. Imaginons la
     * liste de fichier A, B, et C, il compare alors A avec B, A avec C, B avec
     * A, B avec C, C avec A, et C avec B.
     */
    private void faireAnalyseNormale() {

        //nombre de fichiers analysés
        int nbrFichiers = fichiersAnalyse.size();

        for (int i = 0; i < nbrFichiers; i++) {
            // Annulation ?
            if (Thread.interrupted()) {
                aviserObservateurs();
                return;
            }

            for (int j = 0; j < i; j++) {
                try {
                    float resultat = calculerResultat(i, j, false); //false, pas de concatenation
                    resultatsAnalyse.setRes(i, j, resultat);
                    increaseState();
                } catch (IOException ex) {

                }
            }
        }
    }

    private void faireAnalyseConcatenation() {
        int nbrFichiers = sources.size();

        for (int i = 0; i < nbrFichiers; i++) {

            for (int j = 0; j < i; j++) {
                // Annulation ?
                if (Thread.interrupted()) {
                    aviserObservateurs();
                    return;
                }

                try {
                    float resultat = calculerResultat(i, j, true); //true, concatenation
                    resultatsAnalyse.setRes(i, j, resultat);
                    increaseState();
                } catch (IOException ex) {
                }
            }
        }
    }

    private float calculerResultat(int i, int j, boolean concatenation) throws IOException {

        //resultat de l'analyse
        float resultat = 0;
        long tailleFichier1GZip = 0;
        long tailleFichier2GZip = 0;
        long tailleFichier1et2GZip = 0;

        File fichier1 = fichiersAnalyse.get(i);
        File fichier2 = fichiersAnalyse.get(j);

        List<File> liste1 = sources.get(fichiersAnalyse.get(i));
        List<File> liste2 = sources.get(fichiersAnalyse.get(j));

        /**
         * L'analyse ne va pas comparer fichier1 et fichier2 s'ils ont la même
         * provenance.
         */
        if (!verifierSiMemeSource(fichier1, fichier2)) {

            //si le resultat de l'analyse de fichier1 et fichier2 existe déjà
            if (listeResultats.resultatExiste(fichier1, fichier2)) {
                resultat = listeResultats.getResultat(fichier1, fichier2);
            } else {

                if (concatenation) {
                    tailleFichier1GZip = calculateGZipSize(liste1);
                    tailleFichier2GZip = calculateGZipSize(liste2);
                } else {
                    tailleFichier1GZip = calculateGZipSize(fichier1);
                    tailleFichier2GZip = calculateGZipSize(fichier2);
                }

                if (resultatsAnalyse.getRes(i, j) == -1) {

                    if (concatenation) {
                        tailleFichier1GZip = calculateGZipSize(liste1);

                        tailleFichier1et2GZip
                                = calculateGZipSize(liste1, liste2);
                    } else {
                        tailleFichier1et2GZip
                                = calculateGZipSize(fichier1, fichier2);
                    }

                    resultat = java.lang.Math.min(1F, 1F
                            - (float) (tailleFichier1GZip
                            + tailleFichier2GZip
                            - tailleFichier1et2GZip)
                            / (float) java.lang.Math.max(
                                    tailleFichier1GZip,
                                    tailleFichier2GZip));

                    listeResultats.ajouterResultat(
                            fichier1, fichier2, resultat);
                }
            }
        }
        return resultat;
    }

    /**
     * Lance l'analyse
     */
    public void lancerAnalyse() {
        if (prefs != null) {
            boolean cat = (Boolean) prefs.readPref("CONCATENATION", false);
            if (!(Boolean) prefs.readPref("PROGRESSIVE", false)) {
                prefs.writePref("PROGRESSIVE", true);
            }
            if (cat && !sources.isEmpty()) {
                fichiersAnalyse = new ArrayList(sources.keySet());
            } else {
                fichiersAnalyse = new ArrayList(fichiersOriginaux);
            }

            analyseConcatenation = cat;

            // Vérifier si options d'enlever les commentaires et activée
            enleverCommentaires = checkBoxCommentaires;

            // Vérifier si options d'enlever les commentaires et activée
            enleverWhitespaces = checkBoxWhitepsaces;

        } else { // Pas de préférences gérées (par exemple : pendant un test).
            if (analyseConcatenation && !sources.isEmpty()) {
                fichiersAnalyse = new ArrayList(sources.keySet());
            } else {
                fichiersAnalyse = new ArrayList(fichiersOriginaux);
            }
        }

        state = 0;
        resultatsAnalyse = new MatriceTriangulaire(fichiersAnalyse.size());
        listeResultats = gestionnaireResultats.obtenirListe(this);

        analyseEnCours = true;
        
        if (analyseConcatenation && !sources.isEmpty()) {
            // faireAnalyseNormale();
            faireAnalyseConcatenation();
        } else {
            faireAnalyseNormale();
        }

        state = 1;
        analyseEnCours = false;
        modifie = true;
        aviserObservateurs(ANALYSE_TERMINEE, this);
        
    }

    private void setExRes(List<File> exfiles, float[][] exresults) {
        Map<File, Integer> exFilesTempo = new HashMap();

        for (int i = 0; i < exfiles.size(); i++) {
            exFilesTempo.put(exfiles.get(i), i);
        }

        List<File> fichiers = null;
        if (sources.size() > 0) {
            fichiers = new ArrayList<>(sources.keySet());
        } else {
            fichiers = fichiersAnalyse;
        }
        for (int i = 0; i < fichiers.size(); i++) {
            if (exFilesTempo.containsKey(fichiers.get(i))) { // old file
                // pas besoin de repasser avant
                for (int j = i; j < fichiers.size(); j++) {
                    if (exFilesTempo.containsKey(
                            fichiers.get(j))) { // old file
                        int indiceFile1, indiceFile2;
                        indiceFile1 = exFilesTempo.get(fichiers.get(i));
                        indiceFile2 = exFilesTempo.get(fichiers.get(j));
                        if (indiceFile2 > indiceFile1) {
                            int tempo;
                            tempo = indiceFile1;
                            indiceFile1 = indiceFile2;
                            indiceFile2 = tempo;
                        }

                        resultatsAnalyse.setRes(
                                i, j, exresults[indiceFile1][indiceFile2]);
                    }
                }
            }
        }
    }

    /**
     * Permet d'accéder aux résultats de l'analyse.
     *
     * @return Les résultats de l'analyse.
     */
    public MatriceTriangulaire getResults() {
        return resultatsAnalyse;
    }

    public float getRes(File fichier1, File fichier2) {
        int indiceResultatFichier1, indiceResultatFichier2;
        indiceResultatFichier1 = -1;
        indiceResultatFichier2 = -1;
        for (int i = 0; i < fichiersAnalyse.size(); ++i) {
            if (fichier1 == fichiersAnalyse.get(i)) {
                indiceResultatFichier1 = i;
            }
            if (fichier2 == fichiersAnalyse.get(i)) {
                indiceResultatFichier2 = i;
            }
            if (indiceResultatFichier2 != -1 && indiceResultatFichier1 != -1) {
                i = fichiersAnalyse.size();
            }
        }
        return resultatsAnalyse.getRes(indiceResultatFichier1,
                indiceResultatFichier2);
    }

    private void increaseState() {
        this.state += 1.0 / resultatsAnalyse.getNumAnalyse();
        aviserObservateurs(Observation.PROGRESS, null);
    }

    /**
     * Retire un fichier de la liste des fichiers, selon un fichier passé en
     * paramètres.
     *
     * @param fichier Le fichier à retirer.
     */
    //TODO: Separer en deux methodes et mettre a jour
    //le code dans les endroits ou elle est utiliser
    public void retirerFichierEtMettreAJourMatrice(File fichier) {
        int indexAnalyse = fichiersAnalyse.indexOf(fichier);
        int indexOriginale = fichiersOriginaux.indexOf(fichier);
        fichiersOriginaux.remove(indexOriginale);
        fichiersAnalyse.remove(indexAnalyse);
        resultatsAnalyse.enleverLigneEtColonne(indexAnalyse);

        modifie = true;
    }

    public void CacherFichierEtMettreAJourMatrice(File fichier) {
        if (resultatsComplets == null) {

            try {
                listeFichiersComplete = new ArrayList(fichiersAnalyse);
                resultatsComplets = resultatsAnalyse.clone();
            } catch (CloneNotSupportedException ex) {
                ex.printStackTrace();
            }
        }

        int indexAnalyse = fichiersAnalyse.indexOf(fichier);
        resultatsAnalyse.enleverLigneEtColonne(indexAnalyse);
        fichiersAnalyse.remove(indexAnalyse);
        contientFichiersCaches = true;
        modifie = true;
    }

    public void RestaurerFichiersCaches() {
        if (contientFichiersCaches) {
            try {
                fichiersAnalyse = new ArrayList(listeFichiersComplete);
                resultatsAnalyse = resultatsComplets.clone();
                contientFichiersCaches = false;
                modifie = true;
            } catch (CloneNotSupportedException ex) {
                ex.printStackTrace();
            }
        }
    }

    /**
     * Retire des fichiers de l'analyse selon la différence entre les fichiers
     * originaux et les fichiers restants après la suppression.
     *
     * @param nouveauxFichiers Les fichiers restants.
     */
    //TODO: Separer en methodes
    public void mettreAJourFichiersEtMettreAJourMatrice(
            List<File> nouveauxFichiers) {
        if (state > 0) {
            if (analyseConcatenation && !sources.isEmpty()) {
                Map<File, List<File>> copieSources = new HashMap(sources);

                Object[] set = copieSources.keySet().toArray();

                setFichiers(nouveauxFichiers, false);
                regenererSources();

                int nombreSupprime = 0;
                for (int i = 0; i < set.length; i++) {
                    if (!sources.containsKey((File) set[i])) {
                        resultatsAnalyse.enleverLigneEtColonne(
                                i - nombreSupprime);
                        nombreSupprime++;
                    }
                }
            } else {
                List<File> copieFichiers = new ArrayList(fichiersOriginaux);

                setFichiers(nouveauxFichiers, false);
                regenererSources();

                int nombreSupprime = 0;
                for (int i = 0; i < copieFichiers.size(); i++) {
                    if (!fichiersAnalyse.contains(copieFichiers.get(i))) {
                        resultatsAnalyse.enleverLigneEtColonne(
                                i - nombreSupprime);
                        nombreSupprime++;
                    }
                }
            }

            aviserObservateurs(Observation.UPDATEMAT, null);
        } else {
            setFichiers(nouveauxFichiers, false);
        }

        modifie = true;
    }

    /**
     * Permet d'accéder à l'état de la tâche.
     *
     * @return L'état de la tâche.
     */
    public float getStateCount() {
        return state;
    }

    /**
     * Retourne la liste complète de tous les fichiers qui forment la tâche.
     *
     * @return Les fichiers de la tâche.
     */
    public List<File> getTousFichiers() {
        return new ArrayList(fichiersOriginaux);
    }

    /**
     * Retourne la liste des fichiers qui sont réellement analysés, par exemple,
     * dans le cas d'une analyse par concaténation, les fichiers concaténés.
     *
     * @return Les fichiers analysés.
     */
    public List<File> getFichiersResultats() {
        return fichiersAnalyse;
    }

    /**
     * Permet d'ajouter une source aux sources. Associe ensuite les fichiers aux
     * fichiers de la source.
     *
     * @param source La nouvelle source.
     */
    public void ajouterSource(File source) {
        modifie = true;

        if (sources == null) {
            sources = new HashMap();
        }

        if (!sources.containsKey(source) && source.isDirectory()) {
            sources.put(source, new ArrayList());
        }

        trouverFichiersSource(source);
    }

    /**
     * Trouve les sources des fichiers de la tâche.
     *
     * @return Les sources des fichiers de la tâche.
     */
    private void trouverFichiersSource(File source) {
        File ancetreCommun = extractCommonAncestor();
        for (File fichier : fichiersAnalyse) {
            File parent = fichier.getParentFile();

            if (parent == source) {
                sources.get(source).add(fichier);
            } else {
                while (parent != null
                        && !parent.equals(ancetreCommun)
                        && !parent.equals(source)) {
                    parent = parent.getParentFile();
                }

                if (parent != null && parent.equals(source)) {
                    sources.get(source).add(fichier);
                }
            }
        }
    }

    private void regenererSources() {
        for (File source : sources.keySet()) {
            sources.get(source).clear();
            trouverFichiersSource(source);

            if (sources.get(source).isEmpty()) {
                sources.remove(source);
            }
        }
    }

    /**
     * Retire une source des sources et replace les fichiers dans la liste de
     * fichiers pour l'analyse si l'analyse se fait par concaténation.
     *
     * @param source La source à retirer.
     */
    public void retirerSource(File source) {
        modifie = true;

        if (sources != null) {
            sources.remove(source);
        }
    }

    /**
     * Vérifie si deux fichiers appartiennent à une même source, pour ne pas les
     * comparer entre eux.
     *
     * @param fichier1 Le premier fichier à vérifier.
     * @param fichier2 Le deuxième fichier à vérifier.
     * @return Si les fichiers appartiennent à la même source.
     */
    public boolean verifierSiMemeSource(File fichier1, File fichier2) {
        for (List<File> fichiers : sources.values()) {
            if (fichiers.contains(fichier1) && fichiers.contains(fichier2)) {
                return true;
            }
        }

        return false;
    }

    /**
     * @return Tous les fichiers, classés selon leur premier ancêtre commun.
     */
    public Map<File, File[]> getFilesByRoot() {
        Map<File, File[]> ret = new HashMap();

        for (File root : filesCommonAncestors) {
            List<File> filesUnderThisRoot = new ArrayList();

            for (File file : fichiersOriginaux) {
                if (file.toPath().getRoot().toFile().equals(root)) {
                    filesUnderThisRoot.add(file);
                }
            }

            ret.put(root, filesUnderThisRoot.toArray(new File[0]));
        }

        return ret;
    }

    /**
     * Permet d'accéder à un fichier selon un indice.
     *
     * @param indice L'indice du fichier.
     * @return Le fichier demandé, ou null.
     */
    public File getFile(int indice) {
        if (fichiersAnalyse != null && indice < fichiersAnalyse.size()) {
            return fichiersAnalyse.get(indice);
        } else {
            return null;
        }
    }

    /**
     * Permet d'accéder aux ancêtres communs des fichiers.
     *
     * @return Les ancêtres communs des fichiers.
     */
    public File[] getCommonAncestors() {
        if (filesCommonAncestors == null) {
            return null;
        } else {
            return filesCommonAncestors.clone();
        }
    }

    /**
     * Lorsqu'on veut forcer le type d'analyse malgré les préférences.
     *
     * @param cat Le type d'analyse.
     */
    public void setConcatenation(boolean cat) {
        prefs = null;
        modifie = true;

        analyseConcatenation = cat;
    }

    /**
     * Permet de modifier le rapport de la tâche.
     *
     * @param JReport Le nouveau rapport.
     */
    public void setJReport(String JReport) {
        modifie = true;
        rapport = JReport;
    }

    /**
     * Permet d'accéder au rapport de la tâche.
     *
     * @return Le rapport de la tâche.
     */
    public String getJReport() {
        return rapport;
    }

    /**
     * Indique si la tâche a été modifiée depuis la dernière vérification.
     *
     * @return Si la tâche a été modifiée.
     */
    public boolean getModifie() {
        return modifie;
    }

    /**
     * Permet d'accéder au titre de la tâche.
     *
     * @return Le titre de la tâche.
     */
    public String getTitre() {
        return titre;
    }

    /**
     * Permet de modifier le titre de la tâche.
     *
     * @param titre Le titre de la tâche.
     */
    public void setTitre(String titre) {
        this.titre = titre;
        modifie = true;
    }

    /**
     * Permet de modifier le gestionnaire de préférences associé à la tâche.
     *
     * @param prefs
     */
    public void setPrefs(GestionnairePreferences prefs) {
        this.prefs = prefs;
        /*if (prefs.readPref("COMMENTAIRES", false) != null) {
            checkBoxCommentaires = (boolean) prefs.readPref("COMMENTAIRES", false);
        }
        if (prefs.readPref("WHITESPACES", false) != null) {
            checkBoxWhitepsaces = (boolean) prefs.readPref("WHITESPACES", false);
        }
        if (prefs.readPref("PREVIEW", false) != null) {
            jCheckBoxPreviewFiles = (boolean) prefs.readPref("PREVIEW", false);
        }
        if (prefs.readPref("CONCATENATION", false) != null) {
            jCheckBoxAnalyseConcatenation = (boolean) prefs.readPref("CONCATENATION", false);
        }
        if (prefs.readPref("EXTRACT_PDF", false) != null) {
            pdfExtractor = (boolean) prefs.readPref("EXTRACT_PDF", false);
        }
        if (prefs.readPref("EXTRACT_IMG", false) != null) {
            pdfImages = (boolean) prefs.readPref("EXTRACT_IMG", false);
        }*/
    }
    
    /**
     * Permet de modifier le gestionnaire de préférences associé à la tâche.
     *
     * @param prefs
     */
    public void setPrefsPourTests(GestionnairePreferences prefs) {
        this.prefs = prefs;
        if (prefs.readPref("COMMENTAIRES", false) != null) {
            checkBoxCommentaires = (boolean) prefs.readPref("COMMENTAIRES", false);
        }
        if (prefs.readPref("WHITESPACES", false) != null) {
            checkBoxWhitepsaces = (boolean) prefs.readPref("WHITESPACES", false);
        }
        if (prefs.readPref("PREVIEW", false) != null) {
            jCheckBoxPreviewFiles = (boolean) prefs.readPref("PREVIEW", false);
        }
        if (prefs.readPref("CONCATENATION", false) != null) {
            jCheckBoxAnalyseConcatenation = (boolean) prefs.readPref("CONCATENATION", false);
        }
        if (prefs.readPref("EXTRACT_PDF", false) != null) {
            pdfExtractor = (boolean) prefs.readPref("EXTRACT_PDF", false);
        }
        if (prefs.readPref("EXTRACT_IMG", false) != null) {
            pdfImages = (boolean) prefs.readPref("EXTRACT_IMG", false);
        }
    }

    @Override
    public StringBuffer toXml() {
        modifie = false;

        StringBuffer str = new StringBuffer();

        str.append("<onglet>\n");
        if (this.estSommaire()) {
            str.append("<titre sommaire=\"True\">");
        } else {
            str.append("<titre sommaire=\"False\">");
        }
        str.append(titre);
        str.append("</titre>\n");
        str.append("<rapport>");
        str.append(rapport);
        str.append("</rapport>\n");
        str.append("<analys>\n");
        str.append("<fichs>\n");

        for (File f : fichiersOriginaux) {

            // 1. retourne le hash du fichier en question
            String strHash = null;
            try {
                strHash = convertFileToMD5Str(f.getAbsolutePath());
            } catch (Exception e) {
                // rien
            }

            str.append("<file " + "hash='" + strHash + "'" + ">");
            str.append(SaveAndRestore.escape(f.getAbsolutePath()));
            str.append("</file>\n");
        }

        str.append("</fichs>\n");
        str.append("<sources>\n");
        for (File dossier : sources.keySet()) {
            str.append("<source>").append(dossier).append("</source>\n");
        }
        str.append("</sources>\n");
        if (resultatsAnalyse != null && !analyseEnCours) {
            str.append("<res len=\"");
            str.append(resultatsAnalyse.getLength());
            str.append("\">\n");

            int i = 0;
            for (float[] t : resultatsAnalyse.getValues()) {
                str.append("<li len=\"");
                str.append(t.length);
                str.append("\">\n");

                int j = 0;
                for (float f : t) {
                    str.append("<l i=\"");
                    str.append(i);
                    str.append("\" j=\"");
                    str.append(j);
                    str.append("\">");
                    str.append(f);
                    str.append("</l>\n");

                    j++;
                }
                str.append("</li>\n");
                i++;
            }
        } else {
            // Le résultat ne peut pas être null, car on ne peut pas passer un
            // fichier null, sinon ça plante.
            str.append("<res len=\"").append(0).append("\">\n");
        }

        str.append("</res>\n");

        str.append("<options>\n");

        try {
            bPreview = (boolean) prefs.readPref("PREVIEW", false);
        } catch (NullPointerException e) {
        }
        str.append("<preview>" + bPreview + "</preview>\n");

        try {
            bConcatenation = (boolean) prefs.readPref("CONCATENATION", false);
        } catch (NullPointerException e) {
        }
        str.append("<concatenation>" + bConcatenation + "</concatenation>\n");

        try {
            bCommentaires = (boolean) prefs.readPref("COMMENTAIRES", false);
        } catch (NullPointerException e) {
        }
        str.append("<commentaires>" + bCommentaires + "</commentaires>\n");

        try {
            bWhiteSpaces = (boolean) prefs.readPref("WHITESPACES", false);
        } catch (NullPointerException e) {
        }
        str.append("<whitespaces>" + bWhiteSpaces + "</whitespaces>\n");

        try {
            bExtrairePDF = (boolean) prefs.readPref("EXTRACT_PDF", false);
        } catch (NullPointerException e) {
        }
        str.append("<extrairepdf>" + bExtrairePDF + "</extrairepdf>\n");

        try {
            bExtraireImagePDF = (boolean) prefs.readPref("EXTRACT_IMG", false);
        } catch (NullPointerException e) {
        }
        str.append("<extraireimages>" + bExtraireImagePDF + "</extraireimages>\n");

        str.append("</options>\n");
        IDTache = "Saved";
        str.append("<id>" + IDTache + "</id>\n");
        str.append("<optspecifiques>\n");

        str.append("<preview>" + jCheckBoxPreviewFiles + "</preview>\n");
        str.append("<concatenation>" + jCheckBoxAnalyseConcatenation + "</concatenation>\n");
        str.append("<extrairepdf>" + pdfExtractor + "</extrairepdf>\n");
        str.append("<extraireimages>" + pdfImages + "</extraireimages>\n");
        str.append("<commentaires>" + checkBoxCommentaires + "</commentaires>\n");
        str.append("<whitespaces>" + checkBoxWhitepsaces + "</whitespaces>\n");
        str.append("<progressive>" + isProgressive + "</progressive>\n");
        str.append("<redlimit>" + redLimitTxt + "</redlimit>\n");
        str.append("<yellowlimit>" + yellowLimitTxt + "</yellowlimit>\n");
        str.append("<greenlimit>" + greenLimitTxt + "</greenlimit>\n");
        str.append("</optspecifiques>\n");
        str.append("</analys>\n");
        str.append("</onglet>\n");

        return str;
    }

    public String convertFileToMD5Str(String path) throws FileNotFoundException, IOException, NoSuchAlgorithmException {
        File fichier = new File(path);

        BufferedReader br = new BufferedReader(new FileReader(fichier));

        String contenu = "";
        while ((br.readLine()) != null) {
            contenu += br.readLine();
        }

        MessageDigest m = MessageDigest.getInstance("MD5");
        m.reset();
        m.update(contenu.getBytes());
        byte[] digest = m.digest();
        BigInteger bigInt = new BigInteger(1, digest);
        String hashText = bigInt.toString(16);
        // Now we need to zero pad it if you actually want the full 32 chars.
        while (hashText.length() < 32) {
            hashText = "0" + hashText;
        }

        return hashText;
    }

    @Override
    public void fromDom(Node node) {
        estRestauree = false;
        state = 1;
        for (int i = 0; i < node.getChildNodes().getLength(); i++) {
            if (node.getChildNodes().item(i).getNodeName() != null) {
                switch (node.getChildNodes().item(i).getNodeName()) {
                    case "titre":
                        titre = node.getChildNodes().item(i).getTextContent();
                        break;
                    case "rapport":
                        rapport = node.getChildNodes().item(i).getTextContent();
                        break;
                    case "analys":
                        NodeList analyse
                                = node.getChildNodes().item(i).getChildNodes();
                         {
                            try {
                                setFileListFromXMLNode(analyse);
                            } catch (IOException ex) {
                                Logger.getLogger(Task.class.getName()).log(Level.SEVERE, null, ex);
                            } catch (NoSuchAlgorithmException ex) {
                                Logger.getLogger(Task.class.getName()).log(Level.SEVERE, null, ex);
                            }
                        }
                        break;

                }
            }
        }

        modifie = false;
    }

    private void setFileListFromXMLNode(NodeList analyse) throws IOException, FileNotFoundException, NoSuchAlgorithmException {
        List<File> list = null;
        float[][] resmat = null;
        erreurRestauration = false;

        for (int j = 0; j < analyse.getLength(); j++) {
            switch (analyse.item(j).getNodeName()) {
                case "fichs": {
                    NodeList fichml = analyse.item(j).getChildNodes();
                    list = new ArrayList();

                    for (int k = 0; k < fichml.getLength(); k++) {
                        if (fichml.item(k).getNodeName().equals("file")) {
                            String nomFichier = fichml.item(k)
                                    .getTextContent().trim();
                            File f = new File(nomFichier);

                            Element e = (Element) fichml.item(k);

                            if (f.exists()) {
                                if ((e.getAttribute("hash")).equals(convertFileToMD5Str(nomFichier))) {
                                    list.add(f); //seuls les fichiers existants et non modifiés sont ajoutés à la liste
                                } else {
                                    //le fichier a été modifié
                                    erreurRestauration = true;
                                }
                            } else {
                                //le fichier n'existe pas.
                                erreurRestauration = true;
                            }
                        }
                    }
                    if (erreurRestauration == false) {
                        setFichiers(list);
                        modifie = false;
                    }

                }
                break;
                case "sources": {
                    NodeList fichml = analyse.item(j).getChildNodes();
                    for (int k = 0; k < fichml.getLength(); k++) {
                        if (fichml.item(k).getNodeName().equals("source")) {
                            File f = new File(fichml.item(k)
                                    .getTextContent().trim());
                            ajouterSource(f);
                        }
                    }
                }
                break;
                case "res": {
                    resmat = getMatFromXMLNode(Integer.parseInt(
                            analyse.item(j).getAttributes()
                                    .getNamedItem("len").getTextContent()),
                            analyse.item(j).getChildNodes());
                }
                break;
                case "options": {
                    NodeList options = analyse.item(j).getChildNodes();
                    for (int i = 0; i < options.getLength(); i++) {
                        switch (options.item(i).getNodeName()) {
                            case "preview":
                                bPreview = Boolean.parseBoolean(options.item(i).getTextContent());
                                prefs.writePref("PREVIEW", bPreview);
                                break;
                            case "concatenation":
                                bConcatenation = Boolean.parseBoolean(options.item(i).getTextContent());
                                prefs.writePref("CONCATENATION", bConcatenation);
                                break;
                            case "commentaires":
                                bCommentaires = Boolean.parseBoolean(options.item(i).getTextContent());
                                prefs.writePref("COMMENTAIRES", bCommentaires);
                                break;
                            case "whitespaces":
                                bWhiteSpaces = Boolean.parseBoolean(options.item(i).getTextContent());
                                prefs.writePref("WHITESPACES", bWhiteSpaces);
                                break;
                            case "extrairepdf":
                                bExtrairePDF = Boolean.parseBoolean(options.item(i).getTextContent());
                                prefs.writePref("EXTRACT_PDF", bExtrairePDF);
                                break;
                            case "extraireimages":
                                bExtraireImagePDF = Boolean.parseBoolean(options.item(i).getTextContent());
                                prefs.writePref("EXTRACT_IMG", bExtraireImagePDF);
                        }
                    }
                }
                case "id":
                    IDTache = analyse.item(j).getTextContent();
                    break;
                case "optspecifiques":
                    NodeList optSpec = analyse.item(j).getChildNodes();
                    for (int i = 0; i < optSpec.getLength(); i++) {
                        switch (optSpec.item(i).getNodeName()) {
                            case "preview":
                                jCheckBoxPreviewFiles = Boolean.parseBoolean(optSpec.item(i).getTextContent());
                                break;
                            case "concatenation":
                                jCheckBoxAnalyseConcatenation = Boolean.parseBoolean(optSpec.item(i).getTextContent());
                                break;
                            case "extrairepdf":
                                pdfExtractor = Boolean.parseBoolean(optSpec.item(i).getTextContent());
                                break;
                            case "extraireimages":
                                pdfImages = Boolean.parseBoolean(optSpec.item(i).getTextContent());
                                break;
                            case "commentaires":
                                checkBoxCommentaires = Boolean.parseBoolean(optSpec.item(i).getTextContent());
                                break;
                            case "whitespaces":
                                checkBoxWhitepsaces = Boolean.parseBoolean(optSpec.item(i).getTextContent());
                                break;
                            case "progressive":
                                isProgressive = Boolean.parseBoolean(optSpec.item(i).getTextContent());
                                break;
                            case "redlimit":
                                  try {
                                redLimit = Double.parseDouble(optSpec.item(i).getTextContent());
                                redLimitTxt = optSpec.item(i).getTextContent();
                            } catch (NumberFormatException e) {
                                redLimitTxt = optSpec.item(i).getTextContent();
                            }
                            break;
                            case "yellowlimit":
                                  try {
                                yellowLimit = Double.parseDouble(optSpec.item(i).getTextContent());
                                yellowLimitTxt = optSpec.item(i).getTextContent();
                            } catch (NumberFormatException e) {
                                yellowLimitTxt = optSpec.item(i).getTextContent();
                            }
                            break;
                            case "greenlimit":
                                  try {
                                greenLimit = Double.parseDouble(optSpec.item(i).getTextContent());
                                greenLimitTxt = optSpec.item(i).getTextContent();
                            } catch (NumberFormatException e) {
                                greenLimitTxt = optSpec.item(i).getTextContent();
                            }
                            break;
                        }
                    }
                    break;
            }
        }

        if (erreurRestauration == false) {
            if (resmat != null && resmat.length != 0) {
                setExRes(list, resmat);
            }
        }

    }

    private float[][] getMatFromXMLNode(int taille, NodeList liste) {
        float[][] matrice = new float[taille][];

        int a = 0;
        for (int i = 0; i < liste.getLength(); i++) {
            if ("li".equals(liste.item(i).getNodeName())) {
                NodeList m = liste.item(i).getChildNodes();
                matrice[a] = new float[Integer.parseInt(
                        liste.item(i).getAttributes()
                                .getNamedItem("len").getTextContent())];

                int b = 0;
                for (int j = 0; j < m.getLength(); j++) {
                    if ("l".equals(m.item(j).getNodeName())) {
                        matrice[a][b]
                                = Float.parseFloat(m.item(j).getTextContent());
                        b++;
                    }
                }
                a++;
            }
        }

        return matrice;
    }

    /**
     * Indique ne pas être un sommaire.
     *
     * @return false
     */
    public boolean estSommaire() {
        return false;
    }

    /**
     * Retourne vrai si le dossier passé en paramètre est une source.
     *
     * @param folder Le dossier à tester.
     * @return Si le dossier est une source.
     */
    public boolean isSource(File folder) {
        return sources.keySet().contains(folder);
    }

    /**
     * Supprime la source qui lie les fichiers du dossier.
     *
     * @param folder Le dossier représentant une source.
     */
    public void supprimerSource(File folder) {
        if (sources.keySet().contains(folder)) {
            sources.remove(folder);
        }
    }

    public boolean contientTache(Task tache) {
        return false;
    }

    /**
     * Indique si la tâche a été restaurée ou non.
     *
     * @return Si la tâche a été restaurée ou non.
     */
    public boolean estRestauree() {
        return estRestauree;
    }

    /**
     * Permet de marquer la tâche comme restaurée.
     */
    public void marquerCommeRestauree() {
        estRestauree = true;
    }

    public boolean verifierAnalyseFaite() {
        for (int i = 0; i < resultatsAnalyse.getValues().length; i++) {
            for (int j = 0; j < resultatsAnalyse.getValues()[i].length; j++) {
                if (resultatsAnalyse.getResAt(i, j) < 0) {
                    return false;
                }
            }
        }
        return true;
    }

    /**
     * Retourne vrai si le dossier passé en paramètre contient au moins un
     * sous-dossier qui se retrouve dans le tableau de fichiers.
     *
     * @param dir Dossier à vérifier.
     * @return Si le dossier se trouve dans les fichiers.
     */
    public boolean hasSubdirectoriesInFiles(File dir) {
        if (dir.isDirectory()) {
            String path = dir.getAbsolutePath();

            for (File file : fichiersAnalyse) {
                File parent = file.getParentFile();
                String chemin = parent.getAbsolutePath();

                if (!parent.equals(dir)
                        && chemin.startsWith(path)) {
                    return true;
                }
            }

            return false;
        }

        return false;
    }

    @Override
    public void ajouterObservateur(Observateur ob) { // tester
        if (!observateurs.contains(ob)) {
            observateurs.add(ob);
        }
    }

    @Override
    public void aviserObservateurs() { //tester
        for (Observateur ob : observateurs) {
            ob.changementEtat();
        }
    }

    @Override
    public synchronized void aviserObservateurs(Enum<?> property, Object o) { // tester
        for (Observateur ob : observateurs) {
            ob.changementEtat(property, o);
        }
    }

    @Override
    public void retirerObservateur(Observateur ob) { //tester
        observateurs.remove(ob);
    }

    public static void trier(List<File> fichiers, MatriceTriangulaire resultats, Tri.Type type) {
        if (fichiers != null && resultats != null) {
            if (fichiers.size() == resultats.getLength()) {
                Tri.trier(fichiers, resultats, type);
            }
        }
    }

    /**
     * Retourne la valeur booléenne attestant si la restauration s'est bien
     * passée ou non.
     *
     * @return erreurRestauration = valeur booléenne vraie si le projet n'a pas
     * bien été restauré.
     */
    public boolean getEtatRestauration() {
        return erreurRestauration;
    }

    public void setInvalide(boolean b) {
        invalide = b;
    }
    
    public boolean getInvalide(){
        return invalide;
    }
}
