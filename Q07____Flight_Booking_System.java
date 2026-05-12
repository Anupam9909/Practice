
1. Functional Requirements

* Support multiple cities with airports.
* Multiple airlines operating flights between cities.
* Each flight has multiple schedules (departure dates and times).
* Each flight schedule has different seat types (Economy, Business, First Class).
* Users can search flights by source city, destination city, and date.
* Users can view available seats for a specific flight schedule.
* Users can book multiple seats along with passenger details.
* System should generate a booking confirmation (Booking/Ticket).
* Maintain seat availability status (Available / Booked).

------------------------------------------------------------------------------------------------------------------------------------------------------------------

2. Non-Functional Requirements

* Low latency for search and booking operations.
* Clean, readable, and maintainable OOP design.
* In-memory implementation (no database needed for LLD).
* Easy to extend (add payment, cancellation, etc.).
* Single-threaded implementation (sufficient for this scope).

------------------------------------------------------------------------------------------------------------------------------------------------------------------

3. System Happy Flow

* Admin initializes FlightBookingService and adds cities, airlines, and flights.
* User searches for flights using source, destination, and date → searchFlights().
* System returns list of matching FlightSchedule.
* User selects a schedule and views available seats → getAvailableSeats().
* User selects desired seats and provides passenger details.
* System calls bookTicket() → validates seats and books them.
* Booking object is created and returned as confirmation.

------------------------------------------------------------------------------------------------------------------------------------------------------------------

4. Edge Cases + Handling

* No flights available on given route/date → Return empty list.
* No seats available in requested class → Booking fails.
* Partial seat booking (some seats already booked) → Reject entire booking.
* Invalid city/airport code → Return empty search result.
* Booking more seats than available → Reject with proper message.
* Same passenger details → Allowed (for family/group booking).

------------------------------------------------------------------------------------------------------------------------------------------------------------------

5. UML Diagram      https://drive.google.com/file/d/13jgI8gUJhnwyv9sdkkuWrkB1jPCn-GgD/view?usp=sharing


@startuml
skinparam classAttributeIconSize 0

enum SeatType {
  ECONOMY, BUSINESS, FIRST_CLASS
  '' Different seat classes with different pricing and facilities
}

enum SeatStatus {
  AVAILABLE, BOOKED
  '' Current status of each seat
}

class City {
  - String name               // Name of the city
  - String airportCode        // Airport code (e.g., DEL, BOM, BLR)
  + City(String name, String airportCode)
  '' Represents a city with airport
}

class Airline {
  - String name
  + Airline(String name)
  '' Airline company operating flights
}

class Flight {
  - String flightNumber
  - Airline airline
  - City source
  - City destination
  - int durationMinutes
  + Flight(String flightNumber, Airline airline, City source, City destination, int duration)
  '' Defines a flight route between two cities
}

class FlightSchedule {
  - int scheduleId
  - Flight flight
  - LocalDateTime departureTime
  - List<Seat> seats
  + FlightSchedule(int scheduleId, Flight flight, LocalDateTime departureTime)
  + List<Seat> getAvailableSeats()      // Returns currently available seats
  + boolean bookSeats(List<Seat> seats) // Books seats if all are available
  '' Specific departure of a flight on a particular date and time
}

class Seat {
  - int seatNumber
  - SeatType seatType
  - SeatStatus status
  + Seat(int seatNumber, SeatType seatType)
  + boolean isAvailable()
  + void bookSeat()
  '' Individual seat in a flight schedule
}

class Passenger {
  - String name               // Passenger full name
  - String passportNumber     // Passport or government ID
  + Passenger(String name, String passportNumber)
  '' Represents a traveler
}

class Booking {
  - int bookingId
  - FlightSchedule schedule
  - List<Seat> seats
  - List<Passenger> passengers
  - LocalDateTime bookingTime
  + Booking(FlightSchedule schedule, List<Seat> seats, List<Passenger> passengers)
  '' Confirmed ticket / reservation record
}

