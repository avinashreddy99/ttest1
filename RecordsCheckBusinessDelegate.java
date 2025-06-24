package us.tx.state.dfps.person.businessdelegate;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.Writer;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.*;
import java.util.stream.IntStream;

import javax.mail.MessagingException;
import javax.xml.XMLConstants;
import javax.xml.namespace.QName;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.apache.log4j.Logger;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.type.TypeReference;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ObjectUtils;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import us.tx.state.dfps.businessdelegate.BaseBusinessDelegate;
import us.tx.state.dfps.businessdelegate.StringHelper;
import us.tx.state.dfps.businessdelegate.util.*;
import us.tx.state.dfps.common.dto.CommonDto;
import us.tx.state.dfps.common.exception.WebException;
import us.tx.state.dfps.common.web.CodesConstant;
import us.tx.state.dfps.common.web.MessagesConstants;
import us.tx.state.dfps.common.web.WebConstants;
import us.tx.state.dfps.common.web.bean.EmailDetailBean;
import us.tx.state.dfps.person.util.RecordsCheckUtil;
import us.tx.state.dfps.service.common.request.AddressDtlReq;
import us.tx.state.dfps.service.common.request.CommonHelperReq;
import us.tx.state.dfps.service.common.request.CriminalHistoryReq;
import us.tx.state.dfps.service.common.request.EmailDetailReq;
import us.tx.state.dfps.service.common.request.EmailNotificationsReq;
import us.tx.state.dfps.service.common.request.EmailReq;
import us.tx.state.dfps.service.common.request.PersonPhoneReq;
import us.tx.state.dfps.service.common.request.PhoneReq;
import us.tx.state.dfps.service.common.request.RecordsCheckDetailReq;
import us.tx.state.dfps.service.common.request.RecordsCheckReq;
import us.tx.state.dfps.service.common.request.RecordsCheckStatusReq;
import us.tx.state.dfps.service.common.response.AddressDtlRes;
import us.tx.state.dfps.service.common.response.CommonHelperRes;
import us.tx.state.dfps.service.common.response.CriminalHistoryRes;
import us.tx.state.dfps.service.common.response.EacdResponse;
import us.tx.state.dfps.service.common.response.EmailDetailRes;
import us.tx.state.dfps.service.common.response.EmailNotificationsRes;
import us.tx.state.dfps.service.common.response.EmailRes;
import us.tx.state.dfps.service.common.response.PhoneRes;
import us.tx.state.dfps.service.common.response.RecordsCheckListRes;
import us.tx.state.dfps.service.common.response.RecordsCheckRes;
import us.tx.state.dfps.service.email.EmailNotificationDto;
import us.tx.state.dfps.service.person.dto.CriminalHistoryValueBean;
import us.tx.state.dfps.service.person.dto.DocumentPdbValueDto;
import us.tx.state.dfps.service.person.dto.FilteredNameDto;
import us.tx.state.dfps.service.person.dto.PersonInfoDto;
import us.tx.state.dfps.service.person.dto.PersonNameCheckDto;
import us.tx.state.dfps.service.person.dto.PersonPhoneRetDto;
import us.tx.state.dfps.service.person.dto.RecordsCheckDetailDto;
import us.tx.state.dfps.service.person.dto.RecordsCheckDeterminationDto;
import us.tx.state.dfps.service.person.dto.RecordsCheckDto;
import us.tx.state.dfps.service.person.dto.RecordsCheckNotificationDto;
import us.tx.state.dfps.webservice.DPSCrimHistNameCheckService;
import us.tx.state.dfps.webservice.InvalidNameException_Exception;
import us.tx.state.dfps.webservice.NameCheckCriminalResponse;
import us.tx.state.dfps.webservice.NameCheckRequest;
import us.tx.state.dfps.webservice.NameCheckRequestException_Exception;
import us.tx.state.dfps.webservice.NameCheckResponse;
import us.tx.state.dfps.webservice.NameRequest;

/**
 * businessdelegate- IMPACT PHASE 2 MODERNIZATION Class Description:This class
 * is used to handle the Service Requests for Records Check screen. Jun 9, 2017-
 * 3:02:18 PM © 2017 Texas Department of Family and Protective Services
 *
 *
 * ******Change History**********
 *
 *  05/22/2018   kanakas artf51666  : Update FBI Exigent Check Display Functionality 10/03/2018 - gelenp artf75084 - Modified definition of int
 * 									  'idRecCheckRequestor' in sendname() by replacing getIdPerson() with getIdUser().
 *  12/29/2020   nairl   artf171305 : DEV BR 10.01 Records Check Detail Page
 *  01/25/2021 	 nairl   artf172936 : DEV BR 15.01 Indicator (IMPACT) for Person Who Has Access to CHRI P2
 *  01/26/2021   nairl   artf172912 : DEV BR 16.01 Display the Criminal History Decision Section (Records Check Detail) for Staff Records Only Impact P2
 *  01/28/2021   nairl   artf172948 : DEV BR 22.01 Add a “Completed” Checkbox to FBI Checks (Staff records only) P2
 *  01/28/2021   nairl   artf172943 : DEV BR 17.01 Updated Determination Drop-down menu values P2
 *  02/12/2021   nairl   artf172946 : DEV BR 21.01 Support Manual Entry of Results from DPS’ SecureSite into IMPACT P2
 *  02/12/2021   nairl   artf172945 : DEV BR 19.01 Update Cancel Reason Drop-down Menu (Records Check Detail) P2
 *  04/06/2021   nairl   artf179186 : The Records Check Detail page for FBI Fingerprint Checks for Staff records are not displaying the Criminal History Decision Section when results are pending in Legacy IMPACT and IMPACT 2.0
 */
@Component
public class RecordsCheckBusinessDelegate extends BaseBusinessDelegate {


	@Autowired

	private EmailUtility emailUtility;

	@Autowired
	private RecordsCheckUtil recordCheckUtil;

	@Autowired
	CriminalHistoryBusinessDelegate criminalHistoryBusinessDelegate;


	private static final Logger log = Logger.getLogger(RecordsCheckBusinessDelegate.class);

	public static final String NEW = "1";

	public static final String NEW_USING = "2";

	public static final String INQUIRE = "3";

	public static final String MODIFY = "4";

	private static final String PRIMARY_WORKER_STRING = "PR";

	private static final String SECONDARY_WORKER_STRING = "SE";

	private static final String HTTP_ERROR = "HTTP_ERROR";

	private static final String SUCCESS = "SUCCESS";

	private static final String REVIEW_LATER = "reviewLater";

	private static final String REVIEW_NOW = "reviewNow";

	private static final String PHN_CODE = "PHN";

	private static final String EML_CODE = "EML";

	private static final String CANCELLED = "CANCELLED:";

	private static final String COMPLETED = "COMPLETED:";

	private static final String VALIDATE_ACTION_STRING = "validate";

	private static final String ERROR_ELIGIBLE_EMAIL = "Error sending ELIGIBLE email.  Verify related ABCs request and email addresses exist then try again.";

	private static final String ERROR_INELIGIBLE_EMAIL = "Error sending INELIGIBLE email.  Verify related ABCs request and email addresses exist then try again.";

	private static final String ERROR_CLEARANCE_EMAIL = "Error sending CLEARANCE email.  Verify related ABCs request and email addresses exist then try again.";

	private static final String ERROR_SENDING_EMAIL = "Error sending email.  Verify related ABCs request and email addresses exist then try again.";

	private static final String DEFAULT_REDIRECT_URL_COMPLETE_RECORD = "redirect:/case/person/record/displayRecordCheckList";

	private static final String GENERIC_STRING = "Generic";

	private static final String DETAIL_STRING = "DetailPage";

	private static final String INFORMATION_STRING = "Information";

	private static final String ERROR_URL_STRING = "case/SDMError";

	private static final String NULL_STRING = "null";

	public static final ResourceBundle emailConfigBundle = ResourceBundle.getBundle("EmailConfig");

	private static final String RECORDS_CHECK_EMAIL_CONFIG_BASE = "RecordsCheckDetail.emailId.";

	private static final String NOT_APPLICABLE = "N/A";

	public static final ResourceBundle abcsDocumentPropBundle = ResourceBundle.getBundle("ABCSDocumentsEndPoint");

	private static final String ABCS_DOCUMENT_DELETE_URI = "recordsCheckDetail.deleteABCSDocument.";

	public static final String SERVICE_RECORDSCHECK_AUD = "records_check_aud";

	public static final String SERVICE_UPDATE_RECORDSCHECK_STATUS = "update_records_check_status";

	public static final String SERVICE_RECORDSCHECK_SERVICECODE = "get_records_check_service_code";

	public static final String SERVICE_UPDATE_EMAIL = "update_email";

	public static final String SERVICE_RECORDSCHECK_GENERATALERTS = "generat_alerts";

	public static final String SERVICE_GET_RECORDSCHECK_DETAIL = "get_record_check_detail";

	public static final String SERVICE_GET_PERSON_PHONE_NUMBER = "get_person_phone_number";

	public static final String SERVICE_GET_PERSON_FP_EMAIL_ADDRESS = "get_person_fp_email_address";

	public static final String SERVICE_GET_MOST_RECENT_CASA_FPS_RECORDCHECK = "get_most_recent_casa_fps_record_check";

	public static final String SERVICE_GET_PERSON_CASA_PROVISIONED = "get_person_casa_provisioned";

	public static final String SERVICE_GET_RECORDS_CHECK_DOCUMENT = "retrieve_records_check_document";

	public static final String SERVICE_GET_RECORDS_CHECK_NOTIFICATIONS = "retrive_records_check_notifications";

	public static final String SERVICE_GET_ABCS_CHECK = "get_is_abcs_check";

	public static final String SERVICE_GET_CASA_FPS_CHECK = "get_casa_fps_check";

	public static final String SERVICE_GET_ABCS_CONTRACT_ID = "get_abcs_contract_id";

	public static final String SERVICE_GET_ABCS_ACCESS_DATA = "get_abcs_access_data"; // Added for artf172936

	public static final String SERVICE_GET_PERSON_NAME_LIST = "get_person_name_list";

	public static final String SERVICE_IS_NAME_VALID = "is_name_valid";

	public static final String SERVICE_SAVE_CRIMINAL_HISTORY_NARRATIVE = "save_criminal_history_narrative";

	public static final String SERVICE_HAS_PENDING_FINGERPRINT_CHECK = "has_pending_fingerprint_check";

	public static final String SERVICE_DPS_NAMESHERCH_PROCEDURE = "call_dps_ws_name_search_procedure";

	public static final String SERVICE_GET_PERSON_FPLIST = "get_person_fp_list";

	public static final String SERVICE_FETCH_EMAIL_LIST = "fetch_email_list";

	public static final String SERVICE_SAVE_PERSON_PHONE = "save_person_phone";

	public static final String SERVICE_UPDATE_PERSON_PHONE = "update_person_phone";

	public static final String SERVICE_UPDATE_PERSON_EMAIL = "update_person_email";

	public static final String SERVICE_GET_SCOR_CONTRACT_NUMBER = "get_scor_contract_number";

	public static final String SERVICE_HAS_CURRENT_PRIMARY_ADDRESS = "has_current_primary_address";

	public static final String SERVICE_GENERATE_FBI_ELGB_EXHIRE_EMAIL = "generate_fbi_eligible_exhire_email";

	public static final String SERVICE_GENERATE_ELIGIBLE_EMAIL = "generate_eligible_email";

	public static final String SERVICE_GENERATE_PCS_ELIGIBLE_EMAIL = "generate_pcs_eligible_email";

	public static final String SERVICE_GENERATE_INELIGIBLE_EMAIL = "generate_ineligible_email";

	public static final String SERVICE_GENERATE_PCS_INELIGIBLE_EMAIL ="generate_pcs_ineligible_email";

	public static final String SERVICE_GENERATE_PS_CLEARANCE_EMAIL = "generate_ps_clearance_email";

	public static final String SERVICE_HAS_EMAIL_SENT = "has_email_sent";

	public static final String SERVICE_GET_LAST_UPDATE_DATE_FOR_DOCUMENT = "get_last_update_date_for_document";

	public static final String SERVICE_DELETE_DOCUMENT = "delete_document";

	public static final String SERVICE_ROLE_IN_WORKLOAD_STAGE = "role_in_workload_stage";

	private static final Properties emailProperties = new Properties();

	private static final String EMAIL_CONFIGURATION_FILE = "/EmailConfig.properties";
	static {
		InputStream inputStream = EmailUtility.class.getResourceAsStream(EMAIL_CONFIGURATION_FILE);
		try {
			emailProperties.load(inputStream);
		}catch (Exception e){
			e.printStackTrace();
		}
	}


	public static final String SERVICE_HAS_ORIGINATING_FINGERPRINT_CHECK = "has_originating_fingerprint_check";

	public static final String SERVICE_GET_SID_ORIGINAL_FINGERPRINT = "get_sid_original_fingerprint";

	public static final String SERVICE_GET_ABCS_CHECK_RAP_BACK = "get_is_abcs_check_rap_back";

	public static final String SERVICE_GET_NEW_HIRE_COUNT = "get_new_hire_count";

	public static final String SERVICE_GET_CD_DETERMINATION = "get_cd_determination";

	/**
	 * Method Name: getRecordsCheckDetail Method Description:This method is used to
	 * fetch the Record Check detail for a particular idRecCheck.
	 *
	 * @param idRecCheck
	 * @return recordsCheckDto
	 */
	@SuppressWarnings("unchecked")
	public RecordsCheckDto getRecordsCheckDetail(Long idRecCheck) {
		RecordsCheckReq recordsCheckRequest = new RecordsCheckReq();
		recordsCheckRequest.setIdRecCheck(idRecCheck);

		String completeURI = JNDIUtil.buildServiceUrl(WebConstants.JNDIHOSTURL,
				serviceBundle.getString(SERVICE_GET_RECORDSCHECK_DETAIL));
		RecordsCheckDto recordsCheckDto = new RecordsCheckDto();

		ResponseEntity<RecordsCheckRes> recordsCheckDetailResponse = (ResponseEntity<RecordsCheckRes>) handleResponse(
				configureRestCall().postForEntity(completeURI, recordsCheckRequest, RecordsCheckRes.class));
		if (!ObjectUtils.isEmpty(recordsCheckDetailResponse)
				&& !ObjectUtils.isEmpty(recordsCheckDetailResponse.getBody())
				&& !ObjectUtils.isEmpty(recordsCheckDetailResponse.getBody().getRecordsCheckDto())) {
			recordsCheckDto = recordsCheckDetailResponse.getBody().getRecordsCheckDto();

			RecordsCheckRes recordsCheckResource = getAbcsContractID(recordsCheckDto.getIdRecCheck());
			if (!ObjectUtils.isEmpty(recordsCheckResource)) {
				if(!ObjectUtils.isEmpty(recordsCheckResource.getIdContract())) {
					int idContract = recordsCheckResource.getIdContract().intValue();
					recordsCheckDto.setIdContract(idContract);
				}
				if(!ObjectUtils.isEmpty(recordsCheckResource.getContractType())) {
					recordsCheckDto.setContractType(cacheAdapter.getDecode(CodesConstant.CNTRTYPE, recordsCheckResource.getContractType()));
				}
			}

		}
		return recordsCheckDto;
	}

	/**
	 * Method Name: getPersonPhoneNumber Method Description: This method is used to
	 * retrieve the person's phone number.
	 *
	 * @param idPerson
	 * @return personFPPhone
	 */
	@SuppressWarnings("unchecked")
	public String getPersonPhoneNumber(Long idPerson) {
		PhoneReq phoneRequest = new PhoneReq();
		phoneRequest.setIdPerson(idPerson);

		String completeURI = JNDIUtil.buildServiceUrl(WebConstants.JNDIHOSTURL,
				serviceBundle.getString(SERVICE_GET_PERSON_PHONE_NUMBER));
		ResponseEntity<PhoneRes> phoneDetailResponse = (ResponseEntity<PhoneRes>) handleResponse(
				configureRestCall().postForEntity(completeURI, phoneRequest, PhoneRes.class));

		String personFPPhone = new String();
		if (!ObjectUtils.isEmpty(phoneDetailResponse.getBody().getPhoneDto())) {
			PersonPhoneRetDto personPhoneDto = phoneDetailResponse.getBody().getPhoneDto();
			personFPPhone = personPhoneDto.getIdPersonPhone().toString();
		}
		return personFPPhone;
	}

	/**
	 * Method Name: getPersonEmailAddress Method Description: This method is used to
	 * retrieve the person's email address.
	 *
	 * @param idPerson
	 * @return emailAddress
	 */
	@SuppressWarnings("unchecked")
	public String getPersonEmailAddress(Long idPerson) {
		EmailReq emailRequest = new EmailReq();
		emailRequest.setIdPerson(idPerson);

		String completeURI = JNDIUtil.buildServiceUrl(WebConstants.JNDIHOSTURL,
				serviceBundle.getString(SERVICE_GET_PERSON_FP_EMAIL_ADDRESS));
		ResponseEntity<EmailRes> emailDetailResponse = (ResponseEntity<EmailRes>) handleResponse(
				configureRestCall().postForEntity(completeURI, emailRequest, EmailRes.class));
		String emailAddress = new String();
		if (!ObjectUtils.isEmpty(emailDetailResponse)) {
			emailAddress = emailDetailResponse.getBody().getEmailAddress();
		}

		return emailAddress;
	}

	/**
	 * Method Name: isMostRecentCASAFPSCheck Method Description:This method is used
	 * to check for a particular idRecCheck and idPerson , there is a recent CASA
	 * FPS check.
	 *
	 * @param idPerson
	 * @param idRecordCheck
	 * @return indValid
	 */
	@SuppressWarnings("unchecked")
	public boolean isMostRecentCASAFPSCheck(Long idPerson, Long idRecordCheck) {
		RecordsCheckReq recordsCheckRequest = new RecordsCheckReq();

		recordsCheckRequest.setExtUserType(WebConstants.CASA);
		recordsCheckRequest.setRcCheckType(CodesConstant.CCHKTYPE_75);
		recordsCheckRequest.setIndFPSCheck(WebConstants.YES);
		recordsCheckRequest.setIdRecCheckPerson(idPerson);
		recordsCheckRequest.setIdPdbBgCheck(1l);
		Long idRecCheck = null;
		boolean indValid = false;
		String completeURI = JNDIUtil.buildServiceUrl(WebConstants.JNDIHOSTURL,
				serviceBundle.getString(SERVICE_GET_MOST_RECENT_CASA_FPS_RECORDCHECK));

		ResponseEntity<RecordsCheckRes> mostRecentCasaCheckResponse = (ResponseEntity<RecordsCheckRes>) handleResponse(
				configureRestCall().postForEntity(completeURI, recordsCheckRequest, RecordsCheckRes.class));

		if (!ObjectUtils.isEmpty(mostRecentCasaCheckResponse)
				&& !ObjectUtils.isEmpty(mostRecentCasaCheckResponse.getBody())) {
			idRecCheck = mostRecentCasaCheckResponse.getBody().getIdRecCheck();
		}

		if (!ObjectUtils.isEmpty(idRecCheck) && idRecCheck.equals(idRecordCheck)) {
			indValid = true;
		}
		return indValid;
	}

	/**
	 * Method Name: isPersonCasaProvisioned Method Description:This method is used
	 * to check if the person is CASA provisioned.
	 *
	 * @param idPerson
	 * @return indValid
	 */
	@SuppressWarnings("unchecked")
	public boolean isPersonCasaProvisioned(Long idPerson) {
		boolean indValid = false;
		RecordsCheckReq recordsCheckRequest = new RecordsCheckReq();
		recordsCheckRequest.setIdRecCheckPerson(idPerson);

		String completeURI = JNDIUtil.buildServiceUrl(WebConstants.JNDIHOSTURL,
				serviceBundle.getString(SERVICE_GET_PERSON_CASA_PROVISIONED));
		String isCasaProvisioned = null;

		ResponseEntity<RecordsCheckRes> casaProvisionedResponse = (ResponseEntity<RecordsCheckRes>) handleResponse(
				configureRestCall().postForEntity(completeURI, recordsCheckRequest, RecordsCheckRes.class));

		if (!ObjectUtils.isEmpty(casaProvisionedResponse) && !ObjectUtils.isEmpty(casaProvisionedResponse.getBody())) {
			isCasaProvisioned = casaProvisionedResponse.getBody().getIsPersonCasaProvisioned();
			if (!ObjectUtils.isEmpty(isCasaProvisioned) && isCasaProvisioned.equalsIgnoreCase(WebConstants.YES)) {
				indValid = true;
			}
		}

		return indValid;
	}

	/**
	 * Method Name: retrieveRecordsCheckDocmntn Method Description: This method is
	 * used to retrieve the documents which were uploaded in ABCS for the particular
	 * records check.
	 *
	 * @param idRecCheck
	 * @return docList
	 */
	@SuppressWarnings("unchecked")
	public List<DocumentPdbValueDto> retrieveRecordsCheckDocuments(Long idRecCheck) {
		RecordsCheckReq recordsCheckRequest = new RecordsCheckReq();
		recordsCheckRequest.setIdRecCheck(idRecCheck);

		String completeURI = JNDIUtil.buildServiceUrl(WebConstants.JNDIHOSTURL,
				serviceBundle.getString(SERVICE_GET_RECORDS_CHECK_DOCUMENT));
		List<DocumentPdbValueDto> docList = new ArrayList<DocumentPdbValueDto>();

		ResponseEntity<RecordsCheckRes> retrieveDocListResponse = (ResponseEntity<RecordsCheckRes>) handleResponse(
				configureRestCall().postForEntity(completeURI, recordsCheckRequest, RecordsCheckRes.class));

		if (!ObjectUtils.isEmpty(retrieveDocListResponse) && !ObjectUtils.isEmpty(retrieveDocListResponse.getBody())) {
			docList = retrieveDocListResponse.getBody().getDocumentPbdValueDto();
		}

		return docList;

	}

	/**
	 * Method Name: retrieveRecordsCheckNotif Method Description:This method is used
	 * to retrieve the email notifications for the particular Record Check.
	 *
	 * @param idRecCheck
	 * @return notificationList
	 */
	@SuppressWarnings("unchecked")
	public List<RecordsCheckNotificationDto> retrieveRecordsCheckNotif(Long idRecCheck) {
		RecordsCheckReq recordsCheckRequest = new RecordsCheckReq();
		recordsCheckRequest.setIdRecCheck(idRecCheck);

		String completeURI = JNDIUtil.buildServiceUrl(WebConstants.JNDIHOSTURL,
				serviceBundle.getString(SERVICE_GET_RECORDS_CHECK_NOTIFICATIONS));
		List<RecordsCheckNotificationDto> notificationList = new ArrayList<RecordsCheckNotificationDto>();

		ResponseEntity<RecordsCheckRes> retrieveNotificationListResponse = (ResponseEntity<RecordsCheckRes>) handleResponse(
				configureRestCall().postForEntity(completeURI, recordsCheckRequest, RecordsCheckRes.class));

		if (!ObjectUtils.isEmpty(retrieveNotificationListResponse)
				&& !ObjectUtils.isEmpty(retrieveNotificationListResponse.getBody())) {
			notificationList = retrieveNotificationListResponse.getBody().getRecordsCheckNotificationDto();
		}

		return notificationList;
	}

	/**
	 * Method Name: isABCSCheck Method Description:This method is used to check if
	 * the record check is an ABCS Record Check.
	 *
	 * @param idRecCheck
	 * @return indABCSCheck
	 */
	@SuppressWarnings("unchecked")
	public boolean isABCSCheck(Long idRecCheck) {

		RecordsCheckReq recordsCheckRequest = new RecordsCheckReq();
		recordsCheckRequest.setIdRecCheck(idRecCheck);

		String completeURI = JNDIUtil.buildServiceUrl(WebConstants.JNDIHOSTURL,
				serviceBundle.getString(SERVICE_GET_ABCS_CHECK));
		boolean indABCSCheck = false;
		ResponseEntity<RecordsCheckRes> isABCSCheckResponse = (ResponseEntity<RecordsCheckRes>) handleResponse(
				configureRestCall().postForEntity(completeURI, recordsCheckRequest, RecordsCheckRes.class));
		if (!ObjectUtils.isEmpty(isABCSCheckResponse) && !ObjectUtils.isEmpty(isABCSCheckResponse.getBody())) {
			indABCSCheck = isABCSCheckResponse.getBody().isABCSCheck();
		}

		return indABCSCheck;
	}

	/**
	 * Method Name: isCasaFpsCheck Method Description:This method is used to check
	 * if a particular Record check is CASA FPS record.
	 *
	 * @param idRecCheck
	 * @return indValid
	 */
	@SuppressWarnings("unchecked")
	public boolean isCasaFpsCheck(Long idRecCheck) {
		boolean indValid = false;
		RecordsCheckReq recordsCheckRequest = new RecordsCheckReq();
		recordsCheckRequest.setExtUserType(WebConstants.CASA);
		recordsCheckRequest.setRcCheckType(CodesConstant.CCHKTYPE_75);
		recordsCheckRequest.setIndFPSCheck(WebConstants.YES);
		recordsCheckRequest.setIdRecCheck(idRecCheck);
		recordsCheckRequest.setIdPdbBgCheck(1l);

		String completeURI = JNDIUtil.buildServiceUrl(WebConstants.JNDIHOSTURL,
				serviceBundle.getString(SERVICE_GET_CASA_FPS_CHECK));
		String isCasaFpsCheck = null;

		ResponseEntity<RecordsCheckRes> isCasaFpsCheckResponse = (ResponseEntity<RecordsCheckRes>) handleResponse(
				configureRestCall().postForEntity(completeURI, recordsCheckRequest, RecordsCheckRes.class));
		if (!ObjectUtils.isEmpty(isCasaFpsCheckResponse) && !ObjectUtils.isEmpty(isCasaFpsCheckResponse.getBody())) {
			isCasaFpsCheck = isCasaFpsCheckResponse.getBody().getIsCasaFpsCheck();
			if (!ObjectUtils.isEmpty(isCasaFpsCheck) && isCasaFpsCheck.equalsIgnoreCase(WebConstants.YES)) {
				indValid = true;
			}
		}
		return indValid;
	}

	/**
	 * Added to implement artf171305
	 * Method Name: getAbcsContractID Method Description:This method is used to get
	 * the Contract ID  and Contract Type for the particular Record Check.
	 * Changed the method's return type to implement Artifact artf171305
	 * @param idRecCheck
	 * @return RecordsCheckRes
	 */
	@SuppressWarnings("unchecked")
	public RecordsCheckRes getAbcsContractID(Long idRecCheck) {
		RecordsCheckReq recordsCheckRequest = new RecordsCheckReq();
		RecordsCheckRes response = new RecordsCheckRes();
		recordsCheckRequest.setIdRecCheck(idRecCheck);
		Long idContract = null;
		String completeURI = JNDIUtil.buildServiceUrl(WebConstants.JNDIHOSTURL,
				serviceBundle.getString(SERVICE_GET_ABCS_CONTRACT_ID));
		ResponseEntity<RecordsCheckRes> getAbcsContractIDResponse = (ResponseEntity<RecordsCheckRes>) handleResponse(
				configureRestCall().postForEntity(completeURI, recordsCheckRequest, RecordsCheckRes.class));

		if (!ObjectUtils.isEmpty(getAbcsContractIDResponse)
				&& !ObjectUtils.isEmpty(getAbcsContractIDResponse.getBody())) {
			if (!ObjectUtils.isEmpty(getAbcsContractIDResponse.getBody().getIdContract())) {
				response = getAbcsContractIDResponse.getBody();
			}
		}
		return response;
	}

	/**
	 * Added to implement artf172936
	 * Method Name: getAbcsAccessData
	 * Method Description:This method is used to get the value of the indicator hasChriAccess.
	 * The value for this indicator should be derived from a Yes/No answer to either the “Access to CHRI” question
	 * or the “Access to IMPACT” question in the ABCS FBI background check request processes
	 * @param idRecCheck
	 * @return hasChriAccess
	 */
	@SuppressWarnings("unchecked")
	public boolean getAbcsAccessData(Long idRecCheck) {
		RecordsCheckReq recordsCheckRequest = new RecordsCheckReq();
		recordsCheckRequest.setIdRecCheck(idRecCheck);
		boolean hasChriAccess = false;
		String completeURI = JNDIUtil.buildServiceUrl(WebConstants.JNDIHOSTURL,
				serviceBundle.getString(SERVICE_GET_ABCS_ACCESS_DATA));
		ResponseEntity<RecordsCheckRes> getAbcsAccessDataResponse = (ResponseEntity<RecordsCheckRes>) handleResponse(
				configureRestCall().postForEntity(completeURI, recordsCheckRequest, RecordsCheckRes.class));

		if (!ObjectUtils.isEmpty(getAbcsAccessDataResponse)
				&& !ObjectUtils.isEmpty(getAbcsAccessDataResponse.getBody())) {
			hasChriAccess = getAbcsAccessDataResponse.getBody().isChriAccess();
		}
		return hasChriAccess;
	}


