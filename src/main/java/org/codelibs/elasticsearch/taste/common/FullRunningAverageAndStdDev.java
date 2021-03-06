/**
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.codelibs.elasticsearch.taste.common;

/**
 * <p>
 * Extends {@link FullRunningAverage} to add a running standard deviation computation.
 * Uses Welford's method, as described at http://www.johndcook.com/standard_deviation.html
 * </p>
 */
public final class FullRunningAverageAndStdDev extends FullRunningAverage
        implements RunningAverageAndStdDev {

    /**
     *
     */
    private static final long serialVersionUID = 1L;

    private double stdDev;

    private double mk;

    private double sk;

    public FullRunningAverageAndStdDev() {
        mk = 0.0;
        sk = 0.0;
        recomputeStdDev();
    }

    public FullRunningAverageAndStdDev(final int count, final double average,
            final double mk, final double sk) {
        super(count, average);
        this.mk = mk;
        this.sk = sk;
        recomputeStdDev();
    }

    public double getMk() {
        return mk;
    }

    public double getSk() {
        return sk;
    }

    @Override
    public synchronized double getStandardDeviation() {
        return stdDev;
    }

    @Override
    public synchronized void addDatum(final double datum) {
        super.addDatum(datum);
        final int count = getCount();
        if (count == 1) {
            mk = datum;
            sk = 0.0;
        } else {
            final double oldmk = mk;
            final double diff = datum - oldmk;
            mk += diff / count;
            sk += diff * (datum - mk);
        }
        recomputeStdDev();
    }

    @Override
    public synchronized void removeDatum(final double datum) {
        final int oldCount = getCount();
        super.removeDatum(datum);
        final double oldmk = mk;
        mk = (oldCount * oldmk - datum) / (oldCount - 1);
        sk -= (datum - mk) * (datum - oldmk);
        recomputeStdDev();
    }

    /**
     * @throws UnsupportedOperationException
     */
    @Override
    public void changeDatum(final double delta) {
        throw new UnsupportedOperationException();
    }

    private synchronized void recomputeStdDev() {
        final int count = getCount();
        stdDev = count > 1 ? Math.sqrt(sk / (count - 1)) : Double.NaN;
    }

    @Override
    public RunningAverageAndStdDev inverse() {
        return new InvertedRunningAverageAndStdDev(this);
    }

    @Override
    public synchronized String toString() {
        return String.valueOf(String.valueOf(getAverage()) + ',' + stdDev);
    }

}
