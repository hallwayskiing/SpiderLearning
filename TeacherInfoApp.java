package teacherInfo;

import com.zhangyingwei.cockroach.CockroachContext;
import com.zhangyingwei.cockroach.config.CockroachConfig;
import com.zhangyingwei.cockroach.executer.task.Task;
import com.zhangyingwei.cockroach.queue.TaskQueue;

public class TeacherInfoApp {
    public static void main(String[] args) throws Exception {
        CockroachConfig config = new CockroachConfig();
        config.setAppName("教师信息爬虫");
        config.setThread(1, 200);
        config.setAutoClose(true);
        config.setStore(TeacherInfoStore.class);
        CockroachContext context = new CockroachContext(config);
        TaskQueue queue = TaskQueue.of();
        queue.push(new Task("http://cs.whu.edu.cn/teacher.aspx?showtype=jobtitle&typename=%e6%95%99%e6%8e%88", "武汉大学").retry(5));
        queue.push(new Task("http://www.cs.xjtu.edu.cn/szdw/jsml/js.htm", "西安交通大学").retry(5));
        queue.push(new Task("https://cc.nankai.edu.cn/jswyjy/list.htm", "南开大学").retry(5));
        context.start(queue);
    }
}
