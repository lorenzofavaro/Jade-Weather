[![Build Status](https://travis-ci.com/lorenzofavaro/Jade-Weather.svg?token=tPgSzvEjF2gJuArbXTE5&branch=master)](https://travis-ci.com/lorenzofavaro/Jade-Weather)

# Jade-Weather
This project has been created for the exam of Intelligent Agents of the Master's Degree course at the University of Turin.

The project is an example of the `ContractNet` protocol requesting the weather forecast in a chosen city through [WeatherStack](https://weatherstack.com/) and [WeatherApi](https://www.weatherapi.com/).

## Setup
- Clone repository
- If you're using IntelliJ you are good to do, otherwise follow these steps:
  - Create a new run configuration using the template `application` setting `jade.Boot` as main class
  - Set arguments to `-gui -agents "weatherApi:WeatherAgent(0);weatherStack:WeatherAgent(1);asker:AskerAgent(weatherApi, weatherStack)"`

## Contributing
Libraries used:
- [Tilab: JADE](https://jade.tilab.com/)
- [JSON](https://mvnrepository.com/artifact/org.json/json)

## Author
[Lorenzo Favaro](https://github.com/lorenzofavaro)


