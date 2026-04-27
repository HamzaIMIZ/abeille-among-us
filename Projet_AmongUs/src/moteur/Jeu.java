/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package moteur;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.ImageIO;
/**
 *
 * @author rvillarr
 */
public class Jeu {
    
    private BufferedImage fond;
    private int score;
    public Avatar avatar; // not sure we need it if we add an atrraylist of 4 players
    private ArrayList<Joueur> listeJoueurs; 
    private Joueur joueurLocal;
    public Fleur fleur;
    private Camera camera;
    // Dimensions de la carte et de l'écran
    private final int LARGEUR_CARTE = 3904;
    private final int HAUTEUR_CARTE = 1968;
    private final int LARGEUR_ECRAN ;
    private final int HAUTEUR_ECRAN ;

    public Jeu(int largeurEcran, int hauteurEcran) {
        this.LARGEUR_ECRAN = largeurEcran;
        this.HAUTEUR_ECRAN = hauteurEcran;        
        try {
            this.fond = ImageIO.read(getClass().getClassLoader().getResource("resources/map.png"));
        } catch (IOException ex) {
            Logger.getLogger(Jeu.class.getName()).log(Level.SEVERE, null, ex);
        }
        this.listeJoueurs = new ArrayList<>();
        VJoueur j1 = new VJoueur(1, "Maya", "pass1");
        VJoueur j2 = new VJoueur(2, "Willy", "pass2");
        VJoueur j3 = new VJoueur(3, "Barry", "pass3");
        Imposter traître = new Imposter(4, "Frelon", "sabotage");

        listeJoueurs.add(j1);
        listeJoueurs.add(j2);
        listeJoueurs.add(j3);
        listeJoueurs.add(traître);

    // On définit quel joueur est contrôlé par ce clavier (ex: Maya)
        this.joueurLocal = j1;
    
        this.fleur = new Fleur(LARGEUR_CARTE, HAUTEUR_CARTE);
        
       this.avatar = new Avatar(LARGEUR_CARTE, HAUTEUR_CARTE);
        this.camera = new Camera(LARGEUR_ECRAN, HAUTEUR_ECRAN);
        this.score = 0;
    }
    /**
    * Cette méthode est importante, c'est cool si vous respectez d'ailleurs ce format avec une méthode MiseàJour() et une méthode rendu(). C'est MiseàJour() qui va mettre à jour le jeu coté moteur et déplacer votre joueur coté IG.
    */
    public void miseAJour() {
        this.avatar.miseAJour();
        this.fleur.miseAJour();
        // Centrer la caméra sur l'avatar
        this.camera.centrerSur(avatar.getX(), avatar.getY(), LARGEUR_CARTE, HAUTEUR_CARTE);
        joueurLocal.setPosX(avatar.getX());
        joueurLocal.setPosY(avatar.getY());
    
        for (Joueur j : listeJoueurs) {
        if (collisionEntreJoueurEtFleur(j)) {
            // APPEL POLYMORPHE : l'abeille ajoute, l'imposteur retire
            j.recolter(); 
            
            // Mise à jour du score de l'équipe
            this.score += j.getScoreSession();
            
            this.fleur.relancer();
        }
    }

    this.camera.centrerSur(avatar.getX(), avatar.getY(), LARGEUR_CARTE, HAUTEUR_CARTE);
    }

    /**
    * Maitnenant que vous avez changé du coté "console" votre jeu (avec MiseàJour()) bah... il faut l'afficher. C'est ce que fait rendu()
    */
    public void rendu(Graphics2D contexte) {
        // Remplir le fond en noir
        contexte.setColor(java.awt.Color.BLACK);
        contexte.fillRect(0, 0, LARGEUR_ECRAN, HAUTEUR_ECRAN);
        
        contexte.drawImage(this.fond, 
        (int) - camera.getX(),  // Décalage X
        (int) - camera.getY(),  // Décalage Y
            null);    
        //contexte.drawImage(this.fond, 0, 0, null);
        contexte.drawString("Score : " + score, 10, 20);
        this.fleur.rendu(contexte, camera);
        this.avatar.rendu(contexte, camera);
    }
    
    public boolean estTermine() {
        return this.fleur.getY() > HAUTEUR_CARTE;
    }
    
    public Avatar getAvatar() {
        return this.avatar;
    }

    public boolean collisionEntreJoueurEtFleur(Joueur j) {
    if ((fleur.getX() >= j.getPosX() + avatar.getLargeur()) 
        || (fleur.getX() + fleur.getLargeur() <= j.getPosX())
        || (fleur.getY() >= j.getPosY() + avatar.getHauteur())
        || (fleur.getY() + fleur.getHauteur() <= j.getPosY())) {
        return false;
    } 
    return true;
}
    
}

