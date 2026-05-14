package tn.esprit.farmai.services;

import java.security.SecureRandom;
import java.util.HashMap;
import java.util.Map;

public class OTPService {
    private static final Map<String, String> otpStorage = new HashMap<>(); // Email -> OTP
    private static final SecureRandom random = new SecureRandom();

    public static String generateOTP(String email) {
        String otp = String.format("%06d", random.nextInt(1000000));
        otpStorage.put(email, otp);
        return otp;
    }

    public static boolean verifyOTP(String email, String otp) {
        if (otpStorage.containsKey(email) && otpStorage.get(email).equals(otp)) {
            otpStorage.remove(email);
            return true;
        }
        return false;
    }
}
