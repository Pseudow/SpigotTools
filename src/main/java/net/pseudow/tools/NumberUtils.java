package net.pseudow.tools;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;

public class NumberUtils {
    /**
     * Format a long for example, if we format number 126394 with groupingSize 3 and separator ','
     * it will return 126,396.
     *
     * @author Pseudow
     *
     * @param number - The number you want to format
     * @param groupingSize - the number element in a group
     * @param separator - The char which will separate groups
     * @return the new String formatted
     */
    public static String format(long number, int groupingSize, char separator) {
        DecimalFormatSymbols decimalFormatSymbols = new DecimalFormatSymbols();
        decimalFormatSymbols.setGroupingSeparator(separator);

        DecimalFormat decimalFormat = new DecimalFormat();
        decimalFormat.setDecimalFormatSymbols(decimalFormatSymbols);
        decimalFormat.setGroupingSize(groupingSize);
        decimalFormat.setMaximumFractionDigits(64);

        return decimalFormat.format(number);
    }
}
