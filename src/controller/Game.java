package controller;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import model.board.Board;
import model.board.Position;
import model.pieces.*;

public class Game {

    private Board board;
    private boolean whiteToMove = true;
    private boolean gameOver = false;

    // Square where an en-passant capture may land (the empty square)
    private Position enPassantTarget = null;

    private final List<String> history = new ArrayList<>();

    // Public ctor (starts a fresh game)
    public Game() {
        this.board = new Board();
        setupPieces();
    }

    // Private ctor used for snapshots (no setup)
    private Game(boolean empty) { /* intentionally empty */ }

    // --------- Public getters ----------
    public Board board() { return board; }
    public boolean whiteToMove() { return whiteToMove; }
    public boolean isGameOver() { return gameOver; }
    public List<String> history() { return Collections.unmodifiableList(history); }

    // --------- New game ----------
    public void newGame() {
        this.board = new Board();
        this.whiteToMove = true;
        this.gameOver = false;
        this.enPassantTarget = null;
        this.history.clear();
        setupPieces();
    }

    // --------- Query legal moves ----------
    // Full legality including specials and "king safety"
    public List<Position> legalMovesFrom(Position from) {
        return legalMovesFromWithSpecials(from);
    }

    public boolean isPromotion(Position from, Position to) {
        Piece p = board.get(from);
        if (!(p instanceof Pawn)) return false;
        return p.isWhite() ? to.getRow() == 0 : to.getRow() == 7;
    }

    // --------- Make a move (only if legal) ----------
    public void move(Position from, Position to, Character promotion) {
        if (gameOver) return;

        Piece p = board.get(from);
        if (p == null || p.isWhite() != whiteToMove) return;

        // Enforce legality (includes castling & en passant & king-safety)
        List<Position> legal = legalMovesFromWithSpecials(from);
        if (!legal.contains(to)) return;

        boolean isKing = p instanceof King;
        boolean isPawn = p instanceof Pawn;
        int dCol = Math.abs(to.getColumn() - from.getColumn());

        Piece capturedBefore = board.get(to); // for SAN-ish history
        boolean targetIsKing = (capturedBefore instanceof King);

        // ------- Castling (already validated in legal moves) -------
        if (isKing && dCol == 2) {
            int row = from.getRow();
            // Move king
            board.set(to, p);
            board.set(from, null);
            p.setMoved(true);

            String san;
            if (to.getColumn() == 6) {
                // Short castle: rook h->f
                Piece rook = board.get(new Position(row, 7));
                board.set(new Position(row, 5), rook);
                board.set(new Position(row, 7), null);
                if (rook != null) rook.setMoved(true);
                san = "O-O";
            } else {
                // Long castle: rook a->d
                Piece rook = board.get(new Position(row, 0));
                board.set(new Position(row, 3), rook);
                board.set(new Position(row, 0), null);
                if (rook != null) rook.setMoved(true);
                san = "O-O-O";
            }

            enPassantTarget = null;
            // Switch side
            whiteToMove = !whiteToMove;

            // annotate + or #
            if (isCheckmate(whiteToMove)) {
                san += "#";
                gameOver = true;
            } else if (inCheck(whiteToMove)) {
                san += "+";
            }
            addHistory(san);

            if (!gameOver) checkGameEnd();
            return;
        }

        // ------- En Passant (already validated in legal moves) -------
        boolean diagonal = from.getColumn() != to.getColumn();
        boolean toIsEmpty = board.get(to) == null;
        boolean isEnPassant = isPawn && diagonal && toIsEmpty && to.equals(enPassantTarget);

        String moveStr;

        if (isEnPassant) {
            // Move pawn to target
            board.set(to, p);
            board.set(from, null);
            // Remove the pawn that moved two squares last turn (victim behind target)
            int dir = p.isWhite() ? 1 : -1;
            Position victim = new Position(to.getRow() + dir, to.getColumn());
            board.set(victim, null);
            p.setMoved(true);
            moveStr = coord(from) + "x" + coord(to) + " e.p.";
            enPassantTarget = null;

            // Switch side
            whiteToMove = !whiteToMove;

            // annotate + or #
            if (isCheckmate(whiteToMove)) {
                moveStr += "#";
                gameOver = true;
            } else if (inCheck(whiteToMove)) {
                moveStr += "+";
            }
            addHistory(moveStr);

            if (!gameOver) checkGameEnd();
            return;
        }

        // ------- Promotion (auto-queen if promotion is null) -------
        if (isPawn && isPromotion(from, to)) {
            char ch = (promotion == null) ? 'Q' : Character.toUpperCase(promotion);
            Piece np = switch (ch) {
                case 'R' -> new Rook(board, p.isWhite());
                case 'B' -> new Bishop(board, p.isWhite());
                case 'N' -> new Knight(board, p.isWhite());
                default  -> new Queen(board, p.isWhite());
            };
            np.setMoved(true);
            board.set(from, null);
            board.set(to, np);

            // >>> segurança: se, por algum motivo, havia um Rei na casa alvo (não deveria), termina
            if (targetIsKing) {
                String san = coord(from) + "x" + coord(to) + "=" + np.getSymbol() + "#";
                addHistory(san);
                gameOver = true;
                return;
            }

            moveStr = coord(from) + (capturedBefore != null ? "x" : "-") + coord(to) + "=" + np.getSymbol();
        } else {
            // Normal move / capture
            board.set(to, p);
            board.set(from, null);
            p.setMoved(true);

            // >>> segurança: se capturamos um Rei (não deveria acontecer), termina imediatamente
            if (targetIsKing) {
                String san = coord(from) + "x" + coord(to) + "#";
                addHistory(san);
                gameOver = true;
                return;
            }

            moveStr = coord(from) + (capturedBefore != null ? "x" : "-") + coord(to);
        }

        // ------- En-passant availability after a double pawn push -------
        if (isPawn && Math.abs(to.getRow() - from.getRow()) == 2) {
            int mid = (to.getRow() + from.getRow()) / 2;
            enPassantTarget = new Position(mid, from.getColumn());
        } else {
            enPassantTarget = null;
        }

        // Switch side
        whiteToMove = !whiteToMove;

        // annotate + or #
        if (isCheckmate(whiteToMove)) {
            moveStr += "#";
            gameOver = true;
        } else if (inCheck(whiteToMove)) {
            moveStr += "+";
        }

        addHistory(moveStr);
        if (!gameOver) checkGameEnd();
    }

