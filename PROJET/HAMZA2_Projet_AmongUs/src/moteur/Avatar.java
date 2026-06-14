/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package moteur;

import ig.Carte;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.ImageIO;

/**
 * Classe Avatar personnalisable avec gestion de spawn intelligent sur grille.
 * Calquée strictement sur la logique d'analyse de terrain de Fleur.java.
 *
 * @author guillaume.laurent
 */
public class Avatar {

    protected BufferedImage sprite;
    protected double x, y;
    private boolean toucheGauche, toucheDroite, toucheUp, toucheDown;
    private int largeurCarte;
    private int hauteurCarte;
    private Participant monParticipant; // Référence vers l'objet Participant (pour la synchro BDD)
    private boolean regardeADroite = false;

    // Type de l'avatar sélectionné
    private String typeAvatar;

    // Constructeur à 3 paramètres par défaut
    public Avatar(int largeurCarte, int hauteurCarte, Participant j) {
        this(largeurCarte, hauteurCarte, j, "bee", new ArrayList<>(), null);
    }

    // Constructeur à 6 paramètres prenant en compte la carte pour le calcul du spawn
    public Avatar(int largeurCarte, int hauteurCarte, Participant j, String typeAvatar, List<Participant> autresJoueurs, Carte carte) {
        this.largeurCarte = largeurCarte;
        this.hauteurCarte = hauteurCarte;
        this.monParticipant = j;
        this.typeAvatar = typeAvatar.toLowerCase();

        // 1. Chargement dynamique du fichier image (nécessaire pour connaître la taille du sprite)
        chargerImage();

        // 2. Logique de Spawn Aléatoire basée sur la détection stricte du terrain
        configurerSpawnAleatoire(autresJoueurs, carte);

        // 3. Mise à jour immédiate de l'objet Participant pour écraser l'ancienne position sur Nemrod
        if (this.monParticipant != null) {
            this.monParticipant.setPosX(this.x);
            this.monParticipant.setPosY(this.y);
        }

        this.toucheGauche = false;
        this.toucheDroite = false;
        this.toucheUp = false;
        this.toucheDown = false;
    }

    /**
     * Calcule une position de tuile aléatoire sur la grille, applique les
     * règles de validation de ta méthode estPositionValideFleur, puis convertit
     * en pixels réels.
     */
    private void configurerSpawnAleatoire(List<Participant> autresJoueurs, Carte carte) {
        if (carte == null) {
            System.err.println("Erreur : Impossible d'analyser les collisions, carte non fournie à l'avatar.");
            this.x = 170;
            this.y = 320;
            return;
        }

        boolean positionValide = false;
        int tentatives = 0;

        // Récupération des dimensions de la grille de tuiles depuis l'objet carte
        // Ton TMX indique 244 de large et 123 de haut
        int maxTileX = carte.getLargeur();
        int maxTileY = carte.getHauteur();
        int tailleTuile = carte.getTailleTuile(); // 16 pixels

        // On cherche une tuile libre sur la grille (limité à 1000 essais)
        while (!positionValide && tentatives < 1000) {
            tentatives++;

            // 1. On choisit un index de case (tuile) au hasard sur la grille
            int randomTileX = (int) (Math.random() * maxTileX);
            int randomTileY = (int) (Math.random() * maxTileY);

            // 2. Convertir temporairement cet index de tuile en pixels (X et Y du coin haut-gauche)
            double tentativeX = randomTileX * tailleTuile;
            double tentativeY = randomTileY * tailleTuile;

            // 3. On applique la vérification stricte calquée sur ton code
            // Comme le sprite fait 50x50, on s'assure que le centre ou la tuile de base est parfaitement exploitable
            if (!estPositionValideFleur(tentativeX + 25, tentativeY + 25, carte)) {
                continue; // La case ne respecte pas ton filtre, on passe à l'essai suivant
            }

            // 4. Vérifier s'il n'y a pas un autre joueur trop proche sur Nemrod
            positionValide = true;
            if (autresJoueurs != null) {
                for (Participant autre : autresJoueurs) {
                    double dx = tentativeX - autre.getPosX();
                    double dy = tentativeY - autre.getPosY();
                    double distance = Math.sqrt(dx * dx + dy * dy);

                    if (distance < distanceSecurite) {
                        positionValide = false;
                        break;
                    }
                }
            }

            // Si la tuile passe tous les tests, on valide les coordonnées
            if (positionValide) {
                this.x = tentativeX;
                this.y = tentativeY;
                break; // Triomphe, on quitte la recherche !
            }
        }

        // Si échec après 1000 essais (ex: map saturée), plan de secours
        if (!positionValide) {
            System.err.println("Alerte Spawn : Aucune case valide trouvée après " + tentatives + " essais. Repli.");
            this.x = 170;
            this.y = 320;
        } else {
            System.out.println("Spawn intelligent réussi en " + tentatives + " essais. Coordonnées pixels : (" + x + " ; " + y + ")");
        }
    }

