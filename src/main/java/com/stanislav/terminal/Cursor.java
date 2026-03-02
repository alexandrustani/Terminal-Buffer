package com.stanislav.terminal;

import static java.lang.Math.clamp;

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

    public int getCol() {
        return col;
    }

    public int getRow() {
        return row;
    }
}
