package modele;

import java.awt.Font;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.Hashtable;

import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.SwingConstants;


import controleur.Global;
import outils.connexion.Connection;

/**
 * Gestion des joueurs
 * @author emds
 *
 */
public class Joueur extends Objet implements Global {

	// propri�t�s
	private String pseudo ;
	private int numPerso ;
	private Label message ;
	private JeuServeur jeuServeur ;
	private int vie ; // vie restante du joueur
	private int orientation ; // tourn� vers la gauche (0) ou vers la droite (1)
	private int etape ; // num�ro d'�tape dans l'animation
	private Boule boule;
	
	private static final int MAXVIE = 10;
	private static final int GAIN = 1;
	private static final int PERTE = 2; 
	
	private boolean enSaut = false;
	private int hauteurActuelle = 0;
	private boolean bloque = false;


	
	/**
	 * Constructeur
	 */
	public Joueur(JeuServeur jeuServeur) {
		this.jeuServeur = jeuServeur ;
		vie = MAXVIE ;
		etape = 1 ;
		orientation = DROITE ;
	}
	
	/**
	 * @return the pseudo
	 */
	public String getPseudo() {
		return pseudo;
	}
	
	public Boule getBoule() {
		return boule;
	}

	public int getOrientation() {
		// TODO Auto-generated method stub
		return orientation;
	}
	
	public void gainVie() {
		if (vie < MAXVIE) {
			vie += GAIN;
		}else {
			vie = MAXVIE;
		}
	}
	
	public boolean isBloque() {
	    return bloque;
	}
	
	public void perteVie() {
		if (vie > 0) {
			vie -= PERTE;
		}else {
			vie = 0;
		}
	}
	
	public boolean estMort() {
	    return vie == 0;
	}
	
	public void corrigerPosition() {
	    // Bord gauche
	    if (posX < 0) posX = 0;

	    // Bord droit (sécurisé avec largeur du joueur)
	    if (posX > L_ARENE - L_PERSO) {
	        posX = L_ARENE - L_PERSO;
	    }

	    // Bord haut
	    if (posY < 0) posY = 0;

	    // Bord bas (pareil)
	    if (posY > H_ARENE - H_PERSO) {
	        posY = H_ARENE - H_PERSO;
	    }
	}

	public void setBloque(boolean bloque) {
	    this.bloque = bloque;
	}

	
	public void sauter(ArrayList<Mur> lesMurs, Hashtable<Connection, Joueur> lesJoueurs) {
	    if (!enSaut) {
	        enSaut = true;
	        hauteurActuelle = 0;

	        new Thread(() -> {
	            while (enSaut && hauteurActuelle < HAUTEUR_SAUT) {
	                int ancienneY = posY;
	                posY -= SAUT;
	                hauteurActuelle += SAUT;

	                // collision plafond
	                if (posY < 0 || toucheMur(lesMurs)) {
	                    posY = ancienneY;
	                    break;
	                }

	                corrigerPosition();
	                affiche(MARCHE, etape);

	                try {
	                    Thread.sleep(15); // fluide
	                } catch (InterruptedException e) {
	                    e.printStackTrace();
	                }
	            }

	            // redescente (gravité)
	            while (!toucheMur(lesMurs) && posY + H_PERSO < H_ARENE) {
	                int ancienneY = posY;
	                posY += GRAVITE;

	                if (toucheMur(lesMurs)) {
	                    posY = ancienneY;
	                    break;
	                }

	                corrigerPosition();
	                affiche(MARCHE, etape);

	                try {
	                    Thread.sleep(15);
	                } catch (InterruptedException e) {
	                    e.printStackTrace();
	                }
	            }

	            enSaut = false; // saut terminé
	        }).start();
	    }
	}

	

	/**
	 * Affiche le personnage et son message
	 * @param etat
	 * @param etape
	 */
	public void affiche(String etat, int etape) {
		label.getjLabel().setBounds(posX, posY, L_PERSO, H_PERSO);
		label.getjLabel().setIcon(new ImageIcon(PERSO+numPerso+etat+etape+"d"+orientation+EXTIMAGE));
		message.getjLabel().setBounds(posX-10, posY+H_PERSO, L_PERSO+10, H_MESSAGE);
		message.getjLabel().setText(pseudo+" : "+vie);
		// envoi du personnage � tous les autres joueurs
		jeuServeur.envoi(label);
		jeuServeur.envoi(message);
	}
	
	/**
	 * Initialisation d'un joueur (pseudo et num�ro)
	 * @param pseudo
	 * @param numPerso
	 */
	public void initPerso(String pseudo, int numPerso, Hashtable<Connection, Joueur> lesJoueurs, ArrayList<Mur> lesMurs) {
		this.pseudo = pseudo;
		this.numPerso = numPerso;

		label = new Label(Label.getNbLabel(), new JLabel());
		Label.setNbLabel(Label.getNbLabel() + 1);
		label.getjLabel().setHorizontalAlignment(SwingConstants.CENTER);
		label.getjLabel().setVerticalAlignment(SwingConstants.CENTER);
		jeuServeur.nouveauLabelJeu(label);

		message = new Label(Label.getNbLabel(), new JLabel());
		Label.setNbLabel(Label.getNbLabel() + 1);
		message.getjLabel().setHorizontalAlignment(SwingConstants.CENTER);
		message.getjLabel().setFont(new Font("Dialog", Font.PLAIN, 8));
		jeuServeur.nouveauLabelJeu(message);

		// NE PAS repositionner, on garde le spawn de setConnection
		label.getjLabel().setBounds(posX, posY, L_PERSO, H_PERSO);
		message.getjLabel().setBounds(posX, posY + H_PERSO, L_PERSO + 10, H_MESSAGE);

		affiche(MARCHE, etape);

		boule = new Boule(jeuServeur);
		jeuServeur.envoi(boule.getLabel());
	}


