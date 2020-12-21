package me.polymarsdev.teamsnake.entity;

import java.io.Serializable;

public class Segment implements Serializable {
    int x;
    int y;

    public Segment(int x, int y) {
        this.x = x;
        this.y = y;

    }

    void setPosition(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    boolean moveUp() { //move functions only used on head
        y -= 1;
        return true;
    }

    boolean moveDown() {
        y += 1;
        return true;
    }

    boolean moveLeft() {
        x -= 1;
        return true;
    }

    boolean moveRight() {
        x += 1;
        return true;
    }
}