# db-differ
A java program that runs like a script and writes a report detailing what files
are missing, corrupted, or new, after a database migration.

## Assumptions
Both databases have been started prior to the program running.
No one is modifying either database while the program is running.

## Usage
You'll need to have java 11 installed in order to run the program. If you use a chocolatey or
homebrew, you can use one of the following commands to install it. 

chocolatey:

    ```choco install openjdk11```

homebrew:
    
    ```brew install openjdk@11```

Once you have java installed, clone the git repo, navigate to the root directory (db-differ)
and run `./mvnw spring-boot:run`. A report of the missing, corrupted, and new database entries
will be written to the directory in which the program ran as `report.txt`. Building the program
for the first time will take a while since the 
maven wrapper downloads and installs maven in the `${user.home}/.m2/wrapper/dists` directory.

You can also run the tests with `./mvnw test`.

## Additional Notes
I didn't attempt to optimize since the instructions said not to spend a lot of time on that.
The program takes about ten seconds to run on the eighty thousand or so files in each database
and appears to run in O(n^3) time. So it wouldn't be very useful on a real world DB with tens
of millions of entries or more. 

I also left out some clean up I would usually do for a PR, such as javadoc, pom cleanup, and unit tests,
because it didn't seem necessary given the use case of the program. It doesn't use spring anymore,
aside from the jdbcTemplate, but I left it as a spring application because it seems good enough. 