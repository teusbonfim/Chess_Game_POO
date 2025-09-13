// ========================= src/model/board/Board.java =========================
package model.board;

import java.util.ArrayList;
import java.util.List;
import model.pieces.Piece;

public class Board {

    private final Piece[][] grid = new Piece[8][8];

    /** Verifica se a posição está dentro do tabuleiro (0..7). */
    public boolean isInside(Position p) {
        return p != null && p.isValid();
    }

    /** Retorna a peça na posição ou null se vazio/fora. */
    public Piece get(Position p) {
        return isInside(p) ? grid[p.getRow()][p.getColumn()] : null;
    }

    /**
     * Define a peça na posição (substitui o que houver).
     * Não valida legalidade de movimento — responsabilidade da lógica de jogo.
     */
    public void set(Position p, Piece piece) {
        if (!isInside(p)) return;
        grid[p.getRow()][p.getColumn()] = piece;
        if (piece != null) {
            // Mantém referência de posição da peça sincronizada
            piece.setPosition(p);
        }
    }

    /** Remove e retorna a peça da posição (ou null). */
    public Piece remove(Position p) {
        if (!isInside(p)) return null;
        Piece old = grid[p.getRow()][p.getColumn()];
        grid[p.getRow()][p.getColumn()] = null;
        return old;
    }

    /** Retorna true se a posição estiver vazia. */
    public boolean isEmpty(Position p) {
        return get(p) == null;
    }

    /** Atalho usado no setup inicial. */
    public void placePiece(Piece piece, Position p) {
        set(p, piece);
    }

    /** Limpa completamente o tabuleiro. */
    public void clear() {
        for (int r = 0; r < 8; r++) {
            for (int c = 0; c < 8; c++) {
                grid[r][c] = null;
            }
        }
    }

    /** Lista todas as peças de uma cor. */
    public List<Piece> pieces(boolean white) {
        List<Piece> out = new ArrayList<>();
        for (int r = 0; r < 8; r++) {
            for (int c = 0; c < 8; c++) {
                Piece pc = grid[r][c];
                if (pc != null && pc.isWhite() == white) out.add(pc);
            }
        }
        return out;
    }

    /** Alias conveniente (evita divergência de nomes em outras classes). */
    public List<Piece> getPieces(boolean white) {
        return pieces(white);
    }

    /**
     * Cópia profunda do tabuleiro (clona peças para o novo Board).
     * Requer que Piece.copyFor(b) crie uma nova peça já associada ao Board b,
     * preservando cor/estado (ex.: moved) e que aqui definimos a Position corretamente.
     */
    public Board copy() {
        Board b = new Board();
        for (int r = 0; r < 8; r++) {
            for (int c = 0; c < 8; c++) {
                Piece p = grid[r][c];
                if (p != null) {
                    Piece cp = p.copyFor(b);                 // nova peça ligada ao Board "b"
                    b.grid[r][c] = cp;                       // atribui diretamente (evita set() duplicado)
                    cp.setPosition(new Position(r, c));      // sincroniza a posição do clone
                }
            }
        }
        return b;
    }
}
