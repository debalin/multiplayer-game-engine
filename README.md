## A multiplayer game engine

This project is built component-by-component over the whole semester of Fall, 2016 at NC State. It involves making a multiiplayer game engine using Processing and having features such as a monolithic game object hierarchy, a networked client-server architecture, event management system, timelines, replays and scripting. I will give instructions on how to run the program and a brief of every milestone in this readme. Please refer to the individual HW reports in the [docs](docs/) folder for detailed descriptions of what I had accomplished. 

### Instructions

I had built a playground over the semester to test my engine as I built it. Running according to my instructions, will execute that playground, which internally is using my engine. Later, I also built two small games using my engine. I will talk more about this later in the readme. There are two ways to run my multiplayer game / engine demo with scripting enabled. Remember to clone (`git clone`) my project on to your system first. 

1. **JAR**:
    1. Find the JAR file for this project in
    `out\artifacts\multiplayer_game_engine_jar\multiplayer-game-engine.jar`.
    2. Open a command line and type `java -jar multiplayer-game-engine.jar s` (for server).
    3. For running clients, open other command lines, type and execute `java -jar multiplayer-game-engine.jar c` as many times you want for any number of clients.
    4. Remember that you need the run the server first and then the clients, otherwise this might throw some exception.
    5. Also currently, the client searches for a running server in `localhost`, so running the server and client in different computers will not work. If you still want to run it in different computers, follow my second way of running the program and before building it, open `Constants.java` and assign the server's IP to the `SERVER_ADDRESS` String variable.
    6. The scripts will be present in `out\artifacts\multiplayer_game_engine_jar\scripts\` folder. If you want to change the script, you can open `script.js` and modify accordingly.  
      
2. **IntelliJ**:
    1. Install [IntelliJ Community Edition](https://www.jetbrains.com/idea/download/#section=windows).
    2. Import and build my project.
    3. There should be two run configurations - one for the server and one for the client. Run the "Server" first and then the "Client". The shortcut for running programs in IntelliJ is `Alt + Shift + F10`.
    4. If you don't find the run configurations, make two yourself. For the sever, give a command line argument of `s` and for the client, give a command line argument of `c` (without the quotes).
    5. The scripts will be present in `[root_dir]\scripts\` folder. If you want to change the script, you can open `script.js` and modify accordingly. Remember that this folder is different that what was shown above. This is because I have to keep the scripts folder relative to the executable. So if I have two executables (the JAR and one run through IntelliJ), the paths will be different and there has to be different scripts as well. Moreover, the JARs don't pack the scripts, so they have to be separately supplied to them. 
    
### Network Foundations (HW1)

In this assignment, I had an introduction to the basic concepts of a game engine. Though we were building a multi-threaded server/client architecture, I paid enough attention to make other parts of my engine as generic as possible. I started making my engine as a library which can be used and extended as and when necessary. The main implementation in this milestone was making the foundations for a server/client architecture. I had made game objects as building blocks for my game engine, which were serializable and could be easily passed through the network. The most challenging part was having multiple threads for game loops and network management, and managing the sends and receives over the network across the server and clients. The detailed report for this milestone can be found [here](docs/HW1_Report.pdf).

