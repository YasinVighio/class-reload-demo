import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

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

    private static void copyStaticFields(Class<?> source, Class<?> target) throws Exception {
        for (Field f : source.getDeclaredFields()) {
            if (Modifier.isStatic(f.getModifiers())) {
                f.setAccessible(true);

                Field targetField = target.getDeclaredField(f.getName());
                targetField.setAccessible(true);

                // Remove final modifier if present
                Field modifiersField = Field.class.getDeclaredField("modifiers");
                modifiersField.setAccessible(true);
                modifiersField.setInt(targetField, targetField.getModifiers() & ~Modifier.FINAL);

                targetField.set(null, f.get(null));
            }
        }
    }

}
