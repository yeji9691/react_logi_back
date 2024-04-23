package kr.co.seoulit.logistics.busisvc.logisales.service;

import java.util.*;

import kr.co.seoulit.logistics.busisvc.logisales.mapper.ContractMapper;
import kr.co.seoulit.logistics.busisvc.logisales.mapper.EstimateMapper;
import kr.co.seoulit.logistics.busisvc.logisales.to.EstimateDetailTO;
import kr.co.seoulit.logistics.busisvc.logisales.to.ContractDetailTO;
import kr.co.seoulit.logistics.busisvc.logisales.to.ContractInfoTO;
import kr.co.seoulit.logistics.busisvc.logisales.to.EstimateTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.ModelMap;

@Service
public class LogisalesServiceImpl implements LogisalesService {

	@Autowired
	private ContractMapper contractMapper;
	@Autowired
	private EstimateMapper estimateMapper;


	@Override
	public ArrayList<EstimateTO> getEstimateList(String dateSearchCondition, String startDate, String endDate) {

		ArrayList<EstimateTO> estimateTOList = null;

		HashMap<String, String> map = new HashMap<>();

		map.put("dateSearchCondition", dateSearchCondition);
		map.put("startDate", startDate);
		map.put("endDate", endDate);

		estimateTOList = estimateMapper.selectEstimateList(map);

		return estimateTOList;
	}

	@Override
	public ArrayList<EstimateDetailTO> getEstimateDetailList(String estimateNo) {

		ArrayList<EstimateDetailTO> estimateDetailTOList = null;

		estimateDetailTOList = estimateMapper.selectEstimateDetailList(estimateNo);

		return estimateDetailTOList;
	}

	@Override
	public ModelMap addNewEstimate(String estimateDate, EstimateTO newEstimateTO) {

		ModelMap resultMap = null;

		String newEstimateNo = getNewEstimateNo(estimateDate);
		System.out.println("날짜"+newEstimateNo);
		System.out.println("티오"+newEstimateTO);

		newEstimateTO.setEstimateNo(newEstimateNo);

		estimateMapper.insertEstimate(newEstimateTO);

		ArrayList<EstimateDetailTO> estimateDetailTOList = newEstimateTO.getEstimateDetailTOList(); //bean객체

		for (EstimateDetailTO bean : estimateDetailTOList) {
			String newEstimateDetailNo = getNewEstimateDetailNo(newEstimateNo);

			bean.setEstimateNo(newEstimateNo);

			bean.setEstimateDetailNo(newEstimateDetailNo);
		}

		resultMap = batchEstimateDetailListProcess(estimateDetailTOList,newEstimateNo);

		resultMap.put("newEstimateNo", newEstimateNo);

		return resultMap;
	}

	public String getNewEstimateNo(String estimateDate) {

		StringBuffer newEstimateNo = null;

		int i = estimateMapper.selectEstimateCount(estimateDate);

		newEstimateNo = new StringBuffer();
		newEstimateNo.append("ES");
		newEstimateNo.append(estimateDate.replace("-", ""));
		newEstimateNo.append(String.format("%02d", i));

		return newEstimateNo.toString();
	}

	public String getNewEstimateDetailNo(String estimateNo) {

		StringBuffer newEstimateDetailNo = null;

		int i = estimateMapper.selectEstimateDetailSeq(estimateNo);

		newEstimateDetailNo = new StringBuffer();
		newEstimateDetailNo.append("ES");
		newEstimateDetailNo.append(estimateNo.replace("-", ""));
		newEstimateDetailNo.append("-");
		newEstimateDetailNo.append(String.format("%02d", i));

		return newEstimateDetailNo.toString();
	}

	@Override
    @Transactional
    public HashMap<String, Object> updateEstimate(String estimateDate, EstimateTO updateEstimateTO) {
        HashMap<String, Object> resultMap = new HashMap<>();
        try {
            estimateMapper.updateEstimate(updateEstimateTO);
            resultMap.put("result", "success");
            resultMap.put("errorCode", 1);
            resultMap.put("errorMsg", "성공");
        } catch (Exception e) {
            e.printStackTrace();
            resultMap.put("errorCode", -1);
            resultMap.put("errorMsg", e.getMessage());
        }
        return resultMap;
    }

