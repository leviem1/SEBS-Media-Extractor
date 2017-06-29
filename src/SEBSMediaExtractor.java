import java.io.*;
import java.net.URLDecoder;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

/**
 * Created by Aaron Duran and Levi Muniz on 6/27/17.
 */

public class SEBSMediaExtractor {
    private static ArrayList<File> files = new ArrayList<>();
    private static ArrayList<String> failedFiles = new ArrayList<>();

    public static void main(String[] args) {
        String decoded;
        File extractDir;
        DateFormat df = new SimpleDateFormat("MM/dd/yyyy hh:mm:ss a");


        String currDir = Paths.get(new File(SEBSMediaExtractor.class.getProtectionDomain().getCodeSource().getLocation().getPath()).getParentFile().getPath()).toString();

        try {
            decoded = URLDecoder.decode(currDir, "UTF-8");

        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            return;
        }

        File parentFolder = new File(decoded);
        MXFFinder(parentFolder);
        (extractDir = new File(parentFolder.getAbsolutePath() + File.separator + "extracted")).mkdirs();

        for (File file : files){
            try {
                String toWrite = extractDir.getAbsolutePath() + File.separator + file.getName();

                if (new File(toWrite).exists()) {
                    String fileNew;
                    int count = 1;

                    while (true) {
                        fileNew = toWrite.substring(0, toWrite.lastIndexOf(".")) + " (" + count + ")" + toWrite.substring(toWrite.lastIndexOf("."));
                        count++;
                        if (!new File(fileNew).exists()) break;
                    }

                    toWrite = fileNew;
                }
                Files.copy(file.toPath(), Paths.get(toWrite));
            } catch (IOException ioe) {
                Date dateObj = new Date();
                failedFiles.add("Failed to extract " + file.getAbsolutePath() + " at " + df.format(dateObj));
            }
        }

        if (!failedFiles.isEmpty()) {
            try (
                    FileWriter failedLog = new FileWriter(extractDir + File.separator + "ErrorLog.log", true)
            ) {
                for (String line : failedFiles) {
                    failedLog.append(line);
                }
            } catch (IOException ignored) {}
        }
    }

    private static void MXFFinder(File folder) {
        for (File file : folder.listFiles()) {
            if (file.isDirectory() && !file.getName().equals("extracted")) {
                MXFFinder(file);
            } else if (file.getName().toLowerCase().endsWith(".mxf")) {
                files.add(file);
            }
        }
    }
}
