package net.wr4th;

import com.google.gson.*;
import net.fabricmc.loader.api.FabricLoader;
import net.wr4th.module.Module;
import net.wr4th.module.ModuleManager;
import net.wr4th.setting.Setting;

import java.nio.file.Files;
import java.nio.file.Path;

/** Saves keybinds and module settings to config/wr4th.json. */
public final class Config {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    private Config() {}

    private static Path path() { return FabricLoader.getInstance().getConfigDir().resolve("wr4th.json"); }

    public static void save(ModuleManager mm) {
        try {
            JsonObject root = new JsonObject();
            for (Module m : mm.all()) {
                JsonObject o = new JsonObject();
                o.addProperty("key", m.key);
                JsonObject s = new JsonObject();
                for (Setting<?> st : m.settings) s.add(st.name, st.save());
                o.add("settings", s);
                root.add(m.name, o);
            }
            Files.writeString(path(), GSON.toJson(root));
        } catch (Exception e) {
            WR4TH.LOG.error("Failed to save config", e);
        }
    }

    public static void load(ModuleManager mm) {
        Path p = path();
        if (!Files.exists(p)) return;
        try {
            JsonObject root = JsonParser.parseString(Files.readString(p)).getAsJsonObject();
            for (Module m : mm.all()) {
                if (!root.has(m.name)) continue;
                JsonObject o = root.getAsJsonObject(m.name);
                if (o.has("key")) m.key = o.get("key").getAsInt();
                if (!o.has("settings")) continue;
                JsonObject s = o.getAsJsonObject("settings");
                for (Setting<?> st : m.settings) {
                    if (s.has(st.name)) {
                        try { st.load(s.get(st.name)); } catch (Exception ignored) {}
                    }
                }
            }
        } catch (Exception e) {
            WR4TH.LOG.error("Failed to load config", e);
        }
    }
}
