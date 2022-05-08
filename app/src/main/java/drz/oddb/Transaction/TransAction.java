package drz.oddb.Transaction;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.nfc.NfcAdapter;
import android.os.Bundle;

import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import drz.oddb.Log.*;
import drz.oddb.Memory.*;


import drz.oddb.show.PrintResult;
import drz.oddb.show.ShowTable;
import drz.oddb.Transaction.SystemTable.*;

import drz.oddb.parse.*;

public class TransAction {
    public TransAction(Context context) {
        this.context = context;
        RedoRest();
    }

    Context context;
    public MemManage mem = new MemManage();

    public ObjectTable topt = mem.loadObjectTable();
    public ClassTable classt = mem.loadClassTable();
    public DeputyTable deputyt = mem.loadDeputyTable();
    public BiPointerTable biPointerT = mem.loadBiPointerTable();
    public SwitchingTable switchingT = mem.loadSwitchingTable();

    LogManage log = new LogManage(this);

    public void SaveAll() {
        mem.saveObjectTable(topt);
        mem.saveClassTable(classt);
        mem.saveDeputyTable(deputyt);
        mem.saveBiPointerTable(biPointerT);
        mem.saveSwitchingTable(switchingT);
        mem.saveLog(log.LogT);
        while (!mem.flush()) ;
        while (!mem.setLogCheck(log.LogT.logID)) ;
        mem.setCheckPoint(log.LogT.logID);//成功退出,所以新的事务块一定全部执行
    }

    public void Test() {
        TupleList tpl = new TupleList();
        Tuple t1 = new Tuple();
        t1.tupleHeader = 5;
        t1.tuple = new Object[t1.tupleHeader];
        t1.tuple[0] = "a";
        t1.tuple[1] = 1;
        t1.tuple[2] = "b";
        t1.tuple[3] = 3;
        t1.tuple[4] = "e";
        Tuple t2 = new Tuple();
        t2.tupleHeader = 5;
        t2.tuple = new Object[t2.tupleHeader];
        t2.tuple[0] = "d";
        t2.tuple[1] = 2;
        t2.tuple[2] = "e";
        t2.tuple[3] = 2;
        t2.tuple[4] = "v";

        tpl.addTuple(t1);
        tpl.addTuple(t2);
        String[] attrname = {"attr2", "attr1", "attr3", "attr5", "attr4"};
        int[] attrid = {1, 0, 2, 4, 3};
        String[] attrtype = {"int", "char", "char", "char", "int"};

        PrintSelectResult(tpl, attrname, attrid, attrtype);

        int[] a = InsertTuple(t1);
        Tuple t3 = GetTuple(a[0], a[1]);
        int[] b = InsertTuple(t2);
        Tuple t4 = GetTuple(b[0], b[1]);
        System.out.println(t3);
    }

    private boolean RedoRest() {//redo
        LogTable redo;
        if ((redo = log.GetReDo()) != null) {
            int redonum = redo.logTable.size();   //先把redo指令加前面
            for (int i = 0; i < redonum; i++) {
                String s = redo.logTable.get(i).str;

                log.WriteLog(s);
                query(s);
            }
        } else {
            return false;
        }
        return true;
    }

