package ca.qc.bdeb.baldr.noyau;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URISyntaxException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;
import org.junit.After;
import org.junit.AfterClass;
import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

/**
 *
 * @author Équipe de dév Baldr
 */
public class DefaultTaskTest {

    List<File> fichiers = null;
    private static OuvertureFichier file= new OuvertureFichier();
    public DefaultTaskTest() throws URISyntaxException {
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
        List<File> listeFichiers = new ArrayList();
        listeFichiers.add(file.Ouverture("/LoremIpsum1.txt"));
        listeFichiers.add(file.Ouverture("/LoremIpsum2.txt"));
        listeFichiers.add(file.Ouverture("/LoremIpsum3.txt"));
        
        
        return listeFichiers;
    }

    @Test
    public void VerifierResultat() throws URISyntaxException, InterruptedException {
        Task taskTest = new Task();
        taskTest.setConcatenation(false);
        
        taskTest.setFichiers(fichiers);
        taskTest.lancerAnalyse();

        float[][] resultat = taskTest.getResults().getValues();
        double[][] resultatAttendu = {
            {0},
            {0.882619, 0},
            {0.8735714, 0.8395566, 0},
            {0.2574113, -1, -1, 0},
            {-1, -1, -1, -1, 0},
            {-1, -1, -1, -1, -1, 0}
        };

        for (int i = 0; i < resultat.length; i++) {
            for (int j = 0; j < resultat[i].length; j++) {
                assertEquals((float) resultatAttendu[i][j], resultat[i][j], 0);

            }
        }
    }

    @Test
    public void VerifierStateAvant() {
        Task test = new Task();
        assertEquals(0, test.getStateCount(), 0);

    }

    @Test
    public void VerifierStateApres()
            throws URISyntaxException, InterruptedException {
        Task test = new Task();
        test.setConcatenation(false);
        
        test.setFichiers(fichiers);
        test.lancerAnalyse();

        assertEquals(1, test.getStateCount(), 0);

    }

    @Test
    public void toXML() throws URISyntaxException, InterruptedException, IOException, FileNotFoundException, NoSuchAlgorithmException {
        Task test = new Task();
        test.setConcatenation(false);
        
        String strHash;
        
        test.setFichiers(fichiers);

        File dossier1 = new File("."
                + File.separator + "src"
                + File.separator + "test"
                + File.separator + "resources");

        File dossier2 = new File("."
                + File.separator + "src"
                + File.separator + "test"
                + File.separator + "DifferentSource");
        
        test.ajouterSource(dossier1);
        test.ajouterSource(dossier2);

        test.lancerAnalyse();

        StringBuilder resultat = new StringBuilder();
//        resultat.append("<");
        resultat.append(test.toXml());
        
        StringBuilder s = new StringBuilder();
        s.append("<onglet>\n");
        s.append("<titre sommaire=\"False\">Analyse</titre>\n");
        s.append("<rapport></rapport>\n");
        s.append("<analys>\n");
        s.append("<fichs>\n");
        strHash = test.convertFileToMD5Str(fichiers.get(0).getAbsolutePath());
        s.append("<file " + "hash='" + strHash + "'" + ">");
        s.append(fichiers.get(0).getPath());
        s.append("</file>\n");
        strHash = test.convertFileToMD5Str(fichiers.get(1).getAbsolutePath());
        s.append("<file " + "hash='" + strHash + "'" + ">");
        s.append(fichiers.get(1).getPath());
        s.append("</file>\n");
        strHash = test.convertFileToMD5Str(fichiers.get(2).getAbsolutePath());
        s.append("<file " + "hash='" + strHash + "'" + ">");
        s.append(fichiers.get(2).getPath());
        s.append("</file>\n");
        s.append("</fichs>\n");
        s.append("<sources>\n");
        s.append("<source>");
        s.append(dossier1);
        s.append("</source>\n");
        s.append("<source>");
        s.append(dossier2);
        s.append("</source>\n");
        s.append("</sources>\n");
        s.append("<res len=\"3\">\n");
        s.append("<li len=\"1\">\n");
        s.append("<l i=\"0\" j=\"0\">0.0</l>\n");
        s.append("</li>\n");
        s.append("<li len=\"2\">\n");
        s.append("<l i=\"1\" j=\"0\">0.882619</l>\n");
        s.append("<l i=\"1\" j=\"1\">0.0</l>\n");
        s.append("</li>\n");
        s.append("<li len=\"3\">\n");
        s.append("<l i=\"2\" j=\"0\">0.8735714</l>\n");
        s.append("<l i=\"2\" j=\"1\">0.8395566</l>\n");
        s.append("<l i=\"2\" j=\"2\">0.0</l>\n");
        s.append("</li>\n");
        s.append("</res>\n");
        
        s.append("<options>\n");
        s.append("<preview>false</preview>\n");
        s.append("<concatenation>false</concatenation>\n");
        s.append("<commentaires>false</commentaires>\n");
        s.append("<whitespaces>false</whitespaces>\n");
        s.append("<extrairepdf>false</extrairepdf>\n");
        s.append("<extraireimages>false</extraireimages>\n");
        s.append("</options>\n");
        s.append("<id>Saved</id>\n");
        s.append("<optspecifiques>\n");
        s.append("<preview>" + "true" + "</preview>\n");
        s.append("<concatenation>" + "false" + "</concatenation>\n");
        s.append("<extrairepdf>" + "true" + "</extrairepdf>\n");
        s.append("<extraireimages>" + "true" + "</extraireimages>\n");
        s.append("<commentaires>" + "false" + "</commentaires>\n");  
        s.append("<whitespaces>" + "true" + "</whitespaces>\n");
        s.append("<progressive>" + "true" + "</progressive>\n");
        s.append("<redlimit>" + "0.0" + "</redlimit>\n");
        s.append("<yellowlimit>" + "0.0" + "</yellowlimit>\n");
        s.append("<greenlimit>" + "1.0" + "</greenlimit>\n");
        s.append("</optspecifiques>\n");
        s.append("</analys>\n");
        s.append("</onglet>\n");
        
        assertEquals(s.toString(), resultat.toString());
    }
}