	@Override
	public ModelMap removeEstimate(String estimateNo, String status) {

		ModelMap resultMap = null;

		estimateMapper.deleteEstimate(estimateNo);

		ArrayList<EstimateDetailTO> estimateDetailTOList = getEstimateDetailList(estimateNo);

		for (EstimateDetailTO bean : estimateDetailTOList) {

			bean.setStatus(status);

		}

		resultMap = batchEstimateDetailListProcess(estimateDetailTOList,estimateNo);

		resultMap.put("removeEstimateNo", estimateNo);

		return resultMap;
	}

	@Override
	public ModelMap batchEstimateDetailListProcess(ArrayList<EstimateDetailTO> estimateDetailTOList,String estimateNo) {

		ModelMap resultMap = new ModelMap();

		ArrayList<EstimateDetailTO> list = new ArrayList<>();

		ArrayList<String> insertList = new ArrayList<>();
		ArrayList<String> updateList = new ArrayList<>();
		ArrayList<String> deleteList = new ArrayList<>();

		estimateMapper.initDetailSeq();
		list = estimateMapper.selectEstimateDetailCount(estimateNo);
		TreeSet<Integer> intSet = new TreeSet<>();
		int cnt;

		for(EstimateDetailTO bean : list) {
			String estimateDetailNo = bean.getEstimateDetailNo();
			int no = Integer.parseInt(estimateDetailNo.split("-")[1]);
			intSet.add(no);
		}

		if (intSet.isEmpty()) {
			cnt =  1;
		} else {
			cnt =  intSet.pollLast() + 1;
		}

		boolean isDelete=false;
		for (EstimateDetailTO bean : estimateDetailTOList) {

			String status = bean.getStatus();

			switch (status) {

				case "INSERT":
					if(cnt==1) {
						estimateMapper.insertEstimateDetail(bean);
					}else {
						ArrayList<EstimateDetailTO> newList = estimateMapper.selectEstimateDetailCount(estimateNo);
						int newCnt;
						for(EstimateDetailTO newbean : newList) {
							String estimateDetailNo = newbean.getEstimateDetailNo();
							int no = Integer.parseInt(estimateDetailNo.split("-")[1]);
							intSet.add(no);
						}

						if (intSet.isEmpty()) {
							newCnt =  1;
						} else {
							newCnt =  intSet.pollLast() + 1;
						}
						StringBuffer newEstimateDetailNo = new StringBuffer();
						newEstimateDetailNo.append("ES");
						newEstimateDetailNo.append(estimateNo.replace("-", ""));
						newEstimateDetailNo.append("-");
						newEstimateDetailNo.append(String.format("%02d", newCnt));
						bean.setEstimateDetailNo(newEstimateDetailNo.toString());
						estimateMapper.insertEstimateDetail(bean);
					}
					insertList.add(bean.getEstimateDetailNo());
					break;

				case "UPDATE":
					estimateMapper.updateEstimateDetail(bean);
					updateList.add(bean.getEstimateDetailNo());
					break;

				case "DELETE":
					estimateMapper.deleteEstimateDetail(bean);
					deleteList.add(bean.getEstimateDetailNo());
					isDelete=true;
					//기존의 값을 삭제했을 경우
					break;
			}
		}
		if(isDelete==true) {
			for (EstimateDetailTO bean : estimateDetailTOList) {
				int i = estimateMapper.selectEstimateDetailSeq(estimateNo);
				String newSeq = String.format("%02d", i);
				HashMap<String, String> map=new HashMap<>();
				map.put("estimateDetailNo", bean.getEstimateDetailNo());
				map.put("newSeq", newSeq);
				estimateMapper.reArrangeEstimateDetail(map);
			}
			estimateMapper.initDetailSeq();
		}
		resultMap.put("INSERT", insertList);
		resultMap.put("UPDATE", updateList);
		resultMap.put("DELETE", deleteList);

		return resultMap;
	}

	@Override
	public ArrayList<ContractInfoTO> getContractList(String searchCondition, String startDate, String endDate, String customerCode) {
		ArrayList<ContractInfoTO> contractInfoTOList = null;

		HashMap<String, String> map = new HashMap<>();

		map.put("searchCondition", searchCondition);
		map.put("startDate", startDate);
		map.put("endDate", endDate);
		map.put("customerCode", customerCode);

		contractInfoTOList = contractMapper.selectContractList(map);

		for (ContractInfoTO bean : contractInfoTOList) {
			bean.setContractDetailTOList(contractMapper.selectContractDetailList(bean.getContractNo()));
		}
		return contractInfoTOList;
	}


