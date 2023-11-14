package us.tx.state.dfps.service.contacts.serviceimpl;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;

import us.tx.state.dfps.service.admin.dao.ContactRtrvDao;
import us.tx.state.dfps.service.admin.dao.EventLastUpdateDao;
import us.tx.state.dfps.service.admin.dao.FetchEventStatusDao;
import us.tx.state.dfps.service.admin.dao.FetchStageDao;
import us.tx.state.dfps.service.admin.dto.ContactRtrvInDto;
import us.tx.state.dfps.service.admin.dto.ContactRtrvOutDto;
import us.tx.state.dfps.service.admin.dto.EventLastUpdatedInDto;
import us.tx.state.dfps.service.admin.dto.EventLastUpdatedoDto;
import us.tx.state.dfps.service.admin.dto.FetchEventDto;
import us.tx.state.dfps.service.admin.dto.FetchEventRowDto;
import us.tx.state.dfps.service.admin.dto.FetchEventStatusdiDto;
import us.tx.state.dfps.service.admin.dto.FetchEventStatusdoDto;
import us.tx.state.dfps.service.admin.dto.FetchStagediDto;
import us.tx.state.dfps.service.admin.dto.FetchStagedoDto;
import us.tx.state.dfps.service.admin.dto.StageEventdiDto;
import us.tx.state.dfps.service.common.ServiceConstants;
import us.tx.state.dfps.service.common.dao.EventFetDao;
import us.tx.state.dfps.service.common.util.DateUtils;
import us.tx.state.dfps.service.common.util.TypeConvUtil;
import us.tx.state.dfps.service.contact.dto.ContactSearchDto;
import us.tx.state.dfps.service.contacts.dao.ContactFieldDao;
import us.tx.state.dfps.service.contacts.dao.ContactSearchDao;
import us.tx.state.dfps.service.contacts.dao.EventPersonRetrvDao;
import us.tx.state.dfps.service.contacts.dao.StageCasePersonDao;
import us.tx.state.dfps.service.contacts.dao.StageSearchEventDao;
import us.tx.state.dfps.service.contacts.service.StageClosureEventService;
import us.tx.state.dfps.service.exception.ServiceLayerException;
import us.tx.state.dfps.service.investigation.dao.FacilityInvestigationDao;
import us.tx.state.dfps.service.person.dao.StagePersonRetrvDao;
import us.tx.state.dfps.xmlstructs.inputstructs.ConGuideFetchInDto;
import us.tx.state.dfps.xmlstructs.inputstructs.ContactFieldDiDto;
import us.tx.state.dfps.xmlstructs.inputstructs.EventPersonRetrvInDto;
import us.tx.state.dfps.xmlstructs.inputstructs.EventSearchStageInDto;
import us.tx.state.dfps.xmlstructs.inputstructs.FacilityInvestigationDto;
import us.tx.state.dfps.xmlstructs.inputstructs.InvestigationConclusionDto;
import us.tx.state.dfps.xmlstructs.inputstructs.ServiceInputDto;
import us.tx.state.dfps.xmlstructs.inputstructs.StageCasePersonInDto;
import us.tx.state.dfps.xmlstructs.inputstructs.StagePersonRetrvInDto;
import us.tx.state.dfps.xmlstructs.outputstructs.CaseEventDto;
import us.tx.state.dfps.xmlstructs.outputstructs.ConGuideFetchArrayDto;
import us.tx.state.dfps.xmlstructs.outputstructs.ConGuideFetchOutArrayDto;
import us.tx.state.dfps.xmlstructs.outputstructs.ConGuideFetchOutDto;
import us.tx.state.dfps.xmlstructs.outputstructs.ConGuideFetchOutRowDto;
import us.tx.state.dfps.xmlstructs.outputstructs.ContactFieldDoDto;
import us.tx.state.dfps.xmlstructs.outputstructs.EventIdFetchOutDto;
import us.tx.state.dfps.xmlstructs.outputstructs.EventPersonRetrvArrayOutDto;
import us.tx.state.dfps.xmlstructs.outputstructs.EventPersonRetrvOutDto;
import us.tx.state.dfps.xmlstructs.outputstructs.EventPersonRetrvRowOutDto;
import us.tx.state.dfps.xmlstructs.outputstructs.EventSearchStageOutDto;
import us.tx.state.dfps.xmlstructs.outputstructs.FacilInvstInfoDto;
import us.tx.state.dfps.xmlstructs.outputstructs.FindContactDto;
import us.tx.state.dfps.xmlstructs.outputstructs.StageCasePersonOutDto;
import us.tx.state.dfps.xmlstructs.outputstructs.StagePersonRetrvArrayOutDto;
import us.tx.state.dfps.xmlstructs.outputstructs.StagePersonRetrvOutDto;
import us.tx.state.dfps.xmlstructs.outputstructs.StagePersonRetrvRowOutDto;

/**
 * service-business- IMPACT PHASE 2 MODERNIZATION Class
 * Description:StageClosureEventServiceImpl Oct 31, 2017- 3:56:32 PM © 2017
 * Texas Department of Family and Protective Services
 */
