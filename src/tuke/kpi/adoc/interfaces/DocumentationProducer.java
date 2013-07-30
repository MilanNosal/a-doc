package tuke.kpi.adoc.interfaces;

import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.Element;

/**
 *
 * @author Milan
 */
//DocumentationProducer -> DocumentationComposer -> DocumentationEmitter
public interface DocumentationProducer {
    public String produceDocFor(AnnotationMirror mirror, Element element);
}
