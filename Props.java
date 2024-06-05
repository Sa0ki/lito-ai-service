import java.io.IOException;
import java.util.Properties;

public class Props {
    private final static Properties env = new Properties();
    private static boolean isLoaded = false;
    public static void loadProperties(){
        if(isLoaded)
            return;
        try{
            env.load(Props.class.getClassLoader().getResourceAsStream("resources/application.properties"));
        }catch (IOException e){
            System.out.println(e.getMessage());
        }finally {
            isLoaded = true;
        }
    }
    public static String getValueOf(String var){
        return env.getProperty(var);
    }
}
