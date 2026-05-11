1. Functional Requirements

* Support two players (Player1 with X and Player2 with O).
* 3x3 Tic Tac Toe board.
* Players take alternate turns.
* Allow players to input row and column to place their piece.
* Validate moves (cell must be empty and within bounds).
* Display the board after every move.
* Automatically detect winner after a move (row, column, both diagonals).
* Detect Draw when board is full and no one has won.
* Maintain turn order using a Deque (queue).
* Return game result (WIN or DRAW) at the end.

------------------------------------------------------------------------------------------------------------------------------------------------------------------

2. Non-Functional Requirements

* Simple console-based implementation.
* Low latency (all in-memory operations).
* Readable and maintainable code using OOP principles.
* Extensible design (easy to support N x N board or more piece types).
* Single-threaded (no concurrency required).

------------------------------------------------------------------------------------------------------------------------------------------------------------------

3. System Happy Flow

* initializeGame() creates two Player objects with their PlayingPiece (X and O) and adds them to Deque.
* Creates a 3x3 Board.
* startGame() begins the loop.
* Current player is taken from front of Deque.
* Board is printed and free cells are fetched.
* Player enters row, column.
* Board.addPiece() places the piece if cell is empty.
* checkForWinner() verifies if this move resulted in a win.
* If win → return WIN.
* Otherwise, move player to end of Deque and continue.
* If no free cells left → return DRAW.


------------------------------------------------------------------------------------------------------------------------------------------------------------------


4. Edge Cases + Handling

* Invalid move (cell already occupied): addPiece() returns false → print error, put player back at front of queue, ask again.
* Wrong input format / out of bounds: Code currently assumes correct input (can crash on bad input). In interview, mention adding try-catch + validation.
* Winning move on last cell: Handled because win check happens before draw check.
* Game ends in Draw: When freeSpaces.isEmpty() → return DRAW.
* Win by row/column/diagonal: checkForWinner() checks only affected lines for efficiency.


------------------------------------------------------------------------------------------------------------------------------------------------------------------

5. UML Diagram

@startuml
skinparam classAttributeIconSize 0

enum PieceType {
  X
  O
}

enum GameStatus {
  WIN
  DRAW
}

class Player {
  - String name
  - PlayingPiece playingPiece
  + Player(String name, PlayingPiece piece)
  + String getName()
  + PlayingPiece getPlayingPiece()
}

abstract class PlayingPiece {
  - PieceType pieceType
  + PlayingPiece(PieceType type)
}

class PlayingPieceX extends PlayingPiece {
  + PlayingPieceX()
}

class PlayingPieceO extends PlayingPiece {
  + PlayingPieceO()
}

class Board {
  - int size
  - PlayingPiece[][] board
  + Board(int size)
  + boolean addPiece(int row, int col, PlayingPiece piece)
  + List<Pair<Integer,Integer>> getFreeCells()
  + void printBoard()
}

class TicTacToeGame {
  - Deque<Player> players
  - Board gameBoard
  - Player winner
  + void initializeGame()
  + GameStatus startGame()
  + boolean checkForWinner(int row, int col, PieceType type)
}

Player "2" --> "1" PlayingPiece
PlayingPiece <|-- PlayingPieceX
PlayingPiece <|-- PlayingPieceO
TicTacToeGame --> "2" Player
TicTacToeGame --> "1" Board
TicTacToeGame --> GameStatus
Board --> PlayingPiece

@enduml

------------------------------------------------------------------------------------------------------------------------------------------------------------------

6. UML Explanation:

* PlayingPiece + inheritance allows easy addition of new piece types later.
* Player holds name and piece.
* Board manages grid state and basic operations.
* TicTacToeGame is the main orchestrator (turn management, win checking, game loop).
* Clean and matches your code structure.

------------------------------------------------------------------------------------------------------------------------------------------------------------------

7. Code:

// File: PieceType.java 
public enum PieceType {
    X,
    O
}

// File: GameStatus.java 
public enum GameStatus {
    DRAW,
    WIN
}

//------------------------------------------------------------------------------------------------

// ******  GAME CLASS ****** 
// File: TicTacToeGame.java 
public class TicTacToeGame {
    Deque<Player> players;
    Board gameBoard;
    Player winner;

    public void initializeGame() {

        // Creating 2 Players
        players = new LinkedList<>();
        PlayingPieceX crossPiece = new PlayingPieceX();
        Player player1 = new Player("Player1", crossPiece);

        PlayingPieceO noughtsPiece = new PlayingPieceO();
        Player player2 = new Player("Player2", noughtsPiece);

        players.add(player1);
        players.add(player2);

        // Initialize Board of size 3
        gameBoard = new Board(3);
    }

