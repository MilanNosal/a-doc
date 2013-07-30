package tuke.kpi.adoc.interfaces;

import javax.lang.model.element.Element;

/**
 *
 * @author Milan
 */
public interface DocumentationEmitter {
    public void emitDocumentationFor(Element element, String documentation);
}
