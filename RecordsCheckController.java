package us.tx.state.dfps.web.person.controller;

import java.io.IOException;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.*;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.apache.log4j.Logger;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ObjectUtils;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.Errors;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.fasterxml.jackson.databind.ObjectMapper;

import us.tx.state.dfps.businessdelegate.StringHelper;
import us.tx.state.dfps.businessdelegate.util.CacheAdapter;
import us.tx.state.dfps.businessdelegate.util.TypeConvUtil;
import us.tx.state.dfps.common.dto.CommonDto;
import us.tx.state.dfps.common.dto.ErrorDto;
import us.tx.state.dfps.common.dto.FormTagDto;
import us.tx.state.dfps.common.web.CodesConstant;
import us.tx.state.dfps.common.web.MessagesConstants;
import us.tx.state.dfps.common.web.WebConstants;
import us.tx.state.dfps.common.web.bean.AddressDetailBean;import us.tx.state.dfps.person.businessdelegate.CriminalHistoryBusinessDelegate;
import us.tx.state.dfps.person.businessdelegate.RecordsCheckBusinessDelegate;
import us.tx.state.dfps.person.businessdelegate.RecordsCheckBusinessDelegateDto;
import us.tx.state.dfps.person.businessdelegate.RecordsCheckListBusinessDelegate;
import us.tx.state.dfps.person.util.RecordsCheckUtil;
import us.tx.state.dfps.service.common.request.CriminalHistoryReq;
import us.tx.state.dfps.service.common.request.CriminalHistoryUpdateReq;
import us.tx.state.dfps.service.common.request.RecordsCheckDetailReq;
import us.tx.state.dfps.service.common.request.RecordsCheckReq;
import us.tx.state.dfps.service.common.response.CrimHistoryRes;
import us.tx.state.dfps.service.common.response.RecordsCheckListRes;
import us.tx.state.dfps.service.person.dto.*;
import us.tx.state.dfps.web.common.SessionConstants;
import us.tx.state.dfps.web.controller.BaseController;
import us.tx.state.dfps.web.person.utils.RecordCheckUtil;
import us.tx.state.dfps.web.person.validator.RecordsCheckValidator;
import us.tx.state.dfps.web.security.user.UserProfile;
import us.tx.state.dfps.web.security.user.UserRolesEnum;
import us.tx.state.dfps.web.utils.JsonUtil;
import us.tx.state.dfps.web.utils.PageMode;
import us.tx.state.dfps.web.utils.ServerInfoUtil;
import us.tx.state.dfps.web.utils.UserData;

/**
 * web- IMPACT PHASE 2 MODERNIZATION Class Description:This class is used to
 * process requests for Records Check Jun 6, 2017- 6:58:20 PM © 2017 Texas
 * Department of Family and Protective Services
 * ********Change History**********
 * 02/12/2021 nairl artf172946 : DEV BR 21.01 Support Manual Entry of Results from DPS’ SecureSite into IMPACT P2
 */
@Controller
@RequestMapping("/case/person/record/")
public class RecordsCheckController extends BaseController {

	private static final Logger log = Logger.getLogger(RecordsCheckController.class);

	private static final String VALIDATE_ACTION_STRING = "validate";

	private static final String SAVE_ACTION_STRING = "save";

	private static final String COMMON_APP_DOWN_MESSAGE = "Validating Name failed. Please contact the CSC and provide them with the following information: Common Application database is down.";

	private static final String RENDER_FORMAT = "HTML_WITH_SHELL";

	private static final String EXTENSIBLE_STYLE_SHEET = "/WEB-INF/Html.xslt";

	private static final String NULL_STRING = "null";

	private static final String LTTRNOT = "LTTRNOT";
	private static final String NLCMNOT = "NLCMNOT";
	private static final String ACTRNOT = "ACTRNOT";
	private static final String BARRNOT = "BARRNOT";
	private static final String CREGNOT = "CREGNOT";
	private static final String PCSRNOT = "PCSRNOT";
	private static final String FBIHVEL = "FBIHVEL";
	private static final String FBIVIEL = "FBIVIEL";
	private static final String FBIENOT = "FBIENOT";
	private static final String FBIINOT = "FBIINOT";
	private static final String PCSINEN = "PCSINEN";
	private static final String FORM_TAG_DTO_MAP = "formTagDtoMap";
	private static final String sRecordsCheckNotif = "0";
	private static final String ZERO = "0";
	private static final String ID_REC_CHECK = "idRecCheck";
	private static final String REQ_FUNC_CD = "reqFuncCd";

	/**
	 * Instance of CacheAdapter
	 */
	@Autowired
	CacheAdapter cacheAdapter;

	@Autowired
	RecordsCheckBusinessDelegate recordBusinessDelegate;

	@Autowired
	RecordsCheckListBusinessDelegate recordsListBusinessDelegate;

	@Autowired
	RecordsCheckUtil recordCheckUtil;

	@Autowired
	RecordsCheckValidator recordsCheckValidator;

	@Autowired
	RecordCheckUtil recordsCheckUtility;

	@Autowired
	JsonUtil jsonUtil;

	@Autowired
	CriminalHistoryBusinessDelegate criminalHistoryBusinessDelegate; // Added for artf172946

	/**
	 * @param binder
	 */

