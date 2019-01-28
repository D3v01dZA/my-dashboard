Installation Notes:

Install Java JDK 8+
Install Gradle
Install Postgres

From here on refer to "service/src/main/resources/application-local.properties" for items you need to configure

Create Schema
Run "CREATE EXTENSION 'pgcrypto';" on that Schema

Start the server using "gradle bootRun" in its directory or creating a boot jar and running it