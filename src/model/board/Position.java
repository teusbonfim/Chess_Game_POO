package model.board;

public class Position {
    private int row;
    private int column;
    
    public Position(int row, int column) {
        this.row = row;
        this.column = column;
    }
    
    public int getRow() { return row; }
    public void setRow(int row) { this.row = row; }
    public int getColumn() { return column; }
    public void setColumn(int column) { this.column = column; }
    
    public boolean isValid() {
        return row >= 0 && row < 8 && column >= 0 && column < 8;
    }
    
    @Override
    public String toString() {
        return (char)('a' + column) + "" + (8 - row);
    }
}
