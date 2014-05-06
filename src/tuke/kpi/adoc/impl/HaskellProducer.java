package tuke.kpi.adoc.impl;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.AnnotationValue;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import tuke.kpi.adoc.interfaces.DocumentationProducer;

/**
 * Lifecycle: create init (nastavi a spusti proces) { setType (nastavi prave
 * spracovavany typ) { instantiateFor (spracuje jednu anotaciu nastaveneho typu)
 * }* }* finish
 *
 * TODO: urobit aj xxxx nieco najviac
 * @author Milan
 */
public class HaskellProducer implements DocumentationProducer {
    
    // TODO: trebalo by prejst tuto triedu a psotarat sa o cleanup poriadne

    private HaskellUtilities utilities;
    private ProcessingEnvironment procEnv;
    private RoundEnvironment roundEnv;
    private PrintStream ghciInput;
    private BufferedReader ghciOutput;
    private BufferedReader ghciErrorOutput;
    private Process ghciProcess;
    private TypeElement currentType;
    private File pathToTemplates;

    // initialization
    public void init(String pathToTemplates, ProcessingEnvironment procEnv, RoundEnvironment roundEnv) {
        try {
            this.pathToTemplates = new File(pathToTemplates);
            this.procEnv = procEnv;
            this.roundEnv = roundEnv;
            this.utilities = new HaskellUtilities(procEnv);
            List<String> command = new ArrayList<>();
            command.add("ghci");

            ProcessBuilder builder = new ProcessBuilder(command);

            builder.directory(this.pathToTemplates);

            ghciProcess = builder.start();
            InputStream in = ghciProcess.getInputStream();
            OutputStream out = ghciProcess.getOutputStream();
            InputStream err = ghciProcess.getErrorStream();

            ghciInput = new PrintStream(out);
            ghciOutput = new BufferedReader(new InputStreamReader(in));
            ghciErrorOutput = new BufferedReader(new InputStreamReader(err));

            System.out.println("====================== INITIALIZING =======================");
            String output;
            for (int i = 0; i < 4; i++) {
                output = ghciOutput.readLine();
                System.out.println("GHCi output:: " + output);
            }

            System.out.println("===========================================================");
        } catch (Exception exc) {
            exc.printStackTrace();
        }
    }

    // setting type
    public boolean setAnnotationType(TypeElement type) {
        String name = type.getSimpleName().toString().toLowerCase();

        try {
            if (!haskellFileExists(name)) {
                PrintStream ps = new PrintStream(new File(this.pathToTemplates, name + ".hs"));
                ps.print(this.utilities.generateHaskellPrototype(type, roundEnv));
                ps.flush();
                ps.close();
                return false;
            }
            if (type.equals(currentType)) {
                return true;
            }
            currentType = type;

            ghciInput.println(":load " + name);
            ghciInput.flush();

            String output = "";
            for (int i = 0; i < 2; i++) {
                output = ghciOutput.readLine();
                System.out.println("GHCi output:: " + output);
            }
            if (!output.contains("Ok, modules loaded")) {
                cleanup();
                throw new RuntimeException("Error while loading a " + name + ".hs, haskell file is probably not OK.");
            }
            return true;
        } catch (IOException | InterruptedException ex) {
            ex.printStackTrace();
            return false;
        }
    }

    @Override
    public String produceDocFor(AnnotationMirror mirror, Element element) {
        if (currentType == null) {
            throw new RuntimeException("Current type is not set!");
        }
        try {
            if (!mirror.getAnnotationType().asElement().equals(currentType)) {
                throw new RuntimeException("Type of mirror is not the same as current type! "
                        + "Current: " + currentType.toString()
                        + "; mirror: " + mirror.getAnnotationType().asElement().toString());
            }
            String name = currentType.getSimpleName().toString().toLowerCase();
            StringBuilder command = new StringBuilder(name);

            // toto je string pre nazov oanotovaneho elementu
            command.append(String.format(" \"%s\"", element.toString().replace(",", ", ")));

            Map<? extends ExecutableElement, ? extends AnnotationValue> actualParameters = procEnv.getElementUtils().getElementValuesWithDefaults(mirror);

            AnnotationValue annotationValue;
            for (Element annotationParameter : this.currentType.getEnclosedElements()) {
                if (annotationParameter.getKind() != ElementKind.METHOD) {
                    System.err.println("Member " + annotationParameter + " in annotation type " + name + " is not a method.");
                    continue;
                }
                ExecutableElement parameter = (ExecutableElement) annotationParameter;
                annotationValue = actualParameters.get(parameter);
                command.append(" ").append(this.utilities.getValue(annotationValue.getValue(), parameter.getReturnType()));
            }

            ghciInput.println(command.toString());
            ghciInput.flush();
            
            String output = ghciOutput.readLine();
            System.out.println("GHCi output:: " + output);
            if (output.startsWith("*Main> ")) {
                String result = output.substring(7).trim();
                // orezuje uvodzovky
                result = result.substring(1, result.length() - 1);
                return result;
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        throw new RuntimeException("Some troubles with reading output for " + mirror.toString());
    }

    // uzaver
    public void finish() {
        try {
            ghciInput.println(":quit");
            ghciInput.flush();

            // tu koncim so zadavanim prikazov a idem spracovat vystup
            System.out.println("======================= FINISHING =========================");
            String output = ghciOutput.readLine();
            while (output != null) {
                System.out.println("GHCi output:: " + output);
                output = ghciOutput.readLine();
            }

            output = ghciErrorOutput.readLine();
            while (output != null) {
                System.err.println("GHCi output:: " + output);
                output = ghciErrorOutput.readLine();
            }

            cleanup();
            
            System.out.println("===========================================================\n");

            //p.destroy();
        } catch (Exception exc) {
            exc.printStackTrace();
        }
    }

    private void cleanup() throws IOException, InterruptedException {
        ghciInput.close();
        ghciOutput.close();
        ghciErrorOutput.close();
        ghciProcess.waitFor();
    }

    private boolean haskellFileExists(String filename) {
        File[] files = this.pathToTemplates.listFiles();
        for (File file : files) {
            if (file.getName().equalsIgnoreCase(filename + ".hs")) {
                return true;
            }
        }
        return false;
    }
}
