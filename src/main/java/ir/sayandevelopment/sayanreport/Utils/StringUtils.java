package ir.sayandevelopment.sayanreport.Utils;

public class StringUtils {

    public static String capitalize(String string) {
        return string.toUpperCase().charAt(0) + string.toLowerCase().substring(1);
    }
}
