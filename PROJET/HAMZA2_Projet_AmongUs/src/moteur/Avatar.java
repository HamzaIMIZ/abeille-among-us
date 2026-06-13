/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package moteur;

import ig.Carte;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.ImageIO;

/**
 * Classe Avatar personnalisable et animable
 *
 * @author guillaume.laurent
 */
public class Avatar {

    protected BufferedImage sprite;
    protected double x, y;
    private boolean toucheGauche, toucheDroite, toucheUp, toucheDown;
    private int largeurCarte;
    private int hauteurCarte;
    private Participant monParticipant; // Référence vers l'objet Participant (pour la synchro BDD)
    private boolean regardeADroite = false;
    
    // NOUVEAU : Type de l'avatar sélectionné
    private String typeAvatar;

    // Constructeur à 3 paramètres par défaut (si aucun type n'est fourni, on prend l'abeille)
    public Avatar(int largeurCarte, int hauteurCarte, Participant j) {
        this(largeurCarte, hauteurCarte, j, "bee");
    }

    // NOUVEAU : Constructeur à 4 paramètres pour le Lobby de sélection
    public Avatar(int largeurCarte, int hauteurCarte, Participant j, String typeAvatar) {
        this.largeurCarte = largeurCarte;
        this.hauteurCarte = hauteurCarte;
        this.monParticipant = j;
        // On passe en minuscules pour correspondre aux noms des fichiers de ressources
        this.typeAvatar = typeAvatar.toLowerCase();

        if (j != null) {
            this.x = j.getPosX();
            this.y = j.getPosY();
        } else {
            this.x = 150 + (Math.random() * 300);
            this.y = 300 + (Math.random() * 300);
        }

        // Chargement dynamique du fichier image
        chargerImage();

        this.toucheGauche = false;
        this.toucheDroite = false;
        this.toucheUp = false;
        this.toucheDown = false;
    }

    /**
     * Charge l'image de l'avatar spécifié ou applique un repli sur bee.png en cas d'absence.
     */
    private void chargerImage() {
        int avatar_size = 20; // Configuration globale à 50*50 pixels
        try {
            String chemin = "../resources/" + this.typeAvatar + ".png";
            BufferedImage imageOriginale = ImageIO.read(getClass().getResource(chemin));
            
            this.sprite = new BufferedImage(avatar_size, avatar_size, BufferedImage.TYPE_INT_ARGB);
            Graphics2D g = this.sprite.createGraphics();
            g.drawImage(imageOriginale, 0, 0, avatar_size, avatar_size, null);
            g.dispose();
        } catch (Exception ex) {
            System.err.println("Ressource introuvable : " + this.typeAvatar + ".png. Repli sur l'abeille.");
            try {
                // Image de secours par défaut
                BufferedImage imageOriginale = ImageIO.read(getClass().getResource("../resources/bee.png"));
                this.sprite = new BufferedImage(avatar_size, avatar_size, BufferedImage.TYPE_INT_ARGB);
                Graphics2D g = this.sprite.createGraphics();
                g.drawImage(imageOriginale, 0, 0, avatar_size, avatar_size, null);
                g.dispose();
            } catch (IOException e) {
                Logger.getLogger(Avatar.class.getName()).log(Level.SEVERE, null, e);
            }
        }
    }

    public void miseAJour() {
        // Nettoyage et uniformisation de la vitesse (ici fixée à 10 pixels par tick)
        if (this.toucheGauche) {
            x -= 15;
            regardeADroite = false; 
        }
        if (this.toucheDroite) {
            x += 15;
            regardeADroite = true; 
        }
        if (this.toucheUp) {
            y -= 15;
        }
        if (this.toucheDown) {
            y += 15;
        }

        // Collisions adaptées dynamiquement aux dimensions réelles du sprite (50x50)
        if (x > largeurCarte - sprite.getWidth()) {
            x = largeurCarte - sprite.getWidth();
        }
        if (x < 0) {
            x = 0;
        }
        if (y > hauteurCarte - sprite.getHeight()) {
            y = hauteurCarte - sprite.getHeight();
        }
        if (y < 0) {
            y = 0;
        }

        // Synchronisation avec l'objet Participant (pour la BDD)
        if (monParticipant != null) {
            monParticipant.setPosX(x);
            monParticipant.setPosY(y);
        }
    }

    public void rendu(Graphics2D contexte, Camera camera) {
        int drawX = (int) (x - camera.getX());
        int drawY = (int) (y - camera.getY());
        int w = sprite.getWidth();  // Utilisation directe de la taille du sprite (50)
        int h = sprite.getHeight(); // Utilisation directe de la taille du sprite (50)

        if (sprite != null) {
            if (regardeADroite) {
                // EFFET MIROIR : On retourne l'image à la volée vers la droite
                contexte.drawImage(sprite,
                        drawX, drawY, drawX + w, drawY + h, 
                        w, 0, 0, h, 
                        null);
            } else {
                // DESSIN NORMAL (Regarde à gauche)
                contexte.drawImage(sprite, drawX, drawY, null);
            }
        }
    }

    // NOUVEAU : Méthode permettant de renvoyer directement un objet Rectangle pour les collisions entre joueurs/fleurs
    public Rectangle getHitbox() {
        return new Rectangle((int) x, (int) y, sprite.getWidth(), sprite.getHeight());
    }

    // Getters / setters pour les touches
    public void setToucheGauche(boolean etat) { this.toucheGauche = etat; }
    public void setToucheDroite(boolean etat) { this.toucheDroite = etat; }
    public void setToucheHaut(boolean b) { this.toucheUp = b; }
    public void setToucheBas(boolean b) { this.toucheDown = b; }

    public double getX() { return x; }
    public double getY() { return y; }
    public void setX(double x) { this.x = x; }
    public void setY(double y) { this.y = y; }

    // Correction de l'inversion hauteur/largeur présente dans tes anciens getters
    public double getLargeur() { return sprite.getWidth(); }
    public double getHauteur() { return sprite.getHeight(); }

    public Participant getParticipant() { return monParticipant; }
    public String getTypeAvatar() { return typeAvatar; }
}