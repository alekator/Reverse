import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;


public class ReversiGameAI extends JFrame {
    private static final int BOARD_SIZE = 8;
    private static final char EMPTY = ' ';
    private static final char BLACK = '+';
    private static final char WHITE = '-';

    private char[][] board;
    private char currentPlayer;

    private JMenuBar menuBar;
    private JMenu gameMenu;
    private JMenuItem vsBotItem;
    private JMenuItem botVsBotItem;
    private JMenuItem vsPlayerItem;

    private GameMode currentGameMode;
    private Player player1;
    private Player player2;
    private GameListener gameListener;

    private JButton[][] cellButtons;

    public ReversiGameAI() {
        setTitle("Reversi Game");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(400, 400);
        setLayout(new GridLayout(BOARD_SIZE, BOARD_SIZE));

        initializeBoard();
        currentPlayer = BLACK;

        // Create the menu bar
        menuBar = new JMenuBar();

        // Create the "Game" menu
        gameMenu = new JMenu("Game");

        // Create menu items
        vsBotItem = new JMenuItem("Play vs Bot");
        botVsBotItem = new JMenuItem("Bot vs Bot");
        vsPlayerItem = new JMenuItem("Play vs Player");

        // Add action listeners to menu items
        vsBotItem.addActionListener(new MenuActionListener(GameMode.VS_BOT));
        botVsBotItem.addActionListener(new MenuActionListener(GameMode.BOT_VS_BOT));
        vsPlayerItem.addActionListener(new MenuActionListener(GameMode.VS_PLAYER));

        // Add menu items to the "Game" menu
        gameMenu.add(vsBotItem);
        gameMenu.add(botVsBotItem);
        gameMenu.add(vsPlayerItem);

        // Add the "Game" menu to the menu bar
        menuBar.add(gameMenu);

        // Set the menu bar for the frame
        setJMenuBar(menuBar);

        cellButtons = new JButton[BOARD_SIZE][BOARD_SIZE]; // Initialize the cell buttons array

        updateUI();

        setVisible(true);
    }

    private void initializeBoard() {
        board = new char[BOARD_SIZE][BOARD_SIZE];
        for (int i = 0; i < BOARD_SIZE; i++) {
            for (int j = 0; j < BOARD_SIZE; j++) {
                board[i][j] = EMPTY;
            }
        }

        int center = BOARD_SIZE / 2;
        board[center - 1][center - 1] = WHITE;
        board[center][center] = WHITE;
        board[center - 1][center] = BLACK;
        board[center][center - 1] = BLACK;
    }

    private void updateUI() {
        getContentPane().removeAll();

        for (int i = 0; i < BOARD_SIZE; i++) {
            for (int j = 0; j < BOARD_SIZE; j++) {
                JButton button = new JButton(String.valueOf(board[i][j]));
                button.setFont(new Font("Arial", Font.PLAIN, 20));

                if (board[i][j] == EMPTY) {
                    button.addActionListener(new ButtonActionListener(i, j));
                } else {
                    button.setEnabled(false);
                }

                if (isValidMove(i, j, currentPlayer)) {
                    button.setBackground(Color.GREEN);
                } else {
                    button.setBackground(null);
                }

                getContentPane().add(button);
                cellButtons[i][j] = button;
            }
        }

        repaint();
        revalidate();
        printBoardState();
        System.out.println("Player " + currentPlayer + "'s turn.");
        if (!hasAvailableMoves(BLACK) && !hasAvailableMoves(WHITE)) {
            displayResult();
        } else if (currentGameMode == GameMode.VS_BOT && currentPlayer == WHITE) {
            // Human vs. Bot game, check if the human player has no valid moves
            if (!hasAvailableMoves(BLACK)) {
                displayResult();
            }
        }
    }
    private void printBoardState() {
        System.out.println("\nCurrent board state:");
        for (int i = 0; i < BOARD_SIZE; i++) {
            for (int j = 0; j < BOARD_SIZE; j++) {
                System.out.print(board[i][j] + " ");
            }
            System.out.println();
        }
    }

