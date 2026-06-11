# One Choice

*Survive, grow stronger, know when you’re ready.*

---

## 🎯 Main Gameplay Goal
**One Choice** is a fast-paced, mobility-driven tactical roguelike. You start with nothing but basic movement and a gun. Your goal is to navigate through procedurally generated dungeon floors, clear locked rooms of randomized enemy waves, and collect powerful tactical and stat upgrades.

The ultimate objective is to defeat the **Final Boss**. However, there is a catch: the boss room is available almost immediately. The core challenge is deciding exactly *when* you are ready to make your "One Choice." Fight the boss too early, and you will be quickly overwhelmed. Grind for upgrades too long, and the standard enemies will scale in difficulty over time. Permadeath is strictly enforced—if your health reaches zero, your run is over. No save states, no second chances.

Every time you clear a room you earn upgrades in the form of collectable trophies, moving into them will pick them up and give you stat boosts. 
---

## ⌨️ Controls
* **W, A, S, D** - Move your character
* **Left Mouse Click** - Shoot (aims directly at your mouse cursor)
* **E** - Interact (Use this to enter portals and access the upgrade menu)
* **1, 2, 3 (Number Row)** - Select your desired stat upgrade when the menu is open
* **R** - Restart a new run from the Game Over screen

---

## 🚀 How to Run the Game

### Option 1: Running the Pre-compiled JAR (Recommended for Players)
1. Ensure you have Java installed on your system (Java 17 or newer is recommended).
2. Locate the provided `.jar` file (e.g., `OneChoice.jar`).
3. Simply double-click the `.jar` file to launch the game.
4. *Troubleshooting:* If double-clicking doesn't work, open your terminal or command prompt, navigate to the folder containing the file, and run:
   ```bash
   java -jar OneChoice.jar

Option 2: Running from Source (For Developers)
This project is built using the LibGDX Java framework and uses Gradle for its build system.

Extract the project folder.

Open your preferred Java IDE (IntelliJ IDEA is highly recommended).

Open/Import the folder as a Gradle project.

Allow Gradle to sync and download the required LibGDX dependencies.

To launch the game, run the desktop:run Gradle task, or navigate to DesktopLauncher.java (inside the desktop module) and run its main() method.
