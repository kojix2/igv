package org.igv.sam;

import org.junit.Test;

import java.awt.Rectangle;

import static org.junit.Assert.assertEquals;

public class AlignmentTrackTest {

    @Test
    public void computeVisibleRowRangeStartsAtFirstRowWhenClipIsAboveAlignments() {
        AlignmentTrack.VisibleRowRange range = AlignmentTrack.computeVisibleRowRange(
                new Rectangle(0, 0, 100, 40),
                100,
                10,
                20
        );

        assertEquals(0, range.firstRow);
        assertEquals(0, range.lastRow);
    }

    @Test
    public void computeVisibleRowRangeIncludesPaddingBeforeVisibleRows() {
        AlignmentTrack.VisibleRowRange range = AlignmentTrack.computeVisibleRowRange(
                new Rectangle(0, 145, 100, 30),
                100,
                10,
                20
        );

        assertEquals(3, range.firstRow);
        assertEquals(9, range.lastRow);
    }

    @Test
    public void computeVisibleRowRangeCapsLastRowAtRowCount() {
        AlignmentTrack.VisibleRowRange range = AlignmentTrack.computeVisibleRowRange(
                new Rectangle(0, 270, 100, 80),
                100,
                10,
                20
        );

        assertEquals(16, range.firstRow);
        assertEquals(20, range.lastRow);
    }

    @Test
    public void computeVisibleRowRangeReturnsEmptyRangeWhenClipIsBelowRows() {
        AlignmentTrack.VisibleRowRange range = AlignmentTrack.computeVisibleRowRange(
                new Rectangle(0, 400, 100, 40),
                100,
                10,
                20
        );

        assertEquals(20, range.firstRow);
        assertEquals(20, range.lastRow);
    }

    @Test
    public void computeVisibleRowRangeHandlesOnePixelRows() {
        AlignmentTrack.VisibleRowRange range = AlignmentTrack.computeVisibleRowRange(
                new Rectangle(0, 12, 100, 4),
                10,
                1,
                10
        );

        assertEquals(1, range.firstRow);
        assertEquals(7, range.lastRow);
    }

    @Test
    public void computeVisibleRowRangeHandlesEmptyRows() {
        AlignmentTrack.VisibleRowRange range = AlignmentTrack.computeVisibleRowRange(
                new Rectangle(0, 10, 100, 20),
                10,
                10,
                0
        );

        assertEquals(0, range.firstRow);
        assertEquals(0, range.lastRow);
    }
}
