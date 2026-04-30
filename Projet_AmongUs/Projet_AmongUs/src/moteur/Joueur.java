/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package moteur;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.ImageIO;
/**
 * @author himiz
 * Représente un joueur dans le jeu multijoueur.
 */

public class Joueur {
    private int id;
    private String nom;
    private String motDePasse;
    private int scoreTotal;      // score cumulé sur toutes les parties
    private int nbFleursTotal;   // nombre total de fleurs cueillies
    private int scoreSession;    // score de la partie en cours
    private int nbFleursSession; // fleurs cueillies dans la partie en cours
    private double posX;
    private double posY;
    private boolean imposteur;

    // Constructeur principal
    public Joueur(int id, String nom, String motDePasse) {
        this.id = id;
        this.nom = nom;
        this.motDePasse = motDePasse;
        this.scoreTotal = 0;
        this.nbFleursTotal = 0;
        this.scoreSession = 0;
        this.nbFleursSession = 0;
        this.posX = 170;   // position initiale (comme dans Avatar)
        this.posY = 320;
        this.imposteur = false;
    }

    // Constructeur par défaut
    public Joueur() {
        this(0, "", "");
    }

    // --- Getters et Setters ---
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getNom() { return nom; }
    public void setNom(String nom) { this.nom = nom; }

    public String getMotDePasse() { return motDePasse; }
    public void setMotDePasse(String motDePasse) { this.motDePasse = motDePasse; }

    public int getScoreTotal() { return scoreTotal; }
    public void setScoreTotal(int scoreTotal) { this.scoreTotal = scoreTotal; }

    public int getNbFleursTotal() { return nbFleursTotal; }
    public void setNbFleursTotal(int nbFleursTotal) { this.nbFleursTotal = nbFleursTotal; }

    public int getScoreSession() { return scoreSession; }
    public void setScoreSession(int scoreSession) { this.scoreSession = scoreSession; }

    public int getNbFleursSession() { return nbFleursSession; }
    public void setNbFleursSession(int nbFleursSession) { this.nbFleursSession = nbFleursSession; }

    public double getPosX() { return posX; }
    public void setPosX(double posX) { this.posX = posX; }

    public double getPosY() { return posY; }
    public void setPosY(double posY) { this.posY = posY; }

    public boolean isImposteur() { return imposteur; }
    public void setImposteur(boolean imposteur) { this.imposteur = imposteur; }

    // --- Méthodes utiles ---
    public void cueillirFleur(int points) {
        this.scoreSession += points;
        this.nbFleursSession++;
    }

    public void resetPourNouvellePartie() {
        this.scoreSession = 0;
        this.nbFleursSession = 0;
        this.posX = 170;
        this.posY = 320;
        this.imposteur = false;
    }

    @Override
    public String toString() {
        return "Joueur{" +
                "id=" + id +
                ", nom='" + nom + '\'' +
                ", scoreTotal=" + scoreTotal +
                ", scoreSession=" + scoreSession +
                ", posX=" + posX +
                ", posY=" + posY +
                '}';
    }
}