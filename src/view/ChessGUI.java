// ========================= src/view/ChessGUI.java =========================
package view;

import java.io.IOException;
import java.net.URL;
import javax.imageio.ImageIO;
import controller.Game;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.MatteBorder;
import model.board.Position;
import model.pieces.Pawn;
import model.pieces.Piece;

public class ChessGUI extends JFrame {
    private static final long serialVersionUID = 1L; // evita warning de serialização

    // --- Config de cores/styles ---
    private static final Color LIGHT_SQ = new Color(240, 220, 180);
    private static final Color DARK_SQ = new Color(120, 40, 40);
    private static final Color HILITE_SELECTED = new Color(50, 120, 220);
    private static final Color HILITE_LEGAL = new Color(20, 140, 60);
    private static final Color HILITE_LASTMOVE = new Color(220, 170, 30);

    private static final Border BORDER_SELECTED = new MatteBorder(3, 3, 3, 3, HILITE_SELECTED);
    private static final Border BORDER_LEGAL = new MatteBorder(3, 3, 3, 3, HILITE_LEGAL);
    private static final Border BORDER_LASTMOVE = new MatteBorder(3, 3, 3, 3, HILITE_LASTMOVE);

    private final Game game;

    private final JPanel boardPanel;
    private final JButton[][] squares = new JButton[8][8];

    private final JLabel status;
    private final JTextArea history;
    private final JScrollPane historyScroll;
    private final List<String> capturedWhite = new ArrayList<>();
    private final List<String> capturedBlack = new ArrayList<>();

    // Menu / controles
    private JCheckBoxMenuItem pcAsBlack;
    private JSpinner depthSpinner;
    private JMenuItem newGameItem, quitItem;

    // Seleção atual e movimentos legais
    private Position selected = null;
    private List<Position> legalForSelected = new ArrayList<>();

    // Realce do último lance
    private Position lastFrom = null, lastTo = null;

    // IA
    private boolean aiThinking = false;
    private final Random rnd = new Random();

