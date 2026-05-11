/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package moteur;

/**
 *
 * @author irachak
 */
public class Imposteur extends Participant {
    public Imposteur(int id, String nom, String motDePasse) {
         super(id, nom, motDePasse);
         this.imposteur = true;
    }


    public void calculpoint() {
        // Au lieu de calculerPoint, on utilise la méthode cueillirFleur
        // pour modifier directement le scoreSession de l'objet
        this.scoreSession -= 10; 
    }
}
