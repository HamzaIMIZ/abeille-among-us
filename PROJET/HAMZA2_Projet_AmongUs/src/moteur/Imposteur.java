/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package moteur;

import sql.JoueurSQL;

/**
 *
 * @author irachak
 */
public class Imposteur extends Participant {
    public Imposteur(int id, String nom, String motDePasse) {
         super(id, nom, motDePasse);
         this.imposteur = true;
    }

    @Override
    public void calculpoint() {
        // Au lieu de calculerPoint, on utilise la méthode cueillirFleur
        // pour modifier directement le scoreSession de l'objet
        this.scoreSession -= 5; 
        JoueurSQL sql = new JoueurSQL();
        sql.volerPointsAuxAutres(this.id);
        sql.closeTable();
    }
}