    public ChessGUI() {
        super("Alice Através do Espelho | ChessGame");

        // Look&Feel Nimbus
        try {
            UIManager.setLookAndFeel("javax.swing.plaf.nimbus.NimbusLookAndFeel");
            SwingUtilities.updateComponentTreeUI(this);
        } catch (Exception ignored) {
        }

        this.game = new Game();

        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        setLayout(new BorderLayout(8, 8));

        // Menu
        setJMenuBar(buildMenuBar());

        // Painel do tabuleiro (8x8)
        boardPanel = new JPanel(new GridLayout(8, 8, 0, 0));
        boardPanel.setBackground(DARK_SQ);

        boardPanel.setPreferredSize(new Dimension(920, 680));

        // Criando as bordas
        Border outerMargin = BorderFactory.createEmptyBorder(18, 18, 18, 18);
        Border innerBorder = BorderFactory.createMatteBorder(6, 6, 6, 6, LIGHT_SQ);

        // Combinando as bordas e aplicando no painel
        boardPanel.setBorder(BorderFactory.createCompoundBorder(outerMargin, innerBorder));

        // Cria botões das casas
        for (int r = 0; r < 8; r++) {
            for (int c = 0; c < 8; c++) {
                final int rr = r;
                final int cc = c;
                JButton b = new JButton();
                b.setMargin(new Insets(0, 0, 0, 0));
                b.setFocusPainted(false);
                b.setOpaque(true);
                b.setBorderPainted(true);
                b.setContentAreaFilled(true);
                b.setFont(b.getFont().deriveFont(Font.BOLD, 24f)); // fallback com Unicode
                b.addActionListener(e -> handleClick(new Position(rr, cc)));
                squares[r][c] = b;
                boardPanel.add(b);
            }
        }

        // Barra inferior de status
        status = new JLabel("Vez: Alice");
        status.setBorder(BorderFactory.createEmptyBorder(4, 8, 4, 8));

        // Altere esta linha para uma fonte que suporte Unicode Chess Symbols
        status.setFont(new Font("Segoe UI Symbol", Font.BOLD, 16)); // Exemplo para Windows
        // Ou tente "Arial Unicode MS" ou "DejaVu Sans" se "Segoe UI Symbol" não
        // funcionar
        status.setForeground(new Color(255, 255, 255)); // Exemplo: cor branca (RGB: 255, 255, 255)
        // ...

        // Histórico
        history = new JTextArea(14, 22);
        history.setEditable(false);
        history.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
        // Adicione esta linha para mudar a cor de fundo
        history.setBackground(new Color(255, 255, 255)); // Um tom de lilás claro
        historyScroll = new JScrollPane(history);
        historyScroll.setPreferredSize(new Dimension(250, 400)); // Ajuste as dimensões conforme necessário

        // Layout principal: tabuleiro à esquerda, histórico à direita
        JPanel rightPanel = new JPanel(new BorderLayout(6, 6));
        rightPanel.setBorder(BorderFactory.createEmptyBorder(6, 6, 6, 6));
        JLabel histLabel = new JLabel("Histórico de lances:");
        // Alterando a fonte e a cor do JLabel 'histLabel'
        histLabel.setFont(new Font("Arial", Font.BOLD, 14)); // Exemplo: fonte Arial, negrito, tamanho 14
        histLabel.setForeground(new Color(255, 255, 255)); // Exemplo: cor branca (RGB: 255, 255, 255)
        histLabel.setBorder(BorderFactory.createEmptyBorder(0, 0, 4, 0));
        rightPanel.add(histLabel, BorderLayout.NORTH);
        rightPanel.add(historyScroll, BorderLayout.CENTER);
        rightPanel.add(buildSideControls(), BorderLayout.SOUTH);
        rightPanel.setBackground(new Color(120, 40, 40));

        add(boardPanel, BorderLayout.CENTER);
        add(status, BorderLayout.SOUTH);
        add(rightPanel, BorderLayout.EAST);

        // Atualiza ícones conforme a janela/painel muda de tamanho
        boardPanel.addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                refresh(); // recarrega ícones ajustando o tamanho
            }
        });

        setMinimumSize(new Dimension(950, 680));
        setLocationRelativeTo(null);
        getContentPane().setBackground(new Color(168, 68, 78)); // cinza claro

        setupAccelerators();

        setVisible(true);
        refresh();
        maybeTriggerAI();
    }

    // ----------------- Menus e controles -----------------

    private JMenuBar buildMenuBar() {
        JMenuBar mb = new JMenuBar();
        JMenu gameMenu = new JMenu("Menu");
        newGameItem = new JMenuItem("Novo Jogo");
        newGameItem.setAccelerator(
                KeyStroke.getKeyStroke(KeyEvent.VK_N, Toolkit.getDefaultToolkit().getMenuShortcutKeyMaskEx()));
        newGameItem.addActionListener(e -> doNewGame());
        pcAsBlack = new JCheckBoxMenuItem("PC joga com a Rainha de Copas");
        pcAsBlack.setSelected(false);
        JMenu depthMenu = new JMenu("Profundidade IA");
        depthSpinner = new JSpinner(new SpinnerNumberModel(1, 1, 4, 1));
        depthSpinner.setToolTipText("Profundidade efetiva da IA (heurística não-minimax)");
        depthMenu.add(depthSpinner);
        quitItem = new JMenuItem("Sair");
        quitItem.setAccelerator(
                KeyStroke.getKeyStroke(KeyEvent.VK_Q, Toolkit.getDefaultToolkit().getMenuShortcutKeyMaskEx()));
        quitItem.addActionListener(e -> dispatchEvent(new WindowEvent(this, WindowEvent.WINDOW_CLOSING)));
        gameMenu.add(newGameItem);
        gameMenu.addSeparator();
        gameMenu.add(pcAsBlack);
        gameMenu.add(depthMenu);
        gameMenu.addSeparator();
        gameMenu.add(quitItem);
        mb.add(gameMenu);
        return mb;
    }

    private JPanel buildSideControls() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        // Adicione esta linha para mudar a cor de fundo
        panel.setBackground(new Color (211, 218, 217)); // Exemplo: um cinza claro
        JButton btnNew = new JButton("Novo Jogo");
        btnNew.addActionListener(e -> doNewGame());
        panel.add(btnNew);
        btnNew.setBackground(new Color(120, 40, 40)); // vermelho escuro
        btnNew.setForeground(Color.WHITE); // Texto branco
        btnNew.setFont(new Font("Segoe UI", Font.BOLD, 14));

        JCheckBox cb = new JCheckBox("PC (Rainha de Copas)");
        cb.setSelected(pcAsBlack.isSelected());
        cb.addActionListener(e -> {
            pcAsBlack.setSelected(cb.isSelected());
            maybeTriggerAI();
        });
        panel.add(cb);

        panel.add(new JLabel("Nível IA:"));
        JSpinner sp = new JSpinner(new SpinnerNumberModel(1, 1, 4, 1));
        sp.setValue(depthSpinner.getValue()); // Garante que o valor inicial seja o mesmo do menu
        sp.addChangeListener(e -> {
            depthSpinner.setValue(sp.getValue());
            maybeTriggerAI();
        });
        panel.add(sp);
        return panel;
    }

    private void setupAccelerators() {
        getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW)
                .put(KeyStroke.getKeyStroke(KeyEvent.VK_N, Toolkit.getDefaultToolkit().getMenuShortcutKeyMaskEx()),
                        "newGame");
        getRootPane().getActionMap().put("newGame", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                doNewGame();
            }
        });

        getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW)
                .put(KeyStroke.getKeyStroke(KeyEvent.VK_Q, Toolkit.getDefaultToolkit().getMenuShortcutKeyMaskEx()),
                        "quit");
        getRootPane().getActionMap().put("quit", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                dispatchEvent(new WindowEvent(ChessGUI.this, WindowEvent.WINDOW_CLOSING));
            }
        });
    }

    private void doNewGame() {
        selected = null;
        legalForSelected.clear();
        lastFrom = lastTo = null;
        aiThinking = false;
        game.newGame();
        refresh();
        maybeTriggerAI();
    }

    // ----------------- Interação de tabuleiro -----------------

    private void handleClick(Position clicked) {
        if (game.isGameOver() || aiThinking)
            return;

        // Se for vez do PC (Rainha de Copas) e modo PC ativado, ignore cliques
        if (pcAsBlack.isSelected() && !game.whiteToMove())
            return;

        Piece p = game.board().get(clicked);

        if (selected == null) {
            // Nada selecionado ainda: só seleciona se for peça da vez
            if (p != null && p.isWhite() == game.whiteToMove()) {
                selected = clicked;
                legalForSelected = game.legalMovesFrom(selected);
            }
        } else {
            // Já havia uma seleção
            List<Position> legals = game.legalMovesFrom(selected); // recalc por segurança
            if (legals.contains(clicked)) {
                Character promo = null;
                Piece moving = game.board().get(selected);
                Piece captured = game.board().get(clicked);
                // Verifique se uma peça foi capturada
                if (captured != null) {
                    if (captured.isWhite()) {
                        capturedWhite.add(captured.getSymbol());
                    } else {
                        capturedBlack.add(captured.getSymbol());
                    }
                }
                if (moving instanceof Pawn && game.isPromotion(selected, clicked)) {
                    promo = askPromotion();
                }
                lastFrom = selected;
                lastTo = clicked;
                game.move(selected, clicked, promo);
                selected = null;
                legalForSelected.clear();
                refresh();
                maybeAnnounceEnd();
                maybeTriggerAI();
                return;
            } else if (p != null && p.isWhite() == game.whiteToMove()) {
                // Troca a seleção para outra peça da vez
                selected = clicked;
                legalForSelected = game.legalMovesFrom(selected);
            } else {
                // Clique inválido: limpa seleção
                selected = null;
                legalForSelected.clear();
            }
        }
        refresh();
    }

    private Character askPromotion() {
        String[] opts = { "Rainha", "Torre", "Bispo", "Cavalo" };
        int ch = JOptionPane.showOptionDialog(
                this,
                "Escolha a peça para promoção:",
                "Promoção",
                JOptionPane.DEFAULT_OPTION,
                JOptionPane.QUESTION_MESSAGE,
                null,
                opts,
                opts[0]);
        return switch (ch) {
            case 1 -> 'R';
            case 2 -> 'B';
            case 3 -> 'N';
            default -> 'Q';
        };
    }

    // ----------------- IA (não bloqueante) -----------------
    // Implementação da IA de Nível 1 (aleatória) e Nível 2 (avaliação de posição)
    private void maybeTriggerAI() {
        if (game.isGameOver())
            return;
        if (!pcAsBlack.isSelected())
            return;
        if (game.whiteToMove())
            return;

        aiThinking = true;
        status.setText("Vez: Rainha de Copas — pensando...");

        final int depth = (Integer) depthSpinner.getValue();

        new SwingWorker<Move, Void>() {
            @Override
            protected Move doInBackground() {
                List<Move> allMoves = collectAllLegalMovesForSide(false);
                if (allMoves.isEmpty()) {
                    return null;
                }
                // Se a profundidade for 1, faz um movimento aleatório (Nível 1)
                if (depth == 1) {
                    return allMoves.get(rnd.nextInt(allMoves.size()));
                } else {
                    // Usa a avaliação para encontrar o melhor lance (Nível 2)
                    int bestScore = Integer.MAX_VALUE;
                    Move bestMove = null;

                    for (Move move : allMoves) {
                        game.move(move.from, move.to, null);
                        int score = evaluateBoard();
                        game.undoLastMove();

                        if (score < bestScore) {
                            bestScore = score;
                            bestMove = move;
                        }
                    }
                    return bestMove;
                }
            }

            @Override
            protected void done() {
                try {
                    Move bestMove = get();
                    if (bestMove != null && !game.isGameOver() && !game.whiteToMove()) {
                        lastFrom = bestMove.from;
                        lastTo = bestMove.to;
                        Character promo = null;
                        Piece moving = game.board().get(bestMove.from);
                        if (moving instanceof Pawn && game.isPromotion(bestMove.from, bestMove.to)) {
                            promo = 'Q';
                        }
                        game.move(bestMove.from, bestMove.to, promo);
                    }
                } catch (Exception ignored) {
                } finally {
                    aiThinking = false;
                    refresh();
                    maybeAnnounceEnd();
                }
            }
        }.execute();
    }

    private int evaluateBoard() {
        int score = 0;
        for (int r = 0; r < 8; r++) {
            for (int c = 0; c < 8; c++) {
                Position pos = new Position(r, c);
                Piece p = game.board().get(pos);
                if (p != null) {
                    int value = pieceValue(p);
                    // Adiciona bônus para peças avançadas e controle do centro
                    if (p.isWhite()) {
                        score += value + centerBonus(pos);
                        score += (7 - r) * 5; // Bônus para peão avançado branco
                    } else {
                        score -= value + centerBonus(pos);
                        score -= r * 5; // Bônus para peão avançado preto
                    }
                }
            }
        }
        return score;
    }

    private static class Move {
        final Position from, to;

        Move(Position f, Position t) {
            this.from = f;
            this.to = t;
        }
    }

    private List<Move> collectAllLegalMovesForSide(boolean whiteSide) {
        List<Move> moves = new ArrayList<>();
        for (int r = 0; r < 8; r++) {
            for (int c = 0; c < 8; c++) {
                Position from = new Position(r, c);
                Piece piece = game.board().get(from);
                if (piece != null && piece.isWhite() == whiteSide) {
                    for (Position to : game.legalMovesFrom(from)) {
                        moves.add(new Move(from, to));
                    }
                }
            }
        }
        return moves;
    }

    private int pieceValue(Piece p) {
        if (p == null)
            return 0;
        switch (p.getSymbol()) {
            case "P":
                return 100;
            case "N":
            case "B":
                return 300;
            case "R":
                return 500;
            case "Q":
                return 900;
            case "K":
                return 20000;
        }
        return 0;
    }

    private int centerBonus(Position pos) {
        int r = pos.getRow(), c = pos.getColumn();
        if ((r == 3 || r == 4) && (c == 3 || c == 4))
            return 10;
        if ((r >= 2 && r <= 5) && (c >= 2 && c <= 5))
            return 4;
        return 0;
    }
    // ----------------- Atualização de UI -----------------

    private void refresh() {
        // 1) Cores base e limpa bordas
        for (int r = 0; r < 8; r++) {
            for (int c = 0; c < 8; c++) {
                boolean light = (r + c) % 2 == 0;
                Color base = light ? LIGHT_SQ : DARK_SQ;
                JButton b = squares[r][c];
                b.setBackground(base);
                b.setBorder(null);
                b.setToolTipText(null);
            }
        }

        // 2) Realce último lance
        if (lastFrom != null)
            squares[lastFrom.getRow()][lastFrom.getColumn()].setBorder(BORDER_LASTMOVE);
        if (lastTo != null)
            squares[lastTo.getRow()][lastTo.getColumn()].setBorder(BORDER_LASTMOVE);

        // 3) Realce seleção e movimentos legais
        if (selected != null) {
            squares[selected.getRow()][selected.getColumn()].setBorder(BORDER_SELECTED);
            for (Position d : legalForSelected) {
                squares[d.getRow()][d.getColumn()].setBorder(BORDER_LEGAL);
            }
        }

        // 4) Ícones das peças (ou Unicode como fallback)
        int iconSize = computeSquareIconSize();
        for (int r = 0; r < 8; r++) {
            for (int c = 0; c < 8; c++) {
                Piece p = game.board().get(new Position(r, c));
                JButton b = squares[r][c];

                if (p == null) {
                    b.setIcon(null);
                    b.setText("");
                    continue;
                }

                char sym = p.getSymbol().charAt(0);
                ImageIcon icon = ImageUtil.getPieceIcon(p.isWhite(), sym, iconSize);
                if (icon != null) {
                    b.setIcon(icon);
                    b.setText("");
                } else {
                    b.setIcon(null);
                    b.setText(toUnicode(p.getSymbol(), p.isWhite()));
                }
            }
        }
        // Constrói a string com as peças capturadas
        StringBuilder capturesText = new StringBuilder(" | Capturas: ");

        // Adiciona as capturas das Brancas (peças pretas capturadas)
        for (String symbol : capturedBlack) {
            capturesText.append(toUnicode(symbol, false)).append(" ");
        }

        // Adiciona as capturas das Pretas (peças brancas capturadas)
        for (String symbol : capturedWhite) {
            capturesText.append(toUnicode(symbol, true)).append(" ");
        }

        // 5) Status e histórico
        String side = game.whiteToMove() ? "Alice" : "Rainha de Copas";
        String chk = game.inCheck(game.whiteToMove()) ? " — Xeque!" : "";
        if (aiThinking)
            chk = " — PC pensando...";

        status.setText("Vez: " + side + chk + capturesText.toString());

        StringBuilder sb = new StringBuilder();
        var hist = game.history();
        for (int i = 0; i < hist.size(); i++) {
            if (i % 2 == 0)
                sb.append((i / 2) + 1).append('.').append(' ');
            sb.append(hist.get(i)).append(' ');
            if (i % 2 == 1)
                sb.append('\n');
        }
        history.setText(sb.toString());
        history.setCaretPosition(history.getDocument().getLength());
    }

    private void maybeAnnounceEnd() {
        if (!game.isGameOver())
            return;
        String msg;
        if (game.inCheck(game.whiteToMove())) {
            msg = "Xeque-Mate: cortem-lhes a cabeça!" + (game.whiteToMove() ? "Alice" : "Rainha de Copas")
                    + " estão em mate.";
        } else {
            msg = "Empate por afogamento (stalemate).";
        }
        JOptionPane.showMessageDialog(this, msg, "Fim de Jogo", JOptionPane.INFORMATION_MESSAGE);
    }

    private String toUnicode(String sym, boolean white) {
        return switch (sym) {
            case "K" -> white ? "\u2654" : "\u265A";
            case "Q" -> white ? "\u2655" : "\u265B";
            case "R" -> white ? "\u2656" : "\u265C";
            case "B" -> white ? "\u2657" : "\u265D";
            case "N" -> white ? "\u2658" : "\u265E";
            case "P" -> white ? "\u2659" : "\u265F";
            default -> "";
        };
    }

    private int computeSquareIconSize() {
        JButton b = squares[0][0];
        int w = Math.max(1, b.getWidth());
        int h = Math.max(1, b.getHeight());
        int side = Math.min(w, h);
        if (side <= 1)
            return 64;
        return Math.max(24, side - 8);
    }

    private Icon loadPieceIcon(String key) {
        String resourcePath = "/resources/" + key + ".png";
        try {
            URL url = getClass().getResource(resourcePath);
            if (url != null) {
                Image img = ImageIO.read(url).getScaledInstance(64, 64, Image.SCALE_SMOOTH);
                return new ImageIcon(img);
            }
            String[] fallbacks = new String[] {
                    "resources/" + key + ".png",
                    "../resources/" + key + ".png",
                    "./resources/" + key + ".png"
            };
            for (String fp : fallbacks) {
                java.io.File f = new java.io.File(fp);
                if (f.exists()) {
                    Image img = ImageIO.read(f).getScaledInstance(64, 64, Image.SCALE_SMOOTH);
                    return new ImageIcon(img);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(ChessGUI::new);
    }
}