    public String query(String s) {

        ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(s.getBytes());
        parse p = new parse(byteArrayInputStream);
        try {


            //预置插入

            if (classt.startTmp == 0) {
                classt.startTmp++;
                System.out.println("第一次执行时,预置插入下列命令");
                System.out.println("//下列5条sql语句预置插入\n" +
                        "CREATE CLASS company1 (name char,age int, salary int);\n" +
                        "CREATE CLASS company2 (name char,age int, salary int);\n" +
                        "INSERT INTO company1 VALUES (\"aa\",20,1000);\n" +
                        "INSERT INTO company2 VALUES (\"bb\",20,1000);\n" +
                        "INSERT INTO company1 VALUES (\"cc\",20,1000);");

                String[] company1CreateTmp = new String[]{"1", "3", "company1", "name", "char", "age", "int", "salary", "int"};
                String[] company2CreateTmp = new String[]{"1", "3", "company2", "name", "char", "age", "int", "salary", "int"};
                String[] company1InsertTmp = new String[]{"4", "3", "company1", "aa", "20", "1000"};
                String[] company2InsertTmp = new String[]{"4", "3", "company2", "bb", "20", "1000"};
                String[] company3InsertTmp = new String[]{"4", "3", "company1", "cc", "20", "1000"};

                String[] company3Union = new String[]{"9", "2", "2", "company3", "name", "0", "0", "nameNew1", "age", "0", "0", "ageNew1", "company1", "age", "=", "20", "name", "0", "0", "nameNew1", "age", "0", "0", "ageNew2", "company2", "age", "=", "20"};
                String[] company4Union = new String[]{"9", "2", "2", "company4", "nameNew1", "0", "0", "nameNew2", "ageNew1", "0", "0", "ageNew2", "company3", "age", "=", "20", "nameNew1", "0", "0", "nameNew2", "ageNew1", "0", "0", "ageNew2", "company3", "age", "=", "20"};

                CreateOriginClass(company1CreateTmp);
                CreateOriginClass(company2CreateTmp);
                Insert(company1InsertTmp);
                Insert(company3InsertTmp);
                Insert(company2InsertTmp);
                CreateUnionDeputyClass(company3Union);
                CreateUnionDeputyClass(company4Union);

                System.out.println("预置命令插入成功");
            }

            String[] aa = p.Run();
            System.out.println("打印");
            System.out.println(Arrays.toString(aa));

            switch (Integer.parseInt(aa[0])) {
                case parse.OPT_CREATE_ORIGINCLASS:
                    log.WriteLog(s);
                    CreateOriginClass(aa);
                    new AlertDialog.Builder(context).setTitle("提示").setMessage("创建成功").setPositiveButton("确定", null).show();
                    break;
                case parse.OPT_CREATE_SELECTDEPUTY:
                    log.WriteLog(s);
                    CreateSelectDeputy(aa);
                    new AlertDialog.Builder(context).setTitle("提示").setMessage("创建成功").setPositiveButton("确定", null).show();
                    break;
                case parse.OPT_DROP:
                    log.WriteLog(s);
                    Drop(aa);
                    new AlertDialog.Builder(context).setTitle("提示").setMessage("删除成功").setPositiveButton("确定", null).show();
                    break;
                case parse.OPT_INSERT:
                    log.WriteLog(s);
                    Insert(aa);
                    new AlertDialog.Builder(context).setTitle("提示").setMessage("插入成功").setPositiveButton("确定", null).show();
                    break;
                case parse.OPT_DELETE:
                    log.WriteLog(s);
                    Delete(aa);
                    new AlertDialog.Builder(context).setTitle("提示").setMessage("删除成功").setPositiveButton("确定", null).show();
                    break;
                case parse.OPT_SELECT_DERECTSELECT:
                    DirectSelect(aa);
                    break;
                case parse.OPT_SELECT_INDERECTSELECT:
                    InDirectSelect(aa);
                    break;
                case parse.OPT_CREATE_UPDATE:
                    log.WriteLog(s);
                    Update(aa);

                    new AlertDialog.Builder(context).setTitle("提示").setMessage("更新成功").setPositiveButton("确定", null).show();
                    break;
                case parse.OPT_CREATE_UNIONDEPUTYCLASS:
                    log.WriteLog(s);
                    System.out.println("进入创建union代理类");
                    CreateUnionDeputyClass(aa);
                    new AlertDialog.Builder(context).setTitle("提示").setMessage("union代理类创建成功").setPositiveButton("确定", null).show();
                    break;
                default:
                    break;

            }
        } catch (ParseException e) {

            e.printStackTrace();
        }

        return s;

    }


