package org.jeecg.modules.smartAnswerAssContent.service.impl;

import org.jeecg.modules.smartAnswerAssContent.entity.SmartAnswerAssContent;
import org.jeecg.modules.smartAnswerAssContent.entity.SmartAnswerFile;
import org.jeecg.modules.smartAnswerAssContent.entity.SmartAnswerAssScore;
import org.jeecg.modules.smartAnswerAssContent.mapper.SmartAnswerFileMapper;
import org.jeecg.modules.smartAnswerAssContent.mapper.SmartAnswerAssScoreMapper;
import org.jeecg.modules.smartAnswerAssContent.mapper.SmartAnswerAssContentMapper;
import org.jeecg.modules.smartAnswerAssContent.service.ISmartAnswerAssContentService;
import org.springframework.stereotype.Service;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Collection;
import java.util.Map;

/**
 * @Description: 答题考核节点表
 * @Author: jeecg-boot
 * @Date:   2022-02-21
 * @Version: V1.0
 */
@Service
public class SmartAnswerAssContentServiceImpl extends ServiceImpl<SmartAnswerAssContentMapper, SmartAnswerAssContent> implements ISmartAnswerAssContentService {

	@Autowired
	private SmartAnswerAssContentMapper smartAnswerAssContentMapper;
	@Autowired
	private SmartAnswerFileMapper smartAnswerFileMapper;
	@Autowired
	private SmartAnswerAssScoreMapper smartAnswerAssScoreMapper;
	
	@Override
	@Transactional
	public void delMain(String id) {
		smartAnswerFileMapper.deleteByMainId(id);
		smartAnswerAssScoreMapper.deleteByMainId(id);
		smartAnswerAssContentMapper.deleteById(id);
	}

	@Override
	@Transactional
	public void delBatchMain(Collection<? extends Serializable> idList) {
		for(Serializable id:idList) {
			smartAnswerFileMapper.deleteByMainId(id.toString());
			smartAnswerAssScoreMapper.deleteByMainId(id.toString());
			smartAnswerAssContentMapper.deleteById(id);
		}
	}

    @Override
    public List<SmartAnswerAssContent> listAllByAssContentIdAndMissionId(String missionId, String assContentId) {
		return smartAnswerAssContentMapper.listAllByAssContentIdAndMissionId(missionId, assContentId);
    }

    @Override
    public Map<String, Double> listAllByAssContentIdAndMainIdAndRoleId(List<String> mainIds, String assContentId, String roleId) {
		Map<String, Double> map = new HashMap<String, Double>();
		if (mainIds.size() == 0) {
			return map;
		}
		List<SmartAnswerAssScore> scoreList = smartAnswerAssScoreMapper.listAllByAssContentIdAndMainIdAndRoleId(mainIds, assContentId, roleId);
		for(SmartAnswerAssScore score : scoreList) {
			map.put(score.getMainId(), score.getScore());
		}
		return map;
	}

}
