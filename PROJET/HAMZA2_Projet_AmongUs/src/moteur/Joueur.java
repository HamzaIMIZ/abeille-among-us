/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package moteur;

/**
 *
 * @author irachak
 */
public class Joueur extends Participant {
    public Joueur(int id, String nom, String motDePasse) {
        super(id, nom, motDePasse); // Appel au constructeur de la classe de base
    }
    
    
    @Override
    public int calculpoint(int pointsFleur) {
        this.scoreSession += pointsFleur;
        if (this.scoreSession < 0) {
            this.scoreSession = 0;
        }
        return pointsFleur;
    }
}