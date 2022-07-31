package org.jeecg.modules.smartAnswerInfo.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.google.common.base.Joiner;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authz.annotation.Logical;
import org.apache.shiro.authz.annotation.RequiresRoles;
import org.jeecg.common.api.vo.Result;
import org.jeecg.common.aspect.annotation.AutoLog;
import org.jeecg.common.system.base.controller.JeecgController;
import org.jeecg.common.system.query.QueryGenerator;
import org.jeecg.common.system.vo.LoginUser;
//import org.jeecg.common.util.DateUtil;
import org.jeecg.common.util.DateUtils;
import org.jeecg.common.util.oConvertUtils;
import org.jeecg.modules.smartAnswerAssContent.entity.SmartAnswerAssContent;
import org.jeecg.modules.smartAnswerAssContent.service.ISmartAnswerAssContentService;
import org.jeecg.modules.smartAnswerInfo.entity.SmartAnswerInfo;
import org.jeecg.modules.smartAnswerInfo.entity.SmartDepartContentScore;
import org.jeecg.modules.smartAnswerInfo.service.ISmartAnswerInfoService;
import org.jeecg.modules.smartAssessmentContent.entity.SmartAssessmentContent;
import org.jeecg.modules.smartAssessmentContent.service.ISmartAssessmentContentService;
import org.jeecg.modules.smartAssessmentDepartment.entity.SmartAssessmentDepartment;
import org.jeecg.modules.smartAssessmentDepartment.service.ISmartAssessmentDepartmentService;
import org.jeecg.modules.smartAssessmentMission.entity.SmartAssessmentDepart;
import org.jeecg.modules.smartAssessmentMission.entity.SmartAssessmentMission;
import org.jeecg.modules.smartAssessmentMission.service.ISmartAssessmentDepartService;
import org.jeecg.modules.smartAssessmentMission.service.ISmartAssessmentMissionService;
import org.jeecg.modules.smartAssessmentTeam.entity.SmartAssessmentTeam;
import org.jeecg.modules.smartAssessmentTeam.service.ISmartAssessmentTeamService;
import org.jeecg.modules.smartOrgMeeting.entity.SmartOrgMeeting;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @Description: 答题信息表
 * @Author: jeecg-boot
 * @Date: 2022-02-21
 * @Version: V1.0
 */
@Api(tags = "答题信息表")
@RestController
@RequestMapping("/smartAnswerInfo/smartAnswerInfo")
@Slf4j
public class SmartAnswerInfoController extends JeecgController<SmartAnswerInfo, ISmartAnswerInfoService> {
    @Autowired
    private ISmartAnswerInfoService smartAnswerInfoService;

    @Autowired
    private ISmartAnswerAssContentService smartAnswerAssContentService;

    @Autowired
    private ISmartAssessmentMissionService smartAssessmentMissionService;

    @Autowired
    private ISmartAssessmentContentService smartAssessmentContentService;

    @Autowired
    private ISmartAssessmentTeamService smartAssessmentTeamService;

    @Autowired
    private ISmartAssessmentDepartmentService smartAssessmentDepartmentService;

    @Autowired
    private ISmartAssessmentDepartService smartAssessmentDepartService;