    private void CreateUnionDeputyClass(String[] p) {

        System.out.print("开始创建union代理类,执行数组:");
        System.out.println(Arrays.toString(p));

        /*
        [OPT_CREATE_UNIONDEPUTYCLASS, attr_count, select_count, unionDeputyName,  [select1],[select2],[select3] ,[select4]]
         */
        //开始解析
        String OPT_union = p[0];
        String attrCount = p[1];
        String selectCount = p[2];
        String unionDeputyName = p[3];

        int OneSelect = (p.length - 3) / Integer.parseInt(selectCount);

        System.out.println("操作符是： " + OPT_union);
        System.out.println("新代理类名称: " + unionDeputyName);
        System.out.println("代理类属性个数: " + attrCount);
        System.out.println("select 个数是: " + selectCount);
        System.out.println("每一个select占用的数组位置个数: " + OneSelect);

        /*
I/System.out: 第一次执行时,预置插入下列命令
I/System.out: CREATE CLASS company (name char,age int, salary int);
I/System.out: INSERT INTO company VALUES ("aa",20,1000);
I/System.out: 进入创建union代理类
I/System.out: 开始创建union代理类,执行数组:[9, 2, 2, allAPP, name, 0, 0, name1, age, 0, 0, age1, company, name, =, "aa", name, 0, 0, name1, age, 0, 0, age1, company, name, =, "aa"]
I/System.out: 操作符是： 9
I/System.out: 新代理类名称: allAPP
I/System.out: 代理类属性个数: 2
I/System.out: select 个数是: 2
I/System.out: 每一个select占用的数组位置个数: 12
I/System.out: 打印第: 1 个select部分
I/System.out: [6, 6, name, 0, 0, name1, age, 0, 0, age1, company, name, =, "aa"]
I/System.out: 打印第: 2 个select部分
I/System.out: [6, 6, name, 0, 0, name1, age, 0, 0, age1, company, name, =, "aa"]
         */

        //拆分select数组
        //selectALL 每一行即一个select语句执行命令
        String[][] selectALL = new String[Integer.parseInt(selectCount)][OneSelect + 2];

        //新代理类属性名
        String[] NewAttr = new String[Integer.parseInt(attrCount)];
        //每一行即一个select语句原属性名
        String[][] oldAttr = new String[Integer.parseInt(selectCount)][Integer.parseInt(attrCount)];


        for (int i = 0; i < Integer.parseInt(selectCount); i++) {

            selectALL[i][0] = "6";
            selectALL[i][1] = "6";
            for (int j = 0; j < OneSelect; j++) {
                selectALL[i][j + 2] = p[i * OneSelect + j + 4];
            }
            System.out.println("打印第: " + (i + 1) + " 个select部分");
            System.out.println(Arrays.toString(selectALL[i]));
        }

        for (int i = 0; i < Integer.parseInt(selectCount); i++) {

            for (int j = 0; j < Integer.parseInt(attrCount); j++) {
                oldAttr[i][j] = selectALL[i][4 * j + 2];
            }
            System.out.println("打印第: " + (i + 1) + " 个select 旧属性名");
            System.out.println(Arrays.toString(oldAttr[i]));
        }


        int[] attrid = new int[Integer.parseInt(attrCount)];
        for (int i = 0; i < Integer.parseInt(attrCount); i = i + 1) {
            NewAttr[i] = selectALL[0][4 * i + 5];
            attrid[i] = i;
        }
        System.out.println("新属性数组: " + Arrays.toString(NewAttr));
        System.out.println("新属性数组id: " + Arrays.toString(attrid));

        //创建新的代理类,循环执行select,并将获取返回结果,将其插入新建的代理类中
        /*
        (1)表的说明
           ClassTable 记录每个类的每个属性: 类名, 类id, 属性名, 属性id(排列位置), 属性类型, 类类型
           ObjectTable 记录每个类的每个元组的位置: 类id, 元组id(排列位置), 块id, 块内偏移
           BitPointer 记录原类中元组和新代理类中元组 id之间的对应关系
           SwitchingTable 记录旧属性和新属性对应关系: rule记录代理规则
           DeputyTable  记录原类和代理类对应关系和规则: 类id, 代理类id, 规则(即where)

        (2)创建代理类时
            第一步: 修改ClassTable
                    新建代理类, 首先代理类的id在原先已分配的id 基础上+1,即新的classid, 然后依次往表里添加属性, 最后加上"de"表示为代理类属性(几个新属性就新增几个表项)
            第二步: 修改DeputyTable
                   由第一步得到新代理类的id, 然后对每一个被select的类, 找到其对应的id, 加上 where条件, 循环填充表项(几个select就新增几个表项)
            第三步: 修改switchTable
                   描述新代理类属性和被选择的代理类的对应关系 (一个新属性可能对应多个旧属性名,依次排列即可, 应该保证 attr和deputy作为switchTable表主键,不重复)
            第四步: 修改ObjectTable
                   将select所有选择出的结果插入代理类中,同时更新object表的元组存储状态
            第五布: 修改BitPointer
                   记录原类中元组和新代理类中新元组 id之间的对应关系

         (3)元组真实数据的存储

            在修改objectTable中涉及如何取出真实数据和如何存放新数据??

         */

        //1-修改ClassTable和SwitchingTable

        System.out.println("开始修改ClassTable");
        //给新生成的代理类分配类id,默认在原有的基础上加1;
        classt.maxid++;

        int classid = classt.maxid; //代理类的id
        int[] bedeputyid = new int[Integer.parseInt(selectCount)]; //被代理的类的id


        int[] bedeputyattrid = new int[Integer.parseInt(attrCount)];

        System.out.println("由于所有新属性名在所有的select中保持一致,选择第一个select语句, 即: ");
        System.out.println(Arrays.toString(selectALL[0]));

        String beDeputyName = selectALL[0][OneSelect - 2];


        for (int i = 0; i < Integer.parseInt(attrCount); i++) {
            for (ClassTableItem item : classt.classTable) {
                //遍历到的类名等于被代理的类名且属性名等于第一个被选择的代理属性名(依次选择),增加代理类新属性名到classt表上
                if (item.classname.equals(beDeputyName) && item.attrname.equals(selectALL[0][4 * i + 2])) {


                    bedeputyattrid[i] = item.attrid;

                    classt.classTable.add(new ClassTableItem(unionDeputyName, classid, Integer.parseInt(attrCount), attrid[i], NewAttr[i], item.attrtype, "de"));

                    //修改SwitchingTable
                    if (Integer.parseInt(selectALL[0][3 + 4 * i]) == 1) {
                        switchingT.switchingTable.add(new SwitchingTableItem(item.attrname, NewAttr[i], selectALL[0][4 + 4 * i]));
                    }
                    if (Integer.parseInt(selectALL[0][3 + 4 * i]) == 0) {
                        switchingT.switchingTable.add(new SwitchingTableItem(item.attrname, NewAttr[i], "0"));
                    }
                    break;
                }
            }
        }

        for (int i = 0; i < Integer.parseInt(selectCount); i++) {
//            System.out.println("被代理类的名字是" + selectALL[i][OneSelect - 2]);
            for (ClassTableItem item : classt.classTable) {
                if (item.classname.equals(selectALL[i][OneSelect - 2])) {
                    bedeputyid[i] = item.classid;
                    System.out.println("此时为  " + item.classid);
                    break;
                }
            }
        }


//        System.out.println("============================="+Arrays.toString(bedeputyattrid));
//        System.out.println("============================="+Arrays.toString(bedeputyid));


        //2-修改deputyTable
        String[][] condition = new String[Integer.parseInt(selectCount)][3];
        for (int i = 0; i < Integer.parseInt(selectCount); i++) {
            condition[i][2] = selectALL[i][OneSelect + 1];
//            String s=condition[i][2];
//            s=s.replace("\"","");
//            condition[i][2]=s;
//            System.out.println(condition[i][2]);
            condition[i][1] = selectALL[i][OneSelect];
            condition[i][0] = selectALL[i][OneSelect - 1];
            deputyt.deputyTable.add(new DeputyTableItem(bedeputyid[i], classid, condition[i]));
            System.out.println("打印第: " + (i + 1) + " 个where");
            System.out.println(Arrays.toString(condition[i]));
        }

        //3-修改objectTable和BiPointTable

        TupleList tpl = new TupleList();

        int[] conid = new int[Integer.parseInt(selectCount)];
        String[] contype = new String[Integer.parseInt(selectCount)];

        for (int i = 0; i < Integer.parseInt(selectCount); i++) {
            for (ClassTableItem item3 : classt.classTable) {
                if (item3.attrname.equals(condition[i][0])) {
                    conid[i] = item3.attrid;
                    contype[i] = item3.attrtype;
                    break;
                }
            }
        }

        System.out.println("所有Where所选择的属性在其类属性的排名位置: " + Arrays.toString(conid));
        System.out.println("所有Where所选择的属性的类型: " + Arrays.toString(contype));


        List<ObjectTableItem> obj = new ArrayList<>();

        for (int i = 0; i < Integer.parseInt(selectCount); i++) {
//            System.out.println("开始选择第 "+(i+1)+" 个类");

            for (ObjectTableItem item2 : topt.objectTable) {
//                System.out.println("第 "+(i+1)+" 次遍历object表");
//                System.out.println("classid= " +item2.classid);
//                System.out.println("第一个被代理类id是 "+ bedeputyid[i]);

                if (item2.classid == bedeputyid[i]) {
                    Tuple tuple = GetTuple(item2.blockid, item2.offset);

                    System.out.println("取出的元组是" + Arrays.toString(tuple.tuple));

                    if (Condition(contype[i], tuple, conid[i], condition[i][2])) {

                        Tuple ituple = new Tuple();
                        ituple.tupleHeader = Integer.parseInt(attrCount);
                        ituple.tuple = new Object[Integer.parseInt(attrCount)];

                        for (int o = 0; o < Integer.parseInt(attrCount); o++) {
                            if (Integer.parseInt(selectALL[i][3 + 4 * o]) == 1) {
                                int value = Integer.parseInt(selectALL[i][4 + 4 * o]);
                                int orivalue = Integer.parseInt((String) tuple.tuple[bedeputyattrid[o]]);
                                Object ob = value + orivalue;
                                ituple.tuple[o] = ob;
                            }

//                            System.out.println("++++++++++++++++++++++++-------------"+bedeputyattrid[o]);

                            if (Integer.parseInt(selectALL[i][3 + 4 * o]) == 0) {
//                                System.out.println("+++++++++++++++++" + tuple.tuple[bedeputyattrid[o]]);
                                ituple.tuple[o] = tuple.tuple[bedeputyattrid[o]];
                            }
                        }

                        topt.maxTupleId++;
                        int tupid = topt.maxTupleId;
                        int[] aa = InsertTuple(ituple);
                        obj.add(new ObjectTableItem(classid, tupid, aa[0], aa[1]));
                        biPointerT.biPointerTable.add(new BiPointerTableItem(bedeputyid[i], item2.tupleid, classid, tupid));

                    }
                }
            }

        }

        for (ObjectTableItem item6 : obj) {
            topt.objectTable.add(item6);
        }


    }

