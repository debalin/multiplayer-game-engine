## A multiplayer game engine

This project was a part of the Game Engine Foundations course (by [Dr. David L. Roberts](https://www.csc.ncsu.edu/faculty/robertsd/)) and was built component-by-component over the whole semester of Fall, 2016 at NC State. It involves making a multiplayer game engine using the Processing library in Java and having features such as a monolithic game object hierarchy, a networked client-server architecture, event management system, timelines, replays and scripting. I will give instructions on how to run the program and a brief of every milestone in this readme. Please refer to the individual HW reports in the [docs](docs/) folder for detailed descriptions of what I had accomplished. 

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

### Game Object Model (HW2)

I built the implementation of this assignment on my last homework assignment code. On a side note, I had already implemented quite a lot of this assignment in the first homework and hence devoted my time to fine tune and optimize my code to remove lags and improve efficiency. Specifically, I extended my Game Object Model from the first assignment a little bit, added death zones and spawn points and implemented a string network protocol to compare with my existing method of transmitting serialized game objects over the network. The game object model used is of monolithic type, i.e. there is one `GameObject` class at the top of the tree and then multiple classes inherit from it depending on the requirements. This is in contrast to having a component model or a property-centric model. The detailed report for this milestone can be found [here](docs/HW2_Report.pdf).

### Events (HW3)

For this one, I had to make a substantial change on how my network architecture was implemented. That being said, I saw some pros and cons to a strictly game object approach or using a event based approach which I have talked about in detail in the actual report [here](docs/HW3_Report.pdf). Previously I was sending game objects across the network and they had to be simply rendered by the client without any other overhead. Though this seems easy, it's a very traffic-heavy approach, which got eliminated once I shifted to events. Now, I was only sending events which represented any change in the clients, and other clients would simply handle them and modify the game objects accordingly. I also put the [Chandy-Misra-Bryant](http://citeseerx.ist.psu.edu/viewdoc/download?doi=10.1.1.158.1073&rep=rep1&type=pdf) algorithm in place to take care of event synchronization. Having the concept of timelines and implementing replays were also a part of this assignment. Replays are possible in three different speeds - slow, normal and fast.  

### Scripting (HW4)

I had to make my game engine capable of handling scripts. As I was using Processing, it made sense to use JavaScript as the scripting language. I built the script manager (inspired by one of Professor's dummy code) and added respective events to handle them. The detailed report can be found [here](docs/HW4_Report.pdf).

### Two games (HW4)

HW4 also had the requirement for building two games. I made the classic arcade game **Space Invaders** and the popular **Bubble Shooter**. Their implementations are fairly basic as they were built in an incredibly short period of time, but they are playable and complete with scoring and deaths. Both of them were built using the game engine that I had built over the semester. Comparisons of their codebases with what I had before gives an insight into how much I had to change for reusing my engine. These are present in the reports for the games, which you can find in the separate repositories dedicated for them [here (Space Invaders)](https://github.com/debalin/space-invaders) and [here (Bubble Shooter)](https://github.com/debalin/bubble-shooter).  

There are tons of other features which can be added to this engine, to make it more usable and flexible. There are also a couple of issues with events not getting exactly replicated on individual client machines, in my current implementation. But I doubt I will work on this project anymore, as this course was designed to merely introduce us to the fundamental concepts of game engines. From here, I can move on to actually using Unity, Unreal, etc. and seeing how they work.   
