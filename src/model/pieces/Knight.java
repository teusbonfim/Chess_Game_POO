// ========================= src/model/pieces/Knight.java =========================
package model.pieces;

import java.util.*;
import model.board.*;

public class Knight extends Piece {

    public Knight(Board b, boolean w) { super(b, w); }

    @Override
    public String getSymbol() { return "N"; }

    @Override
    public Piece copyFor(Board newBoard) {
        Knight clone = new Knight(newBoard, isWhite);
        clone.moved = this.moved;
        if (this.position != null) {
            clone.setPosition(new Position(this.position.getRow(), this.position.getColumn()));
        }
        return clone;
    }

    @Override
    public List<Position> getPossibleMoves() {
        List<Position> moves = new ArrayList<>();
        if (position == null || board == null) return moves;

        int[][] jumps = {
            {-2,-1},{-2,1},{-1,-2},{-1,2},
            { 1,-2},{ 1,2},{ 2,-1},{ 2,1}
        };

        for (int[] d : jumps) {
            int r = position.getRow() + d[0];
            int c = position.getColumn() + d[1];
            if (r < 0 || r > 7 || c < 0 || c > 7) continue;

            Position to = new Position(r, c);
            Piece occ = board.get(to);
            if (occ == null || occ.isWhite() != this.isWhite) {
                moves.add(to);
            }
        }
        return moves;
    }
}
