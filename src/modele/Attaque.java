package modele;

import java.util.ArrayList;
import java.util.Hashtable;

import controleur.Global;
import outils.connexion.Connection;

public class Attaque extends Thread implements Global {

	private Joueur attaquant;
	private JeuServeur jeuServeur;
	private ArrayList<Mur> lesMurs;
	private Hashtable<Connection, Joueur> lesJoueurs;
	
	public Attaque(Joueur attaquant, JeuServeur jeuServeur, ArrayList<Mur> lesMurs, Hashtable<Connection, Joueur> lesJoueurs) {
		this.attaquant = attaquant;
		this.jeuServeur = jeuServeur;
		this.lesMurs = lesMurs;
		this.lesJoueurs = lesJoueurs;
		
		super.start();
	}
	
	@Override
	public void run() {
	    Boule laBoule = attaquant.getBoule();
	    int orientation = attaquant.getOrientation();
	    // Rendre la boule visible
	    laBoule.getLabel().getjLabel().setVisible(true);
	    laBoule.setPosX(attaquant.getPosX());
	    laBoule.setPosY(attaquant.getPosY());	    
	    Joueur victime = null;
	    do {
	        // Déplacement de la boule selon l'orientation
	        if (orientation == GAUCHE) {
	            laBoule.setPosX(laBoule.getPosX() - LEPAS);
	        } else if (orientation == DROITE) {
	            laBoule.setPosX(laBoule.getPosX() + LEPAS);
	        }
	        // Mise à jour de la position graphique
	        laBoule.getLabel().getjLabel().setBounds(laBoule.getPosX(), laBoule.getPosY(), L_BOULE, H_BOULE);
	        // Envoi de la position mise à jour aux clients
	        jeuServeur.envoi(laBoule.getLabel());
	        pause(10,0);
	        victime = toucheJoueur();
	    } while (laBoule.getPosX() >= 0 && laBoule.getPosX() <= L_ARENE && laBoule.getPosY() >= 0 && laBoule.getPosY() <= H_ARENE && !toucheMur() && victime == null);
	 // ✅ Vérifier que la victime n'est pas null avant d'exécuter `affiche()`
	    if (victime!=null && !victime.estMort()) {
	    	jeuServeur.envoi(HURT);
	    	victime.perteVie();
	    	attaquant.gainVie();
	    	for (int i = 1; i <= NBETATSBLESSE; i++) {
	            victime.affiche(BLESSE, i);
	            pause(80, 0);
	        }
	    	if (victime.estMort()) {
	    		jeuServeur.envoi(DEATH);
	    		for (int i = 1; i <= NBETATSBLESSE; i++) {
	                victime.affiche(MORT, i);
	                pause(80, 0);
	            }
	    	}else {
	    		victime.affiche(MARCHE,  1);
	    	}
	    	attaquant.affiche(MARCHE, 1);
	    }
	    // Rendre la boule invisible après la fin
	    laBoule.getLabel().getjLabel().setVisible(false);

	    // Envoyer l'état invisible de la boule aux clients
	    jeuServeur.envoi(laBoule.getLabel());
	    
	}
	
	private boolean toucheMur() {
	    if (lesMurs == null) return false;

	    for (int i = 0; i < lesMurs.size(); i++) {
	        Mur mur = lesMurs.get(i);

	        if (attaquant.getBoule().toucheObjet(mur)) {
	            // Si c'est un mur destructible
	            if (mur instanceof MurDestructible) {
	                MurDestructible destructible = (MurDestructible) mur;
	                destructible.subirDegat();

	                // Si le mur est détruit, on le retire de la liste et on le rend invisible
	                if (destructible.estDetruit()) {
	                    destructible.getLabel().getjLabel().setVisible(false);  // Cache le mur
	                    jeuServeur.envoi(destructible.getLabel());              // Mise à jour client
	                    lesMurs.remove(i); // Supprime le mur de la liste
	                    i--; // Reculer d'un indice car la liste a été modifiée
	                }
	            }

	            return true; // Dans tous les cas, la boule est stoppée
	        }
	    }

	    return false;
	}
	
	private Joueur toucheJoueur() {
	    for (Joueur joueur : lesJoueurs.values()) {
	        if (joueur != attaquant && attaquant.getBoule().toucheObjet(joueur)) { // ✅ Vérifie collision
	            return joueur;
	        }
	    }
	    return null;
	}
	
	public void pause(long milli, int nano) {
		try {
			Thread.sleep(milli, nano);
		} catch (InterruptedException e) {
			System.err.println("Le thread a été interrompu : " + e.getMessage());
		}
	}
	
}
