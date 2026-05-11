1. Functional Requirements

* Standard 8x8 chess board with all 6 types of pieces.
* Two players (White and Black) taking alternate turns.
* Support selecting source and destination positions for moves.
* Piece-specific movement validation.
* Basic Check, Checkmate, and Stalemate detection.
* Display board after each move.
* Proper game flow and result declaration.

------------------------------------------------------------------------------------------------------------------------------------------------------------------

2. Non-Functional Requirements

* Clean OOP design using inheritance for pieces.
* Highly readable and maintainable.
* Easy to extend (castling, en-passant, promotion etc.).
* In-memory, single-threaded implementation.

------------------------------------------------------------------------------------------------------------------------------------------------------------------

3. System Happy Flow

* initializeGame() sets up Board and places all pieces.
* startGame() runs the main game loop.
* Current player inputs source and destination.
* makeMove() validates and executes the move.
* Game checks for Check / Checkmate / Stalemate.
* Turn switches until game ends.

------------------------------------------------------------------------------------------------------------------------------------------------------------------

4. Edge Cases + Handling

* Illegal move according to piece rules → rejected.
* Attempt to move opponent’s piece or into check → rejected.
* King already in check → must resolve it.
* No legal moves available → Checkmate or Stalemate.
* Pawn reaching last rank → auto promote to Queen (can be extended).

------------------------------------------------------------------------------------------------------------------------------------------------------------------

5. UML Diagram:    https://drive.google.com/file/d/1lnx-KtpO98Uf1eTeqbnJ5YcycHmbToyK/view?usp=sharing

@startuml
skinparam classAttributeIconSize 0

enum PieceType {
  KING, QUEEN, ROOK, BISHOP, KNIGHT, PAWN
  '' Enum to identify type of chess piece
}

enum PieceColour {
  WHITE, BLACK
  '' Represents color of piece and player
}

enum GameStatus {
  ONGOING, CHECK, CHECKMATE, STALEMATE
  '' Current state of the game
}

class Position {
  - int row        // row coordinate (0-7) on the chessboard
  - int col        // column coordinate (0-7) on the chessboard
  + Position(int row, int col)
  '' Represents a square location on the 8x8 board
}

class Cell {
  - Position position   // location of this cell
  - Piece piece         // piece currently occupying this cell (null if empty)
  + Cell(Position position)
  + void setPiece(Piece piece)     // places a piece on this cell
  + Piece getPiece()               // returns piece on this cell
  '' Represents one square on the chess board
}

abstract class Piece {
  - PieceType pieceType     // type of this piece (KING, QUEEN etc)
  - PieceColour colour      // color of this piece (WHITE or BLACK)
  - Position position       // current position of this piece
  + Piece(PieceType type, PieceColour colour, Position pos)
  + abstract boolean canMove(Board board, Position from, Position to)  // checks if this piece can legally move to target
  + PieceType getPieceType()
  + PieceColour getColour()
  '' Base class for all chess pieces with common attributes
}

class King extends Piece {
  + King(PieceColour colour, Position pos)
  + boolean canMove(Board board, Position from, Position to)  // King moves one square in any direction
}

class Queen extends Piece {
  + Queen(PieceColour colour, Position pos)
  + boolean canMove(Board board, Position from, Position to)  // Queen moves any number of squares vertically, horizontally or diagonally
}

class Rook extends Piece {
  + Rook(PieceColour colour, Position pos)
  + boolean canMove(Board board, Position from, Position to)  // Rook moves any number of squares vertically or horizontally
}

class Bishop extends Piece {
  + Bishop(PieceColour colour, Position pos)
  + boolean canMove(Board board, Position from, Position to)  // Bishop moves any number of squares diagonally
}

class Knight extends Piece {
  + Knight(PieceColour colour, Position pos)
  + boolean canMove(Board board, Position from, Position to)  // Knight moves in L-shape (2 in one direction + 1 perpendicular)
}

class Pawn extends Piece {
  + Pawn(PieceColour colour, Position pos)
  + boolean canMove(Board board, Position from, Position to)  // Pawn moves forward, captures diagonally
}

class Board {
  - Cell[][] cells     // 8x8 grid of cells representing the chessboard
  + Board()
  + void initializeBoard()          // places all pieces in starting positions
  + Cell getCell(Position pos)      // returns cell at given position
  + void movePiece(Position from, Position to)  // moves piece from one cell to another
  + void printBoard()               // displays current board state
  '' Manages the complete state of the chess board
}

class Player {
  - String name
  - PieceColour colour
  + Player(String name, PieceColour colour)
  '' Represents one of the two players
}

