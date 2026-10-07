package net.wr4th.module;

public enum Category {
    COMBAT("Combat"),
    MOVEMENT("Movement"),
    PLAYER("Player"),
    RENDER("Render"),
    WORLD("World");

    public final String display;

    Category(String display) { this.display = display; }
}
