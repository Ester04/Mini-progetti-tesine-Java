package org.example;

import javax.swing.*;
import java.awt.*;

public class TrisLauncher extends JFrame {

    public TrisLauncher() {
        // Titolo TRIS con lettere colorate alternate
        JLabel trisTitle = new JLabel();
        trisTitle.setAlignmentX(Component.CENTER_ALIGNMENT);
        trisTitle.setFont(new Font("Arial", Font.BOLD, 96));
        trisTitle.setHorizontalAlignment(SwingConstants.CENTER);

        // Costruzione del titolo colorato (blu e rosso alternati)
        String[] lettere = {"T", "R", "I", "S"};
        Color[] colori = {Color.BLUE, Color.RED, Color.BLUE, Color.RED};
        StringBuilder html = new StringBuilder("<html><div style='text-align: center;'>");
        for (int i = 0; i < lettere.length; i++) {
            String hex = String.format("#%02x%02x%02x", colori[i].getRed(), colori[i].getGreen(), colori[i].getBlue());
            html.append("<span style='color:").append(hex).append(";'>")
                    .append(lettere[i]).append("</span> ");
        }
        html.append("</div></html>");
        trisTitle.setText(html.toString());

        setTitle("Benvenuto in Tris");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1000, 1000);

        setExtendedState(JFrame.MAXIMIZED_BOTH);
        setUndecorated(true);

        setLocationRelativeTo(null);

        BackgroundPanel panel = new BackgroundPanel();
        panel.setLayout(new GridBagLayout());

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.CENTER;
        gbc.insets = new Insets(20, 40, 20, 40);

        JPanel contentPanel = new JPanel();
        contentPanel.add(trisTitle);
        contentPanel.add(Box.createRigidArea(new Dimension(0, 40)));

        contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
        contentPanel.setOpaque(false);
        contentPanel.setBorder(BorderFactory.createEmptyBorder(80, 80, 60, 80));

        JLabel title = new JLabel("Scegli la versione di Tris...");
        title.setAlignmentX(Component.CENTER_ALIGNMENT);
        title.setFont(new Font("Arial", Font.BOLD, 48));
        title.setForeground(Color.DARK_GRAY);

        JPanel classicPanel = createOptionRow("Tris Classico", this::showClassicRules, this::launchClassic, 1);
        JPanel ultimatePanel = createOptionRow("Super Tris", this::showUltimateRules, this::launchUltimate, 2);
        JPanel treGiocatoriPanel = createOptionRow("Tris 3 Giocatori", this::showTreGiocatoriRules, this::launchTreGiocatori, 3);

        contentPanel.add(title);
        contentPanel.add(Box.createRigidArea(new Dimension(0, 60)));
        contentPanel.add(classicPanel);
        contentPanel.add(Box.createRigidArea(new Dimension(0, 30)));
        contentPanel.add(ultimatePanel);
        contentPanel.add(Box.createRigidArea(new Dimension(0, 30)));
        contentPanel.add(treGiocatoriPanel);

