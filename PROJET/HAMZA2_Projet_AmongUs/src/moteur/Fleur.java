/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package moteur;

import ig.Carte;
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
    private double x, y;
    private int largeurCarte;
    private int hauteurCarte;
    
    private int id;
    private int type;
    private int points;
    
    
    public Fleur(int largeurCarte, int hauteurCarte, int type) {
        int fleur_size = 64;
        this.largeurCarte = largeurCarte;
        this.hauteurCarte = hauteurCarte; 
        this.type = type;
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
        
        if(this.type == 1){this.points = 100;}      //type == 1 means it is a normal fleur
        if(this.type == 2){this.points = -100;}     //type == 2 means it is a toxic fleur
        if(this.type == 3){this.points = 200;}      //type == 3 means it is a powerful fleur
        else{this.type = 1; this.points = 100;}
        
    }
    
        // Constructeur par défaut
    public Fleur() {
        this(0, 0, 0);
    }
    

    public void miseAJour() {
        
    }

    public void rendu(Graphics2D contexte, Camera camera) {
        contexte.drawImage(this.sprite, (int) (x - camera.getX()), (int) (y - camera.getY()), null);
    }
    
    public void relancer(Carte carte) {
        int attempts = 0;
        boolean positionTrouvee = false;

        while (!positionTrouvee && attempts < 100) {
            // Random position in the map
            double testX = Math.random() * carte.getLargeur() * carte.getTailleTuile();
            double testY = Math.random() * carte.getHauteur() * carte.getTailleTuile();

            if (estPositionValide(testX, testY, carte)) {// Check if this position is valid
                this.x = testX;
                this.y = testY;
                positionTrouvee = true;
            }
            attempts++;
        }

        // If no valid position found after 100 tries, place at default position
        if (!positionTrouvee) {
            this.x = 200;
            this.y = 200;
        }
    }
    
    
    private boolean estPositionValide(double posX, double posY, Carte carte) {
        int tailleTuile = carte.getTailleTuile();
        int tileX = (int) posX / tailleTuile;
        int tileY = (int) posY / tailleTuile;

        // Check bounds
        if (tileX < 0 || tileX >= carte.getLargeur() || tileY < 0 || tileY >= carte.getHauteur()) {
            return false;
        }

        int[][] rooms = carte.getRooms();
        int[][] background = carte.getBackground();
        int[][] veget = carte.getVeget();
        int[][] veg2 = carte.getVeg2();

        // Must NOT be on a wall (rooms must be -1)
        if (rooms != null && rooms[tileY][tileX] != -1) {
            return false; // There's a wall here
        }

        // Must be on ground (at least one of background, veget, or veg2 is not -1)
        boolean surTerrain = false;
        if (background != null && background[tileY][tileX] != -1) {
            surTerrain = true;
        }
        if (veget != null && veget[tileY][tileX] != -1) {
            surTerrain = true;
        }
        if (veg2 != null && veg2[tileY][tileX] != -1) {
            surTerrain = true;
        }
        return surTerrain;
    }

    
    public double getLargeur() {
        return sprite.getHeight();
    }

    public double getHauteur() {
        return sprite.getWidth();
    }

    public double getX() {
        return x;
    }

    public void setX(double x) {
        this.x = x;
    }

    public double getY() {
        return y;
    }

    public void setY(double y) {
        this.y = y;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public int getPoints() {
        return points;
    }

    public void setPoints(int points) {
        this.points = points;
    }
    
    
    
    

}

