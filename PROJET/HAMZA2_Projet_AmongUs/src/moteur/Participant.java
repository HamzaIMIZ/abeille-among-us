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

public class Participant {
    protected int id;
    protected String nom;
    protected String motDePasse;
    protected int scoreSession;    // score de la partie en cours
    protected double posX;
    protected double posY;
    protected boolean imposteur;

    // Constructeur principal
    public Participant(int id, String nom, String motDePasse) {
        this.id = id;
        this.nom = nom;
        this.motDePasse = motDePasse;
        this.scoreSession = 0;
        this.posX = 170;   // position initiale (comme dans Avatar)
        this.posY = 320;
        this.imposteur = false;
    }

    // Constructeur par défaut
    public Participant() {
        this(0, "", "");
    }
     public Participant(String nom) {
        this.nom = nom;
    }
     

    // --- Getters et Setters ---
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getNom() { return nom; }
    public void setNom(String nom) { this.nom = nom; }

    public String getMotDePasse() { return motDePasse; }
    public void setMotDePasse(String motDePasse) { this.motDePasse = motDePasse; }


    public int getScoreSession() { return scoreSession; }
    public void setScoreSession(int scoreSession) { this.scoreSession = scoreSession; }

    
    public double getPosX() { return posX; }
    public void setPosX(double posX) { this.posX = posX; }

    public double getPosY() { return posY; }
    public void setPosY(double posY) { this.posY = posY; }

    public boolean isImposteur() { return imposteur; }
    public void setImposteur(boolean imposteur) { this.imposteur = imposteur; }
    
    public void calculpoint() {
        
    }
  
    public void resetPourNouvellePartie() {
        this.scoreSession = 0;
        this.posX = 170;
        this.posY = 320;
        this.imposteur = false;
    }
    

    @Override
    public String toString() {
        return "Joueur{" +
                "id=" + id +
                ", nom='" + nom + '\'' +
                ", scoreSession=" + scoreSession +
                ", posX=" + posX +
                ", posY=" + posY +
                '}';
    }
}