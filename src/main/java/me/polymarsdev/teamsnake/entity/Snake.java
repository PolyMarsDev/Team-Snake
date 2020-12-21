package me.polymarsdev.teamsnake.entity;

import me.polymarsdev.teamsnake.handler.GameHandler;
import me.polymarsdev.teamsnake.objects.Grid;
import me.polymarsdev.teamsnake.objects.Randomizer;
import me.polymarsdev.teamsnake.util.StatsUtil;

import java.io.Serializable;
import java.util.ArrayList;

public class Snake implements Serializable {
    //snake modes
    final int CLASSIC = 0;
    final int GLUTTED = 1;
    final int CHARGED = 2;
    final int DEVIL = 3;
    //apple modes
    final int SINGLE = 0;
    final int PORTAL = 1;
    //directions
    final int N = 0;
    final int E = 1;
    final int S = 2;
    final int W = 3;
    int direction = E;
    int size = 0;
    int snakeMode = CLASSIC;
    int appleMode = SINGLE;
    boolean yummed = false;
    int bestScore = 0;
    int startingSize;
    boolean dead = false;
    Grid grid;
    ArrayList<Segment> segments;
    Segment head;
    Randomizer rand;

    public Snake(int size, Grid grid) {
        rand = new Randomizer();
        startingSize = size;
        this.size = startingSize;
        this.grid = grid;
        head = new Segment(0, 1);
        segments = new ArrayList<>();
        segments.add(head);
    }

    public int getSnakeMode() {
        return snakeMode;
    }

    public int getAppleMode() {
        return appleMode;
    }

    public int getSize() {
        return size;
    }

    public int getLength() {
        return segments.size();
    }

    public boolean isYummed() {
        return yummed;
    }

    public int getScore() {
        return size - 1;
    }

    public boolean isDead() {
        return dead;
    }

    public int getBestScore(GameHandler gameHandler) {
        if (getScore() > bestScore) {
            bestScore = getScore();
            StatsUtil.setStats(gameHandler.message.getGuild().getId(), "bestscore", String.valueOf(bestScore));
        }
        return bestScore;
    }

    public void setBestScore(int bestScore) {
        this.bestScore = bestScore;
    }

    public void grow() {
        size += 1;
        segments.add(new Segment(head.getX(), head.getY()));
    }

    public void setMode(int snakeMode, int appleMode) {
        this.snakeMode = snakeMode;
        this.appleMode = appleMode;
    }

    public void setYummed(boolean yummed) {
        this.yummed = yummed;
    }

    public void setDirection(int direction) {
        if (snakeMode == DEVIL) {
            for (int i = 0; i < 2; i++) {
                direction++;
                if (direction < 0) {
                    direction = 3;
                } else if (direction > 3) {
                    direction = 0;
                }
            }
        }
        this.direction = direction;
    }

    public int getDirection() {
        return direction;
    }

    void reset(GameHandler gameHandler) {
        snakeMode = CLASSIC;
        getBestScore(gameHandler); //update best
        size = startingSize;
        head.setPosition(1, 1);
        direction = E;
        dead = false;
    }

    int getX() {
        return head.getX();
    }

    int getY() {
        return head.getY();
    }

    public boolean wallCollide() {
        return (head.getX() > grid.getWidth() - 1 || head.getX() < 0 || head.getY() > grid.getHeight() - 1
                || head.getY() < 0);
    }

    public boolean wallCollide(int x, int y) {
        return (x > grid.getWidth() - 1 || x < 0 || y > grid.getHeight() - 1 || y < 0);
    }

    public boolean hasApple() {
        return head.getX() == grid.getApple().getX() && head.getY() == grid.getApple().getY();
    }

    public boolean hasSegment(int x, int y, boolean tail) {
        return grid.hasSegment(x, y, false, tail);
    }

    public Segment getSegment(int index) {
        return segments.get(index);
    }

    public void changeModes() {
        if (yummed || dead || getLength() == (grid.getHeight() * grid.getHeight()) - 1) {
            snakeMode = CLASSIC;
        } else {
            int random = Randomizer.nextInt(4);
            if (random != 0) {
                snakeMode = CLASSIC;
            } else {
                int random2 = Randomizer.nextInt(3);
                if (random2 == 0) {
                    snakeMode = DEVIL;
                } else if (random2 == 1) {
                    snakeMode = GLUTTED;
                } else if (random2 == 2) {
                    snakeMode = CHARGED;
                }
            }
        }
    }

    public void move() {
        if (direction == N) {
            if (snakeMode == CHARGED) {
                while (!hasSegment(head.getX(), head.getY() - 1, false) && !wallCollide(head.getX(), head.getY() - 1)) {
                    if (grid.hasApple(head.getX(), head.getY() - 1)) {
                        grow();
                        grid.getApple().randomize(size, head.getX(), head.getY() - 1);
                    }
                    moveSegments();
                    head.moveUp();
                }
            } else if (updateLogic(head.getX(), head.getY() - 1)) {
                moveSegments();
                head.moveUp();
            }
        } else if (direction == E) {
            if (snakeMode == CHARGED) {
                while (!hasSegment(head.getX() + 1, head.getY(), false) && !wallCollide(head.getX() + 1, head.getY())) {
                    if (grid.hasApple(head.getX() + 1, head.getY())) {
                        grow();
                        grid.getApple().randomize(size, head.getX() + 1, head.getY());
                    }
                    moveSegments();
                    head.moveRight();
                }
            } else if (updateLogic(head.getX() + 1, head.getY())) {
                moveSegments();
                head.moveRight();
            }
        } else if (direction == S) {
            if (snakeMode == CHARGED) {
                while (!hasSegment(head.getX(), head.getY() + 1, false) && !wallCollide(head.getX(), head.getY() + 1)) {
                    if (grid.hasApple(head.getX(), head.getY() + 1)) {
                        grow();
                        grid.getApple().randomize(size, head.getX(), head.getY() + 1);
                    }
                    moveSegments();
                    head.moveDown();
                }
            } else if (updateLogic(head.getX(), head.getY() + 1)) {
                moveSegments();
                head.moveDown();
            }
        } else if (direction == W) {
            if (snakeMode == CHARGED) {
                while (!hasSegment(head.getX() - 1, head.getY(), false) && !wallCollide(head.getX() - 1, head.getY())) {
                    if (grid.hasApple(head.getX() - 1, head.getY())) {
                        grow();
                        grid.getApple().randomize(size, head.getX() - 1, head.getY());
                    }
                    moveSegments();
                    head.moveLeft();
                }
            } else if (updateLogic(head.getX() - 1, head.getY())) {
                moveSegments();
                head.moveLeft();
            }
        }
    }

    boolean updateLogic(int x, int y) //returns false if dead
    {
        if (grid.hasApple(x, y)) {
            if (snakeMode == GLUTTED) {
                dead = true;
                return true; //even though it's dead i want it to move into the apple
            } else {
                grow();
                setYummed(true);
                grid.getApple().randomize(size, x, y);
            }
        }
        if (hasSegment(x, y, false) || wallCollide(x, y)) {
            dead = true;
        }
        return !dead;
    }

    void moveSegments() {
        for (int i = size - 1; i >= 1; i--) {
            segments.get(i).setPosition(segments.get(i - 1).getX(), segments.get(i - 1).getY());
        }
    }
}