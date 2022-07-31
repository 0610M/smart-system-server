package org.jeecg.modules.smartAssessmentContent.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.google.common.base.Joiner;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.beanutils.PropertyUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.shiro.SecurityUtils;
import org.jeecg.common.api.vo.Result;
import org.jeecg.common.aspect.annotation.AutoLog;
import org.jeecg.common.system.api.ISysBaseAPI;
import org.jeecg.common.system.base.controller.JeecgController;
import org.jeecg.common.system.query.QueryGenerator;
import org.jeecg.common.system.vo.DictModel;
import org.jeecg.common.system.vo.LoginUser;
import org.jeecg.common.system.vo.SysDepartModel;
import org.jeecg.common.util.oConvertUtils;
import org.jeecg.modules.common.service.CommonService;
import org.jeecg.modules.smartAnswerInfo.entity.SmartAnswerInfo;
import org.jeecg.modules.smartAnswerInfo.service.ISmartAnswerInfoService;
import org.jeecg.modules.smartAssessmentContent.entity.SmartAssessmentContent;
import org.jeecg.modules.smartAssessmentContent.service.ISmartAssessmentContentService;
import org.jeecg.modules.smartAssessmentDepartment.entity.SmartAssessmentDepartment;
import org.jeecg.modules.smartAssessmentDepartment.service.ISmartAssessmentDepartmentService;
import org.jeecg.modules.smartAssessmentMission.entity.SmartAssessmentDepart;
import org.jeecg.modules.smartAssessmentMission.entity.SmartAssessmentMission;
import org.jeecg.modules.smartAssessmentMission.service.ISmartAssessmentMissionService;
import org.jeecg.modules.smartAssessmentTeam.entity.SmartAssessmentTeam;
import org.jeecg.modules.smartAssessmentTeam.service.ISmartAssessmentTeamService;
import org.jeecg.modules.smartFinanceResult.entity.SmartFinanceResult;
import org.jeecg.modules.smartRankVisible.entity.SmartRankVisible;
import org.jeecg.modules.smartRankVisible.service.ISmartRankVisibleService;
import org.jeecgframework.poi.excel.ExcelImportUtil;
import org.jeecgframework.poi.excel.def.NormalExcelConstants;
import org.jeecgframework.poi.excel.entity.ExportParams;
import org.jeecgframework.poi.excel.entity.ImportParams;
import org.jeecgframework.poi.excel.view.JeecgEntityExcelView;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

/**
 * @Description: 考核节点表
 * @Author: jeecg-boot
 * @Date: 2022-02-12
 * @Version: V1.0
 */
@Api(tags = "考核节点表")
@RestController
@RequestMapping("/smartAssessmentContent/smartAssessmentContent")
@Slf4j
public class SmartAssessmentContentController extends JeecgController<SmartAssessmentContent, ISmartAssessmentContentService> {
    @Autowired
    private ISmartAssessmentContentService smartAssessmentContentService;

    @Autowired
    private ISmartAssessmentMissionService smartAssessmentMissionService;

    @Autowired
    private ISmartRankVisibleService smartRankVisibleService;

    @Autowired
    private ISmartAssessmentDepartmentService smartAssessmentDepartmentService;

    @Autowired
    private ISmartAssessmentTeamService smartAssessmentTeamService;

    @Autowired
    private ISmartAnswerInfoService smartAnswerInfoService;

    @Autowired
    ISysBaseAPI iSysBaseAPI;

    @Value("${jeecg.path.upload}")
    private String upLoadPath;

    /**
     * 分页列表查询
     *
     * @param id
     * @return
     */
    @AutoLog(value = "考核节点表-根据ID分页列表查询")
    @ApiOperation(value = "考核节点表-根据ID分页列表查询", notes = "答题信息表-根据ID分页列表查询")
    @GetMapping(value = "/list")
    public Result<?> queryPageList(@RequestParam("id") String id) {
        try {
            QueryWrapper<SmartAssessmentContent> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("id", id);
            List<SmartAssessmentContent> list = smartAssessmentContentService.list(queryWrapper);
            IPage<SmartAssessmentContent> pageList = new Page<>(1, 10, list.size());
            pageList.setRecords(list);
            return Result.OK(pageList);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return Result.error("查询节点失败：" + e.getMessage());
        }
    }

