package data;

import builders.LogFileUnpacker;
import builders.LogItem;
import builders.LogPair;
import org.apache.commons.lang3.StringUtils;
import utils.Gzip;
import utils.ZipUtil;

import java.io.File;
import java.io.IOException;

public class LogsUnpackMain {
    public static void main(String[] args) throws IOException {
        int[] counts = new int[31];
        File dir = new File("logs/");
        File[] directoryListing = dir.listFiles();
        if (directoryListing != null) {
            for (File child : directoryListing) {
                LogFileUnpacker unpacker = new LogFileUnpacker("logs/" + child.getName());


                LogPair lp;
                boolean should_decompress = false;
                do {
                    lp = unpacker.next();
                    if (lp.type == LogItem.IS_COMPRESSED) {
                        if (lp.value.equals("true")) {
                            should_decompress = true;
                        }
                    }


                    if (lp.type == LogItem.CODE_FRAGMENT) {
                        int lc = StringUtils.countMatches(lp.value, "\n");
                        if (lc > 30) lc = 30;
                        counts[lc]++;
                        /*if (should_decompress) {
                            System.out.println(ZipUtil.decompress(lp.value));
                        } else {
                            System.out.println(lp.value);

                        }*/
                    }

                } while (lp.type != LogItem.END);
            }
        }

        int sum = 0;
        for (int i = 0; i < 31; ++i) {
            sum += counts[i];
            System.out.println("LoC:" + (i+1) + "; Count: " + counts[i]);
        }
        System.out.println(sum);

        /*String tmp = "package builders;\n" +
                "\n" +
                "public class LogPair {\n" +
                "    public LogItem type;\n" +
                "    public String value;\n" +
                "\n" +
                "    public LogPair(LogItem type, String value) {\n" +
                "        this.type = type;\n" +
                "        this.value = value;\n" +
                "    }\n" +
                "}";
        String r1 = ZipUtil.compress(tmp);
        System.out.println(r1);
        System.out.println(ZipUtil.decompress(r1));*/
    }
}
