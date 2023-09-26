package com.wansensoft.service.msg;

import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.wansensoft.entities.msg.Msg;
import com.wansensoft.entities.msg.MsgEx;
import com.wansensoft.entities.msg.MsgExample;
import com.wansensoft.entities.user.User;
import com.wansensoft.service.log.LogService;
import com.wansensoft.service.user.UserService;
import com.wansensoft.utils.constants.BusinessConstants;
import com.wansensoft.utils.constants.ExceptionConstants;
import com.wansensoft.plugins.exception.BusinessRunTimeException;
import com.wansensoft.mappers.msg.MsgMapper;
import com.wansensoft.mappers.msg.MsgMapperEx;
import com.wansensoft.utils.StringUtil;
import com.wansensoft.utils.Tools;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import jakarta.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static com.wansensoft.utils.Tools.getCenternTime;

@Service
public class MsgServiceImpl extends ServiceImpl<MsgMapper, Msg> implements MsgService {
    private Logger logger = LoggerFactory.getLogger(MsgServiceImpl.class);

    private final MsgMapper msgMapper;
    private final MsgMapperEx msgMapperEx;
    private final UserService userService;
    private final LogService logService;

    public MsgServiceImpl(MsgMapper msgMapper, MsgMapperEx msgMapperEx, UserService userService, LogService logService) {
        this.msgMapper = msgMapper;
        this.msgMapperEx = msgMapperEx;
        this.userService = userService;
        this.logService = logService;
    }

    public Msg getMsg(long id) {
        Msg result=null;
        try{
            result=msgMapper.selectByPrimaryKey(id);
        }catch(Exception e){
            logger.error("异常码[{}],异常提示[{}],异常[{}]",
                    ExceptionConstants.DATA_READ_FAIL_CODE, ExceptionConstants.DATA_READ_FAIL_MSG,e);
            throw new BusinessRunTimeException(ExceptionConstants.DATA_READ_FAIL_CODE,
                    ExceptionConstants.DATA_READ_FAIL_MSG);
        }
        return result;
    }

    public List<Msg> getMsg() {
        MsgExample example = new MsgExample();
        example.createCriteria().andDeleteFlagNotEqualTo(BusinessConstants.DELETE_FLAG_DELETED);
        List<Msg> list=null;
        try{
            list=msgMapper.selectByExample(example);
        }catch(Exception e){
            logger.error("异常码[{}],异常提示[{}],异常[{}]",
                    ExceptionConstants.DATA_READ_FAIL_CODE, ExceptionConstants.DATA_READ_FAIL_MSG,e);
            throw new BusinessRunTimeException(ExceptionConstants.DATA_READ_FAIL_CODE,
                    ExceptionConstants.DATA_READ_FAIL_MSG);
        }
        return list;
    }

    public List<MsgEx> select(String name, int offset, int rows) {
        List<MsgEx> list=null;
        try{
            User userInfo = userService.getCurrentUser();
            if(!BusinessConstants.DEFAULT_MANAGER.equals(userInfo.getLoginName())) {
                list = msgMapperEx.selectByConditionMsg(userInfo.getId(), name, offset, rows);
                if (null != list) {
                    for (MsgEx msgEx : list) {
                        if (msgEx.getCreateTime() != null) {
                            msgEx.setCreateTimeStr(getCenternTime(msgEx.getCreateTime()));
                        }
                    }
                }
            }
        }catch(Exception e){
            logger.error("异常码[{}],异常提示[{}],异常[{}]",
                    ExceptionConstants.DATA_READ_FAIL_CODE, ExceptionConstants.DATA_READ_FAIL_MSG,e);
            throw new BusinessRunTimeException(ExceptionConstants.DATA_READ_FAIL_CODE,
                    ExceptionConstants.DATA_READ_FAIL_MSG);
        }
        return list;
    }

    public Long countMsg(String name) {
        Long result=null;
        try{
            User userInfo = userService.getCurrentUser();
            if(!BusinessConstants.DEFAULT_MANAGER.equals(userInfo.getLoginName())) {
                result = msgMapperEx.countsByMsg(userInfo.getId(), name);
            }
        }catch(Exception e){
            logger.error("异常码[{}],异常提示[{}],异常[{}]",
                    ExceptionConstants.DATA_READ_FAIL_CODE, ExceptionConstants.DATA_READ_FAIL_MSG,e);
            throw new BusinessRunTimeException(ExceptionConstants.DATA_READ_FAIL_CODE,
                    ExceptionConstants.DATA_READ_FAIL_MSG);
        }
        return result;
    }

