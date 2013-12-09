# Data Monitoring Service

A cron job (written in **Java**) which monitors data in database (**MongoDB**) by ensuring there is data input from time to time.

If there's no data input, an email notification will be sent to the specified recipient(s).

## Why

Assuming you have a service which pumps data into database consistently, and need to make sure that service runs perfectly without checking the database manually. You need someone to do that for you. So Data Monitoring Service come to the rescue.

## How

Monitors data in an interval time defined in configuration, and send email notification when the state changes.

Consists of two states:
 * **Data Online State** monitor and sends notification email when there is no data comes in.
 * **Data Offline State** monitor and sends notification email when data comes in.

## Configuration (config.txt)

###### Mongo Configuration
 * **MONGO_HOST** database server
 * **MONGO_PORT** database port
 * **MONGO_DB** database db
 * **MONGO_COLLECTION** database collection
 * **MONGO_DATE_KEY** the key of the datetime stored in collection

###### Mail Configuration
 * **MAIL_HOST** mail server
 * **MAIL_PROTOCOL** mail protocol, either TLS or SSL. default is SSL
 * **MAIL_USER** user who sends email notification to recipient(s)
 * **MAIL_PASS** user password
 * **MAIL_RECIPIENT** recipients who receive email notification when state change. multiple recipient is allowed, using comma as separator

###### Mail Content
 * **MAIL_SUBJECT_OFF** subject when data goes offline
 * **MAIL_CONTENT_OFF** content when data goes offline. the first `%s` found in value will be replaced by time in `yyyy-MM-dd HH:mm:ss zzz` format
 * **MAIL_SUBJECT_ON** subject when data goes online
 * **MAIL_CONTENT_ON** content when data goes online. the first `%s` found in value will be replaced by time in `yyyy-MM-dd HH:mm:ss zzz` format

###### Service Configuration
 * **RANGE** range of time (in minutes) to check for expected amount of data comes in
 * **EXPECTED_COUNT** minimum amount of data record should exist within the time range
 * **INTERVAL** thread sleep interval (in milliseconds) before next execution

## Setup

###### Clone this repository
```
git clone https://github.com/zynick/DataMonitoringService.git
```

###### Maven build
```
mvn clean dependency:copy-dependencies package
```

###### Get the following files and put into a new directory
* config.txt
* target/data-monitor-1.0.0.jar
* target/*

###### Configuration (config.txt)
change accordingly to your environment preference.

###### Execute
```
java -jar data-monitor-1.0.0.jar config.txt
```
