package org.igv.sam;

import htsjdk.samtools.util.Locatable;
import org.igv.feature.genome.ChromosomeNameComparator;
import org.igv.sam.mods.BaseModificationSet;

import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;
import java.util.function.ToIntFunction;

public enum SortOption {

    START {
        @Override
        Comparator<Alignment> getAlignmentComparator(final int center, final String tag, final byte referenceBase) {
            return Comparator.comparingInt(Alignment::getAlignmentStart);
        }
    }, STRAND {
        @Override
        Comparator<Alignment> getAlignmentComparator(final int center, final String tag, final byte referenceBase) {
            return nullSafeComparator((Alignment a) -> a instanceof LinkedAlignment
                    ? ((LinkedAlignment) a).getStrandAtPosition(center)
                    : a.getReadStrand());
        }
    }, BASE {
        @Override
        Comparator<Alignment> getAlignmentComparator(final int center, final String tag, final byte referenceBase) {

            final Comparator<Alignment> insertionComparator = Comparator.comparing((Alignment alignment) -> {
                String insertionBases = "";
                AlignmentBlock leftInsertion = alignment.getInsertionAt(center + 1); //todo figure out what's going on with the +1 here..
                if (leftInsertion != null) {
                    insertionBases += leftInsertion.getBases().getString();
                }
                AlignmentBlock rightInsertion = alignment.getInsertionAt(center);
                if (rightInsertion != null) {
                    insertionBases += rightInsertion.getBases().getString();
                }
                return insertionBases;
            }).reversed();

            final int refBase = referenceBase >= 97 ? referenceBase - 32 : referenceBase;

            final ToIntFunction<Alignment> baseCompare = (Alignment a) -> {
                int base = a.getBase(center);
                if (base >= 97) base -= 32; //normalize case
                if (base == refBase) base = Integer.MAX_VALUE - 3;
                if (base == (int) 'N') base = Integer.MAX_VALUE - 2;
                if (base == 0) base = 97;  // > any letter, causes deletions to be placed after snps
                return base;
            };

            final Comparator<Alignment> deletionComparator = Comparator.comparing(
                    (Alignment a) -> a.getDeletionAt(center),
                    Comparator.nullsLast(Comparator.comparing(Gap::getnBases).thenComparing(Gap::getStart)
                    ));

            final ToIntFunction<Alignment> baseQualityCompare = ((Alignment a) -> -a.getPhred(center));

            return Comparator.comparingInt(baseCompare)
                    .thenComparing(deletionComparator)
                    .thenComparing(insertionComparator)
                    .thenComparingInt(baseQualityCompare);

        }
    }, BASE_MODIFICATION {
        @Override
        Comparator<Alignment> getAlignmentComparator(final int center, final String tag, final byte referenceBase) {
            return Comparator.comparing(
                            (Alignment a) -> getBaseModificationLikelihood(a, center, tag),
                            Comparator.nullsLast(Comparator.reverseOrder()))
                    .thenComparingInt(Alignment::getAlignmentStart);
        }
    }, QUALITY {
        @Override
        Comparator<Alignment> getAlignmentComparator(final int center, final String tag, final byte referenceBase) {
            return Comparator.comparingInt(Alignment::getMappingQuality).reversed();
        }
    }, SAMPLE {
        @Override
        Comparator<Alignment> getAlignmentComparator(final int center, final String tag, final byte referenceBase) {
            return nullSafeComparator(Alignment::getSample);
        }
    }, READ_GROUP {
        @Override
        Comparator<Alignment> getAlignmentComparator(final int center, final String tag, final byte referenceBase) {
            return nullSafeComparator(Alignment::getReadGroup);
        }
    }, INSERT_SIZE {
        @Override
        Comparator<Alignment> getAlignmentComparator(final int center, final String tag, final byte referenceBase) {
            return Comparator.comparingInt((Alignment a) -> Math.abs(a.getInferredInsertSize())).reversed();
        }
    }, FIRST_OF_PAIR_STRAND {
        @Override
        Comparator<Alignment> getAlignmentComparator(final int center, final String tag, final byte referenceBase) {
            return nullSafeComparator(Alignment::getFirstOfPairStrand);
        }
    }, MATE_CHR {
        @Override
        Comparator<Alignment> getAlignmentComparator(final int center, final String tag, final byte referenceBase) {
            return Comparator.comparing((Alignment a) -> a.getMate() == null)
                    .thenComparing(a -> a.getMate() != null && Objects.equals(a.getMate().getChr(), a.getChr()))
                    .thenComparing(nullSafeComparator(a -> a.getMate() == null ? null : a.getMate().getChr()));

        }
    }, TAG {
        @Override
        Comparator<Alignment> getAlignmentComparator(final int center, final String tag, final byte referenceBase) {
            return Comparator.comparing((Alignment a) -> a.getAttribute(tag),
                    Comparator.nullsLast(Comparator.comparing(Object::hashCode)));
            //todo It would be nice to sort by something smarter than hash code but the possibility of mixed tag types makes that more complicated
        }
    },
    LEFT_CLIP {
        @Override
        Comparator<Alignment> getAlignmentComparator(final int center, final String tag, final byte referenceBase) {
            return Comparator.comparing((Alignment a) -> a.getClippingCounts().isLeftClipped()).reversed()
                    .thenComparing(Alignment::getAlignmentStart);
        }
    },
    RIGHT_CLIP {
        @Override
        Comparator<Alignment> getAlignmentComparator(final int center, final String tag, final byte referenceBase) {
            return Comparator.comparing((Alignment a) -> a.getClippingCounts().isRightClipped()).reversed()
                    .thenComparing(Alignment::getAlignmentEnd);
        }
    },
    SUPPLEMENTARY {
        @Override
        Comparator<Alignment> getAlignmentComparator(final int center, final String tag, final byte referenceBase) {
            return Comparator.comparing(Alignment::isSupplementary).reversed();
        }
    }, NONE {
        @Override
        Comparator<Alignment> getAlignmentComparator(final int center, final String tag, final byte referenceBase) {
            return (a1, a2) -> 0;
        }
    }, HAPLOTYPE {
        @Override
        Comparator<Alignment> getAlignmentComparator(final int center, final String tag, final byte referenceBase) {
            return Comparator.comparingInt(Alignment::getClusterDistance);
        }
    }, READ_ORDER {
        @Override
        Comparator<Alignment> getAlignmentComparator(final int center, final String tag, final byte referenceBase) {
            return Comparator.comparing(Alignment::isPaired)
                    .thenComparing(Alignment::isFirstOfPair)
                    .thenComparing(Alignment::isSecondOfPair).reversed();
        }
    }, READ_NAME {
        @Override
        Comparator<Alignment> getAlignmentComparator(final int center, final String tag, final byte referenceBase) {
            return Comparator.comparing(Alignment::getReadName);
        }
    }, ALIGNED_READ_LENGTH {
        @Override
        Comparator<Alignment> getAlignmentComparator(final int center, final String tag, final byte referenceBase) {
            return Comparator.comparingInt(a -> a.getAlignmentStart() - a.getAlignmentEnd());
        }
    };

