package gdg.travodobackend.app.travel.entity;

import java.util.List;
import java.util.Random;

public class MapColor {

    public static final List<String> COLORS = List.of(
            "#EE8787", "#FFD2C2", "#EAAF4F", "#FFE386",
            "#A4C664", "#B8CDFF", "#769FFF", "#506CAD"
    );

    private static final Random RANDOM = new Random();

    public static String random() {
        return COLORS.get(RANDOM.nextInt(COLORS.size()));
    }
}
