Project Title: ChitChat – Multi-Threaded Desktop Chat Application
Project Overview
ChitChat is a real-time, desktop-based messaging system developed using Java socket programming and object-oriented principles. The application relies on a client-server architecture where a centralized server manages incoming connections, authenticates active users, and routes real-time text communications across active clients over a network connection.
Core Object-Oriented Concepts Applied
 * Encapsulation: User credentials, message payloads, and active connection sockets are wrapped in dedicated domain models with private data members and controlled getter/setter access.
 * Inheritance & Interfaces: Shared communication components extend Java base event handlers and extend thread management instances (Runnable interface) to manage concurrent user activity.
 * Polymorphism: Method overriding is utilized across custom UI components and message-processing event listeners to handle varying packet types (e.g., handshake, standard message, disconnect).
 * Abstraction: The underlying networking logic (Socket and ServerSocket operational streams) is abstracted behind clean service layers, decoupling network transport from the front-end interface.
Technical Architecture & Features
 * Multi-Threading: Individual client connections are assigned dedicated server-side threads (ClientHandler), ensuring continuous non-blocking message transport across concurrent sessions.
 * Graphical User Interface: Built using Java Swing/AWT, providing a clean user interface featuring custom panel layouts, styled message containers, and active participant windows.
 * Network Socket Operations: Implements TCP/IP communication protocols utilizing Java InputStream and OutputStream abstractions for reliable message delivery.
System Workflow
 * Server Initialization: The centralized server binds to a specified port and listens continuously for incoming client socket requests.
 * Client Registration: A client application initiates a TCP handshake, sending client credentials to establish a dedicated thread session.
 * Broadcasting & Delivery: Received messages are parsed, logged, and broadcast dynamically to all currently active sessions connected to the server hub.
