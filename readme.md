# RETICULA Package
REal TIme Code qUalLity Assessment
--

### Description
This package contains the components to reproduce a working copy of my Bachelor Thesis.

Advisor: Professor Gabriele Bavota

## Structure

- **CK Metrics Calculator**: Assesses a project code metrics
- **Java Project Analyzer**: Analyzes Java Open Source Systems and store their result in a PostgreSQL database
- **RETICULA Plug-in**: IntelliJ Plugin to assess a  project's code quality as compared to OSS in real time 
- **WebServer**: Behave as a proxy between the plug-in and the OSS's metrics database.
- **gits.json**: JSON File containing a list of over 2M Java Repositories

## How to use it
*You will find more detailed instructions into each folder's component.*

#### Minimal setup
*Dependencies: IntelliJ*

Install RETICULA Plug-in in IntelliJ 

#### Complete setup
*Dependencies: Scala, sbt, Java and IntelliJ*

1. Install PostgreSQL
2. Run the JPA enough time to have a decent amount of OSS analyzed in the database
3. Run the WebServer
4. Install RETICULA Plug-in
5. Change the RETICULA's settings in IntelliJ to use your WebServer.

 