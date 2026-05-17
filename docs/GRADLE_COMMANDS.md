# Gradle Commands Reference

This project uses the Gradle wrapper (`gradlew`). Using `./gradlew` ensures that you are using the correct version of Gradle for this project.

Here are some of the most common and useful commands you can use in this project:

### Building and Running
* **`./gradlew build`**
  Builds the project. This compiles the code, runs the tests, and packages the application into an executable JAR file.

* **`./gradlew bootRun`**
  Runs the Spring Boot application locally. It will compile your code and start the embedded web server.

* **`./gradlew clean`**
  Deletes the build directory (`build/`), removing all generated files. This is useful if you want to ensure a completely fresh build.

* **`./gradlew assemble`**
  Compiles the Java code and creates the JAR file without running the tests.

### Testing
* **`./gradlew test`**
  Executes the unit tests. Results are usually output to `build/reports/tests/test/index.html`.

* **`./gradlew test --tests "*ClassNameTest"`**
  Runs a specific test class (replace `ClassNameTest` with the name of your test class). e.g., `./gradlew test --tests "*StatisticsServiceImplTest"`.

### Dependencies & Information
* **`./gradlew dependencies`**
  Displays a tree of all the project dependencies, including transitive dependencies. Very helpful for resolving version conflicts.

* **`./gradlew tasks`**
  Displays a list of all available tasks you can run in this project.

* **`./gradlew help`**
  Displays the generic Gradle help message.

### Formatting & Code Quality
*(If applicable/configured in your project)*
* **`./gradlew check`**
  Runs all verification tasks, including tests and any configured linters/static code analysis tools (like Checkstyle, PMD, SpotBugs).
