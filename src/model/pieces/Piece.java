// ========================= src/model/pieces/Piece.java =========================
package model.pieces;


import model.board.Board;
import model.board.Position;
import java.util.*;


public abstract class Piece {
protected Position position;
protected final boolean isWhite;
protected final Board board;
protected boolean moved = false;


public Piece(Board board, boolean isWhite) {
this.board = board; this.isWhite = isWhite;
}


public boolean isWhite(){ return isWhite; }
public Position getPosition(){ return position; }
public void setPosition(Position position){ this.position = position; }
public boolean hasMoved(){ return moved; }
public void setMoved(boolean moved){ this.moved = moved; }


// Pseudo-movimentos (não filtram xeque ao próprio rei)
public abstract List<Position> getPossibleMoves();
// Casas atacadas (para peão difere dos possíveis)
public List<Position> getAttacks(){ return getPossibleMoves(); }


public abstract String getSymbol(); // K,Q,R,B,N,P


// Fábrica de cópia para outro board
public abstract Piece copyFor(Board newBoard);


protected boolean empty(int r, int c){ return new Position(r,c).isValid() && board.get(new Position(r,c))==null; }
protected boolean enemy(int r, int c){
Position p = new Position(r,c);
if(!p.isValid()) return false; Piece q = board.get(p);
return q!=null && q.isWhite()!=this.isWhite;
}
protected void addIfFreeOrEnemy(List<Position> list, int r, int c){
Position p = new Position(r,c); if(!p.isValid()) return;
var q = board.get(p); if(q==null || q.isWhite()!=this.isWhite) list.add(p);
}
}