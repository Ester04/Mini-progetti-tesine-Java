package org.example;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Stack;

public class TrisTreGiocatori4x4 extends JPanel {
    private final char[][] board = new char[4][4];
    private final char[] players = {'X', 'O', '▲'};
    private int currentPlayerIndex = 0;
    private boolean gameEnded = false;
    private final int cellSize = 120;
    private int[][] winningLine = null;

    private final Stack<Move> movesHistory = new Stack<>(); // cronologia mosse

    private final JButton menuButton;
    private final JButton undoButton;

    public TrisTreGiocatori4x4() {
        setPreferredSize(new Dimension(1000, 1000));
        setBackground(Color.WHITE);
        setFont(new Font("Arial", Font.BOLD, 96));
        setLayout(null);

        // Pulsante Menu
        menuButton = createStyledButton("Menu");
        menuButton.addActionListener(e -> {
            JFrame topFrame = (JFrame) SwingUtilities.getWindowAncestor(this);
            topFrame.dispose();
            SwingUtilities.invokeLater(() -> new TrisLauncher().setVisible(true));
        });
        add(menuButton);

        // Pulsante Annulla mossa (freccetta)
        undoButton = createStyledButton("←");
        undoButton.setFont(new Font("Arial", Font.BOLD, 28));
        undoButton.addActionListener(e -> undoLastMove());
        add(undoButton);

        // Aggiorna posizione pulsanti quando la finestra viene ridimensionata
        addComponentListener(new java.awt.event.ComponentAdapter() {
            public void componentResized(java.awt.event.ComponentEvent evt) {
                int width = getWidth();
                menuButton.setBounds(width - 180, 20, 160, 50);
                undoButton.setBounds(width - 180, 80, 160, 50);
            }
        });

        addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                if (gameEnded) return;

                int totalGridSize = 4 * cellSize;
                int offsetX = (getWidth() - totalGridSize) / 2;
                int offsetY = (getHeight() - totalGridSize) / 2;

                int x = e.getX() - offsetX;
                int y = e.getY() - offsetY;

                if (x < 0 || y < 0) return;

                int col = x / cellSize;
                int row = y / cellSize;

                if (row < 0 || row >= 4 || col < 0 || col >= 4) return;

                if (board[row][col] == '\0') {
                    board[row][col] = players[currentPlayerIndex];
                    movesHistory.push(new Move(row, col, currentPlayerIndex)); // salva mossa

                    if (checkWin(players[currentPlayerIndex])) {
                        gameEnded = true;
                    } else if (isBoardFull()) {
                        gameEnded = true;
                        winningLine = null;
                    } else {
                        currentPlayerIndex = (currentPlayerIndex + 1) % players.length;
                    }
                    repaint();
                }
            }
        });
    }

    private JButton createStyledButton(String text) {
        JButton button = new JButton(text);
        button.setFont(new Font("Arial", Font.BOLD, 18));
        button.setForeground(Color.WHITE);
        button.setBackground(new Color(139, 0, 139)); // dark magenta
        button.setFocusPainted(false);
        button.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Color.BLACK, 2, true),
                BorderFactory.createEmptyBorder(10, 20, 10, 20)
        ));
        button.setOpaque(true);
        return button;
    }

    private void undoLastMove() {
        if (movesHistory.isEmpty()) return;

        Move last = movesHistory.pop();
        board[last.row][last.col] = '\0'; // rimuovi simbolo
        currentPlayerIndex = last.playerIndex; // torna al giocatore precedente
        winningLine = null;
        gameEnded = false;
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        drawBackgroundGrid((Graphics2D) g);
        drawGrid((Graphics2D) g);
        drawMarks((Graphics2D) g);
        drawWinningLine((Graphics2D) g);
        drawWinText((Graphics2D) g);
        drawCurrentPlayer((Graphics2D) g);
    }

    private void drawBackgroundGrid(Graphics2D g2) {
        g2.setColor(new Color(230, 230, 230));
        int spacing = 40;
        for (int x = 0; x < getWidth(); x += spacing)
            g2.drawLine(x, 0, x, getHeight());
        for (int y = 0; y < getHeight(); y += spacing)
            g2.drawLine(0, y, getWidth(), y);
    }

    private void drawGrid(Graphics2D g2) {
        int offsetX = (getWidth() - 4 * cellSize) / 2;
        int offsetY = (getHeight() - 4 * cellSize) / 2;

        g2.setColor(Color.BLACK);
        g2.setStroke(new BasicStroke(6));
        for (int i = 1; i < 4; i++) {
            g2.drawLine(offsetX, offsetY + i * cellSize, offsetX + 4 * cellSize, offsetY + i * cellSize);
            g2.drawLine(offsetX + i * cellSize, offsetY, offsetX + i * cellSize, offsetY + 4 * cellSize);
        }
    }

    private void drawMarks(Graphics2D g2) {
        int offsetX = (getWidth() - 4 * cellSize) / 2;
        int offsetY = (getHeight() - 4 * cellSize) / 2;
        g2.setFont(new Font("Arial", Font.BOLD, 96));

        for (int row = 0; row < 4; row++) {
            for (int col = 0; col < 4; col++) {
                char mark = board[row][col];
                if (mark != '\0') {
                    switch (mark) {
                        case 'X' -> g2.setColor(Color.RED);
                        case 'O' -> g2.setColor(Color.BLUE);
                        case '▲' -> g2.setColor(Color.GREEN.darker());
                        default -> g2.setColor(Color.BLACK);
                    }
                    String symbol = String.valueOf(mark);
                    FontMetrics fm = g2.getFontMetrics();
                    int x = offsetX + col * cellSize + (cellSize - fm.stringWidth(symbol)) / 2;
                    int y = offsetY + row * cellSize + ((cellSize - fm.getHeight()) / 2 + fm.getAscent());
                    g2.drawString(symbol, x, y);
                }
            }
        }
    }

    private void drawWinningLine(Graphics2D g2) {
        if (winningLine == null) return;

        int offsetX = (getWidth() - 4 * cellSize) / 2;
        int offsetY = (getHeight() - 4 * cellSize) / 2;

        int x1 = offsetX + winningLine[0][1] * cellSize + cellSize / 2;
        int y1 = offsetY + winningLine[0][0] * cellSize + cellSize / 2;
        int x2 = offsetX + winningLine[2][1] * cellSize + cellSize / 2;
        int y2 = offsetY + winningLine[2][0] * cellSize + cellSize / 2;

        g2.setColor(Color.MAGENTA.darker());
        g2.setStroke(new BasicStroke(10));
        g2.drawLine(x1, y1, x2, y2);
    }

    private void drawWinText(Graphics2D g2) {
        if (!gameEnded) return;

        Color winColor = (winningLine != null) ? Color.MAGENTA.darker() : Color.DARK_GRAY;
        g2.setColor(winColor);
        g2.setFont(new Font("Arial", Font.BOLD, 60));

        String message = (winningLine != null)
                ? "Vittoria di " + players[currentPlayerIndex] + "!"
                : "Pareggio!";
        FontMetrics fm = g2.getFontMetrics();
        int textWidth = fm.stringWidth(message);
        int x = (getWidth() - textWidth) / 2;
        int y = getHeight() - (getHeight() - cellSize * 4) / 4 + 24;
        g2.drawString(message, x, y);
    }

    private void drawCurrentPlayer(Graphics2D g2) {
        if (gameEnded) return;
        g2.setColor(Color.MAGENTA.darker());
        g2.setFont(new Font("Arial", Font.BOLD, 32));
        g2.drawString("Turno: " + players[currentPlayerIndex], 20, 50); // più in alto a sinistra
    }

    private boolean isBoardFull() {
        for (char[] row : board)
            for (char cell : row)
                if (cell == '\0') return false;
        return true;
    }

    private boolean checkWin(char player) {
        for (int i = 0; i < 4; i++) {
            for (int j = 0; j <= 1; j++) {
                if (board[i][j] == player && board[i][j + 1] == player && board[i][j + 2] == player) {
                    winningLine = new int[][]{{i, j}, {i, j + 1}, {i, j + 2}};
                    return true;
                }
                if (board[j][i] == player && board[j + 1][i] == player && board[j + 2][i] == player) {
                    winningLine = new int[][]{{j, i}, {j + 1, i}, {j + 2, i}};
                    return true;
                }
            }
        }
        for (int i = 0; i <= 1; i++) {
            for (int j = 0; j <= 1; j++) {
                if (board[i][j] == player && board[i + 1][j + 1] == player && board[i + 2][j + 2] == player) {
                    winningLine = new int[][]{{i, j}, {i + 1, j + 1}, {i + 2, j + 2}};
                    return true;
                }
                if (board[i][j + 2] == player && board[i + 1][j + 1] == player && board[i + 2][j] == player) {
                    winningLine = new int[][]{{i, j + 2}, {i + 1, j + 1}, {i + 2, j}};
                    return true;
                }
            }
        }
        return false;
    }

    private static class Move {
        int row, col, playerIndex;
        Move(int row, int col, int playerIndex) {
            this.row = row;
            this.col = col;
            this.playerIndex = playerIndex;
        }
    }

    public static void main(String[] args) {
        JFrame frame = new JFrame("Tris 3 Giocatori - 4x4");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.getContentPane().add(new TrisTreGiocatori4x4());
        frame.pack();
        frame.setExtendedState(JFrame.MAXIMIZED_BOTH);
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }
}
