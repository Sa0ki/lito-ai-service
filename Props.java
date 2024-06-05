import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class Props {
    private final static Properties env = new Properties();
    private static boolean isLoaded = false;

    public static void loadProperties() {
        if (isLoaded)
            return;

        try (InputStream input = Props.class.getClassLoader().getResourceAsStream("application.properties")) {
            if (input == null) {
                System.out.println("Sorry, unable to find application.properties");
                return;
            }

            env.load(input);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            isLoaded = true;
        }
    }

    public static String getValueOf(String var) {
        return env.getProperty(var);
    }
}
