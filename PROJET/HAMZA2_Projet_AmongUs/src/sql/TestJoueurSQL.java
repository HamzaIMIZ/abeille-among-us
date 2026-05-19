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
import moteur.Participant;


public class TestJoueurSQL {

    public static void main(String[] args) {
        // Créer un Participant en mémoire
        Participant j = new Participant();
        //j.setScoreTotal(0);
        //j.setNbFleursTotal(0);
    

        j.setScoreSession(0);
        
        j.setPosX(170);
        j.setPosY(320);
        j.setImposteur(false);

        // Connexion à la base
        JoueurSQL sql = new JoueurSQL();

        // Insérer le Participant
        sql.creerParticipant(j);

        // Afficher l'ID généré
        System.out.println("Participant ajouté avec ID = " + j.getId());

        // Vérification : on recharge le Participant depuis la base
        Participant j2 = new Participant();
        j2.setId(j.getId());
        sql.voirParticipant(j2);
        
        System.out.println("\n=== Modification du Participant ===");
        j.setNom("TestModifModifie");
        j.setMotDePasse("nouveauMdp");


        j.setScoreSession(30);

        //j.setScoreTotal(150);
        //j.setNbFleursTotal(10);
        j.setScoreSession(30);
        //.setNbFleursSession(5);

        j.setPosX(500);
        j.setPosY(600);
        j.setImposteur(true);
        sql.modifierParticipant(j);
        System.out.println("Modification effectuée.");
        sql.voirParticipant(j);
        // Fermeture
        sql.closeTable();
    }
}