    // --------- Checks / mates ----------
    public boolean inCheck(boolean whiteSide) {
        Position k = findKing(whiteSide);
        // Se o rei não existe no tabuleiro, trate como "em xeque" (estado inválido/terminal).
        if (k == null) return true;
        return isSquareAttacked(k, whiteSide);
    }

    public boolean isCheckmate(boolean whiteSide) {
        if (!inCheck(whiteSide)) return false;

        // If the side has any legal move that avoids check, it's not mate
        for (int row = 0; row < 8; row++) {
            for (int col = 0; col < 8; col++) {
                Position from = new Position(row, col);
                Piece piece = board.get(from);
                if (piece != null && piece.isWhite() == whiteSide) {
                    for (Position to : legalMovesFromWithSpecials(from)) {
                        Game g = snapshotShallow();
                        g.forceMoveNoChecks(from, to);
                        if (!g.inCheck(whiteSide)) return false;
                    }
                }
            }
        }
        return true;
    }

    private void checkGameEnd() {
        // Checkmate
        if (isCheckmate(whiteToMove)) {
            gameOver = true;
            addHistory("Checkmate: " + (whiteToMove ? "White" : "Black") + " loses");
            return;
        }

        // Stalemate: no legal moves and not in check
        if (!inCheck(whiteToMove)) {
            boolean hasAny = false;
            for (int r = 0; r < 8 && !hasAny; r++) {
                for (int c = 0; c < 8 && !hasAny; c++) {
                    Position from = new Position(r, c);
                    Piece piece = board.get(from);
                    if (piece != null && piece.isWhite() == whiteToMove) {
                        if (!legalMovesFromWithSpecials(from).isEmpty()) {
                            hasAny = true;
                        }
                    }
                }
            }
            if (!hasAny) {
                gameOver = true;
                addHistory("Draw: stalemate");
            }
        }
    }

    // --------- Helpers: legality & attack maps ----------
    private List<Position> legalMovesFromWithSpecials(Position from) {
        Piece p = board.get(from);
        if (p == null || p.isWhite() != whiteToMove) return List.of();

        List<Position> moves = new ArrayList<>(p.getPossibleMoves());

        // En Passant candidate square
        if (p instanceof Pawn && enPassantTarget != null) {
            int dir = p.isWhite() ? -1 : 1; // white pawns go up (row--), so attack is -1
            if (from.getRow() + dir == enPassantTarget.getRow()
                    && Math.abs(from.getColumn() - enPassantTarget.getColumn()) == 1) {
                // Ensure there is an enemy pawn on the square behind target
                Piece victim = board.get(new Position(enPassantTarget.getRow() - dir, enPassantTarget.getColumn()));
                if (victim instanceof Pawn && victim.isWhite() != p.isWhite()) {
                    moves.add(enPassantTarget);
                }
            }
        }

        // Castling candidates (king not moved, not in check, path empty, pass squares not attacked)
        if (p instanceof King && !p.hasMoved() && !inCheck(p.isWhite())) {
            int row = from.getRow();
            // Short castle to g-file (col 6)
            if (canCastle(row, 4, 7, 5, 6, p.isWhite())) moves.add(new Position(row, 6));
            // Long castle to c-file (col 2)
            if (canCastle(row, 4, 0, 3, 2, p.isWhite())) moves.add(new Position(row, 2));
        }

        // >>> NUNCA permitir "capturar" Rei inimigo
        moves.removeIf(to -> {
            Piece tgt = board.get(to);
            return (tgt instanceof King) && (tgt.isWhite() != p.isWhite());
        });

        // Filter out moves que deixam o próprio rei em xeque
        moves.removeIf(to -> leavesKingInCheck(from, to));
        return moves;
    }

