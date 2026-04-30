/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
 
package moteur;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.ImageIO;
import javax.swing.Timer;
import sql.JoueurSQL;

public class Jeu {

    // ---------- Attributs de la version mono-joueur
    private BufferedImage fond;
    private Avatar avatar;  // devient l'avatar contrôlé par ce joueur
    private Fleur fleur;
    private Camera camera;
    private int score;
    private final int LARGEUR_CARTE = 3904;
    private final int HAUTEUR_CARTE = 1968;
    private final int LARGEUR_ECRAN;
    private final int HAUTEUR_ECRAN;

    // ---------- NOUVEAUX attributs pour le multijoueur ----------
    private List<Joueur> autresJoueurs;   // liste des joueurs distants (données lues en BDD)
    private JoueurSQL JoueurSql;                // accès à la base de données
    private Timer timerSync;              // timer pour la synchronisation périodique
    private int monJoueurId;             // identifiant du joueur local dans la BDD
    private BufferedImage spriteAutreJoueur; // image utilisée pour dessiner les autres joueurs

    // ---------- CONSTRUCTEUR (modifié) ----------
    // Version mono-joueur : public Jeu(int largeurEcran, int hauteurEcran)
    // Version multijoueur : on ajoute le paramètre Joueur monCompte
    public Jeu(int largeurEcran, int hauteurEcran, Joueur monCompte) {
        this.LARGEUR_ECRAN = largeurEcran;
        this.HAUTEUR_ECRAN = hauteurEcran;
        this.monJoueurId = monCompte.getId();
        this.JoueurSql = new JoueurSQL();
        this.autresJoueurs = new ArrayList<>();
        this.score = 0;

        // Chargement du fond
        try {
            this.fond = ImageIO.read(getClass().getClassLoader().getResource("resources/map.png"));
        } catch (IOException ex) {
            Logger.getLogger(Jeu.class.getName()).log(Level.SEVERE, null, ex);
        }

        // Chargement du sprite pour les autres joueurs (NOUVEAU)
        try {
            BufferedImage original = ImageIO.read(getClass().getResource("../resources/bee.png"));
            this.spriteAutreJoueur = new BufferedImage(25, 25, BufferedImage.TYPE_INT_ARGB);
            Graphics2D g = this.spriteAutreJoueur.createGraphics();
            g.drawImage(original, 0, 0, 25, 25, null);
            g.dispose();
        } catch (IOException ex) {
            Logger.getLogger(Jeu.class.getName()).log(Level.SEVERE, null, ex);
        }

        // Création de la fleur
        this.fleur = new Fleur(LARGEUR_CARTE, HAUTEUR_CARTE);

        // Création de l'avatar (modifié : on lie l'avatar au compte Joueur)
        this.avatar = new Avatar(LARGEUR_CARTE, HAUTEUR_CARTE, monCompte);
        // On positionne l'avatar aux coordonnées enregistrées en BDD (NOUVEAU)
        this.avatar.setX(monCompte.getPosX());
        this.avatar.setY(monCompte.getPosY());

        this.camera = new Camera(LARGEUR_ECRAN, HAUTEUR_ECRAN);

        // NOUVEAU : Timer de synchronisation avec la BDD (toutes les 100 ms)
        this.timerSync = new Timer(100, (e) -> {
            // 1. Envoyer notre position et notre score dans la BDD
            JoueurSql.mettreAJourPositionScore(monJoueurId, avatar.getX(), avatar.getY(), this.score);
            // 2. Récupérer la liste des autres joueurs actifs
            List<Joueur> tous = JoueurSql.getAutresJoueurs(monJoueurId);
            autresJoueurs.clear();
            autresJoueurs.addAll(tous);
        });
        this.timerSync.start();
    }

    // ---------- METHODE miseAJour (inchangée dans sa logique) ----------
    public void miseAJour() {
        this.avatar.miseAJour();      // déplacement local
        this.fleur.miseAJour();       // mise à jour de la fleur (si elle bouge)
        if (collisionEntreAvatarEtFleur()) {
            this.score++;
            fleur.relancer();
        }
        this.camera.centrerSur(avatar.getX(), avatar.getY(), LARGEUR_CARTE, HAUTEUR_CARTE);
    }

    // ---------- METHODE rendu (modifiée pour afficher les autres joueurs) ----------
    public void rendu(Graphics2D contexte) {
        // Fond noir et dessin de la map (inchangé)
        contexte.setColor(java.awt.Color.BLACK);
        contexte.fillRect(0, 0, LARGEUR_ECRAN, HAUTEUR_ECRAN);
        contexte.drawImage(this.fond, (int) -camera.getX(), (int) -camera.getY(), null);

        // Dessin de la fleur
        this.fleur.rendu(contexte, camera);

        // NOUVEAU : dessin des autres joueurs (leurs avatars)
        for (Joueur autre : autresJoueurs) {
            int screenX = (int) (autre.getPosX() - camera.getX());
            int screenY = (int) (autre.getPosY() - camera.getY());
            contexte.drawImage(this.spriteAutreJoueur, screenX, screenY, null);
            contexte.setColor(java.awt.Color.WHITE);
            contexte.drawString(autre.getNom(), screenX, screenY - 5);
        }

        // Dessin de l'avatar local
        this.avatar.rendu(contexte, camera);

        // Affichage du score (inchangé)
        contexte.setColor(java.awt.Color.WHITE);
        contexte.drawString("Score : " + this.score, 10, 20);
    }

    // ---------- Collision 
    private boolean collisionEntreAvatarEtFleur() {
        double ax = avatar.getX(), ay = avatar.getY(), aw = avatar.getLargeur(), ah = avatar.getHauteur();
        double fx = fleur.getX(), fy = fleur.getY(), fw = fleur.getLargeur(), fh = fleur.getHauteur();
        return !(fx >= ax + aw || fx + fw <= ax || fy >= ay + ah || fy + fh <= ay);
    }

    // ---------- estTermine
    public boolean estTermine() {
        return this.fleur.getY() > HAUTEUR_CARTE;
    }

    // ---------- NOUVELLE METHODE pour arrêter proprement le jeu ----------
    public void arreter() {
        if (timerSync != null && timerSync.isRunning()) {
            timerSync.stop();
        }
        JoueurSql.setActif(monJoueurId, false);
        JoueurSql.closeTable();
    }

    // ---------- Getter pour l'avatar (utilisé par FenetreDeJeu pour les touches) ----------
    public Avatar getAvatar() {
        return this.avatar;
    }
}