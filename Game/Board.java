package Game;

import Neural.Net.NeuralNet;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.List;

public class Board extends JPanel implements ActionListener {

    private final int B_WIDTH = 300;
    private final int B_HEIGHT = 300;
    private final int DOT_SIZE = 10;
    private final int ALL_DOTS = 900;
    private final int RAND_POS = 29;
    private final int DELAY = 140;

    private final int x[] = new int[ALL_DOTS];
    private final int y[] = new int[ALL_DOTS];

    private int dots;
    private int apple_x;
    private int apple_y;

    private boolean leftDirection = false;
    private boolean rightDirection = true;
    private boolean upDirection = false;
    private boolean downDirection = false;
    public boolean inGame = true;
    private boolean isAi = false;

    private String movingDirection = "right";

    private Timer timer;
    private Image ball;
    private Image apple;
    private Image head;

    private NeuralNet brain;
    private int fitness;


    public Board() {
        addKeyListener(new TAdapter());
        setBackground(Color.black);
        setFocusable(true);

        setPreferredSize(new Dimension(B_WIDTH, B_HEIGHT));
        loadImages();
        initGame();
    }

    public Board(NeuralNet brain) {
        this.isAi = true;
        this.brain = brain;
        addKeyListener(new TAdapter());
        setBackground(Color.black);
        setFocusable(true);

        setPreferredSize(new Dimension(B_WIDTH, B_HEIGHT));
        loadImages();
        initGame();
    }

    private void loadImages() {

        ImageIcon iid = new ImageIcon(this.getClass().getResource("/Game/Media/dot.png"));
        ball = iid.getImage();

        ImageIcon iia = new ImageIcon(this.getClass().getResource("/Game/Media/apple.png"));
        apple = iia.getImage();

        ImageIcon iih = new ImageIcon(this.getClass().getResource("/Game/Media/head.png"));
        head = iih.getImage();
    }

