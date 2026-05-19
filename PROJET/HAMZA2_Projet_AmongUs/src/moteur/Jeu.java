/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
 
package moteur;

import ig.Carte;
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

    private Carte carte;
    private Avatar avatar;  
    private Fleur fleur;
    private Camera camera;
    private int score;
    private final int LARGEUR_CARTE = 3904;
    private final int HAUTEUR_CARTE = 1968;
    private final int LARGEUR_ECRAN;
    private final int HAUTEUR_ECRAN;

    private List<Participant> autresParticipants;   // liste des joueurs distants (données lues en BDD)
    private JoueurSQL JoueurSql;                // accès à la base de données
    private Timer timerSync;              // timer pour la synchronisation périodique
    private int monParticipantId;             // identifiant du joueur local dans la BDD
    private BufferedImage spriteAutreParticipant; // image utilisée pour dessiner les autres joueurs

    public Jeu(int largeurEcran, int hauteurEcran, Participant monCompte) {
        this.LARGEUR_ECRAN = largeurEcran;
        this.HAUTEUR_ECRAN = hauteurEcran;
        this.monParticipantId = monCompte.getId();
        this.JoueurSql = new JoueurSQL();
        this.autresParticipants = new ArrayList<>();
        this.score = 0;
        
        String tmxFile = getClass().getResource("/resources/map.tmx").getPath();
        this.carte = new Carte(tmxFile);
        // Chargement du sprite pour les autres joueurs (NOUVEAU)
        try {
            BufferedImage original = ImageIO.read(getClass().getResource("../resources/bee.png"));
            this.spriteAutreParticipant = new BufferedImage(25, 25, BufferedImage.TYPE_INT_ARGB);
            Graphics2D g = this.spriteAutreParticipant.createGraphics();
            g.drawImage(original, 0, 0, 25, 25, null);
            g.dispose();
        } catch (IOException ex) {
            Logger.getLogger(Jeu.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        this.fleur = new Fleur(LARGEUR_CARTE, HAUTEUR_CARTE, 1); // Création de la fleur
        this.avatar = new Avatar(LARGEUR_CARTE, HAUTEUR_CARTE, monCompte); // Création de l'avatar
        // On positionne l'avatar aux coordonnées enregistrées en BDD (NOUVEAU)
        this.avatar.setX(monCompte.getPosX());
        this.avatar.setY(monCompte.getPosY());

        this.camera = new Camera(LARGEUR_ECRAN, HAUTEUR_ECRAN);

        // NOUVEAU : Timer de synchronisation avec la BDD (toutes les 100 ms)
        this.timerSync = new Timer(100, (e) -> {
            // 1. Envoyer notre position et notre score dans la BDD
            JoueurSql.mettreAJourPositionScore(monParticipantId, avatar.getX(), avatar.getY(), this.score);
            // 2. Récupérer la liste des autres joueurs actifs
            List<Participant> tous = JoueurSql.getAutresParticipant(monParticipantId);
            autresParticipants.clear();
            autresParticipants.addAll(tous);
        });
        this.timerSync.start();
    }

    public void miseAJour() {
        this.carte.miseAJour();
        // Save old position before moving
        double oldX = avatar.getX();
        double oldY = avatar.getY();
        this.avatar.miseAJour();      // déplacement local
        
        // Check collision with walls
        if (collisionAvecMurs(avatar.getX(), avatar.getY())) { // Collision detected! Restore old position
            avatar.setX(oldX);
            avatar.setY(oldY);
            if (avatar.getParticipant() != null) { // Update Participant object too if it exists
                avatar.getParticipant().setPosX(oldX);
                avatar.getParticipant().setPosY(oldY);
            }
        }
        this.fleur.miseAJour();       // mise à jour de la fleur (si elle bouge)
        if (collisionEntreAvatarEtFleur()) {
            if (this.avatar.getParticipant() != null) {
                this.avatar.getParticipant().calculpoint();
                this.score = this.avatar.getParticipant().getScoreSession();
            }
            
            fleur.relancer(this.carte);
        }
        this.camera.centrerSur(avatar.getX(), avatar.getY(), LARGEUR_CARTE, HAUTEUR_CARTE);

    }

    public void rendu(Graphics2D contexte) {
        // Fond noir et dessin de la map (inchangé)
        contexte.setColor(java.awt.Color.WHITE);
        contexte.fillRect(0, 0, LARGEUR_ECRAN, HAUTEUR_ECRAN);
        contexte.translate((int) -camera.getX(), (int) -camera.getY());
        this.carte.rendu(contexte);
        contexte.translate((int) camera.getX(), (int) camera.getY());

        // Dessin de la fleur
        this.fleur.rendu(contexte, camera);

        // NOUVEAU : dessin des autres joueurs (leurs avatars)
        for (Participant autre : autresParticipants) {
            int screenX = (int) (autre.getPosX() - camera.getX());
            int screenY = (int) (autre.getPosY() - camera.getY());
            contexte.drawImage(this.spriteAutreParticipant, screenX, screenY, null);
            contexte.setColor(java.awt.Color.WHITE);
            contexte.drawString(autre.getNom(), screenX, screenY - 5);
        }

        // Dessin de l'avatar local
        this.avatar.rendu(contexte, camera);

        // Affichage du score (inchangé)
        contexte.setColor(java.awt.Color.BLACK);
        contexte.drawString("Score : " + this.score, 10, 20);
    }

    //  Collision with abeille
    private boolean collisionEntreAvatarEtFleur() {
        double ax = avatar.getX(), ay = avatar.getY(), aw = avatar.getLargeur(), ah = avatar.getHauteur();
        double fx = fleur.getX(), fy = fleur.getY(), fw = fleur.getLargeur(), fh = fleur.getHauteur();
        return !(fx >= ax + aw || fx + fw <= ax || fy >= ay + ah || fy + fh <= ay);
    }
    
    // Check collision with wqlls
    private boolean collisionAvecMurs(double newX, double newY) {
        int[][] rooms = carte.getRooms();
        if (rooms == null) return false;

        int tailleTuile = carte.getTailleTuile();
        int largeurSprite = (int) avatar.getLargeur();
        int hauteurSprite = (int) avatar.getHauteur();

        // Check the 4 corners of the avatar
        // Top-left corner
        int tileX1 = (int) newX / tailleTuile;
        int tileY1 = (int) newY / tailleTuile;

        // Top-right corner
        int tileX2 = (int) (newX + largeurSprite - 1) / tailleTuile;
        int tileY2 = (int) newY / tailleTuile;

        // Bottom-left corner
        int tileX3 = (int) newX / tailleTuile;
        int tileY3 = (int) (newY + hauteurSprite - 1) / tailleTuile;

        // Bottom-right corner
        int tileX4 = (int) (newX + largeurSprite - 1) / tailleTuile;
        int tileY4 = (int) (newY + hauteurSprite - 1) / tailleTuile;

        // Check if any corner is on a wall tile (value != -1)
        if (estSurMur(rooms, tileX1, tileY1)) return true;
        if (estSurMur(rooms, tileX2, tileY2)) return true;
        if (estSurMur(rooms, tileX3, tileY3)) return true;
        if (estSurMur(rooms, tileX4, tileY4)) return true;

        return false;
    }

    // check if a tile position is a wall
    private boolean estSurMur(int[][] rooms, int tileX, int tileY) {
        if (tileY < 0 || tileY >= rooms.length || tileX < 0 || tileX >= rooms[0].length) {
            return true; // Out of bounds = treat as wall
        }
        return rooms[tileY][tileX] != -1; // If value is not -1, it's a wall
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
        JoueurSql.closeTable();
    }

    // ---------- Getter pour l'avatar (utilisé par FenetreDeJeu pour les touches) ----------
    public Avatar getAvatar() {
        return this.avatar;
    }
}