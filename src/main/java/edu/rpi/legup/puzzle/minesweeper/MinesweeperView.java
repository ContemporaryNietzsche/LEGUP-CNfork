package edu.rpi.legup.puzzle.minesweeper;

import edu.rpi.legup.controller.BoardController;
import edu.rpi.legup.model.gameboard.PuzzleElement;
import edu.rpi.legup.ui.boardview.GridBoardView;
import java.awt.*;
import java.io.IOException;
import java.util.Objects;
import javax.imageio.ImageIO;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;

public class MinesweeperView extends GridBoardView {

    private static final Logger LOGGER = LogManager.getLogger(MinesweeperView.class.getName());
    private static final Image BOMB_IMAGE;
    private static final Image EMPTY_IMAGE;
    private static final Color BACKGROUND_COLOR = new Color(230, 230, 250); // Lavender background
    private static final Font CELL_FONT = new Font("Arial", Font.BOLD, 20);

    static {
        Image tempBombImage = null;
        Image tempEmptyImage = null;
        try {
            tempBombImage = ImageIO.read(Objects.requireNonNull(
                    ClassLoader.getSystemClassLoader().getResource(
                            "edu/rpi/legup/images/minesweeper/tiles/Bomb.png")));
            tempEmptyImage = ImageIO.read(Objects.requireNonNull(
                    ClassLoader.getSystemClassLoader().getResource(
                            "edu/rpi/legup/images/minesweeper/tiles/Empty.png")));
        } catch (IOException e) {
            LOGGER.error("Failed to open Minesweeper images");
        }
        BOMB_IMAGE = tempBombImage;
        EMPTY_IMAGE = tempEmptyImage;
    }

    public MinesweeperView(@NotNull MinesweeperBoard board) {
        super(new BoardController(), new MinesweeperController(), board.getDimension());
        setBackground(BACKGROUND_COLOR);

        for (PuzzleElement<?> puzzleElement : board.getPuzzleElements()) {
            final MinesweeperCell cell = (MinesweeperCell) puzzleElement;
            final Point loc = cell.getLocation();
            final MinesweeperElementView elementView = new MinesweeperElementView(cell);
            elementView.setIndex(cell.getIndex());
            elementView.setSize(elementSize);
            elementView.setFont(CELL_FONT); // Set custom font
            elementView.setLocation(new Point(loc.x * elementSize.width, loc.y * elementSize.height));
            elementView.setOpaque(true);
            elementView.setBackground(new Color(245, 245, 220)); // Beige color for cells
            elementView.setBorder(BorderFactory.createLineBorder(Color.DARK_GRAY)); // Cell border
            elementViews.add(elementView);
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        g.setColor(Color.DARK_GRAY);
        g.setFont(CELL_FONT);
        g.drawString("Minesweeper", getWidth() / 2 - 50, 30); // Title at the top
    }
}
