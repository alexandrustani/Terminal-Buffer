package com.stanislav.terminal;

import com.stanislav.terminal.core.TerminalBuffer;
import com.stanislav.terminal.enums.Moves;
import com.stanislav.terminal.model.Attribute;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class TerminalBufferTest {

    private static int scrollbackSize(final TerminalBuffer tb) {
        return tb.getTotalLines() - tb.getHeight();
    }

    // -----------------------
    // Constructor + init
    // -----------------------

    @Test
    void constructor_rejectsNonPositiveWidth() {
        assertThrows(IllegalArgumentException.class, () -> new TerminalBuffer(0, 2, 0));
        assertThrows(IllegalArgumentException.class, () -> new TerminalBuffer(-1, 2, 0));
    }

    @Test
    void constructor_rejectsNonPositiveHeight() {
        assertThrows(IllegalArgumentException.class, () -> new TerminalBuffer(2, 0, 0));
        assertThrows(IllegalArgumentException.class, () -> new TerminalBuffer(2, -1, 0));
    }

    @Test
    void constructor_rejectsNegativeScrollback() {
        assertThrows(IllegalArgumentException.class, () -> new TerminalBuffer(2, 2, -1));
    }

    @Test
    void constructor_initializesDimensionsCursorAndDefaults() {
        TerminalBuffer tb = new TerminalBuffer(80, 24, 100);

        assertEquals(80, tb.getWidth());
        assertEquals(24, tb.getHeight());
        assertEquals(100, tb.getMaxScrollBack());

        TerminalBuffer.CursorPosition pos = tb.getCursorPosition();
        assertEquals(0, pos.row());
        assertEquals(0, pos.col());

        assertNotNull(tb.getCurrentAttribute());
        assertEquals(24, tb.getTotalLines());
        assertEquals(0, scrollbackSize(tb));
    }

    @Test
    void constructor_initializesBlankScreenLines() {
        TerminalBuffer tb = new TerminalBuffer(5, 2, 10);

        assertEquals("     ", tb.getLineAsString(0));
        assertEquals("     ", tb.getLineAsString(1));
        assertEquals("     \n     ", tb.getScreenAsString());
    }

    // -----------------------
    // Current attributes
    // -----------------------

    @Test
    void setCurrentAttribute_changesCurrentAttribute() {
        TerminalBuffer tb = new TerminalBuffer(5, 2, 0);
        Attribute a = Attribute.defaults();
        tb.setCurrentAttribute(a);
        assertEquals(a, tb.getCurrentAttribute());
    }

    @Test
    void setCurrentAttribute_rejectsNull() {
        TerminalBuffer tb = new TerminalBuffer(5, 2, 0);
        assertThrows(NullPointerException.class, () -> tb.setCurrentAttribute(null));
    }

    // -----------------------
    // Cursor movement
    // -----------------------

    @Test
    void moveCursor_rejectsNullMove() {
        TerminalBuffer tb = new TerminalBuffer(5, 2, 0);
        assertThrows(NullPointerException.class, () -> tb.moveCursor(null, 1));
    }

    @Test
    void moveCursor_rejectsNegativeN() {
        TerminalBuffer tb = new TerminalBuffer(5, 2, 0);
        assertThrows(IllegalArgumentException.class, () -> tb.moveCursor(Moves.RIGHT, -1));
    }

    @Test
    void moveCursor_clampsAtTopLeft() {
        TerminalBuffer tb = new TerminalBuffer(5, 3, 0);

        tb.moveCursor(Moves.UP, 999);
        tb.moveCursor(Moves.LEFT, 999);

        TerminalBuffer.CursorPosition pos = tb.getCursorPosition();
        assertEquals(0, pos.row());
        assertEquals(0, pos.col());
    }

    @Test
    void moveCursor_clampsAtBottomRight() {
        TerminalBuffer tb = new TerminalBuffer(5, 3, 0);

        tb.moveCursor(Moves.DOWN, 999);
        tb.moveCursor(Moves.RIGHT, 999);

        TerminalBuffer.CursorPosition pos = tb.getCursorPosition();
        assertEquals(2, pos.row());
        assertEquals(4, pos.col());
    }

    // -----------------------
    // Write
    // -----------------------

    @Test
    void write_rejectsNull() {
        TerminalBuffer tb = new TerminalBuffer(5, 2, 0);
        assertThrows(NullPointerException.class, () -> tb.write(null));
    }

    @Test
    void write_overwritesExistingContentAndMovesCursor() {
        TerminalBuffer tb = new TerminalBuffer(5, 2, 0);

        tb.write("abc");
        tb.moveCursor(Moves.LEFT, 2);
        tb.write("Z");

        assertEquals("aZc  ", tb.getLineAsString(0));

        TerminalBuffer.CursorPosition pos = tb.getCursorPosition();
        assertEquals(0, pos.row());
        assertEquals(2, pos.col());
    }

    @Test
    void write_wrapsToNextLineWhenReachingWidth() {
        TerminalBuffer tb = new TerminalBuffer(5, 2, 0);

        tb.write("ABCDE");
        tb.write("X");

        assertEquals("ABCDE", tb.getLineAsString(0));
        assertEquals("X    ", tb.getLineAsString(1));
    }

    @Test
    void write_newlineMovesToNextLine() {
        TerminalBuffer tb = new TerminalBuffer(5, 2, 0);

        tb.write("HI\nX");

        assertEquals("HI   ", tb.getLineAsString(0));
        assertEquals("X    ", tb.getLineAsString(1));

        TerminalBuffer.CursorPosition pos = tb.getCursorPosition();
        assertEquals(1, pos.row());
        assertEquals(1, pos.col());
    }

    @Test
    void write_scrollsWhenWritingPastBottom() {
        TerminalBuffer tb = new TerminalBuffer(3, 2, 10);

        tb.write("111\n222\n333");

        assertTrue(scrollbackSize(tb) >= 1);
        assertTrue(tb.getAllAsString().contains("333"));
        assertEquals(2, tb.getHeight()); // screen height constant
    }

    @Test
    void scrollback_respectsMaxSize() {
        TerminalBuffer tb = new TerminalBuffer(3, 2, 2);

        tb.write("111\n222\n333\n444\n555");

        assertEquals(2, scrollbackSize(tb));
        assertEquals(4, tb.getTotalLines());
        assertTrue(tb.getAllAsString().contains("555"));
    }

    // -----------------------
    // Fill line
    // -----------------------

    @Test
    void fillLine_rejectsOutOfBoundsRow() {
        TerminalBuffer tb = new TerminalBuffer(4, 2, 0);
        assertThrows(IndexOutOfBoundsException.class, () -> tb.fillLine(-1, 'X'));
        assertThrows(IndexOutOfBoundsException.class, () -> tb.fillLine(2, 'X'));
    }

    @Test
    void fillLine_fillsWithCharacter() {
        TerminalBuffer tb = new TerminalBuffer(4, 2, 0);

        tb.fillLine(0, 'X');
        assertEquals("XXXX", tb.getLineAsString(0));
        assertEquals("    ", tb.getLineAsString(1));
    }

    @Test
    void fillLine_nullClearsLineToSpaces() {
        TerminalBuffer tb = new TerminalBuffer(4, 2, 0);

        tb.write("ABCD");
        assertEquals("ABCD", tb.getLineAsString(0));

        tb.fillLine(0, null);
        assertEquals("    ", tb.getLineAsString(0));
    }

    // -----------------------
    // Insert empty line at bottom
    // -----------------------

    @Test
    void insertEmptyLineAtBottom_scrollsScreenUp() {
        TerminalBuffer tb = new TerminalBuffer(3, 2, 10);

        tb.write("AAA\nBBB");
        assertEquals("AAA", tb.getLineAsString(0));
        assertEquals("BBB", tb.getLineAsString(1));

        int before = scrollbackSize(tb);
        tb.insertEmptyLineAtBottom();

        assertEquals(before + 1, scrollbackSize(tb));
        assertEquals("BBB", tb.getLineAsString(tb.getTotalLines() - 2));
        assertEquals("   ", tb.getLineAsString(tb.getTotalLines() - 1));
    }

    @Test
    void insertEmptyLineAtBottom_withZeroScrollback_keepsScrollbackEmpty() {
        TerminalBuffer tb = new TerminalBuffer(3, 2, 0);

        tb.write("AAA\nBBB");
        tb.insertEmptyLineAtBottom();

        assertEquals(0, scrollbackSize(tb));
        assertEquals(tb.getHeight(), tb.getTotalLines());
    }

    // -----------------------
    // Clear operations
    // -----------------------

    @Test
    void clearScreen_clearsVisibleAreaAndResetsCursorButKeepsScrollback() {
        TerminalBuffer tb = new TerminalBuffer(3, 2, 5);

        tb.write("111\n222\n333");
        int before = scrollbackSize(tb);
        assertTrue(before >= 1);

        tb.clearScreen();

        assertEquals("   \n   ", tb.getScreenAsString());
        TerminalBuffer.CursorPosition pos = tb.getCursorPosition();
        assertEquals(0, pos.row());
        assertEquals(0, pos.col());

        assertEquals(before, scrollbackSize(tb));
    }

    @Test
    void clearScreenAndScrollback_clearsEverything() {
        TerminalBuffer tb = new TerminalBuffer(3, 2, 5);

        tb.write("111\n222\n333");
        assertTrue(scrollbackSize(tb) >= 1);

        tb.clearScreenAndScrollback();

        assertEquals(0, scrollbackSize(tb));
        assertEquals(tb.getHeight(), tb.getTotalLines());
        assertEquals("   \n   ", tb.getScreenAsString());
    }

    // -----------------------
    // Content access (bounds + attribute)
    // -----------------------

    @Test
    void getCharAt_readsFromScreenAndReturnsSpaceForEmpty() {
        TerminalBuffer tb = new TerminalBuffer(5, 2, 0);

        tb.write("HI");
        assertEquals('H', tb.getCharAt(0, 0));
        assertEquals('I', tb.getCharAt(0, 1));
        assertEquals(' ', tb.getCharAt(0, 4));
    }

    @Test
    void getCharAt_rejectsOutOfBounds() {
        TerminalBuffer tb = new TerminalBuffer(5, 2, 0);

        assertThrows(IndexOutOfBoundsException.class, () -> tb.getCharAt(0, -1));
        assertThrows(IndexOutOfBoundsException.class, () -> tb.getCharAt(0, 5));
        assertThrows(IndexOutOfBoundsException.class, () -> tb.getCharAt(-1, 0));
        assertThrows(IndexOutOfBoundsException.class, () -> tb.getCharAt(2, 0));
    }

    @Test
    void getAttributeAt_returnsNonNullAndRejectsOutOfBounds() {
        TerminalBuffer tb = new TerminalBuffer(5, 2, 0);

        tb.write("A");
        assertNotNull(tb.getAttributeAt(0, 0));

        assertThrows(IndexOutOfBoundsException.class, () -> tb.getAttributeAt(0, -1));
        assertThrows(IndexOutOfBoundsException.class, () -> tb.getAttributeAt(0, 5));
        assertThrows(IndexOutOfBoundsException.class, () -> tb.getAttributeAt(-1, 0));
        assertThrows(IndexOutOfBoundsException.class, () -> tb.getAttributeAt(2, 0));
    }

    @Test
    void getLineAsString_rejectsOutOfBoundsGlobalRow() {
        TerminalBuffer tb = new TerminalBuffer(3, 2, 0);
        assertThrows(IndexOutOfBoundsException.class, () -> tb.getLineAsString(-1));
        assertThrows(IndexOutOfBoundsException.class, () -> tb.getLineAsString(2));
    }

    @Test
    void getAllAsString_containsScrollbackAndScreenInOrder() {
        TerminalBuffer tb = new TerminalBuffer(3, 2, 10);

        tb.write("111\n222\n333");
        String all = tb.getAllAsString();

        assertTrue(all.contains("111"));
        assertTrue(all.contains("222"));
        assertTrue(all.contains("333"));

        assertTrue(all.indexOf("111") < all.indexOf("222"));
        assertTrue(all.indexOf("222") < all.indexOf("333"));
    }
}