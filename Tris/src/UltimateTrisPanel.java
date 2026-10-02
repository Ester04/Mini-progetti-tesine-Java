package org.example;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Stack;

public class UltimateTrisPanel extends JPanel {

    private final int cellSize = 70; // dimensione cella piccolo tris
    private final int smallGridSize = 3 * cellSize; // dimensione sotto-griglia
    private final int bigGridSize = 3 * smallGridSize; // dimensione griglia globale
    private final char[][][] boards = new char[3][3][9]; // 3x3 sotto-griglie con 9 celle ciascuna
    private final boolean[][] smallGridWon = new boolean[3][3]; // segnare se sotto-griglia è già vinta
    private final char[][] smallGridWinner = new char[3][3]; // chi ha vinto sotto-griglia ('X','O' o '\0')

    private char currentPlayer = 'X';
    private int activeSmallGridRow = -1; // sotto-griglia attiva per la prossima mossa (-1 = qualsiasi)
    private int activeSmallGridCol = -1;

    private boolean gameEnded = false;
    private char gameWinner = '\0';

    private final JButton menuButton;
    private final JButton undoButton;

    private final Stack<Move> movesHistory = new Stack<>();

    public UltimateTrisPanel() {
        setPreferredSize(new Dimension(1000, 1000));
        setBackground(Color.WHITE);
        setFont(new Font("Arial", Font.BOLD, 20));
        setLayout(null); // layout nullo per posizionare pulsanti in alto a destra

        // Pulsante Menu
        menuButton = createStyledButton("Menu");
        menuButton.addActionListener(e -> {
            JFrame topFrame = (JFrame) SwingUtilities.getWindowAncestor(this);
            topFrame.dispose();
            SwingUtilities.invokeLater(() -> new TrisLauncher().setVisible(true));
        });
        add(menuButton);

        // Pulsante Undo (freccetta)
        undoButton = createStyledButton("←");
        undoButton.setFont(new Font("Arial", Font.BOLD, 28)); // freccia più grande
        undoButton.addActionListener(e -> undoLastMove());
        add(undoButton);

        // Aggiorna posizione pulsanti quando il panel viene ridimensionato
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

                int offsetX = (getWidth() - bigGridSize) / 2;
                int offsetY = (getHeight() - bigGridSize) / 2;

                int x = e.getX() - offsetX;
                int y = e.getY() - offsetY;

                if (x < 0 || y < 0 || x >= bigGridSize || y >= bigGridSize) return;

                int bigCol = x / smallGridSize;
                int bigRow = y / smallGridSize;

                if (activeSmallGridRow != -1 && activeSmallGridCol != -1) {
                    if (bigRow != activeSmallGridRow || bigCol != activeSmallGridCol) return;
                }

                int smallX = x % smallGridSize;
                int smallY = y % smallGridSize;

                int cellCol = smallX / cellSize;
                int cellRow = smallY / cellSize;

                int cellIndex = cellRow * 3 + cellCol;

                if (boards[bigRow][bigCol][cellIndex] != '\0' || smallGridWon[bigRow][bigCol]) return;

                // salva mossa per Undo (include la sotto-griglia attiva precedente)
                movesHistory.push(new Move(
                        bigRow,
                        bigCol,
                        cellIndex,
                        currentPlayer,
                        activeSmallGridRow,
                        activeSmallGridCol
                ));

                // segna mossa
                boards[bigRow][bigCol][cellIndex] = currentPlayer;

                // controlla se la sotto-griglia è vinta
                if (checkSmallGridWin(bigRow, bigCol, currentPlayer)) {
                    smallGridWon[bigRow][bigCol] = true;
                    smallGridWinner[bigRow][bigCol] = currentPlayer;
                }

                // controlla vittoria globale
                if (checkGlobalWin(currentPlayer)) {
                    gameEnded = true;
                    gameWinner = currentPlayer;
                } else if (isGlobalBoardFull()) {
                    gameEnded = true;
                    gameWinner = '\0'; // pareggio
                }

                // aggiorna la sotto-griglia attiva
                activeSmallGridRow = cellRow;
                activeSmallGridCol = cellCol;

                if (activeSmallGridRow != -1 && (smallGridWon[activeSmallGridRow][activeSmallGridCol]
                        || isSmallGridFull(activeSmallGridRow, activeSmallGridCol))) {
                    activeSmallGridRow = -1;
                    activeSmallGridCol = -1;
                }

                currentPlayer = (currentPlayer == 'X') ? 'O' : 'X';
                repaint();
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
        boards[last.bigRow][last.bigCol][last.cellIndex] = '\0';
        currentPlayer = last.player;

        // Ripristina la sotto-griglia attiva precedente
        activeSmallGridRow = last.prevActiveRow;
        activeSmallGridCol = last.prevActiveCol;

        // Ripristina stato sotto-griglia vinta
        for (int r = 0; r < 3; r++) {
            for (int c = 0; c < 3; c++) {
                smallGridWon[r][c] = checkSmallGridWin(r, c, 'X') || checkSmallGridWin(r, c, 'O');
                if (smallGridWon[r][c]) {
                    // simbolo centrale se presente
                    smallGridWinner[r][c] = boards[r][c][4] != '\0' ? boards[r][c][4] : '\0';
                } else {
                    smallGridWinner[r][c] = '\0';
                }
            }
        }

        gameEnded = false;
        gameWinner = '\0';

        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;

        int offsetX = (getWidth() - bigGridSize) / 2;
        int offsetY = (getHeight() - bigGridSize) / 2;

        drawBackgroundGrid(g2);

        // disegna tutte le sotto-griglie e le mosse
        for (int bigRow = 0; bigRow < 3; bigRow++) {
            for (int bigCol = 0; bigCol < 3; bigCol++) {
                int startX = offsetX + bigCol * smallGridSize;
                int startY = offsetY + bigRow * smallGridSize;

                drawSmallGrid(g2, startX, startY, bigRow, bigCol);
            }
        }

        // disegna la griglia grande nera (più spessa)
        g2.setColor(Color.BLACK);
        g2.setStroke(new BasicStroke(5));
        for (int i = 1; i < 3; i++) {
            g2.drawLine(offsetX, offsetY + i * smallGridSize, offsetX + bigGridSize, offsetY + i * smallGridSize);
            g2.drawLine(offsetX + i * smallGridSize, offsetY, offsetX + i * smallGridSize, offsetY + bigGridSize);
        }

        // evidenzia la sotto-griglia attiva
        if (activeSmallGridRow != -1 && activeSmallGridCol != -1) {
            g2.setColor(new Color(0, 128, 255, 80));
            g2.fillRect(offsetX + activeSmallGridCol * smallGridSize, offsetY + activeSmallGridRow * smallGridSize, smallGridSize, smallGridSize);
        }

        // testo stato partita
        g2.setColor(Color.BLACK);
        g2.setFont(new Font("Arial", Font.BOLD, 32));
        String status;
        if (gameEnded) {
            if (gameWinner == '\0') status = "Pareggio!";
            else {
                status = "Vittoria di " + gameWinner + "!";
                g2.setColor(Color.MAGENTA.darker());
                g2.setFont(new Font("Arial", Font.BOLD, 60));
            }
        } else {
            if (activeSmallGridRow != -1)
                status = "Gioca in sotto-griglia [" + (activeSmallGridRow+1) + "," + (activeSmallGridCol+1) + "]";
            else status = "Giocata libera";
        }
        FontMetrics fm = g2.getFontMetrics();
        int w = fm.stringWidth(status);
        g2.drawString(status, (getWidth() - w) / 2, offsetY + bigGridSize + offsetY/2 + 12);

        drawCurrentPlayer(g2);
    }

