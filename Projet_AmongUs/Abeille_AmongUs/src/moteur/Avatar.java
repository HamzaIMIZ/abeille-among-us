/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package moteur;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.ImageIO;

/**
 * Exemple de classe lutin
 *
 * @author guillaume.laurent
 */
    
    public class Avatar {

    protected BufferedImage sprite;
    protected double x, y;
    private boolean toucheGauche, toucheDroite, toucheUp, toucheDown;
    private int largeurCarte;
    private int hauteurCarte;
    
    // référence vers l'objet Joueur (pour la synchro BDD)
    private Joueur monJoueur;

    // Constructeur mono‑joueur (peut être supprimé)
    public Avatar(int largeurCarte, int hauteurCarte) {
        this(largeurCarte, hauteurCarte, null);
    }

    // Constructeur multijoueur
    public Avatar(int largeurCarte, int hauteurCarte, Joueur j) {
        this.largeurCarte = largeurCarte;
        this.hauteurCarte = hauteurCarte;
        this.monJoueur = j;
        
        if (j != null) {
            this.x = j.getPosX();
            this.y = j.getPosY();
        } else {
            this.x = 170;
            this.y = 320;
        }

        int avatar_size = 25;
        try {
            BufferedImage imageOriginale = ImageIO.read(getClass().getResource("../resources/bee.png"));
            this.sprite = new BufferedImage(avatar_size, avatar_size, BufferedImage.TYPE_INT_ARGB);
            Graphics2D g = this.sprite.createGraphics();
            g.drawImage(imageOriginale, 0, 0, avatar_size, avatar_size, null);
            g.dispose();
        } catch (IOException ex) {
            Logger.getLogger(Avatar.class.getName()).log(Level.SEVERE, null, ex);
        }

        this.toucheGauche = false;
        this.toucheDroite = false;
        this.toucheUp = false;
        this.toucheDown = false;
    }

    public void miseAJour() {
        if (this.toucheGauche) { x -= 10; }
        if (this.toucheDroite) { x += 10; }
        if (this.toucheUp) { y -= 10; }
        if (this.toucheDown) { y += 10; }

        // Collisions avec les bords de la carte
        if (x > largeurCarte - sprite.getWidth()) { x = largeurCarte - sprite.getWidth(); }
        if (x < 0) { x = 0; }
        if (y > hauteurCarte - sprite.getHeight()) { y = hauteurCarte - sprite.getHeight(); }
        if (y < 0) { y = 0; }

        // Synchronisation avec l'objet Joueur (pour la BDD)
        if (monJoueur != null) {
            monJoueur.setPosX(x);
            monJoueur.setPosY(y);
        }
    }

    public void rendu(Graphics2D contexte, Camera camera) {
        contexte.drawImage(this.sprite, (int) (x - camera.getX()), (int) (y - camera.getY()), null);
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
    
    public double getLargeur() { return sprite.getHeight(); }
    public double getHauteur() { return sprite.getWidth(); }

    public Joueur getJoueur() { return monJoueur; }
}

