package data;

import builders.LogFileUnpacker;
import builders.LogItem;
import builders.LogPair;
import utils.Gzip;
import utils.ZipUtil;

import java.io.IOException;

public class LogsUnpackMain {
    public static void main(String[] args) throws IOException {
        LogFileUnpacker unpacker = new LogFileUnpacker("logs/acp-bucket-1575290261234.txt");

        String tmp = "package builders;\n" +
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
        System.out.println(ZipUtil.decompress(r1));
        /*LogPair lp;
        boolean should_decompress = false;
        do {
            lp = unpacker.next();
            if (lp.type == LogItem.IS_COMPRESSED) {
                if (lp.value.equals("true")) {
                    should_decompress = true;
                }
            }


            if (lp.type == LogItem.FILE_CONTENT) {
                if (should_decompress) {
                    System.out.println(ZipUtil.decompress(lp.value));
                } else {
                    System.out.println(lp.value);

                }
            }

        }while (lp.type != LogItem.END);*/
    }
}
