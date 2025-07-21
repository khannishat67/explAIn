# explAIn - IntelliJ Platform Plugin

## Overview
explAIn is an IntelliJ Platform plugin built with Kotlin and Java, managed by Gradle. The plugin provides instant explanations of Java code directly in your editor.

## Features
- Kotlin and Java interoperability
- IntelliJ Platform plugin integration
- Explains Java methods and their references under the cursor
- Trigger explanations with the shortcut `Ctrl+Alt+E`
- Uses OpenAI completions for code explanations
- Uses code references for context

## Requirements
- IntelliJ IDEA
- JDK 21 or higher
- Gradle (or use the Gradle Wrapper)
- OpenAI API Key (required to run the plugin)

## Usage

1. Install the plugin in IntelliJ IDEA.
2. Set your OpenAI API Key in the plugin settings.
3. Place your cursor on a Java method or reference.
4. Press `Ctrl+Alt+E` to get an instant explanation.

## Project Structure
- `src/main/kotlin` \- Kotlin source files
- `src/main/java` \- Java source files
- `src/test` \- Unit tests

## Contributing
Pull requests are welcome. For major changes, please open an issue first to discuss what you would like to change.

## License
This project is licensed under the MIT License.