    private boolean canCastle(int row, int kingCol, int rookCol, int passCol1, int passCol2, boolean whiteSide) {
        Piece rook = board.get(new Position(row, rookCol));
        if (!(rook instanceof Rook) || rook.hasMoved()) return false;

        // Path between king and rook must be empty
        int step = (rookCol > kingCol) ? 1 : -1;
        for (int c = kingCol + step; c != rookCol; c += step) {
            if (board.get(new Position(row, c)) != null) return false;
        }

        // Squares king passes through (and destination) must not be attacked
        Position p1 = new Position(row, passCol1);
        Position p2 = new Position(row, passCol2);
        if (isSquareAttacked(p1, whiteSide) || isSquareAttacked(p2, whiteSide)) return false;

        return true;
    }

    private boolean leavesKingInCheck(Position from, Position to) {
        Piece mover = board.get(from);
        if (mover == null) return true;

        Game g = snapshotShallow();
        g.forceMoveNoChecks(from, to);
        return g.inCheck(mover.isWhite());
    }

    /**
     * True se `sq` está atacada por QUALQUER peça do lado oposto a `sideToProtect`.
     * Implementa padrões de ataque corretos para peão/cavalo/rei/deslizantes.
     */
    private boolean isSquareAttacked(Position sq, boolean sideToProtect) {
        int r = sq.getRow(), c = sq.getColumn();

        // 1) Ataques de peão (peão inimigo estaria uma linha "atrás" da sq na direção dele)
        int dir = sideToProtect ? -1 : 1; // protegendo brancas => peões pretos atacam +1 (descendo)
        int rp = r - dir;
        if (rp >= 0 && rp < 8) {
            if (c - 1 >= 0) {
                Piece p = board.get(new Position(rp, c - 1));
                if (p instanceof Pawn && p.isWhite() != sideToProtect) return true;
            }
            if (c + 1 < 8) {
                Piece p = board.get(new Position(rp, c + 1));
                if (p instanceof Pawn && p.isWhite() != sideToProtect) return true;
            }
        }

        // 2) Ataques de cavalo
        int[][] KJUMPS = {{-2,-1},{-2,1},{-1,-2},{-1,2},{1,-2},{1,2},{2,-1},{2,1}};
        for (int[] d : KJUMPS) {
            int rr = r + d[0], cc = c + d[1];
            if (rr>=0 && rr<8 && cc>=0 && cc<8) {
                Piece p = board.get(new Position(rr, cc));
                if (p instanceof Knight && p.isWhite() != sideToProtect) return true;
            }
        }

        // 3) Ataques do rei (adjacentes)
        for (int dr=-1; dr<=1; dr++) for (int dc=-1; dc<=1; dc++) {
            if (dr==0 && dc==0) continue;
            int rr = r+dr, cc = c+dc;
            if (rr>=0 && rr<8 && cc>=0 && cc<8) {
                Piece p = board.get(new Position(rr, cc));
                if (p instanceof King && p.isWhite() != sideToProtect) return true;
            }
        }

        // 4) Deslizantes: torre/rainha (linhas/colunas)
        int[][] ROOK_DIRS = {{-1,0},{1,0},{0,-1},{0,1}};
        for (int[] d : ROOK_DIRS) {
            int rr = r + d[0], cc = c + d[1];
            while (rr>=0 && rr<8 && cc>=0 && cc<8) {
                Piece p = board.get(new Position(rr, cc));
                if (p != null) {
                    if (p.isWhite() != sideToProtect && (p instanceof Rook || p instanceof Queen)) return true;
                    break;
                }
                rr += d[0]; cc += d[1];
            }
        }

        // 5) Deslizantes: bispo/rainha (diagonais)
        int[][] BISHOP_DIRS = {{-1,-1},{-1,1},{1,-1},{1,1}};
        for (int[] d : BISHOP_DIRS) {
            int rr = r + d[0], cc = c + d[1];
            while (rr>=0 && rr<8 && cc>=0 && cc<8) {
                Piece p = board.get(new Position(rr, cc));
                if (p != null) {
                    if (p.isWhite() != sideToProtect && (p instanceof Bishop || p instanceof Queen)) return true;
                    break;
                }
                rr += d[0]; cc += d[1];
            }
        }

        return false;
    }

