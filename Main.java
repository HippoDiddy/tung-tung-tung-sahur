import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.Graphics2D;
import java.util.ArrayList;
import java.util.Random;

public class Main extends JFrame {
    private GamePanel gamePanel;

    public Main() {
        setTitle("🎮 TUNG TUNG TUNG SAHUR - BRAINROT EDITION 🎮");
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
    private ArrayList<Tung> fallingTungs;
    private ArrayList<Enemy> enemies;
    private ArrayList<GameMessage> gameMessages;
    private int score = 0;
    private double multiplier = 1.0;
    private boolean gameRunning = false;
    private boolean gamePaused = false;
    private Random random;
    private int tungCount = 0;
    private int missedTungs = 0;
    private int enemiesDefeated = 0;
    private long gameStartTime = 0;
    private long elapsedSeconds = 0;
    private KeyListener keyListener;
    private boolean[] keysPressed = new boolean[256];
    private int difficultyLevel = 1;

    public GamePanel() {
        setPreferredSize(new Dimension(900, 700));
        setBackground(new Color(10, 10, 20));
        setFocusable(true);

        player = new Player(400, 600);
        fallingTungs = new ArrayList<>();
        enemies = new ArrayList<>();
        gameMessages = new ArrayList<>();
        random = new Random();

        // Better key handling
        keyListener = new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                int code = e.getKeyCode();
                if (code < keysPressed.length) {
                    keysPressed[code] = true;
                }
                
                if (code == KeyEvent.VK_SPACE) {
                    if (!gameRunning) {
                        startGame();
                    } else if (gamePaused) {
                        gamePaused = false;
                    } else if (gameRunning) {
                        player.jump();
                    }
                }
                if (code == KeyEvent.VK_P && gameRunning) {
                    gamePaused = !gamePaused;
                }
                if (code == KeyEvent.VK_R) {
                    resetGame();
                }
            }

            @Override
            public void keyReleased(KeyEvent e) {
                int code = e.getKeyCode();
                if (code < keysPressed.length) {
                    keysPressed[code] = false;
                }
            }
        };

        addKeyListener(keyListener);

        // Game Loop with continuous input
        Timer gameTimer = new Timer(16, e -> {
            if (gameRunning && !gamePaused) {
                handleInput();
                update();
            }
            repaint();
        });
        gameTimer.start();

        // Spawn Tungs
        Timer spawnTimer = new Timer(800 - (int)(tungCount * 5), e -> {
            if (gameRunning && !gamePaused) {
                spawnTung();
            }
        });
        spawnTimer.start();

        // Spawn Enemies - gets harder over time
        Timer enemySpawnTimer = new Timer(3000, e -> {
            if (gameRunning && !gamePaused) {
                spawnEnemy();
            }
        });
        enemySpawnTimer.start();