    @Transactional(value = "transactionManager", rollbackFor = Exception.class)
    public int insertMsg(JSONObject obj, HttpServletRequest request) {
        Msg msg = JSONObject.parseObject(obj.toJSONString(), Msg.class);
        int result=0;
        try{
            User userInfo = userService.getCurrentUser();
            if(!BusinessConstants.DEFAULT_MANAGER.equals(userInfo.getLoginName())) {
                msg.setCreateTime(new Date());
                msg.setStatus("1");
                result=msgMapper.insertSelective(msg);
                logService.insertLog("消息",
                        BusinessConstants.LOG_OPERATION_TYPE_ADD + msg.getMsgTitle(), request);
            }
        }catch(Exception e){
            logger.error("异常码[{}],异常提示[{}],异常[{}]",
                    ExceptionConstants.DATA_WRITE_FAIL_CODE, ExceptionConstants.DATA_WRITE_FAIL_MSG,e);
            throw new BusinessRunTimeException(ExceptionConstants.DATA_WRITE_FAIL_CODE,
                    ExceptionConstants.DATA_WRITE_FAIL_MSG);
        }
        return result;
    }

    @Transactional(value = "transactionManager", rollbackFor = Exception.class)
    public int updateMsg(JSONObject obj, HttpServletRequest request) {
        Msg msg = JSONObject.parseObject(obj.toJSONString(), Msg.class);
        int result=0;
        try{
            result=msgMapper.updateByPrimaryKeySelective(msg);
            logService.insertLog("消息",
                    BusinessConstants.LOG_OPERATION_TYPE_EDIT + msg.getMsgTitle(), request);
        }catch(Exception e){
            logger.error("异常码[{}],异常提示[{}],异常[{}]",
                    ExceptionConstants.DATA_WRITE_FAIL_CODE, ExceptionConstants.DATA_WRITE_FAIL_MSG,e);
            throw new BusinessRunTimeException(ExceptionConstants.DATA_WRITE_FAIL_CODE,
                    ExceptionConstants.DATA_WRITE_FAIL_MSG);
        }
        return result;
    }

    @Transactional(value = "transactionManager", rollbackFor = Exception.class)
    public int deleteMsg(Long id, HttpServletRequest request) {
        int result=0;
        try{
            result=msgMapper.deleteByPrimaryKey(id);
            logService.insertLog("消息",
                    BusinessConstants.LOG_OPERATION_TYPE_DELETE + id, request);
        }catch(Exception e){
            logger.error("异常码[{}],异常提示[{}],异常[{}]",
                    ExceptionConstants.DATA_WRITE_FAIL_CODE, ExceptionConstants.DATA_WRITE_FAIL_MSG,e);
            throw new BusinessRunTimeException(ExceptionConstants.DATA_WRITE_FAIL_CODE,
                    ExceptionConstants.DATA_WRITE_FAIL_MSG);
        }
        return result;
    }

    @Transactional(value = "transactionManager", rollbackFor = Exception.class)
    public int batchDeleteMsg(String ids, HttpServletRequest request) {
        List<Long> idList = StringUtil.strToLongList(ids);
        MsgExample example = new MsgExample();
        example.createCriteria().andIdIn(idList);
        int result=0;
        try{
            result=msgMapper.deleteByExample(example);
            logService.insertLog("消息", "批量删除,id集:" + ids, request);
        }catch(Exception e){
            logger.error("异常码[{}],异常提示[{}],异常[{}]",
                    ExceptionConstants.DATA_WRITE_FAIL_CODE, ExceptionConstants.DATA_WRITE_FAIL_MSG,e);
            throw new BusinessRunTimeException(ExceptionConstants.DATA_WRITE_FAIL_CODE,
                    ExceptionConstants.DATA_WRITE_FAIL_MSG);
        }
        return result;
    }

    public int checkIsNameExist(Long id, String name) {
        MsgExample example = new MsgExample();
        example.createCriteria().andIdNotEqualTo(id).andMsgTitleEqualTo(name).andDeleteFlagNotEqualTo(BusinessConstants.DELETE_FLAG_DELETED);
        List<Msg> list=null;
        try{
            list= msgMapper.selectByExample(example);
        }catch(Exception e){
            logger.error("异常码[{}],异常提示[{}],异常[{}]",
                    ExceptionConstants.DATA_READ_FAIL_CODE, ExceptionConstants.DATA_READ_FAIL_MSG,e);
            throw new BusinessRunTimeException(ExceptionConstants.DATA_READ_FAIL_CODE,
                    ExceptionConstants.DATA_READ_FAIL_MSG);
        }
        return list==null?0:list.size();
    }

    /**
     *  逻辑删除角色信息
     * @Param: ids
     * @return int
     */
    @Transactional(value = "transactionManager", rollbackFor = Exception.class)
    public int batchDeleteMsgByIds(String ids) {
        logService.insertLog("序列号",
                BusinessConstants.LOG_OPERATION_TYPE_DELETE + ids,
                ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest());
        String [] idArray=ids.split(",");
        int result=0;
        try{
            result=msgMapperEx.batchDeleteMsgByIds(idArray);
        }catch(Exception e){
            logger.error("异常码[{}],异常提示[{}],异常[{}]",
                    ExceptionConstants.DATA_WRITE_FAIL_CODE, ExceptionConstants.DATA_WRITE_FAIL_MSG,e);
            throw new BusinessRunTimeException(ExceptionConstants.DATA_WRITE_FAIL_CODE,
                    ExceptionConstants.DATA_WRITE_FAIL_MSG);
        }
        return result;
    }

