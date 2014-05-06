package tuke.kpi.adoc;

import java.io.File;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import tuke.kpi.adoc.impl.HaskellProducer;
import tuke.kpi.adoc.impl.SimpleComposer;
import tuke.kpi.adoc.impl.SimpleJavaDocEmitter;
import tuke.kpi.adoc.interfaces.DocumentationComposer;
import tuke.kpi.adoc.interfaces.DocumentationEmitter;
import tuke.kpi.adoc.interfaces.SupportedAnnotatedTypes;

@SupportedSourceVersion(SourceVersion.RELEASE_6)
@SupportedAnnotationTypes(value={"*"})
public class ADocProcessor extends AbstractProcessor {
    
    private static final String JAVA_DOC_PATH = "C:\\Users\\Milan\\Documents\\NetBeansProjects\\ADocTest\\javadoc";
    private static final String HASKELL_PATH = "C:\\Users\\Milan\\Documents\\NetBeansProjects\\ADocTest\\src";
    private HaskellProducer haskellExec;
    private DocumentationComposer comp = new SimpleComposer();
    private DocumentationEmitter emit = new SimpleJavaDocEmitter(new File(JAVA_DOC_PATH));
    private Map<Element, Map<TypeElement, String>> processedDocumentation = new LinkedHashMap<>();
    private Map<Element, String> compiledDocumentation = new LinkedHashMap<>();

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        if (roundEnv.getRootElements().size() > 0) {
            this.haskellExec = new HaskellProducer();
            this.haskellExec.init(HASKELL_PATH, this.processingEnv, roundEnv);

            // vyberie anotacie oznacene nasim @Documented
            for (TypeElement annotationType : annotations) {
                if (annotationType.getAnnotation(Documented.class) != null) {
                    processAnnotationType(annotationType, roundEnv);
                }
            }
            this.haskellExec.finish();
//
            for (Element element : this.processedDocumentation.keySet()) {
                String compiled = this.comp.compose(element, this.processedDocumentation.get(element));
                this.compiledDocumentation.put(element, compiled);
            }

            for (Element element : this.compiledDocumentation.keySet()) {
                System.out.println(">>  " + element);
                System.out.println(">>> " + this.compiledDocumentation.get(element));
                this.emit.emitDocumentationFor(element, this.compiledDocumentation.get(element));
            }
        }
        return false;
    }

    private void processAnnotationType(TypeElement annotationType, RoundEnvironment roundEnv) {
        // cas nastavit typ anotacii
        if (this.haskellExec.setAnnotationType(annotationType) == true) {
            Set<? extends Element> annotatedElements = roundEnv.getElementsAnnotatedWith(annotationType);

            for (Element element : annotatedElements) {
                if(!SupportedAnnotatedTypes.supportedKinds.contains(element.getKind())) {
                    System.out.println("Found element " + element.toString() + " annotated by annotation "
                            + annotationType.getQualifiedName().toString() + " that is supposed "
                            + "to be documented. However, this type of element is not "
                            + "supported and is skipped in processing.");
                    continue;
                }
                for (AnnotationMirror mirror : element.getAnnotationMirrors()) {
                    if (annotationType.equals(mirror.getAnnotationType().asElement())) {
                        String generated = this.haskellExec.produceDocFor(mirror, element);
                        if (this.processedDocumentation.containsKey(element)) {
                            this.processedDocumentation.get(element).put(annotationType, generated);
                        } else {
                            Map<TypeElement, String> mapOfDocumentations = new HashMap<>();
                            mapOfDocumentations.put(annotationType, generated);
                            this.processedDocumentation.put(element, mapOfDocumentations);
                        }
                        // bude sa pokracovat v prechadzani dalsich elementov, tento
                        // je zvladnuty
                        break;
                    }
                }
                //throw new RuntimeException("Nenasla sa anotacia kde mala byt.");
            }
        } else {
            System.out.println("Generating .hs template for annotation type " + annotationType.getQualifiedName().toString()
                    + ".");
        }
    }
}
