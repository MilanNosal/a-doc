package tuke.kpi.adoc.interfaces;

import java.util.HashSet;
import java.util.Set;
import javax.lang.model.element.ElementKind;

/**
 * Pre zistenie podporovanych typov.
 * @author Milan
 */
// TODO: pridat parameter, checknut ze ako sa to sprava pri enum constant
public class SupportedAnnotatedTypes {
    public static final Set<ElementKind> supportedKinds;
    
    static {
        supportedKinds = new HashSet<>();
        supportedKinds.add(ElementKind.ANNOTATION_TYPE);
        supportedKinds.add(ElementKind.CLASS);
        supportedKinds.add(ElementKind.INTERFACE);
        supportedKinds.add(ElementKind.ENUM);
        
        supportedKinds.add(ElementKind.CONSTRUCTOR);
        supportedKinds.add(ElementKind.METHOD);
        
        supportedKinds.add(ElementKind.ENUM_CONSTANT);
        supportedKinds.add(ElementKind.FIELD);
    }
}
