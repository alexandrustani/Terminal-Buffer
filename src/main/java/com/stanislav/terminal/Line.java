package com.stanislav.terminal;

import java.util.Objects;

public class Line {
    private final Cell[] cells;

    public Line(final int width, final Attribute defaultAttr) {
        if (width <= 0)
            throw new IllegalArgumentException("width must be greater than 0");

        Objects.requireNonNull(defaultAttr);

        this.cells = new Cell[width];
        for (int i = 0; i < width; i++)
            cells[i] = Cell.empty(defaultAttr);
    }

    public int width() {
        return cells.length;
    }

    public Cell cell(int col) {
        return cells[col];
    }

    public Line copy() {
        Line copy = new Line(cells.length, Attribute.defaults());

        for (int i = 0; i < cells.length; i++)
            copy.cells[i] = this.cells[i].copy();

        return copy;
    }

    public String toPlainString() {
        StringBuilder sb = new StringBuilder(cells.length);

        for (Cell c : cells)
            sb.append(c.isEmpty() ? ' ' : c.ch());

        return sb.toString();
    }
}
