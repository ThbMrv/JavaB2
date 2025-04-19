package modele;

import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.SwingConstants;

import controleur.Global;

/**
 * Gestion des murs
 * @author emds
 *
 */
public class Mur extends Objet implements Global {

    /**
     * Constructeur modifié pour aligner les murs en pont
     * @param index Position du mur dans la séquence
     * @param totalMurs Nombre total de murs à créer
     */
    public Mur(int index, int totalMurs) {

        // Calcul de l'espacement des murs pour qu'ils soient collés
        int espace = L_MUR; // Chaque mur est placé juste à côté du précédent
        posX = index * espace; // Position horizontale alignée
        posY = H_ARENE / 2; // Alignement au centre de l'arène
        
        // Création du label pour ce mur
        label = new Label(-1, new JLabel());
        label.getjLabel().setHorizontalAlignment(SwingConstants.CENTER);
        label.getjLabel().setVerticalAlignment(SwingConstants.CENTER);
        label.getjLabel().setBounds(posX, posY, L_MUR, H_MUR);
        label.getjLabel().setIcon(new ImageIcon(MUR));
    }
    
    public boolean estDestructible() {
        return false; // Par défaut, un mur n'est pas destructible
    }

}
