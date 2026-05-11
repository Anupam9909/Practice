1. Functional Requirements

* Multi-level parking lot.
* Support Bike, Car, Truck.
* Different spot sizes: Small, Medium, Large.
* Park vehicle → find suitable spot across all levels and issue Ticket.
* Unpark using Ticket and free the spot.
* Proper level-wise spot allocation.

------------------------------------------------------------------------------------------------------------------------------------------------------------------

2. Non-Functional Requirements

* Low latency for park/unpark.
* Clean, maintainable OOP design.
* In-memory, single-threaded.
* Easy to extend (more levels, spot types).

------------------------------------------------------------------------------------------------------------------------------------------------------------------

3. System Happy Flow

* ParkingLot is initialized and levels are added.
* Vehicle comes → parkVehicle(vehicle) called.
* ParkingLot searches each Level for available compatible spot.
* First suitable spot found → assigned to vehicle and Ticket generated.
* Vehicle exits → freeSpot(ticket) → spot freed.

------------------------------------------------------------------------------------------------------------------------------------------------------------------

4. Edge Cases + Handling

* No suitable spot available → return null.
* Vehicle does not fit in spot size → rejected.
* Invalid ticket → ignored safely.
* Parking on higher floors only when lower floors are full.

------------------------------------------------------------------------------------------------------------------------------------------------------------------

5. UML Diagram:   https://drive.google.com/file/d/1qbErKZGwBtgqbrzX5dMgrHJoppIJWc8H/view?usp=sharing

@startuml
skinparam classAttributeIconSize 0

enum VehicleType {
  CAR, BIKE, TRUCK
  '' Defines type of vehicle
}

enum SpotType {
  SMALL, MEDIUM, LARGE
  '' Defines capacity/size of parking spot
}

abstract class Vehicle {
  - String licenseNumber
  - VehicleType vehicleType
  + Vehicle(String licenseNumber, VehicleType vehicleType)
  + String getLicenseNumber()
  + VehicleType getVehicleType()
  '' Abstract base for all vehicles
}

class Car extends Vehicle {
  + Car(String licenseNumber)
}

class Bike extends Vehicle {
  + Bike(String licenseNumber)
}

class Truck extends Vehicle {
  + Truck(String licenseNumber)
}

class ParkingSpot {
  - int spotId
  - SpotType spotType
  - boolean occupied
  - Vehicle vehicle   // null if free
  + ParkingSpot(int spotId, SpotType spotType)
  + boolean isFree()
  + boolean canFitVehicle(Vehicle v)
  + void assignVehicle(Vehicle v)
  + void removeVehicle()
  '' Single parking slot
}

class Level {
  - int floorNumber
  - List<ParkingSpot> spots
  + Level(int floorNumber, int small, int medium, int large)
  + ParkingSpot getAvailableSpot(VehicleType vehicleType)
  + ParkingSpot parkVehicle(Vehicle vehicle)
  '' One floor in parking lot
}

class Ticket {
  - int ticketId
  - Vehicle vehicle
  - ParkingSpot spot
  - long entryTime
  + Ticket(Vehicle vehicle, ParkingSpot spot)
  + int getTicketId()
  + Vehicle getVehicle()
  + ParkingSpot getSpot()
  '' Parking receipt
}

class ParkingLot {
  - List<Level> levels
  - Map<Integer, Ticket> activeTickets
  + ParkingLot()
  + void addLevel(Level level)
  + ParkingSpot findSpot(VehicleType vehicleType)
  + Ticket parkVehicle(Vehicle vehicle)
  + void freeSpot(Ticket ticket)
  '' Main system controller
}

Vehicle <|-- Car
Vehicle <|-- Bike
Vehicle <|-- Truck

ParkingLot "1" *-- "many" Level
Level "1" *-- "many" ParkingSpot
ParkingSpot --> SpotType
ParkingSpot --> "0..1" Vehicle
Ticket --> Vehicle
Ticket --> ParkingSpot
ParkingLot --> Ticket
@enduml


------------------------------------------------------------------------------------------------------------------------------------------------------------------

UML Explanation:

Vehicle is abstract with concrete implementations.
ParkingSpot manages occupancy and size compatibility.
Level handles spots on one floor.
ParkingLot coordinates everything.
Fully consistent with your mentioned entities.

------------------------------------------------------------------------------------------------------------------------------------------------------------------

6. LLD Code (Java)

import java.util.*;

// VehicleType.java
public enum VehicleType {
    CAR, BIKE, TRUCK
}

// SpotType.java
public enum SpotType {
    SMALL, MEDIUM, LARGE
}

// Vehicle.java
public abstract class Vehicle {
    private final String licenseNumber;
    private final VehicleType vehicleType;

    public Vehicle(String licenseNumber, VehicleType vehicleType) {
        this.licenseNumber = licenseNumber;
        this.vehicleType = vehicleType;
    }

    public String getLicenseNumber() {
        return licenseNumber;
    }

    public VehicleType getVehicleType() {
        return vehicleType;
    }
}

// Car.java
public class Car extends Vehicle {
    public Car(String licenseNumber) {
        super(licenseNumber, VehicleType.CAR);
    }
}