        // Difficulty updater
        Timer difficultyTimer = new Timer(1000, e -> {
            if (gameRunning && !gamePaused) {
                elapsedSeconds++;
                difficultyLevel = 1 + (int)(elapsedSeconds / 10); // Increases every 10 seconds
            }
        });
        difficultyTimer.start();
    }

    private void handleInput() {
        // Smooth movement
        if (keysPressed[KeyEvent.VK_LEFT] || keysPressed['A']) {
            player.moveLeft();
        }
        if (keysPressed[KeyEvent.VK_RIGHT] || keysPressed['D']) {
            player.moveRight();
        }
    }

    private void startGame() {
        gameRunning = true;
        gamePaused = false;
        score = 0;
        multiplier = 1.0;
        tungCount = 0;
        missedTungs = 0;
        enemiesDefeated = 0;
        elapsedSeconds = 0;
        difficultyLevel = 1;
        fallingTungs.clear();
        enemies.clear();
        gameMessages.clear();
        gameStartTime = System.currentTimeMillis();
        playSound(400, 100);
        playSound(600, 100);
        playSound(800, 100);
    }

    private void resetGame() {
        gameRunning = false;
        gamePaused = false;
        score = 0;
        multiplier = 1.0;
        tungCount = 0;
        missedTungs = 0;
        enemiesDefeated = 0;
        elapsedSeconds = 0;
        difficultyLevel = 1;
        fallingTungs.clear();
        enemies.clear();
        gameMessages.clear();
        player.reset();
    }

    private void spawnTung() {
        int x = random.nextInt(800);
        fallingTungs.add(new Tung(x, -50));
    }

    private void spawnEnemy() {
        int x = random.nextInt(800);
        
        // Spawn chance increases with difficulty
        int spawnChance = Math.min(80, 20 + (difficultyLevel * 10));
        int roll = random.nextInt(100);
        
        if (roll < spawnChance) {
            // Decide which enemy type
            int enemyType = random.nextInt(2); // 0 = Ohio Sigma, 1 = Skibidi Rizzler
            
            if (enemyType == 0) {
                enemies.add(new OhioSigma(x, -50, difficultyLevel));
            } else {
                enemies.add(new SkibidiRizzler(x, -50, difficultyLevel));
            }
        }
    }

    private void update() {
        // Update Player
        player.update();

        // Update Tungs
        for (int i = 0; i < fallingTungs.size(); i++) {
            Tung tung = fallingTungs.get(i);
            tung.update();

            // Collision mit Player
            if (tung.y > player.y - 10 && tung.y < player.y + 50 &&
                tung.x > player.x - 30 && tung.x < player.x + 80) {
                
                int points = (int) (10 * multiplier);
                score += points;
                multiplier += 0.2;
                tungCount++;
                
                addGameMessage("+" + points + "pts! x" + String.format("%.1f", multiplier), new Color(0, 255, 0));
                playSound(800, 30);
                playSound(1200, 30);
                
                fallingTungs.remove(i);
                i--;
            }
            // Tung missed
            else if (tung.y > 700) {
                multiplier = Math.max(1.0, multiplier - 0.1);
                missedTungs++;
                
                addGameMessage("MISSED! Multiplier down", new Color(255, 0, 0));
                playSound(200, 100);
                
                fallingTungs.remove(i);
                i--;
            }
        }

        // Update Enemies
        for (int i = 0; i < enemies.size(); i++) {
            Enemy enemy = enemies.get(i);
            enemy.update();

            // Collision mit Player
            if (enemy.y > player.y - 20 && enemy.y < player.y + 50 &&
                enemy.x > player.x - 40 && enemy.x < player.x + 80) {
                
                // If jumping, defeat enemy
                if (player.isJumping && player.velocityY < 0) {
                    int points = (int) (50 * multiplier);
                    score += points;
                    multiplier += 0.5;
                    enemiesDefeated++;
                    
                    String enemyName = enemy instanceof OhioSigma ? "OHIO SIGMA" : "SKIBIDI RIZZLER";
                    addGameMessage(enemyName + " DEFEATED! +" + points, new Color(255, 100, 255));
                    playSound(1200, 50);
                    playSound(800, 50);
                    
                    enemies.remove(i);
                    i--;
                } else {
                    // Hit by enemy - lose multiplier
                    multiplier = Math.max(1.0, multiplier - 0.3);
                    addGameMessage("HIT! Multiplier down", new Color(255, 0, 0));
                    playSound(200, 100);
                    playSound(150, 100);
                }
            }
            // Enemy missed
            else if (enemy.y > 700) {
                multiplier = Math.max(1.0, multiplier - 0.15);
                addGameMessage("ENEMY ESCAPED!", new Color(255, 100, 0));
                playSound(200, 100);
                
                enemies.remove(i);
                i--;
            }
        }

        // Remove old messages
        for (int i = gameMessages.size() - 1; i >= 0; i--) {
            GameMessage msg = gameMessages.get(i);
            msg.age++;
            if (msg.age > 120) {
                gameMessages.remove(i);
            }
        }
    }

    private void addGameMessage(String text, Color color) {
        gameMessages.add(new GameMessage(text, color, player.x, 550));
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        // Animated Background Gradient
        float hue = (System.currentTimeMillis() % 3000) / 3000f;
        Color color1 = Color.getHSBColor(hue, 0.8f, 0.4f);
        Color color2 = Color.getHSBColor((hue + 0.3f) % 1f, 0.8f, 0.6f);
        
        GradientPaint gradient = new GradientPaint(0, 0, color1,
                getWidth(), getHeight(), color2);
        g2d.setPaint(gradient);
        g2d.fillRect(0, 0, getWidth(), getHeight());

        // Scanline Effect
        g2d.setColor(new Color(0, 0, 0, 20));
        for (int y = 0; y < getHeight(); y += 2) {
            g2d.drawLine(0, y, getWidth(), y);
        }

        // Border
        g2d.setColor(new Color(0, 255, 0));
        g2d.setStroke(new BasicStroke(6));
        g2d.drawRect(30, 30, getWidth() - 60, getHeight() - 60);

        // Inner Border
        g2d.setColor(new Color(255, 0, 255));
        g2d.setStroke(new BasicStroke(3));
        g2d.drawRect(35, 35, getWidth() - 70, getHeight() - 70);

        // Title
        g2d.setFont(new Font("Arial", Font.BOLD, 50));
        g2d.setColor(new Color(255, 255, 0));
        g2d.setStroke(new BasicStroke(3));
        
        String title = "🎮 TUNG TUNG TUNG SAHUR 🎮";
        FontMetrics fm = g2d.getFontMetrics();
        int titleX = (getWidth() - fm.stringWidth(title)) / 2;
        
        // Shadow effect
        g2d.setColor(new Color(255, 0, 255));
        g2d.drawString(title, titleX + 3, 90 + 3);
        
        g2d.setColor(new Color(255, 255, 0));
        g2d.drawString(title, titleX, 90);

        // Subtitle
        g2d.setFont(new Font("Arial", Font.PLAIN, 20));
        g2d.setColor(new Color(0, 255, 255));
        String subtitle = "CATCH TUNGS & DEFEAT ENEMIES! (no cap fr fr 💀)";
        g2d.drawString(subtitle, (getWidth() - fm.stringWidth(subtitle)) / 2, 130);

        // Game Area Background
        g2d.setColor(new Color(0, 0, 0));
        g2d.fillRect(50, 160, getWidth() - 100, 500);
        g2d.setColor(new Color(0, 255, 0));
        g2d.setStroke(new BasicStroke(3));
        g2d.drawRect(50, 160, getWidth() - 100, 500);

        if (gameRunning) {
            // Draw falling Tungs
            for (Tung tung : fallingTungs) {
                tung.draw(g2d);
            }

            // Draw Enemies
            for (Enemy enemy : enemies) {
                enemy.draw(g2d);
            }

            // Draw Player (Sahur character)
            player.draw(g2d);

            // Draw floating messages
            for (GameMessage msg : gameMessages) {
                msg.draw(g2d);
            }

            // Game Stats
            g2d.setFont(new Font("Arial", Font.BOLD, 28));
            g2d.setColor(new Color(255, 255, 0));
            g2d.drawString("SCORE: " + score, 80, 680);
            g2d.drawString("COMBO: " + String.format("%.1f", multiplier) + "x", 350, 680);
            g2d.drawString("DIFFICULTY: " + difficultyLevel, 650, 680);

            // Additional stats
            g2d.setFont(new Font("Arial", Font.BOLD, 20));
            g2d.setColor(new Color(100, 255, 100));
            g2d.drawString("CAUGHT: " + tungCount, 80, 720);
            g2d.setColor(new Color(255, 100, 100));
            g2d.drawString("ENEMIES: " + enemiesDefeated, 350, 720);
            g2d.setColor(new Color(255, 100, 100));
            g2d.drawString("MISSED: " + missedTungs, 600, 720);

            // Controls Help
            g2d.setFont(new Font("Arial", Font.PLAIN, 12));
            g2d.setColor(new Color(0, 255, 255));
            g2d.drawString("A/LEFT | D/RIGHT | SPACEBAR JUMP | P PAUSE | R RESET", 220, 720);

        } else {
            // Start Screen
            g2d.setFont(new Font("Arial", Font.BOLD, 40));
            g2d.setColor(new Color(255, 0, 255));
            String startText = "PRESS SPACEBAR TO START";
            g2d.drawString(startText, (getWidth() - fm.stringWidth(startText)) / 2, 280);

            // Instructions
            g2d.setFont(new Font("Arial", Font.PLAIN, 18));
            g2d.setColor(new Color(0, 255, 255));
            
            String[] instructions = {
                "HOW TO PLAY:",
                "• Move your SAHUR (green character) left & right with A/D",
                "• JUMP with SPACEBAR to defeat enemies!",
                "• CATCH the falling TUNG TUNG TUNGS (pink squares)",
                "• DEFEAT enemies: OHIO SIGMA 🔵 & SKIBIDI RIZZLER 🚽",
                "• Each caught tung = +10 points × multiplier",
                "• Each defeated enemy = +50 points × multiplier",
                "• Difficulty increases over time - enemies spawn more often!",
                "• BECOME THE ULTIMATE SIGMA MASTER! 🚀"
            };

            int y = 350;
            for (String instruction : instructions) {
                g2d.drawString(instruction, 100, y);
                y += 30;
            }

            // Brainrot Messages
            g2d.setFont(new Font("Arial", Font.ITALIC, 14));
            g2d.setColor(new Color(255, 255, 0));
            
            String[] messages = {
                "🌟 it's giving retro energy ✨",
                "💀 no shot you're still reading this",
                "🚽 skibidi fr fr",
                "📱 it's giving epic gamer vibes"
            };
            int msgIndex = (int) (System.currentTimeMillis() / 3000) % messages.length;
            g2d.drawString(messages[msgIndex], (getWidth() - fm.stringWidth(messages[msgIndex])) / 2, 690);
        }

        // Pause Screen
        if (gamePaused) {
            g2d.setColor(new Color(0, 0, 0, 150));
            g2d.fillRect(0, 0, getWidth(), getHeight());
            
            g2d.setFont(new Font("Arial", Font.BOLD, 60));
            g2d.setColor(new Color(255, 0, 255));
            String pauseText = "PAUSED";
            g2d.drawString(pauseText, (getWidth() - fm.stringWidth(pauseText)) / 2, getHeight() / 2);
            
            g2d.setFont(new Font("Arial", Font.PLAIN, 24));
            g2d.setColor(new Color(0, 255, 255));
            String resumeText = "Press SPACEBAR to resume";
            g2d.drawString(resumeText, (getWidth() - fm.stringWidth(resumeText)) / 2, getHeight() / 2 + 60);
        }
    }

    private void playSound(int frequency, int duration) {
        new Thread(() -> {
            try {
                System.out.print('\u0007');
            } catch (Exception e) {
                // Sound not available
            }
        }).start();
    }
}

