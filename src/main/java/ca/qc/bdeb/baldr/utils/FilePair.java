/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ca.qc.bdeb.baldr.utils;

import java.io.File;
import java.util.Objects;

/**
 *
 * @author 6145046
 */
/**
     * Paire de deux fichiers compressés ensemble.
     */
    public class FilePair implements Comparable<FilePair> {

        private final File file1;
        private final File file2;

        public FilePair(File file1, File file2) {
            this.file1 = file1;
            this.file2 = file2;
        }

        @Override
        public int hashCode() {
            int hash = 7;
            hash = 83 * hash + Objects.hashCode(this.file1);
            hash = 83 * hash + Objects.hashCode(this.file2);
            return hash;
        }

        /**
         * Vérifie si une autre paire contient les mêmes fichiers, sans tenir
         * compte de l'ordre
         * @param autrePaire la paire à comparer
         * @return True si et seulement si les deux objets sont de type 
         * PaireFichier et qu'ils contiennent les mêmes fichiers
         */
        private boolean comparerPaires(FilePair autrePaire) {
           return (file1.equals(autrePaire.file1)  && file2.equals(autrePaire.file2)) ||
                   (file1.equals(autrePaire.file1)  && file2.equals(autrePaire.file2));
        }

        @Override
        public int compareTo(FilePair o) {
            if (comparerPaires(o)) {
                return 0;
            } else if (!file1.equals(o.file1)) {
                return file1.compareTo(o.file1);
            } else {
              return file2.compareTo(o.file2);
            }
        }
}
