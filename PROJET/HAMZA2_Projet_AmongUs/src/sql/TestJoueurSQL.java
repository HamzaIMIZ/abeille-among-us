/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package sql;

/**
 *
 * @author himiz
 */
import moteur.Joueur;


public class TestJoueurSQL {

    public static void main(String[] args) {
        // Créer un joueur en mémoire
        Joueur j = new Joueur(0, "Alice", "secret");
        j.setScoreTotal(0);
        j.setNbFleursTotal(0);
        j.setScoreSession(0);
        j.setNbFleursSession(0);
        j.setPosX(170);
        j.setPosY(320);
        j.setImposteur(false);

        // Connexion à la base
        JoueurSQL sql = new JoueurSQL();

        // Insérer le joueur
        sql.creerJoueur(j);

        // Afficher l'ID généré
        System.out.println("Joueur ajouté avec ID = " + j.getId());

        // Vérification : on recharge le joueur depuis la base
        Joueur j2 = new Joueur();
        j2.setId(j.getId());
        sql.voirJoueur(j2);
        
        System.out.println("\n=== Modification du joueur ===");
        j.setNom("TestModifModifie");
        j.setMotDePasse("nouveauMdp");
        j.setScoreTotal(150);
        j.setNbFleursTotal(10);
        j.setScoreSession(30);
        j.setNbFleursSession(5);
        j.setPosX(500);
        j.setPosY(600);
        j.setImposteur(true);
        sql.modifierJoueur(j);
        System.out.println("Modification effectuée.");
        sql.voirJoueur(j);
        // Fermeture
        sql.closeTable();
    }
}