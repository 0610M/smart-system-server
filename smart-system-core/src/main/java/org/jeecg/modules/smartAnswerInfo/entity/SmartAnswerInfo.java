package org.jeecg.modules.smartAnswerInfo.entity;

import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.util.Date;
import java.math.BigDecimal;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import com.fasterxml.jackson.annotation.JsonFormat;
import org.springframework.format.annotation.DateTimeFormat;
import org.jeecgframework.poi.excel.annotation.Excel;
import org.jeecg.common.aspect.annotation.Dict;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

/**
 * @Description: 答题信息表
 * @Author: jeecg-boot
 * @Date:   2022-02-21
 * @Version: V1.0
 */
@Data
@TableName("smart_answer_info")
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = false)
@ApiModel(value="smart_answer_info对象", description="答题信息表")
public class SmartAnswerInfo implements Serializable {
    private static final long serialVersionUID = 1L;

	/**主键*/
	@TableId(type = IdType.ASSIGN_ID)
    @ApiModelProperty(value = "主键")
    private java.lang.String id;
	/**创建人*/
    @ApiModelProperty(value = "创建人")
    private java.lang.String createBy;
	/**创建日期*/
	@JsonFormat(timezone = "GMT+8",pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss")
    @ApiModelProperty(value = "创建日期")
    private java.util.Date createTime;
	/**更新人*/
    @ApiModelProperty(value = "更新人")
    private java.lang.String updateBy;
	/**更新日期*/
	@JsonFormat(timezone = "GMT+8",pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss")
    @ApiModelProperty(value = "更新日期")
    private java.util.Date updateTime;
	/**所属部门*/
    @ApiModelProperty(value = "所属部门")
    private java.lang.String sysOrgCode;
	/**考核任务*/
	@Excel(name = "考核任务", width = 15, dictTable = "smart_assessment_mission", dicText = "mission_name", dicCode = "id")
    @Dict(dictTable = "smart_assessment_mission", dicText = "mission_name", dicCode = "id")
    @ApiModelProperty(value = "考核任务")
    private java.lang.String missionId;
	/**单位*/
	@Excel(name = "单位", width = 15, dictTable = "sys_depart", dicText = "depart_name", dicCode = "id")
	@Dict(dictTable = "sys_depart", dicText = "depart_name", dicCode = "id")
    @ApiModelProperty(value = "单位")
    private java.lang.String depart;
	/**任务状态*/
	@Excel(name = "任务状态", width = 15)
    @ApiModelProperty(value = "任务状态")
    private java.lang.String missionStatus;
    /**截止时间*/
    @JsonFormat(timezone = "GMT+8",pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss")
    @ApiModelProperty(value = "截止时间")
    private java.util.Date endTime;
    /**已评分内容ID*/
    @ApiModelProperty(value = "已评分内容ID")
    private java.lang.String markedContent;
	/**完成要点个数*/
	@Excel(name = "完成要点个数", width = 15)
    @ApiModelProperty(value = "完成要点个数")
    private java.lang.Integer finishedKeyPointAmount;
    /**总要点个数*/
    @Excel(name = "总要点个数", width = 15)
    @ApiModelProperty(value = "总要点个数")
    private java.lang.Integer totalKeyPointAmount;
	/**完成度*/
	@Excel(name = "完成度", width = 15)
    @ApiModelProperty(value = "完成度")
    private java.lang.Double completionDegree;
	/**总分*/
	@Excel(name = "总分", width = 15)
    @ApiModelProperty(value = "总分")
    private java.lang.Double totalPoints;
	/**排名*/
	@Excel(name = "排名", width = 15)
    @ApiModelProperty(value = "排名")
    private java.lang.Integer ranking;
    /**是否查看评分详情*/
    @Dict(dicCode = "yn")
    @ApiModelProperty(value = "是否查看评分详情")
    private java.lang.String isShowScore;
}
