package io.microdev.source.util;

public class Range<T extends Number> {

    private T begin;
    private T end;

    public Range(T begin, T end) {
        this.begin = begin;
        this.end = end;
    }

    public T getBegin() {
        return begin;
    }

    public void setBegin(T begin) {
        this.begin = begin;
    }

    public T getEnd() {
        return end;
    }

    public void setEnd(T end) {
        this.end = end;
    }

    @SuppressWarnings("unchecked")
    public T getRange() {
        if (begin instanceof Byte) {
            return (T) Byte.valueOf((byte) (end.byteValue() - begin.byteValue()));
        }
        if (begin instanceof Short) {
            return (T) Short.valueOf((short) (end.intValue() - begin.intValue()));
        }
        if (begin instanceof Integer) {
            return (T) Integer.valueOf(end.intValue() - begin.intValue());
        }
        if (begin instanceof Long) {
            return (T) Long.valueOf(end.longValue() - begin.longValue());
        }
        if (begin instanceof Float) {
            return (T) Float.valueOf(end.floatValue() - begin.floatValue());
        }
        if (begin instanceof Double) {
            return (T) Double.valueOf(end.doubleValue() - begin.doubleValue());
        }

        return null;
    }

    @SuppressWarnings("unchecked")
    public T getSpread() {
        if (begin instanceof Byte) {
            return (T) Byte.valueOf((byte) Math.abs(end.byteValue() - begin.byteValue()));
        }
        if (begin instanceof Short) {
            return (T) Short.valueOf((short) Math.abs(end.intValue() - begin.intValue()));
        }
        if (begin instanceof Integer) {
            return (T) Integer.valueOf(Math.abs(end.intValue() - begin.intValue()));
        }
        if (begin instanceof Long) {
            return (T) Long.valueOf(Math.abs(end.longValue() - begin.longValue()));
        }
        if (begin instanceof Float) {
            return (T) Float.valueOf(Math.abs(end.floatValue() - begin.floatValue()));
        }
        if (begin instanceof Double) {
            return (T) Double.valueOf(Math.abs(end.doubleValue() - begin.doubleValue()));
        }

        return null;
    }

    @Override
    public boolean equals(Object o) {
        if (o instanceof Range) {
            Range oRange = (Range) o;

            if (oRange.begin instanceof Number && oRange.end instanceof Number) {
                return oRange.begin.equals(begin) && oRange.end.equals(end);
            }
        }

        return super.equals(o);
    }

    @Override
    public String toString() {
        return "[" + begin + "," + end + "]";
    }

}
