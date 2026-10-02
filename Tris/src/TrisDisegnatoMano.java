package org.example;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Stack;

public class TrisDisegnatoMano extends JPanel {
    private final char[][] board = new char[3][3];
    private char currentPlayer = 'X';
    private boolean gameEnded = false;
    private final int cellSize = 140;
    private int[][] winningLine = null;

    private final Stack<Move> movesHistory = new Stack<>(); // cronologia mosse

    private JButton menuButton;
    private JButton undoButton;

    public TrisDisegnatoMano() {
        setPreferredSize(new Dimension(1000, 1000));
        setBackground(Color.WHITE);
        setFont(new Font("Arial", Font.BOLD, 96));
        setLayout(null);

        // Pulsante MENU
        menuButton = createStyledButton("Menu");
        menuButton.addActionListener(e -> {
            JFrame topFrame = (JFrame) SwingUtilities.getWindowAncestor(this);
            topFrame.dispose();
            SwingUtilities.invokeLater(() -> new TrisLauncher().setVisible(true));
        });
        add(menuButton);

        // Pulsante UNDO
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

                int totalGridSize = 3 * cellSize;
                int offsetX = (getWidth() - totalGridSize) / 2;
                int offsetY = (getHeight() - totalGridSize) / 2;

                int x = e.getX() - offsetX;
                int y = e.getY() - offsetY;

                if (x < 0 || y < 0) return;

                int col = x / cellSize;
                int row = y / cellSize;

                if (row < 3 && col < 3 && board[row][col] == '\0') {
                    board[row][col] = currentPlayer;
                    movesHistory.push(new Move(row, col, currentPlayer)); // salva mossa

                    if (checkWin(currentPlayer)) {
                        gameEnded = true;
                    } else if (isBoardFull()) {
                        gameEnded = true;
                        winningLine = null;
                    } else {
                        currentPlayer = (currentPlayer == 'X') ? 'O' : 'X';
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
        currentPlayer = last.player; // torna al giocatore precedente
        winningLine = null;
        gameEnded = false;
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;

        int totalGridSize = 3 * cellSize;
        int offsetX = (getWidth() - totalGridSize) / 2;
        int offsetY = (getHeight() - totalGridSize) / 2;

        drawBackgroundGrid(g2);
        drawGrid(g2, offsetX, offsetY);
        drawMarks(g2, offsetX, offsetY);
        drawWinningLine(g2, offsetX, offsetY);
        drawWinText(g2);
        drawCurrentPlayer(g2);
    }

    private void drawBackgroundGrid(Graphics2D g2) {
        g2.setColor(new Color(220, 220, 220));
        int spacing = 40;

        for (int x = 0; x < getWidth(); x += spacing)
            g2.drawLine(x, 0, x, getHeight());

        for (int y = 0; y < getHeight(); y += spacing)
            g2.drawLine(0, y, getWidth(), y);
    }

    private void drawGrid(Graphics2D g2, int offsetX, int offsetY) {
        g2.setColor(Color.BLACK);
        g2.setStroke(new BasicStroke(6));
        for (int i = 1; i < 3; i++) {
            g2.drawLine(offsetX, offsetY + i * cellSize, offsetX + 3 * cellSize, offsetY + i * cellSize);
            g2.drawLine(offsetX + i * cellSize, offsetY, offsetX + i * cellSize, offsetY + 3 * cellSize);
        }
    }

    private void drawMarks(Graphics2D g2, int offsetX, int offsetY) {
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 3; col++) {
                char mark = board[row][col];
                if (mark != '\0') {
                    if (mark == 'X') {
                        g2.setColor(Color.RED);
                        g2.setStroke(new BasicStroke(12));
                        int x1 = offsetX + col * cellSize + 30;
                        int y1 = offsetY + row * cellSize + 30;
                        int x2 = offsetX + (col + 1) * cellSize - 30;
                        int y2 = offsetY + (row + 1) * cellSize - 30;
                        g2.drawLine(x1, y1, x2, y2);
                        g2.drawLine(x1, y2, x2, y1);
                    } else {
                        g2.setColor(Color.BLUE);
                        g2.setStroke(new BasicStroke(10));
                        int x = offsetX + col * cellSize + cellSize / 2;
                        int y = offsetY + row * cellSize + cellSize / 2;
                        g2.drawOval(x - 50, y - 50, 100, 100);
                    }
                }
            }
        }
    }

    private void drawWinningLine(Graphics2D g2, int offsetX, int offsetY) {
        if (winningLine == null) return;

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

        g2.setColor(Color.MAGENTA.darker());
        g2.setFont(new Font("Arial", Font.BOLD, 60));

        String message = (winningLine != null) ? "Vittoria di " + currentPlayer + "!" : "Pareggio!";
        FontMetrics fm = g2.getFontMetrics();
        int textWidth = fm.stringWidth(message);
        int x = (getWidth() - textWidth) / 2;
        int y = getHeight() - (getHeight() - cellSize * 3) / 4 + 24;
        g2.drawString(message, x, y);
    }

    private void drawCurrentPlayer(Graphics2D g2) {
        if (gameEnded) return;

        g2.setColor(Color.MAGENTA.darker());
        g2.setFont(new Font("Arial", Font.BOLD, 32));
        g2.drawString("Turno del giocatore: " + currentPlayer, 20, 50);
    }

    private boolean isBoardFull() {
        for (char[] row : board) {
            for (char cell : row) {
                if (cell == '\0') return false;
            }
        }
        return true;
    }

    private boolean checkWin(char player) {
        for (int i = 0; i < 3; i++) {
            if (board[i][0] == player && board[i][1] == player && board[i][2] == player) {
                winningLine = new int[][]{{i, 0}, {i, 1}, {i, 2}};
                return true;
            }
            if (board[0][i] == player && board[1][i] == player && board[2][i] == player) {
                winningLine = new int[][]{{0, i}, {1, i}, {2, i}};
                return true;
            }
        }
        if (board[0][0] == player && board[1][1] == player && board[2][2] == player) {
            winningLine = new int[][]{{0, 0}, {1, 1}, {2, 2}};
            return true;
        }
        if (board[0][2] == player && board[1][1] == player && board[2][0] == player) {
            winningLine = new int[][]{{0, 2}, {1, 1}, {2, 0}};
            return true;
        }
        return false;
    }

    private static class Move {
        int row, col;
        char player;
        Move(int row, int col, char player) {
            this.row = row;
            this.col = col;
            this.player = player;
        }
    }

    public static void main(String[] args) {
        JFrame frame = new JFrame("Tris classico");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.getContentPane().add(new TrisDisegnatoMano());
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }
}
