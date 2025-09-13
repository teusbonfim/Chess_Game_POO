// ========================= src/model/pieces/King.java =========================
package model.pieces;

import java.util.ArrayList;
import java.util.List;
import model.board.Board;
import model.board.Position;

public class King extends Piece {

    public King(Board b, boolean w) { super(b, w); }

    @Override
    public String getSymbol() { return "K"; }

    @Override
    public Piece copyFor(Board newBoard) {
        King k = new King(newBoard, isWhite);
        k.moved = this.moved;
        if (this.position != null) {
            k.setPosition(new Position(this.position.getRow(), this.position.getColumn()));
        }
        return k;
    }

    @Override
    public List<Position> getPossibleMoves() {
        List<Position> moves = new ArrayList<>();
        if (position == null || board == null) return moves;

        for (int dr = -1; dr <= 1; dr++) {
            for (int dc = -1; dc <= 1; dc++) {
                if (dr == 0 && dc == 0) continue;
                int r = position.getRow() + dr;
                int c = position.getColumn() + dc;
                if (r < 0 || r > 7 || c < 0 || c > 7) continue;

                Position to = new Position(r, c);
                Piece occ = board.get(to);
                if (occ == null || occ.isWhite() != this.isWhite) {
                    moves.add(to);
                }
            }
        }

        // Roques são tratados no controller.Game (candidatos adicionados lá)
        return moves;
    }

    /**
     * Opcional: casas atacadas pelo rei (as 8 adjacentes).
     * Útil se quiser consultar ataques por peça diretamente.
     */
    @Override
    public List<Position> getAttacks() {
        List<Position> attacks = new ArrayList<>();
        if (position == null) return attacks;

        for (int dr = -1; dr <= 1; dr++) {
            for (int dc = -1; dc <= 1; dc++) {
                if (dr == 0 && dc == 0) continue;
                int r = position.getRow() + dr;
                int c = position.getColumn() + dc;
                if (r < 0 || r > 7 || c < 0 || c > 7) continue;
                attacks.add(new Position(r, c));
            }
        }
        return attacks;
    }
}