    private void switchPlayer() {
        currentPlayer = (currentPlayer == BLACK) ? WHITE : BLACK;
    }

    private boolean isValidMove(int row, int col, char player) {
        if (row < 0 || row >= BOARD_SIZE || col < 0 || col >= BOARD_SIZE || board[row][col] != EMPTY) {
            return false;
        }

        for (int dr = -1; dr <= 1; dr++) {
            for (int dc = -1; dc <= 1; dc++) {
                if (dr == 0 && dc == 0) {
                    continue;
                }

                int r = row + dr;
                int c = col + dc;
                boolean isValidDirection = false;

                while (r >= 0 && r < BOARD_SIZE && c >= 0 && c < BOARD_SIZE && board[r][c] == getOpponent(player)) {
                    r += dr;
                    c += dc;
                    isValidDirection = true;
                }

                if (isValidDirection && r >= 0 && r < BOARD_SIZE && c >= 0 && c < BOARD_SIZE && board[r][c] == player) {
                    return true;
                }
            }
        }

        return false;
    }

    private void placePiece(int row, int col, char player) {
        board[row][col] = player;

        for (int dr = -1; dr <= 1; dr++) {
            for (int dc = -1; dc <= 1; dc++) {
                if (dr == 0 && dc == 0) {
                    continue;
                }

                int r = row + dr;
                int c = col + dc;
                boolean isValidDirection = false;
                boolean hasOpponentPiece = false;

                while (r >= 0 && r < BOARD_SIZE && c >= 0 && c < BOARD_SIZE && board[r][c] == getOpponent(player)) {
                    r += dr;
                    c += dc;
                    isValidDirection = true;
                    hasOpponentPiece = true;
                }

                if (isValidDirection && r >= 0 && r < BOARD_SIZE && c >= 0 && c < BOARD_SIZE && board[r][c] == player && hasOpponentPiece) {
                    while (r != row || c != col) {
                        r -= dr;
                        c -= dc;
                        board[r][c] = player;
                    }
                }
            }
        }
    }

    private char getOpponent(char player) {
        return (player == BLACK) ? WHITE : BLACK;
    }

    private boolean hasAvailableMoves(char player) {
        for (int i = 0; i < BOARD_SIZE; i++) {
            for (int j = 0; j < BOARD_SIZE; j++) {
                if (isValidMove(i, j, player)) {
                    return true;
                }
            }
        }
        return false;
    }

    private int countPieces(char player) {
        int count = 0;
        for (int i = 0; i < BOARD_SIZE; i++) {
            for (int j = 0; j < BOARD_SIZE; j++) {
                if (board[i][j] == player) {
                    count++;
                }
            }
        }
        return count;
    }

    private void displayResult() {
        int blackCount = countPieces(BLACK);
        int whiteCount = countPieces(WHITE);

        System.out.println("Number of Black pieces: " + blackCount);
        System.out.println("Number of White pieces: " + whiteCount);

        if (blackCount > whiteCount) {
            System.out.println("Winner: Black");
            if (currentGameMode == GameMode.VS_BOT && currentPlayer == BLACK) {
                System.out.println("Congratulations! You won!");
            }
        } else if (whiteCount > blackCount) {
            System.out.println("Winner: White");
            if (currentGameMode == GameMode.VS_BOT && currentPlayer == WHITE) {
                System.out.println("Sorry, you lost!");
            }
        } else {
            System.out.println("It's a Tie");
        }
    }


    private class ButtonActionListener implements ActionListener {
        private int row;
        private int col;

