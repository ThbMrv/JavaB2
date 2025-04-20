package modele;

import javax.swing.JOptionPane;
import javax.swing.JPanel;

import controleur.Controle;
import outils.connexion.Connection;

/**
 * Gestion du jeu c�t� client
 * @author emds
 *
 */
public class JeuClient extends Jeu {

	// propri�t�s
	private Connection connection ;
	
	/**
	 * Controleur
	 * @param controle
	 */
	public JeuClient(Controle controle) {
		super.controle = controle ;
	}
	
	@Override
	public void setConnection(Connection connection) {
		this.connection = connection ;
	}

	@Override
	public void reception(Connection connection, Object info) {
	    if(info instanceof JPanel) {
	        controle.evenementModele(this, "ajout panel murs", info);
	    } else if(info instanceof Label) {
	        controle.evenementModele(this, "ajout joueur", info);
	    } else if (info instanceof String) {
	        String message = (String) info;
	        // popup
	        if (message.startsWith("popup~")) {
	            String texte = message.substring("popup~".length());
	            javax.swing.JOptionPane.showMessageDialog(null, texte, "🎉 Fin de la partie", javax.swing.JOptionPane.INFORMATION_MESSAGE);
	            return;
	        }
	        controle.evenementModele(this, "remplace chat", message);
	    }
	    if (info instanceof Integer) {
	        controle.evenementModele(this, "son", info);
	    }
	}


	@Override
	public void deconnection(Connection connection) {
	    System.exit(0);
	}
	
	/**
	 * Envoi d'une information vers l'ordinateur distant
	 * @param info
	 */
	public void envoi(Object info) {
		super.envoi(connection, info) ;
	}

}