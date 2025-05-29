package utils;

import java.io.File;
import java.util.Map;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import io.github.cdimascio.dotenv.Dotenv;


public class CloudinaryUploaderUtils {
    private static Cloudinary cloudinary;
    private static final Dotenv dotenv = Dotenv.load();

    private static final String cloudName = dotenv.get("CLOUD_NAME");
    private static final String cloudKey = dotenv.get("CLOUD_KEY");
    private static final String clouSecret = dotenv.get("CLOUD_SECRET");

    static {
        cloudinary = new Cloudinary(ObjectUtils.asMap(
                "cloud_name", cloudName,
                "api_key", cloudKey,
                "api_secret", clouSecret
        ));
    }

    public static String uploadImage(File file) throws Exception {
        Map uploadResult = cloudinary.uploader().upload(file, ObjectUtils.emptyMap());
        return (String) uploadResult.get("secure_url");
    }
}
