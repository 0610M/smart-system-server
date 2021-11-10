package org.jeecg.modules.smartPostMarriage.controller;

import java.io.UnsupportedEncodingException;
import java.io.IOException;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.jeecgframework.poi.excel.ExcelImportUtil;
import org.jeecgframework.poi.excel.def.NormalExcelConstants;
import org.jeecgframework.poi.excel.entity.ExportParams;
import org.jeecgframework.poi.excel.entity.ImportParams;
import org.jeecgframework.poi.excel.view.JeecgEntityExcelView;
import org.jeecg.common.system.vo.LoginUser;
import org.apache.shiro.SecurityUtils;
import org.jeecg.common.api.vo.Result;
import org.jeecg.common.system.query.QueryGenerator;
import org.jeecg.common.util.oConvertUtils;
import org.jeecg.modules.smartPostMarriage.entity.SmartPostMarriageReportFile;
import org.jeecg.modules.smartPostMarriage.entity.SmartPostMarriageReport;
import org.jeecg.modules.smartPostMarriage.vo.SmartPostMarriageReportPage;
import org.jeecg.modules.smartPostMarriage.service.ISmartPostMarriageReportService;
import org.jeecg.modules.smartPostMarriage.service.ISmartPostMarriageReportFileService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.extern.slf4j.Slf4j;
import com.alibaba.fastjson.JSON;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.jeecg.common.aspect.annotation.AutoLog;

 /**
 * @Description: 8项规定婚后报备表
 * @Author: jeecg-boot
 * @Date:   2021-11-05
 * @Version: V1.0
 */
@Api(tags="8项规定婚后报备表")
@RestController
@RequestMapping("/smartPostMarriage/smartPostMarriageReport")
@Slf4j
public class SmartPostMarriageReportController {
	@Autowired
	private ISmartPostMarriageReportService smartPostMarriageReportService;
	@Autowired
	private ISmartPostMarriageReportFileService smartPostMarriageReportFileService;
	
	/**
	 * 分页列表查询
	 *
	 * @param smartPostMarriageReport
	 * @param pageNo
	 * @param pageSize
	 * @param req
	 * @return
	 */
	@AutoLog(value = "8项规定婚后报备表-分页列表查询")
	@ApiOperation(value="8项规定婚后报备表-分页列表查询", notes="8项规定婚后报备表-分页列表查询")
	@GetMapping(value = "/list")
	public Result<?> queryPageList(SmartPostMarriageReport smartPostMarriageReport,
								   @RequestParam(name="pageNo", defaultValue="1") Integer pageNo,
								   @RequestParam(name="pageSize", defaultValue="10") Integer pageSize,
								   HttpServletRequest req) {
		QueryWrapper<SmartPostMarriageReport> queryWrapper = QueryGenerator.initQueryWrapper(smartPostMarriageReport, req.getParameterMap());
		Page<SmartPostMarriageReport> page = new Page<SmartPostMarriageReport>(pageNo, pageSize);
		IPage<SmartPostMarriageReport> pageList = smartPostMarriageReportService.page(page, queryWrapper);
		return Result.OK(pageList);
	}
	
	/**
	 *   添加
	 *
	 * @param smartPostMarriageReportPage
	 * @return
	 */
	@AutoLog(value = "8项规定婚后报备表-添加")
	@ApiOperation(value="8项规定婚后报备表-添加", notes="8项规定婚后报备表-添加")
	@PostMapping(value = "/add")
	public Result<?> add(@RequestBody SmartPostMarriageReportPage smartPostMarriageReportPage) {
		SmartPostMarriageReport smartPostMarriageReport = new SmartPostMarriageReport();
		BeanUtils.copyProperties(smartPostMarriageReportPage, smartPostMarriageReport);
		smartPostMarriageReportService.saveMain(smartPostMarriageReport, smartPostMarriageReportPage.getSmartPostMarriageReportFileList());
		return Result.OK("添加成功！");
	}
	
	/**
	 *  编辑
	 *
	 * @param smartPostMarriageReportPage
	 * @return
	 */
	@AutoLog(value = "8项规定婚后报备表-编辑")
	@ApiOperation(value="8项规定婚后报备表-编辑", notes="8项规定婚后报备表-编辑")
	@PutMapping(value = "/edit")
	public Result<?> edit(@RequestBody SmartPostMarriageReportPage smartPostMarriageReportPage) {
		SmartPostMarriageReport smartPostMarriageReport = new SmartPostMarriageReport();
		BeanUtils.copyProperties(smartPostMarriageReportPage, smartPostMarriageReport);
		SmartPostMarriageReport smartPostMarriageReportEntity = smartPostMarriageReportService.getById(smartPostMarriageReport.getId());
		if(smartPostMarriageReportEntity==null) {
			return Result.error("未找到对应数据");
		}
		smartPostMarriageReportService.updateMain(smartPostMarriageReport, smartPostMarriageReportPage.getSmartPostMarriageReportFileList());
		return Result.OK("编辑成功!");
	}
	
	/**
	 *   通过id删除
	 *
	 * @param id
	 * @return
	 */
	@AutoLog(value = "8项规定婚后报备表-通过id删除")
	@ApiOperation(value="8项规定婚后报备表-通过id删除", notes="8项规定婚后报备表-通过id删除")
	@DeleteMapping(value = "/delete")
	public Result<?> delete(@RequestParam(name="id",required=true) String id) {
		smartPostMarriageReportService.delMain(id);
		return Result.OK("删除成功!");
	}
	
