//package com.yh.bigdata.silkworm.api.controller;
//
//import java.util.HashMap;
//
//import org.junit.Before;
//import org.junit.Test;
//import org.springframework.beans.factory.annotation.Autowired;
//
//import com.alibaba.fastjson.JSON;
//import com.yh.bigdata.silkworm.api.BaseTest;
//import com.yh.bigdata.silkworm.api.utils.BeanMapUtil;
//import com.yh.bigdata.stock.common.constants.ConcurrentPeriodType;
//import com.yh.bigdata.stock.common.constants.CronPeriodType;
//import com.yh.bigdata.stock.common.constants.DispatchType;
//import com.yh.bigdata.stock.common.constants.InstanceCreateType;
//import com.yh.bigdata.stock.common.constants.JobTriggerType;
//import com.yh.bigdata.stock.common.dao.JobNewMapper;
//import com.yh.bigdata.stock.common.param.base.Response;
//import com.yh.bigdata.stock.common.param.job.CreateJobVO;
//import com.yh.bigdata.stock.common.param.task.TaskPageQuery;
//
///**
// * @author duyp
// * 
// * @date 2019/01/29
// * 
// * @comment
// */
//public class JobControllerTest extends BaseTest {
//
//	public long max_job_id = 0;
//
//	@Autowired
//	JobNewMapper jobNewMapper;
//
//	@Before
//	public void jobInit() {
//		Long maxId = jobNewMapper.selectMaxId();
//		max_job_id = maxId == null ? 0 : maxId.longValue();
//	}
//
//	@Test
//	public void testAdd() throws Exception {
//		CreateJobVO createJobVO = new CreateJobVO();
//		createJobVO.setName("TEST_JOB_NAME_" + (max_job_id + 1));
//		createJobVO.setClusterId(1l);
//		createJobVO.setTopicId(1l);
//		createJobVO.setLevelId(1l);
//		createJobVO.setIsEnable(true);
//		createJobVO.setIsActive(true);
//		createJobVO.setDispatchType(DispatchType.RUN.getByteCode());
//		createJobVO.setTriggerType(JobTriggerType.CronTrigger.getByteIndex());
//		createJobVO.setInstanceType(InstanceCreateType.NEXTDAY.getByteCode());
//		createJobVO.setIsDepend(false);
//		createJobVO.setDependTriggerInst(""); // ???
//		createJobVO.setConcurrentPeriodType(ConcurrentPeriodType.SELF_CONCURRENT.getByteCode());
//		createJobVO.setConcurrentPeriodJobs("");
//		createJobVO.setCronPeriodType(CronPeriodType.DAY.getByteCode());
//		
//		Response response = getRestTemplate().postForObject(getBaseUrl() + "/job/create", createJobVO, Response.class);
//		System.out.println(JSON.toJSONString(response));
//	}
//
//	@Test
//	public void testFindPage() throws Exception {
//		TaskPageQuery pageQuery = new TaskPageQuery();
//		Response response = getRestTemplate().postForObject(getBaseUrl() + "/job/findPage", pageQuery, Response.class);
//		System.out.println(JSON.toJSONString(response));
//
//	}
//
//
//	@Test
//	public void testFindOne() throws Exception {
//		Response response = getRestTemplate().getForObject(getBaseUrl() + "/job/" + 1, Response.class, new HashMap<>());
//		System.out.println(JSON.toJSONString(response));
//
//	}
//}
