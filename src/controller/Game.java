package controller;

import model.board.Board;
import model.board.Position;
import model.pieces.*;
import javax.swing.*;

public class Game {
    private Board board;
    private boolean isWhiteTurn;
    private Position lastPawnDoubleMove;

    public Game() {
        board = new Board();
        isWhiteTurn = true;
        setupPieces();
    }

    private void setupPieces() {
        board.placePiece(new Rook(board,true), new Position(7,0));
        board.placePiece(new Rook(board,true), new Position(7,7));
        board.placePiece(new Rook(board,false), new Position(0,0));
        board.placePiece(new Rook(board,false), new Position(0,7));
        // aqui adiciona os Reis, Peões, etc.
    }

    public Board getBoard() { return board; }
    public boolean isWhiteTurn() { return isWhiteTurn; }

    // ================== Movimentos especiais ==================
    
    // Movimento de peça (genérico + especiais)
    public void movePiece(Piece selectedPiece, Position originalPosition, Position destination) {
        
        // -------- Roque --------
        if (selectedPiece instanceof King &&
            Math.abs(destination.getColumn() - originalPosition.getColumn()) == 2) {

            int rookColumn = (destination.getColumn() > originalPosition.getColumn()) ? 7 : 0;
            int newRookColumn = (destination.getColumn() > originalPosition.getColumn())
                                  ? destination.getColumn() - 1
                                  : destination.getColumn() + 1;

            Position rookPosition = new Position(originalPosition.getRow(), rookColumn);
            Position newRookPosition = new Position(originalPosition.getRow(), newRookColumn);

            Piece rook = board.getPieceAt(rookPosition);
            board.removePiece(rookPosition);
            board.placePiece(rook, newRookPosition);
        }

        // -------- Movimento normal --------
        board.removePiece(originalPosition);
        board.placePiece(selectedPiece, destination);

        // -------- Promoção --------
        checkSpecialConditions(selectedPiece, destination);

        // -------- En Passant --------
        if (selectedPiece instanceof Pawn) {
            // movimento duplo
            if (Math.abs(destination.getRow() - originalPosition.getRow()) == 2) {
                lastPawnDoubleMove = destination;
            } else {
                // captura en passant
                if (Math.abs(destination.getColumn() - originalPosition.getColumn()) == 1 &&
                    board.getPieceAt(destination) == null) {
                    Position capturedPawnPos = new Position(originalPosition.getRow(), destination.getColumn());
                    board.removePiece(capturedPawnPos);
                }
                lastPawnDoubleMove = null;
            }
        } else {
            lastPawnDoubleMove = null;
        }

        // alterna turno
        isWhiteTurn = !isWhiteTurn;
    }

    // ================== Condições especiais ==================

    private void checkSpecialConditions(Piece piece, Position destination) {
        // -------- Promoção de Peão --------
        if (piece instanceof Pawn) {
            if ((piece.isWhite() && destination.getRow() == 0) ||
                (!piece.isWhite() && destination.getRow() == 7)) {

                String[] options = {"Rainha", "Torre", "Bispo", "Cavalo"};
                int choice = JOptionPane.showOptionDialog(null,
                        "Escolha uma peça para promoção:",
                        "Promoção de Peão",
                        JOptionPane.DEFAULT_OPTION,
                        JOptionPane.QUESTION_MESSAGE,
                        null, options, options[0]);

                Piece newPiece;
                switch (choice) {
                    case 1: newPiece = new Rook(board, piece.isWhite()); break;
                    case 2: newPiece = new Bishop(board, piece.isWhite()); break;
                    case 3: newPiece = new Knight(board, piece.isWhite()); break;
                    default: newPiece = new Queen(board, piece.isWhite());
                }

                board.removePiece(destination);
                board.placePiece(newPiece, destination);
            }
        }
    }

    // ================== Xeque e Xeque-mate ==================

    private boolean isInCheck(boolean whiteKing) {
        // encontrar posição do Rei
        Position kingPosition = null;
        for (int row = 0; row < 8; row++) {
            for (int col = 0; col < 8; col++) {
                Position pos = new Position(row, col);
                Piece piece = board.getPieceAt(pos);
                if (piece instanceof King && piece.isWhite() == whiteKing) {
                    kingPosition = pos;
                    break;
                }
            }
            if (kingPosition != null) break;
        }
        // verificar se rei está atacado
        return board.isUnderAttack(kingPosition, !whiteKing);
    }

    private boolean isCheckmate(boolean whiteKing) {
        if (!isInCheck(whiteKing)) {
            return false;
        }
        // verificar se alguma peça pode se mover para sair do xeque
        for (int row = 0; row < 8; row++) {
            for (int col = 0; col < 8; col++) {
                Position pos = new Position(row, col);
                Piece piece = board.getPieceAt(pos);
                if (piece != null && piece.isWhite() == whiteKing) {
                    for (Position movePos : piece.getPossibleMoves()) {
                        if (!moveCausesCheck(piece, movePos)) {
                            return false;
                        }
                    }
                }
            }
        }
        return true;
    }

    // simulação de movimento para verificar se deixa o rei em xeque
    private boolean moveCausesCheck(Piece piece, Position destination) {
        Position original = piece.getPosition();
        Piece captured = board.getPieceAt(destination);

        board.removePiece(original);
        board.placePiece(piece, destination);

        boolean causesCheck = isInCheck(piece.isWhite());

        board.removePiece(destination);
        board.placePiece(piece, original);
        if (captured != null) {
            board.placePiece(captured, destination);
        }

        return causesCheck;
    }
}

 // ========== Novo método movePiece com histórico ==========
    public boolean movePiece(Position originalPosition, Position destination) {
        Piece selectedPiece = board.getPieceAt(originalPosition);
        if (selectedPiece == null) return false;

        Piece capturedPiece = board.getPieceAt(destination);

        // Executa o movimento (inclui roque, promoção, en passant...)
        // ... aqui entraria a lógica que já implementamos antes ...

        // Criar objeto Move para o histórico
        Move move = new Move(originalPosition, destination, selectedPiece, capturedPiece);

        // identificar casos especiais
        if (selectedPiece instanceof King &&
            Math.abs(destination.getColumn() - originalPosition.getColumn()) == 2) {
            move.setCastling(true);
        }
        if (selectedPiece instanceof Pawn &&
            capturedPiece == null &&
            originalPosition.getColumn() != destination.getColumn()) {
            move.setEnPassant(true);
        }
        if (selectedPiece instanceof Pawn &&
            (destination.getRow() == 0 || destination.getRow() == 7)) {
            move.setPromotion(true);
        }

        // adicionar ao histórico
        moveHistory.add(move);

        isWhiteTurn = !isWhiteTurn;
        return true;
    }

    // ========== Método para desfazer ==========
    public boolean undoLastMove() {
        if (moveHistory.isEmpty()) return false;

        Move lastMove = moveHistory.remove(moveHistory.size() - 1);

        // mover peça de volta
        board.removePiece(lastMove.getTo());
        board.placePiece(lastMove.getPiece(), lastMove.getFrom());

        // restaurar peça capturada
        if (lastMove.getCapturedPiece() != null) {
            board.placePiece(lastMove.getCapturedPiece(), lastMove.getTo());
        }

        // TODO: lidar com roque, promoção e en passant aqui

        // restaura turno
        isWhiteTurn = !isWhiteTurn;
        return true;
    }