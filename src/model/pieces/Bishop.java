// ========================= src/model/pieces/Bishop.java =========================
package model.pieces;

import java.util.ArrayList;
import java.util.List;
import model.board.Board;
import model.board.Position;

public class Bishop extends Piece {

    public Bishop(Board b, boolean w) { super(b, w); }

    @Override
    public String getSymbol() { return "B"; }

    @Override
    public Piece copyFor(Board newBoard) {
        Bishop clone = new Bishop(newBoard, isWhite);
        clone.moved = this.moved;
        if (this.position != null) {
            clone.setPosition(new Position(this.position.getRow(), this.position.getColumn()));
        }
        return clone;
    }

    @Override
    public List<Position> getPossibleMoves() {
        List<Position> moves = new ArrayList<>();
        if (position == null) return moves;

        // Quatro diagonais
        addRay(moves, -1, -1); // noroeste
        addRay(moves, -1,  1); // nordeste
        addRay(moves,  1, -1); // sudoeste
        addRay(moves,  1,  1); // sudeste

        return moves;
    }

    private void addRay(List<Position> acc, int dRow, int dCol) {
        int r = position.getRow() + dRow;
        int c = position.getColumn() + dCol;

        while (r >= 0 && r < 8 && c >= 0 && c < 8) {
            Position to = new Position(r, c);
            Piece occ = board.get(to);

            if (occ == null) {
                acc.add(to);
            } else {
                if (occ.isWhite() != this.isWhite) {
                    acc.add(to); // pode capturar a primeira peça adversária
                }
                break; // bloqueia após encontrar qualquer peça
            }

            r += dRow;
            c += dCol;
        }
    }
}
