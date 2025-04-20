package controleur;

/**
 * Regroupement des constantes de l'application
 * @author emds
 *
 */
public interface Global {
	
	public static final Integer PORT = 6666 ;
	public static final int MAX_JOUEURS = 2;

	int DETRUIRE_PONT = 6;
	
	// fichiers
	public static final String
		SEPARATOR = "//",
		CHEMIN = "media" + SEPARATOR,
		CHEMINFONDS = CHEMIN + "fonds" + SEPARATOR,
		CHEMINPERSOS = CHEMIN + "personnages" + SEPARATOR,
		CHEMINMURS = CHEMIN + "murs" + SEPARATOR,
		CHEMINBOULES = CHEMIN + "boules" + SEPARATOR,
		PERSO = CHEMINPERSOS + "perso",
		EXTIMAGE = ".gif" ;

	// images
	public static final String
	FONDCHOIX = CHEMINFONDS + "fondchoix.jpg",	
	FONDARENE = CHEMINFONDS+"test.png",
	MUR = CHEMINMURS + "sandstone_smooth.gif",
	BOULE = CHEMINBOULES + "boule.gif",
	CHEMINSONS = CHEMIN + "sons/",
	SONPRECEDENT = CHEMINSONS + "precedent.wav",
	SONSUIVANT = CHEMINSONS + "suivant.wav",
	SONGO = CHEMINSONS + "go.wav",
	SONWELCOME = CHEMINSONS + "welcome.wav",
	SONAMBIANCE = CHEMINSONS + "ambiance.wav";
	
	// personnages
	public static final int
		GAUCHE = 0,
		DROITE = 1,
		HAUT = 2,
		BAS = 3,
		TIRE = 4,
		NBETATSMARCHE = 4,
		NBETATSBLESSE = 2,
		NBETATSMORT = 2,
		LEPAS = 10,
		SAUT = 5,
		HAUTEUR_SAUT = 100,
		GRAVITE = 5,
		NBPERSOS = 3,
		H_PERSO = 44,
		L_PERSO = 39,
		FIGHT = 0,
		HURT = 1,
		DEATH = 2;
	public static final String
		MARCHE = "marche",
		BLESSE = "touche",
		MORT = "mort" ;
	
	public static final String[] SON = { "fight.wav", "hurt.wav", "death.wav" };
	
	// messages serveurs
	public static final String SEPARE = "~" ;
	public static final int
		PSEUDO = 0,
		CHAT = 1,
		ACTION = 2,
		SCORE_MAX = 3;
	
	// tailles
	public static final int
		H_ARENE = 900,
		L_ARENE = 1720,
		H_CHAT = 200,
		H_MESSAGE = 8,
		H_SAISIE = 25,
		MARGE = 5 ;
	
	// murs
	public static final int
		NBMURS = 57,
		H_MUR = 30,
		L_MUR = 30 ;
	
	public static final int
		L_BOULE = 17,
		H_BOULE = 17;
	
}
