package ca.qc.bdeb.baldr.noyau;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import org.w3c.dom.Node;

/**
 * Un projet contient plusieurs tâches et peut être sauvegardé dans un fichier
 * et ré-ouvert.
 *
 * @author mbernard
 */
public class Projet implements Savable {

    public boolean sommaireExiste = false;

    private File lastFile = null;

    /**
     * Les tâches contenues par le projet.
     */
    private final List<Task> tasks;

    /**
     * Vrai si quelque chose a changé dans le projet, faux sinon.
     */
    private boolean modifie = false;

    /**
     * Le noyau qui contient le projet.
     */
    private final Noyau noyau;

    /**
     * La tache sommaire globale.
     */
    private Sommaire tacheSommaire = null;

    /**
     * Gestionnaire des listes des résultats de comparaison de chaque paire de
     * fichiers
     */
    private GestionnaireResultats gestResult;

    /**
     * Crée un nouveau projet.
     *
     * @param noyau Le noyau qui contient le projet.
     */
    public Projet(Noyau noyau) {
        this.noyau = noyau;

        tasks = new ArrayList();
        gestResult = new GestionnaireResultats();
    }

    public File getLastFile() {
        return lastFile;
    }

    public void setLastFile(File lastFile) {
        modifie = true;

        this.lastFile = lastFile;

        if (lastFile == null) {
            modifie = false;
        }
    }

    /**
     * Permet d'obtenir l'attribut "modifie" du projet.
     *
     * @return
     */
    public boolean getModifie() {
        return modifie;
    }

    /**
     * Permet d'accéder au noyau associé au projet.
     *
     * @return Le noyau associé au projet.
     */
    public Noyau getNoyau() {
        return noyau;
    }

    /**
     * Créée une nouvelle tâche de base.
     *
     * @return La nouvelle tâche.
     */
    public Task createTask() {
        Task nouvelleTask = new Task();
        nouvelleTask.setPrefs(noyau.getPrefs());

        registerTask(nouvelleTask);

        return nouvelleTask;
    }

    /**
     * Créée une nouvelle tâche associée à un numéro.
     *
     * @param i Le numéro de référence de l'analyse.
     * @return La nouvelle tâche ainsi créée.
     */
    public Task createTask(int i) {

        Task nouvelleTask = new Task();
        nouvelleTask.setPrefs(noyau.getPrefs());
        nouvelleTask.setGestionnaireResultats(gestResult);

        registerTask(i, nouvelleTask);

        if (tasks.size() == 1) {
            // C'est la première tâche vide ajoutée à un nouveau projet,
            // donc le projet n'est pas réellement modifié.
            modifie = false;
        }

        return nouvelleTask;
    }

    /**
     * Enregistre une tâche dans la liste des tâches.
     *
     * @param i Le numéro de la tâche.
     * @param tsk La tâche à enregistrer.
     * @return La tâche, ou null si erreur.
     */
    private Task registerTask(int i, Task tsk) {
        modifie = true;

        tasks.add(i, tsk);
        return tasks.get(i);
    }

    /**
     *
     * @return La tache sommaire du projet.
     */
    public Sommaire getTacheSommaire() {
        return tacheSommaire;
    }

    /**
     * Assigne la tache sommaire
     *
     * @param tacheSommaire La nouvelle tache sommaire
     */
    public void setTacheSommaire(Sommaire tacheSommaire) {
        this.tacheSommaire = tacheSommaire;
    }

    /**
     * Enregistre une tâche dans la liste des tâches.
     *
     * @param tsk La tâche à enregistrer.
     */
    private void registerTask(Task tsk) {
        modifie = true;
        tasks.add(tsk);
    }

    /**
     * Retire une tâche de la liste des tâches.
     *
     * @param tsk La tâche à retirer.
     */
    public void retirerTask(Task tsk) {
        modifie = true;
        tasks.remove(tsk);
    }

    /**
     * Duplique une tâche et l'enregistre.
     *
     * @param original La tâche à dupliquer.
     * @return La nouvelle tâche.
     */
    public Task duplicateAndRegisterTask(Task original) {
        Task copie = null;

        try {
            copie = original.clone();
            copie.setGestionnaireResultats(gestResult);
        } catch (CloneNotSupportedException ex) {
            // TODO(pascal) : Affiche message d'erreur indiquant que la copie
            // est impossible.
        }

        registerTask(copie);
        return copie;
    }

    public int findTaskTabIndex(Task analyse) {
        int index = 0;
        for (int i = 0; i < tasks.size(); i++) {
            if (analyse == tasks.get(i)) {
                index = i;
                break;
            }
        }
        return index;
    }

