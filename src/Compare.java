import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashSet;

public class Compare {
    public static void main(String[] args) throws IOException {
        String file1 = "database_without_name.txt";
        File file = new File(file1);
        BufferedReader br = new BufferedReader(new FileReader(file));
        String line;
        HashSet<String> database = new HashSet<>();
        while ((line=br.readLine())!=null) {
            database.add(line);
        }
        br.close();
        String file2 = "result.txt";
        file = new File(file2);
        br = new BufferedReader(new FileReader(file));
        int total = 0;
        int hit = 0;
        while ((line=br.readLine())!=null) {
            if(database.contains(line)){
                hit++;
//                System.out.println(total+1);
            }
            total++;
        }
        System.out.println("hit/total:"+hit+"/"+total);
    }
}
