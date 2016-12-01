## CSC591 (Game Engine Foundations) Homework 4

### Homework Spec 

The homework spec can be found in `[root_dir]\docs\HW4.pdf`. Below is a brief excerpt from the same: 

Your task for the fourth and final assignment is to implement a script management system, some scripted functionality, and a new game (or games) to demonstrate the resuability of your engine design.

### Introduction

My assignments have been building on one other. And by that respect, I probably had to make the least amount of change for this assignment. I simply added the script manager and added respective events to handle them. Even for the two games that I built, I almost did little to no change to my engine. All this will be apparent as I go through the following sections. Unlike before, I will be presenting steps to run my program individually in each of the sections, instead of one set of instructions at the very top. This is because we would be needing at least three different executables for this assignment.   
   
### Scripting

#### Running my program

There are two ways to run my multiplayer game / engine demo with scripting enabled:

1. **JAR**:
    1. Find the JAR file for this project in
    `[root_dir]\out\artifacts\multiplayer_game_engine_jar\multiplayer-game-engine.jar`.
    2. Open a command line and type `java -jar multiplayer-game-engine.jar s` (for server).
    3. For running clients, open other command lines, type and execute `java -jar multiplayer-game-engine.jar c` as many times you want for any number of clients.
    4. Remember that you need the run the server first and then the clients, otherwise this might throw some exception. This should be normal, as for most multiplayer games, the headless server generally is always running. 
    5. Also currently, the client searches for a running server in `localhost`, so running the server and client in different computers will not work. If you still want to run it in different computers, follow my second way of running the program and before building it, open `Constants.java` and assign the server's IP to the `SERVER_ADDRESS` String variable.
    6. The scripts will be present in `[root_dir]\out\artifacts\multiplayer_game_engine_jar\scripts\` folder. If you want to change the script, you can open `script.js` and modify accordingly.  
      
2. **IntelliJ**:
    1. Install [IntelliJ Community Edition](https://www.jetbrains.com/idea/download/#section=windows).
    2. Import and build my project.
    3. There should be two run configurations - one for the server and one for the client. Run the "Server" first and then the "Client". The shortcut for running programs in IntelliJ is `Alt + Shift + F10`.
    4. If you don't find the run configurations, make two yourself. For the sever, give a command line argument of `s` and for the client, give a command line argument of `c` (without the quotes).
    5. The scripts will be present in `[root_dir]\scripts\` folder. If you want to change the script, you can open `script.js` and modify accordingly. Remember that this folder is different that what was shown above. This is because I have to keep the scripts folder relative to the executable. So if I have two executables (the JAR and one run through IntelliJ), the paths will be different and there has to be different scripts as well. Moreover, the JARs don't pack the scripts, so they have to be separately supplied to them.  
      
#### Explanation and features

There are two ways of handling scripts in my game - one being the vanilla way of running a script in every update cycle. This is showcased by the "Modifying game objects" sub-section below. In this case, the `controller` class registers a set of bindings (of objects) with the engine which it wants the scripts to have. And then in every update cycle, based on the toggle script value, the script is executed. The function to be executed is also registered with the engine. The other method of calling scripts is on-demand. And this is showcased in the "Handling events" sub-section. Here, I only execute scripts when some specific event(s) arise. The significant parts of my scripting are written below: 

1. **Toggling the script**: In-game, you can toggle execution of the script by pressing the `T` key. I have kept this because I don't want to run every script from the moment the game starts. I want to find the right time to run it. Also, I think giving this power to the game developer is invaluable. All these instructions are present on the bottom-left corner of the screen. 
2. **Modifying game objects**: The function `stairMover` does this. It allows changing the positioning of the standing stairs (static rectangles). There are at max three static rectangles in my game, so I have three blocks for moving the stairs up, down, left or right. If you see any statement, for e.g. `standingStair2.moveUp(0)` - the function name describes what I am trying to do here. In this case, I am trying to move the stair up with (currently) `0` velocity. So it will not move at all. You can change this to something else and see the stair move, without the need of any re-compilation. This is an example of modifying game objects (specifically the position in this case) in the game. 
3. **Handling events**: My user inputs are driven through events (well almost everything in my game is driven by events now). I have modified my game/playground such that, when the scripts are toggled, the user input is handled through scripts. This is done in the `handlePlayer` function. When the user input event is raised, it goes to my event handler and based on the script toggle, it raises another event for the script. This again comes to my event handler class, but in a different method specifically reserved for scripts, and then the user input event is passed to the script through the values `key` (which key was pressed) and `set` (whether it was pressed or released). The way that I have handled user events in my scripts has a subtle difference. Previously pressing `A` or `D` would move the player left or right. And pressing `SPACE` would make the player jump. But now, pressing `A` or `D` would make the player move and jump at the same time. There's no way to just move left or right any more. You can see this in my creencast, when I toggle the script and move my players, they will never move just left or right, but will always have a jumping component to their movement. This is not necessarily anything that improves a game, but instead is just a way to showcase that I am handling user inputs through scripts now.   

#### Thoughts

Implementing the scripting was rather easy. I built over the code that Professor had given in class. The only non-trivial part was setting up the execution of the script correctly, so that all the objects to be bound and the function to be called gets properly passed through. As I am trying to build an engine which can be plugged in to build any game, scripting is an important part of it. So scripting will have to be a part of my engine. So I had to build ways so that my game can properly pass important information about script paths, bindings and script function names and then let the engine handle the execution in the right moment. In that my current game can be replaced with another game, and no change needs to be done at the engine level. 

#### Screencast

I have uploaded a screencast to YouTube so that it's easier for you to check what I've done. The demo shows two clients and shows the scripts being toggled and values changed to immediately reflect the same in-game. First, I show the game objects being modified and then I show player movement after toggling a script. It might be a little difficult to understand when exactly am I toggling the script, because that is not reflected on-screen, but you can understand that it has been toggled when you see my character not making any straight left or right moves, but always jumping. And then toggled back to not using scripts when it starts making those kind of moves.
 
 https://www.youtube.com/watch?v=3hwwboad5Js&feature=youtu.be