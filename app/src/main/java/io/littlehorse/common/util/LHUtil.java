package io.littlehorse.common.util;

import static com.google.protobuf.util.Timestamps.fromMillis;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.hash.Hashing;
import com.google.protobuf.Timestamp;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Base64;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.apache.commons.lang3.StringUtils;

public class LHUtil {

    public static final ObjectMapper mapper = new ObjectMapper();

    public static Timestamp fromDate(Date date) {
        if (date == null) return null;
        return fromMillis(date.getTime());
    }

    public static Date fromProtoTs(Timestamp proto) {
        if (proto == null) return null;
        Date out = Date.from(
            Instant.ofEpochSecond(proto.getSeconds(), proto.getNanos())
        );

        if (out.getTime() == 0) {
            out = new Date();
        }

        return out;
    }

    public static void logBack(int framesBack, Object... things) {
        framesBack += 2; // 2 frames needed for processing the thing.
        StackTraceElement ste = Thread.currentThread().getStackTrace()[framesBack];

        StringBuilder builder = new StringBuilder();

        builder.append("LHorse: ");
        builder.append(ste.getMethodName());
        builder.append(" ");
        builder.append(ste.getFileName());
        builder.append(": ");
        builder.append(ste.getLineNumber());
        builder.append(": ");
        for (Object thing : things) {
            builder.append(thing == null ? "null" : thing.toString());
            builder.append(" ");
        }
        System.out.println(builder.toString());
    }

    public static void log(Object... things) {
        logBack(1, things); // Add one frame back because of this method call.
    }

    public static String generateGuid() {
        return UUID.randomUUID().toString().replaceAll("-", "");
    }

    public static String toLhDbFormat(Date date) {
        return date == null ? "null" : String.format("%012d", date.getTime());
    }

    public static String toLHDbVersionFormat(int version) {
        return String.format("%05d", version);
    }

    public static String toLhDbFormat(Long val) {
        return val == null ? "null" : String.format("%012d", val);
    }

    public static String toLhDbFormat(Boolean val) {
        return val == null ? "null" : val.toString();
    }

    /**
     * @precondition every input string is a valid LHName.
     */
    public static String getCompositeId(String... components) {
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < components.length; i++) {
            builder.append(components[i]);
            if (i + 1 < components.length) {
                builder.append('/');
            }
        }
        return builder.toString();
    }

    public static boolean isValidLHName(String name) {
        return name.matches("[a-z0-9]([-a-z0-9]*[a-z0-9])?");
    }

    public static String digestify(String str) {
        return fullDigestify(str).substring(0, 8);
    }

    public static String fullDigestify(String str) {
        return Hashing
            .sha256()
            .hashString(str, StandardCharsets.UTF_8)
            .toString()
            .substring(0, 18);
    }

    /*
     * The regex with which this is compliant is the same as the regex for
     * kubernetes hostnames.
     */
    public static String toLHName(String oldStr) {
        String str = new String(oldStr);
        str = str.toLowerCase();

        str = str.replaceAll("[. _\n]", "-");
        str = str.replaceAll("[^0-9a-z-]", "");
        str = str.replaceAll("-[-]+", "-");
        str = StringUtils.stripStart(str, "-");
        str = StringUtils.stripEnd(str, "-");
        if (str.length() >= 63) {
            str = str.substring(0, 54) + "-" + digestify(str);
        }
        if (!LHUtil.isValidLHName(str)) {
            throw new RuntimeException("Stupid programmer error.");
        }
        return str;
    }

    /**
     * TODO: THis needs more thought. We want the double to be searchable both positive and negative,
     * and we want to be able to do range queries.
     */
    public static String toLhDbFormat(Double val) {
        return val == null ? "null" : String.format("%20.10f", val);
    }

    public static String toLhDbFormat(String val) {
        // TODO: Determine if we want to truncate this by just hashing it.
        return val;
    }

    @SuppressWarnings("unchecked")
    public static List<Object> strToJsonArr(String jsonStr) {
        try {
            return mapper.readValue(jsonStr, List.class);
        } catch (JsonProcessingException exn) {
            exn.printStackTrace();
            return null;
        }
    }

    @SuppressWarnings("unchecked")
    public static Map<String, Object> strToJsonObj(String jsonStr) {
        try {
            return mapper.readValue(jsonStr, Map.class);
        } catch (JsonProcessingException exn) {
            exn.printStackTrace();
            return null;
        }
    }

    public static String b64Encode(byte[] bytes) {
        return Base64.getEncoder().encodeToString(bytes);
    }

    public static String objToString(Object obj) {
        if (obj == null) return null;
        if (obj instanceof Map || obj instanceof List) {
            try {
                return mapper.writeValueAsString(obj);
            } catch (Exception exn) {
                LHUtil.log("Failed writing map or list to json, returning null.");
                return null;
            }
        }
        return obj.toString();
    }

    public static byte[] objToBytes(Object obj) {
        String s = objToString(obj);
        return s == null ? null : s.getBytes();
    }

    @SuppressWarnings("all")
    public static boolean deepEquals(Object left, Object right) {
        if (left == null && right == null) return true;
        if (left == null || right == null) return false;

        if (left.getClass() == Long.class) {
            left = Double.valueOf((Long) left);
        }
        if (right.getClass() == Long.class) {
            right = Double.valueOf((Long) right);
        }
        if (left.getClass() == Integer.class) {
            left = Double.valueOf((Integer) left);
        }
        if (right.getClass() == Integer.class) {
            right = Double.valueOf((Integer) right);
        }
        if (right.getClass() == Float.class) {
            right = Double.valueOf((Float) right);
        }
        if (left.getClass() == Float.class) {
            left = Double.valueOf((Float) left);
        }

        if (!left.getClass().equals(right.getClass())) {
            return false;
        }

        if (left instanceof List) {
            List<Object> lList = (List<Object>) left;
            List<Object> rList = (List<Object>) right;
            if (rList.size() != lList.size()) return false;

            for (int i = 0; i < lList.size(); i++) {
                if (!deepEquals(lList.get(i), rList.get(i))) {
                    return false;
                }
            }
            return true;
        } else if (left instanceof Map) {
            Map<String, Object> rMap = (Map<String, Object>) right;
            Map<String, Object> lMap = (Map<String, Object>) left;

            for (Map.Entry<String, Object> e : rMap.entrySet()) {
                if (!deepEquals(e.getValue(), lMap.get(e.getKey()))) {
                    return false;
                }
            }
            return true;
        } else {
            return left.equals(right);
        }
    }
}
