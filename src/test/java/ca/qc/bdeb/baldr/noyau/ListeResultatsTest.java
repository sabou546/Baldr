/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ca.qc.bdeb.baldr.noyau;

import java.io.File;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;
import org.junit.Ignore;

/**
 *
 * @author 1819132
 */
public class ListeResultatsTest {
    
    public ListeResultatsTest() {
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

    /**
     * Test of ajouterResultat method, of class ListeResultats.
     */
    @Test
    public void testAjouterResultat() {
        File fichier1 = new File("/LoremIpsum1.txt");
        File fichier2 = new File("/LoremIpsum2.txt");
        float resultat = 0.5F;
        ListeResultats instance = new ListeResultats();
        instance.ajouterResultat(fichier1, fichier2, resultat);
        
        assertEquals(instance.resultatExiste(fichier1, fichier2), true);
    }

    /**
     * Test of resultatExiste method, of class ListeResultats.
     */
    @Test
    public void testResultatExiste() {
        File fichier1 = new File("/LoremIpsum1.txt");
        File fichier2 = new File("/LoremIpsum2.txt");
        ListeResultats instance = new ListeResultats();
        instance.ajouterResultat(fichier1, fichier2, 0);
        boolean expResult = true;
        boolean result = instance.resultatExiste(fichier1, fichier2);
        assertEquals(expResult, result);
    }

    /**
     * Test of getResultat method, of class ListeResultats.
     */
    @Test
    public void testGetResultat() {
        File fichier1 = new File("/LoremIpsum1.txt");
        File fichier2 = new File("/LoremIpsum2.txt");
        ListeResultats instance = new ListeResultats();
        float expResult = 0.5F;
        instance.ajouterResultat(fichier1, fichier2, 0.5F);
        float result = instance.getResultat(fichier1, fichier2);
        assertEquals(expResult, result, 0.5);
    }

    /**
     * Test of viderListe method, of class ListeResultats.
     */
    @Test
    public void testViderListe() {       
        File fichier1 = new File("/LoremIpsum1.txt");
        File fichier2 = new File("/LoremIpsum2.txt");
        ListeResultats instance = new ListeResultats();
        instance.ajouterResultat(fichier1, fichier2, 0.5F);
        instance.viderListe();
        assertEquals(instance.resultatExiste(fichier1, fichier2), false);
    }
    
}