	/**
	 * Method Name: saveRecordsCheckDetail Method Description: This method is used
	 * to save the Record Check Details.
	 *
	 * @param checkDto
	 * @param recordsCheckDtosList
	 * @param commonData
	 * @param nameList
	 * @return recordsCheckListResponse
	 */
	public RecordsCheckListRes saveRecordsCheckDetail(RecordsCheckDto checkDto,
			List<RecordsCheckDto> recordsCheckDtosList, CommonDto commonData, URL styleSheetURL,
			List<PersonInfoDto> nameList) {
		Map<String, Object> returnMap = new HashMap<String, Object>();
		RecordsCheckDetailReq recordsCheckDetailRequest = populateRecordsCheckDetailSaveRequest(checkDto,
				recordsCheckDtosList, commonData);

		// Call REST service for saving a record check detail
		String completeUrl = JNDIUtil.buildServiceUrl(WebConstants.JNDIHOSTURL,
				serviceBundle.getString(SERVICE_RECORDSCHECK_AUD));
		ResponseEntity<RecordsCheckListRes> response = (ResponseEntity<RecordsCheckListRes>) handleResponse(
				configureRestCall().postForEntity(completeUrl, recordsCheckDetailRequest, RecordsCheckListRes.class));
		RecordsCheckListRes recordsCheckListResponse = response.getBody();
		if(ObjectUtils.isEmpty(recordsCheckListResponse.getErrorDto())){
			String pageMode = null;
			String cdSearchType = null;
			String idSaveRecCheck = null;
			String reviewType = null;
			if (!ObjectUtils.isEmpty(checkDto.getPageModeStr())) {
				pageMode = checkDto.getPageModeStr();
			}
			if (!ObjectUtils.isEmpty(checkDto.getRecCheckCheckType())) {
				cdSearchType = checkDto.getRecCheckCheckType();
			}

			if (!ObjectUtils.isEmpty(recordsCheckListResponse)
					&& !ObjectUtils.isEmpty(recordsCheckListResponse.getIdRecordCheck())
					&& recordsCheckListResponse.getIdRecordCheck() != 0l) {
				idSaveRecCheck = String.valueOf(recordsCheckListResponse.getIdRecordCheck());
			}
			if (!ObjectUtils.isEmpty(checkDto.getIndReviewNow())) {
				reviewType = checkDto.getIndReviewNow();
			}

			if (!ObjectUtils.isEmpty(cdSearchType) && CodesConstant.CCHKTYPE_10.equals(cdSearchType)
					&& !ObjectUtils.isEmpty(pageMode) && pageMode.equals(NEW)) {
				if (!ObjectUtils.isEmpty(reviewType) && REVIEW_LATER.equalsIgnoreCase(reviewType)) {

					// adding it as a flash attribute for redirecting to the display
					// method of
					// Records Check Detail screen

					returnMap.put("idRecCheckWS", recordsCheckListResponse.getIdRecordCheck());
					// Call DSP_WS_NAME_SEARCH Procedure to insert the names in to
					// TEMP_DPS_NAME_SEARCH table.
					callDPSWSNameSearchProcedure(recordsCheckListResponse.getIdRecordCheck(), commonData);
				} else if (!ObjectUtils.isEmpty(reviewType) && REVIEW_NOW.equalsIgnoreCase(reviewType)) {
					returnMap.put("idRecCheckWS", recordsCheckListResponse.getIdRecordCheck());
					String errorMessage = sendName(recordsCheckDetailRequest, commonData, recordsCheckListResponse,
							styleSheetURL, nameList);
					if (!ObjectUtils.isEmpty(errorMessage)) {

						returnMap.put("informationMessage", errorMessage);
					}

				}
				returnMap.put("bReviewNowLater", "false");
			}

			// artf51666 - changed from CCHKTYPE_15 to CCHKTYPE_95
			if (!ObjectUtils.isEmpty(pageMode) && pageMode.equals(NEW) && CodesConstant.CCHKTYPE_95.equals(cdSearchType)) {
				if (!hasPendingFingerprintCheck(commonData.getIdPerson())) {
					// Verify individual is at least 10 years of age before creating
					// FBI Fingerprint Check
					boolean tooYoung = false;

					if (!ObjectUtils.isEmpty(recordsCheckListResponse)) {
						Date dateOfBirth = !ObjectUtils.isEmpty(recordsCheckListResponse.getPersonBirthDate())
								? recordsCheckListResponse.getPersonBirthDate()
								: null;
						if (!ObjectUtils.isEmpty(dateOfBirth) && DateFormatUtil.getAge(dateOfBirth) < 10) {
							tooYoung = true;
						}
					}

					if (!tooYoung) {
						returnMap.put("idRecCheckWS", recordsCheckListResponse.getIdRecordCheck());
						// populate the request for FingerPrint Record Check
						RecordsCheckDetailReq fingerPrintRequest = populateFingerprintCheckRequest(recordsCheckListResponse,
								checkDto, commonData);

						// call the business delegate method to call the service
						saveFingerPrintRecordsCheckDetail(fingerPrintRequest);

					}
				}
			}

			if (!ObjectUtils.isEmpty(pageMode) && pageMode.equals(NEW) && CodesConstant.CCHKTYPE_80.equals(cdSearchType)) {

				// call business delegate method to insert email information into
				// PERSON_EMAIL table.
				if (!ObjectUtils.isEmpty(checkDto.getPhoneNumber())) {
					String phoneNumber = checkDto.getPhoneNumber();
					String hiddenPersonPhoneNumber = checkDto.getHiddenPersonPhone();
					int hdnIdPersonPhone = 0;
					if (NumberUtils.isNumber(checkDto.getIdHiddenPersonPhone())) {
						hdnIdPersonPhone = Integer.parseInt(checkDto.getIdHiddenPersonPhone());
					}
					Boolean hdnIndPhonePrimary = Boolean.FALSE;

					if (!ObjectUtils.isEmpty(checkDto.getIndHiddenPhonePrimary())) {
						hdnIndPhonePrimary = Boolean.valueOf(checkDto.getIndHiddenPhonePrimary());
					}
					if (!phoneNumber.equalsIgnoreCase(hiddenPersonPhoneNumber)) {
						addPersonPhoneDetail(checkDto, hiddenPersonPhoneNumber, commonData, hdnIndPhonePrimary);
						updatePersonPhone(hdnIdPersonPhone);
					}
				}
				if (!ObjectUtils.isEmpty(checkDto.getEmail())) {
					String emailAddress = checkDto.getEmail();
					String existingEmail = checkDto.getHiddenPersonEmail();
					int idStaff = commonData.getIdPerson().intValue();
					String idPersonEmail = checkDto.getIdHiddenPersonEmail();
					boolean existingIndPrimary = Boolean.getBoolean(checkDto.getIndHiddenEmailPrimary());
					int hdnIdEmailPerson = 0;
					if (NumberUtils.isNumber(idPersonEmail)) {
						hdnIdEmailPerson = Integer.parseInt(idPersonEmail);
					}

					if (!emailAddress.equalsIgnoreCase(existingEmail)) {
						addReckCheckPersonEmailDetail(checkDto, existingEmail, existingIndPrimary, commonData);
						updatePersonEmail(idStaff, hdnIdEmailPerson);
					}
				}
			}
			if (pageMode.equals(NEW)) {
				pageMode = MODIFY;
				checkDto.setPageModeStr(pageMode);
			}

			RecordsCheckDto previousRecordCheckDetails = null;
			boolean detailPage = false;
			if (!ObjectUtils.isEmpty(cdSearchType) && CodesConstant.CCHKTYPE_10.equals(cdSearchType) || // DPS Criminal History
					CodesConstant.CCHKTYPE_80.equals(cdSearchType) || // FBI Finger Print
					CodesConstant.CCHKTYPE_75.equals(cdSearchType) || // FPS HistoryCheck Batch
					CodesConstant.CCHKTYPE_81.equals(cdSearchType))
			{
				detailPage = true;

				// Check if the record is already in the session list
				boolean idExists = false;
				if (!CollectionUtils.isEmpty(recordsCheckDtosList)) {
					idExists = recordsCheckDtosList.stream()
							.anyMatch(t -> t.getIdRecCheck().equals(recordsCheckListResponse.getIdRecordCheck()));
				}

				if (idExists) {
					previousRecordCheckDetails = recordsCheckDtosList.stream().filter(recordsCheck -> recordsCheck
							.getIdRecCheck().equals(recordsCheckListResponse.getIdRecordCheck())).findAny().orElse(null);
				}

				if (null != previousRecordCheckDetails && !ObjectUtils.isEmpty(cdSearchType)
						&& !ObjectUtils.isEmpty(previousRecordCheckDetails.getRecChkDeterm())
						&& CodesConstant.CCHKTYPE_75.equals(cdSearchType)) {
					detailPage = false;
				} else {
					if (null != previousRecordCheckDetails
							&& !ObjectUtils.isEmpty(previousRecordCheckDetails.getDtDetermFinal())) {
						detailPage = false;
					}
				}
			}
			if (detailPage) {
				returnMap.put("returnURL", "redirect:/case/person/record/recordAction?pageMode=" + pageMode
						+ "&recordsCheckDetailIndex=" + idSaveRecCheck + "");

			} else {

				if (recordCheckUtil.canHaveNarrative(cdSearchType, checkDto.getDtRecCheckRequest())) {

					// Check if the record is already in the session list
					/*boolean idExists = false;
					if (!CollectionUtils.isEmpty(recordsCheckDtosList)) {
						idExists = recordsCheckDtosList.stream()
								.anyMatch(t -> t.getIdRecCheck().equals(recordsCheckListResponse.getIdRecordCheck()));
					} */

					// else only update the last update and get the index value
					/*if (idExists) {
						previousRecordCheckDetails = recordsCheckDtosList.stream().filter(recordsCheck -> recordsCheck
								.getIdRecCheck().equals(recordsCheckListResponse.getIdRecordCheck())).findAny()
								.orElse(null);

					}*/

					returnMap.put("txtUlIdRecCheck", idSaveRecCheck);
					returnMap.put("returnURL", "redirect:/case/person/record/recordAction?pageMode=" + pageMode
							+ "&recordsCheckDetailIndex=" + idSaveRecCheck + "");
				} else {
					returnMap.put("returnURL", "redirect:/case/person/record/displayRecordCheckList");
				}

			}
			recordsCheckListResponse.setReturnValuesMap(returnMap);
		}
		return recordsCheckListResponse;
	}

	/**
	 * Method Name: callDPSWSNameSearchProcedure Method Description:This method is
	 * used to call the PLSQL to make an entry into Temp table when a Name Request
	 * is request during Save of Record Check.
	 *
	 * @param idRecordCheck
	 * @param idRecCheckPerson
	 * @param idUser
	 */
	public void callDPSWSNameSearchProcedure(Long idRecordCheck, Long idRecCheckPerson, Long idUser) {
		RecordsCheckReq recordsCheckRequest = new RecordsCheckReq();
		recordsCheckRequest.setIdRecCheckPerson(idRecCheckPerson);
		recordsCheckRequest.setIdRecCheck(idRecordCheck);
		recordsCheckRequest.setUserId(idUser.toString());
		String completeURI = JNDIUtil.buildServiceUrl(WebConstants.JNDIHOSTURL,
				serviceBundle.getString(SERVICE_DPS_NAMESHERCH_PROCEDURE));
		handleResponse(configureRestCall().postForEntity(completeURI, recordsCheckRequest, RecordsCheckRes.class));
	}

	/**
	 * Method Name: callNameRequest Method Description:This method is used to fetch
	 * the name list for the person in Record Check.
	 *
	 * @param recordsCheckDetailReq
	 * @return personInfoValueBeanList
	 */
	public List<PersonInfoDto> callNameRequest(RecordsCheckDetailReq recordsCheckDetailReq) {
		// Get the name List
		List<PersonInfoDto> personInfoValueBeanList = new ArrayList<PersonInfoDto>();
		if (!ObjectUtils.isEmpty(recordsCheckDetailReq.getIdRecCheckPerson())
				&& recordsCheckDetailReq.getIdRecCheckPerson() != 0l) {
			personInfoValueBeanList = getPersonNameList(recordsCheckDetailReq.getIdRecCheckPerson());
		}
		return personInfoValueBeanList;
	}

	/**
	 * Method Name: getPersonNameList Method Description:This method is used to
	 * fetch the Person name list for a particular person for whom the Record Check
	 * is done.
	 *
	 * @param idRecCheckPerson
	 * @return nameList
	 */
	@SuppressWarnings("unchecked")
	private List<PersonInfoDto> getPersonNameList(Long idRecCheckPerson) {
		RecordsCheckReq recordsCheckRequest = new RecordsCheckReq();
		recordsCheckRequest.setIdRecCheckPerson(idRecCheckPerson);
		String completeURI = JNDIUtil.buildServiceUrl(WebConstants.JNDIHOSTURL,
				serviceBundle.getString(SERVICE_GET_PERSON_NAME_LIST));
		List<PersonInfoDto> nameList = new ArrayList<PersonInfoDto>();
		ResponseEntity<RecordsCheckRes> nameListResponse = (ResponseEntity<RecordsCheckRes>) handleResponse(
				configureRestCall().postForEntity(completeURI, recordsCheckRequest, RecordsCheckRes.class));
		if (!ObjectUtils.isEmpty(nameListResponse) && !ObjectUtils.isEmpty(nameListResponse.getBody())
				&& !CollectionUtils.isEmpty(nameListResponse.getBody().getPersoNameList())) {
			nameList = nameListResponse.getBody().getPersoNameList();
		}

		return nameList;
	}

	/**
	 * Method Name: isNameValid Method Description:This method is used to check if
	 * the names in the name List is Valid before sending the request to DPS
	 * Criminal Name Request service.
	 *
	 * @return response
	 */
	@SuppressWarnings("unchecked")
	public RecordsCheckRes isNameValid(PersonNameCheckDto personNameCheckDto) {
		RecordsCheckRes response = new RecordsCheckRes();
		RecordsCheckReq recordsCheckRequest = new RecordsCheckReq();
		/*recordsCheckRequest.setFirstName(personInfoDto.getFirstName());
		recordsCheckRequest.setMiddleName(personInfoDto.getMiddleName());
		recordsCheckRequest.setLastName(personInfoDto.getLastName());
		recordsCheckRequest.setPersonInfoDto(personInfoDto);*/
		recordsCheckRequest.setPersonNameCheckDto(personNameCheckDto);
		String completeURI = JNDIUtil.buildServiceUrl(WebConstants.JNDIHOSTURL,
				serviceBundle.getString(SERVICE_IS_NAME_VALID));

		ResponseEntity<RecordsCheckRes> nameValidResponse = (ResponseEntity<RecordsCheckRes>) handleResponse(
				configureRestCall().postForEntity(completeURI, recordsCheckRequest, RecordsCheckRes.class));
		if (!ObjectUtils.isEmpty(nameValidResponse) && !ObjectUtils.isEmpty(nameValidResponse.getBody())
				&& !ObjectUtils.isEmpty(Boolean.valueOf(nameValidResponse.getBody().isNameValid()))) {
			response = nameValidResponse.getBody();
		}

		return response;
	}

	/**
	 * Method Name: saveToCriminalHistoryAndNarrative Method Description:This method
	 * is used to save the criminal history results and narrative returned from DPS
	 * Criminal History Name Request service.
	 *
	 * @param criminalHistoryReq
	 */
	public void saveToCriminalHistoryAndNarrative(CriminalHistoryReq criminalHistoryReq) {

		String completeURI = JNDIUtil.buildServiceUrl(WebConstants.JNDIHOSTURL,
				serviceBundle.getString(SERVICE_SAVE_CRIMINAL_HISTORY_NARRATIVE));
		handleResponse(configureRestCall().postForEntity(completeURI, criminalHistoryReq, CriminalHistoryRes.class));

	}

	/**
	 * Method Name: updateRecordsCheckStatus Method Description:This method is used
	 * to update the Record Check status when the results are returned.
	 *
	 * @param idRecordCheck
	 * @param status
	 */
	public void updateRecordsCheckStatus(Long idRecordCheck, String status) {

		RecordsCheckStatusReq recordsCheckStatusReq = new RecordsCheckStatusReq();
		recordsCheckStatusReq.setIdRecCheck(idRecordCheck);
		recordsCheckStatusReq.setStatus(status);
		// Call REST service for updating record check status
		String completeUrl = JNDIUtil.buildServiceUrl(WebConstants.JNDIHOSTURL,
				serviceBundle.getString(SERVICE_UPDATE_RECORDSCHECK_STATUS));
		handleResponse(configureRestCall().postForEntity(completeUrl, recordsCheckStatusReq, RecordsCheckRes.class));
	}

	/**
	 * Method Name: generateAlerts Method Description:This method is used to
	 * generate alerts when the results are returned for the DPS Criminal History
	 * Record Check.
	 *
	 * @param idRecCheckPerson
	 * @param idRecCheckRequestor
	 * @param idStage
	 * @param status
	 */
	public void generateAlerts(int idRecCheckPerson, int idRecCheckRequestor, int idStage, String status) {
		RecordsCheckStatusReq recordsCheckStatusReq = new RecordsCheckStatusReq();
		recordsCheckStatusReq.setIdRecCheckPerson((long) idRecCheckPerson);
		recordsCheckStatusReq.setIdRecCheckRequestor((long) idRecCheckRequestor);
		recordsCheckStatusReq.setIdStage((long) idStage);
		recordsCheckStatusReq.setStatus(status);
		// Call REST service for updating record check status
		String completeUrl = JNDIUtil.buildServiceUrl(WebConstants.JNDIHOSTURL,
				serviceBundle.getString(SERVICE_RECORDSCHECK_GENERATALERTS));
		handleResponse(configureRestCall().postForEntity(completeUrl, recordsCheckStatusReq, RecordsCheckRes.class));
	}

	/**
	 * Method Name: hasPendingFingerprintCheck Method Description:This method is
	 * used to check if an existing FingerPrint check in pending status is present.
	 *
	 * @param idPerson
	 * @return indPendingFingerprintCheck
	 */
	@SuppressWarnings("unchecked")
	public boolean hasPendingFingerprintCheck(Long idPerson) {
		boolean indPendingFingerprintCheck = false;
		RecordsCheckReq recordsCheckRequest = new RecordsCheckReq();
		recordsCheckRequest.setIdRecCheckPerson(idPerson);
		recordsCheckRequest.setRcCheckType(CodesConstant.CCHKTYPE_80);
		recordsCheckRequest.setRequestDate(new GregorianCalendar(2009, 05, 06).getTime());
		String completeURI = JNDIUtil.buildServiceUrl(WebConstants.JNDIHOSTURL,
				serviceBundle.getString(SERVICE_HAS_PENDING_FINGERPRINT_CHECK));
		ResponseEntity<RecordsCheckRes> hasPendingFingerprintCheckResponse = (ResponseEntity<RecordsCheckRes>) handleResponse(
				configureRestCall().postForEntity(completeURI, recordsCheckRequest, RecordsCheckRes.class));
		if (!ObjectUtils.isEmpty(hasPendingFingerprintCheckResponse)
				&& !ObjectUtils.isEmpty(hasPendingFingerprintCheckResponse.getBody()) && !ObjectUtils.isEmpty(
						Boolean.valueOf(hasPendingFingerprintCheckResponse.getBody().isHasPendingFingerprintCheck()))) {
			indPendingFingerprintCheck = hasPendingFingerprintCheckResponse.getBody().isHasPendingFingerprintCheck();
		}

		return indPendingFingerprintCheck;
	}

	/**
	 * Method Name: getServiceCode Method Description:This method is used to fetch
	 * the Service Code to be displayed in Records Check detail screen.
	 *
	 * @param idRecCheck
	 * @return serviceCode
	 */
	public String getServiceCode(int idRecCheck) {
		RecordsCheckStatusReq recordsCheckStatusReq = new RecordsCheckStatusReq();
		recordsCheckStatusReq.setIdRecCheck((long) idRecCheck);
		// Call REST service for getting record check service code
		String completeUrl = JNDIUtil.buildServiceUrl(WebConstants.JNDIHOSTURL,
				serviceBundle.getString(SERVICE_RECORDSCHECK_SERVICECODE));
		ResponseEntity<RecordsCheckRes> response = (ResponseEntity<RecordsCheckRes>) handleResponse(
				configureRestCall().postForEntity(completeUrl, recordsCheckStatusReq, RecordsCheckRes.class));
		return response.getBody().getServiceCode();
	}

	/**
	 * Method Name: saveFingerPrintRecordsCheckDetail Method Description: This
	 * method is used to save the Finger Print Records Check detail.
	 *
	 * @param fingerPrintRequest
	 */
	public void saveFingerPrintRecordsCheckDetail(RecordsCheckDetailReq fingerPrintRequest) {

		// Call REST service for saving a record check detail
		String completeUrl = JNDIUtil.buildServiceUrl(WebConstants.JNDIHOSTURL,
				serviceBundle.getString(SERVICE_RECORDSCHECK_AUD));
		handleResponse(configureRestCall().postForEntity(completeUrl, fingerPrintRequest, RecordsCheckListRes.class));
	}

	/**
	 * Method Name: getPersonPhoneList Method Description:This method is used to
	 * retrieve the Phone Numbers to be displayed while displaying the FBI
	 * Fingerprint Record Check detail.
	 *
	 * @param idPerson
	 * @return phoneList
	 */
	@SuppressWarnings("unchecked")
	public List<PersonPhoneRetDto> getPersonPhoneList(Long idPerson) {
		List<PersonPhoneRetDto> phoneList = new ArrayList<PersonPhoneRetDto>();
		PhoneReq request = new PhoneReq();
		request.setIdPerson(idPerson);
		String completeURI = JNDIUtil.buildServiceUrl(WebConstants.JNDIHOSTURL,
				serviceBundle.getString(SERVICE_GET_PERSON_FPLIST));
		ResponseEntity<PhoneRes> phoneListResponse = (ResponseEntity<PhoneRes>) handleResponse(
				configureRestCall().postForEntity(completeURI, request, PhoneRes.class));
		if (!ObjectUtils.isEmpty(phoneListResponse) && !ObjectUtils.isEmpty(phoneListResponse.getBody())) {
			phoneList = phoneListResponse.getBody().getPhoneDtoList();
		}

		return phoneList;
	}

	/**
	 * Method Name: emailList Method Description:This method is used to retrieve the
	 * Email addresses to be displayed while displaying the FBI Fingerprint Record
	 * Check detail.
	 *
	 * @param idPerson
	 * @return emailList
	 */
	@SuppressWarnings("unchecked")
	public List<EmailDetailBean> emailList(Long idPerson) {

		List<EmailDetailBean> emailList = new ArrayList<EmailDetailBean>();
		EmailReq request = new EmailReq();
		request.setIdPerson(idPerson);
		String completeURI = JNDIUtil.buildServiceUrl(WebConstants.JNDIHOSTURL,
				serviceBundle.getString(SERVICE_FETCH_EMAIL_LIST));
		ResponseEntity<EmailRes> emailListResponse = (ResponseEntity<EmailRes>) handleResponse(
				configureRestCall().postForEntity(completeURI, request, EmailRes.class));
		if (!ObjectUtils.isEmpty(emailListResponse) && !ObjectUtils.isEmpty(emailListResponse.getBody())) {
			emailList = emailListResponse.getBody().getEmailList();
		}

		return emailList;

	}

	/**
	 * Method Name: savePhone Method Description:This method is used to save the
	 * person's phone detail when a new FBI fingerprint records check is created.
	 *
	 * @param personPhoneReq
	 */
	public void savePhone(PersonPhoneReq personPhoneReq) {
		String completeURI = JNDIUtil.buildServiceUrl(WebConstants.JNDIHOSTURL,
				serviceBundle.getString(SERVICE_SAVE_PERSON_PHONE));
		handleResponse(configureRestCall().postForEntity(completeURI, personPhoneReq, PhoneRes.class));

	}

	/**
	 * Method Name: updatePersonPhone Method Description:This method is used to
	 * update the person's phone information while modifying the FBI Records Check
	 * detail.
	 *
	 * @param idPerson
	 */
	public void updatePersonPhone(int idPerson) {
		PersonPhoneReq request = new PersonPhoneReq();
		PersonPhoneRetDto personPhoneRetDto = new PersonPhoneRetDto();
		personPhoneRetDto.setIdPersonPhone((long) idPerson);
		request.setPersonPhoneRetDto(personPhoneRetDto);
		String completeURI = JNDIUtil.buildServiceUrl(WebConstants.JNDIHOSTURL,
				serviceBundle.getString(SERVICE_UPDATE_PERSON_PHONE));
		handleResponse(configureRestCall().postForEntity(completeURI, request, PhoneRes.class));

	}

	/**
	 * Method Name: saveEmailDetailsForRecordsCheck Method Description:This method
	 * is used to save the person's email details while saving the FBI fingerprint
	 * Records Check detail.
	 *
	 * @param request
	 */
	public void saveEmailDetailsForRecordsCheck(EmailDetailReq request) {
		// Call REST service for saving email address
		String completeUrl = JNDIUtil.buildServiceUrl(WebConstants.JNDIHOSTURL,
				serviceBundle.getString(SERVICE_UPDATE_EMAIL));
		handleResponse(configureRestCall().postForEntity(completeUrl, request, EmailDetailRes.class));
	}

	/**
	 * Method Name: updatePersonEmail Method Description:This method is used to
	 * update the person's email information while modifying the FBI fingerprint's
	 * record check.
	 *
	 * @param idStaff
	 * @param idPerson
	 */
	public void updatePersonEmail(int idStaff, int idPerson) {
		EmailDetailReq request = new EmailDetailReq();
		EmailDetailBean emailDetailDto = new EmailDetailBean();
		emailDetailDto.setIdStaff((long) idStaff);
		emailDetailDto.setIdEmail((long) idPerson);
		request.setEmailDetail(emailDetailDto);
		// Call REST service for update email service
		String completeURI = JNDIUtil.buildServiceUrl(WebConstants.JNDIHOSTURL,
				serviceBundle.getString(SERVICE_UPDATE_PERSON_EMAIL));
		handleResponse(configureRestCall().postForEntity(completeURI, request, EmailDetailRes.class));

	}

	/**
	 * Method Name: getScorContractNbr Method Description:This method is used to
	 * fetch the contract number.
	 *
	 * @param idRecCheck
	 * @return contractNumber
	 */
	@SuppressWarnings("unchecked")
	public String getScorContractNbr(Long idRecCheck) {

		RecordsCheckReq recordsCheckReq = new RecordsCheckReq();
		recordsCheckReq.setIdRecCheck(idRecCheck);
		String completeURI = JNDIUtil.buildServiceUrl(WebConstants.JNDIHOSTURL,
				serviceBundle.getString(SERVICE_GET_SCOR_CONTRACT_NUMBER));
		String contractNumber = null;

		ResponseEntity<RecordsCheckRes> contractNumberResponse = (ResponseEntity<RecordsCheckRes>) handleResponse(
				configureRestCall().postForEntity(completeURI, recordsCheckReq, RecordsCheckRes.class));

		if (!ObjectUtils.isEmpty(contractNumberResponse) && !ObjectUtils.isEmpty(contractNumberResponse.getBody())) {
			contractNumber = contractNumberResponse.getBody().getContractNumber();

		}
		return contractNumber;
	}

	/**
	 * Method Name: hasCurrentPrimaryAddress Method Description:This method is used
	 * to check if the particular person has a primary address.
	 *
	 * @param idPerson
	 * @return indCurrentPrimaryAddress
	 */
	@SuppressWarnings("unchecked")
	public String hasCurrentPrimaryAddress(Long idPerson) {
		AddressDtlReq addressRequest = new AddressDtlReq();
		addressRequest.setUlIdPerson(idPerson);
		String completeURI = JNDIUtil.buildServiceUrl(WebConstants.JNDIHOSTURL,
				serviceBundle.getString(SERVICE_HAS_CURRENT_PRIMARY_ADDRESS));
		ResponseEntity<AddressDtlRes> hasCurrentPrimaryAddressResponse = (ResponseEntity<AddressDtlRes>) handleResponse(
				configureRestCall().postForEntity(completeURI, addressRequest, AddressDtlRes.class));
		if (!ObjectUtils.isEmpty(hasCurrentPrimaryAddressResponse.getBody().getAddressDetail())
				&& WebConstants.YES.equalsIgnoreCase(
						hasCurrentPrimaryAddressResponse.getBody().getAddressDetail().getIndPersAddrLinkPrimary())) {
			return hasCurrentPrimaryAddressResponse.getBody().getAddressDetail().getIndPersAddrLinkPrimary();
		}

		return null;
	}

	/**
	 * Method Name: generateFBIClearanceEmail Method Description:This method is used
	 * to generate the FBI Clearance email.
	 *
	 * @param checkDto
	 * @param nmPerson
	 * @return notificationDto
	 */
	@SuppressWarnings("unchecked")
	public EmailNotificationDto generateFBIEligibleEmailExHire(RecordsCheckDto checkDto, String nmPerson) {
		EmailNotificationsReq request = populateGenerateFBIEligibleEmailReq(checkDto, nmPerson);
		EmailNotificationDto notificationDto = null;
		String completeURI = JNDIUtil.buildServiceUrl(WebConstants.JNDIHOSTURL,
				serviceBundle.getString(SERVICE_GENERATE_FBI_ELGB_EXHIRE_EMAIL));
		ResponseEntity<EmailNotificationsRes> fbiEmailNotificationResponse = (ResponseEntity<EmailNotificationsRes>) handleResponse(
				configureRestCall().postForEntity(completeURI, request, EmailNotificationsRes.class));
		if (!ObjectUtils.isEmpty(fbiEmailNotificationResponse)
				&& !ObjectUtils.isEmpty(fbiEmailNotificationResponse.getBody())
				&& !ObjectUtils.isEmpty(fbiEmailNotificationResponse.getBody().getEmailNotificationDto())) {
			notificationDto = fbiEmailNotificationResponse.getBody().getEmailNotificationDto();
		}

		return notificationDto;
	}

	/**
	 * Method Name: generateEligibleEmail Method Description:This method is used
	 * to generate the FBI Eligible email.
	 *
	 * @param checkDto
	 * @param nmPerson
	 * @param idPerson
	 * @return notificationDto
	 */
	@SuppressWarnings("unchecked")
	public EmailNotificationDto generateEligibleEmail(RecordsCheckDto checkDto, String nmPerson, Long idPerson) {
		EmailNotificationsReq request = populateGenerateEligibleEmailReq(checkDto, nmPerson, idPerson);
		EmailNotificationDto notificationDto = null;
		log.info("PD 92219 - Added loggers: Inside generateEligibleEmail method- background check initiated for " + nmPerson);
		String completeURI = JNDIUtil.buildServiceUrl(WebConstants.JNDIHOSTURL,
				serviceBundle.getString(SERVICE_GENERATE_ELIGIBLE_EMAIL));
			ResponseEntity<EmailNotificationsRes> fbiEmailNotificationResponse = (ResponseEntity<EmailNotificationsRes>) handleResponse(
					configureRestCall().postForEntity(completeURI, request, EmailNotificationsRes.class));
			if (!ObjectUtils.isEmpty(fbiEmailNotificationResponse)
					&& !ObjectUtils.isEmpty(fbiEmailNotificationResponse.getBody())
					&& !ObjectUtils.isEmpty(fbiEmailNotificationResponse.getBody().getEmailNotificationDto())) {
				notificationDto = fbiEmailNotificationResponse.getBody().getEmailNotificationDto();
			}
		return notificationDto;
	}

	/**
	 * Method Name: generateFBIIneligibleEmail Method Description:This method is used
	 * to generate the FBI Clearance email.
	 *
	 * @param checkDto
	 * @param nmPerson
	 * @param idPerson
	 * @return notificationDto
	 */
	@SuppressWarnings("unchecked")
	public EmailNotificationDto generateIneligibleEmail(RecordsCheckDto checkDto, String nmPerson, Long idPerson,Long idUser) {
		EmailNotificationsReq request = populateGenerateIneligibleEmailReq(checkDto, nmPerson, idPerson,idUser);
		EmailNotificationDto notificationDto = null;
		String completeURI = JNDIUtil.buildServiceUrl(WebConstants.JNDIHOSTURL,
					serviceBundle.getString(SERVICE_GENERATE_INELIGIBLE_EMAIL));
			ResponseEntity<EmailNotificationsRes> fbiEmailNotificationResponse = (ResponseEntity<EmailNotificationsRes>) handleResponse(
					configureRestCall().postForEntity(completeURI, request, EmailNotificationsRes.class));
			if (!ObjectUtils.isEmpty(fbiEmailNotificationResponse)
					&& !ObjectUtils.isEmpty(fbiEmailNotificationResponse.getBody())
					&& !ObjectUtils.isEmpty(fbiEmailNotificationResponse.getBody().getEmailNotificationDto())) {
				notificationDto = fbiEmailNotificationResponse.getBody().getEmailNotificationDto();
			}


		return notificationDto;
	}

	public EmailNotificationDto generatePCSIneligibleEmail(RecordsCheckDto checkDto, String nmPerson, Long idPerson,Long idUser) {
		EmailNotificationsReq request = populateGenerateIneligibleEmailReq(checkDto, nmPerson, idPerson,idUser);
		EmailNotificationDto notificationDto = null;
		RecordsCheckRes recordsCheckResource = getAbcsContractID(checkDto.getIdRecCheck());
		if(recordsCheckResource.getContractType().equals("PCSX"))
		{
			String completeURI = JNDIUtil.buildServiceUrl(WebConstants.JNDIHOSTURL,
					serviceBundle.getString(SERVICE_GENERATE_PCS_INELIGIBLE_EMAIL));
			ResponseEntity<EmailNotificationsRes> fbiEmailNotificationResponse = (ResponseEntity<EmailNotificationsRes>) handleResponse(
					configureRestCall().postForEntity(completeURI, request, EmailNotificationsRes.class));
			if (!ObjectUtils.isEmpty(fbiEmailNotificationResponse)
					&& !ObjectUtils.isEmpty(fbiEmailNotificationResponse.getBody())
					&& !ObjectUtils.isEmpty(fbiEmailNotificationResponse.getBody().getEmailNotificationDto())) {
				notificationDto = fbiEmailNotificationResponse.getBody().getEmailNotificationDto();
			}
		}

		return notificationDto;
	}


	/**
	 * Method Name: generatePSClearanceEmail Method Description:This method is used
	 * to generate the PS Clearance email.
	 *
	 * @param checkDto
	 * @param idPerson
	 * @return notificationDto
	 */
	@SuppressWarnings("unchecked")
	public EmailNotificationDto generatePSClearanceEmail(RecordsCheckDto checkDto, Long idPerson, Long idUser) {
		EmailNotificationsReq request = populateGeneratePSClearanceEmailReq(checkDto, idPerson,idUser);
		EmailNotificationDto notificationDto = null;
		RecordsCheckRes recordsCheckResource = getAbcsContractID(checkDto.getIdRecCheck());
		String completeURI = JNDIUtil.buildServiceUrl(WebConstants.JNDIHOSTURL,
					serviceBundle.getString(SERVICE_GENERATE_PS_CLEARANCE_EMAIL));
			ResponseEntity<EmailNotificationsRes> psEmailNotificationResponse = (ResponseEntity<EmailNotificationsRes>) handleResponse(
					configureRestCall().postForEntity(completeURI, request, EmailNotificationsRes.class));
			if (!ObjectUtils.isEmpty(psEmailNotificationResponse)
					&& !ObjectUtils.isEmpty(psEmailNotificationResponse.getBody())
					&& !ObjectUtils.isEmpty(psEmailNotificationResponse.getBody().getEmailNotificationDto())) {
				notificationDto = psEmailNotificationResponse.getBody().getEmailNotificationDto();
			}
		return notificationDto;
	}

	public EmailNotificationDto generatePCSEligibleEmail(RecordsCheckDto checkDto, Long idPerson, Long idUser) {
		EmailNotificationsReq request = populateGeneratePSClearanceEmailReq(checkDto, idPerson,idUser);
		EmailNotificationDto notificationDto = null;
		RecordsCheckRes recordsCheckResource = getAbcsContractID(checkDto.getIdRecCheck());
		if(recordsCheckResource.getContractType().equals("PCSX"))
		{
			String completeURI = JNDIUtil.buildServiceUrl(WebConstants.JNDIHOSTURL,
					serviceBundle.getString(SERVICE_GENERATE_PCS_ELIGIBLE_EMAIL));
			ResponseEntity<EmailNotificationsRes> psEmailNotificationResponse = (ResponseEntity<EmailNotificationsRes>) handleResponse(
					configureRestCall().postForEntity(completeURI, request, EmailNotificationsRes.class));
			if (!ObjectUtils.isEmpty(psEmailNotificationResponse)
					&& !ObjectUtils.isEmpty(psEmailNotificationResponse.getBody())
					&& !ObjectUtils.isEmpty(psEmailNotificationResponse.getBody().getEmailNotificationDto())) {
				notificationDto = psEmailNotificationResponse.getBody().getEmailNotificationDto();
			}
		}
		return notificationDto;
	}

