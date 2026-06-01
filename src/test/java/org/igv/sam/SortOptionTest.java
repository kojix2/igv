package org.igv.sam;

import org.igv.feature.LocusScore;
import org.igv.feature.Strand;
import org.igv.sam.mods.BaseModificationSet;
import org.igv.track.WindowFunction;
import org.junit.Test;

import java.awt.*;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertSame;

public class SortOptionTest {

    @Test
    public void testBaseModificationSortUsesClickedPositionLikelihood() {
        TestAlignment high = new TestAlignment(100, 200, new BaseModificationSet('C', '+', "m", Map.of(201, (byte) 230)));
        TestAlignment low = new TestAlignment(100, 200, new BaseModificationSet('C', '+', "m", Map.of(201, (byte) 20)));
        TestAlignment missing = new TestAlignment(100, 200, null);

        List<Alignment> alignments = new ArrayList<>(List.of(missing, low, high));
        Comparator<Alignment> comparator = SortOption.BASE_MODIFICATION.getAlignmentComparator(101, "m", (byte) 0);
        alignments.sort(comparator);

        assertSame(high, alignments.get(0));
        assertSame(low, alignments.get(1));
        assertSame(missing, alignments.get(2));
    }

    @Test
    public void testMethylationSortCombinesCpGStrandsFromCPosition() {
        TestAlignment plusStrandC = new TestAlignment(100, 200, new BaseModificationSet('C', '+', "m", Map.of(201, (byte) 20)));
        TestAlignment minusStrandG = new TestAlignment(100, 200, new BaseModificationSet('G', '-', "m", Map.of(202, (byte) 230)));
        TestAlignment missing = new TestAlignment(100, 200, null);

        List<Alignment> alignments = new ArrayList<>(List.of(missing, plusStrandC, minusStrandG));
        Comparator<Alignment> comparator = SortOption.BASE_MODIFICATION.getAlignmentComparator(101, "m", (byte) 0);
        alignments.sort(comparator);

        assertSame(minusStrandG, alignments.get(0));
        assertSame(plusStrandC, alignments.get(1));
        assertSame(missing, alignments.get(2));
    }

    @Test
    public void testMethylationSortCombinesCpGStrandsFromGPosition() {
        TestAlignment plusStrandC = new TestAlignment(100, 200, new BaseModificationSet('C', '+', "m", Map.of(201, (byte) 20)));
        TestAlignment minusStrandG = new TestAlignment(100, 200, new BaseModificationSet('G', '-', "m", Map.of(202, (byte) 230)));
        TestAlignment missing = new TestAlignment(100, 200, null);

        List<Alignment> alignments = new ArrayList<>(List.of(missing, plusStrandC, minusStrandG));
        Comparator<Alignment> comparator = SortOption.BASE_MODIFICATION.getAlignmentComparator(102, "m", (byte) 0);
        alignments.sort(comparator);

        assertSame(minusStrandG, alignments.get(0));
        assertSame(plusStrandC, alignments.get(1));
        assertSame(missing, alignments.get(2));
    }

    @Test
    public void testMethylationSortUsesVisibleCpGPartnerWhenClickedBaseIsNotCovered() {
        TestAlignment minusStrandG = new TestAlignment(102, 200, "GT", new BaseModificationSet('G', '-', "m", Map.of(200, (byte) 230)));
        TestAlignment missing = new TestAlignment(102, 200, "GT", null);

        List<Alignment> alignments = new ArrayList<>(List.of(missing, minusStrandG));
        Comparator<Alignment> comparator = SortOption.BASE_MODIFICATION.getAlignmentComparator(101, "m", (byte) 0);
        alignments.sort(comparator);

        assertSame(minusStrandG, alignments.get(0));
        assertSame(missing, alignments.get(1));
    }