    //CREATE SELECTDEPUTY aa SELECT  b1+2 AS c1,b2 AS c2,b3 AS c3 FROM  bb WHERE t1="1" ;
    //2,3,aa,b1,1,2,c1,b2,0,0,c2,b3,0,0,c3,bb,t1,=,"1"
    //0 1 2  3  4 5 6  7  8 9 10 11 121314 15 16 17 18
    private void CreateSelectDeputy(String[] p) {
        //处理的属性个数
        int count = Integer.parseInt(p[1]);
        //生成的代理类的名称
        String classname = p[2];//代理类的名字
        //被代理的类的名称
        String bedeputyname = p[4 * count + 3];//代理的类的名字

        classt.maxid++;
        int classid = classt.maxid;//代理类的id
        int bedeputyid = -1;//代理的类的id

        String[] attrname = new String[count];
        String[] bedeputyattrname = new String[count];


        int[] bedeputyattrid = new int[count];
        String[] attrtype = new String[count];
        int[] attrid = new int[count];


        //提取旧属性名称和代理类名称
        for (int j = 0; j < count; j++) {
            attrname[j] = p[4 * j + 6];
            attrid[j] = j;
            bedeputyattrname[j] = p[4 * j + 3];
        }

        String attrtype1;
        for (int i = 0; i < count; i++) {
            for (ClassTableItem item : classt.classTable) {
                if (item.classname.equals(bedeputyname) && item.attrname.equals(p[3 + 4 * i])) {
                    bedeputyid = item.classid;
                    bedeputyattrid[i] = item.attrid;

                    classt.classTable.add(new ClassTableItem(classname, classid, count, attrid[i], attrname[i], item.attrtype, "de"));
                    //swi
                    if (Integer.parseInt(p[4 + 4 * i]) == 1) {
                        switchingT.switchingTable.add(new SwitchingTableItem(item.attrname, attrname[i], p[5 + 4 * i]));
                    }
                    if (Integer.parseInt(p[4 + 4 * i]) == 0) {
                        switchingT.switchingTable.add(new SwitchingTableItem(item.attrname, attrname[i], "0"));
                    }
                    break;
                }
            }
            ;
        }


        //where条件
        String[] con = new String[3];
        con[0] = p[4 + 4 * count];
        con[1] = p[5 + 4 * count];
        con[2] = p[6 + 4 * count];
        deputyt.deputyTable.add(new DeputyTableItem(bedeputyid, classid, con));


        TupleList tpl = new TupleList();

        int conid = 0;
        String contype = null;
        for (ClassTableItem item3 : classt.classTable) {
            if (item3.attrname.equals(con[0])) {
                conid = item3.attrid;
                contype = item3.attrtype;
                break;
            }
        }


        List<ObjectTableItem> obj = new ArrayList<>();
        for (ObjectTableItem item2 : topt.objectTable) {
            if (item2.classid == bedeputyid) {
                Tuple tuple = GetTuple(item2.blockid, item2.offset);
                if (Condition(contype, tuple, conid, con[2])) {
                    //插入
                    //swi
                    Tuple ituple = new Tuple();
                    ituple.tupleHeader = count;
                    ituple.tuple = new Object[count];

                    for (int o = 0; o < count; o++) {
                        if (Integer.parseInt(p[4 + 4 * o]) == 1) {
                            int value = Integer.parseInt(p[5 + 4 * o]);
                            int orivalue = Integer.parseInt((String) tuple.tuple[bedeputyattrid[o]]);
                            Object ob = value + orivalue;
                            ituple.tuple[o] = ob;
                        }
                        if (Integer.parseInt(p[4 + 4 * o]) == 0) {
                            ituple.tuple[o] = tuple.tuple[bedeputyattrid[o]];
                        }
                    }

                    topt.maxTupleId++;
                    int tupid = topt.maxTupleId;

                    int[] aa = InsertTuple(ituple);
                    //topt.objectTable.add(new ObjectTableItem(classid,tupid,aa[0],aa[1]));
                    obj.add(new ObjectTableItem(classid, tupid, aa[0], aa[1]));

                    //bi
                    biPointerT.biPointerTable.add(new BiPointerTableItem(bedeputyid, item2.tupleid, classid, tupid));

                }
            }
        }
        for (ObjectTableItem item6 : obj) {
            topt.objectTable.add(item6);
        }
    }


