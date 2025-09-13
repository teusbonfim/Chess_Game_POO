// ========================= src/model/board/Position.java =========================
package model.board;

import java.util.Objects;

public final class Position {

    private final int row;    // 0..7 (0 = topo / linha 8, 7 = fundo / linha 1)
    private final int column; // 0..7 (0 = 'a', 7 = 'h')

    public Position(int row, int column) {
        this.row = row;
        this.column = column;
    }

    public int getRow() { return row; }
    public int getColumn() { return column; }

    /** Retorna true se a posição estiver dentro do tabuleiro 8x8. */
    public boolean isValid() {
        return row >= 0 && row < 8 && column >= 0 && column < 8;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Position)) return false;
        Position that = (Position) o;
        return row == that.row && column == that.column;
    }

    @Override
    public int hashCode() {
        return Objects.hash(row, column);
    }

    /** Notação algébrica padrão (ex: a1, e4, h8). */
    @Override
    public String toString() {
        char file = (char) ('a' + column);
        int rank = 8 - row;
        return "" + file + rank;
    }
}