    // Executes a move on this.board without doing legality checks or specials.
    // Used only inside snapshots where the move was already validated.
    private void forceMoveNoChecks(Position from, Position to) {
        Piece p = board.get(from);
        if (p == null) return;

        int dCol = Math.abs(to.getColumn() - from.getColumn());
        boolean isPawn = p instanceof Pawn;
        boolean isKing = p instanceof King;

        // Detect en passant: pawn moves diagonally onto empty square that equals enPassantTarget
        boolean diagonal = from.getColumn() != to.getColumn();
        boolean toIsEmpty = board.get(to) == null;
        boolean ep = isPawn && diagonal && toIsEmpty && enPassantTarget != null && to.equals(enPassantTarget);

        // Detect castling: king moves two columns
        boolean castle = isKing && dCol == 2;

        // Base move
        board.set(to, p);
        board.set(from, null);
        p.setMoved(true);

        // Apply en passant capture
        if (ep) {
            int dir = p.isWhite() ? 1 : -1; // victim behind target
            Position victim = new Position(to.getRow() + dir, to.getColumn());
            board.set(victim, null);
        }

        // Apply rook move for castling
        if (castle) {
            int row = to.getRow();
            if (to.getColumn() == 6) {
                // O-O: rook h -> f
                Piece rook = board.get(new Position(row, 7));
                board.set(new Position(row, 5), rook);
                board.set(new Position(row, 7), null);
                if (rook != null) rook.setMoved(true);
            } else if (to.getColumn() == 2) {
                // O-O-O: rook a -> d
                Piece rook = board.get(new Position(row, 0));
                board.set(new Position(row, 3), rook);
                board.set(new Position(row, 0), null);
                if (rook != null) rook.setMoved(true);
            }
        }

        // For snapshot simulation we don't keep EP availability
        enPassantTarget = null;
    }

    // --------- King location ----------
    private Position findKing(boolean whiteSide) {
        for (int row = 0; row < 8; row++) {
            for (int col = 0; col < 8; col++) {
                Position pos = new Position(row, col);
                Piece piece = board.get(pos);
                if (piece instanceof King && piece.isWhite() == whiteSide) {
                    return pos;
                }
            }
        }
        return null;
    }

    // --------- Snapshot ----------
    private Game snapshotShallow() {
        Game g = new Game(true);
        g.board = this.board.copy(); // IMPORTANT: Board.copy() must deep-copy pieces and fix their board refs.
        g.whiteToMove = this.whiteToMove;
        g.gameOver = this.gameOver;
        g.enPassantTarget = (this.enPassantTarget == null)
                ? null
                : new Position(this.enPassantTarget.getRow(), this.enPassantTarget.getColumn());
        g.history.addAll(this.history);
        return g;
    }

    // --------- Notation helpers ----------
    private void addHistory(String moveStr) {
        history.add(moveStr);
    }

    private String coord(Position p) {
        char file = (char) ('a' + p.getColumn());
        int rank = 8 - p.getRow();
        return "" + file + rank;
    }

    // --------- Initial setup ----------
    private void setupPieces() {
        // White back rank (row 7)
        board.placePiece(new Rook(board, true), new Position(7, 0));
        board.placePiece(new Knight(board, true), new Position(7, 1));
        board.placePiece(new Bishop(board, true), new Position(7, 2));
        board.placePiece(new Queen(board, true), new Position(7, 3));
        board.placePiece(new King(board, true), new Position(7, 4));
        board.placePiece(new Bishop(board, true), new Position(7, 5));
        board.placePiece(new Knight(board, true), new Position(7, 6));
        board.placePiece(new Rook(board, true), new Position(7, 7));
        // White pawns (row 6)
        for (int c = 0; c < 8; c++) {
            board.placePiece(new Pawn(board, true), new Position(6, c));
        }

        // Black back rank (row 0)
        board.placePiece(new Rook(board, false), new Position(0, 0));
        board.placePiece(new Knight(board, false), new Position(0, 1));
        board.placePiece(new Bishop(board, false), new Position(0, 2));
        board.placePiece(new Queen(board, false), new Position(0, 3));
        board.placePiece(new King(board, false), new Position(0, 4));
        board.placePiece(new Bishop(board, false), new Position(0, 5));
        board.placePiece(new Knight(board, false), new Position(0, 6));
        board.placePiece(new Rook(board, false), new Position(0, 7));
        // Black pawns (row 1)
        for (int c = 0; c < 8; c++) {
            board.placePiece(new Pawn(board, false), new Position(1, c));
        }
    }
}