	/**
	 * Method Name: sendEmailWithConfirmation Method Description:This method is used
	 * to send an email when the Records check is Saved and Completed.
	 *
	 * @param emailNotificationDto
	 * @return indEmailSent
	 */
	public boolean sendEmailWithConfirmation(EmailNotificationDto emailNotificationDto, String hostName) throws MessagingException {
		List<String> emailToList = new ArrayList<String>();
		try {
			// Check if the environment is prod , then set the correct emailId
			// else set the
			// test emailId
			emailToList.add(emailNotificationDto.getRecipientTo());
			emailNotificationDto.setEmailToList(emailToList);
			emailUtility.sendEmail(emailNotificationDto, RECORDS_CHECK_EMAIL_CONFIG_BASE, WebConstants.EMPTY_STRING,
					hostName);
		} catch (Exception e) {
			log.error("sendEmailWithConfirmation Exception occurred",e);
			throw e;
		}
		return true;
	}

	/**
	 * Method Name: hasEmailSent Method Description:This method is used to check if
	 * the email is sent while the Records Check is Saved and Completed.
	 *
	 * @param idRecCheck
	 * @return indEmailSent
	 */
	@SuppressWarnings("unchecked")
	public boolean hasEmailSent(Long idRecCheck) {
		boolean indEmailSent = false;
		String completeURI = JNDIUtil.buildServiceUrl(WebConstants.JNDIHOSTURL,
				serviceBundle.getString(SERVICE_HAS_EMAIL_SENT));
		RecordsCheckReq recordsCheckRequest = new RecordsCheckReq();
		recordsCheckRequest.setIdRecCheck(idRecCheck);
		String hasEmailSentInd = null;

		ResponseEntity<RecordsCheckRes> hasEmailSentResponse = (ResponseEntity<RecordsCheckRes>) handleResponse(
				configureRestCall().postForEntity(completeURI, recordsCheckRequest, RecordsCheckRes.class));

		if (!ObjectUtils.isEmpty(hasEmailSentResponse)) {
			hasEmailSentInd = hasEmailSentResponse.getBody().getIndEmailSent();
			if (!ObjectUtils.isEmpty(hasEmailSentInd) && hasEmailSentInd.equalsIgnoreCase(WebConstants.YES)) {
				indEmailSent = true;
			}
		}
		return indEmailSent;
	}

	/**
	 * Method Name: setCompletedEmailFlag Method Description:This method is used to
	 * save the Records Check detail with the completed email indicator.
	 *
	 * @param emailFlagReq
	 * @return recordsCheckListResponse
	 */
	public RecordsCheckListRes setCompletedEmailFlag(RecordsCheckDetailReq emailFlagReq) {

		// Call REST service for saving a record check detail
		String completeUrl = JNDIUtil.buildServiceUrl(WebConstants.JNDIHOSTURL,
				serviceBundle.getString(SERVICE_RECORDSCHECK_AUD));
		ResponseEntity<RecordsCheckListRes> response = (ResponseEntity<RecordsCheckListRes>) handleResponse(
				configureRestCall().postForEntity(completeUrl, emailFlagReq, RecordsCheckListRes.class));
		return response.getBody();
	}

	/**
	 * Method Name: getRecordDocumentTsLastUpdate Method Description:This method is
	 * used to get the last updated date of a document which is used to displayed on
	 * Records Check detail.
	 *
	 * @param idRecCheck
	 * @return dtLastUpdate
	 */
	public Date getRecordDocumentTsLastUpdate(Long idRecCheck) {
		RecordsCheckReq recordsCheckRequest = new RecordsCheckReq();
		recordsCheckRequest.setIdRecCheck(idRecCheck);
		Date dtLastUpdate = null;
		String completeURI = JNDIUtil.buildServiceUrl(WebConstants.JNDIHOSTURL,
				serviceBundle.getString(SERVICE_GET_LAST_UPDATE_DATE_FOR_DOCUMENT));

		@SuppressWarnings("unchecked")
		ResponseEntity<RecordsCheckRes> lastUpdateResponse = (ResponseEntity<RecordsCheckRes>) handleResponse(
				configureRestCall().postForEntity(completeURI, recordsCheckRequest, RecordsCheckRes.class));

		if (!ObjectUtils.isEmpty(lastUpdateResponse) && !ObjectUtils.isEmpty(lastUpdateResponse.getBody())
				&& !ObjectUtils.isEmpty(lastUpdateResponse.getBody().getDocumentLastUpdate())) {
			dtLastUpdate = lastUpdateResponse.getBody().getDocumentLastUpdate();

		}
		return dtLastUpdate;
	}

	/**
	 * Method Name: deleteDocumentPdbRecord Method Description:This method is used
	 * to delete a document .
	 *
	 * @param request
	 */
	@SuppressWarnings("unchecked")
	public void deleteDocumentPdbRecord(RecordsCheckDetailReq request) {
		String completeURI = JNDIUtil.buildServiceUrl(WebConstants.JNDIHOSTURL,
				serviceBundle.getString(SERVICE_DELETE_DOCUMENT));

		ResponseEntity<RecordsCheckRes> deleteDocumentResponse = (ResponseEntity<RecordsCheckRes>) handleResponse(
				configureRestCall().postForEntity(completeURI, request, RecordsCheckRes.class));
		if (!ObjectUtils.isEmpty(deleteDocumentResponse) && !ObjectUtils.isEmpty(deleteDocumentResponse.getBody())
				&& !ObjectUtils.isEmpty(deleteDocumentResponse.getBody().getErrorDto())) {

			throw new WebException("Error occured while deleting the document from the repository");
		}

	}

	/**
	 * Method Name: populateRecordsCheckDetailSaveRequest Method Description: This
	 * method is used to populate the Request for Save/Delete Records Check
	 *
	 * @param checkDto
	 * @param recordsCheckDtoList
	 * @param commonData
	 * @return
	 */
	@SuppressWarnings("unchecked")
	private RecordsCheckDetailReq populateRecordsCheckDetailSaveRequest(RecordsCheckDto checkDto,
			List<RecordsCheckDto> recordsCheckDtoList, CommonDto commonData) {
		String cancelText = CANCELLED;
		String completedText = COMPLETED;
		int commentMaxLength = 4000;
		String cdSearchType = !ObjectUtils.isEmpty(checkDto.getRecCheckCheckType()) ? checkDto.getRecCheckCheckType()
				: null;

		List<RecordsCheckDto> recordsCheckList = recordsCheckDtoList;
		RecordsCheckDto existingRecord = null;
		int size = 0;
		size = IntStream.range(0, recordsCheckList.size())
				.filter(i -> checkDto.getIdRecCheck().equals(recordsCheckList.get(i).getIdRecCheck())).findFirst()
				.orElse(-1);
		if (size != -1) {
			existingRecord = recordsCheckList.stream()
					.filter(recordsCheck -> recordsCheck.getIdRecCheck().equals(checkDto.getIdRecCheck())).findAny()
					.orElse(null);
		}

		// create the input request
		RecordsCheckDetailReq request = new RecordsCheckDetailReq();
		List<RecordsCheckDetailDto> recordsDetailList = new ArrayList<RecordsCheckDetailDto>();
		RecordsCheckDetailDto recordsCheckDetailDto = new RecordsCheckDetailDto();
		request.setUserId(commonData.getIdUserLogon());

		boolean calledFromJavascript = false;
		String pageMode = checkDto.getPageModeStr();
		// If it is a new Records Check detail Save
		if (!ObjectUtils.isEmpty(pageMode) && pageMode.equalsIgnoreCase(NEW) || calledFromJavascript) {
			request.setReqFuncCd(WebConstants.REQ_FUNC_CD_ADD);
			recordsCheckDetailDto.setScrDataAction(WebConstants.ACTION_A);
			recordsCheckDetailDto.setIdRecCheckRequestor(commonData.getIdUser());
			recordsCheckDetailDto.setIdStage(commonData.getIdStage());
			if (!ObjectUtils.isEmpty(cdSearchType)) {
				recordsCheckDetailDto.setRecCheckCheckType(cdSearchType);
			}
			if (!ObjectUtils.isEmpty(checkDto.getDtRecCheckCompleted())) {
				recordsCheckDetailDto.setDtRecCheckCompleted(checkDto.getDtRecCheckCompleted());
			}
			if (!ObjectUtils.isEmpty(checkDto.getDtRecCheckRequest())) {
				recordsCheckDetailDto.setDtRecCheckRequest(checkDto.getDtRecCheckRequest());
			}
			if (!ObjectUtils.isEmpty(checkDto.getRecCheckComments())) {
				recordsCheckDetailDto.setRecCheckComments(checkDto.getRecCheckComments());
			}

			recordsCheckDetailDto.setIndClearedEmail(WebConstants.NO);

			String reviewType = !ObjectUtils.isEmpty(checkDto.getIndReviewNow()) ? checkDto.getIndReviewNow() : null;
			if (!ObjectUtils.isEmpty(reviewType) && !ObjectUtils.isEmpty(cdSearchType)
					&& CodesConstant.CCHKTYPE_10.equals(cdSearchType)) {
				String indReview = WebConstants.EMPTY_STRING;
				if (REVIEW_NOW.equalsIgnoreCase(reviewType)) {
					indReview = WebConstants.YES;
				} else if (REVIEW_LATER.equalsIgnoreCase(reviewType)) {
					indReview = WebConstants.NO;
				}
				recordsCheckDetailDto.setIndReviewNow(indReview);
			}

			Map<String, String> phoneMap = new HashMap<String, String>();
			if (!ObjectUtils.isEmpty(checkDto.getPhoneListStr()))
				phoneMap = (Map<String, String>) jsonToMap(checkDto.getPhoneListStr());
			Map<String, String> emailMap = new HashMap<String, String>();
			if (!ObjectUtils.isEmpty(checkDto.getPhoneListStr()))
				emailMap = (Map<String, String>) jsonToMap(checkDto.getEmailListStr());
			String phoneNumber = WebConstants.EMPTY_STRING;
			String emailAddress = WebConstants.EMPTY_STRING;
			if (!ObjectUtils.isEmpty(checkDto.getPhoneNumber())) {
				phoneNumber = FormattingHelper.decodeFormattedPhoneString(phoneMap.get(checkDto.getPhoneNumber()));
			}
			if (!ObjectUtils.isEmpty(checkDto.getEmail())) {
				emailAddress = emailMap.get(checkDto.getEmail());
			}
			String contactType = !ObjectUtils.isEmpty(checkDto.getRecCheckContMethod())
					? checkDto.getRecCheckContMethod()
					: null;
			String contactMethodValue = WebConstants.EMPTY_STRING;
			if (!ObjectUtils.isEmpty(cdSearchType) && CodesConstant.CCHKTYPE_80.equals(cdSearchType)) {

				String cdContactMethod = WebConstants.EMPTY_STRING;
				if (!ObjectUtils.isEmpty(contactType) && EML_CODE.equalsIgnoreCase(contactType)) {
					cdContactMethod = CodesConstant.FBIMTHD_EML; // EML
					contactMethodValue = emailAddress;
				} else if (!ObjectUtils.isEmpty(contactType) && PHN_CODE.equalsIgnoreCase(contactType)) {
					cdContactMethod = CodesConstant.FBIMTHD_PHN; // PHN
					contactMethodValue = phoneNumber;
				}
				recordsCheckDetailDto.setRecCheckContMethod(cdContactMethod);
				recordsCheckDetailDto.setRecCheckContMethodValue(contactMethodValue);
			}
			request.setPageSizeNbr(1);
		}

		else {
			request.setReqFuncCd(WebConstants.REQ_FUNC_CD_UPDATE);
			recordsCheckDetailDto.setScrDataAction(WebConstants.ACTION_U);
			if (!ObjectUtils.isEmpty(checkDto)) {

				if (!ObjectUtils.isEmpty(checkDto.getDtLastUpdate())) {
					recordsCheckDetailDto.setDtLastUpdate(checkDto.getDtLastUpdate());
				}
				if (!ObjectUtils.isEmpty(checkDto.getIdRecCheck()) && checkDto.getIdRecCheck() != 0l) {
					recordsCheckDetailDto.setIdRecCheck(checkDto.getIdRecCheck());
				}
				if (!ObjectUtils.isEmpty(checkDto.getIdRecCheckRequestor())
						&& checkDto.getIdRecCheckRequestor() != 0l) {
					recordsCheckDetailDto.setIdRecCheckRequestor(checkDto.getIdRecCheckRequestor());
				}
				if (!ObjectUtils.isEmpty(checkDto.getIdStage()) && checkDto.getIdStage() != 0l) {
					recordsCheckDetailDto.setIdStage(checkDto.getIdStage());
				} else {
					recordsCheckDetailDto.setIdStage(0l);
				}

				if (!ObjectUtils.isEmpty(checkDto.getRecCheckEmpType())) {
					recordsCheckDetailDto.setRecCheckEmpType(checkDto.getRecCheckEmpType());
				}

				if (!ObjectUtils.isEmpty(checkDto.getRecCheckStatus())) {
					recordsCheckDetailDto.setRecCheckStatus(checkDto.getRecCheckStatus());
				}

				if (!ObjectUtils.isEmpty(checkDto.getDtClearedEmailSent())) {
					recordsCheckDetailDto.setDtClearedEmailSent(checkDto.getDtClearedEmailSent());
				}
				if (!ObjectUtils.isEmpty(checkDto.getDtDetermFinal())) {
					recordsCheckDetailDto.setDtDetermFinal(checkDto.getDtDetermFinal());
				}
				if (!ObjectUtils.isEmpty(checkDto.getDtClrdEmailRequested())) {
					recordsCheckDetailDto.setDtClrdEmailRequested(checkDto.getDtClrdEmailRequested());
				}
				String reviewType = !ObjectUtils.isEmpty(checkDto.getIndReviewNow()) ? checkDto.getIndReviewNow()
						: null;
				if (!ObjectUtils.isEmpty(reviewType) && !ObjectUtils.isEmpty(cdSearchType)
						&& CodesConstant.CCHKTYPE_10.equals(cdSearchType)) {
					String indReview = WebConstants.EMPTY_STRING;
					if (REVIEW_NOW.equalsIgnoreCase(reviewType)) {
						indReview = WebConstants.YES;
					} else if (REVIEW_LATER.equalsIgnoreCase(reviewType)) {
						indReview = WebConstants.NO;
					}
					recordsCheckDetailDto.setIndReviewNow(indReview);
				}

			}
			if (!ObjectUtils.isEmpty(cdSearchType)) {
				recordsCheckDetailDto.setRecCheckCheckType(cdSearchType);
			}
			if (!ObjectUtils.isEmpty(checkDto.getDtRecCheckRequest())) {
				recordsCheckDetailDto.setDtRecCheckRequest(checkDto.getDtRecCheckRequest());
			}
			if (!ObjectUtils.isEmpty(checkDto.getDtRecCheckCompleted())) {
				recordsCheckDetailDto.setDtRecCheckCompleted(checkDto.getDtRecCheckCompleted());
			}
			recordsCheckDetailDto.setRecCheckComments(checkDto.getRecCheckComments());
			if (!ObjectUtils.isEmpty(checkDto.getIndClearedEmail())) {
				recordsCheckDetailDto.setIndClearedEmail(checkDto.getIndClearedEmail());
			}
			if (!ObjectUtils.isEmpty(checkDto.getCdRapBackReview())) {
				recordsCheckDetailDto.setCdRapBackReview(checkDto.getCdRapBackReview());
			}
			if (!ObjectUtils.isEmpty(checkDto.getCdRapBackReview()) &&
					CodesConstant.RBRVW_COMP.equalsIgnoreCase(checkDto.getCdRapBackReview())) {
				recordsCheckDetailDto.setDtRecCheckCompleted(new Date());
				recordsCheckDetailDto.setDtRapBackRecordReviewed(new Date());
			}

			recordsCheckDetailDto.setDtDetermFinal(checkDto.getDtDetermFinal());
			recordsCheckDetailDto.setRecChkDeterm(checkDto.getRecChkDeterm());
			recordsCheckDetailDto.setDtClrdEmailRequested(checkDto.getDtClrdEmailRequested());

			if (!ObjectUtils.isEmpty(cdSearchType) && CodesConstant.CCHKTYPE_10.equals(cdSearchType) || // DPS
																										// Criminal
																										// History
					CodesConstant.CCHKTYPE_75.equals(cdSearchType) || // FPS
																		// History
																		// Check-BATCH
					CodesConstant.CCHKTYPE_80.equals(cdSearchType)) // FBI
																	// FINGERPRINT
			{
				if (!ObjectUtils.isEmpty(checkDto.getRecChkDeterm())
						&& CodesConstant.CDETERM_REEL.equals(checkDto.getRecChkDeterm())) {
					recordsCheckDetailDto.setIndComplete(WebConstants.NO);
				} else {
					recordsCheckDetailDto.setIndComplete(WebConstants.YES);
				}

				if (!ObjectUtils.isEmpty(checkDto.getRecChkDeterm()) && CodesConstant.CCHKTYPE_80.equals(cdSearchType)){
					if(ObjectUtils.isEmpty(checkDto.getDtRecCheckCompleted()) && (CodesConstant.CDETERM_BAR.equals(checkDto.getRecChkDeterm())
							|| CodesConstant.CDETERM_BRDN.equals(checkDto.getRecChkDeterm())
							|| CodesConstant.CDETERM_BRNR.equals(checkDto.getRecChkDeterm())
							|| CodesConstant.CDETERM_CLAP.equals(checkDto.getRecChkDeterm())
							|| CodesConstant.CDETERM_CLER.equals(checkDto.getRecChkDeterm())
							|| CodesConstant.CDETERM_NOTA.equals(checkDto.getRecChkDeterm())
							|| CodesConstant.CDETERM_ELGB.equals(checkDto.getRecChkDeterm())
							|| CodesConstant.CDETERM_INEG.equals(checkDto.getRecChkDeterm()))){
						recordsCheckDetailDto.setDtRecCheckCompleted(new Date());
					}
					else
						recordsCheckDetailDto.setDtRecCheckCompleted(null);
				}
				if (!ObjectUtils.isEmpty(checkDto.getRecChkDeterm()) && CodesConstant.CCHKTYPE_75.equals(cdSearchType)){
					if(ObjectUtils.isEmpty(checkDto.getDtRecCheckCompleted()) && (CodesConstant.CDETERM_BAR.equals(checkDto.getRecChkDeterm())
							|| CodesConstant.CDETERM_CLER.equals(checkDto.getRecChkDeterm())
							|| CodesConstant.CDETERM_NOTA.equals(checkDto.getRecChkDeterm())
							|| CodesConstant.CDETERM_ELGB.equals(checkDto.getRecChkDeterm())
							|| CodesConstant.CDETERM_INEG.equals(checkDto.getRecChkDeterm()))){
						recordsCheckDetailDto.setDtRecCheckCompleted(new Date());
					}
					else
						recordsCheckDetailDto.setDtRecCheckCompleted(null);
				}
				if (!ObjectUtils.isEmpty(checkDto.getRecChkDeterm()) && CodesConstant.CCHKTYPE_75.equals(cdSearchType)){
					if(CodesConstant.CDETERM_BAR.equals(checkDto.getRecChkDeterm())
							|| CodesConstant.CDETERM_CLER.equals(checkDto.getRecChkDeterm())
							|| CodesConstant.CDETERM_NOTA.equals(checkDto.getRecChkDeterm())
							|| CodesConstant.CDETERM_ELGB.equals(checkDto.getRecChkDeterm())
							|| CodesConstant.CDETERM_INEG.equals(checkDto.getRecChkDeterm())){
						recordsCheckDetailDto.setDtRecCheckCompleted(new Date());
					}
				}
				if (!ObjectUtils.isEmpty(checkDto.getRecChkDeterm())) {
					if (!ObjectUtils.isEmpty(recordsCheckDetailDto.getRecChkDeterm())
							&& !ObjectUtils.isEmpty(existingRecord)
							&& !recordsCheckDetailDto.getRecChkDeterm().equals(existingRecord.getRecChkDeterm())) {
						recordsCheckDetailDto.setRecChkDeterm(checkDto.getRecChkDeterm());

						if (!ObjectUtils.isEmpty(recordsCheckDetailDto.getRecChkDeterm())
								&& !CodesConstant.CDETERM_REEL.equals(recordsCheckDetailDto.getRecChkDeterm())
								&& !CodesConstant.CDETERM_POSS.equals(recordsCheckDetailDto.getRecChkDeterm())) {

							recordsCheckDetailDto.setDtDetermFinal(new Date());
						}
					}
				}
				recordsCheckDetailDto.setIdPerson(commonData.getIdUser());
			}

			if (!ObjectUtils.isEmpty(checkDto.getRecCheckCheckType())
					&& (CodesConstant.CCHKTYPE_80.equals(checkDto.getRecCheckCheckType()) || CodesConstant.CCHKTYPE_81.equals(checkDto.getRecCheckCheckType()))) {
				/* Code changes for artf172946 */
				if (!ObjectUtils.isEmpty(checkDto) && !ObjectUtils.isEmpty(checkDto.getIndCrimHistoryResultCopied())) {
					recordsCheckDetailDto.setIndCrimHistResultCopied(checkDto.getIndCrimHistoryResultCopied());
					if(checkDto.getIndCrimHistoryResultCopied().equals(WebConstants.YES)){
						recordsCheckDetailDto.setTxtDpsSID(checkDto.getTxtDpsSID());
					}
				}else {
					recordsCheckDetailDto.setTxtDpsSID(null);
				}
				/* End of code changes for artf172946 */
				/* Code changes for artf172943 */
				if(CodesConstant.CDETERM_MHPA.equals(recordsCheckDetailDto.getRecChkDeterm())
						|| CodesConstant.CDETERM_PNRS.equals(recordsCheckDetailDto.getRecChkDeterm())
						|| CodesConstant.CDETERM_PFRBB.equals(recordsCheckDetailDto.getRecChkDeterm())
						|| CodesConstant.CDETERM_PFRBC.equals(recordsCheckDetailDto.getRecChkDeterm())){
					recordsCheckDetailDto.setDtDetermFinal(null);
				}
				/* End of code changes for artf172943 */
				if (!ObjectUtils.isEmpty(checkDto) && !ObjectUtils.isEmpty(checkDto.getPhoneNumber())) {
					recordsCheckDetailDto.setRecCheckContMethod(PHN_CODE);
					recordsCheckDetailDto.setRecCheckContMethodValue(checkDto.getPhoneNumber());
				} else if (!ObjectUtils.isEmpty(checkDto) && !ObjectUtils.isEmpty(checkDto.getEmail())) {
					recordsCheckDetailDto.setRecCheckContMethod(EML_CODE);
					recordsCheckDetailDto.setRecCheckContMethodValue(checkDto.getEmail());
				}

				if (!ObjectUtils.isEmpty(checkDto) && !ObjectUtils.isEmpty(checkDto.getCdRecCheckServ())) {
					recordsCheckDetailDto.setCdRecCheckServ(checkDto.getCdRecCheckServ());
				} else {
					recordsCheckDetailDto.setCdRecCheckServ(WebConstants.EMPTY_STRING);
				}

				if(!ObjectUtils.isEmpty(checkDto) &&(!ObjectUtils.isEmpty(checkDto.getIndRapBackSubscriptionCopied()))){
					recordsCheckDetailDto.setIndRapBackSubscriptionCopied(checkDto.getIndRapBackSubscriptionCopied());
					recordsCheckDetailDto.setCdORIAccntNum(checkDto.getCdORIAccntNum());
					recordsCheckDetailDto.setDtRapBackExp(checkDto.getDtRapBackExp());
				}

			}

			// artf51666 - changed from CCHKTYPE_15 to CCHKTYPE_95
			if (!ObjectUtils.isEmpty(checkDto.getRecCheckCheckType())
					&& CodesConstant.CCHKTYPE_95.equals(checkDto.getRecCheckCheckType())) {
				if (!ObjectUtils.isEmpty(checkDto) && !ObjectUtils.isEmpty(checkDto.getPhoneNumber())) {
					recordsCheckDetailDto.setRecCheckContMethod(PHN_CODE);

				} else if (!ObjectUtils.isEmpty(checkDto) && !ObjectUtils.isEmpty(checkDto.getEmail())) {
					recordsCheckDetailDto.setRecCheckContMethod(EML_CODE);

				}

			}
			if (!ObjectUtils.isEmpty(checkDto.getRecCheckCheckType())
					&& CodesConstant.CCHKTYPE_80.equals(checkDto.getRecCheckCheckType())) {
				String cancelReason = null;

				if (!ObjectUtils.isEmpty(checkDto.getCancelReason())) {
					cancelReason = checkDto.getCancelReason();
				}
				if ((CodesConstant.CCRIMSTA_ZA).equals(cancelReason)) {
					cancelText = completedText;
				}
				if (!ObjectUtils.isEmpty(cancelReason)
						&& !ObjectUtils.isEmpty(cacheAdapter.getDecode(CodesConstant.CCRIMSTA, cancelReason))) {
					Date wkDate = new Date();
					recordsCheckDetailDto.setDtRecCheckCompleted(new Date());
					recordsCheckDetailDto.setRecCheckStatus(cancelReason);
					recordsCheckDetailDto.setDtDetermFinal(new Date()); //Added for artf172945
					recordsCheckDetailDto.setFbiCancelReason(cacheAdapter.getDecode(CodesConstant.CCRIMSTA, cancelReason)); //Added for artf172945
					recordsCheckDetailDto.setIdCancelledPerson(commonData.getIdUser()); //Added for artf172945
					recordsCheckDetailDto.setRecChkDeterm(CodesConstant.CDETERM_NOTA); // Added for artf172945
					StringBuffer cancelReasonBufferString = new StringBuffer();
					cancelReasonBufferString.append(cancelText).append("  ");
					cancelReasonBufferString.append(cacheAdapter.getDecode(CodesConstant.CCRIMSTA, cancelReason));
					cancelReasonBufferString.append(" by ").append(commonData.getNmUserFullName());
					cancelReasonBufferString.append(" on ").append(wkDate).append(".  ");
					if (!ObjectUtils.isEmpty(checkDto.getRecCheckComments())
							&& commentMaxLength >= checkDto.getRecCheckComments().length()
									+ cancelReasonBufferString.length()) {
						cancelReasonBufferString.append(checkDto.getRecCheckComments());
					} else {
						cancelReasonBufferString = new StringBuffer();
						if (!ObjectUtils.isEmpty(checkDto.getRecCheckComments())
								&& commentMaxLength > (checkDto.getRecCheckComments().length()
										+ cacheAdapter.getDecode(CodesConstant.CCRIMSTA, cancelReason).length() + 2
										+ commonData.getNmUserFullName().length() + 4 + cancelText.length())) {
							// Prepend Cancel text, cancel reason & user if
							// comment too long for date
							cancelReasonBufferString.append(cancelText);
							cancelReasonBufferString
									.append(cacheAdapter.getDecode(CodesConstant.CCRIMSTA, cancelReason));
							cancelReasonBufferString.append(" by ").append(commonData.getNmUserFullName());
							cancelReasonBufferString.append(". ");
							cancelReasonBufferString.append(checkDto.getRecCheckComments());
						} else if (!ObjectUtils.isEmpty(checkDto.getRecCheckComments())
								&& commentMaxLength > (checkDto.getRecCheckComments().length()
										+ cacheAdapter.getDecode(CodesConstant.CCRIMSTA, cancelReason).length() + 2
										+ cancelText.length())) {
							// Prepend Cancel text and cancel reason if comment
							// too long for user & date
							cancelReasonBufferString.append(cancelText);
							cancelReasonBufferString
									.append(cacheAdapter.getDecode(CodesConstant.CCRIMSTA, cancelReason));
							cancelReasonBufferString.append(". ");
							cancelReasonBufferString.append(checkDto.getRecCheckComments());
						} else if (!ObjectUtils.isEmpty(checkDto.getRecCheckComments())
								&& commentMaxLength > checkDto.getRecCheckComments().length() + cancelText.length()) {
							// Prepend Cancel text if comment too long for user,
							// date & Cancel reason
							cancelReasonBufferString.append(cancelText);
							cancelReasonBufferString.append(checkDto.getRecCheckComments());
						} else {
							//artf185299:  Prepend Cancel text to first 3990 instead of 990 characters of
							// comment if too long.
							cancelReasonBufferString.append(cancelText);
							if (!ObjectUtils.isEmpty(checkDto.getRecCheckComments())) {
								cancelReasonBufferString.append(checkDto.getRecCheckComments().substring(0, 3990));
							} else {
								cancelReasonBufferString.append("");
							}

						}

					}
					recordsCheckDetailDto.setRecCheckComments(cancelReasonBufferString.toString());
					cancelReasonBufferString = null;
				}
				// begin of unsubscribe
				if (!ObjectUtils.isEmpty(checkDto.getIndRapBackUnSubScrType())
						&& checkDto.getIndRapBackUnSubScrType().equals("I")
						&& !ObjectUtils.isEmpty(checkDto.getDtRapBackUnSubrReq())) {
					recordsCheckDetailDto.setDtRapBackUnSubReq(checkDto.getDtRapBackUnSubrReq());
					recordsCheckDetailDto.setIndRapBackUnSubScrType(checkDto.getIndRapBackUnSubScrType());
					recordsCheckDetailDto.setDisableUnsubcribeOptions(true);
				}
				if (!ObjectUtils.isEmpty(checkDto.getIndRapBackUnSubScrType())
						&& checkDto.getIndRapBackUnSubScrType().equals("D")
						&& !ObjectUtils.isEmpty(checkDto.getDtRapBackUnSubscribed())) {
					recordsCheckDetailDto.setDtRapBackUnsubscribed(checkDto.getDtRapBackUnSubscribed());
					recordsCheckDetailDto.setCdFbiSubscriptionStatus(WebConstants.UNS);
					recordsCheckDetailDto.setDtRapBackExp(null);
					recordsCheckDetailDto.setIndRapBackUnSubScrType(checkDto.getIndRapBackUnSubScrType());
					recordsCheckDetailDto.setDisableUnsubcribeOptions(true);
				}

				// end of unsubscribe
			}
		}

		request.setPageSizeNbr(1);
		recordsDetailList.add(recordsCheckDetailDto);
		request.setRecordsCheckDtoList(recordsDetailList);
		request.setIdRecCheckPerson(commonData.getIdPerson());
		return request;
	}

	/**
	 * Method Name: populateGeneratePSClearanceEmailReq Method Description:This
	 * method is used to populate the Request for PS Clearance Email.
	 *
	 * @param checkDto
	 * @param idPeron
	 * @return emailNotificationsRequest
	 */
	private EmailNotificationsReq populateGeneratePSClearanceEmailReq(RecordsCheckDto checkDto, Long idPeron,Long idUser) {
		EmailNotificationsReq emailNotificationsRequest = new EmailNotificationsReq();
		emailNotificationsRequest.setPersonId(idPeron);
		emailNotificationsRequest.setRecCheckRequestDate(checkDto.getDtRecCheckRequest());
		emailNotificationsRequest.setRecordCheckId(checkDto.getIdRecCheck());
		emailNotificationsRequest.setRecordCheckType(checkDto.getRecCheckCheckType());
		emailNotificationsRequest.setuserId(idUser);
		return emailNotificationsRequest;
	}

	/**
	 * Method Name: populateGenerateFBIClearanceEmailReq Method Description:This
	 * method is used to populate the Request for FBI Clearance Email.
	 *
	 * @param checkDto
	 * @param nmPerson
	 * @return emailNotificationsRequest
	 */
	private EmailNotificationsReq populateGenerateFBIEligibleEmailReq(RecordsCheckDto checkDto, String nmPerson) {
		EmailNotificationsReq emailNotificationsRequest = new EmailNotificationsReq();
		emailNotificationsRequest.setApplicantName(nmPerson);
		emailNotificationsRequest.setRecCheckRequestDate(checkDto.getDtRecCheckRequest());
		emailNotificationsRequest.setRecordCheckId(checkDto.getIdRecCheck());
		return emailNotificationsRequest;
	}

