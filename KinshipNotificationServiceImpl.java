package us.tx.state.dfps.service.contacts.serviceimpl;

import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.collections.CollectionUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.ObjectUtils;

import us.tx.state.dfps.common.dto.GenericCaseInfoDto;
import us.tx.state.dfps.common.dto.KinshipNotificationDto;
import us.tx.state.dfps.common.dto.PersonAddressDto;
import us.tx.state.dfps.common.dto.WorkerDetailDto;
import us.tx.state.dfps.populateletter.dto.CodesTablesDto;
import us.tx.state.dfps.service.common.ServiceConstants;
import us.tx.state.dfps.service.common.dao.StageDao;
import us.tx.state.dfps.service.common.request.KinshipNotificationReq;
import us.tx.state.dfps.service.contacts.dao.KinshipNotificationDao;
import us.tx.state.dfps.service.contacts.service.KinshipNotificationService;
import us.tx.state.dfps.service.disasterplan.dao.DisasterPlanDao;
import us.tx.state.dfps.service.forms.dto.PreFillDataServiceDto;
import us.tx.state.dfps.service.forms.util.KinshipNotificationPrefillData;
import us.tx.state.dfps.service.medicalconsenter.daoimpl.MedicalConsenterRtrvDaoImpl;
import us.tx.state.dfps.service.pca.dao.PcaDao;
import us.tx.state.dfps.service.person.dao.PersonPhoneDao;
import us.tx.state.dfps.service.person.dto.PersonPhoneRetDto;
import us.tx.state.dfps.service.person.dto.SupervisorDto;
import us.tx.state.dfps.service.populateletter.dao.PopulateLetterDao;
import us.tx.state.dfps.service.workload.dto.PersonDto;
import us.tx.state.dfps.service.workload.dto.StagePersonDto;

/**
 * service-business- IMPACT PHASE 2 MODERNIZATION Class Description :MONTHLY
 * EVALUATION FORM Tuxedo Service :CKIN07S Mar 28, 2018- 11:24:47 AM © 2017
 * Texas Department of Family and Protective Services
 */

@Service
@Transactional
public class KinshipNotificationServiceImpl implements KinshipNotificationService {

	private static final String STAGE_PER_ROLE = "PR";

	private static final String CODETYPE_TITLE = "CBRDTTLE";

	private static final String CODETYPE_NAME = "CBRDNAME";
	private static final int CHILD_AGE_LIMIT = 18;

	@Autowired
	private StageDao stageDao;

	@Autowired
	private PcaDao pcaDao;

	@Autowired
	private DisasterPlanDao disasterPlanDao;

	@Autowired
	private KinshipNotificationDao kinshipNotificationDao;

	@Autowired
	private PopulateLetterDao populateLetterDao;

	@Autowired
	PersonPhoneDao personPhoneDao;

	@Autowired
	KinshipNotificationPrefillData kinshipNotificationPrefillData;

	public KinshipNotificationServiceImpl() {
		super();
	}

	/**
	 * Method Name: getKinshipNotificationDetails Method Description: Fetches
	 * Kinship Notification Details
	 * 
	 * @param cpsInvConclValReq
	 * @return PreFillDataServiceDto
	 */

	private static final Logger log = Logger.getLogger(MedicalConsenterRtrvDaoImpl.class);

