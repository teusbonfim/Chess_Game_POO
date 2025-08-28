package model.board;
import model.pieces.Piece;
public class Move {
private Position from;
private Position to;
private Piece piece;
private Piece capturedPiece;
private boolean isPromotion;
private boolean isCastling;
private boolean isEnPassant;
public Move(Position from, Position to, Piece piece, Piece capturedPiece) {
this.from = from;
this.to = to;
this.piece = piece;
this.capturedPiece = capturedPiece;
this.isPromotion = false;
this.isCastling = false;
this.isEnPassant = false;
}
// Getters e setters
public Position getFrom() {
return from;
}
public Position getTo() {
return to;
}
public Piece getPiece() {
return piece;
}
public Piece getCapturedPiece() {
return capturedPiece;
}
public boolean isPromotion() {
return isPromotion;
}
public void setPromotion(boolean promotion) {
isPromotion = promotion;
}
public boolean isCastling() {
return isCastling;
}
public void setCastling(boolean castling) {
isCastling = castling;
}
public boolean isEnPassant() {
return isEnPassant;
}
public void setEnPassant(boolean enPassant) {
isEnPassant = enPassant;
}
@Override
public String toString() {
return piece.getSymbol() + from.toString() + "-" + to.toString();
}
}