    //CREATE CLASS dZ123 (nB1 int,nB2 char) ;
    //1,2,dZ123,nB1,int,nB2,char
    private void CreateOriginClass(String[] p) {
        String classname = p[2];
        int count = Integer.parseInt(p[1]);
        classt.maxid++;
        int classid = classt.maxid;
        for (int i = 0; i < count; i++) {
            classt.classTable.add(new ClassTableItem(classname, classid, count, i, p[2 * i + 3], p[2 * i + 4], "ori"));
        }
    }

    //INSERT INTO aa VALUES (1,2,"3");
    //4,3,aa,1,2,"3"
    //0 1 2  3 4  5
    private int Insert(String[] p) {


        int count = Integer.parseInt(p[1]);
        for (int o = 0; o < count + 3; o++) {
            p[o] = p[o].replace("\"", "");
        }


        String classname = p[2];
        Object[] tuple_ = new Object[count];

        int classid = 0;

        for (ClassTableItem item : classt.classTable) {
            if (item.classname.equals(classname)) {
                classid = item.classid;
            }
        }

        for (int j = 0; j < count; j++) {
            tuple_[j] = p[j + 3];
        }


        Tuple tuple = new Tuple(tuple_);
        tuple.tupleHeader = count;

        int[] a = InsertTuple(tuple);
        topt.maxTupleId++;
        int tupleid = topt.maxTupleId;
        topt.objectTable.add(new ObjectTableItem(classid, tupleid, a[0], a[1]));

        //向代理类加元组

        for (DeputyTableItem item : deputyt.deputyTable) {
            if (classid == item.originid) {
                //判断代理规则

                String attrtype = null;
                int attrid = 0;
                for (ClassTableItem item1 : classt.classTable) {
                    if (item1.classid == classid && item1.attrname.equals(item.deputyrule[0])) {
                        attrtype = item1.attrtype;
                        attrid = item1.attrid;
                        break;
                    }
                }

                if (Condition(attrtype, tuple, attrid, item.deputyrule[2])) {
                    String[] ss = p.clone();
                    String s1 = null;

                    for (ClassTableItem item2 : classt.classTable) {
                        if (item2.classid == item.deputyid) {
                            s1 = item2.classname;
                            break;
                        }
                    }
                    //是否要插switch的值
                    //收集源类属性名
                    String[] attrname1 = new String[count];
                    int[] attrid1 = new int[count];
                    int k = 0;
                    for (ClassTableItem item3 : classt.classTable) {
                        if (item3.classid == classid) {
                            attrname1[k] = item3.attrname;
                            attrid1[k] = item3.attrid;
                            k++;

                            if (k == count)
                                break;
                        }
                    }
                    for (int l = 0; l < count; l++) {
                        for (SwitchingTableItem item4 : switchingT.switchingTable) {
                            if (item4.attr.equals(attrname1[l])) {
                                //判断被置换的属性是否是代理类的

                                for (ClassTableItem item8 : classt.classTable) {
                                    if (item8.attrname.equals(item4.deputy) && Integer.parseInt(item4.rule) != 0) {
                                        if (item8.classid == item.deputyid) {
                                            int sw = Integer.parseInt(p[3 + attrid1[l]]);
                                            ss[3 + attrid1[l]] = new Integer(sw + Integer.parseInt(item4.rule)).toString();
                                            break;
                                        }
                                    }
                                }


                            }
                        }
                    }

                    ss[2] = s1;
                    int deojid = Insert(ss);
                    //插入Bi
                    biPointerT.biPointerTable.add(new BiPointerTableItem(classid, tupleid, item.deputyid, deojid));


                }
            }
        }
        return tupleid;


    }

