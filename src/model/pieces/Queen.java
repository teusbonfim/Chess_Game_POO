package model.pieces;

import java.util.ArrayList;
import java.util.List;
import model.board.Board;
import model.board.Position;

public class Queen extends Piece {

    public Queen(Board board, boolean isWhite) {
        super(board, isWhite);
    }

    @Override
    public String getSymbol() {
        return "Q";
    }

    @Override
    public List<Position> getPossibleMoves() {
        List<Position> moves = new ArrayList<>();
        if (position == null || board == null) return moves;

        // Torre (4 direções)
        addRay(moves, -1,  0); // cima
        addRay(moves,  1,  0); // baixo
        addRay(moves,  0, -1); // esquerda
        addRay(moves,  0,  1); // direita

        // Bispo (4 diagonais)
        addRay(moves, -1, -1); // noroeste
        addRay(moves, -1,  1); // nordeste
        addRay(moves,  1, -1); // sudoeste
        addRay(moves,  1,  1); // sudeste

        return moves;
    }

    @Override
    public Piece copyFor(Board newBoard) {
        Queen clone = new Queen(newBoard, this.isWhite);
        clone.moved = this.moved;
        if (this.position != null) {
            clone.setPosition(new Position(this.position.getRow(), this.position.getColumn()));
        }
        return clone;
    }

    private void addRay(List<Position> out, int dRow, int dCol) {
        int r = position.getRow() + dRow;
        int c = position.getColumn() + dCol;

        while (r >= 0 && r < 8 && c >= 0 && c < 8) {
            Position to = new Position(r, c);
            Piece occ = board.get(to);

            if (occ == null) {
                out.add(to);
            } else {
                if (occ.isWhite() != this.isWhite) {
                    out.add(to); // captura a primeira peça adversária no raio
                }
                break; // bloqueia após encontrar qualquer peça
            }

            r += dRow;
            c += dCol;
        }
    }
}
