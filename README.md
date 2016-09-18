## CSC591 (Game Engine Foundations) Homework 1: Network Foundations

### Homework Spec 

The homework spec can be found in `[root_dir]\docs\HW1.pdf`. Below is a brief excerpt from the same: 

Your task for this assignment is to explore the basics of constructing a multi-threaded network server. This
will be the first part of your game engine that you will develop throughout the course of the semester. As
part of the process, you will familiarize yourself with the Processing environment, which you will also use
for the rest of the semester.

### Introduction

In this assignment, I had an introduction to the basic concepts of a game engine. Though we were building a multi-threaded server/client architecture, I paid enough attention to make other parts of my engine as generic as possible.
 I made my engine as a library which can be used and extended as and when necessary. I will be describing the individual parts of my engine
  as I go forward explaining each section of this assignment. Also, I will mention all my engine components in a 
  separate list with a brief description of each of them.
   
### Running my program

There are two ways to run my program:

1. JAR:
    1. Find the JAR file for this project in `[root_dir]\out\artifacts\CSC591_GE_HW1.jar\CSC591_GE_HW1.jar`.
    2. Open a command line and type `java -jar CSC591_GE_HW1.jar s` (for server).
    3. For running clients, type and execute `java -jar CSC591_GE_HW1.jar c` as many times you want for any number of clients.
    4. Remember that you need the run the server first and then the clients, otherwise this might throw some exception. 
     Also currently, the client searches for a running server in `localhost`, so running the server and client in different computers will not work. If you still want to run it in different computers,
      follow my second way of running the program and before building it, open `Constants.java` and assign the server's IP to the `SERVER_ADDRESS` String variable.
2. IntelliJ:
    1. Install [IntelliJ Community Edition](https://www.jetbrains.com/idea/download/#section=windows).
    2. Import and build my project.
    3. There should be two run configurations - one for the server and one for the client. Run the "Server" first and then the "Client". The shortcut for running programs in IntelliJ is `Alt + Shift + F10`.
    4. If you don't find the run configurations, make two yourself. For the sever, give a command line argument of "s" and for the client, give a command line argument of "c" (without the quotes).  
 