    // Slightly shortened way of writing this common use case
    protected final <T, U extends Comparable<? super U>> Comparator<T> nullSafeComparator(final Function<? super T, ? extends U> extractor) {
        return Comparator.comparing(extractor, Comparator.nullsLast(Comparator.naturalOrder()));
    }

    /**
     * get a Comparator that will perform the relevant sort.
     */
    public Comparator<Row> getComparator(final int center, final byte reference, final String tag, boolean invertSort) {
        Comparator<Alignment> alignmentComparator = getAlignmentComparator(center, tag, reference);
        if (invertSort) alignmentComparator = alignmentComparator.reversed();
        return Comparator.comparing((Row row) -> getFeatureContaining(row.getAlignments(), center, tag),
                Comparator.nullsLast(alignmentComparator));

    }

    // private method to save a few lines of code in each comparator since they all would have to start with getting the
    // center alignment anyway
    abstract Comparator<Alignment> getAlignmentComparator(final int center, final String tag, final byte referenceBase);

    private Alignment getFeatureContaining(List<Alignment> alignments, int position, String tag) {
        Alignment alignment = AlignmentInterval.getFeatureContaining(alignments, position);
        if (this == BASE_MODIFICATION && "m".equals(tag)) {
            for (int candidatePosition : new int[]{position, position - 1, position + 1}) {
                Alignment candidate = AlignmentInterval.getFeatureContaining(alignments, candidatePosition);
                if (candidate != null && getBaseModificationLikelihood(candidate, position, tag) != null) {
                    return candidate;
                }
            }
            if (alignment == null) {
                alignment = AlignmentInterval.getFeatureContaining(alignments, position - 1);
                if (alignment == null) {
                    alignment = AlignmentInterval.getFeatureContaining(alignments, position + 1);
                }
            }
        }
        return alignment;
    }

