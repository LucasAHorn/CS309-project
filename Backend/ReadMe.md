# Event App Backend

## Requirements
- **MySQL Server**  
  - Configure according to `backend/src/main/resources/META-INF/application.properties`.  
  - You can edit `application.properties` to match your local setup.
- **Java Application**  
  - Run: `backend/src/main/java/eventAppApplication.java`.

## Building a JAR (for server hosting)
1. Open a command prompt.
2. Navigate to the root of the `backend` directory.
3. Execute:
   ```bash
   mvn clean package

## Main layout
- controller
  - This conatins API calls using restful API
- model
  - This contains data models for temporary or long term use
- repository
  - This specifies what models are saved to the SQL server and any unique methods to get, update, or delete data
- service
  - This contains functions that are called based on the time to move past events into the past 
- util
  - This contains general functions used in multiple files
- websocket
  - This contains all websocket related functions

### SocketHTML
- This contains HTML files for testing sockets in a browser