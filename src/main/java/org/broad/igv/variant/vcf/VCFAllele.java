/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2007-2015 Broad Institute
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

package org.broad.igv.variant.vcf;

import org.broad.igv.variant.Allele;

/**
 * @author Jim Robinson
 * @date Aug 1, 2011
 */
public class VCFAllele implements Allele {

    htsjdk.variant.variantcontext.Allele htsjdkAllele;


    public VCFAllele(htsjdk.variant.variantcontext.Allele htsjdkAllele) {
        this.htsjdkAllele = htsjdkAllele;
    }

    public byte[] getBases() {
        return htsjdkAllele.getBases();
    }

    public boolean isNonRefAllele() {
        return htsjdkAllele.isNonRefAllele();
    }

    @Override
    public String getDisplayString() {
        return toString();
    }

    public String toString() {
        if (htsjdkAllele.isNonRefAllele()) {
            return "NON_REF";
        } else
            return htsjdkAllele.getDisplayString();
    }

}
