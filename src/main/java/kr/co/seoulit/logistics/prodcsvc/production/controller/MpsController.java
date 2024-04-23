package kr.co.seoulit.logistics.prodcsvc.production.controller;

import java.util.ArrayList;
import java.util.HashMap;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.*;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import kr.co.seoulit.logistics.busisvc.logisales.to.ContractDetailInMpsAvailableTO;
import kr.co.seoulit.logistics.prodcsvc.production.service.ProductionService;
import kr.co.seoulit.logistics.prodcsvc.production.to.MpsTO;
import kr.co.seoulit.logistics.prodcsvc.production.to.SalesPlanInMpsAvailableTO;

@CrossOrigin("http://localhost:3000")
@RestController
@RequestMapping("/production/*")

public class MpsController {

	@Autowired
	private ProductionService productionService;

	ModelMap map = null;

	private static Gson gson = new GsonBuilder().serializeNulls().create();

	//주생산계획(MPS) - MPS 조회
	@RequestMapping(value="/mps/list", method=RequestMethod.GET)
	public ModelMap searchMpsInfo(HttpServletRequest request, HttpServletResponse response) {
		String startDate = request.getParameter("startDate");
		String endDate = request.getParameter("endDate");
		String includeMrpApply = request.getParameter("includeMrpApply");
		map = new ModelMap();
		try {
			ArrayList<MpsTO> mpsTOList = productionService.getMpsList(startDate, endDate, includeMrpApply);

			map.put("gridRowJson", mpsTOList);
			map.put("errorCode", 1);
			map.put("errorMsg", "성공");
		} catch (Exception e1) {
			e1.printStackTrace();
			map.put("errorCode", -1);
			map.put("errorMsg", e1.getMessage());
		}
		return map;
	}


	//주생산계획(MPS) - MPS등록 가능 조회
	@RequestMapping(value="/mps/contractdetail-available", method=RequestMethod.GET)
	public ModelMap searchContractDetailListInMpsAvailable(HttpServletRequest request,
														   HttpServletResponse response) {
		String searchCondition = request.getParameter("searchCondition");
		String startDate = request.getParameter("startDate");
		String endDate = request.getParameter("endDate");
		map = new ModelMap();
		try {
			ArrayList<ContractDetailInMpsAvailableTO> contractDetailInMpsAvailableList =
					productionService.getContractDetailListInMpsAvailable(searchCondition, startDate, endDate);
			map.put("gridRowJson", contractDetailInMpsAvailableList);
			map.put("errorCode", 1);
			map.put("errorMsg", "성공");
		} catch (Exception e1) {
			e1.printStackTrace();
			map.put("errorCode", -1);
			map.put("errorMsg", e1.getMessage());
		}
		return map;
	}


	//공정 계획 관리 - 수주상세조회   <<< MRP에서 MPS 조회
	@RequestMapping(value="/mps/contractdetail-processplanavailable", method=RequestMethod.GET)
	public ModelMap searchContractDetailListInProcessPlanAvailable(HttpServletRequest request,
																   HttpServletResponse response) {
		String searchCondition = request.getParameter("searchCondition");
		String startDate = request.getParameter("startDate");
		String endDate = request.getParameter("endDate");
		map = new ModelMap();
		try {
			ArrayList<ContractDetailInMpsAvailableTO> contractDetailInMpsAvailableList =
					productionService.getContractDetailListInProcessPlanAvailable(searchCondition, startDate, endDate);
			map.put("gridRowJson", contractDetailInMpsAvailableList);
			map.put("errorCode", 1);
			map.put("errorMsg", "성공");
		} catch (Exception e1) {
			e1.printStackTrace();
			map.put("errorCode", -1);
			map.put("errorMsg", e1.getMessage());
		}
		return map;
	}