    /**
     * 分页列表查询
     *
     * @return
     */
    @AutoLog(value = "考核节点表-查询负责的考核要点")
    @ApiOperation(value = "考核节点表-查询负责的考核要点", notes = "考核节点表-查询负责的考核要点")
    @GetMapping(value = "/listInCharge")
    public Result<?> listInCharge(@RequestParam("missionId") String missionId,
                                  @RequestParam(name = "roleType", defaultValue = "depart") String roleType,
                                  @RequestParam(name = "roleId") String roleId) {
        try {
            QueryWrapper<SmartAssessmentContent> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("mission_id", missionId);
            queryWrapper.eq("is_key", 1);
            Integer responsibleAmount = 0;
            String departIds = "";

            if ("depart".equals(roleType)) {
                queryWrapper.like("ass_depart", roleId);
                SmartAssessmentDepartment assessmentDepartment = smartAssessmentDepartmentService.getById(roleId);
                if (assessmentDepartment != null) {
                    responsibleAmount = assessmentDepartment.getResponsibleAmount();
                    departIds = assessmentDepartment.getResponsibleDepart();
                }
            } else {
                queryWrapper.like("ass_team", roleId);
                SmartAssessmentTeam assessmentTeam = smartAssessmentTeamService.getById(roleId);
                if (assessmentTeam != null) {
                    responsibleAmount = assessmentTeam.getDepartAmount();
                    departIds = assessmentTeam.getDeparts();
                }
            }
            List<SmartAssessmentContent> list = smartAssessmentContentService.list(queryWrapper);
            // 查询每个考核要点的数目
            for (SmartAssessmentContent content : list) {
                QueryWrapper<SmartAnswerInfo> answerInfoQueryWrapper = new QueryWrapper<>();
                answerInfoQueryWrapper.eq("mission_id", missionId);
                answerInfoQueryWrapper.in("depart", Arrays.asList(departIds.split(",")));
                answerInfoQueryWrapper.notLike("marked_content", content.getId() + '_' + roleId);
                long count = smartAnswerInfoService.count(answerInfoQueryWrapper);
                content.setSortNo((double) count);
            }

            return Result.OK(list);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return Result.error("查询节点失败");
        }
    }

    /**
     * 分页列表查询
     *
     * @param smartAssessmentContent
     * @param pageNo
     * @param pageSize
     * @param req
     * @return
     */
    @AutoLog(value = "考核节点表-分页列表查询")
    @ApiOperation(value = "考核节点表-分页列表查询", notes = "考核节点表-分页列表查询")
    @GetMapping(value = "/rootList")
    public Result<?> queryRootList(SmartAssessmentContent smartAssessmentContent,
                                   @RequestParam(name = "pageNo", defaultValue = "1") Integer pageNo,
                                   @RequestParam(name = "pageSize", defaultValue = "10") Integer pageSize,
                                   HttpServletRequest req) {
        if (oConvertUtils.isEmpty(smartAssessmentContent.getMissionId())) {
            return Result.error("考核任务id不能为空");
        }
        String hasQuery = req.getParameter("hasQuery");
        if (hasQuery != null && "true".equals(hasQuery)) {
            QueryWrapper<SmartAssessmentContent> queryWrapper = QueryGenerator.initQueryWrapper(smartAssessmentContent, req.getParameterMap());
            List<SmartAssessmentContent> list = smartAssessmentContentService.queryTreeListNoPage(queryWrapper);
            IPage<SmartAssessmentContent> pageList = new Page<>(1, 10, list.size());
            pageList.setRecords(list);
            return Result.OK(pageList);
        } else {
            String parentId = smartAssessmentContent.getPid();
            if (oConvertUtils.isEmpty(parentId)) {
                parentId = "0";
            }
            smartAssessmentContent.setPid(null);
            QueryWrapper<SmartAssessmentContent> queryWrapper = QueryGenerator.initQueryWrapper(smartAssessmentContent, req.getParameterMap());
            // 使用 eq 防止模糊查询
            queryWrapper.eq("pid", parentId);
            Page<SmartAssessmentContent> page = new Page<SmartAssessmentContent>(pageNo, pageSize);
            IPage<SmartAssessmentContent> pageList = smartAssessmentContentService.page(page, queryWrapper);
            return Result.OK(pageList);
        }
    }

