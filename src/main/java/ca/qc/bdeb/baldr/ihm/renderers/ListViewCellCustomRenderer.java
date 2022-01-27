package ca.qc.bdeb.baldr.ihm.renderers;

import ca.qc.bdeb.baldr.noyau.GestionnairePreferences;
import ca.qc.bdeb.baldr.noyau.Task;
import java.awt.Component;
import javax.swing.JLabel;
import javax.swing.JTable;

/**
 * Gestionnaire de rendu pour les cellules de la vue en liste.
 *
 * @author Maxim Bernard
 */
public class ListViewCellCustomRenderer extends AbstractResultCellCustomRenderer {
    private final Task analys;

    /**
     * Construit un ListViewCellCustomRenderer à partir du
     * TableCellCustomRenderer.
     *
     * @param min
     * @param max
     * @param nombreDecimal
     */
    public ListViewCellCustomRenderer(Task analys, double min, double max, int nombreDecimal,
            GestionnairePreferences preferences) {
        super(min, max, nombreDecimal,preferences);
        this.analys = analys;
    }

    public Component getTableCellRendererComponent(JTable table,
            Object o, boolean isSelected, boolean hasFocus, int row, int column) {
        double value = (double) o;
        JLabel reu = (JLabel) rend.getTableCellRendererComponent(table,
                value, isSelected, hasFocus, row, column);

        writeNumericValueToComponent(reu, value);
        setComponentColorFromValue(reu, value, isSelected, analys);

        return reu;
    }


}