class Player {
    public int x, y;
    public int velocityY = 0;
    public boolean isJumping = false;
    private static final int WIDTH = 60;
    private static final int HEIGHT = 50;
    private static final int SPEED = 8;
    private static final int JUMP_POWER = 15;
    private static final int GROUND_Y = 600;
    private static final int GRAVITY = 1;
    private int startX;
    private int startY;

    public Player(int x, int y) {
        this.x = x;
        this.y = y;
        this.startX = x;
        this.startY = y;
    }

    public void reset() {
        this.x = startX;
        this.y = startY;
        this.velocityY = 0;
        this.isJumping = false;
    }

    public void moveLeft() {
        if (x > 50) {
            x -= SPEED;
        }
    }

    public void moveRight() {
        if (x < 790) {
            x += SPEED;
        }
    }

    public void jump() {
        if (!isJumping && y >= GROUND_Y - 5) {
            velocityY = -JUMP_POWER;
            isJumping = true;
        }
    }

    public void update() {
        // Apply gravity
        velocityY += GRAVITY;
        y += velocityY;

        // Ground collision
        if (y >= GROUND_Y) {
            y = GROUND_Y;
            velocityY = 0;
            isJumping = false;
        }

        // Ceiling collision
        if (y < 160) {
            y = 160;
            velocityY = 0;
        }
    }

