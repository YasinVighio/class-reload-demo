public class Config {

    private static int configId;

    static {
        configId=Context.configType;
    }

    public static String configName = configId == 1 ? "config 1": "config 2";
}
