package ca.qc.bdeb.baldr.noyau;

import ca.qc.bdeb.baldr.utils.Observation;
import ca.qc.bdeb.baldr.main.Main;
import ca.qc.bdeb.baldr.utils.Observateur;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.junit.After;
import org.junit.AfterClass;
import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 *
 * @author etudiants
 */
public class TaskTest {

    private List<File> fichiers;
    private static OuvertureFichier file = new OuvertureFichier();

    public TaskTest() throws URISyntaxException {
        fichiers = FaireTableauFile();
    }

    @BeforeClass
    public static void setUpClass() {
    }

    @AfterClass
    public static void tearDownClass() {
    }

    @Before
    public void setUp() {
    }

    @After
    public void tearDown() {
    }

    public final List<File> FaireTableauFile() throws URISyntaxException {

        List<File> fichier = new ArrayList();
        fichier.add(file.Ouverture("/LoremIpsum1.txt"));
        fichier.add(file.Ouverture("/LoremIpsum2.txt"));
        fichier.add(file.Ouverture("/LoremIpsum3.txt"));
        fichier.add(file.Ouverture("/LoremIpsum4.txt"));
        fichier.add(file.Ouverture("/LoremIpsum5.txt"));
        fichier.add(file.Ouverture("/LoremIpsum6.txt"));
        return fichier;
    }

    public List<File> FaireTableauFilePourComparaison() throws URISyntaxException {
        List<File> fichier = new ArrayList();
        fichier.add(file.Ouverture("/LoremIpsum1.txt"));
        fichier.add(file.Ouverture("/LoremIpsum2.txt"));
        fichier.add(file.Ouverture("/LoremIpsum3.txt"));
        fichier.add(file.Ouverture("/LoremIpsum4.txt"));
        fichier.add(file.Ouverture("/LoremIpsum5.txt"));
        fichier.add(file.Ouverture("/LoremIpsum6.txt"));
        return fichier;
    }

    /**
     * Vérifie qu'il y a un fichier de moins et un résultats de moins dans
     * l'analyse
     *
     * @throws URISyntaxException
     */
    @Test
    public void testEnleverUnFichier() throws URISyntaxException {
        Task tache = new Task();

        tache.setConcatenation(false);

        tache.setFichiers(fichiers);
        tache.lancerAnalyse();

        assertEquals(fichiers.size(), tache.getTousFichiers().size());
        assertEquals(fichiers.size(), tache.getResults().getLength());

        tache.retirerFichierEtMettreAJourMatrice(fichiers.get(5));
        assertTrue(tache.getModifie());
        assertEquals(fichiers.size() - 1, tache.getTousFichiers().size());
        assertEquals(fichiers.size() - 1, tache.getResults().getLength());
    }

    /**
     * Vérifie que la suppression de plusieurs fichiers fonctionne bien.
     *
     * @throws URISyntaxException
     */
    @Test
    public void testMettreAJourFichiers() throws URISyntaxException {
        Task tache = new Task();
        tache.setConcatenation(false);

        tache.setFichiers(fichiers);
        tache.lancerAnalyse();

        assertEquals(fichiers.size(), tache.getTousFichiers().size());
        assertEquals(fichiers.size(), tache.getResults().getLength());

        List<File> nouveauxFichiers = new ArrayList(fichiers);
        nouveauxFichiers.remove(0);
        nouveauxFichiers.remove(1);

        tache.mettreAJourFichiersEtMettreAJourMatrice(nouveauxFichiers);

        assertEquals(nouveauxFichiers.size(),
                tache.getTousFichiers().size());
        assertEquals(nouveauxFichiers.size(),
                tache.getResults().getLength());
    }

    /**
     * Vérifie que la suppression de plusieurs fichiers fonctionne bien.
     *
     * @throws URISyntaxException
     */
    @Test
    public void testMettreAJourFichiersConcatenation()
            throws URISyntaxException {
        Task tache = new Task();
        tache.setConcatenation(true);

        tache.setFichiers(fichiers);

        tache.ajouterSource(fichiers.get(0).getParentFile());

        tache.lancerAnalyse();

        assertEquals(fichiers.size(), tache.getTousFichiers().size());
        assertEquals(1, tache.getResults().getLength());

        List<File> nouveauxFichiers = new ArrayList(fichiers);
        nouveauxFichiers.remove(0);
        nouveauxFichiers.remove(1);

        tache.mettreAJourFichiersEtMettreAJourMatrice(nouveauxFichiers);

        assertEquals(nouveauxFichiers.size(),
                tache.getTousFichiers().size());
        assertEquals(1, tache.getResults().getLength());
    }

