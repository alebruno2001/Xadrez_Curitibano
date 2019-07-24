package xadrez;

public class Peca {
	
	int[] posicao = new int[3]; //posição = [y,x,z] = [linha,coluna,plano] , 0 <= x,y,z <= 7
	boolean isWhite;
	int id;
	/* 0 - Rei
	 * 1 - Príncipe
	 * 2 - Peão
	 * 3 - Torre
	 * 4 - Padre
	 * 5 - Bispo
	 * 6 - Cavalo
	 * 7 - Elefante
	 * 8 - Águia
	 * 9 - Marquês
	 * 10- Conde
	 * 11- Duque
	 */
	
	public Peca(int[] position, boolean white, int type) {
		posicao = position;
		isWhite = white;
		id = type;
	}
}
