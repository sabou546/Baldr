package ca.qc.bdeb.baldr.ihm.renderers;

import ca.qc.bdeb.baldr.noyau.GestionnairePreferences;
import ca.qc.bdeb.baldr.noyau.Task;
import java.awt.Color;
import java.text.DecimalFormat;
import javax.swing.JLabel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellRenderer;

/**
 * Permet de rendre un cellule du tableau de résultats avec un arrière-plan de
 * couleur selon sa valeur.
 *
 * @author Maxim Bernard
 */
public abstract class AbstractResultCellCustomRenderer implements TableCellRenderer {
    
    

    protected TableCellRenderer rend;
    protected double min;
    protected double max;
    protected int nombreDecimales;
    protected GestionnairePreferences preferences;

    public AbstractResultCellCustomRenderer(double min, double max,
            int nombreDecimal, GestionnairePreferences pref) {
        this.min = min;
        this.max = max;
        this.nombreDecimales = nombreDecimal;
        rend = new DefaultTableCellRenderer();
        preferences = pref;

    }

    public void setNombreDecimales(int nombreDecimales) {
        this.nombreDecimales = nombreDecimales;
    }

    protected void writeNumericValueToComponent(JLabel reu, double value) {
        if (value != 0) {
            DecimalFormat df = new DecimalFormat();
            df.setMaximumFractionDigits(nombreDecimales);
            df.setMinimumFractionDigits(nombreDecimales);
            df.setDecimalSeparatorAlwaysShown(true);
            reu.setText(String.valueOf(df.format(value)));
        } else {
            reu.setText("");
        }
    }

    protected void setComponentColorFromValue(JLabel reu, double value, boolean isSelected, Task analyse) {
        // si toutes les valeurs sont égales, utilise le mode fixe des couleurs.
       
        
        if (analyse.getIsProgressive() && (min != max)) {
            float hue = (float) (0.37 * (((value) - min) / (max - min)));
            if (!isSelected) {
                reu.setBackground(value == 0 ? Color.WHITE : Color.getHSBColor(hue, 0.5F, 1));
            } else {
                reu.setBackground(value == 0 ? Color.WHITE : Color.getHSBColor(hue, 0.5F, 0.75F));
            }
        } else {
            Color couleur = Color.green;
            if (value == 0) {
                couleur = Color.white;
            } else if (value < analyse.getRedLimit()) {
                couleur = Color.red;
            } else if (value < analyse.getYellowLimit()) {
                couleur = Color.YELLOW;
            } else {
                couleur = Color.green;
            }

            if (!isSelected) {
                reu.setBackground(couleur);
            } else {
                reu.setBackground(couleur.darker());
            }

            
            
        } 
        if(analyse.getInvalide() == true){
                reu.setBackground(Color.GRAY);
            }
    }

}