    /**
     * 获取子数据
     *
     * @param smartAssessmentContent
     * @param req
     * @return
     */
    @AutoLog(value = "考核节点表-获取子数据")
    @ApiOperation(value = "考核节点表-获取子数据", notes = "考核节点表-获取子数据")
    @GetMapping(value = "/childList")
    public Result<?> queryPageList(SmartAssessmentContent smartAssessmentContent, HttpServletRequest req) {
        QueryWrapper<SmartAssessmentContent> queryWrapper = QueryGenerator.initQueryWrapper(smartAssessmentContent, req.getParameterMap());
        List<SmartAssessmentContent> list = smartAssessmentContentService.list(queryWrapper);
        IPage<SmartAssessmentContent> pageList = new Page<>(1, 10, list.size());
        pageList.setRecords(list);
        return Result.OK(pageList);
    }

    /**
     * 批量查询子节点
     *
     * @param parentIds 父ID（多个采用半角逗号分割）
     * @param parentIds
     * @return 返回 IPage
     * @return
     */
    @AutoLog(value = "考核节点表-批量获取子数据")
    @ApiOperation(value = "考核节点表-批量获取子数据", notes = "考核节点表-批量获取子数据")
    @GetMapping("/getChildListBatch")
    public Result getChildListBatch(@RequestParam("parentIds") String parentIds) {
        try {
            QueryWrapper<SmartAssessmentContent> queryWrapper = new QueryWrapper<>();
            List<String> parentIdList = Arrays.asList(parentIds.split(","));
            queryWrapper.in("pid", parentIdList);
            List<SmartAssessmentContent> list = smartAssessmentContentService.list(queryWrapper);
            IPage<SmartAssessmentContent> pageList = new Page<>(1, 10, list.size());
            pageList.setRecords(list);
            return Result.OK(pageList);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return Result.error("批量查询子节点失败：" + e.getMessage());
        }
    }

    /**
     * 查询字典值
     *
     * @param req
     * @return
     */
    @AutoLog(value = "考核节点表-查询字典值")
    @ApiOperation(value="考核节点表-查询字典值", notes="考核节点表-查询字典值")
    @GetMapping(value = "/dict")
    public Result<?> queryDicList(@RequestParam("level") String level,
                                  @RequestParam("missionId") String missionId,
                                  HttpServletRequest req) {
        QueryWrapper<SmartAssessmentContent> queryWrapper = new QueryWrapper<>();
        queryWrapper.select("id", "name");
        queryWrapper.eq("is_key", "0");
        queryWrapper.eq("mission_id", missionId);
        if ("1".equals(level)) {
            queryWrapper.eq("pid", "0");
        } else if ("2".equals(level)) {
            queryWrapper.ne("pid", "0");
        } else {
            return Result.error("查询字典值失败：查询级别错误");
        }
        List<SmartAssessmentContent> list = smartAssessmentContentService.list(queryWrapper);
        return Result.OK(list);
    }

    /**
     * 查询某考核内容负责评分的考核单位
     *
     * @param req
     * @return
     */
    @AutoLog(value = "考核节点表-查询某考核内容负责评分的考核单位")
    @ApiOperation(value="考核节点表-查询某考核内容负责评分的考核单位", notes="考核节点表-查询某考核内容负责评分的考核单位")
    @GetMapping(value = "/departmentDictByContentId")
    public Result<?> queryDepartmentDicListByContentId(@RequestParam(name="contentId", required = true) String contentId,
                                             HttpServletRequest req) {
        SmartAssessmentContent assessmentContent = smartAssessmentContentService.getById(contentId);
        if (assessmentContent == null) {
            return Result.error("查询某考核内容负责评分的考核单位失败：考核内容不存在");
        }
        String assDepart = assessmentContent.getAssDepart();
        if (StringUtils.isEmpty(assDepart)) {
            return Result.error("查询某考核内容负责评分的考核单位失败：考核内容负责评分的考核单位为空");
        }
        QueryWrapper<SmartAssessmentDepartment> queryWrapper = new QueryWrapper<>();
        queryWrapper.in("id", Arrays.asList(assDepart.split(",")));
        List<SmartAssessmentDepartment> list = smartAssessmentDepartmentService.list(queryWrapper);
        Page<SmartAssessmentDepartment> pageList = new Page<>(1, list.size(), list.size());
        pageList.setRecords(list);

        return Result.OK(pageList);
    }

