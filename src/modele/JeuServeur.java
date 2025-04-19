package modele;

import java.util.ArrayList;
import java.util.Hashtable;

import javax.swing.JLabel;

import controleur.Controle;
import controleur.Global;
import outils.connexion.Connection;
import java.awt.Font;


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
	
	private Label labelTimer;               // Label pour l'affichage du chrono
	private int dureePartie = 60;           // durée max (en secondes)
	private boolean timerLance = false;     // pour éviter de le lancer 2 fois
	
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
	    int ySpawn = (H_ARENE / 2) - H_PERSO - H_MUR;

	    joueur.setPosX(xSpawn);
	    joueur.setPosY(ySpawn);

	    lesJoueurs.put(connection, joueur);
	    if (!lesJoueursDansLordre.contains(joueur)) {
	        lesJoueursDansLordre.add(joueur);
	    }
	    scores.put(joueur, 0);
	}



	@Override
	public void reception(Connection connection, Object info) {
		String[] infos = ((String)info).split(SEPARE) ;
		String laPhrase ;
		switch(Integer.parseInt(infos[0])) {
			// un nouveau joueur vient d'arriver
		case PSEUDO:
		    // 1. Envoie du panel de murs au nouveau joueur
		    controle.evenementModele(this, "envoi panel murs", connection);

		    // 2. Envoie des joueurs déjà connectés au nouveau joueur
		    for (Joueur joueur : lesJoueursDansLordre) {
		        super.envoi(connection, joueur.getLabel());
		        super.envoi(connection, joueur.getMessage());

		        if (joueur.getBoule() != null) {
		            super.envoi(connection, joueur.getBoule().getLabel());
		        }
		    }

		    // 3. Initialisation du nouveau joueur
		    Joueur nouveau = lesJoueurs.get(connection);
		    nouveau.initPerso(infos[1], Integer.parseInt(infos[2]), lesJoueurs, lesMurs);

		    // 4. Message de bienvenue
		    laPhrase = "*** " + nouveau.getPseudo() + " vient de se connecter ***";
		    controle.evenementModele(this, "ajout phrase", laPhrase);

		    // 5. 🕐 Démarrage du chrono si 2 joueurs connectés
		    if (lesJoueursDansLordre.size() == 2 && !timerLance) {
		        timerLance = true;
		        startTimer(); // ⏱ méthode définie dans JeuServeur
		    }
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
	        partieTerminee = true;
	        envoi("popup~Fin de la partie ! " + joueur.getPseudo() + " a gagné !");
	        return;
	    }

	    // ✅ Reset uniquement le joueur qui a marqué
	    int index = lesJoueursDansLordre.indexOf(joueur);
	    int xSpawn = (index == 0) ? 0 : (NBMURS - 1) * L_MUR;
	    int ySpawn = (H_ARENE / 2) - H_PERSO - H_MUR;
	    joueur.setPosX(xSpawn);
	    joueur.setPosY(ySpawn);
	    joueur.affiche(MARCHE, 1);

	    // ✅ Bloque TOUS les joueurs
	    for (Joueur j : lesJoueursDansLordre) {
	        j.setBloque(true);
	    }

	    // ✅ Affiche un seul message de compte à rebours dans le chat
	    new Thread(() -> {
	        for (int i = 3; i > 0; i--) {
	            controle.evenementModele(this, "ajout phrase", "⏳ Respawn dans : " + i);
	            try {
	                Thread.sleep(1000);
	            } catch (InterruptedException e) {
	                e.printStackTrace();
	            }
	        }

	        for (Joueur j : lesJoueursDansLordre) {
	            j.setBloque(false); // ✅ débloquer tout le monde après 3s
	        }
	    }).start();
	}

	private void startTimer() {
	    labelTimer = new Label(Label.getNbLabel(), new JLabel());
	    Label.setNbLabel(Label.getNbLabel() + 1);
	    labelTimer.getjLabel().setFont(new Font("Arial", Font.BOLD, 16));
	    labelTimer.getjLabel().setBounds(L_ARENE / 2 - 50, 10, 200, 30); // haut de l'écran
	    labelTimer.getjLabel().setText("Temps : " + dureePartie);
	    nouveauLabelJeu(labelTimer);

	    new Thread(() -> {
	        for (int i = dureePartie; i >= 0; i--) {
	            final int t = i;
	            labelTimer.getjLabel().setText("Temps : " + t);
	            envoi(labelTimer);

	            try {
	                Thread.sleep(1000);
	            } catch (InterruptedException e) {
	                e.printStackTrace();
	            }
	        }

	        // ⏰ Temps écoulé : fin de partie
	        partieTerminee = true;

	        Joueur gagnant = null;
	        boolean egalite = false;

	        int maxScore = -1;
	        for (Joueur j : scores.keySet()) {
	            int sc = scores.get(j);
	            if (sc > maxScore) {
	                maxScore = sc;
	                gagnant = j;
	                egalite = false;
	            } else if (sc == maxScore) {
	                egalite = true;
	            }
	        }

	        if (egalite || maxScore == 0) {
	            envoi("popup~⏱ Temps écoulé ! Égalité !");
	        } else {
	            envoi("popup~⏱ Temps écoulé ! " + gagnant.getPseudo() + " gagne avec " + maxScore + " point(s) !");
	        }

	    }).start();
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
