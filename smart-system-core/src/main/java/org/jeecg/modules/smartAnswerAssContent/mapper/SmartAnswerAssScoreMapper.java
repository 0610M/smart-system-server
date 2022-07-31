package org.jeecg.modules.smartAnswerAssContent.mapper;

import java.util.List;
import org.jeecg.modules.smartAnswerAssContent.entity.SmartAnswerAssScore;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;

/**
 * @Description: 答题评分表
 * @Author: jeecg-boot
 * @Date:   2022-02-21
 * @Version: V1.0
 */
public interface SmartAnswerAssScoreMapper extends BaseMapper<SmartAnswerAssScore> {

	public boolean deleteByMainId(@Param("mainId") String mainId);
    
	public List<SmartAnswerAssScore> selectByMainId(@Param("mainId") String mainId);

	List<SmartAnswerAssScore> listAllByAssContentIdAndMainIdAndRoleId(@Param("mainIds") List<String> mainIds, @Param("assContentId") String assContentId, @Param("roleId") String roleId);
}
