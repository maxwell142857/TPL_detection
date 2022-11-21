import java.io.*;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

public class Serialization {
    static class edge implements Comparable{
        int from,to;

        public edge(int from, int to) {
            this.from = from;
            this.to = to;
        }

        @Override
        public int compareTo(Object o) {
            edge oo = (edge)o;
            return this.from-oo.from;
        }
    }
    public static void main(String[] args) throws IOException {
        String path = "./sootOutput";		//要遍历的路径
//        String path = "./target";		//要遍历的路径
        File file = new File(path);		//获取其file对象
        File[] fs = file.listFiles();	//遍历path下的文件和目录，放在File数组中
        for(File f:fs){					//遍历File[]数组
            String name = f.toString();
            int length = name.length();
            if(name.startsWith("dot", length-3)){
                name = name.substring(13,length-4);//delete ".\sootOutput\",".dot"
                ArrayList<edge> edges = new ArrayList<>();
                HashMap<Integer,String> descriptions = new HashMap<>();
                BufferedReader br = new BufferedReader(new FileReader(f));
                String line;
                while ((line=br.readLine())!=null) {
                    line = line.trim();
                    if(line.startsWith("\"")){
                        //two situation
                        if(line.contains("->")){
                            //situation1: "number" -> "number"
                           String[] pieces = line.split("->");
                           try{
                               int from = Integer.parseInt(pieces[0].substring(1,pieces[0].length()-1));
                               int to = Integer.parseInt(pieces[1].substring(1,pieces[1].length()-2));
                               edges.add(new edge(from,to));
                           }catch (Exception e){
                               // mistake situation2 as situation1,because the description contains "->"
                               String tmp = line.substring(line.indexOf("label="));
                               tmp = tmp.substring(7,tmp.indexOf("\\l"));
                               descriptions.put(Integer.parseInt(line.substring(1,line.indexOf(" ")-1)),"("+count(line)+"-"+tmp+")");
//                               System.out.println(name);
                           }
                        }else{
                            //situation2: "number" [description]
                            String tmp = line.substring(line.indexOf("label="));
                            tmp = tmp.substring(7,tmp.indexOf("\\l"));
                            descriptions.put(Integer.parseInt(line.substring(1,line.indexOf(" ")-1)),"("+count(line)+"-"+tmp+")");
                        }
                    }
                }
                br.close();

                int node_cnt = descriptions.size();
                int edge_cnt = edges.size();
                int index = 0;
                Collections.sort(edges);
                StringBuilder content = new StringBuilder();
//                content.append(name).append(";");
                content.append(node_cnt).append(";");
                for (int i = 0; i < node_cnt; i++) {
//                    content.append("\n");
                    content.append(i).append(descriptions.get(i)).append(":");
                    while (index<edge_cnt&&edges.get(index).from==i){
                        content.append(edges.get(index).to).append(descriptions.get(edges.get(index).to)).append(" ");
                        index++;
                    }
                    content.append(";");
                }
//                BufferedWriter bw = new BufferedWriter(new FileWriter("./result.txt",true));
                BufferedWriter bw = new BufferedWriter(new FileWriter(args[0],true));
                bw.write(SHA256(content.toString()));
//                bw.write(content.toString());
                bw.newLine();
                bw.close();
//                System.out.println(name);
            }
        }
    }
    public static int count(String s){
        int cnt = 0;
        int index = 0;
        int length = s.length();
        while (index<length-1){
            if(s.charAt(index)=='\\'&&s.charAt(index+1)=='l'){
                cnt++;
                index += 2;
            }else{
                index++;
            }
        }
        return cnt;
    }
    public static String SHA256(final String strText)
    {
        return SHA(strText, "SHA-256");
    }
    private static String SHA(final String strText, final String strType) {
        // 返回值
        String strResult = null;

        // 是否是有效字符串
        if (strText != null && strText.length() > 0) {
            try {
                // SHA 加密开始
                // 创建加密对象 并傳入加密類型
                MessageDigest messageDigest = MessageDigest.getInstance(strType);
                // 传入要加密的字符串
                messageDigest.update(strText.getBytes());
                // 得到 byte 類型结果
                byte byteBuffer[] = messageDigest.digest();

                // 將 byte 轉換爲 string
                StringBuffer strHexString = new StringBuffer();
                // 遍歷 byte buffer
                for (int i = 0; i < byteBuffer.length; i++) {
                    String hex = Integer.toHexString(0xff & byteBuffer[i]);
                    if (hex.length() == 1) {
                        strHexString.append('0');
                    }
                    strHexString.append(hex);
                }
                // 得到返回結果
                strResult = strHexString.toString();
            } catch (NoSuchAlgorithmException e) {
                e.printStackTrace();
            }
        }

        return strResult;
    }
}