    public void restore(File f) {
        SaveAndRestore sav = new SaveAndRestore(this);
        sav.restore(f);
        lastFile = f;

        modifie = false;
    }

    public File save(File f) throws RienASauvegarderException {
        modifie = false;

        SaveAndRestore sav = new SaveAndRestore(this);
        sav.save(f);

        lastFile = f;
        return f;
    }

    @Override
    public StringBuffer toXml() {
        StringBuffer str = new StringBuffer();

        for (Task task : tasks) {
            if (task != null) {
                str.append(task.toXml());
            }
        }

        return str;
    }

    @Override
    public void fromDom(Node node) {
        if ("onglet".equals(node.getNodeName())) {
            //Task pour verifier s'il le task est un sommaire ou non
            Task xmlTestSommaire = new Task();
            if (xmlTestSommaire.xmlEstSommaire(node)) {
                Sommaire restoredSommaire = new Sommaire(tasks);
                restoredSommaire.setPrefs(noyau.getPrefs());
                restoredSommaire.fromDom(node);
                restoredSommaire.setJCheckBoxPreviewFiles(xmlTestSommaire.getJCheckBoxPreviewFiles());
                restoredSommaire.setJCheckBoxAnalyseConcatenation(xmlTestSommaire.getJCheckBoxAnalyseConcatenation());
                restoredSommaire.setPdfExtractor(xmlTestSommaire.getPdfExtractor());
                restoredSommaire.setPdfImages(xmlTestSommaire.getPdfImages());
                restoredSommaire.setCheckBoxCommentaires(xmlTestSommaire.getCheckBoxCommentaires());
                restoredSommaire.setCheckBoxWhitepsaces(xmlTestSommaire.getCheckBoxWhitepsaces());
                restoredSommaire.setIsProgressive(xmlTestSommaire.getIsProgressive());
                try {
                    restoredSommaire.setRedLimit(Double.parseDouble(xmlTestSommaire.getRedLimitTxt()));
                    restoredSommaire.setRedLimitTxt(xmlTestSommaire.getRedLimitTxt());
                } catch (NumberFormatException e) {
                    restoredSommaire.setRedLimitTxt(xmlTestSommaire.getRedLimitTxt());
                }
                try {
                    restoredSommaire.setYellowLimit(Double.parseDouble(xmlTestSommaire.getYellowLimitTxt()));
                    restoredSommaire.setYellowLimitTxt(xmlTestSommaire.getYellowLimitTxt());
                } catch (NumberFormatException e) {
                    restoredSommaire.setYellowLimitTxt(xmlTestSommaire.getYellowLimitTxt());
                }
                try {
                    restoredSommaire.setGreenLimit(Double.parseDouble(xmlTestSommaire.getGreenLimitTxt()));
                    restoredSommaire.setGreenLimitTxt(xmlTestSommaire.getGreenLimitTxt());
                } catch (NumberFormatException e) {
                    restoredSommaire.setGreenLimitTxt(xmlTestSommaire.getGreenLimitTxt());
                }
                registerTask(restoredSommaire);
                this.sommaireExiste = true;
            } else {
                Task restoredTask = new Task();
                restoredTask.setPrefs(noyau.getPrefs());
                restoredTask.fromDom(node);
                registerTask(restoredTask);
                tasks.add(restoredTask);
            }
        }

    }

    /**
     * Permet d'accéder aux tâches du projet.
     *
     * @return Les tâches du projet.
     */
    public List<Task> getTasks() {
        return new ArrayList(tasks);
    }

    /**
     * Enlève toutes les tâches.
     */
    public void clearTasks() {
        modifie = true;
        tasks.clear();
    }

    /**
     * Créée une tâche sommaire en y ajoutant comme taches composantes toutes
     * les taches déjà créer et leurs fichiers comme fichiers d'analyse.
     *
     * @param i L'index de la nouvelle tâche.
     * @return La nouvelle tâche.
     */
    public Sommaire creerTacheSommaire(int i) {
        modifie = true;

        Sommaire newSommaire = new Sommaire(new ArrayList(tasks));
        setTacheSommaire(newSommaire);
        newSommaire.setPrefs(noyau.getPrefs());
        newSommaire.setGestionnaireResultats(gestResult);
        newSommaire.setFichiers(newSommaire.creationFichierSommaire());
        tasks.add(i, newSommaire);

        return newSommaire;
    }

    public boolean verifierFichiersDansAnalyses() {
        boolean fichiersEnAnalyses = false;

        for (Task task : tasks) {
            if (task.getTousFichiers() != null
                    && !task.getTousFichiers().isEmpty()) {
                fichiersEnAnalyses = true;
                break;
            }
        }
        return fichiersEnAnalyses;
    }

}
