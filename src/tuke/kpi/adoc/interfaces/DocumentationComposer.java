package tuke.kpi.adoc.interfaces;

import java.util.Map;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;

/**
 *
 * @author Milan
 */
public interface DocumentationComposer {
    public String compose(Element element, Map<TypeElement, String> docs);
}