class FlightBookingService {
  - List<City> cities
  - List<Flight> flights
  - List<FlightSchedule> schedules     // Persistent list of all flight schedules
  + FlightBookingService()
  + void addCity(City city)
  + void addFlight(Flight flight)      // Also creates sample schedules
  + List<FlightSchedule> searchFlights(String sourceCode, String destinationCode, LocalDate date)
  + Booking bookTicket(FlightSchedule schedule, List<Seat> seats, List<Passenger> passengers)
  '' Main facade/service class for all operations
}

Flight "1" --> "1" Airline
Flight "1" --> "1" City : source
Flight "1" --> "1" City : destination
Flight "1" *-- "many" FlightSchedule
FlightSchedule "1" *-- "many" Seat
Seat --> SeatType
Seat --> SeatStatus
Booking --> FlightSchedule
Booking "1" *-- "many" Seat
Booking "1" *-- "many" Passenger
FlightBookingService --> City
FlightBookingService --> Flight
FlightBookingService --> FlightSchedule
@enduml



----------------------------------------------------------------------------------------------------------------------
code:

import java.time.*;
import java.util.*;

// ====================== ENUMS ======================

public enum SeatType {
    ECONOMY, BUSINESS, FIRST_CLASS
}

public enum SeatStatus {
    AVAILABLE, BOOKED
}

// ====================== ENTITIES ======================

public class City {
    private final String name;
    private final String airportCode;

    public City(String name, String airportCode) {
        this.name = name;
        this.airportCode = airportCode;
    }

    public String getName() { return name; }
    public String getAirportCode() { return airportCode; }
}

public class Airline {
    private final String name;

    public Airline(String name) {
        this.name = name;
    }

    public String getName() { return name; }
}

public class Flight {
    private final String flightNumber;
    private final Airline airline;
    private final City source;
    private final City destination;
    private final int durationMinutes;

    public Flight(String flightNumber, Airline airline, City source, City destination, int durationMinutes) {
        this.flightNumber = flightNumber;
        this.airline = airline;
        this.source = source;
        this.destination = destination;
        this.durationMinutes = durationMinutes;
    }

    public String getFlightNumber() { return flightNumber; }
    public City getSource() { return source; }
    public City getDestination() { return destination; }
}

public class Seat {
    private final int seatNumber;
    private final SeatType seatType;
    private SeatStatus status;

    public Seat(int seatNumber, SeatType seatType) {
        this.seatNumber = seatNumber;
        this.seatType = seatType;
        this.status = SeatStatus.AVAILABLE;
    }

    public boolean isAvailable() {
        return status == SeatStatus.AVAILABLE;
    }

    public void bookSeat() {
        this.status = SeatStatus.BOOKED;
    }

    public int getSeatNumber() { return seatNumber; }
    public SeatType getSeatType() { return seatType; }
}

public class Passenger {
    private final String name;
    private final String passportNumber;

    public Passenger(String name, String passportNumber) {
        this.name = name;
        this.passportNumber = passportNumber;
    }

    public String getName() { return name; }
}

public class Booking {
    private final int bookingId;
    private final FlightSchedule schedule;
    private final List<Seat> seats;
    private final List<Passenger> passengers;
    private final LocalDateTime bookingTime;

    public Booking(FlightSchedule schedule, List<Seat> seats, List<Passenger> passengers) {
        this.bookingId = (int) (System.currentTimeMillis() % 1000000);
        this.schedule = schedule;
        this.seats = seats;
        this.passengers = passengers;
        this.bookingTime = LocalDateTime.now();
    }

    public int getBookingId() { return bookingId; }
}

// ====================== FLIGHT SCHEDULE ======================

public class FlightSchedule {
    private final int scheduleId;
    private final Flight flight;
    private final LocalDateTime departureTime;
    private final List<Seat> seats;

    public FlightSchedule(int scheduleId, Flight flight, LocalDateTime departureTime) {
        this.scheduleId = scheduleId;
        this.flight = flight;
        this.departureTime = departureTime;
        this.seats = new ArrayList<>();
        initializeSeats();
    }

