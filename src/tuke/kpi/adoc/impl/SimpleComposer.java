package tuke.kpi.adoc.impl;

import java.util.Map;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import tuke.kpi.adoc.interfaces.DocumentationComposer;

/**
 *
 * @author Milan
 */
public class SimpleComposer implements DocumentationComposer {
    @Override
    public String compose(Element element, Map<TypeElement, String> docs) {
        StringBuilder sb = new StringBuilder();
        for (String doc : docs.values()) {
            sb.append(doc);
        }
        return sb.toString();
    }
}
