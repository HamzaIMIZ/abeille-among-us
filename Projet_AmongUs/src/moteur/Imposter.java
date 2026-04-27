/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package moteur;

/**
 *
 * @author irachak
 */
public class Imposter extends Joueur {

    public Imposter(int id, String nom, String motDePasse) {
        super(id, nom, motDePasse);
        this.setImposteur(true);
    }

    @Override
    public void recolter() {
        // Le sabotage : l'imposteur retire des points au score global
        this.scoreSession -= 20; 
        System.out.println("Sabotage par un imposteur ! (-20)");
    }
}