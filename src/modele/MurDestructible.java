package modele;

public class MurDestructible extends Mur {
	private int vie = 1; // ✅ Nombre de coups avant destruction (modifiable)
	
	public MurDestructible(int k, int nbMurs) {
		super(k, nbMurs);
		posY = H_ARENE / 2 - H_PERSO; // ✅ Niveau du joueur
		label.getjLabel().setBounds(posX, posY, L_MUR, H_MUR);
	}
	
	@Override
	public boolean estDestructible() {
		return true;
	}

	public void subirDegat() {
		vie--; // Perte de 1 PV
	}

	public boolean estDetruit() {
		return vie <= 0;
	}
}
