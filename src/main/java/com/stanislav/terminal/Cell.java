package com.stanislav.terminal;

import java.util.Objects;

public final class Cell {
    private char ch;
    private Attribute attr;

    public Cell(final char newCh, final Attribute newAttr) {
        this.ch = newCh;
        this.attr = Objects.requireNonNull(newAttr);
    }

    public static Cell empty(Attribute attr) {
        return new Cell('\0', attr);
    }

    public char ch() {
        return ch;
    }

    public Attribute attr() {
        return attr;
    }

    public void set(char ch, Attribute attr) {
        this.ch = ch;
        this.attr = Objects.requireNonNull(attr);
    }

    public Cell copy() {
        return new Cell(this.ch, this.attr);
    }

    public boolean isEmpty() {
        return ch == '\0';
    }
}