	/**
	 * Method Name: populateGenerateEligibleEmailReq Method Description:This
	 * method is used to populate the Request for FBI Clearance Email.
	 *
	 * @param checkDto
	 * @param nmPerson
	 * @param idPerson
	 * @return emailNotificationsRequest
	 */
	private EmailNotificationsReq populateGenerateEligibleEmailReq(RecordsCheckDto checkDto, String nmPerson, Long idPerson) {
		EmailNotificationsReq emailNotificationsRequest = new EmailNotificationsReq();
		emailNotificationsRequest.setPersonId(idPerson);
		emailNotificationsRequest.setApplicantName(nmPerson);
		emailNotificationsRequest.setRecCheckRequestDate(checkDto.getDtRecCheckRequest());
		emailNotificationsRequest.setRecordCheckId(checkDto.getIdRecCheck());
		emailNotificationsRequest.setRecordCheckType(checkDto.getRecCheckCheckType());
		return emailNotificationsRequest;
	}

	/**
	 * Method Name: populateGenerate
	 * IneligibleEmailReq Method Description:This
	 * method is used to populate the Request for FBI Clearance Email.
	 *
	 * @param checkDto
	 * @param nmPerson
	 * @param idPerson
	 * @return emailNotificationsRequest
	 */
	private EmailNotificationsReq populateGenerateIneligibleEmailReq(RecordsCheckDto checkDto, String nmPerson, Long idPerson, Long idUser) {
		EmailNotificationsReq emailNotificationsRequest = new EmailNotificationsReq();
		emailNotificationsRequest.setApplicantName(nmPerson);
		emailNotificationsRequest.setPersonId(idPerson);
		emailNotificationsRequest.setRecCheckRequestDate(checkDto.getDtRecCheckRequest());
		emailNotificationsRequest.setRecordCheckId(checkDto.getIdRecCheck());
		emailNotificationsRequest.setRecordCheckType(checkDto.getRecCheckCheckType());
		emailNotificationsRequest.setuserId(idUser);
		return emailNotificationsRequest;
	}


	/**
	 * Method Name: jsonToMap Method Description:This method is used to convert a
	 * json string to Map.
	 *
	 * @param inputJson
	 * @return resultMap
	 */
	private Object jsonToMap(String inputJson) {
		Map<Object, Object> resultMap = new HashMap<>();
		ObjectMapper mapper = new ObjectMapper();
		try {
			resultMap = mapper.readValue(inputJson, new TypeReference<HashMap<String, String>>() {
			});
		} catch (IOException ioExp) {
		}

		return resultMap;
	}

