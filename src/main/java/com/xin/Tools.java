package com.xin;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.charset.UnsupportedCharsetException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.objectweb.asm.ClassWriter.COMPUTE_MAXS;

/**
 * @author linxixin@cvte.com
 * @version 1.0
 * @description
 */
public class Tools {

    public static byte[] getNewClassBytes(String className, ClassLoader classLoader, GeneralClassAdapter method) throws IOException, ClassNotFoundException {
        ClassReader classReader = new ClassReader(getClassByte(classLoader, className));
        ClassWriter classWriter = new ClassWriter(classReader, COMPUTE_MAXS | COMPUTE_MAXS);
        method.setCp(classWriter);
        classReader.accept(method, ClassReader.EXPAND_FRAMES);
//        System.out.println("修改 " + className + " 完毕_~");
//            FileUtils.writeByteArrayToFile(new File("D:\\PreparedStatement.class"), classWriter.toByteArray());
        return classWriter.toByteArray();
    }

    public static byte[] getClassByte(ClassLoader classLoader, String name) throws ClassNotFoundException {
        try {
            InputStream resourceAsStream = classLoader.getResourceAsStream(name.replace('.', '/')
                                                                                   + ".class");
            if (resourceAsStream == null) {
                throw new ClassNotFoundException(name + " 类不存在无法进行注入");
            }
            return readClass(
                    resourceAsStream, true);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return new byte[0];
    }

    public static byte[] readClass(final InputStream is, boolean close)
            throws IOException {
        if (is == null) {
            throw new IOException("Class not found");
        }
        try {
            byte[] b = new byte[is.available()];
            int len = 0;
            while (true) {
                int n = is.read(b, len, b.length - len);
                if (n == -1) {
                    if (len < b.length) {
                        byte[] c = new byte[len];
                        System.arraycopy(b, 0, c, 0, len);
                        b = c;
                    }
                    return b;
                }
                len += n;
                if (len == b.length) {
                    int last = is.read();
                    if (last < 0) {
                        return b;
                    }
                    byte[] c = new byte[b.length + 1000];
                    System.arraycopy(b, 0, c, 0, len);
                    c[len++] = (byte) last;
                    b = c;
                }
            }
        } finally {
            if (close) {
                is.close();
            }
        }
    }


    public static Charset charset = StandardCharsets.UTF_8;

    public static Map<String, List<String>> decodeParams(String s) {
        Map<String, List<String>> params = new LinkedHashMap<String, List<String>>();
        String name = null;
        int pos = 0; // Beginning of the unprocessed region
        int i;       // End of the unprocessed region
        char c = 0;  // Current character
        for (i = 0; i < s.length(); i++) {
            c = s.charAt(i);
            if (c == '=' && name == null) {
                if (pos != i) {
                    name = decodeComponent(s.substring(pos, i), charset);
                }
                pos = i + 1;
            } else if (c == '&') {
                if (name == null && pos != i) {
                    // We haven't seen an `=' so far but moved forward.
                    // Must be a param of the form '&a&' so add it with
                    // an empty value.
                    addParam(params, decodeComponent(s.substring(pos, i), charset), "");
                } else if (name != null) {
                    addParam(params, name, decodeComponent(s.substring(pos, i), charset));
                    name = null;
                }
                pos = i + 1;
            }
        }

        if (pos != i) {  // Are there characters we haven't dealt with?
            if (name == null) {     // Yes and we haven't seen any `='.
                addParam(params, decodeComponent(s.substring(pos, i), charset), "");
            } else {                // Yes and this must be the last value.
                addParam(params, name, decodeComponent(s.substring(pos, i), charset));
            }
        } else if (name != null) {  // Have we seen a name without value?
            addParam(params, name, "");
        }

        return params;
    }

    private static String decodeComponent(String s, Charset charset) {
        if (s == null) {
            return "";
        }

        try {
            return URLDecoder.decode(s, charset.name());
        } catch (UnsupportedEncodingException e) {
            throw new UnsupportedCharsetException(charset.name());
        }
    }

    private static void addParam(Map<String, List<String>> params, String name, String value) {
        List<String> values = params.get(name);
        if (values == null) {
            values = new ArrayList<String>(1);  // Often there's only 1 value.
            params.put(name, values);
        }
        values.add(value);
    }
}