    /**
     * 分页列表查询本单位正在进行中的的考核任务
     *
     * @param smartAnswerInfo
     * @param pageNo
     * @param pageSize
     * @param req
     * @return
     */
    @AutoLog(value = "答题信息表-分页列表查询正在进行中的的考核任务")
    @ApiOperation(value = "答题信息表-分页列表查询正在进行中的的考核任务", notes = "答题信息表-分页列表查询正在进行中的的考核任务")
    @GetMapping(value = "/list")
    @RequiresRoles("admin")
    public Result<?> queryPageList(SmartAnswerInfo smartAnswerInfo,
                                   @RequestParam(name = "pageNo", defaultValue = "1") Integer pageNo,
                                   @RequestParam(name = "pageSize", defaultValue = "10") Integer pageSize,
                                   HttpServletRequest req) {
        LoginUser sysUser = (LoginUser) SecurityUtils.getSubject().getPrincipal();
        // 被考核单位登录账号查询包含自己的所有考核任务ID
        QueryWrapper<SmartAssessmentDepart> smartAssessmentDepartQueryWrapper = new QueryWrapper<>();
        smartAssessmentDepartQueryWrapper.select("distinct mission_id")
                .eq("assessment_depart", sysUser.getDepartId());
        List<SmartAssessmentDepart> missionList = smartAssessmentDepartService.list(smartAssessmentDepartQueryWrapper);
        List<String> missionIdList = new ArrayList<>();
        missionList.forEach(mission -> {
            missionIdList.add(mission.getMissionId());
        });

        if (missionList.size() == 0) {
            return Result.error("无考核任务！");
        }

        // 查询上面所有考核任务信息
        QueryWrapper<SmartAnswerInfo> queryWrapper = QueryGenerator.initQueryWrapper(smartAnswerInfo, req.getParameterMap());
        // 包含本单位的任务
        queryWrapper.select(SmartAnswerInfo.class,i -> !i.getColumn().equals("total_points")
                        && !i.getColumn().equals("ranking")
                        && !i.getColumn().equals("marked_content")
                        && !i.getColumn().equals("is_show_score"))
                .eq("depart", sysUser.getDepartId()).in("mission_id", missionIdList)
                .ne("mission_status", "发布评分结果");
        Page<SmartAnswerInfo> page = new Page<SmartAnswerInfo>(pageNo, pageSize);
        IPage<SmartAnswerInfo> pageList = smartAnswerInfoService.page(page, queryWrapper);
        return Result.OK(pageList);
    }

