package ir.sayandevelopment.sayanreport.Utils;

import java.text.DecimalFormat;

public class MilliCounter {

    DecimalFormat decimalFormat = new DecimalFormat("#.00");

    private long time = 0;
    private double elapsed = 0;

    public void start() {
        time = System.nanoTime();
    }

    public void stop() {
        elapsed = (System.nanoTime() - time) * Math.pow(10, -6);
    }

    public float get() {
        return Float.parseFloat(decimalFormat.format(elapsed));
    }

}
