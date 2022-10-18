package teacherInfo;

import com.zhangyingwei.cockroach.executer.response.TaskResponse;
import com.zhangyingwei.cockroach.store.IStore;
import org.apache.poi.xssf.usermodel.*;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TeacherInfoStore implements IStore {
    private int time;//爬取网页的序号
    private XSSFWorkbook outBook;//输出教师信息
    private XSSFWorkbook dirBook;//输出统计方向
    private XSSFSheet dirSheet;//所有任务共用一页统计方向
    private List<String> allDirs;//记录所有任务的细分研究方向
    private List<Teacher> allTeachers;//记录所有任务的教师信息

    public TeacherInfoStore() {
        time = 0;
        allDirs = new ArrayList<>();
        allTeachers = new ArrayList<>();
        outBook = new XSSFWorkbook();
        dirBook = new XSSFWorkbook();
        dirSheet = dirBook.createSheet("统计方向");
    }

    @Override
    public void store(TaskResponse response) throws Exception {
        String content = response.getContent().string();//爬取的HTML文档
        String school = response.getTask().getGroup();//目标网站的学校名

        Pattern namePtn = null;
        Pattern titlePtn = null;
        Pattern dirLinePtn = null;

        //根据不同学校的页面创建不同的正则表达式
        switch (school) {
            case "武汉大学":
                namePtn = Pattern.compile("id=[2-9][0-9][0-9]\">([^<]*)");
                titlePtn = Pattern.compile("class=\"w4\">(.*)<");
                dirLinePtn = Pattern.compile("class=\"w5\">(.*)<");
                break;
            case "西安交通大学":
                namePtn = Pattern.compile("<h3>(.*)&nbsp");
                titlePtn = Pattern.compile("&nbsp; \\((.*)\\)");
                dirLinePtn = Pattern.compile("研究方向：</h4>\r\n                                     <p>(.*)<");
                break;
            case "南开大学":
                namePtn = Pattern.compile("page.htm\">(.*)<");
                titlePtn = Pattern.compile("8%\">(.*)<");
                dirLinePtn = Pattern.compile("研究方向\">(.*)<");
                break;
        }
        Matcher nameMtr = namePtn.matcher(content);
        Matcher titleMtr = titlePtn.matcher(content);
        Matcher dirLineMtr = dirLinePtn.matcher(content);

        //丢弃多余数据(根据正则表达式)
        if (school.equals("武汉大学")) {
            titleMtr.find();
            dirLineMtr.find();
        }

        //DEBUG:测试正则表达式的正确性
//        System.out.println(nameMtr.find());
//        System.out.println(titleMtr.find());
//        System.out.println(dirMtr.find());

        //创建outBook sheet,页数为当前任务的爬取序号g
        XSSFSheet outSheet = outBook.createSheet();
        outBook.setSheetName(time++, school);

        //创建outBook表头
        String[] outHeads = {"姓名", "职称", "研究方向"};
        XSSFRow outFirstRow = outSheet.createRow(0);
        for (int i = 0; i < outHeads.length; i++) {
            XSSFCell cell = outFirstRow.createCell(i);
            cell.setCellValue(outHeads[i]);
        }

        //创建当前任务的教师队列
        List<Teacher> teachers = new ArrayList<>();

        //将匹配到的所有信息入队
        while (nameMtr.find() && titleMtr.find() && dirLineMtr.find()) {
            //将每位老师的研究方向拆分
            String line = dirLineMtr.group(1);
            Pattern dirsPtn = Pattern.compile("[\\u4e00-\\u9fa5]+");
            Matcher dirsMtr = dirsPtn.matcher(line);
            List<String> dirs = new ArrayList<>();
            while (dirsMtr.find()) {
                if (!allDirs.contains(dirsMtr.group(0)))
                    allDirs.add(dirsMtr.group(0));//记录所有不同的研究方向
                dirs.add(dirsMtr.group(0));
            }
            teachers.add(new Teacher(nameMtr.group(1), titleMtr.group(1), dirs));//当前任务的队列
            allTeachers.add(new Teacher(nameMtr.group(1), titleMtr.group(1), dirs));//所有任务的队列
        }

        //将教师信息数据输入outBook
        for (int i = 0; i < teachers.size(); i++) {
            //创建行,行数从1开始（0为表头）
            XSSFRow row = outSheet.createRow(i + 1);
            //写入单元格内容
            Teacher teacher = teachers.get(i);
            outBook.createCellStyle();
            row.createCell(0).setCellValue(teacher.getName());
            row.createCell(1).setCellValue(teacher.getTitle());
            //研究方向字符可能过长，设置自动换行
            for (int j = 0; j < teacher.getDirections().size(); j++) {
                XSSFCell cell = row.createCell(j + 2);
                XSSFCellStyle style = outBook.createCellStyle();
                style.setWrapText(true);
                cell.setCellStyle(style);
                cell.setCellValue(teacher.getDirections().get(j));
            }
        }

        //创建dirBook表头
        String[] dirHeads = {"研究方向", "教师名单"};
        XSSFRow dirFirstRow = dirSheet.createRow(0);
        for (int i = 0; i < dirHeads.length; i++) {
            XSSFCell cell = dirFirstRow.createCell(i);
            cell.setCellValue(dirHeads[i]);
        }

        //统计研究方向，输入到dirBook
        //每次调用store()都会覆盖上一次的内容
        int rowNum = 1;//从第一行开始
        for (String curDir : allDirs) {
            XSSFRow row = dirSheet.createRow(rowNum++);
            XSSFCell cell = row.createCell(0);
            XSSFCellStyle style = dirBook.createCellStyle();
            style.setWrapText(true);//设置自动换行
            cell.setCellStyle(style);
            cell.setCellValue(curDir);
            int matchNum = 1;//匹配到的教师的输出指针,每行重置
            for (Teacher teacher : allTeachers)
                for (String dir : teacher.getDirections())
                    //遍历所有教师的所有研究方向，如果与当前方向相同则将教师名字添加到当前名单
                    if (curDir.equals(dir)) {
                        row.createCell(matchNum++).setCellValue(teacher.getName());
                        break;
                    }
        }

        //写入目的文件
        outBook.write(Files.newOutputStream(Paths.get("D:\\teachers.xlsx")));
        dirBook.write(Files.newOutputStream(Paths.get("D:\\directions.xlsx")));
        System.out.println("-----------------------------输出成功！-----------------------------");
    }

}

