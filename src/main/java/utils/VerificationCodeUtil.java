import java.util.Random;

public class VerificationCodeUtil {

    public static String generateCode() {
        Random rand = new Random();
        int code = rand.nextInt(999999); // generates a random 6-digit code
        return String.format("%06d", code);
    }
}
