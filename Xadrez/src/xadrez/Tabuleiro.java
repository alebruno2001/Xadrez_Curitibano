package xadrez;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.image.BufferedImage;
import java.awt.image.ImageObserver;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;

import javax.imageio.ImageIO;
import javax.swing.*;

@SuppressWarnings({ "serial", "unused" })
public class Tabuleiro extends JFrame {
	
	Graphics2D g;
	BufferedImage tabuleiro;
	ImageIcon table_icon;
	JLabel table_label;
	JPanel table;
	JScrollPane scroll;
	
	Color preto;
	Color branco;
	Color destaque;
	BufferedImage sheet;
	BufferedImage[][] sub_images;
	//BufferedImage[0] = reiB, principeB, peaoB, torreB, padreB, bispoB, cavaloB, elefanteB, aguiaB, marquesB, condeB, duqueB; //nessa ordem
	//BufferedImage[1] = reiP, principeP, peaoP, torreP, padreP, bispoP, cavaloP, elefanteP, aguiaP, marquesP, condeP, duqueP; //nessa ordem
	
	Peca[] pecas_brancas;
	Peca[] pecas_pretas;
	boolean isWhiteTurn;
	int[] buffer_pos;
	int buffer_id;
	HashSet<int[]> casas_destacadas;
	int peca_selecionada;
	int[] fora;
	
	public Tabuleiro() {
		super("Xadrez 3D");
		
		
		try {tabuleiro = ImageIO.read(new File("Tabuleiro_3d_Horizontal.png"));} catch (IOException e) {e.printStackTrace();}
		table_icon = new ImageIcon(tabuleiro);
		table_label = new JLabel();
		table_label.setIcon(table_icon);
		scroll = new JScrollPane();
		scroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		scroll.setViewportView(table_label);
		table_label.addMouseListener(new Mouse());
		
		g = tabuleiro.createGraphics();
		
		preto  = new Color(185, 122, 87);
		branco = new Color(127, 127, 127);
		destaque = new Color(32, 177, 76);
		try {sheet = ImageIO.read(new File("SpriteSheet.png"));} catch (IOException e) {e.printStackTrace();}
		sub_images = new BufferedImage[2][];
		sub_images[0] = new BufferedImage[12];
		sub_images[1] = new BufferedImage[12];
		for (int i = 0; i < 12; i++) {
			sub_images[1][i] = sheet.getSubimage(i*25, 0, 25, 25);
		}
		for (int i = 0; i < 12; i++) {
			sub_images[0][i] = sheet.getSubimage(i*25, 25, 25, 25);
		}
		
		pecas_brancas = new Peca[128];
		pecas_pretas = new Peca[128];
		buffer_pos = new int[3];
		buffer_id = 2;
		arrumarTabuleiro();
		isWhiteTurn = true;
		casas_destacadas = new HashSet<int[]>();
		fora = new int[] {8,8,8};
		//peca_selecionada = 0;
		
		getContentPane().add(scroll);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setSize(1775,246);
		//this.pack();
		this.setResizable(false);
		setVisible(true);
	}
	
	public class Mouse implements MouseListener {
		