    /**
     * 查询某考核内容负责评分的考核组
     *
     * @param req
     * @return
     */
    @AutoLog(value = "考核节点表-查询某考核内容负责评分的考核组")
    @ApiOperation(value="考核节点表-查询某考核内容负责评分的考核组", notes="考核节点表-查询某考核内容负责评分的考核组")
    @GetMapping(value = "/teamDictByContentId")
    public Result<?> queryTeamDicListByContentId(@RequestParam(name="contentId", required = true) String contentId,
                                                       HttpServletRequest req) {
        SmartAssessmentContent assessmentContent = smartAssessmentContentService.getById(contentId);
        if (assessmentContent == null) {
            return Result.error("查询某考核内容负责评分的考核组失败：考核内容不存在");
        }
        String assTeam = assessmentContent.getAssTeam();
        if (StringUtils.isEmpty(assTeam)) {
            return Result.error("查询某考核内容负责评分的考核组失败：考核内容负责评分的考核组为空");
        }
        QueryWrapper<SmartAssessmentTeam> queryWrapper = new QueryWrapper<>();
        queryWrapper.in("id", Arrays.asList(assTeam.split(",")));
        List<SmartAssessmentTeam> list = smartAssessmentTeamService.list(queryWrapper);
        return Result.OK(list);
    }

    /**
     * 添加
     *
     * @param smartAssessmentContent
     * @return
     */
    @AutoLog(value = "考核节点表-添加")
    @ApiOperation(value = "考核节点表-添加", notes = "考核节点表-添加")
    @PostMapping(value = "/add")
    @Transactional(rollbackFor = Exception.class)
    public Result<?> add(@RequestBody SmartAssessmentContent smartAssessmentContent) {
        smartAssessmentContentService.addSmartAssessmentContent(smartAssessmentContent);
        // 配置考核排名字段是否可见
        Integer sortNum = 1;
        if (Objects.equals(smartAssessmentContent.getPid(), "0")) {
            SmartRankVisible smartRankVisible = new SmartRankVisible();
            smartRankVisible.setMissionId(smartAssessmentContent.getMissionId());
            smartRankVisible.setContentId(smartAssessmentContent.getId());
            smartRankVisible.setContentName(smartAssessmentContent.getName());
            smartRankVisible.setVisible("1");
            smartRankVisible.setTag("content");
            smartRankVisible.setSort(sortNum);
            smartRankVisibleService.save(smartRankVisible);
            sortNum++;
        }
        // 如果是考核要点,则自动向上级增加分数
        Integer point = smartAssessmentContent.getPoint();
        QueryWrapper<SmartAssessmentContent> queryWrapper = new QueryWrapper<>();

        if (smartAssessmentContent.getIsKey() == 1) {
            updateSuperiorPoint(smartAssessmentContent, point);
        }
        return Result.OK("添加成功！");
    }

    /**
     * 校对分数
     *
     * @param missionId
     * @return
     */
    @AutoLog(value = "考核节点表-校对分数")
    @ApiOperation(value = "考核节点表-校对分数", notes = "考核节点表-校对分数")
    @GetMapping(value = "/checkPoint")
    @Transactional(rollbackFor = Exception.class)
    public Result<?> checkPoint(@RequestParam(name = "missionId", required = true) String missionId) {
        // 查询数据库中的数据
        QueryWrapper<SmartAssessmentContent> queryWrapper = new QueryWrapper<>();
        SmartAssessmentContent updateContent = new SmartAssessmentContent();
        updateContent.setPoint(0);
        queryWrapper.eq("mission_id", missionId).eq("is_key", 0);
        smartAssessmentContentService.update(updateContent, queryWrapper);

        // 重置考核任务总分数
        SmartAssessmentMission mission = smartAssessmentMissionService.getById(missionId);
        mission.setTotalPoint(0);
        smartAssessmentMissionService.updateById(mission);

        queryWrapper.clear();
        queryWrapper.eq("mission_id", missionId).eq("is_key", 1);
        List<SmartAssessmentContent> assessmentContentList = smartAssessmentContentService.list(queryWrapper);
        assessmentContentList.forEach(smartAssessmentContent -> {
            updateSuperiorPoint(smartAssessmentContent, smartAssessmentContent.getPoint());
        });

        return Result.OK("校正成功!");
    }

