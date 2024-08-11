package edu.rpi.legup.puzzle.minesweeper.rules;

import edu.rpi.legup.model.gameboard.Board;
import edu.rpi.legup.model.gameboard.CaseBoard;
import edu.rpi.legup.model.gameboard.PuzzleElement;
import edu.rpi.legup.model.rules.CaseRule;
import edu.rpi.legup.model.tree.TreeNode;
import edu.rpi.legup.model.tree.TreeTransition;
import edu.rpi.legup.puzzle.minesweeper.*;

import java.awt.*;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class SatisfyFlagCaseRule extends CaseRule {
    public SatisfyFlagCaseRule() {
        super(
                "MINE-CASE-0002",
                "Satisfy Flag",
                "Create a different path for each valid way to mark bombs and filled cells around a flag",
                "edu/rpi/legup/images/minesweeper/cases/Satisfy_Flag.png");
    }

    @Override
    public CaseBoard getCaseBoard(Board board) {
        MinesweeperBoard minesweeperBoard = (MinesweeperBoard) board.copy();
        CaseBoard caseBoard = new CaseBoard(minesweeperBoard, this);
        minesweeperBoard.setModifiable(false);

        for (PuzzleElement element : minesweeperBoard.getPuzzleElements()) {
            MinesweeperCell cell = (MinesweeperCell) element;
            if (isValidFlagCell(cell, minesweeperBoard)) {
                caseBoard.addPickableElement(element);
            }
        }
        return caseBoard;
    }

    private boolean isValidFlagCell(MinesweeperCell cell, MinesweeperBoard board) {
        int cellNumber = cell.getTileNumber();
        return cellNumber > 0 && cellNumber <= 8 && MinesweeperUtilities.hasEmptyAdjacent(board, cell);
    }

    @Override
    public ArrayList<Board> getCases(Board board, PuzzleElement puzzleElement) {
        ArrayList<Board> cases = new ArrayList<>();

        MinesweeperBoard minesweeperBoard = (MinesweeperBoard) board.copy();
        MinesweeperCell cell = (MinesweeperCell) minesweeperBoard.getPuzzleElement(puzzleElement);

        int maxBombsAllowed = cell.getTileNumber();
        if (maxBombsAllowed <= 0 || maxBombsAllowed > 8) {
            return cases;
        }

        List<MinesweeperCell> adjacentCells = MinesweeperUtilities.getAdjacentCells(minesweeperBoard, cell);
        int placedBombs = 0;
        int unsetCellsCount = 0;
        ArrayList<MinesweeperCell> unsetCells = new ArrayList<>();

        for (MinesweeperCell adjCell : adjacentCells) {
            if (adjCell.getTileType() == MinesweeperTileType.BOMB) {
                placedBombs++;
            } else if (adjCell.getTileType() == MinesweeperTileType.UNSET) {
                unsetCellsCount++;
                unsetCells.add(adjCell);
            }
        }

        if (placedBombs >= maxBombsAllowed || unsetCellsCount == 0) {
            return cases;
        }

        ArrayList<boolean[]> bombPlacements = MinesweeperUtilities.getCombinations(maxBombsAllowed - placedBombs, unsetCellsCount);

        for (boolean[] placement : bombPlacements) {
            Board caseBoard = board.copy();
            for (int i = 0; i < placement.length; i++) {
                MinesweeperCell unsetCell = (MinesweeperCell) caseBoard.getPuzzleElement(unsetCells.get(i));
                unsetCell.setCellType(placement[i] ? MinesweeperTileData.bomb() : MinesweeperTileData.empty());
                caseBoard.addModifiedData(unsetCell);
            }
            cases.add(caseBoard);
        }

        return cases;
    }

    @Override
    public String checkRuleRaw(TreeTransition transition) {
        TreeNode parent = transition.getParents().get(0);
        List<TreeTransition> childTransitions = parent.getChildren();

        Set<PuzzleElement> modifiedCells = transition.getBoard().getModifiedData();
        if (modifiedCells.isEmpty()) {
            return super.getInvalidUseOfRuleMessage();
        }

        if (!areModifiedCellsWithinSquare(modifiedCells, 3)) {
            return super.getInvalidUseOfRuleMessage();
        }

        MinesweeperBoard board = (MinesweeperBoard) parent.getBoard();
        List<MinesweeperCell> possibleCenters = findPossibleCenters(modifiedCells, board);

        if (possibleCenters.isEmpty()) {
            return super.getInvalidUseOfRuleMessage();
        }

        for (MinesweeperCell center : possibleCenters) {
            if (isValidTransition(center, board, modifiedCells, childTransitions)) {
                return null;
            }
        }

        return super.getInvalidUseOfRuleMessage();
    }

    private boolean areModifiedCellsWithinSquare(Set<PuzzleElement> modifiedCells, int squareSize) {
        int minX = Integer.MAX_VALUE, maxX = Integer.MIN_VALUE;
        int minY = Integer.MAX_VALUE, maxY = Integer.MIN_VALUE;

        for (PuzzleElement modCell : modifiedCells) {
            Point loc = ((MinesweeperCell) modCell).getLocation();
            minX = Math.min(minX, loc.x);
            maxX = Math.max(maxX, loc.x);
            minY = Math.min(minY, loc.y);
            maxY = Math.max(maxY, loc.y);
        }

        return maxX - minX <= squareSize && maxY - minY <= squareSize;
    }

    private List<MinesweeperCell> findPossibleCenters(Set<PuzzleElement> modifiedCells, MinesweeperBoard board) {
        Set<MinesweeperCell> possibleCenters = new HashSet<>();

        for (PuzzleElement modCell : modifiedCells) {
            List<MinesweeperCell> adjacentCells = MinesweeperUtilities.getAdjacentCells(board, (MinesweeperCell) modCell);
            possibleCenters.addAll(adjacentCells);
        }

        possibleCenters.removeIf(cell -> cell.getTileNumber() <= 0 || cell.getTileNumber() >= 9);
        return new ArrayList<>(possibleCenters);
    }

    private boolean isValidTransition(MinesweeperCell center, MinesweeperBoard board, Set<PuzzleElement> modifiedCells, List<TreeTransition> childTransitions) {
        int maxBombs = center.getTileNumber();
        List<MinesweeperCell> adjCells = MinesweeperUtilities.getAdjacentCells(board, center);

        int bombCount = 0;
        int emptyCount = 0;
        for (MinesweeperCell cell : adjCells) {
            if (cell.getTileType() == MinesweeperTileType.BOMB) {
                bombCount++;
            } else if (cell.getTileType() == MinesweeperTileType.UNSET) {
                emptyCount++;
            }
        }

        if (emptyCount == 0 || bombCount > maxBombs) {
            return false;
        }

        ArrayList<boolean[]> combinations = MinesweeperUtilities.getCombinations(maxBombs - bombCount, emptyCount);

        if (combinations.size() != childTransitions.size()) {
            return false;
        }

        for (TreeTransition transition : childTransitions) {
            MinesweeperBoard transitionBoard = (MinesweeperBoard) transition.getBoard();
            boolean[] transitionState = new boolean[adjCells.size()];

            for (int i = 0; i < adjCells.size(); i++) {
                MinesweeperCell adjCell = adjCells.get(i);
                transitionState[i] = transitionBoard.getPuzzleElement(adjCell).getTileType() == MinesweeperTileType.BOMB;
            }

            boolean matched = combinations.removeIf(combination -> Arrays.equals(combination, transitionState));

            if (!matched) {
                return false;
            }
        }

        return combinations.isEmpty();
    }

    @Override
    public String checkRuleRawAt(TreeTransition transition, PuzzleElement puzzleElement) {
        return null;
    }
}