        public ButtonActionListener(int row, int col) {
            this.row = row;
            this.col = col;
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            if (isValidMove(row, col, currentPlayer)) {
                placePiece(row, col, currentPlayer);
                switchPlayer();
                updateUI();

                if (!hasAvailableMoves(currentPlayer)) {
                    switchPlayer();
                    if (!hasAvailableMoves(currentPlayer)) {
                        displayResult();
                    }
                }

                // AI Move
                if (currentGameMode == GameMode.VS_BOT && currentPlayer == WHITE) {
                    makeAIMove();
                } else if (currentGameMode == GameMode.BOT_VS_BOT && currentPlayer == BLACK) {
                    makeAIMove();
                }

                // Delay before updating UI for automatic AI moves
                Timer timer = new Timer(10, new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        updateUI();
                    }
                });
                timer.setRepeats(false);
                timer.start();
            }
        }
    }

    private void makeAIMove() {
        Player currentPlayerAI = (currentPlayer == BLACK) ? player1 : player2;
        int[] move = currentPlayerAI.makeMove();
        int row = move[0];
        int col = move[1];
        placePiece(row, col, currentPlayer);
        switchPlayer();
    }

    private int[] minimax(int depth, char player) {
        char opponent = getOpponent(player);
        int[] bestMove = {-1, -1, (player == WHITE) ? Integer.MIN_VALUE : Integer.MAX_VALUE};

        for (int i = 0; i < BOARD_SIZE; i++) {
            for (int j = 0; j < BOARD_SIZE; j++) {
                if (isValidMove(i, j, player)) {
                    char[][] tempBoard = copyBoard(board);
                    placePiece(i, j, player);

                    int score;

                    if (depth == 0 || !hasAvailableMoves(opponent)) {
                        score = evaluateBoard(player);
                    } else {
                        score = minimax(depth - 1, opponent)[2];
                    }

                    board = copyBoard(tempBoard);

                    if ((player == WHITE && score > bestMove[2]) || (player == BLACK && score < bestMove[2])) {
                        bestMove[0] = i;
                        bestMove[1] = j;
                        bestMove[2] = score;
                    }
                }
            }
        }

        return bestMove;
    }

    private char[][] copyBoard(char[][] board) {
        char[][] copy = new char[BOARD_SIZE][BOARD_SIZE];
        for (int i = 0; i < BOARD_SIZE; i++) {
            for (int j = 0; j < BOARD_SIZE; j++) {
                copy[i][j] = board[i][j];
            }
        }
        return copy;
    }

    private int evaluateBoard(char player) {
        int playerCount = countPieces(player);
        int opponentCount = countPieces(getOpponent(player));
        return playerCount - opponentCount;
    }

    private class MenuActionListener implements ActionListener {
        private GameMode gameMode;

        public MenuActionListener(GameMode gameMode) {
            this.gameMode = gameMode;
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            currentGameMode = gameMode;
            switch (gameMode) {
                case VS_BOT:
                    player1 = new HumanPlayer(BLACK);
                    player2 = new AIPlayer(WHITE);
                    break;
                case BOT_VS_BOT:
                    player1 = new AIPlayer(BLACK);
                    player2 = new AIPlayer(WHITE);
                    break;
                case VS_PLAYER:
                    player1 = new HumanPlayer(BLACK);
                    player2 = new HumanPlayer(WHITE);
                    break;
            }
            currentPlayer = BLACK;
            initializeBoard();
            updateUI();

            if (currentGameMode != GameMode.VS_PLAYER) {
                gameListener = new GameListener();
                automaticGame();
            }
        }
    }

    private void automaticGame() {
        if (currentGameMode == GameMode.VS_BOT) {
            makeAIMove();
            Timer timer = new Timer(10, new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    updateUI();
                    if (!hasAvailableMoves(currentPlayer)) {
                        switchPlayer();
                        if (!hasAvailableMoves(currentPlayer)) {
                            //displayResult();
                        } else {
                            automaticGame();
                        }
                    } else {
                        automaticGame();
                    }
                }
            });
            timer.setRepeats(false);
            timer.start();
        } else if (currentGameMode == GameMode.BOT_VS_BOT) {
            makeAIMove();
            Timer timer = new Timer(10, new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    updateUI();
                    if (!hasAvailableMoves(currentPlayer)) {
                        switchPlayer();
                        if (!hasAvailableMoves(currentPlayer)) {
                            //displayResult();
                        } else {
                            automaticGame();
                        }
                    } else {
                        automaticGame();
                    }
                }
            });
            timer.setRepeats(false);
            timer.start();
        }
    }

    private abstract class Player {
        protected char symbol;

        public Player(char symbol) {
            this.symbol = symbol;
        }

        public abstract int[] makeMove();
    }

    private class HumanPlayer extends Player {
        public HumanPlayer(char symbol) {
            super(symbol);
        }

        @Override
        public int[] makeMove() {
            // Human player makes the move through the UI, need update and create console app
            return new int[]{-1, -1};
        }
    }

    private class AIPlayer extends Player {
        public AIPlayer(char symbol) {
            super(symbol);
        }

        @Override
        public int[] makeMove() {
            // Simple AI implementation using Minimax with depth 3
            int[] move = minimax(3, symbol);
            return new int[]{move[0], move[1]};
        }
    }

    private class GameListener {
        public void onPlayerMove(Player player) {
            if (player instanceof HumanPlayer) {
            } else if (player instanceof AIPlayer) {
                makeAIMove();
                updateUI();

                if (!hasAvailableMoves(currentPlayer)) {
                    switchPlayer();
                    if (!hasAvailableMoves(currentPlayer)) {
                        //displayResult();
                    } else {
                        automaticGame();
                    }
                } else {
                    automaticGame();
                }
            }
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            new ReversiGameAI();
        });
    }
}