		@Override
		public void mouseClicked(MouseEvent me) {
			
			boolean buffer = true;
			
			buffer_pos[2] = Math.floorDiv(me.getX(), 225);
			buffer_pos[1] = Math.floorDiv(me.getX()-(225*buffer_pos[2]), 25);
			buffer_pos[0] = Math.floorDiv(me.getY(), 25);
			
			if (isWhiteTurn) {
				for (int[] casa : casas_destacadas) {
					System.out.println(casa[0]);
					if (buffer_pos[2]==casa[2] && buffer_pos[1]==casa[1] && buffer_pos[0]==casa[0]) {
						limparDestaque();
						
						if ((casa[0]+casa[1]+casa[2])%2 == 0) {
							g.setColor(branco);
						}
						else {
							g.setColor(preto);
						}
						g.fillRect((25*casa[1])+(225*casa[2]), 25*casa[0], 25, 25);
						
						if ((pecas_brancas[peca_selecionada].posicao[0]+pecas_brancas[peca_selecionada].posicao[1]+pecas_brancas[peca_selecionada].posicao[2])%2 == 0) {
							g.setColor(branco);
						}
						else {
							g.setColor(preto);
						}
						g.fillRect((25*pecas_brancas[peca_selecionada].posicao[1])+(225*pecas_brancas[peca_selecionada].posicao[2]), 25*pecas_brancas[peca_selecionada].posicao[0], 25, 25);
						
						g.drawImage(sub_images[0][pecas_brancas[peca_selecionada].id], null, (25*casa[1])+(225*casa[2]), 25*casa[0]);
						
						pecas_brancas[peca_selecionada].posicao = casa;
						
						for (int i = 0; i < 128; i++) {
							Peca peca = pecas_pretas[i];
							if (casa[2]==peca.posicao[2] && casa[1]==peca.posicao[1] && casa[0]==peca.posicao[0]) {
								pecas_pretas[i].posicao = fora;
								break;
							}
						}
						
						isWhiteTurn = false;
						buffer = false;
						System.out.println("Ok");
						
						break;
					}
				}
				if (buffer) {
					for (int i = 0; i < 128; i++) {
						Peca peca = pecas_brancas[i];
						if (buffer_pos[2]==peca.posicao[2] && buffer_pos[1]==peca.posicao[1] && buffer_pos[0]==peca.posicao[0]) {
							System.out.println(peca.id);
							limparDestaque();
							selecionarPeca(peca);
							peca_selecionada = i;
							
							break;
						}
					}
				}
			} else {
				for (int[] casa : casas_destacadas) {
					System.out.println(casa[0]);
					if (buffer_pos[2]==casa[2] && buffer_pos[1]==casa[1] && buffer_pos[0]==casa[0]) {
						limparDestaque();
						
						if ((casa[0]+casa[1]+casa[2])%2 == 0) {
							g.setColor(branco);
						}
						else {
							g.setColor(preto);
						}
						g.fillRect((25*casa[1])+(225*casa[2]), 25*casa[0], 25, 25);
						
						if ((pecas_pretas[peca_selecionada].posicao[0]+pecas_pretas[peca_selecionada].posicao[1]+pecas_pretas[peca_selecionada].posicao[2])%2 == 0) {
							g.setColor(branco);
						}
						else {
							g.setColor(preto);
						}
						g.fillRect((25*pecas_pretas[peca_selecionada].posicao[1])+(225*pecas_pretas[peca_selecionada].posicao[2]), 25*pecas_pretas[peca_selecionada].posicao[0], 25, 25);
						
						g.drawImage(sub_images[1][pecas_pretas[peca_selecionada].id], null, (25*casa[1])+(225*casa[2]), 25*casa[0]);
						
						pecas_pretas[peca_selecionada].posicao = casa;
						
						for (int i = 0; i < 128; i++) {
							Peca peca = pecas_brancas[i];
							if (casa[2]==peca.posicao[2] && casa[1]==peca.posicao[1] && casa[0]==peca.posicao[0]) {
								pecas_brancas[i].posicao = fora;
								break;
							}
						}
						
						isWhiteTurn = true;
						buffer = false;
						System.out.println("Ok");
						
						break;
					}
				}
				if (buffer) {
					for (int i = 0; i < 128; i++) {
						Peca peca = pecas_pretas[i];
						if (buffer_pos[2]==peca.posicao[2] && buffer_pos[1]==peca.posicao[1] && buffer_pos[0]==peca.posicao[0]) {
							System.out.println(peca.id);
							limparDestaque();
							selecionarPeca(peca);
							peca_selecionada = i;
							
							break;
						}
					}
				}
			}
			
			scroll.repaint();
		}
		
		@Override
		public void mouseEntered(MouseEvent arg0) {}
		@Override
		public void mouseExited(MouseEvent arg0) {}
		@Override
		public void mousePressed(MouseEvent arg0) {}
		@Override
		public void mouseReleased(MouseEvent arg0) {}
		
	}
	
