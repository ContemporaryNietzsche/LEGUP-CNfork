package edu.rpi.legup.puzzle.minesweeper.rules;

import edu.rpi.legup.model.gameboard.Board;
import edu.rpi.legup.model.gameboard.PuzzleElement;
import edu.rpi.legup.model.rules.ContradictionRule;
import edu.rpi.legup.puzzle.minesweeper.MinesweeperBoard;
import edu.rpi.legup.puzzle.minesweeper.MinesweeperCell;
import edu.rpi.legup.puzzle.minesweeper.MinesweeperTileType;
import edu.rpi.legup.puzzle.minesweeper.MinesweeperUtilities;

import java.util.ArrayList;
import java.util.List;

public class MoreBombsThanFlagContradictionRule extends ContradictionRule {

    public MoreBombsThanFlagContradictionRule() {
        super(
                "MINE-CONT-0001",
                "More Bombs Than Flag",
                "There cannot be more bombs around a flagged cell than the specified number.",
                "edu/rpi/legup/images/minesweeper/contradictions/Bomb_Surplus.jpg");
    }

    /**
     * Checks whether the transition has a contradiction at the specific puzzleElement index using
     * this rule.
     *
     * @param board board to check contradiction
     * @param puzzleElement equivalent puzzleElement
     * @return null if the transition contains a contradiction at the specified puzzleElement,
     *     otherwise error message
     */
    @Override
    public String checkContradictionAt(Board board, PuzzleElement puzzleElement) {
        MinesweeperBoard minesweeperBoard = (MinesweeperBoard) board;
        MinesweeperCell cell = (MinesweeperCell) minesweeperBoard.getPuzzleElement(puzzleElement);

        int cellNum = cell.getTileNumber();
        if (cellNum < 0 || cellNum >= 10) {
            return super.getNoContradictionMessage();
        }

        List<MinesweeperCell> adjCells = MinesweeperUtilities.getAdjacentCells(minesweeperBoard, cell);
        int numBombs = 0;
        int numFlags = 0;

        for (MinesweeperCell adjCell : adjCells) {
            if (adjCell.getTileType() == MinesweeperTileType.BOMB) {
                numBombs++;
            } else if (adjCell.getTileType() == MinesweeperTileType.FLAG) {
                numFlags++;
            }
        }

        if (numBombs > cellNum) {
            return String.format("Contradiction: %d bombs found around cell [%d, %d], but only %d allowed.",
                    numBombs, cell.getLocation().x, cell.getLocation().y, cellNum);
        }

        if (numFlags > cellNum) {
            return String.format("Warning: %d flags placed around cell [%d, %d], but only %d bombs are expected.",
                    numFlags, cell.getLocation().x, cell.getLocation().y, cellNum);
        }

        if (numFlags + numBombs < cellNum) {
            return String.format("Hint: Only %d bombs/flags placed around cell [%d, %d], but %d are needed.",
                    numFlags + numBombs, cell.getLocation().x, cell.getLocation().y, cellNum);
        }

        return super.getNoContradictionMessage();
    }
}
