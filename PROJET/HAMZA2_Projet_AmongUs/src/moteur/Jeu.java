/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
 
package moteur;

import ig.Carte;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.ImageIO;
import javax.swing.Timer;
import sql.JoueurSQL;
import sql.FleurSQL;
import javax.swing.JOptionPane; 

public class Jeu {

    private Carte carte;
    private Avatar avatar;  
    //private Fleur fleur;
    private Camera camera;
    private int score;
    private int scoreEquipe;
    //private int cyclesChrono = 0; // chrono + vote
    private boolean phaseVoteLancee = false; // chrono + vote
    private final int LARGEUR_CARTE = 3904;
    private final int HAUTEUR_CARTE = 1968;
    private final int LARGEUR_ECRAN;
    private final int HAUTEUR_ECRAN;

    private List<Participant> autresParticipants;   // liste des joueurs distants (données lues en BDD)
    private JoueurSQL JoueurSql;                // accès à la base de données
    //private Timer timerSync;              // timer pour la synchronisation périodique
    private int monParticipantId;             // identifiant du joueur local dans la BDD
    private BufferedImage spriteAutreParticipant; // image utilisée pour dessiner les autres joueurs
    
    private List<Fleur> fleurs = new ArrayList<>();
    private FleurSQL fleurSql;
    
    private Timer timerJoueurs;
    private Timer timerFleurs;

    public Jeu(int largeurEcran, int hauteurEcran, Participant monCompte) {
        this.LARGEUR_ECRAN = largeurEcran;
        this.HAUTEUR_ECRAN = hauteurEcran;
        this.monParticipantId = monCompte.getId();
        this.JoueurSql = new JoueurSQL();
        this.autresParticipants = new ArrayList<>();
        this.score = 0;
        
        String tmxFile = getClass().getResource("/resources/map.tmx").getPath();
        this.carte = new Carte(tmxFile);
        
        this.fleurSql = new FleurSQL();
        //this.fleur = new Fleur(LARGEUR_CARTE, HAUTEUR_CARTE, 1); // Création de la fleur
        this.fleurs = new ArrayList<>();
        
        // Chargement du sprite pour les autres joueurs (NOUVEAU)
        try {
            BufferedImage original = ImageIO.read(getClass().getResource("../resources/bee.png"));
            this.spriteAutreParticipant = new BufferedImage(25, 25, BufferedImage.TYPE_INT_ARGB);
            Graphics2D g = this.spriteAutreParticipant.createGraphics();
            g.drawImage(original, 0, 0, 25, 25, null);
            g.dispose();
        } catch (IOException ex) {
            Logger.getLogger(Jeu.class.getName()).log(Level.SEVERE, null, ex);
        }
       
        
        this.avatar = new Avatar(LARGEUR_CARTE, HAUTEUR_CARTE, monCompte); // Création de l'avatar
        // On positionne l'avatar aux coordonnées enregistrées en BDD (NOUVEAU)
        this.avatar.setX(monCompte.getPosX());
        this.avatar.setY(monCompte.getPosY());

        this.camera = new Camera(LARGEUR_ECRAN, HAUTEUR_ECRAN);
        initialiserFleurs();
        
        // NOUVEAU : Timer de synchronisation avec la BDD (toutes les 100 ms)
        this.timerJoueurs = new Timer(100, (e) -> {
            int sommeEquipe = 0;

            // joueur local
            Participant moi = this.avatar.getParticipant();
            if (moi != null) {
                int monScorePersoActuel = moi.getScoreSession();
                JoueurSql.mettreAJourPositionScore(monParticipantId, avatar.getX(), avatar.getY(), monScorePersoActuel);
                this.score = monScorePersoActuel;

                if (moi.isImposteur()) {
                    sommeEquipe -= monScorePersoActuel;
                } else {
                    sommeEquipe += monScorePersoActuel;
                }
            }

            // autres joueurs
            List<Participant> tous = JoueurSql.getAutresParticipant(monParticipantId);
            autresParticipants.clear();
            autresParticipants.addAll(tous);

            for (Participant autre : tous) {
                if (autre.isImposteur()) {
                    sommeEquipe -= autre.getScoreSession();
                } else {
                    sommeEquipe += autre.getScoreSession();
                }
            }

            this.scoreEquipe = sommeEquipe;
            
            fleurs.clear();
            fleurs.addAll(fleurSql.getToutesFleurs());
        // ... fin de la mise à jour des autres joueurs ...

    fleurs.clear();
    fleurs.addAll(fleurSql.getToutesFleurs());

    // NOUVEAU : On interroge le serveur SQL pour connaître le temps écoulé réel
    int tempsEcoule = JoueurSql.getTempsEcoule();
    
    // Si 60 secondes sont écoulées, on arrête tout
    if (tempsEcoule >= 60 && !phaseVoteLancee) {
        phaseVoteLancee = true;
        this.timerJoueurs.stop();
        this.timerFleurs.stop();
        lancerSondage();
    }
    });
        
        this.timerFleurs = new Timer(1200, (e) -> {
            fleurs.clear();
            fleurs.addAll(fleurSql.getToutesFleurs());
        });
        
        
        
        
        this.timerJoueurs.start();
        this.timerFleurs.start();
    }

