package io.github.ggabriel67.kanvas.user;

import java.awt.*;

public class AvatarColorGenerator
{
    public static String generateColor(String input) {
        int hash = input.hashCode();

        float hue = (hash & 0xFFFFFFF) % 360 / 360f;

        float saturation = 0.6f;
        float brightness = 0.7f;

        Color color = Color.getHSBColor(hue, saturation, brightness);

        return String.format("#%02x%02x%02x", color.getRed(), color.getGreen(), color.getBlue());
    }
}
