# Comprehensive Project Documentation
**Project Title:** AI-Based Smart Traffic Control System Using OS Synchronization

This document explains everything about the project in simple, beginner-friendly terms. It covers how the interface works, how the core engine runs, and what Operating System (OS) concepts are used.

---

## 1. Introduction: The Big Picture
In a normal program, everything happens one step at a time. But in an Operating System, hundreds of programs run at the exact same time. This is called **Multi-threading**. 

This project simulates a real-life traffic intersection to demonstrate multi-threading:
- **Vehicles = Threads:** Every car is its own independent mini-program running at the same time.
- **The Intersection = Critical Section:** A restricted area where only a few threads (cars) can be at once. If too many enter, they crash.
- **Traffic Lights = Shared Resources:** They control who gets to go.

The goal of this project is to use OS concepts to safely manage all these moving cars without any crashes or bugs.

---

## 2. How the Interface (GUI) Works
The visual interface is built using Java Swing. It uses a technique called **Double-Buffering** and runs at 30 frames per second (FPS).

### How things move on screen:
1. Every 33 milliseconds, the screen asks the background engine: *"Where is every car right now?"*
2. The cars (which are running on separate threads) constantly update their X and Y coordinates.
3. Because multiple threads are reading and writing coordinates at the same time, we use the `volatile` keyword in Java. This guarantees that the screen always sees the most accurate, up-to-date position of the car.

### What the Buttons Do:
*   **Start:** Fires a starting gun (`CountDownLatch`) that releases all cars at once.
*   **Add Normal / Emergency:** Spawns a new car thread. Emergency cars trigger the AI to change the traffic lights.
*   **Trigger Deadlock:** Spawns two special cars that get stuck blocking each other permanently to demonstrate a system freeze.
*   **Resolve Deadlock:** Forcibly deletes one of the stuck cars (an OS concept called *Preemption*) to free the system.
*   **Exchanger Demo:** Takes two cars from different lanes and makes them safely swap data.
*   **Convoy Demo:** Makes 3 cars wait for each other at a barrier, then cross the intersection together.

---

## 3. The Core Engine: A Car's Life Cycle
When you spawn a vehicle, here is exactly what its thread does (`TrafficSimulationEngine.java`):

1.  **APPROACHING:** The car moves toward the intersection. It checks if there is a car directly in front of it. If so, it stops to maintain a safe distance.
2.  **WAITING:** The car stops at the white line. It waits for TWO things:
    *   The traffic light to turn **Green**.
    *   The **Semaphore** to have an open permit.
3.  **IN_INTERSECTION:** The car grabs the Semaphore permit and drives quickly through the intersection. No other car can take its spot.
4.  **PASSED:** The car leaves the intersection, releases the Semaphore permit back to the system, and drives off the screen. The thread then silently kills itself to save memory.

---

## 4. The 6 Operating System Concepts Used

This is the most important part of the project. Here is how standard Java OS locks are used:

### 1. Semaphore (`IntersectionSemaphore.java`)
*   **What it is:** A bouncer at a club. It has a limited number of VIP passes.
*   **How we use it:** We gave the Semaphore **3 permits**. When a car wants to enter the intersection, it must take a permit (`acquire()`). If 3 cars are already inside, the 4th car must wait until one leaves and returns its permit (`release()`). This prevents traffic jams inside the box.

### 2. CountDownLatch (`StartLatch.java`)
*   **What it is:** A starting gun at a race.
*   **How we use it:** When the app opens, cars spawn but don't move. They all call `await()` on the latch. When you click the **Start** button, it counts down to zero (`countDown()`). Suddenly, the gates open and every single car thread starts moving at the exact same millisecond.

### 3. CyclicBarrier (`ConvoyBarrier.java`)
*   **What it is:** A group of friends waiting at a restaurant until everyone arrives before going to the table.
*   **How we use it:** The **Convoy Demo** button spawns 3 cars. Even if one arrives at the intersection early, it will wait (`await()`) until all 3 cars have arrived. Once the 3rd car arrives, the barrier breaks and they all drive through together.

### 4. Phaser (`TrafficPhaser.java`)
*   **What it is:** A multi-phase cyclic lock.
*   **How we use it:** It controls the traffic lights. It has 4 phases:
    *   Phase 0: North-South Green
    *   Phase 1: North-South Yellow
    *   Phase 2: East-West Green
    *   Phase 3: East-West Yellow
    It perfectly cycles through these states and tells the AI exactly how much time is left in the current phase.

### 5. Exchanger (`VehicleExchanger.java`)
*   **What it is:** A secure briefcase swap between two spies.
*   **How we use it:** Two separate threads (e.g., a North car and an East car) meet at a synchronization point. The North car hands over a string ("North has 5 cars waiting") and the East car hands over ("East has 2 cars waiting"). They safely swap data without using dangerous global variables.

### 6. Deadlock (`DeadlockManager.java`)
*   **What it is:** Two people refuse to move out of each other's way in a hallway.
*   **How we use it:** Thread A locks Resource 1, and Thread B locks Resource 2. Then, Thread A demands Resource 2, while Thread B demands Resource 1. Both threads freeze forever. This is called a **Circular Wait**. We fix it using **Preemption** (interrupting one thread to break the cycle).

---

## 5. The AI Controller
Instead of a dumb timer that just waits 5 seconds, the `SmartTrafficController` is an intelligent background thread that wakes up every 3 seconds to analyze the roads.

It follows these rules in strict priority:
1.  **Emergency Rule:** If there is an Ambulance or Police car, IMMEDIATELY give that lane maximum Green time (10 seconds) to clear the path.
2.  **Starvation Rule:** If a lane has been waiting at a red light for more than 15 seconds, forcefully add time to their next green light so they don't wait forever.
3.  **Proportional Rule:** If North has 8 cars waiting, but East only has 2, give North 8 seconds of Green, and East only 2 seconds of Green.
4.  **Deadlock Warning:** If all 4 lanes are totally packed with cars, sound a warning to the logs.

---

## 6. Code Folder Structure

If you need to find where something is written in the code:

*   **`src/Main.java`** - The entry point that starts the app.
*   **`src/gui/`** - All the visual drawing code (IntersectionPanel, ControlPanel, etc).
*   **`src/simulation/`** - The brain of the project (`TrafficSimulationEngine`). Controls threads.
*   **`src/sync/`** - The folder containing all the OS concepts (Semaphore, Phaser, Deadlock, etc).
*   **`src/ai/`** - The `SmartTrafficController` logic.
*   **`src/model/`** - Basic data templates for what a Vehicle is, Lane directions, and Light colors.
