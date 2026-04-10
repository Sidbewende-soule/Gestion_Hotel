package com.hotel.controller;

import com.hotel.dao.DashboardDAO;
import com.hotel.model.ReservationResume;
import com.hotel.model.StatsDashboard;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.print.PrinterJob;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;
import javafx.scene.paint.LinearGradient;
import javafx.scene.paint.Stop;
import javafx.scene.paint.CycleMethod;
import javafx.stage.FileChooser;
import javafx.util.Duration;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URL;
import java.time.LocalDateTime;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.ResourceBundle;

/**
 * Controller du tableau de bord.
 * – Affiche 4 KPIs temps réel
 * – Graphique barres (Canvas) revenus mensuels
 * – Rafraîchissement automatique toutes les 30 s
 * – Export CSV (Excel) et PDF (PrinterJob)
 */
public class DashboardController implements Initializable {

    // ── KPI Labels ─────────────────────────────────────────────────────────────
    @FXML private Label kpiReservations;
    @FXML private Label kpiReservationsTotal;
    @FXML private Label kpiRevenus;
    @FXML private Label kpiTauxOccupation;
    @FXML private Label kpiChambres;
    @FXML private Label kpiSatisfaction;
    @FXML private Label kpiNombreAvis;
    @FXML private Label lblDerniereMaj;

    // ── Résumé stats (panneau export) ─────────────────────────────────────────
    @FXML private Label statReservTotal;
    @FXML private Label statRevenus;
    @FXML private Label statOccupation;
    @FXML private Label statSatisf;

    // ── Graphique ──────────────────────────────────────────────────────────────
    @FXML private Canvas canvasChart;
    @FXML private ComboBox<Integer> cbAnnee;
    @FXML private HBox legendeMois;

    // ── Tableau réservations récentes ─────────────────────────────────────────
    @FXML private TableView<ReservationResume> tableReservations;
    @FXML private TableColumn<ReservationResume, Integer>   colId;
    @FXML private TableColumn<ReservationResume, String>    colClient;
    @FXML private TableColumn<ReservationResume, String>    colChambre;
    @FXML private TableColumn<ReservationResume, LocalDate> colDebut;
    @FXML private TableColumn<ReservationResume, LocalDate> colFin;
    @FXML private TableColumn<ReservationResume, String>    colStatut;

    // ── Boutons ────────────────────────────────────────────────────────────────
    @FXML private Button btnExportPDF;
    @FXML private Button btnExportExcel;
    @FXML private Button btnActualiser;

    // ── Internes ───────────────────────────────────────────────────────────────
    private final DashboardDAO dao = new DashboardDAO();
    private StatsDashboard     stats;
    private Timeline           autoRefresh;

    private static final String[] MOIS_NOMS = {
        "Jan","Fév","Mar","Avr","Mai","Jun",
        "Jul","Aoû","Sep","Oct","Nov","Déc"
    };

    // ── Couleurs graphique ─────────────────────────────────────────────────────
    private static final String[] COULEURS_BARRES = {
        "#4361EE","#3F37C9","#4895EF","#4CC9F0","#7209B7",
        "#560BAD","#480CA8","#3A0CA3","#3F37C9","#4361EE","#4895EF","#4CC9F0"
    };

