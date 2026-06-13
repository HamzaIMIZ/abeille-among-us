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
    public int calculpoint(int pointsFleur) {
        this.scoreSession += pointsFleur;
        if (this.scoreSession < 0) {
            this.scoreSession = 0;
        }
        return -pointsFleur;
    }
}
