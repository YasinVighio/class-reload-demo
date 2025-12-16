public class Config {

    private static int configId;

    static {
        configId=Context.configType;
    }

    public final static String configName = configId == 1 ? "config 1": "config 2";
}