    /**
     * 评分时查看被考核单位
     *
     * @param smartAnswerInfo
     * @param type
     * @param pageNo
     * @param pageSize
     * @param req
     * @return
     */
    @AutoLog(value = "答题信息表-分页列表查询")
    @ApiOperation(value = "答题信息表-分页列表查询", notes = "答题信息表-分页列表查询")
    @GetMapping(value = "/listInCharge")
    public Result<?> queryChargePageList(SmartAnswerInfo smartAnswerInfo,
                                         @RequestParam(name = "type", defaultValue = "depart") String type,
                                         @RequestParam(name = "roleId") String roleId,
                                         @RequestParam(name = "contentId") String contentId,
                                         @RequestParam(name = "pageNo", defaultValue = "1") Integer pageNo,
                                         @RequestParam(name = "pageSize", defaultValue = "10") Integer pageSize,
                                         HttpServletRequest req) {
        LoginUser sysUser = (LoginUser) SecurityUtils.getSubject().getPrincipal();

        if (oConvertUtils.isEmpty(roleId)) {
            return Result.error("没有权限！");
        }
        if ("depart".equals(type)) {
            // 检查评分单位和评分人是否匹配
            QueryWrapper<SmartAssessmentDepartment> departmentQueryWrapper = new QueryWrapper<>();
            departmentQueryWrapper.eq("depart_id", sysUser.getDepartId()).eq("id", roleId);
            SmartAssessmentDepartment one = smartAssessmentDepartmentService.getOne(departmentQueryWrapper);
            if (oConvertUtils.isEmpty(one)) {
                return Result.error("权限不正确！");
            }
            // 对考核要点进行权限控制，检查是否在负责范围内
            QueryWrapper<SmartAssessmentContent> contentQueryWrapper = new QueryWrapper<>();
            contentQueryWrapper.eq("mission_id", smartAnswerInfo.getMissionId()).eq("id", contentId).like("ass_depart", one.getId());
            SmartAssessmentContent content = smartAssessmentContentService.getOne(contentQueryWrapper);
            if (oConvertUtils.isEmpty(content)) {
                return Result.error("不负责该考核要点评分！");
            }

            // 只查看所负责的单位
            smartAnswerInfo.setDepart(one.getResponsibleDepart());
        } else {
            // 检查考核组和评分人是否匹配
            QueryWrapper<SmartAssessmentTeam> teamQueryWrapper = new QueryWrapper<>();
            teamQueryWrapper.and(QueryWrapper -> QueryWrapper.eq("team_leader", sysUser.getId())
                    .or().like("deputy_team_Leader", sysUser.getId())
                    .or().like("members", sysUser.getId()));
            teamQueryWrapper.eq("id", roleId);
            SmartAssessmentTeam one = smartAssessmentTeamService.getOne(teamQueryWrapper);
            if (oConvertUtils.isEmpty(one)) {
                return Result.error("权限不正确！");
            }

            // 对考核要点进行权限控制，检查是否在负责范围内
            QueryWrapper<SmartAssessmentContent> contentQueryWrapper = new QueryWrapper<>();
            contentQueryWrapper.eq("mission_id", smartAnswerInfo.getMissionId()).eq("id", contentId).like("ass_team", one.getId());
            SmartAssessmentContent content = smartAssessmentContentService.getOne(contentQueryWrapper);
            if (oConvertUtils.isEmpty(content)) {
                return Result.error("不负责该考核要点评分！");
            }

            // 只查看所负责的单位
            smartAnswerInfo.setDepart(one.getDeparts());
        }

        String content = smartAnswerInfo.getMarkedContent();
        smartAnswerInfo.setMarkedContent(null);


        QueryWrapper<SmartAnswerInfo> queryWrapper = QueryGenerator.initQueryWrapper(smartAnswerInfo, req.getParameterMap());
        queryWrapper.select(SmartAnswerInfo.class, i -> !i.getColumn().equals("total_points")
                && !i.getColumn().equals("ranking")
                && !i.getColumn().equals("is_show_score"));

        if (oConvertUtils.isNotEmpty(content)) {
            if (StringUtils.startsWith(content, "!")) {
                queryWrapper.notLike("marked_content", content.replace("!", ""));
            } else {
                queryWrapper.like("marked_content", content);
            }
        }

        Page<SmartAnswerInfo> page = new Page<SmartAnswerInfo>(pageNo, pageSize);
        IPage<SmartAnswerInfo> pageList = smartAnswerInfoService.page(page, queryWrapper);
        pageList.getRecords().forEach(item -> {
            String markedContent = item.getMarkedContent();
            int index = StringUtils.indexOf(markedContent, contentId + "_" + roleId);
            if (index == -1) {
                item.setMarkedContent("未评分");
            } else {
                item.setMarkedContent("已评分");
            }
        });
        return Result.OK(pageList);
    }

    /**
     * 最终评分列表成绩
     *
     * @param pageNo
     * @param pageSize
     * @return
     */
    @AutoLog(value = "答题信息表-分页列表查询")
    @ApiOperation(value = "答题信息表-分页列表查询", notes = "答题信息表-分页列表查询")
    @GetMapping(value = "/listDepartContentScore")
    public Result<?> queryChargePageList(@RequestParam(name = "missionId") String missionId,
                                         @RequestParam(name = "assContentId") String assContentId,
                                         @RequestParam(name = "pageNo", defaultValue = "1") Integer pageNo,
                                         @RequestParam(name = "pageSize", defaultValue = "10") Integer pageSize) {
        Page<SmartDepartContentScore> page = new Page<>(pageNo, pageSize);
        IPage<SmartDepartContentScore> departContentScores = smartAnswerInfoService.selectByMissionIdAndContentId(page, missionId, assContentId);
        return Result.OK(departContentScores);
    }

