public class Main {
    public static void main(String[] args) throws Exception {
        Context.configType = 1;

        // First load
        ConfigLoader loader1 = new ConfigLoader("out/production/class-reload-demo"); //compile path
        Class<?> reloaded1 = loader1.loadClass("Config");

        // Copy static fields to original Config
        copyStaticFields(reloaded1, Config.class);
        System.out.println("After first reload: " + Config.configName); // config 1

        // Change context
        Context.configType = 2;

        // Reload in a new classloader
        ConfigLoader loader2 = new ConfigLoader("out/production/class-reload-demo");
        Class<?> reloaded2 = loader2.loadClass("Config");

        // Copy static fields again
        copyStaticFields(reloaded2, Config.class);
        System.out.println("After second reload: " + Config.configName); // config 2
    }

    // Copies all static fields from source class to target class
    private static void copyStaticFields(Class<?> source, Class<?> target) throws Exception {
        java.lang.reflect.Field[] fields = source.getDeclaredFields();
        for (java.lang.reflect.Field f : fields) {
            if (java.lang.reflect.Modifier.isStatic(f.getModifiers())) {
                f.setAccessible(true);
                java.lang.reflect.Field targetField = target.getDeclaredField(f.getName());
                targetField.setAccessible(true);
                targetField.set(null, f.get(null));
            }
        }
    }
}