	@Transactional(readOnly = true, isolation = Isolation.READ_COMMITTED, propagation = Propagation.REQUIRED, rollbackFor = {
			Exception.class })
	public PreFillDataServiceDto getKinshipNotificationDetails(KinshipNotificationReq kinshipNotificationReq) {

		KinshipNotificationDto kinshipNotificationDto = new KinshipNotificationDto();
		log.debug("Entering method getPersonNamesDtls in KinshipNotificationServiceImpl");
		// CCMN19D Call DAM for worker ID
		StagePersonDto stagePersonDto = stageDao.getStagePersonLinkDetails(kinshipNotificationReq.getIdStage(),
				STAGE_PER_ROLE);
		kinshipNotificationDto.setStagePersonDto(stagePersonDto);

		// CCMN60D Call DAM for Supervisor
		SupervisorDto supervisorDto = new SupervisorDto();
		if (!ObjectUtils.isEmpty(stagePersonDto) && !ObjectUtils.isEmpty(stagePersonDto.getIdTodoPersWorker())) {
			supervisorDto = pcaDao.getSupervisorPersonId(stagePersonDto.getIdTodoPersWorker());
			kinshipNotificationDto.setSupervisorDto(supervisorDto);
		}

		// CSEC01D Call employee address/phone DAM
		if (!ObjectUtils.isEmpty(stagePersonDto) && !ObjectUtils.isEmpty(stagePersonDto.getIdTodoPersWorker())) {
			WorkerDetailDto workerDetailDtoCW = disasterPlanDao.getWorkerInfoById(stagePersonDto.getIdTodoPersWorker());
			kinshipNotificationDto.setWorkerDetailDtoCW(workerDetailDtoCW);
		}

		// CSEC01D Call supervisor address/phone DAM
		if (!ObjectUtils.isEmpty(supervisorDto.getIdPerson())) {
			WorkerDetailDto workerDetailDtoSV = disasterPlanDao.getWorkerInfoById(supervisorDto.getIdPerson());
			kinshipNotificationDto.setWorkerDetailDtoSV(workerDetailDtoSV);
		}

		// CLSCGFD
		List<PersonDto> personDtoList = kinshipNotificationDao.getPersonNamesDtls(kinshipNotificationReq.getIdEvent());
		kinshipNotificationDto.setPersonDtoList(personDtoList);
		
		//Below code is to set the child list selected in the contact 
		if (!CollectionUtils.isEmpty(personDtoList)) {
			List<PersonDto> childList = personDtoList.stream().filter(
					person -> ((CHILD_AGE_LIMIT > getAge(person.getDtPersonBirth(), person.getDtEventOccurred()))
							&& ServiceConstants.PRN_TYPE.equals(person.getCdStagePersType())
							&& ((ObjectUtils.isEmpty(person.getCdStagePersRelInt()))
									|| !("AB".equals(person.getCdStagePersRelInt())
											|| "PA".equals(person.getCdStagePersRelInt())
											|| "PG".equals(person.getCdStagePersRelInt())
											|| "PB".equals(person.getCdStagePersRelInt())
											|| "PD".equals(person.getCdStagePersRelInt())
											|| "ST".equals(person.getCdStagePersRelInt())))))
					.collect(Collectors.toList());
			kinshipNotificationDto.setChildDtoList(childList);
		}
		
		// CLSC03D
		List<CodesTablesDto> codestableDtoList = populateLetterDao.getPersonInfoByCode(CODETYPE_TITLE, CODETYPE_NAME);
		kinshipNotificationDto.setCodestableDtoList(codestableDtoList);

		// CSEC02D
		GenericCaseInfoDto genericCaseInfoDto = disasterPlanDao.getGenericCaseInfo(kinshipNotificationReq.getIdStage());
		kinshipNotificationDto.setGenericCaseInfoDto(genericCaseInfoDto);

		// CLSCGHD
		List<PersonAddressDto> personAddressDtoList = kinshipNotificationDao
				.getPersonAddress(kinshipNotificationReq.getIdEvent());
		kinshipNotificationDto.setPersonAddressDtoList(personAddressDtoList);

		// CCMNB0D
		List<PersonPhoneRetDto> personPhoneRetDto = kinshipNotificationDao
				.getPersonPhoneDetailList(stagePersonDto.getIdTodoPersWorker());
		kinshipNotificationDto.setPersonPhoneRetDto(personPhoneRetDto);

		// calling prefillData method with kinshipNotificationDto as i/p
		// parameter
		return kinshipNotificationPrefillData.returnPrefillData(kinshipNotificationDto);

	}
	/**
	 * 
	 *Method Name:	getAge
	 *Method Description: This method is used to calculate the age of the child.
	 *@param birthdate
	 *@param fromDate
	 *@return
	 */
	private static int getAge(Date birthdate, Date fromDate) {
		int age = ServiceConstants.Zero;		
		Calendar fromDateCal = Calendar.getInstance();
		Calendar toDate = Calendar.getInstance();
		if (!ObjectUtils.isEmpty(birthdate) && !ObjectUtils.isEmpty(fromDate)) {
			fromDateCal.setTime(fromDate);
			toDate.setTime(birthdate);
			age = fromDateCal.get(Calendar.YEAR) - toDate.get(Calendar.YEAR);
			// birthday has occurred
			if ((toDate.get(Calendar.MONTH) > fromDateCal.get(Calendar.MONTH)) ||
			// birthday has occurred in current month
					((toDate.get(Calendar.MONTH) == fromDateCal.get(Calendar.MONTH)) && ((toDate.get(Calendar.DAY_OF_MONTH) > fromDateCal.get(Calendar.DAY_OF_MONTH))))) {
				age = age - 1;
			}
		}
		return age;
	} 

}