    private void drawBackgroundGrid(Graphics2D g2) {
        g2.setColor(new Color(220, 220, 220));
        int spacing = 20;
        for (int x = 0; x < getWidth(); x += spacing) g2.drawLine(x, 0, x, getHeight());
        for (int y = 0; y < getHeight(); y += spacing) g2.drawLine(0, y, getWidth(), y);
    }

    private void drawSmallGrid(Graphics2D g2, int startX, int startY, int bigRow, int bigCol) {
        g2.setColor(Color.BLACK);
        g2.setStroke(new BasicStroke(2));
        for (int i = 1; i < 3; i++) {
            g2.drawLine(startX + i * cellSize, startY, startX + i * cellSize, startY + smallGridSize);
            g2.drawLine(startX, startY + i * cellSize, startX + smallGridSize, startY + i * cellSize);
        }

        char[] smallBoard = boards[bigRow][bigCol];
        for (int i = 0; i < 9; i++) {
            int r = i / 3;
            int c = i % 3;
            char mark = smallBoard[i];
            if (mark != '\0') {
                if (mark == 'X') {
                    g2.setColor(Color.RED);
                    g2.setStroke(new BasicStroke(4));
                    int x1 = startX + c * cellSize + 8;
                    int y1 = startY + r * cellSize + 8;
                    int x2 = startX + (c + 1) * cellSize - 8;
                    int y2 = startY + (r + 1) * cellSize - 8;
                    g2.drawLine(x1, y1, x2, y2);
                    g2.drawLine(x1, y2, x2, y1);
                } else {
                    g2.setColor(Color.BLUE);
                    g2.setStroke(new BasicStroke(4));
                    int cx = startX + c * cellSize + cellSize / 2;
                    int cy = startY + r * cellSize + cellSize / 2;
                    g2.drawOval(cx - 30, cy - 30, 60, 60);
                }
            }
        }

        if (smallGridWon[bigRow][bigCol]) {
            g2.setColor(new Color(72,61,139, 80));
            g2.fillRect(startX, startY, smallGridSize, smallGridSize);
            g2.setColor(smallGridWinner[bigRow][bigCol] == 'X' ? Color.RED : Color.BLUE);
            g2.setStroke(new BasicStroke(12));
            int centerX = startX + smallGridSize / 2;
            int centerY = startY + smallGridSize / 2;
            int size = cellSize * 2;
            if (smallGridWinner[bigRow][bigCol] == 'X') {
                g2.drawLine(centerX - size / 2, centerY - size / 2, centerX + size / 2, centerY + size / 2);
                g2.drawLine(centerX - size / 2, centerY + size / 2, centerX + size / 2, centerY - size / 2);
            } else {
                g2.drawOval(centerX - size / 2, centerY - size / 2, size, size);
            }
        }
    }