	/**
	 * Method Name: populateGenerateFBIClearanceEmailReq Method Description:This
	 * method is used to create the model for displaying the Records Check screen.
	 *
	 * @param recordsCheckDto
	 * @param recordsBusinessDelegate
	 * @param response
	 * @return returnMap
	 */
	public Map<String, Object> createModelForDisplayRecordsCheck(RecordsCheckDto recordsCheckDto,
			RecordsCheckBusinessDelegateDto recordsBusinessDelegate, RecordsCheckListRes response) {
		Map<String, Object> returnMap = new HashMap<String, Object>();
		String docExists = WebConstants.STRING_FALSE;
		String informationMessage = recordsBusinessDelegate.getInformationMessage();
		boolean indModify = false;
		boolean indRowCanBeDeleted = false;
		String indReviewNowLaterString = recordsBusinessDelegate.getIndReviewNowLaterString();
		boolean indReviewNowLater = recordsBusinessDelegate.isIndReviewNowLater();
		boolean isShowEligibleEmailButton = recordsCheckDto.isShowEligibleEmailButton();
		boolean isDisabledEmailButton = recordsCheckDto.isDisableEmailButton();
		boolean isShowInEligibleEmailButton = recordsCheckDto.isShowIneligibleEmailButton();

		Long idRecCheck = 0l;
		String nmRequested = WebConstants.EMPTY_STRING;
		String pageMode = recordsBusinessDelegate.getPageMode();
		Long idPerson = 0l;
		long idLoggedInUser = recordsBusinessDelegate.getIdUserLogon();
		long idStage = recordsBusinessDelegate.getIdStage();
		String recordsCheckDetermList = recordsCheckDto.getRecordsCheckDeterminationListStr();
		recordsCheckDto.setPersonType(recordsBusinessDelegate.getPersonType());
		indModify = recordsBusinessDelegate.isIndModifyFlag();
		idRecCheck = recordsBusinessDelegate.getIdRecCheck();
		nmRequested = recordsBusinessDelegate.getNmRequested();
		idPerson = recordsBusinessDelegate.getIdPerson();
		indRowCanBeDeleted = recordsBusinessDelegate.isIndRowCanBeDeleted();
		if (!ObjectUtils.isEmpty(idRecCheck) && idRecCheck!= 0l) {
			// this is passed from the Records Check List page, or reloaded from
			// this page in case of a validation error.

			recordsCheckDto = getRecordsCheckDetail(idRecCheck);
			recordsCheckDto.setDtLastUpdateStr(recordsBusinessDelegate.getDtLastUpdateStr());
			recordsCheckDto.setDtLastUpdate(recordsBusinessDelegate.getDtLastUpdate());
			recordsCheckDto.setNmRequestedBy(nmRequested);
			recordsCheckDto.setRowCanBeDeleted(indRowCanBeDeleted);

			if (!ObjectUtils.isEmpty(recordsCheckDetermList)) {
				recordsCheckDto.setRecordsCheckDetermination(
						(List<RecordsCheckDeterminationDto>) RecordsCheckUtil.jsonToList(recordsCheckDetermList));
			}
			List<Integer> checkedBoxesForDelete = new ArrayList<>();
			checkedBoxesForDelete.add(idRecCheck.intValue());
			recordsCheckDto.setCheckedBoxesForDelete(checkedBoxesForDelete);

			setInformationMessage(indModify, recordsCheckDto, informationMessage, recordsBusinessDelegate, idStage,
					idLoggedInUser);

			Date date = null;
			// Call the business delegate method to get the last update of the
			// narrative
			// document for DPS Check
			date = getRecordDocumentTsLastUpdate(recordsCheckDto.getIdRecCheck());

			if (!ObjectUtils.isEmpty(date)) {
				returnMap.put("indicator", Boolean.TRUE);

				docExists = WebConstants.TRUE_LOWER;
			} else {
				returnMap.put("indicator", Boolean.FALSE);

			}
			returnMap.put("docExists", docExists);
			recordsCheckDto.setIndicator(false);

			if(getNewHireCount(recordsCheckDto.getIdRecCheck()) == 0) {
				boolean bShowSendResenButton = !ObjectUtils.isEmpty(recordsCheckDto)
						? checkShowSendResendEmailButton(recordsCheckDto, idPerson)
						: false;
				recordsCheckDto.setShowSendResenButton(bShowSendResenButton);
			}


			// Call the business delegate method to check if the email is
			// already sent for
			// the particular record checkId
			if (!ObjectUtils.isEmpty(recordsCheckDto.getIdRecCheck()) && recordsCheckDto.getIdRecCheck() != 0l) {
				recordsCheckDto.setIndEmailSent(hasEmailSent(recordsCheckDto.getIdRecCheck()));
			}

			List<DocumentPdbValueDto> recordsCheckDocList = retrieveRecordsCheckDocuments(
					recordsCheckDto.getIdRecCheck());
			if (!ObjectUtils.isEmpty(recordsCheckDocList) && !CollectionUtils.isEmpty(recordsCheckDocList)) {
				for (DocumentPdbValueDto documentPdbValueDto : recordsCheckDocList) {
					try {
						documentPdbValueDto.setDocumentName(recordCheckUtil.getDocumentNameFromABCs(
								WebConstants.ABCS_APP_CODE, documentPdbValueDto.getIdDocRepository().intValue()));
					} catch (Exception e) {

					}
				}

			}
			recordsCheckDto.setRecordsCheckDocList(recordsCheckDocList);

			List<RecordsCheckNotificationDto> recordsCheckNotificationList = new ArrayList<RecordsCheckNotificationDto>();
			if (indModify && !ObjectUtils.isEmpty(recordsCheckDto)) {
				recordsCheckNotificationList = retrieveRecordsCheckNotif(recordsCheckDto.getIdRecCheck());

			}
			recordsCheckDto.setRecordsCheckNotificationList(recordsCheckNotificationList);

			recordsCheckDto.setDisableEmailButton(isDisabledEmailButton);
		}

		// JSP Logic
		// This is for disabling the requested date field
		boolean indDisableDate = false;
		// This is for displaying or hiding the delete button based on type of
		// Record
		// Check
		boolean indDeletable = true;
		// This is enabling or disabling the Results button
		boolean indResultsDisabled = true;
		// This is for disabling the Record Check Type field
		boolean indDisableType = false;
		// This is used for displaying or hiding the narrative button
		boolean indEBCNarrativeDisabled = true;
		boolean narrModeView = false;
		// This is used for enabling or disabling the completed date field
		boolean indDisableCompletedDate = false;
		// This is used for enabling or disabling the completed checkbox
		boolean indDisableCompletedCkbox = false;
		// This is used for hiding or displaying the email button
		boolean indShowEmailButton = false;
		// This is for hiding or displaying the completed checkbox
		boolean indShowCompletedCkbox = false;
		// This is for storing the value of the completed checkbox
		boolean indCompletedCkboxChkd = false;

		boolean indIncompleteBatchCk = false;

		int idContract = 0;
		// This is for displaying the email date
		boolean indShowEmailDate = false;
		// This is disabling the Determination field
		boolean indSelectDetermin = false;
		// This is for displaying History section
		boolean indHistorySection = false;
		// This is for disabling the Email and Phone dropdown
		boolean indDisableemailPhone = false;
		boolean indShowUploadedDocuments = false;
		boolean indShowNotifications = false;
		String sectionHeading = "";
		int historyCount = 0;
		String FPS_HIST_DEC = WebConstants.FPS_HISTORY_DECISION;
		String CRM_HIST_DEC = WebConstants.CRIMINAL_HISTORY_DECISION;
		Map<String, String> excludeDeterm = new TreeMap<String, String>();
		excludeDeterm = cacheAdapter.getCodeCategories(CodesConstant.CDETERM);
		Date rapBackReleaseDt = DateUtil.toJavaDate(cacheAdapter.getDecode(CodesConstant.CRELDATE, CodesConstant.JUN_2024_RAPBACK));

		RecordsCheckRes recordsCheckResource = getAbcsContractID(recordsCheckDto.getIdRecCheck());
		if (!ObjectUtils.isEmpty(recordsCheckResource)) {
			if(!ObjectUtils.isEmpty(recordsCheckResource.getIdContract())) {
				idContract = recordsCheckResource.getIdContract().intValue();
				recordsCheckDto.setIdContract(idContract);
			}
			if(!ObjectUtils.isEmpty(recordsCheckResource.getContractType())) {
				recordsCheckDto.setContractType(cacheAdapter.getDecode(CodesConstant.CNTRTYPE, recordsCheckResource.getContractType()));
			}
		}

		boolean excludePossibleMatchFromDeterm = false;

		Date dtRequest = recordsCheckDto.getDtRecCheckRequest();
		if (ObjectUtils.isEmpty(dtRequest)) {
			dtRequest = Calendar.getInstance().getTime();
		}
		Date requestDt = dtRequest;

		if (!ObjectUtils.isEmpty(pageMode)) {
			recordsCheckDto.setPageModeStr(pageMode);
		}

		if (!ObjectUtils.isEmpty(response.getNmPersonFirst())) {
			recordsCheckDto.setPersonFirstName(response.getNmPersonFirst());
		}
		if (!ObjectUtils.isEmpty(response.getNmPersonFull())) {
			recordsCheckDto.setPersonFullName(response.getNmPersonFull());
		}
		if (!ObjectUtils.isEmpty(response.getNmPersonLast())) {
			recordsCheckDto.setPersonLastName(response.getNmPersonLast());
		}
		if (!ObjectUtils.isEmpty(response.getPersonSex())) {
			recordsCheckDto.setPersonSex(response.getPersonSex());
		}
		if (!ObjectUtils.isEmpty(response.getPersonEthnicGroup())) {
			recordsCheckDto.setEthnicity(response.getPersonEthnicGroup());
		}
		if (!ObjectUtils.isEmpty(response.getPersonBirthDate())) {
			recordsCheckDto.setPersonDateOfBirth(response.getPersonBirthDate());
		}
		if (!ObjectUtils.isEmpty(response.getIndPersonDobApprox())) {
			recordsCheckDto.setIndPersonDobApprx(response.getIndPersonDobApprox());
		}
		if (!ObjectUtils.isEmpty(response.getPersonAge()) && response.getPersonAge() != 0l) {
			recordsCheckDto.setPersonAge(response.getPersonAge().intValue());
		} else if (!ObjectUtils.isEmpty(response.getPersonBirthDate()) && response.getPersonAge() == 0) {
			recordsCheckDto.setPersonAge(DateFormatUtil.getAge(response.getPersonBirthDate()));
		}
		if (!ObjectUtils.isEmpty(response.getIndRecCheckDpsIncomplete())) {
			recordsCheckDto.setRecCheckDpsIncomplete(response.getIndRecCheckDpsIncomplete());
		}

		if (!ObjectUtils.isEmpty(pageMode) && pageMode.equalsIgnoreCase(NEW)) {
			recordsCheckDto.setNmRequestedBy(recordsBusinessDelegate.getNmUser());
			recordsCheckDto.setIdRecCheckRequestor(recordsBusinessDelegate.getIdUser());

		} else {

			if (!ObjectUtils.isEmpty(recordsCheckDto.getIdRecCheck())) {
				String tmpDeterm = recordsCheckDto.getRecChkDeterm();
				if (CodesConstant.CDETERM_BRDN.equals(tmpDeterm) || CodesConstant.CDETERM_BRNR.equals(tmpDeterm)
						|| CodesConstant.CDETERM_REEL.equals(tmpDeterm)
						|| CodesConstant.CDETERM_CLAP.equals(tmpDeterm)) {
					excludePossibleMatchFromDeterm = true;
				}
			}
		}

		if (NEW.equals(pageMode) && !ObjectUtils.isEmpty(indReviewNowLaterString)
				&& !WebConstants.STRING_FALSE.equalsIgnoreCase(indReviewNowLaterString)) {
			indReviewNowLater = true;
		}
		String selFPSDet = StringHelper.getNonNullString(recordsCheckDto.getRecChkDeterm());
		if (!ObjectUtils.isEmpty(recordsCheckDto.getRecCheckCheckType())
				&& (CodesConstant.CCHKTYPE_10.equals(recordsCheckDto.getRecCheckCheckType())
						|| CodesConstant.CCHKTYPE_20.equals(recordsCheckDto.getRecCheckCheckType())
						|| CodesConstant.CCHKTYPE_85.equals(recordsCheckDto.getRecCheckCheckType())
						|| CodesConstant.CCHKTYPE_75.equals(recordsCheckDto.getRecCheckCheckType()))) {
			// if dps, central registry or FPS History type, only the comments
			// should be
			// enabled
			// disable the search type field
			indDisableCompletedDate = true;
			indDisableDate = true; // Requested date
		}



		/** Code changes for artf172943 **/
		if (!ObjectUtils.isEmpty(recordsCheckDto.getRecCheckCheckType())
				&& !CodesConstant.CCHKTYPE_80.equals(recordsCheckDto.getRecCheckCheckType())
				&& ((CodesConstant.CCHKTYPE_10.equals(recordsCheckDto.getRecCheckCheckType()) && getNewHireCount(recordsCheckDto.getIdRecCheck()) == 0))) {
			if(!ObjectUtils.isEmpty(recordsCheckDto.getContractType()) && recordsCheckDto.getContractType().equalsIgnoreCase("PCS"))
			{
				excludeDeterm.remove(CodesConstant.CDETERM_PNRS);
			}else {
				excludeDeterm.remove(CodesConstant.CDETERM_MHPA);
				excludeDeterm.remove(CodesConstant.CDETERM_PNRS);
			}
		}

        /**Code changes for artf258123**/

		/*
		 * Condition to show the determination value for new hire
		 * if the RecordCheck Type is FBI fingerprint, and
		 * it is subscribed to FBI Sub (FRB SUB)
		 * after RapBackReleaseDate
		 */
		if (!ObjectUtils.isEmpty(recordsCheckDto.getRecCheckCheckType())
				&& CodesConstant.CCHKTYPE_80.equals(recordsCheckDto.getRecCheckCheckType())
				&& !ObjectUtils.isEmpty(recordsCheckDto.getCdFbiSubscriptionDStatus())
				&& (getNewHireCount(recordsCheckDto.getIdRecCheck()) > 0)
				&& (Objects.nonNull(recordsCheckDto.getIdPdbBackgroundCheck()) && recordsCheckDto.getIdPdbBackgroundCheck()!=0)
				&& (!ObjectUtils.isEmpty(recordsCheckDto.getDtRecCheckRequest()) && (DateUtil.isAfter(recordsCheckDto.getDtRecCheckRequest(), rapBackReleaseDt)))
		) {
				removeIfNotDbValue(excludeDeterm, CodesConstant.CDETERM_BAR, getCdDetermination(recordsCheckDto.getIdRecCheck()));
				removeIfNotDbValue(excludeDeterm, CodesConstant.CDETERM_BRDN, getCdDetermination(recordsCheckDto.getIdRecCheck()));
				removeIfNotDbValue(excludeDeterm, CodesConstant.CDETERM_BRNR, getCdDetermination(recordsCheckDto.getIdRecCheck()));
				removeIfNotDbValue(excludeDeterm, CodesConstant.CDETERM_CLAP, getCdDetermination(recordsCheckDto.getIdRecCheck()));
				removeIfNotDbValue(excludeDeterm, CodesConstant.CDETERM_CLER, getCdDetermination(recordsCheckDto.getIdRecCheck()));
			    removeIfNotDbValue(excludeDeterm, CodesConstant.CDETERM_PFRBB, getCdDetermination(recordsCheckDto.getIdRecCheck()));
				removeIfNotDbValue(excludeDeterm, CodesConstant.CDETERM_PFRBC, getCdDetermination(recordsCheckDto.getIdRecCheck()));
				removeIfNotDbValue(excludeDeterm, CodesConstant.CDETERM_REEL, getCdDetermination(recordsCheckDto.getIdRecCheck()));
				removeIfNotDbValue(excludeDeterm, CodesConstant.CDETERM_PNRS, getCdDetermination(recordsCheckDto.getIdRecCheck()));
		}

		/*
		 * Condition to show the determination value for new hire
		 * if the RecordCheck Type is FBI fingerprint, and
		 * it is not subscribed to FBI Sub (Initial FBI)
		 * after RapBackReleaseDate
		 */
		if(!ObjectUtils.isEmpty(recordsCheckDto.getRecCheckCheckType())
				&& CodesConstant.CCHKTYPE_80.equals(recordsCheckDto.getRecCheckCheckType())
				&& ObjectUtils.isEmpty(recordsCheckDto.getCdFbiSubscriptionDStatus())
				&& (getNewHireCount(recordsCheckDto.getIdRecCheck()) > 0)
				&& (Objects.nonNull(recordsCheckDto.getIdPdbBackgroundCheck()) && recordsCheckDto.getIdPdbBackgroundCheck() != 0)
				&& (!ObjectUtils.isEmpty(recordsCheckDto.getDtRecCheckRequest()) && (DateUtil.isAfter(recordsCheckDto.getDtRecCheckRequest(), rapBackReleaseDt)))
		){
			removeIfNotDbValue(excludeDeterm, CodesConstant.CDETERM_BAR, getCdDetermination(recordsCheckDto.getIdRecCheck()));
			removeIfNotDbValue(excludeDeterm, CodesConstant.CDETERM_BRDN, getCdDetermination(recordsCheckDto.getIdRecCheck()));
			removeIfNotDbValue(excludeDeterm, CodesConstant.CDETERM_BRNR, getCdDetermination(recordsCheckDto.getIdRecCheck()));
			removeIfNotDbValue(excludeDeterm, CodesConstant.CDETERM_CLAP, getCdDetermination(recordsCheckDto.getIdRecCheck()));
			removeIfNotDbValue(excludeDeterm, CodesConstant.CDETERM_CLER, getCdDetermination(recordsCheckDto.getIdRecCheck()));
			removeIfNotDbValue(excludeDeterm, CodesConstant.CDETERM_ELGB, getCdDetermination(recordsCheckDto.getIdRecCheck()));
			removeIfNotDbValue(excludeDeterm, CodesConstant.CDETERM_INEG, getCdDetermination(recordsCheckDto.getIdRecCheck()));
			removeIfNotDbValue(excludeDeterm, CodesConstant.CDETERM_PFRBB, getCdDetermination(recordsCheckDto.getIdRecCheck()));
			removeIfNotDbValue(excludeDeterm, CodesConstant.CDETERM_PFRBC, getCdDetermination(recordsCheckDto.getIdRecCheck()));
			removeIfNotDbValue(excludeDeterm, CodesConstant.CDETERM_PNRS, getCdDetermination(recordsCheckDto.getIdRecCheck()));
			removeIfNotDbValue(excludeDeterm, CodesConstant.CDETERM_REEL, getCdDetermination(recordsCheckDto.getIdRecCheck()));
		}

		/*
		 * Condition to show the determination value for non-new hire
		 * if the RecordCheck Type is FBI fingerprint, and
		 * it is subscribed to FBI Sub (FRB SUB)
		 * after RapBackReleaseDate
		 */
		if (!ObjectUtils.isEmpty(recordsCheckDto.getRecCheckCheckType())
				&& CodesConstant.CCHKTYPE_80.equals(recordsCheckDto.getRecCheckCheckType())
				&& !ObjectUtils.isEmpty(recordsCheckDto.getCdFbiSubscriptionDStatus())
				&& (getNewHireCount(recordsCheckDto.getIdRecCheck()) == 0)
				&& (Objects.nonNull(recordsCheckDto.getIdPdbBackgroundCheck()) && recordsCheckDto.getIdPdbBackgroundCheck()!=0)
				&& (!ObjectUtils.isEmpty(recordsCheckDto.getDtRecCheckRequest()) && (DateUtil.isAfter(recordsCheckDto.getDtRecCheckRequest(), rapBackReleaseDt)))
		) {
				removeIfNotDbValue(excludeDeterm, CodesConstant.CDETERM_BAR, getCdDetermination(recordsCheckDto.getIdRecCheck()));
				removeIfNotDbValue(excludeDeterm, CodesConstant.CDETERM_BRDN, getCdDetermination(recordsCheckDto.getIdRecCheck()));
				removeIfNotDbValue(excludeDeterm, CodesConstant.CDETERM_BRNR, getCdDetermination(recordsCheckDto.getIdRecCheck()));
				removeIfNotDbValue(excludeDeterm, CodesConstant.CDETERM_CLAP, getCdDetermination(recordsCheckDto.getIdRecCheck()));
				removeIfNotDbValue(excludeDeterm, CodesConstant.CDETERM_CLER, getCdDetermination(recordsCheckDto.getIdRecCheck()));
				removeIfNotDbValue(excludeDeterm, CodesConstant.CDETERM_PFRBB, getCdDetermination(recordsCheckDto.getIdRecCheck()));
				removeIfNotDbValue(excludeDeterm, CodesConstant.CDETERM_PFRBC, getCdDetermination(recordsCheckDto.getIdRecCheck()));
				removeIfNotDbValue(excludeDeterm, CodesConstant.CDETERM_PNRS, getCdDetermination(recordsCheckDto.getIdRecCheck()));
				removeIfNotDbValue(excludeDeterm, CodesConstant.CDETERM_REEL, getCdDetermination(recordsCheckDto.getIdRecCheck()));
				removeIfNotDbValue(excludeDeterm, CodesConstant.CDETERM_RVHR, getCdDetermination(recordsCheckDto.getIdRecCheck()));
				removeIfNotDbValue(excludeDeterm, CodesConstant.CDETERM_POSS, getCdDetermination(recordsCheckDto.getIdRecCheck()));
		}

		/*
		 * Condition to show the determination value for non-new hire
		 * if the RecordCheck Type is FBI fingerprint, and
		 * it is not subscribed to FBI Sub (Initial FBI)
		 * after RapBackReleaseDate
		 */
		if(!ObjectUtils.isEmpty(recordsCheckDto.getRecCheckCheckType())
				&& CodesConstant.CCHKTYPE_80.equals(recordsCheckDto.getRecCheckCheckType())
				&& ObjectUtils.isEmpty(recordsCheckDto.getCdFbiSubscriptionDStatus())
				&& (getNewHireCount(recordsCheckDto.getIdRecCheck()) == 0)
				&& (Objects.nonNull(recordsCheckDto.getIdPdbBackgroundCheck()) && recordsCheckDto.getIdPdbBackgroundCheck() != 0)
				&& (!ObjectUtils.isEmpty(recordsCheckDto.getDtRecCheckRequest()) && (DateUtil.isAfter(recordsCheckDto.getDtRecCheckRequest(), rapBackReleaseDt)))
		) {
			removeIfNotDbValue(excludeDeterm, CodesConstant.CDETERM_BAR, getCdDetermination(recordsCheckDto.getIdRecCheck()));
			removeIfNotDbValue(excludeDeterm, CodesConstant.CDETERM_BRDN, getCdDetermination(recordsCheckDto.getIdRecCheck()));
			removeIfNotDbValue(excludeDeterm, CodesConstant.CDETERM_BRNR, getCdDetermination(recordsCheckDto.getIdRecCheck()));
			removeIfNotDbValue(excludeDeterm, CodesConstant.CDETERM_CLAP, getCdDetermination(recordsCheckDto.getIdRecCheck()));
			removeIfNotDbValue(excludeDeterm, CodesConstant.CDETERM_CLER, getCdDetermination(recordsCheckDto.getIdRecCheck()));
			removeIfNotDbValue(excludeDeterm, CodesConstant.CDETERM_ELGB, getCdDetermination(recordsCheckDto.getIdRecCheck()));
			removeIfNotDbValue(excludeDeterm, CodesConstant.CDETERM_INEG, getCdDetermination(recordsCheckDto.getIdRecCheck()));
			removeIfNotDbValue(excludeDeterm, CodesConstant.CDETERM_PFRBB, getCdDetermination(recordsCheckDto.getIdRecCheck()));
			removeIfNotDbValue(excludeDeterm, CodesConstant.CDETERM_PFRBC, getCdDetermination(recordsCheckDto.getIdRecCheck()));
			removeIfNotDbValue(excludeDeterm, CodesConstant.CDETERM_PNRS, getCdDetermination(recordsCheckDto.getIdRecCheck()));
			removeIfNotDbValue(excludeDeterm, CodesConstant.CDETERM_REEL, getCdDetermination(recordsCheckDto.getIdRecCheck()));
			removeIfNotDbValue(excludeDeterm, CodesConstant.CDETERM_RVHR, getCdDetermination(recordsCheckDto.getIdRecCheck()));
			removeIfNotDbValue(excludeDeterm, CodesConstant.CDETERM_POSS, getCdDetermination(recordsCheckDto.getIdRecCheck()));
		}

		/*
		 * Condition to show the determination value for new hire
		 * if the RecordCheck Type is DPS Criminal History Check
		 */
		if (!ObjectUtils.isEmpty(recordsCheckDto.getRecCheckCheckType())
				&& CodesConstant.CCHKTYPE_10.equals(recordsCheckDto.getRecCheckCheckType())
				&& getNewHireCount(recordsCheckDto.getIdRecCheck()) > 0) {

			removeIfNotDbValue(excludeDeterm, CodesConstant.CDETERM_BAR, getCdDetermination(recordsCheckDto.getIdRecCheck()));
			removeIfNotDbValue(excludeDeterm, CodesConstant.CDETERM_BRDN, getCdDetermination(recordsCheckDto.getIdRecCheck()));
			removeIfNotDbValue(excludeDeterm, CodesConstant.CDETERM_BRNR, getCdDetermination(recordsCheckDto.getIdRecCheck()));
			removeIfNotDbValue(excludeDeterm, CodesConstant.CDETERM_CLAP, getCdDetermination(recordsCheckDto.getIdRecCheck()));
			removeIfNotDbValue(excludeDeterm, CodesConstant.CDETERM_CLER, getCdDetermination(recordsCheckDto.getIdRecCheck()));
			removeIfNotDbValue(excludeDeterm, CodesConstant.CDETERM_POSS, getCdDetermination(recordsCheckDto.getIdRecCheck()));
			removeIfNotDbValue(excludeDeterm, CodesConstant.CDETERM_REEL, getCdDetermination(recordsCheckDto.getIdRecCheck()));
			removeIfNotDbValue(excludeDeterm, CodesConstant.CDETERM_PFRBE, getCdDetermination(recordsCheckDto.getIdRecCheck()));
			removeIfNotDbValue(excludeDeterm, CodesConstant.CDETERM_PFRBI, getCdDetermination(recordsCheckDto.getIdRecCheck()));
			removeIfNotDbValue(excludeDeterm, CodesConstant.CDETERM_PFRBB, getCdDetermination(recordsCheckDto.getIdRecCheck()));
			removeIfNotDbValue(excludeDeterm, CodesConstant.CDETERM_PFRBC, getCdDetermination(recordsCheckDto.getIdRecCheck()));
			removeIfNotDbValue(excludeDeterm, CodesConstant.CDETERM_PNRS, getCdDetermination(recordsCheckDto.getIdRecCheck()));
		}

		/*
		 * Condition to show the determination value for non-new hire
		 * if the RecordCheck Type is DPS Criminal History Check
		 */
		if (!ObjectUtils.isEmpty(recordsCheckDto.getRecCheckCheckType())
				&& CodesConstant.CCHKTYPE_10.equals(recordsCheckDto.getRecCheckCheckType())
				&& getNewHireCount(recordsCheckDto.getIdRecCheck()) == 0) {
			/*
			 * Condition to show the determination value for PCS contract type
			 * if the RecordCheck Type is DPS Criminal History Check
			 */
			if(!ObjectUtils.isEmpty(recordsCheckDto.getContractType()) && recordsCheckDto.getContractType().equalsIgnoreCase("PCS"))
			{
				removeIfNotDbValue(excludeDeterm, CodesConstant.CDETERM_BAR, getCdDetermination(recordsCheckDto.getIdRecCheck()));
				removeIfNotDbValue(excludeDeterm, CodesConstant.CDETERM_BRDN, getCdDetermination(recordsCheckDto.getIdRecCheck()));
				removeIfNotDbValue(excludeDeterm, CodesConstant.CDETERM_BRNR, getCdDetermination(recordsCheckDto.getIdRecCheck()));
				removeIfNotDbValue(excludeDeterm, CodesConstant.CDETERM_CLAP, getCdDetermination(recordsCheckDto.getIdRecCheck()));
				removeIfNotDbValue(excludeDeterm, CodesConstant.CDETERM_CLER, getCdDetermination(recordsCheckDto.getIdRecCheck()));
				removeIfNotDbValue(excludeDeterm, CodesConstant.CDETERM_PFRBB, getCdDetermination(recordsCheckDto.getIdRecCheck()));
				removeIfNotDbValue(excludeDeterm, CodesConstant.CDETERM_PFRBC, getCdDetermination(recordsCheckDto.getIdRecCheck()));
				removeIfNotDbValue(excludeDeterm, CodesConstant.CDETERM_PFRBE, getCdDetermination(recordsCheckDto.getIdRecCheck()));
				removeIfNotDbValue(excludeDeterm, CodesConstant.CDETERM_PFRBI, getCdDetermination(recordsCheckDto.getIdRecCheck()));
				removeIfNotDbValue(excludeDeterm, CodesConstant.CDETERM_RVHR, getCdDetermination(recordsCheckDto.getIdRecCheck()));
				removeIfNotDbValue(excludeDeterm, CodesConstant.CDETERM_REEL, getCdDetermination(recordsCheckDto.getIdRecCheck()));
				removeIfNotDbValue(excludeDeterm, CodesConstant.CDETERM_POSS, getCdDetermination(recordsCheckDto.getIdRecCheck()));
			}
			else {
				removeIfNotDbValue(excludeDeterm, CodesConstant.CDETERM_ELGB, getCdDetermination(recordsCheckDto.getIdRecCheck()));
				removeIfNotDbValue(excludeDeterm, CodesConstant.CDETERM_INEG, getCdDetermination(recordsCheckDto.getIdRecCheck()));
				removeIfNotDbValue(excludeDeterm, CodesConstant.CDETERM_PFRBE, getCdDetermination(recordsCheckDto.getIdRecCheck()));
				removeIfNotDbValue(excludeDeterm, CodesConstant.CDETERM_PFRBI, getCdDetermination(recordsCheckDto.getIdRecCheck()));
				removeIfNotDbValue(excludeDeterm, CodesConstant.CDETERM_RVHR, getCdDetermination(recordsCheckDto.getIdRecCheck()));
				removeIfNotDbValue(excludeDeterm, CodesConstant.CDETERM_PFRBB, getCdDetermination(recordsCheckDto.getIdRecCheck()));
				removeIfNotDbValue(excludeDeterm, CodesConstant.CDETERM_PFRBC, getCdDetermination(recordsCheckDto.getIdRecCheck()));
				removeIfNotDbValue(excludeDeterm, CodesConstant.CDETERM_RVHR, getCdDetermination(recordsCheckDto.getIdRecCheck()));
			}
		}

		/*
		 * Condition to show the determination value for new hire
		 * if the RecordCheck Type is FPS Batch
		 */
		if (!ObjectUtils.isEmpty(recordsCheckDto.getRecCheckCheckType())
				&& CodesConstant.CCHKTYPE_75.equals(recordsCheckDto.getRecCheckCheckType())
				&& getNewHireCount(recordsCheckDto.getIdRecCheck()) > 0) {

			removeIfNotDbValue(excludeDeterm, CodesConstant.CDETERM_BAR, getCdDetermination(recordsCheckDto.getIdRecCheck()));
			removeIfNotDbValue(excludeDeterm, CodesConstant.CDETERM_BRDN, getCdDetermination(recordsCheckDto.getIdRecCheck()));
			removeIfNotDbValue(excludeDeterm, CodesConstant.CDETERM_BRNR, getCdDetermination(recordsCheckDto.getIdRecCheck()));
			removeIfNotDbValue(excludeDeterm, CodesConstant.CDETERM_CLAP, getCdDetermination(recordsCheckDto.getIdRecCheck()));
			removeIfNotDbValue(excludeDeterm, CodesConstant.CDETERM_CLER, getCdDetermination(recordsCheckDto.getIdRecCheck()));
			removeIfNotDbValue(excludeDeterm, CodesConstant.CDETERM_POSS, getCdDetermination(recordsCheckDto.getIdRecCheck()));
			removeIfNotDbValue(excludeDeterm, CodesConstant.CDETERM_REEL, getCdDetermination(recordsCheckDto.getIdRecCheck()));
			removeIfNotDbValue(excludeDeterm, CodesConstant.CDETERM_PFRBE, getCdDetermination(recordsCheckDto.getIdRecCheck()));
			removeIfNotDbValue(excludeDeterm, CodesConstant.CDETERM_PFRBI, getCdDetermination(recordsCheckDto.getIdRecCheck()));
			removeIfNotDbValue(excludeDeterm, CodesConstant.CDETERM_PFRBB, getCdDetermination(recordsCheckDto.getIdRecCheck()));
			removeIfNotDbValue(excludeDeterm, CodesConstant.CDETERM_PFRBC, getCdDetermination(recordsCheckDto.getIdRecCheck()));
			removeIfNotDbValue(excludeDeterm, CodesConstant.CDETERM_PNRS, getCdDetermination(recordsCheckDto.getIdRecCheck()));
			removeIfNotDbValue(excludeDeterm, CodesConstant.CDETERM_RVHR, getCdDetermination(recordsCheckDto.getIdRecCheck()));
			removeIfNotDbValue(excludeDeterm, CodesConstant.CDETERM_MHPA, getCdDetermination(recordsCheckDto.getIdRecCheck()));
		}

		/*
		 * Condition to show the determination value for non-new hire DFPS account
		 * if the RecordCheck Type is FPS Batch
		 * Story CABCSA-14: SD 84181: Modify Record Check Detail Page-PCS-FPS Drop Down Options
		 */
		if (!ObjectUtils.isEmpty(recordsCheckDto.getRecCheckCheckType())
				&& CodesConstant.CCHKTYPE_75.equals(recordsCheckDto.getRecCheckCheckType())
				&& getNewHireCount(recordsCheckDto.getIdRecCheck()) == 0) {

			if(!ObjectUtils.isEmpty(recordsCheckDto.getContractType()) && recordsCheckDto.getContractType().equalsIgnoreCase("PCS"))
			{
				removeIfNotDbValue(excludeDeterm, CodesConstant.CDETERM_BAR, getCdDetermination(recordsCheckDto.getIdRecCheck()));
				removeIfNotDbValue(excludeDeterm, CodesConstant.CDETERM_CLER, getCdDetermination(recordsCheckDto.getIdRecCheck()));
				removeIfNotDbValue(excludeDeterm, CodesConstant.CDETERM_PFRBB, getCdDetermination(recordsCheckDto.getIdRecCheck()));
				removeIfNotDbValue(excludeDeterm, CodesConstant.CDETERM_PFRBC, getCdDetermination(recordsCheckDto.getIdRecCheck()));
				removeIfNotDbValue(excludeDeterm, CodesConstant.CDETERM_PFRBE, getCdDetermination(recordsCheckDto.getIdRecCheck()));
				removeIfNotDbValue(excludeDeterm, CodesConstant.CDETERM_PFRBI, getCdDetermination(recordsCheckDto.getIdRecCheck()));
				removeIfNotDbValue(excludeDeterm, CodesConstant.CDETERM_PNRS, getCdDetermination(recordsCheckDto.getIdRecCheck()));
				removeIfNotDbValue(excludeDeterm, CodesConstant.CDETERM_POSS, getCdDetermination(recordsCheckDto.getIdRecCheck()));
				removeIfNotDbValue(excludeDeterm, CodesConstant.CDETERM_RVHR, getCdDetermination(recordsCheckDto.getIdRecCheck()));

			}
			else {
				removeIfNotDbValue(excludeDeterm, CodesConstant.CDETERM_BRDN, getCdDetermination(recordsCheckDto.getIdRecCheck()));
				removeIfNotDbValue(excludeDeterm, CodesConstant.CDETERM_BRNR, getCdDetermination(recordsCheckDto.getIdRecCheck()));
				removeIfNotDbValue(excludeDeterm, CodesConstant.CDETERM_CLAP, getCdDetermination(recordsCheckDto.getIdRecCheck()));
				removeIfNotDbValue(excludeDeterm, CodesConstant.CDETERM_POSS, getCdDetermination(recordsCheckDto.getIdRecCheck()));
				removeIfNotDbValue(excludeDeterm, CodesConstant.CDETERM_REEL, getCdDetermination(recordsCheckDto.getIdRecCheck()));
				removeIfNotDbValue(excludeDeterm, CodesConstant.CDETERM_PFRBE, getCdDetermination(recordsCheckDto.getIdRecCheck()));
				removeIfNotDbValue(excludeDeterm, CodesConstant.CDETERM_PFRBI, getCdDetermination(recordsCheckDto.getIdRecCheck()));
				removeIfNotDbValue(excludeDeterm, CodesConstant.CDETERM_PFRBB, getCdDetermination(recordsCheckDto.getIdRecCheck()));
				removeIfNotDbValue(excludeDeterm, CodesConstant.CDETERM_PFRBC, getCdDetermination(recordsCheckDto.getIdRecCheck()));
				removeIfNotDbValue(excludeDeterm, CodesConstant.CDETERM_PNRS, getCdDetermination(recordsCheckDto.getIdRecCheck()));
				removeIfNotDbValue(excludeDeterm, CodesConstant.CDETERM_RVHR, getCdDetermination(recordsCheckDto.getIdRecCheck()));
				removeIfNotDbValue(excludeDeterm, CodesConstant.CDETERM_MHPA, getCdDetermination(recordsCheckDto.getIdRecCheck()));
				removeIfNotDbValue(excludeDeterm, CodesConstant.CDETERM_ELGB, getCdDetermination(recordsCheckDto.getIdRecCheck()));
				removeIfNotDbValue(excludeDeterm, CodesConstant.CDETERM_INEG, getCdDetermination(recordsCheckDto.getIdRecCheck()));
			}
		}


		/** End of code changes for artf172943 **/
		if (!ObjectUtils.isEmpty(recordsCheckDto.getRecCheckCheckType())
				&& (CodesConstant.CCHKTYPE_10.equals(recordsCheckDto.getRecCheckCheckType()) || // DPS
																								// Criminal
																								// History
						CodesConstant.CCHKTYPE_80.equals(recordsCheckDto.getRecCheckCheckType()) || // FBI
																									// Finger
																									// Print
						CodesConstant.CCHKTYPE_75.equals(recordsCheckDto.getRecCheckCheckType()))) // FPS
																									// HistoryCheck
																									// Batch
		{
			if (recordsBusinessDelegate.isIndRecCheckAccess()) {

				indShowUploadedDocuments = true;
				indShowNotifications = true;

				if (CodesConstant.CCHKTYPE_75.equals(recordsCheckDto.getRecCheckCheckType())) {

					if (!ObjectUtils.isEmpty(recordsCheckDto.getDtRecCheckRequest())) {
						if (DateFormatUtil.isAfter(recordsCheckDto.getDtRecCheckRequest(),
								DateFormatUtil.RECORDS_CHK_DETERMIN_DATE)) {
							indHistorySection = true;

							if (ObjectUtils.isEmpty(recordsCheckDto.getDtRecCheckCompleted())) {
								indSelectDetermin = true;
							}
						}
					}
				}

			}
			if (!ObjectUtils.isEmpty(recordsCheckDto.getRecCheckCheckType())
					&& CodesConstant.CCHKTYPE_10.equals(recordsCheckDto.getRecCheckCheckType()) || // DPS
																									// Criminal
																									// History
					CodesConstant.CCHKTYPE_80.equals(recordsCheckDto.getRecCheckCheckType())) // FBI
																								// Finger
																								// Print
			{
				sectionHeading = CRM_HIST_DEC;
				/** Code changes for artf172912 and artf172948.
				   isABCSCheck indicator value is needed for both record check type 10 and 80 **/
				boolean isABCSCheck = false;
				if (!ObjectUtils.isEmpty(recordsCheckDto.getIdRecCheck())
						&& recordsCheckDto.getIdRecCheck() != 0l) {
					isABCSCheck = isABCSCheck(recordsCheckDto.getIdRecCheck());
				}
				recordsCheckDto.setAbcsCheck(isABCSCheck);
				/** End of code changes for artf172912 and artf172948**/
				/** Code changes for artf172912 **/
				/* Modified the code to fix defect artf179186 */
				if ((CodesConstant.CCHKTYPE_10.equals(recordsCheckDto.getRecCheckCheckType())
						&& indModify && recordsBusinessDelegate.isIndRecCheckAccess()
						&& !ObjectUtils.isEmpty(recordsCheckDto.getIdStage()) && recordsCheckDto.getIdStage() == 0l
						&& !ObjectUtils.isEmpty(recordsCheckDto.getDtRecCheckCompleted())
						&& !(!ObjectUtils.isEmpty(recordsCheckDto.getRecCheckStatus())
								&& CodesConstant.CCRIMSTA_N.equals(recordsCheckDto.getRecCheckStatus())
								|| CodesConstant.CCRIMSTA_W.equals(recordsCheckDto.getRecCheckStatus())
								|| CodesConstant.CCRIMSTA_X.equals(recordsCheckDto.getRecCheckStatus())
								|| CodesConstant.CCRIMSTA_Y.equals(recordsCheckDto.getRecCheckStatus())
								|| CodesConstant.CCRIMSTA_Z.equals(recordsCheckDto.getRecCheckStatus())
								|| CodesConstant.CCRIMSTA_ZA.equals(recordsCheckDto.getRecCheckStatus())
								|| CodesConstant.CCRIMSTA_T.equals(recordsCheckDto.getRecCheckStatus())))
				|| (CodesConstant.CCHKTYPE_80.equals(recordsCheckDto.getRecCheckCheckType())
						&& indModify
						&& isABCSCheck)) {
					if (CodesConstant.CCHKTYPE_10.equals(recordsCheckDto.getRecCheckCheckType())
							&& !ObjectUtils.isEmpty(recordsCheckDto.getDtRecCheckRequest()) && DateFormatUtil.isAfter(
							recordsCheckDto.getDtRecCheckRequest(), DateFormatUtil.RECORDS_CHK_DETERMIN_DATE)) {
						indHistorySection = true;
					}else if(CodesConstant.CCHKTYPE_80.equals(recordsCheckDto.getRecCheckCheckType())
							&& !ObjectUtils.isEmpty(recordsCheckDto.getRecCheckStatus())){
						indHistorySection = true;
					}
				}
				/* End of code changes to fix defect artf179186 */
				/** End of code changes for artf172912 **/

				if (!ObjectUtils.isEmpty(recordsCheckDto.getRecCheckCheckType())
						&& CodesConstant.CCHKTYPE_10.equals(recordsCheckDto.getRecCheckCheckType())) {
					if (indModify && isABCSCheck && recordsBusinessDelegate.isIndRecCheckAccess()
							&& !ObjectUtils.isEmpty(recordsCheckDto.getIdStage()) && recordsCheckDto.getIdStage() == 0l
							&& !ObjectUtils.isEmpty(recordsCheckDto.getDtRecCheckCompleted())) {
						if (indModify && !ObjectUtils.isEmpty(recordsCheckDto.getRecCheckStatus())
								&& CodesConstant.CCRIMSTA_N.equals(recordsCheckDto.getRecCheckStatus())) {
							indHistorySection = false;
							if (!ObjectUtils.isEmpty(recordsCheckDto.getDtRecCheckRequest()) && DateFormatUtil
									.isAfter(recordsCheckDto.getDtRecCheckRequest(), DateFormatUtil.CCH_DPS_WS)) {
								indHistorySection = true;
							}
						}
					}

				}
				/* Code changes to fix defect artf179186 */
				if(!ObjectUtils.isEmpty(recordsCheckDto.getRecCheckCheckType())
						&& CodesConstant.CCHKTYPE_80.equals(recordsCheckDto.getRecCheckCheckType())){
					if ((CodesConstant.CCRIMSTA_S.equals(recordsCheckDto.getRecCheckStatus())) || (CodesConstant.CCRIMSTA_Z.equals(recordsCheckDto.getRecCheckStatus()))
							|| (CodesConstant.CCRIMSTA_Y.equals(recordsCheckDto.getRecCheckStatus()))|| (CodesConstant.CCRIMSTA_X.equals(recordsCheckDto.getRecCheckStatus()))
							|| (CodesConstant.CCRIMSTA_W.equals(recordsCheckDto.getRecCheckStatus()))|| (CodesConstant.CCRIMSTA_PG.equals(recordsCheckDto.getRecCheckStatus()))){
						indSelectDetermin = true;
					}
				}
				/* End of code changes to fix defect artf179186 */
				if (indModify && StringHelper.isValid(selFPSDet)
						|| !ObjectUtils.isEmpty(recordsCheckDto.getDtDetermFinal())) {

					if (indModify && (null != selFPSDet && !selFPSDet.isEmpty())
						&& !(CodesConstant.CDETERM_REEL.equals(selFPSDet)
							|| CodesConstant.CDETERM_POSS.equals(selFPSDet)
							|| CodesConstant.CDETERM_MHPA.equals(selFPSDet) // Added for artf172943
							|| CodesConstant.CDETERM_PNRS.equals(selFPSDet)
							|| CodesConstant.CDETERM_PFRBB.equals(selFPSDet)
							|| CodesConstant.CDETERM_PFRBC.equals(selFPSDet)
							|| CodesConstant.CDETERM_PFRBE.equals(selFPSDet)
							|| CodesConstant.CDETERM_PFRBI.equals(selFPSDet)
							|| CodesConstant.CDETERM_RVHR.equals(selFPSDet)
					))  { // Added for artf172943
						indSelectDetermin = true;
					}
				}
				if (indModify && StringHelper.isValid(selFPSDet) && CodesConstant.CDETERM_REEL.equals(selFPSDet)) {
					excludeDeterm.remove(CodesConstant.CDETERM_CLER);
					excludeDeterm.remove(CodesConstant.CDETERM_BAR);
					excludeDeterm.remove(CodesConstant.CDETERM_NOTA);
				}
				if (excludePossibleMatchFromDeterm) {
					excludeDeterm.remove(CodesConstant.CDETERM_POSS);
				}
				if (indModify && StringHelper.isValid(selFPSDet)) {
					if (indModify && (CodesConstant.CDETERM_CLER.equals(selFPSDet) || CodesConstant.CDETERM_CLAP.equals(selFPSDet) || CodesConstant.CDETERM_PFRBC.equals(selFPSDet))
							|| !ObjectUtils.isEmpty(recordsCheckDto.getDtClrdEmailRequested())) {
						// Call the business delegate method to get the contract
						// id
						/* Code changes to implement artf171305 */
						RecordsCheckRes recordsCheckRes = getAbcsContractID(recordsCheckDto.getIdRecCheck());
						if (!ObjectUtils.isEmpty(recordsCheckRes)) {
							if(!ObjectUtils.isEmpty(recordsCheckRes.getIdContract())) {
								idContract = recordsCheckRes.getIdContract().intValue();
								recordsCheckDto.setIdContract(idContract);
							}
							if(!ObjectUtils.isEmpty(recordsCheckRes.getContractType())) {
								recordsCheckDto.setContractType(cacheAdapter.getDecode(CodesConstant.CNTRTYPE, recordsCheckRes.getContractType()));
							}
						}
						/* End Code change for artf171305 */

						//Code Update to Show Eligible/Ineligible/Clearance Email date
						if ((0 < idContract) && !ObjectUtils.isEmpty(recordsCheckDto.getDtRecCheckRequest())
								&& !DateFormatUtil.isBefore(recordsCheckDto.getDtRecCheckRequest(),
								DateFormatUtil.RECORDS_CHECK_CLEARANCE_EMAIL_DATE)
								&& ((!CodesConstant.CDETERM_ELGB.equalsIgnoreCase(recordsCheckDto.getRecChkDeterm()))
								&& (!CodesConstant.CDETERM_PFRBE.equalsIgnoreCase(recordsCheckDto.getRecChkDeterm()))
								&& (!CodesConstant.CDETERM_INEG.equalsIgnoreCase(recordsCheckDto.getRecChkDeterm()))
								&& (!CodesConstant.CDETERM_NOTA.equalsIgnoreCase(recordsCheckDto.getRecChkDeterm()))
								&& (!CodesConstant.CDETERM_RVHR.equalsIgnoreCase(recordsCheckDto.getRecChkDeterm()))
								&& (!CodesConstant.CDETERM_PFRBI.equalsIgnoreCase(recordsCheckDto.getRecChkDeterm())))) {
							if (ObjectUtils.isEmpty(recordsCheckDto.getDtClrdEmailRequested())) {
								indShowEmailButton = true;
							}
							indShowEmailDate = true; // Email Requested Date

						}
					}

				}

				if (!ObjectUtils.isEmpty(recordsCheckDto.getRecCheckCheckType())
						&& CodesConstant.CCHKTYPE_80.equals(recordsCheckDto.getRecCheckCheckType())) {

					/* Code changes to implement artf171305 */
					if (!ObjectUtils.isEmpty(recordsCheckDto) && recordsCheckDto.getIdContract() == 0 && StringUtils.isEmpty(recordsCheckDto.getContractType())){
							RecordsCheckRes recordsCheckRes80 = getAbcsContractID(recordsCheckDto.getIdRecCheck());
							if (!ObjectUtils.isEmpty(recordsCheckRes80)) {
								if(!ObjectUtils.isEmpty(recordsCheckRes80.getIdContract())) {
									idContract = recordsCheckRes80.getIdContract().intValue();
									recordsCheckDto.setIdContract(idContract);
								}
								if(!ObjectUtils.isEmpty(recordsCheckRes80.getContractType())) {
									recordsCheckDto.setContractType(cacheAdapter.getDecode(CodesConstant.CNTRTYPE, recordsCheckRes80.getContractType()));
								}
							}
					}
					/* End Code change for artf171305 */

					/** Code change for artf172936 **/
					// Call the business delegate to get ChriminalHistory access value
					boolean hasChriAccess = false;
					if (!ObjectUtils.isEmpty(recordsCheckDto.getIdRecCheck()) && recordsCheckDto.getIdRecCheck() != 0l) {
						hasChriAccess = getAbcsAccessData(recordsCheckDto.getIdRecCheck());
						recordsCheckDto.setChriAccess(hasChriAccess);
					}
					/** End of code change for artf172936 **/
					/** Code changes for artf172948 **/
					if (isABCSCheck){
					 	indShowCompletedCkbox = true;
						indDisableCompletedCkbox = true;
						if (indModify && !ObjectUtils.isEmpty(recordsCheckDto.getDtRecCheckCompleted())
								&& !ObjectUtils.isEmpty(recordsCheckDto.getRecChkDeterm())
								&& (CodesConstant.CDETERM_BAR.equals(recordsCheckDto.getRecChkDeterm())
								|| CodesConstant.CDETERM_BRDN.equals(recordsCheckDto.getRecChkDeterm())
								|| CodesConstant.CDETERM_BRNR.equals(recordsCheckDto.getRecChkDeterm())
								|| CodesConstant.CDETERM_CLAP.equals(recordsCheckDto.getRecChkDeterm())
								|| CodesConstant.CDETERM_CLER.equals(recordsCheckDto.getRecChkDeterm())
								|| CodesConstant.CDETERM_NOTA.equals(recordsCheckDto.getRecChkDeterm()))) {
							indCompletedCkboxChkd = true;
						}
					}
					/** End of code change for artf172948 **/
					/* Code changes for artf172946 */
					if (indModify && ObjectUtils.isEmpty(recordsCheckDto.getDtRecCheckCompleted()) && TypeConvUtil.isNullOrEmpty(recordsCheckDto.getIndCrimHistoryResultCopied())){
						recordsCheckDto.setDisableCrimResult(false);
					}
					if (indModify && !ObjectUtils.isEmpty(recordsCheckDto.getIndCrimHistoryResultCopied())
							&& recordsCheckDto.getIndCrimHistoryResultCopied().equals(WebConstants.YES)
							&& (ObjectUtils.isEmpty(recordsCheckDto.getRecCheckStatus()) || recordsCheckDto.getRecCheckStatus().equals(CodesConstant.CCRIMSTA_S))){
						recordsCheckDto.setShowAddResultsButton(true);
					}
					/* End of code changes for artf172946 */
					/* Code changes for artf172945 */
					if(!ObjectUtils.isEmpty(recordsCheckDto.getIdCancelledPerson())) {
						recordsCheckDto.setFbiCancelledPersonName(criminalHistoryBusinessDelegate.getNmPersonFull(recordsCheckDto.getIdCancelledPerson()));

					}
					/* End of code changes for artf172945 */

					if(indModify && (!TypeConvUtil.isNullOrEmpty(recordsCheckDto.getDtRecCheckReceived()) && TypeConvUtil.isNullOrEmpty(recordsCheckDto.getIndRapBackSubscriptionCopied()))){
						recordsCheckDto.setDisableRapBackSubsCopied(false);
					}
					if(indModify && (!TypeConvUtil.isNullOrEmpty(recordsCheckDto.getDtRecCheckReceived()) && !TypeConvUtil.isNullOrEmpty(recordsCheckDto.getIndRapBackSubscriptionCopied()) &&(recordsCheckDto.getIndRapBackSubscriptionCopied().equals(WebConstants.YES)) && TypeConvUtil.isNullOrEmpty(recordsCheckDto.getDtRapBackExp()))){
						recordsCheckDto.setDisableDtRapBackExp(false);
					}
					if( (indModify && !TypeConvUtil.isNullOrEmpty(recordsCheckDto.getIndCrimHistoryResultCopied()) && recordsCheckDto.getIndCrimHistoryResultCopied().equals(WebConstants.YES)
							&& (recordsCheckDto.isDisableCrimResult() && TypeConvUtil.isNullOrEmpty(recordsCheckDto.getTxtDpsSID())) && ObjectUtils.isEmpty(recordsCheckDto.getDtRecCheckCompleted()))){
						recordsCheckDto.setDisabletxtDpsSID(false);
					}
					if(indModify &&(!TypeConvUtil.isNullOrEmpty(recordsCheckDto.getIndRapBackSubscriptionCopied()) && (recordsCheckDto.getIndRapBackSubscriptionCopied().equals(WebConstants.YES)) && (TypeConvUtil.isNullOrEmpty(recordsCheckDto.getCdORIAccntNum()) || TypeConvUtil.isNullOrEmpty(recordsCheckDto.getDtRapBackExp())))){
						recordsCheckDto.setDisableORIAccount(false);
					}
			}
			}
		}
		if (!NEW.equals(pageMode) && !canDeleteRecordCheck(recordsCheckDto.getRecCheckCheckType())) {
			indDeletable = false;
			recordsCheckDto.setCheckedBoxesForDelete(new ArrayList<Integer>());
			indDisableType = true;
		}

		if (!ObjectUtils.isEmpty(recordsCheckDto.getRecCheckCheckType())
				&& (CodesConstant.CCHKTYPE_81.equals(recordsCheckDto.getRecCheckCheckType()))){
			if (!NEW.equals(pageMode)) {
				indDisableType = true;
			}
			boolean isABCS = isABCSCheckForRapBack(recordsCheckDto.getIdRecordCheckPerson());
			recordsCheckDto.setTxtDpsSIDForOrignFpChck(getTxtDpsSIDForOrignFpChck(recordsCheckDto.getIdRecordCheckPerson()));
			recordsCheckDto.setAbcsCheck(isABCS);

			if(isABCS){
				indShowCompletedCkbox = true;
				indDisableCompletedCkbox = true;
				if (indModify && !ObjectUtils.isEmpty(recordsCheckDto.getDtRecCheckCompleted())) {
					indCompletedCkboxChkd = true;
				}
			}

			if(ObjectUtils.isEmpty(recordsCheckDto.getIndCrimHistoryResultCopied())){
				recordsCheckDto.setIndCrimHistoryResultCopied(WebConstants.YES);
			}
			if( (indModify && !TypeConvUtil.isNullOrEmpty(recordsCheckDto.getIndCrimHistoryResultCopied())
					&& recordsCheckDto.getIndCrimHistoryResultCopied().equals(WebConstants.YES)
					&& (recordsCheckDto.isDisableCrimResult()
					&& TypeConvUtil.isNullOrEmpty(recordsCheckDto.getTxtDpsSID())))){
				recordsCheckDto.setDisabletxtDpsSID(false);
			}
			if (indModify && !ObjectUtils.isEmpty(recordsCheckDto.getIndCrimHistoryResultCopied())
					&& recordsCheckDto.getIndCrimHistoryResultCopied().equals(WebConstants.YES)
					&& !TypeConvUtil.isNullOrEmpty(recordsCheckDto.getTxtDpsSID())
					&& (ObjectUtils.isEmpty(recordsCheckDto.getDtRecCheckCompleted()) && ObjectUtils.isEmpty(recordsCheckDto.getRecCheckStatus()))){
				recordsCheckDto.setShowAddResultsButton(true);
				recordsCheckDto.setDisabletxtDpsSID(false);
			}
			if (indModify && !ObjectUtils.isEmpty(recordsCheckDto.getIndCrimHistoryResultCopied())
					&& recordsCheckDto.getIndCrimHistoryResultCopied().equals(WebConstants.YES)
					&& !TypeConvUtil.isNullOrEmpty(recordsCheckDto.getTxtDpsSID())){
				if((!isABCS && !ObjectUtils.isEmpty(recordsCheckDto.getDtRecCheckCompleted()))
						|| !ObjectUtils.isEmpty(recordsCheckDto.getRecCheckStatus()) ){
					indResultsDisabled = false;
					recordsCheckDto.setDisabletxtDpsSID(true);
				}
			}

			if (indModify && !ObjectUtils.isEmpty(recordsCheckDto.getIndCrimHistoryResultCopied())
					&& recordsCheckDto.getIndCrimHistoryResultCopied().equals(WebConstants.NO)){
				indResultsDisabled = false;
			}

			if( indModify
					&& isABCS && !ObjectUtils.isEmpty(recordsCheckDto.getDtRecCheckReceived()) &&
					!ObjectUtils.isEmpty(recordsCheckDto.getRecCheckStatus())){
				if(ObjectUtils.isEmpty(recordsCheckDto.getCdRapBackReview())){
					recordsCheckDto.setCdRapBackReview(CodesConstant.RBRVW_NEW);
				}
				if(!ObjectUtils.isEmpty(recordsCheckDto.getCdRapBackReview())
						&& CodesConstant.RBRVW_COMP.equalsIgnoreCase(recordsCheckDto.getCdRapBackReview()) ){
					recordsCheckDto.setIndDisableRapBackReview(true);
				}

			}else{
				recordsCheckDto.setIndDisableRapBackReview(true);
			}
		}


		if (!ObjectUtils.isEmpty(recordsCheckDto.getRecCheckCheckType())
				&& CodesConstant.CCHKTYPE_10.equals(recordsCheckDto.getRecCheckCheckType())
				&& StringHelper.isValid(recordsCheckDto.getRecCheckStatus())
				&& !recordsBusinessDelegate.isIndDpsAccess()) {
			// only DPS records check have Results. Show results if type = 10,
			// show the
			// Results btn
			// also have to check that there is a status.
			indResultsDisabled = false;
		}
		if (indModify && !ObjectUtils.isEmpty(recordsCheckDto.getRecCheckCheckType())
				&& CodesConstant.CCHKTYPE_80.equals(recordsCheckDto.getRecCheckCheckType())
				&& StringHelper.isValid(recordsCheckDto.getRecCheckStatus())
				&& !recordsBusinessDelegate.isIndFBIAccess()) {
			// Also verify record check status is null so we can provide access
			// to
			// results should batch processing post an FBI fingerprint result
			// to an FBI Fingerprint Check created before SIR rollout.
			if (DateFormatUtil.isBefore(requestDt, DateFormatUtil.FINGERPRINT_AUTOMATION_DATE)
					&& ObjectUtils.isEmpty(recordsCheckDto.getRecCheckStatus())) {
				indResultsDisabled = true;
			} else {
				indResultsDisabled = false;
			}

		}
		if (!NEW.equals(pageMode) && !INQUIRE.equals(pageMode)
				&& recordCheckUtil.canHaveNarrative(recordsCheckDto.getRecCheckCheckType(), dtRequest)) {
			indEBCNarrativeDisabled = false;

			// - Do not show Records Check narrative button for
			// DPS Direct Check if user is assigned the
			// Restrict DPS Results security attribute
			if (!ObjectUtils.isEmpty(recordsCheckDto.getRecCheckCheckType())
					&& CodesConstant.CCHKTYPE_25.equals(recordsCheckDto.getRecCheckCheckType())
					&& recordsBusinessDelegate.isIndDpsAccess()) {
				indEBCNarrativeDisabled = true;
			}

			// - Do not show Records Check narrative button for
			// any FBI Exigent Checks OR (FBI Fingerprint Checks
			// created before rollout of SIR 26251) if user is
			// assigned the Restrict FBI Results security attribute

			// artf51666 - changed from CCHKTYPE_15 to CCHKTYPE_95
			if ((!ObjectUtils.isEmpty(recordsCheckDto.getRecCheckCheckType())
					&& CodesConstant.CCHKTYPE_80.equals(recordsCheckDto.getRecCheckCheckType())
					|| CodesConstant.CCHKTYPE_95.equals(recordsCheckDto.getRecCheckCheckType()))
					&& recordsBusinessDelegate.isIndFBIAccess()) {
				indEBCNarrativeDisabled = true;

			} else if (!ObjectUtils.isEmpty(recordsCheckDto.getRecCheckCheckType())
					&& CodesConstant.CCHKTYPE_80.equals(recordsCheckDto.getRecCheckCheckType())) {
				// - Do not show Records Check Narrative button for
				// FBI Fingerprint Checks created after deployment of SIR.
				// These narratives will now be stored in Criminal History.
				if (DateFormatUtil.isBefore(requestDt, DateFormatUtil.FINGERPRINT_AUTOMATION_DATE)) {
					indEBCNarrativeDisabled = false;
				} else {
					indEBCNarrativeDisabled = true;
				}
			}
		}

		if (INQUIRE.equals(pageMode)) {
			indResultsDisabled = true;
		}
		if (!ObjectUtils.isEmpty(recordsCheckDto.getRecCheckCheckType())
				&& CodesConstant.CCHKTYPE_75.equals(recordsCheckDto.getRecCheckCheckType())) {

			indShowCompletedCkbox = true;
			sectionHeading = FPS_HIST_DEC;
			/*
			 * remove RE-Eligible,Bar - RE Not Requested, Bar - RE Denied, Clear - RE
			 * Approved from determination dropdown these are no valid fields for FPS
			 * HistoryCheck Batch.
			 */
			excludeDeterm.remove(CodesConstant.CDETERM_CLAP);
			excludeDeterm.remove(CodesConstant.CDETERM_BRNR);
			excludeDeterm.remove(CodesConstant.CDETERM_BRDN);
			excludeDeterm.remove(CodesConstant.CDETERM_REEL);

			// Call the business delegate method to check if the record check is
			// Casa Fps
			// check
			if (isCasaFpsCheck(recordsCheckDto.getIdRecCheck())) {
				excludeDeterm.remove(CodesConstant.CDETERM_NOTA);
			}

			// Call the business delegate method to get the contract id
			/* Code changes for artf171305 */
			RecordsCheckRes recordsCheckRes = getAbcsContractID(recordsCheckDto.getIdRecCheck());
			if (!ObjectUtils.isEmpty(recordsCheckRes)&& !ObjectUtils.isEmpty(recordsCheckRes.getIdContract())) {
				idContract = recordsCheckRes.getIdContract().intValue();
			/* End code changes for artf171305 */
				if (0 < idContract) {
					recordsCheckDto.setIdContract(idContract);
					// call the business delegate method to get the contract
					// number
					String contractNumber = getScorContractNbr(recordsCheckDto.getIdRecCheck());
					if (!ObjectUtils.isEmpty(contractNumber)) {
						returnMap.put("contractnbr", contractNumber);
					}
				}

			}
			if (indModify && (StringHelper.isValid(selFPSDet) && !CodesConstant.CDETERM_MHPA.equals(selFPSDet))) {
				indSelectDetermin = true;

				if (indModify && !ObjectUtils.isEmpty(selFPSDet) && CodesConstant.CDETERM_CLER.equals(selFPSDet)
						|| !ObjectUtils.isEmpty(recordsCheckDto.getDtClrdEmailRequested())) {
					// For IMPACT users only, show ABCs Send Email button
					// for valid FPS History ABCs requests (which will have an
					// associated
					// ABCs contract ID). This should not occur in production
					// but test
					// environments often have IMPACT and ABCs databases which
					// are out of sync.
					if (0 < idContract) {
						recordsCheckDto.setIdContract(idContract);
						// Do Not Show Email button or Requested Date
						// in history section for FPS History - Batch Checks
						// which have been auto-cleared (CD_REC_CHECK_STATUS =
						// 'N')
						if (indModify && !(CodesConstant.CCRIMSTA_N.equals(recordsCheckDto.getRecCheckStatus()))) {
							// If Date Cleared Email Requested is not valid
							// date, show email button

							if (indModify && ObjectUtils.isEmpty(recordsCheckDto.getDtClrdEmailRequested())
									&& !isCasaFpsCheck(recordsCheckDto.getIdRecCheck())) {
								indShowEmailButton = true;
							}
							indShowEmailDate = true;

						}
					}
				}

			}

			if (indModify && !ObjectUtils.isEmpty(recordsCheckDto.getDtRecCheckCompleted())) {
				indDisableCompletedCkbox = true;
				indDisableCompletedDate = true;
				indCompletedCkboxChkd = true;
			} else {
				indIncompleteBatchCk = true;
			}
		}

		if (!ObjectUtils.isEmpty(recordsCheckDto.getRecCheckCheckType())
				&& CodesConstant.CCHKTYPE_10.equals(recordsCheckDto.getRecCheckCheckType())) {
			/* Code changes for artf171305 */
			RecordsCheckRes recordsCheckRes = getAbcsContractID(recordsCheckDto.getIdRecCheck());
			if (!ObjectUtils.isEmpty(recordsCheckRes) && !ObjectUtils.isEmpty(recordsCheckRes.getIdContract()) && recordsCheckRes.getIdContract().intValue() > 0) {
				idContract = recordsCheckRes.getIdContract().intValue();
			/* End code changes for artf171305 */
				recordsCheckDto.setIdContract(idContract);
			}
		}
		List<RecordsCheckDeterminationDto> determinationDtos = new ArrayList<RecordsCheckDeterminationDto>();

		if (indModify && !CollectionUtils.isEmpty(recordsCheckDto.getRecordsCheckDetermination())) {
			determinationDtos = recordsCheckDto.getRecordsCheckDetermination();
			historyCount = 0;
			for (int determination = 0; determination < determinationDtos.size(); determination++) {
				RecordsCheckDeterminationDto row = determinationDtos.get(determination);
				if (StringHelper.isValid(row.getRecChkDeterm())) {
					historyCount++;
				}
			}

		}
		recordsCheckDto.setRecordsCheckDeterminationListStr(recordCheckUtil.objectToJsonString(determinationDtos));
		recordsCheckDto.setHistoryCount(historyCount);
		if (!ObjectUtils.isEmpty(recordsCheckDto.getRecCheckCheckType())
				&& (CodesConstant.CCHKTYPE_80.equals(recordsCheckDto.getRecCheckCheckType())
				|| CodesConstant.CCHKTYPE_81.equals(recordsCheckDto.getRecCheckCheckType()))
				&& !DateFormatUtil.isBefore(requestDt, DateFormatUtil.FINGERPRINT_AUTOMATION_DATE)) {
			indDisableCompletedDate = true;
			indDisableDate = true;
		}
		// artf51666 - changed from CCHKTYPE_15 to CCHKTYPE_95
		if (indModify && !ObjectUtils.isEmpty(recordsCheckDto.getRecCheckCheckType())
				&& CodesConstant.CCHKTYPE_95.equals(recordsCheckDto.getRecCheckCheckType())
				&& (!ObjectUtils.isEmpty(recordsCheckDto.getDtRecCheckCompleted()))) {
			indDisableCompletedDate = true;
			indDisableDate = true;
		}
		if (indModify && !ObjectUtils.isEmpty(recordsCheckDto.getRecCheckCheckType())
				&& CodesConstant.CCHKTYPE_80.equals(recordsCheckDto.getRecCheckCheckType())
				&& !ObjectUtils.isEmpty(recordsCheckDto.getRecCheckContMethod())) {
			indDisableemailPhone = true;
		}

		if (!ObjectUtils.isEmpty(recordsCheckDto.getRecCheckCheckType())
				&& !CodesConstant.CCHKTYPE_10.equals(recordsCheckDto.getRecCheckCheckType())) {
			excludeDeterm.remove(CodesConstant.CDETERM_POSS);
		}

		// Call the business delegate to get isABCSCheck value
		boolean isABCSCheck = false;
		if (!ObjectUtils.isEmpty(recordsCheckDto.getIdRecCheck()) && recordsCheckDto.getIdRecCheck() != 0l) {
			if (!ObjectUtils.isEmpty(recordsCheckDto.getRecCheckCheckType())
					&& (CodesConstant.CCHKTYPE_81.equals(recordsCheckDto.getRecCheckCheckType()))){
				isABCSCheck = isABCSCheckForRapBack(recordsCheckDto.getIdRecordCheckPerson());
			}else{
			isABCSCheck = isABCSCheck(recordsCheckDto.getIdRecCheck());
		}

		}

		recordsCheckDto.setAbcsCheck(isABCSCheck);
		if (indModify && isABCSCheck && !ObjectUtils.isEmpty(recordsCheckDto.getRecCheckCheckType())
				&& CodesConstant.CCHKTYPE_10.equals(recordsCheckDto.getRecCheckCheckType())
				&& !ObjectUtils.isEmpty(recordsCheckDto.getRecCheckStatus())
				&& CodesConstant.CCRIMSTA_N.equals(recordsCheckDto.getRecCheckStatus())) {
			indShowEmailDate = false;
			indShowEmailButton = false;
		}
		// get the map for check type
		Map<String, String> recordCheckSearchType = new TreeMap<String, String>();
		recordCheckSearchType = getSearchTypeList(pageMode, recordsBusinessDelegate.getCdStage(), indDeletable,
				recordsBusinessDelegate.isIndRecCheckAccess());
		if (indDeletable && !NEW.equals(pageMode)) {
			returnMap.put("bDisplayDelete", Boolean.TRUE);

		} else {
			returnMap.put("bDisplayDelete", Boolean.FALSE);

		}

		// Logic for displaying the Review Now/Review Later checkboxes

		if (!ObjectUtils.isEmpty(recordsCheckDto.getRecCheckCheckType())
				&& CodesConstant.CCHKTYPE_10.equals(recordsCheckDto.getRecCheckCheckType()) && NEW.equals(pageMode)
				|| (!isABCSCheck && (MODIFY.equals(pageMode) || INQUIRE.equals(pageMode))
						&& !ObjectUtils.isEmpty(recordsCheckDto)
						&& !ObjectUtils.isEmpty(recordsCheckDto.getDtRecCheckRequest())
						&& DateFormatUtil.isAfter(recordsCheckDto.getDtRecCheckRequest(), DateFormatUtil.CCH_DPS_WS)
						&& !ObjectUtils.isEmpty(recordsCheckDto.getRecCheckCheckType())
						&& CodesConstant.CCHKTYPE_10.equals(recordsCheckDto.getRecCheckCheckType()))) {
			recordsCheckDto.setShowRadioButton(true);
			if (!ObjectUtils.isEmpty(recordsCheckDto.getIndReviewNow())
					&& recordsCheckDto.getIndReviewNow().equalsIgnoreCase(WebConstants.YES)) {
				recordsCheckDto.setIndReviewNow(REVIEW_NOW);
			} else if (!ObjectUtils.isEmpty(recordsCheckDto.getIndReviewNow())
					&& recordsCheckDto.getIndReviewNow().equalsIgnoreCase(WebConstants.NO)) {
				recordsCheckDto.setIndReviewNow(REVIEW_LATER);
			} else {
				recordsCheckDto.setIndReviewNow(REVIEW_NOW);
			}
		} else {
			recordsCheckDto.setShowRadioButton(false);

		}
		if (indModify && !indResultsDisabled && !ObjectUtils.isEmpty(recordsCheckDto.getRecCheckStatus())
				&& !(CodesConstant.CCRIMSTA_S).equals(recordsCheckDto.getRecCheckStatus())
				&& !(CodesConstant.CCRIMSTA_W).equals(recordsCheckDto.getRecCheckStatus())
				&& !(CodesConstant.CCRIMSTA_X).equals(recordsCheckDto.getRecCheckStatus())
				&& !(CodesConstant.CCRIMSTA_Y).equals(recordsCheckDto.getRecCheckStatus())
				&& !(CodesConstant.CCRIMSTA_Z).equals(recordsCheckDto.getRecCheckStatus())
				&& !(CodesConstant.CCRIMSTA_ZA).equals(recordsCheckDto.getRecCheckStatus())) {
			recordsCheckDto.setShowResultsButton(true);
		} else {
			recordsCheckDto.setShowResultsButton(false);
		}

		// Condition for displaying the Contact Section
		if (!ObjectUtils.isEmpty(recordsCheckDto.getRecCheckCheckType())
				&& CodesConstant.CCHKTYPE_80.equals(recordsCheckDto.getRecCheckCheckType())) {
			if (ObjectUtils.isEmpty(recordsCheckDto.getRecCheckContMethod())) {
				recordsCheckDto.setRecCheckContMethod(PHN_CODE);
			}

		}
		// Getting the phone and email list for Search Type 80 and 15
		// artf51666 - changed from CCHKTYPE_15 to CCHKTYPE_95
		if (!ObjectUtils.isEmpty(recordsCheckDto.getRecCheckCheckType())
				&& (CodesConstant.CCHKTYPE_80.equals(recordsCheckDto.getRecCheckCheckType())
						|| CodesConstant.CCHKTYPE_95.equals(recordsCheckDto.getRecCheckCheckType()))) {

			populateEmailPhoneList(recordsCheckDto, returnMap, idPerson);
		}
		if (MODIFY.equals(pageMode) && CodesConstant.CCHKTYPE_80.equals(recordsCheckDto.getRecCheckCheckType())
				&& (ObjectUtils.isEmpty(recordsCheckDto.getDtRecCheckCompleted()))) {
			Map<String, String> cancelReasonList = new TreeMap<String, String>();
			cancelReasonList = createCancelReasonMap();
			recordsCheckDto.setCancelReasonList(cancelReasonList);
		}
		if (ObjectUtils.isEmpty(recordsCheckDto.getDtRecCheckRequest())) {
			recordsCheckDto.setDtRecCheckRequest(new Date());
		}
		returnMap.put("bEBCNarrativeDisabled", indEBCNarrativeDisabled);
		returnMap.put("sectionHeading", sectionHeading);

		// unsubscribe to rapback BOC
		// if the subscription type filed is empty then enable unsubscribe
		if(ObjectUtils.isEmpty(recordsCheckDto.getIndRapBackUnSubScrType())){
			recordsCheckDto.setDisableUnsubcribeOptions(false);
		}
		// disable fields on once status is unsubscribed
		if(!ObjectUtils.isEmpty(recordsCheckDto.getIndRapBackUnSubScrType())){
			recordsCheckDto.setDisableORIAccount(true);
			recordsCheckDto.setDisableDtRapBackExp(true);
			recordsCheckDto.setDisableFRBSlectionOptions(true);
		}
		// if batch updated subscription status then hide the unsubscribe section
		if( !ObjectUtils.isEmpty(recordsCheckDto.getCdFbiSubscriptionDStatus()) ){
			// disable fields on once status is unsubscribed by batch
			if(recordsCheckDto.getCdFbiSubscriptionDStatus().equals(WebConstants.UNS)){
				recordsCheckDto.setDisableORIAccount(true);
				recordsCheckDto.setDisableDtRapBackExp(true);
				recordsCheckDto.setDisableFRBSlectionOptions(true);
			}
			if(recordsCheckDto.getCdFbiSubscriptionDStatus().equals(WebConstants.UNS) && ObjectUtils.isEmpty(recordsCheckDto.getIndRapBackUnSubScrType())){
				recordsCheckDto.setShowRapBackUnsubscribeSection(false);
			}
		}else{ // if subscription status is null then hide unsubscribe section
			recordsCheckDto.setShowRapBackUnsubscribeSection(false);
		}

		if( Objects.nonNull(recordsCheckDto.getContractType()) && (recordsCheckDto.getContractType().equals("PCS")))
		{
			if((Objects.nonNull(recordsCheckDto.getIdPdbBackgroundCheck()) && recordsCheckDto.getIdPdbBackgroundCheck()!=0)
					&& (DateUtil.isAfter(recordsCheckDto.getDtRecCheckRequest(), rapBackReleaseDt))
					&& (CodesConstant.CCHKTYPE_75.equals(recordsCheckDto.getRecCheckCheckType()))
					&& ("N".equalsIgnoreCase(recordsCheckDto.getIndClearedEmail()) || Objects.isNull(recordsCheckDto.getDtClearedEmailSent()))
					&&(isShowEligibleEmailButton)
			)
					//&& (CodesConstant.CDETERM_ELGB.equals(recordsCheckDto.getRecChkDeterm()) || CodesConstant.CDETERM_PFRBE.equals(recordsCheckDto.getRecChkDeterm())))
			{
				recordsCheckDto.setShowEligibleEmailButton(true);
			}

			if((Objects.nonNull(recordsCheckDto.getIdPdbBackgroundCheck()) && recordsCheckDto.getIdPdbBackgroundCheck()!=0)
					//&& (DateUtil.isAfter(recordsCheckDto.getDtRecCheckRequest(), rapBackReleaseDt))
					&& (CodesConstant.CCHKTYPE_75.equals(recordsCheckDto.getRecCheckCheckType()))
					&& ("Y".equalsIgnoreCase(recordsCheckDto.getIndClearedEmail()) && null !=recordsCheckDto.getDtClearedEmailSent())
					&&(isShowEligibleEmailButton))
			{
				recordsCheckDto.setShowResendEligibleEmailButton(true);
			}

			/** code to show send Ineligible Email Button for 80 records **/
			if ((Objects.nonNull(recordsCheckDto.getIdPdbBackgroundCheck()) && recordsCheckDto.getIdPdbBackgroundCheck()!=0)
					&& (DateUtil.isAfter(recordsCheckDto.getDtRecCheckRequest(), rapBackReleaseDt))
					&& (/*CodesConstant.CCHKTYPE_10.equals(recordsCheckDto.getRecCheckCheckType())
				&&*/ (CodesConstant.CCHKTYPE_75.equals(recordsCheckDto.getRecCheckCheckType())))
					&& ("N".equalsIgnoreCase(recordsCheckDto.getIndClearedEmail()) || Objects.isNull(recordsCheckDto.getDtClearedEmailSent()))
					&& (isShowInEligibleEmailButton))
			{
				recordsCheckDto.setShowIneligibleEmailButton(true);
			}

			if ((Objects.nonNull(recordsCheckDto.getIdPdbBackgroundCheck()) && recordsCheckDto.getIdPdbBackgroundCheck()!=0)
					&& (DateUtil.isAfter(recordsCheckDto.getDtRecCheckRequest(), rapBackReleaseDt))
					&& ( (CodesConstant.CCHKTYPE_75.equals(recordsCheckDto.getRecCheckCheckType())))
					&& ("Y".equalsIgnoreCase(recordsCheckDto.getIndClearedEmail()) && null !=recordsCheckDto.getDtClearedEmailSent())
					&& (isShowInEligibleEmailButton))
			{
				recordsCheckDto.setShowResendIneligibleEmailButton(true);
			}

		}else {
			/** code to show Eligible Email Button for 80 records **/
			if ((Objects.nonNull(recordsCheckDto.getIdPdbBackgroundCheck()) && recordsCheckDto.getIdPdbBackgroundCheck() != 0)
					&& (DateUtil.isAfter(recordsCheckDto.getDtRecCheckRequest(), rapBackReleaseDt))
					&& (CodesConstant.CCHKTYPE_75.equals(recordsCheckDto.getRecCheckCheckType())
					|| CodesConstant.CCHKTYPE_10.equals(recordsCheckDto.getRecCheckCheckType())
					|| CodesConstant.CCHKTYPE_80.equals(recordsCheckDto.getRecCheckCheckType()))
					&& ("N".equalsIgnoreCase(recordsCheckDto.getIndClearedEmail()) || Objects.isNull(recordsCheckDto.getDtClearedEmailSent()))
					&& (CodesConstant.CDETERM_ELGB.equals(recordsCheckDto.getRecChkDeterm()) || CodesConstant.CDETERM_PFRBE.equals(recordsCheckDto.getRecChkDeterm()))) {
				recordsCheckDto.setShowEligibleEmailButton(true);
			}

			/** code to show Resend Eligible Email Button for 80 records **/
			if((Objects.nonNull(recordsCheckDto.getIdPdbBackgroundCheck()) && recordsCheckDto.getIdPdbBackgroundCheck()!=0)
					//&& (DateUtil.isAfter(recordsCheckDto.getDtRecCheckRequest(), rapBackReleaseDt))
					&& (CodesConstant.CCHKTYPE_75.equals(recordsCheckDto.getRecCheckCheckType())
					||(CodesConstant.CCHKTYPE_80.equals(recordsCheckDto.getRecCheckCheckType()))
					|| CodesConstant.CCHKTYPE_10.equals(recordsCheckDto.getRecCheckCheckType()))
					&& ("Y".equalsIgnoreCase(recordsCheckDto.getIndClearedEmail()) && null !=recordsCheckDto.getDtClearedEmailSent())
					&& (CodesConstant.CDETERM_ELGB.equals(recordsCheckDto.getRecChkDeterm()) || CodesConstant.CDETERM_PFRBE.equals(recordsCheckDto.getRecChkDeterm()))
			)
			{
				recordsCheckDto.setShowResendEligibleEmailButton(true);
			}

			/** code to show send Ineligible Email Button for 80 records **/
			if ((Objects.nonNull(recordsCheckDto.getIdPdbBackgroundCheck()) && recordsCheckDto.getIdPdbBackgroundCheck()!=0)
					&& (DateUtil.isAfter(recordsCheckDto.getDtRecCheckRequest(), rapBackReleaseDt))
					&& (CodesConstant.CCHKTYPE_10.equals(recordsCheckDto.getRecCheckCheckType())
					|| (CodesConstant.CCHKTYPE_75.equals(recordsCheckDto.getRecCheckCheckType())))
					&& ("N".equalsIgnoreCase(recordsCheckDto.getIndClearedEmail()) || Objects.isNull(recordsCheckDto.getDtClearedEmailSent()))
					&& (CodesConstant.CDETERM_INEG.equals(recordsCheckDto.getRecChkDeterm()) || CodesConstant.CDETERM_PFRBI.equals(recordsCheckDto.getRecChkDeterm())))
			{
				recordsCheckDto.setShowIneligibleEmailButton(true);
			}

			if ((Objects.nonNull(recordsCheckDto.getIdPdbBackgroundCheck()) && recordsCheckDto.getIdPdbBackgroundCheck()!=0)
					&& (DateUtil.isAfter(recordsCheckDto.getDtRecCheckRequest(), rapBackReleaseDt))
					&& (CodesConstant.CCHKTYPE_10.equals(recordsCheckDto.getRecCheckCheckType())
					|| (CodesConstant.CCHKTYPE_75.equals(recordsCheckDto.getRecCheckCheckType())))
					&& ("Y".equalsIgnoreCase(recordsCheckDto.getIndClearedEmail()) && null !=recordsCheckDto.getDtClearedEmailSent())
					&& (CodesConstant.CDETERM_INEG.equals(recordsCheckDto.getRecChkDeterm()) || CodesConstant.CDETERM_PFRBI.equals(recordsCheckDto.getRecChkDeterm())))
			{
				recordsCheckDto.setShowResendIneligibleEmailButton(true);
			}
		}

		/** code to show Eligible Email Date for Eligible records **/
		if((Objects.nonNull(recordsCheckDto.getIdPdbBackgroundCheck()) && recordsCheckDto.getIdPdbBackgroundCheck()!=0)
				&& (DateUtil.isAfter(recordsCheckDto.getDtRecCheckRequest(), rapBackReleaseDt))
				&& (CodesConstant.CCHKTYPE_75.equals(recordsCheckDto.getRecCheckCheckType())
				|| CodesConstant.CCHKTYPE_10.equals(recordsCheckDto.getRecCheckCheckType())
				|| CodesConstant.CCHKTYPE_80.equals(recordsCheckDto.getRecCheckCheckType()))
				&& ("Y".equalsIgnoreCase(recordsCheckDto.getIndClearedEmail()) && null !=(recordsCheckDto.getDtClearedEmailSent()))
				&& (CodesConstant.CDETERM_ELGB.equals(recordsCheckDto.getRecChkDeterm())
				|| CodesConstant.CDETERM_PFRBE.equals(recordsCheckDto.getRecChkDeterm())))
		{
			recordsCheckDto.setShowEligibleEmailReqDate(true);
		}

		if(Objects.nonNull(recordsCheckDto.getIdPdbBackgroundCheck()) && recordsCheckDto.getIdPdbBackgroundCheck()!=0){
			recordsCheckDto.setStaff(true);
		}

		if ((getNewHireCount(recordsCheckDto.getIdRecCheck()) > 0)
				&& (DateUtil.isAfter(recordsCheckDto.getDtRecCheckRequest(), rapBackReleaseDt))
				&& (CodesConstant.CCHKTYPE_80.equals(recordsCheckDto.getRecCheckCheckType()))
				&& ("N".equalsIgnoreCase(recordsCheckDto.getIndClearedEmail()) || Objects.isNull(recordsCheckDto.getDtClearedEmailSent()))
				&& (CodesConstant.CDETERM_INEG.equals(recordsCheckDto.getRecChkDeterm()) || CodesConstant.CDETERM_PFRBI.equals(recordsCheckDto.getRecChkDeterm())))
		{
			recordsCheckDto.setShowIneligibleEmailButton(true);
		}

		if ((getNewHireCount(recordsCheckDto.getIdRecCheck()) > 0)
				&& (DateUtil.isAfter(recordsCheckDto.getDtRecCheckRequest(), rapBackReleaseDt))
				&& (CodesConstant.CCHKTYPE_80.equals(recordsCheckDto.getRecCheckCheckType()))
				&& ("Y".equalsIgnoreCase(recordsCheckDto.getIndClearedEmail()) && null !=(recordsCheckDto.getDtClearedEmailSent()))
				&& (CodesConstant.CDETERM_INEG.equals(recordsCheckDto.getRecChkDeterm()) || CodesConstant.CDETERM_PFRBI.equals(recordsCheckDto.getRecChkDeterm())))
		{
			recordsCheckDto.setShowResendIneligibleEmailButton(true);
		}

		/** code to show Eligible Email Date for Ineligible records **/
		if ((Objects.nonNull(recordsCheckDto.getIdPdbBackgroundCheck()) && recordsCheckDto.getIdPdbBackgroundCheck()!=0)
				&& (DateUtil.isAfter(recordsCheckDto.getDtRecCheckRequest(), rapBackReleaseDt))
				&& (CodesConstant.CCHKTYPE_10.equals(recordsCheckDto.getRecCheckCheckType())
				|| CodesConstant.CCHKTYPE_75.equals(recordsCheckDto.getRecCheckCheckType()))
				&& ("Y".equalsIgnoreCase(recordsCheckDto.getIndClearedEmail()) && null !=(recordsCheckDto.getDtClearedEmailSent()))
				&& (CodesConstant.CDETERM_INEG.equals(recordsCheckDto.getRecChkDeterm()) || CodesConstant.CDETERM_PFRBI.equals(recordsCheckDto.getRecChkDeterm())))
		{
			recordsCheckDto.setShowIneligibleEmailReqDate(true);
		}

		if ((getNewHireCount(recordsCheckDto.getIdRecCheck()) > 0)
				&& (DateUtil.isAfter(recordsCheckDto.getDtRecCheckRequest(), rapBackReleaseDt))
				&& (CodesConstant.CCHKTYPE_80.equals(recordsCheckDto.getRecCheckCheckType()))
				&& ("Y".equalsIgnoreCase(recordsCheckDto.getIndClearedEmail()) && null !=(recordsCheckDto.getDtClearedEmailSent()))
				&& (CodesConstant.CDETERM_INEG.equals(recordsCheckDto.getRecChkDeterm()) || CodesConstant.CDETERM_PFRBI.equals(recordsCheckDto.getRecChkDeterm())))
		{
			recordsCheckDto.setShowIneligibleEmailReqDate(true);
		}

//		/** code to show Resend Ineligible Email Button for 80 records **/
//		if ((Objects.nonNull(recordsCheckDto.getIdPdbBackgroundCheck()) && recordsCheckDto.getIdPdbBackgroundCheck()!=0)
//				&& (DateUtil.isAfter(recordsCheckDto.getDtRecCheckRequest(), rapBackReleaseDt))
//				&& (CodesConstant.CCHKTYPE_10.equals(recordsCheckDto.getRecCheckCheckType())
//				|| (CodesConstant.CCHKTYPE_75.equals(recordsCheckDto.getRecCheckCheckType())))
//				&& ("N".equalsIgnoreCase(recordsCheckDto.getIndClearedEmail()) && Objects.isNull(recordsCheckDto.getDtClearedEmailSent()))
//				&& (CodesConstant.CDETERM_INEG.equals(recordsCheckDto.getRecChkDeterm()) || CodesConstant.CDETERM_PFRBI.equals(recordsCheckDto.getRecChkDeterm())))
//		{
//			recordsCheckDto.setShowResendIneligibleEmailButton(true);
//		}
//
//		if ((getNewHireCount(recordsCheckDto.getIdRecCheck()) > 0)
//				&& (DateUtil.isAfter(recordsCheckDto.getDtRecCheckRequest(), rapBackReleaseDt))
//				&& (CodesConstant.CCHKTYPE_80.equals(recordsCheckDto.getRecCheckCheckType()))
//				&& ("N".equalsIgnoreCase(recordsCheckDto.getIndClearedEmail()) && Objects.isNull(recordsCheckDto.getDtClearedEmailSent()))
//				&& (CodesConstant.CDETERM_INEG.equals(recordsCheckDto.getRecChkDeterm()) || CodesConstant.CDETERM_PFRBI.equals(recordsCheckDto.getRecChkDeterm())))
//		{
//			recordsCheckDto.setShowResendIneligibleEmailButton(true);
//		}


		if((Objects.nonNull(recordsCheckDto.getIdPdbBackgroundCheck()) && recordsCheckDto.getIdPdbBackgroundCheck()!=0)
			&& CodesConstant.CCHKTYPE_80.equals(recordsCheckDto.getRecCheckCheckType())
			&& (!ObjectUtils.isEmpty(recordsCheckDto.getDtRecCheckRequest()) && (DateUtil.isAfter(recordsCheckDto.getDtRecCheckRequest(), rapBackReleaseDt))))
		{
			recordsCheckDto.setShowFbiNotifications(true);
		}

		// unsubscribe to rapback EOC
		recordsCheckDto.setDeterminationList(excludeDeterm);
		recordsCheckDto.setShowUploadedDocuments(indShowUploadedDocuments);
		recordsCheckDto.setShowNotifications(indShowNotifications);
		recordsCheckDto.setDisableemailPhone(indDisableemailPhone);
		recordsCheckDto.setCheckTypeList(recordCheckSearchType);
		recordsCheckDto.setDisableDate(indDisableDate);
		recordsCheckDto.setDeletable(indDeletable);
		recordsCheckDto.setResultsDisabled(indResultsDisabled);
		recordsCheckDto.setIndDisableType(indDisableType);
		recordsCheckDto.setIndEBCNarrativeDisabled(indEBCNarrativeDisabled);
		recordsCheckDto.setNarrModeView(narrModeView);
		recordsCheckDto.setDisableCompletedDate(indDisableCompletedDate);
		recordsCheckDto.setDisableCompletedCkbox(indDisableCompletedCkbox);
		recordsCheckDto.setShowEmailButton(indShowEmailButton);
		recordsCheckDto.setShowCompletedCkbox(indShowCompletedCkbox);
		recordsCheckDto.setCompletedCkboxChkd(indCompletedCkboxChkd);
		recordsCheckDto.setIncompleteBatchCk(indIncompleteBatchCk);
		recordsCheckDto.setShowEmailDate(indShowEmailDate);
		recordsCheckDto.setSelectDetermin(indSelectDetermin);
		recordsCheckDto.setHistorySection(indHistorySection);
		recordsCheckDto.setReviewNowLater(indReviewNowLater);
		recordsCheckDto.setExcludePossibleMatchFromDeterm(excludePossibleMatchFromDeterm);
		recordsCheckDto.setRapBackORIAccountList(getORIAccountList());
		returnMap.put("recordsCheckDto", recordsCheckDto);

		return returnMap;
	}

