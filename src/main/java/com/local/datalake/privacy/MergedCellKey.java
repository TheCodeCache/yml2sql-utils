package com.local.datalake.privacy;

/**
 * immutable key used in hashmap,
 * <li>basically it determines whether a particular cell is part of merged cell
 */
public final class MergedCellKey {

    // first row number (inclusive) of the merged cell - 0-based indexing
    private int firstRow;
    // last row number (inclusive) of the merged cell - 0-based indexing
    private int lastRow;
    // cell column index
    private int colIdx;

    public MergedCellKey(int firstRow, int lastRow, int colIdx) {
        super();
        this.firstRow = firstRow;
        this.lastRow = lastRow;
        this.colIdx = colIdx;
    }

    public int getFirstRow() {
        return firstRow;
    }

    public int getLastRow() {
        return lastRow;
    }

    public int getColIdx() {
        return colIdx;
    }

    /**
     * hashcode impl
     */
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + colIdx;
        result = prime * result + firstRow;
        result = prime * result + lastRow;
        return result;
    }

    /**
     * equals impl
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        MergedCellKey other = (MergedCellKey) obj;
        return colIdx == other.colIdx && firstRow == other.firstRow && lastRow == other.lastRow;
    }
}