    private static Integer getBaseModificationLikelihood(Alignment alignment, int position, String modification) {

        int[] modifiedPositions = getBaseModificationSortPositions(alignment, position, modification);
        if (modifiedPositions.length == 0) {
            return null;
        }

        List<BaseModificationSet> baseModificationSets = alignment.getBaseModificationSets();
        if (baseModificationSets == null) {
            return null;
        }

        Integer maxLikelihood = null;
        for (BaseModificationSet bmSet : baseModificationSets) {
            if (modification != null && !modification.equals(bmSet.getModification())) {
                continue;
            }

            for (int modifiedPosition : modifiedPositions) {
                Integer readIndex = getReadIndex(alignment, modifiedPosition);
                if (readIndex != null && bmSet.containsPosition(readIndex)) {
                    int likelihood = Byte.toUnsignedInt(bmSet.getLikelihoods().get(readIndex));
                    maxLikelihood = maxLikelihood == null ? likelihood : Math.max(maxLikelihood, likelihood);
                }
            }
        }
        return maxLikelihood;
    }

    private static int[] getBaseModificationSortPositions(Alignment alignment, int position, String modification) {
        if (!"m".equals(modification)) {
            return new int[]{position};
        }

        byte previousBase = uppercase(alignment.getBase(position - 1));
        byte base = uppercase(alignment.getBase(position));
        byte nextBase = uppercase(alignment.getBase(position + 1));
        if ((base == 'C' && nextBase == 'G') || (base == 0 && nextBase == 'G')) {
            return new int[]{position, position + 1};
        } else if ((base == 'G' && previousBase == 'C') || (base == 0 && previousBase == 'C')) {
            return new int[]{position - 1, position};
        } else {
            return new int[0];
        }
    }

    private static byte uppercase(byte base) {
        return base >= 'a' && base <= 'z' ? (byte) (base - 32) : base;
    }

    private static Integer getReadIndex(Alignment alignment, int position) {
        for (AlignmentBlock block : alignment.getAlignmentBlocks()) {
            int offset = position - block.getStart();
            if (offset >= 0 && offset < block.getBasesLength()) {
                return block.getBasesOffset() + offset;
            }
        }
        return null;
    }

    /**
     * Custom valueOf method with backward compatibility.
     * Supports "NUCLEOTIDE" as an alias for "BASE" for backward compatibility.
     *
     * @param name the string name of the enum constant
     * @return the enum constant with the specified name
     * @throws IllegalArgumentException if no constant with the specified name is found
     */
    public static SortOption fromString(String name) {
        if (name == null) {
            return SortOption.NONE;
        }
        if ("NUCLEOTIDE".equals(name)) {
            return BASE;
        }
        return SortOption.valueOf(name);
    }

    public static final Comparator<Locatable> POSITION_COMPARATOR = Comparator.nullsFirst(Comparator.comparing(Locatable::getContig, ChromosomeNameComparator.get()))
            .thenComparing(Locatable::getStart)
            .thenComparing(Locatable::getEnd);
}