    /**
     * Méthode de validation copiée fidèlement sur tes critères : Le sol
     * (background) doit exister, et aucune collision ou décoration bloquante
     * (rooms, veget, veg2) ne doit être présente sur cette tuile.
     */
    private boolean estPositionValideFleur(double posX, double posY, Carte carte) {
        int tailleTuile = carte.getTailleTuile();
        int tileX = (int) (posX / tailleTuile);
        int tileY = (int) (posY / tailleTuile);

        if (tileX < 0 || tileX >= carte.getLargeur() || tileY < 0 || tileY >= carte.getHauteur()) {
            return false;
        }

        int[][] rooms = carte.getRooms();
        int[][] background = carte.getBackground();
        int[][] veget = carte.getVeget();
        int[][] veg2 = carte.getVeg2();

        if (rooms != null && rooms[tileY][tileX] != -1) {
            return false;
        }
        if (background == null || background[tileY][tileX] == -1) {
            return false;
        }
        if (veget != null && veget[tileY][tileX] != -1) {
            return false;
        }
        if (veg2 != null && veg2[tileY][tileX] != -1) {
            return false;
        }

        return true;
    }

    /**
     * Charge l'image de l'avatar spécifié.
     */
    private void chargerImage() {
        int avatar_size = 25;
        try {
            String chemin = "../resources/" + this.typeAvatar + ".png";
            BufferedImage imageOriginale = ImageIO.read(getClass().getResource(chemin));

            this.sprite = new BufferedImage(avatar_size, avatar_size, BufferedImage.TYPE_INT_ARGB);
            Graphics2D g = this.sprite.createGraphics();
            g.drawImage(imageOriginale, 0, 0, avatar_size, avatar_size, null);
            g.dispose();
        } catch (Exception ex) {
            System.err.println("Ressource introuvable : " + this.typeAvatar + ".png. Repli sur l'abeille.");
            try {
                BufferedImage imageOriginale = ImageIO.read(getClass().getResource("../resources/bee.png"));
                this.sprite = new BufferedImage(avatar_size, avatar_size, BufferedImage.TYPE_INT_ARGB);
                Graphics2D g = this.sprite.createGraphics();
                g.drawImage(imageOriginale, 0, 0, avatar_size, avatar_size, null);
                g.dispose();
            } catch (IOException e) {
                Logger.getLogger(Avatar.class.getName()).log(Level.SEVERE, null, e);
            }
        }
    }

    public void miseAJour() {
        if (this.toucheGauche) {
            x -= 10;
            regardeADroite = false;
        }
        if (this.toucheDroite) {
            x += 10;
            regardeADroite = true;
        }
        if (this.toucheUp) {
            y -= 10;
        }
        if (this.toucheDown) {
            y += 10;
        }

        if (x > largeurCarte - sprite.getWidth()) {
            x = largeurCarte - sprite.getWidth();
        }
        if (x < 0) {
            x = 0;
        }
        if (y > hauteurCarte - sprite.getHeight()) {
            y = hauteurCarte - sprite.getHeight();
        }
        if (y < 0) {
            y = 0;
        }

        if (monParticipant != null) {
            monParticipant.setPosX(x);
            monParticipant.setPosY(y);
        }
    }

    public void rendu(Graphics2D contexte, Camera camera) {
        int drawX = (int) (x - camera.getX());
        int drawY = (int) (y - camera.getY());
        int w = sprite.getWidth();
        int h = sprite.getHeight();

        if (sprite != null) {
            if (regardeADroite) {
                contexte.drawImage(sprite, drawX, drawY, drawX + w, drawY + h, w, 0, 0, h, null);
            } else {
                contexte.drawImage(sprite, drawX, drawY, null);
            }
        }
    }

    public Rectangle getHitbox() {
        return new Rectangle((int) x, (int) y, sprite.getWidth(), sprite.getHeight());
    }

    public void setToucheGauche(boolean etat) {
        this.toucheGauche = etat;
    }

    public void setToucheDroite(boolean etat) {
        this.toucheDroite = etat;
    }

    public void setToucheHaut(boolean b) {
        this.toucheUp = b;
    }

    public void setToucheBas(boolean b) {
        this.toucheDown = b;
    }

    public double getX() {
        return x;
    }

    public double getY() {
        return y;
    }

    public void setX(double x) {
        this.x = x;
    }

    public void setY(double y) {
        this.y = y;
    }

    public double getLargeur() {
        return sprite != null ? sprite.getWidth() : 50;
    }

    public double getHauteur() {
        return sprite != null ? sprite.getHeight() : 50;
    }

    public Participant getParticipant() {
        return monParticipant;
    }

    public String getTypeAvatar() {
        return typeAvatar;
    }
}
