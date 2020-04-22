/*
This file is part of PeerUnit.

PeerUnit is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

PeerUnit is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with PeerUnit.  If not, see <http://www.gnu.org/licenses/>.
 */
package fr.inria.peerunit.base;

/**
 *
 * @author sunye
 */
public abstract class Range {

    public final static Range ALL = new AllValues();
    private static String RANGE_SEPARATOR = "-";
    private static String UNION_SEPARATOR = ",";

    public abstract boolean includes(int i);

    public static Range fromString(String str) {
        String[] values = str.split(UNION_SEPARATOR);
        Range[] ranges = new Range[values.length];
        
        for (int i = 0; i < values.length; i++) {
            ranges[i] = fromUnion(values[i].trim());
        }
        
        if (values.length == 1) {
            return ranges[0];
        } else {
            return new Union(ranges);
        }
    }

    protected static Range fromUnion(String str) {
        if (str.equals("*")) {
            return ALL;
        }
        String[] values = str.split(RANGE_SEPARATOR);
        if (values.length == 1) {
            return newInstance(Integer.parseInt(values[0]));
        }
        if (values.length == 2) {
            int start, end;
            start = Integer.parseInt(values[0]);
            if ("*".equals(values[1])) {
                end = Integer.MAX_VALUE;
            } else {
                end = Integer.parseInt(values[1]);
            }

            return newInstance(start,end);
        }

        return null;
    }

    public static Range newInstance() {
        return ALL;
    }

    public static Range newInstance(int i) {
        return new SingleValue(i);
    }

    public static Range newInstance(int v1, int v2) {
        return v1 > v2 ? new Interval(v2, v1) : new Interval(v1, v2);
    }

}

class AllValues extends Range {

    @Override
    public boolean includes(int i) {
        return i >= 0;
    }
}

class Interval extends Range {

    private int from;
    private int to;

    protected Interval(int from, int to) {
        assert to > from;

        this.from = from;
        this.to = to;
    }

    @Override
    public boolean includes(int i) {
        return i >= from && i <= to;
    }
}

class SingleValue extends Range {

    private final int value;

    protected SingleValue(int i) {
        assert i >= 0;

        value = i;
    }

    @Override
    public boolean includes(int i) {
        return i == value;
    }
}

class Union extends Range {

    private Range[] ranges;

    protected Union(Range[] r) {
        ranges = r;
    }

    @Override
    public boolean includes(int i) {
        boolean result = false;
        int cc = 0;
        while(!result && cc < ranges.length) {
            result = ranges[cc].includes(i);
            cc++;
        }

        return result;
    }
}