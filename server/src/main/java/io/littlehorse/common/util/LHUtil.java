package io.littlehorse.common.util;

import static com.google.protobuf.util.Timestamps.fromMillis;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.hash.Hashing;
import com.google.protobuf.Message;
import com.google.protobuf.Timestamp;
import io.littlehorse.common.LHSerializable;
import io.littlehorse.sdk.common.proto.MetricsWindowLength;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Base64;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

@Slf4j
public class LHUtil {

    public static final ObjectMapper mapper = new ObjectMapper();

    public static Timestamp fromDate(Date date) {
        if (date == null) return null;
        return fromMillis(date.getTime());
    }

    public static Date fromProtoTs(Timestamp proto) {
        if (proto == null) return null;
        Date out = Date.from(Instant.ofEpochSecond(proto.getSeconds(), proto.getNanos()));

        if (out.getTime() == 0) {
            out = new Date();
        }

        return out;
    }

    public static String generateGuid() {
        return UUID.randomUUID().toString().replaceAll("-", "");
    }

    public static String toLhDbFormat(Date date) {
        return date == null ? "null" : String.format("%012d", date.getTime());
    }

    public static Date getWindowStart(Date time, MetricsWindowLength type) {
        long windowLength = getWindowLengthMillis(type);
        return new Date((time.getTime() / windowLength) * windowLength);
    }

    public static long getWindowLengthMillis(MetricsWindowLength type) {
        switch (type) {
            case MINUTES_5:
                return 1000 * 60 * 5;
            case HOURS_2:
                return 1000 * 60 * 60 * 2;
            case DAYS_1:
                return 1000 * 60 * 60 * 24;
            default:
                throw new RuntimeException("Invalid window!");
        }
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

    /** @precondition every input string is a valid LHName. */
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
        return Hashing.sha256()
                .hashString(str, StandardCharsets.UTF_8)
                .toString()
                .substring(0, 18);
    }

    @SuppressWarnings("unchecked")
    public static <U extends Message, T extends LHSerializable<U>> Class<U> getProtoBaseClass(Class<T> cls) {
        try {
            T t = cls.getDeclaredConstructor().newInstance();
            return (Class<U>) t.getProtoBaseClass();
        } catch (Exception exn) {
            throw new RuntimeException(exn);
        }
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
     * TODO: THis needs more thought. We want the double to be searchable both
     * positive and
     * negative, and we want to be able to do range queries.
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
            log.error(exn.getMessage(), exn);
            return null;
        }
    }

    @SuppressWarnings("unchecked")
    public static Map<String, Object> strToJsonObj(String jsonStr) {
        try {
            return mapper.readValue(jsonStr, Map.class);
        } catch (JsonProcessingException exn) {
            log.error(exn.getMessage(), exn);
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
                log.error("Failed writing map or list to json, returning null.", exn);
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
