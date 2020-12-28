package net.pseudow.tools;

import org.bukkit.Color;

import java.util.Random;

public class ColorUtils {
        private static Random random = new Random();

        /**
         * Pick a random color
         *
         * @author Someone in github, i don't remember sorry. If you think
         * this class is yours please DM me.
         *
         * @return Color
         */
        public static Color randomColor()
        {
            return getColor((random.nextInt(17) + 1));
        }

        /**
         * Get a color with a number (to work with
         * randoms)
         *
         * @author Someone in github, i don't remember sorry. If you think
         * this class is yours please DM me.
         *
         * @param i Number of the color
         *
         * @return Color
         */
        public static Color getColor(int i)
        {
            Color c = null;

            if (i == 1)
                c = Color.AQUA;
            else if (i == 2)
                c = Color.BLACK;
            else if (i == 3)
                c = Color.BLUE;
            else if (i == 4)
                c = Color.FUCHSIA;
            else if (i == 5)
                c = Color.GRAY;
            else if (i == 6)
                c = Color.GREEN;
            else if (i == 7)
                c = Color.LIME;
            else if (i == 8)
                c = Color.MAROON;
            else if (i == 9)
                c = Color.NAVY;
            else if (i == 10)
                c = Color.OLIVE;
            else if (i == 11)
                c = Color.ORANGE;
            else if (i == 12)
                c = Color.PURPLE;
            else if (i == 13)
                c = Color.RED;
            else if (i == 14)
                c = Color.SILVER;
            else if (i == 15)
                c = Color.TEAL;
            else if (i == 16)
                c = Color.WHITE;
            else if (i == 17)
                c = Color.YELLOW;

            return c;
        }
}

