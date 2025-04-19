package modele;

import java.util.ArrayList;
import java.util.Hashtable;

import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.SwingConstants;

import controleur.Global;
import outils.connexion.Connection;

public class Boule extends Objet implements Global{
	
	private JeuServeur jeuServeur;
	
	public Boule(JeuServeur jeuServeur) {
		posX = 0;
		posY = 0;
		this.jeuServeur = jeuServeur;
		label = new Label(Label.getNbLabel(), new JLabel());
		Label.setNbLabel(Label.getNbLabel()+ 1);
		label.getjLabel().setHorizontalAlignment(SwingConstants.CENTER);
		label.getjLabel().setVerticalAlignment(SwingConstants.CENTER);
		label.getjLabel().setBounds(posX, posY, L_BOULE, H_BOULE);
		label.getjLabel().setIcon(new ImageIcon(BOULE));
		
		label.getjLabel().setVisible(false);
		
		jeuServeur.nouveauLabelJeu(label);
	}
	
	public void tireBoule(Joueur attaquant, ArrayList<Mur> lesMurs, Hashtable<Connection, Joueur> lesJoueurs) {
	    // Position initiale de la boule en fonction de l'orientation
	    if (attaquant.getOrientation() == GAUCHE) {
	        setPosX(attaquant.getPosX() - L_BOULE - 1);
	    } else if (attaquant.getOrientation() == DROITE) {
	        setPosX(attaquant.getPosX() + L_PERSO + 1);
	    }

	    //setPosY(attaquant.getPosY() + L_PERSO / 2 - H_BOULE / 2);
	    setPosY(attaquant.getPosY() + H_PERSO / 2 - H_BOULE / 2);

	    // Lancer l'attaque animée
	    new Attaque(attaquant, jeuServeur, lesMurs, lesJoueurs);
	}


}
