1. Functional Requirements

* Multiple cities with multiple hotels.
* Each hotel has different room types (Single, Double, Suite, Deluxe).
* Rooms can be booked for specific date ranges (check-in to check-out).
* Search hotels by city and dates.
* View available rooms for given check-in/check-out dates.
* Book one or more rooms with guest details.
* Prevent double booking on overlapping dates.
* Generate booking confirmation.

------------------------------------------------------------------------------------------------------------------------------------------------------------------

2. Non-Functional Requirements

* Low latency for search and booking.
* Clean, maintainable OOP design.
* Correct modeling of room availability using date ranges.
* In-memory implementation.
* Single-threaded.

------------------------------------------------------------------------------------------------------------------------------------------------------------------

3. System Happy Flow

* Initialize HotelBookingService with cities and hotels.
* User searches hotels in a city for given dates.
* System returns hotels having available rooms for that date range.
* User selects hotel and rooms → provides guest details.
* System checks date overlap and books if available.
* Booking is created and rooms are reserved for the date range.

------------------------------------------------------------------------------------------------------------------------------------------------------------------

4. Edge Cases + Handling

* No rooms available for given dates → return empty list.
* Overlapping dates with existing booking → reject.
* Check-out before check-in → invalid.
* Partial room availability → book only available rooms.

------------------------------------------------------------------------------------------------------------------------------------------------------------------

5. UML Diagram   https://drive.google.com/file/d/14N0ad-fRMEXKkuG7svDi8SEuH3BdR6yU/view?usp=sharing


@startuml
skinparam classAttributeIconSize 0

enum RoomType {
  SINGLE, DOUBLE, SUITE, DELUXE
  '' Different categories of rooms
}

class City {
  - String name
  - List<Hotel> hotels
  + City(String name)
  + void addHotel(Hotel hotel)
  + List<Hotel> getHotels()
  '' Represents a city containing multiple hotels
}

class Hotel {
  - String name
  - String address
  - List<Room> rooms
  + Hotel(String name, String address)
  + void addRoom(Room room)
  + List<Room> getAvailableRooms(LocalDate checkIn, LocalDate checkOut)
  '' Represents one hotel property
}

class Room {
  - int roomNumber
  - RoomType roomType
  - List<Booking> bookings     // List of all bookings for this room (for date range check)
  + Room(int roomNumber, RoomType roomType)
  + boolean isAvailable(LocalDate checkIn, LocalDate checkOut)  // checks for date overlap
  + void addBooking(Booking booking)
  '' Individual room - availability managed by date range
}

class Guest {
  - String name
  - String email
  - String phone
  + Guest(String name, String email, String phone)
  '' Customer / Guest details
}

class Booking {
  - int bookingId
  - Hotel hotel
  - List<Room> rooms
  - List<Guest> guests
  - LocalDate checkInDate
  - LocalDate checkOutDate
  - LocalDateTime bookingTime
  + Booking(Hotel hotel, List<Room> rooms, List<Guest> guests, LocalDate checkIn, LocalDate checkOut)
  '' Confirmed hotel reservation with date range
}

class HotelBookingService {
  - List<City> cities
  + HotelBookingService()
  + void addCity(City city)
  + List<Hotel> searchHotels(String cityName, LocalDate checkIn, LocalDate checkOut)
  + Booking bookRooms(Hotel hotel, List<Room> rooms, List<Guest> guests, LocalDate checkIn, LocalDate checkOut)
  '' Main service facade for all hotel booking operations
}

City "1" *-- "many" Hotel
Hotel "1" *-- "many" Room
Booking "1" --> "1" Hotel
Booking "1" *-- "many" Room
Booking "1" *-- "many" Guest
Room "1" *-- "many" Booking
HotelBookingService --> City
HotelBookingService --> Hotel
HotelBookingService --> Booking
@enduml


------------------------------------------------------------------------------------------------------------------------------------------------------------------

NOTE:
Hotel room availability is date-range based, not permanently booked.
So instead of using RoomStatus = BOOKED, I maintained booking history inside the room and check availability using booking date overlap logic.
RoomStatus can still be added for operational states like maintenance or cleaning.

------------------------------------------------------------------------------------------------------------------------------------------------------------------

6. LLD Code (Java)

import java.time.*;
import java.util.*;

// Enums
public enum RoomType {
    SINGLE, DOUBLE, SUITE, DELUXE
}

// City.java
public class City {
    private final String name;
    private final List<Hotel> hotels;

    public City(String name) {
        this.name = name;
        this.hotels = new ArrayList<>();
    }

