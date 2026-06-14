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
import moteur.Imposteur;
import moteur.Jeu;
import moteur.Joueur;
import moteur.Participant;
import sql.JoueurSQL;   


public class FenetreDeJeu extends JFrame implements ActionListener, KeyListener {

    private BufferedImage framebuffer;
    private Graphics2D contexte;
    private JLabel jLabel1;
    private Jeu jeu;        
    private Timer timer;
    private final int width = 640;
    private final int height = 480;

    // NOUVEAU : le compte du joueur local (pour la fermeture)
    private Participant monCompte;

    public FenetreDeJeu(Participant moi) {
        // Le joueur existe déjà en base (créé dans Welcome/Lobby) :
        // on le réutilise tel quel, sans en recréer un.
        this.monCompte = moi;

        // Initialisation de la fenêtre
        this.setSize(width, height);
        this.setResizable(true);
        this.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        this.setTitle("Abeille Among Us - " + monCompte.getNom());

        this.jLabel1 = new JLabel();
        this.jLabel1.setPreferredSize(new java.awt.Dimension(width, height));
        this.setContentPane(this.jLabel1);
        this.pack();

        // Buffer graphique
        this.framebuffer = new BufferedImage(this.jLabel1.getWidth(), this.jLabel1.getHeight(), BufferedImage.TYPE_INT_ARGB);
        this.jLabel1.setIcon(new ImageIcon(framebuffer));
        this.contexte = this.framebuffer.createGraphics();

        System.out.println("Entré en jeu : " + monCompte.getNom() + " (ID=" + monCompte.getId() + ")");

        // Création du jeu multijoueur (on passe le compte)
        this.jeu = new Jeu(this.jLabel1.getWidth(), this.jLabel1.getHeight(), this.monCompte);

        // Timer pour la boucle de jeu (40 ms)
        this.timer = new Timer(40, this);
        this.timer.start();
        this.addKeyListener(this);
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
    }

//    public static void main(String[] args) {
//        FenetreDeJeu fenetre = new FenetreDeJeu();
//        fenetre.setVisible(true);
//    }
//    public static void main(String[] args) {
////        // Lancement solo pour tests (hors lobby)
//////        Participant test = new Participant("TestSolo");
//////        new sql.JoueurSQL().creerParticipant(test);   // crée une ligne pour le test
////            FenetreDeJeu fenetre = new FenetreDeJeu();
////            fenetre.setVisible(true);
//    }
//}