	/**
	 *  批量删除
	 *
	 * @param ids
	 * @return
	 */
	@AutoLog(value = "8项规定婚后报备表-批量删除")
	@ApiOperation(value="8项规定婚后报备表-批量删除", notes="8项规定婚后报备表-批量删除")
	@DeleteMapping(value = "/deleteBatch")
	public Result<?> deleteBatch(@RequestParam(name="ids",required=true) String ids) {
		this.smartPostMarriageReportService.delBatchMain(Arrays.asList(ids.split(",")));
		return Result.OK("批量删除成功！");
	}
	
	/**
	 * 通过id查询
	 *
	 * @param id
	 * @return
	 */
	@AutoLog(value = "8项规定婚后报备表-通过id查询")
	@ApiOperation(value="8项规定婚后报备表-通过id查询", notes="8项规定婚后报备表-通过id查询")
	@GetMapping(value = "/queryById")
	public Result<?> queryById(@RequestParam(name="id",required=true) String id) {
		SmartPostMarriageReport smartPostMarriageReport = smartPostMarriageReportService.getById(id);
		if(smartPostMarriageReport==null) {
			return Result.error("未找到对应数据");
		}
		return Result.OK(smartPostMarriageReport);

	}
	
	/**
	 * 通过id查询
	 *
	 * @param id
	 * @return
	 */
	@AutoLog(value = "8项规定婚后报备宴请发票与附件表通过主表ID查询")
	@ApiOperation(value="8项规定婚后报备宴请发票与附件表主表ID查询", notes="8项规定婚后报备宴请发票与附件表-通主表ID查询")
	@GetMapping(value = "/querySmartPostMarriageReportFileByMainId")
	public Result<?> querySmartPostMarriageReportFileListByMainId(@RequestParam(name="id",required=true) String id) {
		List<SmartPostMarriageReportFile> smartPostMarriageReportFileList = smartPostMarriageReportFileService.selectByMainId(id);
		return Result.OK(smartPostMarriageReportFileList);
	}

    /**
    * 导出excel
    *
    * @param request
    * @param smartPostMarriageReport
    */
    @RequestMapping(value = "/exportXls")
    public ModelAndView exportXls(HttpServletRequest request, SmartPostMarriageReport smartPostMarriageReport) {
      // Step.1 组装查询条件查询数据
      QueryWrapper<SmartPostMarriageReport> queryWrapper = QueryGenerator.initQueryWrapper(smartPostMarriageReport, request.getParameterMap());
      LoginUser sysUser = (LoginUser) SecurityUtils.getSubject().getPrincipal();

      //Step.2 获取导出数据
      List<SmartPostMarriageReport> queryList = smartPostMarriageReportService.list(queryWrapper);
      // 过滤选中数据
      String selections = request.getParameter("selections");
      List<SmartPostMarriageReport> smartPostMarriageReportList = new ArrayList<SmartPostMarriageReport>();
      if(oConvertUtils.isEmpty(selections)) {
          smartPostMarriageReportList = queryList;
      }else {
          List<String> selectionList = Arrays.asList(selections.split(","));
          smartPostMarriageReportList = queryList.stream().filter(item -> selectionList.contains(item.getId())).collect(Collectors.toList());
      }

      // Step.3 组装pageList
      List<SmartPostMarriageReportPage> pageList = new ArrayList<SmartPostMarriageReportPage>();
      for (SmartPostMarriageReport main : smartPostMarriageReportList) {
          SmartPostMarriageReportPage vo = new SmartPostMarriageReportPage();
          BeanUtils.copyProperties(main, vo);
          List<SmartPostMarriageReportFile> smartPostMarriageReportFileList = smartPostMarriageReportFileService.selectByMainId(main.getId());
          vo.setSmartPostMarriageReportFileList(smartPostMarriageReportFileList);
          pageList.add(vo);
      }

      // Step.4 AutoPoi 导出Excel
      ModelAndView mv = new ModelAndView(new JeecgEntityExcelView());
      mv.addObject(NormalExcelConstants.FILE_NAME, "8项规定婚后报备表列表");
      mv.addObject(NormalExcelConstants.CLASS, SmartPostMarriageReportPage.class);
      mv.addObject(NormalExcelConstants.PARAMS, new ExportParams("8项规定婚后报备表数据", "导出人:"+sysUser.getRealname(), "8项规定婚后报备表"));
      mv.addObject(NormalExcelConstants.DATA_LIST, pageList);
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
    public Result<?> importExcel(HttpServletRequest request, HttpServletResponse response) {
      MultipartHttpServletRequest multipartRequest = (MultipartHttpServletRequest) request;
      Map<String, MultipartFile> fileMap = multipartRequest.getFileMap();
      for (Map.Entry<String, MultipartFile> entity : fileMap.entrySet()) {
          MultipartFile file = entity.getValue();// 获取上传文件对象
          ImportParams params = new ImportParams();
          params.setTitleRows(2);
          params.setHeadRows(1);
          params.setNeedSave(true);
          try {
              List<SmartPostMarriageReportPage> list = ExcelImportUtil.importExcel(file.getInputStream(), SmartPostMarriageReportPage.class, params);
              for (SmartPostMarriageReportPage page : list) {
                  SmartPostMarriageReport po = new SmartPostMarriageReport();
                  BeanUtils.copyProperties(page, po);
                  smartPostMarriageReportService.saveMain(po, page.getSmartPostMarriageReportFileList());
              }
              return Result.OK("文件导入成功！数据行数:" + list.size());
          } catch (Exception e) {
              log.error(e.getMessage(),e);
              return Result.error("文件导入失败:"+e.getMessage());
          } finally {
              try {
                  file.getInputStream().close();
              } catch (IOException e) {
                  e.printStackTrace();
              }
          }
      }
      return Result.OK("文件导入失败！");
    }

}
