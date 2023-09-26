package com.wansensoft.mappers.account;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.wansensoft.entities.account.AccountHead;
import com.wansensoft.entities.account.AccountHeadVo4ListEx;
import com.wansensoft.entities.account.AccountItem;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.Date;
import java.util.List;

@Mapper
public interface AccountHeadMapperEx extends BaseMapper<AccountHead> {

    List<AccountHeadVo4ListEx> selectByConditionAccountHead(
            @Param("type") String type,
            @Param("creatorArray") String[] creatorArray,
            @Param("billNo") String billNo,
            @Param("beginTime") String beginTime,
            @Param("endTime") String endTime,
            @Param("organId") Long organId,
            @Param("creator") Long creator,
            @Param("handsPersonId") Long handsPersonId,
            @Param("accountId") Long accountId,
            @Param("status") String status,
            @Param("remark") String remark,
            @Param("number") String number,
            @Param("offset") Integer offset,
            @Param("rows") Integer rows);

    Long countsByAccountHead(
            @Param("type") String type,
            @Param("creatorArray") String[] creatorArray,
            @Param("billNo") String billNo,
            @Param("beginTime") String beginTime,
            @Param("endTime") String endTime,
            @Param("organId") Long organId,
            @Param("creator") Long creator,
            @Param("handsPersonId") Long handsPersonId,
            @Param("accountId") Long accountId,
            @Param("status") String status,
            @Param("remark") String remark,
            @Param("number") String number);

    List<AccountHeadVo4ListEx> getDetailByNumber(
            @Param("billNo") String billNo);

    int batchDeleteAccountHeadByIds(@Param("updateTime") Date updateTime, @Param("updater") Long updater, @Param("ids") String[] ids);

    List<AccountHead> getAccountHeadListByAccountIds(@Param("accountIds") String[] accountIds);

    List<AccountHead> getAccountHeadListByOrganIds(@Param("organIds") String[] organIds);

    List<AccountHead> getAccountHeadListByHandsPersonIds(@Param("handsPersonIds") String[] handsPersonIds);

    List<AccountItem> getFinancialBillNoByBillIdList(
            @Param("idList") List<Long> idList);

    List<AccountHead> getFinancialBillNoByBillId(
            @Param("billId") Long billId);
}