    /**
     * 最终评分按照考核组和考核单位查看评分
     *
     * @param type
     * @param roleId
     * @param contentId 考核要点id
     * @param pageNo 页码
     * @param pageSize 每页条数
     * @param req
     * @return
     */
    @AutoLog(value = "答题信息表-分页列表查询")
    @ApiOperation(value = "答题信息表-分页列表查询", notes = "答题信息表-分页列表查询")
    @GetMapping(value = "/listInCharge2")
    public Result<?> queryChargeScorePageList(SmartAnswerInfo smartAnswerInfo,
                                              @RequestParam(name = "type", defaultValue = "depart") String type,
                                              @RequestParam(name = "roleId") String roleId,
                                              @RequestParam(name = "contentId") String contentId,
                                              @RequestParam(name = "pageNo", defaultValue = "1") Integer pageNo,
                                              @RequestParam(name = "pageSize", defaultValue = "10") Integer pageSize,
                                              HttpServletRequest req) {
        LoginUser sysUser = (LoginUser) SecurityUtils.getSubject().getPrincipal();

        if (oConvertUtils.isEmpty(roleId)) {
            return Result.error("没有权限！");
        }

        if ("depart".equals(type)) {
            SmartAssessmentDepartment one = smartAssessmentDepartmentService.getById(roleId);
            if (oConvertUtils.isEmpty(one)) {
                return Result.error("权限不正确！");
            }
            // 只查看所负责的单位
            smartAnswerInfo.setDepart(one.getResponsibleDepart());
        } else {
            SmartAssessmentTeam one = smartAssessmentTeamService.getById(roleId);
            if (oConvertUtils.isEmpty(one)) {
                return Result.error("权限不正确！");
            }
            // 只查看所负责的单位
            smartAnswerInfo.setDepart(one.getDeparts());
        }

        String content = smartAnswerInfo.getMarkedContent();
        smartAnswerInfo.setMarkedContent(null);


        QueryWrapper<SmartAnswerInfo> queryWrapper = QueryGenerator.initQueryWrapper(smartAnswerInfo, req.getParameterMap());

        queryWrapper.select(SmartAnswerInfo.class, i -> !i.getColumn().equals("total_points")
                && !i.getColumn().equals("ranking")
                && !i.getColumn().equals("is_show_score"));

        if (oConvertUtils.isNotEmpty(content)) {
            if ("0".equals(content)) {
                queryWrapper.notLike("marked_content", contentId + "_" + roleId);
            } else {
                queryWrapper.like("marked_content", contentId + "_" + roleId);
            }
        }

        Page<SmartAnswerInfo> page = new Page<SmartAnswerInfo>(pageNo, pageSize);
        IPage<SmartAnswerInfo> pageList = smartAnswerInfoService.page(page, queryWrapper);
        if (pageList.getTotal() == 0) {
            return Result.error("无数据！");
        }

        List<String> answerInfoIdList = pageList.getRecords().stream().map(SmartAnswerInfo::getId).collect(Collectors.toList());

        // 2. 获取成绩
        Map<String, Double> map = smartAnswerAssContentService.listAllByAssContentIdAndMainIdAndRoleId(answerInfoIdList, contentId, roleId);

        pageList.getRecords().forEach(item -> {
            String markedContent = item.getMarkedContent();
            int index = StringUtils.indexOf(markedContent, contentId + "_" + roleId);
            if (index == -1) {
                item.setMarkedContent("未评分");
            } else {
                item.setMarkedContent("已评分");
            }

            item.setTotalPoints(map.get(item.getId()));
        });

        return Result.OK(pageList);
    }

    /**
     * 分页列表查询
     *
     * @param smartAnswerInfo
     * @param pageNo
     * @param pageSize
     * @param req
     * @return
     */
    @AutoLog(value = "答题信息表-分页列表查询")
    @ApiOperation(value = "答题信息表-分页列表查询", notes = "答题信息表-分页列表查询")
    @GetMapping(value = "/listAll")
    public Result<?> queryAllPageList(SmartAnswerInfo smartAnswerInfo,
                                      @RequestParam(name = "pageNo", defaultValue = "1") Integer pageNo,
                                      @RequestParam(name = "pageSize", defaultValue = "10") Integer pageSize,
                                      HttpServletRequest req) {
        QueryWrapper<SmartAnswerInfo> queryWrapper = QueryGenerator.initQueryWrapper(smartAnswerInfo, req.getParameterMap());
        Page<SmartAnswerInfo> page = new Page<SmartAnswerInfo>(pageNo, pageSize);
        IPage<SmartAnswerInfo> pageList = smartAnswerInfoService.page(page, queryWrapper);
        return Result.OK(pageList);
    }