@Service
@Transactional
public class StageClosureEventServiceImpl implements StageClosureEventService {

	@Autowired
	private FetchStageDao fetchStageDao;

	@Autowired
	private ContactSearchDao ContactSearchDao;

	@Autowired
	private EventFetDao eventFetDao;

	@Autowired
	private EventLastUpdateDao eventLastUpdateDao;

	@Autowired
	private FetchEventStatusDao fetchEventStatusDao;

	@Autowired
	private FacilityInvestigationDao facilityInvestigationDao;

	@Autowired
	private StageSearchEventDao stageSearchEventDao;

	@Autowired
	private ContactRtrvDao contactRtrvDao;

	@Autowired
	private ContactFieldDao contactFieldDao;

	@Autowired
	private StageCasePersonDao stageCasePersonDao;

	@Autowired
	private StagePersonRetrvDao stagePersonRetrvDao;

	@Autowired
	private EventPersonRetrvDao eventPersonRetrvDao;

	@Autowired
	MessageSource messageSource;

	private static final Logger log = Logger.getLogger(StageClosureEventServiceImpl.class);

	/**
	 * Method Name: getContactDetailCFRes Method Description:Determine if there
	 * is a Stage Conclusion Event in the tables
	 * 
	 * @param conGuideFetchInDto
	 * @return ConGuideFetchOutDto
	 */
	@Override
	@Transactional(readOnly = false, isolation = Isolation.READ_COMMITTED, propagation = Propagation.REQUIRED)
	public ConGuideFetchOutDto getContactDetailCFRes(ConGuideFetchInDto conGuideFetchInDto){
		log.debug("Entering method getContactDetailCFRes in StageClosureEventService");
		{
			ConGuideFetchOutDto conGuideFetchOutDto = new ConGuideFetchOutDto();
			Date dtDtStageStart = getStageStartDate(conGuideFetchInDto.getIdStage(), conGuideFetchOutDto);
			conGuideFetchOutDto.setIndStructNarrExists(
					getContactCount(conGuideFetchInDto, dtDtStageStart) ? ServiceConstants.Y : ServiceConstants.Y);
			getContactGuide(conGuideFetchInDto, conGuideFetchOutDto);
			if (ServiceConstants.ZERO_VAL != conGuideFetchInDto.getIdEvent()) {
				conGuideFetchOutDto.setEventIdFetchOutDto(getEventDetails(conGuideFetchInDto));
				Boolean eventIsNotNew = !ServiceConstants.EVENTSTATUS_NEW
						.equals(conGuideFetchOutDto.getEventIdFetchOutDto().getSzCdEventStatus());
				getNewContactGuide(conGuideFetchInDto, conGuideFetchOutDto);
				if (eventIsNotNew) {
					getContactGuideExisting(conGuideFetchInDto, conGuideFetchOutDto);
				}
				getEventLastUpdate(conGuideFetchInDto, conGuideFetchOutDto);
			} else {
				conGuideFetchOutDto.setEventIdFetchOutDto(new EventIdFetchOutDto());
			}
			Boolean eventIsNew = ServiceConstants.FALSEVAL;
			eventIsNew = !ObjectUtils.isEmpty(conGuideFetchOutDto.getEventIdFetchOutDto().getSzCdEventStatus())
					&& ServiceConstants.EVENTSTATUS_NEW
							.equals(conGuideFetchOutDto.getEventIdFetchOutDto().getSzCdEventStatus());
			if ((ServiceConstants.ZERO_VAL == conGuideFetchInDto.getIdEvent()) || eventIsNew) {
				getPrincipalsforStage(conGuideFetchInDto, conGuideFetchOutDto);
			}
			getEventstatus(conGuideFetchInDto, conGuideFetchOutDto);
			if (ServiceConstants.AFC_STAGE.equals(conGuideFetchOutDto.getCdStageProgram())
					&& ServiceConstants.INVESTIGATION.equals(conGuideFetchOutDto.getCdStage())) {
				getFacilityInvestigationDetail(conGuideFetchInDto, conGuideFetchOutDto);
				getFirstEREVContactDateForStage(conGuideFetchInDto, conGuideFetchOutDto);
			}
			getDateCaseOpenedForStage(conGuideFetchInDto, conGuideFetchOutDto);
			log.debug("Exiting method getContactDetailCFRes in Csys08sStageClosureEventServiceService");
			return conGuideFetchOutDto;
		}

	}

