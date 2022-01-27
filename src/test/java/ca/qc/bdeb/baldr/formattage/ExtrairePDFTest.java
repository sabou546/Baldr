/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ca.qc.bdeb.baldr.formattage;

import static ca.qc.bdeb.baldr.main.Main.isUnix;
import ca.qc.bdeb.baldr.noyau.GestionnairePreferences;
import java.io.File;
import java.io.IOException;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.util.PDFTextStripper;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;
import ca.qc.bdeb.baldr.noyau.Task;
import ca.qc.bdeb.baldr.noyau.Tri;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import org.junit.Ignore;


/**
 *
 * @author phamk
 */
@Ignore
// Ces tests prennent trop de temps à s'exécuter.
public class ExtrairePDFTest {

    public ExtrairePDFTest() {
    }
    public static ExtrairePDF test = new ExtrairePDF();
    public static ExtrairePDF pdfExtractor = new ExtrairePDF();
    public static HashMap<File, Long> precalculatedFiles = new HashMap<>();

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

    @Test
    public void TestExtractionSimilaire() throws IOException {
        // mettre un pdf la.
        File fichier = new File("src\\test\\resources\\410_Cours_3_Abrege_des_oraux.pdf");
        File fichier_Copy = new File("src\\test\\resources\\410_Copy_Cours_3_Abrege_des_oraux.pdf");
        if(isUnix){
            fichier = new File("src//test//resources//410_Cours_3_Abrege_des_oraux.pdf");
            fichier_Copy = new File("src//test//resources//410_Copy_Cours_3_Abrege_des_oraux.pdf");
        }
        PDDocument doc1 = PDDocument.load(fichier.getCanonicalPath());

        //faire et mettre la copie du pdf ci-dessus la.
        // Je fais appelle a la methode extrairePDF dans ExtrairePDF
        String s2 = test.ExtrairePDF(fichier_Copy.getCanonicalFile());

        PDFTextStripper stripper = new PDFTextStripper();
        int pages = doc1.getNumberOfPages();
        stripper.setLineSeparator("\n");
        stripper.setStartPage(1);
        stripper.setEndPage(pages);
        String s1 = stripper.getText(doc1);

        assertEquals(s1, s2);
        doc1.close();

    }

    @Test
    public void TestExtractionDifferent() throws IOException {
        // mettre un pdf la.
        File fichier = new File("src\\test\\resources\\410_Cours_3_Abrege_des_oraux.pdf");
        File fichier_Diff = new File("src\\test\\resources\\Bois_de_Boulogne_YUMMY_FINAL.pdf");
        if(isUnix){
            fichier = new File("src//test//resources//410_Cours_3_Abrege_des_oraux.pdf");
            fichier_Diff = new File("src//test//resources//Bois_de_Boulogne_YUMMY_FINAL.pdf");
        }
        PDDocument doc1 = PDDocument.load(fichier.getCanonicalPath());

        //faire et mettre le pdf ci-dessus la.
        // Je fais appelle a la methode extrairePDF dans ExtrairePDF
        String s2 = test.ExtrairePDF(fichier_Diff.getCanonicalFile());

        PDFTextStripper stripper = new PDFTextStripper();
        int pages = doc1.getNumberOfPages();
        stripper.setLineSeparator("\n");
        stripper.setStartPage(1);
        stripper.setEndPage(pages);
        String s1 = stripper.getText(doc1);

        assertNotEquals(s1, s2);
        
        doc1.close();
    }

    @Test
  //  @Ignore
    public void TestExtractionPDFMatrice() throws IOException, InterruptedException {

        File fichier = new File("src\\test\\resources\\410_Cours_3_Abrege_des_oraux.pdf");
        File fichier_2 = new File("src\\test\\resources\\410_Copy_Cours_3_Abrege_des_oraux.pdf");
        File fichier_3 = new File("src\\test\\resources\\Bois_de_Boulogne_YUMMY_FINAL.pdf");
        File fichier_4 = new File("src\\test\\resources\\lo.txt");
        if(isUnix){
            fichier = new File("src//test//resources//410_Cours_3_Abrege_des_oraux.pdf");
            fichier_2 = new File("src//test//resources//410_Copy_Cours_3_Abrege_des_oraux.pdf");
            fichier_3 = new File("src//test//resources//Bois_de_Boulogne_YUMMY_FINAL.pdf");
            fichier_4 = new File("src//test//resources//lo.txt");
        }
        ArrayList<File> tab = new ArrayList<>();

        tab.add(fichier);
        tab.add(fichier_2);
        tab.add(fichier_3);
        tab.add(fichier_4);

        Task thr = new Task();
        GestionnairePreferences p = new GestionnairePreferences(getClass());
        p.writePref("EXTRACT_PDF", true);
        thr.setPrefs(p);
        thr.setFichiers(tab);

        thr.lancerAnalyse();

        //(Liste de fichier, La matrice)
        Tri.triResultatCroissant(thr.getFichiersResultats(), thr.getResults());
        float[][] result = thr.getResults().getValues();
        assertTrue(result[0][0] < 0.5);
        assertTrue(result[1][0] < 0.5);
        assertTrue(result[2][0] > 0.5);

    }
    
  //  @Ignore
    @Test
    public void TestExtrairePDF() throws IOException {
        ExtrairePDF exPDF = new ExtrairePDF();
        URL path = getClass().getResource("target//classes//PDFTest//420-P47-BB-PC.pdf");
        File file = new File("target//classes//PDFTest//420-P47-BB-PC.pdf");
        if(isUnix){
           file = new File("target//classes//PDFTest//420-P47-BB-PC.pdf"); 
        }
        String resultat = exPDF.ExtrairePDF(file);
        assertNotEquals(resultat, "");
    }
    
  //  @Ignore
    @Test
    public void TestExtraireImages() throws IOException{
        ExtrairePDF exPDF = new ExtrairePDF();
        URL path = getClass().getResource("target//classes//PDFTest//420-P47-BB-PC.pdf");
        File file = new File("target//classes//PDFTest//420-P47-BB-PC.pdf");
        if(isUnix){
           file = new File("target//classes//PDFTest//420-P47-BB-PC.pdf"); 
        }
        String resultat = exPDF.extraireImages(file);
        assertNotEquals(resultat, "");
    }
    
    

}