enum GameMode {
    VS_BOT,
    BOT_VS_BOT,
    VS_PLAYER
}

//class ReversiGameAITest {
//
//    @Test
//    void testInitialBoardSetup() {
//        ReversiGameAI game = new ReversiGameAI();
//
//        char[][] expectedBoard = new char[][]{
//                {' ', ' ', ' ', ' ', ' ', ' ', ' ', ' '},
//                {' ', ' ', ' ', ' ', ' ', ' ', ' ', ' '},
//                {' ', ' ', ' ', ' ', ' ', ' ', ' ', ' '},
//                {' ', ' ', ' ', '-', '+', ' ', ' ', ' '},
//                {' ', ' ', ' ', '+', '-', ' ', ' ', ' '},
//                {' ', ' ', ' ', ' ', ' ', ' ', ' ', ' '},
//                {' ', ' ', ' ', ' ', ' ', ' ', ' ', ' '},
//                {' ', ' ', ' ', ' ', ' ', ' ', ' ', ' '},
//        };
//
//        assertArrayEquals(expectedBoard, game.getBoard());
//    }
//
//    @Test
//    void testPlayerVsBot() {
//        ReversiGameAI game = new ReversiGameAI();
//        game.setGameMode(GameMode.VS_BOT);
//        game.getCurrentPlayer().makeMove(); // Ход игрока
//        game.updateUI();
//
//        // Проверка на победу игрока
//        assertTrue(game.isGameOver());
//        assertEquals(BLACK, game.getWinner());
//        assertTrue(outputStream.toString().contains("Игрок + выиграл!"));
//
//        // Проверка на правильность отображаемого счета
//        assertEquals(60, game.countPieces(BLACK));
//        assertEquals(4, game.countPieces(WHITE));
//        assertTrue(outputStream.toString().contains("Количество черных фишек: 60"));
//        assertTrue(outputStream.toString().contains("Количество белых фишек: 4"));
//    }
//
//    @Test
//    void testBotVsBot() {
//        ReversiGameAI game = new ReversiGameAI();
//        game.setGameMode(GameMode.BOT_VS_BOT);
//        game.runGame();
//
//        // Проверка на победу бота
//        assertTrue(game.isGameOver());
//        assertEquals(BLACK, game.getWinner());
//        assertTrue(outputStream.toString().contains("Игрок + выиграл!"));
//
//        // Проверка на правильность отображаемого счета
//        assertEquals(64, game.countPieces(BLACK));
//        assertEquals(0, game.countPieces(WHITE));
//        assertTrue(outputStream.toString().contains("Количество черных фишек: 64"));
//        assertTrue(outputStream.toString().contains("Количество белых фишек: 0"));
//    }
//
//
//}
//
