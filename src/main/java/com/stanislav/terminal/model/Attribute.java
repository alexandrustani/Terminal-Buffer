package com.stanislav.terminal.model;

import com.stanislav.terminal.enums.StyleFlag;
import com.stanislav.terminal.enums.TermColor;

import java.util.EnumSet;
import java.util.Objects;

public final class Attribute {
    private final TermColor foregc;
    private final TermColor backgc;
    private final EnumSet<StyleFlag> styles;

    public Attribute (final TermColor foreground, final TermColor background, final EnumSet<StyleFlag> styles) {
        this.foregc = Objects.requireNonNull(foreground);
        this.backgc = Objects.requireNonNull(background);
        this.styles = styles.isEmpty() ? EnumSet.noneOf(StyleFlag.class) : EnumSet.copyOf(styles);
    }

    /**
     * Creates an default Attribute
     *
     * @return the created default Attribute
     */
    public static Attribute defaults() {
        return new Attribute(TermColor.DEFAULT, TermColor.DEFAULT, EnumSet.noneOf(StyleFlag.class));
    }

    public TermColor foreground() {
        return foregc;
    }

    public TermColor background() {
        return backgc;
    }

    public EnumSet<StyleFlag> styles() {
        return EnumSet.copyOf(styles);
    }

    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Attribute that)) return false;
        return foregc == that.foregc &&
                backgc == that.backgc &&
                styles.equals(that.styles);
    }

    @Override
    public int hashCode() {
        return Objects.hash(foregc, backgc, styles);
    }

    @Override
    public String toString() {
        return "Attributes{fg=" + foregc + ", bg=" + backgc + ", styles=" + styles + "}";
    }
}
