package com.stanislav.terminal.core;

public final class Cursor {
    private int row;
    private int col;
    private int maxHeight;
    private int maxWidth;

    public Cursor(final int row, final int col, final int maxHeight, final int maxWidth) {
        this.row = row;
        this.col = col;
        this.maxHeight = maxHeight;
        this.maxWidth = maxWidth;
    }

    public void setCursor(final int row, final int col) {
        this.col = clamp(col, 0, maxWidth - 1);
        this.row = clamp(row, 0, maxHeight - 1);
    }

    /**
     * Clamp function for the limitations of the screen
     *
     * @param value - given value
     * @param min - left margin
     * @param max - right margin
     * @return the clamped value
     */
    private static int clamp(final int value, final int min, final int max) {
        if (value < min)
            return min;

        if (value > max)
            return max;

        return value;
    }

    public int getCol() {
        return col;
    }

    public int getRow() {
        return row;
    }
}