	/**
	 * Method Name: displayRecordsCheckDetail. Method Description:This method is
	 * used to display the Records Check Detail page.
	 *
	 * @param model
	 * @param recordsCheckDto
	 * @param pageMode
	 * @param request
	 * @param recordsCheckDetailIndex
	 * @param session
	 * @return
	 */
	@SuppressWarnings("unchecked")
	@RequestMapping(value = "/recordAction")
	public String displayRecordsCheckDetail(Model model,
											@ModelAttribute("recordsCheckDto") RecordsCheckDto recordsCheckDto,
											@RequestParam("pageMode") String pageMode, HttpServletRequest request,
											@RequestParam("recordsCheckDetailIndex") String recordsCheckDetailIndex,
											HttpSession session) {
		Map<String, Object> returnMap = new HashMap<String, Object>();
		HashMap<String, Object> forwardedValues = (HashMap<String, Object>) request.getAttribute("forwardMap");
		String recordsDeterminationsStr = WebConstants.EMPTY_STRING;
		List<String> errorList = new ArrayList<String>();
		String indReviewNowLaterString = null;
		Date dtLastUpdate = new Date();
		boolean indReviewNowLater = false;
		boolean indRowCanBeDeleted = false;
		boolean indModify = false;
		boolean isShowEligibleButton = false;
		boolean isShowInEligibleButton = false;
		boolean isDisableButton = false;
		Long idPerson = 0l;
		Long idRecCheck = 0l;
		String nmRequested = WebConstants.EMPTY_STRING;
		List<RecordsCheckDto> recordsList = new ArrayList<RecordsCheckDto>();
		UserData commonData = getUserData(model, request);
		UserProfile userProfile = (UserProfile) request.getSession().getAttribute(SessionConstants.USER_PROFILE);


		if (NumberUtils.isParsable(recordsCheckDetailIndex)){
			request.setAttribute(ID_REC_CHECK, Long.parseLong(recordsCheckDetailIndex));
		}else{
			request.setAttribute(REQ_FUNC_CD, WebConstants.ACTION_A);
		}

		// Call the utility method to get the records check list
		RecordsCheckListRes response = recordsCheckUtility.getRecordsCheckList(request, commonData);

		boolean indRecCheckAccess = false;
		if (request.isUserInRole(UserRolesEnum.SEC_EMPL_REC_CHECK.toString())) {
			indRecCheckAccess = true;
		}
		if (WebConstants.INTAKE_STAGE.equalsIgnoreCase(commonData.getCdStage())) {
			recordsList = response.getListToBeDisplayedINT();
		} else {
			recordsList = response.getListToBeDisplayed();
		}
		if (PageMode.MODIFY.equalsIgnoreCase(pageMode)) {
			indModify = true;
		}
		if (!ObjectUtils.isEmpty(commonData) && commonData.getIdPerson() != 0) {
			idPerson = commonData.getIdPerson();
		}
		boolean error = false;

		/*
		 * There is a flow where after saving the Records Check Details , the Records
		 * Check Details page should be displayed
		 */
		if (!ObjectUtils.isEmpty(recordsCheckDetailIndex)) {
			idRecCheck = Long.parseLong(recordsCheckDetailIndex);
		}
		// Check if the list from session is not empty if
		if (!ObjectUtils.isEmpty(pageMode) && (pageMode.equals(PageMode.MODIFY) || pageMode.equals(PageMode.VIEW))
				&& !CollectionUtils.isEmpty(recordsList)) {
			RecordsCheckDto checkDto = recordsList.stream().filter(recordsCheckDetail -> recordsCheckDetail
					.getIdRecCheck().equals(Long.valueOf(recordsCheckDetailIndex))).findAny().orElse(null);
			if (!ObjectUtils.isEmpty(checkDto)) {
				indRowCanBeDeleted = checkDto.getRowCanBeDeleted();
				idRecCheck = checkDto.getIdRecCheck();
				nmRequested = checkDto.getNmRequestedBy();
				dtLastUpdate = checkDto.getDtLastUpdate();
				recordsDeterminationsStr = jsonUtil.objectToJsonString(checkDto.getRecordsCheckDetermination());
				model.addAttribute("index", recordsCheckDetailIndex);
			}

			//Manual eligible and ineligible notification for PCS accout type CABCSA-5 and CABCSA-238
			String contractType = WebConstants.EMPTY_STRING;
			if(ObjectUtils.isEmpty(checkDto.getContractType()))
			{
				contractType = recordBusinessDelegate.getRecordsCheckDetail(checkDto.getIdRecCheck()).getContractType();
				checkDto.setContractType(contractType);
			}
			if((!ObjectUtils.isEmpty(contractType)) && checkDto.getContractType().equals( "PCS"))
			{
				request.setAttribute(ID_REC_CHECK,Long.parseLong("0"));
				RecordsCheckListRes checkListRes =recordsCheckUtility.getRecordsCheckList(request,commonData);

				Map<LocalDate, List<RecordsCheckDto>> groupedRecordCheckDTO = checkListRes.getListToBeDisplayed().stream()
						.collect(Collectors.groupingBy(dto -> dto.getDtRecCheckRequest()
								.toInstant()
								.atZone(ZoneId.systemDefault())
								.toLocalDate()));

				groupedRecordCheckDTO.forEach((key1,recordsCheckDtoList)->{
				Boolean isRecordCheckEligible = recordsCheckDtoList.stream().
						allMatch(recordCheck -> Objects.equals(recordCheck.getRecChkDeterm(), WebConstants.ELGB ) || Objects.equals(recordCheck.getRecChkDeterm(), WebConstants.CLER ));
				Boolean isNonFinalDetermination = recordsCheckDtoList.stream().
							anyMatch(recordCheck -> Objects.equals(recordCheck.getRecChkDeterm(),WebConstants.MHPA)
												    ||Objects.equals(recordCheck.getRecChkDeterm(),WebConstants.PFRBE)
									                ||Objects.equals(recordCheck.getRecChkDeterm(),WebConstants.PFRBI));
				Boolean isAllChecksNotCompleted = recordsCheckDtoList.stream().
						anyMatch(recordCheck -> Objects.equals(recordCheck.getDtDetermFinal(),WebConstants.NULL_CASTOR_DATE));
				if(recordsCheckDtoList.stream().anyMatch(recordCheck -> Objects.equals(recordCheck.getIdRecCheck(), checkDto.getIdRecCheck()))) {
					checkDto.setShowEligibleEmailButton(isRecordCheckEligible);
					checkDto.setShowIneligibleEmailButton(!isRecordCheckEligible);
					checkDto.setDisableEmailButton(isAllChecksNotCompleted || isNonFinalDetermination);
				}
				});

				if(checkDto.getIdRecCheck().equals(Long.valueOf(recordsCheckDetailIndex)) && checkDto.getRecChkDeterm() != null) {
					isDisableButton = checkDto.isDisableEmailButton();
					isShowEligibleButton = checkDto.isShowEligibleEmailButton();
					isShowInEligibleButton=checkDto.isShowIneligibleEmailButton();
				}

			}

		}

		RecordsCheckDto validationRecordsDto = new RecordsCheckDto();
		if (!ObjectUtils.isEmpty(pageMode) && pageMode.equals(PageMode.NEW)) {
			validationRecordsDto = recordsCheckDto;
			if (!ObjectUtils.isEmpty(validationRecordsDto.getRecCheckCheckType())) {
				if (CodesConstant.CCHKTYPE_10.equalsIgnoreCase(recordsCheckDto.getRecCheckCheckType())) {
					Errors errors = new BeanPropertyBindingResult(validationRecordsDto, "recordsCheckDto");
					RecordsCheckDetailReq checkDetailReq = new RecordsCheckDetailReq();
					checkDetailReq.setIdRecCheckPerson(idPerson);
					List<PersonInfoDto> nameList = recordBusinessDelegate.callNameRequest(checkDetailReq);
					validationRecordsDto.setCallingPage(commonData.getPrePage());
					errorList = validateSave(validationRecordsDto, idPerson, indRecCheckAccess, errors,
							recordsCheckDto.getButtonClicked(), nameList);
					if (errors.hasErrors() || (!CollectionUtils.isEmpty(errorList) && errorList.size() > 0)) {
						validationRecordsDto.setCheckTypeList(
								(Map<String, String>) JsonUtil.jsonToMap(recordsCheckDto.getCheckTypeListStr()));
						validationRecordsDto.setCancelReasonList(
								(Map<String, String>) JsonUtil.jsonToMap(recordsCheckDto.getCancelReasonListStr()));
						validationRecordsDto.setDeterminationList(
								(Map<String, String>) JsonUtil.jsonToMap(recordsCheckDto.getDeterminationListStr()));
						if (!(errors.getAllErrors().toString()
								.contains(cacheAdapter.getMessage(MessagesConstants.MSG_CRIM_HIST_NM_INVALID))
								|| (errors.getAllErrors()).toString().contains(COMMON_APP_DOWN_MESSAGE))) {
							validationRecordsDto.setRecCheckCheckType(WebConstants.EMPTY_STRING);
							validationRecordsDto.setRecCheckComments(WebConstants.EMPTY_STRING);
						}
						validationRecordsDto.setDisableCompletedDate(true);
						validationRecordsDto.setDtRecCheckCompleted(null);
						Integer errorCount = 0;
						if (!CollectionUtils.isEmpty(errorList)) {
							errorCount = errorList.size();
						}
						if (!ObjectUtils.isEmpty(errors) && errors.hasErrors()) {
							errorCount = errorCount + errors.getErrorCount();
						}

						model.addAttribute("errorCount", errorCount);
						model.addAttribute("errorList", errorList);
						model.addAttribute("recordsCheckDto", validationRecordsDto);
						model.addAttribute("nmPersonFull", commonData.getNmPerson());
						model.addAttribute("personId", idPerson);
						model.addAttribute("activeThirdLevelNav", "recordsCheck");
						model.addAttribute("activeSideNav", "person");
						model.addAttribute("cacheAdapter", cacheAdapter);
						updateUserData(commonData, model);
						error = true;
					} else {
						indReviewNowLater = true;
					}
				}
			}
		}
		if (!error) {
			// Fetching the Records Check details
			if (!VALIDATE_ACTION_STRING.equals(recordsCheckDto.getButtonClicked())) {
				recordsCheckDto = new RecordsCheckDto();
			}
			if (model.asMap().containsKey("bReviewNowLater")) {
				indReviewNowLaterString = (String) model.asMap().get("bReviewNowLater");
			} else if (!ObjectUtils.isEmpty(forwardedValues)
					&& !ObjectUtils.isEmpty(forwardedValues.get("bReviewNowLater"))) {
				indReviewNowLaterString = (String) forwardedValues.get("bReviewNowLater");
			}
			if (model.asMap().containsKey("setIndEmailErrorMessage")) {
				errorList.add((String) (model.asMap().get("setIndEmailErrorMessage")));
			} else if (!ObjectUtils.isEmpty(forwardedValues)
					&& !ObjectUtils.isEmpty(forwardedValues.get("setIndEmailErrorMessage"))) {
				errorList.add((String) forwardedValues.get("setIndEmailErrorMessage"));
			}
			/*
			 * Call the business delegate method to create the Model object for displaying
			 * the Records Check detail screen
			 */
			RecordsCheckBusinessDelegateDto delegateDto = new RecordsCheckBusinessDelegateDto();

			// idStage
			delegateDto.setIdStage(commonData.getIdStage());
			// modifyFlag
			delegateDto.setIndModifyFlag(indModify);
			// personType
			delegateDto.setPersonType(commonData.getPersonType());
			// nmRequested
			delegateDto.setNmRequested(nmRequested);
			// idRecCheck
			delegateDto.setIdRecCheck(idRecCheck);
			// pageMode
			delegateDto.setPageMode(pageMode);
			// last Updated date string
			if (!ObjectUtils.isEmpty(dtLastUpdate)) {
				delegateDto.setDtLastUpdateStr(jsonUtil.objectToJsonString(dtLastUpdate));
			}
			delegateDto.setIdUserLogon(userProfile.getUserId());
			// person Id
			delegateDto.setIdPerson(idPerson);
			// row can be deleted
			delegateDto.setIndRowCanBeDeleted(indRowCanBeDeleted);
			// flag value if the user has REC_CHECK access
			if (request.isUserInRole(UserRolesEnum.SEC_EMPL_REC_CHECK.toString())) {
				delegateDto.setIndRecCheckAccess(true);
			}
			// flag value if the user has SEC_MNTN_PERSON access
			if (request.isUserInRole(UserRolesEnum.SEC_MNTN_PERSON.toString())) {
				delegateDto.setIndMntnPersonkAccess(true);
			}
			// flag value if the user has SEC_RESTRICT_DPS_RESULTS access
			if (request.isUserInRole(UserRolesEnum.SEC_RESTRICT_DPS_RESULTS.toString())) {
				delegateDto.setIndDpsAccess(true);
			}
			// flag value if the user has SEC_RESTRICT_FBI_RESULTS access
			if (request.isUserInRole(UserRolesEnum.SEC_RESTRICT_FBI_RESULTS.toString())) {
				delegateDto.setIndFBIAccess(true);
			}
			// idUser
			delegateDto.setIdUser(userProfile.getUserId());
			// user name
			delegateDto.setNmUser(userProfile.getUserFullName());
			// stage code
			delegateDto.setCdStage(commonData.getCdStage());
			// flag value for review now or later
			delegateDto.setIndReviewNowLater(indReviewNowLater);
			// last updated date
			delegateDto.setDtLastUpdate(dtLastUpdate);
			delegateDto.setIndReviewNowLaterString(indReviewNowLaterString);
			String informationMessage = new String(WebConstants.EMPTY_STRING);
			String errorOpeningDocument = new String(WebConstants.EMPTY_STRING);

			if (model.asMap().containsKey("errorOpeningDocumentMessage")) {
				errorOpeningDocument = (String) (model.asMap().get("errorOpeningDocumentMessage"));
			} else if (!ObjectUtils.isEmpty(forwardedValues)
					&& !ObjectUtils.isEmpty(forwardedValues.get("errorOpeningDocumentMessage"))) {
				errorOpeningDocument = (String) forwardedValues.get("errorOpeningDocumentMessage");
			}
			if (model.asMap().containsKey("informationMessage")) {
				informationMessage = (String) (model.asMap().get("informationMessage"));
			}else if (!ObjectUtils.isEmpty(forwardedValues)
					&& !ObjectUtils.isEmpty(forwardedValues.get("informationMessage"))) {
				informationMessage = (String) forwardedValues.get("informationMessage");
			}
			model.addAttribute("errorOpeningDocument", errorOpeningDocument);
			delegateDto.setInformationMessage(informationMessage);
			recordsCheckDto.setRecordsCheckDeterminationListStr(recordsDeterminationsStr);
			recordsCheckDto.setShowEligibleEmailButton(isShowEligibleButton);
			recordsCheckDto.setDisableEmailButton(isDisableButton);
			recordsCheckDto.setShowIneligibleEmailButton(isShowInEligibleButton);



			// Call the business delegate to populate the Model object for
			// displaying the
			// Records Check detail
			returnMap = (Map<String, Object>) recordBusinessDelegate.createModelForDisplayRecordsCheck(recordsCheckDto,
					delegateDto, response);
			recordsCheckDto = (RecordsCheckDto) returnMap.get("recordsCheckDto");



			if (!(boolean) returnMap.get("bEBCNarrativeDisabled")) {
				List<FormTagDto> formTagDtoList = new ArrayList<FormTagDto>();
				FormTagDto formTagDtoA = new FormTagDto();
				formTagDtoA.setDocType("EBC");
				formTagDtoA.setRenderFormat(RENDER_FORMAT);
				if (!ObjectUtils.isEmpty(commonData)) {
					formTagDtoA.setCheckStage(String.valueOf(commonData.getIdStage()));

				}
				formTagDtoA.setsIdRecCheck(recordsCheckDto.getIdRecCheck().toString());
				formTagDtoA.setTimestamp(new Date().toString());
				formTagDtoA.setModeOfPage(WebConstants.PAGE_MODE_MODIFY);
				formTagDtoA.setDocExists((String) returnMap.get("docExists"));
				formTagDtoList.add(formTagDtoA);
				Map<String, String> formTagDtoMap = new HashMap<>();
				formTagDtoMap.put(formTagDtoA.getDocType(), JsonUtil.objectToEcodedJson(formTagDtoA));
				model.addAttribute(SessionConstants.FORM_TAG_DTOLIST, formTagDtoMap);
			}
			// setting the list of dropdown values in HttpServletRequest Session for
			// retrieving it during validation errors ='
			if (!ObjectUtils.isEmpty(recordsCheckDto.getRecCheckCheckType())
					&& recordsCheckDto.getRecCheckCheckType().equalsIgnoreCase(CodesConstant.CCHKTYPE_75)) {
				Map<String, String> checkTypeList = new TreeMap<String, String>();
				checkTypeList.put(CodesConstant.CCHKTYPE_75,
						cacheAdapter.getDecode(CodesConstant.CCHKTYPE, CodesConstant.CCHKTYPE_75));
				recordsCheckDto.setCheckTypeList(checkTypeList);
			}

			if (!ObjectUtils.isEmpty(recordsCheckDto.getRecCheckCheckType())
					&& recordsCheckDto.getRecCheckCheckType().equalsIgnoreCase(CodesConstant.CCHKTYPE_81)) {
				Map<String, String> rapBackReviewList = new TreeMap<String, String>();
				rapBackReviewList = cacheAdapter.getCodeCategories(CodesConstant.RBRVW);
				recordsCheckDto.setRapBackReviewList(rapBackReviewList);
			}


			recordsCheckDto.setCheckTypeListStr(jsonUtil.objectToJsonString(recordsCheckDto.getCheckTypeList()));
			recordsCheckDto.setCancelReasonListStr(jsonUtil.objectToJsonString(recordsCheckDto.getCancelReasonList()));
			recordsCheckDto.setDeterminationListStr(jsonUtil.objectToJsonString(recordsCheckDto.getDeterminationList()));
			if(!ObjectUtils.isEmpty(commonData.getDtStageClose())){
				recordsCheckDto.setStageClosed(true);
			}

			//Added for Warranty defect 11804
			if ((!ObjectUtils.isEmpty(recordsCheckDto.getDtRecCheckCompleted()))
				&& (CodesConstant.CDETERM_ELGB.equals(recordsCheckDto.getRecChkDeterm())
				|| CodesConstant.CDETERM_INEG.equals(recordsCheckDto.getRecChkDeterm())
				|| CodesConstant.CDETERM_NOTA.equals(recordsCheckDto.getRecChkDeterm())
				|| CodesConstant.CDETERM_CLER.equals(recordsCheckDto.getRecChkDeterm())
				|| CodesConstant.CDETERM_BAR.equals(recordsCheckDto.getRecChkDeterm()))
			) {
				recordsCheckDto.setDtRecCheckCompletedStr(
						jsonUtil.objectToJsonString(recordsCheckDto.getDtRecCheckCompleted()));
			}
			// Setting the flag values in the model
			if (returnMap.containsKey("indicator")) {
				model.addAttribute("indicator", (boolean) returnMap.get("indicator"));
			}
			if (returnMap.containsKey("contractnbr")) {
				model.addAttribute("contractnbr", (String) returnMap.get("contractnbr"));
			}
			if (returnMap.containsKey("bDisplayDelete")) {
				model.addAttribute("bDisplayDelete", (boolean) returnMap.get("bDisplayDelete"));
			}
			if (returnMap.containsKey("nbrPersonPhone")) {
				model.addAttribute("nbrPersonPhone", (String) returnMap.get("nbrPersonPhone"));
			}
			if (returnMap.containsKey("phonePrimary")) {
				model.addAttribute("phonePrimary", (String) returnMap.get("phonePrimary"));
			}
			if (returnMap.containsKey("idPersonPhone")) {
				model.addAttribute("idPersonPhone", (String) returnMap.get("idPersonPhone"));
			}
			if (returnMap.containsKey("phoneList")) {
				model.addAttribute("phoneList", (Map<String, String>) returnMap.get("phoneList"));
			}
			if (returnMap.containsKey("fPrintEmail")) {
				model.addAttribute("fPrintEmail", (String) returnMap.get("fPrintEmail"));
			}
			if (returnMap.containsKey("idEmailPerson")) {
				model.addAttribute("idEmailPerson", (String) returnMap.get("idEmailPerson"));
			}
			if (returnMap.containsKey("emailPrimary")) {
				model.addAttribute("emailPrimary", (String) returnMap.get("emailPrimary"));
			}
			if (returnMap.containsKey("emailPersonList")) {
				model.addAttribute("emailPersonList", (Map<String, String>) returnMap.get("emailPersonList"));
			}
			model.addAttribute("recordsCheckDto", recordsCheckDto);
			if(!TypeConvUtil.isNullOrEmpty(recordsCheckDto.getCdFbiSubscriptionDStatus()) && (recordsCheckDto.getCdFbiSubscriptionDStatus().equalsIgnoreCase(WebConstants.SUB))) {
				model.addAttribute("cdFbiSubscriptionDStatus", WebConstants.SUBSCRIBED);
			}
			if(!TypeConvUtil.isNullOrEmpty(recordsCheckDto.getCdFbiSubscriptionDStatus()) && (recordsCheckDto.getCdFbiSubscriptionDStatus().equalsIgnoreCase(WebConstants.UNS))) {
				model.addAttribute("cdFbiSubscriptionDStatus", WebConstants.UN_SUBSCRIBED);
			}
			model.addAttribute("nmPersonFull", commonData.getNmPerson());
			model.addAttribute("personId", idPerson);
			model.addAttribute("errorList", errorList);
			model.addAttribute("cacheAdapter", cacheAdapter);
			model.addAttribute("sectionHeading", (String) returnMap.get("sectionHeading"));
			model.addAttribute("idUser", userProfile.getUserId());
			setFormDetail(model, userProfile, idRecCheck);
			request.setAttribute("emailFlag", "Y");
		}
		String returnUrl = WebConstants.EMPTY_STRING;
		updateUserData(commonData, model);
		log.info("The object passed to the jsp are ");
		String callingPageTab = commonData.getPrePage();
		log.info("The object passed to the jsp are " +callingPageTab);
		if (WebConstants.STAFF_DETAIL.equals(callingPageTab)) {
			model.addAttribute("activeThirdLevelNav", "recordsCheck");
			model.addAttribute("activeSideNav", "staffSearch");
			returnUrl = "search/staffSearchThirdLevelNav/RecordsCheckDetail";
		} else if (WebConstants.SIDE_NAV_PERSON_SEARCH.equals(callingPageTab)) {
			model.addAttribute("activeThirdLevelNav", "recordsCheck");
			model.addAttribute("activeSideNav", "personSearch");
			returnUrl = "search/personThirdLevelNav/RecordsCheckDetail";
		} else {
			model.addAttribute("activeThirdLevelNav", "recordsCheck");
			model.addAttribute("activeSideNav", "person");
			returnUrl = "case/personThirdLevelNav/RecordsCheckDetail";
		}
		return returnUrl;
	}

