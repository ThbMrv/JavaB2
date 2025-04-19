package vue;

import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;

import controleur.Controle;
import controleur.Global;
import outils.son.Son;

import javax.swing.JLabel;
import javax.swing.JTextField;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

/**
 * Frame de l'ar�ne du jeu
 * @author emds
 *
 */
public class Arene extends JFrame implements Global {

	private JPanel contentPane;
	private JTextField txtSaisie;
	private JPanel jpnMurs ;
	private JPanel jpnJeu ;
	private boolean client ; // arene du client ou du serveur ?
	private Controle controle ;
	private JTextArea txtChat ;
	private Son[] lessons = new Son[SON.length]; // Tableau des sons

	/**
	 * Retourne le contenu complet de la zone de chat
	 * @return
	 */
	public String getContenuTxtChat() {
		return txtChat.getText() ;
	}
	
	/**
	 * Remplace le contenu de txtChat par le contenu du param�tre
	 * @param contenuTxtChat
	 */
	public void remplaceChat(String contenuTxtChat) {
		txtChat.setText(contenuTxtChat);
	}
	
	/**
	 * Ajout d'un mur
	 * @param unMur
	 */
	public void ajoutMur(JLabel unMur) {
		jpnMurs.add(unMur);
		jpnMurs.repaint();
	}
	
	/**
	 * Ajout d'un nouveau personnage (c�t� serveur)
	 * @param unJoueur
	 */
	public void ajoutJoueur(JLabel unJoueur) {
		jpnJeu.add(unJoueur);
		jpnJeu.repaint();
	}
	
	/**
	 * Ajout ou modification d'un personnage (c�t� client)
	 * @param num
	 * @param unLabel
	 */
	public void ajoutModifJoueur(int num, JLabel unLabel) {
		// tentative de suppression
		try {
			jpnJeu.remove(num);
		} catch (ArrayIndexOutOfBoundsException  e) {
		}
		jpnJeu.add(unLabel, num);
		jpnJeu.repaint();	
	}
	
	
	/**
	 * Ajout de tous les murs en une fois
	 * @param lesMurs
	 */
	public void ajoutPanelMurs(JPanel lesMurs) {
		jpnMurs.add(lesMurs);
		jpnMurs.repaint();
		contentPane.requestFocus();
	}
	
	/**
	 * Ajout d'une nouvelle phrase dans la zone de chat
	 * @param unePhrase
	 */
	public void ajoutChat(String unePhrase) {
		txtChat.setText(unePhrase+"\r\n"+txtChat.getText());
	}
	
	/**
	 * Traite la touche utilis�e
	 * @param arg0
	 */
	private void txtSaisie_keyPressed(KeyEvent arg0) {
		if (arg0.getKeyCode()==KeyEvent.VK_ENTER) {
			if (!txtSaisie.getText().equals("")) {
				controle.evenementVue(this, CHAT+SEPARE+txtSaisie.getText());
				txtSaisie.setText("");
				contentPane.requestFocus();
			}
		}
	}
	
	private void contentPane_keyPressed(KeyEvent arg0) {
		int valeur = -1; 
	    switch (arg0.getKeyCode()) {
	        case KeyEvent.VK_LEFT : valeur = GAUCHE ; break; 
	        case KeyEvent.VK_RIGHT : valeur = DROITE ; break;
	        case KeyEvent.VK_UP : valeur = HAUT ; break;   
	        case KeyEvent.VK_DOWN : valeur = BAS ; break;
	        case KeyEvent.VK_SPACE : valeur = TIRE ; break;   
	        case KeyEvent.VK_W: controle.evenementVue(this, ACTION + SEPARE + SAUT); break;
	        case KeyEvent.VK_G : valeur = DETRUIRE_PONT; break;

	    };

	    if (valeur != -1) {
	        controle.evenementVue(this, ACTION + SEPARE + valeur);
	    }
	}
	
	/**
	 * @return jpnMurs
	 */
	public JPanel getJpnMurs() {
		return jpnMurs ;
	}

	public void joueSon(int numero) {
	    if (numero >= 0 && numero < lessons.length) {
	        lessons[numero].play();
	    }
	}

	/**
	 * Create the frame.
	 */
	public Arene(String typeJeu, Controle controle) {
		// arene pour un client ou un serveur ?
		client = (typeJeu.equals("client"));
		// r�cup�ration du controleur
		this.controle = controle;
		
		// les objets graphiques
		setTitle("Arena");
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(100, 100, L_ARENE+3*MARGE, H_ARENE + H_CHAT);
		contentPane = new JPanel();
		if (client)  {
			contentPane.addKeyListener(new KeyAdapter() {
				@Override
				public void keyPressed(KeyEvent arg0) {
					contentPane_keyPressed(arg0);
				}
			});
		}
		
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(contentPane);
		contentPane.setLayout(null);
		
		jpnJeu = new JPanel();
		jpnJeu.setBounds(0, 0, L_ARENE, H_ARENE);
		jpnJeu.setOpaque(false);
		contentPane.add(jpnJeu);
		jpnJeu.setLayout(null);
		
		jpnMurs = new JPanel();
		jpnMurs.setBounds(0, 0, L_ARENE, H_ARENE);
		jpnMurs.setOpaque(false);
		contentPane.add(jpnMurs);
		jpnMurs.setLayout(null);
		
		
		// zone de saisie que pour le client
		if (client) {
			txtSaisie = new JTextField();
			txtSaisie.addKeyListener(new KeyAdapter() {
				@Override
				public void keyPressed(KeyEvent arg0) {
					txtSaisie_keyPressed(arg0);
				}
			});
			txtSaisie.setBounds(0, H_ARENE, L_ARENE, H_SAISIE);
			contentPane.add(txtSaisie);
			txtSaisie.setColumns(10);
		}
		
		
		JScrollPane jspChat = new JScrollPane();
		jspChat.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
		jspChat.setBounds(0, H_ARENE + H_SAISIE, L_ARENE, H_CHAT - H_SAISIE - 7*MARGE);
		contentPane.add(jspChat);
		
		txtChat = new JTextArea();
		jspChat.setViewportView(txtChat);
		
		JLabel lblFond = new JLabel("");
		lblFond.setIcon(new ImageIcon(FONDARENE));
		lblFond.setBounds(0, 0, L_ARENE, H_ARENE);
		contentPane.add(lblFond);
		
		if (client) { // Charger les sons seulement sur le client
			(new Son(SONAMBIANCE)).playContinue();
		    for (int k = 0; k < SON.length; k++) {
		        lessons[k] = new Son(CHEMINSONS + SON[k]);
		    }
		}

	}
}