    /**
     * 添加
     *
     * @param smartAnswerInfo
     * @return
     */
    @AutoLog(value = "答题信息表-添加")
    @ApiOperation(value = "答题信息表-添加", notes = "答题信息表-添加")
    @PostMapping(value = "/add")
    @Transactional(rollbackFor = Exception.class)
    public Result<?> add(@RequestBody SmartAnswerInfo smartAnswerInfo) {
        smartAnswerInfoService.save(smartAnswerInfo);
        return Result.OK("添加成功！");
    }

    /**
     * 编辑
     *
     * @param smartAnswerInfo
     * @return
     */
    @AutoLog(value = "答题信息表-编辑")
    @ApiOperation(value = "答题信息表-编辑", notes = "答题信息表-编辑")
    @PutMapping(value = "/edit")
    @Transactional(rollbackFor = Exception.class)
    public Result<?> edit(@RequestBody SmartAnswerInfo smartAnswerInfo) {
        smartAnswerInfoService.updateById(smartAnswerInfo);
        return Result.OK("编辑成功!");
    }

    /**
     * 更新完成度
     *
     * @param smartAnswerInfo
     * @return
     */
    @AutoLog(value = "答题信息表-更新完成度")
    @ApiOperation(value = "答题信息表-更新完成度", notes = "答题信息表-更新完成度")
    @PutMapping(value = "/updateCompletionDegree")
    @Transactional(rollbackFor = Exception.class)
    public Result<?> updateCompletionDegree(@RequestBody SmartAnswerInfo smartAnswerInfo) {
        if (smartAnswerInfo.getTotalKeyPointAmount() == 0) {
            return Result.error("该考核任务总要点个数为0，无法计算！");
        }
        // 查询上传文件数目大于0的要点个数
        QueryWrapper<SmartAnswerAssContent> assContentQueryWrapper = new QueryWrapper<>();
        assContentQueryWrapper.eq("main_id", smartAnswerInfo.getId())
                .eq("is_key", 1)
                .eq("has_child", "0")
                .ne("upload_count", 0);
        double count = smartAnswerAssContentService.count(assContentQueryWrapper);
        smartAnswerInfo.setFinishedKeyPointAmount((int) count);

        smartAnswerInfo.setCompletionDegree(count / smartAnswerInfo.getTotalKeyPointAmount());


        smartAnswerInfoService.updateById(smartAnswerInfo);
        return Result.OK("更新完成度成功!");
    }

