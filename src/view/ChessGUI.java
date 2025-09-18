// ========================= src/view/ChessGUI.java =========================

package view;

import controller.Game;
import ai.IANivel3;
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

// Classe principal da interface gráfica do jogo de xadrez
public class ChessGUI extends JFrame {
    private static final long serialVersionUID = 1L; // evita warning de serialização

    // --- Temas de cores ---
    public enum BoardTheme {
        VERMELHO_BEGE(new Color(240, 220, 180), new Color(120, 40, 40));

        public final Color light, dark;

        BoardTheme(Color light, Color dark) {
            this.light = light;
            this.dark = dark;
        }
    }

    private BoardTheme currentTheme = BoardTheme.VERMELHO_BEGE;
    private static final Color HILITE_SELECTED = new Color(255, 237, 41); // cor de seleção
    private static final Color HILITE_LEGAL = new Color(26, 20, 196); // cor de movimentos legais
    private static final Color HILITE_LASTMOVE = new Color(220, 170, 30); // cor do último lance

    private static final Border BORDER_SELECTED = new MatteBorder(3, 3, 3, 3, HILITE_SELECTED);
    private static final Border BORDER_LEGAL = new MatteBorder(3, 3, 3, 3, HILITE_LEGAL);
    private static final Border BORDER_LASTMOVE = new MatteBorder(3, 3, 3, 3, HILITE_LASTMOVE);

    private final Game game; // lógica do jogo

    private final JPanel boardPanel; // painel do tabuleiro
    private final JButton[][] squares = new JButton[8][8]; // botões das casas

    private final JLabel status; // barra de status inferior
    private final JTextArea history; // área de histórico de jogadas
    private final JScrollPane historyScroll; // scroll do histórico

    private final JPanel capturedWhitePanel;
    private final JPanel capturedBlackPanel;

    private final List<Piece> capturedWhite = new ArrayList<>();
    private final List<Piece> capturedBlack = new ArrayList<>();

    // Menu e controles
    private JCheckBoxMenuItem pcAsBlack;
    private JMenuItem newGameItem, quitItem;

    // Controle de seleção e movimentos legais
    private Position selected = null;
    private List<Position> legalForSelected = new ArrayList<>();

    // Realce do último lance
    private Position lastFrom = null, lastTo = null;

    // IA
    private boolean aiThinking = false;
    private final Random rnd = new Random();
    private int aiLevel = 0; // 0 = fácil, 1 = médio, 2 = difícil

