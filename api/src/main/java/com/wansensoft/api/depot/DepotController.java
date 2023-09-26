package com.wansensoft.api.depot;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.wansensoft.entities.depot.Depot;
import com.wansensoft.entities.depot.DepotEx;
import com.wansensoft.entities.material.MaterialInitialStock;
import com.wansensoft.service.depot.DepotService;
import com.wansensoft.service.material.MaterialService;
import com.wansensoft.service.userBusiness.UserBusinessService;
import com.wansensoft.utils.BaseResponseInfo;
import com.wansensoft.utils.ErpInfo;
import com.wansensoft.utils.Response;
import com.wansensoft.utils.ResponseJsonUtil;
import com.wansensoft.utils.enums.CodeEnum;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import java.math.BigDecimal;
import java.util.*;

/**
 *
 */
@RestController
@RequestMapping(value = "/depot")
@Api(tags = {"仓库管理"})
public class DepotController {
    private Logger logger = LoggerFactory.getLogger(DepotController.class);


    private final DepotService depotService;

    private final UserBusinessService userBusinessService;

    private final MaterialService materialService;

    public DepotController(DepotService depotService, UserBusinessService userBusinessService, MaterialService materialService) {
        this.depotService = depotService;
        this.userBusinessService = userBusinessService;
        this.materialService = materialService;
    }

    /**
     * 仓库列表
     * @param request
     * @return
     * @throws Exception
     */
    @GetMapping(value = "/getAllList")
    @ApiOperation(value = "仓库列表")
    public BaseResponseInfo getAllList(HttpServletRequest request) throws Exception{
        BaseResponseInfo res = new BaseResponseInfo();
        try {
            List<Depot> depotList = depotService.getAllList();
            res.code = 200;
            res.data = depotList;
        } catch(Exception e){
            res.code = 500;
            res.data = "获取数据失败";
        }
        return res;
    }

    /**
     * 用户对应仓库显示
     * @param type
     * @param keyId
     * @param request
     * @return
     */
    @GetMapping(value = "/findUserDepot")
    @ApiOperation(value = "用户对应仓库显示")
    public JSONArray findUserDepot(@RequestParam("UBType") String type, @RequestParam("UBKeyId") String keyId,
                                 HttpServletRequest request) throws Exception{
        JSONArray arr = new JSONArray();
        try {
            //获取权限信息
            String ubValue = userBusinessService.getUBValueByTypeAndKeyId(type, keyId);
            List<Depot> dataList = depotService.findUserDepot();
            //开始拼接json数据
            JSONObject outer = new JSONObject();
            outer.put("id", 0);
            outer.put("key", 0);
            outer.put("value", 0);
            outer.put("title", "仓库列表");
            outer.put("attributes", "仓库列表");
            //存放数据json数组
            JSONArray dataArray = new JSONArray();
            if (null != dataList) {
                for (Depot depot : dataList) {
                    JSONObject item = new JSONObject();
                    item.put("id", depot.getId());
                    item.put("key", depot.getId());
                    item.put("value", depot.getId());
                    item.put("title", depot.getName());
                    item.put("attributes", depot.getName());
                    Boolean flag = ubValue.contains("[" + depot.getId().toString() + "]");
                    if (flag) {
                        item.put("checked", true);
                    }
                    dataArray.add(item);
                }
            }
            outer.put("children", dataArray);
            arr.add(outer);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return arr;
    }

    /**
     * 获取当前用户拥有权限的仓库列表
     * @param request
     * @return
     * @throws Exception
     */
    @GetMapping(value = "/findDepotByCurrentUser")
    @ApiOperation(value = "获取当前用户拥有权限的仓库列表")
    public BaseResponseInfo findDepotByCurrentUser(HttpServletRequest request) throws Exception{
        BaseResponseInfo res = new BaseResponseInfo();
        try {
            JSONArray arr = depotService.findDepotByCurrentUser();
            res.code = 200;
            res.data = arr;
        } catch (Exception e) {
            res.code = 500;
            res.data = "获取数据失败";
        }
        return res;
    }

    /**
     * 更新默认仓库
     * @param object
     * @param request
     * @return
     * @throws Exception
     */
    @PostMapping(value = "/updateIsDefault")
    @ApiOperation(value = "更新默认仓库")
    public String updateIsDefault(@RequestBody JSONObject object,
                                       HttpServletRequest request) throws Exception{
        Long depotId = object.getLong("id");
        Map<String, Object> objectMap = new HashMap<>();
        int res = depotService.updateIsDefault(depotId);
        if(res > 0) {
            return ResponseJsonUtil.returnJson(objectMap, ErpInfo.OK.name, ErpInfo.OK.code);
        } else {
            return ResponseJsonUtil.returnJson(objectMap, ErpInfo.ERROR.name, ErpInfo.ERROR.code);
        }
    }

    /**
     * 仓库列表-带库存
     * @param mId
     * @param request
     * @return
     */
    @GetMapping(value = "/getAllListWithStock")
    @ApiOperation(value = "仓库列表-带库存")
    public BaseResponseInfo getAllList(@RequestParam("mId") Long mId,
                                       HttpServletRequest request) {
        BaseResponseInfo res = new BaseResponseInfo();
        try {
            List<Depot> list = depotService.getAllList();
            List<DepotEx> depotList = new ArrayList<DepotEx>();
            for(Depot depot: list) {
                DepotEx de = new DepotEx();
                if(mId!=0) {
                    BigDecimal initStock = materialService.getInitStock(mId, depot.getId());
                    BigDecimal currentStock = materialService.getCurrentStockByMaterialIdAndDepotId(mId, depot.getId());
                    de.setInitStock(initStock);
                    de.setCurrentStock(currentStock);
                    MaterialInitialStock materialInitialStock = materialService.getSafeStock(mId, depot.getId());
                    de.setLowSafeStock(materialInitialStock.getLowSafeStock());
                    de.setHighSafeStock(materialInitialStock.getHighSafeStock());
                } else {
                    de.setInitStock(BigDecimal.ZERO);
                    de.setCurrentStock(BigDecimal.ZERO);
                }
                de.setId(depot.getId());
                de.setName(depot.getName());
                depotList.add(de);
            }
            res.code = 200;
            res.data = depotList;
        } catch(Exception e){
            res.code = 500;
            res.data = "获取数据失败";
        }
        return res;
    }

    /**
     * 批量设置状态-启用或者禁用
     * @param jsonObject
     * @param request
     * @return
     */
    @PostMapping(value = "/batchSetStatus")
    @ApiOperation(value = "批量设置状态")
    public String batchSetStatus(@RequestBody JSONObject jsonObject,
                                 HttpServletRequest request)throws Exception {
        Boolean status = jsonObject.getBoolean("status");
        String ids = jsonObject.getString("ids");
        Map<String, Object> objectMap = new HashMap<>();
        int res = depotService.batchSetStatus(status, ids);
        if(res > 0) {
            return ResponseJsonUtil.returnJson(objectMap, ErpInfo.OK.name, ErpInfo.OK.code);
        } else {
            return ResponseJsonUtil.returnJson(objectMap, ErpInfo.ERROR.name, ErpInfo.ERROR.code);
        }
    }
}
