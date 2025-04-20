package modele;

public class MurDestructible extends Mur {
	private int vie = 1;
	
	public MurDestructible(int k, int nbMurs) {
		super(k, nbMurs);
		posY = H_ARENE / 2 - H_PERSO;
		label.getjLabel().setBounds(posX, posY, L_MUR, H_MUR);
	}
	
	@Override
	public boolean estDestructible() {
		return true;
	}

	public void subirDegat() {
		vie--;
	}

	public boolean estDetruit() {
		return vie <= 0;
	}
}