class Move {
  - Position from     // starting position of the move
  - Position to       // destination position of the move
  + Move(Position from, Position to)
  '' Represents a single chess move
}

class Game {
  - Board board
  - Player whitePlayer
  - Player blackPlayer
  - Player currentPlayer     // player whose turn it is now
  - GameStatus status
  + void initializeGame()
  + GameStatus startGame()                    // main game loop
  + boolean makeMove(Position from, Position to)   // validates and executes a move
  - boolean isKingInCheck(PieceColour colour)     // checks if king of given color is under attack
  - boolean hasAnyLegalMove(PieceColour colour)   // checks if player has any valid move left
  - void switchPlayer()                           // changes turn to other player
  '' Main controller class that manages game flow and rules
}

Piece <|-- King
Piece <|-- Queen
Piece <|-- Rook
Piece <|-- Bishop
Piece <|-- Knight
Piece <|-- Pawn

Board "1" *-- "64" Cell
Cell --> Position
Cell --> Piece
Game --> Board
Game --> "2" Player
Game --> Move
Piece --> PieceType
Piece --> PieceColour
@enduml

------------------------------------------------------------------------------------------------------------------------------------------------------------------

6. LLD Code (Java)

// Enums
public enum PieceType {
    KING, QUEEN, ROOK, BISHOP, KNIGHT, PAWN
}

public enum PieceColour {
    WHITE, BLACK
}

public enum GameStatus {
    ONGOING, CHECK, CHECKMATE, STALEMATE
}

// Position.java
public class Position {
    private final int row;
    private final int col;

    public Position(int row, int col) {
        this.row = row;
        this.col = col;
    }

    public int getRow() { return row; }
    public int getCol() { return col; }
}

// Cell.java
public class Cell {
    private final Position position;
    private Piece piece;

    public Cell(Position position) {
        this.position = position;
    }

    public void setPiece(Piece piece) {
        this.piece = piece;
    }

    public Piece getPiece() {
        return piece;
    }

    public Position getPosition() {
        return position;
    }
}

// Piece.java (Abstract)
public abstract class Piece {
    protected final PieceType pieceType;
    protected final PieceColour colour;
    protected Position position;

    public Piece(PieceType pieceType, PieceColour colour, Position position) {
        this.pieceType = pieceType;
        this.colour = colour;
        this.position = position;
    }

    public abstract boolean canMove(Board board, Position from, Position to);

    public PieceType getPieceType() { return pieceType; }
    public PieceColour getColour() { return colour; }
    public Position getPosition() { return position; }
    public void setPosition(Position position) { this.position = position; }
}

// King.java, Queen.java, Rook.java, Bishop.java, Knight.java, Pawn.java
// (Same as previous response - canMove() methods remain the same)

// Board.java, Player.java, Move.java, Game.java similar to previous version with minor updates to match UML.

public class Board {
    private final Cell[][] cells = new Cell[8][8];

    public Board() {
        for (int i = 0; i < 8; i++) {
            for (int j = 0; j < 8; j++) {
                cells[i][j] = new Cell(new Position(i, j));
            }
        }
        initializeBoard();
    }

    public void initializeBoard() {
        // Place all starting pieces here...
        cells[0][4].setPiece(new King(PieceColour.BLACK, new Position(0,4)));
        cells[7][4].setPiece(new King(PieceColour.WHITE, new Position(7,4)));
        // ... other pieces
    }

    public Cell getCell(Position pos) {
        if (pos.getRow() < 0 || pos.getRow() > 7 || pos.getCol() < 0 || pos.getCol() > 7) {
            return null;
        }
        return cells[pos.getRow()][pos.getCol()];
    }

    public void movePiece(Position from, Position to) {
        Cell fromCell = getCell(from);
        Cell toCell = getCell(to);
        if (fromCell != null && toCell != null) {
            Piece piece = fromCell.getPiece();
            toCell.setPiece(piece);
            fromCell.setPiece(null);
            if (piece != null) piece.setPosition(to);
        }
    }

    public void printBoard() {
        // board printing logic
    }
}

// Game.java (Core)
public class Game {
    private Board board;
    private Player whitePlayer;
    private Player blackPlayer;
    private Player currentPlayer;
    private GameStatus status;

    public void initializeGame() {
        board = new Board();
        whitePlayer = new Player("White", PieceColour.WHITE);
        blackPlayer = new Player("Black", PieceColour.BLACK);
        currentPlayer = whitePlayer;
        status = GameStatus.ONGOING;
    }

    // startGame(), makeMove(), switchPlayer() etc. same as previous version
}

