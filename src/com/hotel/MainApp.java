package com.hotel;

import com.hotel.model.Utilisateur;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.HBox;
import javafx.scene.layout.BorderPane;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.stage.Stage;

/**
 * Classe principale de l'application de Gestion Hôtelière.
 * Gère le cycle de vie de l'application JavaFX, l'authentification et la navigation.
 */
public class MainApp extends Application {

    private Stage primaryStage; // Fenêtre principale de l'application
    private static MainApp instance; // Instance unique (Singleton) pour accès global
    private Utilisateur currentUser; // Utilisateur actuellement connecté

    /**
     * Point d'entrée JavaFX. Initialise la fenêtre principale.
     */
    @Override
    public void start(Stage stage) throws Exception {
        instance = this;
        this.primaryStage = stage;
        showLogin(); // Affiche l'écran de connexion au démarrage
    }

    /**
     * Retourne l'instance actuelle de l'application.
     * @return instance de MainApp
     */
    public static MainApp getInstance() {
        return instance;
    }

    /**
     * Charge et affiche la vue de connexion (Login).
     */
    public void showLogin() throws Exception {
        Node loginView = loadView("/com/hotel/view/LoginView.fxml");
        Scene scene = new Scene(new BorderPane(loginView), 800, 500);
        // Application du fichier de styles CSS
        scene.getStylesheets().add(getClass().getResource("/com/hotel/view/styles.css").toExternalForm());
        
        primaryStage.setTitle("Hôtel Management — Connexion");
        primaryStage.setScene(scene);
        primaryStage.centerOnScreen();
        primaryStage.show();
    }

    /**
     * Méthode appelée après une authentification réussie.
     * @param user L'utilisateur qui vient de se connecter
     */
    public void loginSuccess(Utilisateur user) throws Exception {
        this.currentUser = user;
        showMainNav(); // Affiche l'interface principale après le login
    }

