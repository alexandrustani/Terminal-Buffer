package com.stanislav.terminal.core;

import com.stanislav.terminal.enums.Moves;
import com.stanislav.terminal.model.Attribute;
import com.stanislav.terminal.model.Cell;
import com.stanislav.terminal.model.Line;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Objects;

public final class TerminalBuffer {
    private final int width;
    private final int height;
    private final int maxScrollBack;

    private final Deque<Line> screen;
    private final Deque<Line> scrollBack;

    private Attribute currentAttribute;
    private final Cursor cursor;

    private boolean pendingWrap;

    public record CursorPosition(int row, int col) {}

    /**
     * Initializes a terminal buffer with screen size and scrollback.
     *
     * @param width buffer width in columns;
     * @param height buffer height in rows;
     * @param maxScrollBack maximum stored scrollback lines;
     */
    public TerminalBuffer(final int width, final int height, final int maxScrollBack) {
        if (width <= 0)
            throw new IllegalArgumentException("width must be greater than 0");

        if (height <= 0)
            throw new IllegalArgumentException("height must be greater than 0");

        if (maxScrollBack < 0)
            throw new IllegalArgumentException("maxScrollBack must be >= 0");

        this.width = width;
        this.height = height;
        this.maxScrollBack = maxScrollBack;

        this.currentAttribute = Attribute.defaults();

        this.screen = new ArrayDeque<>(height);
        for (int i = 0; i < height; i++)
            screen.addLast(new Line(width, currentAttribute));

        this.scrollBack = new ArrayDeque<>(Math.max(1, maxScrollBack));
        this.cursor = new Cursor(0, 0, height, width);

        this.pendingWrap = false;
    }

    /**
     * Returns the buffer width in columns.
     *
     * @return buffer width (columns)
     */
    public int getWidth() {
        return width;
    }

    /**
     * Returns the buffer height in rows.
     *
     * @return buffer height (rows)
     */
    public int getHeight() {
        return height;
    }

    /**
     * Returns maximum number of scrollback lines stored.
     *
     * @return max scrollback lines
     */
    public int getMaxScrollBack() {
        return maxScrollBack;
    }

    /**
     * Returns current attribute used for writing cells.
     *
     * @return current attribute
     */
    public Attribute getCurrentAttribute() {
        return currentAttribute;
    }

    /**
     * Sets the current attribute used for subsequent writes.
     *
     * @param newAttribute new attribute to apply; must be non-null
     */
    public void setCurrentAttribute(final Attribute newAttribute) {
        this.currentAttribute = Objects.requireNonNull(newAttribute);
    }

    /**
     * Returns current cursor position within the visible screen.
     *
     * @return cursor position (row, col)
     */
    public CursorPosition getCursorPosition() {
        return new CursorPosition(cursor.getRow(), cursor.getCol());
    }

    /**
     * Moves cursor by direction and distance, clamped by cursor.
     *
     * @param move direction to move (UP/DOWN/LEFT/RIGHT); non-null
     * @param n number of cells to move; must be >= 0
     */
    public void moveCursor(final Moves move, final int n) {
        Objects.requireNonNull(move);
        if (n < 0)
            throw new IllegalArgumentException("n must be >= 0");

        pendingWrap = false;

        switch (move) {
            case UP -> cursor.setCursor(cursor.getRow() - n, cursor.getCol());
            case DOWN -> cursor.setCursor(cursor.getRow() + n, cursor.getCol());
            case LEFT -> cursor.setCursor(cursor.getRow(), cursor.getCol() - n);
            case RIGHT -> cursor.setCursor(cursor.getRow(), cursor.getCol() + n);
        }
    }

    /**
     * Writes text at cursor, handling newlines and wrapping.
     *
     * @param text text to write; must be non-null
     */
    public void write(final String text) {
        Objects.requireNonNull(text);

        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);

            if (c == '\n') {
                pendingWrap = false;
                moveToNextLineWithScrollIfNeeded();
                continue;
            }

            if (pendingWrap) {
                moveToNextLineWithScrollIfNeeded();
                pendingWrap = false;
            }

            int row = cursor.getRow();
            int col = cursor.getCol();

            Line line = getScreenLine(row);
            line.cell(col).set(c, currentAttribute);

