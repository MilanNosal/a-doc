Prerequisites:
GHCi haskell compiler installed and the link to the GHCi compiler added to the PATH environment variable. The short ghci command has to be runnable from the command line.
Java 1.5+ JDK.
NetBeans IDE for easier testing.
ADoc NetBeans project or its jar file (from the https://code.google.com/p/a-doc/ project web).

Steps for running ADoc:

1. Add the ADoc project (or .jar file) to the classpath of the testing project.
In NetBeans IDE right-click 'Libraries' in the testing project, choose 'Add Project' or 'Add JAR/Folder' and select the ADoc project or the ADoc.jar, respectively. This will register the ADoc processor to your project.

2. Use the @tuke.kpi.adoc.Documented annotation to annotated annotation types that should be documented using the ADoc tool. These annotations we will address from now as phrase annotations.

E.g.:
            @tuke.kpi.adoc.Documented
            public @interface Deprecated {
               String version();
               String replacement();
            }

3. Choose a directory where the templates for the phrase annotations will be. For each phrase annotation type create a haskell template with the same name in lowercase. Haskell function returns String value and takes as parameters String name of the annotated element (e.g. a class annotated with the @Deprecated annotation), and the parameters of the phrase annotation in the order they are written in the annotation type.

E.g. for the @Deprecated there will be "deprecated.hs" file with the following content:
            deprecated :: String -> String -> String -> String
            deprecated annotatedElement version replacement = "Element " ++ annotatedElement ++ " is deprecated since " ++ version ++ ". Use " ++ replacement ++ " instead."

Prototype of a haskell template can be automatically generated by the ADoc tool (step 6), if you run the tool while the template files are non-existent. Then you can modify the prototype to fulfill your needs.

4. Use phrase annotations everywhere where it is needed. Annotated elements have to have some JavaDoc documentation, to which the documentation phrase will be appended.

E.g.:
            /**
             * Interesting class.
             * @author Milan
             */
            @main.annotations.Deprecated(version = "1.1", replacement = "main.Bar")
            public class Foo {
            ...

5. Use JavaDoc tool to generate standard JavaDoc documentation.
In NetBeans right-click the project and select 'Generate Javadoc', the documentation will be generated to the dist directory of the project. Since the dist directory is deleted on each 'clean and build', move the javadoc directory to a safe location (e.g., to the project root directory, or src).
Now you have the standard JavaDoc documentation prepared and you can enhance it with the ADoc tool.

6. Run the javac with the ADocProcessor annotation processor from the ADoc tool. Pass the annotation processor parameters with keys 'javadoc' and 'templates' that are paths to javadoc directory and the directory with the templates, respectively.
You can do it from the command line, but if you are using the NetBeans IDE, by adding it to the Libraries the annotation processor should be run each time you compile the project (we suggest using 'clean and build' option).
To make sure NetBeans runs the annotation processor, right-click on the project and select "Properties". In 'Build'->'Compiling' check the 'Enable Annotation Processing', if it is not checked.
In the same form, add new processor options: an option with 'javadoc' key with a value of a path to javadoc directory (e.g., "C:\projects\adoc-test\javadoc"), and an option with key 'templates' with a value pointing to the templates directory (from step 3, e.g., "C:\projects\adoc-test\templates").
Now running 'clean and build' upon the testing project will run the ADoc processor and generate the documentation.