    /**
     * Vérifie si une analyse avec des fichiers de plus a une incidence sur les
     * résultats précédents
     *
     * @throws URISyntaxException
     */
    @Test
    public void faireTestComparaison() throws URISyntaxException {
        Task tache = new Task();
        tache.setConcatenation(false);

        tache.setFichiers(fichiers);
        tache.lancerAnalyse();
        Task tacheMoinsUnFichier = new Task();
        tacheMoinsUnFichier.setConcatenation(false);
        List<File> fichiers2 = FaireTableauFilePourComparaison();
        tacheMoinsUnFichier.setFichiers(fichiers2);
        tacheMoinsUnFichier.lancerAnalyse();
        tacheMoinsUnFichier.retirerFichierEtMettreAJourMatrice(fichiers.get(5));
        
        assertEquals(
                tache.getResults().getValues().length - 1,
                tacheMoinsUnFichier.getResults().getValues().length);
    }
    
    @Test
    public void testerGetRes() throws URISyntaxException {
        Task tache = new Task();
        tache.setConcatenation(false);

        tache.setFichiers(fichiers);
        tache.lancerAnalyse();
        
        assertEquals(0.8826190233230591, tache.getRes(fichiers.get(0), fichiers.get(1)), 0);
    }

    @Test
    public void testStop() throws URISyntaxException {
        final Task tache = new Task();
        tache.setConcatenation(false);

        tache.setFichiers(fichiers);
        Thread analyse = new Thread() {
            @Override
            public void run() {
                tache.lancerAnalyse();
            }
        };
        
        analyse.start();
        analyse.interrupt();
        
        try {
            analyse.join();
        } catch (InterruptedException ex) {
            Logger.getLogger(TaskTest.class.getName()).log(Level.SEVERE, null, ex);
            fail("Erreur lors de l'interruption du thread d'analyse");
        }
        
        assertEquals(-1, tache.getRes(fichiers.get(0), fichiers.get(1)), 0);
    }

    @Test
    public void testConcatenerFichiersSansSources() {
        Task tache = new Task();
        tache.setConcatenation(true);

        tache.lancerAnalyse();

        List<File> fichiersResultats = tache.getFichiersResultats();

        assertEquals(0, fichiersResultats.size());
    }

    @Test
    public void testConcatenerFichiers() {
        Task tache = new Task();
        tache.setFichiers(fichiers);
        tache.ajouterSource(fichiers.get(0).getParentFile());

        List<File> fichiersResultats = tache.getFichiersResultats();

        // Avant la concaténation, on s'assure qu'il y a plus de fichiers
        // qu'après.
        assertEquals(fichiers.size(), fichiersResultats.size());

        tache.setConcatenation(true);
        tache.lancerAnalyse();

        fichiersResultats = tache.getFichiersResultats();

        assertEquals(1, fichiersResultats.size());
    }

    @Test
    public void testIsSource() throws URISyntaxException {
        List<File> fichier = new ArrayList();
        fichier.add(file.Ouverture("/LoremIpsum1.txt"));
        Task tache = new Task();
        tache.setFichiers(fichier);
        File Dossier = file.Ouverture("");
        tache.ajouterSource(Dossier);
        assertTrue(tache.isSource(Dossier));
    }

    @Test
    public void testRetirerSource() {
        Task tache = new Task();

        File source = fichiers.get(0).getParentFile();

        tache.setFichiers(fichiers);
        tache.ajouterSource(source);

        int nombreFichiersSansSource = tache.getFichiersResultats().size();

        assertTrue(tache.isSource(source));

        tache.retirerSource(source);

        assertFalse(tache.isSource(source));

        assertEquals(nombreFichiersSansSource,
                tache.getFichiersResultats().size());
    }

    @Test
    public void estSommaireTest() {
        Task tache = new Task();
        assertFalse(tache.estSommaire());
    }
    
    @Test
    public void TesterCacherFichier(){
        Task tache = new Task();
        tache.setFichiers(fichiers);
        tache.CacherFichierEtMettreAJourMatrice(fichiers.get(0));
        assertEquals(fichiers.get(0), fichiers.get(0));
    }