    // Construtor da interface
    public ChessGUI() {
        super("Alice Através do Espelho | ChessGame");

        // Tenta aplicar o tema Nimbus
        try {
            UIManager.setLookAndFeel("javax.swing.plaf.nimbus.NimbusLookAndFeel");
            SwingUtilities.updateComponentTreeUI(this);
        } catch (Exception ignored) {
        }

        this.game = new Game();

        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        setLayout(new BorderLayout(8, 8));

        // Menu superior
        setJMenuBar(buildMenuBar());

        // Painel do tabuleiro (8x8)
        boardPanel = new JPanel(new GridLayout(8, 8, 0, 0));
        boardPanel.setBackground(new Color(120, 40, 40));
        boardPanel.setPreferredSize(new Dimension(920, 680));

        // Criando as bordas
        Border outerMargin = BorderFactory.createEmptyBorder(18, 18, 18, 18);
        Border innerBorder = BorderFactory.createMatteBorder(6, 6, 6, 6, new Color(240, 220, 180));

        // Combinando as bordas e aplicando no painel
        boardPanel.setBorder(BorderFactory.createCompoundBorder(outerMargin, innerBorder));

        // Cria os botões das casas do tabuleiro
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
                b.setFont(b.getFont().deriveFont(Font.BOLD, 24f)); // fallback Unicode
                b.addActionListener(e -> handleClick(new Position(rr, cc))); // ação de clique
                squares[r][c] = b;
                boardPanel.add(b);
            }
        }

        // Barra inferior de status
        status = new JLabel("Jogada: Alice");
        status.setBorder(BorderFactory.createEmptyBorder(4, 8, 4, 8));
        status.setFont(new Font("Segoe UI", Font.CENTER_BASELINE, 14));
        status.setForeground(Color.WHITE);

        // Histórico de jogadas
        history = new JTextArea(14, 22);
        history.setEditable(false);
        history.setFont(new Font(Font.MONOSPACED, Font.BOLD, 14));
        history.setForeground(Color.BLACK);
        history.setBackground(new Color(156, 174, 102));
        historyScroll = new JScrollPane(history);

        capturedWhitePanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 4));
        capturedBlackPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 4));

        // Painel lateral direito (histórico + controles)
        JPanel rightPanel = new JPanel(new BorderLayout(6, 6));
        rightPanel.setBorder(BorderFactory.createEmptyBorder(6, 6, 6, 6));
        rightPanel.setBackground(new Color(120, 40, 40));

        JPanel capturedPiecesPanel = new JPanel();
        capturedPiecesPanel.setLayout(new BoxLayout(capturedPiecesPanel, BoxLayout.Y_AXIS));
        capturedPiecesPanel.setBackground(new Color(240, 220, 180));
        capturedPiecesPanel.setBorder(new MatteBorder(5, 5, 5, 5, new Color(240, 220, 180)));

        JLabel capturedWhiteLabel = new JLabel("Peças da Rainha de Copas");
        capturedWhiteLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        capturedWhiteLabel.setHorizontalAlignment(SwingConstants.LEFT);
        capturedWhiteLabel.setForeground(Color.white);

        JLabel capturedBlackLabel = new JLabel("Peças da Alice ");
        capturedBlackLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        capturedBlackLabel.setHorizontalAlignment(SwingConstants.LEFT);
        capturedBlackLabel.setForeground(Color.white);

        capturedWhitePanel.setBackground(new Color(240, 220, 180));
        capturedBlackPanel.setBackground(new Color(240, 220, 180));

        JPanel capturedTopPanel = new JPanel(new BorderLayout());
        capturedTopPanel.setBackground(new Color(120, 40, 40));
        capturedTopPanel.add(capturedWhiteLabel, BorderLayout.NORTH);
        capturedTopPanel.add(capturedWhitePanel, BorderLayout.CENTER);

        JPanel capturedBottomPanel = new JPanel(new BorderLayout());
        capturedBottomPanel.setBackground(new Color(120, 40, 40));
        capturedBottomPanel.add(capturedBlackLabel, BorderLayout.NORTH);
        capturedBottomPanel.add(capturedBlackPanel, BorderLayout.CENTER);

        rightPanel.add(capturedWhiteLabel, BorderLayout.NORTH);
        rightPanel.add(capturedWhitePanel, BorderLayout.CENTER);
        rightPanel.add(capturedBlackLabel, BorderLayout.CENTER); // Será sobreposto
        rightPanel.add(capturedBlackPanel, BorderLayout.SOUTH);

        // Painel para o histórico e controles
        JPanel historyAndControls = new JPanel(new BorderLayout(6, 6));
        historyAndControls.setBackground(new Color(120, 40, 40));
        JLabel histLabel = new JLabel("Histórico de Jogadas:");
        histLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        histLabel.setForeground(Color.WHITE);
        histLabel.setBorder(BorderFactory.createEmptyBorder(0, 0, 4, 0));

        historyAndControls.add(histLabel, BorderLayout.NORTH);
        historyAndControls.add(historyScroll, BorderLayout.CENTER);
        historyAndControls.add(buildSideControls(), BorderLayout.SOUTH);

        rightPanel.add(capturedPiecesPanel, BorderLayout.NORTH);
        rightPanel.add(historyAndControls, BorderLayout.CENTER);

        JPanel topRightPanel = new JPanel(new BorderLayout());
        topRightPanel.setBackground(new Color(120, 40, 40));

        JPanel aliceCapturedContainer = new JPanel(new BorderLayout());
        aliceCapturedContainer.setBackground(new Color(120, 40, 40));
        aliceCapturedContainer.add(capturedWhiteLabel, BorderLayout.NORTH);
        aliceCapturedContainer.add(capturedWhitePanel, BorderLayout.SOUTH);

        JPanel blackCapturedContainer = new JPanel(new BorderLayout());
        blackCapturedContainer.setBackground(new Color(120, 40, 40));
        blackCapturedContainer.add(capturedBlackLabel, BorderLayout.NORTH);
        blackCapturedContainer.add(capturedBlackPanel, BorderLayout.SOUTH);

        JPanel capturedTopSection = new JPanel();
        capturedTopSection.setLayout(new BoxLayout(capturedTopSection, BoxLayout.Y_AXIS));
        capturedTopSection.setBackground(new Color(120, 40, 40));
        capturedTopSection.add(aliceCapturedContainer);
        capturedTopSection.add(Box.createRigidArea(new Dimension(0, 10)));
        capturedTopSection.add(blackCapturedContainer);

        rightPanel.remove(capturedPiecesPanel);
        rightPanel.add(capturedTopSection, BorderLayout.NORTH);

        add(boardPanel, BorderLayout.CENTER);
        add(status, BorderLayout.SOUTH);
        add(rightPanel, BorderLayout.EAST);

        // Atualiza ícones ao redimensionar o painel do tabuleiro
        boardPanel.addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                refresh(); // recarrega ícones ajustando o tamanho
            }
        });

        getContentPane().setBackground(Color.DARK_GRAY); // fundo da janela
        setMinimumSize(new Dimension(1100, 780));
        setLocationRelativeTo(null);

        // Atalhos de teclado: Ctrl+N (novo jogo), Ctrl+Q (sair)
        setupAccelerators();

        setVisible(true);
        refresh();
        maybeTriggerAI(); // inicia IA se for vez do PC
    }

    // ----------------- Menus e controles -----------------

    // Cria a barra de menu superior
    private JMenuBar buildMenuBar() {
        JMenuBar mb = new JMenuBar();

        JMenu gameMenu = new JMenu("Menu");

        newGameItem = new JMenuItem("Novo Jogo");
        newGameItem.setAccelerator(
                KeyStroke.getKeyStroke(KeyEvent.VK_N, Toolkit.getDefaultToolkit().getMenuShortcutKeyMaskEx()));
        newGameItem.addActionListener(e -> doNewGame());

        pcAsBlack = new JCheckBoxMenuItem("PC joga com a Rainha de Copas");
        pcAsBlack.setSelected(false);

        quitItem = new JMenuItem("Sair");
        quitItem.setAccelerator(
                KeyStroke.getKeyStroke(KeyEvent.VK_Q, Toolkit.getDefaultToolkit().getMenuShortcutKeyMaskEx()));
        quitItem.addActionListener(e -> dispatchEvent(new WindowEvent(this, WindowEvent.WINDOW_CLOSING)));

        gameMenu.add(newGameItem);
        gameMenu.addSeparator();
        gameMenu.add(pcAsBlack);
        gameMenu.addSeparator();
        gameMenu.add(quitItem);

        mb.add(gameMenu);
        return mb;
    }

    // Cria os controles laterais (novo jogo, IA, nível)
    private JPanel buildSideControls() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        // Adicione esta linha para mudar a cor de fundo
        panel.setBackground(new Color(120, 40, 40)); // Exemplo: um cinza claro
        JButton btnNew = new JButton("Novo Jogo");
        btnNew.addActionListener(e -> doNewGame());
        btnNew.setBackground(new Color(0, 100, 128)); // verde
        btnNew.setForeground(Color.WHITE); // texto branco
        btnNew.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btnNew.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                btnNew.setBackground(new Color(255, 255, 255)); // verde escuro
            }

            public void mouseExited(java.awt.event.MouseEvent evt) {
                btnNew.setBackground(new Color(0, 100, 128)); // verde original
            }
        });
        panel.add(btnNew);

        JCheckBox cb = new JCheckBox("PC (Rainha de Copas)");
        cb.setSelected(pcAsBlack.isSelected());
        cb.addActionListener(e -> pcAsBlack.setSelected(cb.isSelected()));
        cb.setFocusPainted(false);
        cb.setFont(new Font("Segoe UI", Font.BOLD, 14));
        cb.setForeground(Color.WHITE);

        panel.add(cb);

        JLabel jl = new JLabel("IA:");
        jl.setFont(new Font("Segoe UI", Font.BOLD, 14));
        panel.add(jl);
        jl.setForeground(Color.WHITE);
        JComboBox<String> aiLevelBox = new JComboBox<>(new String[] { "Iniciante", "Intermediário", "Experiente" });
        aiLevelBox.setFont(new Font("Segoe UI", Font.BOLD, 12));
        aiLevelBox.setSelectedIndex(aiLevel);
        aiLevelBox.addActionListener(e -> aiLevel = aiLevelBox.getSelectedIndex());
        panel.add(aiLevelBox);

        return panel;
    }

    // Configura atalhos de teclado para novo jogo e sair
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

    // Inicia um novo jogo
    private void doNewGame() {
        selected = null;
        legalForSelected.clear();
        lastFrom = lastTo = null;
        aiThinking = false;
        game.newGame();

        capturedWhite.clear();
        capturedBlack.clear();
        capturedWhitePanel.removeAll();
        capturedBlackPanel.removeAll();
        capturedWhitePanel.revalidate();
        capturedBlackPanel.revalidate();
        capturedWhitePanel.repaint();
        capturedBlackPanel.repaint();

        refresh();
        maybeTriggerAI();
    }

    // ----------------- Interação de tabuleiro -----------------

    // Lida com cliques nas casas do tabuleiro
    private void handleClick(Position clicked) {
        if (game.isGameOver() || aiThinking)
            return;

        // Se for vez do PC, ignora cliques do usuário
        if (pcAsBlack.isSelected() && !game.whiteToMove())
            return;

        Piece p = game.board().get(clicked);

        if (selected == null) {
            // Seleciona peça da vez
            if (p != null && p.isWhite() == game.whiteToMove()) {
                selected = clicked;
                legalForSelected = game.legalMovesFrom(selected);
            }
        } else {
            // Já havia uma seleção
            List<Position> legals = game.legalMovesFrom(selected); // recalcula por segurança
            if (legals.contains(clicked)) {
                Character promo = null;
                Piece moving = game.board().get(selected);
                if (moving instanceof Pawn && game.isPromotion(selected, clicked)) {
                    promo = askPromotion(); // pergunta peça para promoção
                }

                Piece capturedPiece = game.board().get(clicked);
                if (capturedPiece != null) {
                    if (capturedPiece.isWhite()) {
                        capturedWhite.add(capturedPiece);
                    } else {
                        capturedBlack.add(capturedPiece);
                    }
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
                // Troca seleção para outra peça da vez
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

    // Pergunta ao usuário qual peça promover o peão
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

    // Aciona a jogada da IA se for vez do PC
    private void maybeTriggerAI() {
        if (game.isGameOver())
            return;
        if (!pcAsBlack.isSelected())
            return;
        if (game.whiteToMove())
            return; // PC joga de Rainha de Copas

        aiThinking = true;
        status.setText("Vez: Rainha de Copas — PC pensando...");

        // Executa IA em thread separada (SwingWorker)
        new SwingWorker<Void, Void>() {
            Position aiFrom, aiTo;

            @Override
            protected Void doInBackground() {
                Move chosen = null;
                if (aiLevel == 0) {
                    // Fácil: aleatório
                    var allMoves = collectAllLegalMovesForSide(false);
                    if (allMoves.isEmpty())
                        return null;
                    chosen = allMoves.get(rnd.nextInt(allMoves.size()));
                } else if (aiLevel == 1) {
                    // Médio: prioriza capturas e centro
                    var allMoves = collectAllLegalMovesForSide(false);
                    if (allMoves.isEmpty())
                        return null;
                    int bestScore = Integer.MIN_VALUE;
                    List<Move> bestList = new ArrayList<>();
                    for (Move mv : allMoves) {
                        int score = 0;
                        Piece target = game.board().get(mv.to);
                        if (target != null)
                            score += pieceValue(target);
                        score += centerBonus(mv.to);
                        if (score > bestScore) {
                            bestScore = score;
                            bestList.clear();
                            bestList.add(mv);
                        } else if (score == bestScore) {
                            bestList.add(mv);
                        }
                    }
                    chosen = bestList.get(rnd.nextInt(bestList.size()));
                } else if (aiLevel == 2) {
                    // Difícil: usa IANivel3
                    IANivel3 iaNivel3 = new IANivel3();
                    model.board.Move move = iaNivel3.makeMove(game);
                    if (move != null) {
                        aiFrom = move.getFrom();
                        aiTo = move.getTo();
                        return null;
                    }
                }
                if (chosen != null) {
                    aiFrom = chosen.from;
                    aiTo = chosen.to;
                }
                return null;
            }

            @Override
            protected void done() {
                try {
                    get();
                } catch (Exception ignored) {
                }
                if (aiFrom != null && aiTo != null && !game.isGameOver() && !game.whiteToMove()) {

                    Piece capturedPiece = game.board().get(aiTo);
                    if (capturedPiece != null) {
                        if (capturedPiece.isWhite()) {
                            capturedWhite.add(capturedPiece);
                        } else {
                            capturedBlack.add(capturedPiece);
                        }
                    }

                    lastFrom = aiFrom;
                    lastTo = aiTo;
                    Character promo = null;
                    Piece moving = game.board().get(aiFrom);
                    if (moving instanceof Pawn && game.isPromotion(aiFrom, aiTo)) {
                        promo = 'Q';
                    }
                    game.move(aiFrom, aiTo, promo);
                }
                aiThinking = false;
                refresh();
                maybeAnnounceEnd();
            }
        }.execute();
    }

    // Classe interna para representar um movimento simples (usada pela IA)
    private static class Move {
        final Position from, to;

        Move(Position f, Position t) {
            this.from = f;
            this.to = t;
        }
    }

    // Coleta todos os movimentos legais para o lado especificado
    private List<Move> collectAllLegalMovesForSide(boolean whiteSide) {
        List<Move> moves = new ArrayList<>();
        if (whiteSide != game.whiteToMove())
            return moves;

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

    // Valor das peças para IA
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

    // Bônus para casas centrais (IA)
    private int centerBonus(Position pos) {
        int r = pos.getRow(), c = pos.getColumn();
        if ((r == 3 || r == 4) && (c == 3 || c == 4))
            return 10;
        if ((r >= 2 && r <= 5) && (c >= 2 && c <= 5))
            return 4;
        return 0;
    }

    // ----------------- Atualização de UI -----------------

    // Atualiza toda a interface (tabuleiro, ícones, status, histórico)
    private void refresh() {
        // 1) Cores base e limpa bordas
        for (int r = 0; r < 8; r++) {
            for (int c = 0; c < 8; c++) {
                boolean light = (r + c) % 2 == 0;
                Color base = light ? currentTheme.light : currentTheme.dark;
                JButton b = squares[r][c];
                b.setBackground(base);
                b.setBorder(null);
                b.setToolTipText(null);
            }
        }

        // 2) Realce do último lance
        if (lastFrom != null)
            squares[lastFrom.getRow()][lastFrom.getColumn()].setBorder(BORDER_LASTMOVE);
        if (lastTo != null)
            squares[lastTo.getRow()][lastTo.getColumn()].setBorder(BORDER_LASTMOVE);

        // 3) Realce da seleção e movimentos legais
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

        // 5) Atualiza status e histórico
        String side = game.whiteToMove() ? "Alice" : "Rainha de Copas";
        String chk = game.inCheck(game.whiteToMove()) ? " — Xeque!" : "";
        if (aiThinking)
            chk = " — PC pensando...";
        status.setText("Jogada: " + side + chk);

        // Atualiza cor do histórico conforme tema
        history.setBackground(currentTheme.light);

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

        updateCapturedPieces();
    }

    /**
     * Atualiza os painéis que exibem as peças capturadas.
     * Limpa os painéis existentes e adiciona os ícones das peças capturadas
     * de cada lado.
     */
    private void updateCapturedPieces() {
        int iconSize = 32; // Tamanho fixo para as peças capturadas
        capturedWhitePanel.removeAll();
        capturedBlackPanel.removeAll();

        // Peças de Alice (brancas) - capturadas pelo Adversário (pretas)
        for (Piece p : capturedWhite) {
            char sym = p.getSymbol().charAt(0);
            ImageIcon icon = ImageUtil.getPieceIcon(p.isWhite(), sym, iconSize);
            JLabel label = new JLabel();
            if (icon != null) {
                label.setIcon(icon);
            } else {
                label.setText(toUnicode(p.getSymbol(), p.isWhite()));
                label.setFont(label.getFont().deriveFont(Font.BOLD, 24f));
            }
            capturedWhitePanel.add(label);
        }

        // Peças da Rainha de Copas (pretas) - capturadas por Alice (brancas)
        for (Piece p : capturedBlack) {
            char sym = p.getSymbol().charAt(0);
            ImageIcon icon = ImageUtil.getPieceIcon(p.isWhite(), sym, iconSize);
            JLabel label = new JLabel();
            if (icon != null) {
                label.setIcon(icon);
            } else {
                label.setText(toUnicode(p.getSymbol(), p.isWhite()));
                label.setFont(label.getFont().deriveFont(Font.BOLD, 24f));
            }
            capturedBlackPanel.add(label);
        }

        capturedWhitePanel.revalidate();
        capturedBlackPanel.revalidate();
        capturedWhitePanel.repaint();
        capturedBlackPanel.repaint();
    }

    // Exibe mensagem de fim de jogo
    private void maybeAnnounceEnd() {
        if (!game.isGameOver())
            return;
        String msg;
        if (game.inCheck(game.whiteToMove())) {
            msg = "Xeque-mate! Cortem-lhes a cabeça!" + (game.whiteToMove() ? "Alice" : "Rainha de Copas")
                    + " estão em mate.";
        } else {
            msg = "Empate por afogamento (stalemate).";
        }
        JOptionPane.showMessageDialog(this, msg, "Fim de Jogo", JOptionPane.INFORMATION_MESSAGE);
    }

    // Retorna o Unicode da peça (fallback)
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

    // Calcula o tamanho ideal do ícone da peça
    private int computeSquareIconSize() {
        JButton b = squares[0][0];
        int w = Math.max(1, b.getWidth());
        int h = Math.max(1, b.getHeight());
        int side = Math.min(w, h);
        if (side <= 1)
            return 64;
        return Math.max(24, side - 8);
    }

    // Ponto de entrada do programa
    public static void main(String[] args) {
        SwingUtilities.invokeLater(ChessGUI::new);
    }
}