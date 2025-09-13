// ========================= src/model/pieces/Pawn.java =========================
package model.pieces;

import java.util.*;
import model.board.*;

public class Pawn extends Piece {

    public Pawn(Board b, boolean w) {
        super(b, w);
    }

    @Override
    public String getSymbol() {
        return "P";
    }

    @Override
    public Piece copyFor(Board newBoard) {
        Pawn clone = new Pawn(newBoard, isWhite);
        clone.moved = this.moved;
        if (this.position != null) {
            clone.setPosition(new Position(position.getRow(), position.getColumn()));
        }
        return clone;
    }

    @Override
    public List<Position> getPossibleMoves() {
        List<Position> moves = new ArrayList<>();
        int dir = isWhite ? -1 : 1;

        // Um passo à frente
        Position f1 = new Position(position.getRow() + dir, position.getColumn());
        if (f1.isValid() && board.get(f1) == null) {
            moves.add(f1);

            // Dois passos à frente (se ainda não moveu)
            Position f2 = new Position(position.getRow() + 2 * dir, position.getColumn());
            if (!moved && f2.isValid() && board.get(f2) == null) {
                moves.add(f2);
            }
        }

        // Capturas diagonais
        Position left = new Position(position.getRow() + dir, position.getColumn() - 1);
        Position right = new Position(position.getRow() + dir, position.getColumn() + 1);

        if (left.isValid()) {
            Piece target = board.get(left);
            if (target != null && target.isWhite() != isWhite) {
                moves.add(left);
            }
        }
        if (right.isValid()) {
            Piece target = board.get(right);
            if (target != null && target.isWhite() != isWhite) {
                moves.add(right);
            }
        }

        // Obs: En passant tratado no Game
        return moves;
    }

    @Override
    public List<Position> getAttacks() {
        List<Position> attacks = new ArrayList<>();
        int dir = isWhite ? -1 : 1;

        Position left = new Position(position.getRow() + dir, position.getColumn() - 1);
        Position right = new Position(position.getRow() + dir, position.getColumn() + 1);

        if (left.isValid()) attacks.add(left);
        if (right.isValid()) attacks.add(right);

        return attacks;
    }
}