        panel.add(contentPanel, gbc);
        setContentPane(panel);
    }

    private JPanel createOptionRow(String label, Runnable infoAction, Runnable startAction, int index) {
        JPanel row = new JPanel();
        row.setOpaque(false);
        row.setLayout(new FlowLayout(FlowLayout.CENTER, 20, 0));
        row.setAlignmentX(Component.CENTER_ALIGNMENT);

        JButton playBtn = new JButton(label);
        styleButton(playBtn, index);
        playBtn.addActionListener(e -> startAction.run());

        JButton infoBtn = new CircularButton("?");
        infoBtn.setToolTipText("Mostra le regole di " + label);
        infoBtn.addActionListener(e -> infoAction.run());

        row.add(playBtn);
        row.add(infoBtn);
        return row;
    }

    private void styleButton(JButton button, int index) {
        button.setFont(new Font("Arial", Font.PLAIN, 36));
        button.setFocusPainted(false);

        Color bgColor = (index % 2 == 0) ? new Color(173, 216, 230) : new Color(255, 182, 193); // azzurro o rosa
        Color borderColor = (index % 2 == 0) ? Color.BLUE : Color.RED;

        button.setBackground(bgColor);
        button.setOpaque(true);
        button.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(borderColor, 4, true),
                BorderFactory.createEmptyBorder(20, 40, 20, 40)
        ));
        button.setAlignmentX(Component.CENTER_ALIGNMENT);
    }

    private void showClassicRules() {
        JOptionPane.showMessageDialog(this,
                """
                Regole Tris Classico:
                - Due giocatori si alternano a mettere X e O su una griglia 3x3.
                - Vince chi allinea per primo tre simboli in orizzontale, verticale o diagonale.
                - Se la griglia si riempie senza vincitori, è pareggio.
                """,
                "Regole Tris Classico",
                JOptionPane.INFORMATION_MESSAGE
        );
    }

    private void showUltimateRules() {
        JOptionPane.showMessageDialog(this,
                """
                Regole Super Tris (Ultimate Tic-Tac-Toe):
                - Il campo di gioco è un 3x3 formato da 9 sotto-griglie 3x3.
                - Ogni mossa determina la sotto-griglia dove l’avversario dovrà giocare.
                - Se giochi in una cella (riga,colonna), il tuo avversario deve giocare nella sotto-griglia corrispondente.
                - Vinci ottenendo 3 sotto-griglie vinte in fila come nel tris classico.
                """,
                "Regole Super Tris",
                JOptionPane.INFORMATION_MESSAGE
        );
    }

    private void showTreGiocatoriRules() {
        JOptionPane.showMessageDialog(this,
                """
                Regole Tris 3 Giocatori (4x4):
                - Tre giocatori con simboli unici (X, O, ▲).
                - I giocatori inseriscono i simboli alternandosi sempre allo stesso modo.
                - Vince chi allinea 3 simboli consecutivi (in qualsiasi direzione).
                Serve strategia per anticipare gli avversari!!
                """,
                "Regole Tris 3 Giocatori",
                JOptionPane.INFORMATION_MESSAGE
        );
    }

    private void launchClassic() {
        JFrame classicFrame = new JFrame("Tris Classico");
        classicFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        classicFrame.getContentPane().add(new TrisDisegnatoMano());
        classicFrame.setExtendedState(JFrame.MAXIMIZED_BOTH);
        classicFrame.setUndecorated(true);
        classicFrame.setVisible(true);
        dispose();
    }

    private void launchUltimate() {
        JFrame ultimateFrame = new JFrame("Super Tris");
        ultimateFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        ultimateFrame.getContentPane().add(new UltimateTrisPanel());
        ultimateFrame.setExtendedState(JFrame.MAXIMIZED_BOTH);
        ultimateFrame.setUndecorated(true);
        ultimateFrame.setVisible(true);
        dispose();
    }

    private void launchTreGiocatori() {
        JFrame treGiocatoriFrame = new JFrame("Tris a 3 Giocatori");
        treGiocatoriFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        treGiocatoriFrame.getContentPane().add(new TrisTreGiocatori4x4());
        treGiocatoriFrame.setExtendedState(JFrame.MAXIMIZED_BOTH); // Schermo intero
        treGiocatoriFrame.setUndecorated(true);
        treGiocatoriFrame.setVisible(true);
        dispose();
    }


    static class BackgroundPanel extends JPanel {
        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            g.setColor(new Color(230, 230, 230));
            int size = 40;
            for (int x = 0; x < getWidth(); x += size)
                g.drawLine(x, 0, x, getHeight());
            for (int y = 0; y < getHeight(); y += size)
                g.drawLine(0, y, getWidth(), y);
        }
    }

    static class CircularButton extends JButton {
        public CircularButton(String text) {
            super(text);
            setFont(new Font("Arial", Font.BOLD, 32));
            setFocusPainted(false);
            setContentAreaFilled(false);
            setBorderPainted(false);
            setPreferredSize(new Dimension(80, 80));
            setOpaque(false);
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(Color.LIGHT_GRAY);
            g2.fillOval(0, getHeight() / 4, getHeight() / 2 + 4, getHeight() / 2 + 4);
            g2.setColor(getForeground());
            FontMetrics fm = g2.getFontMetrics();
            int stringWidth = fm.stringWidth(getText());
            int stringHeight = fm.getAscent();
            g2.drawString(getText(), (getHeight() / 2 + 4 - stringWidth) / 2, (getHeight() + stringHeight) / 2);
            g2.dispose();
        }

        @Override
        public boolean contains(int x, int y) {
            double dx = x - getWidth() / 2.0;
            double dy = y - getHeight() / 2.0;
            return dx * dx + dy * dy <= Math.pow(getWidth() / 2.0, 2);
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new TrisLauncher().setVisible(true));
    }
}
