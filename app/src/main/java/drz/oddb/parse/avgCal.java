package drz.oddb.parse;

import java.util.ArrayList;
import java.util.Arrays;

public class avgCal {
    //存储外部发送的四组坐标数据
    private static String[] tuples = new String[4];
    int cnt = 0;


    //接收外部发送的坐标数据
    public int append(String tuple1) {
        String i = tuple1;
        i = i.trim();
        i = i.replaceAll("\\[", "");
        i = i.replaceAll("\\]", "");
        //tuples[cnt] = i.split(",");
        tuples[cnt] = tuple1;
        System.out.println("Received: " + tuples[cnt]);
        cnt++;
        return 0;
    }


    //计算平均值并返回包含前四组数据在内的二维数组
    public String[][] Calculate() {
        String temp[][] = new String[5][];
        //ArrayList result = new ArrayList<Integer>();
        int cnt = 0;
        for (; cnt <= 3; cnt++) {
            String i = tuples[cnt];
            i = i.trim();
            i = i.replaceAll("\\[", "");
            i = i.replaceAll("\\]", "");
            i = i.replaceAll(" ", "");
            temp[cnt] = i.split(",");
        }
        int length = temp[0].length;
        for (int j = 0; j <= 3; j++) {
            for (int i = 2; i < length; i++) {
                temp[j][i - 2] = temp[j][i].trim();
            }
        }

        System.out.println("INPUT:");
        for (int i = 0; i <= 3; i++) {
            System.out.println(Arrays.deepToString(temp[i]));
        }

        temp[4] = temp[3].clone();
        for (int j = 0; j < length; j++) {
            int res = Integer.parseInt(temp[0][j]) + Integer.parseInt(temp[1][j]) + Integer.parseInt(temp[2][j]) + Integer.parseInt(temp[3][j]);
            res = res / 4;
            temp[4][j] = Integer.toString(res);
        }

        System.out.println("OUTPUT:");
        for (int i = 0; i <= 4; i++) {
            System.out.println(Arrays.deepToString(temp[i]));
        }
        return temp;
    }


}