    public GameStatus startGame() {

        boolean noWinner = true;
        while (noWinner) {

            // Remove the player whose turn is and also put the player in the list back
            Player currentPlayer = players.removeFirst();

            // Get the free space from the board
            gameBoard.printBoard();
            List<Pair<Integer, Integer>> freeSpaces = gameBoard.getFreeCells();
            if (freeSpaces.isEmpty()) {
                noWinner = false;
                continue;
            }

            // Read the user input
            System.out.print("Player: " + currentPlayer.name + " - Please enter [row, column]: ");
            Scanner inputScanner = new Scanner(System.in);
            String s = inputScanner.nextLine();
            String[] values = s.split(",");
            int inputRow = Integer.valueOf(values[0]);
            int inputColumn = Integer.valueOf(values[1]);


            // Place the piece in the board
            boolean validMove = gameBoard.addPiece(inputRow, inputColumn, currentPlayer.playingPiece);
            if (!validMove) {
                // Invalid Move: Player can not insert the piece into this cell, player has to choose another cell
                System.out.println("Incorrect position chosen, try again!");
                players.addFirst(currentPlayer); // Add the player back to the queue(in the front)
                continue;
            }
            players.addLast(currentPlayer); // Add the player to the end of the queue

            // Check if the valid move is a winning move or not
            boolean isWinner = checkForWinner(inputRow, inputColumn, currentPlayer.playingPiece.pieceType);
            if (isWinner) {
                gameBoard.printBoard();
                winner = currentPlayer;
                return GameStatus.WIN;
            }
        }

        return GameStatus.DRAW;
    }

    public boolean checkForWinner(int row, int column, PieceType pieceType) {

        boolean rowMatch = true;
        boolean columnMatch = true;
        boolean diagonalMatch = true;
        boolean antiDiagonalMatch = true;

        // Check Row
        for (int i = 0; i < gameBoard.size; i++) {
            if (gameBoard.board[row][i] == null || gameBoard.board[row][i].pieceType != pieceType) {
                rowMatch = false;
                break;
            }
        }

        // Check Column
        for (int i = 0; i < gameBoard.size; i++) {
            if (gameBoard.board[i][column] == null || gameBoard.board[i][column].pieceType != pieceType) {
                columnMatch = false;
                break;
            }
        }

        // Check Diagonally
        for (int i = 0, j = 0; i < gameBoard.size; i++, j++) {
            if (gameBoard.board[i][j] == null || gameBoard.board[i][j].pieceType != pieceType) {
                diagonalMatch = false;
                break;
            }
        }

        // Check Anti-Diagonally
        for (int i = 0, j = gameBoard.size - 1; i < gameBoard.size; i++, j--) {
            if (gameBoard.board[i][j] == null || gameBoard.board[i][j].pieceType != pieceType) {
                antiDiagonalMatch = false;
                break;
            }
        }

        return rowMatch || columnMatch || diagonalMatch || antiDiagonalMatch;
    }
}

//------------------------------------------------------------------------------------------------
// ******  PLAYER CLASS ******
// File: Player.java 
public class Player {

    public String name;
    public PlayingPiece playingPiece;

    public Player(String name, PlayingPiece playingPiece) {
        this.name = name;
        this.playingPiece = playingPiece;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public PlayingPiece getPlayingPiece() {
        return playingPiece;
    }

    public void setPlayingPiece(PlayingPiece playingPiece) {
        this.playingPiece = playingPiece;
    }
}

//------------------------------------------------------------------------------------------------


// ******  BOARD CLASS ******
// File: Board.java 
public class Board {

    public int size;
    public PlayingPiece[][] board;

    public Board(int size) {
        this.size = size;
        board = new PlayingPiece[size][size];
    }

    public boolean addPiece(int row, int column, PlayingPiece playingPiece) {

        if (board[row][column] != null) {
            return false;
        }
        board[row][column] = playingPiece;
        return true;
    }

    public List<Pair<Integer, Integer>> getFreeCells() {
        List<Pair<Integer, Integer>> freeCells = new ArrayList<>();

        for (int i = 0; i < size; i++) {
            for (int j = 0; j < size; j++) {
                if (board[i][j] == null) {
                    Pair<Integer, Integer> rowColumn = new Pair<>(i, j);
                    freeCells.add(rowColumn);
                }
            }
        }

        return freeCells;
    }

    public void printBoard() {

        for (int i = 0; i < size; i++) {
            for (int j = 0; j < size; j++) {
                if (board[i][j] != null) {
                    System.out.print(board[i][j].pieceType.name() + "   ");
                } else {
                    System.out.print("    ");

                }
                System.out.print(" | ");
            }
            System.out.println();

        }
    }
}


//------------------------------------------------------------------------------------------------

// ******  PLAYING PIECE CLASS ****** 
// File: PlayingPiece.java 
// PlayingPiece.java
public abstract class PlayingPiece {
    public final PieceType pieceType;   // final as type doesn't change

    public PlayingPiece(PieceType pieceType) {
        this.pieceType = pieceType;
    }
}

// PlayingPieceX.java
public class PlayingPieceX extends PlayingPiece {
    public PlayingPieceX() {
        super(PieceType.X);
    }
}

// PlayingPieceO.java
public class PlayingPieceO extends PlayingPiece {
    public PlayingPieceO() {
        super(PieceType.O);
    }
}


//------------------------------------------------------------------------------------------------
//------------------------------------------------------------------------------------------------

// ******  PLAYGAME CLASS : main() ****** 
// File: PlayGame.java 
public class PlayGame {
    public static void main(String[] args) {
        System.out.println("\n===>>> TicTacToe Game\n");
        TicTacToeGame game = new TicTacToeGame();
        game.initializeGame();
        GameStatus status = game.startGame();
        System.out.print("\n===>>> GAME OVER: ");
        switch (status) {
            case WIN:
                System.out.print(game.winner.name + " won the game");
                break;
            case DRAW:
                System.out.print(" Its a Draw!");
                break;
            default:
                System.out.print(" Game Ends");
                break;
        }

    }
}