    /**
     * 编辑
     *
     * @param smartAssessmentContent
     * @return
     */
    @AutoLog(value = "考核节点表-编辑")
    @ApiOperation(value = "考核节点表-编辑", notes = "考核节点表-编辑")
    @PutMapping(value = "/edit")
    @Transactional(rollbackFor = Exception.class)
    public Result<?> edit(@RequestBody SmartAssessmentContent smartAssessmentContent) {
        // 查询数据库中的数据
        QueryWrapper<SmartAssessmentContent> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("id", smartAssessmentContent.getId());
        SmartAssessmentContent lastOne = smartAssessmentContentService.getOne(queryWrapper);

        if (lastOne.getIsKey() - smartAssessmentContent.getIsKey() == 1) {
            // 如果是由考核要点变成不是考核要点
            smartAssessmentContent.setInstructions(null);
            smartAssessmentContent.setPoint(0);
            smartAssessmentContent.setAssDepart(null);
            smartAssessmentContent.setAssTeam(null);
        }

        // 与更新的数据相减,得出增量
        int increment = smartAssessmentContent.getPoint() - lastOne.getPoint();

        // 先更新信息到数据库
        smartAssessmentContentService.updateSmartAssessmentContent(smartAssessmentContent);

        // 如果分数有变化则更新上级分数和总分
        if (increment != 0) {
            updateSuperiorPoint(smartAssessmentContent, increment);
        }
        return Result.OK("编辑成功!");
    }

    /**
     * 更新上级分数
     *
     * @param smartAssessmentContent
     * @param increment              分数增量
     */
    @Transactional(rollbackFor = Exception.class)
    void updateSuperiorPoint(SmartAssessmentContent smartAssessmentContent, int increment) {
        // 更新考核任务总分
        SmartAssessmentMission mission = smartAssessmentMissionService.getById(smartAssessmentContent.getMissionId());
        mission.setTotalPoint(mission.getTotalPoint() + increment);
        smartAssessmentMissionService.updateById(mission);

        QueryWrapper<SmartAssessmentContent> queryWrapper = new QueryWrapper<>();
        while (oConvertUtils.isNotEmpty(smartAssessmentContent.getPid())) {
            // 查找该考核要点的上级
            queryWrapper.clear();
            queryWrapper.eq("id", smartAssessmentContent.getPid());
            smartAssessmentContent = smartAssessmentContentService.getOne(queryWrapper);
            if (oConvertUtils.isEmpty(smartAssessmentContent)) {
                break;
            }
            smartAssessmentContent.setPoint(smartAssessmentContent.getPoint() + increment);
            smartAssessmentContentService.updateSmartAssessmentContent(smartAssessmentContent);
        }
    }

    /**
     * 通过id删除
     *
     * @param id
     * @return
     */
    @AutoLog(value = "考核节点表-通过id删除")
    @ApiOperation(value = "考核节点表-通过id删除", notes = "考核节点表-通过id删除")
    @DeleteMapping(value = "/delete")
    @Transactional(rollbackFor = Exception.class)
    public Result<?> delete(@RequestParam(name = "id", required = true) String id) {
        // 查询数据库中的数据
        QueryWrapper<SmartAssessmentContent> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("id", id);
        SmartAssessmentContent smartAssessmentContent = smartAssessmentContentService.getOne(queryWrapper);
        if (oConvertUtils.isNotEmpty(smartAssessmentContent)) {
            updateSuperiorPoint(smartAssessmentContent, smartAssessmentContent.getPoint() * -1);
        }
        smartAssessmentContentService.deleteSmartAssessmentContent(id);
        // 同步删除排名字段可见表中的内容
        QueryWrapper<SmartRankVisible> rankVisibleQueryWrapper = new QueryWrapper<>();
        rankVisibleQueryWrapper.eq("content_id", id);
        smartRankVisibleService.remove(rankVisibleQueryWrapper);
        return Result.OK("删除成功!");
    }

    /**
     * 批量删除
     *
     * @param ids
     * @return
     */
    @AutoLog(value = "考核节点表-批量删除")
    @ApiOperation(value = "考核节点表-批量删除", notes = "考核节点表-批量删除")
    @DeleteMapping(value = "/deleteBatch")
    @Transactional(rollbackFor = Exception.class)
    public Result<?> deleteBatch(@RequestParam(name = "ids", required = true) String ids) {
        this.smartAssessmentContentService.removeByIds(Arrays.asList(ids.split(",")));
        return Result.OK("批量删除成功！");
    }

