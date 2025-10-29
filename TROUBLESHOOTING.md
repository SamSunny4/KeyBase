# Troubleshooting Guide

If you encounter issues while building or running KeyBase, please check the following common solutions:

## Database Issues

### "Column 'date_added' not found" Error

If you see errors related to the date_added column:

1. Make sure you're running the latest version of the initialization script
2. Delete any existing database files in the `data` directory
3. Run the application again to recreate the database

### Missing Database Files

If the application can't find the database:

1. Verify the `data` directory exists in the same location as the JAR file
2. Check the database.properties file has the correct path:
   ```
   db.url=jdbc:h2:./data/keybase;AUTO_SERVER=TRUE
   ```
3. Try running the DatabaseInitializer manually:
   ```
   java -cp "lib/*;KeyBase.jar" src.DatabaseInitializer
   ```

## Compilation Errors

### "Cannot find symbol" Errors

If you see errors about missing symbols:

1. Make sure all required JAR files are in the lib directory
2. Verify you're compiling with the correct classpath:
   ```
   javac -cp "lib/*" -d build/classes src/*.java
   ```

### "Class not found" Errors

If the application can't find a class:

1. Check that all classes are compiled into build/classes
2. Verify the JAR file contains all necessary classes:
   ```
   jar tf dist/KeyBase.jar
   ```

## Runtime Errors

### "Image capture failed" Errors

If webcam capture isn't working:

1. Verify your webcam is connected and working
2. Check that the webcam-capture libraries are properly included
3. Try using a lower resolution for capture

### UI Component Errors

If UI components aren't showing correctly:

1. Make sure you're using a compatible Java version (Java 8 or higher)
2. Check for errors in the console that might indicate missing UI components
3. Try using the system look and feel instead of the default

## Quick Fixes

### Reset Database

To completely reset the database:

1. Delete all files in the `data` directory
2. Run the application again to recreate the database

### Rebuild from Scratch

For a clean rebuild:

1. Delete the `build` and `dist` directories
2. Run the build script again:
   ```
   .\build.bat    # On Windows
   ./build.sh     # On macOS/Linux
   ```

### Debug Mode

To run in debug mode with more logging:

1. Add the `-Ddebug=true` flag when running:
   ```
   java -Ddebug=true -cp "lib/*;KeyBase.jar" src.MainForm
   ```