	/**
	 * 
	 * Method Name: getContactGuideExisting Method Description:this method is
	 * used to get the contact guide which is existing
	 * 
	 * @param conGuideFetchInDto
	 * @param conGuideFetchOutDto
	 */
	private void getContactGuideExisting(ConGuideFetchInDto conGuideFetchInDto,
			ConGuideFetchOutDto conGuideFetchOutDto) {
		EventPersonRetrvInDto eventPersonRetrvInDto = new EventPersonRetrvInDto();
		eventPersonRetrvInDto.setUlIdEvent(conGuideFetchInDto.getIdEvent());
		ServiceInputDto ServiceInputDto = new ServiceInputDto();
		ServiceInputDto.setUsPageNbr(conGuideFetchInDto.getServiceInputDto().getUsPageNbr());
		ServiceInputDto.setUlPageSizeNbr(conGuideFetchInDto.getServiceInputDto().getUlPageSizeNbr());
		eventPersonRetrvInDto.setServiceInputDto(ServiceInputDto);
		EventPersonRetrvOutDto eventPersonRetrvOutDto = eventPersonRetrvDao.getPersonIdsForStage(eventPersonRetrvInDto);
		Map<Long, ConGuideFetchOutRowDto> personRowMap = new HashMap<Long, ConGuideFetchOutRowDto>();
		ConGuideFetchOutArrayDto conGuideFetchOutArrayDto = conGuideFetchOutDto.getConGuideFetchOutArrayDto();
		for (ConGuideFetchOutRowDto conGuideFetchOutRowDto : conGuideFetchOutArrayDto.getConGuideFetchOutRowDtos()) {
			personRowMap.put(conGuideFetchOutRowDto.getUlIdPerson(), conGuideFetchOutRowDto);
		}
		EventPersonRetrvArrayOutDto eventPersonRetrvArrayOutDto = eventPersonRetrvOutDto.getPersonRetrvArrayOutDto();
		for (EventPersonRetrvRowOutDto eventPersonRetrvRowOutDto : eventPersonRetrvArrayOutDto
				.getEventPersonRetrvRowOutDtoList()) {
			Long ulIdPerson = eventPersonRetrvRowOutDto.getUlIdPerson();
			if (ulIdPerson == conGuideFetchOutDto.getIdPerson()) {
				conGuideFetchOutDto.setDtLastUpdate3(eventPersonRetrvRowOutDto.getTsLastUpdate());
			}

			ConGuideFetchOutRowDto conGuideFetchOutRowDto = (ConGuideFetchOutRowDto) personRowMap.get(ulIdPerson);
			if (!TypeConvUtil.isNullOrEmpty(conGuideFetchOutRowDto)) {
				conGuideFetchOutRowDto.setcSysIndContactOccurred(ServiceConstants.Y);
				conGuideFetchOutRowDto.setTsLastUpdate(eventPersonRetrvRowOutDto.getTsLastUpdate());
				conGuideFetchOutRowDto.setcIndPersRmvlNotified(eventPersonRetrvRowOutDto.getcIndPersRmvlNotified());
				conGuideFetchOutRowDto.setcIndKinNotifChild(eventPersonRetrvRowOutDto.getcIndKinNotifChild());
			}
		}
	}

