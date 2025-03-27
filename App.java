
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import javax.imageio.ImageIO;
import javax.swing.*;

class Bird {

    private int x, y, velocity;
    private final int jumpStrength = -12;
    private final int gravity = 1;
    private BufferedImage birdImage;

    public Bird(int x, int y) {
        this.x = x;
        this.y = y;
        this.velocity = 0;
        try {
            birdImage = ImageIO.read(new File("flappybird.png"));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void update() {
        velocity += gravity;
        y += velocity;
    }

    public void jump() {
        velocity = jumpStrength;
    }

    public void reset() {
        y = 290;
        velocity = 0;
    }

    public void draw(Graphics g) {
        g.drawImage(birdImage, x, y, 40, 30, null);
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public int getWidth() {
        return 40;
    }

    public int getHeight() {
        return 30;
    }
}

class Pipe {

    private int x, topY, bottomY;
    private static final int WIDTH = 60;
    private static final int HEIGHT = 400;
    private static final int GAP = 150;
    private static int SPEED = 3;
    private BufferedImage topPipeImage, bottomPipeImage;

    public Pipe(int startX) {
        x = startX;
        resetHeight();
        try {
            topPipeImage = ImageIO.read(new File("toppipe.png"));
            bottomPipeImage = ImageIO.read(new File("bottompipe.png"));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void resetHeight() {
        int randomHeight = (int) (Math.random() * 250) + 100; // Adjust for taller pipes
        topY = randomHeight - HEIGHT;
        bottomY = randomHeight + GAP;
    }

    public void move() {
        x -= SPEED;
        SPEED += 0.01;
    }

    public boolean isOffScreen() {
        return x + WIDTH < 0;
    }

    public void reset(int startX) {
        x = startX;
        resetHeight();
    }

    public void draw(Graphics g) {
        g.drawImage(topPipeImage, x, topY, WIDTH, HEIGHT, null);
        g.drawImage(bottomPipeImage, x, bottomY, WIDTH, HEIGHT, null);
    }

    public int getX() {
        return x;
    }

    public int getTopY() {
        return topY;
    }

    public int getBottomY() {
        return bottomY;
    }

    public void setSpeed(int speed) {
        SPEED = speed;
    }
}

class GamePanel extends JPanel implements ActionListener, KeyListener {

    private Timer timer;
    private Bird bird;
    private ArrayList<Pipe> pipes;
    private boolean gameOver;
    private int score;
    private BufferedImage backgroundImage;
    private boolean gameStarted = false;

    public GamePanel() {
        setPreferredSize(new Dimension(360, 640));
        setBackground(Color.CYAN);
        addKeyListener(this);
        setFocusable(true);

        try {
            backgroundImage = ImageIO.read(new File("flappybirdbg.png"));
        } catch (IOException e) {
            e.printStackTrace();
        }

        initGame();
        timer = new Timer(20, this);
    }

    private void initGame() {
        bird = new Bird(80, 290);
        pipes = new ArrayList<>();
        for (int i = 0; i < 3; i++) {
            pipes.add(new Pipe(400 + i * 200));
        }
        gameOver = false;
        score = 0;
        gameStarted = false;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (!gameOver) {
            bird.update();
            for (Pipe pipe : pipes) {
                pipe.move();
                if (pipe.isOffScreen()) {
                    pipe.reset(600);
                    score++;
                }
            }
            checkCollision();
            repaint();
        }
    }

    private void checkCollision() {
        Rectangle birdRect = new Rectangle(bird.getX(), bird.getY(), bird.getWidth(), bird.getHeight());
        for (Pipe pipe : pipes) {
            Rectangle topPipe = new Rectangle(pipe.getX(), pipe.getTopY(), 60, 400);
            Rectangle bottomPipe = new Rectangle(pipe.getX(), pipe.getBottomY(), 60, 400);
            if (birdRect.intersects(topPipe) || birdRect.intersects(bottomPipe) || bird.getY() >= 520) {
                gameOver = true;
                timer.stop();
            }
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        g.drawImage(backgroundImage, 0, 0, getWidth(), getHeight(), null);
        bird.draw(g);
        for (Pipe pipe : pipes) {
            pipe.draw(g);
        }

        // Draw grass at the bottom
        g.setColor(Color.green);
        g.fillRect(0, 554, 360, 120);

        g.setColor(Color.BLACK);
        g.setFont(new Font("Arial", Font.BOLD, 20));
        g.drawString("Score: " + score, 20, 30);

        if (!gameStarted) {
            g.drawString("Press ENTER to Start", 80, 280);
        } else if (gameOver) {
            g.setColor(Color.RED);
            g.drawString("Game Over!", 115, 300);
            g.setColor(Color.BLACK);
            g.drawString("Press R to Restart", 80, 280);
        }
    }

    @Override
    public void keyPressed(KeyEvent e) {
        if (e.getKeyCode() == KeyEvent.VK_ENTER) {
            if (!gameStarted) {
                gameStarted = true;
                timer.start();
            }
        }
        if (e.getKeyCode() == KeyEvent.VK_SPACE && !gameOver) {
            bird.jump();
        }
        if (e.getKeyCode() == KeyEvent.VK_R && gameOver) {
            resetGame();
        }
    }

    private void resetGame() {
        initGame();
        bird.reset();
        for (int i = 0; i < pipes.size(); i++) {
            pipes.get(i).reset(400 + i * 200);
            pipes.get(i).setSpeed(3);
        }
        repaint();
    }

    @Override
    public void keyReleased(KeyEvent e) {
    }

    @Override
    public void keyTyped(KeyEvent e) {
    }
}

public class App {

    public static void main(String[] args) {
        JFrame frame = new JFrame("Flappy Bird");
        GamePanel gamePanel = new GamePanel();

        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(360, 640);
        frame.setResizable(false);
        frame.add(gamePanel);
        frame.setVisible(true);
    }
}