	public void selecionarPeca(Peca p) {
		switch (p.id) {
		case (0):
			for (int i = 0; i < 3; i++) {
				for (int j = 0; j < 3; j++) {
					for (int k = 0; k < 3; k++) {
						casas_destacadas.add(new int[] {p.posicao[0]-1+i,p.posicao[1]-1+j,p.posicao[2]-1+k});
					}
				}
			} break;
		case (1):
			for (int i = 0; i < 8; i++) {
				casas_destacadas.add(new int[] {p.posicao[0],p.posicao[1],i});
				casas_destacadas.add(new int[] {p.posicao[0],i,p.posicao[2]});
				casas_destacadas.add(new int[] {i,p.posicao[1],p.posicao[2]});
			}
			for (int i = -7; i < 8; i++) {
				casas_destacadas.add(new int[] {p.posicao[0]  ,p.posicao[1]+i,p.posicao[2]+i});
				casas_destacadas.add(new int[] {p.posicao[0]+i,p.posicao[1]  ,p.posicao[2]+i});
				casas_destacadas.add(new int[] {p.posicao[0]+i,p.posicao[1]+i,p.posicao[2]  });
				casas_destacadas.add(new int[] {p.posicao[0]  ,p.posicao[1]-i,p.posicao[2]+i});
				casas_destacadas.add(new int[] {p.posicao[0]-i,p.posicao[1]  ,p.posicao[2]+i});
				casas_destacadas.add(new int[] {p.posicao[0]-i,p.posicao[1]+i,p.posicao[2]  });
			}
			for (int i = -7; i < 8; i++) {
				casas_destacadas.add(new int[] {p.posicao[0]+i,p.posicao[1]+i,p.posicao[2]+i});
				casas_destacadas.add(new int[] {p.posicao[0]-i,p.posicao[1]+i,p.posicao[2]+i});
				casas_destacadas.add(new int[] {p.posicao[0]+i,p.posicao[1]-i,p.posicao[2]+i});
				casas_destacadas.add(new int[] {p.posicao[0]+i,p.posicao[1]+i,p.posicao[2]-i});
				casas_destacadas.add(new int[] {p.posicao[0]-i,p.posicao[1]-i,p.posicao[2]+i});
				casas_destacadas.add(new int[] {p.posicao[0]-i,p.posicao[1]-i,p.posicao[2]+i});
				casas_destacadas.add(new int[] {p.posicao[0]-i,p.posicao[1]+i,p.posicao[2]-i});
				casas_destacadas.add(new int[] {p.posicao[0]-i,p.posicao[1]-i,p.posicao[2]-i});
			} break;
		case (2):
			if (p.isWhite) {
				casas_destacadas.add(new int[] {p.posicao[0],p.posicao[1],p.posicao[2]+1});
				casas_destacadas.add(new int[] {p.posicao[0]+1,p.posicao[1],p.posicao[2]+1});
				casas_destacadas.add(new int[] {p.posicao[0]-1,p.posicao[1],p.posicao[2]+1});
				casas_destacadas.add(new int[] {p.posicao[0],p.posicao[1]+1,p.posicao[2]+1});
				casas_destacadas.add(new int[] {p.posicao[0],p.posicao[1]-1,p.posicao[2]+1});
			}
			else {
				casas_destacadas.add(new int[] {p.posicao[0],p.posicao[1],p.posicao[2]-1});
				casas_destacadas.add(new int[] {p.posicao[0]+1,p.posicao[1],p.posicao[2]-1});
				casas_destacadas.add(new int[] {p.posicao[0]-1,p.posicao[1],p.posicao[2]-1});
				casas_destacadas.add(new int[] {p.posicao[0],p.posicao[1]+1,p.posicao[2]-1});
				casas_destacadas.add(new int[] {p.posicao[0],p.posicao[1]-1,p.posicao[2]-1});
			} break;
		case (3):
			for (int i = 0; i < 8; i++) {
				casas_destacadas.add(new int[] {p.posicao[0],p.posicao[1],i});
				casas_destacadas.add(new int[] {p.posicao[0],i,p.posicao[2]});
				casas_destacadas.add(new int[] {i,p.posicao[1],p.posicao[2]});
			} break;
		case (4):
			for (int i = -7; i < 8; i++) {
				casas_destacadas.add(new int[] {p.posicao[0]  ,p.posicao[1]+i,p.posicao[2]+i});
				casas_destacadas.add(new int[] {p.posicao[0]+i,p.posicao[1]  ,p.posicao[2]+i});
				casas_destacadas.add(new int[] {p.posicao[0]+i,p.posicao[1]+i,p.posicao[2]  });
				casas_destacadas.add(new int[] {p.posicao[0]  ,p.posicao[1]-i,p.posicao[2]+i});
				casas_destacadas.add(new int[] {p.posicao[0]-i,p.posicao[1]  ,p.posicao[2]+i});
				casas_destacadas.add(new int[] {p.posicao[0]-i,p.posicao[1]+i,p.posicao[2]  });
			} break;
		case (5):
			for (int i = -7; i < 8; i++) {
				casas_destacadas.add(new int[] {p.posicao[0]+i,p.posicao[1]+i,p.posicao[2]+i});
				casas_destacadas.add(new int[] {p.posicao[0]-i,p.posicao[1]+i,p.posicao[2]+i});
				casas_destacadas.add(new int[] {p.posicao[0]+i,p.posicao[1]-i,p.posicao[2]+i});
				casas_destacadas.add(new int[] {p.posicao[0]+i,p.posicao[1]+i,p.posicao[2]-i});
				casas_destacadas.add(new int[] {p.posicao[0]-i,p.posicao[1]-i,p.posicao[2]+i});
				casas_destacadas.add(new int[] {p.posicao[0]-i,p.posicao[1]-i,p.posicao[2]+i});
				casas_destacadas.add(new int[] {p.posicao[0]-i,p.posicao[1]+i,p.posicao[2]-i});
				casas_destacadas.add(new int[] {p.posicao[0]-i,p.posicao[1]-i,p.posicao[2]-i});
			} break;
		case (6):
			for (int i = -1; i <= 1; i += 2) {
				for (int j = -2; j <= 2; j += 4) {
					casas_destacadas.add(new int[] {p.posicao[0]  ,p.posicao[1]+i,p.posicao[2]+j});
					casas_destacadas.add(new int[] {p.posicao[0]  ,p.posicao[1]+j,p.posicao[2]+i});
					casas_destacadas.add(new int[] {p.posicao[0]+j,p.posicao[1]+i,p.posicao[2]  });
					casas_destacadas.add(new int[] {p.posicao[0]+j,p.posicao[1]  ,p.posicao[2]+i});
					casas_destacadas.add(new int[] {p.posicao[0]+i,p.posicao[1]+j,p.posicao[2]  });
					casas_destacadas.add(new int[] {p.posicao[0]+i,p.posicao[1]  ,p.posicao[2]+j});
				}
			} break;
		case (7):
			for (int i = -1; i <= 1; i += 2) {
				for (int j = -2; j <= 2; j += 4) {
					for (int k = -1; k <= 1; k += 2) {
						casas_destacadas.add(new int[] {p.posicao[0]+k,p.posicao[1]+i,p.posicao[2]+j});
						casas_destacadas.add(new int[] {p.posicao[0]+k,p.posicao[1]+j,p.posicao[2]+i});
						casas_destacadas.add(new int[] {p.posicao[0]+j,p.posicao[1]+i,p.posicao[2]+k});
						casas_destacadas.add(new int[] {p.posicao[0]+j,p.posicao[1]+k,p.posicao[2]+i});
						casas_destacadas.add(new int[] {p.posicao[0]+i,p.posicao[1]+j,p.posicao[2]+k});
						casas_destacadas.add(new int[] {p.posicao[0]+i,p.posicao[1]+k,p.posicao[2]+j});
					}
				}
			} break;
		case (8):
			for (int i = -1; i <= 1; i += 2) {
				for (int j = -2; j <= 2; j += 4) {
					for (int k = -2; k <= 2; k += 4) {
						casas_destacadas.add(new int[] {p.posicao[0]+k,p.posicao[1]+i,p.posicao[2]+j});
						casas_destacadas.add(new int[] {p.posicao[0]+k,p.posicao[1]+j,p.posicao[2]+i});
						casas_destacadas.add(new int[] {p.posicao[0]+j,p.posicao[1]+i,p.posicao[2]+k});
						casas_destacadas.add(new int[] {p.posicao[0]+j,p.posicao[1]+k,p.posicao[2]+i});
						casas_destacadas.add(new int[] {p.posicao[0]+i,p.posicao[1]+j,p.posicao[2]+k});
						casas_destacadas.add(new int[] {p.posicao[0]+i,p.posicao[1]+k,p.posicao[2]+j});
					}
				}
			} break;
		case (9):
			for (int i = 0; i < 8; i++) {
				casas_destacadas.add(new int[] {p.posicao[0],p.posicao[1],i});
				casas_destacadas.add(new int[] {p.posicao[0],i,p.posicao[2]});
				casas_destacadas.add(new int[] {i,p.posicao[1],p.posicao[2]});
			}
			for (int i = -7; i < 8; i++) {
				casas_destacadas.add(new int[] {p.posicao[0]  ,p.posicao[1]+i,p.posicao[2]+i});
				casas_destacadas.add(new int[] {p.posicao[0]+i,p.posicao[1]  ,p.posicao[2]+i});
				casas_destacadas.add(new int[] {p.posicao[0]+i,p.posicao[1]+i,p.posicao[2]  });
				casas_destacadas.add(new int[] {p.posicao[0]  ,p.posicao[1]-i,p.posicao[2]+i});
				casas_destacadas.add(new int[] {p.posicao[0]-i,p.posicao[1]  ,p.posicao[2]+i});
				casas_destacadas.add(new int[] {p.posicao[0]-i,p.posicao[1]+i,p.posicao[2]  });
			} break;
		case (10):
			for (int i = 0; i < 8; i++) {
				casas_destacadas.add(new int[] {p.posicao[0],p.posicao[1],i});
				casas_destacadas.add(new int[] {p.posicao[0],i,p.posicao[2]});
				casas_destacadas.add(new int[] {i,p.posicao[1],p.posicao[2]});
			}
			for (int i = -7; i < 8; i++) {
				casas_destacadas.add(new int[] {p.posicao[0]+i,p.posicao[1]+i,p.posicao[2]+i});
				casas_destacadas.add(new int[] {p.posicao[0]-i,p.posicao[1]+i,p.posicao[2]+i});
				casas_destacadas.add(new int[] {p.posicao[0]+i,p.posicao[1]-i,p.posicao[2]+i});
				casas_destacadas.add(new int[] {p.posicao[0]+i,p.posicao[1]+i,p.posicao[2]-i});
				casas_destacadas.add(new int[] {p.posicao[0]-i,p.posicao[1]-i,p.posicao[2]+i});
				casas_destacadas.add(new int[] {p.posicao[0]-i,p.posicao[1]-i,p.posicao[2]+i});
				casas_destacadas.add(new int[] {p.posicao[0]-i,p.posicao[1]+i,p.posicao[2]-i});
				casas_destacadas.add(new int[] {p.posicao[0]-i,p.posicao[1]-i,p.posicao[2]-i});
			} break;
		case (11):
			for (int i = -7; i < 8; i++) {
				casas_destacadas.add(new int[] {p.posicao[0]  ,p.posicao[1]+i,p.posicao[2]+i});
				casas_destacadas.add(new int[] {p.posicao[0]+i,p.posicao[1]  ,p.posicao[2]+i});
				casas_destacadas.add(new int[] {p.posicao[0]+i,p.posicao[1]+i,p.posicao[2]  });
				casas_destacadas.add(new int[] {p.posicao[0]  ,p.posicao[1]-i,p.posicao[2]+i});
				casas_destacadas.add(new int[] {p.posicao[0]-i,p.posicao[1]  ,p.posicao[2]+i});
				casas_destacadas.add(new int[] {p.posicao[0]-i,p.posicao[1]+i,p.posicao[2]  });
			}
			for (int i = -7; i < 8; i++) {
				casas_destacadas.add(new int[] {p.posicao[0]+i,p.posicao[1]+i,p.posicao[2]+i});
				casas_destacadas.add(new int[] {p.posicao[0]-i,p.posicao[1]+i,p.posicao[2]+i});
				casas_destacadas.add(new int[] {p.posicao[0]+i,p.posicao[1]-i,p.posicao[2]+i});
				casas_destacadas.add(new int[] {p.posicao[0]+i,p.posicao[1]+i,p.posicao[2]-i});
				casas_destacadas.add(new int[] {p.posicao[0]-i,p.posicao[1]-i,p.posicao[2]+i});
				casas_destacadas.add(new int[] {p.posicao[0]-i,p.posicao[1]-i,p.posicao[2]+i});
				casas_destacadas.add(new int[] {p.posicao[0]-i,p.posicao[1]+i,p.posicao[2]-i});
				casas_destacadas.add(new int[] {p.posicao[0]-i,p.posicao[1]-i,p.posicao[2]-i});
			} break;
		}
		
		//HashSet<int[]> buffer = casas_destacadas;
		//for (int[] casa : buffer) {
		//	if (Arrays.equals(casa, p.posicao) || p.posicao[0]<0 || p.posicao[0]>7 || p.posicao[1]<0 || p.posicao[1]>7 || p.posicao[2]<0 || p.posicao[2]>7) {
		//		casas_destacadas.remove(casa);
		//	}
		//}
		
		for (int[] casa : casas_destacadas) {
			g.setColor(destaque);
			g.drawRect((25*casa[1])+(225*casa[2]), 25*casa[0], 25, 25);
		}
	}
	