	/**
	 * 
	 * Method Name: getNewContactGuide Method Description: this method is used
	 * to get the contact gueid details
	 * 
	 * @param conGuideFetchInDto
	 * @param conGuideFetchOutDto
	 */
	private void getNewContactGuide(ConGuideFetchInDto conGuideFetchInDto, ConGuideFetchOutDto conGuideFetchOutDto) {
		ContactFieldDiDto contactFieldDiDto = new ContactFieldDiDto();
		contactFieldDiDto.setIdEvent(conGuideFetchInDto.getIdEvent());
		contactFieldDiDto.setIdStage(conGuideFetchInDto.getIdStage());
		ContactFieldDoDto contactFieldDoDto = contactFieldDao.getContactDetails(contactFieldDiDto);
		if (!ObjectUtils.isEmpty(contactFieldDoDto)) {
			//Added the null condition for warranty defect 12105
			if (!ObjectUtils.isEmpty(contactFieldDoDto.getIdPerson())) {
				conGuideFetchOutDto.setIdPerson(contactFieldDoDto.getIdPerson());
			}
			if (!ObjectUtils.isEmpty(contactFieldDoDto.getNmPersonFull())) {
				conGuideFetchOutDto.setNmPersonFull(contactFieldDoDto.getNmPersonFull());
			}
			if (!ObjectUtils.isEmpty(contactFieldDoDto.getCdContactLocation())) {
				conGuideFetchOutDto.setCdContactLocation(contactFieldDoDto.getCdContactLocation());
			}
			if (!ObjectUtils.isEmpty(contactFieldDoDto.getCdContactMethod())) {
				conGuideFetchOutDto.setCdContactMethod(contactFieldDoDto.getCdContactMethod());
			}
			if (!ObjectUtils.isEmpty(contactFieldDoDto.getCdContactOthers())) {
				conGuideFetchOutDto.setCdContactOthers(contactFieldDoDto.getCdContactOthers());
			}
			if (!ObjectUtils.isEmpty(contactFieldDoDto.getCdContactPurpose())) {
				conGuideFetchOutDto.setCdContactPurpose(contactFieldDoDto.getCdContactPurpose());
			}
			if (!ObjectUtils.isEmpty(contactFieldDoDto.getCdContactType())) {
				conGuideFetchOutDto.setCdContactType(contactFieldDoDto.getCdContactType());
			}
			if (!ObjectUtils.isEmpty(contactFieldDoDto.getDtLastUpdate())) {
				conGuideFetchOutDto.setDtLastUpdate(contactFieldDoDto.getDtLastUpdate());
			}
			if (!ObjectUtils.isEmpty(contactFieldDoDto.getDtLastUpdateSecond())) {
				conGuideFetchOutDto.setDtLastUpdate2(contactFieldDoDto.getDtLastUpdateSecond());
			}
			if (!ObjectUtils.isEmpty(contactFieldDoDto.getDtLastUpdateThird())) {
				conGuideFetchOutDto.setDtLastUpdate3(contactFieldDoDto.getDtLastUpdateThird());
			}
			if (!ObjectUtils.isEmpty(contactFieldDoDto.getDtContactOccurred())) {
				conGuideFetchOutDto.setDtContactOccurred(contactFieldDoDto.getDtContactOccurred());
			}
			if (!ObjectUtils.isEmpty(contactFieldDoDto.getDtMonthlySummBegin())) {
				conGuideFetchOutDto.setDtMonthlySummBegin(contactFieldDoDto.getDtMonthlySummBegin());
			}
			if (!ObjectUtils.isEmpty(contactFieldDoDto.getDtMonthlySummEnd())) {
				conGuideFetchOutDto.setDtMonthlySummEnd(contactFieldDoDto.getDtMonthlySummEnd());
			}
			SimpleDateFormat localDateFormat = new SimpleDateFormat(ServiceConstants.TIME_FORMAT);
			//Warranty Defect#12244 Removed the unwanted null check to set the contact time
			if (!ObjectUtils.isEmpty(contactFieldDoDto.getDtContactOccurred())) {
				String time = localDateFormat.format(conGuideFetchOutDto.getDtContactOccurred());
				contactFieldDoDto.setTmScrTmCntct(time);
			}
			ConGuideFetchArrayDto conGuideFetchArrayDto = new ConGuideFetchArrayDto();
			if (!ObjectUtils.isEmpty(contactFieldDoDto.getTmScrTmCntct())) {
				addTmScrTmCntct(contactFieldDoDto.getTmScrTmCntct(), conGuideFetchArrayDto);
			}

			conGuideFetchOutDto.setConGuideFetchArrayDto(conGuideFetchArrayDto);
			if (!ObjectUtils.isEmpty(contactFieldDoDto.getIndContactAttempted())) {
				conGuideFetchOutDto.setIndContactAttempted(contactFieldDoDto.getIndContactAttempted());
			}
			if (!ObjectUtils.isEmpty(contactFieldDoDto.getCdReasonScreenOut())) {
				conGuideFetchOutDto.setCdReasonScreenOut(contactFieldDoDto.getCdReasonScreenOut());
			}
			if (!ObjectUtils.isEmpty(contactFieldDoDto.getIndKinRecmd())) {
				conGuideFetchOutDto.setIndKinRecmd(contactFieldDoDto.getIndKinRecmd());
			}
			if (!ObjectUtils.isEmpty(contactFieldDoDto.getNmKnCgvr())) {
				conGuideFetchOutDto.setNmKnCgvr(contactFieldDoDto.getNmKnCgvr());
			}
			if (!ObjectUtils.isEmpty(contactFieldDoDto.getCdRsnNotNeed())) {
				conGuideFetchOutDto.setCdRsnNotNeed(contactFieldDoDto.getCdRsnNotNeed());
			}
			if (!ObjectUtils.isEmpty(contactFieldDoDto.getAmtNeeded())) {
				conGuideFetchOutDto.setAmtNeeded(contactFieldDoDto.getAmtNeeded());
			} else {
				conGuideFetchOutDto.setAmtNeeded(ServiceConstants.Zero);				
			}
			if (!ObjectUtils.isEmpty(contactFieldDoDto.getIndSiblingVisit())) {
				conGuideFetchOutDto.setIndSiblingVisit(contactFieldDoDto.getIndSiblingVisit());
			}
			if (!ObjectUtils.isEmpty(contactFieldDoDto.getIndAnnounced())) {
				conGuideFetchOutDto.setSetBIndAnnounced(contactFieldDoDto.getIndAnnounced());
			}
			if (!ObjectUtils.isEmpty(contactFieldDoDto.getCdChildSafety())) {
				conGuideFetchOutDto.setSzCdChildSafety(contactFieldDoDto.getCdChildSafety());
			}
			if (!ObjectUtils.isEmpty(contactFieldDoDto.getCdPendLegalAction())) {
				conGuideFetchOutDto.setSzCdPendLegalAction(contactFieldDoDto.getCdPendLegalAction());
			}
			if (!ObjectUtils.isEmpty(contactFieldDoDto.getIndPrinInterview())) {
				conGuideFetchOutDto.setbIndPrinInterview(contactFieldDoDto.getIndPrinInterview());
			}
			if (!ObjectUtils.isEmpty(contactFieldDoDto.getCdProfCollateral())) {
				conGuideFetchOutDto.setSzCdProfCollateral(contactFieldDoDto.getCdProfCollateral());
			}
			if (!ObjectUtils.isEmpty(contactFieldDoDto.getCdAdministrative())) {
				conGuideFetchOutDto.setSzCdAdministrative(contactFieldDoDto.getCdAdministrative());
			}
			if (!ObjectUtils.isEmpty(contactFieldDoDto.getComments())) {
				conGuideFetchOutDto.setSzTxtComments(contactFieldDoDto.getComments());
			}
			if (!ObjectUtils.isEmpty(contactFieldDoDto.getIndFamPlnCompleted())) {
				conGuideFetchOutDto.setbIndFamPlnCompleted(contactFieldDoDto.getIndFamPlnCompleted());
			}
			if (!ObjectUtils.isEmpty(contactFieldDoDto.getIndSafPlnCompleted())) {
				conGuideFetchOutDto.setbIndSafPlnCompleted(contactFieldDoDto.getIndSafPlnCompleted());
			}
			if (!ObjectUtils.isEmpty(contactFieldDoDto.getIndSafConResolved())) {
				conGuideFetchOutDto.setbBIndSafConResolved(contactFieldDoDto.getIndSafConResolved());
			}
			if (!ObjectUtils.isEmpty(contactFieldDoDto.getNmPersonFull())) {
				conGuideFetchOutDto.setUlNbrHours(contactFieldDoDto.getNbrHours());
			}
			if (!ObjectUtils.isEmpty(contactFieldDoDto.getNbrMins())) {
				conGuideFetchOutDto.setUlNbrMins(contactFieldDoDto.getNbrMins());
			}
			/* artf128844 - Changes for FCL: ORDER #9 - START */
			if (!StringUtils.isEmpty(contactFieldDoDto.getCdFtfExceptionRsn())) {
				conGuideFetchOutDto.setCdFtfExceptionRsn(contactFieldDoDto.getCdFtfExceptionRsn());
			}
			/* artf128844 - Changes for FCL: ORDER #9 - END */
		}
	}