            if (col == width - 1) {
                pendingWrap = true;
            } else {
                cursor.setCursor(row, col + 1);
            }
        }
    }

    /**
     * Fills a screen row with given character or empties it.
     *
     * @param row target row index on screen; 0..height-1
     * @param chOrNull character to fill, or null to clear cells
     */
    public void fillLine(final int row, final Character chOrNull) {
        if (row < 0 || row >= height)
            throw new IndexOutOfBoundsException();

        char ch = (chOrNull == null) ? '\0' : chOrNull;
        Line line = getScreenLine(row);

        for (int col = 0; col < width; col++)
            line.cell(col).set(ch, currentAttribute);
    }

    /**
     * Inserts new empty bottom line, scrolling screen up.
     */
    public void insertEmptyLineAtBottom() {
        scrollUpOneLine();
        pendingWrap = false;
    }

    /**
     * Clears screen lines and resets cursor to top-left.
     */
    public void clearScreen() {
        screen.clear();

        for (int i = 0; i < height; i++)
            screen.addLast(new Line(width, currentAttribute));

        cursor.setCursor(0, 0);
        pendingWrap = false;
    }

    /**
     * Clears both screen and scrollback history.
     */
    public void clearScreenAndScrollback() {
        clearScreen();
        scrollBack.clear();
    }

    /**
     * Returns total lines including scrollback and visible screen.
     *
     * @return total line count
     */
    public int getTotalLines() {
        return scrollBack.size() + height;
    }

    /**
     * Returns character at global row and column position.
     *
     * @param globalRow row across scrollback+screen; 0..total-1
     * @param col column index; 0..width-1
     * @return character at position, or space if empty
     */
    public char getCharAt(final int globalRow, final int col) {
        if (col < 0 || col >= width)
            throw new IndexOutOfBoundsException();

        Cell cell = getLineGlobal(globalRow).cell(col);
        return cell.isEmpty() ? ' ' : cell.ch();
    }

    /**
     * Returns attribute at global row and column position.
     *
     * @param globalRow row across scrollback+screen; 0..total-1
     * @param col column index; 0..width-1
     * @return attribute at position
     */
    public Attribute getAttributeAt(final int globalRow, final int col) {
        if (col < 0 || col >= width)
            throw new IndexOutOfBoundsException();

        return getLineGlobal(globalRow).cell(col).attr();
    }

    /**
     * Returns plain string of a global row line.
     *
     * @param globalRow row across scrollback+screen; 0..total-1
     * @return line as plain string
     */
    public String getLineAsString(final int globalRow) {
        return getLineGlobal(globalRow).toPlainString();
    }

    /**
     * Returns plain string for visible screen only.
     *
     * @return screen rendered as lines separated by newline
     */
    public String getScreenAsString() {
        StringBuilder sb = new StringBuilder();
        int r = 0;

        for (Line line : screen) {
            sb.append(line.toPlainString());

            if (r != height - 1)
                sb.append('\n');

            r++;
        }

        return sb.toString();
    }

    /**
     * Returns plain string for scrollback plus visible screen.
     *
     * @return all content rendered as newline-separated lines
     */
    public String getAllAsString() {
        StringBuilder sb = new StringBuilder();
        int total = getTotalLines();

        for (int r = 0; r < total; r++) {
            sb.append(getLineAsString(r));

            if (r != total - 1)
                sb.append('\n');
        }

        return sb.toString();
    }

    /**
     * Scrolls screen up and records removed line into scrollback.
     */
    private void scrollUpOneLine() {
        Line top = screen.removeFirst();

        if (maxScrollBack > 0) {
            scrollBack.addLast(top.copy());

            while (scrollBack.size() > maxScrollBack)
                scrollBack.removeFirst();
        }

        screen.addLast(new Line(width, currentAttribute));
    }

    /**
     * Advances cursor to next line, scrolling if at bottom.
     */
    private void moveToNextLineWithScrollIfNeeded() {
        int nextRow = cursor.getRow() + 1;

        if (nextRow >= height) {
            scrollUpOneLine();
            cursor.setCursor(height - 1, 0);
        } else {
            cursor.setCursor(nextRow, 0);
        }
    }

    /**
     * Returns screen line by row index.
     *
     * @param row row index within visible screen; 0..height-1
     * @return line at that row
     */
    private Line getScreenLine(final int row) {
        if (row < 0 || row >= height)
            throw new IndexOutOfBoundsException();

        int idx = 0;

        for (Line l : screen) {
            if (idx == row)
                return l;
            idx++;
        }

        throw new IllegalStateException();
    }

    /**
     * Returns line by global row across scrollback and screen.
     *
     * @param globalRow row index across all lines; 0..total-1
     * @return line at that global row
     */
    private Line getLineGlobal(final int globalRow) {
        int sbSize = scrollBack.size();

        if (globalRow < 0 || globalRow >= sbSize + height)
            throw new IndexOutOfBoundsException();

        if (globalRow < sbSize) {
            int idx = 0;

            for (Line l : scrollBack) {
                if (idx == globalRow)
                    return l;
                idx++;
            }

            throw new IllegalStateException();
        }

        return getScreenLine(globalRow - sbSize);
    }
}