    private void initGame() {

        dots = 3;

        for (int z = 0; z < dots; z++) {
            x[z] = 50 - z * 10;
            y[z] = 50;
        }

        locateApple();

        timer = new Timer(DELAY, this);
        timer.start();
    }

    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);

        doDrawing(g);
    }

    private void doDrawing(Graphics g) {
        if (inGame) {

            g.drawImage(apple, apple_x, apple_y, this);

            for (int z = 0; z < dots; z++) {
                if (z == 0) {
                    g.drawImage(head, x[z], y[z], this);
                } else {
                    g.drawImage(ball, x[z], y[z], this);
                }
            }

            Toolkit.getDefaultToolkit().sync();


        } else {

            gameOver(g);
        }
    }

    /**
     * Inputs for Neural Net:
     * 0 - 1 Field in moving direction
     * 1 - 2 Fields in moving direction
     * 2 - apples x coordinate
     * 3 - apples y coordinate
     * 4 - own x coordinate
     * 5 - own y coordinate
     */

    private void makeMove() {
        List<Double> inputs = new ArrayList<Double>();
        List<Double> outputs;
        int chosenOutput = 0;
        double maxOutput = 0;
        int[] nextFieldStatus;

        nextFieldStatus = checkNextFields();

        inputs.add((double) nextFieldStatus[0]);
        inputs.add((double) nextFieldStatus[1]);

        inputs.add(((double) apple_x / (double) B_WIDTH));
        inputs.add(((double) apple_y / (double) B_HEIGHT));

        inputs.add(((double) x[0] / (double) B_WIDTH));
        inputs.add(((double) y[0] / (double) B_HEIGHT));
        outputs = brain.update(inputs);
        for (int i = 0; i < 4; i++) {
            if (outputs.get(i) > maxOutput) {
                maxOutput = outputs.get(i);
                chosenOutput = i;
            }
        }
        switch (chosenOutput) {
            case 0:
                upDirection = true;
                downDirection = false;
                rightDirection = false;
                leftDirection = false;
                movingDirection = "up";
                break;
            case 1:
                upDirection = false;
                downDirection = true;
                rightDirection = false;
                leftDirection = false;
                movingDirection = "down";
                break;
            case 2:
                upDirection = false;
                downDirection = false;
                rightDirection = true;
                leftDirection = false;
                movingDirection = "right";
                break;
            case 3:
                upDirection = false;
                downDirection = false;
                rightDirection = false;
                leftDirection = true;
                movingDirection = "left";
                break;
        }
    }

    private int[] checkNextFields() {
        boolean collision = false;
        int[] result = new int[2];
        List<int[]> nextMove;

        int[] tempX = x.clone();
        int[] tempY = y.clone();

        nextMove = move(tempX, tempY, movingDirection);
        collision = checkCollisionAt(nextMove.get(0), nextMove.get(1));
        result[0] = collision ? 1 : -1;

        nextMove = move(nextMove.get(0), nextMove.get(1), movingDirection);
        collision = checkCollisionAt(nextMove.get(0), nextMove.get(1));
        result[1] = collision ? 1 : -1;

        return result;
    }

    private void gameOver(Graphics g) {

        String msg = "Game Over";
        Font small = new Font("Helvetica", Font.BOLD, 14);
        FontMetrics metr = getFontMetrics(small);

        g.setColor(Color.white);
        g.setFont(small);
        g.drawString(msg, (B_WIDTH - metr.stringWidth(msg)) / 2, B_HEIGHT / 2);
    }

    private void checkApple() {

        if ((x[0] == apple_x) && (y[0] == apple_y)) {

            dots++;
            if (isAi) {
                fitness += 10;
            }
            locateApple();
        }
    }

    private void move() {

        for (int z = dots; z > 0; z--) {
            x[z] = x[(z - 1)];
            y[z] = y[(z - 1)];
        }

        if (leftDirection) {
            x[0] -= DOT_SIZE;
        }

        if (rightDirection) {
            x[0] += DOT_SIZE;
        }

        if (upDirection) {
            y[0] -= DOT_SIZE;
        }

        if (downDirection) {
            y[0] += DOT_SIZE;
        }
    }

    private List<int[]> move(int[] x, int[] y, String direction) {

        for (int z = dots; z > 0; z--) {
            x[z] = x[(z - 1)];
            y[z] = y[(z - 1)];
        }

        if (direction.equals("left")) {
            x[0] -= DOT_SIZE;
        }

        if (direction.equals("right")) {
            x[0] += DOT_SIZE;
        }

        if (direction.equals("up")) {
            y[0] -= DOT_SIZE;
        }

        if (direction.equals("down")) {
            y[0] += DOT_SIZE;
        }
        List<int[]> result = new ArrayList<int[]>();
        result.add(x);
        result.add(y);
        return result;
    }

    private void checkCollision() {

        for (int z = dots; z > 0; z--) {

            if ((x[0] == x[z]) && (y[0] == y[z])) {
                inGame = false;
            }
        }

        if (y[0] >= B_HEIGHT) {
            inGame = false;
        }

        if (y[0] <= 0) {
            inGame = false;
        }

        if (x[0] >= B_WIDTH) {
            inGame = false;
        }

        if (x[0] <= 0) {
            inGame = false;
        }

        if (!inGame) {
            timer.stop();
        } else if (isAi) {
            fitness++;
        }
    }

    private boolean checkCollisionAt(int x[], int y[]) {
        boolean result = false;
        for (int z = dots; z > 0; z--) {

            if ((x[0] == x[z]) && (y[0] == y[z])) {
                result = true;
            }
        }

        if (y[0] >= B_HEIGHT) {
            result = true;
        }

        if (y[0] <= 0) {
            result = true;
        }

        if (x[0] >= B_WIDTH) {
            result = true;
        }

        if (x[0] <= 0) {
            result = true;
        }

        return result;
    }

    private void locateApple() {

        int r = (int) (Math.random() * RAND_POS);
        apple_x = ((r * DOT_SIZE));

        r = (int) (Math.random() * RAND_POS);
        apple_y = ((r * DOT_SIZE));
    }

    @Override
    public void actionPerformed(ActionEvent e) {

        if (inGame) {

            checkApple();
            checkCollision();
            move();
            if (isAi) {
                makeMove();
            }
        }

        repaint();
    }

    private class TAdapter extends KeyAdapter {

        @Override
        public void keyPressed(KeyEvent e) {

            int key = e.getKeyCode();

            if ((key == KeyEvent.VK_LEFT) && (!rightDirection)) {
                leftDirection = true;
                upDirection = false;
                downDirection = false;
            }

            if ((key == KeyEvent.VK_RIGHT) && (!leftDirection)) {
                rightDirection = true;
                upDirection = false;
                downDirection = false;
            }

            if ((key == KeyEvent.VK_UP) && (!downDirection)) {
                upDirection = true;
                rightDirection = false;
                leftDirection = false;
            }

            if ((key == KeyEvent.VK_DOWN) && (!upDirection)) {
                downDirection = true;
                rightDirection = false;
                leftDirection = false;
            }
        }
    }

    public void putWeights(List<Double> weights) {
        if (isAi) {
            brain.putWeights(weights);
        }
    }

    public boolean timerStopped() {
        return !timer.isRunning();
    }


    public int getFitness() {
        return fitness;
    }
}