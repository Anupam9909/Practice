1. Functional Requirements

* A city has multiple theatres.
* Each theatre has multiple screens.
* Each screen has multiple shows (movie + start time).
* Each show has multiple seats with status (AVAILABLE / BOOKED).
* User can search movies in a city.
* User can select theatre → screen → show.
* User can view available seats.
* User can book seats and get a Booking confirmation (Ticket).

------------------------------------------------------------------------------------------------------------------------------------------------------------------

2. Non-Functional Requirements

* Low latency for search and booking operations.
* Clean, readable, and maintainable OOP design.
* In-memory implementation.
* Easy to extend (more cities, payment, etc.).
* Single-threaded (no concurrency forced).

------------------------------------------------------------------------------------------------------------------------------------------------------------------

3. System Happy Flow

* BookingService is initialized with cities and theatres.
* User searches movies in a city → searchMovies(city).
* User selects a movie and gets list of shows → getShows(movie, city).
* User selects a show and views available seats → show.getAvailableSeats().
* User selects seats and books → book(show, seats).
* Booking is created, seats are marked BOOKED, and confirmation is returned.

------------------------------------------------------------------------------------------------------------------------------------------------------------------

4. Edge Cases + Handling

* No shows available for movie → return empty list.
* All seats booked → return false on booking.
* Trying to book already booked seat → reject.
* Invalid seat selection (wrong show) → reject.
* Booking more seats than available → reject.

------------------------------------------------------------------------------------------------------------------------------------------------------------------

5. UML Diagram:  https://drive.google.com/file/d/1UUhXuMb104KBMlI2I8CtiHMpD-reK8gf/view?usp=sharing

@startuml
skinparam classAttributeIconSize 0

enum SeatType {
  REGULAR, PREMIUM, RECLINER
  '' Type of seat for pricing and preference
}

enum SeatStatus {
  AVAILABLE, BOOKED
  '' Current status of a seat
}

class City {
  - String name
  - List<Theatre> theatres
  + City(String name)
  + void addTheatre(Theatre theatre)
  + List<Theatre> getTheatres()
  '' Represents a city containing multiple theatres
}

class Theatre {
  - String name
  - List<Screen> screens
  + Theatre(String name)
  + void addScreen(Screen screen)
  + List<Screen> getScreens()
  '' Represents one theatre building
}

class Screen {
  - int screenId
  - List<Show> shows
  + Screen(int screenId)
  + void addShow(Show show)
  + List<Show> getShows()
  '' One screen inside a theatre
}

class Movie {
  - int movieId
  - String title
  - int duration
  - String genre
  + Movie(int movieId, String title, int duration, String genre)
  + String getTitle()
  '' Movie details
}

class Show {
  - int showId
  - Movie movie
  - LocalDateTime startTime
  - List<Seat> seats
  + Show(int showId, Movie movie, LocalDateTime startTime)
  + List<Seat> getAvailableSeats()
  + boolean bookSeats(List<Seat> seatsToBook)
  '' One particular screening of a movie
}

class Seat {
  - int seatId
  - SeatType seatType
  - SeatStatus status
  + Seat(int seatId, SeatType seatType)
  + boolean isAvailable()
  + void bookSeat()
  + void cancelBooking()
  '' Individual seat in a show
}

class Booking {
  - int bookingId
  - Show show
  - List<Seat> seats
  - LocalDateTime bookingTime
  + Booking(Show show, List<Seat> seats)
  + int getBookingId()
  '' Confirmed booking / ticket
}

class BookingService {
  - List<City> cities
  + BookingService()
  + List<Movie> searchMovies(String cityName)
  + List<Show> getShows(Movie movie, String cityName)
  + Booking book(Show show, List<Seat> seats)
  '' Main service class for all operations
}

City "1" *-- "many" Theatre
Theatre "1" *-- "many" Screen
Screen "1" *-- "many" Show
Show "1" *-- "many" Seat
Seat --> SeatType
Seat --> SeatStatus
Booking --> Show
Booking --> "many" Seat
BookingService --> City
@enduml

------------------------------------------------------------------------------------------------------------------------------------------------------------------
UML Explanation:

City → Theatre → Screen → Show → Seat forms the hierarchy.
Movie is associated with Show.
BookingService acts as the facade for all user operations.
All classes and methods shown in UML are implemented in code.

------------------------------------------------------------------------------------------------------------------------------------------------------------------

6. LLD Code (Java)

import java.time.*;
import java.util.*;

// SeatType.java
public enum SeatType {
    REGULAR, PREMIUM, RECLINER
}

// SeatStatus.java
public enum SeatStatus {
    AVAILABLE, BOOKED
}

// Movie.java
public class Movie {
    private final int movieId;
    private final String title;
    private final int duration;
    private final String genre;

    public Movie(int movieId, String title, int duration, String genre) {
        this.movieId = movieId;
        this.title = title;
        this.duration = duration;
        this.genre = genre;
    }

    public String getTitle() { return title; }
    public int getMovieId() { return movieId; }
}

// Seat.java
public class Seat {
    private final int seatId;
    private final SeatType seatType;
    private SeatStatus status;

    public Seat(int seatId, SeatType seatType) {
        this.seatId = seatId;
        this.seatType = seatType;
        this.status = SeatStatus.AVAILABLE;
    }

    public boolean isAvailable() {
        return status == SeatStatus.AVAILABLE;
    }

