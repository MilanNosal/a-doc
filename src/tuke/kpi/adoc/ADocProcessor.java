package tuke.kpi.adoc;

import java.io.File;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedOptions;
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
@SupportedOptions(value = {"javadoc", "templates"})
public class ADocProcessor extends AbstractProcessor {
    
    private static String JAVA_DOC_PATH = null;
    private static String HASKELL_TEMPLATES_PATH = null;
    private HaskellProducer haskellProducer;
    private final DocumentationComposer composer = new SimpleComposer();
    private DocumentationEmitter emitter;
    private final Map<Element, Map<TypeElement, String>> processedDocumentation = new LinkedHashMap<>();
    private final Map<Element, String> compiledDocumentation = new LinkedHashMap<>();

    @Override
    public synchronized void init(ProcessingEnvironment processingEnv) {
        super.init(processingEnv);
        JAVA_DOC_PATH = this.processingEnv.getOptions().get("javadoc");
        HASKELL_TEMPLATES_PATH = this.processingEnv.getOptions().get("templates");
        if (JAVA_DOC_PATH == null || HASKELL_TEMPLATES_PATH == null) {
            RuntimeException t = new RuntimeException("You have to set paths to javadoc and to haskell templates. Use annotation processor parameters with keys 'javadoc' and 'templates'.");
            System.err.println(t.getMessage());
            throw t;
        }
        emitter = new SimpleJavaDocEmitter(new File(JAVA_DOC_PATH));
    }
    
    

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        if (roundEnv.getRootElements().size() > 0) {
            this.haskellProducer = new HaskellProducer();
            this.haskellProducer.init(HASKELL_TEMPLATES_PATH, this.processingEnv, roundEnv);

            // vyberie anotacie oznacene nasim @Documented
            for (TypeElement annotationType : annotations) {
                if (annotationType.getAnnotation(Documented.class) != null) {
                    processAnnotationType(annotationType, roundEnv);
                }
            }
            this.haskellProducer.finish();

            for (Element element : this.processedDocumentation.keySet()) {
                String compiled = this.composer.compose(element, this.processedDocumentation.get(element));
                this.compiledDocumentation.put(element, compiled);
            }

            for (Element element : this.compiledDocumentation.keySet()) {
                System.out.println(">>  " + element);
                System.out.println(">>> " + this.compiledDocumentation.get(element));
                this.emitter.emitDocumentationFor(element, this.compiledDocumentation.get(element));
            }
        }
        return false;
    }

    private void processAnnotationType(TypeElement annotationType, RoundEnvironment roundEnv) {
        // cas nastavit typ anotacii
        if (this.haskellProducer.setAnnotationType(annotationType) == true) {
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
                        String generated = this.haskellProducer.produceDocFor(mirror, element);
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
