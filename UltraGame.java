import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.*;
import javax.swing.Timer;

public class UltraGame extends JPanel implements ActionListener, KeyListener, MouseListener {
    // Game Settings
    private final int WIDTH = 800, HEIGHT = 600;
    private boolean running = true;

    // Player Stats
    private float px = 400, py = 300, pvx = 0, pvy = 0;
    private int health = 100, style = 0;
    private float dashCooldown = 0, dashDuration = 0;
    private Set<Integer> keys = new HashSet<>();

    // Entities
    private ArrayList<Bullet> bullets = new ArrayList<>();
    private ArrayList<Enemy> enemies = new ArrayList<>();
    private ArrayList<Particle> particles = new ArrayList<>();

    public UltraGame() {
        setPreferredSize(new Dimension(WIDTH, HEIGHT));
        setBackground(Color.BLACK);
        setFocusable(true);
        addKeyListener(this);
        addMouseListener(this);

        // Main Game
        new Timer(16, this).start();
        spawnEnemy();
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (!running) return;
        update();
        repaint();
    }

    private void update() {
        float speed = 5.0f;
        if (dashDuration > 0) { speed = 15.0f; dashDuration--; }

        if (keys.contains(KeyEvent.VK_W)) pvy = -speed;
        else if (keys.contains(KeyEvent.VK_S)) pvy = speed;
        else pvy = 0;

        if (keys.contains(KeyEvent.VK_A)) pvx = -speed;
        else if (keys.contains(KeyEvent.VK_D)) pvx = speed;
        else pvx = 0;

        px += pvx; py += pvy;
        if (dashCooldown > 0) dashCooldown--;
        if (style > 0) style -= 1; // Style Decay

        //  Dash
        if (keys.contains(KeyEvent.VK_SHIFT) && dashCooldown <= 0) {
            dashDuration = 10;
            dashCooldown = 40;
            addStyle(50, "BOOST");
        }

        //  Bullets
        for (int i = bullets.size() - 1; i >= 0; i--) {
            bullets.get(i).update();
            if (bullets.get(i).offscreen()) bullets.remove(i);
        }

        // bad guys
        if (Math.random() < 0.02) spawnEnemy();
        for (int i = enemies.size() - 1; i >= 0; i--) {
            Enemy en = enemies.get(i);
            en.update(px, py);

            // Check Hits karne wala part
            for (int j = bullets.size() - 1; j >= 0; j--) {
                if (en.getBounds().intersects(bullets.get(j).getBounds())) {
                    en.hp -= 20;
                    bullets.remove(j);
                    spawnParticles(en.x, en.y, Color.RED);
                }
            }

            if (en.hp <= 0) {
                // heal hoga if killed near player
                float dist = (float)Math.hypot(px - en.x, py - en.y);
                if (dist < 100) {
                    health = Math.min(100, health + 20);
                    addStyle(200, "HEALTH replenish");
                } else {
                    addStyle(100, "K-k-KILL");
                }
                enemies.remove(i);
            }
        }
    }

    private void addStyle(int pts, String msg) {
        style += pts;
        System.out.println("+ " + msg + " [" + getRank() + "]");
    }

    private String getRank() {
        if (style > 3000) return "SSS+";
        if (style > 2000) return "SSS";
        if (style > 1000) return "S";
        if (style > 500) return "B";
        return "D";
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;

        // Player modle
        g2.setColor(dashDuration > 0 ? Color.CYAN : Color.WHITE);
        g2.fillRect((int)px-10, (int)py-10, 20, 20);

        // lal ladke
        g2.setColor(Color.RED);
        for (Enemy en : enemies) g2.fillRect((int)en.x-15, (int)en.y-15, 30, 30);

        // dischkyaooo
        g2.setColor(Color.YELLOW);
        for (Bullet b : bullets) g2.fillOval((int)b.x-3, (int)b.y-3, 6, 6);

        // interface
        g2.setColor(Color.WHITE);
        g2.setFont(new Font("Monospaced", Font.BOLD, 20));
        g2.drawString("HEALTH: " + health, 20, 40);
        g2.setColor(Color.ORANGE);
        g2.drawString("STYLE: " + getRank() + " (" + style + ")", 20, 70);
    }

    private void spawnEnemy() { enemies.add(new Enemy((float)Math.random()*WIDTH, (float)Math.random()*HEIGHT)); }
    private void spawnParticles(float x, float y, Color c) { /* Visual only */ }
    public void keyPressed(KeyEvent e) { keys.add(e.getKeyCode()); }
    public void keyReleased(KeyEvent e) { keys.remove(e.getKeyCode()); }
    public void keyTyped(KeyEvent e) {}
    public void mousePressed(MouseEvent e) {
        float angle = (float)Math.atan2(e.getY() - py, e.getX() - px);
        bullets.add(new Bullet(px, py, angle));
    }
    public void mouseReleased(MouseEvent e) {}
    public void mouseEntered(MouseEvent e) {}
    public void mouseExited(MouseEvent e) {}
    public void mouseClicked(MouseEvent e) {}

    public static void main(String[] args) {
        JFrame f = new JFrame("GAME-by-divyam");
        f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        f.add(new UltraGame());
        f.pack();
        f.setVisible(true);
    }
}

class Bullet {
    float x, y, angle, speed = 12.0f;
    Bullet(float x, float y, float a) { this.x = x; this.y = y; this.angle = a; }
    void update() { x += Math.cos(angle) * speed; y += Math.sin(angle) * speed; }
    boolean offscreen() { return x < 0 || x > 800 || y < 0 || y > 600; }
    Rectangle getBounds() { return new Rectangle((int)x-3, (int)y-3, 6, 6); }
}

class Enemy {
    float x, y, hp = 40;
    Enemy(float x, float y) { this.x = x; this.y = y; }
    void update(float px, float py) {
        float angle = (float)Math.atan2(py - y, px - x);
        x += Math.cos(angle) * 1.5f;
        y += Math.sin(angle) * 1.5f;
    }
    Rectangle getBounds() { return new Rectangle((int)x-15, (int)y-15, 30, 30); }
}