    private boolean Condition(String attrtype, Tuple tuple, int attrid, String value1) {
        String value = value1.replace("\"", "");
        switch (attrtype) {
            case "int":
                int value_int = Integer.parseInt(value);
                if (Integer.parseInt((String) tuple.tuple[attrid]) == value_int)
                    return true;
                break;
            case "char":
                String value_string = value;
                if (tuple.tuple[attrid].equals(value_string))
                    return true;
                break;

        }
        return false;
    }

    //DELETE FROM bb WHERE t4="5SS";
    //5,bb,t4,=,"5SS"
    private void Delete(String[] p) {
        String classname = p[1];
        String attrname = p[2];
        int classid = 0;
        int attrid = 0;
        String attrtype = null;
        for (ClassTableItem item : classt.classTable) {
            if (item.classname.equals(classname) && item.attrname.equals(attrname)) {
                classid = item.classid;
                attrid = item.attrid;
                attrtype = item.attrtype;
                break;
            }
        }
        //寻找需要删除的
        OandB ob2 = new OandB();
        for (Iterator it1 = topt.objectTable.iterator(); it1.hasNext(); ) {
            ObjectTableItem item = (ObjectTableItem) it1.next();
            if (item.classid == classid) {
                Tuple tuple = GetTuple(item.blockid, item.offset);
                if (Condition(attrtype, tuple, attrid, p[4])) {
                    //需要删除的元组
                    OandB ob = new OandB(DeletebyID(item.tupleid));
                    for (ObjectTableItem obj : ob.o) {
                        ob2.o.add(obj);
                    }
                    for (BiPointerTableItem bip : ob.b) {
                        ob2.b.add(bip);
                    }

                }
            }
        }
        for (ObjectTableItem obj : ob2.o) {
            topt.objectTable.remove(obj);
        }
        for (BiPointerTableItem bip : ob2.b) {
            biPointerT.biPointerTable.remove(bip);
        }

    }

    private OandB DeletebyID(int id) {

        List<ObjectTableItem> todelete1 = new ArrayList<>();
        List<BiPointerTableItem> todelete2 = new ArrayList<>();
        OandB ob = new OandB(todelete1, todelete2);
        for (Iterator it1 = topt.objectTable.iterator(); it1.hasNext(); ) {
            ObjectTableItem item = (ObjectTableItem) it1.next();
            if (item.tupleid == id) {
                //需要删除的tuple


                //删除代理类的元组
                int deobid = 0;

                for (Iterator it = biPointerT.biPointerTable.iterator(); it.hasNext(); ) {
                    BiPointerTableItem item1 = (BiPointerTableItem) it.next();
                    if (item.tupleid == item1.deputyobjectid) {
                        //it.remove();
                        if (!todelete2.contains(item1))
                            todelete2.add(item1);
                    }
                    if (item.tupleid == item1.objectid) {
                        deobid = item1.deputyobjectid;
                        OandB ob2 = new OandB(DeletebyID(deobid));

                        for (ObjectTableItem obj : ob2.o) {
                            if (!todelete1.contains(obj))
                                todelete1.add(obj);
                        }
                        for (BiPointerTableItem bip : ob2.b) {
                            if (!todelete2.contains(bip))
                                todelete2.add(bip);
                        }

                        //biPointerT.biPointerTable.remove(item1);

                    }
                }


                //删除自身
                DeleteTuple(item.blockid, item.offset);
                if (!todelete2.contains(item)) ;
                //TODO !todelete1.contains(item)
                todelete1.add(item);


            }
        }

        return ob;
    }

    //DROP CLASS asd;
    //3,asd

    private void Drop(String[] p) {
        List<DeputyTableItem> dti;
        dti = Drop1(p);
        for (DeputyTableItem item : dti) {
            deputyt.deputyTable.remove(item);
        }
    }

    private List<DeputyTableItem> Drop1(String[] p) {
        String classname = p[1];
        int classid = 0;
        //找到classid顺便 清除类表和switch表
        for (Iterator it1 = classt.classTable.iterator(); it1.hasNext(); ) {
            ClassTableItem item = (ClassTableItem) it1.next();
            if (item.classname.equals(classname)) {
                classid = item.classid;
                for (Iterator it = switchingT.switchingTable.iterator(); it.hasNext(); ) {
                    SwitchingTableItem item2 = (SwitchingTableItem) it.next();
                    if (item2.attr.equals(item.attrname) || item2.deputy.equals(item.attrname)) {
                        it.remove();
                    }
                }
                it1.remove();
            }
        }
        //清元组表同时清了bi
        OandB ob2 = new OandB();
        for (ObjectTableItem item1 : topt.objectTable) {
            if (item1.classid == classid) {
                OandB ob = DeletebyID(item1.tupleid);
                for (ObjectTableItem obj : ob.o) {
                    ob2.o.add(obj);
                }
                for (BiPointerTableItem bip : ob.b) {
                    ob2.b.add(bip);
                }
            }
        }
        for (ObjectTableItem obj : ob2.o) {
            topt.objectTable.remove(obj);
        }
        for (BiPointerTableItem bip : ob2.b) {
            biPointerT.biPointerTable.remove(bip);
        }

        //清deputy
        List<DeputyTableItem> dti = new ArrayList<>();
        for (DeputyTableItem item3 : deputyt.deputyTable) {
            if (item3.deputyid == classid) {
                if (!dti.contains(item3))
                    dti.add(item3);
            }
            if (item3.originid == classid) {
                //删除代理类
                String[] s = p.clone();
                List<String> sname = new ArrayList<>();
                for (ClassTableItem item5 : classt.classTable) {
                    if (item5.classid == item3.deputyid) {
                        sname.add(item5.classname);
                    }
                }
                for (String item4 : sname) {

                    s[1] = item4;
                    List<DeputyTableItem> dti2 = Drop1(s);
                    for (DeputyTableItem item8 : dti2) {
                        if (!dti.contains(item8))
                            dti.add(item8);
                    }

                }
                if (!dti.contains(item3))
                    dti.add(item3);
            }
        }
        return dti;

    }


