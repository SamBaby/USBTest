package invoice_print_machine;

public class PrintCommand {
    public static byte[] rollback60 = new byte[]{0x1B, 0x6A, 0x60};
    public static byte[] rollForward05 = new byte[]{0x1B, 0x4A, 0x05};
    public static byte[] rollForward20 = new byte[]{0x1B, 0x4A, 0x20};
    public static byte[] blankA0 = new byte[]{0x1B, 0x4A, (byte) 0xA0};
    public static byte[] blank50 = new byte[]{0x1B, 0x4A, 0x50};
    public static byte[] cut = new byte[]{0x1B, 0x6D};
    public static byte[] position50 = new byte[]{0x1B, 0x24, 0x50, 0x00};
    public static byte[] position40 = new byte[]{0x1B, 0x24, 0x40, 0x00};
    public static byte[] reset = new byte[]{0x1B, 0x40};
}
