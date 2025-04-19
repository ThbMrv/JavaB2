package modele;

import java.util.ArrayList;
import java.util.Hashtable;

import controleur.Controle;
import controleur.Global;
import outils.connexion.Connection;

/**
 * Gestion du jeu c�t� serveur
 * @author emds
 *
 */
public class JeuServeur extends Jeu implements Global {

	// propri�t�s
	private ArrayList<Mur> lesMurs = new ArrayList<Mur>() ;
	private Hashtable<Connection, Joueur> lesJoueurs = new Hashtable<Connection, Joueur>() ;
	private Hashtable<Joueur, Integer> scores = new Hashtable<>();
	private ArrayList<Joueur> lesJoueursDansLordre = new ArrayList<Joueur>() ;
	private boolean partieTerminee = false;
	
	/**
	 * Constructeur
	 * @param controle
	 */
	public JeuServeur(Controle controle) {
		super.controle = controle ;
		// initialisation du rang du dernier label m�moris�
		Label.setNbLabel(0);
	}
	
	/**
	 * G�n�ration des murs
	 */
	public void constructionMurs() {
	    for (int k = 0; k < NBMURS; k++) {
	        // 1️⃣ Crée d'abord le mur "de sol" (le pont)
	        Mur murSol = new Mur(k, NBMURS);
	        lesMurs.add(murSol);
	        controle.evenementModele(this, "ajout mur", murSol.getLabel().getjLabel());

	        // 2️⃣ Exclure les zones de spawn
	        int margeSpawn = 4; // nombre de blocs à ne pas bloquer près des bords
	        if (k < margeSpawn || k > (NBMURS - margeSpawn)) continue;

	        // 3️⃣ 30% de chance d’ajouter un mur destructible AU-DESSUS DU PONT
	        if (Math.random() < 0.3) {
	            Mur murDestruct = new MurDestructible(k, NBMURS);
	            lesMurs.add(murDestruct);
	            controle.evenementModele(this, "ajout mur", murDestruct.getLabel().getjLabel());
	        }
	    }
	}


	
	/**
	 * Demande au controleur d'ajouter un joueuer dans l'ar�ne
	 * @param label
	 */
	public void nouveauLabelJeu(Label label) {
		controle.evenementModele(this, "ajout joueur", label.getjLabel());
	}
	
	/**
	 * Envoi � tous les clients
	 */
	public void envoi(Object info) {
		for (Connection connection : lesJoueurs.keySet()) {
			super.envoi(connection, info);
		}
	}

	@Override
	public void setConnection(Connection connection) {
		Joueur joueur = new Joueur(this);

		int index = lesJoueursDansLordre.size();
		int xSpawn = (index == 0) ? 0 : (NBMURS - 1) * L_MUR;
		int ySpawn = (H_ARENE / 2) - H_PERSO - H_MUR; // ✅ spawn juste au-dessus du pont

		joueur.setPosX(xSpawn);
		joueur.setPosY(ySpawn);

		lesJoueurs.put(connection, joueur);
		lesJoueursDansLordre.add(joueur);
		scores.put(joueur, 0);
	}


	@Override
	public void reception(Connection connection, Object info) {
		String[] infos = ((String)info).split(SEPARE) ;
		String laPhrase ;
		switch(Integer.parseInt(infos[0])) {
			// un nouveau joueur vient d'arriver
		case PSEUDO:
		    controle.evenementModele(this, "envoi panel murs", connection);

		    // d'abord on envoie les anciens joueurs (déjà initialisés)
		    for (Joueur joueur : lesJoueursDansLordre) {
		    	super.envoi(connection, joueur.getLabel());
		    	super.envoi(connection, joueur.getMessage());

		    	if (joueur.getBoule() != null) {
		    		super.envoi(connection, joueur.getBoule().getLabel());
		    	}
		    }
		    // maintenant on initialise le nouveau joueur
		    Joueur nouveau = lesJoueurs.get(connection);
		    nouveau.initPerso(infos[1], Integer.parseInt(infos[2]), lesJoueurs, lesMurs);

		    // ensuite seulement, on l'ajoute à la liste
		    lesJoueursDansLordre.add(nouveau);

		    // message de bienvenue
		    laPhrase = "***" + nouveau.getPseudo() + " vient de se connecter ***";
		    controle.evenementModele(this, "ajout phrase", laPhrase);
		    break;
			case CHAT :
				laPhrase = lesJoueurs.get(connection).getPseudo()+" > "+infos[1] ;
				controle.evenementModele(this, "ajout phrase", laPhrase);
				break ;
			case ACTION :
				if (partieTerminee) return;

				int action = Integer.parseInt(infos[1]);
				Joueur joueur = lesJoueurs.get(connection);

				if (!joueur.estMort()) {
					joueur.action(action, lesMurs, lesJoueurs);
				}

				int index = lesJoueursDansLordre.indexOf(joueur);
				int goalX = (index == 0) ? (NBMURS - 1) * L_MUR : 0;

				if (Math.abs(joueur.getPosX() - goalX) < 20) {
					ajouterPointEtReset(joueur);
				}
				break;
		}
	}
	
	private void ajouterPointEtReset(Joueur joueur) {
		int score = scores.get(joueur) + 1;
		scores.put(joueur, score);

		String message = joueur.getPseudo() + " marque un point ! (" + score + "/" + SCORE_MAX + ")";
		controle.evenementModele(this, "ajout phrase", message);

		if (score >= SCORE_MAX) {
			String victoire = "🏆 " + joueur.getPseudo() + " a gagné la partie !";
			controle.evenementModele(this, "ajout phrase", victoire);

			partieTerminee = true; // ✅ empêche tout mouvement

			// ✅ Envoie une instruction de popup à tous les clients
			envoi("popup~Fin de la partie ! " + joueur.getPseudo() + " a gagné !");
			return;
		}
		// Seul le joueur qui marque est respawn
		int index = lesJoueursDansLordre.indexOf(joueur);
		int xSpawn = (index == 0) ? 0 : (NBMURS - 1) * L_MUR;
		int ySpawn = (H_ARENE / 2) - H_PERSO - H_MUR;

		joueur.setPosX(xSpawn);
		joueur.setPosY(ySpawn);
		joueur.affiche(MARCHE, 1);

	}

	
	@Override
	public void deconnection(Connection connection) {
		// TODO Auto-generated method stub
		Joueur joueur = lesJoueurs.get(connection);

	    if (joueur != null) {
	        joueur.departJoueur(this);
	        lesJoueurs.remove(connection);
	        System.out.println("🚪 Joueur " + joueur.getPseudo() + " déconnecté.");
	    }
	}

}