    //SELECT  b1+2 AS c1,b2 AS c2,b3 AS c3 FROM  bb WHERE t1="1";
    //6,3,b1,1,2,c1,b2,0,0,c2,b3,0,0,c3,bb,t1,=,"1"
    //0 1 2  3 4 5  6  7 8 9  10 111213 14 15 16 17
    private TupleList DirectSelect(String[] p) {
        TupleList tpl = new TupleList();
        int attrnumber = Integer.parseInt(p[1]);
        String[] attrname = new String[attrnumber];
        int[] attrid = new int[attrnumber];
        String[] attrtype = new String[attrnumber];
        String classname = p[2 + 4 * attrnumber];
        int classid = 0;
        for (int i = 0; i < attrnumber; i++) {
            for (ClassTableItem item : classt.classTable) {
                if (item.classname.equals(classname) && item.attrname.equals(p[2 + 4 * i])) {
                    classid = item.classid;
                    attrid[i] = item.attrid;
                    attrtype[i] = item.attrtype;
                    attrname[i] = p[5 + 4 * i];
                    //重命名

                    break;
                }
            }
        }


        int sattrid = 0;
        String sattrtype = null;
        for (ClassTableItem item : classt.classTable) {
//            System.out.println("----"+item.classid);
//            System.out.println("---"+classid);
//            System.out.println("------"+item.attrname);
//            System.out.println("-----"+p[3 + 4 * attrnumber]);
            if (item.classid == classid && item.attrname.equals(p[3 + 4 * attrnumber])) {
//                System.out.println("---------++++"+item.classid);
//                System.out.println("---------++++"+classid);
//                System.out.println("---------++++"+item.attrname);
//                System.out.println("---------++++"+p[3 + 4 * attrnumber]);

                sattrid = item.attrid;
                sattrtype = item.attrtype;
                break;
            }
        }

        System.out.println("---------------------" + sattrtype);

        for (ObjectTableItem item : topt.objectTable) {
            if (item.classid == classid) {
                Tuple tuple = GetTuple(item.blockid, item.offset);
                if (Condition(sattrtype, tuple, sattrid, p[4 * attrnumber + 5])) {
                    //Switch

                    for (int j = 0; j < attrnumber; j++) {
                        if (Integer.parseInt(p[3 + 4 * j]) == 1) {
                            int value = Integer.parseInt(p[4 + 4 * j]);
                            int orivalue = Integer.parseInt((String) tuple.tuple[attrid[j]]);
                            Object ob = value + orivalue;
                            tuple.tuple[attrid[j]] = ob;
                        }

                    }


                    tpl.addTuple(tuple);
                }
            }
        }
        for (int i = 0; i < attrnumber; i++) {
            attrid[i] = i;
        }
        PrintSelectResult(tpl, attrname, attrid, attrtype);
        return tpl;

    }


    //SELECT popSinger -> singer.nation  FROM popSinger WHERE singerName = "JayZhou";
    //7,2,popSinger,singer,nation,popSinger,singerName,=,"JayZhou"
    //0 1 2         3      4      5         6          7  8
    private TupleList InDirectSelect(String[] p) {
        TupleList tpl = new TupleList();
        String classname = p[3];
        String attrname = p[4];
        String crossname = p[2];
        String[] attrtype = new String[1];
        String[] con = new String[3];
        con[0] = p[6];
        con[1] = p[7];
        con[2] = p[8];

        int classid = 0;
        int crossid = 0;
        String crossattrtype = null;
        int crossattrid = 0;
        for (ClassTableItem item : classt.classTable) {
            if (item.classname.equals(classname)) {
                classid = item.classid;
                if (attrname.equals(item.attrname))
                    attrtype[0] = item.attrtype;
            }
            if (item.classname.equals(crossname)) {
                crossid = item.classid;
                if (item.attrname.equals(con[0])) {
                    crossattrtype = item.attrtype;
                    crossattrid = item.attrid;
                }
            }
        }

        for (ObjectTableItem item1 : topt.objectTable) {
            if (item1.classid == crossid) {
                Tuple tuple = GetTuple(item1.blockid, item1.offset);
                if (Condition(crossattrtype, tuple, crossattrid, con[2])) {
                    for (BiPointerTableItem item3 : biPointerT.biPointerTable) {
                        if (item1.tupleid == item3.objectid && item3.deputyid == classid) {
                            for (ObjectTableItem item2 : topt.objectTable) {
                                if (item2.tupleid == item3.deputyobjectid) {
                                    Tuple ituple = GetTuple(item2.blockid, item2.offset);
                                    tpl.addTuple(ituple);
                                }
                            }
                        }
                    }

                }
            }

        }
        String[] name = new String[1];
        name[0] = attrname;
        int[] id = new int[1];
        id[0] = 0;
        PrintSelectResult(tpl, name, id, attrtype);
        return tpl;


    }

