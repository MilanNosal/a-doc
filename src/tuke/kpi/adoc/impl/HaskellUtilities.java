package tuke.kpi.adoc.impl;

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

        throw new RuntimeException("Unsupported type of annotation parameter!");
    }
}