// Bike.java
public class Bike extends Vehicle {
    public Bike(String licenseNumber) {
        super(licenseNumber, VehicleType.BIKE);
    }
}

// Truck.java
public class Truck extends Vehicle {
    public Truck(String licenseNumber) {
        super(licenseNumber, VehicleType.TRUCK);
    }
}

// ParkingSpot.java
public class ParkingSpot {
    private final int spotId;
    private final SpotType spotType;
    private boolean occupied;
    private Vehicle vehicle;

    public ParkingSpot(int spotId, SpotType spotType) {
        this.spotId = spotId;
        this.spotType = spotType;
        this.occupied = false;
        this.vehicle = null;
    }

    public boolean isFree() {
        return !occupied;
    }

    public boolean canFitVehicle(Vehicle v) {
        VehicleType type = v.getVehicleType();
        if (type == VehicleType.BIKE) return true;
        if (type == VehicleType.CAR) return spotType != SpotType.SMALL;
        if (type == VehicleType.TRUCK) return spotType == SpotType.LARGE;
        return false;
    }

    public void assignVehicle(Vehicle v) {
        this.vehicle = v;
        this.occupied = true;
    }

    public void removeVehicle() {
        this.vehicle = null;
        this.occupied = false;
    }
}

// Level.java
public class Level {
    private final int floorNumber;
    private final List<ParkingSpot> spots;

    public Level(int floorNumber, int small, int medium, int large) {
        this.floorNumber = floorNumber;
        this.spots = new ArrayList<>();
        int id = 1;
        for (int i = 0; i < small; i++) spots.add(new ParkingSpot(id++, SpotType.SMALL));
        for (int i = 0; i < medium; i++) spots.add(new ParkingSpot(id++, SpotType.MEDIUM));
        for (int i = 0; i < large; i++) spots.add(new ParkingSpot(id++, SpotType.LARGE));
    }

    public ParkingSpot getAvailableSpot(VehicleType vehicleType) {
        for (int i = 0; i < spots.size(); i++) {
            ParkingSpot spot = spots.get(i);
            if (spot.isFree()) {
                Vehicle temp = createTempVehicle(vehicleType);
                if (spot.canFitVehicle(temp)) {
                    return spot;
                }
            }
        }
        return null;
    }

    private Vehicle createTempVehicle(VehicleType type) {
        if (type == VehicleType.BIKE) return new Bike("");
        if (type == VehicleType.CAR) return new Car("");
        return new Truck("");
    }

    public ParkingSpot parkVehicle(Vehicle vehicle) {
        ParkingSpot spot = getAvailableSpot(vehicle.getVehicleType());
        if (spot != null) {
            spot.assignVehicle(vehicle);
            return spot;
        }
        return null;
    }
}

// Ticket.java
public class Ticket {
    private final int ticketId;
    private final Vehicle vehicle;
    private final ParkingSpot spot;
    private final long entryTime;

    public Ticket(Vehicle vehicle, ParkingSpot spot) {
        this.ticketId = (int) (System.currentTimeMillis() % 1000000);
        this.vehicle = vehicle;
        this.spot = spot;
        this.entryTime = System.currentTimeMillis();
    }

    public int getTicketId() { return ticketId; }
    public Vehicle getVehicle() { return vehicle; }
    public ParkingSpot getSpot() { return spot; }
}

// ParkingLot.java - BUG FIXED
public class ParkingLot {
    private final List<Level> levels;
    private final Map<Integer, Ticket> activeTickets;

    public ParkingLot() {
        this.levels = new ArrayList<>();
        this.activeTickets = new HashMap<>();
    }

    public void addLevel(Level level) {
        levels.add(level);
    }

    public ParkingSpot findSpot(VehicleType vehicleType) {
        for (int i = 0; i < levels.size(); i++) {
            Level level = levels.get(i);
            ParkingSpot spot = level.getAvailableSpot(vehicleType);
            if (spot != null) {
                return spot;
            }
        }
        return null;
    }

    public Ticket parkVehicle(Vehicle vehicle) {
        // Search for spot across all levels
        ParkingSpot spot = null;
        Level targetLevel = null;

        for (int i = 0; i < levels.size(); i++) {
            Level level = levels.get(i);
            spot = level.getAvailableSpot(vehicle.getVehicleType());
            if (spot != null) {
                targetLevel = level;
                break;
            }
        }

        if (spot == null || targetLevel == null) {
            System.out.println("No spot available for " + vehicle.getVehicleType());
            return null;
        }

        targetLevel.parkVehicle(vehicle);   // Now parking on correct level

        Ticket ticket = new Ticket(vehicle, spot);
        activeTickets.put(ticket.getTicketId(), ticket);

        System.out.println(vehicle.getVehicleType() + " parked successfully. Ticket ID: " + ticket.getTicketId());
        return ticket;
    }

    public void freeSpot(Ticket ticket) {
        if (ticket == null || !activeTickets.containsKey(ticket.getTicketId())) {
            System.out.println("Invalid Ticket");
            return;
        }

        ticket.getSpot().removeVehicle();
        activeTickets.remove(ticket.getTicketId());
        System.out.println("Spot freed for Ticket ID: " + ticket.getTicketId());
    }
}
