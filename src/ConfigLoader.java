import java.nio.file.Files;
import java.nio.file.Paths;

public class ConfigLoader extends ClassLoader {
    private final String classPath;

    public ConfigLoader(String classPath) {
        this.classPath = classPath;
    }

    @Override
    public Class<?> loadClass(String name) throws ClassNotFoundException {
        // Only load Config manually; all others use parent
        if (!name.equals("Config")) {
            return super.loadClass(name);
        } // if not added all classes will be loaded by this loader

        try {
            byte[] classData = Files.readAllBytes(Paths.get(classPath + "/Config.class"));
            return defineClass("Config", classData, 0, classData.length); //need to hard code class name without FQN
        } catch (Exception e) {
            throw new ClassNotFoundException("Cannot load Config", e);
        }
    }
}