    //UPDATE Song SET type = ‘jazz’WHERE songId = 100;
    //OPT_CREATE_UPDATE，Song，type，“jazz”，songId，=，100
    //0                  1     2      3        4      5  6
    private void Update(String[] p) {
        String classname = p[1];
        String attrname = p[2];
        String cattrname = p[4];

        int classid = 0;
        int attrid = 0;
        String attrtype = null;
        int cattrid = 0;
        String cattrtype = null;
        for (ClassTableItem item : classt.classTable) {
            if (item.classname.equals(classname)) {
                classid = item.classid;
                break;
            }
        }
        for (ClassTableItem item1 : classt.classTable) {
            if (item1.classid == classid && item1.attrname.equals(attrname)) {
                attrtype = item1.attrtype;
                attrid = item1.attrid;
            }
        }
        for (ClassTableItem item2 : classt.classTable) {
            if (item2.classid == classid && item2.attrname.equals(cattrname)) {
                cattrtype = item2.attrtype;
                cattrid = item2.attrid;
            }
        }


        for (ObjectTableItem item3 : topt.objectTable) {
            if (item3.classid == classid) {
                Tuple tuple = GetTuple(item3.blockid, item3.offset);
                if (Condition(cattrtype, tuple, cattrid, p[6])) {
                    UpdatebyID(item3.tupleid, attrid, p[3].replace("\"", ""));

                }
            }
        }
    }

    private void UpdatebyID(int tupleid, int attrid, String value) {
        for (ObjectTableItem item : topt.objectTable) {
            if (item.tupleid == tupleid) {
                Tuple tuple = GetTuple(item.blockid, item.offset);
                tuple.tuple[attrid] = value;
                UpateTuple(tuple, item.blockid, item.offset);
                Tuple tuple1 = GetTuple(item.blockid, item.offset);
                UpateTuple(tuple1, item.blockid, item.offset);
            }
        }

        String attrname = null;
        for (ClassTableItem item2 : classt.classTable) {
            if (item2.attrid == attrid) {
                attrname = item2.attrname;
                break;
            }
        }
        for (BiPointerTableItem item1 : biPointerT.biPointerTable) {
            if (item1.objectid == tupleid) {


                for (ClassTableItem item4 : classt.classTable) {
                    if (item4.classid == item1.deputyid) {
                        String dattrname = item4.attrname;
                        int dattrid = item4.attrid;
                        for (SwitchingTableItem item5 : switchingT.switchingTable) {
                            String dswitchrule = null;
                            String dvalue = null;
                            if (item5.attr.equals(attrname) && item5.deputy.equals(dattrname)) {
                                dvalue = value;
                                if (Integer.parseInt(item5.rule) != 0) {
                                    dswitchrule = item5.rule;
                                    dvalue = Integer.toString(Integer.parseInt(dvalue) + Integer.parseInt(dswitchrule));
                                }
                                UpdatebyID(item1.deputyobjectid, dattrid, dvalue);
                                break;
                            }
                        }
                    }
                }
            }
        }

    }


    //INSERT INTO aa VALUES (1,2,"3");
    //4,3,aa,1,2,"3"


    private class OandB {
        public List<ObjectTableItem> o = new ArrayList<>();
        public List<BiPointerTableItem> b = new ArrayList<>();

        public OandB() {
        }

        public OandB(OandB oandB) {
            this.o = oandB.o;
            this.b = oandB.b;
        }

        public OandB(List<ObjectTableItem> o, List<BiPointerTableItem> b) {
            this.o = o;
            this.b = b;
        }
    }


    private Tuple GetTuple(int id, int offset) {

        return mem.readTuple(id, offset);
    }

    private int[] InsertTuple(Tuple tuple) {
        return mem.writeTuple(tuple);
    }

    private void DeleteTuple(int id, int offset) {
        mem.deleteTuple();
        return;
    }

    private void UpateTuple(Tuple tuple, int blockid, int offset) {
        mem.UpateTuple(tuple, blockid, offset);
    }

    private void PrintTab(ObjectTable topt, SwitchingTable switchingT, DeputyTable deputyt, BiPointerTable biPointerT, ClassTable classTable) {
        Intent intent = new Intent(context, ShowTable.class);

        Bundle bundle0 = new Bundle();
        bundle0.putSerializable("ObjectTable", topt);
        bundle0.putSerializable("SwitchingTable", switchingT);
        bundle0.putSerializable("DeputyTable", deputyt);
        bundle0.putSerializable("BiPointerTable", biPointerT);
        bundle0.putSerializable("ClassTable", classTable);
        intent.putExtras(bundle0);
        context.startActivity(intent);


    }

    private void PrintSelectResult(TupleList tpl, String[] attrname, int[] attrid, String[] type) {
        Intent intent = new Intent(context, PrintResult.class);


        Bundle bundle = new Bundle();
        bundle.putSerializable("tupleList", tpl);
        bundle.putStringArray("attrname", attrname);
        bundle.putIntArray("attrid", attrid);
        bundle.putStringArray("type", type);
        intent.putExtras(bundle);
        context.startActivity(intent);


    }

    public void PrintTab() {
        PrintTab(topt, switchingT, deputyt, biPointerT, classt);
    }
}

