package xadrez;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

/*
 * @author  = Alexandre Bruno dos Santos
 * @version = 1.0
 * @year    = 2019
 * @city    = Curitiba
 */
@SuppressWarnings({"serial"})
public class Tabuleiro extends JFrame {
	
	//Variáveis Gráficas
	Graphics2D g;
	BufferedImage tabuleiro;
	ImageIcon table_icon;
	JLabel table_label;
	JPanel table;
	JScrollPane scroll;
	BufferedImage sheet;
	BufferedImage[][] sub_images;
	//BufferedImage[0] = {reiB, principeB, peaoB, torreB, padreB, bispoB, cavaloB, elefanteB, aguiaB, marquesB, condeB, duqueB}
	//BufferedImage[1] = {reiP, principeP, peaoP, torreP, padreP, bispoP, cavaloP, elefanteP, aguiaP, marquesP, condeP, duqueP}
	final String[] nome_da_cor;
	//nome_da_cor[0] = "Brancas"
	//nome_da_cor[1] = "Pretas"
	final Color[] cor;
	//cor[0] = branco/cinza
	//cor[1] = preto/marrom
	final Color destaque; //verde
	
	Peca[][] pecas;
	//pecas[0] = Conjunto de todas as peças brancas
	//pecas[1] = Conjunto de todas as peças pretas
	int vez; // vez = 0 significa vez das brancas e vez = 1 significa vez das pretas
			 // (1-vez) é uma forma de indicar o jogador que não está na vez
			 // Perceba que 1-(1)=0 e 1-(0)=1
	final int[] fora; // Quando capturada, a peça não será deletada no array pecas,
				// pois isso poderia gerar problemas
				// Na verdade, ela é lançada para fora do tabuleiro
				// Ou seja, sua posição será igualada à constante fora
	//Buffers gerais
	int[] buffer_pos;
	int buffer_id;
	int peca_selecionada; // Representa a última peça selecionada
	ArrayList<int[]> casas_destacadas; // Armazena as casas para as quais a peça selecionada pode ser movida
	//Buffers para en passant
	boolean en_passant_possivel;
	int[] en_passant_pos;
	
	public Tabuleiro() {
		//Inicializa todas as variavéis globais e a janela de jogo
		
		super("Xadrez Curitibano");
		
		//Carrega e define como imagem de fundo a imagem do tabuleiro e constrói a interface gráfica
		try {tabuleiro = ImageIO.read(new File("Tabuleiro_3d_Horizontal.png"));} catch (IOException e) {e.printStackTrace();}
		table_icon = new ImageIcon(tabuleiro);
		table_label = new JLabel();
		table_label.setIcon(table_icon);
		scroll = new JScrollPane();
		scroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
		scroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_NEVER);
		scroll.setViewportView(table_label);
		table_label.addMouseListener(new Mouse());
		g = tabuleiro.createGraphics();
		
		//Carrega a SpriteSheet e corta a imagem de cada peça
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
		
		//Inicializa e define constantes
		fora = new int[] {8,8,8};
		nome_da_cor = new String[2];
		nome_da_cor[0] = "Brancas";
		nome_da_cor[1] = "Pretas";
		cor = new Color[2];
		cor[0] = new Color(127, 127, 127);// Branco/Cinza
		cor[1]  = new Color(185, 122, 87);// Preto/Marrom
		destaque = new Color(32, 177, 76);// Verde
		
		//Inicializa buffers
		buffer_pos = new int[3];
		buffer_id = 2;
		casas_destacadas = new ArrayList<int[]>();
		en_passant_possivel = false;
		en_passant_pos = new int[3];
		
		//Distribui as peças de cada jogador e inicia o jogo com a vez das brancas
		pecas = new Peca[2][128];
		arrumarTabuleiro();
		vez = 0;
		
