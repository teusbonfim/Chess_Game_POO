package model.pieces;

import model.board.Board;
import model.board.Position;
import java.util.ArrayList;
import java.util.List;

public class Rook extends Piece {
    public Rook(Board board, boolean isWhite) {
        super(board, isWhite);
    }
    
    @Override
    public List<Position> getPossibleMoves() {
        List<Position> moves = new ArrayList<>();
        int[][] directions = {{-1,0},{0,1},{1,0},{0,-1}};
        
        for (int[] dir : directions) {
            int row = position.getRow();
            int col = position.getColumn();
            while (true) {
                row += dir[0];
                col += dir[1];
                Position newPos = new Position(row,col);
                if (!newPos.isValid()) break;
                Piece pieceAt = board.getPieceAt(newPos);
                if (pieceAt == null) {
                    moves.add(newPos);
                } else if (pieceAt.isWhite() != isWhite) {
                    moves.add(newPos);
                    break;
                } else break;
            }
        }
        return moves;
    }
    
    @Override
    public String getSymbol() { return "R"; }
}