	/**
	 * 
	 * Method Name: addTmScrTmCntct Method Description: this method is used to
	 * populate the time for contact
	 * 
	 * @param tmScrTmCntct
	 * @param conGuideFetchArrayDto
	 */
	private void addTmScrTmCntct(String tmScrTmCntct, ConGuideFetchArrayDto conGuideFetchArrayDto) {
		if (conGuideFetchArrayDto.getTmScrTmCntctList().size() >= ServiceConstants.TWO_INT) {
			throw new ServiceLayerException(messageSource.getMessage("Index.Out.of.bound", null, Locale.US));
		}
		conGuideFetchArrayDto.getTmScrTmCntctList().add(tmScrTmCntct);
	}

	/**
	 * 
	 * Method Name: getEventLastUpdate Method Description: this method is used
	 * to get the event last update date
	 * 
	 * @param conGuideFetchInDto
	 * @param conGuideFetchOutDto
	 */
	private void getEventLastUpdate(ConGuideFetchInDto conGuideFetchInDto, ConGuideFetchOutDto conGuideFetchOutDto) {
		EventLastUpdatedInDto eventLastUpdatediDto = new EventLastUpdatedInDto();
		eventLastUpdatediDto.setIdEvent(conGuideFetchInDto.getIdEvent());
		eventLastUpdatediDto.setNmTable(conGuideFetchInDto.getNmTable());
		ServiceInputDto serviceInputDto = new ServiceInputDto();
		serviceInputDto.setSzUserId(conGuideFetchInDto.getServiceInputDto().getSzUserId());
		eventLastUpdatediDto.setServiceInputDto(serviceInputDto);
		EventLastUpdatedoDto eventLastUpdatedoDto = eventLastUpdateDao.getDtLastUpdateForEvent(eventLastUpdatediDto);
		if (!ObjectUtils.isEmpty(eventLastUpdatedoDto.getTsLastUpdate())) {
			conGuideFetchOutDto.setNarrStatus(ServiceConstants.NARRATIVE_TXT_DESCR);
			conGuideFetchOutDto.setDtLastUpdate(eventLastUpdatedoDto.getTsLastUpdate());
		}
	}

	/**
	 * 
	 * Method Name: getPrincipalsforStage Method Description: this method is
	 * used to get the principals for the given stage
	 * 
	 * @param conGuideFetchInDto
	 * @param conGuideFetchOutDto
	 */
	private void getPrincipalsforStage(ConGuideFetchInDto conGuideFetchInDto, ConGuideFetchOutDto conGuideFetchOutDto) {
		StageCasePersonInDto stageCasePersonInDto = new StageCasePersonInDto();
		stageCasePersonInDto.setUlIdStage(conGuideFetchInDto.getIdStage());
		StageCasePersonOutDto stageCasePersonOutDto = stageCasePersonDao.getPrincipalsForStage(stageCasePersonInDto);
		if (!ObjectUtils.isEmpty(stageCasePersonOutDto)) {
			conGuideFetchOutDto.setIdPerson(stageCasePersonOutDto.getUlIdPerson());
			conGuideFetchOutDto.setNmPersonFull(stageCasePersonOutDto.getSzNmPersonFull());
			conGuideFetchOutDto.setIndContactAttempted(String.valueOf(ServiceConstants.INDICATOR_NO));
			conGuideFetchOutDto.setDtContactOccurred(new Date());
			conGuideFetchOutDto.setDtMonthlySummEnd(conGuideFetchOutDto.getDtContactOccurred());
			conGuideFetchOutDto.setDtMonthlySummBegin(null);
		}
	}

