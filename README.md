# Game API

## Table of Contents

- [Project Overview](#project-overview)
- [Game Rules](#game-rules)
- [Getting Started](#getting-started)
  - [Prerequisites](#prerequisites)
  - [Installation](#installation)
- [Usage](#usage)
- [API Documentation](#api-documentation)
- [Testing](#testing)

## Project Overview

This project is a backend logic for a simple game written in Java. For communication is used WebSocket. 

## Game Rules

- Player is sending a bet and whole number from 1 to 100 to a server
- Server generates random whole number from 1 to 100, and if the player's number is greater, calculates win and sends it back to the client.
- Win depends on chance: <i>bet * (99 / (100 - number))</i>, as an example, if player selected the number 50 and bet 40.5, the win would be 80.19. Exception: if the player chose 100 as the number, the win is calculated as: <i>bet * 99</i>.

## Getting Started

These instructions will help you get a copy of the project up and running on your local machine for development and testing purposes.

### Prerequisites

To run this project, you need to have the following software installed on your machine:

- Java Development Kit (JDK) 20 or later

### Installation

1. Clone the repository to your local machine:

   ```bash
   git clone https://github.com/erlemaido/game-backend.git

2. Navigate to the project directory:

   ```bash
   cd game-backend

3. Install project dependencies using Maven:

   ```bash
   ./mvnw install

## Usage

1. Run the application using Maven:

   ```bash
   ./mvnw spring-boot:run

2. Run the project from IntelliJ:

   Run the ```GameBackendApplication.java``` file in ```src/main/java/erle/assignment/game/GameBackendApplication.java```
   

3. Once the application is running, you can access the API endpoint. Since the code is using WebSocket for communication, WebSocket client is needed to interact with a WebSocket endpoint. Here's a simple client application written in Javascript to simulate the request: https://github.com/erlemaido/websocket-client 

## API Documentation

1. Example request:

   ```
   {
    "bet": 40.5,
    "number": 50
   }

2. Example response:

    ```
    {
     "winningAmount": 80.19
    }

In the example above, the client is making a request to play the game. The ```bet``` field represents the amount the player is betting, and the ```number``` field represents the player's chosen number. The server then responds with the result of the game, which is a ```BigDecimal``` value representing the player's winnings. In this case, the response is ```80.19```, indicating that the player has won and their winnings amount to 80.19.
    

## Testing

To run the tests for this project, execute the following command:

   ```bash
   ./mvnw test