    private void initializeSeats() {
        for (int i = 1; i <= 60; i++) {
            SeatType type = (i <= 8) ? SeatType.FIRST_CLASS :
                           (i <= 20) ? SeatType.BUSINESS : SeatType.ECONOMY;
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
            if (!seatsToBook.get(i).isAvailable()) {
                return false;
            }
        }
        for (int i = 0; i < seatsToBook.size(); i++) {
            seatsToBook.get(i).bookSeat();
        }
        return true;
    }

    public Flight getFlight() { return flight; }
    public LocalDateTime getDepartureTime() { return departureTime; }
}

// ====================== MAIN SERVICE ======================

public class FlightBookingService {
    private final List<City> cities = new ArrayList<>();
    private final List<Flight> flights = new ArrayList<>();
    private final List<FlightSchedule> schedules = new ArrayList<>();

    public FlightBookingService() {
        // Default constructor
    }

    public void addCity(City city) {
        cities.add(city);
    }

    public void addFlight(Flight flight) {
        flights.add(flight);
        createSampleSchedules(flight);
    }

    private void createSampleSchedules(Flight flight) {
        for (int i = 0; i < 3; i++) {
            LocalDateTime depTime = LocalDate.now().plusDays(i).atTime(8 + i * 3, 0);
            FlightSchedule schedule = new FlightSchedule(
                schedules.size() + 1001, flight, depTime);
            schedules.add(schedule);
        }
    }

    public List<FlightSchedule> searchFlights(String sourceCode, String destinationCode, LocalDate date) {
        List<FlightSchedule> result = new ArrayList<>();

        for (int i = 0; i < schedules.size(); i++) {
            FlightSchedule schedule = schedules.get(i);
            Flight flight = schedule.getFlight();

            String srcCode = flight.getSource().getAirportCode();
            String destCode = flight.getDestination().getAirportCode();
            LocalDate depDate = schedule.getDepartureTime().toLocalDate();

            if (srcCode.equals(sourceCode) && 
                destCode.equals(destinationCode) && 
                depDate.equals(date)) {
                
                result.add(schedule);
            }
        }
        return result;
    }

    public Booking bookTicket(FlightSchedule schedule, List<Seat> seats, List<Passenger> passengers) {
        if (schedule.bookSeats(seats)) {
            Booking booking = new Booking(schedule, seats, passengers);
            System.out.println("Booking successful! Booking ID: " + booking.getBookingId());
            return booking;
        }
        System.out.println("Booking failed. Some seats are not available.");
        return null;
    }
}

// ====================== DEMO CLASS ======================

public class FlightBookingDemo {
    public static void main(String[] args) {
        FlightBookingService service = new FlightBookingService();

        // Setup Data
        City delhi = new City("Delhi", "DEL");
        City mumbai = new City("Mumbai", "BOM");

        service.addCity(delhi);
        service.addCity(mumbai);

        Airline indigo = new Airline("Indigo");
        Flight flight1 = new Flight("6E 234", indigo, delhi, mumbai, 135);

        service.addFlight(flight1);

        // Search
        List<FlightSchedule> availableFlights = service.searchFlights("DEL", "BOM", LocalDate.now());

        if (!availableFlights.isEmpty()) {
            FlightSchedule selectedSchedule = availableFlights.get(0);
            List<Seat> availableSeats = selectedSchedule.getAvailableSeats();

            List<Seat> seatsToBook = new ArrayList<>();
            if (availableSeats.size() >= 2) {
                seatsToBook.add(availableSeats.get(0));
                seatsToBook.add(availableSeats.get(1));
            }

            List<Passenger> passengers = new ArrayList<>();
            passengers.add(new Passenger("Amit Kumar", "P987654"));
            passengers.add(new Passenger("Sneha Sharma", "P123456"));

            service.bookTicket(selectedSchedule, seatsToBook, passengers);
        }
    }
}
