package org.jeecg.modules.smartReportingInformationDetail.controller;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.jeecg.common.api.vo.Result;
import org.jeecg.common.system.query.QueryGenerator;
import org.jeecg.common.util.oConvertUtils;
import org.jeecg.modules.smartReportingInformation.entity.SmartReportingInformation;
import org.jeecg.modules.smartReportingInformation.service.ISmartReportingInformationService;
import org.jeecg.modules.smartReportingInformationDetail.entity.SmartReportingInformationDetail;
import org.jeecg.modules.smartReportingInformationDetail.service.ISmartReportingInformationDetailService;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.extern.slf4j.Slf4j;

import org.jeecg.modules.smartReportingInformationDetail.service.ISmartReportingInformationDetailService;
import org.jeecg.modules.tasks.smartVerifyTask.service.SmartVerify;
import org.jeecgframework.poi.excel.ExcelImportUtil;
import org.jeecgframework.poi.excel.def.NormalExcelConstants;
import org.jeecgframework.poi.excel.entity.ExportParams;
import org.jeecgframework.poi.excel.entity.ImportParams;
import org.jeecgframework.poi.excel.view.JeecgEntityExcelView;
import org.jeecg.common.system.base.controller.JeecgController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;
import org.springframework.web.servlet.ModelAndView;
import com.alibaba.fastjson.JSON;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.jeecg.common.aspect.annotation.AutoLog;

 /**
 * @Description: 举报信息详情表
 * @Author: jeecg-boot
 * @Date:   2021-11-09
 * @Version: V1.0
 */
@Api(tags="举报信息详情表")
@RestController
@RequestMapping("/smartReportingInformationDetail/smartReportingInformationDetail")
@Slf4j
public class SmartReportingInformationDetailController extends JeecgController<SmartReportingInformationDetail, ISmartReportingInformationDetailService> {
	@Autowired
	private ISmartReportingInformationDetailService smartReportingInformationDetailService;
	@Autowired
	private ISmartReportingInformationService smartReportingInformationService;
	@Autowired
	private SmartVerify smartVerify;
	public String verifyType="纠治四风举报详细信息";

	/**
	 * 分页列表查询
	 *
	 * @param smartReportingInformationDetail
	 * @param pageNo
	 * @param pageSize
	 * @param req
	 * @return
	 */
	@AutoLog(value = "举报信息详情表-分页列表查询")
	@ApiOperation(value="举报信息详情表-分页列表查询", notes="举报信息详情表-分页列表查询")
	@GetMapping(value = "/list")
	public Result<?> queryPageList(SmartReportingInformationDetail smartReportingInformationDetail,
								   @RequestParam(name="pageNo", defaultValue="1") Integer pageNo,
								   @RequestParam(name="pageSize", defaultValue="10") Integer pageSize,
								   HttpServletRequest req) {
		QueryWrapper<SmartReportingInformationDetail> queryWrapper = QueryGenerator.initQueryWrapper(smartReportingInformationDetail, req.getParameterMap());
		Page<SmartReportingInformationDetail> page = new Page<SmartReportingInformationDetail>(pageNo, pageSize);
		IPage<SmartReportingInformationDetail> pageList = smartReportingInformationDetailService.page(page, queryWrapper);
		return Result.OK(pageList);
	}

	/**
	 *   添加
	 *
	 * @param smartReportingInformationDetail
	 * @return
	 */
	@AutoLog(value = "举报信息详情表-添加")
	@ApiOperation(value="举报信息详情表-添加", notes="举报信息详情表-添加")
	@PostMapping(value = "/add")
	public Result<?> add(@RequestBody SmartReportingInformationDetail smartReportingInformationDetail) {
		SmartReportingInformation smartReportingInformation = new SmartReportingInformation();
        //举报信息详情表的被反映人姓名对应举报信息表的被反映人信息
		smartReportingInformation.setReflectedInformation(smartReportingInformationDetail.getReflectedName());
		//举报信息详情表的被反映人单位对应举报信息表的被反映人单位
		smartReportingInformation.setReflectedDocumentid(smartReportingInformationDetail.getReflectedDocumentid());
        //举报信息详情表的举报时间对应举报信息表的举报时间
		smartReportingInformation.setReportingTime(smartReportingInformationDetail.getReportingTime());

		smartReportingInformationDetailService.save(smartReportingInformationDetail);
		smartReportingInformationService.save(smartReportingInformation);
		smartVerify.addVerifyRecord(smartReportingInformationDetail.getId(),verifyType);
		return Result.OK("添加成功！");

	}

	/**
	 *  编辑
	 *
	 * @param smartReportingInformationDetail
	 * @return
	 */
	@AutoLog(value = "举报信息详情表-编辑")
	@ApiOperation(value="举报信息详情表-编辑", notes="举报信息详情表-编辑")
	@PutMapping(value = "/edit")
	public Result<?> edit(@RequestBody SmartReportingInformationDetail smartReportingInformationDetail) {
		smartReportingInformationDetailService.updateById(smartReportingInformationDetail);
		return Result.OK("编辑成功!");
	}

	/**
	 *   通过id删除
	 *
	 * @param id
	 * @return
	 */
	@AutoLog(value = "举报信息详情表-通过id删除")
	@ApiOperation(value="举报信息详情表-通过id删除", notes="举报信息详情表-通过id删除")
	@DeleteMapping(value = "/delete")
	public Result<?> delete(@RequestParam(name="id",required=true) String id) {
		smartReportingInformationDetailService.removeById(id);
		return Result.OK("删除成功!");
	}

	/**
	 *  批量删除
	 *
	 * @param ids
	 * @return
	 */
	@AutoLog(value = "举报信息详情表-批量删除")
	@ApiOperation(value="举报信息详情表-批量删除", notes="举报信息详情表-批量删除")
	@DeleteMapping(value = "/deleteBatch")
	public Result<?> deleteBatch(@RequestParam(name="ids",required=true) String ids) {
		this.smartReportingInformationDetailService.removeByIds(Arrays.asList(ids.split(",")));
		return Result.OK("批量删除成功!");
	}

	/**
	 * 通过id查询
	 *
	 * @param id
	 * @return
	 */
	@AutoLog(value = "举报信息详情表-通过id查询")
	@ApiOperation(value="举报信息详情表-通过id查询", notes="举报信息详情表-通过id查询")
	@GetMapping(value = "/queryById")
	public Result<?> queryById(@RequestParam(name="id",required=true) String id) {
		SmartReportingInformationDetail smartReportingInformationDetail = smartReportingInformationDetailService.getById(id);
		if(smartReportingInformationDetail==null) {
			return Result.error("未找到对应数据");
		}
		return Result.OK(smartReportingInformationDetail);
	}

    /**
    * 导出excel
    *
    * @param request
    * @param smartReportingInformationDetail
    */
    @RequestMapping(value = "/exportXls")
    public ModelAndView exportXls(HttpServletRequest request, SmartReportingInformationDetail smartReportingInformationDetail) {
        return super.exportXls(request, smartReportingInformationDetail, SmartReportingInformationDetail.class, "举报信息详情表");
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
        return super.importExcel(request, response, SmartReportingInformationDetail.class);
    }

}
