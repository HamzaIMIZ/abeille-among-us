/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package ig;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.ImageIO;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

/**
 * Classe Carte — chargée depuis un fichier TMX (Sprite Fusion)
 */
public class Carte {

    private int largeur;
    private int hauteur;
    private int tailleTuile = 16; 

    private BufferedImage[] tuiles;

    private int[][] background;
    private int[][] rooms;
    private int[][] veget;
    private int[][] veg2;

    public Carte(String cheminTMX) {
        // charger tileset (int his cqse, spritesheet.png avec 8 colonnes, 2560 tuiles)
        try {
            BufferedImage tileset = ImageIO.read(getClass().getResource("/resources/spritesheet.png"));
            tuiles = new BufferedImage[2560];
            for (int i = 0; i < tuiles.length; i++) {
                int x = (i % 8) * tailleTuile;  
                int y = (i / 8) * tailleTuile;
                tuiles[i] = tileset.getSubimage(x, y, tailleTuile, tailleTuile);
            }
        } catch (IOException ex) {
            Logger.getLogger(Carte.class.getName()).log(Level.SEVERE, null, ex);
        }

        try {
            DocumentBuilder db = DocumentBuilderFactory.newInstance().newDocumentBuilder();
            Document doc = db.parse(new File(cheminTMX));

            Element map = (Element) doc.getElementsByTagName("map").item(0);
            this.largeur = Integer.parseInt(map.getAttribute("width"));   
            this.hauteur = Integer.parseInt(map.getAttribute("height"));  

            // Lire chaque calque <layer> par son name
            NodeList layers = doc.getElementsByTagName("layer");
            for (int i = 0; i < layers.getLength(); i++) {
                Element layer = (Element) layers.item(i);
                String nom = layer.getAttribute("name");
                int[][] tableau = lireCalque(layer, largeur, hauteur);

                switch (nom) {
                    case "background": this.background = tableau; break;
                    case "Rooms":      this.rooms      = tableau; break;
                    case "veget":      this.veget      = tableau; break;
                    case "veg2":       this.veg2       = tableau; break;
                }
            }

        } catch (Exception ex) {
            Logger.getLogger(Carte.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

//   lit le CSV, retourne int[][]
    private int[][] lireCalque(Element layer, int largeur, int hauteur) {
        String csv = layer.getElementsByTagName("data").item(0).getTextContent().trim();
        String[] valeurs = csv.split(",");

        int[][] tableau = new int[hauteur][largeur];
        for (int y = 0; y < hauteur; y++) {
            for (int x = 0; x < largeur; x++) {
                int id = Integer.parseInt(valeurs[y * largeur + x].trim());
                // Dans TMX : 0 = vide, sinon l'id commence à 1 (firstgid=1)
                // On soustrait 1 
                if (id == 0) {
                    tableau[y][x] = -1;
                } else {
                    tableau[y][x] = id - 1;
                }
            }
        }
        return tableau;
    }

    public void miseAJour() {
    }

    public void rendu(Graphics2D contexte) {
        dessinerCalque(contexte, background);
        dessinerCalque(contexte, rooms);
        dessinerCalque(contexte, veget);
        dessinerCalque(contexte, veg2);
    }

    //dessine un calque entier
    private void dessinerCalque(Graphics2D contexte, int[][] calque) {
        if (calque == null) return;
        for (int y = 0; y < hauteur; y++) {
            for (int x = 0; x < largeur; x++) {
                int index = calque[y][x];
                if (index == -1) continue; // case vide, on ne dessine rien
                contexte.drawImage(tuiles[index], x * tailleTuile, y * tailleTuile, null);
            }
        }
    }
    
    
    public int[][] getRooms() {
        return this.rooms;
    }

    public int getTailleTuile() {
        return this.tailleTuile;
    }
    public int getLargeurPixels() { return largeur * tailleTuile; }
    public int getHauteurPixels() { return hauteur * tailleTuile; }
}