    public List<MsgEx> getMsgByStatus(String status) {
        List<MsgEx> resList=new ArrayList<>();
        try{
            User userInfo = userService.getCurrentUser();
            if(!BusinessConstants.DEFAULT_MANAGER.equals(userInfo.getLoginName())) {
                MsgExample example = new MsgExample();
                example.createCriteria().andStatusEqualTo(status).andUserIdEqualTo(userInfo.getId())
                        .andDeleteFlagNotEqualTo(BusinessConstants.DELETE_FLAG_DELETED);
                example.setOrderByClause("id desc");
                List<Msg> list = msgMapper.selectByExample(example);
                if (null != list) {
                    for (Msg msg : list) {
                        if (msg.getCreateTime() != null) {
                            MsgEx msgEx = new MsgEx();
                            msgEx.setId(msg.getId());
                            msgEx.setMsgTitle(msg.getMsgTitle());
                            msgEx.setMsgContent(msg.getMsgContent());
                            msgEx.setStatus(msg.getStatus());
                            msgEx.setType(msg.getType());
                            msgEx.setCreateTimeStr(Tools.getCenternTime(msg.getCreateTime()));
                            resList.add(msgEx);
                        }
                    }
                }
            }
        }catch(Exception e){
            logger.error("异常码[{}],异常提示[{}],异常[{}]",
                    ExceptionConstants.DATA_READ_FAIL_CODE, ExceptionConstants.DATA_READ_FAIL_MSG,e);
            throw new BusinessRunTimeException(ExceptionConstants.DATA_READ_FAIL_CODE,
                    ExceptionConstants.DATA_READ_FAIL_MSG);
        }
        return resList;
    }

    @Transactional(value = "transactionManager", rollbackFor = Exception.class)
    public void batchUpdateStatus(String ids, String status) {
        List<Long> idList = StringUtil.strToLongList(ids);
        Msg msg = new Msg();
        msg.setStatus(status);
        MsgExample example = new MsgExample();
        example.createCriteria().andIdIn(idList);
        try{
            msgMapper.updateByExampleSelective(msg, example);
        }catch(Exception e){
            logger.error("异常码[{}],异常提示[{}],异常[{}]",
                    ExceptionConstants.DATA_WRITE_FAIL_CODE, ExceptionConstants.DATA_WRITE_FAIL_MSG,e);
            throw new BusinessRunTimeException(ExceptionConstants.DATA_WRITE_FAIL_CODE,
                    ExceptionConstants.DATA_WRITE_FAIL_MSG);
        }
    }

    public Long getMsgCountByStatus(String status) {
        Long result=null;
        try{
            User userInfo= userService.getCurrentUser();
            if(!BusinessConstants.DEFAULT_MANAGER.equals(userInfo.getLoginName())) {
                result = msgMapperEx.getMsgCountByStatus(status, userInfo.getId());
            }
        }catch(Exception e){
            logger.error("异常码[{}],异常提示[{}],异常[{}]",
                    ExceptionConstants.DATA_READ_FAIL_CODE, ExceptionConstants.DATA_READ_FAIL_MSG,e);
            throw new BusinessRunTimeException(ExceptionConstants.DATA_READ_FAIL_CODE,
                    ExceptionConstants.DATA_READ_FAIL_MSG);
        }
        return result;
    }

    public Integer getMsgCountByType(String type) {
        int msgCount = 0;
        try{
            User userInfo = userService.getCurrentUser();
            if(!BusinessConstants.DEFAULT_MANAGER.equals(userInfo.getLoginName())) {
                MsgExample example = new MsgExample();
                example.createCriteria().andTypeEqualTo(type).andDeleteFlagNotEqualTo(BusinessConstants.DELETE_FLAG_DELETED);
                List<Msg> list = msgMapper.selectByExample(example);
                msgCount = list.size();
            }
        }catch(Exception e){
            logger.error("异常码[{}],异常提示[{}],异常[{}]",
                    ExceptionConstants.DATA_READ_FAIL_CODE, ExceptionConstants.DATA_READ_FAIL_MSG,e);
            throw new BusinessRunTimeException(ExceptionConstants.DATA_READ_FAIL_CODE,
                    ExceptionConstants.DATA_READ_FAIL_MSG);
        }
        return msgCount;
    }

    @Transactional(value = "transactionManager", rollbackFor = Exception.class)
    public void readAllMsg() {
        try{
            User userInfo = userService.getCurrentUser();
            if(!BusinessConstants.DEFAULT_MANAGER.equals(userInfo.getLoginName())) {
                Msg msg = new Msg();
                msg.setStatus("2");
                MsgExample example = new MsgExample();
                example.createCriteria();
                msgMapper.updateByExampleSelective(msg, example);
            }
        }catch(Exception e){
            logger.error("异常码[{}],异常提示[{}],异常[{}]",
                    ExceptionConstants.DATA_WRITE_FAIL_CODE, ExceptionConstants.DATA_WRITE_FAIL_MSG,e);
            throw new BusinessRunTimeException(ExceptionConstants.DATA_WRITE_FAIL_CODE,
                    ExceptionConstants.DATA_WRITE_FAIL_MSG);
        }
    }
}