	/**
	 * Method Name: saveRecordCheckDetail. Method Description:This method is used to
	 * save the Records Check Detail.
	 *
	 * @param model
	 * @param request
	 * @param checkDto
	 * @param redirectAttributes
	 * @param error
	 * @return
	 */
	@SuppressWarnings("unchecked")
	@RequestMapping(value = "/recordAction/SaveRecordCheckDetail")
	public String saveRecordCheckDetail(Model model, HttpServletRequest request,
										@ModelAttribute("recordsCheckDto") RecordsCheckDto checkDto, RedirectAttributes redirectAttributes,
										Errors error) {
		ObjectMapper mapper = new ObjectMapper();
		String returnUrl = WebConstants.EMPTY_STRING;
		HashMap<String, Object> forwardedValues = (HashMap<String, Object>) request.getAttribute("forwardMap");
		if(!ObjectUtils.isEmpty(checkDto.getTxtDpsSID()) && checkDto.getTxtDpsSID().contains(",")) {
			checkDto.setTxtDpsSID(checkDto.getTxtDpsSID().replace(",",""));
		}
		// Read checkDto from forwardmap if present
		if(!ObjectUtils.isEmpty(forwardedValues)) {
			if(forwardedValues.containsKey("recordsCheckDto")) {
				checkDto = (RecordsCheckDto) forwardedValues.get("recordsCheckDto");
			}
		}
		else {
			forwardedValues = new HashMap<String, Object>();
		}
		if (!ObjectUtils.isEmpty(checkDto.getRecordsCheckDeterminationListStr())) {
			try {
				checkDto.setRecordsCheckDetermination(
						mapper.readValue(checkDto.getRecordsCheckDeterminationListStr(), mapper.getTypeFactory()
								.constructCollectionType(List.class, RecordsCheckDeterminationDto.class)));
			} catch (IOException e) {
				log.info("Exception occured while converting json to list in save method of RecordsCheckController");
			}
		}

		checkDto.setRecCheckCheckType(request.getParameter("checkType"));
		List<String> errorList = new ArrayList<String>();
		Map<String, Object> returnValueMap = new HashMap<String, Object>();
		Long idPerson = 0l;
		UserProfile userProfile = (UserProfile) request.getSession().getAttribute(SessionConstants.USER_PROFILE);
		UserData commonData = getUserData(model, request);
		List<RecordsCheckDto> recordsCheckList = new ArrayList<RecordsCheckDto>();
		String path = EXTENSIBLE_STYLE_SHEET;
		URL styleSheetURL = null;
		try {
			styleSheetURL = request.getServletContext().getResource(path);
		} catch (MalformedURLException e) {
			log.info(
					"Exception occured while getting the resource path of stylesheet in save method of RecordsCheckController");

		}
		if (!ObjectUtils.isEmpty(checkDto.getIdRecCheck()) && !WebConstants.ZERO_VAL.equals(checkDto.getIdRecCheck())){
			request.setAttribute(ID_REC_CHECK, checkDto.getIdRecCheck());
			RecordsCheckListRes response = recordsCheckUtility.getRecordsCheckList(request, commonData);
			if (WebConstants.INTAKE_STAGE.equalsIgnoreCase(commonData.getCdStage())) {
				recordsCheckList = response.getListToBeDisplayedINT();
			} else {
				recordsCheckList = response.getListToBeDisplayed();
			}
		}
		boolean indRecCheckAccess = false;
		if (request.isUserInRole(UserRolesEnum.SEC_EMPL_REC_CHECK.toString())) {
			indRecCheckAccess = true;
		}
		if (!ObjectUtils.isEmpty(commonData) && commonData.getIdPerson() != 0) {
			idPerson = commonData.getIdPerson();
		}
		if (!ObjectUtils.isEmpty(checkDto.getDtLastUpdateStr())) {
			checkDto.setDtLastUpdate((Date) jsonUtil.jsonStringToObject(checkDto.getDtLastUpdateStr(), Date.class));
		}
		//Added for Warranty defect 11804
		if (!ObjectUtils.isEmpty(checkDto.getDtRecCheckCompletedStr())) {
			checkDto.setDtRecCheckCompleted((Date) jsonUtil.jsonStringToObject(checkDto.getDtRecCheckCompletedStr(), Date.class));
		}
		String actionReq = checkDto.getButtonClicked();
		// Perform the validation and return to the display screen if any errors
		// are
		// present
		RecordsCheckDetailReq checkDetailReq = new RecordsCheckDetailReq();
		checkDetailReq.setIdRecCheckPerson(idPerson);
		List<PersonInfoDto> nameList = recordBusinessDelegate.callNameRequest(checkDetailReq);
		checkDto.setCallingPage(commonData.getPrePage());
		errorList = validateSave(checkDto, idPerson, indRecCheckAccess, error, actionReq, nameList);

		// If the validation error list is not empty then displaying the Records
		// Check
		// Detail screen again
		if (error.hasErrors() || (!CollectionUtils.isEmpty(errorList) && errorList.size() > 0) ) {
			if (CodesConstant.CCHKTYPE_10.equalsIgnoreCase(checkDto.getRecCheckCheckType())) {
				model.addAttribute("flagClearType", "true");
			}
			if (CodesConstant.CCHKTYPE_80.equalsIgnoreCase(checkDto.getRecCheckCheckType())) {
				model.addAttribute("phoneList", JsonUtil.jsonToMap(checkDto.getPhoneListStr()));
				model.addAttribute("emailPersonList", JsonUtil.jsonToMap(checkDto.getEmailListStr()));
			}
			Integer errorCount = 0;
			if (!CollectionUtils.isEmpty(errorList)) {
				errorCount = errorList.size();
			}
			if (!ObjectUtils.isEmpty(error) && error.hasErrors()) {
				errorCount = errorCount + error.getErrorCount();
			}
			if (CodesConstant.CDETERM_PFRBI.equals(checkDto.getRecChkDeterm())
					|| CodesConstant.CDETERM_PFRBE.equals(checkDto.getRecChkDeterm())
					|| CodesConstant.CDETERM_PFRBB.equals(checkDto.getRecChkDeterm())
					|| CodesConstant.CDETERM_PFRBC.equals(checkDto.getRecChkDeterm())
					|| CodesConstant.CDETERM_RVHR.equals(checkDto.getRecChkDeterm())
					||CodesConstant.CDETERM_REEL.equals(checkDto.getRecChkDeterm())
					||CodesConstant.CDETERM_MHPA.equals(checkDto.getRecChkDeterm())
					||CodesConstant.CDETERM_PNRS.equals(checkDto.getRecChkDeterm())
					||CodesConstant.CDETERM_CLAP.equals(checkDto.getRecChkDeterm()))
			{
				checkDto.setDtRecCheckCompletedStr("");
			}
			model.addAttribute("errorCount", errorCount);
			checkDto.setCheckTypeList((Map<String, String>) JsonUtil.jsonToMap(checkDto.getCheckTypeListStr()));
			checkDto.setCancelReasonList((Map<String, String>) JsonUtil.jsonToMap(checkDto.getCancelReasonListStr()));
			checkDto.setDeterminationList((Map<String, String>) JsonUtil.jsonToMap(checkDto.getDeterminationListStr()));
			model.addAttribute("recordsCheckDto", checkDto);
			model.addAttribute("nmPersonFull", commonData.getNmPerson());
			model.addAttribute("personId", idPerson);
			model.addAttribute("activeThirdLevelNav", "recordsCheck");
			model.addAttribute("activeSideNav", "person");
			model.addAttribute("cacheAdapter", cacheAdapter);
			model.addAttribute("errorList", errorList);
			updateUserData(commonData, model);
			String callingPageTab = commonData.getPrePage();
			if (WebConstants.STAFF_DETAIL.equals(callingPageTab)) {
				model.addAttribute("activeThirdLevelNav", "recordsCheck");
				model.addAttribute("activeSideNav", "staffSearch");
				returnUrl = "search/staffSearchThirdLevelNav/RecordsCheckDetail";
			} else if (WebConstants.SIDE_NAV_PERSON_SEARCH.equals(callingPageTab)) {
				model.addAttribute("activeThirdLevelNav", "recordsCheck");
				model.addAttribute("activeSideNav", "personSearch");
				returnUrl = "search/personThirdLevelNav/RecordsCheckDetail";
			} else {
				model.addAttribute("activeThirdLevelNav", "recordsCheck");
				model.addAttribute("activeSideNav", "person");
				returnUrl = "case/personThirdLevelNav/RecordsCheckDetail";
			}
		}
		// Proceed with the save functionality
		else {
			CommonDto commonDto = new CommonDto();
			commonDto.setIdUser(userProfile.getUserId());
			commonDto.setIdUserLogon(userProfile.getUserLogonId());
			commonDto.setNmUserFullName(userProfile.getUserFullName());
			commonDto.setIdPerson(idPerson);
			commonDto.setIdStage(commonData.getIdStage());
			// Call the business delegate method for updating/saving the Records
			// Check
			// Detail
			RecordsCheckListRes recordsCheckSaveRes = recordBusinessDelegate.saveRecordsCheckDetail(checkDto,
					recordsCheckList, commonDto, styleSheetURL, nameList);
			if (!ObjectUtils.isEmpty(recordsCheckSaveRes.getErrorDto())
					&& recordsCheckSaveRes.getErrorDto().getErrorCode() > 0
					&& recordsCheckSaveRes.getErrorDto().getErrorCode() == WebConstants.TIME_MISMATCH_EXCEPTION) {
				errorList.add(cacheAdapter.getMessage(MessagesConstants.MSG_CMN_TMSTAMP_MISMATCH));
				if (CodesConstant.CCHKTYPE_10.equalsIgnoreCase(checkDto.getRecCheckCheckType())) {
					model.addAttribute("flagClearType", "true");
				}
				if (CodesConstant.CCHKTYPE_80.equalsIgnoreCase(checkDto.getRecCheckCheckType())) {
					model.addAttribute("phoneList", JsonUtil.jsonToMap(checkDto.getPhoneListStr()));
					model.addAttribute("emailPersonList", JsonUtil.jsonToMap(checkDto.getEmailListStr()));
				}
				model.addAttribute("errorCount", errorList.size());
				checkDto.setCheckTypeList((Map<String, String>) JsonUtil.jsonToMap(checkDto.getCheckTypeListStr()));
				checkDto.setCancelReasonList((Map<String, String>) JsonUtil.jsonToMap(checkDto.getCancelReasonListStr()));
				checkDto.setDeterminationList((Map<String, String>) JsonUtil.jsonToMap(checkDto.getDeterminationListStr()));
				model.addAttribute("recordsCheckDto", checkDto);
				model.addAttribute("nmPersonFull", commonData.getNmPerson());
				model.addAttribute("personId", idPerson);
				model.addAttribute("activeThirdLevelNav", "recordsCheck");
				model.addAttribute("activeSideNav", "person");
				model.addAttribute("cacheAdapter", cacheAdapter);
				model.addAttribute("errorList", errorList);
				updateUserData(commonData, model);
				String callingPageTab = commonData.getPrePage();
				if (WebConstants.STAFF_DETAIL.equals(callingPageTab)) {
					model.addAttribute("activeThirdLevelNav", "recordsCheck");
					model.addAttribute("activeSideNav", "staffSearch");
					returnUrl = "search/staffSearchThirdLevelNav/RecordsCheckDetail";
				} else if (WebConstants.SIDE_NAV_PERSON_SEARCH.equals(callingPageTab)) {
					model.addAttribute("activeThirdLevelNav", "recordsCheck");
					model.addAttribute("activeSideNav", "personSearch");
					returnUrl = "search/personThirdLevelNav/RecordsCheckDetail";
				} else {
					model.addAttribute("activeThirdLevelNav", "recordsCheck");
					model.addAttribute("activeSideNav", "person");
					returnUrl = "case/personThirdLevelNav/RecordsCheckDetail";
				}
			}else{
				/* Code changes for artf172946 */
				if (CodesConstant.CCHKTYPE_80.equalsIgnoreCase(checkDto.getRecCheckCheckType())) {
					CrimHistoryRes response = getCrimHistList(checkDto.getIdRecCheck());
					CrimHistoryDto crimHistInitialFBI = new CrimHistoryDto();
					CrimHistoryDto crimHistFrbSub = new CrimHistoryDto();
					if(!TypeConvUtil.isNullOrEmpty(response.getCriminalHistoryList())) {
						Optional<CrimHistoryDto> crimHistoryInitialFBI = response.getCriminalHistoryList().stream().filter(crimhistdto -> !ObjectUtils.isEmpty(crimhistdto.getdPSMatchType()) && crimhistdto.getdPSMatchType().equalsIgnoreCase(WebConstants.INITIAL_FBI)).findAny();
						crimHistInitialFBI = crimHistoryInitialFBI.isPresent()? crimHistoryInitialFBI.get() : null;
						Optional<CrimHistoryDto> crimHistoryFrbSub = response.getCriminalHistoryList().stream().filter(crimhistdto -> !ObjectUtils.isEmpty(crimhistdto.getdPSMatchType()) && crimhistdto.getdPSMatchType().equalsIgnoreCase(WebConstants.FRB_SUB)).findAny();
						crimHistFrbSub = crimHistoryFrbSub.isPresent()? crimHistoryFrbSub.get() : null;
					}
					if (!ObjectUtils.isEmpty(checkDto.getIndCrimHistoryResultCopied()) && checkDto.getIndCrimHistoryResultCopied().equalsIgnoreCase(WebConstants.YES)) {
								CriminalHistoryUpdateReq crimHistoryUpdateReq = populateCrimHistoryUpdateReq(TypeConvUtil.isNullOrEmpty(crimHistInitialFBI) ? WebConstants.REQ_FUNC_CD_ADD :WebConstants.REQ_FUNC_CD_UPDATE,crimHistInitialFBI,
										checkDto.getIdRecCheck(), String.valueOf(checkDto.getTxtDpsSID()), WebConstants.INITIAL_FBI);
							CrimHistoryRes criminalHistoryRes = criminalHistoryBusinessDelegate
									.saveCrimHistory(crimHistoryUpdateReq);
						}
						if (!ObjectUtils.isEmpty(checkDto.getIndRapBackSubscriptionCopied()) && checkDto.getIndRapBackSubscriptionCopied().equalsIgnoreCase(WebConstants.YES) && !ObjectUtils.isEmpty(checkDto.getDtRapBackExp())) {
							String dpsSID = (!ObjectUtils.isEmpty(checkDto.getIndCrimHistoryResultCopied())
									&& checkDto.getIndCrimHistoryResultCopied().equalsIgnoreCase(WebConstants.NO))? crimHistInitialFBI.getIdDPSS(): String.valueOf(checkDto.getTxtDpsSID());
							if (!ObjectUtils.isEmpty(checkDto.getIndCrimHistoryResultCopied()) && checkDto.getIndCrimHistoryResultCopied().equalsIgnoreCase(WebConstants.NO)){

							}
								CriminalHistoryUpdateReq crimHistoryUpdateReq = populateCrimHistoryUpdateReq(TypeConvUtil.isNullOrEmpty(crimHistFrbSub) ? WebConstants.REQ_FUNC_CD_ADD : WebConstants.REQ_FUNC_CD_UPDATE,crimHistFrbSub,
										checkDto.getIdRecCheck(), dpsSID, WebConstants.FRB_SUB);
								CrimHistoryRes criminalHistoryRes = criminalHistoryBusinessDelegate
										.saveCrimHistory(crimHistoryUpdateReq);
					}
					//[artf212427] Defect: 18415 - : FBI check preventing stage closure
					if ((null != checkDto.getCancelReason() && !checkDto.getCancelReason().isEmpty()) || (!ObjectUtils.isEmpty(checkDto.getIndCrimHistoryResultCopied()) && checkDto.getIndCrimHistoryResultCopied().equalsIgnoreCase(WebConstants.NO))) {
								if (!TypeConvUtil.isNullOrEmpty(crimHistInitialFBI)) {
							boolean crimHistNarrPresent = criminalHistoryBusinessDelegate
											.getCriminalHistNarr(crimHistInitialFBI.getIdCrimHist());
							if (!crimHistNarrPresent) {
										CriminalHistoryUpdateReq crimHistoryUpdateReq = populateCrimHistoryUpdateReq(WebConstants.REQ_FUNC_CD_DELETE,crimHistInitialFBI,
												checkDto.getIdRecCheck(), String.valueOf(checkDto.getTxtDpsSID()), WebConstants.INITIAL_FBI);
								CrimHistoryRes criminalHistoryRes = criminalHistoryBusinessDelegate
										.saveCrimHistory(crimHistoryUpdateReq);
							}
						}
					}
					if (!ObjectUtils.isEmpty(checkDto.getIndRapBackSubscriptionCopied()) && checkDto.getIndRapBackSubscriptionCopied().equalsIgnoreCase(WebConstants.NO)) {
						if (!ObjectUtils.isEmpty(crimHistFrbSub)) {
							boolean crimHistNarrPresent = criminalHistoryBusinessDelegate
									.getCriminalHistNarr(crimHistFrbSub.getIdCrimHist());
							if (!crimHistNarrPresent) {
								CriminalHistoryUpdateReq crimHistoryUpdateReq = populateCrimHistoryUpdateReq(WebConstants.REQ_FUNC_CD_DELETE,crimHistFrbSub,
										checkDto.getIdRecCheck(), String.valueOf(checkDto.getTxtDpsSID()), WebConstants.FRB_SUB);
								CrimHistoryRes criminalHistoryRes = criminalHistoryBusinessDelegate
										.saveCrimHistory(crimHistoryUpdateReq);
				}
						}
					}
				}

				if (CodesConstant.CCHKTYPE_81.equalsIgnoreCase(checkDto.getRecCheckCheckType())) {
					CrimHistoryRes response = getCrimHistList(checkDto.getIdRecCheck());
					CrimHistoryDto crimHistRapBack = new CrimHistoryDto();
					if(!TypeConvUtil.isNullOrEmpty(response.getCriminalHistoryList())) {
						Optional<CrimHistoryDto> crimHistoryRapBack = response.getCriminalHistoryList().stream().filter(crimhistdto -> !ObjectUtils.isEmpty(crimhistdto.getdPSMatchType()) && crimhistdto.getdPSMatchType().equalsIgnoreCase(WebConstants.RAP_BACK)).findAny();
						crimHistRapBack = crimHistoryRapBack.isPresent()? crimHistoryRapBack.get() : null;
					}
					if (!ObjectUtils.isEmpty(checkDto.getIndCrimHistoryResultCopied()) && checkDto.getIndCrimHistoryResultCopied().equalsIgnoreCase(WebConstants.YES)) {
						CriminalHistoryUpdateReq crimHistoryUpdateReq = populateCrimHistoryUpdateReq(TypeConvUtil.isNullOrEmpty(crimHistRapBack) ? WebConstants.REQ_FUNC_CD_ADD :WebConstants.REQ_FUNC_CD_UPDATE,crimHistRapBack,
								checkDto.getIdRecCheck(), String.valueOf(checkDto.getTxtDpsSID()), WebConstants.RAP_BACK);
						CrimHistoryRes criminalHistoryRes = criminalHistoryBusinessDelegate
								.saveCrimHistory(crimHistoryUpdateReq);
					}
				}
				/* End of code changes for artf172946 */
				returnValueMap = recordsCheckSaveRes.getReturnValuesMap();

				if (returnValueMap.containsKey("idRecCheckWS")) {
					forwardedValues.put("idRecCheckWS", (Long) returnValueMap.get("idRecCheckWS"));
					redirectAttributes.addFlashAttribute("idRecCheckWS", (Long) returnValueMap.get("idRecCheckWS"));
				}
				if (returnValueMap.containsKey("informationMessage")) {
					forwardedValues.put("informationMessage",
							(String) returnValueMap.get("informationMessage"));
					redirectAttributes.addFlashAttribute("informationMessage",
							(String) returnValueMap.get("informationMessage"));
				}
				if (returnValueMap.containsKey("bReviewNowLater")) {
					forwardedValues.put("bReviewNowLater", (String) returnValueMap.get("bReviewNowLater"));
					redirectAttributes.addFlashAttribute("bReviewNowLater", (String) returnValueMap.get("bReviewNowLater"));
				}
				if (returnValueMap.containsKey("bReviewNowLater")) {
					forwardedValues.put("bReviewNowLater", (String) returnValueMap.get("bReviewNowLater"));
					redirectAttributes.addFlashAttribute("bReviewNowLater", (String) returnValueMap.get("bReviewNowLater"));
				}
				if (returnValueMap.containsKey("txtUlIdRecCheck")) {
					forwardedValues.put("txtUlIdRecCheck",
							Long.valueOf((String) returnValueMap.get("txtUlIdRecCheck")));
					redirectAttributes.addFlashAttribute("txtUlIdRecCheck",
							Long.valueOf((String) returnValueMap.get("txtUlIdRecCheck")));
				}
				updateUserData(commonData, model);
				redirectAttributes.addFlashAttribute(WebConstants.USER_DATA,commonData);
				returnUrl = (String) returnValueMap.get("returnURL");
			}
		}
		request.setAttribute("forwardMap", forwardedValues);
		return returnUrl;
	}
	/* Code changes for artf172946 */
	private CriminalHistoryUpdateReq populateCrimHistoryUpdateReq(String reqFuncCd,CrimHistoryDto crimHistoryDto, Long idRecCheck, String txtDpsSid, String dpsMatchType) {
		List<CrimHistorySaveDto> crimHistorySaveList = new ArrayList<CrimHistorySaveDto>();
		CriminalHistoryUpdateReq crimHistoryUpdateReq = new CriminalHistoryUpdateReq();
		crimHistoryUpdateReq.setReqFuncCd(reqFuncCd);
		CrimHistorySaveDto crimHistorySaveDto = new CrimHistorySaveDto();
		crimHistorySaveDto.setIdRecCheck(idRecCheck);
		if(!TypeConvUtil.isNullOrEmpty(crimHistoryDto)) {
			crimHistorySaveDto.setIdCrimHist(crimHistoryDto.getIdCrimHist());
			crimHistorySaveDto.setDtLastUpdate(crimHistoryDto.getDtLastUpdate());
			crimHistorySaveDto.setDtResultsPosted(crimHistoryDto.getDtResultsPosted());
			crimHistorySaveDto.setCrimHistCmnts(crimHistoryDto.getCrimHistCmnts());
			crimHistorySaveDto.setCdCrimCheckStatus(crimHistoryDto.getCdCrimCheckStatus());
			crimHistorySaveDto.setCdCrimHistAction(crimHistoryDto.getCdCrimHistAction());
			crimHistorySaveDto.setNmCrimHistReturned(crimHistoryDto.getNmCrimHistReturned());
		}
		crimHistorySaveDto.setTxtDpsSid(txtDpsSid);
		crimHistorySaveDto.setdPSMatchType(dpsMatchType);
		crimHistorySaveList.add(crimHistorySaveDto);
		crimHistoryUpdateReq.setCriminalHistoryList(crimHistorySaveList);
		return crimHistoryUpdateReq;
	}

