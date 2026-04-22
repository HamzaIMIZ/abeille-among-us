/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jeu;

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
    

    public Avatar(int largeurCarte, int hauteurCarte) {
        int avatar_size = 25;
        this.largeurCarte = largeurCarte;
        this.hauteurCarte = hauteurCarte;    
        try {
            BufferedImage imageOriginale = ImageIO.read(getClass().getResource("../resources/bee.png"));
            // Créer une nouvelle image redimensionnée à 64x64
            this.sprite = new BufferedImage(avatar_size, avatar_size, BufferedImage.TYPE_INT_ARGB);
            Graphics2D g = this.sprite.createGraphics();
            g.drawImage(imageOriginale, 0, 0, avatar_size, avatar_size, null);
            g.dispose();
           
        } catch (IOException ex) {
            Logger.getLogger(Avatar.class.getName()).log(Level.SEVERE, null, ex);
        }
        this.x = 170;
        this.y = 320;
        this.toucheGauche = false;
        this.toucheDroite = false;
        this.toucheUp = false;
        this.toucheDown = false;
    }

    public void miseAJour() {
        if (this.toucheGauche) {x -= 10;}
        if (this.toucheDroite) {x += 10;}
        if (this.toucheUp) {y -= 10;}
        if (this.toucheDown) {y += 10;}
        if (x > largeurCarte - sprite.getWidth()) { // collision avec le bord droit de la scene
            x = largeurCarte - sprite.getWidth();
        }
        if (x < 0) { // collision avec le bord gauche de la scene
            x = 0;
        }
        
        if (y > hauteurCarte - sprite.getHeight()) { // collision avec le bord droit de la scene
            y = hauteurCarte - sprite.getHeight();
        }
        if (y < 0) { // collision avec le bord de la scene
            y = 0;
        }
    }

    public void rendu(Graphics2D contexte, Camera camera) {
        contexte.drawImage(this.sprite, (int) (x - camera.getX()), (int) (y - camera.getY()), null);
    }
    
    public void setToucheGauche(boolean etat) {
        this.toucheGauche = etat;
    }
    
    public void setToucheDroite(boolean etat) {
        this.toucheDroite = etat;
    }
    
    public void setToucheHaut(boolean b) {
        this.toucheUp = b; 
    }
    
    public void setToucheBas(boolean b) {
        this.toucheDown = b; 
    }

    public double getX() {
        return x;
    }

    public double getY() {
        return y;
    }
    
    public double getLargeur() {
        return sprite.getHeight();
    }

    public double getHauteur() {
        return sprite.getWidth();
    }


    
    
}

