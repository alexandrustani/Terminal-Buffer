package com.stanislav.terminal;

import java.util.*;

public final class TerminalBuffer {
    public final int width;
    public final int height;
    public final int maxScrollBack;

    public final List<Line> screen;
    public final Queue<Line> scrollBack;

    public final Attribute currentAttribute;
    private final Cursor cursor;

    public TerminalBuffer(final int width, final int height, final int maxScrollBack) {
        if (height <= 0)
            throw new IllegalArgumentException("height must be greater than 0");

        if (maxScrollBack <= 0)
            throw new IllegalArgumentException("maxScrollBack must be greater than 0");

        this.width = width;
        this.height = height;
        this.maxScrollBack = maxScrollBack;

        screen = new ArrayList<>(height);
        this.currentAttribute = Attribute.defaults();

        for (int i = 0; i < height; i++)
            screen.add(new Line(width, this.currentAttribute));

        this.scrollBack = new ArrayDeque<>(Math.max(1, maxScrollBack));

        cursor = new Cursor(0, 0, height, width);
    }

    public int getWidth() {
        return this.width;
    }

    public int getHeight() {
        return this.height;
    }

    public int getMaxScrollBack() {
        return this.maxScrollBack;
    }

    public void moveCursor(Moves move, int N) {
        if (move == Moves.UP)
            cursor.setCursor(cursor.getRow() + N, cursor.getCol());
        else if (move == Moves.DOWN)
            cursor.setCursor(cursor.getRow() - N, cursor.getCol());
        else if (move == Moves.LEFT)
            cursor.setCursor(cursor.getRow(), cursor.getCol() - N);
        else if (move == Moves.RIGHT)
            cursor.setCursor(cursor.getRow(), cursor.getCol() + N);
    }

    public Cursor getCursor() {
        return cursor;
    }
}
