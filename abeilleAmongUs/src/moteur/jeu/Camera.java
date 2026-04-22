/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package jeu;

/**
 *
 * @author rvillarr
 */
public class Camera {
    private double x, y;  // Position de la caméra
    private int largeurEcran, hauteurEcran;
    
    public Camera(int largeurEcran, int hauteurEcran) {
        this.x = 0;
        this.y = 0;
        this.largeurEcran = largeurEcran;
        this.hauteurEcran = hauteurEcran;
    }
    
    public void centrerSur(double avatarX, double avatarY, int largeurCarte, int hauteurCarte) {
        // Position de la caméra = position de l'avatar - centre de l'écran
        this.x = avatarX - largeurEcran / 2;
        this.y = avatarY - hauteurEcran / 2;
        
        // Limiter la caméra aux bords de la carte
        if (this.x < 0) this.x = 0;
        if (this.y < 0) this.y = 0;
        if (this.x > largeurCarte - largeurEcran) this.x = largeurCarte - largeurEcran;
        if (this.y > hauteurCarte - hauteurEcran) this.y = hauteurCarte - hauteurEcran;
    }
    
    public double getX() {
        return x;
    }
    
    public double getY() {
        return y;
    }
}