	/**
	 * 
	 * Method Name: getStageStartDate Method Description: this method is used to
	 * get the stage start date
	 * 
	 * @param ulIdStage
	 * @param conGuideFetchOutDto
	 * @return Date
	 */
	private Date getStageStartDate(Long ulIdStage, ConGuideFetchOutDto conGuideFetchOutDto) {
		FetchStagediDto fetchStagediDto = new FetchStagediDto();
		fetchStagediDto.setUlIdStage(ulIdStage);
		FetchStagedoDto fetchStagedoDto = new FetchStagedoDto();
		fetchStagedoDto = fetchStageDao.getStageDetails(fetchStagediDto).get(ServiceConstants.Zero);
		conGuideFetchOutDto.setCdStage(fetchStagedoDto.getSzCdStage());
		conGuideFetchOutDto.setCdStageProgram(fetchStagedoDto.getSzCdStageProgram());
		conGuideFetchOutDto.setCdStageClassification(fetchStagedoDto.getSzCdStageClassification());
		conGuideFetchOutDto.setDtStageClose(fetchStagedoDto.getDtDtStageClose());
		return fetchStagedoDto.getDtDtStageStart();

	}

	/**
	 * 
	 * Method Name: getEventDetails Method Description: this method is used to
	 * get the event details
	 * 
	 * @param ConGuideFetchInDto
	 * @return EventIdFetchOutDto
	 */
	private EventIdFetchOutDto getEventDetails(ConGuideFetchInDto ConGuideFetchInDto){
		FetchEventDto fetchEventDto = new FetchEventDto();
		fetchEventDto.setIdEvent(ConGuideFetchInDto.getIdEvent());
		EventIdFetchOutDto eventIdFetchOutDto = new EventIdFetchOutDto();
		FetchEventRowDto fetchEventRowDto = new FetchEventRowDto();
		fetchEventRowDto = eventFetDao.fetchEventDetails(fetchEventDto).getFetchEventRowDto();
		eventIdFetchOutDto.setUlIdEvent(fetchEventRowDto.getIdEvent());
		eventIdFetchOutDto.setSzCdTask(fetchEventRowDto.getCdTask());
		eventIdFetchOutDto.setTsLastUpdate(fetchEventRowDto.getDtLastUpdate());
		eventIdFetchOutDto.setSzCdEventType(fetchEventRowDto.getCdEventType());
		eventIdFetchOutDto.setSzCdEventStatus(fetchEventRowDto.getCdEventStatus());
		eventIdFetchOutDto.setDtDtEventOccurred(fetchEventRowDto.getDtEventOccurred());
		eventIdFetchOutDto.setDtDtEventCreated(fetchEventRowDto.getDtDtEventCreated());
		eventIdFetchOutDto.setUlIdStage(fetchEventRowDto.getIdStage());
		eventIdFetchOutDto.setUlIdPerson(fetchEventRowDto.getIdPerson());
		eventIdFetchOutDto.setSzTxtEventDescr(fetchEventRowDto.getTxtEventDescr());
		return eventIdFetchOutDto;
	}