    public void addHotel(Hotel hotel) {
        hotels.add(hotel);
    }

    public List<Hotel> getHotels() {
        return hotels;
    }

    public String getName() { return name; }
}

// Hotel.java
public class Hotel {
    private final String name;
    private final String address;
    private final List<Room> rooms;

    public Hotel(String name, String address) {
        this.name = name;
        this.address = address;
        this.rooms = new ArrayList<>();
    }

    public void addRoom(Room room) {
        rooms.add(room);
    }

    public List<Room> getAvailableRooms(LocalDate checkIn, LocalDate checkOut) {
        List<Room> available = new ArrayList<>();
        for (int i = 0; i < rooms.size(); i++) {
            if (rooms.get(i).isAvailable(checkIn, checkOut)) {
                available.add(rooms.get(i));
            }
        }
        return available;
    }

    public String getName() { return name; }
    public String getAddress() { return address; }
}

// Room.java
public class Room {
    private final int roomNumber;
    private final RoomType roomType;
    private final List<Booking> bookings;   // Track all bookings for date overlap check

    public Room(int roomNumber, RoomType roomType) {
        this.roomNumber = roomNumber;
        this.roomType = roomType;
        this.bookings = new ArrayList<>();
    }

    public boolean isAvailable(LocalDate checkIn, LocalDate checkOut) {
        for (int i = 0; i < bookings.size(); i++) {
            Booking b = bookings.get(i);
            // Overlap condition: Not (new checkout <= existing checkin OR new checkin >= existing checkout)
            if (!(checkOut.isBefore(b.getCheckInDate()) || checkIn.isAfter(b.getCheckOutDate()))) {
                return false;
            }
        }
        return true;
    }

    public void addBooking(Booking booking) {
        bookings.add(booking);
    }

    public int getRoomNumber() { return roomNumber; }
    public RoomType getRoomType() { return roomType; }
}

// Guest.java
public class Guest {
    private final String name;
    private final String email;
    private final String phone;

    public Guest(String name, String email, String phone) {
        this.name = name;
        this.email = email;
        this.phone = phone;
    }

    public String getName() { return name; }
}

// Booking.java
public class Booking {
    private final int bookingId;
    private final Hotel hotel;
    private final List<Room> rooms;
    private final List<Guest> guests;
    private final LocalDate checkInDate;
    private final LocalDate checkOutDate;
    private final LocalDateTime bookingTime;

    public Booking(Hotel hotel, List<Room> rooms, List<Guest> guests, 
                   LocalDate checkIn, LocalDate checkOut) {
        this.bookingId = (int) (System.currentTimeMillis() % 1000000);
        this.hotel = hotel;
        this.rooms = rooms;
        this.guests = guests;
        this.checkInDate = checkIn;
        this.checkOutDate = checkOut;
        this.bookingTime = LocalDateTime.now();
    }

    public int getBookingId() { return bookingId; }
    public LocalDate getCheckInDate() { return checkInDate; }
    public LocalDate getCheckOutDate() { return checkOutDate; }
}

// HotelBookingService.java
public class HotelBookingService {
    private final List<City> cities = new ArrayList<>();

    public void addCity(City city) {
        cities.add(city);
    }

    public List<Hotel> searchHotels(String cityName, LocalDate checkIn, LocalDate checkOut) {
        List<Hotel> result = new ArrayList<>();
        for (int i = 0; i < cities.size(); i++) {
            City city = cities.get(i);
            if (city.getName().equalsIgnoreCase(cityName)) {
                for (int j = 0; j < city.getHotels().size(); j++) {
                    Hotel hotel = city.getHotels().get(j);
                    if (!hotel.getAvailableRooms(checkIn, checkOut).isEmpty()) {
                        result.add(hotel);
                    }
                }
            }
        }
        return result;
    }

    public Booking bookRooms(Hotel hotel, List<Room> rooms, List<Guest> guests, 
                             LocalDate checkIn, LocalDate checkOut) {
        
        for (int i = 0; i < rooms.size(); i++) {
            if (!rooms.get(i).isAvailable(checkIn, checkOut)) {
                System.out.println("Booking failed. Some rooms are not available for selected dates.");
                return null;
            }
        }

        Booking booking = new Booking(hotel, rooms, guests, checkIn, checkOut);

        for (int i = 0; i < rooms.size(); i++) {
            rooms.get(i).addBooking(booking);
        }

        System.out.println("Booking successful! Booking ID: " + booking.getBookingId());
        return booking;
    }
}