	@RequestMapping(value="/mps/salesplan-available", method=RequestMethod.GET)
	public ModelMap searchSalesPlanListInMpsAvailable(HttpServletRequest request, HttpServletResponse response) {
		String searchCondition = request.getParameter("searchCondition");
		String startDate = request.getParameter("startDate");
		String endDate = request.getParameter("endDate");
		map = new ModelMap();
		try {
			ArrayList<SalesPlanInMpsAvailableTO> salesPlanInMpsAvailableList =
					productionService.getSalesPlanListInMpsAvailable(searchCondition, startDate, endDate);

			map.put("gridRowJson", salesPlanInMpsAvailableList);
			map.put("errorCode", 1);
			map.put("errorMsg", "성공");
		} catch (Exception e1) {
			e1.printStackTrace();
			map.put("errorCode", -1);
			map.put("errorMsg", e1.getMessage());
		}
		return map;
	}


	//	주생산계획(MPS) - MPS 등록
	@RequestMapping(value="/mps/contractdetail", method=RequestMethod.POST)   // 앞단에서는 post로 데이터 보냄!!!
	public ModelMap convertContractDetailToMps(@RequestBody ContractDetailInMpsAvailableTO batchList) {
		System.out.println(batchList);
		map = new ModelMap();

		try {

			HashMap<String, Object> resultMap =
					productionService.convertContractDetailToMps(batchList);

			map.put("result", resultMap);
			map.put("errorCode", 1);
			map.put("errorMsg", "성공");
		} catch (Exception e1) {
			e1.printStackTrace();
			map.put("errorCode", -1);
			map.put("errorMsg", e1.getMessage());
		}
		return map;
	}


	//주생산계획(MPS) - MPS 등록
//	@RequestMapping(value="/mps/contractdetail", method=RequestMethod.POST)   // 앞단에서는 post로 데이터 보냄!!!
//	public ModelMap convertContractDetailToMps(@RequestBody HashMap<String, ArrayList<ContractDetailInMpsAvailableTO>> batchList) {
//
//		map = new ModelMap();
//
//		ArrayList<ContractDetailInMpsAvailableTO> contractDetailInMpsAvailableList = batchList.get("batchList");
//
//		try {
//
//			HashMap<String, Object> resultMap =
//					productionService.convertContractDetailToMps(contractDetailInMpsAvailableList);
//
//			map.put("result", resultMap);
//			map.put("errorCode", 1);
//			map.put("errorMsg", "성공");
//		} catch (Exception e1) {
//			e1.printStackTrace();
//			map.put("errorCode", -1);
//			map.put("errorMsg", e1.getMessage());
//		}
//		return map;
//	}

//	//주생산계획(MPS) - MPS 등록
//	@RequestMapping(value="mps/contractdetail", method=RequestMethod.PUT)   // 앞단에서는 post로 데이터 보냄!!!
//	public ModelMap convertContractDetailToMps(HttpServletRequest request, HttpServletResponse response) {
//		String batchList = request.getParameter("batchList");
//		map = new ModelMap();
//		ContractDetailInMpsAvailableTO contractDetailInMpsAvailableList = gson.fromJson(batchList,
//				new TypeToken<ArrayList<ContractDetailInMpsAvailableTO>>() {}.getType());
//		try {
//			HashMap<String, Object> resultMap = productionService
//					.convertContractDetailToMps(contractDetailInMpsAvailableList);
//
//			map.put("result", resultMap);
//			map.put("errorCode", 1);
//			map.put("errorMsg", "성공");
//		} catch (Exception e1) {
//			e1.printStackTrace();
//			map.put("errorCode", -1);
//			map.put("errorMsg", e1.getMessage());
//		}
//		return map;
//	}

	@RequestMapping(value="/mps/salesplan", method=RequestMethod.PUT)
	public ModelMap convertSalesPlanToMps(HttpServletRequest request, HttpServletResponse response) {
		String batchList = request.getParameter("batchList");
		map = new ModelMap();
		try {
			ArrayList<SalesPlanInMpsAvailableTO> salesPlanInMpsAvailableList = gson.fromJson(batchList,
					new TypeToken<ArrayList<SalesPlanInMpsAvailableTO>>() {
					}.getType());
			HashMap<String, Object> resultMap = productionService.convertSalesPlanToMps(salesPlanInMpsAvailableList);

			map.put("result", resultMap);
			map.put("errorCode", 1);
			map.put("errorMsg", "성공");
		} catch (Exception e1) {
			e1.printStackTrace();
			map.put("errorCode", -1);
			map.put("errorMsg", e1.getMessage());
		}
		return map;
	}

}
