package edu.rpi.legup.puzzle.minesweeper;

import edu.rpi.legup.model.gameboard.GridBoard;
import java.util.Random;

public class MinesweeperBoard extends GridBoard {

    public MinesweeperBoard(int width, int height, int numBombs) {
        super(width, height);
        placeBombs(numBombs);
    }

    public MinesweeperBoard(int size, int numBombs) {
        super(size);
        placeBombs(numBombs);
    }

    @Override
    public MinesweeperCell getCell(int x, int y) {
        return (MinesweeperCell) super.getCell(x, y);
    }

    /**
     * Performs a deep copy of the Board
     *
     * @return a new copy of the board that is independent of this one
     */
    @Override
    public MinesweeperBoard copy() {
        MinesweeperBoard newMinesweeperBoard =
                new MinesweeperBoard(this.dimension.width, this.dimension.height);
        for (int x = 0; x < this.dimension.width; x++) {
            for (int y = 0; y < this.dimension.height; y++) {
                newMinesweeperBoard.setCell(x, y, getCell(x, y).copy());
            }
        }
        return newMinesweeperBoard;
    }

    /**
     * Randomly places a given number of bombs on the board.
     *
     * @param numBombs The number of bombs to place.
     */
    private void placeBombs(int numBombs) {
        Random random = new Random();
        int width = this.dimension.width;
        int height = this.dimension.height;

        for (int i = 0; i < numBombs; i++) {
            int x, y;
            do {
                x = random.nextInt(width);
                y = random.nextInt(height);
            } while (getCell(x, y).hasBomb()); // Ensure no duplicate bombs

            getCell(x, y).setBomb(true);
        }
    }
}