    /**
     * 签收
     *
     * @param smartAnswerInfo
     * @return
     */
    @AutoLog(value = "答题信息表-签收")
    @ApiOperation(value = "答题信息表-签收", notes = "答题信息表-签收")
    @PutMapping(value = "/sign")
    @Transactional(rollbackFor = Exception.class)
    public Result<?> sign(@RequestBody SmartAnswerInfo smartAnswerInfo) {
        // 检查是否已截止
//        int dateDiff = DateUtils.dateDiff('s', DateUtils.getCalendar(DateUtils.getMillis(smartAnswerInfo.getEndTime())), DateUtils.getCalendar());
//        if (dateDiff <= 0) {
//            return Result.error("已过截止时间!");
//        }

        LoginUser sysUser = (LoginUser) SecurityUtils.getSubject().getPrincipal();


        // 更新考核任务被考核单位状态
        QueryWrapper<SmartAssessmentDepart> departQueryWrapper = new QueryWrapper<>();
        departQueryWrapper.eq("mission_id", smartAnswerInfo.getMissionId()).eq("assessment_depart", smartAnswerInfo.getDepart());
        SmartAssessmentDepart assessmentDepart = smartAssessmentDepartService.getOne(departQueryWrapper);
        assessmentDepart.setSignStatus("已签收");
        assessmentDepart.setSignUser(sysUser.getId());
        assessmentDepart.setSignTime(DateUtils.getDate());
        smartAssessmentDepartService.updateById(assessmentDepart);


        // 生成答题记录
        // 首先查询所有考核内容、摘要、要点
        QueryWrapper<SmartAssessmentContent> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("mission_id", smartAnswerInfo.getMissionId());
        List<SmartAssessmentContent> smartAssessmentContentList = smartAssessmentContentService.list(queryWrapper);
        // 然后遍历生成记录
        for (SmartAssessmentContent smartAssessmentContent : smartAssessmentContentList) {
            SmartAnswerAssContent smartAnswerAssContent = new SmartAnswerAssContent();
            smartAnswerAssContent.setMainId(smartAnswerInfo.getId());
            smartAnswerAssContent.setPid(smartAssessmentContent.getPid());
            smartAnswerAssContent.setHasChild(smartAssessmentContent.getHasChild());
            smartAnswerAssContent.setAssContentId(smartAssessmentContent.getId());
            smartAnswerAssContent.setIsKey(smartAssessmentContent.getIsKey());
            smartAnswerAssContentService.save(smartAnswerAssContent);
        }

        smartAnswerInfo.setMissionStatus("已签收");
        // 如果是单位主动签收则会为"NULL", 被动签收会是"未评分".评分时会无法按照评分状态进行筛选
        smartAnswerInfo.setMarkedContent("");
        smartAnswerInfoService.updateById(smartAnswerInfo);

        return Result.OK("签收成功!");
    }

    /**
     * 通过id删除
     *
     * @param id
     * @return
     */
    @AutoLog(value = "答题信息表-通过id删除")
    @ApiOperation(value = "答题信息表-通过id删除", notes = "答题信息表-通过id删除")
    @DeleteMapping(value = "/delete")
    @Transactional(rollbackFor = Exception.class)
    public Result<?> delete(@RequestParam(name = "id", required = true) String id) {
        smartAnswerInfoService.removeById(id);
        return Result.OK("删除成功!");
    }

    /**
     * 批量删除
     *
     * @param ids
     * @return
     */
    @AutoLog(value = "答题信息表-批量删除")
    @ApiOperation(value = "答题信息表-批量删除", notes = "答题信息表-批量删除")
    @DeleteMapping(value = "/deleteBatch")
    @Transactional(rollbackFor = Exception.class)
    public Result<?> deleteBatch(@RequestParam(name = "ids", required = true) String ids) {
        this.smartAnswerInfoService.removeByIds(Arrays.asList(ids.split(",")));
        return Result.OK("批量删除成功!");
    }

    /**
     * 通过id查询
     *
     * @param id
     * @return
     */
    @AutoLog(value = "答题信息表-通过id查询")
    @ApiOperation(value = "答题信息表-通过id查询", notes = "答题信息表-通过id查询")
    @GetMapping(value = "/queryById")
    public Result<?> queryById(@RequestParam(name = "id", required = true) String id) {
        SmartAnswerInfo smartAnswerInfo = smartAnswerInfoService.getById(id);
        if (smartAnswerInfo == null) {
            return Result.error("未找到对应数据");
        }
        return Result.OK(smartAnswerInfo);
    }

    /**
     * 导出excel
     *
     * @param request
     * @param smartAnswerInfo
     */
    @RequestMapping(value = "/exportXls")
    public ModelAndView exportXls(HttpServletRequest request, SmartAnswerInfo smartAnswerInfo) {
        return super.exportXls(request, smartAnswerInfo, SmartAnswerInfo.class, "答题信息表");
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
        return super.importExcel(request, response, SmartAnswerInfo.class);
    }

}