    @Test
    public void testSetFiles() {
        Task tache = new Task();
        List<File> tabFichiersDansRepertoires = new ArrayList();

        if (Main.isUnix) {
            tabFichiersDansRepertoires.add(new File("/home/user/fic1"));
            tabFichiersDansRepertoires.add(new File("/home/user/fic2"));
            tabFichiersDansRepertoires.add(new File("/home/user/rep1/fic3"));
        } else if (Main.isMac) {
            tabFichiersDansRepertoires.add(new File("/Users/user/fic1"));
            tabFichiersDansRepertoires.add(new File("/Users/user/fic1"));
            tabFichiersDansRepertoires.add(new File("/Users/user/rep1/fic3"));
        } else {
            tabFichiersDansRepertoires.add(new File("C:\\home\\user\\fic1"));
            tabFichiersDansRepertoires.add(new File("C:\\home\\user\\fic2"));
            tabFichiersDansRepertoires.add(new File("C:\\home\\user\\fic3"));
        }

        tache.setFichiers(tabFichiersDansRepertoires);
        File commonAncestor = tache.getCommonAncestors()[0];

        String expected;

        if (Main.isUnix) {
            expected = "/home/user"; // <- pas vrai pour tous les *NIX...
        } else if (Main.isMac) {
            expected = "/Users/user";
        } else {
            // On suppose que c'est Windows, mais idéalement, il faudrait
            // s'en assurer explicitement pour éviter des problèmes futurs.
            expected = "C:\\home\\user";
        }

        assertEquals(expected, commonAncestor.getAbsolutePath());
    }

    @Test
    public void testFichiersDifferentsLecteurs() {
        if (!Main.isUnix && !Main.isMac) {
            Task tache = new Task();

            List<File> tabFichiers = new ArrayList();
            tabFichiers.add(new File("C:\\home\\user\\fic1"));
            tabFichiers.add(new File("G:\\rep\\fic3"));

            tache.setFichiers(tabFichiers);

            Map<File, File[]> fichiersClasses = tache.getFilesByRoot();

            assertTrue(fichiersClasses.containsKey(new File("C:\\")));
            assertTrue(fichiersClasses.containsKey(new File("G:\\")));

            assertArrayEquals(fichiersClasses.get(new File("C:\\")),
                    new File[]{new File("C:\\home\\user\\fic1")});

            assertArrayEquals(fichiersClasses.get(new File("G:\\")),
                    new File[]{new File("G:\\rep\\fic3")});
        }
    }

    @Test
    public void testVerifierOccurenceSousDossier() {
        Task tache = new Task();
        List<File> tabFichiersDansRepertoires = new ArrayList();

        File file1 = new File("src"
                + File.separator + "test"
                + File.separator + "resources"
                + File.separator + "sousDossier"
                + File.separator + "LoremIpsum1.txt")
                .getAbsoluteFile();

        File file2 = new File("src"
                + File.separator + "test"
                + File.separator + "sousDossier"
                + File.separator + "LoremIpsum2.txt")
                .getAbsoluteFile();

        tabFichiersDansRepertoires.add(file1);
        tabFichiersDansRepertoires.add(file2);

        tache.setFichiers(tabFichiersDansRepertoires);

        File dossier1 = new File("src"
                + File.separator + "test"
                + File.separator + "resources")
                .getAbsoluteFile();

        File dossier2 = new File("src"
                + File.separator + "test"
                + File.separator + "differentSource")
                .getAbsoluteFile();

        assertTrue(tache.hasSubdirectoriesInFiles(dossier1));
        assertFalse(tache.hasSubdirectoriesInFiles(dossier2));
        assertFalse(tache.hasSubdirectoriesInFiles(file2));
    }

    @Test
    public void testFilesNull() {
        Task tache = new Task();
        tache.setFichiers(null);

        assertEquals(Collections.EMPTY_LIST, tache.getTousFichiers());
    }

    @Test
    public void testGetFile() throws URISyntaxException {

        ArrayList<File> fichierAttendu = new ArrayList();
        fichierAttendu.add(file.Ouverture("/LoremIpsum6.txt"));
        fichierAttendu.add(file.Ouverture("/LoremIpsum5.txt"));
        fichierAttendu.add(file.Ouverture("/LoremIpsum4.txt"));
        fichierAttendu.add(file.Ouverture("/LoremIpsum3.txt"));
        fichierAttendu.add(file.Ouverture("/LoremIpsum2.txt"));
        fichierAttendu.add(file.Ouverture("/LoremIpsum1.txt"));

        Task tache = new Task();
        tache.setFichiers(fichierAttendu);

        MatriceTriangulaire resultats = tache.getResults();
        List<File> fichiersResultats = tache.getFichiersResultats();
        Task.trier(fichiersResultats, resultats, Tri.Type.AlphabetiqueDecroissant);

        for (int j = 0; j < fichierAttendu.size(); j++) {
            assertEquals(fichierAttendu.get(j), fichiersResultats.get(j));
        }
    }

