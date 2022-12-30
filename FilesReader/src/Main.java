import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;


public class Main {
    // Hash map where the keys are the files that require other files, and these
    // requirement files are the value of the key.
    public static HashMap<String, List<String>> currentAndRequires = new HashMap<>();

    // All the files we have collected.
    public static List<String> allFiles = new ArrayList<>();

    // All the keys of the currentAnfRequires hashMap.
    public static List<String> keys = new ArrayList<>();

    // Reading file
    public static List<String> readFile(String path, Charset enc) throws IOException {
        return Files.readAllLines(Paths.get(path), enc);
    }

    // Adding to current-requirements hashMap.
    private static void AddToRR(String path) throws fileEXCE, IOException {
        List<String> lines = readFile(path, StandardCharsets.ISO_8859_1);
        for (String l : lines) {
            if (l.contains("require")) {
                String req = l.substring(9, l.length() - 1) + ".txt";
                if (!currentAndRequires.containsKey(path)) {
                    currentAndRequires.put(path, new ArrayList<>());
                    keys.add(path);
                    currentAndRequires.get(path).add(req);
                } else {
                    if (currentAndRequires.get(path).contains(req)) {
                        currentAndRequires.clear();
                        throw new fileEXCE("This paths are going to cycle..");
                    }
                    currentAndRequires.get(path).add(req);
                }
            }
        }
    }

    // Going through the folders and collecting text files.
    private static void readAllFolders(File current) throws IOException, fileEXCE {
        String[] paths = current.list();
        if (paths != null && paths.length != 0) {
            for (String e : paths) {
                String curFile = current + "\\" + e;
                File path = new File(curFile);
                if (path.isFile()) {
                    allFiles.add(curFile);
                    AddToRR(curFile);
                } else if (path.isDirectory()) {
                    readAllFolders(path);
                }
            }
        }
    }

    public static void main(String[] args) {
        System.out.println("Please, enter file path");
        Scanner in = new Scanner(System.in);
        String path = in.nextLine();
        File fileToCheck = new File(path);
        while(!(fileToCheck.exists() || fileToCheck.isDirectory()) || !fileToCheck.canRead()) {
            System.out.println("Such a file does not exist, try again");
            path = in.nextLine();
            fileToCheck = new File(path);
        }
        try {
            readAllFolders(fileToCheck);
        }catch (IOException e) {
            System.out.println(e.getMessage());
        } catch (fileEXCE e) {
            throw new RuntimeException(e);
        }

        if (!currentAndRequires.isEmpty()) {
            for (var key : keys) {
                int i1 = allFiles.indexOf(key);
                for (var i : currentAndRequires.get(key)) {
                    String toSearch = "";
                    for (var j : allFiles) {
                        if (j.contains(i.replace('/', '\\'))) {
                            toSearch = j;
                        }
                    }
                    int i2 = allFiles.indexOf(toSearch);
                    // If the file stands earlier than the file which it requires,
                    // they replace each other.
                    if (i1 < i2) {
                        String temp = allFiles.get(i2);
                        allFiles.set(i2, allFiles.get(i1));
                        allFiles.set(i1, temp);
                    }
                }
            }
            StringBuilder outp = new StringBuilder();
            for (var i : allFiles) {
                try {
                    List<String> out = readFile(i, StandardCharsets.ISO_8859_1);
                    outp.append(String.join(System.lineSeparator(), out));
                    outp.append("\n");
                } catch (IOException e) {
                    System.out.println(e.getMessage());
                }
            }
            try (FileOutputStream fos = new FileOutputStream("res.txt")){
                byte[] buf = outp.toString().getBytes();
                fos.write(buf, 0, buf.length);
            } catch (IOException ex) {
                System.out.println(ex.getMessage());
            }
        } else {
            System.out.println("beda");
        }
    }
}