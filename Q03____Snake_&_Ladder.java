1. Functional Requirements

* 10x10 board (100 cells).
* Support multiple players using Deque for turn management.
* Configurable number of snakes and ladders (randomly placed).
* Support multiple dice (configurable dice count).
* Roll dice and move player token.
* Apply Snake (down) or Ladder (up) if player lands on start position.
* Player must reach exactly the last cell to win (overshooting not allowed).
* Show current and new position after every move.
* Declare winner when someone reaches the final cell.

------------------------------------------------------------------------------------------------------------------------------------------------------------------

2. Non-Functional Requirements

* Simple and clean OOP design.
* In-memory, fast execution.
* Easy to configure board size, snakes, and ladders.
* Readable code with clear separation of concerns.
* Single-threaded implementation.

------------------------------------------------------------------------------------------------------------------------------------------------------------------

3. System Happy Flow

* Game constructor calls initializeGame().
* Board is created with given size, snakes, and ladders (randomly generated).
* Players are added to Deque.
* startGame() loop runs until there is a winner.
* findPlayerTurn() gives turn to next player.
* Player rolls dice → new position calculated.
* jumpCheck() applies snake or ladder if present.
* Update player position.
* Check winning condition.
* Switch turn using Deque.

------------------------------------------------------------------------------------------------------------------------------------------------------------------

4. Edge Cases + Handling

* Overshooting final cell: Player stays at current position.
* Landing on snake head: Move to tail.
* Landing on ladder bottom: Move to top.
* Snake/Ladder at final position: Handled by jump logic.
* Random generation collision: Avoided by while loop condition.
* Multiple players: Turn managed by Deque (round-robin).

------------------------------------------------------------------------------------------------------------------------------------------------------------------

5. UML Diagram:   https://drive.google.com/file/d/1mUGJYzvufo20fl3PfoUqN-itjOrLcG1l/view?usp=sharing

@startuml
skinparam classAttributeIconSize 0

class Player {
  - String id
  - int currentPosition     // current position of player on board (0-based)
  + Player(String id, int currentPosition)
  + String getId()
  + int getCurrentPosition()
  + void setCurrentPosition(int pos)
  '' Represents a player and tracks their token position
}

class Jump {
  - int start     // starting cell of snake or ladder
  - int end       // ending cell of snake or ladder
  + Jump()
  '' Represents a teleport (Snake or Ladder)
}

class Cell {
  - Jump jump     // null if no snake/ladder, else contains jump info
  + Cell()
  + void setJump(Jump jump)
  + Jump getJump()
  '' Represents one cell on the board
}

class Board {
  - Cell[][] cells     // 2D array representing the board
  + Board(int boardSize, int numberOfSnakes, int numberOfLadders)
  - void initializeCells(int boardSize)      // creates Cell objects for all positions
  - void addSnakesLadders(Cell[][] cells, int snakes, int ladders)  // randomly places snakes and ladders
  + Cell getCell(int playerPosition)         // converts linear position to 2D cell
  '' Manages board structure and snakes/ladders
}

class Dice {
  - int diceCount     // number of dice to roll
  - int min = 1
  - int max = 6
  + Dice(int diceCount)
  + int rollDice()    // returns sum of all dice rolls
  '' Simulates rolling one or more dice
}

class Game {
  - Board board
  - Dice dice
  - Deque<Player> playersList     // maintains turn order
  - Player winner
  + Game()
  - void initializeGame()                // sets up board, dice and players
  - void addPlayers()                    // adds default players
  + void startGame()                     // main game loop
  - Player findPlayerTurn()              // returns current player and rotates turn
  - int jumpCheck(int playerNewPosition) // applies snake or ladder if present
  '' Main controller class that runs the game
}

Game "1" --> "1" Board
Game "1" --> "1" Dice
Game "1" --> "1..*" Player : uses Deque
Board "1" *-- "100" Cell
Cell --> Jump
@enduml

------------------------------------------------------------------------------------------------------------------------------------------------------------------

UML Explanation:

* Board uses 2D Cell[][] as per your code.
* Cell holds Jump for snakes/ladders.
* Jump is used for both snakes and ladders.
* Game uses Deque for turn management.
* All classes and important methods/variables have detailed comments.

------------------------------------------------------------------------------------------------------------------------------------------------------------------

>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>
6. LLD Code (Java)

import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

// Jump.java
public class Jump {
    int start;
    int end;

    public Jump() {}

    // getters and setters
    public int getStart() { return start; }
    public int getEnd() { return end; }
    public void setStart(int start) { this.start = start; }
    public void setEnd(int end) { this.end = end; }
}

// Cell.java
public class Cell {
    Jump jump;

    public Cell() {
        this.jump = null;
    }

    // getters and setters
    public Jump getJump() { return jump; }
    public void setJump(Jump jump) { this.jump = jump; }
}

// Player.java
public class Player {
    String id;
    int currentPosition;

    public Player(String id, int currentPosition) {
        this.id = id;
        this.currentPosition = currentPosition;
    }

