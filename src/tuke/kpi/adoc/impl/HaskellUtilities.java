package tuke.kpi.adoc.impl;

import java.util.List;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.element.AnnotationValue;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.ArrayType;
import static javax.lang.model.type.TypeKind.ARRAY;
import static javax.lang.model.type.TypeKind.BOOLEAN;
import static javax.lang.model.type.TypeKind.CHAR;
import static javax.lang.model.type.TypeKind.DECLARED;
import static javax.lang.model.type.TypeKind.DOUBLE;
import static javax.lang.model.type.TypeKind.FLOAT;
import static javax.lang.model.type.TypeKind.INT;
import static javax.lang.model.type.TypeKind.LONG;
import static javax.lang.model.type.TypeKind.SHORT;
import javax.lang.model.type.TypeMirror;

/**
 * dorobit aj generovanie typu a kostry
 * TODO: urobit aj xyxyxyxyxxyx nieco najviac
 * @author Milan
 */
public class HaskellUtilities {
    private ProcessingEnvironment procEnv;
    
    public HaskellUtilities (ProcessingEnvironment procEnv) {
        this.procEnv = procEnv;
    }
    
    /**
     * V podstate konverter z Javy do Haskellu.
     * @param value
     * @return 
     */
    public String getValue(Object value, TypeMirror type) {
        switch (type.getKind()){
            case INT: {
                return Integer.toString((Integer) value);
            }
            case BOOLEAN: {
                return ((Boolean) value).booleanValue() ? "True" : "False";
            }
            case DECLARED: {
                if (type.toString().equals("java.lang.String")) {
                    return String.format("\"%s\"", value);
                }
                
                Element element = procEnv.getTypeUtils().asElement(type);
                if(element.getKind() == ElementKind.ENUM) {
                    return value.toString();
                }
                
                throw new RuntimeException("Unsupported parameter type of annotation type " + type);
            }
            case CHAR: {
                return String.format("'%s'", value);
            }
            case FLOAT: {
                return Float.toString((Float) value);
            }
            case DOUBLE: {
                return Double.toString((Double) value);
            }
            case LONG: {
                return Long.toString((Long) value);
            }
            case SHORT: {
                return Short.toString((Short) value);
            }
            case ARRAY: {
                ArrayType array = (ArrayType) type;
                StringBuilder builder = new StringBuilder("[");
                List<? extends AnnotationValue> components = (List) value;
                for (int i = 0; i < components.size(); i++) {
                    builder.append(getValue(components.get(i).getValue(), array.getComponentType()));
                    if(i < (components.size() - 1)) {
                        builder.append(",");
                    }
                }
                builder.append("]");
                return builder.toString();
            }
            //case 
            default: {
                throw new RuntimeException("Unsupported parameter type of annotation type " + type);
            }
        }
    }
    
    public String generateHaskellPrototype (TypeElement annotationType, RoundEnvironment roundEnv) {
        String name = annotationType.getSimpleName().toString().toLowerCase();
        StringBuilder prototype = new StringBuilder(
                String.format("-- function type prototype for %s, "
                + "first String argument is a name of annotated element, the result is a documentation fragment for the annotation"
                + "\n%s", annotationType.getQualifiedName(), name));
        prototype.append(" :: String ->");
        
        StringBuilder datatypes = new StringBuilder();
        
        for(Element annotationParameter : annotationType.getEnclosedElements()) {
            if (annotationParameter.getKind() != ElementKind.METHOD) {
                System.err.println("Member " + annotationParameter + " in annotation type " + name + " is not a method.");
                continue;
            }
            ExecutableElement method = (ExecutableElement) annotationParameter;
            prototype.append(" ").append(getType(method.getReturnType(), datatypes)).append(" ->");
        }
        
        prototype.append(" String\n\n");
        
        // TODO: dotiahnut aj nejaku sablonku jednoduchu
        prototype.append(String.format("-- function prototype, change according to %s's semantics\n"
                + "%s annotatedElement", annotationType.getQualifiedName(), name));
        for(Element annotationParameter : annotationType.getEnclosedElements()) {
            if (annotationParameter.getKind() == ElementKind.METHOD) {
                prototype.append(" ").append(annotationParameter.getSimpleName());
            }
        }
        prototype.append(" = \"Dummy documentation fragment.\"\n");        
        
        // teraz zretazim udajove typy s prototypom
        datatypes.append(prototype.toString());
        
        return datatypes.toString();
    }
    
    private String getType(TypeMirror type, StringBuilder datatypes) {
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
                
                Element element = procEnv.getTypeUtils().asElement(type);
                if(element.getKind() == ElementKind.ENUM) {
                    TypeElement enumeration = (TypeElement) element;
                    datatypes.append(String.format("-- datatype for enum %s\ndata %s = ", enumeration.getQualifiedName(), enumeration.getSimpleName().toString()));
                    boolean first = true;
                    for(Element constant : enumeration.getEnclosedElements()) {
                        if(constant.getKind() == ElementKind.ENUM_CONSTANT) {
                            if(first) {
                                first = false;
                            } else {
                                datatypes.append(" | ");
                            }
                            datatypes.append(constant.getSimpleName().toString());
                        } else {
                            System.err.println("Something unexpecting in enum type " + enumeration.toString() + ", >> " + constant.toString());
                        }
                    }
                    datatypes.append(" deriving (Show)\n\n");
                    return enumeration.getSimpleName().toString();
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
                return "Integer";
            }
            case SHORT: {
                return "Int";
            }
            case ARRAY: {
                ArrayType array = (ArrayType) type;
                return String.format("[%s]", getType(array.getComponentType(), datatypes));
            }
            //case 
            default: {
                throw new RuntimeException("Unsupported parameter type of annotation type " + type);
            }
        }
    }
}
