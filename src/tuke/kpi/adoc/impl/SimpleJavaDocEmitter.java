package tuke.kpi.adoc.impl;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import javax.lang.model.element.Element;
import static javax.lang.model.element.ElementKind.CLASS;
import static javax.lang.model.element.ElementKind.CONSTRUCTOR;
import static javax.lang.model.element.ElementKind.ENUM_CONSTANT;
import static javax.lang.model.element.ElementKind.FIELD;
import static javax.lang.model.element.ElementKind.METHOD;
import javax.lang.model.element.TypeElement;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import tuke.kpi.adoc.interfaces.DocumentationEmitter;

/**
 *
 * @author Milan
 */
public class SimpleJavaDocEmitter implements DocumentationEmitter {

    private File javadocDir;

    public SimpleJavaDocEmitter(File javadocDir) {
        this.javadocDir = javadocDir;
    }
    
    /**
     * Tato metoda doplni cast dokumentacie pre dany element.
     * @param element
     * @param documentation 
     */
    @Override
    public void emitDocumentationFor(Element element, String documentation) {
        try {
            File file = JavaDocUtilities.openFileForElement(element, SimpleJavaDocEmitter.this.javadocDir);
            Document doc = Jsoup.parse(file, "UTF-8");
            org.jsoup.nodes.Element block = null;

            switch (element.getKind()) {
                case CLASS:
                case INTERFACE:
                case ENUM:
                case ANNOTATION_TYPE:
                    block = this.selectTargetForType(doc, (TypeElement) element);
                    break;
                case METHOD:
                case CONSTRUCTOR:
                case ENUM_CONSTANT:
                case FIELD:
                    block = this.selectTargetForMember(doc, element);
                    break;
                default:
                    System.err.println("Unsupported type yet.");
                    return;
            }

            // po predchadzajucom switchi by nemalo byt null
            Elements gene = block.select("div.generatedADoc");

            if (gene.isEmpty()) {
                block.append("\n<div class=\"generatedADoc\">" + documentation + "</div>");
            } else {
                gene.first().html(documentation);
            }
            try (PrintWriter writer = new PrintWriter(file, "UTF-8")) {
                writer.write(doc.html());
                writer.flush();
            }
        } catch (FileNotFoundException ex) {
            throw new RuntimeException("File for " + element.toString() + " does not exist!");
        } catch (IOException ex) {
            throw new RuntimeException("Problem reading " + element.toString() + "!", ex);
        }
    }

    public org.jsoup.nodes.Element selectTargetForType(Document doc, TypeElement type) {
        org.jsoup.nodes.Element desc = doc.select("div.description").first();
        org.jsoup.nodes.Element block = desc.select("div.block").first();
        return block;
    }

    public org.jsoup.nodes.Element selectTargetForMember(Document doc, Element member) {
        org.jsoup.nodes.Element list = doc.select("li.blockList > a[name="
                + getDescription(member)
                + "_detail]").first().parent();
        String selector = "a[name=" + member.toString().replace(",", ", ") + "] + ul[class*=blockList]";
        org.jsoup.nodes.Element ul = list.select(selector).first();
        org.jsoup.nodes.Element block = ul.select("div.block").first();
        return block;
    }
 
    private String getDescription(Element element) {
        return element.getKind().toString().toLowerCase(); 
//        switch (element.getKind()) {
//            case FIELD:
//                return "field";
//            case ENUM_CONSTANT:
//                return "enum_constant";
//            case METHOD:
//                return "method";
//            case CONSTRUCTOR:
//                return "constructor";
//            default:
//                throw new RuntimeException("Unsupported type in SimpleJavaDocEmitter.getDescription(Element element)!");
//        }
    }
}