    /**
     * Configure et affiche l'interface principale avec barre de navigation.
     * Le contenu de la barre varie selon le rôle de l'utilisateur.
     */
    private void showMainNav() throws Exception {
        // --- 1. Création de la barre de navigation (Toolbar) ---
        HBox navbar = new HBox(12);
        navbar.setAlignment(Pos.CENTER_LEFT);
        navbar.setStyle("-fx-background-color: #3A0CA3; -fx-padding: 10 20;");

        // Label de bienvenue avec nom et rôle
        Label appLabel = new Label("🏨  Hôtel — " + currentUser.getNom() + " (" + currentUser.getRole() + ")");
        appLabel.setStyle("-fx-text-fill: white; -fx-font-size: 16px; -fx-font-weight: bold;");
        navbar.getChildren().add(appLabel);

        // Définition des boutons de navigation
        Button btnDashboard   = createNavBtn("📊  Dashboard");
        Button btnClients     = createNavBtn("👥  Clients");
        Button btnChambres    = createNavBtn("🛏️  Chambres");
        Button btnReserv      = createNavBtn("📅  Réservations");
        Button btnFactures    = createNavBtn("🧾  Factures");
        
        // Bouton de déconnexion stylisé en rouge
        Button btnLogout      = createNavBtn("🚪  Sortir");
        btnLogout.setStyle("-fx-background-color: #EF4444; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 6; -fx-padding: 7 16; -fx-cursor: hand;");

        // --- 2. Filtrage des accès selon le rôle ---
        String role = (currentUser.getRole() != null) ? currentUser.getRole().toLowerCase() : "";

        if (role.contains("admin")) {
            // L'administrateur a accès à tout
            navbar.getChildren().addAll(btnDashboard, btnClients, btnChambres, btnReserv, btnFactures);
        } else if (role.contains("direction")) {
            // La direction ne voit que les statistiques
            navbar.getChildren().addAll(btnDashboard);
        } else if (role.contains("reception")) {
            // Le réceptionniste gère les opérations quotidiennes
            navbar.getChildren().addAll(btnClients, btnChambres, btnReserv, btnFactures);
        } else {
            // Mode restreint par défaut
            navbar.getChildren().addAll(btnClients, btnReserv);
        }
        
        // Ajout d'un espaceur pour pousser le bouton Logout à droite
        javafx.scene.layout.Region spacer = new javafx.scene.layout.Region();
        HBox.setHgrow(spacer, javafx.scene.layout.Priority.ALWAYS);
        navbar.getChildren().addAll(spacer, btnLogout);

        // --- 3. Initialisation du conteneur racine (BorderPane) ---
        BorderPane root = new BorderPane();
        root.setTop(navbar);

        // Chargement de la vue par défaut selon le rôle
        try {
            if (role.contains("direction") || role.contains("admin")) {
                root.setCenter(loadView("/com/hotel/view/Dashboard.fxml"));
                primaryStage.setTitle("Hôtel Management — Tableau de Bord");
            } else {
                root.setCenter(loadView("/com/hotel/view/ReservationCreation.fxml"));
                primaryStage.setTitle("Hôtel Management — Réservations");
            }
        } catch (Exception ex) { ex.printStackTrace(); }

        // --- 4. Configuration de la scène principale ---
        Scene scene = new Scene(root, 1200, 700);
        scene.getStylesheets().add(getClass().getResource("/com/hotel/view/styles.css").toExternalForm());

        primaryStage.setScene(scene);
        primaryStage.centerOnScreen();

        // --- 5. Définition des actions de clic sur les boutons ---
        btnDashboard.setOnAction(e -> {
            try { root.setCenter(loadView("/com/hotel/view/Dashboard.fxml")); primaryStage.setTitle("Hôtel — Dashboard"); } catch (Exception ex) { ex.printStackTrace(); }
        });
        btnClients.setOnAction(e -> {
            try { root.setCenter(loadView("/com/hotel/view/GestionClients.fxml")); primaryStage.setTitle("Hôtel — Clients"); } catch (Exception ex) { ex.printStackTrace(); }
        });
        btnChambres.setOnAction(e -> {
            try { root.setCenter(loadView("/com/hotel/view/ChambreView.fxml")); primaryStage.setTitle("Hôtel — Chambres"); } catch (Exception ex) { ex.printStackTrace(); }
        });
        btnReserv.setOnAction(e -> {
            try { root.setCenter(loadView("/com/hotel/view/ReservationCreation.fxml")); primaryStage.setTitle("Hôtel — Réservations"); } catch (Exception ex) { ex.printStackTrace(); }
        });
        btnFactures.setOnAction(e -> {
            try { root.setCenter(loadView("/com/hotel/view/GestionFactures.fxml")); primaryStage.setTitle("Hôtel — Factures"); } catch (Exception ex) { ex.printStackTrace(); }
        });
        btnLogout.setOnAction(e -> {
            try { this.currentUser = null; showLogin(); } catch (Exception ex) { ex.printStackTrace(); } // Retour au login
        });
    }

    /**
     * Utilitaire pour créer un bouton de navigation avec un style harmonisé et effets de survol.
     */
    private Button createNavBtn(String text) {
        Button btn = new Button(text);
        // Style de base
        btn.setStyle(
            "-fx-background-color: rgba(255,255,255,0.15);" +
            "-fx-text-fill: white; -fx-font-weight: bold;" +
            "-fx-background-radius: 6; -fx-padding: 7 16; -fx-cursor: hand;"
        );
        // Changement de couleur au survol de la souris
        btn.setOnMouseEntered(e -> btn.setStyle(
            "-fx-background-color: rgba(255,255,255,0.3);" +
            "-fx-text-fill: white; -fx-font-weight: bold;" +
            "-fx-background-radius: 6; -fx-padding: 7 16; -fx-cursor: hand;"
        ));
        btn.setOnMouseExited(e -> btn.setStyle(
            "-fx-background-color: rgba(255,255,255,0.15);" +
            "-fx-text-fill: white; -fx-font-weight: bold;" +
            "-fx-background-radius: 6; -fx-padding: 7 16; -fx-cursor: hand;"
        ));
        return btn;
    }

    /**
     * Utilitaire de chargement de fichiers FXML.
     * @param fxmlPath Le chemin vers le fichier .fxml
     * @return Le composant racine (Node) chargé
     */
    private Node loadView(String fxmlPath) throws Exception {
        FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
        return loader.load();
    }

    /**
     * Lancement de l'application.
     */
    public static void main(String[] args) {
        launch(args);
    }
}