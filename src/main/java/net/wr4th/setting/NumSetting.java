package net.wr4th.setting;

import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;

public class NumSetting extends Setting<Double> {
    public final double min, max, step;

    public NumSetting(String name, double def, double min, double max, double step) {
        super(name, def);
        this.min = min;
        this.max = max;
        this.step = step;
    }

    @Override
    public void set(Double v) {
        double d = Math.max(min, Math.min(max, v));
        if (step > 0) d = Math.round(d / step) * step;
        value = Math.max(min, Math.min(max, d));
    }

    public double fraction() { return (value - min) / (max - min); }
    public int asInt() { return (int) Math.round(value); }
    public float asFloat() { return value.floatValue(); }

    public String display() {
        return step >= 1 ? String.valueOf(asInt()) : String.format("%.2f", value);
    }

    @Override public JsonElement save() { return new JsonPrimitive(value); }
    @Override public void load(JsonElement e) { set(e.getAsDouble()); }
}