    @Test
    public void testGetFileNull() throws URISyntaxException {
        Task tache = new Task();
        assertNull(tache.getFile(0));
        tache.setFichiers(fichiers);
        assertNotNull(tache.getFile(0));
    }

    //Je doute de la pertinence de ce test
    @Test
    public void testMarquerRestauration() {
        Task tache = new Task();
        assertFalse("Le tache n'a pas été marquer comme restaurée",
                 tache.estRestauree());
        tache.marquerCommeRestauree();
        assertTrue(tache.estRestauree());
    }
    
    @Test
    public void testAjouterObservateur() {
        Sommaire sommaire = new Sommaire();
        TestObservateur observateur1 = new TestObservateur();
        sommaire.ajouterObservateur(observateur1);
        sommaire.aviserObservateurs();
        assertEquals(1, observateur1.nombreChangement);
    }

    @Test
    public void testEnleverObservateur() {

        Sommaire sommaire = new Sommaire();

        TestObservateur observateur1 = new TestObservateur();
        sommaire.ajouterObservateur(observateur1);
        sommaire.aviserObservateurs(Observation.AJOUTEONGLET, null);
        assertEquals(1, observateur1.nombreChangement);
        sommaire.retirerObservateur(observateur1);
        sommaire.aviserObservateurs(Observation.AJOUTEONGLET, null);
        assertEquals(1, observateur1.nombreChangement);

    }

    @Test
    public void testVerifierAnalyseFaite() {
        Task tache = new Task();
        tache.setFichiers(fichiers);
        tache.lancerAnalyse();
        boolean analyseFaite = tache.verifierAnalyseFaite();
        assertTrue("L'analyse n'a pas été faite!", analyseFaite);
    }

    @Test
    public void testMakeComp() throws URISyntaxException {
        Task tache = new Task();
        tache.setFichiers(fichiers);
        GestionnairePreferences prefs = new GestionnairePreferences(getClass());
        prefs.writePref("COMMENTAIRES", true);
        tache.setPrefs(prefs);
        tache.lancerAnalyse();
        boolean analyseFaite = tache.verifierAnalyseFaite();
        assertTrue("L'analyse n'a pas été faite!", analyseFaite);
    }

    @Test
    public void testAnalyseConcatenation() throws URISyntaxException {
        Task tache = new Task();

        File fichier = file.Ouverture("/sousDossier/LoremIpsum1.txt");

        ArrayList<File> listeFichiers = new ArrayList<>();
        listeFichiers.add(fichier);
        for (File f : fichiers) {
            listeFichiers.add(f);
        }

        tache.setFichiers(listeFichiers);
        GestionnairePreferences prefs = new GestionnairePreferences(getClass());
        prefs.writePref("CONCATENATION", true);
        tache.setPrefs(prefs);
        File sourceJava = listeFichiers.get(0).getParentFile();
        File sourceTxt = listeFichiers.get(1).getParentFile();
        tache.ajouterSource(sourceJava);
        tache.ajouterSource(sourceTxt);
        tache.lancerAnalyse();

        boolean analyseFaite = tache.verifierAnalyseFaite();
        assertTrue("L'analyse n'a pas été faite!", analyseFaite);
    }

    /**
     * Permet d'analyser un fichier normal source de verifier l'analyse d'un
     * fichier source avec un autre fichier source avec qui possède les mêmes
     * fichier à l'interieur
     */
    @Test
    public void testAnalyseConcatenationFichierContenuIdentique() throws URISyntaxException {
        Task tache = new Task();
        File fichier = file.Ouverture("/LoremIpsum1fois/LoremIpsum1.txt");
        ArrayList<File> listeFichiers = new ArrayList<>();
        listeFichiers.add(fichier);
        tache.setFichiers(listeFichiers);

        //Creation du dossier identique avec 5 fois le meme fichier txt
        Task tache2 = new Task();
        File fichier1_1 = file.Ouverture("/LoremIpsum1fois5/LoremIpsum1_1.txt");
        File fichier1_2 = file.Ouverture("/LoremIpsum1fois5/LoremIpsum1_2.txt");
        File fichier1_3 = file.Ouverture("/LoremIpsum1fois5/LoremIpsum1_3.txt");
        File fichier1_4 = file.Ouverture("/LoremIpsum1fois5/LoremIpsum1_4.txt");
        File fichier1_5 = file.Ouverture("/LoremIpsum1fois5/LoremIpsum1_5.txt");
        ArrayList<File> listeFichierTache2 = new ArrayList<>();
        listeFichierTache2.add(fichier1_1);
        listeFichierTache2.add(fichier1_2);
        listeFichierTache2.add(fichier1_3);
        listeFichierTache2.add(fichier1_4);
        listeFichierTache2.add(fichier1_5);
        tache2.setFichiers(listeFichierTache2);
        GestionnairePreferences prefs2 = new GestionnairePreferences(getClass());
        prefs2.writePref("CONCATENATION", true);
        tache2.setPrefs(prefs2);
        File sourceTxt1_1 = listeFichierTache2.get(0).getParentFile();

        tache2.ajouterSource(sourceTxt1_1);

        tache.lancerAnalyse();
        tache2.lancerAnalyse();

        assertArrayEquals(tache.getResults().getValues(), tache2.getResults().getValues());

    }