	@Override
	public ArrayList<ContractDetailTO> getContractDetailList(String contractNo) {

		ArrayList<ContractDetailTO> contractDetailTOList = null;

		contractDetailTOList = contractMapper.selectContractDetailList(contractNo);

		return contractDetailTOList;
	}

	@Override
	public ArrayList<EstimateTO> getEstimateListInContractAvailable(String startDate, String endDate) {

		ArrayList<EstimateTO> estimateListInContractAvailable = null;

		HashMap<String, String> map = new HashMap<>();

		map.put("startDate", startDate);
		map.put("endDate", endDate);

		estimateListInContractAvailable = contractMapper.selectEstimateListInContractAvailable(map);

		for (EstimateTO bean : estimateListInContractAvailable) {

			bean.setEstimateDetailTOList(estimateMapper.selectEstimateDetailList(bean.getEstimateNo()));//ES2022011360

		}

		return estimateListInContractAvailable;
	}

	@Override
//   public ModelMap addNewContract(HashMap<String,String[]>  workingContractList) {
	public ModelMap addNewContract(Map<String,Object> workingContractList) {

		ModelMap resultMap = new ModelMap();
//      HashMap<String,String> setValue = null;
//      StringBuffer str = null;
//
//      setValue=new HashMap<String,String>();
//      for(String key:workingContractList.keySet()) {
//         str=new StringBuffer();
//
//         // {수주상세번호,수주유형,수주등록자...)
//         for(String value:workingContractList.get(key)) {
//            if(key.equals("contractDate")) {
//               String newContractNo=getNewContractNo(value);
//               str.append(newContractNo+",");
//            }
//            else str.append(value+",");
//         }
//
//         str.substring(0, str.length()-1);
//         if(key.equals("contractDate"))
//            setValue.put("newContractNo", str.toString());
//
//         else
//            setValue.put(key, str.toString());
//      }
		String newContractNo=getNewContractNo((String)workingContractList.get("contractDate"));
		workingContractList.put("newContractNo", newContractNo);
		contractMapper.insertContractDetail(workingContractList);

		resultMap.put("gridRowJson", workingContractList.get("RESULT"));
		resultMap.put("errorCode", workingContractList.get("ERROR_CODE"));
		resultMap.put("errorMsg", workingContractList.get("ERROR_MSG"));

		return resultMap;
	}

	public String getNewContractNo(String contractDate) {

		StringBuffer newContractNo = null;

		int i = contractMapper.selectContractCount(contractDate);
		newContractNo = new StringBuffer();
		newContractNo.append("CO"); //CO
		newContractNo.append(contractDate.replace("-", ""));
		newContractNo.append(String.format("%02d", i));

		return newContractNo.toString();
	}

	@Override
	public ModelMap batchContractDetailListProcess(ArrayList<ContractDetailTO> contractDetailTOList) {

		ModelMap resultMap = new ModelMap();

		ArrayList<String> insertList = new ArrayList<>();
		ArrayList<String> updateList = new ArrayList<>();
		ArrayList<String> deleteList = new ArrayList<>();

		for (ContractDetailTO bean : contractDetailTOList) {

			String status = bean.getStatus();

			switch (status) {

				case "INSERT":

					//contractMapper.insertContractDetail(bean);
					insertList.add(bean.getContractDetailNo());

					break;

				case "UPDATE":

					//contractMapper.updateContractDetail(bean);
					updateList.add(bean.getContractDetailNo());

					break;

				case "DELETE":

					contractMapper.deleteContractDetail(bean);
					deleteList.add(bean.getContractDetailNo());

					break;

			}

		}

		resultMap.put("INSERT", insertList);
		resultMap.put("UPDATE", updateList);
		resultMap.put("DELETE", deleteList);

		return resultMap;
	}

	@Override
	public void changeContractStatusInEstimate(String estimateNo, String contractStatus) {

		HashMap<String, String> map = new HashMap<>();

		map.put("estimateNo", estimateNo);
		map.put("contractStatus", contractStatus);

		estimateMapper.changeContractStatusOfEstimate(map);

	}

	public void processPlan(HashMap<String,String[]> processMap) {
		ModelMap resultMap = new ModelMap();
		HashMap<String, String> map = new HashMap<>();


		System.out.println("서비스임플 map : "+processMap);
		Set<String> keys = processMap.keySet();
		keys.forEach((key)->{
			System.out.println(processMap.get(key));
			for(String val:processMap.get(key)) {
				map.put(key,val);
			}
		});
		System.out.println("프로시저 변수값 map : "+map);
		contractMapper.processPlan(map);


	}
}
