package org.jeecg.modules.smartAnswerAssContent.service;

import org.apache.ibatis.annotations.Param;
import org.jeecg.modules.smartAnswerAssContent.entity.SmartAnswerFile;
import org.jeecg.modules.smartAnswerAssContent.entity.SmartAnswerAssScore;
import org.jeecg.modules.smartAnswerAssContent.entity.SmartAnswerAssContent;
import com.baomidou.mybatisplus.extension.service.IService;
import org.springframework.beans.factory.annotation.Autowired;
import java.io.Serializable;
import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * @Description: 答题考核节点表
 * @Author: jeecg-boot
 * @Date:   2022-02-21
 * @Version: V1.0
 */
public interface ISmartAnswerAssContentService extends IService<SmartAnswerAssContent> {

	/**
	 * 删除一对多
	 */
	public void delMain (String id);
	
	/**
	 * 批量删除一对多
	 */
	public void delBatchMain (Collection<? extends Serializable> idList);

	/**
	 * 获取某个任务中某个考核要点所有被考核单位的得分情况
	 *
	 * @param missionId
	 * @param assContentId
	 * @return
	 */
	List<SmartAnswerAssContent> listAllByAssContentIdAndMissionId(String missionId, String assContentId);

	Map<String, Double> listAllByAssContentIdAndMainIdAndRoleId(List<String> mainIds, String assContentId, String roleId);
}
