package tuke.kpi.adoc.impl;

import java.io.File;
import java.util.HashSet;
import java.util.Set;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.TypeElement;
import tuke.kpi.adoc.interfaces.SupportedAnnotatedTypes;

/**
 * Hlada subory pre html v javadocu.
 * @author Milan
 */
public abstract class JavaDocUtilities {
    
    public static File openFileForElement(Element element, File directory) {
        if(element == null) {
            throw new NullPointerException();
        }
        if(element instanceof TypeElement) {
            return openFileForClass((TypeElement) element, directory);
        }
        Element parent = element.getEnclosingElement();
        while (!(parent instanceof TypeElement)) {
            parent = parent.getEnclosingElement();
        }
        return openFileForClass((TypeElement) parent, directory);
    }

    private static File openFileForClass(TypeElement element, File directory) {
        String filename = findFileForClass(element);
        File file = new File(directory, filename);
        if (!file.exists()) {
            throw new RuntimeException("File " + file.getAbsolutePath() + " does not exist!");
        }
        return file;
    }

    private static String findFileForClass(TypeElement element) {
        Element parent = element.getEnclosingElement();
        // takto najdem nazov suboru pre triedu
        String roadSoFar = element.getSimpleName().toString() + ".html";
        while (parent != null && parent.getKind() != ElementKind.PACKAGE) {
            roadSoFar = parent.getSimpleName().toString() + "." + roadSoFar;
            parent = parent.getEnclosingElement();
        }
        String relPath = (parent == null) ? "" : parent.toString();

        return relPath.replace('.', '/') + "/" + roadSoFar;
    }
}
