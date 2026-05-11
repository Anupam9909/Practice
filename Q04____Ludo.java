1. Functional Requirements

* Support 4 players (Red, Green, Blue, Yellow).
* Each player has 4 tokens/pieces.
* Standard dice (1-6). Rolling 6 gives an extra turn.
* Tokens start in the yard (home base).
* Need a 6 to bring a token out onto the board.
* Move token according to dice value.
* Capture opponent’s token by landing on the same cell (send back to yard).
* Safe zones (stars) where pieces cannot be captured.
* Exact roll needed to enter Home column.
* Win when all 4 tokens of a player reach Home.
* Show current board state and player positions.

------------------------------------------------------------------------------------------------------------------------------------------------------------------

2. Non-Functional Requirements

* Clean OOP design with good separation of concerns.
* Easy to understand and explain in interview.
* Extensible (can add more rules like team play later).
* In-memory, single-threaded implementation.
* Readable code (similar to your Snake & Ladder style).

------------------------------------------------------------------------------------------------------------------------------------------------------------------

3. System Happy Flow

* initializeGame() creates 4 players with 4 tokens each and sets up the board.
* startGame() runs the main loop.
* Current player rolls dice.
* Choose a token to move (if multiple options).
* Validate and execute move.
* Check for capture.
* If dice == 6, give extra turn.
* Check if any player has all tokens home → declare winner.

------------------------------------------------------------------------------------------------------------------------------------------------------------------

4. Edge Cases + Handling

* Rolling 6 → extra turn + option to bring new token out.
* No valid move → turn passes.
* Landing on own token → blocked (invalid move).
* Landing on opponent → capture (send to yard).
* Exact roll needed for home entry.
* Safe cells (stars) → cannot be captured.
* All tokens in yard → must roll 6 to start.

------------------------------------------------------------------------------------------------------------------------------------------------------------------

5. UML Diagram:   https://drive.google.com/file/d/13MV3in_kuWsDiCDJapi9wQ8AOZjUJMyY/view?usp=sharing

@startuml
skinparam classAttributeIconSize 0

enum Colour {
  RED, GREEN, BLUE, YELLOW
  '' Player and token color
}

enum GameStatus {
  IN_PROGRESS, FINISHED
  '' Current game state
}

class Position {
  - int cellNumber     // 0 to 51 (main track)
  - int homePosition   // position in home column
  - boolean isInYard
  - boolean isInHome
  + Position()
  '' Represents location of a token
}

class Token {
  - String id
  - Colour colour
  - Position position
  + Token(Colour colour, String id)
  + void move(int steps)
  + boolean isHome()
  '' One of the 4 pieces per player
}

class Player {
  - Colour colour
  - String name
  - List<Token> tokens
  + Player(Colour colour, String name)
  + boolean hasAllTokensHome()
  '' Represents one player with 4 tokens
}

class Dice {
  + int roll()   // returns 1-6
  '' Simulates dice roll
}

class Board {
  - Map<Integer, List<Token>> cellToTokens   // main track cells
  + boolean isSafeCell(int cell)
  + void moveToken(Token token, int steps)
  + void captureToken(Token token)
  '' Manages board positions and captures
}

class Game {
  - List<Player> players
  - Board board
  - Dice dice
  - int currentPlayerIndex
  - GameStatus status
  - Player winner
  + void initializeGame()
  + void startGame()
  + void playTurn(Player player)
  - boolean makeMove(Player player, Token token, int diceValue)
  - void switchPlayer()
  '' Main game controller
}

Player "4" --> Game
Player "1" *-- "4" Token
Token --> Position
Token --> Colour
Game --> Board
Game --> Dice
Board --> Token
@enduml

------------------------------------------------------------------------------------------------------------------------------------------------------------------

UML Explanation:

* Token is the core movable unit.
* Player manages 4 tokens.
* Board handles positioning and captures.
* Game controls flow and turns.
* Clean and scalable design.

------------------------------------------------------------------------------------------------------------------------------------------------------------------

6. LLD Code (Java)

import java.util.*;

// Colour.java
public enum Colour {
    RED, GREEN, BLUE, YELLOW
}

// GameStatus.java
public enum GameStatus {
    IN_PROGRESS, FINISHED
}

