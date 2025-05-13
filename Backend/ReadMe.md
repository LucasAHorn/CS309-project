# BackEnd

To run:

- Run src/main/java/com.springboot.EventApp/EventAppApplication

Notes:
- any file in src/main/java/com.springboot.EventApp directory will run by server


# Standard users currently have:

- Username (unique)
- Password
- BirthCity
- firstPetName
  note: password, BirthCity, and firstPetName are stored as a hash
- eventIDsRSVPd
- EventGroupIDs

# To connect with filezilla
The jar file for springbok is running from the /home directory

- sftp://coms-3090-009.class.las.iastate.edu
- username
- password
- leave port blank

# To connect to server

server runs on:
- http://coms-3090-009.class.las.iastate.edu:8080/
  - This should replace 'localhost:8080/' in applications


Database management
Open terminal:
- ssh your-netid@coms-3090-009.class.las.iastate.edu
    - ex: ssh luhorn@coms-3090-009.class.las.iastate.edu
- fingerprinting: yes
- user same password as netID
- mysql -u username-here -p
    - ex: mysql -u sharedUser -p
    - password: 123

# MariaDB Credentials:

- username: sharedUser
- password: 123

# Change Server

go into application.properties and change to below or similar:

- FOR REMOTE SERVER: - should always be online (use vpn when off campus) <br>
  spring.application.username=Backend_RT1 <br>
  spring.jpa.hibernate.ddl-auto=update <br>
  spring.datasource.url=jdbc:mysql://coms-3090-009.class.las.iastate.edu:3306/RT1 <br>
  spring.datasource.username=sharedUser <br>
  spring.datasource.password=123 <br>
  spring.datasource.driver-class-username=com.mysql.cj.jdbc.Driver <br>
  spring.jpa.show-sql: true <br>
  <br>
- FOR LOCAL SERVER: - start through command line with admin: net start mysql80 <br>
  spring.application.username=Backend_RT1 <br>
  spring.jpa.hibernate.ddl-auto=update <br>
  spring.datasource.url=jdbc:mysql://localhost:3306/RT1 <br>
  spring.datasource.username=lucash <br>
  spring.datasource.password=<FillMeOut> <br>
  spring.datasource.driver-class-username=com.mysql.cj.jdbc.Driver <br>
  spring.jpa.show-sql: true

# Make JAR File
- open command prompt
- navigate to root of backend
- mvn clean package
  - (this will clean and create a jar file that is complete with the dependencies)