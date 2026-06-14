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
import java.sql.PreparedStatement;
import java.sql.SQLException;
import moteur.Fleur;

public class FleurSQL {
    
    private String adresseBase;
    private String user;
    private String motdepasse;
    private Connection connexion; //lui c'est l'état de la connexion, autant en faire aussi un attribut.
    
    
    //Ici, on fait un constructeur qui va juste initialiser l'intermédiaire SQL


    public FleurSQL() {
        this.connexion = SingletonJDBC.getInstance().getConnection();
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
           
            requete.executeUpdate();
           
            // Récupérer l'ID généré automatiquement
            ResultSet generatedKeys = requete.getGeneratedKeys();
            if (generatedKeys.next()) {
                J.setId(generatedKeys.getInt(1));
            }
            generatedKeys.close();
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
            requete.setInt(5, J.getId());
            
            requete.executeUpdate();
            requete.close();
            
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }
     
     public void supprimerFleur(Fleur J){
       
         try {
            PreparedStatement requete = connexion.prepareStatement("DELETE FROM Fleur WHERE id = ?");
            requete.setLong(1, J.getId());
            requete.executeUpdate();
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
     
 

    public List<Fleur> getToutesFleurs() {
      List<Fleur> liste = new ArrayList<>();
        try {
            PreparedStatement stmt = connexion.prepareStatement(
                "SELECT id, type, points, posX, posY FROM Fleur");
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                Fleur j = new Fleur();
                j.setId(rs.getInt("id"));
                j.setType(rs.getInt("type"));
                j.setPoints(rs.getInt("points"));
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
    
    public int compterFleurs() {
        try {
            PreparedStatement req = connexion.prepareStatement(
                "SELECT COUNT(*) FROM Fleur");
            ResultSet rs = req.executeQuery();
            if (rs.next()) return rs.getInt(1);
            req.close();
        } catch (SQLException ex) { ex.printStackTrace(); }
        return 0;
    }

    // Delete ALL flowers (called on arreter() to clean up)
    public void supprimerToutesLesFleurs() {
        try {
            PreparedStatement req = connexion.prepareStatement("DELETE FROM Fleur");
            req.executeUpdate();
            req.close();
        } catch (SQLException ex) { ex.printStackTrace(); }
    }

    public void closeTable() {
        // Connexion partagée (singleton) : on ne la ferme pas ici.
}
    }