    public void draw(Graphics2D g) {
        // SAHUR CHARACTER DESIGN
        
        // Body (green rectangle)
        g.setColor(new Color(0, 255, 0));
        g.fillRect(x + 10, y + 15, 40, 25);
        g.setStroke(new BasicStroke(2));
        g.setColor(new Color(255, 255, 0));
        g.drawRect(x + 10, y + 15, 40, 25);

        // Head (circle on top)
        g.setColor(new Color(0, 255, 0));
        g.fillOval(x + 15, y, 30, 20);
        g.setColor(new Color(255, 255, 0));
        g.drawOval(x + 15, y, 30, 20);

        // Eyes
        g.setColor(new Color(255, 0, 255));
        g.fillOval(x + 20, y + 5, 5, 5);
        g.fillOval(x + 35, y + 5, 5, 5);

        // Mouth (happy/surprised based on jump)
        g.setColor(new Color(255, 0, 255));
        g.setStroke(new BasicStroke(2));
        if (isJumping) {
            g.drawOval(x + 27, y + 10, 6, 6); // Surprised face
        } else {
            g.drawArc(x + 20, y + 8, 20, 8, 0, 180); // Happy face
        }

        // Arms (reaching out to catch)
        g.setColor(new Color(0, 255, 0));
        g.setStroke(new BasicStroke(3));
        g.drawLine(x + 10, y + 20, x, y + 18);
        g.drawLine(x + 50, y + 20, x + 60, y + 18);

        // Feet
        g.setColor(new Color(255, 255, 0));
        g.fillRect(x + 15, y + 40, 8, 10);
        g.fillRect(x + 37, y + 40, 8, 10);

        // Label
        g.setFont(new Font("Arial", Font.BOLD, 12));
        g.setColor(new Color(0, 255, 255));
        g.drawString("SAHUR", x + 10, y + 65);
    }
}

