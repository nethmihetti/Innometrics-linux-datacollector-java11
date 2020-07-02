# Innometrics-linux-datacollector-java11

## How to Run the project
1. File -> Project Structure -> Libraries
2. Click `+` and select `java`
3. Give the path the the java fx library `lib` folder
2. Run -> Edit configurations
3. Under VM options add the following command:
`--module-path %PATH_TO_FX_lib% --add-modules javafx.controls,javafx.fxml`.
4. Run `src/sample/Main`