	public CrimHistoryRes getCrimHistList( Long idRecCheck) {
		CriminalHistoryReq criminalHistoryReq = new CriminalHistoryReq();
		criminalHistoryReq.setIdRecCheck(idRecCheck);
		CrimHistoryRes crimHistoryRes = criminalHistoryBusinessDelegate
				.getCrimHistList(criminalHistoryReq);
		return crimHistoryRes;
	}
	/* End of code changes for artf172946 */
	/**
	 * Method Name: validateSave. Method Description:This method is used to validate
	 * the details before Save/Change Search Type.
	 *
	 * @param checkDto
	 * @param personId
	 * @param user
	 * @param error
	 * @param actionReq
	 * @param nameList
	 * @return
	 */
	private List<String> validateSave(RecordsCheckDto checkDto, Long personId, boolean indRecCheckAccess, Errors error,
									  String actionReq, List<PersonInfoDto> nameList) {
		List<String> errorList = new ArrayList<String>();
		if (actionReq.equals(SAVE_ACTION_STRING) && ObjectUtils.isEmpty(checkDto.getRecCheckCheckType())) {
			error.rejectValue("recCheckCheckType", "empty check type",
					"Search Type - Field is required. Please enter a value.");
		} else if ((ObjectUtils.isEmpty(actionReq) && actionReq.equalsIgnoreCase(SAVE_ACTION_STRING)
				|| !ObjectUtils.isEmpty(checkDto.getRecCheckCheckType()))
				|| CodesConstant.CCHKTYPE_10.equals(checkDto.getRecCheckCheckType())) {

			// Call the validate method in the Validator class to check if there
			// is any
			// server side validation errors
			errorList = recordsCheckValidator.validate(checkDto, personId, indRecCheckAccess, error, nameList);

		}
		return errorList;
	}

