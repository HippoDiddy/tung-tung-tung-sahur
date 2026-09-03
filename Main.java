import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.Graphics2D;
import java.util.ArrayList;
import java.util.Random;

public class Main extends JFrame {
    private GamePanel gamePanel;

    public Main() {
        setTitle("🎮 TUNG TUNG TUNG SAHUR 🎮");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setResizable(false);
        
        gamePanel = new GamePanel();
        add(gamePanel);
        
        pack();
        setLocationRelativeTo(null);
        setVisible(true);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new Main());
    }
}

class GamePanel extends JPanel {
    private Player player;
    private ArrayList<Tung> tungs;
    private int score = 0;
    private double multiplier = 1.0;
    private boolean gameRunning = false;
    private boolean gamePaused = false;
    private Random random;
    private long lastSpawnTime = 0;
    private int tungCount = 0;

    public GamePanel() {
        setPreferredSize(new Dimension(800, 600));
        setBackground(new Color(20, 20, 40));
        setFocusable(true);

        player = new Player(370, 540);
        tungs = new ArrayList<>();
        random = new Random();

        addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_LEFT) {
                    player.moveLeft();
                }
                if (e.getKeyCode() == KeyEvent.VK_RIGHT) {
                    player.moveRight();
                }
                if (e.getKeyCode() == KeyEvent.VK_SPACE) {
                    if (!gameRunning) {
                        startGame();
                    }
                }
            }
        });

        // Game Loop
        Timer gameTimer = new Timer(20, e -> {
            if (gameRunning && !gamePaused) {
                update();
            }
            repaint();
        });
        gameTimer.start();

        // Spawn Tungs
        Timer spawnTimer = new Timer(1000, e -> {
            if (gameRunning && !gamePaused) {
                spawnTung();
            }
        });
        spawnTimer.start();
    }

    private void startGame() {
        gameRunning = true;
        gamePaused = false;
        score = 0;
        multiplier = 1.0;
        tungCount = 0;
        tungs.clear();
        playSound(400, 100);
        playSound(600, 100);
    }

    private void spawnTung() {
        int x = random.nextInt(750);
        tungs.add(new Tung(x, -30));
    }

    private void update() {
        // Update Tungs
        for (int i = 0; i < tungs.size(); i++) {
            Tung tung = tungs.get(i);
            tung.update();

            // Collision mit Player
            if (tung.y > 520 && tung.y < 580 &&
                tung.x > player.x - 20 && tung.x < player.x + 60) {
                score += (int) (10 * multiplier);
                multiplier += 0.5;
                tungCount++;
                playSound(800, 50);
                playSound(1200, 50);
                tungs.remove(i);
                i--;
            }
            // Tung verschwunden
            else if (tung.y > 600) {
                multiplier = Math.max(1, multiplier - 0.3);
                playSound(200, 100);
                tungs.remove(i);
                i--;
            }
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // Background Gradient
        GradientPaint gradient = new GradientPaint(0, 0, new Color(102, 126, 234),
                getWidth(), getHeight(), new Color(240, 147, 251));
        g2d.setPaint(gradient);
        g2d.fillRect(0, 0, getWidth(), getHeight());

        // Border
        g2d.setColor(new Color(0, 255, 0));
        g2d.setStroke(new BasicStroke(5));
        g2d.drawRect(0, 0, getWidth() - 1, getHeight() - 1);

        // Title
        g2d.setFont(new Font("Arial", Font.BOLD, 40));
        g2d.setColor(new Color(255, 255, 0));
        FontMetrics fm = g2d.getFontMetrics();
        String title = "🎮 TUNG TUNG TUNG 🎮";
        g2d.drawString(title, (getWidth() - fm.stringWidth(title)) / 2, 50);

        // Subtitle
        g2d.setFont(new Font("Arial", Font.PLAIN, 16));
        g2d.setColor(new Color(0, 255, 255));
        String subtitle = "sahur edition (no cap fr fr 💀)";
        g2d.drawString(subtitle, (getWidth() - fm.stringWidth(subtitle)) / 2, 80);

        // Game Area
        g2d.setColor(new Color(0, 0, 0));
        g2d.fillRect(50, 120, 700, 400);
        g2d.setColor(new Color(0, 255, 0));
        g2d.setStroke(new BasicStroke(3));
        g2d.drawRect(50, 120, 700, 400);

        // Draw Player
        if (gameRunning) {
            player.draw(g2d);

            // Draw Tungs
            for (Tung tung : tungs) {
                tung.draw(g2d);
            }
        }

        // Score
        g2d.setFont(new Font("Arial", Font.BOLD, 24));
        g2d.setColor(new Color(255, 255, 0));
        g2d.drawString("SCORE: " + score, 80, 560);
        g2d.drawString("MULTIPLIER: " + String.format("%.1f", multiplier) + "x", 350, 560);

        // Controls
        g2d.setFont(new Font("Arial", Font.PLAIN, 14));
        g2d.setColor(new Color(0, 255, 255));
        g2d.drawString("⬅️ LEFT | ➡️ RIGHT | SPACEBAR TO START", 150, 590);

        // Brainrot Text
        if (!gameRunning) {
            g2d.setFont(new Font("Arial", Font.BOLD, 20));
            g2d.setColor(new Color(255, 0, 255));
            String startText = "PRESS SPACEBAR TO START";
            g2d.drawString(startText, (getWidth() - fm.stringWidth(startText)) / 2, 300);
        }

        // Brainrot Messages
        g2d.setFont(new Font("Arial", Font.ITALIC, 12));
        g2d.setColor(new Color(255, 255, 0));
        String[] messages = {
            "it's giving retro energy ✨",
            "no shot you're still playing 💀",
            "skibidi fr fr 🚽"
        };
        int msgIndex = (int) (System.currentTimeMillis() / 3000) % messages.length;
        g2d.drawString(messages[msgIndex], 250, 535);
    }

    private void playSound(int frequency, int duration) {
        new Thread(() -> {
            try {
                // Beep sound mit System.out
                System.out.print('\u0007'); // Bell character
            } catch (Exception e) {
                // Kein Sound verfügbar
            }
        }).start();
    }
}

class Player {
    public int x, y;
    private static final int WIDTH = 50;
    private static final int HEIGHT = 50;
    private static final int SPEED = 15;

    public Player(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public void moveLeft() {
        if (x > 50) {
            x -= SPEED;
        }
    }

    public void moveRight() {
        if (x < 750) {
            x += SPEED;
        }
    }

    public void draw(Graphics2D g) {
        g.setColor(new Color(0, 255, 0));
        g.fillRect(x, y, WIDTH, HEIGHT);
        g.setColor(new Color(255, 255, 255));
        g.setStroke(new BasicStroke(2));
        g.drawRect(x, y, WIDTH, HEIGHT);
    }
}

class Tung {
    public int x, y;
    private static final int SIZE = 30;
    private static final int SPEED = 3;
    private double rotation = 0;

    public Tung(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public void update() {
        y += SPEED;
        rotation += 5;
    }

    public void draw(Graphics2D g) {
        g.setColor(new Color(255, 0, 255));

        // Rotated square
        int[] xPoints = {x, x + SIZE, x + SIZE, x};
        int[] yPoints = {y, y, y + SIZE, y + SIZE};

        g.fillPolygon(xPoints, yPoints, 4);
        g.setColor(new Color(255, 255, 0));
        g.setStroke(new BasicStroke(2));
        g.drawPolygon(xPoints, yPoints, 4);
    }
}