	/**
	 * 
	 * Method Name: getContactGuide Method Description: this method is used to
	 * get the contact guid details
	 * 
	 * @param conGuideFetchInDto
	 * @param conGuideFetchOutDto
	 */
	private void getContactGuide(ConGuideFetchInDto conGuideFetchInDto, ConGuideFetchOutDto conGuideFetchOutDto) {
		StagePersonRetrvInDto stagePersonRetrvInDto = new StagePersonRetrvInDto();
		ServiceInputDto serviceInputDto = new ServiceInputDto();
		serviceInputDto.setUsPageNbr(conGuideFetchInDto.getServiceInputDto().getUsPageNbr());
		serviceInputDto.setUlPageSizeNbr(conGuideFetchInDto.getServiceInputDto().getUlPageSizeNbr());
		stagePersonRetrvInDto.setServiceInputDto(serviceInputDto);
		stagePersonRetrvInDto.setUlIdStage(conGuideFetchInDto.getIdStage());
		stagePersonRetrvInDto.setSzCdStagePersType(ServiceConstants.STAFF_TYPE);
		ConGuideFetchOutArrayDto conGuideFetchOutArrayDto = new ConGuideFetchOutArrayDto();
		StagePersonRetrvOutDto stagePersonRetrvOutDto = stagePersonRetrvDao
				.getPersonDetailsForStage(stagePersonRetrvInDto);
		conGuideFetchOutDto.setServiceOutputDto(stagePersonRetrvOutDto.getServiceOutputDto());
		StagePersonRetrvArrayOutDto stagePersonRetrvArrayOutDto = stagePersonRetrvOutDto
				.getStagePersonRetrvArrayOutDto();
		for (StagePersonRetrvRowOutDto stagePersonRetrvRowOutDto : stagePersonRetrvArrayOutDto
				.getStagePersonRetrvRowOutDtoList()) {
			if (stagePersonRetrvRowOutDto.getUlIdPerson() != ServiceConstants.ZERO_VAL) {
				ConGuideFetchOutRowDto conGuideFetchOutRowDto = new ConGuideFetchOutRowDto();
				conGuideFetchOutRowDto
						.setSzNmPersonFull(!StringUtils.isEmpty(stagePersonRetrvRowOutDto.getSzCdNameSuffix())
								? stagePersonRetrvRowOutDto.getSzNmPersonFull() + ' '
										+ stagePersonRetrvRowOutDto.getSzCdNameSuffix()
								: stagePersonRetrvRowOutDto.getSzNmPersonFull());
				conGuideFetchOutRowDto.setSzCdStagePersRole(stagePersonRetrvRowOutDto.getSzCdStagePersRole());
				conGuideFetchOutRowDto.setSzCdStagePersRelInt(stagePersonRetrvRowOutDto.getSzCdStagePersRelInt());
				conGuideFetchOutRowDto.setSzCdStagePersType(stagePersonRetrvRowOutDto.getSzCdStagePersType());
				conGuideFetchOutRowDto.setUlIdPerson(stagePersonRetrvRowOutDto.getUlIdPerson());
				conGuideFetchOutRowDto.setDtDtPersonBirth(stagePersonRetrvRowOutDto.getDtDtPersonBirth());
				conGuideFetchOutRowDto
						.setSzCdPersonMaritalStatus(stagePersonRetrvRowOutDto.getSzCdPersonMaritalStatus());
				/* artf128844 - Changes for FCL: ORDER #9 - START */
				conGuideFetchOutRowDto.setDtDtPersonDeath(stagePersonRetrvRowOutDto.getDtDtPersonDeath());
				/* artf128844 - Changes for FCL: ORDER #9 - END */
				addROWCSYS08SO(conGuideFetchOutArrayDto, conGuideFetchOutRowDto);
			}
		}
		conGuideFetchOutDto.setConGuideFetchOutArrayDto(conGuideFetchOutArrayDto);
	}

	/**
	 * 
	 * Method Name: addROWCSYS08SO Method Description: this method is used to
	 * populate the contact guide
	 * 
	 * @param conGuideFetchOutArrayDto
	 * @param conGuideFetchOutRowDto
	 */
	private void addROWCSYS08SO(ConGuideFetchOutArrayDto conGuideFetchOutArrayDto,
			ConGuideFetchOutRowDto conGuideFetchOutRowDto) {
		{
			//Defect-13121 removed the condition to check for list size
			conGuideFetchOutArrayDto.getConGuideFetchOutRowDtos().add(conGuideFetchOutRowDto);
		}
	}

	/**
	 * 
	 * Method Name: getContactCount Method Description:
	 * 
	 * @param conGuideFetchInDto
	 * @param dtDtStageStart
	 * @return Boolean
	 */
	private Boolean getContactCount(ConGuideFetchInDto conGuideFetchInDto, Date dtDtStageStart) {
		ContactSearchDto contactSearchDto = new ContactSearchDto();
		contactSearchDto.setSzCdContactLocation(null);
		contactSearchDto.setSzCdContactMethod(null);
		contactSearchDto.setSzCdContactOthers(null);
		contactSearchDto.setSzCdContactPurpose(null);
		contactSearchDto.setSzCdContactType(null);
		contactSearchDto.setUlIdStage(conGuideFetchInDto.getIdStage());
		contactSearchDto.setDtScrSearchDateFrom(dtDtStageStart);
		contactSearchDto.setDtScrSearchDateTo(null);
		ServiceInputDto ServiceInputDto = new ServiceInputDto();
		ServiceInputDto.setUsPageNbr(ServiceConstants.INITIAL_PAGE);
		ServiceInputDto.setUlPageSizeNbr(ServiceConstants.CSYS04DO__ROWCSYS04DO_SIZE);
		ServiceInputDto.setSzUserId(conGuideFetchInDto.getServiceInputDto().getSzUserId());
		contactSearchDto.setArchInputStruct(ServiceInputDto);
		FindContactDto findContactDto = new FindContactDto();
		findContactDto = ContactSearchDao.searchContacts(contactSearchDto);
		return findContactDto.getServiceOutputDto().getRowQtySize() > ServiceConstants.ZERO_VAL;
	}