	/**
	 * Method Name: saveRecordsCheckCompletion. Method Description:This method is
	 * used to save and Complete Records Check.
	 *
	 * @param model
	 * @param request
	 * @param checkDto
	 * @param redirectAttributes
	 * @param error
	 * @return String
	 */
	@RequestMapping(value = "/recordAction/SaveAndComplete")
	public String saveRecordsCheckCompletion(Model model, HttpServletRequest request,
											 @ModelAttribute("recordsCheckDto") RecordsCheckDto checkDto, RedirectAttributes redirectAttributes,
											 Errors error) {
		checkDto.setRecCheckCheckType(request.getParameter("checkType"));
		List<String> errorList = new ArrayList<String>();
		String path = EXTENSIBLE_STYLE_SHEET;
		URL styleSheetURL = null;
		String returnUrl = WebConstants.EMPTY_STRING;
		try {
			styleSheetURL = request.getServletContext().getResource(path);
		} catch (MalformedURLException e) {
			log.info(
					"Exception occured while getting the resource path of stylesheet in save method of RecordsCheckController");

		}
		UserProfile userProfile = (UserProfile) request.getSession().getAttribute(SessionConstants.USER_PROFILE);
		UserData commonData = getUserData(model, request);
		List<RecordsCheckDto> sessionDtoList = new ArrayList<RecordsCheckDto>();

		RecordsCheckListRes response = recordsCheckUtility.getRecordsCheckList(request, commonData);
		if (WebConstants.INTAKE_STAGE.equalsIgnoreCase(commonData.getCdStage())) {
			sessionDtoList = response.getListToBeDisplayedINT();
		} else {
			sessionDtoList = response.getListToBeDisplayed();
		}
		CommonDto commonDto = new CommonDto();
		commonDto.setIdUser(userProfile.getUserId());
		commonDto.setIdUserLogon(userProfile.getUserLogonId());
		commonDto.setNmUserFullName(userProfile.getUserFullName());
		commonDto.setIdPerson(commonData.getIdPerson());
		if (!ObjectUtils.isEmpty(checkDto.getDtLastUpdateStr())) {
			checkDto.setDtLastUpdate((Date) jsonUtil.jsonStringToObject(checkDto.getDtLastUpdateStr(), Date.class));
			;
		}
		// Call the business delegate method to save and complete the Records
		// Check
		// detail
		List<Object> listValues = recordBusinessDelegate.saveAndCompleteRecordsCheck(checkDto, sessionDtoList,
				commonDto, styleSheetURL);
		if (!ObjectUtils.isEmpty(listValues) && listValues.size() == 1
				&& ((ErrorDto)listValues.get(0)).getErrorCode() == WebConstants.TIME_MISMATCH_EXCEPTION) {
			errorList.add(cacheAdapter.getMessage(MessagesConstants.MSG_CMN_TMSTAMP_MISMATCH));
			if (CodesConstant.CCHKTYPE_10.equalsIgnoreCase(checkDto.getRecCheckCheckType())) {
				model.addAttribute("flagClearType", "true");
			}
			if (CodesConstant.CCHKTYPE_80.equalsIgnoreCase(checkDto.getRecCheckCheckType())) {
				model.addAttribute("phoneList", JsonUtil.jsonToMap(checkDto.getPhoneListStr()));
				model.addAttribute("emailPersonList", JsonUtil.jsonToMap(checkDto.getEmailListStr()));
			}
			model.addAttribute("errorCount", errorList.size());
			checkDto.setCheckTypeList((Map<String, String>) JsonUtil.jsonToMap(checkDto.getCheckTypeListStr()));
			checkDto.setCancelReasonList((Map<String, String>) JsonUtil.jsonToMap(checkDto.getCancelReasonListStr()));
			checkDto.setDeterminationList((Map<String, String>) JsonUtil.jsonToMap(checkDto.getDeterminationListStr()));
			model.addAttribute("recordsCheckDto", checkDto);
			model.addAttribute("nmPersonFull", commonData.getNmPerson());
			model.addAttribute("personId", commonData.getIdPerson());
			model.addAttribute("activeThirdLevelNav", "recordsCheck");
			model.addAttribute("activeSideNav", "person");
			model.addAttribute("cacheAdapter", cacheAdapter);
			model.addAttribute("errorList", errorList);
			updateUserData(commonData, model);
			String callingPageTab = commonData.getPrePage();
			if (WebConstants.STAFF_DETAIL.equals(callingPageTab)) {
				model.addAttribute("activeThirdLevelNav", "recordsCheck");
				model.addAttribute("activeSideNav", "staffSearch");
				returnUrl = "search/staffSearchThirdLevelNav/RecordsCheckDetail";
			} else if (WebConstants.SIDE_NAV_PERSON_SEARCH.equals(callingPageTab)) {
				model.addAttribute("activeThirdLevelNav", "recordsCheck");
				model.addAttribute("activeSideNav", "personSearch");
				returnUrl = "search/personThirdLevelNav/RecordsCheckDetail";
			} else {
				model.addAttribute("activeThirdLevelNav", "recordsCheck");
				model.addAttribute("activeSideNav", "person");
				returnUrl = "case/personThirdLevelNav/RecordsCheckDetail";
			}
		}else{
			updateUserData(commonData, model);
			HashMap<String, Object> forwardedValues = new HashMap<String, Object>();
			forwardedValues.put("txtUlIdRecCheck", listValues.get(1));
			request.setAttribute("forwardMap", forwardedValues);
			returnUrl = "forward:/case/person/record/recordAction?pageMode=" + listValues.get(0) + "&recordsCheckDetailIndex="
					+ listValues.get(1) + "";
		}
		return returnUrl;
	}