    // getters and setters
    public String getId() { return id; }
    public int getCurrentPosition() { return currentPosition; }
    public void setCurrentPosition(int currentPosition) { this.currentPosition = currentPosition; }
}

// Dice.java
public class Dice {
    int diceCount;
    int min = 1;
    int max = 6;

    public Dice(int diceCount) {
        this.diceCount = diceCount;
    }

    public int rollDice() {
        int totalSum = 0;
        int diceUsed = 0;

        while (diceUsed < diceCount) {
            totalSum += ThreadLocalRandom.current().nextInt(min, max + 1);
            diceUsed++;
        }
        return totalSum;
    }
}

// Board.java
public class Board {
    Cell[][] cells;

    public Board(int boardSize, int numberOfSnakes, int numberOfLadders) {
        initializeCells(boardSize);
        addSnakesLadders(cells, numberOfSnakes, numberOfLadders);
    }

    private void initializeCells(int boardSize) {
        cells = new Cell[boardSize][boardSize];
        for (int i = 0; i < boardSize; i++) {
            for (int j = 0; j < boardSize; j++) {
                cells[i][j] = new Cell();
            }
        }
    }

    private void addSnakesLadders(Cell[][] cells, int numberOfSnakes, int numberOfLadders) {
        int totalCells = cells.length * cells.length;

        // Add Snakes
        while (numberOfSnakes > 0) {
            int snakeHead = ThreadLocalRandom.current().nextInt(1, totalCells - 1);
            int snakeTail = ThreadLocalRandom.current().nextInt(1, totalCells - 1);
            if (snakeTail >= snakeHead) continue;

            Jump snakeObj = new Jump();
            snakeObj.setStart(snakeHead);
            snakeObj.setEnd(snakeTail);

            Cell cell = getCell(snakeHead);
            cell.setJump(snakeObj);
            numberOfSnakes--;
        }

        // Add Ladders
        while (numberOfLadders > 0) {
            int ladderStart = ThreadLocalRandom.current().nextInt(1, totalCells - 1);
            int ladderEnd = ThreadLocalRandom.current().nextInt(1, totalCells - 1);
            if (ladderStart >= ladderEnd) continue;

            Jump ladderObj = new Jump();
            ladderObj.setStart(ladderStart);
            ladderObj.setEnd(ladderEnd);

            Cell cell = getCell(ladderStart);
            cell.setJump(ladderObj);
            numberOfLadders--;
        }
    }

    Cell getCell(int playerPosition) {
        int boardRow = playerPosition / cells.length;
        int boardColumn = playerPosition % cells.length;
        return cells[boardRow][boardColumn];
    }
}

// Game.java
public class Game {
    Board board;
    Dice dice;
    Deque<Player> playersList = new LinkedList<>();
    Player winner;

    public Game() {
        initializeGame();
    }

    private void initializeGame() {
        board = new Board(10, 5, 4);   // 10x10 board, 5 snakes, 4 ladders
        dice = new Dice(1);
        winner = null;
        addPlayers();
    }

    private void addPlayers() {
        Player player1 = new Player("Player-1", 0);
        Player player2 = new Player("Player-2", 0);
        playersList.add(player1);
        playersList.add(player2);
    }

    public void startGame() {
        while (winner == null) {
            Player playerTurn = findPlayerTurn();
            System.out.println("Player turn: " + playerTurn.getId() 
                             + " current position is: " + playerTurn.getCurrentPosition());

            int diceNumbers = dice.rollDice();
            System.out.println("Rolled: " + diceNumbers);

            int playerNewPosition = playerTurn.getCurrentPosition() + diceNumbers;
            playerNewPosition = jumpCheck(playerNewPosition);
            playerTurn.setCurrentPosition(playerNewPosition);

            System.out.println("Player turn: " + playerTurn.getId() 
                             + " new Position is: " + playerNewPosition);

            if (playerNewPosition >= board.cells.length * board.cells.length - 1) {
                winner = playerTurn;
            }
        }
        System.out.println("\n===> The Winner is: " + winner.getId());
    }

    private Player findPlayerTurn() {
        Player playerTurns = playersList.removeFirst();
        playersList.addLast(playerTurns);
        return playerTurns;
    }

    private int jumpCheck(int playerNewPosition) {
        int totalCells = board.cells.length * board.cells.length - 1;
        if (playerNewPosition > totalCells) {
            return playerNewPosition;   // will be handled in startGame
        }

        Cell cell = board.getCell(playerNewPosition);
        if (cell.getJump() != null && cell.getJump().getStart() == playerNewPosition) {
            String jumpBy = (cell.getJump().getStart() < cell.getJump().getEnd()) ? "Ladder" : "Snake";
            System.out.println("[+] Jump done by: " + jumpBy + " -> " + cell.getJump().getEnd());
            return cell.getJump().getEnd();
        }
        return playerNewPosition;
    }
}

// SnakeNLadderDemo.java
public class SnakeNLadderDemo {
    public static void main(String[] args) {
        Game obj = new Game();
        obj.startGame();
    }
}
