// ========================= src/model/board/Move.java =========================
package model.board;

import java.util.Objects;
import model.pieces.Piece;

public class Move {

    private final Position from;
    private final Position to;
    private final Piece moved;
    private final Piece captured;
    private final boolean castleKingSide;
    private final boolean castleQueenSide;
    private final boolean enPassant;
    private final Character promotion; // 'Q','R','B','N' ou null

    public Move(Position from, Position to, Piece moved, Piece captured,
                boolean castleKingSide, boolean castleQueenSide,
                boolean enPassant, Character promotion) {
        this.from = from;
        this.to = to;
        this.moved = moved;
        this.captured = captured;
        this.castleKingSide = castleKingSide;
        this.castleQueenSide = castleQueenSide;
        this.enPassant = enPassant;
        this.promotion = promotion;
    }

    // --- Getters ---
    public Position getFrom() { return from; }
    public Position getTo() { return to; }
    public Piece getMoved() { return moved; }
    public Piece getCaptured() { return captured; }
    public boolean isCastleKingSide() { return castleKingSide; }
    public boolean isCastleQueenSide() { return castleQueenSide; }
    public boolean isEnPassant() { return enPassant; }
    public Character getPromotion() { return promotion; }

    // --- Utilidades ---

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(moved != null ? moved.getSymbol() : "?");
        sb.append(from != null ? from : "?");
        sb.append(" -> ");
        sb.append(to != null ? to : "?");
        if (promotion != null) sb.append("=").append(promotion);
        if (castleKingSide) sb.append(" (O-O)");
        if (castleQueenSide) sb.append(" (O-O-O)");
        if (enPassant) sb.append(" e.p.");
        if (captured != null) sb.append(" x").append(captured.getSymbol());
        return sb.toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Move)) return false;
        Move other = (Move) o;
        return Objects.equals(from, other.from)
                && Objects.equals(to, other.to)
                && Objects.equals(moved, other.moved)
                && Objects.equals(promotion, other.promotion)
                && castleKingSide == other.castleKingSide
                && castleQueenSide == other.castleQueenSide
                && enPassant == other.enPassant;
    }

    @Override
    public int hashCode() {
        return Objects.hash(from, to, moved, promotion, castleKingSide, castleQueenSide, enPassant);
    }

    // --- Fábricas convenientes ---
    public static Move normal(Position from, Position to, Piece moved, Piece captured) {
        return new Move(from, to, moved, captured, false, false, false, null);
    }

    public static Move promotion(Position from, Position to, Piece moved, Piece captured, char promo) {
        return new Move(from, to, moved, captured, false, false, false, promo);
    }

    public static Move enPassant(Position from, Position to, Piece moved, Piece captured) {
        return new Move(from, to, moved, captured, false, false, true, null);
    }

    public static Move castle(Position from, Position to, Piece king, boolean kingSide) {
        return new Move(from, to, king, null,
                kingSide, !kingSide, false, null);
    }
}
