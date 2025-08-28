package model.board;

import model.pieces.Piece;

public class Board {
    private Piece[][] pieces = new Piece[8][8];
    
    public Piece getPieceAt(Position pos) {
        if (!pos.isValid()) return null;
        return pieces[pos.getRow()][pos.getColumn()];
    }
    
    public void placePiece(Piece piece, Position pos) {
        if (!pos.isValid()) return;
        pieces[pos.getRow()][pos.getColumn()] = piece;
        if (piece != null) piece.setPosition(pos);
    }
    
    public void removePiece(Position pos) {
        if (!pos.isValid()) return;
        pieces[pos.getRow()][pos.getColumn()] = null;
    }
    
    public boolean isPositionEmpty(Position pos) {
        return getPieceAt(pos) == null;
    }
}