    // ══════════════════════════════════════════════════════════════════════════
    // Initialisation
    // ══════════════════════════════════════════════════════════════════════════

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        configurerColonnesTableau();
        remplirComboAnnee();
        chargerDonnees();
        demarrerRefreshAuto();
    }

    // ── Config tableau ─────────────────────────────────────────────────────────
    private void configurerColonnesTableau() {
        colId.setCellValueFactory(new PropertyValueFactory<>("idReservation"));
        colClient.setCellValueFactory(new PropertyValueFactory<>("nomClient"));
        colChambre.setCellValueFactory(new PropertyValueFactory<>("chambre"));
        colDebut.setCellValueFactory(new PropertyValueFactory<>("dateDebut"));
        colFin.setCellValueFactory(new PropertyValueFactory<>("dateFin"));
        colStatut.setCellValueFactory(new PropertyValueFactory<>("statut"));

        // Colonne statut colorée
        colStatut.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(String statut, boolean empty) {
                super.updateItem(statut, empty);
                if (empty || statut == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(statut);
                    String couleur = switch (statut.toUpperCase()) {
                        case "CONFIRMEE"  -> "-fx-text-fill: #059669; -fx-font-weight: bold;";
                        case "EN_ATTENTE" -> "-fx-text-fill: #D97706; -fx-font-weight: bold;";
                        case "ANNULEE"    -> "-fx-text-fill: #DC2626; -fx-font-weight: bold;";
                        case "TERMINEE"   -> "-fx-text-fill: #6366F1; -fx-font-weight: bold;";
                        default            -> "-fx-text-fill: #475569;";
                    };
                    setStyle(couleur);
                }
            }
        });
    }

    // ── Remplir combo année ────────────────────────────────────────────────────
    private void remplirComboAnnee() {
        int anneeActuelle = LocalDate.now().getYear();
        ObservableList<Integer> annees = FXCollections.observableArrayList();
        for (int a = anneeActuelle; a >= anneeActuelle - 4; a--) {
            annees.add(a);
        }
        cbAnnee.setItems(annees);
        cbAnnee.setValue(anneeActuelle);
    }

    // ── Chargement principal ───────────────────────────────────────────────────
    private void chargerDonnees() {
        int annee = cbAnnee.getValue() != null ? cbAnnee.getValue() : LocalDate.now().getYear();

        // Chargement dans un thread background pour ne pas geler l'UI
        Thread t = new Thread(() -> {
            StatsDashboard s = new StatsDashboard();
            s.setReservationsActives(dao.getNombreReservationsActives());
            s.setTotalReservations(dao.getNombreTotalReservations());
            s.setChambresTotal(dao.getNombreChambresTotal());
            s.setChambresOccupees(dao.getNombreChambresOccupees());
            s.setTauxOccupation(dao.getTauxOccupation());
            s.setTotalRevenus(dao.getTotalRevenus());
            s.setSatisfactionMoyenne(dao.getNoteSatisfactionMoyenne());
            s.setNombreAvis(dao.getNombreAvis());
            s.setRevenusParMois(dao.getRevenusParMois(annee));

            java.util.List<ReservationResume> reservations = dao.getReservationsRecentes(15);

            Platform.runLater(() -> {
                this.stats = s;
                mettreAJourKPIs(s);
                mettreAJourTableau(reservations);
                dessinerGraphique(s.getRevenusParMois());
                mettreAJourHorodatage();
            });
        });
        t.setDaemon(true);
        t.start();
    }

    // ── Mise à jour KPIs ───────────────────────────────────────────────────────
    private void mettreAJourKPIs(StatsDashboard s) {
        kpiReservations.setText(String.valueOf(s.getReservationsActives()));
        kpiReservationsTotal.setText(s.getTotalReservations() + " réservations au total");

        kpiRevenus.setText(String.format("%,.0f FCFA", s.getTotalRevenus()));

        kpiTauxOccupation.setText(String.format("%.1f %%", s.getTauxOccupation()));
        kpiChambres.setText(s.getChambresOccupees() + " / " + s.getChambresTotal() + " chambres");

        kpiSatisfaction.setText(String.format("%.1f / 5", s.getSatisfactionMoyenne()));
        kpiNombreAvis.setText(s.getNombreAvis() + " avis");

        // Résumé panneau export
        statReservTotal.setText("Réservations : " + s.getTotalReservations());
        statRevenus.setText(String.format("Revenus : %,.0f FCFA", s.getTotalRevenus()));
        statOccupation.setText(String.format("Occupation : %.1f %%", s.getTauxOccupation()));
        statSatisf.setText(String.format("Satisfaction : %.1f/5", s.getSatisfactionMoyenne()));
    }

    // ── Mise à jour tableau ────────────────────────────────────────────────────
    private void mettreAJourTableau(java.util.List<ReservationResume> liste) {
        tableReservations.setItems(FXCollections.observableArrayList(liste));
    }

    // ── Horodatage ────────────────────────────────────────────────────────────
    private void mettreAJourHorodatage() {
        String heure = LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss"));
        lblDerniereMaj.setText("↻ Mise à jour : " + heure);
    }

    // ══════════════════════════════════════════════════════════════════════════
    // Graphique en barres (Canvas)
    // ══════════════════════════════════════════════════════════════════════════

    private void dessinerGraphique(Map<Integer, Double> revenusParMois) {
        double largeur  = canvasChart.getWidth();
        double hauteur  = canvasChart.getHeight();
        GraphicsContext gc = canvasChart.getGraphicsContext2D();

        // Fond
        gc.clearRect(0, 0, largeur, hauteur);
        gc.setFill(Color.web("#F8FAFF"));
        gc.fillRect(0, 0, largeur, hauteur);

        // Paramètres du graphique
        double margeGauche = 60;
        double margeDroite = 20;
        double margeHaut   = 20;
        double margeBas    = 40;
        double zoneW = largeur - margeGauche - margeDroite;
        double zoneH = hauteur - margeHaut - margeBas;

        // Valeur max pour normaliser
        double maxVal = revenusParMois.values().stream()
                                      .mapToDouble(Double::doubleValue).max().orElse(1.0);
        if (maxVal == 0) maxVal = 1;

        // Grille horizontale (5 lignes)
        gc.setStroke(Color.web("#E2E8F0"));
        gc.setLineWidth(1);
        for (int i = 0; i <= 5; i++) {
            double y = margeHaut + zoneH - (zoneH * i / 5.0);
            gc.strokeLine(margeGauche, y, margeGauche + zoneW, y);
            // Étiquette valeur
            gc.setFill(Color.web("#94A3B8"));
            gc.setFont(javafx.scene.text.Font.font(10));
            gc.fillText(String.format("%,.0f", maxVal * i / 5.0), 2, y + 4);
        }

        // Axe Y
        gc.setStroke(Color.web("#CBD5E1"));
        gc.setLineWidth(1.5);
        gc.strokeLine(margeGauche, margeHaut, margeGauche, margeHaut + zoneH);
        // Axe X
        gc.strokeLine(margeGauche, margeHaut + zoneH, margeGauche + zoneW, margeHaut + zoneH);

        // Barres
        double barLargeur = (zoneW / 12.0) * 0.6;
        double barEspace  = (zoneW / 12.0);

        for (int mois = 1; mois <= 12; mois++) {
            double montant = revenusParMois.getOrDefault(mois, 0.0);
            double barH    = (montant / maxVal) * zoneH;
            double x       = margeGauche + (mois - 1) * barEspace + (barEspace - barLargeur) / 2.0;
            double y       = margeHaut + zoneH - barH;

            // Dégradé de la barre
            if (barH > 0) {
                LinearGradient gradient = new LinearGradient(
                    0, y, 0, y + barH, false, CycleMethod.NO_CYCLE,
                    new Stop(0, Color.web(COULEURS_BARRES[mois - 1])),
                    new Stop(1, Color.web(COULEURS_BARRES[mois - 1] + "88"))
                );
                gc.setFill(gradient);
                // Coins arrondis simulés
                gc.fillRoundRect(x, y, barLargeur, barH, 4, 4);

                // Valeur au-dessus si assez de place
                if (barH > 18) {
                    gc.setFill(Color.web("#1E293B"));
                    gc.setFont(javafx.scene.text.Font.font(9));
                    String valTxt = montant >= 1_000_000 ? String.format("%.1fM", montant / 1_000_000)
                                  : montant >= 1_000     ? String.format("%.0fk", montant / 1_000)
                                  : String.format("%.0f", montant);
                    gc.fillText(valTxt, x + barLargeur / 2 - 8, y - 3);
                }
            } else {
                // Barre vide (hauteur minimale)
                gc.setFill(Color.web("#E2E8F0"));
                gc.fillRoundRect(x, margeHaut + zoneH - 3, barLargeur, 3, 2, 2);
            }

            // Étiquette mois
            gc.setFill(Color.web("#64748B"));
            gc.setFont(javafx.scene.text.Font.font(10));
            gc.fillText(MOIS_NOMS[mois - 1], x + barLargeur / 2 - 8, margeHaut + zoneH + 16);
        }
    }

    // ══════════════════════════════════════════════════════════════════════════
    // Auto-refresh (30 secondes)
    // ══════════════════════════════════════════════════════════════════════════

    private void demarrerRefreshAuto() {
        autoRefresh = new Timeline(new KeyFrame(Duration.seconds(30), e -> chargerDonnees()));
        autoRefresh.setCycleCount(Timeline.INDEFINITE);
        autoRefresh.play();
    }

    // ══════════════════════════════════════════════════════════════════════════
    // Actions FXML
    // ══════════════════════════════════════════════════════════════════════════

    @FXML
    private void actualiser() {
        chargerDonnees();
    }

    @FXML
    private void changerAnnee() {
        chargerDonnees();
    }

    // ── Export CSV (Excel) ─────────────────────────────────────────────────────
    @FXML
    private void exporterExcel() {
        FileChooser fc = new FileChooser();
        fc.setTitle("Enregistrer le rapport Excel");
        fc.setInitialFileName("rapport_hotel_" + LocalDate.now() + ".csv");
        fc.getExtensionFilters().add(
            new FileChooser.ExtensionFilter("Fichier CSV / Excel (*.csv)", "*.csv"));

        File fichier = fc.showSaveDialog(btnExportExcel.getScene().getWindow());
        if (fichier == null) return;

        try (BufferedWriter bw = new BufferedWriter(new FileWriter(fichier))) {
            // En-tête rapport
            bw.write("RAPPORT HÔTEL — " + LocalDateTime.now()
                      .format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")));
            bw.newLine();
            bw.newLine();

            // KPIs
            bw.write("=== INDICATEURS CLÉS ===");  bw.newLine();
            bw.write("Réservations actives;" + (stats != null ? stats.getReservationsActives() : "—")); bw.newLine();
            bw.write("Total réservations;"  + (stats != null ? stats.getTotalReservations()   : "—")); bw.newLine();
            bw.write("Chambres total;"      + (stats != null ? stats.getChambresTotal()        : "—")); bw.newLine();
            bw.write("Chambres occupées;"   + (stats != null ? stats.getChambresOccupees()     : "—")); bw.newLine();
            bw.write(String.format("Taux d'occupation;%.1f %%",
                     stats != null ? stats.getTauxOccupation() : 0)); bw.newLine();
            bw.write(String.format("Revenus encaissés;%,.0f FCFA",
                     stats != null ? stats.getTotalRevenus() : 0)); bw.newLine();
            bw.write(String.format("Satisfaction moyenne;%.1f / 5",
                     stats != null ? stats.getSatisfactionMoyenne() : 0)); bw.newLine();
            bw.write("Nombre d'avis;"      + (stats != null ? stats.getNombreAvis() : "—")); bw.newLine();
            bw.newLine();

            // Revenus par mois
            bw.write("=== REVENUS MENSUELS (" + cbAnnee.getValue() + ") ==="); bw.newLine();
            bw.write("Mois;Revenus (FCFA)"); bw.newLine();
            if (stats != null && stats.getRevenusParMois() != null) {
                for (Map.Entry<Integer, Double> entry : stats.getRevenusParMois().entrySet()) {
                    bw.write(MOIS_NOMS[entry.getKey() - 1] + ";"
                             + String.format("%.0f", entry.getValue()));
                    bw.newLine();
                }
            }
            bw.newLine();

            // Tableau réservations récentes
            bw.write("=== RÉSERVATIONS RÉCENTES ==="); bw.newLine();
            bw.write("N°;Client;Chambre;Arrivée;Départ;Statut"); bw.newLine();
            for (ReservationResume r : tableReservations.getItems()) {
                bw.write(r.getIdReservation() + ";"
                       + r.getNomClient()     + ";"
                       + r.getChambre()       + ";"
                       + r.getDateDebut()     + ";"
                       + r.getDateFin()       + ";"
                       + r.getStatut());
                bw.newLine();
            }

            afficherInfo("✅  Export réussi", "Fichier enregistré :\n" + fichier.getAbsolutePath());

        } catch (IOException ex) {
            afficherErreur("Erreur export CSV", ex.getMessage());
        }
    }

    // ── Export PDF via PrinterJob ──────────────────────────────────────────────
    @FXML
    private void exporterPDF() {
        PrinterJob job = PrinterJob.createPrinterJob();
        if (job == null) {
            afficherErreur("Erreur", "Aucune imprimante disponible.\n"
                + "Veuillez installer « Microsoft Print to PDF ».");
            return;
        }
        // Ouvrir le dialogue d'impression (choix Microsoft Print to PDF)
        boolean affiche = job.showPrintDialog(btnExportPDF.getScene().getWindow());
        if (!affiche) return;

        // Imprimer la scène entière (le ScrollPane parent)
        javafx.scene.Node racine = btnExportPDF.getScene().getRoot();
        boolean ok = job.printPage(racine);
        if (ok) {
            job.endJob();
            afficherInfo("✅  Export PDF", "Le document a été envoyé à l'imprimante/PDF.");
        } else {
            afficherErreur("Erreur PDF", "L'impression a échoué.");
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Utilitaires dialogue
    // ─────────────────────────────────────────────────────────────────────────
    private void afficherInfo(String titre, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(titre);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void afficherErreur(String titre, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(titre);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
