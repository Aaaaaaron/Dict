package dict;

import org.github.jamm.MemoryMeter;

public class MemoryMeterUtil {
    public static void main(String[] args) {
        int a = 2;
        measure(a);
    }

    public static void measure(Object anObject) {
        MemoryMeter meter = new MemoryMeter();
        System.out.println("-----------------------------------");
        System.out.printf("size: %d bytes\n", meter.measure(anObject));
        System.out.printf("retained size: %d bytes\n", meter.measureDeep(anObject));
        System.out.printf("inner object count: %d\n", meter.countChildren(anObject));
    }
}