	/**
	 * Method Name: openUploadedDocument. Method Description:This method is used to
	 * open an Uploaded Document.
	 *
	 * @param model
	 * @param request
	 * @param response
	 * @param redirectAttributes
	 * @return String
	 */
	@RequestMapping(value = "/openDocument")
	public String openUploadedDocument(Model model, HttpServletRequest request, HttpServletResponse response,
									   RedirectAttributes redirectAttributes) {
		UserData userData = new UserData();
		String userDataJson = (String) request.getParameter("userDataJson");
		HashMap<String, Object> forwardedValues = new HashMap<String, Object>();
		if (StringUtils.isNotBlank(userDataJson)) {
			userData = (UserData) JsonUtil.jsonToDecodedObject(userDataJson, UserData.class);
		}
		int idRecordCheck = Integer.parseInt((String) request.getParameter("idRecCheck"));
		String pageMode = request.getParameter("pageMode");
		Integer idDocRepository = null;
		if (!ObjectUtils.isEmpty(request.getParameter("documentIndex"))) {
			idDocRepository = Integer.valueOf(request.getParameter("documentIndex"));
		}
		if (!ObjectUtils.isEmpty(idDocRepository)) {
			JSONObject jsonObject = recordCheckUtil.getDocumentJSON(WebConstants.ABCS_APP_CODE, idDocRepository);
			String nmDocString = null;
			String mimeTypeString = null;
			try {
				if (!ObjectUtils.isEmpty(jsonObject)) {

					nmDocString = jsonObject.getString("nmDoc");
					mimeTypeString = jsonObject.getString("mimeType");
				}
				if (!ObjectUtils.isEmpty(jsonObject) && !ObjectUtils.isEmpty(mimeTypeString)
						&& !(NULL_STRING.equals(nmDocString)) && StringHelper.isValid(nmDocString)) {
					renderFile(jsonObject, response);
				} else {
					forwardedValues.put("errorOpeningDocumentMessage",
							"Unable to open requested document. Please see administrator.");
				}
			} catch (Exception e) {
				forwardedValues.put("errorOpeningDocumentMessage",
						"Unable to open requested document. Please see administrator.");
			}
		}
		updateUserData(userData, model);
		request.setAttribute("forwardMap", forwardedValues);
		return "forward:/case/person/record/recordAction?pageMode=" + pageMode + "&recordsCheckDetailIndex="
				+ idRecordCheck + "";
	}

	/**
	 * Method Name: renderFile. Method Description:This method is used to render the
	 * document.
	 *
	 * @param jsonObject
	 * @param response
	 */
	private void renderFile(JSONObject jsonObject, HttpServletResponse response) {

		try {
			JSONObject mimeTypeObj = (JSONObject) jsonObject.get(WebConstants.MIME_TYPE);
			String fileExt = mimeTypeObj.getString(WebConstants.CODE);
			String contentType = mimeTypeObj.getString(WebConstants.DECODE);
			String docName = jsonObject.getString(WebConstants.NM_DOC);
			String fileName = docName + "." + fileExt;
			if (StringHelper.isValid(contentType)) {
				int contentLength = Base64.decodeBase64(jsonObject.getString(WebConstants.CONTENT)).length;
				response.reset();
				response.setContentType(contentType);
				// Jira PD-462 - PD 90499 : code fix for uploaded doc display
				response.setHeader("Content-disposition", "attachment;filename=\"" + fileName + "\"");
				response.setHeader("Content-Type", "application/force-download");
				response.setContentLength(contentLength);
				response.setCharacterEncoding("UTF-8");

				OutputStream outPutstream = response.getOutputStream();
				outPutstream.write(Base64.decodeBase64(jsonObject.getString(WebConstants.CONTENT)), 0, contentLength);
				outPutstream.flush();
				outPutstream.close();

			}

		} catch (IOException | JSONException e) {
		}

	}

	/**
	 * Method Name: deleteUploadedDocument. Method Description:This method is used
	 * to delete the uploaded document.
	 *
	 * @param model
	 * @param request
	 * @param redirectAttributes
	 * @return String
	 */
	@RequestMapping(value = "/deleteUploadedDocument")
	public String deleteUploadedDocument(Model model, HttpServletRequest request,
										 RedirectAttributes redirectAttributes) {
		UserData userData = new UserData();
		String userDataJson = (String) request.getParameter("userDataJson");
		if (StringUtils.isNotBlank(userDataJson)) {
			userData = (UserData) JsonUtil.jsonToDecodedObject(userDataJson, UserData.class);
		}
		Map<String, String> returnMap = new HashMap<>();
		int idRecordCheck = Integer.parseInt((String) request.getParameter("idRecCheck"));
		String pageMode = request.getParameter("pageMode");
		// Using the doc id build the URL to consume the service
		Integer idDocRepository = null;
		if (!ObjectUtils.isEmpty(request.getParameter("documentIndex"))) {
			idDocRepository = Integer.valueOf(request.getParameter("documentIndex"));
		}
		// Call the business delegate method to delete the uploaded document
		returnMap = recordBusinessDelegate.deleteUploadedDocument(pageMode, idDocRepository, idRecordCheck);
		if (returnMap.containsKey("error")) {
			model.addAttribute("error", returnMap.get("error"));
		}
		if (returnMap.containsKey("errorMessageFromDeleteDocument")) {
			model.addAttribute("errorMessageFromDeleteDocument", returnMap.get("errorMessageFromDeleteDocument"));
		}
		String returnURL = returnMap.get("returnUrl");
		updateUserData(userData, model);
		return returnURL;
	}

	/**
	 * Method Name: setCompletedEmailFlag. Method Description:This method is used to
	 * the save the completed Email Flag.
	 *
	 * @param model
	 * @param request
	 * @param checkDto
	 * @param redirectAttributes
	 * @param error
	 * @return String
	 */
	//artf233497: Removed param and changed endpoint url as this is causing infinite loop beacuse of another api with same url.
	@RequestMapping(value = "/recordAction/SetCompletedEmailFlag")
	public String setCompletedEmailFlag(Model model, HttpServletRequest request,
										@ModelAttribute("recordsCheckDto") RecordsCheckDto checkDto, RedirectAttributes redirectAttributes,
										Errors error) {
		checkDto.setRecCheckCheckType(request.getParameter("checkType"));
		UserData userData = getUserData(model, request);
		UserProfile userProfile = (UserProfile) request.getSession().getAttribute(SessionConstants.USER_PROFILE);
		Map<String, String> returnMap = new HashMap<>();
		HashMap<String, Object> forwardedValues = new HashMap<String, Object>();
		// Call the business delegate method to set the completed Flag for the
		// particular Records Check
		CommonDto commonDto = new CommonDto();
		commonDto.setIdPerson(userData.getIdPerson());
		commonDto.setIdUserLogon(userProfile.getUserLogonId());
		commonDto.setIdUser(userProfile.getUserId());
		commonDto.setNmUserFullName(userData.getNmPerson());
		if (!ObjectUtils.isEmpty(checkDto.getDtLastUpdateStr())) {
			checkDto.setDtLastUpdate((Date) jsonUtil.jsonStringToObject(checkDto.getDtLastUpdateStr(), Date.class));
		}
		String hostName = ServerInfoUtil.getHostName();
		if (!ObjectUtils.isEmpty(hostName)) {
			hostName = hostName.replace(" ", "");
		}
		log.info("PD 92219 - Added loggers: before calling setCompletedEmailFlag");
		returnMap = recordBusinessDelegate.setCompletedEmailFlag(checkDto, commonDto, hostName);
		if (returnMap.containsKey("informationMessage")) {
			forwardedValues.put("informationMessage", returnMap.get("informationMessage"));
		}
		if (returnMap.containsKey("setIndEmailErrorMessage")) {
			forwardedValues.put("setIndEmailErrorMessage", returnMap.get("setIndEmailErrorMessage"));
		} else if (returnMap.containsKey("error")) {
			model.addAttribute("error", returnMap.get("error"));
		}
		request.setAttribute("forwardMap", forwardedValues);
		updateUserData(userData, model);
		return returnMap.get("returnUrl");
	}

	/**
	 * Method Name: saveRecordsCheckDeatilFormReport Method Description:This method
	 * is used to create a Central Registry or FPS History check record in db.
	 *
	 * @param model
	 * @param request
	 * @param attributes
	 * @return
	 */
	@RequestMapping("saveRecordsCheckFormsReports")
	public String saveRecordsCheckDeatilFormReport(Model model, HttpServletRequest request,
												   RedirectAttributes attributes) {
		UserData userData = getUserData(model, request);
		SimpleDateFormat format = new SimpleDateFormat(WebConstants.SLASH_DATE_MASK);
		// records check type
		String cdRecCheckType = request.getParameter("recCheckType");
		// completed date
		String dtCompleted = request.getParameter("dtCompleted");
		// requested date
		String dtRequest = request.getParameter("dtRequest");
		HashMap<String, Object> forwardedValues = new HashMap<String, Object>();

		// Setting the values in dto before redirecting the values to the save
		// method
		RecordsCheckDto checkDto = new RecordsCheckDto();
		checkDto.setRecCheckCheckType(cdRecCheckType);
		try {
			checkDto.setDtRecCheckRequest(format.parse(dtRequest));
			checkDto.setDtRecCheckCompleted(format.parse(dtCompleted));
		} catch (ParseException e) {

		}
		checkDto.setPageModeStr(PageMode.NEW);
		checkDto.setButtonClicked("save");
		updateUserData(userData, model);
		forwardedValues.put("actionPerformed", "save");
		forwardedValues.put("recordsCheckDto", checkDto);
		String returnURL = "forward:/case/person/record/recordAction/SaveRecordCheckDetail?checkType="
				+ cdRecCheckType;
		request.setAttribute("forwardMap", forwardedValues);
		return returnURL;
	}