    //Cette methode permet de tester un dossier concatener soit different a un dossier normal sans source
    @Test
    public void testAnalyseConcatenationFichierContenuDifferent() throws URISyntaxException {
        Task tache = new Task();
        File fichier = file.Ouverture("/LoremIpsum2.txt");
        ArrayList<File> listeFichiers = new ArrayList<>();
        listeFichiers.add(fichier);

        File fichier1_1 = file.Ouverture("/LoremIpsum1fois5/LoremIpsum1_1.txt");
        File fichier1_2 = file.Ouverture("/LoremIpsum1fois5/LoremIpsum1_2.txt");
        File fichier1_3 = file.Ouverture("/LoremIpsum1fois5/LoremIpsum1_3.txt");
        File fichier1_4 = file.Ouverture("/LoremIpsum1fois5/LoremIpsum1_4.txt");
        File fichier1_5 = file.Ouverture("/LoremIpsum1fois5/LoremIpsum1_5.txt");
        listeFichiers.add(fichier1_1);
        listeFichiers.add(fichier1_2);
        listeFichiers.add(fichier1_3);
        listeFichiers.add(fichier1_4);
        listeFichiers.add(fichier1_5);
        tache.setFichiers(listeFichiers);

        GestionnairePreferences prefs2 = new GestionnairePreferences(getClass());
        prefs2.writePref("CONCATENATION", true);
        tache.setPrefs(prefs2);
        File sourceTxt1 = listeFichiers.get(0).getParentFile();

        tache.ajouterSource(sourceTxt1);

        File sourceTxt2 = listeFichiers.get(1).getParentFile();

        tache.ajouterSource(sourceTxt2);

        tache.lancerAnalyse();
        
        assertTrue(tache.getResults().getValues().length == 2);
    }

    @Test
    public void testEnleverCommentairesRessemblanceAssembleur() throws URISyntaxException {

        ArrayList<File> listeFichiers = new ArrayList<>();
        listeFichiers.add(new File(getClass().getResource("/commentaires/assembleur-avec.asm").toURI()));
        listeFichiers.add(new File(getClass().getResource("/commentairesDifferents/assembleur-different.asm").toURI()));

        GestionnairePreferences prefs = new GestionnairePreferences(getClass());
        prefs.writePref("COMMENTAIRES", false);
        double differenceAvecCommentaires = comparerDeuxFichiers(listeFichiers, prefs);
        prefs.writePref("COMMENTAIRES", true);
        double differenceSansCommentaires = comparerDeuxFichiers(listeFichiers, prefs);

        boolean differencePositive = (differenceAvecCommentaires - differenceSansCommentaires) > 0;

        assertTrue("Il n'y a pas de différence dans le retrait de commentaires",
                differencePositive);
    }
    
    @Test
    public void testEnleverCommentairesRessemblanceCSharp() throws URISyntaxException {

        ArrayList<File> listeFichiers = new ArrayList<>();
        listeFichiers.add(new File(getClass().getResource("/commentaires/csharp-avec.cs").toURI()));
        listeFichiers.add(new File(getClass().getResource("/commentairesDifferents/csharp-different.cs").toURI()));

        GestionnairePreferences prefs = new GestionnairePreferences(getClass());
        prefs.writePref("COMMENTAIRES", false);
        double differenceAvecCommentaires = comparerDeuxFichiers(listeFichiers, prefs);
        prefs.writePref("COMMENTAIRES", true);
        double differenceSansCommentaires = comparerDeuxFichiers(listeFichiers, prefs);

        boolean differencePositive = (differenceAvecCommentaires - differenceSansCommentaires) > 0;

        assertTrue("Il n'y a pas de différence dans le retrait de commentaires",
                differencePositive);
    }

