package data;

public class CopyLogsExtractorMain {
    public static void main(String[] args) throws Exception {
        CloudLogsExtractor extractor = new CloudLogsExtractor();

        //extractor.dumpBucketsCount();
        //extractor.listBucketsNames();
        extractor.downloadAll();
    }
}