	/**
	 * Method Name: saveAndCompleteRecordsCheck Method Description:This method is
	 * used to save and complete Records Check Detail.
	 *
	 * @param checkDto
	 * @param sessionDtoList
	 * @param commonData
	 * @return returnList
	 */
	public List<Object> saveAndCompleteRecordsCheck(RecordsCheckDto checkDto, List<RecordsCheckDto> sessionDtoList,
			CommonDto commonData, URL styleSheetURL) {
		List<Object> returnList = new ArrayList<Object>();
		String idSaveRecCheck = null;
		String pageMode = null;

		if (!ObjectUtils.isEmpty(checkDto.getPageModeStr())) {
			pageMode = checkDto.getPageModeStr();
		}

		// Call the business delegate method for updating/saving the Records
		// Check
		// Detail
		RecordsCheckListRes recordsCheckSaveRes = saveRecordsCheckDetail(checkDto, sessionDtoList, commonData,
				styleSheetURL, null);
		if (!ObjectUtils.isEmpty(recordsCheckSaveRes) && ObjectUtils.isEmpty(recordsCheckSaveRes.getErrorDto())) {
			if (!ObjectUtils.isEmpty(recordsCheckSaveRes.getIdRecordCheck())
					&& recordsCheckSaveRes.getIdRecordCheck() != 0l) {
				idSaveRecCheck = String.valueOf(recordsCheckSaveRes.getIdRecordCheck());
			}
			returnList.add(0, pageMode);
			returnList.add(1, idSaveRecCheck);
		}else{
			returnList.add(recordsCheckSaveRes.getErrorDto());
		}
		return returnList;
	}

