package ai;

import controller.Game;
import model.board.Move;
import model.board.Position;
import model.pieces.Piece;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class IANivel3 implements IA {

    private static final int MAX_DEPTH = 3; // Profundidade máxima da busca

    @Override
    public Move makeMove(Game game) {
        List<Move> allLegalMoves = collectAllLegalMoves(game, game.whiteToMove());
        if (allLegalMoves.isEmpty()) {
            return null;
        }

        double bestScore = game.whiteToMove() ? Double.NEGATIVE_INFINITY : Double.POSITIVE_INFINITY;
        List<Move> bestMoves = new ArrayList<>();

        for (Move move : allLegalMoves) {
            Game gameCopy = game.snapshotShallow();
            Character promo = move.getPromotion();
            gameCopy.move(move.getFrom(), move.getTo(), promo);

            double score = minimax(gameCopy, MAX_DEPTH, Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY, !game.whiteToMove());

            if (game.whiteToMove()) { // Maximiza para as brancas
                if (score > bestScore) {
                    bestScore = score;
                    bestMoves.clear();
                    bestMoves.add(move);
                } else if (score == bestScore) {
                    bestMoves.add(move);
                }
            } else { // Minimiza para as pretas
                if (score < bestScore) {
                    bestScore = score;
                    bestMoves.clear();
                    bestMoves.add(move);
                } else if (score == bestScore) {
                    bestMoves.add(move);
                }
            }
        }

        Random random = new Random();
        return bestMoves.get(random.nextInt(bestMoves.size()));
    }

    private double minimax(Game game, int depth, double alpha, double beta, boolean maximizingPlayer) {
        if (depth == 0 || game.isGameOver()) {
            return evaluateBoard(game);
        }

        List<Move> allLegalMoves = collectAllLegalMoves(game, maximizingPlayer);

        if (maximizingPlayer) {
            double maxEval = Double.NEGATIVE_INFINITY;
            for (Move move : allLegalMoves) {
                Game gameCopy = game.snapshotShallow();
                Character promo = move.getPromotion();
                gameCopy.move(move.getFrom(), move.getTo(), promo);

                double eval = minimax(gameCopy, depth - 1, alpha, beta, false);
                maxEval = Math.max(maxEval, eval);
                alpha = Math.max(alpha, eval);
                if (beta <= alpha) {
                    break; // Poda
                }
            }
            return maxEval;
        } else {
            double minEval = Double.POSITIVE_INFINITY;
            for (Move move : allLegalMoves) {
                Game gameCopy = game.snapshotShallow();
                Character promo = move.getPromotion();
                gameCopy.move(move.getFrom(), move.getTo(), promo);

                double eval = minimax(gameCopy, depth - 1, alpha, beta, true);
                minEval = Math.min(minEval, eval);
                beta = Math.min(beta, eval);
                if (beta <= alpha) {
                    break; // Poda
                }
            }
            return minEval;
        }
    }

    private List<Move> collectAllLegalMoves(Game game, boolean whiteSide) {
        List<Move> moves = new ArrayList<>();
        for (int r = 0; r < 8; r++) {
            for (int c = 0; c < 8; c++) {
                Position from = new Position(r, c);
                Piece piece = game.board().get(from);
                if (piece != null && piece.isWhite() == whiteSide) {
                    for (Position to : game.legalMovesFrom(from)) {
                        Character promo = null;
                        if (piece instanceof model.pieces.Pawn && game.isPromotion(from, to)) {
                            promo = 'Q';
                        }
                        moves.add(new Move(from, to, piece, game.board().get(to), false, false, false, promo));
                    }
                }
            }
        }
        return moves;
    }

    private double evaluateBoard(Game game) {
        double score = 0;
        for (int r = 0; r < 8; r++) {
            for (int c = 0; c < 8; c++) {
                Position pos = new Position(r, c);
                Piece p = game.board().get(pos);
                if (p != null) {
                    int pieceValue = IAUtils.getPieceValue(p);
                    int positionBonus = IAUtils.getPositionBonus(p, pos);

                    if (p.isWhite()) {
                        score += pieceValue + positionBonus;
                    } else {
                        score -= pieceValue + positionBonus;
                    }
                }
            }
        }
        return score;
    }
}