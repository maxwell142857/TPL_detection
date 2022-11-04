import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashSet;

public class CompareName {
    public static void main(String[] args) throws IOException {
        HashSet<String> database = new HashSet<>();
        String database_path = "./sootOutput1";		//要遍历的路径
        File file = new File(database_path);		//获取其file对象
        File[] fs = file.listFiles();	//遍历path下的文件和目录，放在File数组中
        for(File f:fs) {
            database.add(f.getName());
        }
        int total = 0;
        int hit = 0;
        String target_path = "./sootOutput";		//要遍历的路径
        file = new File(target_path);		//获取其file对象
        fs = file.listFiles();	//遍历path下的文件和目录，放在File数组中
        for(File f:fs) {                    //遍历File[]数组
            if(database.contains(f.getName())){
                hit++;
            }
            total++;
        }
        System.out.printf("hit/total:%d/%d",hit,total);
    }
}