// Position.java
public class Position {
    int cellNumber = -1;      // -1 means in yard
    int homePosition = -1;
    boolean isInYard = true;
    boolean isInHome = false;
}

// Token.java
public class Token {
    private final String id;
    private final Colour colour;
    private Position position;

    public Token(Colour colour, String id) {
        this.colour = colour;
        this.id = id;
        this.position = new Position();
    }

    public String getId() { return id; }
    public Colour getColour() { return colour; }
    public Position getPosition() { return position; }

    public void moveToYard() {
        position = new Position();
    }

    public boolean isHome() {
        return position.isInHome;
    }
}

// Player.java
public class Player {
    private final Colour colour;
    private final String name;
    private final List<Token> tokens = new ArrayList<>();

    public Player(Colour colour, String name) {
        this.colour = colour;
        this.name = name;
        for (int i = 1; i <= 4; i++) {
            tokens.add(new Token(colour, name + "-T" + i));
        }
    }

    public List<Token> getTokens() { return tokens; }
    public Colour getColour() { return colour; }
    public String getName() { return name; }

    public boolean hasAllTokensHome() {
        for (Token t : tokens) {
            if (!t.isHome()) return false;
        }
        return true;
    }
}

// Dice.java
public class Dice {
    private final Random random = new Random();

    public int roll() {
        return random.nextInt(6) + 1;
    }
}

// Board.java (Simplified)
public class Board {
    private final Map<Integer, List<Token>> cells = new HashMap<>(); // cellNumber -> tokens

    public boolean isSafeCell(int cell) {
        // Star positions (simplified)
        return cell % 13 == 0 || cell == 8 || cell == 21;
    }

    public void placeToken(Token token, int cellNumber) {
        cells.computeIfAbsent(cellNumber, k -> new ArrayList<>()).add(token);
    }

    public void removeToken(Token token, int cellNumber) {
        List<Token> list = cells.get(cellNumber);
        if (list != null) list.remove(token);
    }

    // More methods for move validation can be added
}

// Game.java
public class Game {
    private final List<Player> players = new ArrayList<>();
    private final Board board = new Board();
    private final Dice dice = new Dice();
    private int currentPlayerIndex = 0;
    private GameStatus status = GameStatus.IN_PROGRESS;
    private Player winner;

    public void initializeGame() {
        players.add(new Player(Colour.RED, "Red"));
        players.add(new Player(Colour.GREEN, "Green"));
        players.add(new Player(Colour.BLUE, "Blue"));
        players.add(new Player(Colour.YELLOW, "Yellow"));
    }

    public void startGame() {
        System.out.println("=== Ludo Game Started ===\n");

        while (status == GameStatus.IN_PROGRESS) {
            Player currentPlayer = players.get(currentPlayerIndex);
            playTurn(currentPlayer);

            if (currentPlayer.hasAllTokensHome()) {
                winner = currentPlayer;
                status = GameStatus.FINISHED;
            }
        }

        System.out.println("\n🎉 Winner is: " + winner.getName() + " 🎉");
    }

    private void playTurn(Player player) {
        boolean extraTurn = false;
        do {
            extraTurn = false;
            System.out.println("\n" + player.getName() + "'s turn");
            int roll = dice.roll();
            System.out.println("Rolled: " + roll);

            // TODO: In real interview, show list of movable tokens and take input
            // For demo, we simulate moving first non-home token
            Token tokenToMove = getMovableToken(player, roll);
            if (tokenToMove != null) {
                makeMove(tokenToMove, roll);
                if (roll == 6) extraTurn = true;
            } else {
                System.out.println("No valid move.");
            }
        } while (extraTurn);
        
        switchPlayer();
    }

    private Token getMovableToken(Player player, int roll) {
        for (Token t : player.getTokens()) {
            if (!t.isHome()) return t;   // simplified
        }
        return null;
    }

    private void makeMove(Token token, int steps) {
        // Simplified move logic - can be expanded
        System.out.println(token.getId() + " moved " + steps + " steps.");
        // Add real movement, capture, home entry logic here
    }

    private void switchPlayer() {
        currentPlayerIndex = (currentPlayerIndex + 1) % 4;
    }
}

// LudoDemo.java
public class LudoDemo {
    public static void main(String[] args) {
        Game game = new Game();
        game.initializeGame();
        game.startGame();
    }
}