    public void miseAJour() {
        this.carte.miseAJour();
        // Save old position before moving
        double oldX = avatar.getX();
        double oldY = avatar.getY();
        this.avatar.miseAJour();      // déplacement local
        
        // Check collision with walls
        if (collisionAvecMurs(avatar.getX(), avatar.getY())) { // Collision detected! Restore old position
            avatar.setX(oldX);
            avatar.setY(oldY);
            if (avatar.getParticipant() != null) { // Update Participant object too if it exists
                avatar.getParticipant().setPosX(oldX);
                avatar.getParticipant().setPosY(oldY);
            }
        }
        
        if (collisionEntreJoueurs(avatar.getX(), avatar.getY())) {
            // Collision détectée ! On force le retour à l'ancienne position
            avatar.setX(oldX);
            avatar.setY(oldY);
            if (avatar.getParticipant() != null) {
                avatar.getParticipant().setPosX(oldX);
                avatar.getParticipant().setPosY(oldY);
            }
            System.out.println("Mouvement interdit : Collision avec un autre joueur !");
        }
        
        
        for (int i = fleurs.size() - 1; i >= 0; i--) {
            Fleur f = fleurs.get(i);
            if (collisionEntreAvatarEtFleur(f)) {

                Participant p = avatar.getParticipant();
                if (p != null) {
                    int variationEquipe = p.calculpoint(f.getPoints());

                    // score local affiché = score personnel réel du joueur local
                    this.score = p.getScoreSession();

                    // score équipe mis à jour localement
                    this.scoreEquipe += variationEquipe;
                }

                fleurSql.supprimerFleur(f);
                fleurs.remove(i);

                int nouveauType;
                double hasard = Math.random();
                if (hasard < 0.8) {
                    nouveauType = 1;
                } else {
                    nouveauType = 2;
                }

                Fleur nouvelleFleur = new Fleur(nouveauType);
                nouvelleFleur.relancer(carte);
                fleurSql.creerFleur(nouvelleFleur);
                fleurs.add(nouvelleFleur);
            }
        }
        this.camera.centrerSur(avatar.getX(), avatar.getY(), LARGEUR_CARTE, HAUTEUR_CARTE);
        
        
        // Systeme chrono + vote
        
        //this.cyclesChrono++;
    
        // Si 1 minute est passée (1500 cycles de 40ms)
        //if (this.cyclesChrono >= 1500 && !phaseVoteLancee) {
          //  phaseVoteLancee = true;
            //this.timerJoueurs.stop(); // On arrête la synchronisation des mouvements et scores
            //this.timerFleurs.stop();
         //   boolean scoreValide = (this.scoreEquipe >= 50);

          //  if (!scoreValide) {
                // Le score est insuffisant, les abeilles ont la pression pour le vote
           //     javax.swing.JOptionPane.showMessageDialog(null, 
          //          "TEMPS ÉCOULÉ ! Le score de l'équipe est insuffisant (" + this.scoreEquipe + " < 50).\n" +
          //          "Le sabotage a réussi. Les abeilles doivent OBLIGATOIREMENT trouver l'imposteur pour se sauver !");
          //  } else {
                // Le score est bon, les abeilles sont en position de force
           //     javax.swing.JOptionPane.showMessageDialog(null, 
            //        "TEMPS ÉCOULÉ ! Bon travail, l'objectif de score est atteint (" + this.scoreEquipe + " >= 50).\n" +
             //       "Place au vote final pour confirmer votre victoire !");
           // }
            

            // Dans les deux cas, on va au vote, et on transmet si le score était bon ou pas
            //lancerSondage();
        //}
    }