    @Test
    public void testEnleverCommentairesRessemblancePython() throws URISyntaxException {

        ArrayList<File> listeFichiers = new ArrayList<>();
        listeFichiers.add(new File(getClass().getResource("/commentaires/python-avec.py").toURI()));
        listeFichiers.add(new File(getClass().getResource("/commentairesDifferents/python-different.py").toURI()));

        GestionnairePreferences prefs = new GestionnairePreferences(getClass());
        prefs.writePref("COMMENTAIRES", false);
        double differenceAvecCommentaires = comparerDeuxFichiers(listeFichiers, prefs);
        prefs.writePref("COMMENTAIRES", true);
        double differenceSansCommentaires = comparerDeuxFichiers(listeFichiers, prefs);

        boolean differencePositive = (differenceAvecCommentaires - differenceSansCommentaires) > 0;

        assertTrue("Il n'y a pas de différence dans le retrait de commentaires", differencePositive);
    }
    
    @Test
    public void testEnleverCommentairesRessemblanceSql() throws URISyntaxException {

        ArrayList<File> listeFichiers = new ArrayList<>();
        listeFichiers.add(new File(getClass().getResource("/commentaires/sql-avec.sql").toURI()));
        listeFichiers.add(new File(getClass().getResource("/commentairesDifferents/sql-different.sql").toURI()));

        GestionnairePreferences prefs = new GestionnairePreferences(getClass());
        prefs.writePref("COMMENTAIRES", false);
        double differenceAvecCommentaires = comparerDeuxFichiers(listeFichiers, prefs);
        prefs.writePref("COMMENTAIRES", true);
        double differenceSansCommentaires = comparerDeuxFichiers(listeFichiers, prefs);

        boolean differencePositive = (differenceAvecCommentaires - differenceSansCommentaires) > 0;

        assertTrue("Il n'y a pas de différence dans le retrait de commentaires", differencePositive);
    }
    
    // VERIFIE SI LE RETRAIT DE COMMENTAIRES ENLEVE AUSSI LES LIGNES.
    @Test
    public void testCompareRetraitCommentairesEtRetraitCommentairesLignes() throws URISyntaxException {
        ArrayList<File> listeFichiers = new ArrayList<>();
        listeFichiers.add(new File(getClass().getResource("/comparaisonCommentairesLignes/original.asm").toURI()));
        listeFichiers.add(new File(getClass().getResource("/comparaisonCommentairesLignes/lignescommentaires.asm").toURI()));
        
        GestionnairePreferences prefs = new GestionnairePreferences(getClass());
        
        prefs.writePref("COMMENTAIRES", true);
        prefs.writePref("WHITESPACES", false);
        double differenceSansCommentaires = comparerDeuxFichiers(listeFichiers, prefs);

        prefs.writePref("COMMENTAIRES", true);
        prefs.writePref("WHITESPACES", true);
        double differenceSansLignesCommentaires = comparerDeuxFichiers(listeFichiers, prefs);
        
        assertTrue(differenceSansCommentaires == differenceSansLignesCommentaires);
    }

    /*
    Méthode qui éxécute deux analyses; une avec des lignes blanches et l'autre
    en activant l'option qui enlève ces dernières.
    
    Ensuite, le résultat des deux analyses est comparée et le test passe si le
    résultat de la première analyse est plus grand que la deuxième analyse.
    
    Rappel: Plus que le résultat est proche à 0, plus qu'il y a de ressemblances
    entre les fichiers analysés.
    */
    @Test
    public void testEnleverLignesBlanches() throws URISyntaxException {

        ArrayList<File> listeFichiers = new ArrayList<>();        
        listeFichiers.add(new File(getClass().getResource("/lignesVides/avecLignesVides.txt").toURI()));
        listeFichiers.add(new File(getClass().getResource("/lignesVides/sansLignesVides.txt").toURI()));

        GestionnairePreferences prefs = new GestionnairePreferences(getClass());
        
        // Analyse des deux fichiers sans enlever les lignes blanches
        prefs.writePref("COMMENTAIRES", false);
        prefs.writePref("WHITESPACES", false);
        double differencetAvecLignesBlanches = comparerDeuxFichiers(listeFichiers, prefs);
        
        // Analyse des deux fichiers en enlevant les lignes blanches
        prefs.writePref("COMMENTAIRES", false);
        prefs.writePref("WHITESPACES", true);
        double differenceSansLignesBlanches = comparerDeuxFichiers(listeFichiers, prefs);
        
        assertTrue(differencetAvecLignesBlanches > differenceSansLignesBlanches);
    }

    private double comparerDeuxFichiers(ArrayList<File> listeFichiers, GestionnairePreferences prefs) {
        Task tache = new Task();
        tache.setFichiers(listeFichiers);

        tache.setPrefsPourTests(prefs);

        tache.lancerAnalyse();
        
        return tache.getRes(listeFichiers.get(0), listeFichiers.get(1));
    }