    public void bookSeat() {
        this.status = SeatStatus.BOOKED;
    }

    public void cancelBooking() {
        this.status = SeatStatus.AVAILABLE;
    }

    public int getSeatId() { return seatId; }
    public SeatType getSeatType() { return seatType; }
}

// Show.java
public class Show {
    private final int showId;
    private final Movie movie;
    private final LocalDateTime startTime;
    private final List<Seat> seats;

    public Show(int showId, Movie movie, LocalDateTime startTime) {
        this.showId = showId;
        this.movie = movie;
        this.startTime = startTime;
        this.seats = new ArrayList<>();
        initializeSeats();
    }

    private void initializeSeats() {
        for (int i = 1; i <= 50; i++) {   // 50 seats per show
            SeatType type = (i <= 10) ? SeatType.RECLINER : 
                           (i <= 30) ? SeatType.PREMIUM : SeatType.REGULAR;
            seats.add(new Seat(i, type));
        }
    }

    public List<Seat> getAvailableSeats() {
        List<Seat> available = new ArrayList<>();
        for (int i = 0; i < seats.size(); i++) {
            if (seats.get(i).isAvailable()) {
                available.add(seats.get(i));
            }
        }
        return available;
    }

    public boolean bookSeats(List<Seat> seatsToBook) {
        for (int i = 0; i < seatsToBook.size(); i++) {
            Seat seat = seatsToBook.get(i);
            if (!seat.isAvailable()) {
                return false;   // any seat already booked
            }
        }
        for (int i = 0; i < seatsToBook.size(); i++) {
            seatsToBook.get(i).bookSeat();
        }
        return true;
    }

    public Movie getMovie() { return movie; }
    public LocalDateTime getStartTime() { return startTime; }
}

// Screen.java
public class Screen {
    private final int screenId;
    private final List<Show> shows;

    public Screen(int screenId) {
        this.screenId = screenId;
        this.shows = new ArrayList<>();
    }

    public void addShow(Show show) {
        shows.add(show);
    }

    public List<Show> getShows() {
        return shows;
    }
}

// Theatre.java
public class Theatre {
    private final String name;
    private final List<Screen> screens;

    public Theatre(String name) {
        this.name = name;
        this.screens = new ArrayList<>();
    }

    public void addScreen(Screen screen) {
        screens.add(screen);
    }

    public List<Screen> getScreens() {
        return screens;
    }

    public String getName() { return name; }
}

// City.java
public class City {
    private final String name;
    private final List<Theatre> theatres;

    public City(String name) {
        this.name = name;
        this.theatres = new ArrayList<>();
    }

    public void addTheatre(Theatre theatre) {
        theatres.add(theatre);
    }

    public List<Theatre> getTheatres() {
        return theatres;
    }

    public String getName() { return name; }
}

// Booking.java
public class Booking {
    private final int bookingId;
    private final Show show;
    private final List<Seat> seats;
    private final LocalDateTime bookingTime;

    public Booking(Show show, List<Seat> seats) {
        this.bookingId = (int) (System.currentTimeMillis() % 1000000);
        this.show = show;
        this.seats = seats;
        this.bookingTime = LocalDateTime.now();
    }

    public int getBookingId() { return bookingId; }
    public Show getShow() { return show; }
    public List<Seat> getSeats() { return seats; }
}

// BookingService.java
public class BookingService {
    private final List<City> cities;

    public BookingService() {
        this.cities = new ArrayList<>();
    }

    public void addCity(City city) {
        cities.add(city);
    }

    public List<Movie> searchMovies(String cityName) {
        List<Movie> movies = new ArrayList<>();
        for (int i = 0; i < cities.size(); i++) {
            City city = cities.get(i);
            if (city.getName().equalsIgnoreCase(cityName)) {
                for (int j = 0; j < city.getTheatres().size(); j++) {
                    Theatre theatre = city.getTheatres().get(j);
                    for (int k = 0; k < theatre.getScreens().size(); k++) {
                        Screen screen = theatre.getScreens().get(k);
                        for (int m = 0; m < screen.getShows().size(); m++) {
                            Show show = screen.getShows().get(m);
                            Movie movie = show.getMovie();
                            if (!movies.contains(movie)) {   // avoid duplicates
                                movies.add(movie);
                            }
                        }
                    }
                }
            }
        }
        return movies;
    }

    public List<Show> getShows(Movie movie, String cityName) {
        List<Show> shows = new ArrayList<>();
        for (int i = 0; i < cities.size(); i++) {
            City city = cities.get(i);
            if (city.getName().equalsIgnoreCase(cityName)) {
                for (int j = 0; j < city.getTheatres().size(); j++) {
                    Theatre theatre = city.getTheatres().get(j);
                    for (int k = 0; k < theatre.getScreens().size(); k++) {
                        Screen screen = theatre.getScreens().get(k);
                        for (int m = 0; m < screen.getShows().size(); m++) {
                            Show show = screen.getShows().get(m);
                            if (show.getMovie().getMovieId() == movie.getMovieId()) {
                                shows.add(show);
                            }
                        }
                    }
                }
            }
        }
        return shows;
    }

    public Booking book(Show show, List<Seat> seats) {
        if (show.bookSeats(seats)) {
            Booking booking = new Booking(show, seats);
            System.out.println("Booking successful! Booking ID: " + booking.getBookingId());
            return booking;
        }
        System.out.println("Booking failed. Some seats are not available.");
        return null;
    }
}