	/**
	 * @return the message
	 */
	public Label getMessage() {
		return message;
	}
	
	/**
	 * Contr�le si le joueur chevauche un des autres joueurs
	 * @param lesJoueurs
	 * @return
	 */
	private boolean toucheJoueur(Hashtable<Connection, Joueur> lesJoueurs) {
		for (Joueur unJoueur : lesJoueurs.values()) {
			if (!unJoueur.equals(this)) {
				if (toucheObjet(unJoueur)) {
					return true ;
				}
			}
		}
		return false ;
	}
	
	/**
	 * Contr�le si le joueur chevauche un des murs
	 * @param lesMurs
	 * @return
	 */
	private boolean toucheMur(ArrayList<Mur> lesMurs) {
		for (Mur unMur : lesMurs) {
			if (toucheObjet(unMur)) {
				return true ;
			}
		}
		return false ;
	}
	
	/**
	 * Calcul de la premi�re position al�atoire du joueur (sans chevaucher un autre joueur ou un mur)
	 * @param lesJoueurs
	 * @param lesMurs
	 */
	private void premierePosition(Hashtable<Connection, Joueur> lesJoueurs, ArrayList<Mur> lesMurs) {
		label.getjLabel().setBounds(0, 0, L_PERSO, H_PERSO);
		do {
			posX = (int) Math.round(Math.random() * (L_ARENE - L_PERSO)) ;
			posY = (int) Math.round(Math.random() * (H_ARENE - H_PERSO - H_MESSAGE)) ;
		}while(toucheJoueur(lesJoueurs)||toucheMur(lesMurs)) ;
	}
	
	
	private int deplace (int action, int position, int orientation, int lepas, int max, Hashtable<Connection, Joueur> lesJoueurs, ArrayList<Mur> lesMurs) {
		
		// Mise à jour de l’orientation si GAUCHE ou DROITE
	    if (action == GAUCHE || action == DROITE) {
	        this.orientation = orientation;
	    }

	    // Sauvegarde de l'ancienne position
	    int ancPos = position;

	    // Déplacement
	    position += lepas;

	    // Vérification des limites de l'arène
	    //position = Math.max(0, position);
	    //position = Math.min(max, position);

	    // Mise à jour temporaire des coordonnées
	    if (action == GAUCHE || action == DROITE) {
	        this.posX = position;
	    } else {
	        this.posY = position;
	    }

	    // Vérification des collisions
	    if (toucheMur(lesMurs) || toucheJoueur(lesJoueurs)) {
	        position = ancPos; // Revenir à l'ancienne position si collision
	    }

	    // Gestion de l’animation (changement d’image)
	    etape = (etape % NBETATSMARCHE) + 1;

	    return position;
	}
	
	public void departJoueur(JeuServeur jeuServeur) {
	    if (label != null && label.getjLabel() != null) { 
	        label.getjLabel().setVisible(false);
	        jeuServeur.envoi(label);
	    }
	    if (message != null && message.getjLabel() != null) { 
	        message.getjLabel().setVisible(false);
	        jeuServeur.envoi(message);
	    }
	    if (boule != null && boule.getLabel() != null && boule.getLabel().getjLabel() != null) { 
	        boule.getLabel().getjLabel().setVisible(false);
	        jeuServeur.envoi(boule.getLabel());
	    }
	}


	
	public void action (int action, ArrayList<Mur> lesMurs, Hashtable<Connection, Joueur> lesJoueurs ) {
		if (bloque) return;
		switch (action) {
		case GAUCHE:posX = deplace(action, posX, GAUCHE, -LEPAS, L_ARENE - L_PERSO, lesJoueurs, lesMurs);break;
        case DROITE:posX = deplace(action, posX, DROITE, LEPAS, L_ARENE - L_PERSO, lesJoueurs, lesMurs);break;
        case HAUT:posY = deplace(action, posY, orientation, -LEPAS, H_ARENE - H_PERSO, lesJoueurs, lesMurs);break;
        case BAS:posY = deplace(action, posY, orientation, LEPAS, H_ARENE - H_PERSO, lesJoueurs, lesMurs);break;
        case TIRE:
            if (!boule.getLabel().getjLabel().isVisible()) {
                jeuServeur.envoi(FIGHT); // Envoi du son de tir
                boule.tireBoule(this, lesMurs, lesJoueurs); // Tir de la boule
            }
            break;
        case SAUT:
            sauter(lesMurs, lesJoueurs);
            break;
		}

		corrigerPosition();
		affiche(MARCHE, etape);
	}
}