    public void rendu(Graphics2D contexte) {
        // Fond noir et dessin de la map (inchangé)
        contexte.setColor(java.awt.Color.WHITE);
        contexte.fillRect(0, 0, LARGEUR_ECRAN, HAUTEUR_ECRAN);
        contexte.translate((int) -camera.getX(), (int) -camera.getY());
        this.carte.rendu(contexte);
        contexte.translate((int) camera.getX(), (int) camera.getY());

        // NOUVEAU : dessin des autres joueurs (leurs avatars)
        for (Participant autre : autresParticipants) {
            int screenX = (int) (autre.getPosX() - camera.getX());
            int screenY = (int) (autre.getPosY() - camera.getY());
            contexte.drawImage(this.spriteAutreParticipant, screenX, screenY, null);
            contexte.setColor(java.awt.Color.WHITE);
            contexte.drawString(autre.getNom(), screenX, screenY - 5);
        }

        // Dessin de l'avatar local
        this.avatar.rendu(contexte, camera);

        for (Fleur f : fleurs) {
            f.rendu(contexte, camera);
        }
        // Affichage du score (inchangé)
        contexte.setColor(java.awt.Color.BLACK);
        contexte.drawString("Score  : " + this.score, 10, 40);
        
        contexte.drawString("Score Équipe : " + this.scoreEquipe, 10, 20);
    }

    //  Collision flower with abeille
    private boolean collisionEntreAvatarEtFleur(Fleur f) {
        double ax = avatar.getX(), ay = avatar.getY(),
               aw = avatar.getLargeur(), ah = avatar.getHauteur();
        double fx = f.getX(), fy = f.getY(),
               fw = f.getLargeur(), fh = f.getHauteur();
        return !(fx >= ax + aw || fx + fw <= ax || fy >= ay + ah || fy + fh <= ay);
    }
    
    // Check collision with wqlls
    private boolean collisionAvecMurs(double newX, double newY) {
        int[][] rooms = carte.getRooms();
        if (rooms == null) return false;

        int tailleTuile = carte.getTailleTuile();
        int largeurSprite = (int) avatar.getLargeur();
        int hauteurSprite = (int) avatar.getHauteur();

        // Check the 4 corners of the avatar
        // Top-left corner
        int tileX1 = (int) newX / tailleTuile;
        int tileY1 = (int) newY / tailleTuile;

        // Top-right corner
        int tileX2 = (int) (newX + largeurSprite - 1) / tailleTuile;
        int tileY2 = (int) newY / tailleTuile;

        // Bottom-left corner
        int tileX3 = (int) newX / tailleTuile;
        int tileY3 = (int) (newY + hauteurSprite - 1) / tailleTuile;

        // Bottom-right corner
        int tileX4 = (int) (newX + largeurSprite - 1) / tailleTuile;
        int tileY4 = (int) (newY + hauteurSprite - 1) / tailleTuile;

        // Check if any corner is on a wall tile (value != -1)
        if (estSurMur(rooms, tileX1, tileY1)) return true;
        if (estSurMur(rooms, tileX2, tileY2)) return true;
        if (estSurMur(rooms, tileX3, tileY3)) return true;
        if (estSurMur(rooms, tileX4, tileY4)) return true;

        return false;
    }

    // check if a tile position is a wall
    private boolean estSurMur(int[][] rooms, int tileX, int tileY) {
        if (tileY < 0 || tileY >= rooms.length || tileX < 0 || tileX >= rooms[0].length) {
            return true; // Out of bounds = treat as wall
        }
        return rooms[tileY][tileX] != -1; // If value is not -1, it's a wall
    }

    public boolean estTermine() {
        return false;
    }

    public void arreter() {
        if (timerJoueurs != null && timerJoueurs.isRunning()) timerJoueurs.stop();
        if (timerFleurs != null && timerFleurs.isRunning()) timerFleurs.stop();
        JoueurSql.supprimerParticipant(monParticipantId);    
        JoueurSql.closeTable();
          
        fleurSql.closeTable();
    }