	/**
	 *
	 * Method Name: setFormDetail Method Description:this method is for setting up
	 * the data required for forms
	 *
	 * @param model
	 * @param profile
	 * @param userdata
	 */
	private void setFormDetail(Model model, UserProfile profile, Long idRecCheck) {
		Map<String, String> formTagDtoMap = new HashMap<>();

		// set values for recCheckLttrnotDto to launch DFPS Letterhead
		// Notification
		FormTagDto recCheckLttrnotDto = new FormTagDto();
		recCheckLttrnotDto.setDocType(LTTRNOT);
		recCheckLttrnotDto.setDocExists(WebConstants.STRING_FALSE);
		recCheckLttrnotDto.setCheckStage(WebConstants.ZERO);
		recCheckLttrnotDto.setPromptSavePage(null);
		recCheckLttrnotDto.setPostInSameWindow(WebConstants.STRING_FALSE);
		recCheckLttrnotDto.setDeleteDocument(WebConstants.STRING_FALSE);
		recCheckLttrnotDto.setRenderFormat(WebConstants.HTML_WITH_SHELL);
		recCheckLttrnotDto.setModeOfPage(PageMode.MODIFY);
		recCheckLttrnotDto.setLevel1Tab(WebConstants.SEARCH_PERSONSEARCH);
		recCheckLttrnotDto.setLevel2Tab(WebConstants.PERSON_PERSONSEARCH);
		recCheckLttrnotDto.setLevel3Tab(WebConstants.RECORDS_CHECK_RECORDSCHECK);
		recCheckLttrnotDto.setCheckForNewMode(WebConstants.STRING_TRUE);
		recCheckLttrnotDto.setOnClick(null);
		recCheckLttrnotDto.setWindowName(null);
		recCheckLttrnotDto.setPromptNoDocument(WebConstants.STRING_FALSE);
		recCheckLttrnotDto.setbResend(WebConstants.STRING_FALSE);
		recCheckLttrnotDto.setsTimestamp(String.valueOf(new Date()));
		recCheckLttrnotDto.setUserId(String.valueOf(profile.getUserId()));
		recCheckLttrnotDto.setpRecCheck(String.valueOf(idRecCheck));
		recCheckLttrnotDto.setsRecordsCheckNotif(sRecordsCheckNotif);
		recCheckLttrnotDto.setpCdNotifStat(WebConstants.STATUS_NEW);
		recCheckLttrnotDto.setpRecordsCheckNotif(ZERO);
		formTagDtoMap.put(recCheckLttrnotDto.getDocType(), JsonUtil.objectToEcodedJson(recCheckLttrnotDto));

		// set values for recCheckNlcmnotDto to launch Non-Licensing Match
		// Summary Notification
		FormTagDto recCheckNlcmnotDto = new FormTagDto();
		recCheckNlcmnotDto.setDocType(NLCMNOT);
		recCheckNlcmnotDto.setDocExists(WebConstants.STRING_FALSE);
		recCheckNlcmnotDto.setCheckStage(WebConstants.ZERO);
		recCheckNlcmnotDto.setPromptSavePage(null);
		recCheckNlcmnotDto.setPostInSameWindow(WebConstants.STRING_FALSE);
		recCheckNlcmnotDto.setDeleteDocument(WebConstants.STRING_FALSE);
		recCheckNlcmnotDto.setRenderFormat(WebConstants.HTML_WITH_SHELL);
		recCheckNlcmnotDto.setModeOfPage(PageMode.MODIFY);
		recCheckNlcmnotDto.setLevel1Tab(WebConstants.SEARCH_PERSONSEARCH);
		recCheckNlcmnotDto.setLevel2Tab(WebConstants.PERSON_PERSONSEARCH);
		recCheckNlcmnotDto.setLevel3Tab(WebConstants.RECORDS_CHECK_RECORDSCHECK);
		recCheckNlcmnotDto.setCheckForNewMode(WebConstants.STRING_TRUE);
		recCheckNlcmnotDto.setOnClick(null);
		recCheckNlcmnotDto.setWindowName(null);
		recCheckNlcmnotDto.setPromptNoDocument(WebConstants.STRING_FALSE);
		recCheckNlcmnotDto.setbResend(WebConstants.STRING_FALSE);
		recCheckNlcmnotDto.setsTimestamp(String.valueOf(new Date()));
		recCheckNlcmnotDto.setUserId(String.valueOf(profile.getUserId()));
		recCheckNlcmnotDto.setpRecCheck(String.valueOf(idRecCheck));
		recCheckNlcmnotDto.setsRecordsCheckNotif(sRecordsCheckNotif);
		recCheckNlcmnotDto.setpCdNotifStat(WebConstants.STATUS_NEW);
		recCheckNlcmnotDto.setpRecordsCheckNotif(ZERO);
		formTagDtoMap.put(recCheckNlcmnotDto.getDocType(), JsonUtil.objectToEcodedJson(recCheckNlcmnotDto));

		// set values for recCheckActrnotDto to launch Action Required
		// Notification
		FormTagDto recCheckActrnotDto = new FormTagDto();
		recCheckActrnotDto.setDocType(ACTRNOT);
		recCheckActrnotDto.setDocExists(WebConstants.STRING_FALSE);
		recCheckActrnotDto.setCheckStage(WebConstants.ZERO);
		recCheckActrnotDto.setPromptSavePage(null);
		recCheckActrnotDto.setPostInSameWindow(WebConstants.STRING_FALSE);
		recCheckActrnotDto.setDeleteDocument(WebConstants.STRING_FALSE);
		recCheckActrnotDto.setRenderFormat(WebConstants.HTML_WITH_SHELL);
		recCheckActrnotDto.setModeOfPage(PageMode.MODIFY);
		recCheckActrnotDto.setLevel1Tab(WebConstants.SEARCH_PERSONSEARCH);
		recCheckActrnotDto.setLevel2Tab(WebConstants.PERSON_PERSONSEARCH);
		recCheckActrnotDto.setLevel3Tab(WebConstants.RECORDS_CHECK_RECORDSCHECK);
		recCheckActrnotDto.setCheckForNewMode(WebConstants.STRING_TRUE);
		recCheckActrnotDto.setOnClick(null);
		recCheckActrnotDto.setWindowName(null);
		recCheckActrnotDto.setPromptNoDocument(WebConstants.STRING_FALSE);
		recCheckActrnotDto.setbResend(WebConstants.STRING_FALSE);
		recCheckActrnotDto.setsTimestamp(String.valueOf(new Date()));
		recCheckActrnotDto.setUserId(String.valueOf(profile.getUserId()));
		recCheckActrnotDto.setpRecCheck(String.valueOf(idRecCheck));
		recCheckActrnotDto.setsRecordsCheckNotif(sRecordsCheckNotif);
		recCheckActrnotDto.setpCdNotifStat(WebConstants.STATUS_NEW);
		recCheckActrnotDto.setpRecordsCheckNotif(ZERO);
		formTagDtoMap.put(recCheckActrnotDto.getDocType(), JsonUtil.objectToEcodedJson(recCheckActrnotDto));

		// set values for recCheckBarrnotDto to launch Barred Notification
		FormTagDto recCheckBarrnotDto = new FormTagDto();
		recCheckBarrnotDto.setDocType(BARRNOT);
		recCheckBarrnotDto.setDocExists(WebConstants.STRING_FALSE);
		recCheckBarrnotDto.setCheckStage(WebConstants.ZERO);
		recCheckBarrnotDto.setPromptSavePage(null);
		recCheckBarrnotDto.setPostInSameWindow(WebConstants.STRING_FALSE);
		recCheckBarrnotDto.setDeleteDocument(WebConstants.STRING_FALSE);
		recCheckBarrnotDto.setRenderFormat(WebConstants.HTML_WITH_SHELL);
		recCheckBarrnotDto.setModeOfPage(PageMode.MODIFY);
		recCheckBarrnotDto.setLevel1Tab(WebConstants.SEARCH_PERSONSEARCH);
		recCheckBarrnotDto.setLevel2Tab(WebConstants.PERSON_PERSONSEARCH);
		recCheckBarrnotDto.setLevel3Tab(WebConstants.RECORDS_CHECK_RECORDSCHECK);
		recCheckBarrnotDto.setCheckForNewMode(WebConstants.STRING_TRUE);
		recCheckBarrnotDto.setOnClick(null);
		recCheckBarrnotDto.setWindowName(null);
		recCheckBarrnotDto.setPromptNoDocument(WebConstants.STRING_FALSE);
		recCheckBarrnotDto.setbResend(WebConstants.STRING_FALSE);
		recCheckBarrnotDto.setsTimestamp(String.valueOf(new Date()));
		recCheckBarrnotDto.setUserId(String.valueOf(profile.getUserId()));
		recCheckBarrnotDto.setpRecCheck(String.valueOf(idRecCheck));
		recCheckBarrnotDto.setsRecordsCheckNotif(sRecordsCheckNotif);
		recCheckBarrnotDto.setpCdNotifStat(WebConstants.STATUS_NEW);
		recCheckBarrnotDto.setpRecordsCheckNotif(ZERO);
		formTagDtoMap.put(recCheckBarrnotDto.getDocType(), JsonUtil.objectToEcodedJson(recCheckBarrnotDto));

		// set values for recCheckCregnotDto to launch Child Abuse Neglect
		// Central Registry Check Notification
		FormTagDto recCheckCregnotDto = new FormTagDto();
		recCheckCregnotDto.setDocType(CREGNOT);
		recCheckCregnotDto.setDocExists(WebConstants.STRING_FALSE);
		recCheckCregnotDto.setCheckStage(WebConstants.ZERO);
		recCheckCregnotDto.setPromptSavePage(null);
		recCheckCregnotDto.setPostInSameWindow(WebConstants.STRING_FALSE);
		recCheckCregnotDto.setDeleteDocument(WebConstants.STRING_FALSE);
		recCheckCregnotDto.setRenderFormat(WebConstants.HTML_WITH_SHELL);
		recCheckCregnotDto.setModeOfPage(PageMode.MODIFY);
		recCheckCregnotDto.setLevel1Tab(WebConstants.SEARCH_PERSONSEARCH);
		recCheckCregnotDto.setLevel2Tab(WebConstants.PERSON_PERSONSEARCH);
		recCheckCregnotDto.setLevel3Tab(WebConstants.RECORDS_CHECK_RECORDSCHECK);
		recCheckCregnotDto.setCheckForNewMode(WebConstants.STRING_TRUE);
		recCheckCregnotDto.setOnClick(null);
		recCheckCregnotDto.setWindowName(null);
		recCheckCregnotDto.setPromptNoDocument(WebConstants.STRING_FALSE);
		recCheckCregnotDto.setbResend(WebConstants.STRING_FALSE);
		recCheckCregnotDto.setsTimestamp(String.valueOf(new Date()));
		recCheckCregnotDto.setUserId(String.valueOf(profile.getUserId()));
		recCheckCregnotDto.setpRecCheck(String.valueOf(idRecCheck));
		recCheckCregnotDto.setsRecordsCheckNotif(sRecordsCheckNotif);
		recCheckCregnotDto.setpCdNotifStat(WebConstants.STATUS_NEW);
		recCheckCregnotDto.setpRecordsCheckNotif(ZERO);
		formTagDtoMap.put(recCheckCregnotDto.getDocType(), JsonUtil.objectToEcodedJson(recCheckCregnotDto));

		// set values for recCheckCregnotDto to launch PCS Risk Evaluation
		// Decision Notification
		FormTagDto recCheckPcsrnotDto = new FormTagDto();
		recCheckPcsrnotDto.setDocType(PCSRNOT);
		recCheckPcsrnotDto.setDocExists(WebConstants.STRING_FALSE);
		recCheckPcsrnotDto.setCheckStage(WebConstants.ZERO);
		recCheckPcsrnotDto.setPromptSavePage(null);
		recCheckPcsrnotDto.setPostInSameWindow(WebConstants.STRING_FALSE);
		recCheckPcsrnotDto.setDeleteDocument(WebConstants.STRING_FALSE);
		recCheckPcsrnotDto.setRenderFormat(WebConstants.HTML_WITH_SHELL);
		recCheckPcsrnotDto.setModeOfPage(PageMode.MODIFY);
		recCheckPcsrnotDto.setLevel1Tab(WebConstants.SEARCH_PERSONSEARCH);
		recCheckPcsrnotDto.setLevel2Tab(WebConstants.PERSON_PERSONSEARCH);
		recCheckPcsrnotDto.setLevel3Tab(WebConstants.RECORDS_CHECK_RECORDSCHECK);
		recCheckPcsrnotDto.setCheckForNewMode(WebConstants.STRING_TRUE);
		recCheckPcsrnotDto.setOnClick(null);
		recCheckPcsrnotDto.setWindowName(null);
		recCheckPcsrnotDto.setPromptNoDocument(WebConstants.STRING_FALSE);
		recCheckPcsrnotDto.setbResend(WebConstants.STRING_FALSE);
		recCheckPcsrnotDto.setsTimestamp(String.valueOf(new Date()));
		recCheckPcsrnotDto.setUserId(String.valueOf(profile.getUserId()));
		recCheckPcsrnotDto.setpRecCheck(String.valueOf(idRecCheck));
		recCheckPcsrnotDto.setsRecordsCheckNotif(sRecordsCheckNotif);
		recCheckPcsrnotDto.setpCdNotifStat(WebConstants.STATUS_NEW);
		recCheckPcsrnotDto.setpRecordsCheckNotif(ZERO);
		formTagDtoMap.put(recCheckPcsrnotDto.getDocType(), JsonUtil.objectToEcodedJson(recCheckPcsrnotDto));

		// set values for recCheckFbihvelDto to launch Action Required
		// Notification
		FormTagDto recCheckFbihvelDto = new FormTagDto();
		recCheckFbihvelDto.setDocType(FBIHVEL);
		recCheckFbihvelDto.setDocExists(WebConstants.STRING_FALSE);
		recCheckFbihvelDto.setCheckStage(WebConstants.ZERO);
		recCheckFbihvelDto.setPromptSavePage(null);
		recCheckFbihvelDto.setPostInSameWindow(WebConstants.STRING_FALSE);
		recCheckFbihvelDto.setDeleteDocument(WebConstants.STRING_FALSE);
		recCheckFbihvelDto.setRenderFormat(WebConstants.HTML_WITH_SHELL);
		recCheckFbihvelDto.setModeOfPage(PageMode.MODIFY);
		recCheckFbihvelDto.setLevel1Tab(WebConstants.SEARCH_PERSONSEARCH);
		recCheckFbihvelDto.setLevel2Tab(WebConstants.PERSON_PERSONSEARCH);
		recCheckFbihvelDto.setLevel3Tab(WebConstants.RECORDS_CHECK_RECORDSCHECK);
		recCheckFbihvelDto.setCheckForNewMode(WebConstants.STRING_TRUE);
		recCheckFbihvelDto.setOnClick(null);
		recCheckFbihvelDto.setWindowName(null);
		recCheckFbihvelDto.setPromptNoDocument(WebConstants.STRING_FALSE);
		recCheckFbihvelDto.setbResend(WebConstants.STRING_FALSE);
		recCheckFbihvelDto.setsTimestamp(String.valueOf(new Date()));
		recCheckFbihvelDto.setUserId(String.valueOf(profile.getUserId()));
		recCheckFbihvelDto.setpRecCheck(String.valueOf(idRecCheck));
		recCheckFbihvelDto.setsRecordsCheckNotif(sRecordsCheckNotif);
		recCheckFbihvelDto.setpCdNotifStat(WebConstants.STATUS_NEW);
		recCheckFbihvelDto.setpRecordsCheckNotif(ZERO);
		formTagDtoMap.put(recCheckFbihvelDto.getDocType(), JsonUtil.objectToEcodedJson(recCheckFbihvelDto));


		// set values for recCheckFbivielDto to launch Action Required
		// Notification
		FormTagDto recCheckFbivielDto = new FormTagDto();
		recCheckFbivielDto.setDocType(FBIVIEL);
		recCheckFbivielDto.setDocExists(WebConstants.STRING_FALSE);
		recCheckFbivielDto.setCheckStage(WebConstants.ZERO);
		recCheckFbivielDto.setPromptSavePage(null);
		recCheckFbivielDto.setPostInSameWindow(WebConstants.STRING_FALSE);
		recCheckFbivielDto.setDeleteDocument(WebConstants.STRING_FALSE);
		recCheckFbivielDto.setRenderFormat(WebConstants.HTML_WITH_SHELL);
		recCheckFbivielDto.setModeOfPage(PageMode.MODIFY);
		recCheckFbivielDto.setLevel1Tab(WebConstants.SEARCH_PERSONSEARCH);
		recCheckFbivielDto.setLevel2Tab(WebConstants.PERSON_PERSONSEARCH);
		recCheckFbivielDto.setLevel3Tab(WebConstants.RECORDS_CHECK_RECORDSCHECK);
		recCheckFbivielDto.setCheckForNewMode(WebConstants.STRING_TRUE);
		recCheckFbivielDto.setOnClick(null);
		recCheckFbivielDto.setWindowName(null);
		recCheckFbivielDto.setPromptNoDocument(WebConstants.STRING_FALSE);
		recCheckFbivielDto.setbResend(WebConstants.STRING_FALSE);
		recCheckFbivielDto.setsTimestamp(String.valueOf(new Date()));
		recCheckFbivielDto.setUserId(String.valueOf(profile.getUserId()));
		recCheckFbivielDto.setpRecCheck(String.valueOf(idRecCheck));
		recCheckFbivielDto.setsRecordsCheckNotif(sRecordsCheckNotif);
		recCheckFbivielDto.setpCdNotifStat(WebConstants.STATUS_NEW);
		recCheckFbivielDto.setpRecordsCheckNotif(ZERO);
		formTagDtoMap.put(recCheckFbivielDto.getDocType(), JsonUtil.objectToEcodedJson(recCheckFbivielDto));

		// set values for recCheckFbienotDto to launch Action Required
		// Notification
		FormTagDto recCheckFbienotDto = new FormTagDto();
		recCheckFbienotDto.setDocType(FBIENOT);
		recCheckFbienotDto.setDocExists(WebConstants.STRING_FALSE);
		recCheckFbienotDto.setCheckStage(WebConstants.ZERO);
		recCheckFbienotDto.setPromptSavePage(null);
		recCheckFbienotDto.setPostInSameWindow(WebConstants.STRING_FALSE);
		recCheckFbienotDto.setDeleteDocument(WebConstants.STRING_FALSE);
		recCheckFbienotDto.setRenderFormat(WebConstants.HTML_WITH_SHELL);
		recCheckFbienotDto.setModeOfPage(PageMode.MODIFY);
		recCheckFbienotDto.setLevel1Tab(WebConstants.SEARCH_PERSONSEARCH);
		recCheckFbienotDto.setLevel2Tab(WebConstants.PERSON_PERSONSEARCH);
		recCheckFbienotDto.setLevel3Tab(WebConstants.RECORDS_CHECK_RECORDSCHECK);
		recCheckFbienotDto.setCheckForNewMode(WebConstants.STRING_TRUE);
		recCheckFbienotDto.setOnClick(null);
		recCheckFbienotDto.setWindowName(null);
		recCheckFbienotDto.setPromptNoDocument(WebConstants.STRING_FALSE);
		recCheckFbienotDto.setbResend(WebConstants.STRING_FALSE);
		recCheckFbienotDto.setsTimestamp(String.valueOf(new Date()));
		recCheckFbienotDto.setUserId(String.valueOf(profile.getUserId()));
		recCheckFbienotDto.setpRecCheck(String.valueOf(idRecCheck));
		recCheckFbienotDto.setsRecordsCheckNotif(sRecordsCheckNotif);
		recCheckFbienotDto.setpCdNotifStat(WebConstants.STATUS_NEW);
		recCheckFbienotDto.setpRecordsCheckNotif(ZERO);
		formTagDtoMap.put(recCheckFbienotDto.getDocType(), JsonUtil.objectToEcodedJson(recCheckFbienotDto));

		// set values for recCheckFbiinotDto to launch Action Required
		// Notification
		FormTagDto recCheckFbiinotDto = new FormTagDto();
		recCheckFbiinotDto.setDocType(FBIINOT);
		recCheckFbiinotDto.setDocExists(WebConstants.STRING_FALSE);
		recCheckFbiinotDto.setCheckStage(WebConstants.ZERO);
		recCheckFbiinotDto.setPromptSavePage(null);
		recCheckFbiinotDto.setPostInSameWindow(WebConstants.STRING_FALSE);
		recCheckFbiinotDto.setDeleteDocument(WebConstants.STRING_FALSE);
		recCheckFbiinotDto.setRenderFormat(WebConstants.HTML_WITH_SHELL);
		recCheckFbiinotDto.setModeOfPage(PageMode.MODIFY);
		recCheckFbiinotDto.setLevel1Tab(WebConstants.SEARCH_PERSONSEARCH);
		recCheckFbiinotDto.setLevel2Tab(WebConstants.PERSON_PERSONSEARCH);
		recCheckFbiinotDto.setLevel3Tab(WebConstants.RECORDS_CHECK_RECORDSCHECK);
		recCheckFbiinotDto.setCheckForNewMode(WebConstants.STRING_TRUE);
		recCheckFbiinotDto.setOnClick(null);
		recCheckFbiinotDto.setWindowName(null);
		recCheckFbiinotDto.setPromptNoDocument(WebConstants.STRING_FALSE);
		recCheckFbiinotDto.setbResend(WebConstants.STRING_FALSE);
		recCheckFbiinotDto.setsTimestamp(String.valueOf(new Date()));
		recCheckFbiinotDto.setUserId(String.valueOf(profile.getUserId()));
		recCheckFbiinotDto.setpRecCheck(String.valueOf(idRecCheck));
		recCheckFbiinotDto.setsRecordsCheckNotif(sRecordsCheckNotif);
		recCheckFbiinotDto.setpCdNotifStat(WebConstants.STATUS_NEW);
		recCheckFbiinotDto.setpRecordsCheckNotif(ZERO);
		formTagDtoMap.put(recCheckFbiinotDto.getDocType(), JsonUtil.objectToEcodedJson(recCheckFbiinotDto));


		// set values for recCheckPCSIneligibletDto to launch PCS Ineligible
		// Notification
		FormTagDto recCheckPCSIneligibletDto = new FormTagDto();
		recCheckPCSIneligibletDto.setDocType(PCSINEN);
		recCheckPCSIneligibletDto.setDocExists(WebConstants.STRING_FALSE);
		recCheckPCSIneligibletDto.setCheckStage(WebConstants.ZERO);
		recCheckPCSIneligibletDto.setPromptSavePage(null);
		recCheckPCSIneligibletDto.setPostInSameWindow(WebConstants.STRING_FALSE);
		recCheckPCSIneligibletDto.setDeleteDocument(WebConstants.STRING_FALSE);
		recCheckPCSIneligibletDto.setRenderFormat(WebConstants.HTML_WITH_SHELL);
		recCheckPCSIneligibletDto.setModeOfPage(PageMode.MODIFY);
		recCheckPCSIneligibletDto.setLevel1Tab(WebConstants.SEARCH_PERSONSEARCH);
		recCheckPCSIneligibletDto.setLevel2Tab(WebConstants.PERSON_PERSONSEARCH);
		recCheckPCSIneligibletDto.setLevel3Tab(WebConstants.RECORDS_CHECK_RECORDSCHECK);
		recCheckPCSIneligibletDto.setCheckForNewMode(WebConstants.STRING_TRUE);
		recCheckPCSIneligibletDto.setOnClick(null);
		recCheckPCSIneligibletDto.setWindowName(null);
		recCheckPCSIneligibletDto.setPromptNoDocument(WebConstants.STRING_FALSE);
		recCheckPCSIneligibletDto.setbResend(WebConstants.STRING_FALSE);
		recCheckPCSIneligibletDto.setsTimestamp(String.valueOf(new Date()));
		recCheckPCSIneligibletDto.setUserId(String.valueOf(profile.getUserId()));
		recCheckPCSIneligibletDto.setpRecCheck(String.valueOf(idRecCheck));
		recCheckPCSIneligibletDto.setsRecordsCheckNotif(sRecordsCheckNotif);
		recCheckPCSIneligibletDto.setpCdNotifStat(WebConstants.STATUS_NEW);
		recCheckPCSIneligibletDto.setpRecordsCheckNotif(ZERO);
		formTagDtoMap.put(recCheckPCSIneligibletDto.getDocType(), JsonUtil.objectToEcodedJson(recCheckPCSIneligibletDto));



		model.addAttribute(FORM_TAG_DTO_MAP, formTagDtoMap);

	}

}