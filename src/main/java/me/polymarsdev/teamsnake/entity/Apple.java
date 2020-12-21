package me.polymarsdev.teamsnake.entity;

import me.polymarsdev.teamsnake.objects.Grid;
import me.polymarsdev.teamsnake.objects.Randomizer;

import java.io.Serializable;

public class Apple implements Serializable {
    int x;
    int y;
    Grid grid;

    public Apple(Grid grid) {
        this.grid = grid;
        x = Randomizer.nextInt(grid.getWidth());
        y = Randomizer.nextInt(grid.getHeight());
    }

    public void setPos(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public void randomize(int size, int excludeX, int excludeY) {
        if (size >= 49) { // prevent getting stuck in the while-loop below when board is won/filled up
            x = -1;
            y = -1;
            return;
        }
        int tempX = Randomizer.nextInt(grid.getWidth());
        int tempY = Randomizer.nextInt(grid.getHeight());
        while (grid.hasSegment(tempX, tempY, true, true) || (tempX == excludeX && tempY == excludeY)) {
            tempX = Randomizer.nextInt(grid.getWidth());
            tempY = Randomizer.nextInt(grid.getHeight());
        }
        x = tempX;
        y = tempY;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }


}