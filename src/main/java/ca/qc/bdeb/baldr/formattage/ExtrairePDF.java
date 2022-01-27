package ca.qc.bdeb.baldr.formattage;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.graphics.xobject.PDXObjectImage;
import org.apache.pdfbox.util.PDFTextStripper;

/**
 *
 * @author 1662835
 */
public class ExtrairePDF {
    
    public String  ExtrairePDF (File file) throws IOException {
//        URL  url = Main.class.getResource("/test.pdf");        
        PDDocument doc = PDDocument.load(file);
        PDFTextStripper stripper = new PDFTextStripper();
        int pages = doc.getNumberOfPages();
        stripper.setLineSeparator("\n");
        stripper.setStartPage(1);
        stripper.setEndPage(pages);
        String s = stripper.getText(doc);
        doc.close();
        return s;
    }
    
    /**
     * Extrait les images d'un fichier pdf et en retourne une représentation
     * courte sous forme de chaîne de caractères (hash)
     * @param fichier fichier pdf à analyser
     * @return String représentant grossièrement le contenu du fichier
     * @throws IOException 
     */
    public String extraireImages(File fichier) throws IOException {
        PDDocument doc = PDDocument.load(fichier);
        List pages = doc.getDocumentCatalog().getAllPages();
        Iterator iterPages = pages.iterator();
        StringBuilder sb = new StringBuilder();
        
        while( iterPages.hasNext() )
        {
            PDPage page = (PDPage)iterPages.next();
            Map objets = page.findResources().getXObjects();
            
            if( objets != null )
            {
                Iterator iterObjets = objets.keySet().iterator();
                
                while(iterObjets.hasNext()) {
                    String key = (String)iterObjets.next();
                    Object objet = objets.get(key);
                    
                    if (objet instanceof PDXObjectImage) {
                        sb.append('\n');
                        sb.append(getHashProportionnel((PDXObjectImage)(objet)));
                    }
                }
            }
        }
        
        doc.close();
        return sb.toString();
    }
    
    /**
     * Traduit une image en une chaîne de caractères de longueur proportionnelle
     * au nombre de pixels que contient l'image.
     * @param image image à convertir
     * @return résultat du hachage
     * @throws IOException 
     */
    private String getHashProportionnel(PDXObjectImage image) throws IOException {
        BufferedImage buff = image.getRGBImage();
        
        if (buff == null) {
            return "";
        }
        
        int[] pixels = buff.getRGB(
                0,
                0,
                buff.getWidth(), 
                buff.getHeight(), 
                null, 
                0, 
                buff.getWidth());
        
        int nbPixels = pixels.length;
        
        // La pondération de l'image est proportionnelle à la taille du hash
        int tailleHash = (int)Math.sqrt(nbPixels);
        
        int pixelsParLettre = nbPixels / tailleHash + 1;
        char[] hash = new char[tailleHash];
        int numeroLettre = 0;
        int numeroPixel = 0;
        int groupePixels = 0;
        
        do {
            groupePixels += pixels[numeroPixel];
            numeroPixel++;
            
            if (numeroPixel % pixelsParLettre == 0) {
                
                /*
                 * TODO: améliorer pour donner un résultat plus similaire lorque
                 * seulement quelques pixels changent
                 */
                hash[numeroLettre] = 
                        (char)('a' + Math.abs(groupePixels / pixelsParLettre) % 26);
                
                groupePixels = 0;
                numeroLettre++;
            }
        } while (numeroPixel < nbPixels);
        
        return String.valueOf(hash);
    }
    
    /**
     * Traduit une image en une chaîne de caractères de longueur fixe
     * en découpant l'image en secteurs rectangulaires.
     * @param image image à convertir
     * @return résultat du hachage
     * @throws IOException 
     */
    private String getHashParSecteurs (PDXObjectImage image) throws IOException {
        BufferedImage buff = image.getRGBImage();
        int largeurImage = buff.getWidth();
        int hauteurImage = buff.getHeight();
        int base = 75;
        char[] hash = new char[base * base];
        int largeurRectangle = largeurImage / base;
        int hauteurRectangle = hauteurImage / base;
        
        int indexLettre = 0;
        
        for (int i = 0; i < base; i++) {
            for (int j = 0; j < base; j++) {
                int origX = j * largeurRectangle;
                int origY = i * hauteurRectangle;
                
                int largeurSection = 
                        (j + 1 < base) ? largeurRectangle : largeurImage - origX;
                
                int hauteurSection = 
                        (i + 1 < base) ? hauteurRectangle : hauteurImage - origY;
                
                int[] pixels = 
                        buff.getRGB(
                                origX, 
                                origY, 
                                largeurSection, 
                                hauteurSection, 
                                null, 
                                0, 
                                largeurSection);
                
                int totalPixels = 0;
                
                for (int pixel : pixels) {
                    totalPixels += pixel;
                }
                
                hash[indexLettre] = 
                        (char)('a' + Math.abs(totalPixels / pixels.length) % 26);
                
                indexLettre++;
            }
        }
        
        return String.valueOf(hash);
    }
}

    