		//Especifica valores da janela de jogo
		getContentPane().add(scroll);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setSize(1785,246);
		setResizable(false);
		setVisible(true);
	}
	
	public class Mouse implements MouseListener {
		
		@Override
		public void mouseClicked(MouseEvent me) {
			//Caso o usuário clique em algum lugar dentro da tela de jogo
			//3 coisas principais podem acontecer:
			//- Selecionar uma peça da cor que está na vez
			//- Escolher uma casa destacada, realizando um movimento com a peça selecionada anteriormente
			//- Nenhuma das anteriores, nesse caso nada acontece. Se tiver selecionada uma peça antes, o destaque desaparece
			
			//Buffers para controle lógico
			boolean selecionou_casa_destacada = false;
			boolean selecionou_peca = false;
			
			buffer_pos[2] = Math.floorDiv(me.getX(), 225); //Calcula-se em qual plano o usuário clicou
			buffer_pos[1] = Math.floorDiv(me.getX()-(225*buffer_pos[2]), 25); //Calcula-se em qual coluna o usuário clicou
			buffer_pos[0] = Math.floorDiv(me.getY(), 25); //Calcula-se em qual linha o usuário clicou
			
			//Primeiro verifica-se se alguma casa destacada foi escolhida
			for (int[] casa : casas_destacadas) {
				if (buffer_pos[2]==casa[2] && buffer_pos[1]==casa[1] && buffer_pos[0]==casa[0]) {
					//Se sim, a peça selecionada será movida
					//Por isso, o destaque deve ser limpo
					limparDestaque();
					
					//Se for possível realizar en passant
					if (en_passant_possivel) {
						//O en passant deixa de ser possível, pois deve ser realizado imediatamente após o salto duplo do peão
						en_passant_possivel = false;
						//Se a peça selecionada for um peão e casa escolhida for a de en passant, realiza-se en passant
						if (pecas[vez][peca_selecionada].id==2) {
							if (casa[2]==en_passant_pos[2] && casa[1]==en_passant_pos[1] && casa[0]==en_passant_pos[0]) {
								en_passant_pos[2] += -1 + (2*vez);
								//O peão que sofreu en passant desaparece, mesmo que sua casa não vá ser ocupada por outra peça como na captura normal
								for (int i = 0; i < 128; i++) {
									Peca peca = pecas[1-vez][i];
									if (en_passant_pos[2]==peca.posicao[2] && en_passant_pos[1]==peca.posicao[1] && en_passant_pos[0]==peca.posicao[0]) {
										g.setColor(cor[(pecas[1-vez][i].posicao[0]+pecas[1-vez][i].posicao[1]+pecas[1-vez][i].posicao[2])%2]);
										g.fillRect((25*pecas[1-vez][i].posicao[1])+(225*pecas[1-vez][i].posicao[2]), 25*pecas[1-vez][i].posicao[0], 25, 25);
										pecas[1-vez][i].posicao = fora;
										break;
									}
								}
							}
						}
					}
					
					//Se a peça selecionada for um peão, verificamos mais algumas propriedades especiais
					if (pecas[vez][peca_selecionada].id==2) {
						if (pecas[vez][peca_selecionada].posicao[2] == 1 + (5*vez)) {
							if (casa[2] == 3 + vez) {
								//Se estiver realizando salto duplo, pode sofrer en passant na próxima jogada
								en_passant_possivel = true;
								en_passant_pos = new int[] {pecas[vez][peca_selecionada].posicao[0], pecas[vez][peca_selecionada].posicao[1], 2 + (3*vez)};
							}
						}
						if (pecas[vez][peca_selecionada].posicao[2] == 6 - (5*vez)) {
							//Se estiver chegando no último plano, *poderá* ser promovida
							//Se o usuário fechar o popup de promoção, seu peão não será promovido
							Object[] options = new Object[10];
							for (int i = 3; i < 12; i++) {
								options[i-3] = new ImageIcon(sub_images[1][i]);
							}
							options[9] = new ImageIcon(sub_images[1][1]);
							int promocao = JOptionPane.showOptionDialog(null, "Para qual peça você gostaria de promover seu peão?", "Promoção de peão", JOptionPane.DEFAULT_OPTION, JOptionPane.QUESTION_MESSAGE, null, options, options[9]);
							if (promocao == 9) {
								pecas[vez][peca_selecionada].id = 1;
							} else {
								pecas[vez][peca_selecionada].id = promocao + 3;
							}
						}
					}
					
					//Como a peça selecionada está sendo movida
					//Limpa-se ou Repinta-se a casa antiga e a casa nova da peça selecionada
					g.setColor(cor[(casa[0]+casa[1]+casa[2])%2]);
					g.fillRect((25*casa[1])+(225*casa[2]), 25*casa[0], 25, 25);
					
					g.setColor(cor[(pecas[vez][peca_selecionada].posicao[0]+pecas[vez][peca_selecionada].posicao[1]+pecas[vez][peca_selecionada].posicao[2])%2]);
					g.fillRect((25*pecas[vez][peca_selecionada].posicao[1])+(225*pecas[vez][peca_selecionada].posicao[2]), 25*pecas[vez][peca_selecionada].posicao[0], 25, 25);
					
					//Desenha-se a peça em sua nova casa
					g.drawImage(sub_images[vez][pecas[vez][peca_selecionada].id], null, (25*casa[1])+(225*casa[2]), 25*casa[0]);
					
					pecas[vez][peca_selecionada].posicao = casa;
					
					vez = 1-vez;
					selecionou_casa_destacada = true;
					
					//Se havia alguma peça na casa para a qual a peça selecionada está se movendo
					//Esta peça está sendo capturada, ou seja, deve ser retirada do jogo
					for (int i = 0; i < 128; i++) {
						Peca peca = pecas[vez][i];
						if (casa[2]==peca.posicao[2] && casa[1]==peca.posicao[1] && casa[0]==peca.posicao[0]) {
							pecas[vez][i].posicao = fora;
							//Se a peça capturada é o rei, o jogo termina
							//Nesse caso, usuário tem duas opções
							//- Recomeçar (O tabuleiro retorna ao seu estado inicial e é vez das brancas)
							//- Finalizar (O programa se encerra)
							if (pecas[vez][i].id==0) {
								scroll.repaint();
								int jogar_novamente = JOptionPane.showOptionDialog(null, nome_da_cor[1-vez] + " vencem!", "PARABÉNS", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE, null, new Object[]{"Jogar Novamente", "Sair"}, "Jogar Novamente");
								if (jogar_novamente == JOptionPane.YES_OPTION) {
									try {tabuleiro = ImageIO.read(new File("Tabuleiro_3d_Horizontal.png"));} catch (IOException e) {e.printStackTrace();}
									g.drawImage(tabuleiro, null, 0, 0);
									arrumarTabuleiro();
									vez = 0;
								} else {
									System.exit(0);
								}
							}
							break;
						}
					}
					break;
				}
			}
			//Se nenhuma casa destacada foi selecionada ou se não há casas destacadas para serem selecionadas
			//Verifica-se se alguma peça foi selecionada
			if (!selecionou_casa_destacada) {
				for (int i = 0; i < 128; i++) {
					Peca peca = pecas[vez][i];
					if (buffer_pos[2]==peca.posicao[2] && buffer_pos[1]==peca.posicao[1] && buffer_pos[0]==peca.posicao[0]) {
						limparDestaque();
						selecionarPeca(peca);
						peca_selecionada = i;
						selecionou_peca = true;
						
						break;
					}
				}
				//Se nem casa destacada nem peça tiverem sido selecionados
				//Deduz-se que o usuário clicou em alguma casa não válida, vazia ou numa peça inimiga
				if (!selecionou_peca) {
					limparDestaque();
				}
			}
			//Exibe-se as alterações gráficas realizadas
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
		//Destaca as casas que a peça selecionada p pode ocupar nessa jogada
		//Incluindo deslocamentos, capturas e en passants
		
		buffer_pos = p.posicao.clone();
		
		switch (p.id) {
		case (0): //Rei
			for (int i = -1; i <= 1; i++) {
				for (int j = -1; j <= 1; j++) {
					for (int k = -1; k <= 1; k++) {
						casas_destacadas.add(new int[] {p.posicao[0]+i,p.posicao[1]+j,p.posicao[2]+k});
					}
				}
			} break;
		case (1): //Príncipe
			for (int i = -1; i <= 1; i++) {
				for (int j = -1; j <= 1; j++) {
					for (int k = -1; k <= 1; k++) {
						for (int l = 1; l < 8; l++) {
							buffer_pos[0] += i;
							buffer_pos[1] += j;
							buffer_pos[2] += k;
							casas_destacadas.add(buffer_pos.clone());
							if (estaOcupada(buffer_pos)) {break;}
						}
						buffer_pos = p.posicao.clone();
					}
				}
			} break;
		case (2): //Peão
			if (p.isWhite) {
				int[] p0  = new int[] {p.posicao[0],p.posicao[1],p.posicao[2]+1};
				int[] p00 = new int[] {p.posicao[0],p.posicao[1],p.posicao[2]+2};
				casas_destacadas.add(p0);
				if (p.posicao[2] == 1) {
					casas_destacadas.add(p00);
				}
				
				int[] p1 = new int[] {p.posicao[0]+1,p.posicao[1],p.posicao[2]+1};
				int[] p2 = new int[] {p.posicao[0]-1,p.posicao[1],p.posicao[2]+1};
				int[] p3 = new int[] {p.posicao[0],p.posicao[1]+1,p.posicao[2]+1};
				int[] p4 = new int[] {p.posicao[0],p.posicao[1]-1,p.posicao[2]+1};
				
				for (Peca peca : pecas[1]) {
					if (Arrays.equals(peca.posicao, p1) || Arrays.equals(peca.posicao, p2) || Arrays.equals(peca.posicao, p3) || Arrays.equals(peca.posicao, p4)) {
						casas_destacadas.add(peca.posicao);
					}
					if (Arrays.equals(peca.posicao, p0)) {
						casas_destacadas.remove(p0);
						casas_destacadas.remove(p00);
					}
					if (Arrays.equals(peca.posicao, p00)) {
						casas_destacadas.remove(p00);
					}
				}
				
				for (Peca peca : pecas[0]) {
					if (Arrays.equals(peca.posicao, p0)) {
						casas_destacadas.remove(p0);
						casas_destacadas.remove(p00);
					}
				}
				if (en_passant_possivel) {
					if (p.posicao[2] == 4) {
						if (Arrays.equals(en_passant_pos, p1) || Arrays.equals(en_passant_pos, p2) || Arrays.equals(en_passant_pos, p3) || Arrays.equals(en_passant_pos, p4)) {
							casas_destacadas.add(en_passant_pos.clone());
						}
					}
				}
			}
			else {
				int[] p0  = new int[] {p.posicao[0],p.posicao[1],p.posicao[2]-1};
				int[] p00 = new int[] {p.posicao[0],p.posicao[1],p.posicao[2]-2};
				casas_destacadas.add(p0);
				if (p.posicao[2] == 6) {
					casas_destacadas.add(p00);
				}
				
				int[] p1 = new int[] {p.posicao[0]+1,p.posicao[1],p.posicao[2]-1};
				int[] p2 = new int[] {p.posicao[0]-1,p.posicao[1],p.posicao[2]-1};
				int[] p3 = new int[] {p.posicao[0],p.posicao[1]+1,p.posicao[2]-1};
				int[] p4 = new int[] {p.posicao[0],p.posicao[1]-1,p.posicao[2]-1};
				
				for (Peca peca : pecas[0]) {
					if (Arrays.equals(peca.posicao, p1) || Arrays.equals(peca.posicao, p2) || Arrays.equals(peca.posicao, p3) || Arrays.equals(peca.posicao, p4)) {
						casas_destacadas.add(peca.posicao);
					}
					if (Arrays.equals(peca.posicao, p0)) {
						casas_destacadas.remove(p0);
						casas_destacadas.remove(p00);
					}
					if (Arrays.equals(peca.posicao, p00)) {
						casas_destacadas.remove(p00);
					}
				}
				
				for (Peca peca : pecas[1]) {
					if (Arrays.equals(peca.posicao, p0)) {
						casas_destacadas.remove(p0);
						casas_destacadas.remove(p00);
					}
				}
				
				if (en_passant_possivel) {
					if (p.posicao[2] == 3) {
						if (Arrays.equals(en_passant_pos, p1) || Arrays.equals(en_passant_pos, p2) || Arrays.equals(en_passant_pos, p3) || Arrays.equals(en_passant_pos, p4)) {
							casas_destacadas.add(en_passant_pos.clone());
						}
					}
				}
			} break;
		case (3): //Torre
			movimentoLinear(p, 2);
			break;
		case (4): //Padre
			movimentoLinear(p, 1);
			break;
		case (5): //Bispo
			movimentoLinear(p, 0);
			break;
		case (6): //Cavalo
			movimentoComplementar(p, 0);
			break;
		case (7): //Elefante
			movimentoComplementar(p, 1);
			break;
		case (8): //Águia
			movimentoComplementar(p, 2);
			break;
		case (9): //Marquês
			movimentoLinear(p, 1, 2);
			break;
		case (10): //Conde
			movimentoLinear(p, 0, 2);
			break;
		case (11): //Duque
			movimentoLinear(p, 0, 1);
			break;
		}
		
		//Retira o destaque das casas que estão fora do tabuleiro
		ArrayList<int[]> buffer = new ArrayList<int[]>();
		for (int[] casa : casas_destacadas) {
			if (casa[0]<0 || casa[0]>7 || casa[1]<0 || casa[1]>7 || casa[2]<0 || casa[2]>7) {
				buffer.add(casa);
			}
		}
		casas_destacadas.removeAll(buffer);
		buffer.clear();
		
		//Retira o destaque das casas ocupadas por peças da mesma cor
		//Isso evita que sejam capturados aliados
		if (p.isWhite) {
			for (Peca peca : pecas[0]) {
				for (int[] casa : casas_destacadas) {
					if (Arrays.equals(peca.posicao, casa)) {
						buffer.add(casa);
					}
				}
			}
		} else {
			for (Peca peca : pecas[1]) {
				for (int[] casa : casas_destacadas) {
					if (Arrays.equals(peca.posicao, casa)) {
						buffer.add(casa);
					}
				}
			}
		}
		casas_destacadas.removeAll(buffer);
		
		//Desenha um quadrado verde ao redor de cada casa destacada
		//Para auxiliar o jogador no planejamento de suas jogadas
		for (int[] casa : casas_destacadas) {
			g.setColor(destaque);
			g.drawRect((25*casa[1])+(225*casa[2]), 25*casa[0], 24, 24);
		}
	}
	
	public boolean estaOcupada(int[] casa) {
		//Verifica se determinada casa está ocupada por peça de qualquer cor
		boolean b = false;
		for (Peca peca : pecas[0]) {
			if (peca.posicao[0]==casa[0] && peca.posicao[1]==casa[1] && peca.posicao[2]==casa[2]) {
				b = true;
				break;
			}
		}
		for (Peca peca : pecas[1]) {
			if (peca.posicao[0]==casa[0] && peca.posicao[1]==casa[1] && peca.posicao[2]==casa[2]) {
				b = true;
				break;
			}
		}
		return b;
	}
	
	public void limparDestaque() {
		//Desenha um quadrado da cor da casa sobre cada quadrado verde de destaque
		for (int[] pos : casas_destacadas) {
			g.setColor(cor[(pos[0]+pos[1]+pos[2])%2]);
			g.drawRect((25*pos[1])+(225*pos[2]), 25*pos[0], 24, 24);
		}
		casas_destacadas.clear();
	}
	
	public void movimentoLinear(Peca p, int n_zeros) {
		//Movimento de Peças Lineares Simples ou Clero
		for (int i = -1; i <= 1; i++) {
			for (int j = -1; j <= 1; j++) {
				for (int k = -1; k <= 1; k++) {
					if (quantosZeros(i,j,k) == n_zeros) {
						for (int l = 1; l < 8; l++) {
							buffer_pos[0] += i;
							buffer_pos[1] += j;
							buffer_pos[2] += k;
							casas_destacadas.add(buffer_pos.clone());
							if (estaOcupada(buffer_pos)) {break;}
						}
						buffer_pos = p.posicao.clone();
					}
				}
			}
		}
	}
	
	public void movimentoLinear(Peca p, int n1_zeros, int n2_zeros) {
		//Movimento de Peças Lineares Compostas ou Corte Real
		for (int i = -1; i <= 1; i++) {
			for (int j = -1; j <= 1; j++) {
				for (int k = -1; k <= 1; k++) {
					if (quantosZeros(i,j,k) == n1_zeros || quantosZeros(i,j,k) == n2_zeros) {
						for (int l = 1; l < 8; l++) {
							buffer_pos[0] += i;
							buffer_pos[1] += j;
							buffer_pos[2] += k;
							casas_destacadas.add(buffer_pos.clone());
							if (estaOcupada(buffer_pos)) {break;}
						}
						buffer_pos = p.posicao.clone();
					}
				}
			}
		}
	}
	
	public void movimentoComplementar(Peca p, int c) {
		//Movimento de Peças Complementares ou Animais
		for (int i = -1; i <= 1; i += 2) {
			for (int j = -1; j <= 1; j += 2) {
				for (int k = -1; k <= 1; k += 2) {
					for (int l = 0; l < 3; l++) {
						buffer_pos[l] += i*2;
						buffer_pos[(l+1)%3] += j;
						buffer_pos[(l+2)%3] += k*c;
						casas_destacadas.add(buffer_pos.clone());
						buffer_pos = p.posicao.clone();
						
						buffer_pos[l] += i*2;
						buffer_pos[(l+1)%3] += k*c;
						buffer_pos[(l+2)%3] += j;
						casas_destacadas.add(buffer_pos.clone());
						buffer_pos = p.posicao.clone();
					}
				}
			}
		}
	}
	
	public int quantosZeros(int[] numeros) {
		int n_zeros = 0;
		
		for (int numero : numeros) {
			if (numero == 0) {
				n_zeros++;
			}
		}
		
		return n_zeros;
	}
	
	public int quantosZeros(int x1, int x2, int x3) {
		int n_zeros = 0;
		
		if (x1 == 0) {
			n_zeros++;
		}
		if (x2 == 0) {
			n_zeros++;
		}
		if (x3 == 0) {
			n_zeros++;
		}
		
		return n_zeros;
	}
	
	public void arrumarTabuleiro() {
		
		//Arrumando os peões:
		for (int i = 0; i < 8; i++) {
			for (int j = 0; j < 8; j++) {
				pecas[0][(8*i) + j] = new Peca(new int[] {i,j,1},true,2);
				g.drawImage(sub_images[0][2], null, (25*j)+225, 25*i);
			}
		}
		
		for (int i = 0; i < 8; i++) {
			for (int j = 0; j < 8; j++) {
				pecas[1][(8*i) + j] = new Peca(new int[] {i,j,6},false,2);
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
				pecas[0][64+(16*i)+(4*j)] = new Peca(new int[] {i,j,0},true,buffer_id);
				g.drawImage(sub_images[0][buffer_id], null, 25*j, 25*i);
				
				buffer_pos[0] = 7-i;
				pecas[0][65+(16*i)+(4*j)] = new Peca(new int[] {7-i,j,0},true,buffer_id);
				g.drawImage(sub_images[0][buffer_id], null, 25*j, 25*(7-i));
				
				buffer_pos[1] = 7-j;
				pecas[0][66+(16*i)+(4*j)] = new Peca(new int[] {7-i,7-j,0},true,buffer_id);
				g.drawImage(sub_images[0][buffer_id], null, 25*(7-j), 25*(7-i));
				
				buffer_pos[0] = i;
				pecas[0][67+(16*i)+(4*j)] = new Peca(new int[] {i,7-j,0},true,buffer_id);
				g.drawImage(sub_images[0][buffer_id], null, 25*(7-j), 25*i);
			}
		}
		
		buffer_pos[2] = 7;
		for (int i = 0; i < 4; i++) {
			for (int j = 0; j < 4; j++) {
				buffer_id = qualPeca(i+1,j+1);
				
				buffer_pos[0] = i;
				buffer_pos[1] = j;
				pecas[1][64+(16*i)+(4*j)] = new Peca(new int[] {i,j,7},false,buffer_id);
				g.drawImage(sub_images[1][buffer_id], null, (25*j)+1575, 25*i);
				
				buffer_pos[0] = 7-i;
				pecas[1][65+(16*i)+(4*j)] = new Peca(new int[] {7-i,j,7},false,buffer_id);
				g.drawImage(sub_images[1][buffer_id], null, (25*j)+1575, 25*(7-i));
				
				buffer_pos[1] = 7-j;
				pecas[1][66+(16*i)+(4*j)] = new Peca(new int[] {7-i,7-j,7},false,buffer_id);
				g.drawImage(sub_images[1][buffer_id], null, (25*(7-j))+1575, 25*(7-i));
				
				buffer_pos[0] = i;
				pecas[1][67+(16*i)+(4*j)] = new Peca(new int[] {i,7-j,7},false,buffer_id);
				g.drawImage(sub_images[1][buffer_id], null, (25*(7-j))+1575, 25*i);
			}
		}
		
		//Colocando os Reis:
		pecas[0][127] = new Peca(new int[]{3,4,0},true,0);
		g.setColor(cor[1]);
		g.fillRect(100, 75, 25, 25);
		g.drawImage(sub_images[0][0], null, 100, 75);
		
		pecas[1][127] = new Peca(new int[]{3,4,7},false,0);
		g.setColor(cor[0]);
		g.fillRect(1675, 75, 25, 25);
		g.drawImage(sub_images[1][0], null, 1675, 75);
	}
	
	public int qualPeca(int linha, int coluna) {
		//Usa critérios "númericos" para determinar a posição inicial de cada peça
		int id = 2;
		int produto = linha*coluna;
		
		switch (produto) {
		case (1): id = 3; break;
		case (2): id = 6; break;
		case (3): id = 4; break;
		case (4): if (linha == coluna) {id = 7;}  else {id = 9;}  break;
		case (6): id = 8; break;
		case (8): id = 10; break;
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
