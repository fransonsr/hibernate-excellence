package fransonsr;

import java.util.UUID;

import javax.xml.bind.DatatypeConverter;

@SuppressWarnings("restriction")
public class TestUUID {

    public static final String TEST_UUID_FORMAT = "00000000-0000-0000-0000-%012d";

    public static UUID toUUID(int i) {
        return UUID.fromString(String.format(TEST_UUID_FORMAT, i));
    }

    public static byte[] toBytes(UUID uuid) {
        String hex = uuid.toString().replace("-", "");

        return DatatypeConverter.parseHexBinary(hex);
    }

    public static UUID fromBytes(byte[] bytes) {
        String hex = DatatypeConverter.printHexBinary(bytes);
        StringBuilder buff = new StringBuilder()
            .append(hex.substring(0, 8))
            .append("-")
            .append(hex.substring(8, 12))
            .append("-")
            .append(hex.substring(12, 16))
            .append("-")
            .append(hex.substring(16, 20))
            .append("-")
            .append(hex.substring(20));

        return UUID.fromString(buff.toString());
    }

    public static void main(String[] args) {
        String uuidString = "4af2e305-1283-c243-8eae-f7b2125099de";
        UUID uuid = UUID.fromString(uuidString);

        byte[] bytes = TestUUID.toBytes(uuid);

        System.out.println(TestUUID.fromBytes(bytes).toString());

    }
}
