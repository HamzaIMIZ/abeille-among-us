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
public class Fleur {

    protected BufferedImage sprite;
    protected double x, y;
    private int largeurCarte;
    private int hauteurCarte;
    
    public Fleur(int largeurCarte, int hauteurCarte) {
        int fleur_size = 64;
        this.largeurCarte = largeurCarte;
        this.hauteurCarte = hauteurCarte; 
        try {
            BufferedImage imageOriginale = ImageIO.read(getClass().getResource("../resources/fleur1.png"));
            // Créer une nouvelle image redimensionnée 
            this.sprite = new BufferedImage(fleur_size, fleur_size, BufferedImage.TYPE_INT_ARGB);
            Graphics2D g = this.sprite.createGraphics();
            g.drawImage(imageOriginale, 0, 0, fleur_size, fleur_size, null);
            g.dispose();
        } catch (IOException ex) {
            Logger.getLogger(Fleur.class.getName()).log(Level.SEVERE, null, ex);
        }
        this.x = 200;
        this.y = 200;
    }

    public void miseAJour() {
        
    }

    public void rendu(Graphics2D contexte, Camera camera) {
        contexte.drawImage(this.sprite, (int) (x - camera.getX()), (int) (y - camera.getY()), null);
    }
    
    public void relancer() {
        this.x = 15 + Math.random() * 330;
        this.y = 0;
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

