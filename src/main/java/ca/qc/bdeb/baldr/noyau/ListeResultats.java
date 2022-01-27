package ca.qc.bdeb.baldr.noyau;

import ca.qc.bdeb.baldr.utils.FilePair;
import java.io.File;
import java.util.TreeMap;

/**
 * Regroupement des résultats d'analyse.
 * 
 * @author Olivier Lavigne
 */
public class ListeResultats {
    /**
     * Liste des résultats d'analyse
     */
    private TreeMap<FilePair, Float> resultats;
    
    /**
     * Instancie une liste de résultats d'analyse
     */
    public ListeResultats() {
        resultats = new TreeMap();
    }
    
    /**
     * Ajoute un résultat d'analyse à la liste
     * @param fichier1 Le premier fichier analysé
     * @param fichier2 Le deuxième fichier analysé
     * @param resultat Le résultat de la comparaison des deux fichiers 
     */
    public synchronized void ajouterResultat(File fichier1, File fichier2, float resultat) {
        FilePair paire;
        paire = new FilePair(fichier1, fichier2);
        resultats.put(paire, resultat);
    }
    
    /**
     * Vérifie si la paire de fichiers a déjà été analysée
     * @param fichier1 Le premier fichier analysé
     * @param fichier2 Le deuxième fichier analysé
     * @return True si un résultat existe pour la paire de fichiers
     */
    public synchronized boolean resultatExiste(File fichier1, File fichier2) {
        FilePair paire = new FilePair(fichier1, fichier2);
        return resultats.containsKey(paire);
    }
    
    /**
     * Retourne le résultat d'une comparaison de fichiers déjà complétée.
     * @param fichier1 Le premier fichier
     * @param fichier2 Le deuxième fichier
     * @return Le score de comparaison des deux fichiers
     */
    public synchronized float getResultat(File fichier1, File fichier2) {
        FilePair paire = new FilePair(fichier1, fichier2);
        return resultats.get(paire);
    }
    
    /**
     * Vide la liste des résultats
     */
    public synchronized void viderListe() {
        resultats.clear();
    }
        
}
