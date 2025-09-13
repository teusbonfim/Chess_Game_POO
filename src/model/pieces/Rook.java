// ========================= src/model/pieces/Rook.java =========================
package model.pieces;

import java.util.ArrayList;
import java.util.List;
import model.board.Board;
import model.board.Position;

public class Rook extends Piece {

    public Rook(Board board, boolean isWhite) {
        super(board, isWhite);
    }

    @Override
    public String getSymbol() {
        return "R";
    }

    /** Movimentos possíveis: ortogonais até bloquear (captura a 1ª peça adversária e para). */
    @Override
    public List<Position> getPossibleMoves() {
        List<Position> moves = new ArrayList<>();
        Position from = getPosition();
        if (from == null) return moves;

        // Quatro raios ortogonais
        addRay(moves, from, -1,  0); // cima
        addRay(moves, from,  1,  0); // baixo
        addRay(moves, from,  0, -1); // esquerda
        addRay(moves, from,  0,  1); // direita
        return moves;
    }

    /** Necessário para Board.copy(): clona a peça preservando cor/estado e (opcional) posição. */
    @Override
    public Piece copyFor(Board newBoard) {
        Rook clone = new Rook(newBoard, this.isWhite());
        clone.moved = this.moved; // importante para roque
        if (this.position != null) {
            clone.setPosition(new Position(this.position.getRow(), this.position.getColumn()));
        }
        return clone;
    }

    private void addRay(List<Position> acc, Position from, int dRow, int dCol) {
        int r = from.getRow();
        int c = from.getColumn();

        while (true) {
            r += dRow;
            c += dCol;

            // limites do tabuleiro
            if (r < 0 || r > 7 || c < 0 || c > 7) break;

            Position to = new Position(r, c);
            Piece occ = board.get(to);

            if (occ == null) {
                acc.add(to);
            } else {
                if (occ.isWhite() != this.isWhite()) {
                    acc.add(to); // pode capturar a primeira peça adversária
                }
                break; // bloqueia após encontrar qualquer peça
            }
        }
    }
}
