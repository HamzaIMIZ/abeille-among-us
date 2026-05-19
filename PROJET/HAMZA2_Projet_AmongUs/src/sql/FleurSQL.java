/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package sql;

/**
 *
 * @author abriton
 */

import java.sql.*;

import java.util.List;
import java.util.ArrayList;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import moteur.Fleur;

public class FleurSQL {
    
    private String adresseBase;
    private String user;
    private String motdepasse;
    private Connection connexion; //lui c'est l'état de la connexion, autant en faire aussi un attribut.
    
    
    //Ici, on fait un constructeur qui va juste initialiser l'intermédiaire SQL
    public FleurSQL(){
        this.adresseBase = "jdbc:mariadb://nemrod.ens2m.fr:3306/2025-2026_s2_vs1_tp1_AbeilleAmongUs";
        this.user = "etudiant";
        this.motdepasse = "YTDTvj9TR3CDYCmP";
	
	//Vous avez vu que, avant de faire une requête, il fallait se connecter à la BD, ce que je te propose c'est de te connecter/déco UNE seule fois, et pas à 
	//chaque fois que tu fais une requête : La connection à la BD prend du TEMPS, si tu fais plusieurs co/déco, ça va être long :)
	try {
	
	this.connexion = DriverManager.getConnection(this.adresseBase, this.user, this.motdepasse);
	
	} catch (SQLException ex) {
            ex.printStackTrace();
        }

    }
    
   public void creerFleur(Fleur J) {

       try {
            // La table Fleur possède les colonnes :
            // id (auto-incrément), type, points, posX, posY,
            PreparedStatement requete = connexion.prepareStatement(
                "INSERT INTO Fleur (type, points, posX, posY) " + "VALUES (?, ?, ?, ?)", Statement.RETURN_GENERATED_KEYS);
            requete.setInt(1, J.getType());
            requete.setInt(2, J.getPoints());
            requete.setDouble(3, J.getX());
            requete.setDouble(4, J.getY());
            
            int nb = requete.executeUpdate();
            System.out.println(nb + " Participant(s) ajouté(s)");
            
            // Récupérer l'ID généré automatiquement
            ResultSet generatedKeys = requete.getGeneratedKeys();
            if (generatedKeys.next()) {
                J.setId(generatedKeys.getInt(1));
            }
            requete.close();
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }

     public void modifierFleur(Fleur J){
       
        try {
            PreparedStatement requete = connexion.prepareStatement(
                "UPDATE Fleur SET type = ?, points = ?, posX = ?, posY = ?" +
                "WHERE id = ?"
            );
            requete.setInt(1, J.getType());
            requete.setInt(2, J.getPoints());
            requete.setDouble(3, J.getX());
            requete.setDouble(4, J.getY());
            requete.setLong(5, J.getId());
            
            int nb = requete.executeUpdate();
            System.out.println(nb + " fleur(s) mis à jour");
            requete.close();
            
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }
     
     public void supprimerFleur(Fleur J){
       
         try {
            PreparedStatement requete = connexion.prepareStatement("DELETE FROM Fleur WHERE id = ?");
            requete.setLong(1, J.getId());
            int nb = requete.executeUpdate();
            System.out.println(nb + " fleur(s) supprimé(s)");
            requete.close();
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }
     
     public void voirFleur(Fleur J){
       //TODO (va utiliser SELECT dans sa requête SQL)
       //Un autre exemple car je suis gentille. Là je récupère toutes les infos du Participant J, de nom J.getNom()
        try {

            PreparedStatement requete = connexion.prepareStatement("SELECT * FROM Fleur WHERE id = ?");
            requete.setInt(1, J.getId());
            System.out.println(requete);
            ResultSet resultat = requete.executeQuery();
            OutilsJDBC.afficherResultSet(resultat);

            requete.close();

        } catch (SQLException ex) {
            ex.printStackTrace();
        }

    }   
     
     
    public void closeTable(){
       //On a lancé la connexion dans le Constructeur, il faut fermer donc la connexion quand tout est fini. Dans le jeu, il y a de fortes chance que tu le fasses quand tu supprimes tes Participants
	// à priori quand le jeu est terminé. 
        try {

            this.connexion.close();
            // add delete all flowers in the base de donnés here
        } catch (SQLException ex) {
            ex.printStackTrace();
        }

    }
    
 

    public List<Fleur> getFleurs(int monId) {
      List<Fleur> liste = new ArrayList<>();
    try {
        PreparedStatement stmt = connexion.prepareStatement(
            "SELECT id, nom, posX, posY, scoreSession FROM Joueur WHERE actif=1 AND id != ?");
        stmt.setInt(1, monId);
        ResultSet rs = stmt.executeQuery();
        while (rs.next()) {
            Fleur j = new Fleur();
            j.setId(rs.getInt("id"));
            j.setType(rs.getInt("type"));
            j.setX(rs.getDouble("posX"));
            j.setY(rs.getDouble("posY"));
            liste.add(j);
        }
        rs.close();
        stmt.close();
    } catch (SQLException e) {
        e.printStackTrace();
    }
    return liste;
    }


   //Si tu as une autre table, tu dois créer une autre classe similaire à celle-ci ! A présent, ton collègue qui travaille sur le moteur pourra
   //facilement utiliser tes méthodes pour mettre à jour la BDD ! En utilisant les méthodes que tu as crée pour lui :)
}