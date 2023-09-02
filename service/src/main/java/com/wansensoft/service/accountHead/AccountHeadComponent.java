package com.wansensoft.service.accountHead;

import com.alibaba.fastjson.JSONObject;
import com.wansensoft.service.ICommonQuery;
import com.wansensoft.utils.Constants;
import com.wansensoft.utils.QueryUtils;
import com.wansensoft.utils.StringUtil;
import org.springframework.stereotype.Service;

import jakarta.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Map;

@Service
@AccountHeadResource
public class AccountHeadComponent implements ICommonQuery {

    private final AccountHeadService accountHeadService;

    public AccountHeadComponent(AccountHeadService accountHeadService) {
        this.accountHeadService = accountHeadService;
    }

    @Override
    public Object selectOne(Long id) throws Exception {
        return accountHeadService.getAccountHead(id);
    }

    @Override
    public List<?> select(Map<String, String> map)throws Exception {
        return getAccountHeadList(map);
    }

    private List<?> getAccountHeadList(Map<String, String> map)throws Exception {
        String search = map.get(Constants.SEARCH);
        String type = StringUtil.getInfo(search, "type");
        String roleType = StringUtil.getInfo(search, "roleType");
        String billNo = StringUtil.getInfo(search, "billNo");
        String beginTime = StringUtil.getInfo(search, "beginTime");
        String endTime = StringUtil.getInfo(search, "endTime");
        Long organId = StringUtil.parseStrLong(StringUtil.getInfo(search, "organId"));
        Long creator = StringUtil.parseStrLong(StringUtil.getInfo(search, "creator"));
        Long handsPersonId = StringUtil.parseStrLong(StringUtil.getInfo(search, "handsPersonId"));
        Long accountId = StringUtil.parseStrLong(StringUtil.getInfo(search, "accountId"));
        String status = StringUtil.getInfo(search, "status");
        String remark = StringUtil.getInfo(search, "remark");
        String number = StringUtil.getInfo(search, "number");
        return accountHeadService.select(type, roleType, billNo, beginTime, endTime, organId, creator, handsPersonId,
                accountId, status, remark, number, QueryUtils.offset(map), QueryUtils.rows(map));
    }

    @Override
    public Long counts(Map<String, String> map)throws Exception {
        String search = map.get(Constants.SEARCH);
        String type = StringUtil.getInfo(search, "type");
        String roleType = StringUtil.getInfo(search, "roleType");
        String billNo = StringUtil.getInfo(search, "billNo");
        String beginTime = StringUtil.getInfo(search, "beginTime");
        String endTime = StringUtil.getInfo(search, "endTime");
        Long organId = StringUtil.parseStrLong(StringUtil.getInfo(search, "organId"));
        Long creator = StringUtil.parseStrLong(StringUtil.getInfo(search, "creator"));
        Long handsPersonId = StringUtil.parseStrLong(StringUtil.getInfo(search, "handsPersonId"));
        Long accountId = StringUtil.parseStrLong(StringUtil.getInfo(search, "accountId"));
        String status = StringUtil.getInfo(search, "status");
        String remark = StringUtil.getInfo(search, "remark");
        String number = StringUtil.getInfo(search, "number");
        return accountHeadService.countAccountHead(type, roleType, billNo, beginTime, endTime, organId, creator, handsPersonId,
                accountId, status, remark, number);
    }

    @Override
    public int insert(JSONObject obj, HttpServletRequest request) throws Exception{
        return accountHeadService.insertAccountHead(obj, request);
    }

    @Override
    public int update(JSONObject obj, HttpServletRequest request)throws Exception {
        return accountHeadService.updateAccountHead(obj, request);
    }

    @Override
    public int delete(Long id, HttpServletRequest request)throws Exception {
        return accountHeadService.deleteAccountHead(id, request);
    }

    @Override
    public int deleteBatch(String ids, HttpServletRequest request)throws Exception {
        return accountHeadService.batchDeleteAccountHead(ids, request);
    }

    @Override
    public int checkIsNameExist(Long id, String name)throws Exception {
        return 0;
    }

}