    /**
     * 通过id查询
     *
     * @param id
     * @return
     */
    @AutoLog(value = "考核节点表-通过id查询")
    @ApiOperation(value = "考核节点表-通过id查询", notes = "考核节点表-通过id查询")
    @GetMapping(value = "/queryById")
    public Result<?> queryById(@RequestParam(name = "id", required = true) String id) {
        SmartAssessmentContent smartAssessmentContent = smartAssessmentContentService.getById(id);
        if (smartAssessmentContent == null) {
            return Result.error("未找到对应数据");
        }
        return Result.OK(smartAssessmentContent);
    }

    /**
     * 导出excel
     *
     * @param request
     * @param smartAssessmentContent
     */
    @RequestMapping(value = "/exportXls")
    public ModelAndView exportXls(HttpServletRequest request, SmartAssessmentContent smartAssessmentContent) {
        // Step.1 组装查询条件
        QueryWrapper<SmartAssessmentContent> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("mission_id", smartAssessmentContent.getMissionId());
        queryWrapper.orderByAsc("pid", "is_key");
//        QueryWrapper<SmartAssessmentContent> queryWrapper = QueryGenerator.initQueryWrapper(smartAssessmentContent, request.getParameterMap());
        LoginUser sysUser = (LoginUser) SecurityUtils.getSubject().getPrincipal();
//        queryWrapper.orderByAsc("pid");
//        queryWrapper.orderByAsc("is_key");

        // Step.2 获取导出数据
        List<SmartAssessmentContent> pageList = service.list(queryWrapper);
        List<SmartAssessmentContent> exportList = null;

        // 过滤选中数据
        String selections = request.getParameter("selections");
        if (oConvertUtils.isNotEmpty(selections)) {
            List<String> selectionList = Arrays.asList(selections.split(","));
            exportList = pageList.stream().filter(item -> selectionList.contains(getId(item))).collect(Collectors.toList());
        } else {
            exportList = pageList;
        }


        for (SmartAssessmentContent item : exportList) {
            if (oConvertUtils.isNotEmpty(item.getAssDepart())) {
                List<String> departList = Arrays.asList(item.getAssDepart().split(","));
                List<String> departNameList = new ArrayList<>();
                for (String departId : departList) {
                    SmartAssessmentDepartment assessmentDepartment = smartAssessmentDepartmentService.getById(departId);
                    if (oConvertUtils.isEmpty(assessmentDepartment)) {
                        continue;
                    }
                    SysDepartModel sysDepartModel = iSysBaseAPI.selectAllById(assessmentDepartment.getDepartId());
                    if (oConvertUtils.isNotEmpty(sysDepartModel)) {
                        departNameList.add(sysDepartModel.getDepartName());
                    }
                }
                item.setAssDepart(Joiner.on(",").join(departNameList));
            }
        }

        // Step.3 AutoPoi 导出Excel
        ModelAndView mv = new ModelAndView(new JeecgEntityExcelView());
        mv.addObject(NormalExcelConstants.FILE_NAME, "考核节点表"); //此处设置的filename无效 ,前端会重更新设置一下
        mv.addObject(NormalExcelConstants.CLASS, SmartAssessmentContent.class);
        //update-begin--Author:liusq  Date:20210126 for：图片导出报错，ImageBasePath未设置--------------------
        ExportParams exportParams = new ExportParams("考核节点表" + "报表", "导出人:" + sysUser.getRealname(), "考核节点表");
        exportParams.setImageBasePath(upLoadPath);
        //update-end--Author:liusq  Date:20210126 for：图片导出报错，ImageBasePath未设置----------------------
        mv.addObject(NormalExcelConstants.PARAMS, exportParams);
        mv.addObject(NormalExcelConstants.DATA_LIST, exportList);
        return mv;
    }

