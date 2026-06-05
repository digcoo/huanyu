//package com.yh.bigdata.silkworm.api.dao;
//
//import com.yh.bigdata.silkworm.api.config.MyBatisConfig;
//import com.yh.bigdata.silkworm.api.datasource.CmsDataSource;
//import com.yh.bigdata.stock.common.constants.JobTriggerType;
//import com.yh.bigdata.stock.common.dao.TaskNewMapper;
//import com.yh.bigdata.stock.common.model.StockBase;
//import com.yh.bigdata.stock.common.model.TaskNew;
//
//import org.junit.Assert;
//import org.junit.Test;
//import org.junit.runner.RunWith;
//import org.mybatis.spring.boot.test.autoconfigure.MybatisTest;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
//import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
//import org.springframework.test.annotation.Rollback;
//import org.springframework.test.context.junit4.SpringRunner;
//import org.springframework.transaction.annotation.Transactional;
//
///**
// * Created by zhou1 on 2019/1/14.
// */
//@RunWith(SpringRunner.class)
////SpringBootTest 是springboot 用于测试的注解，可指定启动类或者测试环境等，这里直接默认。
////@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, properties = "test.env=true")
//@MybatisTest
////跑真实数据库
//@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
//@Transactional
//@ImportAutoConfiguration({MyBatisConfig.class,CmsDataSource.class})
//public class TaskNewMapperTest {
//    private static long mockTaskId;
//
//    @Autowired
//    TaskNewMapper tMapper;
//
//    public static TaskNew generateTestTaskInst(long jobId){
//        long currentId = mockTaskId++;
//
//        TaskNew task = new TaskNew();
//
//        task.setName(String.format("TaskName_%d",currentId));
//        task.setJobId(jobId);
//        task.setType("kettle");
//        task.setScriptFile("testFile");
//
//        task.setIsEnable(true);
//        task.setIsActive(true);
//        task.setIsFailRetry(true);
//        task.setIsFailAlarm(true);
//        task.setIsTimeoutExecAlarm(true);
//        task.setIsTimeoutStartAlarm(true);
//        task.setIsDeadlineStartAlarm(true);
//        task.setIsDeadlineFinishAlarm(true);
//
//        task.setOwner("testOwner");
//        task.setStatus(new Byte("0"));
//
//        task.setCreateAccount("test_user");
//        task.setUpdateAccount("test_user");
//        task.setIsDel(false);
//        task.setCreateTime(java.util.Calendar.getInstance().getTime());
//        task.setUpdateTime(java.util.Calendar.getInstance().getTime());
//
//        return task;
//    }
//
//    //测试插入数据成功
//    @Test
//    @Rollback(true)
//    public void insertJobTest() throws InterruptedException {
//        for(int i = 0; i<5; i++){
//            TaskNew task = generateTestTaskInst(0L);
//
//            int insertResult = tMapper.insert(task);
//            Assert.assertEquals(1,insertResult);
//        }
//    }
//}