	/**
	 * Method Name: setCompletedEmailFlag Method Description:This method is used to
	 * set the Complete Email Flag.
	 *
	 * @param checkDto
	 * @param commonDto
	 * @return returnMapValues
	 */
	public Map<String, String> setCompletedEmailFlag(RecordsCheckDto checkDto, CommonDto commonDto, String hostName) {
		Map<String, String> returnMapValues = new HashMap<String, String>();
		RecordsCheckDetailReq emailFlagReq = null;
		String returnUrl = DEFAULT_REDIRECT_URL_COMPLETE_RECORD;
		log.info("PD 92219 - Added loggers: Inside setCompletedEmailFlag method");
		emailFlagReq = populateIndCompletedEmail(checkDto, commonDto, hostName);

		if (!ObjectUtils.isEmpty(emailFlagReq.getErrorMessage())) {
			String[] errorArray = emailFlagReq.getErrorMessage().split("\\|");
			if (errorArray.length != 0) {
				String destinationPage = errorArray[0];
				if (GENERIC_STRING.equals(destinationPage)) {
					returnMapValues.put("error", errorArray[1]);
					returnUrl = ERROR_URL_STRING;
				} else if (DETAIL_STRING.equals(destinationPage)) {
					returnMapValues.put("setIndEmailErrorMessage", errorArray[1]);
				} else if (INFORMATION_STRING.equals(destinationPage)) {
					returnMapValues.put("informationMessage", errorArray[1]);
				}
			}
		}

		if (!ObjectUtils.isEmpty(checkDto.getIdRecCheck()) && checkDto.getIdRecCheck() != 0L
				&& !isCasaFpsCheck(checkDto.getIdRecCheck()) && !returnUrl.equals(ERROR_URL_STRING)) {
			if ((getNewHireCount(checkDto.getIdRecCheck()) > 0)
					&& (CodesConstant.CDETERM_ELGB.equals(checkDto.getRecChkDeterm())
					|| CodesConstant.CDETERM_PFRBE.equals(checkDto.getRecChkDeterm()))) {
				if (!ObjectUtils.isEmpty(emailFlagReq)
						&& !CollectionUtils.isEmpty(emailFlagReq.getRecordsCheckDtoList())
						&& !ObjectUtils.isEmpty(emailFlagReq.getRecordsCheckDtoList().get(0))
						&& !ObjectUtils.isEmpty(emailFlagReq.getRecordsCheckDtoList().get(0).getDtClearedEmailSent())
						&& emailFlagReq.isEmailSend()) {
					// call the business delegate method to update the record check details
					setCompletedEmailFlag(emailFlagReq);
					returnMapValues.put("informationMessage", "Eligible Email sent successfully");
				} else {
					returnMapValues.put("informationMessage",
							!ObjectUtils.isEmpty(emailFlagReq.getErrorMessage()) ? emailFlagReq.getErrorMessage() : ERROR_ELIGIBLE_EMAIL);
				}
			}
			if ((getNewHireCount(checkDto.getIdRecCheck()) > 0)
					&& (CodesConstant.CDETERM_INEG.equals(checkDto.getRecChkDeterm())
					|| CodesConstant.CDETERM_PFRBI.equals(checkDto.getRecChkDeterm()))) {
				if (!ObjectUtils.isEmpty(emailFlagReq)
						&& !CollectionUtils.isEmpty(emailFlagReq.getRecordsCheckDtoList())
						&& !ObjectUtils.isEmpty(emailFlagReq.getRecordsCheckDtoList().get(0))
						&& !ObjectUtils.isEmpty(emailFlagReq.getRecordsCheckDtoList().get(0).getDtClearedEmailSent())
						&& emailFlagReq.isEmailSend()) {
					// call the business delegate method to update the record check details
					setCompletedEmailFlag(emailFlagReq);
					returnMapValues.put("informationMessage", "Ineligible Email sent successfully");
				} else {
					returnMapValues.put("informationMessage",
							!ObjectUtils.isEmpty(emailFlagReq.getErrorMessage()) ? emailFlagReq.getErrorMessage() : ERROR_INELIGIBLE_EMAIL);
				}
			}
			if (getNewHireCount(checkDto.getIdRecCheck()) == 0) {
				if (!ObjectUtils.isEmpty(emailFlagReq)
						&& !CollectionUtils.isEmpty(emailFlagReq.getRecordsCheckDtoList())
						&& !ObjectUtils.isEmpty(emailFlagReq.getRecordsCheckDtoList().get(0))
						&& !ObjectUtils.isEmpty(emailFlagReq.getRecordsCheckDtoList().get(0).getDtClearedEmailSent())
						&& emailFlagReq.isEmailSend()) {
					// call the business delegate method to update the record check details
					setCompletedEmailFlag(emailFlagReq);
					if(checkDto.isShowEligibleEmailButton() || checkDto.isShowResendEligibleEmailButton())
					{
						returnMapValues.put("informationMessage", "Eligible Email sent successfully");
					}
					else if(checkDto.isShowIneligibleEmailButton() || checkDto.isShowResendIneligibleEmailButton())
					{
						returnMapValues.put("informationMessage", "Ineligible Email sent successfully");
					}

				} else {
					returnMapValues.put("informationMessage",
							!ObjectUtils.isEmpty(emailFlagReq.getErrorMessage()) ? emailFlagReq.getErrorMessage() : ERROR_CLEARANCE_EMAIL);
				}
			}
		}
		if (!returnUrl.equals(ERROR_URL_STRING)) {
			returnUrl = "forward:/case/person/record/recordAction?pageMode=" + MODIFY + "&recordsCheckDetailIndex="
					+ checkDto.getIdRecCheck() + "";
		}
		returnMapValues.put("returnUrl", returnUrl);

		return returnMapValues;
	}


	/**
	 * Method Name: populateIndCompletedEmail Method Description:This method is used
	 * to populate the request for setting the Email Completed Flag.
	 *
	 * @param checkDto
	 * @return recordsCheckDetailReq
	 */
	private RecordsCheckDetailReq populateIndCompletedEmail(RecordsCheckDto checkDto, CommonDto commonDto,
			String hostName) {
		Date rapBackReleaseDt = DateUtil.toJavaDate(cacheAdapter.getDecode(CodesConstant.CRELDATE, CodesConstant.JUN_2024_RAPBACK));
		RecordsCheckDetailReq recordsCheckDetailReq = new RecordsCheckDetailReq();
		List<RecordsCheckDetailDto> recordsCheckDtoList = new ArrayList<RecordsCheckDetailDto>();
		RecordsCheckDetailDto recordsCheckDetailDto = new RecordsCheckDetailDto();
		String errorMessage = WebConstants.EMPTY_STRING;
		boolean emailSent = false;
		EacdRestEmailService eacdRestEmailService = new EacdRestEmailService();
		EacdResponse eacdResponse = null;
		recordsCheckDetailReq.setUserId(commonDto.getIdUserLogon());
		recordsCheckDetailReq.setReqFuncCd(WebConstants.REQ_FUNC_CD_UPDATE);
		recordsCheckDetailDto.setScrDataAction(WebConstants.REQ_FUNC_CD_UPDATE);
		recordsCheckDetailDto.setDtLastUpdate(checkDto.getDtLastUpdate());
		recordsCheckDetailDto.setIdRecCheck(checkDto.getIdRecCheck());
		recordsCheckDetailDto.setIdRecCheckRequestor(checkDto.getIdRecCheckRequestor());
		recordsCheckDetailDto.setIdStage(checkDto.getIdStage());
		recordsCheckDetailDto.setRecCheckCheckType(checkDto.getRecCheckCheckType());
		recordsCheckDetailDto.setRecCheckEmpType(checkDto.getRecCheckEmpType());
		recordsCheckDetailDto.setRecCheckStatus(checkDto.getRecCheckStatus());
		recordsCheckDetailDto.setDtRecCheckCompleted(checkDto.getDtRecCheckCompleted());
		recordsCheckDetailDto.setDtRecCheckRequest(checkDto.getDtRecCheckRequest());
		recordsCheckDetailDto.setRecCheckComments(checkDto.getRecCheckComments());
		recordsCheckDetailDto.setIndClearedEmail(checkDto.getIndClearedEmail());
		recordsCheckDetailDto.setDtClearedEmailSent(checkDto.getDtClearedEmailSent());
		recordsCheckDetailDto.setDtClrdEmailRequested(checkDto.getDtClrdEmailRequested());

		recordsCheckDetailDto.setRecCheckContMethod(checkDto.getRecCheckContMethod());
		recordsCheckDetailDto.setCdRecCheckServ(checkDto.getCdRecCheckServ());
		recordsCheckDetailDto.setRecCheckContMethodValue(checkDto.getRecCheckContMethodValue());

		checkDto.setRecChkDeterm(getCdDetermination(checkDto.getIdRecCheck()));

		recordsCheckDetailDto.setRecChkDeterm(ContextHelper.getStringSafe(checkDto.getRecChkDeterm()));
		//artf205009 setting PersonID to display the correct "Enter By" name in Record Detail page.
		recordsCheckDetailDto.setIdPerson(commonDto.getIdUser());
		recordsCheckDetailDto.setDtDetermFinal(checkDto.getDtDetermFinal());
		recordsCheckDetailDto.setRecCheckComments(ContextHelper.getStringSafe(checkDto.getRecCheckComments()));
		recordsCheckDetailDto.setIndCrimHistResultCopied(checkDto.getIndCrimHistoryResultCopied());
		recordsCheckDetailDto.setIndRapBackSubscriptionCopied(checkDto.getIndRapBackSubscriptionCopied());
		recordsCheckDetailDto.setTxtDpsSID(checkDto.getTxtDpsSID());

		log.info("PD 92219 - Added loggers: Inside populateIndCompletedEmail method");
		EmailNotificationDto emailNotificationDto = new EmailNotificationDto();
		boolean isAbcsPCSAccount = false;
		isAbcsPCSAccount = getAbcsContractID(checkDto.getIdRecCheck()).getContractType().equals("PCSX");

		if(isAbcsPCSAccount)
		{
			try{
				if ((getNewHireCount(checkDto.getIdRecCheck()) == 0)
						//&& (DateUtil.isAfter(checkDto.getDtRecCheckRequest(), rapBackReleaseDt))
						&& (CodesConstant.CCHKTYPE_75.equals(checkDto.getRecCheckCheckType()))
						&&  (checkDto.isShowEligibleEmailButton()||checkDto.isShowResendEligibleEmailButton())) {
					emailNotificationDto = generatePCSEligibleEmail(checkDto, commonDto.getIdPerson(), commonDto.getIdUser());
				}else if  ((getNewHireCount(checkDto.getIdRecCheck()) == 0)
						//&& (DateUtil.isAfter(checkDto.getDtRecCheckRequest(), rapBackReleaseDt))
						&& (CodesConstant.CCHKTYPE_75.equals(checkDto.getRecCheckCheckType()))
						&&  (checkDto.isShowIneligibleEmailButton()||checkDto.isShowResendIneligibleEmailButton())) {
					emailNotificationDto = generatePCSIneligibleEmail(checkDto, commonDto.getNmUserFullName(), commonDto.getIdPerson(),commonDto.getIdUser());
				}
				log.error(String.format("populateIndCompletedEmail:  sending email with record type: %s and emailNotificationDto: %s",
						checkDto.getRecCheckCheckType(),
						emailNotificationDto == null ? "emailNotificationDto is null  " : commonDto.getIdUser() + " - " + emailNotificationDto.getFromAddress()
								+ " - " + emailNotificationDto.getRecipientTo() + " - " + emailNotificationDto.getEmailToList()));
				if (emailNotificationDto != null && !ObjectUtils.isEmpty(emailNotificationDto.getMessage())
						&& !ObjectUtils.isEmpty(emailNotificationDto.getRecipientTo())
						&& !ObjectUtils.isEmpty(emailNotificationDto.getFromAddress())) {
					log.error("populateIndCompletedEmail: sendEmailWithConfirmation ");
					emailSent = sendEmailWithConfirmation(emailNotificationDto, hostName);
				}
				if (emailNotificationDto == null) {
					errorMessage = String.format("Error sending email for record type: %s : email notifications details are not available",
							checkDto.getRecCheckCheckType());
				}
			}catch (Exception ex) {
				errorMessage = "Exception sending email: " + ex.getMessage();
				log.error("populateIndCompletedEmail: Exception while sending email ", ex);
			}

		}
		else if (!isCasaFpsCheck(checkDto.getIdRecCheck()) && !isAbcsPCSAccount) {
			try {
				if ((getNewHireCount(checkDto.getIdRecCheck()) > 0)
						&& (CodesConstant.CCHKTYPE_80.equals(checkDto.getRecCheckCheckType())
						|| CodesConstant.CCHKTYPE_10.equals(checkDto.getRecCheckCheckType())
						|| CodesConstant.CCHKTYPE_75.equals(checkDto.getRecCheckCheckType()))
						&& (CodesConstant.CDETERM_ELGB.equals(checkDto.getRecChkDeterm())
						|| CodesConstant.CDETERM_PFRBE.equals(checkDto.getRecChkDeterm()))) {
					log.info("PD 92219 - Added loggers: Before calling generateEligibleEmail method - RecordCheckType " + checkDto.getRecCheckCheckType());
					emailNotificationDto = generateEligibleEmail(checkDto, commonDto.getNmUserFullName(), commonDto.getIdPerson());
				} else if (getNewHireCount(checkDto.getIdRecCheck()) > 0
						&& (CodesConstant.CCHKTYPE_80.equals(checkDto.getRecCheckCheckType())
						|| CodesConstant.CCHKTYPE_10.equals(checkDto.getRecCheckCheckType())
						|| CodesConstant.CCHKTYPE_75.equals(checkDto.getRecCheckCheckType()))
						&& (CodesConstant.CDETERM_INEG.equals(checkDto.getRecChkDeterm())
						|| CodesConstant.CDETERM_PFRBI.equals(checkDto.getRecChkDeterm()))) {
					emailNotificationDto = generateIneligibleEmail(checkDto, commonDto.getNmUserFullName(), commonDto.getIdPerson(),commonDto.getIdUser());
				} else if ((getNewHireCount(checkDto.getIdRecCheck()) == 0)
						&& (DateUtil.isAfter(checkDto.getDtRecCheckRequest(), rapBackReleaseDt))
						&& (CodesConstant.CCHKTYPE_80.equals(checkDto.getRecCheckCheckType()))
						&& (CodesConstant.CDETERM_ELGB.equals(checkDto.getRecChkDeterm())
						|| CodesConstant.CDETERM_PFRBE.equals(checkDto.getRecChkDeterm()))) {
					emailNotificationDto = generateFBIEligibleEmailExHire(checkDto, commonDto.getNmUserFullName());

				} else if ((getNewHireCount(checkDto.getIdRecCheck()) == 0)
						&& (DateUtil.isAfter(checkDto.getDtRecCheckRequest(), rapBackReleaseDt))
						&& (CodesConstant.CCHKTYPE_10.equals(checkDto.getRecCheckCheckType()) || CodesConstant.CCHKTYPE_75.equals(checkDto.getRecCheckCheckType()))
						  ) {
					emailNotificationDto = generatePSClearanceEmail(checkDto, commonDto.getIdPerson(),commonDto.getIdUser());
				}
				log.error(String.format("populateIndCompletedEmail:  sending email with record type: %s and emailNotificationDto: %s",
						checkDto.getRecCheckCheckType(),
						emailNotificationDto == null ? "emailNotificationDto is null  " : commonDto.getIdUser() + " - " + emailNotificationDto.getFromAddress()
								+ " - " + emailNotificationDto.getRecipientTo() + " - " + emailNotificationDto.getEmailToList()));

				if (emailNotificationDto != null && !ObjectUtils.isEmpty(emailNotificationDto.getMessage())
						&& !ObjectUtils.isEmpty(emailNotificationDto.getRecipientTo())
						&& !ObjectUtils.isEmpty(emailNotificationDto.getFromAddress())) {
					log.error("populateIndCompletedEmail: sendEmailWithConfirmation ");
					emailSent = sendEmailWithConfirmation(emailNotificationDto, hostName);
				}
				if (emailNotificationDto == null) {
					errorMessage = String.format("Error sending email for record type: %s : email notifications details are not available",
							checkDto.getRecCheckCheckType());
				}
			} catch (Exception ex) {
				errorMessage = "Exception sending email: " + ex.getMessage();
				log.error("populateIndCompletedEmail: Exception while sending email ", ex);
			}
		} else {
			String env = System.getProperty(WebConstants.ENVIRONMENT);
			log.info("env = " + env);
			if (!"PROD".equalsIgnoreCase(env)) {
				env = emailProperties.getProperty("sendClearedEmail.hostlookup." + env.toUpperCase(), env);
			}
			if (!StringHelper.isValid(env)) {
				errorMessage = "Generic|"
						+ "Environment variable $ENV is null; Unable to resolve Email web service url.";
			} else {
				String emailUri = cacheAdapter.getDecode(CodesConstant.URLCCEML, env);
				log.info("The emailUri from the database is " + emailUri);
				try {
					if (CodesConstant.CDETERM_CLER
							.equalsIgnoreCase(ContextHelper.getStringSafe(checkDto.getRecChkDeterm()))) {
						StringBuffer clearURIBuffer = new StringBuffer(emailUri)
								.append("sendCaseConnectionClearanceEmail/");
						log.info("The uri appended for clearance email is  " + clearURIBuffer);
						eacdResponse = eacdRestEmailService.sendEacdEmail((long) checkDto.getIdRecCheck(),
								commonDto.getIdUser(), clearURIBuffer.toString());
					} else if (CodesConstant.CDETERM_BAR
							.equalsIgnoreCase(ContextHelper.getStringSafe(checkDto.getRecChkDeterm()))) {
						StringBuffer clearURIBuffer = new StringBuffer(emailUri)
								.append("sendCaseConnectionBarredEmail/");
						log.info("The uri appended for bar email is  " + clearURIBuffer);
						eacdResponse = eacdRestEmailService.sendEacdEmail((long) checkDto.getIdRecCheck(),
								commonDto.getIdUser(), clearURIBuffer.toString());
					}
					if (ObjectUtils.isEmpty(eacdResponse)) {
						errorMessage = "Information|"
								+ cacheAdapter.getMessage(MessagesConstants.MSG_CC_EMAIL_SEND_DELAYED);
					} else if (!ObjectUtils.isEmpty(eacdResponse) && ObjectUtils.isEmpty(eacdResponse.getCode())) {
						errorMessage = "Information|"
								+ cacheAdapter.getMessage(MessagesConstants.MSG_CC_EMAIL_SEND_DELAYED);
					} else if (!ObjectUtils.isEmpty(eacdResponse)
							&& HTTP_ERROR.equalsIgnoreCase(eacdResponse.getCode().trim())) {
						errorMessage = "DetailPage|" + eacdResponse.getDescription();
					} else if (!ObjectUtils.isEmpty(eacdResponse) && !ObjectUtils.isEmpty(eacdResponse.getCode())
							&& !SUCCESS.equalsIgnoreCase(eacdResponse.getCode().trim())) {
						String errorMessageFromResponse = eacdResponse.getDescription();
						StringTokenizer messages = new StringTokenizer(errorMessageFromResponse.toString(), "|");
						while (messages.hasMoreTokens()) {
							String message = messages.nextToken().trim();
							if (StringHelper.isValid(message)) {
								errorMessage = "DetailPage|" + message.toString();
							}
						}

					} else if (!ObjectUtils.isEmpty(eacdResponse.getCode())
							&& SUCCESS.equalsIgnoreCase(eacdResponse.getCode().trim())) {
						errorMessage = "Information|" + eacdResponse.getDescription();
					}
				} catch (MalformedURLException ex) {
					errorMessage = "DetailPage|" + ex.getMessage();
				} catch (IOException ex) {
					errorMessage = "DetailPage|" + ex.getMessage();
				} catch (JSONException ex) {
					errorMessage = "DetailPage|" + ex.getMessage();
				}
			}
		}
		if (emailSent) {
			recordsCheckDetailDto.setDtClearedEmailSent(new Date());
			recordsCheckDetailDto.setDtClrdEmailRequested(new Date());
			recordsCheckDetailDto.setIndComplete(WebConstants.YES);
			recordsCheckDetailDto.setIndClearedEmail(WebConstants.YES);
		}
		recordsCheckDetailReq.setPageSizeNbr(1);
		recordsCheckDtoList.add(recordsCheckDetailDto);
		recordsCheckDetailReq.setRecordsCheckDtoList(recordsCheckDtoList);
		recordsCheckDetailReq.setIdRecCheckPerson(commonDto.getIdPerson());
		recordsCheckDetailReq.setErrorMessage(errorMessage);
		recordsCheckDetailReq.setEmailSend(emailSent);
		return recordsCheckDetailReq;
	}

	/**
	 * Method Name: deleteUploadedDocument Method Description:This method is used to
	 * delete the uploaded document.
	 *
	 * @param pageMode
	 * @param idDocRepository
	 * @param idRecordsCheck
	 * @return returnMapValues
	 */
	public Map<String, String> deleteUploadedDocument(String pageMode, Integer idDocRepository,
			Integer idRecordsCheck) {
		String returnUrl = "redirect:/case/person/record/recordAction?pageMode=" + pageMode
				+ "&recordsCheckDetailIndex=" + idRecordsCheck + "";
		Map<String, String> returnMapValues = new HashMap<String, String>();

		try {
			if (!ObjectUtils.isEmpty(idDocRepository)) {

				deleteDocument(idDocRepository, WebConstants.ABCS_APP_CODE);

				JSONObject jsonObject = recordCheckUtil.getDocumentJSON(WebConstants.ABCS_APP_CODE, idDocRepository);
				String nmDocString = null;
				try {
					if (!ObjectUtils.isEmpty(jsonObject)) {
						nmDocString = jsonObject.getString("nmDoc");
					}
				} catch (Exception e) {

				}
				if (ObjectUtils.isEmpty(jsonObject) || NULL_STRING.equals(nmDocString)
						|| !StringHelper.isValid(nmDocString)) {
					deleteDocumentPdbRecord(populateDocumentDeleteRequest(idDocRepository));
				} else {
					returnMapValues.put("errorMessageFromDeleteDocument",
							"An error occurred while deleting the document");
					returnUrl = "redirect:/case/person/record/displayRecordCheckList";
				}

			}
		} catch (Exception e) {
			returnMapValues.put("error", e.getMessage());
			returnUrl = ERROR_URL_STRING;
		}
		returnMapValues.put("returnUrl", returnUrl);
		return returnMapValues;
	}

	/**
	 * Method Name: populateDocumentDeleteRequest Method Description: This method is
	 * used to populate the request for document delete
	 *
	 * @param idDocRepository
	 * @return recordsCheckDetailReq
	 */
	private RecordsCheckDetailReq populateDocumentDeleteRequest(Integer idDocRepository) {
		RecordsCheckDetailReq recordsCheckDetailReq = new RecordsCheckDetailReq();
		recordsCheckDetailReq.setIdDocRepository(Long.valueOf(idDocRepository));
		return recordsCheckDetailReq;
	}

	/**
	 * Method Name: deleteDocument Method Description: This method is used to delete
	 * the document.
	 *
	 * @param idDocRepository
	 * @param applicationCode
	 */
	private void deleteDocument(Integer idDocRepository, String applicationCode) {
		String envName = System.getProperty(WebConstants.ENVIRONMENT);
		String uri = getUriBaseForDelete(envName) + idDocRepository + "/" + applicationCode;
		HttpClient httpClient = new HttpClient();
		GetMethod method = new GetMethod(uri);

		try {

			httpClient.executeMethod(method);

		} catch (HttpException e) {

		} catch (IOException e) {

		} finally {
			method.releaseConnection();
		}
	}

	/**
	 * Method Name: getUriBaseForDelete Method Description: This method is used to
	 * get the URI based on environment.
	 *
	 * @param envName
	 * @return uriBaseForDelete
	 */
	private String getUriBaseForDelete(String envName) {
		String uriBaseForDelete = null;
		uriBaseForDelete = abcsDocumentPropBundle.getString(ABCS_DOCUMENT_DELETE_URI + envName.toUpperCase());

		return uriBaseForDelete;
	}

	/**
	 * Method Name: sendName Method Description:This method is used to send request
	 * to DPS Criminal Name Request
	 *
	 * @param recordsCheckDetailReq
	 * @param commonDto
	 * @param recordsCheckSaveRes
	 * @param nameList
	 * @return errorMessage
	 */
	private String sendName(RecordsCheckDetailReq recordsCheckDetailReq, CommonDto commonDto,
			RecordsCheckListRes recordsCheckSaveRes, URL styleSheetURL, List<PersonInfoDto> nameList) {

		int idRecCheckPerson = recordsCheckDetailReq.getIdRecCheckPerson().intValue();
		int idRecCheckRequestor = commonDto.getIdUser().intValue();
		int idStage = !ObjectUtils.isEmpty(recordsCheckDetailReq.getRecordsCheckDtoList().get(0).getIdStage())
				? recordsCheckDetailReq.getRecordsCheckDtoList().get(0).getIdStage().intValue()
				: 0;
		String errorMessage = null;
		DPSCrimHistNameCheckService dpsService = null;
		String commonAppURL = WebConstants.EMPTY_STRING;
		log.info("Before call to Common APP Service " + System.currentTimeMillis());
		try {

			commonAppURL = cacheAdapter.getDecode(CodesConstant.URLNMCHK,
					JNDIUtil.getEnvName(WebConstants.webServiceEnv));
			dpsService = new DPSCrimHistNameCheckService(new URL(commonAppURL),
					new QName(WebConstants.COMMON_APP_URI, WebConstants.COMMON_APP_PORT));
		} catch (MalformedURLException e) {
			log.error(e.getMessage());
		}
		log.info("The end point for the service is " + commonAppURL);
		log.info("The person id for whom the service is called is  " + idRecCheckPerson);
		log.info("The stage id when calling the criminal history service is " + idStage);

		NameCheckRequest nameCheckRequest = new NameCheckRequest();
		NameCheckResponse nameCheckResponse = null;
		List<NameRequest> nameRequestList = new ArrayList<NameRequest>();
		List<Integer> responseSummaryClaimed = new ArrayList<Integer>();
		NameRequest nameRequest = null;

		boolean dobSet = false;
		boolean ssnSet = false;
		String dob = null;
		String ssn = null;
		for (PersonInfoDto personInfoDto : nameList) {
			nameRequest = new NameRequest();

			nameRequest.setFirstName(personInfoDto.getFirstName());
			nameRequest.setMiddleName(personInfoDto.getMiddleName());
			nameRequest.setLastName(personInfoDto.getLastName());

			if (!dobSet) {
				dob = personInfoDto.getDob();
				dobSet = true;
			}
			if (!ssnSet) {
				// set fields
				ssn = personInfoDto.getSsn();
				ssnSet = true;
			}

			// Add all the names to nameRequestList object
			nameRequestList.add(nameRequest);
		}
		nameCheckRequest.setIdRequest(recordsCheckSaveRes.getIdRecordCheck().intValue());
		nameCheckRequest.setIdRequestor(idRecCheckRequestor);
		nameCheckRequest.setDateOfBirthDay(Integer.valueOf(dob.substring(8, 10)));
		nameCheckRequest.setDateOfBirthMonth(Integer.valueOf(dob.substring(5, 7)));
		nameCheckRequest.setDateOfBirthYear(Integer.valueOf(dob.substring(0, 4)));
		nameCheckRequest.getNameRequest().addAll(nameRequestList);
		nameCheckRequest.setCdCheckType(WebConstants.CHECK_TYPE_NAME);
		nameCheckRequest.setNmApplication(WebConstants.APPLICATION_IMPACT);
		nameCheckRequest.setImmediate(true);
		nameCheckRequest.setRapSheetFormat(WebConstants.RAP_FORMAT_CRS_V1);
		nameCheckRequest.setThresholdRank(WebConstants.RANK_THRESHOLD);
		nameCheckRequest.setAgencyNumber(WebConstants.AGENCY_NUMBER);
		nameCheckRequest.setSsn(ssn);

		try {
			nameCheckResponse = dpsService.getDPSCrimHistNameCheckPort().checkName(nameCheckRequest);
		} catch (NameCheckRequestException_Exception e) {

			errorMessage = cacheAdapter.getMessage(MessagesConstants.MSG_DPS_DELAYED_RESPONSE);
			log.info("The exception while calling the criminal check service is " + errorMessage);
			callDPSWSNameSearchProcedure(recordsCheckSaveRes.getIdRecordCheck(),
					recordsCheckDetailReq.getIdRecCheckPerson(), commonDto.getIdUser());
			return errorMessage;
		} catch (InvalidNameException_Exception e) {
			errorMessage = e.getMessage();
			log.info("The exception while calling the criminal check service is " + errorMessage);
			return errorMessage;
		}

		log.info("After call to Common APP Service " + System.currentTimeMillis());

		if (!ObjectUtils.isEmpty(nameCheckResponse)
				&& (WebConstants.DELAYED_RESPONSE.equalsIgnoreCase(nameCheckResponse.getStatus())
						|| WebConstants.NOT_AVAILABLE.equalsIgnoreCase(nameCheckResponse.getStatus()))) {
			errorMessage = cacheAdapter.getMessage(MessagesConstants.MSG_DPS_DELAYED_RESPONSE);
			callDPSWSNameSearchProcedure(recordsCheckSaveRes.getIdRecordCheck(),
					recordsCheckDetailReq.getIdRecCheckPerson(), commonDto.getIdUser());
			return errorMessage;
		} else if (!ObjectUtils.isEmpty(nameCheckResponse)
				&& !(CodesConstant.CCRIMSTA_N.equalsIgnoreCase(nameCheckResponse.getStatus())
						|| CodesConstant.CCRIMSTA_T.equalsIgnoreCase(nameCheckResponse.getStatus()))
				&& !CollectionUtils.isEmpty(nameCheckResponse.getNameCheckCriminalResponse())) {
			Iterator<NameCheckCriminalResponse> nameCheckCriminalResponseIter = nameCheckResponse
					.getNameCheckCriminalResponse().iterator();
			while (nameCheckCriminalResponseIter.hasNext()) {
				NameCheckCriminalResponse nameCheckCriminalResponse = nameCheckCriminalResponseIter.next();
				StreamResult result = processRAPSheet(nameCheckCriminalResponse.getRapSheet(), styleSheetURL);
				// Populate FilteredName object
				FilteredNameDto filteredName = new FilteredNameDto();
				filteredName.setIdRecCheck(recordsCheckSaveRes.getIdRecordCheck());
				filteredName.setMatchType(nameCheckCriminalResponse.getTxtDpsMatchType());
				filteredName.setSearchedFirstName(
						StringHelper.getNonNullString(nameCheckCriminalResponse.getCrimHistNameComb().getNmFirst()));
				filteredName.setSearchedMiddleName(
						StringHelper.getNonNullString(nameCheckCriminalResponse.getCrimHistNameComb().getNmMiddle()));
				filteredName.setSearchedLastName(
						StringHelper.getNonNullString(nameCheckCriminalResponse.getCrimHistNameComb().getNmLast()));
				filteredName.setSearchType(nameCheckCriminalResponse.getCrimHistNameComb().getCdCombType());
				filteredName.setDpsNumber(nameCheckCriminalResponse.getTxtSid());
				// Insert into Criminal History and Narrative
				CriminalHistoryValueBean criminalHistoryBean = new CriminalHistoryValueBean();
				criminalHistoryBean.setIdRecCheck(filteredName.getIdRecCheck());
				String matchedName = nameCheckCriminalResponse.getTxtMatchSummary().substring(0,
						nameCheckCriminalResponse.getTxtMatchSummary().indexOf('['));
				if (StringHelper.isValid(matchedName) && matchedName.length() > 30) {
					criminalHistoryBean.setCrimHistReturned(matchedName.substring(0, 29));
				} else {
					criminalHistoryBean.setCrimHistReturned(matchedName);
				}
				criminalHistoryBean.setStatus(nameCheckResponse.getStatus());
				criminalHistoryBean.setSearchType(filteredName.getSearchType());
				criminalHistoryBean.setDpsMatchType(filteredName.getMatchType());
				criminalHistoryBean.setIdDpsS(filteredName.getDpsNumber());
				criminalHistoryBean.setPersonFirst(filteredName.getSearchedFirstName());
				criminalHistoryBean.setPersonMiddle(filteredName.getSearchedMiddleName());
				criminalHistoryBean.setPersonLast(filteredName.getSearchedLastName());

				// If Paper only
				if (CodesConstant.CCRIMSTA_I.equalsIgnoreCase(nameCheckResponse.getStatus())
						|| nameCheckCriminalResponse.isPaper()) {
					criminalHistoryBean.setCrimHistAction("ACP");
					criminalHistoryBean.setStatus(CodesConstant.CCRIMSTA_I);
				} else {
					criminalHistoryBean.setCrimHistAction(StringHelper.EMPTY_STRING);
					responseSummaryClaimed.add(nameCheckCriminalResponse.getIdCrimHistNmCmbRsp());
				}
				saveToCriminalHistoryAndNarrative(populateRequest(criminalHistoryBean, filteredName, result));
			}
		}
		// Update RECORDS_CHECK
		if (!ObjectUtils.isEmpty(nameCheckResponse)
				&& !StringHelper.EMPTY_STRING.equals(nameCheckResponse.getStatus())) {
			// Update Records_Check table with the status
			updateRecordsCheckStatus(recordsCheckSaveRes.getIdRecordCheck(), nameCheckResponse.getStatus());

			// generate Alerts
			generateAlerts(idRecCheckPerson, idRecCheckRequestor, idStage, nameCheckResponse.getStatus());

		}
		return errorMessage;
	}

	/**
	 * Method Name: processRAPSheet Method Description:This method is used to
	 * process the RAP sheet from DPS Criminal History Response
	 *
	 * @return result
	 */
	private StreamResult processRAPSheet(String rapSheetXML, URL styleSheetURL) {
		Writer outWriter = new StringWriter();
		StreamResult result = new StreamResult(outWriter);
		InputStream inputStream;

		if (StringHelper.isValid(rapSheetXML) && !(rapSheetXML.indexOf("<INDV") < 0)) {
			rapSheetXML = rapSheetXML.substring(rapSheetXML.indexOf("<INDV"), rapSheetXML.length());
			try {
				inputStream = styleSheetURL.openStream();
				try {
					DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
					dbf.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
					DocumentBuilder builder = dbf.newDocumentBuilder();
					Document document = builder.parse(new InputSource(new StringReader(rapSheetXML)));

					TransformerFactory tranFactory = TransformerFactory.newInstance();
					Transformer transformer = tranFactory
							.newTransformer(new javax.xml.transform.stream.StreamSource(inputStream));
					Source src = new DOMSource(document);
					transformer.transform(src, result);

				} catch (ParserConfigurationException e) {

				} catch (SAXException e) {

				} catch (IOException e) {

				}
			} catch (TransformerConfigurationException e) {
				log.error(e.getMessage());
			} catch (TransformerException e) {
				log.error(e.getMessage());
			} catch (IOException e1) {
				log.error(e1.getMessage());
			}
		}

		return result;
	}

