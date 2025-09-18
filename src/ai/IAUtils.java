package ai;

import model.board.Position;
import model.pieces.*;

public class IAUtils {

    // Valores das peças
    public static int getPieceValue(Piece p) {
        if (p instanceof Pawn) return 100;
        if (p instanceof Knight) return 320;
        if (p instanceof Bishop) return 330;
        if (p instanceof Rook) return 500;
        if (p instanceof Queen) return 900;
        if (p instanceof King) return 20000;
        return 0;
    }

    // Bônus de posição
    public static int getPositionBonus(Piece p, Position pos) {
        int bonus = 0;
        int r = pos.getRow();
        int c = pos.getColumn();

        // Bônus para controle do centro
        if ((r == 3 || r == 4) && (c == 3 || c == 4)) {
            bonus += 10;
        } else if ((r >= 2 && r <= 5) && (c >= 2 && c <= 5)) {
            bonus += 4;
        }

        // Bônus para peões avançados
        if (p instanceof Pawn) {
            if (p.isWhite()) {
                bonus += (7 - r) * 5; // Mais pontos quanto mais perto do final
            } else {
                bonus += r * 5; // Mais pontos quanto mais perto do final
            }
        }

        return bonus;
    }
}