	/**
	 * 
	 * Method Name: getEventstatus Method Description: this method is used to
	 * get the event status
	 * 
	 * @param conGuideFetchInDto
	 * @param conGuideFetchOutDto
	 */
	private void getEventstatus(ConGuideFetchInDto conGuideFetchInDto, ConGuideFetchOutDto conGuideFetchOutDto) {
		FetchEventStatusdiDto fetchEventStatusdiDto = new FetchEventStatusdiDto();
		fetchEventStatusdiDto.setUlIdStage(conGuideFetchInDto.getIdStage());
		fetchEventStatusdiDto.setUlIdCase(ServiceConstants.ZERO_VAL);
		fetchEventStatusdiDto.setUlIdPerson(ServiceConstants.ZERO_VAL);
		InvestigationConclusionDto investigationConclusionDto = new InvestigationConclusionDto();
		StageEventdiDto stageEventdiDto = new StageEventdiDto();
		Boolean isInvest = ServiceConstants.INVEST.equals(conGuideFetchOutDto.getCdStage());
		stageEventdiDto.setSzCdEventType(
				isInvest ? ServiceConstants.SVC_CD_EVENT_TYPE_CONCL : ServiceConstants.SVC_CD_EVENT_TYPE_CLOSE);
		investigationConclusionDto.addRowccmn87di(stageEventdiDto);
		fetchEventStatusdiDto.setInvestigationConclusionDto(investigationConclusionDto);
		ServiceInputDto ServiceInputDto = new ServiceInputDto();
		ServiceInputDto.setUsPageNbr(ServiceConstants.INITIAL_PAGE);
		ServiceInputDto.setUlPageSizeNbr(ServiceConstants.INITIAL_PAGE);
		ServiceInputDto.setCreqFuncCd(ServiceConstants.REQ_FUNC_CD_UPDATE);
		fetchEventStatusdiDto.setArchInputStruct(ServiceInputDto);
		conGuideFetchOutDto.setIdEvent(ServiceConstants.Zero_Value);
		FetchEventStatusdoDto fetchEventStatusdoDto = fetchEventStatusDao.searchEvents(fetchEventStatusdiDto);
		if (fetchEventStatusdoDto.getRowccmn87doArrayDto().getRowccmn87doCount() > ServiceConstants.ZERO_VAL) {
			CaseEventDto caseEventDto = fetchEventStatusdoDto.getRowccmn87doArrayDto()
					.getRowccmn87do(ServiceConstants.Zero);
			if ((ServiceConstants.SVC_CD_EVENT_STATUS_PENDING.equals(caseEventDto.getSzCdEventStatus()))) {
				conGuideFetchOutDto.setIdEvent(caseEventDto.getUlIdEvent());
			}
		}
	}

	/**
	 * 
	 * Method Name: getFacilityInvestigationDetail Method Description: this
	 * method is used to get the facility investigation
	 * 
	 * @param conGuideFetchInDto
	 * @param conGuideFetchOutDto
	 */
	private void getFacilityInvestigationDetail(ConGuideFetchInDto conGuideFetchInDto,
			ConGuideFetchOutDto conGuideFetchOutDto) {
		FacilityInvestigationDto facilityInvestigationDto = new FacilityInvestigationDto();
		facilityInvestigationDto.setUlIdStage(conGuideFetchInDto.getIdStage().intValue());
		FacilInvstInfoDto cinv17do = facilityInvestigationDao.getFacilityInvestigationDetail(facilityInvestigationDto);
		conGuideFetchOutDto.setcIndFacilSuperintNotif(cinv17do.getcIndFacilSuperintNotif());
	}

	/**
	 * 
	 * Method Name: getFirstEREVContactDateForStage Method Description: this
	 * method is used to get the contact Date for particular stage
	 * 
	 * @param conGuideFetchInDto
	 * @param conGuideFetchOutDto
	 */
	private void getFirstEREVContactDateForStage(ConGuideFetchInDto conGuideFetchInDto,
			ConGuideFetchOutDto conGuideFetchOutDto) {
		ContactRtrvInDto contactRtrvInDto = new ContactRtrvInDto();
		contactRtrvInDto.setUlIdStage(conGuideFetchInDto.getIdStage());
		ContactRtrvOutDto contactRtrvOutDto = contactRtrvDao.getFirstEREVContactDateForStage(contactRtrvInDto);
		if (!DateUtils.isNull(contactRtrvOutDto.getDtDTContactOccurred())) {
			conGuideFetchOutDto.setNbrReviewContact(ServiceConstants.REVIEW_CONTACT);
		}
	}

	/**
	 * 
	 * Method Name: getDateCaseOpenedForStage Method Description: this method is
	 * used to get the date, for which case was opened for given stage
	 * 
	 * @param conGuideFetchInDto
	 * @param conGuideFetchOutDto
	 */
	private void getDateCaseOpenedForStage(ConGuideFetchInDto conGuideFetchInDto,
			ConGuideFetchOutDto conGuideFetchOutDto) {
		EventSearchStageInDto eventSearchStageInDto = new EventSearchStageInDto();
		eventSearchStageInDto.setServiceInputDto(conGuideFetchInDto.getServiceInputDto());
		eventSearchStageInDto.setUlIdStage(conGuideFetchInDto.getIdStage());
		EventSearchStageOutDto eventSearchStageOutDto = stageSearchEventDao
				.getDateCaseOpenedForStage(eventSearchStageInDto);
		if (!ObjectUtils.isEmpty(eventSearchStageOutDto.getDtCaseOpened())) {
			conGuideFetchOutDto.setDtDtIntStart(eventSearchStageOutDto.getDtCaseOpened());
		}
	}
}