	/**
	 * Method Name: populateRequest Method Description:This method is used to
	 * populate the request for Criminal History Narrative.
	 *
	 * @param criminalHistoryBean
	 * @param filteredName
	 * @param result
	 * @return criminalHistoryNarrativeReq
	 */
	private CriminalHistoryReq populateRequest(CriminalHistoryValueBean criminalHistoryBean,
			FilteredNameDto filteredName, StreamResult result) {
		CriminalHistoryReq criminalHistoryNarrativeReq = new CriminalHistoryReq();
		criminalHistoryNarrativeReq.setCriminalHistoryValueBean(criminalHistoryBean);
		criminalHistoryNarrativeReq.setFilteredNameDto(filteredName);
		StringWriter sw = (StringWriter) result.getWriter();
		StringBuffer sb = sw.getBuffer();
		String output = sb.toString();
		criminalHistoryNarrativeReq.setResults(output);
		return criminalHistoryNarrativeReq;
	}

	/**
	 * Method Name: callDPSWSNameSearchProcedure Method Description: This method is
	 * used to insert record into temp table for batch processing of Records Check
	 * detail.
	 *
	 * @param idRecordCheck
	 * @param commonDto
	 */
	private void callDPSWSNameSearchProcedure(Long idRecordCheck, CommonDto commonDto) {
		callDPSWSNameSearchProcedure(idRecordCheck, commonDto.getIdPerson(), commonDto.getIdUser());

	}

	/**
	 * Method Name: aaddReckCheckPersonEmailDetail Method Description:This method is
	 * used to add a new email for FBI FingerPrint.
	 *
	 * @param checkDto
	 * @param existingEmail
	 * @param existingPrimaryInd
	 * @param commonDto
	 */
	@SuppressWarnings("unchecked")
	private void addReckCheckPersonEmailDetail(RecordsCheckDto checkDto, String existingEmail,
			boolean existingPrimaryInd, CommonDto commonDto) {
		int staffId = commonDto.getIdUser().intValue();
		int personId = commonDto.getIdPerson().intValue();
		String selEmailAddress = ((Map<String, String>) jsonToMap(checkDto.getEmailListStr())).get(checkDto.getEmail());
		EmailDetailBean emailDetailBean = new EmailDetailBean();
		emailDetailBean.setIdPerson((long) personId);
		emailDetailBean.setEmailType(CodesConstant.CEMLPRTY_FP);
		emailDetailBean.setEmail(selEmailAddress);
		emailDetailBean.setIdCreatedPerson((long) staffId);
		emailDetailBean.setIdStaff((long) (staffId));
		emailDetailBean.setIdEmail(0l);
		EmailDetailReq request = new EmailDetailReq();
		request.setEmailDetail(emailDetailBean);
		saveEmailDetailsForRecordsCheck(request);

	}

	/**
	 * Method Name: addPersonPhoneDetail Method Description:This method is used to
	 * add a new phone for FBI FingerPrint.
	 *
	 * @param checkDto
	 * @param hiddenPersonPhoneNumber
	 * @param commonData
	 * @param hdnIndPhonePrimary
	 */
	@SuppressWarnings("unchecked")
	private void addPersonPhoneDetail(RecordsCheckDto checkDto, String hiddenPersonPhoneNumber, CommonDto commonData,
			Boolean hdnIndPhonePrimary) {
		PersonPhoneReq personPhoneReq = new PersonPhoneReq();
		PersonPhoneRetDto personPhoneRetDto = new PersonPhoneRetDto();
		personPhoneRetDto.setIndPersonPhoneInvalid(WebConstants.NO);
		if (hdnIndPhonePrimary) {
			personPhoneRetDto.setIndPersonPhonePrimary(WebConstants.YES);
		} else {
			personPhoneRetDto.setIndPersonPhonePrimary(WebConstants.NO);

		}
		personPhoneRetDto.setDtPersonPhoneStart(new Date());
		personPhoneRetDto.setPersonPhone(FormattingHelper.decodeFormattedPhoneString(
				((Map<String, String>) (jsonToMap(checkDto.getPhoneListStr()))).get(checkDto.getPhoneNumber())));
		personPhoneRetDto.setPersonPhoneExtension(WebConstants.EMPTY_STRING);
		personPhoneRetDto.setCdPersonPhoneType(CodesConstant.CPHNTYP_FP);
		personPhoneRetDto.setPersonPhoneComments(WebConstants.EMPTY_STRING);
		personPhoneReq.setReqFuncCd(WebConstants.REQ_FUNC_CD_ADD);
		personPhoneReq.setUlIdPerson(commonData.getIdPerson());
		personPhoneReq.setUlIdStage(commonData.getIdStage());
		personPhoneReq.setPersonPhoneRetDto(personPhoneRetDto);
		savePhone(personPhoneReq);
	}

	/**
	 * Method Name: populateFingerprintCheckRequest Method Description:This method
	 * is used to create the request for a new FBI FingerPrint Request.
	 *
	 * @param response
	 * @param checkDto
	 * @param commonData
	 * @return recordsCheckReq
	 */
	private RecordsCheckDetailReq populateFingerprintCheckRequest(RecordsCheckListRes response,
			RecordsCheckDto checkDto, CommonDto commonData) {
		RecordsCheckDetailReq recordsCheckReq = new RecordsCheckDetailReq();
		List<RecordsCheckDetailDto> recordsCheckDetailList = new ArrayList<RecordsCheckDetailDto>();
		RecordsCheckDetailDto recordsCheckDetailDto = new RecordsCheckDetailDto();
		recordsCheckReq.setUserId(commonData.getIdUser().toString());
		// TODO - Hardcoding the value to false since Forms And Reports are not
		// implemented now
		boolean calledFromJavascript = false;
		String serviceCode = WebConstants.EMPTY_STRING;
		if (!ObjectUtils.isEmpty(checkDto.getPageModeStr()) && checkDto.getPageModeStr().equals(NEW)
				|| calledFromJavascript) {

			int idRecCheck = (Integer) response.getIdRecordCheck().intValue();
			serviceCode = getServiceCode(idRecCheck);
			recordsCheckReq.setReqFuncCd(WebConstants.REQ_FUNC_CD_ADD);
			recordsCheckDetailDto.setScrDataAction(WebConstants.ACTION_A);
			recordsCheckDetailDto.setIdRecCheckRequestor(commonData.getIdUser());
			recordsCheckDetailDto.setIdStage(commonData.getIdStage());
			recordsCheckDetailDto.setRecCheckCheckType(CodesConstant.CCHKTYPE_80);
			recordsCheckDetailDto.setRecCheckContMethod(CodesConstant.FBIMTHD_EML);
			if (!ObjectUtils.isEmpty(serviceCode)) {
				recordsCheckDetailDto.setCdRecCheckServ(serviceCode);
			}
			String emailAddress = cacheAdapter.getDecode(CodesConstant.FBICBCU, CodesConstant.FBICBCU_CBCU);
			recordsCheckDetailDto.setRecCheckContMethodValue(emailAddress);
			recordsCheckDetailDto.setDtRecCheckRequest(new Date());
			recordsCheckDetailDto.setIndClearedEmail(ContextHelper.getStringSafe(WebConstants.NO));
			recordsCheckReq.setPageSizeNbr(1);
			recordsCheckDetailList.add(recordsCheckDetailDto);
			recordsCheckReq.setRecordsCheckDtoList(recordsCheckDetailList);
			recordsCheckReq.setIdRecCheckPerson(commonData.getIdPerson());
		}

		return recordsCheckReq;
	}

	/**
	 * Method Name: checkShowSendResendEmailButton Method Description:This method is
	 * used to check if the Send/Resend Email button to be displayed.
	 *
	 * @param recordsCheckDto
	 * @param idPerson
	 * @return indShowSendResendEmailButton
	 */
	private boolean checkShowSendResendEmailButton(RecordsCheckDto recordsCheckDto, Long idPerson) {
		boolean indShowSendResendEmailButton = false;
		boolean isMostRecentCASAFPSCheck = isMostRecentCASAFPSCheck(recordsCheckDto.getIdRecordCheckPerson(),
				recordsCheckDto.getIdRecCheck());
		boolean isRecCheckCompleted = (!ObjectUtils.isEmpty(recordsCheckDto.getDtRecCheckCompleted())) ? true : false;
		boolean indCdDeterm = false;
		Date rapBackReleaseDt = DateUtil.toJavaDate(cacheAdapter.getDecode(CodesConstant.CRELDATE, CodesConstant.JUN_2024_RAPBACK));
		boolean isRapBackReleaseBeforeDt = false;

		//artf257140 : SendClearedEmail button should not display for Bar Determination.
		if (CodesConstant.CDETERM_CLER.equalsIgnoreCase(recordsCheckDto.getRecChkDeterm())) {
			indCdDeterm = true;
		}
		if(!ObjectUtils.isEmpty(recordsCheckDto.getDtRecCheckRequest()) &&
				(DateUtil.isBefore(recordsCheckDto.getDtRecCheckRequest(), rapBackReleaseDt))){
			isRapBackReleaseBeforeDt = true;
		}
		boolean isPersonProvisioned = isPersonCasaProvisioned(recordsCheckDto.getIdRecordCheckPerson());
		// PD92372: Enabled send cleared email button if the email is not sent.
		if (isMostRecentCASAFPSCheck && isRecCheckCompleted && indCdDeterm && !isPersonProvisioned && (isRapBackReleaseBeforeDt || !hasEmailSent(recordsCheckDto.getIdRecCheck()))) {
			indShowSendResendEmailButton = true;
		}

		return indShowSendResendEmailButton;

	}


	/**
	 * Method Name: canDeleteRecordCheck Method Description:This method is used to
	 * check if a particular Record Check can be deleted or not.
	 *
	 * @param cdRecCheckCheckType
	 * @return boolean
	 */

	public boolean canDeleteRecordCheck(String cdRecCheckCheckType) {
		// artf51666 - changed from CCHKTYPE_15 to CCHKTYPE_95
		return !(CodesConstant.CCHKTYPE_10.equals(cdRecCheckCheckType)
				|| CodesConstant.CCHKTYPE_95.equals(cdRecCheckCheckType)
				|| CodesConstant.CCHKTYPE_20.equals(cdRecCheckCheckType)
				|| CodesConstant.CCHKTYPE_25.equals(cdRecCheckCheckType)
				|| CodesConstant.CCHKTYPE_75.equals(cdRecCheckCheckType)
				|| CodesConstant.CCHKTYPE_80.equals(cdRecCheckCheckType)
				|| CodesConstant.CCHKTYPE_85.equals(cdRecCheckCheckType));
	}

	/**
	 * Method Name: createCancelReasonMap Method Description:This method is used to
	 * create the drop down value for Cancel Reason
	 *
	 * @return cancelReasonList
	 */

	private Map<String, String> createCancelReasonMap() {
		Map<String, String> cancelReasonList = new TreeMap<String, String>();
		cancelReasonList = cacheAdapter.getCodeCategories(CodesConstant.CCRIMSTA);
		cancelReasonList.remove(CodesConstant.CCRIMSTA_B);
		cancelReasonList.remove(CodesConstant.CCRIMSTA_C);
		cancelReasonList.remove(CodesConstant.CCRIMSTA_D);
		cancelReasonList.remove(CodesConstant.CCRIMSTA_E);
		cancelReasonList.remove(CodesConstant.CCRIMSTA_H);
		cancelReasonList.remove(CodesConstant.CCRIMSTA_I);
		cancelReasonList.remove(CodesConstant.CCRIMSTA_K);
		cancelReasonList.remove(CodesConstant.CCRIMSTA_M);
		cancelReasonList.remove(CodesConstant.CCRIMSTA_N);
		cancelReasonList.remove(CodesConstant.CCRIMSTA_O);
		cancelReasonList.remove(CodesConstant.CCRIMSTA_P);
		cancelReasonList.remove(CodesConstant.CCRIMSTA_Q);
		cancelReasonList.remove(CodesConstant.CCRIMSTA_R);
		cancelReasonList.remove(CodesConstant.CCRIMSTA_S);
		cancelReasonList.remove(CodesConstant.CCRIMSTA_U);
		cancelReasonList.remove(CodesConstant.CCRIMSTA_V);
		cancelReasonList.remove(CodesConstant.CCRIMSTA_T);
		cancelReasonList.remove(CodesConstant.CCRIMSTA_F);
		return cancelReasonList;
	}

	/**
	 * Method Name: populateEmailPhoneList Method Description:This method is used to
	 * populate the drop down values for email and phone when the search type is FBI
	 * FingerPrint screen.
	 *
	 * @param recordsCheckDto
	 * @param returnMap
	 * @param idPerson
	 */

	private void populateEmailPhoneList(RecordsCheckDto recordsCheckDto, Map<String, Object> returnMap, Long idPerson) {
		String actionPerformed = recordsCheckDto.getButtonClicked();
		Map<String, String> phoneList = new HashMap<String, String>();
		List<PersonPhoneRetDto> phoneDtoList = getPersonPhoneList(idPerson);
		String nbrPhone = null;
		String email = null;
		if (VALIDATE_ACTION_STRING.equals(actionPerformed)) {
			if (!CollectionUtils.isEmpty(phoneDtoList)) {
				String idPersonPhone = null;
				String phonePrimary = WebConstants.EMPTY_STRING;
				String nbrPersonPhone = WebConstants.EMPTY_STRING;
				for (PersonPhoneRetDto personPhoneRetDto : phoneDtoList) {
					if (personPhoneRetDto.getCdPersonPhoneType().equals(CodesConstant.CPHNTYP_FP)) {
						idPersonPhone = personPhoneRetDto.getIdPersonPhone().toString();
						phonePrimary = personPhoneRetDto.getIndPersonPhonePrimary();
						nbrPersonPhone = personPhoneRetDto.getPersonPhone();
						phoneList.put(personPhoneRetDto.getIdPersonPhone().toString(),
								FormattingHelper.formatPhone(nbrPersonPhone));
						recordsCheckDto.setPhoneNumber(idPersonPhone);
					} else {
						phoneList.put(personPhoneRetDto.getIdPersonPhone().toString(),
								FormattingHelper.formatPhone(personPhoneRetDto.getPersonPhone()));
					}
				}
				nbrPhone = idPersonPhone;

				returnMap.put("phonePrimary", phonePrimary);
				returnMap.put("idPersonPhone", idPersonPhone);

			}
		}
		Map<String, String> emailPersonList = new HashMap<String, String>();
		// get the email list
		if (VALIDATE_ACTION_STRING.equals(actionPerformed)) {
			List<EmailDetailBean> emailList = emailList(idPerson);

			if (!CollectionUtils.isEmpty(emailList)) {
				String fPrintEmail = null;
				int idEmailPerson = 0;
				String emailPrimary = WebConstants.EMPTY_STRING;
				for (EmailDetailBean emailDetailBean : emailList) {
					if (emailDetailBean.getEmailType().equals(CodesConstant.CPHNTYP_FP)) {
						if (emailDetailBean.isPrimary()) {
							emailPrimary = WebConstants.YES;
						} else {
							emailPrimary = WebConstants.NO;
						}
						idEmailPerson = emailDetailBean.getIdEmail().intValue();
						fPrintEmail = emailDetailBean.getEmail();
						emailPersonList.put(emailDetailBean.getIdEmail().toString(), fPrintEmail);
						recordsCheckDto.setEmail(Integer.toString(idEmailPerson));
					} else {
						emailPersonList.put(emailDetailBean.getIdEmail().toString(), emailDetailBean.getEmail());
					}

				}
				email = fPrintEmail;

				returnMap.put("idEmailPerson", String.valueOf(idEmailPerson));
				returnMap.put("emailPrimary", emailPrimary);

			}
		}

		if (!VALIDATE_ACTION_STRING.equals(actionPerformed)
				&& EML_CODE.equalsIgnoreCase(recordsCheckDto.getRecCheckContMethod())) {
			email = recordsCheckDto.getRecCheckContMethodValue();
			recordsCheckDto.setEmail(recordsCheckDto.getRecCheckContMethodValue());
			recordsCheckDto.setPhoneNumber(WebConstants.EMPTY_STRING);
			emailPersonList.put(email, recordsCheckDto.getRecCheckContMethodValue());

			if (NOT_APPLICABLE.equals(recordsCheckDto.getRecCheckContMethodValue())) {
				emailPersonList.put(NOT_APPLICABLE, recordsCheckDto.getRecCheckContMethodValue());
			}
		}
		if (!VALIDATE_ACTION_STRING.equals(actionPerformed)
				&& PHN_CODE.equalsIgnoreCase(recordsCheckDto.getRecCheckContMethod())) {
			nbrPhone = !ObjectUtils.isEmpty(recordsCheckDto.getRecCheckContMethodValue())
					? recordsCheckDto.getRecCheckContMethodValue()
					: null;
			recordsCheckDto.setPhoneNumber(recordsCheckDto.getRecCheckContMethodValue());
			recordsCheckDto.setEmail(WebConstants.EMPTY_STRING);
			phoneList.put(recordsCheckDto.getRecCheckContMethodValue(),
					FormattingHelper.formatPhone(recordsCheckDto.getRecCheckContMethodValue()));

		}
		returnMap.put("fPrintEmail", email);
		returnMap.put("emailPersonList", emailPersonList);
		returnMap.put("nbrPersonPhone", nbrPhone);
		returnMap.put("phoneList", phoneList);
		recordsCheckDto.setEmailListStr(recordCheckUtil.objectToJsonString(emailPersonList));
		recordsCheckDto.setPhoneListStr(recordCheckUtil.objectToJsonString(phoneList));

	}

	/**
	 * Method Name: getSearchTypeList Method Description:This method is used to
	 * create the drop down list for Search Type in Records Check screen screen.
	 *
	 * @param pageMode
	 * @param cdStage
	 * @param indDeletable
	 * @return recordCheckSearchType
	 */
	private Map<String, String> getSearchTypeList(String pageMode, String cdStage, boolean indDeletable,
			boolean indRecCheckAccess) {
		Map<String, String> recordCheckSearchType = new TreeMap<String, String>();
		if (NEW.equals(pageMode)) {
			recordCheckSearchType = cacheAdapter.getCodeCategories(CodesConstant.CCHKTYPE);
		} else if (MODIFY.equals(pageMode) || INQUIRE.equals(pageMode)) {
			recordCheckSearchType = cacheAdapter.getCodeCategoriesWithExpiredCodes(CodesConstant.CCHKTYPE);
		}

		recordCheckSearchType.remove(CodesConstant.CCHKTYPE_75);
		// artf51666
		if (!indRecCheckAccess) {
			recordCheckSearchType.remove(CodesConstant.CCHKTYPE_95);
		}
		// end of artf51666

		if (NEW.equals(pageMode)) {
			recordCheckSearchType.remove(CodesConstant.CCHKTYPE_20);
			recordCheckSearchType.remove(CodesConstant.CCHKTYPE_85);

		}

		boolean modFlag = false;
		if (indDeletable && MODIFY.equals(pageMode)) {
			modFlag = true;
			// artf51666 - changed from CCHKTYPE_15 to CCHKTYPE_95
			recordCheckSearchType.remove(CodesConstant.CCHKTYPE_10);
			recordCheckSearchType.remove(CodesConstant.CCHKTYPE_95);
			recordCheckSearchType.remove(CodesConstant.CCHKTYPE_20);
			recordCheckSearchType.remove(CodesConstant.CCHKTYPE_25);
			recordCheckSearchType.remove(CodesConstant.CCHKTYPE_80);
			recordCheckSearchType.remove(CodesConstant.CCHKTYPE_85);
		}

		if (WebConstants.INTAKE_STAGE.equalsIgnoreCase(cdStage)) {

			if (!modFlag) {
				// artf51666 - changed from CCHKTYPE_15 to CCHKTYPE_95
				//artf258444 : Removed CCHKTYPE_81
				recordCheckSearchType.remove(CodesConstant.CCHKTYPE_95);
				recordCheckSearchType.remove(CodesConstant.CCHKTYPE_20);
				recordCheckSearchType.remove(CodesConstant.CCHKTYPE_25);
				recordCheckSearchType.remove(CodesConstant.CCHKTYPE_80);
				recordCheckSearchType.remove(CodesConstant.CCHKTYPE_81);
				recordCheckSearchType.remove(CodesConstant.CCHKTYPE_85);
			}
			if (!NEW.equals(pageMode)) {
				recordCheckSearchType.remove(CodesConstant.CCHKTYPE_20);
				recordCheckSearchType.remove(CodesConstant.CCHKTYPE_85);
			}
			recordCheckSearchType.remove(CodesConstant.CCHKTYPE_30);
			recordCheckSearchType.remove(CodesConstant.CCHKTYPE_40);
			recordCheckSearchType.remove(CodesConstant.CCHKTYPE_50);
			recordCheckSearchType.remove(CodesConstant.CCHKTYPE_60);
			recordCheckSearchType.remove(CodesConstant.CCHKTYPE_70);
			recordCheckSearchType.remove(CodesConstant.CCHKTYPE_75);
			recordCheckSearchType.remove(CodesConstant.CCHKTYPE_82);
			recordCheckSearchType.remove(CodesConstant.CCHKTYPE_95);// artf51666
																	// - changed
																	// from
																	// CCHKTYPE_15
																	// to
																	// CCHKTYPE_95
		}

		return recordCheckSearchType;

	}

	/**
	 * Method Name: setInformationMessage Method Description:This method is used to
	 * set the Information message to be displayed in the Records Check detail
	 * screen.
	 *
	 * @param indModify
	 * @param recordsCheckDto
	 * @param informationMessage
	 * @param recordsBusinessDelegate
	 * @param idStage
	 * @param idLoggedInUser
	 */
	private void setInformationMessage(boolean indModify, RecordsCheckDto recordsCheckDto, String informationMessage,
			RecordsCheckBusinessDelegateDto recordsBusinessDelegate, long idStage, long idLoggedInUser) {
		boolean dtRequestLegacy = true;
		boolean dtCompletedLegacy = true;
		// Check if the record check requested date is after 10/17/2012 and if
		// yes
		// setting the flag to false
		if (indModify && !ObjectUtils.isEmpty(recordsCheckDto)
				&& !ObjectUtils.isEmpty(recordsCheckDto.getDtRecCheckRequest())) {
			dtRequestLegacy = us.tx.state.dfps.businessdelegate.util.DateFormatUtil
					.isAfter(recordsCheckDto.getDtRecCheckRequest(), DateFormatUtil.CCH_DPS_WS) ? false : true;
		}
		// Check if the record check completed date is after 11/17/2012 and if
		// yes
		// setting the flag to false
		if (indModify && !ObjectUtils.isEmpty(recordsCheckDto)
				&& !ObjectUtils.isEmpty(recordsCheckDto.getDtRecCheckCompleted())) {
			dtCompletedLegacy = DateFormatUtil.isAfter(recordsCheckDto.getDtRecCheckCompleted(),
					DateFormatUtil.CCH_DPS_WS) ? false : true;
		}

		// If the completed date and requested date is before 11/17/2012 and the
		// record
		// check id is not 0 proceed with the logic
		if (indModify && !ObjectUtils.isEmpty(recordsCheckDto) && recordsCheckDto.getIdRecCheck().longValue() > 0l
				&& (!dtRequestLegacy || !dtCompletedLegacy)) {

			// For Primary or Secondary worker, display the message.
			boolean displayMessage = false;
			if (idStage != 0 && idLoggedInUser != 0) {
				String userRole = getRoleInWorkloadStage(idStage, idLoggedInUser);

				if (PRIMARY_WORKER_STRING.equals(userRole) || SECONDARY_WORKER_STRING.equals(userRole)) {
					displayMessage = true;
				}
			}
			// If the user is not primary or secondary worker, check for
			// security rights.
			if (displayMessage == false) {
				if (recordsBusinessDelegate.isIndRecCheckAccess() || recordsBusinessDelegate.isIndMntnPersonkAccess()) {
					displayMessage = true;
				}
			}

			if (indModify && displayMessage
					&& CodesConstant.CCRIMSTA_N.equalsIgnoreCase(recordsCheckDto.getRecCheckStatus())
					&& CodesConstant.CCHKTYPE_10.equalsIgnoreCase(recordsCheckDto.getRecCheckCheckType())) {
				informationMessage = cacheAdapter.getMessage(MessagesConstants.MSG_DPS_NO_HIT);
			} else if (indModify && displayMessage
					&& CodesConstant.CCRIMSTA_T.equalsIgnoreCase(recordsCheckDto.getRecCheckStatus())
					&& CodesConstant.CCHKTYPE_10.equalsIgnoreCase(recordsCheckDto.getRecCheckCheckType())) {
				informationMessage = cacheAdapter.getMessage(MessagesConstants.MSG_DPS_TOO_MANY_RESULTS);

			} else if (indModify && displayMessage
					&& (CodesConstant.CCRIMSTA_F.equalsIgnoreCase(recordsCheckDto.getRecCheckStatus())
							|| CodesConstant.CCRIMSTA_B.equalsIgnoreCase(recordsCheckDto.getRecCheckStatus())
							|| CodesConstant.CCRIMSTA_M.equalsIgnoreCase(recordsCheckDto.getRecCheckStatus())
							|| CodesConstant.CCRIMSTA_H.equalsIgnoreCase(recordsCheckDto.getRecCheckStatus()))
					&& CodesConstant.CCHKTYPE_10.equalsIgnoreCase(recordsCheckDto.getRecCheckCheckType())) {
				informationMessage = cacheAdapter.getMessage(MessagesConstants.MSG_DPS_RESULTS);

			}

			recordsCheckDto.setInformationMessage(informationMessage);
		}

	}

	/**
	 * Method Name: getRoleInWorkloadStage Method Description: This method is used
	 * to fetch the role of the user in the assigned workload .
	 *
	 * @param idStage
	 * @param idLoggedInUser
	 * @return roleInWorkloadStage
	 */
	@SuppressWarnings("unchecked")
	public String getRoleInWorkloadStage(Long idStage, Long idLoggedInUser) {
		CommonHelperReq roleInWorkloadStageInfo = new CommonHelperReq();
		roleInWorkloadStageInfo.setIdStage(idStage);
		roleInWorkloadStageInfo.setIdPerson(idLoggedInUser);
		String completeURI = JNDIUtil.buildServiceUrl(WebConstants.JNDIHOSTURL,
				serviceBundle.getString(SERVICE_ROLE_IN_WORKLOAD_STAGE));
		String roleInWorkloadStage = "";
		ResponseEntity<CommonHelperRes> stageInfo = (ResponseEntity<CommonHelperRes>) handleResponse(
				configureRestCall().postForEntity(completeURI, roleInWorkloadStageInfo, CommonHelperRes.class));

		if (!ObjectUtils.isEmpty(stageInfo.getBody())) {

			roleInWorkloadStage = stageInfo.getBody().getRoleInWorkloadStage();

		}

		return roleInWorkloadStage;
	}

	private Map<String, String> getORIAccountList(){
		Map<String, String> recordCheckSearchType = new TreeMap<String, String>();
		recordCheckSearchType = cacheAdapter.getCodeCategories(CodesConstant.ORIACCT);
		return recordCheckSearchType;
	}

	public boolean hasOriginatingFingerprintCheck(Long idPerson) {
		boolean indOriginatingFPCheck = false;
		RecordsCheckReq recordsCheckRequest = new RecordsCheckReq();
		recordsCheckRequest.setIdRecCheckPerson(idPerson);
		String completeURI = JNDIUtil.buildServiceUrl(WebConstants.JNDIHOSTURL,
				serviceBundle.getString(SERVICE_HAS_ORIGINATING_FINGERPRINT_CHECK));
		ResponseEntity<RecordsCheckRes> response = (ResponseEntity<RecordsCheckRes>) handleResponse(
				configureRestCall().postForEntity(completeURI, recordsCheckRequest, RecordsCheckRes.class));
		if (!ObjectUtils.isEmpty(response)
				&& !ObjectUtils.isEmpty(response.getBody()) && !ObjectUtils.isEmpty(
				Boolean.valueOf(response.getBody().isHasPendingFingerprintCheck()))) {
			indOriginatingFPCheck = response.getBody().isHasOriginatingFPCheck();
}

		return indOriginatingFPCheck;
	}

	public Long getTxtDpsSIDForOrignFpChck(Long idPerson) {
		Long txtDpsSIDForOrignFpChck = 0L;
		RecordsCheckReq recordsCheckRequest = new RecordsCheckReq();
		recordsCheckRequest.setIdRecCheckPerson(idPerson);
		String completeURI = JNDIUtil.buildServiceUrl(WebConstants.JNDIHOSTURL,
				serviceBundle.getString(SERVICE_GET_SID_ORIGINAL_FINGERPRINT));
		ResponseEntity<RecordsCheckRes> response = (ResponseEntity<RecordsCheckRes>) handleResponse(
				configureRestCall().postForEntity(completeURI, recordsCheckRequest, RecordsCheckRes.class));
		if (!ObjectUtils.isEmpty(response)
				&& !ObjectUtils.isEmpty(response.getBody()) && !ObjectUtils.isEmpty(
				Long.valueOf(response.getBody().getTxtDpsSIDOrignFPChck()))) {
			txtDpsSIDForOrignFpChck = response.getBody().getTxtDpsSIDOrignFPChck();
		}

		return txtDpsSIDForOrignFpChck;
	}

	public boolean isABCSCheckForRapBack(Long idPerson) {

		RecordsCheckReq recordsCheckRequest = new RecordsCheckReq();
		recordsCheckRequest.setIdRecCheckPerson(idPerson);

		String completeURI = JNDIUtil.buildServiceUrl(WebConstants.JNDIHOSTURL,
				serviceBundle.getString(SERVICE_GET_ABCS_CHECK_RAP_BACK));
		boolean indABCSCheck = false;
		ResponseEntity<RecordsCheckRes> isABCSCheckResponse = (ResponseEntity<RecordsCheckRes>) handleResponse(
				configureRestCall().postForEntity(completeURI, recordsCheckRequest, RecordsCheckRes.class));
		if (!ObjectUtils.isEmpty(isABCSCheckResponse) && !ObjectUtils.isEmpty(isABCSCheckResponse.getBody())) {
			indABCSCheck = isABCSCheckResponse.getBody().isABCSCheck();
		}

		return indABCSCheck;
	}

	public Long getNewHireCount(Long recordCheckID) {
		RecordsCheckDto recordsCheckDto = new RecordsCheckDto();
		recordsCheckDto.setIdRecCheck(recordCheckID);
		Long newHireCount = 0L;
		String completeURI = JNDIUtil.buildServiceUrl(WebConstants.JNDIHOSTURL,
				serviceBundle.getString(SERVICE_GET_NEW_HIRE_COUNT));
		ResponseEntity<RecordsCheckRes> newHireCountRes = (ResponseEntity<RecordsCheckRes>) handleResponse(
				configureRestCall().postForEntity(completeURI, recordsCheckDto, RecordsCheckRes.class));
		if (!ObjectUtils.isEmpty(newHireCountRes) && !ObjectUtils.isEmpty(newHireCountRes.getBody())) {
			newHireCount = newHireCountRes.getBody().getNewHireCount();
		}

		return newHireCount;
	}

	public String getCdDetermination(Long recordCheckID) {
		RecordsCheckDto recordsCheckDto = new RecordsCheckDto();
		recordsCheckDto.setIdRecCheck(recordCheckID);
		String cdDetermination = null;
		String completeURI = JNDIUtil.buildServiceUrl(WebConstants.JNDIHOSTURL,
				serviceBundle.getString(SERVICE_GET_CD_DETERMINATION));
		ResponseEntity<RecordsCheckRes> cdDeterminationRes = (ResponseEntity<RecordsCheckRes>) handleResponse(
				configureRestCall().postForEntity(completeURI, recordsCheckDto, RecordsCheckRes.class));
		if (!ObjectUtils.isEmpty(cdDeterminationRes) && !ObjectUtils.isEmpty(cdDeterminationRes.getBody())) {
			cdDetermination = cdDeterminationRes.getBody().getCdDetermination();
		}

		return cdDetermination;
	}

	private void removeIfNotDbValue(Map<String, String> excludeDeterm, String code, String cdDeterm){
		if(!code.equals(cdDeterm)){
			excludeDeterm.remove(code);
		}
	}
}
