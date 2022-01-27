/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ca.qc.bdeb.baldr.noyau;

import ca.qc.bdeb.baldr.noyau.GestionnaireResultats;
import ca.qc.bdeb.baldr.noyau.GestionnaireResultats.PreferencesAnalyse;
import java.io.File;
import java.net.URISyntaxException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;
import org.junit.Ignore;

/**
 *
 * @author Victor Gontar
 */
public class GestionnaireResultatsTest {
    GestionnaireResultats gesResul;
    
    public GestionnaireResultatsTest() {
    }
    
    @BeforeClass
    public static void setUpClass() {
    }
    
    @AfterClass
    public static void tearDownClass() {
    }
    
    @Before
    public void setUp() {
        gesResul = new GestionnaireResultats();
    }
    
    @After
    public void tearDown() {
    }
    
    @Test
    public void compareToConcatenationTest() {
        GestionnairePreferences prefs = new GestionnairePreferences(getClass());
        prefs.writePref("CONCATENATION", false);
        GestionnaireResultats.PreferencesAnalyse obj = gesResul.new PreferencesAnalyse(prefs);
        
        
        GestionnairePreferences prefs2 = new GestionnairePreferences(getClass());
        prefs2.writePref("CONCATENATION", true);
        GestionnaireResultats.PreferencesAnalyse obj2 = gesResul.new PreferencesAnalyse(prefs2);
        
        assertEquals(obj.compareTo(obj2), 1);
    }
    
    @Test
    public void compareToCommentairesTest() {
        GestionnairePreferences prefs = new GestionnairePreferences(getClass());
        prefs.writePref("COMMENTAIRES", false);
        GestionnaireResultats.PreferencesAnalyse obj = gesResul.new PreferencesAnalyse(prefs);
        
        GestionnairePreferences prefs2 = new GestionnairePreferences(getClass());
        prefs2.writePref("COMMENTAIRES", true);
        GestionnaireResultats.PreferencesAnalyse obj2 = gesResul.new PreferencesAnalyse(prefs2);
        
        assertEquals(obj.compareTo(obj2), 1);
    }
    
    @Test
    public void compareToWhitespacesTest() {
        GestionnairePreferences prefs = new GestionnairePreferences(getClass());
        prefs.writePref("WHITESPACES", false);
        GestionnaireResultats.PreferencesAnalyse obj = gesResul.new PreferencesAnalyse(prefs);
        
        GestionnairePreferences prefs2 = new GestionnairePreferences(getClass());
        prefs2.writePref("WHITESPACES", true);
        GestionnaireResultats.PreferencesAnalyse obj2 = gesResul.new PreferencesAnalyse(prefs2);
        
        assertEquals(obj.compareTo(obj2), 1);
    }
    
    @Test
    public void compareToExtractPDFWhitespacesTest() {
        GestionnaireResultats gesResul = new GestionnaireResultats();
        GestionnairePreferences prefs = new GestionnairePreferences(getClass());
        prefs.writePref("EXTRACT_PDF", false);
        GestionnaireResultats.PreferencesAnalyse obj = gesResul.new PreferencesAnalyse(prefs);
        
        GestionnairePreferences prefs2 = new GestionnairePreferences(getClass());
        prefs2.writePref("EXTRACT_PDF", true);
        GestionnaireResultats.PreferencesAnalyse obj2 = gesResul.new PreferencesAnalyse(prefs2);
        
        assertEquals(obj.compareTo(obj2), 1);
    }
    
    @Test
    public void compareToExtractIMGTest() {
        GestionnaireResultats gesResul = new GestionnaireResultats();
        GestionnairePreferences prefs = new GestionnairePreferences(getClass());
        prefs.writePref("EXTRACT_IMG", false);
        GestionnaireResultats.PreferencesAnalyse obj = gesResul.new PreferencesAnalyse(prefs);
        
        GestionnairePreferences prefs2 = new GestionnairePreferences(getClass());
        prefs2.writePref("EXTRACT_IMG", true);
        GestionnaireResultats.PreferencesAnalyse obj2 = gesResul.new PreferencesAnalyse(prefs2);
        
        assertEquals(obj.compareTo(obj2), 1);
    }

    /**
     * Cette méthode test vérifie si la méthode equal() 
     * retourne false si on la compare avec une classe qui n'est pas une instance
     * PreferencesAnalyse
     */
    @Test
    public void TestEqualPasPareil(){
        GestionnaireResultats gesResul = new GestionnaireResultats();
        GestionnairePreferences prefs = new GestionnairePreferences(getClass());
        GestionnaireResultats.PreferencesAnalyse obj = gesResul.new PreferencesAnalyse(prefs);
        String test = "Ceci est un texte futil";
        assertFalse(obj.equals(test));
    }
    
    
    @Test
    public void TestEqualPareil(){
        GestionnaireResultats gesResul = new GestionnaireResultats();
        GestionnairePreferences prefs = new GestionnairePreferences(getClass());
        GestionnaireResultats.PreferencesAnalyse obj = gesResul.new PreferencesAnalyse(prefs);
        GestionnaireResultats.PreferencesAnalyse obj2 = gesResul.new PreferencesAnalyse(prefs);
        assertTrue(obj.equals(obj2));
    }
    
    @Test
    public void TestObtenirListeAvecCle(){
        GestionnaireResultats gesResul = new GestionnaireResultats();
        GestionnairePreferences prefs = new GestionnairePreferences(getClass());
        GestionnaireResultats.PreferencesAnalyse obj = gesResul.new PreferencesAnalyse(prefs);
        gesResul.obtenirListe(obj);
        GestionnairePreferences prefs2 = new GestionnairePreferences(getClass());
        GestionnaireResultats.PreferencesAnalyse obj2 = gesResul.new PreferencesAnalyse(prefs2);
        ListeResultats liste = gesResul.obtenirListe(obj2);
        assertNotNull(liste);
    }
}
