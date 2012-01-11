package au.org.ala.bhl.service;

public class LogService {
    
    public static void log(Class<?> source, String format, Object ... args) {        
        String message = (args.length == 0 ? format : String.format(format, args));       
        System.out.println(String.format("[%s-%s] %s", source.getSimpleName(), Thread.currentThread().getName(), message));        
    }

}
