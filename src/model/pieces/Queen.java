package model.pieces;
import model.board.Board;
import model.board.Position;
import java.util.ArrayList;
import java.util.List;
public class Queen extends Piece {
public Queen(Board board, boolean isWhite) {
super(board, isWhite);
}
@Override
public List getPossibleMoves() {
List moves = new ArrayList<>();
// Combina as direções da torre e do bispo
int[][] directions = {
{-1, 0}, {0, 1}, {1, 0}, {0, -1}, // Torre
{-1, -1}, {-1, 1}, {1, -1}, {1, 1} // Bispo
};
for (int[] direction : directions) {
int row = position.getRow();
int col = position.getColumn();
while (true) {
row += direction[0];
col += direction[1];
Position newPos = new Position(row, col);
if (!newPos.isValid()) break;
Piece pieceAt = board.getPieceAt(newPos);
if (pieceAt == null) {
moves.add(newPos);
} else if (pieceAt.isWhite() != isWhite) {
moves.add(newPos);
break;
} else {
break;
}
}
}
return moves;
}
@Override
public String getSymbol() {
return "Q";
}
}