    @Test
    public void testContientTache() {
        Task tache1 = new Task();
        Task tache2 = new Task();
        boolean contient;
        contient = tache1.contientTache(tache2);
        assertFalse("La tâche 1 contient la tâche 2", contient);
    }

    @Test
    public void testSupprimerSource() {
        Task tache = new Task();
        tache.setFichiers(fichiers);
        File source = fichiers.get(0).getParentFile();
        tache.ajouterSource(source);
        assertTrue(tache.isSource(source));

        tache.supprimerSource(source);
        assertFalse(tache.isSource(source));
    }

    @Test
    public void testVerifierSiMemeSource() {
        Task tache = new Task();
        tache.setFichiers(fichiers);
        File source = fichiers.get(0).getParentFile();
        tache.ajouterSource(source);
        boolean memeSource = tache.verifierSiMemeSource(fichiers.get(0), fichiers.get(1));
        assertTrue("Pas la même source.", memeSource);
    }

    class TestObservateur implements Observateur {

        public int nombreChangement = 0;

        @Override
        public void changementEtat() {
            nombreChangement++;
        }

        @Override
        public void changementEtat(Enum<?> e, Object o) {
            nombreChangement++;
        }

    }

    @Test
    public void testRetirerFichiers() throws URISyntaxException {
        List<File> fichier = new ArrayList();
        fichier.add(file.Ouverture("/LoremIpsum1.txt"));
        fichier.add(file.Ouverture("/LoremIpsum2.txt"));
        fichier.add(file.Ouverture("/LoremIpsum3.txt"));
        fichier.add(file.Ouverture("/LoremIpsum4.txt"));
        fichier.add(file.Ouverture("/LoremIpsum5.txt"));
        fichier.add(file.Ouverture("/LoremIpsum6.txt"));
        Task tache = new Task();
        tache.setFichiers(fichier);
        tache.lancerAnalyse();
        
        assertEquals(fichier.size(), tache.getTousFichiers().size());

        tache.retirerFichiers(fichier);

        assertEquals(0, tache.getTousFichiers().size());
    }
    
    
    @Test
    public void testConcatenationComplexe() throws URISyntaxException {

        creerListeFichiersConcatenation();
        
        Task tache = new Task();
        try {
        tache.setFichiers(fichiers);
        File dossierEtudiant1 = file.Ouverture("/EnvironnementConcatenation/Etudiant1");
        File dossierEtudiant2 = file.Ouverture("/EnvironnementConcatenation/Etudiant2");
        File dossierEtudiant3 = file.Ouverture("/EnvironnementConcatenation/Etudiant3"); 
        
        tache.ajouterSource(dossierEtudiant1);
        tache.ajouterSource(dossierEtudiant2);
        tache.ajouterSource(dossierEtudiant3);
        
        tache.setConcatenation(true);
       
        tache.lancerAnalyse();

        Task.trier(fichiers, tache.getResults(), Tri.Type.ResultatCroissant);
        } catch (NullPointerException e) {
            
        }
        
        assertTrue(tache.fichiersAnalyse.size() > 0);
    }
    //check
    @Test
    public void testGetPrefs(){
        Noyau noyau = new Noyau();
        GestionnairePreferences prefs = noyau.getPrefs();
        prefs.writePref("DESTRUCTIVE", true);
        GestionnairePreferences prefsCompare = noyau.getPrefs();
        Task tache = new Task();
        tache.setPrefs(prefs);
        assertEquals(prefsCompare, tache.getPrefs());
    }
    /**
     * Verifie si la methode makeComp fonctionne 
     * avec "EXTRACT_IMG"
     */
    @Test
    @Ignore
    // Ce test prend beaucoup de temps à s'exécuter
    public void testExtraxtImgMakeComp() throws URISyntaxException{
        Task tache = new Task();
        ArrayList<File> liste = new ArrayList<>();
        liste.add(new File(getClass().getResource("/410_Copy_Cours_3_Abrege_des_oraux.pdf").toURI()));
        liste.add(new File(getClass().getResource("/410_Cours_3_Abrege_des_oraux.pdf").toURI()));
        liste.add(new File(getClass().getResource("/Bois_de_Boulogne_YUMMY_FINAL.pdf").toURI()));
        tache.setFichiers(liste);
        GestionnairePreferences prefs = new GestionnairePreferences(getClass());
        prefs.writePref("COMMENTAIRES", true);
        prefs.writePref("EXTRACT_IMG", true);
        tache.setPrefs(prefs);
        tache.lancerAnalyse();
        boolean analyseFaite = tache.verifierAnalyseFaite();
        assertTrue("L'analyse n'a pas été faite!", analyseFaite);
    }
    /**
     * Verifie si l'analyse s'est annulee si elle est est interomput
     */
    @Test
    public void testAnalyseConcatenationAnnuler() throws URISyntaxException{
        final Task tache = new Task();

        File fichier = file.Ouverture("/sousDossier/LoremIpsum1.txt");

        ArrayList<File> listeFichiers = new ArrayList<>();
        listeFichiers.add(fichier);
        for (File f : fichiers) {
            listeFichiers.add(f);
        }

        tache.setFichiers(listeFichiers);
        GestionnairePreferences prefs = new GestionnairePreferences(getClass());
        prefs.writePref("CONCATENATION", true);
        tache.setPrefs(prefs);
        File sourceJava = listeFichiers.get(0).getParentFile();
        File sourceTxt = listeFichiers.get(1).getParentFile();
        tache.ajouterSource(sourceJava);
        tache.ajouterSource(sourceTxt);
        Thread analyse = new Thread() {
            @Override
            public void run() {
                tache.lancerAnalyse();
            }
        };
        
        analyse.start();
        analyse.interrupt();
        
        try {
            analyse.join();
        } catch (InterruptedException ex) {
            Logger.getLogger(TaskTest.class.getName()).log(Level.SEVERE, null, ex);
            fail("Erreur lors de l'interruption du thread d'analyse");
        }

        boolean analyseFaite = tache.verifierAnalyseFaite();
        assertFalse("L'analyse n'a pas été faite!", analyseFaite);
    }
    
