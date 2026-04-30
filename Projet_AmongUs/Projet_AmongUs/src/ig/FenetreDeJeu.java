package ig;

import java.awt.Graphics2D;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.Timer;
import moteur.Jeu;
import moteur.Joueur;
import sql.JoueurSQL;   // NOUVEAU : pour la base de données


public class FenetreDeJeu extends JFrame implements ActionListener, KeyListener {

    private BufferedImage framebuffer;
    private Graphics2D contexte;
    private JLabel jLabel1;
    private Jeu jeu;        
    private Timer timer;
    private final int width = 640;
    private final int height = 480;

    // NOUVEAU : le compte du joueur local (pour la fermeture)
    private Joueur monCompte;

    public FenetreDeJeu() {
        // Initialisation de la fenêtre 
        this.setSize(width, height);
        this.setResizable(false);
        this.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE); // MODIFIÉ pour appeler notre propre fermeture
        this.setTitle("Abeille Among Us - Multijoueur");

        this.jLabel1 = new JLabel();
        this.jLabel1.setPreferredSize(new java.awt.Dimension(width, height));
        this.setContentPane(this.jLabel1);
        this.pack();

        // Buffer graphique
        this.framebuffer = new BufferedImage(this.jLabel1.getWidth(), this.jLabel1.getHeight(), BufferedImage.TYPE_INT_ARGB);
        this.jLabel1.setIcon(new ImageIcon(framebuffer));
        this.contexte = this.framebuffer.createGraphics();

        // MULTIJOUEUR : création ou récupération du compte
        // Dans un vrai jeu, on afficherait une boîte de dialogue pour le pseudo.
        // Ici on choisit un nom fixe pour la démo (à changer pour chaque test).
        String pseudo = "Joueur_" + System.currentTimeMillis() % 1000; // pseudo unique à chaque lancement
        String motDePasse = "mdp";

        JoueurSQL JoueurSql = new JoueurSQL();
        // Pour simplifier, on crée un nouveau joueur à chaque fois.
        this.monCompte = new Joueur(0, pseudo, motDePasse);
        JoueurSql.creerJoueur(monCompte);   // l'ID est automatiquement généré
        System.out.println("Connecté en tant que " + monCompte.getNom() + " (ID=" + monCompte.getId() + ")");

        // Création du jeu multijoueur (on passe le compte)
        this.jeu = new Jeu(this.jLabel1.getWidth(), this.jLabel1.getHeight(), this.monCompte);

        // Timer pour la boucle de jeu (40 ms)
        this.timer = new Timer(40, this);
        this.timer.start();

        // Gestion des touches
        this.addKeyListener(this);

        // ---------- NOUVEAU : gestion de la fermeture propre ----------
        this.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                fermerJeu();
            }
        });
    }

    /**
     * Boucle de jeu appelée par le timer toutes les 40 ms.
     */
    @Override
    public void actionPerformed(ActionEvent e) {
        this.jeu.miseAJour();
        this.jeu.rendu(contexte);
        this.jLabel1.repaint();
        if (this.jeu.estTermine()) {
            fermerJeu();
        }
    }

    /**
     * Nettoie les ressources et ferme l'application.
     */
    private void fermerJeu() {
        System.out.println("Fermeture du jeu...");
        if (jeu != null) {
            jeu.arreter();   // arrête le timer BDD et marque le joueur inactif
        }
        if (timer != null && timer.isRunning()) {
            timer.stop();
        }
        System.exit(0);
    }

    // --- Gestion des touches (inchangée, mais utilise le getAvatar() du Jeu) ---
    @Override
    public void keyPressed(KeyEvent evt) {
        if (evt.getKeyCode() == KeyEvent.VK_RIGHT) {
            this.jeu.getAvatar().setToucheDroite(true);
        }
        if (evt.getKeyCode() == KeyEvent.VK_LEFT) {
            this.jeu.getAvatar().setToucheGauche(true);
        }
        if (evt.getKeyCode() == KeyEvent.VK_UP) {
            this.jeu.getAvatar().setToucheHaut(true);
        }
        if (evt.getKeyCode() == KeyEvent.VK_DOWN) {
            this.jeu.getAvatar().setToucheBas(true);
        }
    }

    @Override
    public void keyReleased(KeyEvent evt) {
        if (evt.getKeyCode() == KeyEvent.VK_RIGHT) {
            this.jeu.getAvatar().setToucheDroite(false);
        }
        if (evt.getKeyCode() == KeyEvent.VK_LEFT) {
            this.jeu.getAvatar().setToucheGauche(false);
        }
        if (evt.getKeyCode() == KeyEvent.VK_UP) {
            this.jeu.getAvatar().setToucheHaut(false);
        }
        if (evt.getKeyCode() == KeyEvent.VK_DOWN) {
            this.jeu.getAvatar().setToucheBas(false);
        }
    }

    @Override
    public void keyTyped(KeyEvent e) {
        // rien
    }

    // --- Point d'entrée ---
    public static void main(String[] args) {
        FenetreDeJeu fenetre = new FenetreDeJeu();
        fenetre.setVisible(true);
    }
}