// Abstract Enemy class
abstract class Enemy {
    public int x, y;
    public int velocityY;
    protected static final int GRAVITY = 1;
    protected int speed;
    protected int difficulty;

    public Enemy(int x, int y, int difficulty) {
        this.x = x;
        this.y = y;
        this.velocityY = 0;
        this.difficulty = difficulty;
        this.speed = 2 + difficulty;
    }

    public void update() {
        velocityY += GRAVITY;
        y += velocityY;
    }

    abstract void draw(Graphics2D g);
}

// Ohio Sigma Enemy
class OhioSigma extends Enemy {
    private Color color;
    private static final int SIZE = 30;

    public OhioSigma(int x, int y, int difficulty) {
        super(x, y, difficulty);
        this.color = new Color(100, 150, 255); // Blue
    }

    public void update() {
        super.update();
        x += (random.nextInt(3) - 1); // Random horizontal movement
    }

    @Override
    void draw(Graphics2D g) {
        // Head (blue circle)
        g.setColor(color);
        g.fillOval(x, y, SIZE, SIZE);
        
        g.setColor(new Color(0, 255, 0));
        g.setStroke(new BasicStroke(2));
        g.drawOval(x, y, SIZE, SIZE);

        // Eyes (angry)
        g.setColor(Color.BLACK);
        g.fillOval(x + 8, y + 8, 5, 5);
        g.fillOval(x + 17, y + 8, 5, 5);

        // Mouth (angry)
        g.setColor(Color.BLACK);
        g.setStroke(new BasicStroke(2));
        g.drawLine(x + 8, y + 20, x + 22, y + 20);

        // Label
        g.setFont(new Font("Arial", Font.BOLD, 11));
        g.setColor(new Color(100, 255, 255));
        g.drawString("OHIO", x + 3, y + 45);
    }