    private void drawCurrentPlayer(Graphics2D g2) {
        if (gameEnded) return;
        g2.setColor(Color.MAGENTA.darker());
        g2.setFont(new Font("Arial", Font.BOLD, 32));
        g2.drawString("Turno del giocatore: " + currentPlayer, 20, 52);
    }

    private boolean checkSmallGridWin(int bigRow, int bigCol, char player) {
        char[] b = boards[bigRow][bigCol];
        int[][] lines = {
                {0,1,2},{3,4,5},{6,7,8},
                {0,3,6},{1,4,7},{2,5,8},
                {0,4,8},{2,4,6}
        };
        for (int[] line : lines) {
            if (b[line[0]] == player && b[line[1]] == player && b[line[2]] == player) return true;
        }
        return false;
    }

    private boolean checkGlobalWin(char player) {
        int[][] lines = {
                {0,0, 0,1, 0,2},
                {1,0, 1,1, 1,2},
                {2,0, 2,1, 2,2},
                {0,0, 1,0, 2,0},
                {0,1, 1,1, 2,1},
                {0,2, 1,2, 2,2},
                {0,0, 1,1, 2,2},
                {0,2, 1,1, 2,0},
        };
        for (int i = 0; i < lines.length; i++) {
            int r1 = lines[i][0], c1 = lines[i][1];
            int r2 = lines[i][2], c2 = lines[i][3];
            int r3 = lines[i][4], c3 = lines[i][5];
            if (smallGridWon[r1][c1] && smallGridWon[r2][c2] && smallGridWon[r3][c3]) {
                char w1 = smallGridWinner[r1][c1];
                char w2 = smallGridWinner[r2][c2];
                char w3 = smallGridWinner[r3][c3];
                if (w1 == player && w2 == player && w3 == player) return true;
            }
        }
        return false;
    }

    private boolean isSmallGridFull(int bigRow, int bigCol) {
        char[] b = boards[bigRow][bigCol];
        for (char c : b) if (c == '\0') return false;
        return true;
    }

    private boolean isGlobalBoardFull() {
        for (int r = 0; r < 3; r++)
            for (int c = 0; c < 3; c++)
                if (!smallGridWon[r][c] && !isSmallGridFull(r,c))
                    return false;
        return true;
    }

    private static class Move {
        int bigRow, bigCol, cellIndex;
        char player;
        int prevActiveRow, prevActiveCol;

        Move(int bigRow, int bigCol, int cellIndex, char player, int prevActiveRow, int prevActiveCol) {
            this.bigRow = bigRow;
            this.bigCol = bigCol;
            this.cellIndex = cellIndex;
            this.player = player;
            this.prevActiveRow = prevActiveRow;
            this.prevActiveCol = prevActiveCol;
        }
    }

    public static void main(String[] args) {
        JFrame frame = new JFrame("Super Tris (Ultimate Tic-Tac-Toe)");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.getContentPane().add(new UltimateTrisPanel());
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }
}