    /**
     * 通过excel导入数据
     *
     * @param request
     * @param response
     * @return
     */
    @RequestMapping(value = "/importExcel", method = RequestMethod.POST)
    @Transactional(rollbackFor = Exception.class)
    public Result<?> importExcel(HttpServletRequest request, HttpServletResponse response) {
        String missionId = request.getParameter("missionId");
        System.out.println(missionId);
        if (oConvertUtils.isEmpty(missionId)) {
            return Result.error("缺少上传参数：考核任务ID");
        }
        MultipartHttpServletRequest multipartRequest = (MultipartHttpServletRequest) request;
        Map<String, MultipartFile> fileMap = multipartRequest.getFileMap();
        LoginUser sysUser = (LoginUser) SecurityUtils.getSubject().getPrincipal();
        for (Map.Entry<String, MultipartFile> entity : fileMap.entrySet()) {
            MultipartFile file = entity.getValue();// 获取上传文件对象
            ImportParams params = new ImportParams();
            params.setTitleRows(2);
            params.setHeadRows(1);
            params.setNeedSave(true);
            try {
                List<SmartAssessmentContent> list = ExcelImportUtil.importExcel(file.getInputStream(), SmartAssessmentContent.class, params);
                //update-begin-author:taoyan date:20190528 for:批量插入数据
                long start = System.currentTimeMillis();
                List<SmartAssessmentContent> successList = new ArrayList<>();
                List<SmartRankVisible> rankVisibleList = new ArrayList<>();

                for (SmartAssessmentContent item : list) {
                    if (!item.getMissionId().equals(missionId)) {
                        return Result.error("文件中的考核任务与选中的考核任务不一致!");
                    }
                }

                for (SmartAssessmentContent item : list) {
                    SmartAssessmentContent content = new SmartAssessmentContent();
                    BeanUtils.copyProperties(item, content);

                    // 4. 父级节点
                    if (oConvertUtils.isNotEmpty(content.getPid()) && !"0".equals(content.getPid())) {
                        SmartAssessmentContent temp = smartAssessmentContentService.getById(content.getPid());

                        for (SmartAssessmentContent successItem : successList) {
                            if (StringUtils.equals(successItem.getName(), temp.getName())) {
                                content.setPid(successItem.getId());
                                break;
                            }
                        }
                    } else {
                        content.setPid("0");
                        Integer sortNum = 1;
                        SmartRankVisible smartRankVisible = new SmartRankVisible();
                        smartRankVisible.setMissionId(content.getMissionId());
                        smartRankVisible.setContentId(content.getId());
                        smartRankVisible.setContentName(content.getName());
                        smartRankVisible.setVisible("1");
                        smartRankVisible.setTag("content");
                        smartRankVisible.setSort(sortNum);
                        rankVisibleList.add(smartRankVisible);
                    }
                    // 考核单位
                    if (oConvertUtils.isNotEmpty(content.getAssDepart())) {
                        List<String> departList = Arrays.asList(content.getAssDepart().split(","));
                        List<String> departIdList = new ArrayList<>();
                        for (String departId : departList) {
                            String assessmentDepartmentId = smartAssessmentDepartmentService.getAssessmentDepartmentIdByDepartName(departId);
                            if (oConvertUtils.isEmpty(assessmentDepartmentId)) {
                                continue;
                            }
                            departIdList.add(assessmentDepartmentId);
                        }
                        content.setAssDepart(Joiner.on(",").join(departIdList));
                    }

                    smartAssessmentContentService.addSmartAssessmentContent(content);
                    successList.add(content);

                }
                smartRankVisibleService.saveBatch(rankVisibleList);

                //400条 saveBatch消耗时间1592毫秒  循环插入消耗时间1947毫秒
                //1200条  saveBatch消耗时间3687毫秒 循环插入消耗时间5212毫秒
                log.info("消耗时间" + (System.currentTimeMillis() - start) + "毫秒");

                //update-end-author:taoyan date:20190528 for:批量插入数据
                return Result.ok("文件导入成功！数据行数：" + list.size());

            } catch (Exception e) {
                //update-begin-author:taoyan date:20211124 for: 导入数据重复增加提示
                String msg = e.getMessage();
                log.error(msg, e);
                if (msg != null && msg.indexOf("Duplicate entry") >= 0) {
                    return Result.error("文件导入失败:有重复数据！");
                } else {
                    return Result.error("文件导入失败:" + e.getMessage());
                }
                //update-end-author:taoyan date:20211124 for: 导入数据重复增加提示
            } finally {
                try {
                    file.getInputStream().close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return Result.error("文件导入失败！");
//        return super.importExcel(request, response, SmartAssessmentContent.class);
    }

    /**
     * 获取对象ID
     *
     * @return
     */
    private String getId(SmartAssessmentContent item) {
        try {
            return PropertyUtils.getProperty(item, "id").toString();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

}
