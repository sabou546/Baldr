package ca.qc.bdeb.baldr.noyau;

import java.util.TreeMap;

/**
 * Gestionnaire de listes de résultats.
 *
 * @author Olivier Lavigne
 */
public class GestionnaireResultats {
    
    TreeMap<PreferencesAnalyse, ListeResultats> listes;

    /**
     * Instasncie un gestionnaire de listes de résultats
     */
    public GestionnaireResultats() {
        listes = new TreeMap();
    }
    
    /**
     * retourne la liste à utiliser en fonction des préférences en vigueur
     * 
     * @param preferences les préférences d'analyse associées à la tâche
     * @return La liste de résultats appropriée
     */
    public synchronized ListeResultats obtenirListe(PreferencesAnalyse preferences) {
        
        if (listes.containsKey(preferences)) {
            return listes.get(preferences);
        } else {
            ListeResultats nouvelleListe = new ListeResultats();
            listes.put(preferences, nouvelleListe);
            return nouvelleListe;
        }
    }
    
    /**
     * retourne la liste à utiliser en fonction des préférences en vigueur
     * 
     * @param prefs gestionnaire préférences par lequel obtenir les préférences
     * actives
     * @return La liste de résultats appropriée
     */
    public synchronized ListeResultats obtenirListe(GestionnairePreferences prefs) {
        return obtenirListe(new PreferencesAnalyse(prefs));
    }
    
    public synchronized ListeResultats obtenirListe(Task t) { //obtenirListe avec les options du task
        return obtenirListe(new PreferencesAnalyse(t));
    }
    
    /**
     * Classe décrivant un ensemble de préférences
     */
    class PreferencesAnalyse implements Comparable<PreferencesAnalyse> {
        private boolean concatenation;
        private boolean commentaires;
        private boolean whitespaces;
        private boolean extraireTextePDF;
        private boolean extraireImagesPDF;
        
        public PreferencesAnalyse (Task t){
            this.concatenation  = t.getJCheckBoxAnalyseConcatenation();
            this.commentaires = t.getCheckBoxCommentaires();
            this.whitespaces = t.getCheckBoxWhitepsaces();
            this.extraireTextePDF = t.getPdfExtractor();
            this.extraireImagesPDF = t.getPdfImages();
            
        }
        
        /**
         * Crée une instance de préférences d'analyse en lisant les préférences
         * dans le gestionnaire de péférences donné
         * @param prefs gestionnaire à partir duquel on lira les préférences
         */
        public PreferencesAnalyse(GestionnairePreferences prefs) {
            if (prefs != null) {
                this.concatenation = (Boolean) prefs.readPref("CONCATENATION", false);
                this.commentaires = (Boolean) prefs.readPref("COMMENTAIRES", false);
                this.whitespaces = (Boolean) prefs.readPref("WHITESPACES", false);
                this.extraireTextePDF = (Boolean) prefs.readPref("EXTRACT_PDF", false);
                this.extraireImagesPDF = (Boolean) prefs.readPref("EXTRACT_IMG", false);
            }
        }

        @Override
        public boolean equals(Object obj) {
            if (!(obj instanceof PreferencesAnalyse)) {
                return false;
            }
            
            PreferencesAnalyse autrePref = (PreferencesAnalyse)obj;
            
            return (autrePref.concatenation == concatenation && 
                    autrePref.commentaires == commentaires && 
                    autrePref.whitespaces == whitespaces &&
                    autrePref.extraireTextePDF == extraireTextePDF &&
                    autrePref.extraireImagesPDF == extraireImagesPDF);
        }

        @Override
        public int compareTo(PreferencesAnalyse o) {
            if (o.concatenation != concatenation) {
                return (concatenation ? -1 : 1);
            } else if (o.commentaires != commentaires) {
                return (commentaires ? -1 : 1);
            } else if (o.whitespaces != whitespaces) {
                return (whitespaces ? -1 : 1);
            } else if (o.extraireTextePDF != extraireTextePDF) {
                return (extraireTextePDF ? -1 : 1);
            } else if (o.extraireImagesPDF != extraireImagesPDF) {
                return (extraireImagesPDF ? -1 : 1);
            } else {
                return 0;
            }
        }
    }
}
