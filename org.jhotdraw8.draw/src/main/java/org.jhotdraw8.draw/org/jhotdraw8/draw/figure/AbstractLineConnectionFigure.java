/*
 * @(#)AbstractLineConnectionFigure.java
 * Copyright © 2022 The authors and contributors of JHotDraw. MIT License.
 */
package org.jhotdraw8.draw.figure;

import javafx.beans.property.ReadOnlyBooleanWrapper;
import javafx.geometry.Point2D;
import javafx.scene.transform.Transform;
import org.jhotdraw8.annotation.NonNull;
import org.jhotdraw8.annotation.Nullable;
import org.jhotdraw8.collection.ImmutableSets;
import org.jhotdraw8.collection.Key;
import org.jhotdraw8.collection.ReadOnlySet;
import org.jhotdraw8.collection.ReadOnlySetWrapper;
import org.jhotdraw8.css.CssPoint2D;
import org.jhotdraw8.css.CssRectangle2D;
import org.jhotdraw8.css.CssSize;
import org.jhotdraw8.draw.connector.Connector;
import org.jhotdraw8.geom.FXTransforms;

import java.util.LinkedHashSet;
import java.util.Set;

/**
 * Base class for line connection figure.
 *
 * @author Werner Randelshofer
 */
public abstract class AbstractLineConnectionFigure extends AbstractLeafFigure
        implements NonTransformableFigure, LineConnectingFigure {

    private final ReadOnlyBooleanWrapper connected = new ReadOnlyBooleanWrapper();

    public AbstractLineConnectionFigure() {
        this(0, 0, 0, 0);
    }

    public AbstractLineConnectionFigure(@NonNull Point2D start, @NonNull Point2D end) {
        this(start.getX(), start.getY(), end.getX(), end.getY());
    }

    public AbstractLineConnectionFigure(double startX, double startY, double endX, double endY) {
        // Performance: Only set properties if they differ from the default value.
        if (startX != 0) {
            set(START_X, CssSize.from(startX));
        }
        if (startY != 0) {
            set(START_Y, CssSize.from(startY));
        }
        if (endX != 0) {
            set(END_X, CssSize.from(endX));
        }
        if (endY != 0) {
            set(END_Y, CssSize.from(endY));
        }
    }

    @Override
    protected <T> void onPropertyChanged(Key<T> key, @Nullable T oldValue, @Nullable T newValue) {
        // When properties of this figure change, we access the layout observer lists
        // of other figures - therefore these must be synchronized lists!

        if (key == START_TARGET) {
            if (getDrawing() != null) {
                if (oldValue != null && get(END_TARGET) != oldValue) {
                    ((Figure) oldValue).getLayoutObservers().remove(AbstractLineConnectionFigure.this);
                }
                if (newValue != null) {
                    ((Figure) newValue).getLayoutObservers().add(AbstractLineConnectionFigure.this);
                }
            }
            updateConnectedProperty();
        } else if (key == END_TARGET) {
            if (getDrawing() != null) {
                if (oldValue != null && get(START_TARGET) != oldValue) {
                    ((Figure) oldValue).getLayoutObservers().remove(AbstractLineConnectionFigure.this);
                }
                if (newValue != null) {
                    ((Figure) newValue).getLayoutObservers().add(AbstractLineConnectionFigure.this);
                }
            }
            updateConnectedProperty();
        } else if (key == START_CONNECTOR) {
            updateConnectedProperty();
        } else if (key == END_CONNECTOR) {
            updateConnectedProperty();
        }
    }

    @Override
    public void doAddedToDrawing(final @NonNull Drawing drawing) {
        final Figure startTarget = get(START_TARGET);
        if (startTarget != null) {
            startTarget.getLayoutObservers().add(this);
        }
        final Figure endTarget = get(END_TARGET);
        if (endTarget != null && endTarget != startTarget) {
            endTarget.getLayoutObservers().add(this);
        }
    }

    @Override
    protected void doRemovedFromDrawing(final @NonNull Drawing drawing) {
        final Figure startTarget = get(START_TARGET);
        if (startTarget != null) {
            startTarget.getLayoutObservers().remove(this);
        }
        final Figure endTarget = get(END_TARGET);
        if (endTarget != null && endTarget != startTarget) {
            endTarget.getLayoutObservers().remove(this);
        }
    }

    @Override
    public @NonNull CssRectangle2D getCssLayoutBounds() {
        CssPoint2D start = getNonNull(START);
        CssPoint2D end = getNonNull(END);
        return new CssRectangle2D(//
                CssSize.min(start.getX(), end.getX()),//
                CssSize.min(start.getY(), end.getY()),//
                start.getX().subtract(end.getX()).abs(), //
                start.getY().subtract(end.getY()).abs()
        );
    }

    /**
     * Returns all figures which are connected by this figure - they provide to
     * the layout of this figure.
     *
     * @return an unmodifiable set of connected figures
     */
    @Override
    public @NonNull ReadOnlySet<Figure> getLayoutSubjects() {
        final Figure startTarget = get(START_TARGET);
        final Figure endTarget = get(END_TARGET);
        if (startTarget == null && endTarget == null) {
            return ImmutableSets.of();
        }
        Set<Figure> ctf = new LinkedHashSet<>();
        if (startTarget != null) {
            ctf.add(startTarget);
        }
        if (endTarget != null) {
            ctf.add(endTarget);
        }
        return new ReadOnlySetWrapper<>(ctf);
    }

    public boolean isConnected() {
        return connected.get();
    }

    @Override
    public boolean isGroupReshapeableWith(@NonNull Set<Figure> others) {
        for (Figure f : getLayoutSubjects()) {
            if (others.contains(f)) {
                return false;
            }
        }
        return true;
    }

    @Override
    public boolean isLayoutable() {
        return true;
    }


    @Override
    public void removeAllLayoutSubjects() {
        set(START_TARGET, null);
        set(END_TARGET, null);
    }

    @Override
    public void removeLayoutSubject(Figure subject) {
        if (subject == get(START_TARGET)) {
            set(START_TARGET, null);
        }
        if (subject == get(END_TARGET)) {
            set(END_TARGET, null);
        }
    }

    @Override
    public void reshapeInLocal(@NonNull Transform transform) {
        if (get(START_TARGET) == null) {
            set(START, new CssPoint2D(
                    FXTransforms.transform(transform, getNonNull(START).getConvertedValue())));
        }
        if (get(END_TARGET) == null) {
            set(END, new CssPoint2D(
                    FXTransforms.transform(transform, getNonNull(END).getConvertedValue())));
        }
    }

    @Override
    public void reshapeInLocal(@NonNull CssSize x, @NonNull CssSize y, @NonNull CssSize width, @NonNull CssSize height) {
        if (get(START_TARGET) == null) {
            set(START, new CssPoint2D(x, y));
        }
        if (get(END_TARGET) == null) {
            set(END, new CssPoint2D(x.add(width), y.add(height)));
        }
    }

    public void setEndConnection(Figure target, Connector connector) {
        set(END_CONNECTOR, connector);
        set(END_TARGET, target);
    }

    public void setStartConnection(Figure target, Connector connector) {
        set(START_CONNECTOR, connector);
        set(START_TARGET, target);
    }

    protected void updateConnectedProperty() {
        connected.set(get(START_CONNECTOR) != null
                && get(START_TARGET) != null && get(END_CONNECTOR) != null && get(END_TARGET) != null);
    }

    public @NonNull ReadOnlyBooleanWrapper connectedProperty() {
        return connected;
    }
}
