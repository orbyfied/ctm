package com.github.orbyfied.argument;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.nio.charset.StandardCharsets;
import java.util.Objects;

public class StdContext {

    public static void apply(ArgContext context) {
        context.setFunctionValue("_replace", new Object() {
            public String invoke(ArgContext ctx, String str, String a, String with, Object... other) {
                return str.replace(a, with);
            }
        });

        context.setFunctionValue("_repeat", new Object() {
            public String invoke(ArgContext ctx, String str, int a, Object... other) {
                return str.repeat(a);
            }
        });

        context.setFunctionValue("_strlen", new Object() {
            public int invoke(ArgContext ctx, String str, Object... other) {
                return str.length();
            }
        });

        context.setFunctionValue("_u_readfileutf8", new Object() {
            public String invoke(ArgContext ctx, String filepath, Object... other) {
                try {
                    FileInputStream s = new FileInputStream(filepath);
                    String str = new String(s.readAllBytes(), StandardCharsets.UTF_8);
                    s.close();
                    return str;
                } catch (Exception e) {
                    throw new IllegalStateException("error in reading file " + filepath, e);
                }
            }
        });

        context.setFunctionValue("_u_loadbimage", new Object() {
            public BufferedImage invoke(ArgContext ctx, String filepath, Object... other) {
                try {
                    return ImageIO.read(new File(filepath));
                } catch (Exception e) {
                    e.printStackTrace();
                    return null;
                }
            }
        });

        context.setFunctionValue("_u_getbipx2416", new Object() {
            public String invoke(ArgContext ctx, BufferedImage image, int x, int y, Object... other) {
                Color color = new Color(image.getRGB(x, y));
                int r = color.getRed();
                int g = color.getGreen();
                int b = color.getBlue();
                return
                        ensurelenhead(Integer.toHexString(r), 2, "0") +
                        ensurelenhead(Integer.toHexString(g), 2, "0") +
                        ensurelenhead(Integer.toHexString(b), 2, "0");
            }
        });

        context.setFunctionValue("_add", new Object() {
            public double invoke(ArgContext ctx, double a, double b, Object... other) {
                double c = a + b;
                if (other.length != 0) {
                    for (Object d : other)
                        c += (double)d;
                }
                return c;
            }
        });

        context.setFunctionValue("_sub", new Object() {
            public double invoke(ArgContext ctx, double a, double b, Object... other) {
                double c = a - b;
                if (other.length != 0) {
                    for (Object d : other)
                        c -= (double)d;
                }
                return c;
            }
        });

        context.setFunctionValue("_mul", new Object() {
            public double invoke(ArgContext ctx, double a, double b, Object... other) {
                double c = a * b;
                if (other.length != 0) {
                    for (Object d : other)
                        c *= (double)d;
                }
                return c;
            }
        });

        context.setFunctionValue("_div", new Object() {
            public double invoke(ArgContext ctx, double a, double b, Object... other) {
                double c = a / b;
                if (other.length != 0) {
                    for (Object d : other)
                        c /= (double)d;
                }
                return c;
            }
        });

        context.setFunctionValue("_sqr", new Object() {
            public double invoke(ArgContext ctx, double a, Object... other) {
                return a * a;
            }
        });

        context.setFunctionValue("_pow", new Object() {
            public double invoke(ArgContext ctx, double a, double b, Object... other) {
                return Math.pow(a, b);
            }
        });

        context.setFunctionValue("_sqrt", new Object() {
            public double invoke(ArgContext ctx, double a, Object... other) {
                return Math.sqrt(a);
            }
        });

        context.setFunctionValue("_print", new Object() {
            public String invoke(ArgContext ctx, Object o, Object... other) {
                System.out.println("========== PRINT CALLED");
                String str = Objects.toString(o);
                System.out.println(str);
                return str;
            }
        });
    }

    private static String ensurelenhead(String s, int len, String filler) {
        return filler.repeat(len - s.length()) + s;
    }

}
