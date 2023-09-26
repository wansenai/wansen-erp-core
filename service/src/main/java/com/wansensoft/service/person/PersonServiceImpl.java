package com.wansensoft.service.person;

import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.wansensoft.entities.account.AccountHead;
import com.wansensoft.entities.depot.DepotHead;
import com.wansensoft.entities.person.Person;
import com.wansensoft.entities.person.PersonExample;
import com.wansensoft.mappers.account.AccountHeadMapperEx;
import com.wansensoft.mappers.depot.DepotHeadMapperEx;
import com.wansensoft.service.log.LogService;
import com.wansensoft.utils.constants.BusinessConstants;
import com.wansensoft.utils.constants.ExceptionConstants;
import com.wansensoft.plugins.exception.BusinessRunTimeException;
import com.wansensoft.plugins.exception.JshException;
import com.wansensoft.mappers.person.PersonMapper;
import com.wansensoft.mappers.person.PersonMapperEx;
import com.wansensoft.utils.StringUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import jakarta.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class PersonServiceImpl extends ServiceImpl<PersonMapper, Person> implements PersonService{
    private Logger logger = LoggerFactory.getLogger(PersonServiceImpl.class);

    private final PersonMapper personMapper;
    private final PersonMapperEx personMapperEx;
    private final LogService logService;
    private final AccountHeadMapperEx accountHeadMapperEx;
    private final DepotHeadMapperEx depotHeadMapperEx;

    public PersonServiceImpl(PersonMapper personMapper, PersonMapperEx personMapperEx, LogService logService, AccountHeadMapperEx accountHeadMapperEx, DepotHeadMapperEx depotHeadMapperEx) {
        this.personMapper = personMapper;
        this.personMapperEx = personMapperEx;
        this.logService = logService;
        this.accountHeadMapperEx = accountHeadMapperEx;
        this.depotHeadMapperEx = depotHeadMapperEx;
    }

    public Person getPerson(long id) {
        Person result=null;
        try{
            result=personMapper.selectByPrimaryKey(id);
        }catch(Exception e){
            JshException.readFail(logger, e);
        }
        return result;
    }

    public List<Person> getPersonListByIds(String ids) {
        List<Long> idList = StringUtil.strToLongList(ids);
        List<Person> list = new ArrayList<>();
        try{
            PersonExample example = new PersonExample();
            example.createCriteria().andIdIn(idList);
            list = personMapper.selectByExample(example);
        }catch(Exception e){
            JshException.readFail(logger, e);
        }
        return list;
    }

    public List<Person> getPerson() {
        PersonExample example = new PersonExample();
        example.createCriteria().andEnabledEqualTo(true).andDeleteFlagNotEqualTo(BusinessConstants.DELETE_FLAG_DELETED);
        List<Person> list=null;
        try{
            list=personMapper.selectByExample(example);
        }catch(Exception e){
            JshException.readFail(logger, e);
        }
        return list;
    }

    public List<Person> select(String name, String type, int offset, int rows) {
        List<Person> list=null;
        try{
            list=personMapperEx.selectByConditionPerson(name, type, offset, rows);
        }catch(Exception e){
            JshException.readFail(logger, e);
        }
        return list;
    }

    public Long countPerson(String name, String type) {
        Long result=null;
        try{
            result=personMapperEx.countsByPerson(name, type);
        }catch(Exception e){
            JshException.readFail(logger, e);
        }
        return result;
    }

    @Transactional(value = "transactionManager", rollbackFor = Exception.class)
    public int insertPerson(JSONObject obj, HttpServletRequest request) {
        Person person = JSONObject.parseObject(obj.toJSONString(), Person.class);
        int result=0;
        try{
            person.setEnabled(true);
            result=personMapper.insertSelective(person);
            logService.insertLog("经手人",
                    BusinessConstants.LOG_OPERATION_TYPE_ADD + person.getName(), request);
        }catch(Exception e){
            JshException.writeFail(logger, e);
        }
        return result;
    }

    @Transactional(value = "transactionManager", rollbackFor = Exception.class)
    public int updatePerson(JSONObject obj, HttpServletRequest request) {
        Person person = JSONObject.parseObject(obj.toJSONString(), Person.class);
        int result=0;
        try{
            result=personMapper.updateByPrimaryKeySelective(person);
            logService.insertLog("经手人",
                    BusinessConstants.LOG_OPERATION_TYPE_EDIT + person.getName(), request);
        }catch(Exception e){
            JshException.writeFail(logger, e);
        }
        return result;
    }

    @Transactional(value = "transactionManager", rollbackFor = Exception.class)
    public int deletePerson(Long id, HttpServletRequest request) {
        return batchDeletePersonByIds(id.toString());
    }

    @Transactional(value = "transactionManager", rollbackFor = Exception.class)
    public int batchDeletePerson(String ids, HttpServletRequest request) {
        return batchDeletePersonByIds(ids);
    }

    @Transactional(value = "transactionManager", rollbackFor = Exception.class)
    public int batchDeletePersonByIds(String ids) {
        int result =0;
        String [] idArray=ids.split(",");
        //校验财务主表	jsh_accounthead
        List<AccountHead> accountHeadList =null;
        try{
            accountHeadList=accountHeadMapperEx.getAccountHeadListByHandsPersonIds(idArray);
        }catch(Exception e){
            JshException.readFail(logger, e);
        }
        if(accountHeadList!=null&& !accountHeadList.isEmpty()){
            logger.error("异常码[{}],异常提示[{}],参数,HandsPersonIds[{}]",
                    ExceptionConstants.DELETE_FORCE_CONFIRM_CODE,ExceptionConstants.DELETE_FORCE_CONFIRM_MSG,ids);
            throw new BusinessRunTimeException(ExceptionConstants.DELETE_FORCE_CONFIRM_CODE,
                    ExceptionConstants.DELETE_FORCE_CONFIRM_MSG);
        }
        //校验单据主表	jsh_depot_head
        List<DepotHead> depotHeadList =null;
        try{
            depotHeadList=depotHeadMapperEx.getDepotHeadListByCreator(idArray);
        }catch(Exception e){
            JshException.readFail(logger, e);
        }
        if(depotHeadList!=null&& !depotHeadList.isEmpty()){
            logger.error("异常码[{}],异常提示[{}],参数,HandsPersonIds[{}]",
                    ExceptionConstants.DELETE_FORCE_CONFIRM_CODE,ExceptionConstants.DELETE_FORCE_CONFIRM_MSG,ids);
            throw new BusinessRunTimeException(ExceptionConstants.DELETE_FORCE_CONFIRM_CODE,
                    ExceptionConstants.DELETE_FORCE_CONFIRM_MSG);
        }
        //记录日志
        StringBuffer sb = new StringBuffer();
        sb.append(BusinessConstants.LOG_OPERATION_TYPE_DELETE);
        List<Person> list = getPersonListByIds(ids);
        for(Person person: list){
            sb.append("[").append(person.getName()).append("]");
        }
        logService.insertLog("经手人", sb.toString(),
                ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest());
        //删除经手人
        try{
            result=personMapperEx.batchDeletePersonByIds(idArray);
        }catch(Exception e){
            JshException.writeFail(logger, e);
        }
        return result;
    }

    public int checkIsNameExist(Long id, String name) {
        PersonExample example = new PersonExample();
        example.createCriteria().andIdNotEqualTo(id).andNameEqualTo(name).andDeleteFlagNotEqualTo(BusinessConstants.DELETE_FLAG_DELETED);
        List<Person> list =null;
        try{
            list=personMapper.selectByExample(example);
        }catch(Exception e){
            JshException.readFail(logger, e);
        }
        return list==null?0:list.size();
    }

    public Map<Long,String> getPersonMap() {
        List<Person> personList = getPerson();
        Map<Long,String> personMap = new HashMap<>();
        for(Person person : personList){
            personMap.put(person.getId(), person.getName());
        }
        return personMap;
    }

    public String getPersonByMapAndIds(Map<Long,String> personMap, String personIds) {
        List<Long> ids = StringUtil.strToLongList(personIds);
        StringBuilder sb = new StringBuilder();
        for(Long id: ids){
            sb.append(personMap.get(id)).append(" ");
        }
        return sb.toString();
    }

    public List<Person> getPersonByType(String type) {
        PersonExample example = new PersonExample();
        example.createCriteria().andTypeEqualTo(type).andEnabledEqualTo(true)
                .andDeleteFlagNotEqualTo(BusinessConstants.DELETE_FLAG_DELETED);
        example.setOrderByClause("sort asc, id desc");
        List<Person> list =null;
        try{
            list=personMapper.selectByExample(example);
        }catch(Exception e){
            JshException.readFail(logger, e);
        }
        return list;
    }

    @Transactional(value = "transactionManager", rollbackFor = Exception.class)
    public int batchSetStatus(Boolean status, String ids) {
        logService.insertLog("经手人",
                BusinessConstants.LOG_OPERATION_TYPE_ENABLED,
                ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest());
        List<Long> personIds = StringUtil.strToLongList(ids);
        Person person = new Person();
        person.setEnabled(status);
        PersonExample example = new PersonExample();
        example.createCriteria().andIdIn(personIds);
        int result=0;
        try{
            result = personMapper.updateByExampleSelective(person, example);
        }catch(Exception e){
            JshException.writeFail(logger, e);
        }
        return result;
    }
}