    @Test
    public void testMethylationSortIgnoresNonCpGMethylation() {
        TestAlignment nonCpgMethylated = new TestAlignment(100, 200, "ATGT",
                new BaseModificationSet('T', '+', "m", Map.of(201, (byte) 230)));
        TestAlignment missing = new TestAlignment(100, 200, "ATGT", null);

        List<Alignment> alignments = new ArrayList<>(List.of(missing, nonCpgMethylated));
        Comparator<Alignment> comparator = SortOption.BASE_MODIFICATION.getAlignmentComparator(101, "m", (byte) 0);
        alignments.sort(comparator);

        assertSame(missing, alignments.get(0));
        assertSame(nonCpgMethylated, alignments.get(1));
    }

    private static class TestAlignment implements Alignment {

        private final AlignmentBlock[] blocks;
        private final List<BaseModificationSet> baseModificationSets;

        TestAlignment(int start, int readOffset, BaseModificationSet baseModificationSet) {
            this(start, readOffset, "ACGT", baseModificationSet);
        }

        TestAlignment(int start, int readOffset, String bases, BaseModificationSet baseModificationSet) {
            this.blocks = new AlignmentBlock[]{new AlignmentBlockImpl(start, bases.getBytes(), null, readOffset, bases.length(), 'M')};
            this.baseModificationSets = baseModificationSet == null ? null : List.of(baseModificationSet);
        }

        @Override
        public int getAlignmentStart() {
            return blocks[0].getStart();
        }

        @Override
        public int getAlignmentEnd() {
            return blocks[0].getEnd();
        }

        @Override
        public boolean contains(double location) {
            return location >= getAlignmentStart() && location < getAlignmentEnd();
        }

        @Override
        public AlignmentBlock[] getAlignmentBlocks() {
            return blocks;
        }

        @Override
        public AlignmentBlock[] getInsertions() {
            return new AlignmentBlock[0];
        }

        @Override
        public List<Gap> getGaps() {
            return List.of();
        }

        @Override
        public int getInferredInsertSize() {
            return 0;
        }

        @Override
        public int getMappingQuality() {
            return 0;
        }

        @Override
        public Strand getReadStrand() {
            return Strand.NONE;
        }

        @Override
        public boolean isProperPair() {
            return false;
        }

        @Override
        public boolean isMapped() {
            return true;
        }

        @Override
        public boolean isPaired() {
            return false;
        }

        @Override
        public boolean isFirstOfPair() {
            return false;
        }

        @Override
        public boolean isSecondOfPair() {
            return false;
        }

        @Override
        public boolean isNegativeStrand() {
            return false;
        }

        @Override
        public boolean isDuplicate() {
            return false;
        }

        @Override
        public boolean isPrimary() {
            return true;
        }

        @Override
        public boolean isSupplementary() {
            return false;
        }

        @Override
        public byte getBase(double position) {
            int basePosition = (int) position;
            for (AlignmentBlock block : blocks) {
                if (block.contains(basePosition)) {
                    return block.getBase(basePosition - block.getStart());
                }
            }
            return 0;
        }

        @Override
        public byte getPhred(double position) {
            return 0;
        }

        @Override
        public void setMateSequence(String sequence) {
        }

        @Override
        public String getPairOrientation() {
            return null;
        }

        @Override
        public Strand getFirstOfPairStrand() {
            return Strand.NONE;
        }

        @Override
        public Strand getSecondOfPairStrand() {
            return Strand.NONE;
        }

        @Override
        public boolean isVendorFailedRead() {
            return false;
        }

        @Override
        public Color getYcColor() {
            return null;
        }

        @Override
        public List<BaseModificationSet> getBaseModificationSets() {
            return baseModificationSets;
        }

        @Override
        public float getScore() {
            return 0;
        }

        @Override
        public String getValueString(double position, int mouseX, WindowFunction windowFunction) {
            return null;
        }

        @Override
        public LocusScore copy() {
            return null;
        }

        @Override
        public String getContig() {
            return "";
        }

        @Override
        public int getStart() {
            return getAlignmentStart();
        }

        @Override
        public int getEnd() {
            return getAlignmentEnd();
        }
    }
}