	public void limparDestaque() {
		for (int[] pos : casas_destacadas) {
			if ((pos[0]+pos[1]+pos[2])%2 == 0) {
				g.setColor(branco);
			}
			else {
				g.setColor(preto);
			}
			g.drawRect((25*pos[1])+(225*pos[2]), 25*pos[0], 25, 25);
		}
		casas_destacadas.clear();
	}
	
	public void arrumarTabuleiro() {
		
		//Arrumando os peões:
		for (int i = 0; i < 8; i++) {
			for (int j = 0; j < 8; j++) {
				pecas_brancas[(8*i) + j] = new Peca(new int[] {i,j,1},true,2);
				g.drawImage(sub_images[0][2], null, (25*j)+225, 25*i);
			}
		}
		
		for (int i = 0; i < 8; i++) {
			for (int j = 0; j < 8; j++) {
				pecas_pretas[(8*i) + j] = new Peca(new int[] {i,j,6},false,2);
				g.drawImage(sub_images[1][2], null, (25*j)+1350, 25*i);
			}
		}
		
		//Arrumando as demais peças:
		buffer_pos[2] = 0;
		for (int i = 0; i < 4; i++) {
			for (int j = 0; j < 4; j++) {
				buffer_id = qualPeca(i+1,j+1);
				
				buffer_pos[0] = i;
				buffer_pos[1] = j;
				pecas_brancas[64+(16*i)+(4*j)] = new Peca(new int[] {i,j,0},true,buffer_id);
				g.drawImage(sub_images[0][buffer_id], null, 25*j, 25*i);
				
				buffer_pos[0] = 7-i;
				pecas_brancas[65+(16*i)+(4*j)] = new Peca(new int[] {7-i,j,0},true,buffer_id);
				g.drawImage(sub_images[0][buffer_id], null, 25*j, 25*(7-i));
				
				buffer_pos[1] = 7-j;
				pecas_brancas[66+(16*i)+(4*j)] = new Peca(new int[] {7-i,7-j,0},true,buffer_id);
				g.drawImage(sub_images[0][buffer_id], null, 25*(7-j), 25*(7-i));
				
				buffer_pos[0] = i;
				pecas_brancas[67+(16*i)+(4*j)] = new Peca(new int[] {i,7-j,0},true,buffer_id);
				g.drawImage(sub_images[0][buffer_id], null, 25*(7-j), 25*i);
			}
		}
		
		buffer_pos[2] = 7;
		for (int i = 0; i < 4; i++) {
			for (int j = 0; j < 4; j++) {
				buffer_id = qualPeca(i+1,j+1);
				
				buffer_pos[0] = i;
				buffer_pos[1] = j;
				pecas_pretas[64+(16*i)+(4*j)] = new Peca(new int[] {i,j,7},false,buffer_id);
				g.drawImage(sub_images[1][buffer_id], null, (25*j)+1575, 25*i);
				
				buffer_pos[0] = 7-i;
				pecas_pretas[65+(16*i)+(4*j)] = new Peca(new int[] {7-i,j,7},false,buffer_id);
				g.drawImage(sub_images[1][buffer_id], null, (25*j)+1575, 25*(7-i));
				
				buffer_pos[1] = 7-j;
				pecas_pretas[66+(16*i)+(4*j)] = new Peca(new int[] {7-i,7-j,7},false,buffer_id);
				g.drawImage(sub_images[1][buffer_id], null, (25*(7-j))+1575, 25*(7-i));
				
				buffer_pos[0] = i;
				pecas_pretas[67+(16*i)+(4*j)] = new Peca(new int[] {i,7-j,7},false,buffer_id);
				g.drawImage(sub_images[1][buffer_id], null, (25*(7-j))+1575, 25*i);
			}
		}
		
		//Colocando os Reis:
		pecas_brancas[127] = new Peca(new int[]{3,4,0},true,0);
		g.setColor(preto);
		g.fillRect(100, 75, 25, 25);
		g.drawImage(sub_images[0][0], null, 100, 75);
		
		pecas_pretas[127] = new Peca(new int[]{3,4,7},false,0);
		g.setColor(branco);
		g.fillRect(1675, 75, 25, 25);
		g.drawImage(sub_images[1][0], null, 1675, 75);
	}
	
	public int qualPeca(int linha, int coluna) {
		int id = 2;
		int produto = linha*coluna;
		
		switch (produto) {
		case (1): id = 3; break;
		case (2): id = 6; break;
		case (3): id = 4; break;
		case (4): if (linha == coluna) {id = 7;}  else {id = 10;}  break;
		case (6): id = 8; break;
		case (8): id = 9; break;
		case (9): id = 5; break;
		case (12): id = 11; break;
		case (16): id = 1; break;
		}
		
		return id;
	}
	
	public static void main(String[] args) {
		new Tabuleiro();
	}

}
