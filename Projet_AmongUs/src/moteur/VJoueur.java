/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package moteur;

/**
 *
 * @author irachak
 */
public class VJoueur extends Joueur{
    public VJoueur(int id, String nom, String motDePasse) {
        super(id, nom, motDePasse); 
        this.setImposteur(false);
    }
    
    public VJoueur() {
        this(0, "", "");
    }

    @Override
    public void recolter() {
        // L'abeille normale ajoute des points au score
        this.scoreSession += 10; 
        System.out.println(this.nom + " a récolté du nectar ! (+10)");
    }
    
}