    @Test
    @Ignore
    // Ce test prend beaucoup de temps à s'exécuter.
    public void testRestaurerFichiersCaches() throws URISyntaxException{
        Task tache = new Task();
        tache.setFichiers(fichiers);
        GestionnairePreferences prefs = new GestionnairePreferences(getClass());
        prefs.writePref("CONCATENATION", true);
        tache.setPrefs(prefs);
        ArrayList<File> liste = new ArrayList<>();
        liste.add(new File(getClass().getResource("/410_Copy_Cours_3_Abrege_des_oraux.pdf").toURI()));
        liste.add(new File(getClass().getResource("/410_Cours_3_Abrege_des_oraux.pdf").toURI()));
        liste.add(new File(getClass().getResource("/Bois_de_Boulogne_YUMMY_FINAL.pdf").toURI()));
        tache.setFichiers(liste);
        prefs.writePref("COMMENTAIRES", true);
        prefs.writePref("EXTRACT_IMG", true);
        tache.setPrefs(prefs);
        tache.lancerAnalyse();
        for (int j = 0; j < tache.getFichiersResultats().size(); j++) {
                    if (tache.getFichiersResultats().get(j).toString().equals(liste.get(2).getPath())) {
                        tache.CacherFichierEtMettreAJourMatrice(
                                tache.getFichiersResultats().get(j));
                        break;
                    }
                }
        tache.RestaurerFichiersCaches();
        assertEquals(tache.getFichiersResultats(), tache.getListeFichiersComplete());
    }    
    public void creerListeFichiersConcatenation() {
        fichiers = new ArrayList<>();
        try {
            fichiers.add(file.Ouverture("/EnvironnementConcatenation/Etudiant1/PanelResults.java"));
            fichiers.add(file.Ouverture("/EnvironnementConcatenation/Etudiant2/Banane.java"));
            fichiers.add(file.Ouverture("/EnvironnementConcatenation/Etudiant2/fichier1.java"));
            fichiers.add(file.Ouverture("/EnvironnementConcatenation/Etudiant2/fichier12.java"));
            fichiers.add(file.Ouverture("/EnvironnementConcatenation/Etudiant2/fichier3.java"));
            fichiers.add(file.Ouverture("/EnvironnementConcatenation/Etudiant2/fichier4.java"));
            fichiers.add(file.Ouverture("/EnvironnementConcatenation/Etudiant2/fichier6.java"));
            fichiers.add(file.Ouverture("/EnvironnementConcatenation/Etudiant2/fichier8.java"));
            fichiers.add(file.Ouverture("/EnvironnementConcatenation/Etudiant2/fichier9.java"));
            fichiers.add(file.Ouverture("/EnvironnementConcatenation/Etudiant2/fichierB.java"));
            fichiers.add(file.Ouverture("/EnvironnementConcatenation/Etudiant3/PanelFiles.java"));
        } catch (URISyntaxException | NullPointerException ex) {

        }
    }


//    @Test
//    public void testTrouverFIchierSource(){
//        
//    }
}
