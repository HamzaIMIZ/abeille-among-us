package moteur;

import ig.Carte;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.ImageIO;

public class Fleur {

    // Static: loaded ONCE for the entire program, shared by all Fleur instances
    private static BufferedImage spriteNormale;
    private static BufferedImage spriteToxique;
    private static boolean spritesCharges = false;

    private BufferedImage sprite;
    private double x, y;
    private int id;
    private int type;
    private int points;

    // Called once — loads original image with no resize
    private static void chargerSprites(Class<?> clazz) {
        if (spritesCharges) return;
        try {
            spriteNormale = ImageIO.read(clazz.getResource("../resources/fleur32.png"));

            // Toxic: draw original + red overlay into a copy
            int w = spriteNormale.getWidth();
            int h = spriteNormale.getHeight();
            spriteToxique = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
            Graphics2D g = spriteToxique.createGraphics();
            g.drawImage(spriteNormale, 0, 0, null);
            g.setColor(new java.awt.Color(255, 0, 0, 120));
            g.fillRect(0, 0, w, h);
            g.dispose();

            spritesCharges = true;
        } catch (IOException ex) {
            Logger.getLogger(Fleur.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    // Default constructor (used when loading from SQL)
    public Fleur() {
        chargerSprites(getClass());
        this.type = 1;
        this.points = 100;
        this.sprite = spriteNormale;
        this.x = 200;
        this.y = 200;
    }

    // Constructor used when creating new flowers
    public Fleur(int type) {
        chargerSprites(getClass());
        this.x = 200;
        this.y = 200;
        setType(type);
    }

    public void miseAJour() { }

    public void rendu(Graphics2D contexte, Camera camera) {
        if (this.sprite != null) {
            contexte.drawImage(this.sprite,
                (int)(x - camera.getX()),
                (int)(y - camera.getY()),
                null);
        }
    }

    public void relancer(Carte carte) {
        int attempts = 0;
        boolean positionTrouvee = false;
        while (!positionTrouvee && attempts < 200) {
            double testX = Math.random() * carte.getLargeur() * carte.getTailleTuile();
            double testY = Math.random() * carte.getHauteur() * carte.getTailleTuile();
            if (estPositionValideFleur(testX, testY, carte)) {
                this.x = testX;
                this.y = testY;
                positionTrouvee = true;
            }
            attempts++;
        }
        if (!positionTrouvee) { this.x = 200; this.y = 200; }
    }

    private boolean estPositionValideFleur(double posX, double posY, Carte carte) {
        int tailleTuile = carte.getTailleTuile();
        int tileX = (int) (posX / tailleTuile);
        int tileY = (int) (posY / tailleTuile);
        if (tileX < 0 || tileX >= carte.getLargeur() || tileY < 0 || tileY >= carte.getHauteur()) return false;

        int[][] rooms = carte.getRooms();
        int[][] background = carte.getBackground();
        int[][] veget = carte.getVeget();
        int[][] veg2 = carte.getVeg2();

        if (rooms != null && rooms[tileY][tileX] != -1) return false;
        if (background == null || background[tileY][tileX] == -1) return false;
        if (veget != null && veget[tileY][tileX] != -1) return false;
        if (veg2 != null && veg2[tileY][tileX] != -1) return false;
        return true;
    }

    public void setType(int type) {
        this.type = type;
        if (type == 2) {
            this.points = -5;
            this.sprite = spriteToxique;
        } else if (type == 3) {
            this.points = 10;
            this.sprite = spriteNormale;
        } else {
            this.type = 1;
            this.points = 5;
            this.sprite = spriteNormale;
        }
    }

    public double getLargeur() { return (sprite != null) ? sprite.getWidth() : 32; }
    public double getHauteur() { return (sprite != null) ? sprite.getHeight() : 32; }
    public double getX() { return x; }
    public void setX(double x) { this.x = x; }
    public double getY() { return y; }
    public void setY(double y) { this.y = y; }
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public int getType() { return type; }
    public int getPoints() { return points; }
    public void setPoints(int points) { this.points = points; }
}