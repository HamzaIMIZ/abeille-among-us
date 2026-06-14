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
import moteur.Joueur;

import java.util.List;
import java.util.ArrayList;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import moteur.Participant;

public class JoueurSQL {
    
    private Connection connexion; //lui c'est l'état de la connexion, autant en faire aussi un attribut.
    
    

    public JoueurSQL() {
        this.connexion = SingletonJDBC.getInstance().getConnection();
    }
    
    //Je t'ai mis ici les 4 méthodes qui vont être importantes à coder, à toi de fustionner ça avec les bouts de code dans tes tests : 
   public void creerParticipant(Participant J) {
        
       
       
       try {
            // La table Participant possède les colonnes :
            // id (auto-incrément), nom, motDePasse, scoreTotal, nbFleursTotal,
            // scoreSession, nbFleursSession, posX, posY, imposteur
            PreparedStatement requete = connexion.prepareStatement(
                "INSERT INTO Joueur (nom, motDePasse, scoreSession, posX, posY, imposteur) " +
                "VALUES (?, ?, ?, ?, ?, ?)",
                Statement.RETURN_GENERATED_KEYS
            );
            requete.setString(1, J.getNom());
            requete.setString(2, J.getMotDePasse());
            requete.setInt(3, J.getScoreSession());
            requete.setDouble(4, J.getPosX());
            requete.setDouble(5, J.getPosY());
            requete.setBoolean(6, J.isImposteur());
            
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

     public void modifierParticipant(Participant J){
       
        try {
            PreparedStatement requete = connexion.prepareStatement(

                "UPDATE Joueur SET nom = ?, motDePasse = ?, " +
                "scoreSession = ?, posX = ?, posY = ?, imposteur = ? " +
                "WHERE id = ?"
            );
            requete.setString(1, J.getNom());
            requete.setString(2, J.getMotDePasse());
            requete.setInt(3, J.getScoreSession());
            requete.setDouble(4, J.getPosX());
            requete.setDouble(5, J.getPosY());
            requete.setBoolean(6, J.isImposteur());

            requete.setInt(7, J.getId());

            
            int nb = requete.executeUpdate();
            System.out.println(nb + " Participant(s) mis à jour");
            requete.close();
            
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }
     
      public void supprimerParticipant(int id) {

        try {
            PreparedStatement requete = connexion.prepareStatement("DELETE FROM Joueur WHERE id = ?");
            requete.setInt(1, id);
            int nb = requete.executeUpdate();
            System.out.println(nb + " Participant(s) supprimé(s)");
            requete.close();
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
     }

     
     public void voirParticipant(Participant J){
       //TODO (va utiliser SELECT dans sa requête SQL)
       //Un autre exemple car je suis gentille. Là je récupère toutes les infos du Participant J, de nom J.getNom()
        try {

            PreparedStatement requete = connexion.prepareStatement("SELECT * FROM Joueur WHERE id = ?");
            requete.setInt(1, J.getId());
            System.out.println(requete);
            ResultSet resultat = requete.executeQuery();
            OutilsJDBC.afficherResultSet(resultat);

            requete.close();

        } catch (SQLException ex) {
            ex.printStackTrace();
        }

    }
    
    public void mettreAJourPositionScore(int id, double x, double y, int scoreSession) {
        try (PreparedStatement stmt = connexion.prepareStatement(
            "UPDATE Joueur SET posX = ?, posY = ?, scoreSession = ? WHERE id = ?")) {
          stmt.setDouble(1, x);
          stmt.setDouble(2, y);
          stmt.setInt(3, scoreSession);
          stmt.setInt(4, id);
          stmt.executeUpdate();
        } catch (SQLException e) {
          e.printStackTrace();
        }
    } 
     
     
     
  public void closeTable() {
        // Connexion partagée (singleton) : on ne la ferme pas ici.
    }
    
 

    public List<Participant> getAutresParticipant(int monId) {
      List<Participant> liste = new ArrayList<>();
    try {
        PreparedStatement stmt = connexion.prepareStatement(
            "SELECT id, nom, posX, posY, scoreSession, imposteur FROM Joueur WHERE id != ?");
        stmt.setInt(1, monId);
        ResultSet rs = stmt.executeQuery();
        while (rs.next()) {
            Participant j = new Participant();
            j.setId(rs.getInt("id"));
            j.setNom(rs.getString("nom"));
            j.setPosX(rs.getDouble("posX"));
            j.setPosY(rs.getDouble("posY"));
            j.setScoreSession(rs.getInt("scoreSession"));
            j.setImposteur(rs.getBoolean("imposteur"));
            liste.add(j);
        }
        rs.close();
        stmt.close();
    } catch (SQLException e) {
        e.printStackTrace();
    }
    return liste;
    }

    public boolean existeDejaUnImposteur() {
        boolean resultat = false;
        try {
            // On compte les participants dont la colonne 'imposteur' est à 1 et qui sont 'actifs' [cite: 259]
            PreparedStatement stmt = connexion.prepareStatement(
                "SELECT COUNT(*) FROM Joueur WHERE imposteur = 1 ");
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                // Si le compte est supérieur à 0, un imposteur est déjà en jeu
                resultat = rs.getInt(1) > 0;
            }
            rs.close();
            stmt.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return resultat;
    }

    public int getMonScore(int monId) {
        int scoreTrouve = 0;
        try {
            PreparedStatement stmt = connexion.prepareStatement(
                "SELECT scoreSession FROM Joueur WHERE id = ?"
            );
            stmt.setInt(1, monId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                scoreTrouve = rs.getInt("scoreSession");
            }
            rs.close();
            stmt.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return scoreTrouve;
    }

    // Vérifie si le joueur désigné par le vote est un imposteur
    public boolean verifierSiImposteurParNom(String nomVote) {
        boolean estImposteur = false;
        try {
            PreparedStatement stmt = connexion.prepareStatement(
                "SELECT imposteur FROM Joueur WHERE nom = ?"
            );
            stmt.setString(1, nomVote);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                estImposteur = rs.getBoolean("imposteur");
            }
            rs.close();
            stmt.close();
        } catch (SQLException e) { e.printStackTrace(); }
        return estImposteur;
    }

    // Récupère le nom du coupable pour l'afficher à la fin en cas d'erreur
    public String getNomVraiImposteur() {
        String nom = "Inconnu";
        try {
            PreparedStatement stmt = connexion.prepareStatement(
                "SELECT nom FROM Joueur WHERE imposteur = 1 LIMIT 1"
            );
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                nom = rs.getString("nom");
            }
            rs.close();
            stmt.close();
        } catch (SQLException e) { e.printStackTrace(); }
        return nom;
    }
   //Si tu as une autre table, tu dois créer une autre classe similaire à celle-ci ! A présent, ton collègue qui travaille sur le moteur pourra
   //facilement utiliser tes méthodes pour mettre à jour la BDD ! En utilisant les méthodes que tu as crée pour lui :)
    public int compterJoueursActifs() {
        try {
            PreparedStatement stmt = connexion.prepareStatement(
                "SELECT COUNT(*) FROM Joueur"
            );
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) return rs.getInt(1);
            rs.close();
            stmt.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }
//    returns a list of active players, displayed  in lobby
    public List<Participant> getJoueursActifs() {
        List<Participant> liste = new ArrayList<>();
        try {
            PreparedStatement stmt = connexion.prepareStatement(
                    "SELECT id, nom, posX, posY, scoreSession, imposteur FROM Joueur");
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                Participant j = new Participant();
                j.setId(rs.getInt("id"));
                j.setNom(rs.getString("nom"));
                j.setPosX(rs.getDouble("posX"));
                j.setPosY(rs.getDouble("posY"));
                j.setScoreSession(rs.getInt("scoreSession"));
                j.setImposteur(rs.getBoolean("imposteur"));
                liste.add(j);
            }
            rs.close();
            stmt.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return liste;
    }  
}