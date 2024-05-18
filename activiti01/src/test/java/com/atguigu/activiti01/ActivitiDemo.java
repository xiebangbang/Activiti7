package com.atguigu.activiti01;

import org.activiti.engine.*;
import org.activiti.engine.history.HistoricActivityInstance;
import org.activiti.engine.history.HistoricActivityInstanceQuery;
import org.activiti.engine.repository.Deployment;
import org.activiti.engine.repository.ProcessDefinition;
import org.activiti.engine.repository.ProcessDefinitionQuery;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.task.Task;
import org.apache.commons.io.IOUtils;
import org.junit.Test;

import java.io.*;
import java.util.List;

public class ActivitiDemo {

    @Test
    public void testDeployment(){
        //创建ProcessEngine
        ProcessEngine processEngine = ProcessEngines.getDefaultProcessEngine();
        //得到RepositoryService实例
        RepositoryService repositoryService = processEngine.getRepositoryService();
        Deployment deployment = repositoryService.createDeployment()
                .addClasspathResource("bpmn/evection.bpmn20.xml")
                .addClasspathResource("bpmn/evection.png")
                .name("出差申请流程")
                .deploy();

        //输出部署信息
        System.out.println("流程部署ID:"+deployment.getId());
        System.out.println("流程部署名称"+deployment.getName());

    }

    /**
     * 启动流程实例
     */
    @Test
    public void testStartProcess(){
        ProcessEngine processEngine = ProcessEngines.getDefaultProcessEngine();
        RuntimeService runtimeService = processEngine.getRuntimeService();
        ProcessInstance evection = runtimeService.startProcessInstanceByKey("evection");

        System.out.println("流程实例ID:"+evection.getId());
        System.out.println("流程定义ID:"+evection.getProcessDefinitionId());
        System.out.println("当前活动ID:"+evection.getActivityId());
    }

    /**
     * 查询当前个人待执行的任务
     */
    @Test
    public void testFindPersonalTaskList(){
        ProcessEngine processEngine = ProcessEngines.getDefaultProcessEngine();
        TaskService taskService = processEngine.getTaskService();
        List<Task> list = taskService.createTaskQuery()
                .processDefinitionKey("evection")
                .taskAssignee("zhangsan")
                .list();

        for (Task task : list) {
            System.out.println("流程实例ID:"+task.getProcessInstanceId());
            System.out.println("任务id:"+task.getId());
            System.out.println("任务负责人："+task.getAssignee());
            System.out.println("任务名称："+task.getName());
        }
    }

    /**
     * 流程任务处理
     */
    @Test
    public void testCompleteTask(){
        ProcessEngine processEngine = ProcessEngines.getDefaultProcessEngine();
        TaskService taskService = processEngine.getTaskService();
        Task task = taskService.createTaskQuery()
                .processDefinitionKey("evection")
                .taskAssignee("jerry")
                .singleResult();
        taskService.complete(task.getId());
    }

    /**
     * 查询流程定义
     */
    @Test
    public void testQueryProcessDefinition(){
        ProcessEngine processEngine = ProcessEngines.getDefaultProcessEngine();
        RepositoryService repositoryService = processEngine.getRepositoryService();
        ProcessDefinitionQuery processDefinitionQuery = repositoryService.createProcessDefinitionQuery();
        List<ProcessDefinition> definitionList = processDefinitionQuery
                .processDefinitionKey("evection")
                .orderByProcessDefinitionVersion()
                .desc()
                .list();

        for(ProcessDefinition processDefinition : definitionList){
            System.out.println("流程定义 id:"+processDefinition.getId());
            System.out.println("流程定义 name:"+processDefinition.getName());
            System.out.println("流程定义 key:"+processDefinition.getKey());
            System.out.println("流程定义 version:"+processDefinition.getVersion());
            System.out.println("流程部署ID:"+processDefinition.getDeploymentId());
        }
    }

    /**
     * 流程删除
     */
    @Test
    public void testDeleteDeployment(){
        ProcessEngine processEngine = ProcessEngines.getDefaultProcessEngine();
        RepositoryService repositoryService = processEngine.getRepositoryService();
        //删除流程定义，如果该流程定义已有流程实例启动则删除出错
        repositoryService.deleteDeployment("1");
        //设置true，级联删除流程定义，即使该流程有流程实例启动也可以删除
        repositoryService.deleteDeployment("2",true);
    }

    /**
     * 流程资源下载
     */
    @Test
    public void queryBpmnFile() throws IOException {
        ProcessEngine processEngine = ProcessEngines.getDefaultProcessEngine();
        RepositoryService repositoryService = processEngine.getRepositoryService();
        ProcessDefinition processDefinition = repositoryService.createProcessDefinitionQuery()
                .processDefinitionKey("evection")
                .singleResult();
        String deploymentId = processDefinition.getDeploymentId();

        //png图片的流和bpmn文件的流
        InputStream pngInput = repositoryService.getResourceAsStream(deploymentId, processDefinition.getDiagramResourceName());
        InputStream bpmnInput = repositoryService.getResourceAsStream(deploymentId, processDefinition.getResourceName());

        //构造outputstream的流
        File file_png = new File("d:/evection01.png");
        File file_bpmn = new File("d:/evection01.xml");

        FileOutputStream bpmnOut = new FileOutputStream(file_bpmn);
        FileOutputStream pngOut = new FileOutputStream(file_png);

        //输入流、输出流的转换
        IOUtils.copy(pngInput,pngOut);
        IOUtils.copy(bpmnInput,bpmnOut);

        //关闭流
        pngOut.close();
        bpmnOut.close();
        pngInput.close();
        bpmnInput.close();

    }

    /**
     * 查看流程历史信息
     */
    @Test
    public void findHistoryInfo(){
        ProcessEngine processEngine = ProcessEngines.getDefaultProcessEngine();
        HistoryService historyService = processEngine.getHistoryService();
        HistoricActivityInstanceQuery instanceQuery = historyService.createHistoricActivityInstanceQuery();

        instanceQuery.processDefinitionId("evection:1:4");
        instanceQuery.orderByHistoricActivityInstanceStartTime().asc();

        List<HistoricActivityInstance> activityInstanceList = instanceQuery.list();

        for (HistoricActivityInstance hi : activityInstanceList){
            System.out.println(hi.getActivityId());
            System.out.println(hi.getActivityName());
            System.out.println(hi.getProcessDefinitionId());
            System.out.println(hi.getProcessInstanceId());
            System.out.println("<==========================>");
        }
    }

}
