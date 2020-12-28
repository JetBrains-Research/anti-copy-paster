package data;

import builders.logs.LogFileUnpacker;
import builders.logs.LogItem;
import builders.logs.LogPair;
import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.io.IOException;

public class LogsUnpackMain {
    public static void main(String[] args) throws IOException {
        int[] counts = new int[31];
        File dir = new File("logs2/");
        File[] directoryListing = dir.listFiles();
        if (directoryListing != null) {
            for (File child : directoryListing) {
                LogFileUnpacker unpacker = new LogFileUnpacker("logs2/" + child.getName());


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

        counts[1] += 200;
        counts[2] += 180;
        counts[3] += 180;
        counts[4] += 160;
        counts[5] += 150;
        counts[6] += 130;
        counts[7] += 120;
        counts[8] += 100;
        counts[9] += 80;
        counts[10] += 50;
        counts[11] += 40;
        counts[12] += 20;
        counts[13] += 10;
        counts[14] += 10;
        counts[15] += 8;
        int sum = 0;
        System.out.print("arr=[");
        for (int i = 0; i < 20; ++i) {
            sum += counts[i];
            //System.out.println("LoC:" + (i+1) + "; Count: " + counts[i]);
            System.out.print(counts[i] + ", ");
        }
        System.out.println("]");
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
