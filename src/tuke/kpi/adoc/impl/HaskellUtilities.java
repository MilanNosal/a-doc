package tuke.kpi.adoc.impl;

import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeMirror;

/**
 * dorobit aj generovanie typu a kostry
 * @author Milan
 */
public class HaskellUtilities {
    /**
     * V podstate konverter z Javy do Haskellu.
     * @param value
     * @return 
     */
    public static String getValue(Object value) {
        if (value instanceof Integer) {
            return Integer.toString((Integer) value);
        }

        if (value instanceof Boolean) {
            return ((Boolean) value).booleanValue() ? "True" : "False";
        }

        if (value instanceof String) {
            return String.format("\"%s\"", value);
        }

        if (value instanceof Character) {
            return String.format("'%s'", value);
        }
        
        if (value instanceof Float) {
            return Float.toString((Float) value);
        }
        
        if (value instanceof Double) {
            return Double.toString((Double) value);
        }

        if (value instanceof Long) {
            return Long.toString((Long) value);
        }
        
        if (value instanceof Short) {
            return Short.toString((Short) value);
        }
        
        throw new RuntimeException("Unsupported type of annotation parameter!");
    }
    
    public static String generateHaskellPrototype (TypeElement annotationType) {
        String name = annotationType.getSimpleName().toString().toLowerCase();
        StringBuilder prototype = new StringBuilder(name);
        prototype.append(" :: String -> ");
        
        for(Element annotationParameter : annotationType.getEnclosedElements()) {
            if (annotationParameter.getKind() != ElementKind.METHOD) {
                System.err.println("Member " + annotationParameter + " in annotation type " + name + " is not a method.");
                continue;
            }
            ExecutableElement method = (ExecutableElement) annotationParameter;
            prototype.append(getType(method.getReturnType())).append(" -> ");
        }
        
        prototype.append(" String\n\n");
        
        // TODO: dotiahnut aj nejaku sablonku jednoduchu
        prototype.append(name).append(" x0");
        int index = 1;
        for(Element annotationParameter : annotationType.getEnclosedElements()) {
            if (annotationParameter.getKind() == ElementKind.METHOD) {
                prototype.append(" x").append(index++);
            }
        }
        prototype.append(" = \"Dummy documentation fragment.\"\n");
        
        return prototype.toString();
    }
    
    // TODO: array, enum
    private static String getType(TypeMirror type) {
        switch (type.getKind()) {
            case INT: {
                return "Int";
            }
            case BOOLEAN: {
                return "Bool";
            }
            case DECLARED: {
                if (type.toString().equals("java.lang.String")) {
                    return "String";
                }
                throw new RuntimeException("Unsupported parameter type of annotation type " + type);
            }
            case CHAR: {
                return "Char";
            }
            case FLOAT: {
                return "Float";
            }
            case DOUBLE: {
                return "Double";
            }
            case LONG: {
                return "Int64";
            }
            case SHORT: {
                return "Int";
            }
            default: {
                throw new RuntimeException("Unsupported parameter type of annotation type " + type);
            }
        }
    }
}
