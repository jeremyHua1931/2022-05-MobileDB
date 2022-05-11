package drz.oddb.parse;

import android.annotation.SuppressLint;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;

public class avgCal {
    //存储外部发送的四组坐标数据
    private static String[] tuples = new String[400];
    int cnt = 0; //cnt为总的元组数，除以四即为每组个数


    //接收外部发送的坐标数据
    public int append(String tuple1) {
        String i = tuple1;
        i = i.trim();
        i = i.replaceAll("\\[", "");
        i = i.replaceAll("\\]", "");
        i = i.replaceAll(" ", "");
        String temp[] = i.split(",");
        String tupleWithoutName=temp[2];
        for(int j=3;j<temp.length;j++){
            tupleWithoutName = tupleWithoutName+","+temp[j] ;
        }
        tuples[cnt] = tupleWithoutName;
        System.out.println("Received: " + tuple1);
        System.out.println("Processed to: " + tupleWithoutName);
        cnt++;
        return 0;
    }


    //计算平均值并返回包含前四组数据在内的二维数组
    public double[][] Calculate() {
        String temp[][] = new String[5][];
        String groups[]=new String[4];
        int divide = cnt/4;
        //循环将每组tuple的值附加给相应的group

        for(int cnt3=0;cnt3<4;cnt3++){
            groups[cnt3] =tuples[cnt3*4];
            for(int k=1;k<divide;k++){
                groups[cnt3] = groups[cnt3] +","+ tuples[cnt3*4+k];
            }
        }
        System.out.println(Arrays.deepToString(groups));
        //ArrayList result = new ArrayList<Integer>();
        int cnt1 = 0;
        for (; cnt1 <= 3; cnt1++) {
            String i = groups[cnt1];
            i = i.trim();
            i = i.replaceAll("\\[", "");
            i = i.replaceAll("\\]", "");
            i = i.replaceAll(" ", "");
            temp[cnt1] = i.split(",");
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
            double res = Double.parseDouble(temp[0][j]) +Double.parseDouble(temp[1][j]) +Double.parseDouble(temp[2][j]) + Double.parseDouble(temp[3][j]);
            res = res / 4;
            temp[4][j] = Double.toString(res) ;
        }

        System.out.println("OUTPUT:");
        for (int i = 0; i <= 4; i++) {
            System.out.println(Arrays.deepToString(temp[i]));
        }

        double[][] result=new double[4][temp[0].length];
        for(int i=0;i<4;i++){
            for(int j=0;j<temp[0].length;j++){
                DecimalFormat df = new DecimalFormat("#0.000000"); // 保留两位小数，四舍五入

                @SuppressLint("DefaultLocale") String tmp11= String.format("%.6f", Double.parseDouble(temp[i][j]));
                System.out.println("====================================="+tmp11);
                result[i][j]= Double.parseDouble(    tmp11       );
            }

        }
        System.out.println(Arrays.toString(result[3]));
        return result;
    }


}