    private Random random = new Random();
}

// Skibidi Rizzler Enemy
class SkibidiRizzler extends Enemy {
    private Color color;
    private static final int SIZE = 28;
    private int wobbleCounter = 0;

    public SkibidiRizzler(int x, int y, int difficulty) {
        super(x, y, difficulty);
        this.color = new Color(255, 100, 150); // Pink/Red
    }

    public void update() {
        super.update();
        wobbleCounter++;
        x += Math.sin(wobbleCounter * 0.1) * 2; // Wobble movement
    }

    @Override
    void draw(Graphics2D g) {
        // Head (pink circle)
        g.setColor(color);
        g.fillOval(x, y, SIZE, SIZE);
        
        g.setColor(new Color(255, 255, 0));
        g.setStroke(new BasicStroke(2));
        g.drawOval(x, y, SIZE, SIZE);

        // Eyes (cool)
        g.setColor(Color.BLACK);
        g.fillOval(x + 6, y + 7, 6, 6);
        g.fillOval(x + 16, y + 7, 6, 6);
        g.fillOval(x + 8, y + 8, 3, 3); // Shine
        g.fillOval(x + 18, y + 8, 3, 3); // Shine

        // Mouth (cool grin)
        g.setColor(Color.BLACK);
        g.setStroke(new BasicStroke(2));
        g.drawArc(x + 5, y + 15, 18, 10, 0, 180);

        // Label
        g.setFont(new Font("Arial", Font.BOLD, 10));
        g.setColor(new Color(255, 200, 100));
        g.drawString("SKIBIDI", x - 3, y + 44);
    }
}

class Tung {
    public int x, y;
    private static final int SIZE = 25;
    private static final int SPEED = 3;
    private double rotation = 0;
    private Color color;

    public Tung(int x, int y) {
        this.x = x;
        this.y = y;
        // Random colors for variety
        Color[] colors = {
            new Color(255, 0, 255),
            new Color(255, 100, 200),
            new Color(200, 0, 255)
        };
        this.color = colors[new Random().nextInt(colors.length)];
    }

    public void update() {
        y += SPEED;
        rotation += 8;
    }

    public void draw(Graphics2D g) {
        // TUNG DESIGN - Spinning square with effects
        
        g.setColor(color);
        g.fillRect(x, y, SIZE, SIZE);
        
        g.setColor(new Color(255, 255, 0));
        g.setStroke(new BasicStroke(2));
        g.drawRect(x, y, SIZE, SIZE);

        // Inner decoration
        g.setColor(new Color(255, 255, 255));
        g.fillOval(x + 5, y + 5, 15, 15);
        
        g.setColor(new Color(255, 0, 255));
        g.setStroke(new BasicStroke(1));
        g.drawOval(x + 5, y + 5, 15, 15);

        // Label
        g.setFont(new Font("Arial", Font.BOLD, 10));
        g.setColor(new Color(255, 255, 0));
        g.drawString("TUNG", x - 5, y - 5);
    }
}

class GameMessage {
    public String text;
    public Color color;
    public int x, y;
    public int age = 0;

    public GameMessage(String text, Color color, int x, int y) {
        this.text = text;
        this.color = color;
        this.x = x;
        this.y = y;
    }

    public void draw(Graphics2D g) {
        float alpha = 1.0f - (age / 120f);
        g.setColor(new Color(color.getRed(), color.getGreen(), color.getBlue(), (int)(alpha * 255)));
        g.setFont(new Font("Arial", Font.BOLD, 16));
        g.drawString(text, x - 20, y - age);
    }
}
