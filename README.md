

# AI SQL Generator Project

## Overview

This project is a Spring Boot application that generates and executes SQL queries based on human-readable input. 
It uses OpenAI's API to interpret the input and generate the corresponding SQL query.

## Features

- Generate SQL queries from human-readable questions.
- Execute the generated SQL queries.
- Log the results of the executed queries.

## Technologies Used

- Java
- Spring Boot
- Maven
- SQLite
- OpenAI API

## Prerequisites

- Java 11 or higher
- Maven
- SQLite

## Getting Started

### Clone the Repository

```sh
git clone https://github.com/madhankumar-anandan/sqlproject.git
cd sqlproject
```

### Configuration

1. Update the `application.properties` file with your OpenAI API key:

```ini
spring.ai.openai.api-key=<YOUR_API_KEY>
```

2. Ensure the SQLite database file path is correct in the `application.properties` file:

```ini
spring.datasource.url=jdbc:sqlite:/path/to/your/olist.sqlite
```
You can download the `olist.sqlite` database from the following link:
https://www.kaggle.com/datasets/terencicp/e-commerce-dataset-by-olist-as-an-sqlite-database/data

### Build and Run the Application

```sh
mvn clean install
mvn spring-boot:run
```

The application will start on port 8086 by default.

## Usage

To generate and execute an SQL query, send a POST request to the appropriate endpoint with the human-readable query. The application will return the generated SQL query and the results of its execution.
Postman collection is available in the repository to test the application. 

## Acknowledgements

- OpenAI for providing the API used to generate SQL queries.
- Spring Boot for the application framework.
- SQLite for the database.


