package fr.ambuconnect.rh.utils;

import com.itextpdf.kernel.colors.ColorConstants;
import com.itextpdf.kernel.colors.DeviceRgb;
import com.itextpdf.layout.Style;
import com.itextpdf.layout.properties.TextAlignment;

public class BulletinPaieStyle {
    public static final DeviceRgb HEADER_BACKGROUND = new DeviceRgb(0, 48, 135);
    public static final DeviceRgb ALTERNATE_ROW = new DeviceRgb(240, 240, 255);
    
    public static Style getHeaderStyle() {
        return new Style()
            .setBackgroundColor(HEADER_BACKGROUND)
            .setFontColor(ColorConstants.WHITE)
            .setTextAlignment(TextAlignment.CENTER)
            .setBold();
    }
    
    public static Style getNormalRowStyle() {
        return new Style()
            .setBackgroundColor(ColorConstants.WHITE)
            .setFontColor(ColorConstants.BLACK)
            .setTextAlignment(TextAlignment.LEFT);
    }
    
    public static Style getAlternateRowStyle() {
        return new Style()
            .setBackgroundColor(ALTERNATE_ROW)
            .setFontColor(ColorConstants.BLACK)
            .setTextAlignment(TextAlignment.LEFT);
    }
    
    public static Style getTotalStyle() {
        return new Style()
            .setBackgroundColor(HEADER_BACKGROUND)
            .setFontColor(ColorConstants.WHITE)
            .setTextAlignment(TextAlignment.RIGHT)
            .setBold();
    }
    
    public static Style getMonetaryStyle() {
        return new Style()
            .setTextAlignment(TextAlignment.RIGHT);
    }
} 