    // ---------- Getter pour l'avatar (utilisé par FenetreDeJeu pour les touches) ----------
    public Avatar getAvatar() {
        return this.avatar;
    }
    private boolean collisionEntreJoueurs(double newX, double newY) {
        // Taille de notre abeille locale
        double aw = avatar.getLargeur();
        double ah = avatar.getHauteur();
        
        // Taille fixe des autres joueurs (25x25 comme tu l'as défini dans ton constructeur de Jeu)
        double ow = 25;
        double oh = 25;

        // On parcourt tous les autres joueurs présents sur la carte
        for (Participant autre : autresParticipants) {
            double ox = autre.getPosX();
            double oy = autre.getPosY();

            // Formule par exclusion du polycopié : on vérifie si les rectangles ne se touchent PAS
            if (!(ox >= newX + aw      || // L'autre est trop à droite
                  ox + ow <= newX      || // L'autre est trop à gauche
                  oy >= newY + ah      || // L'autre est trop en bas
                  oy + oh <= newY))       // L'autre est trop en haut
            {
                // Si aucune exclusion n'est vraie, ils se touchent !
                return true; 
            }
        }
        return false; // Pas de collision
    }
    
    
   // vote
    private void lancerSondage() {
    // 1. Construction de la liste des pseudos
    ArrayList<String> choixJoueurs = new ArrayList<>();
    if (this.avatar.getParticipant() != null) {
        choixJoueurs.add(this.avatar.getParticipant().getNom());
    }
    for (Participant p : autresParticipants) {
        choixJoueurs.add(p.getNom());
    }
    
    // 2. Affichage de la boîte de dialogue de vote
    Object[] options = choixJoueurs.toArray();
    String vote = (String) JOptionPane.showInputDialog(
        null,
        "Selon vous, qui est l'imposteur ?",
        "Sondage de fin de partie",
        JOptionPane.QUESTION_MESSAGE,
        null,
        options,
        options[0]
    );
    
    if (vote != null) {
        boolean estLeVraiImposteur = JoueurSql.verifierSiImposteurParNom(vote);
        String vraiImposteur = JoueurSql.getNomVraiImposteur();
        
        // NOUVEAU : Récupérer le score de l'imposteur
        int scoreImposteur = JoueurSql.getScoreImposteur();
        
        // Comparaison : scoreEquipe vs scoreImposteur
        boolean equipeGagne = (this.scoreEquipe > scoreImposteur);
        
        if (estLeVraiImposteur) {
            // CAS 1 : Les abeilles ont trouvé l'imposteur
            if (equipeGagne) {
                JOptionPane.showMessageDialog(null,
                    "BIEN JOUE ! Vous avez demasque " + vote + " !\n" +
                    "Score Abeilles : " + this.scoreEquipe + "\n" +
                    "Score Imposteur : " + scoreImposteur + "\n" +
                    "VICTOIRE DES ABEILLES !");
            } else {
                JOptionPane.showMessageDialog(null,
                    "Vous avez trouve l'imposteur (" + vote + "), mais\n" +
                    "son score (" + scoreImposteur + ") est superieur au votre (" + 
                    this.scoreEquipe + ").\n" +
                    "L'IMPOSTEUR GAGNE !");
            }
        } else {
            // CAS 2 : Les abeilles se sont trompees
            if (equipeGagne) {
                JOptionPane.showMessageDialog(null,
                    "Dommage ! Vous avez accuse " + vote + " (innocent).\n" +
                    "Mais votre score (" + this.scoreEquipe + ") bat l'imposteur (" + 
                    scoreImposteur + ").\n" +
                    "VICTOIRE DES ABEILLES QUAND MEME !");
            } else {
                JOptionPane.showMessageDialog(null,
                    "DEFAITE TOTALE !\n" +
                    "Mauvaise cible : " + vote + " (innocent)\n" +
                    "Score Abeilles : " + this.scoreEquipe + "\n" +
                    "Score Imposteur : " + scoreImposteur + "\n" +
                    "L'IMPOSTEUR GAGNE !");
            }
        }
        
        JoueurSql.supprimerParticipant(this.monParticipantId);
        this.arreter();
        System.exit(0);
    }
}
    
    private void initialiserFleurs() {
            // If no players in DB yet (fresh session), clean up leftover flowers
            int nbJoueurs = JoueurSql.compterJoueursActifs();
            if (nbJoueurs <= 1) {
                // We are the first (or only) player — reset flowers
                fleurSql.supprimerToutesLesFleurs();
            }

            int nb = fleurSql.compterFleurs();
            System.out.println("Fleurs dans la BDD au démarrage : " + nb);

            if (nb < 20) {
                int aCreer = 20 - nb;
                System.out.println("Création de " + aCreer + " fleurs...");
                for (int i = 0; i < (int)(aCreer * 0.7); i++) {
                    Fleur f = new Fleur(1);
                    f.relancer(carte);
                    fleurSql.creerFleur(f);
                    fleurs.add(f);
                }
                int toxiquesACreer = aCreer - (int)(aCreer * 0.7);
                for (int i = 0; i < toxiquesACreer; i++) {
                    Fleur f = new Fleur(2);
                    f.relancer(carte);
                    fleurSql.creerFleur(f);
                    fleurs.add(f);
                }
            }


            fleurs.clear();
            fleurs.addAll(fleurSql.getToutesFleurs());
            System.out.println("Fleurs chargées en mémoire : " + fleurs.size()); // 
    }
    
}