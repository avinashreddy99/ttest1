package us.tx.state.dfps.service.common.daoimpl;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import org.apache.log4j.Logger;
import org.hibernate.Criteria;
import org.hibernate.Query;
import org.hibernate.SQLQuery;
import org.hibernate.SessionFactory;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;
import org.hibernate.transform.Transformers;
import org.hibernate.type.StandardBasicTypes;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ObjectUtils;

import us.tx.state.dfps.common.domain.CapsCase;
import us.tx.state.dfps.common.domain.Situation;
import us.tx.state.dfps.common.domain.Stage;
import us.tx.state.dfps.common.domain.StageLink;
import us.tx.state.dfps.common.domain.StagePersonLink;
import us.tx.state.dfps.common.domain.Todo;
import us.tx.state.dfps.common.dto.ServiceReqHeaderDto;
import us.tx.state.dfps.common.dto.StagePersonValueDto;
import us.tx.state.dfps.common.exception.InvalidRequestException;
import us.tx.state.dfps.service.admin.dto.StageInsUpdDelOutDto;
import us.tx.state.dfps.service.alternativeresponse.dto.EventValueDto;
import us.tx.state.dfps.service.casepackage.dto.SelectStageDto;
import us.tx.state.dfps.service.casepackage.dto.StageValueBeanDto;
import us.tx.state.dfps.service.common.CodesConstant;
import us.tx.state.dfps.service.common.ServiceConstants;
import us.tx.state.dfps.service.common.dao.StageDao;
import us.tx.state.dfps.service.common.request.CommonHelperReq;
import us.tx.state.dfps.service.common.request.PriorityClosureSaveReq;
import us.tx.state.dfps.service.common.response.FacilRtrvRes;
import us.tx.state.dfps.service.common.util.TypeConvUtil;
import us.tx.state.dfps.service.conservatorship.dto.StageIncomingDto;
import us.tx.state.dfps.service.exception.DataLayerException;
import us.tx.state.dfps.service.exception.DataNotFoundException;
import us.tx.state.dfps.service.stage.dto.PrimaryWorker;
import us.tx.state.dfps.service.workload.dto.EventStagePersonDto;
import us.tx.state.dfps.service.workload.dto.EventStageSearchDto;
import us.tx.state.dfps.service.workload.dto.IntakeNotfChildDto;
import us.tx.state.dfps.service.workload.dto.PersonDto;
import us.tx.state.dfps.service.workload.dto.StageDto;
import us.tx.state.dfps.service.workload.dto.StageIdDto;
import us.tx.state.dfps.service.workload.dto.StagePersDto;
import us.tx.state.dfps.service.workload.dto.StagePersonDto;
import us.tx.state.dfps.service.workload.dto.StagePersonLinkDto;
import us.tx.state.dfps.service.workload.dto.TodoDto;

/**
 * 
 ****************** change history *********************************
 * 11/1/2019  kanakas artf129782: Licensing Investigation Conclusion
 *
 */
@Repository
public class StageDaoImpl implements StageDao {

	@Autowired
	MessageSource messageSource;

	@Value("${StageDaoImpl.getPrimaryCaseWorker}")
	private transient String getPrimaryCaseWorkerSql;

	@Value("${StageDaoImpl.getFacilityDetail}")
	private transient String getFacilityDetailsql;

	@Value("${StagePersonLink.getStagePersonDetails}")
	private String getStagePersonDetails;

	@Value("${SearchStageDaoImpl.searchStageByCaseIdSql}")
	private String searchStageByCaseIdSql;

	@Value("${StageDaoImpl.searchStageEventById}")
	private String searchStageEventByIdSql;

	@Value("${StageDaoImpl.getStagesByType}")
	private String getStagesByTypeSql;

	@Value("${StageDaoImpl.getStagesByTypeAndIndStageClose}")
	private String getStagesByTypeAndIndStageCloseSql;

	@Value("${StageDaoImpl.getStagesByTypeOrderBy}")
	private String getStagesByTypeOrderBySql;

	@Value("${StageDaoImpl.findPrimaryChildForStage}")
	private String findPrimaryChildForStageSql;

	@Value("${StageDaoImpl.hasStageAccess}")
	private String hasStageAccessSql;

	@Value("${StageDaoImpl.getCountStageByAttributes}")
	private String countStageByAttributes;

	@Value("${StageDaoImpl.getIntakeDates}")
	private String intakeDates;

	@Value("${StageDaoImpl.getIndCPSInvsDtlEaConclByStageId}")
	private String getIndCPSInvsDtlEaConclByStageIdSql;

	@Value("${StageDaoImpl.getOpenStageByIdCase}")
	private String getOpenStageByIdCaseSql;

	@Value("${CaseSummary.getStageCommon}")
	private String getStageCommon;

	@Value("${StageDaoImpl.getStageClosureEventType}")
	private String getStageClosureEventType;

	@Value("${StageDaoImpl.getStagePersByIdStage}")
	private String getStagePersByIdStageSql;

	@Value("${StageDaoImpl.getStagesByIdPerson}")
	private String getStagesByIdPersonSql;

	@Value("${StageDaoImpl.getStagesByAttributes}")
	private String getStagesByAttributesSql;

	@Value("${StageDaoImpl.getSUBOpenStagesCount}")
	private String getSUBOpenStagesCountSql;

	@Value("${StageDaoImpl.getSubCloseStagesCongregateCount}")
	private String getSubCloseStagesCongregateCountSql;

	@Value("${StageDaoImpl.getOpenStagesCount}")
	private String getOpenStagesCountSql;

	@Value("${StageDaoImpl.getOpenStagesCount1}")
	private String getOpenStagesCountSql1;

	@Value("${StageDaoImpl.getOpenStage}")
	private String getOpenStage;

	@Value("${StageDaoImpl.isServicePlanExists}")
	private String isServicePlanExists;

	@Value("${StageDaoImpl.isSec3Started}")
	private String isSec3Started;

	@Value("${StageDaoImpl.isInterventionSelected}")
	private String isInterventionSelected;

	@Value("${StageDaoImpl.isInterventionStarted}")
	private String isInterventionStarted;

	@Value("${StageDaoImpl.getIdPersonForPALWorker}")
	private String getIdPersonForPALWorkerSql;

	@Value("${StageDaoImpl.getEventStagePersonListByAttributes}")
	private String getEventStagePersonListByAttributesSql;

	@Value("${StageDaoImpl.getEventStagePersonListByAttributesAppend}")
	private String getEventStagePersonListByAttributesAppendSql;
	
	@Value("${StageDaoImpl.getEventStagePersonListByAttributesTaskAppend}")
	private String getEventStagePersonListByAttributesTaskAppendSql;

	@Value("${StageDaoImpl.getPersonFromPRNStageByIdStage}")
	private String getPersonFromPRNStageByIdStageSql;

	@Value("${StageDaoImpl.getStageByCasePersonId}")
	private String getStageByCasePersonIdSql;

	@Value("${StageDaoImpl.caseStageCheckoutStatusSql}")
	private String caseStageCheckoutStatusSql;

	@Value("${StageDaoImpl.getActiveStageByIdPerson}")
	private String getActiveStageByIdPersonSql;

	@Value("${StageDaoImpl.openStageWithTwoPerson}")
	private String openStageWithTwoPersonSql;

	@Value("${StageDaoImpl.getPendingClosureEvent}")
	private String getPendingClosureEventSql;
	@Value("${StageDaoImpl.retrieveStageInfo}")
	private String retrieveStageInfo;
	// As part of placement Eligibility Determination Bean
	@Value("${StageDaoImpl.findWorkersForStage}")
	private String findWorkersForStageSql;

	@Value("${StageDaoImpl.fetchStageInfoForPerson}")
	private String fetchStageInfoForPersonSql;

	@Value("${StageDaoImpl.getPriorStageByID}")
	private String getPriorStageByID;

	@Value("${StageDaoImpl.getStageListForPC}")
	private String getStageListForPCSql;

	@Value("${StageDaoImpl.getStageListForPCWhereClause}")
	private String getStageListForPCWhereClauseSql;

	@Value("${StageDaoImpl.getOpenStagesForWithPersons}")
	private String getOpenStagesForWithPersonsSql;
	@Value("${StageDaoImpl.getStagePersonListByRole}")
	private String getStagePersonListByRole;

	@Value("${StageDaoImpl.updateStageIndVictimStatus}")
	private String updateStageIndVictimStatus;
	
	@Value("${StageDaoImpl.getRCIAlertSql}")
	private String getRCIAlertSql;

	@Value("${StageDtoImpl.getStagePersonFilterByAdoStage}")
	private String getStagePersonFilterByAdoStage;

	@Value("${StageDtoImpl.getStagesForPCSelfByStageType}")
	private String getStagesForPCSelfByStageType;

	@Value("${StageDtoImpl.getStagePersonFilterByPersTypeAndRole}")
	private String getStagePersonFilterByPersTypeAndRole;

	@Autowired
	private SessionFactory sessionFactory;

	private static final Logger log = Logger.getLogger(StageDaoImpl.class);

	private static final String REASON_CLOSED_01 = "01";

	private static final String REASON_CLOSED_02 = "02";

	public StageDaoImpl() {
	}

	/**
	 * Method Description: This DAM performs a full-row retrieve from the STAGE
	 * table given ID STAGE. DAM Name: CINT21D, CINT40D, CaseMergeValidation
	 * 
	 * Method Description: This DAM performs a full-row retrieve from the STAGE
	 * table given ID STAGE. DAM Name: CINT21D, CINT40D
	 * 
	 * @param idStage
	 * @return StageDto
	 */
	@Override
	public StageDto getStageById(Long idStage) {
		StageDto stageDto = (StageDto) sessionFactory.getCurrentSession().createCriteria(Stage.class)
				.createAlias("capsCase", "capsCase")
				.setProjection(Projections.projectionList()
						.add(Projections.property(ServiceConstants.STAGE_STAGEID), ServiceConstants.STAGE_STAGEID)
						.add(Projections.property("capsCase.idCase"), ServiceConstants.STAGE_IDCASE)
						.add(Projections.property("situation.idSituation"), ServiceConstants.STAGE_IDSITUATION)
						.add(Projections.property(ServiceConstants.STAGE_LASTUPDATE), ServiceConstants.STAGE_LASTUPDATE)
						.add(Projections.property(ServiceConstants.STAGE_TYPE), ServiceConstants.STAGE_TYPE)
						.add(Projections.property(ServiceConstants.STAGE_IDUNIT), ServiceConstants.STAGE_IDUNIT)
						.add(Projections.property(ServiceConstants.STAGE_CLOSE), ServiceConstants.STAGE_CLOSE)
						.add(Projections.property(ServiceConstants.STAGE_CLASSIFICATION),
								ServiceConstants.STAGE_CLASSIFICATION)
						.add(Projections.property(ServiceConstants.STAGE_PRIORITY), ServiceConstants.STAGE_PRIORITY)
						.add(Projections.property(ServiceConstants.STAGE_INITIAL_PRIORITY),
								ServiceConstants.STAGE_INITIAL_PRIORITY)
						.add(Projections.property(ServiceConstants.STAGE_PRIORITYCHGD),
								ServiceConstants.STAGE_PRIORITYCHGD)
						.add(Projections.property(ServiceConstants.STAGE_REASONCLOSED),
								ServiceConstants.STAGE_REASONCLOSED)
						.add(Projections.property(ServiceConstants.STAGE_IDSTAGECLOSE),
								ServiceConstants.STAGE_IDSTAGECLOSE)
						.add(Projections.property(ServiceConstants.STAGE_CNTY), ServiceConstants.STAGE_CNTY)
						.add(Projections.property(ServiceConstants.STAGE_NMSTAGE), ServiceConstants.STAGE_NMSTAGE)
						.add(Projections.property(ServiceConstants.STAGE_REGION), ServiceConstants.STAGE_REGION)
						.add(Projections.property(ServiceConstants.STAGE_START), ServiceConstants.STAGE_START)
						.add(Projections.property(ServiceConstants.STAGE_CDPROGRAM), ServiceConstants.STAGE_CDPROGRAM)
						.add(Projections.property(ServiceConstants.STAGE_CD), ServiceConstants.STAGE_CD)
						.add(Projections.property(ServiceConstants.TXTSTAGEPRIORITYCMNTS),
								ServiceConstants.STAGE_PRIORITYCMNTS)
						.add(Projections.property(ServiceConstants.TXTSTAGECLOSURECMNTS),
								ServiceConstants.STAGE_CLOSURECMNTS)
						.add(Projections.property("cdClientAdvised"), "cdClientAdvised")
						.add(Projections.property("indEcs"), "indEcs")
						.add(Projections.property("indEcsVer"), "indEcsVer")
						.add(Projections.property("indAssignStage"), "indAssignStage")
						.add(Projections.property("dtClientAdvised"), "dtClientAdvised")
						.add(Projections.property(ServiceConstants.STAGE_CREATED), ServiceConstants.STAGE_CREATED)
						.add(Projections.property("indSecondApprover"), "indSecondApprover")
						.add(Projections.property("indFoundOpenCaseAtIntake"), "indFoundOpenCaseAtIntake")
						.add(Projections.property("indFormallyScreened"), "indFormallyScreened")
						.add(Projections.property("indScreened"), "indScreened")
						.add(Projections.property("dtMultiRef"), "dtMultiRef")
						.add(Projections.property("capsCase.nmCase"), "nmCase")
						.add(Projections.property("indScrnngElig"), "indScrnngElig")
						.add(Projections.property("indAlgdVctmUnderAge6"), "indAlgdVctmUnderAge6")
						.add(Projections.property("dtMultiRef"), "dtMultiRef")
						.add(Projections.property("txtIntAsgnmntNote"), "txtIntAsgnmntNote")
						.add(Projections.property("indRevwdReadyAsgn"), "indRevwdReadyAsgn")
						.add(Projections.property("cdStageReopenRsn"), "cdStageReopenRsn")
						.add(Projections.property("txtStageReopenRsnCmnt"), "txtPreviousStageReopenRsnCmnt")
						.add(Projections.property("cdScrnngStat"), "cdScrnngStat"))
				.add(Restrictions.eq(ServiceConstants.STAGE_STAGEID, idStage))
				.setResultTransformer(Transformers.aliasToBean(StageDto.class)).uniqueResult();
		
		if (ObjectUtils.isEmpty(stageDto)){ // For I&R stages, remove the CAPS_CASE join as it will be null
			stageDto = (StageDto) sessionFactory.getCurrentSession().createCriteria(Stage.class)
			.setProjection(Projections.projectionList()
					.add(Projections.property(ServiceConstants.STAGE_STAGEID), ServiceConstants.STAGE_STAGEID)
					.add(Projections.property("situation.idSituation"), ServiceConstants.STAGE_IDSITUATION)
					.add(Projections.property(ServiceConstants.STAGE_LASTUPDATE), ServiceConstants.STAGE_LASTUPDATE)
					.add(Projections.property(ServiceConstants.STAGE_TYPE), ServiceConstants.STAGE_TYPE)
					.add(Projections.property(ServiceConstants.STAGE_IDUNIT), ServiceConstants.STAGE_IDUNIT)
					.add(Projections.property(ServiceConstants.STAGE_CLOSE), ServiceConstants.STAGE_CLOSE)
					.add(Projections.property(ServiceConstants.STAGE_CLASSIFICATION),
							ServiceConstants.STAGE_CLASSIFICATION)
					.add(Projections.property(ServiceConstants.STAGE_PRIORITY), ServiceConstants.STAGE_PRIORITY)
					.add(Projections.property(ServiceConstants.STAGE_INITIAL_PRIORITY),
							ServiceConstants.STAGE_INITIAL_PRIORITY)
					.add(Projections.property(ServiceConstants.STAGE_PRIORITYCHGD),
							ServiceConstants.STAGE_PRIORITYCHGD)
					.add(Projections.property(ServiceConstants.STAGE_REASONCLOSED),
							ServiceConstants.STAGE_REASONCLOSED)
					.add(Projections.property(ServiceConstants.STAGE_IDSTAGECLOSE),
							ServiceConstants.STAGE_IDSTAGECLOSE)
					.add(Projections.property(ServiceConstants.STAGE_CNTY), ServiceConstants.STAGE_CNTY)
					.add(Projections.property(ServiceConstants.STAGE_NMSTAGE), ServiceConstants.STAGE_NMSTAGE)
					.add(Projections.property(ServiceConstants.STAGE_REGION), ServiceConstants.STAGE_REGION)
					.add(Projections.property(ServiceConstants.STAGE_START), ServiceConstants.STAGE_START)
					.add(Projections.property(ServiceConstants.STAGE_CDPROGRAM), ServiceConstants.STAGE_CDPROGRAM)
					.add(Projections.property(ServiceConstants.STAGE_CD), ServiceConstants.STAGE_CD)
					.add(Projections.property(ServiceConstants.TXTSTAGEPRIORITYCMNTS),
							ServiceConstants.STAGE_PRIORITYCMNTS)
					.add(Projections.property(ServiceConstants.TXTSTAGECLOSURECMNTS),
							ServiceConstants.STAGE_CLOSURECMNTS)
					.add(Projections.property("cdClientAdvised"), "cdClientAdvised")
					.add(Projections.property("indEcs"), "indEcs")
					.add(Projections.property("indEcsVer"), "indEcsVer")
					.add(Projections.property("indAssignStage"), "indAssignStage")
					.add(Projections.property("dtClientAdvised"), "dtClientAdvised")
					.add(Projections.property(ServiceConstants.STAGE_CREATED), ServiceConstants.STAGE_CREATED)
					.add(Projections.property("indSecondApprover"), "indSecondApprover")
					.add(Projections.property("indFoundOpenCaseAtIntake"), "indFoundOpenCaseAtIntake")
					.add(Projections.property("indFormallyScreened"), "indFormallyScreened")
					.add(Projections.property("indScreened"), "indScreened")
					.add(Projections.property("dtMultiRef"), "dtMultiRef")
					.add(Projections.property("indScrnngElig"), "indScrnngElig")
					.add(Projections.property("indAlgdVctmUnderAge6"), "indAlgdVctmUnderAge6")
					.add(Projections.property("dtMultiRef"), "dtMultiRef")
					.add(Projections.property("txtIntAsgnmntNote"), "txtIntAsgnmntNote")
					.add(Projections.property("indRevwdReadyAsgn"), "indRevwdReadyAsgn")
					.add(Projections.property("cdStageReopenRsn"), "cdStageReopenRsn")
					.add(Projections.property("txtStageReopenRsnCmnt"), "txtPreviousStageReopenRsnCmnt")
					.add(Projections.property("cdScrnngStat"), "cdScrnngStat"))
			.add(Restrictions.eq(ServiceConstants.STAGE_STAGEID, idStage))
			.setResultTransformer(Transformers.aliasToBean(StageDto.class)).uniqueResult();
		}

		return stageDto;
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<PrimaryWorker> getPrimaryCaseWorker(long idcase) {
		List<PrimaryWorker> primaryWorkerList = new ArrayList<PrimaryWorker>();
		primaryWorkerList = (List<PrimaryWorker>) ((SQLQuery) sessionFactory.getCurrentSession()
				.createSQLQuery(getPrimaryCaseWorkerSql).setParameter("hi_ulIdCase", idcase))
						.addScalar("uidStage", StandardBasicTypes.LONG).addScalar("uidUnit", StandardBasicTypes.LONG)
						.addScalar("uidPerson", StandardBasicTypes.LONG)
						.setResultTransformer(Transformers.aliasToBean(PrimaryWorker.class)).list();
		return primaryWorkerList;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.tx.us.dfps.impact.stage.dao.StageDao#getFacilityDetail(long)
	 */
	@Override
	public FacilRtrvRes getFacilityDetail(long idStage) {
		FacilRtrvRes facilRtrvRes = new FacilRtrvRes();
		facilRtrvRes = (FacilRtrvRes) ((SQLQuery) sessionFactory.getCurrentSession()
				.createSQLQuery(getFacilityDetailsql).setParameter("hi_uidstage", idStage))
						.addScalar("idResource", StandardBasicTypes.LONG)
						.addScalar("indIncmgOnGrnds", StandardBasicTypes.STRING)
						.addScalar("nmIncmgFacilName", StandardBasicTypes.STRING)
						.addScalar("nmIncmgFacilSuprtdant", StandardBasicTypes.STRING)
						.addScalar("cdIncFacilOperBy", StandardBasicTypes.STRING)
						.addScalar("nmIncmgFacilAffiliated", StandardBasicTypes.STRING)
						.addScalar("indIncmgFacilSearch", StandardBasicTypes.STRING)
						.addScalar("indIncmgFacilAbSupvd", StandardBasicTypes.STRING)
						.addScalar("cdIncmgFacilType", StandardBasicTypes.STRING)
						.addScalar("addrIncmgFacilStLn1", StandardBasicTypes.STRING)
						.addScalar("addrIncmgFacilStLn2", StandardBasicTypes.STRING)
						.addScalar("cdIncmgFacilState", StandardBasicTypes.STRING)
						.addScalar("cdIncmgFacilCnty", StandardBasicTypes.STRING)
						.addScalar("addrIncmgFacilCity", StandardBasicTypes.STRING)
						.addScalar("addrIncmgFacilZip", StandardBasicTypes.STRING)
						.addScalar("nbrIncmgFacilPhone", StandardBasicTypes.STRING)
						.addScalar("nbrIncmgFacilPhoneExt", StandardBasicTypes.STRING)
						.addScalar("nmUnitWard", StandardBasicTypes.STRING)
						.addScalar("txtFacilCmnts", StandardBasicTypes.STRING)
						.addScalar("idStage", StandardBasicTypes.LONG)
						.addScalar("cdRsrcType", StandardBasicTypes.STRING)
						.setResultTransformer(Transformers.aliasToBean(FacilRtrvRes.class)).uniqueResult();
		return facilRtrvRes;
	}

	/**
	 * Dam Name: CCMN19D Method Description: This Method is used to retrieve the
	 * STAGE NM and PRIMARY NM from STAGE,PERSON and STAGE PERSON LINK tables.
	 * 
	 * @param uIDStage
	 * @param stgPersonRole
	 * @return StagePersonDto
	 */
	@Override
	public StagePersonDto getStagePersonLinkDetails(Long uIDStage, String stgPersonRole) {
		StagePersonDto stagePersonDto = new StagePersonDto();
		List<StagePersonDto> stagePersonDtoList = (List<StagePersonDto>) ((SQLQuery) sessionFactory.getCurrentSession()
				.createSQLQuery(getStagePersonDetails).setParameter("szCdIDStage", uIDStage)
				.setParameter("cdStagePerRole", stgPersonRole)).addScalar("nmPersonFull", StandardBasicTypes.STRING)
						.addScalar("idTodoPersWorker", StandardBasicTypes.LONG)
						.addScalar("nmStage", StandardBasicTypes.STRING)
						.addScalar("cdStagePerRole", StandardBasicTypes.STRING)
						.setResultTransformer(Transformers.aliasToBean(StagePersonDto.class)).list();
		if (!CollectionUtils.isEmpty(stagePersonDtoList)) {
			if (stagePersonDtoList.size() == 1)
				stagePersonDto = stagePersonDtoList.get(0);
			else {
				stagePersonDto = stagePersonDtoList.stream()
						.filter(tmpDto -> CodesConstant.CROLEALL_PR.equals(tmpDto.getCdStagePerRole())).findFirst()
						.orElse(stagePersonDtoList.get(0));
			}
		}
		return stagePersonDto;
	}

	/**
	 * 
	 * Method Description: This Method is used to perform update operation based
	 * on idStage, idUnit and cdStageRegion. Dam Name : CCMNB7D
	 * 
	 * @param idStage
	 * @param idUnit
	 * @param cdStageRegion
	 * @return String
	 */
	@Override
	public String getStageupdate(Long idStage, Long idUnit, String cdStageRegion) {
		Stage stageEntity = new Stage();
		String returnMsg = "";
		String stateReg = "";
		Criteria cr = sessionFactory.getCurrentSession().createCriteria(Stage.class)
				.add(Restrictions.eq("idStage", idStage));
		stageEntity = (Stage) cr.uniqueResult();
		if (!TypeConvUtil.isNullOrEmpty(TypeConvUtil.toLong(idStage)))
			stageEntity.setIdStage(idStage);
		if (!TypeConvUtil.isNullOrEmpty(TypeConvUtil.toLong(idUnit)))
			stageEntity.setIdUnit(idUnit);
		if (!TypeConvUtil.isNullOrEmpty(TypeConvUtil.toLong(cdStageRegion))) {
			int region = TypeConvUtil.toInt(cdStageRegion);
			if (region == 515)
				stateReg = "00";
			else if (region >= 99)
				stateReg = "99";
			else
				stateReg = cdStageRegion.substring(1, 3);
		}
		stageEntity.setCdStageRegion(stateReg);
		sessionFactory.getCurrentSession().saveOrUpdate(sessionFactory.getCurrentSession().merge(stageEntity));
		returnMsg = ServiceConstants.SUCCESS;
		return returnMsg;
	}

	/**
	 * 
	 * Method Description:The dam retrieves all ID_CASE's from the STAGE table
	 * where ID_CASE matches the ID_CASE passed to this dam from the service.
	 * Dam Name: CCMN86D
	 * 
	 * @param caseId
	 * @return List<StageDto>
	 */
	@SuppressWarnings("unchecked")
	public List<StageIdDto> searchStageByCaseId(Long idCase) {
		List<StageIdDto> stageOutput = new ArrayList<StageIdDto>();
		Query queryStage = sessionFactory.getCurrentSession().createSQLQuery(searchStageByCaseIdSql)
				.addScalar("idStage", StandardBasicTypes.LONG).addScalar("cdStage", StandardBasicTypes.STRING)
				.setResultTransformer(Transformers.aliasToBean(StageIdDto.class));
		queryStage.setParameter("idCase", idCase);
		stageOutput = queryStage.list();
		return stageOutput;
	}

	/**
	 * 
	 * Method Description: This method will used to get Case/Stage info needed
	 * for accessed windows from Approval Status retrieve Dam Name: CCMN59D
	 * 
	 * @param idEvent
	 * @return EventStageSearchDto
	 */
	public EventStageSearchDto stageEventSearchById(Long idEvent) {
		EventStageSearchDto eventStageSearchDto = null;
		Query queryStage = sessionFactory.getCurrentSession().createSQLQuery(searchStageEventByIdSql)
				.addScalar("idStage", StandardBasicTypes.LONG).addScalar("idCase", StandardBasicTypes.LONG)
				.addScalar("cdStage", StandardBasicTypes.STRING).addScalar("cdStageProgram", StandardBasicTypes.STRING)
				.addScalar("cdStageReasonClosed", StandardBasicTypes.STRING)
				.addScalar("nmStage", StandardBasicTypes.STRING).addScalar("dtStageClose", StandardBasicTypes.TIMESTAMP)
				.addScalar("indStageClose", StandardBasicTypes.STRING)
				.setResultTransformer(Transformers.aliasToBean(EventStageSearchDto.class));
		queryStage.setParameter("idEvent", idEvent);
		eventStageSearchDto = (EventStageSearchDto) queryStage.uniqueResult();
		return eventStageSearchDto;
	}

	/**
	 * DAM Name: CINT21D, CSES71D Method Description: This method retrieves to
	 * retrieve szCdStage, since I&R and SPC intakes do not have a stage name
	 * from Stage table.
	 * 
	 * @param idStage
	 * @return Stage
	 */
	public Stage getStageEntityById(Long idStage) {
		return (Stage) sessionFactory.getCurrentSession().load(Stage.class, idStage);
	}

	/**
	 * SIR 1012485 - This method will fetch all stages based on specific stage
	 * and indActive (open or closed)
	 * 
	 * @param ulIdCase
	 * @param indActive
	 * @param cdStage
	 * @throws InvalidRequestException
	 * 			@throws
	 */
	@SuppressWarnings("unchecked")
	@Override
	public List<StageDto> getStagesByType(Long ulIdCase, String indActive, String cdStage)
			throws InvalidRequestException {
		List<StageDto> stageDtoList = new ArrayList<>();
		StringBuilder queryBuff = new StringBuilder();
		queryBuff.append(getStagesByTypeSql);
		switch (indActive) {
		case ServiceConstants.CLOSED_STAGES:
			queryBuff.append(getStagesByTypeAndIndStageCloseSql);
			break;
		case ServiceConstants.OPEN_STAGES:
			queryBuff.append(getStagesByTypeAndIndStageCloseSql);
			break;
		case ServiceConstants.ALL_STAGES:
			break;
		default:
			throw new InvalidRequestException(messageSource.getMessage("illegal.stage.type", null, Locale.US));
		}
		queryBuff.append(getStagesByTypeOrderBySql);
		Query queryStage = sessionFactory.getCurrentSession().createSQLQuery(queryBuff.toString())
				.addScalar("idStage", StandardBasicTypes.LONG).addScalar("idCase", StandardBasicTypes.LONG)
				.addScalar("idSituation", StandardBasicTypes.LONG).addScalar("cdStage", StandardBasicTypes.STRING)
				.addScalar("cdStageType", StandardBasicTypes.STRING)
				.addScalar("cdStageProgram", StandardBasicTypes.STRING)
				.addScalar("cdStageClassification", StandardBasicTypes.STRING)
				.addScalar("idUnit", StandardBasicTypes.LONG)
				.addScalar("cdStageReasonClosed", StandardBasicTypes.STRING)
				.addScalar("nmStage", StandardBasicTypes.STRING).addScalar("dtStageStart", StandardBasicTypes.TIMESTAMP)
				.addScalar("dtStageClose", StandardBasicTypes.TIMESTAMP)
				.addScalar("indStageClose", StandardBasicTypes.STRING).addScalar("nmCase", StandardBasicTypes.STRING)
				.setResultTransformer(Transformers.aliasToBean(StageDto.class));
		queryStage.setParameter("idCase", ulIdCase);
		queryStage.setParameter("cdStage", cdStage);
		if (!TypeConvUtil.isNullOrEmpty(indActive)) {
			if (indActive.equals(ServiceConstants.OPEN_STAGES)) {
				queryStage.setParameter("indStage", ServiceConstants.STRING_IND_N);
			} else if (indActive.equals(ServiceConstants.CLOSED_STAGES)) {
				queryStage.setParameter("indStage", ServiceConstants.STRING_IND_Y);
			}
		}
		stageDtoList = queryStage.list();
		return stageDtoList;
	}

	/**
	 * This function returns Primary Child for the Stage.
	 * 
	 * @param idStage
	 * 
	 * @return idPerson - Primary Child's PersonId
	 */
	@Override
	public Long findPrimaryChildForStage(Long idStage) {
		Long personId = ServiceConstants.NULL_VAL;
		Query queryStage = sessionFactory.getCurrentSession().createSQLQuery(findPrimaryChildForStageSql);
		queryStage.setParameter("idStage", idStage);
		queryStage.setParameter("cdRole", ServiceConstants.CROLES_PC);
		BigDecimal personIdDecimal = (BigDecimal) queryStage.uniqueResult();
		if (!TypeConvUtil.isNullOrEmpty(personIdDecimal)) {
			personId = Long.valueOf(personIdDecimal.longValue());
		}
		return personId;
	}

	/**
	 * 
	 * Method Description: This method returns the Task Code based on the
	 * stageType and Stage program type
	 * 
	 * @param stageType
	 *            This is Stage Type
	 * @param StageProgType
	 *            This is Program Type
	 * @return String value for a TaskCode.
	 *
	 *         (non-Javadoc)
	 * @see us.tx.state.dfps.service.common.dao.StageDao#getTaskCode(java.lang.String,
	 *      java.lang.String)
	 */
	public String getTaskCode(String stageType, String stageProgType) {
		return (String) sessionFactory.getCurrentSession().createSQLQuery(getStageClosureEventType)
				.addScalar("cdTask", StandardBasicTypes.STRING).setParameter("stageType", stageType)
				.setParameter("stageProgType", stageProgType).uniqueResult();
	}

	/**
	 * Looks at the case to determine if the given user has access to a given
	 * stage.<br>
	 * Use this version of the method if you want to test access for the current
	 * user.<br>
	 * The following items are checked: <br>
	 * <li>primary worker assigned to stage</li>
	 * <li>one of the four secondary workers assigned to the stage</li>
	 * <li>the supervisor of any of the above</li>
	 * <li>the designee of any of the above supervisors</li> <code>
	 * Usage Example:<br>
	 * int ulIdStage = GlobalData.getUlIdStage( request );<br>
	 * boolean bStageAccess = CaseUtility.hasStageAccess( ulIdStage, UserProfileHelper.getUserProfile( request ) );<br>
	 * </code>
	 *
	 * @param ulIdStage
	 *            stage id to check
	 * @param ulIdPerson
	 *            user to check
	 * @return whether or not the user has access
	 */
	@SuppressWarnings("unchecked")
	@Override
	public boolean hasStageAccess(Long ulIdStage, Long ulIdPerson) {
		boolean bStageAccess = false;
		List<Long> personIds = new ArrayList<>();
		Date date = new Date();
		Query queryStage = sessionFactory.getCurrentSession().createSQLQuery(hasStageAccessSql);
		queryStage.setParameter("idStage", ulIdStage);
		queryStage.setParameter("idPerson", ulIdPerson);
		queryStage.setParameterList("stageRoles", ServiceConstants.STAGE_ROLES);
		queryStage.setParameterList("unitRoles", ServiceConstants.UNIT_ROLES);
		queryStage.setParameter("date", date);
		personIds = queryStage.list();
		if (personIds.size() >= 1) {
			bStageAccess = true;
		}
		return bStageAccess;
	}

	/**
	 * Method Description: This method will count number of SUBCARE stages
	 * currently open for a child. Service Name: CSUB14S DAM: CSES21D
	 * 
	 * @param personId
	 * @param stagePersRole
	 * @return Integer
	 */
	public Integer getStageCount(Long personId, String stagePersRole, String cdStage) {
		Integer count = 0;
		Query query = (Query) sessionFactory.getCurrentSession().createSQLQuery(countStageByAttributes)
				.setParameter("id_Person", personId).setParameter("cdStgPersRole", stagePersRole)
				.setParameter("cdStg", cdStage);
		count = ((BigDecimal) query.uniqueResult()).intValueExact();
		return count;
	}

	/**
	 * Method Description: This method will retrieve the earliest intake dates.
	 * Service Name: CSUB14S DAM: CLSC84D
	 * 
	 * @param idStage
	 * @return StageIncomingDto
	 */
	public StageIncomingDto getEarliestIntakeDates(Long idStage) {
		StageIncomingDto stgIncmgDtls = new StageIncomingDto();
		stgIncmgDtls = (StageIncomingDto) sessionFactory.getCurrentSession().createSQLQuery(intakeDates)
				.addScalar("idPriorStage", StandardBasicTypes.LONG)
				.addScalar("dtIncomingCall", StandardBasicTypes.TIMESTAMP).addScalar("indIncmgSuspMeth")
				.setParameter("idStg", idStage).setResultTransformer(Transformers.aliasToBean(StageIncomingDto.class))
				.uniqueResult();
		return stgIncmgDtls;
	}

	/**
	 * SIR# 141160 - EA Enhancements getIndCPSInvsDtlEaConclBy
	 * 
	 * Service Name: CCMN03U DAM: CSECA2D
	 * 
	 * @param idStage
	 * @return
	 */
	@Override
	public String getIndCPSInvsDtlEaConclByStageId(Long idStage) {
		String indCPSInvsDtlEaConcl = ServiceConstants.EMPTY_STRING;
		Query queryIndCPSInvDtl = sessionFactory.getCurrentSession().createSQLQuery(getIndCPSInvsDtlEaConclByStageIdSql)
				.setParameter("idStage", idStage).setParameter("indCPS", ServiceConstants.STRING_IND_Y);
		indCPSInvsDtlEaConcl = (String) queryIndCPSInvDtl.uniqueResult();
		return indCPSInvsDtlEaConcl;
	}

	/**
	 * This DAM retrieves all the ID STAGE's (and their corresponding CD STAGE)
	 * associated with an ID STAGE given as input. These stages are all the open
	 * stages associated with ID CASE.
	 * 
	 * Service Name: CCMN03U, DAM Name: CCMNF6D
	 * 
	 * @param idCase
	 * @return
	 */
	@SuppressWarnings("unchecked")
	@Override
	public List<StageDto> getOpenStageByIdCase(Long idCase) {
		List<StageDto> stageDtoList = new ArrayList<>();
		Query queryStage = sessionFactory.getCurrentSession().createSQLQuery(getOpenStageByIdCaseSql)
				.addScalar("idStage", StandardBasicTypes.LONG).addScalar("cdStage", StandardBasicTypes.STRING)
				.setResultTransformer(Transformers.aliasToBean(StageDto.class));
		queryStage.setParameter("idCase", idCase);
		queryStage.setParameter("date", ServiceConstants.GENERIC_END_DATE);
		stageDtoList = queryStage.list();
		return stageDtoList;
	}

	/**
	 * 
	 * Method Description: Method for Priority Closure Save/Close. This Updates
	 * STAGE table.
	 * 
	 * 
	 * @param PriorityClosureSaveReq
	 *            Stage
	 * @return Tuxedo Service Name: CINT21S
	 */
	public void updateStagePriority(PriorityClosureSaveReq priorityClosureSaveReq) {
		Stage stageUpdate = (Stage) sessionFactory.getCurrentSession().get(Stage.class,
				priorityClosureSaveReq.getPriorityClosureDto().getIdStage());
		// If stage is closed return back with an error
		if (stageUpdate.getIndStageClose().equalsIgnoreCase(ServiceConstants.Y)) {
			throw new DataLayerException(ServiceConstants.MSG_SYS_STAGE_CLOSED);
		}
		if (!TypeConvUtil.isNullOrEmpty(priorityClosureSaveReq.getPriorityClosureDto().getIndStageClose()))
			stageUpdate.setIndStageClose(priorityClosureSaveReq.getPriorityClosureDto().getIndStageClose());
		// if
		// (!TypeConvUtil.isNullOrEmpty(priorityClosureSaveReq.getPriorityClosureDto().getIndIntakeFormallyScreened()))
		stageUpdate
				.setIndFormallyScreened(priorityClosureSaveReq.getPriorityClosureDto().getIndIntakeFormallyScreened());
		// if
		// (!TypeConvUtil.isNullOrEmpty(priorityClosureSaveReq.getPriorityClosureDto().getIndOpenCaseFoundAtIntake()))
		stageUpdate.setIndFoundOpenCaseAtIntake(
				priorityClosureSaveReq.getPriorityClosureDto().getIndOpenCaseFoundAtIntake());
		if (!TypeConvUtil.isNullOrEmpty(priorityClosureSaveReq.getPriorityClosureDto().getCdStageType()))
			stageUpdate.setCdStageType(priorityClosureSaveReq.getPriorityClosureDto().getCdStageType());
		if (!TypeConvUtil.isNullOrEmpty(priorityClosureSaveReq.getPriorityClosureDto().getCdStageCurrPriority()))
			stageUpdate.setCdStageCurrPriority(priorityClosureSaveReq.getPriorityClosureDto().getCdStageCurrPriority());
		if (!TypeConvUtil.isNullOrEmpty(priorityClosureSaveReq.getPriorityClosureDto().getCdStageInitialPriority()))
			stageUpdate.setCdStageInitialPriority(
					priorityClosureSaveReq.getPriorityClosureDto().getCdStageInitialPriority());
        /*
         * if (!TypeConvUtil.isNullOrEmpty(priorityClosureSaveReq.getPriorityClosureDto().
         * getCdStageRsnPriorityChgd()))
         */
			stageUpdate.setCdStageRsnPriorityChgd(
					priorityClosureSaveReq.getPriorityClosureDto().getCdStageRsnPriorityChgd());
		if (!TypeConvUtil.isNullOrEmpty(priorityClosureSaveReq.getPriorityClosureDto().getCdStageReasonClosed())
				&& (!TypeConvUtil.isNullOrEmpty(priorityClosureSaveReq.getPriorityClosureDto().getCdStageCurrPriority())
						&& priorityClosureSaveReq.getPriorityClosureDto().getCdStageCurrPriority()
								.equalsIgnoreCase(ServiceConstants.N))) {
			stageUpdate.setCdStageReasonClosed(priorityClosureSaveReq.getPriorityClosureDto().getCdStageReasonClosed());
		} else {
			if (!REASON_CLOSED_02
					.equalsIgnoreCase(priorityClosureSaveReq.getPriorityClosureDto().getCdStageReasonClosed())) {
				stageUpdate.setCdStageReasonClosed(REASON_CLOSED_01);
			}
			if (priorityClosureSaveReq.getPriorityClosureDto().getIsCrsrStage()) {
				stageUpdate.setCdStageReasonClosed(
						priorityClosureSaveReq.getPriorityClosureDto().getCdStageReasonClosed());
			}
		}
        /*
         * if (!TypeConvUtil.isNullOrEmpty(priorityClosureSaveReq.getPriorityClosureDto().
         * getStagePriorityCmnts()))
         */
			stageUpdate
					.setTxtStagePriorityCmnts(priorityClosureSaveReq.getPriorityClosureDto().getStagePriorityCmnts());
		if (!TypeConvUtil.isNullOrEmpty(priorityClosureSaveReq.getPriorityClosureDto().getStageClosureCmnts())
				&& (!TypeConvUtil.isNullOrEmpty(priorityClosureSaveReq.getPriorityClosureDto().getCdStageCurrPriority())
						&& priorityClosureSaveReq.getPriorityClosureDto().getCdStageCurrPriority()
								.equalsIgnoreCase(ServiceConstants.N))) {
			stageUpdate.setTxtStageClosureCmnts(priorityClosureSaveReq.getPriorityClosureDto().getStageClosureCmnts());
		} else {
			stageUpdate.setTxtStageClosureCmnts(ServiceConstants.EMPTY_STR);
			if (priorityClosureSaveReq.getPriorityClosureDto().getIsCrsrStage()) {
				stageUpdate
						.setTxtStageClosureCmnts(priorityClosureSaveReq.getPriorityClosureDto().getStageClosureCmnts());
			}
		}
		if (!TypeConvUtil.isNullOrEmpty(priorityClosureSaveReq.getPriorityClosureDto().getIndScrnngElig()))
			stageUpdate.setIndScrnngElig(priorityClosureSaveReq.getPriorityClosureDto().getIndScrnngElig());
		// if
		// (!TypeConvUtil.isNullOrEmpty(priorityClosureSaveReq.getPriorityClosureDto().getIndAlgdVctmUnderAge6()))
		stageUpdate.setIndAlgdVctmUnderAge6(priorityClosureSaveReq.getPriorityClosureDto().getIndAlgdVctmUnderAge6());
		if (!TypeConvUtil.isNullOrEmpty(priorityClosureSaveReq.getPriorityClosureDto().getCdScrnngStat())) {
			stageUpdate.setCdScrnngStat(priorityClosureSaveReq.getPriorityClosureDto().getCdScrnngStat());
		}
		if (priorityClosureSaveReq.getReqFuncCd().equalsIgnoreCase(ServiceConstants.CLOSE)) {
			stageUpdate.setIndStageClose(ServiceConstants.Y);
			// commenting this as it will be updated in caseStageClose service
			// stageUpdate.setDtStageClose(new Date());
		}
		sessionFactory.getCurrentSession().saveOrUpdate(stageUpdate);
	}

	/**
	 * 
	 * Method Description: Returns a list of open stages/closed and all stages
	 * for a given case; they are sorted by stage code, then stage name. Case
	 * Utility Service
	 * 
	 * @param idCase,caseStatus
	 * @return List<SelectStageDto>
	 */
	@SuppressWarnings("unchecked")
	@Override
	public List<SelectStageDto> getOpenStages(Long idCase, String caseStatus) {
		StringBuilder queryGetStage = new StringBuilder();
		queryGetStage.append(getStageCommon);
		queryGetStage.append(" STAGE.ID_CASE = :idCase");
		if (null != caseStatus)
			queryGetStage = (caseStatus.equalsIgnoreCase(ServiceConstants.ARCHITECTURE_CONS_Y))
					? queryGetStage.append(" AND STAGE.IND_STAGE_CLOSE = 'Y'")
					: queryGetStage.append(" AND STAGE.IND_STAGE_CLOSE = 'N'");
		queryGetStage.append(" ORDER BY CD_STAGE, NM_STAGE");
		return (List<SelectStageDto>) ((SQLQuery) sessionFactory.getCurrentSession()
				.createSQLQuery(queryGetStage.toString()).setLong("idCase", idCase))
						.addScalar("idStage", StandardBasicTypes.LONG).addScalar("idCase", StandardBasicTypes.LONG)
						.addScalar("idSituation", StandardBasicTypes.LONG)
						.addScalar("nmStage", StandardBasicTypes.STRING).addScalar("cdStage", StandardBasicTypes.STRING)
						.addScalar("cdStageType", StandardBasicTypes.STRING)
						.addScalar("cdStageProgram", StandardBasicTypes.STRING)
						.addScalar("cdStageClassification", StandardBasicTypes.STRING)
						.addScalar("dtStartDate", StandardBasicTypes.TIMESTAMP)
						.addScalar("indStageClose", StandardBasicTypes.STRING)
						.addScalar("cdStageReasonClosed", StandardBasicTypes.STRING)
						.addScalar("idUnit", StandardBasicTypes.LONG).addScalar("nmCase", StandardBasicTypes.STRING)
						.addScalar("dtStageClose", StandardBasicTypes.TIMESTAMP)
						.setResultTransformer(Transformers.aliasToBean(SelectStageDto.class)).list();
	}

	/**
	 * This DAM will return a full row from STAGE and STAGE PERS Link given an
	 * ID STAGE
	 * 
	 * 
	 * Service Name - CCMN03U, DAM Name - CLSC75D
	 * 
	 * @param idStage
	 * @return
	 */
	@SuppressWarnings("unchecked")
	@Override
	public List<StagePersDto> getStagePersByIdStage(Long idStage, Long idStageRelated) {
		List<StagePersDto> stagePersDtoList = new ArrayList<>();
		Query queryStage = sessionFactory.getCurrentSession().createSQLQuery(getStagePersByIdStageSql)
				.addScalar("idStage", StandardBasicTypes.LONG).addScalar("dtLastUpdate", StandardBasicTypes.TIMESTAMP)
				.addScalar("cdStageType", StandardBasicTypes.STRING).addScalar("idUnit", StandardBasicTypes.LONG)
				.addScalar("idStage", StandardBasicTypes.LONG).addScalar("idCase", StandardBasicTypes.LONG)
				.addScalar("idSituation", StandardBasicTypes.LONG)
				.addScalar("dtStageClose", StandardBasicTypes.TIMESTAMP)
				.addScalar("cdStageClassification", StandardBasicTypes.STRING)
				.addScalar("cdStageCurrPriority", StandardBasicTypes.STRING)
				.addScalar("cdStageInitialPriority", StandardBasicTypes.STRING)
				.addScalar("cdStageReasonClosed", StandardBasicTypes.STRING)
				.addScalar("cdStageRsnPriorityChgd", StandardBasicTypes.STRING)
				.addScalar("indStageClose", StandardBasicTypes.STRING)
				.addScalar("cdStageCnty", StandardBasicTypes.STRING).addScalar("nmStage", StandardBasicTypes.STRING)
				.addScalar("cdStageRegion", StandardBasicTypes.STRING)
				.addScalar("dtStageStart", StandardBasicTypes.TIMESTAMP)
				.addScalar("cdStageProgram", StandardBasicTypes.STRING).addScalar("cdStage", StandardBasicTypes.STRING)
				.addScalar("stagePriorityCmnts", StandardBasicTypes.STRING)
				.addScalar("stageClosureCmnts", StandardBasicTypes.STRING)
				.addScalar("idStagePersonLink", StandardBasicTypes.LONG)
				.addScalar("dtLastUpdatePers", StandardBasicTypes.TIMESTAMP)
				.addScalar("idPerson", StandardBasicTypes.LONG).addScalar("cdStagePersRole", StandardBasicTypes.STRING)
				.addScalar("indStagePersInLaw", StandardBasicTypes.STRING)
				.addScalar("cdStagePersType", StandardBasicTypes.STRING)
				.addScalar("cdStagePersSearchInd", StandardBasicTypes.STRING)
				.addScalar("stagePersNotes", StandardBasicTypes.STRING)
				.addScalar("dtStagePersLink", StandardBasicTypes.TIMESTAMP)
				.addScalar("cdStagePersRelInt", StandardBasicTypes.STRING)
				.addScalar("indStagePersReporter", StandardBasicTypes.STRING)
				.addScalar("indStagePersEmpNew", StandardBasicTypes.STRING)
				.setResultTransformer(Transformers.aliasToBean(StagePersDto.class));
		queryStage.setParameter("idStage", idStage);
		queryStage.setParameter("idStageRelated", idStageRelated);
		stagePersDtoList = queryStage.list();
		return stagePersDtoList;
	}

	/**
	 * This dam will use Id Person to retrieve all stages for a person from the
	 * Stage Person Link table.
	 * 
	 * Service Name - CCMN03U, DAM Name - CLSC45D
	 * 
	 * @param idPerson
	 * @return
	 */
	@SuppressWarnings("unchecked")
	@Override
	public List<StagePersDto> getStagesByIdPerson(Long idPerson) {
		List<StagePersDto> stagePersDtoList = new ArrayList<>();
		Query queryStage = sessionFactory.getCurrentSession().createSQLQuery(getStagesByIdPersonSql)
				.addScalar("idStage", StandardBasicTypes.LONG).addScalar("cdStageType", StandardBasicTypes.STRING)
				.addScalar("cdStagePersRelInt", StandardBasicTypes.STRING)
				.addScalar("dtStageStart", StandardBasicTypes.TIMESTAMP)
				.addScalar("dtStageClose", StandardBasicTypes.TIMESTAMP)
				.addScalar("cdStagePersType", StandardBasicTypes.STRING).addScalar("cdStage", StandardBasicTypes.STRING)
				.addScalar("nmStage", StandardBasicTypes.STRING).addScalar("idCase", StandardBasicTypes.LONG)
				.addScalar("nmCase", StandardBasicTypes.STRING).addScalar("dtCaseOpened", StandardBasicTypes.TIMESTAMP)
				.addScalar("dtCaseClosed", StandardBasicTypes.TIMESTAMP)
				.setResultTransformer(Transformers.aliasToBean(StagePersDto.class));
		queryStage.setParameter("idPerson", idPerson);
		stagePersDtoList = queryStage.list();
		return stagePersDtoList;
	}

	/**
	 * This dam will retrieve rows from Stage_PersonLink & Stage tables.
	 * 
	 * Service Name - CCMN03U, DAM Name -CSEC29D
	 * 
	 * @param idPerson @param stageRole @param idCase @param
	 * cdStage @return @throws
	 */
	@SuppressWarnings("unchecked")
	@Override
	public List<StagePersDto> getStagesByAttributes(Long idPerson, String stageRole, Long idCase, String cdStage) {
		List<StagePersDto> stagePersDtoList = new ArrayList<>();
		Query queryStage = sessionFactory.getCurrentSession().createSQLQuery(getStagesByAttributesSql)
				.addScalar("idStage", StandardBasicTypes.LONG).addScalar("dtLastUpdate", StandardBasicTypes.TIMESTAMP)
				.addScalar("cdStageType", StandardBasicTypes.STRING).addScalar("idUnit", StandardBasicTypes.LONG)
				.addScalar("idStage", StandardBasicTypes.LONG).addScalar("idCase", StandardBasicTypes.LONG)
				.addScalar("dtStageClose", StandardBasicTypes.TIMESTAMP)
				.addScalar("cdStageClassification", StandardBasicTypes.STRING)
				.addScalar("cdStageCurrPriority", StandardBasicTypes.STRING)
				.addScalar("cdStageInitialPriority", StandardBasicTypes.STRING)
				.addScalar("cdStageReasonClosed", StandardBasicTypes.STRING)
				.addScalar("cdStageRsnPriorityChgd", StandardBasicTypes.STRING)
				.addScalar("indStageClose", StandardBasicTypes.STRING)
				.addScalar("cdStageCnty", StandardBasicTypes.STRING).addScalar("nmStage", StandardBasicTypes.STRING)
				.addScalar("cdStageRegion", StandardBasicTypes.STRING)
				.addScalar("dtStageStart", StandardBasicTypes.TIMESTAMP)
				.addScalar("idSituation", StandardBasicTypes.LONG)
				.addScalar("cdStageProgram", StandardBasicTypes.STRING).addScalar("cdStage", StandardBasicTypes.STRING)
				.addScalar("stagePriorityCmnts", StandardBasicTypes.STRING)
				.addScalar("stageClosureCmnts", StandardBasicTypes.STRING)
				.addScalar("idStagePersonLink", StandardBasicTypes.LONG)
				.addScalar("dtLastUpdatePers", StandardBasicTypes.TIMESTAMP)
				.addScalar("idPerson", StandardBasicTypes.LONG).addScalar("cdStagePersRole", StandardBasicTypes.STRING)
				.addScalar("indStagePersInLaw", StandardBasicTypes.STRING)
				.addScalar("cdStagePersType", StandardBasicTypes.STRING)
				.addScalar("cdStagePersSearchInd", StandardBasicTypes.STRING)
				.addScalar("stagePersNotes", StandardBasicTypes.STRING)
				.addScalar("dtStagePersLink", StandardBasicTypes.TIMESTAMP)
				.addScalar("cdStagePersRelInt", StandardBasicTypes.STRING)
				.addScalar("indStagePersReporter", StandardBasicTypes.STRING)
				.addScalar("indStagePersEmpNew", StandardBasicTypes.STRING)
				.setResultTransformer(Transformers.aliasToBean(StagePersDto.class));
		queryStage.setParameter("idPerson", idPerson);
		queryStage.setParameter("stageRole", stageRole);
		queryStage.setParameter("idCase", idCase);
		queryStage.setParameter("cdStage", cdStage);
		stagePersDtoList = queryStage.list();
		return stagePersDtoList;
	}

	/**
	 * Count number of SUBCARE stages currently open for a child
	 * 
	 * Service Name - CCMN03U, DAM Name - CSES21D
	 * 
	 * @param idPerson
	 * @param stageRole
	 * @return
	 */
	@Override
	public Long getSUBOpenStagesCount(Long idPerson, String stageRole) {
		Long stageCount = ServiceConstants.ZERO_VAL;
		Query queryStage = sessionFactory.getCurrentSession().createSQLQuery(getSUBOpenStagesCountSql);
		queryStage.setParameter("idPerson", idPerson);
		queryStage.setParameter("stageRole", stageRole);
		BigDecimal stageCountDecimal = (BigDecimal) queryStage.uniqueResult();
		if (!TypeConvUtil.isNullOrEmpty(stageCountDecimal)) {
			stageCount = Long.valueOf(stageCountDecimal.longValue());
		}
		return stageCount;
	}

	/**
	 * Based on the ReqFuncCode passed in either OPEN or CLOSE count the number
	 * of stages (open or closed) for the id_person with a specified role for a
	 * specified cd_stage. (ex. check if the id_person with a cd_stage_pers_role
	 * = PC for cd_stage 'PAL' exists in any other PAL stage or any other OPEN
	 * PAL stage.)
	 * 
	 * Service Name - CCMN03U, DAM Name - CSES94D
	 * 
	 * @param idPerson
	 * @param stageRole
	 * @param cdStage
	 * @param ServiceReqHeaderDto
	 * @return
	 */
	@Override
	public Long getOpenStagesCount(Long idPerson, String stageRole, String cdStage,
			ServiceReqHeaderDto ServiceReqHeaderDto) {
		Long stageCount = ServiceConstants.ZERO_VAL;
		String queryStageString = ServiceConstants.EMPTY_STRING;
		if (!TypeConvUtil.isNullOrEmpty(ServiceReqHeaderDto)) {
			if (!TypeConvUtil.isNullOrEmpty(ServiceReqHeaderDto.getReqFuncCd())) {
				if (ServiceReqHeaderDto.getReqFuncCd().equals(ServiceConstants.OPEN_STAGES)) {
					queryStageString = getOpenStagesCountSql;
				} else {
					queryStageString = getOpenStagesCountSql1;
				}
			}
		}
		if (!TypeConvUtil.isNullOrEmpty(queryStageString)) {
			Query queryStage = sessionFactory.getCurrentSession().createSQLQuery(queryStageString);
			queryStage.setParameter("idPerson", idPerson);
			queryStage.setParameter("stageRole", stageRole);
			queryStage.setParameter("cdStage", cdStage);
			BigDecimal stageCountDecimal = (BigDecimal) queryStage.uniqueResult();
			if (!TypeConvUtil.isNullOrEmpty(stageCountDecimal)) {
				stageCount = Long.valueOf(stageCountDecimal.longValue());
			}
		}
		return stageCount;
	}

	@Override
	public Long getSUBStagesCloseCongregateCareCount(Long idCase) {
		Long congregateCount = ServiceConstants.ZERO_VAL;
		Query queryStage = sessionFactory.getCurrentSession().createSQLQuery(getSubCloseStagesCongregateCountSql);
		queryStage.setParameter("idCase", idCase);
		BigDecimal stageCountDecimal = (BigDecimal) queryStage.uniqueResult();
		if (!TypeConvUtil.isNullOrEmpty(stageCountDecimal)) {
			congregateCount = Long.valueOf(stageCountDecimal.longValue());
		}
		return congregateCount;
	}

	/**
	 * This dam was written to add, update, and delete from the STAGE table.
	 * 
	 * Service Name - CCMN03U, DAM Name - CINT12T
	 * 
	 * @param stage
	 * 
	 */
	@Override
	public void saveStage(Stage stage) {
		sessionFactory.getCurrentSession().save(stage);
	}

	/**
	 * This dam was written to add, update, and delete from the STAGE table.
	 * 
	 * Service Name - CCMN03U, DAM Name - CINT12T
	 * 
	 * @param stage
	 */
	@Override
	public void updateStage(Stage stage) {
		sessionFactory.getCurrentSession().saveOrUpdate(sessionFactory.getCurrentSession().merge(stage));
	}

	/**
	 * This dam was written to add, update, and delete from the STAGE table.
	 * 
	 * Service Name - CCMN03U, DAM Name - CINT12T
	 * 
	 * @param stage
	 */
	@Override
	public void deleteStage(Stage stage) {
		sessionFactory.getCurrentSession().delete(stage);
	}

	@Override
	public Long updtStage(Stage stage, String operation) {
		if (operation.equals(ServiceConstants.REQ_FUNC_CD_ADD))
			sessionFactory.getCurrentSession().persist(stage);
		else if (operation.equals(ServiceConstants.REQ_FUNC_CD_UPDATE))
			sessionFactory.getCurrentSession().saveOrUpdate(sessionFactory.getCurrentSession().merge(stage));
		else if (operation.equals(ServiceConstants.REQ_FUNC_CD_DELETE))
			sessionFactory.getCurrentSession().delete(stage);
		Long idstage = stage.getIdStage();
		return idstage;
	}

	/**
	 * Method Description:This method gets all open stages in a case. Service
	 * Name:CaseMergeValidation
	 * 
	 * @param idCase
	 * @return List<StageDto>
	 */
	@SuppressWarnings("unchecked")
	@Override
	@Transactional(readOnly = true, isolation = Isolation.READ_COMMITTED, propagation = Propagation.REQUIRED, rollbackFor = {
			Exception.class })
	public List<StageDto> getOpenStages(Long idCase) {
		List<StageDto> stgDtlList = null;
		stgDtlList = (List<StageDto>) sessionFactory.getCurrentSession().createSQLQuery(getOpenStage)
				.addScalar("idStage", StandardBasicTypes.LONG).addScalar("cdStage").addScalar("cdStageType")
				.addScalar("cdStageProgram").setParameter("idCase", idCase)
				.setParameter("indStgCls", ServiceConstants.IND_STAGE_CLOSE_N)
				.setResultTransformer(Transformers.aliasToBean(StageDto.class)).list();
		return stgDtlList;
	}
	
	/**
	 * 
	 * Method Description: Method to update IND_VICTIM_NOTIFICATION_STATUS in the stage table 
	 * 
	 * @param idTodo
	 *            
	 * @return
	 *
	 * 		
	 */
	@SuppressWarnings("unchecked")
	@Override
	@Transactional(readOnly = true, isolation = Isolation.READ_COMMITTED, propagation = Propagation.REQUIRED, rollbackFor = {
			Exception.class })
	public void updateStageIndVictimStatus(Stage stage){

		log.info("stageUpdate indVictimNotifStatus:" + stage.getIndVictimNotifStatus());
		if(stage != null){
			sessionFactory.getCurrentSession().saveOrUpdate(stage);
		}
	}

	/**
	 * Method Description:This method checks if service plan exists in the to
	 * case. Service Name:CaseMergeValidation
	 * 
	 * @param idStage
	 * @return Boolean
	 */
	@Override
	@Transactional(readOnly = true, isolation = Isolation.READ_COMMITTED, propagation = Propagation.REQUIRED, rollbackFor = {
			Exception.class })
	public Boolean isServicePlanExists(Long idStage) {
		Long recExist = 0l;
		if (null != idStage) {
			Query query = (Query) sessionFactory.getCurrentSession().createSQLQuery(isServicePlanExists)
					.setParameter("idStg", idStage);
			recExist = ((BigDecimal) query.uniqueResult()).longValue();
		}
		return (recExist > 0) ? ServiceConstants.TRUEVAL : ServiceConstants.FALSEVAL;
	}

	/**
	 * Method Description:This method checks if answering sec 3 started in from
	 * case. Service Name:CaseMergeValidation
	 * 
	 * @param idStage
	 * @return Boolean
	 */
	@Override
	@Transactional(readOnly = true, isolation = Isolation.READ_COMMITTED, propagation = Propagation.REQUIRED, rollbackFor = {
			Exception.class })
	public Boolean isSec3Started(Long idStage) {
		Long recExist = 0l;
		if (null != idStage) {
			Query query = (Query) sessionFactory.getCurrentSession().createSQLQuery(isSec3Started).setParameter("idStg",
					idStage);
			recExist = ((BigDecimal) query.uniqueResult()).longValue();
		}
		return (recExist > 0) ? ServiceConstants.TRUEVAL : ServiceConstants.FALSEVAL;
	}

	/**
	 * Method Description:This method checks if Intervention Selected in the
	 * from case. Service Name:CaseMergeValidation
	 * 
	 * @param idStage
	 * @return Boolean
	 * 
	 */
	@Override
	@Transactional(readOnly = true, isolation = Isolation.READ_COMMITTED, propagation = Propagation.REQUIRED, rollbackFor = {
			Exception.class })
	public Boolean isInterventionSelected(Long idStage) {
		Long recExist = 0l;
		if (null != idStage) {
			Query query = (Query) sessionFactory.getCurrentSession().createSQLQuery(isInterventionSelected)
					.setParameter("idStg", idStage);
			recExist = ((BigDecimal) query.uniqueResult()).longValue();
		}
		return (recExist > 0) ? ServiceConstants.TRUEVAL : ServiceConstants.FALSEVAL;
	}

	/**
	 * Method Description:This method checks if intervention started in from
	 * case. Service Name:CaseMergeValidation
	 * 
	 * @param idStage
	 * @return Boolean
	 * 
	 */
	@Override
	@Transactional(readOnly = true, isolation = Isolation.READ_COMMITTED, propagation = Propagation.REQUIRED, rollbackFor = {
			Exception.class })
	public Boolean isInterventionStarted(Long idStage) {
		Long recExist = 0l;
		if (null != idStage) {
			Query query = (Query) sessionFactory.getCurrentSession().createSQLQuery(isInterventionStarted)
					.setParameter("idStg", idStage);
			recExist = ((BigDecimal) query.uniqueResult()).longValue();
		}
		return (recExist > 0) ? ServiceConstants.TRUEVAL : ServiceConstants.FALSEVAL;
	}

	/**
	 * Method Description:This method evaluates for SHIELD Case. Service
	 * Name:CaseMergeValidation
	 * 
	 * @param stageDto
	 * @return Boolean
	 */
	@Override
	@Transactional(readOnly = true, isolation = Isolation.READ_COMMITTED, propagation = Propagation.REQUIRED, rollbackFor = {
			Exception.class })
	public Boolean isValidShieldCase(StageDto stageDto) {
		Boolean isValidShieldCase = Boolean.FALSE;
		Date shieldEffDt = ServiceConstants.SHIELD_EFFECTIVE_DATE;
		Date stgStartDt = stageDto.getDtStageStart();
		if ((ServiceConstants.CSTAGES_INV.equals(stageDto.getCdStage())) && null != stgStartDt
				&& (stgStartDt.after(shieldEffDt))
				|| (ServiceConstants.CSTAGES_SVC.equals(stageDto.getCdStage()))
						&& (ServiceConstants.CSTAGES_TYPE_ICS.equals(stageDto.getCdStageType()))
				|| (ServiceConstants.CSTAGES_TYPE_MNT.equals(stageDto.getCdStageType()))) {
			isValidShieldCase = Boolean.TRUE;
		}
		return isValidShieldCase;
	}

	/**
	 * This DAM will return the ID_PERSON of the lead PAL_COORDINATOR for the
	 * region of conservatorship for the PAL child. Warning - This DAM has had
	 * its GENDAM-generated code extensively modified. Be careful if a re-GENDAM
	 * is necessary.
	 * 
	 * Service Name : CCMN03U, CSEC66D
	 * 
	 * @param idStage
	 * @return
	 */
	@Override
	public Long getIdPersonForPALWorker(Long idStage) {
		Long idPerson = ServiceConstants.ZERO_VAL;
		if (null != idStage) {
			Query queryPALWorker = (Query) sessionFactory.getCurrentSession().createSQLQuery(getIdPersonForPALWorkerSql)
					.setParameter("idStage", idStage);
			// ALM 13510: added isEmpty check
			if (!CollectionUtils.isEmpty(queryPALWorker.list())) {
				BigDecimal personIdDecimal = (BigDecimal) queryPALWorker.list().get(0);
				if (!TypeConvUtil.isNullOrEmpty(personIdDecimal)) {
					idPerson = Long.valueOf(personIdDecimal.longValue());
				}
			}
		}
		return idPerson;
	}

	/**
	 * This DAM ADDs a full row in the STAGE_LINK table. It does not perform any
	 * UPDATE or DELETE functionality.
	 * 
	 * Service Name : CCMN03U, CCMNC1D
	 * 
	 * @param stageLink
	 * 
	 */
	@Override
	public void stageLinkSave(StageLink stageLink) {
		sessionFactory.getCurrentSession().persist(stageLink);
	}

	/**
	 * Dynamically builds Select statement and retrieves all events that satisfy
	 * the criteria Note - This DAM uses Method 4 of dynamic SQL. Note - Due to
	 * the complexity of this DAM, the Oracle array-fetch functionality is not
	 * currently used. Warning - This DAM has had its GENDAM-generated code
	 * extensively modified. Be careful if a re-GENDAM is necessary.
	 * 
	 * Service Name: CCMN03U, CCMN87D
	 * 
	 * @param idStage
	 * @param cdTask
	 * @return
	 */
	@SuppressWarnings("unchecked")
	@Override
	public List<EventStagePersonDto> getEventStagePersonListByAttributes(Long idStage, String cdTask, String cdType) {
		String queryString = getEventStagePersonListByAttributesSql;
		if (!TypeConvUtil.isNullOrEmpty(cdTask)) {
			queryString += " ";
			queryString += getEventStagePersonListByAttributesTaskAppendSql;
		}
		if (!TypeConvUtil.isNullOrEmpty(cdType)) {
			queryString += " ";
			queryString += getEventStagePersonListByAttributesAppendSql;
		}
		List<EventStagePersonDto> eventStagePersonDtoList = new ArrayList<>();
		Query queryStage = (Query) sessionFactory.getCurrentSession().createSQLQuery(queryString)
				.addScalar("cdEventStatus", StandardBasicTypes.STRING)
				.addScalar("cdEventType", StandardBasicTypes.STRING).addScalar("cdStage", StandardBasicTypes.STRING)
				.addScalar("dtEventOccurred", StandardBasicTypes.TIMESTAMP)
				.addScalar("cdStageReasonClosed", StandardBasicTypes.STRING)
				.addScalar("idCase", StandardBasicTypes.LONG).addScalar("idEvent", StandardBasicTypes.LONG)
				.addScalar("idEventStage", StandardBasicTypes.LONG).addScalar("nmStage", StandardBasicTypes.STRING)
				.addScalar("nmPersonFull", StandardBasicTypes.STRING).addScalar("eventDescr", StandardBasicTypes.STRING)
				.addScalar("cdTask", StandardBasicTypes.STRING).addScalar("stringN", StandardBasicTypes.STRING)
				.addScalar("dtEventCreated", StandardBasicTypes.TIMESTAMP)
				.setResultTransformer(Transformers.aliasToBean(EventStagePersonDto.class));
		queryStage.setParameter("idStage", idStage);
		if (!TypeConvUtil.isNullOrEmpty(cdTask)) {
			queryStage.setParameter("cdTask", cdTask);
		}
		if (!TypeConvUtil.isNullOrEmpty(cdType)) {
			queryStage.setParameter("cdType", cdType);
		}
		eventStagePersonDtoList = queryStage.list();
		return eventStagePersonDtoList;
	}

	/**
	 * This DAM retrieves a list of person id's and their full names who are
	 * Principals in the stage given as input to the DAM NOTE: This DAM contains
	 * non-GENDAM generated code which would need to be copied if this DAM is
	 * re-GENDAM'd.
	 * 
	 * Service Name : CCMN03U, DAM Name : CCMNE4D
	 * 
	 * @param idStage
	 * @return
	 */
	@SuppressWarnings("unchecked")
	@Override
	public List<PersonDto> getPersonFromPRNStageByIdStage(Long idStage) {
		List<PersonDto> personDtoList = new ArrayList<>();
		Query queryStage = sessionFactory.getCurrentSession().createSQLQuery(getPersonFromPRNStageByIdStageSql)
				.addScalar("idPerson", StandardBasicTypes.LONG).addScalar("nmPersonFull", StandardBasicTypes.STRING)
				.setResultTransformer(Transformers.aliasToBean(PersonDto.class));
		queryStage.setParameter("idStage", idStage);
		personDtoList = queryStage.list();
		return personDtoList;
	}

	/**
	 * This DAM takes as input an ID PERSON and performs the following logic: 1-
	 * It searches the STAGE_PERSON_LINK table and retrieves all those stages
	 * where ID_PERSON = Input.ID_PERSON.
	 * 
	 * 2- For each ID_STAGE retrieved in Step 1, it searches the STAGE table and
	 * verifies that the DT_STAGE_CLOSE = NULL and retrieves the ID_CASE
	 * associated with ID_STAGE, if any.
	 * 
	 * 3- It searches the PERSON table to verify that CD_PERSON_STATUS = 'A'
	 * (active)
	 * 
	 * 4- For every stage retrieved in Step 1, it searches the STAGE_PERSON_LINK
	 * table and retrieves the ID_PERSON where CD_STAGE_PERS_ROLE = "PR"
	 * (primary).
	 * 
	 * NOTE: This DAM contains non-GENDAM code that would need to be copied if
	 * this DAM is re-GENDAM'd.
	 * 
	 * @param idPerson
	 * @param idCase
	 * @return
	 */
	@SuppressWarnings("unchecked")
	@Override
	public List<StagePersonLinkDto> getStageByCasePersonId(Long idPerson, Long idCase) {
		List<StagePersonLinkDto> stagePersonLinkDtoList = new ArrayList<>();
		Query queryStage = sessionFactory.getCurrentSession().createSQLQuery(getStageByCasePersonIdSql)
				.addScalar("idStage", StandardBasicTypes.LONG).addScalar("idPerson", StandardBasicTypes.LONG)
				.addScalar("idCase", StandardBasicTypes.LONG)
				.setResultTransformer(Transformers.aliasToBean(StagePersonLinkDto.class));
		queryStage.setParameter("ulIdPerson", idPerson);
		queryStage.setParameter("ulIdCase", idCase);
		stagePersonLinkDtoList = queryStage.list();
		return stagePersonLinkDtoList;
	}

	/**
	 * Method-Description:This method checks if the passed CVS stage is
	 * currently checked out to the MPS Mobile device. The indicator for checked
	 * out cases is the CD_MOBILE_STATUS column on the Workload table.
	 * 
	 * @param ulIdStage
	 * @return Boolean -- true or False
	 */
	@SuppressWarnings("unchecked")
	@Override
	@Transactional
	public Boolean getCaseStageCheckoutStatus(Long idStage) {
		Boolean iscaseStageCheckoutStatus = Boolean.FALSE;
		Query query = sessionFactory.getCurrentSession().createSQLQuery(caseStageCheckoutStatusSql)
				.addScalar("cdMobileStatus", StandardBasicTypes.STRING).setParameter("idStage", idStage);
		if (!TypeConvUtil.isNullOrEmpty(query.list())) {
			for (Object obj : query.list()) {
				if (!TypeConvUtil.isNullOrEmpty(obj)
						&& (obj.toString().equalsIgnoreCase(ServiceConstants.CHECKOUTSTATUS_OT)
								|| obj.toString().equalsIgnoreCase(ServiceConstants.CHECKOUTSTATUS_AT))) {
					iscaseStageCheckoutStatus = Boolean.TRUE;
				} else if (TypeConvUtil.isNullOrEmpty(obj) && iscaseStageCheckoutStatus == true) {
					iscaseStageCheckoutStatus = Boolean.TRUE;
				}
			}
		}
		return iscaseStageCheckoutStatus;
	}

	@Value("${StageDaoImpl.selectStageByCaseIdSql}")
	private String selectStageByCaseId;

	@Value("${StageDaoImpl.orderByStageIdClause}")
	private String orderByStageId;

	@Value("${StageDaoImpl.orderByStageIdStartDTClause}")
	private String orderBySituationStartDt;

	/**
	 * Two Dams - differntiated by order by CLSC59D/CLSS30D CLSC59D Method
	 * Description: This DAM will retrieve a full row from STAGE using ID_CASE
	 * as the input CLSS30D : This dam retrieves a full row from the Stage table
	 * for a given ID CASE sortrd by STAGE TYPE
	 * 
	 * @param idCase
	 * @return Stage
	 */
	@SuppressWarnings("unchecked")
	public List<StageDto> getStageEntityByCaseId(Long idCase, StageDao.ORDERBYCLAUSE orderByclause) {
		String sqlQuery = selectStageByCaseId;
		switch (orderByclause) {
		case BYSITUATIONSTARTDT:
			sqlQuery += orderBySituationStartDt;
			break;
		case BYSTAGEID:
			sqlQuery += orderByStageId;
			break;
		}
		return ((List<StageDto>) sessionFactory.getCurrentSession().createSQLQuery(sqlQuery)
				.addScalar("idStage", StandardBasicTypes.LONG).addScalar("dtLastUpdate", StandardBasicTypes.DATE)
				.addScalar("idUnit", StandardBasicTypes.LONG).addScalar("idCase", StandardBasicTypes.LONG)
				.addScalar("dtStageClose", StandardBasicTypes.DATE).addScalar("cdStageType", StandardBasicTypes.STRING)
				.addScalar("cdStageClassification", StandardBasicTypes.STRING)
				.addScalar("cdStageCurrPriority", StandardBasicTypes.STRING)
				.addScalar("cdStageInitialPriority", StandardBasicTypes.STRING)
				.addScalar("cdStageRsnPriorityChgd", StandardBasicTypes.STRING)
				.addScalar("cdStageReasonClosed", StandardBasicTypes.STRING)
				.addScalar("indStageClose", StandardBasicTypes.STRING)
				.addScalar("stagePriorityCmnts", StandardBasicTypes.STRING)
				.addScalar("nmStage", StandardBasicTypes.STRING).addScalar("cdStageRegion", StandardBasicTypes.STRING)
				.addScalar("dtStageCreated", StandardBasicTypes.DATE).addScalar("idSituation", StandardBasicTypes.LONG)
				.addScalar("cdStageProgram", StandardBasicTypes.STRING).addScalar("cdStage", StandardBasicTypes.STRING)
				.addScalar("stageClosureCmnts", StandardBasicTypes.STRING).setParameter("idCase", idCase)
				.setResultTransformer(Transformers.aliasToBean(StageDto.class)).list());
	}

	/**
	 * This list select DAM joins the Stage and Stage Person Link Table to find
	 * all of the active CASES, Stages and programs the ID Person is involved
	 * in.
	 * 
	 * Service Name : CCMN02U, DAM Name : CINV33D
	 * 
	 * @param idPerson
	 * @return
	 */
	@SuppressWarnings("unchecked")
	@Override
	public List<StageDto> getActiveStageByIdPerson(Long idPerson) {
		List<StageDto> stageDtoList = new ArrayList<>();
		Query queryStage = sessionFactory.getCurrentSession().createSQLQuery(getActiveStageByIdPersonSql)
				.addScalar("idStage", StandardBasicTypes.LONG).addScalar("cdStageProgram", StandardBasicTypes.STRING)
				.addScalar("idCase", StandardBasicTypes.LONG)
				.setResultTransformer(Transformers.aliasToBean(StageDto.class));
		queryStage.setParameter("idPerson", idPerson);
		stageDtoList = queryStage.list();
		return stageDtoList;
	}

	@Override
	public ArrayList<StagePersonValueDto> getOpenStagesPersonClosedAndForwardAndWorker(Long fwdPersonId,
			Long closedPersonId) {
		ArrayList<StagePersonValueDto> result = null;
		Query query = sessionFactory.getCurrentSession().createSQLQuery(openStageWithTwoPersonSql)
				.addScalar("indSensitiveCase", StandardBasicTypes.STRING).addScalar("idCase", StandardBasicTypes.LONG)
				.addScalar("idStage", StandardBasicTypes.LONG).addScalar("cdStage", StandardBasicTypes.STRING)
				.addScalar("cdStageProgram", StandardBasicTypes.STRING)
				.addScalar("dtStageStart", StandardBasicTypes.TIMESTAMP)
				.addScalar("cdStagePersRole", StandardBasicTypes.STRING)
				.addScalar("cdStageforwardPersRole", StandardBasicTypes.STRING)
				.setParameter("idPerson1", closedPersonId).setParameter("idPerson2", fwdPersonId)
				.setResultTransformer(Transformers.aliasToBean(StagePersonValueDto.class));
		result = (ArrayList<StagePersonValueDto>) query.list();
		return result;
	}

	@Override
	public ArrayList<EventValueDto> getPendingStageClosureEventForPerson(Long personId) {
		ArrayList<EventValueDto> result = null;
		Query query = sessionFactory.getCurrentSession().createSQLQuery(getPendingClosureEventSql)
				.addScalar("indSensitiveCase", StandardBasicTypes.STRING).addScalar("idCase", StandardBasicTypes.LONG)
				.addScalar("idStage", StandardBasicTypes.LONG).addScalar("cdStage", StandardBasicTypes.STRING)
				.addScalar("cdProgram", StandardBasicTypes.STRING)
				.addScalar("dtEventModified", StandardBasicTypes.TIMESTAMP)
				.addScalar("eventDescr", StandardBasicTypes.STRING).setParameter("idPerson", personId)
				.setResultTransformer(Transformers.aliasToBean(EventValueDto.class));
		result = (ArrayList<EventValueDto>) query.list();
		return result;
	}

	/**
	 * 
	 * Method Name: retrieveStageInfo Method Description: This method retrieves
	 * information from Stage table using idStage.
	 * 
	 * @param idStage
	 * @return StageValueBeanDto
	 * @throws DataNotFoundException
	 */
	@Override
	public StageValueBeanDto retrieveStageInfo(Long idStage) throws DataNotFoundException {

		SQLQuery sqlQuery = (SQLQuery) sessionFactory.getCurrentSession().createSQLQuery(retrieveStageInfo)
				.setResultTransformer(Transformers.aliasToBean(StageValueBeanDto.class));
		sqlQuery.addScalar("idStage", StandardBasicTypes.LONG);
		sqlQuery.addScalar("dtLastUpdate", StandardBasicTypes.DATE);
		sqlQuery.addScalar("cdStageType", StandardBasicTypes.STRING);
		sqlQuery.addScalar("idUnit", StandardBasicTypes.LONG);
		sqlQuery.addScalar("idCase", StandardBasicTypes.LONG);
		sqlQuery.addScalar("idSituation", StandardBasicTypes.LONG);
		sqlQuery.addScalar("dtStageClose", StandardBasicTypes.DATE);
		sqlQuery.addScalar("cdStageClassification", StandardBasicTypes.STRING);
		sqlQuery.addScalar("cdStageCurrPriority", StandardBasicTypes.STRING);
		sqlQuery.addScalar("cdStageInitialPriority", StandardBasicTypes.STRING);
		sqlQuery.addScalar("cdStageRsnPriorityChgd", StandardBasicTypes.STRING);
		sqlQuery.addScalar("cdStageReasonClosed", StandardBasicTypes.STRING);
		sqlQuery.addScalar("indStageClose", StandardBasicTypes.STRING);
		sqlQuery.addScalar("cdStageCnty", StandardBasicTypes.STRING);
		sqlQuery.addScalar("nmStage", StandardBasicTypes.STRING);
		sqlQuery.addScalar("cdStageRegion", StandardBasicTypes.STRING);
		sqlQuery.addScalar("dtStageStart", StandardBasicTypes.DATE);
		sqlQuery.addScalar("cdStageProgram", StandardBasicTypes.STRING);
		sqlQuery.addScalar("cdStage", StandardBasicTypes.STRING);
		sqlQuery.addScalar("stagePriorityCmnts", StandardBasicTypes.STRING);
		sqlQuery.addScalar("stageClosureCmnts", StandardBasicTypes.STRING);
		sqlQuery.addScalar("cdClientAdvised", StandardBasicTypes.STRING);
		sqlQuery.addScalar("indEcs", StandardBasicTypes.STRING);
		sqlQuery.addScalar("indEcsVer", StandardBasicTypes.STRING);
		sqlQuery.addScalar("indAssignStage", StandardBasicTypes.STRING);
		sqlQuery.addScalar("dtClientAdvised", StandardBasicTypes.DATE);
		sqlQuery.addScalar("dtMultiRef", StandardBasicTypes.DATE);
		sqlQuery.addScalar("dtStageCreated", StandardBasicTypes.DATE);
		sqlQuery.addScalar("indSecondApprover", StandardBasicTypes.STRING);
		sqlQuery.addScalar("indScreened", StandardBasicTypes.STRING);
		sqlQuery.addScalar("indFoundOpenCaseAtIntake", StandardBasicTypes.STRING);
		sqlQuery.addScalar("indFormallyScreened", StandardBasicTypes.STRING);

		sqlQuery.setParameter("idStage", idStage);
		StageValueBeanDto stageValueBeanDto = (StageValueBeanDto) sqlQuery.uniqueResult();

		if (TypeConvUtil.isNullOrEmpty(stageValueBeanDto)) {
			throw new DataNotFoundException(
					messageSource.getMessage("stageValueBeanDto.idStage.NotFound", null, Locale.US));
		}

		return stageValueBeanDto;
	}

	/**
	 * @param idStage
	 * @param eligWorkerProfiles
	 * @return List
	 */
	@Override
	public List<StagePersonLinkDto> findWorkersForStage(Long idStage, String[] eligWorkerProfiles) {
		StringBuilder sqlQueryString = new StringBuilder(findWorkersForStageSql);
		sqlQueryString.append(createMutiValueWhereClause("SUBSTR(SEC.TXT_SECURITY_CLASS_PROFIL", eligWorkerProfiles));
		SQLQuery sqlQuery = (SQLQuery) sessionFactory.getCurrentSession().createSQLQuery(sqlQueryString.toString())
				.addScalar("idPerson", StandardBasicTypes.LONG).setParameter("idStage", idStage)
				.setResultTransformer(Transformers.aliasToBean(StagePersonLinkDto.class));
		List<StagePersonLinkDto> stagePersonLinkDtoList = sqlQuery.list();

		return stagePersonLinkDtoList;
	}

	/**
	 * Method Name: createMutiValueWhereClause Method Description:
	 * 
	 * @param columnName
	 * @param eligWorkerProfiles
	 * @return String
	 */
	private String createMutiValueWhereClause(String columnName, String[] eligWorkerProfiles) {
		StringBuilder valueWhereClause = new StringBuilder();
		if (eligWorkerProfiles.length > 0) {
			valueWhereClause.append(" AND ( ");
			for (Integer count = 0; count < eligWorkerProfiles.length; count++) {
				valueWhereClause.append(columnName).append(" , ").append(eligWorkerProfiles[count]).append(", 1) = 1 ");
				if (count < eligWorkerProfiles.length - 1) {
					valueWhereClause.append(" OR ");
				}
			}
			valueWhereClause.append(" ) ");
		}
		return valueWhereClause.toString();
	}

	/**
	 * Method Name: findStageInfoForPerson Method Description: This function
	 * returns Stage Id of the Given Stage Code for the give Person.
	 * 
	 * @param CommonHelperReq
	 * @return StageValueBeanDto
	 */
	@Override
	public StageValueBeanDto findStageInfoForPerson(CommonHelperReq commonHelperReq) throws DataNotFoundException {
		StageValueBeanDto stageValueBeanDto = new StageValueBeanDto();
		long idPerson = 0L;
		String cdStage = "";
		String cdStagePersonRole = "";

		if (null != commonHelperReq) {
			idPerson = commonHelperReq.getIdPerson();
			cdStage = commonHelperReq.getCdStage();
			cdStagePersonRole = commonHelperReq.getCdStagePersonRole();
		}
		ArrayList<StageValueBeanDto> stageValueBeanDtoList = null;
		Query query = sessionFactory.getCurrentSession().createSQLQuery(fetchStageInfoForPersonSql)
				.addScalar("idCase", StandardBasicTypes.LONG).addScalar("idStage", StandardBasicTypes.LONG)
				.setParameter("idPerson", idPerson).setParameter("cdStage", cdStage)
				.setParameter("cdStagePersonRole", cdStagePersonRole)
				.setResultTransformer(Transformers.aliasToBean(StageValueBeanDto.class));
		stageValueBeanDtoList = (ArrayList<StageValueBeanDto>) query.list();

		if (!TypeConvUtil.isNullOrEmpty(stageValueBeanDtoList) && stageValueBeanDtoList.size() > 0) {
			stageValueBeanDto = stageValueBeanDtoList.get(0);
		} else {
			stageValueBeanDto = null;
		}

		return stageValueBeanDto;
	}

	/**
	 * Method Name: retrieveStageInfo Method Description:This method retrieves
	 * information from Stage table using idStage.
	 * 
	 * @param idARStage
	 * @return Stage
	 */
	public Stage retrieveStageInfo(long idARStage) {
		Stage stage = (Stage) sessionFactory.getCurrentSession().get(Stage.class, idARStage);
		return stage;
	}

	/**
	 * Method Name: retrieveStageInfoList Method Description:This method
	 * retrieves information from Stage table using idStage.
	 * 
	 * @param idARStage
	 * @return StageValueBeanDto
	 */
	@Override
	public StageValueBeanDto retrieveStageInfoList(long idARStage) {
		Stage stage = (Stage) sessionFactory.getCurrentSession().get(Stage.class, idARStage);
		StageValueBeanDto stageValueBeanDto = new StageValueBeanDto();
		stageValueBeanDto.setIdStage(stage.getIdStage());
		stageValueBeanDto.setDtLastUpdate(stage.getDtLastUpdate());
		stageValueBeanDto.setCdStageType(stage.getCdStageType());
		stageValueBeanDto.setIdUnit(stage.getIdUnit());
		stageValueBeanDto.setIdCase(stage.getCapsCase().getIdCase());
		stageValueBeanDto.setIdSituation(stage.getSituation().getIdSituation());
		stageValueBeanDto.setDtStageClose(stage.getDtStageClose());
		stageValueBeanDto.setCdStageClassification(stage.getCdStageClassification());
		stageValueBeanDto.setCdStageCurrPriority(stage.getCdStageCurrPriority());
		stageValueBeanDto.setCdStageInitialPriority(stage.getCdStageInitialPriority());
		stageValueBeanDto.setCdStageRsnPriorityChgd(stage.getCdStageRsnPriorityChgd());
		stageValueBeanDto.setCdStageReasonClosed(stage.getCdStageReasonClosed());
		stageValueBeanDto.setIndStageClose(stage.getIndStageClose());
		stageValueBeanDto.setCdStageCnty(stage.getCdStageCnty());
		stageValueBeanDto.setNmStage(stage.getNmStage());
		stageValueBeanDto.setCdStageRegion(stage.getCdStageRegion());
		stageValueBeanDto.setDtStageStart(stage.getDtStageStart());
		stageValueBeanDto.setCdStageProgram(stage.getCdStageProgram());
		stageValueBeanDto.setCdStage(stage.getCdStage());
		stageValueBeanDto.setStagePriorityCmnts(stage.getTxtStagePriorityCmnts());
		stageValueBeanDto.setStageClosureCmnts(stage.getTxtStageClosureCmnts());
		stageValueBeanDto.setCdClientAdvised(stage.getCdClientAdvised());
		stageValueBeanDto.setIndEcs(stage.getIndEcs());
		stageValueBeanDto.setIndEcsVer(stage.getIndEcsVer());
		stageValueBeanDto.setIndAssignStage(stage.getIndAssignStage());
		stageValueBeanDto.setDtClientAdvised(stage.getDtClientAdvised());
		stageValueBeanDto.setDtMultiRef(stage.getDtMultiRef());
		stageValueBeanDto.setDtStageCreated(stage.getDtStageCreated());
		stageValueBeanDto.setIndSecondApprover(stage.getIndSecondApprover());
		stageValueBeanDto.setIndScreened(stage.getIndScreened());
		stageValueBeanDto.setIndFoundOpenCaseAtIntake(stage.getIndFoundOpenCaseAtIntake());
		stageValueBeanDto.setIndFormallyScreened(stage.getIndFormallyScreened());
		return stageValueBeanDto;
	}

	/**
	 * 
	 * Method Name: insertIntoStage Method Description: to insert new stage
	 * details
	 * 
	 * @param stage
	 * @return Integer
	 * @throws DataNotFoundException
	 */
	@Override
	public Long insertIntoStage(StageValueBeanDto stageValueBeanDto) throws DataNotFoundException {

		// Get CapsCase persistent instance details
		CapsCase capsCase = (CapsCase) sessionFactory.getCurrentSession().get(CapsCase.class,
				stageValueBeanDto.getIdCase());
		// Get Situation persistent instance details
		Situation situation = (Situation) sessionFactory.getCurrentSession().get(Situation.class,
				stageValueBeanDto.getIdSituation());

		Stage stage = new Stage();
		stage.setCapsCase(capsCase);
		stage.setDtLastUpdate(stageValueBeanDto.getDtLastUpdate());
		stage.setCdStageType(stageValueBeanDto.getCdStageType());
		stage.setIdUnit(stageValueBeanDto.getIdUnit());

		stage.setSituation(situation);
		stage.setDtStageClose(stageValueBeanDto.getDtStageClose());
		stage.setCdStageClassification(stageValueBeanDto.getCdStageClassification());
		stage.setCdStageCurrPriority(stageValueBeanDto.getCdStageCurrPriority());
		stage.setCdStageInitialPriority(stageValueBeanDto.getCdStageInitialPriority());
		stage.setCdStageRsnPriorityChgd(stageValueBeanDto.getCdStageRsnPriorityChgd());
		stage.setCdStageReasonClosed(stageValueBeanDto.getCdStageReasonClosed());
		stage.setIndStageClose(stageValueBeanDto.getIndStageClose());
		stage.setCdStageCnty(stageValueBeanDto.getCdStageCnty());
		stage.setNmStage(stageValueBeanDto.getNmStage());
		stage.setCdStageRegion(stageValueBeanDto.getCdStageRegion());
		stage.setDtStageStart(stageValueBeanDto.getDtStageStart());
		stage.setCdStageProgram(stageValueBeanDto.getCdStageProgram());
		stage.setCdStage(stageValueBeanDto.getCdStage());
		stage.setTxtStagePriorityCmnts(stageValueBeanDto.getStagePriorityCmnts());
		stage.setTxtStageClosureCmnts(stageValueBeanDto.getStageClosureCmnts());
		stage.setCdClientAdvised(stageValueBeanDto.getCdClientAdvised());
		stage.setIndEcs(stageValueBeanDto.getIndEcs());
		stage.setIndEcsVer(stageValueBeanDto.getIndEcsVer());
		stage.setIndAssignStage(stageValueBeanDto.getIndAssignStage());
		stage.setDtClientAdvised(stageValueBeanDto.getDtClientAdvised());
		stage.setDtMultiRef(stageValueBeanDto.getDtMultiRef());
		stage.setDtStageCreated(stageValueBeanDto.getDtStageCreated());
		stage.setIndSecondApprover(stageValueBeanDto.getIndSecondApprover());
		stage.setIndScreened(stageValueBeanDto.getIndScreened());
		stage.setIndFoundOpenCaseAtIntake(stageValueBeanDto.getIndFoundOpenCaseAtIntake());
		stage.setIndFormallyScreened(stageValueBeanDto.getIndFormallyScreened());

		sessionFactory.getCurrentSession().save(stage);

		return stage.getIdStage();
	}

	/**
	 * Method Name: selectStagePersonLink Method Description:This function
	 * returns StagePersonValueBean with Rel/Int populated for the given Stage
	 * and Person. This function can be expanded to retrieve entire Stage person
	 * link row.
	 * 
	 * @param idPerson
	 * @param idStage
	 * @return StagePersonValueDto
	 * @throws DataNotFoundException
	 */
	@Override
	public StagePersonValueDto selectStagePersonLink(Long idPerson, Long idStage) throws DataNotFoundException {
		Criteria criteria = sessionFactory.getCurrentSession().createCriteria(StagePersonLink.class);
		criteria.add(Restrictions.eq("idStage", idStage));
		criteria.add(Restrictions.eq("idPerson", idPerson));
		List<StagePersonLink> stagePersonLinks = criteria.list();
		StagePersonValueDto stagePersonValueDto = new StagePersonValueDto();
		if (!CollectionUtils.isEmpty(stagePersonLinks)) {

			StagePersonLink stagePersonLink = stagePersonLinks.get(0);
			stagePersonValueDto.setCdStagePersRelLong(stagePersonLink.getCdStagePersRelInt());
			stagePersonValueDto.setIdPerson(stagePersonLink.getIdPerson());
			stagePersonValueDto.setIdStage(stagePersonLink.getIdStage());
			stagePersonValueDto.setIdCase(stagePersonLink.getIdCase());
			stagePersonValueDto.setCdStagePersRole(stagePersonLink.getCdStagePersRole());
		}
		return stagePersonValueDto;
	}

	/**
	 * Method Name: findLinkedStageId Method Description:
	 * 
	 * @param idStage
	 * @param idStage
	 * @return Long
	 * @throws DataNotFoundException
	 */

	@Override
	public Long findLinkedStageId(Long idStage) throws DataNotFoundException {

		Long idPriorStage = (Long) sessionFactory.getCurrentSession().createSQLQuery(getPriorStageByID)
				.addScalar("priorStageId", StandardBasicTypes.LONG).setParameter("idStage", idStage).uniqueResult();

		return idPriorStage;
	}

	/**
	 * Method Name: getStageListForPC Method Description:Fetches the list of
	 * stages where a person is a Primary Child (PC)
	 * 
	 * @param idPerson
	 * @param nOpenStageOnly
	 * @return ArrayList of StageValueBean
	 * @throws SQLException
	 */
	@SuppressWarnings("unchecked")
	public ArrayList<StageValueBeanDto> getStageListForPC(int idPerson, boolean nOpenStageOnly) {
		ArrayList<StageValueBeanDto> stageValueList = null;
		SQLQuery query = null;
		if (nOpenStageOnly) {
			query = sessionFactory.getCurrentSession()
					.createSQLQuery(getStageListForPCSql + getStageListForPCWhereClauseSql);

		} else {
			query = sessionFactory.getCurrentSession().createSQLQuery(getStageListForPCSql);

		}
		query.addScalar("idStage", StandardBasicTypes.LONG).addScalar("dtLastUpdate", StandardBasicTypes.DATE)
				.addScalar("cdStageType", StandardBasicTypes.STRING).addScalar("idUnit", StandardBasicTypes.LONG)
				.addScalar("idCase", StandardBasicTypes.LONG).addScalar("idSituation", StandardBasicTypes.LONG)
				.addScalar("dtStageClose", StandardBasicTypes.DATE)
				.addScalar("cdStageClassification", StandardBasicTypes.STRING)
				.addScalar("cdStageCurrPriority", StandardBasicTypes.STRING)
				.addScalar("cdStageInitialPriority", StandardBasicTypes.STRING)
				.addScalar("cdStageRsnPriorityChgd", StandardBasicTypes.STRING)
				.addScalar("cdStageReasonClosed", StandardBasicTypes.STRING)
				.addScalar("indStageClose", StandardBasicTypes.STRING)
				.addScalar("cdStageCnty", StandardBasicTypes.STRING).addScalar("nmStage", StandardBasicTypes.STRING)
				.addScalar("cdStageRegion", StandardBasicTypes.STRING)
				.addScalar("dtStageStart", StandardBasicTypes.DATE)
				.addScalar("cdStageProgram", StandardBasicTypes.STRING).addScalar("cdStage", StandardBasicTypes.STRING)
				.addScalar("stagePriorityCmnts", StandardBasicTypes.STRING)
				.addScalar("stageClosureCmnts", StandardBasicTypes.STRING)
				.addScalar("cdClientAdvised", StandardBasicTypes.STRING).addScalar("indEcs", StandardBasicTypes.STRING)
				.addScalar("indEcsVer", StandardBasicTypes.STRING)
				.addScalar("indAssignStage", StandardBasicTypes.STRING)
				.addScalar("dtClientAdvised", StandardBasicTypes.DATE).addScalar("dtMultiRef", StandardBasicTypes.DATE)
				.addScalar("dtStageCreated", StandardBasicTypes.DATE)
				.addScalar("indSecondApprover", StandardBasicTypes.STRING)
				.addScalar("indScreened", StandardBasicTypes.STRING)
				.addScalar("indFoundOpenCaseAtIntake", StandardBasicTypes.STRING)
				.addScalar("indFormallyScreened", StandardBasicTypes.STRING);

		query.setParameter(ServiceConstants.STAGE_IDPERSON, idPerson);

		query.setResultTransformer(Transformers.aliasToBean(StageValueBeanDto.class));

		stageValueList = (ArrayList<StageValueBeanDto>) query.list();
		return stageValueList;

	}

	/**
	 * Method Name: getStageProgressedList Method Description:This function
	 * fetches list of progressed stages from a stage
	 * 
	 * @param idStage
	 * @return Stage
	 */
	@Override
	public List<Stage> getStageProgressedList(long idStage) {
		List<Stage> stages = new ArrayList<Stage>();
		Criteria criteria = sessionFactory.getCurrentSession().createCriteria(StageLink.class);
		criteria.add(Restrictions.eq("idPriorStage", idStage));
		List<StageLink> stageLinks = criteria.list();
		for (StageLink stageLink : stageLinks) {
			Criteria criteria1 = sessionFactory.getCurrentSession().createCriteria(Stage.class);
			criteria1.add(Restrictions.eq("idStage", stageLink.getIdStage()));
			stages.add((Stage) criteria1.uniqueResult());
		}
		return stages;
	}

	/**
	 * Method Name: getOpenStagesForWithPersons Method Description:This function
	 * fetches the stages which have both person 1 and person 2
	 * 
	 * @param idPerson1
	 * @param idPerson2
	 * @return ArrayList of stage Id
	 */
	@Override
	public ArrayList<StageDto> getOpenStagesForWithPersons(long idPerson1, long idPerson2) {
		Query query = sessionFactory.getCurrentSession().createSQLQuery(getOpenStagesForWithPersonsSql)
				.addScalar(ServiceConstants.STAGE_STAGEID, StandardBasicTypes.LONG).setParameter("idPerson1", idPerson1)
				.setParameter("idPerson2", idPerson2).setResultTransformer(Transformers.aliasToBean(StageDto.class));

		return (ArrayList) query.list();

	}

	/**
	 * Method Name: getStagePersonListByRole Method Description:This function
	 * fetches the list of persons in a stage in a specific order of their roles
	 * PC (Primary Child), SP (Sustained Perpetrator), DB (Designated
	 * Victim/Perpetrator) DP (Designated Perpetrator) DV (Designated Victim) VP
	 * (Alleged Victim/Perpetrator) VC (Alleged Victim) CL (Client) AP (Alleged
	 * Perpetrator) AV UD (Unknown/Unable to Determine) UC (Unknown/Unable to
	 * Complete) UM (Unknown/Moved) UK (Unknown) NO (No Role)
	 * 
	 * @param idStage
	 * @return ArrayList of StagePersonValueBean
	 */
	@SuppressWarnings("unchecked")
	@Override
	public ArrayList<us.tx.state.dfps.common.dto.StagePersonValueDto> getStagePersonListByRole(long idStage) {

		Query query = sessionFactory.getCurrentSession().createSQLQuery(getStagePersonListByRole)
				.addScalar("idStagePersonLink", StandardBasicTypes.LONG).addScalar("idStage", StandardBasicTypes.LONG)
				.addScalar("dtLastUpdate", StandardBasicTypes.DATE).addScalar("idPerson", StandardBasicTypes.LONG)
				.addScalar("cdStagePersRole", StandardBasicTypes.STRING)
				.addScalar("cdStagePersType", StandardBasicTypes.STRING)
				.addScalar("indKinPrCaregiver", StandardBasicTypes.STRING)
				.addScalar("indNmStage", StandardBasicTypes.LONG)
				.addScalar("indStagePersInLaw", StandardBasicTypes.STRING)
				.addScalar("indStagePersReporter", StandardBasicTypes.STRING)
				.addScalar("stagePersNotes", StandardBasicTypes.STRING)
				.addScalar("indCaringAdult", StandardBasicTypes.STRING)
				.addScalar("indNytdContact", StandardBasicTypes.STRING)
				.addScalar("indNytdContactPrimary", StandardBasicTypes.STRING)
				.setParameter(ServiceConstants.STAGE_STAGEID, idStage)
				.setResultTransformer(Transformers.aliasToBean(us.tx.state.dfps.common.dto.StagePersonValueDto.class));

		ArrayList<us.tx.state.dfps.common.dto.StagePersonValueDto> stagePersonLinkDtoList = (ArrayList<us.tx.state.dfps.common.dto.StagePersonValueDto>) query
				.list();
		return stagePersonLinkDtoList;

	}

	/**
	 * Method Name: deleteStagePersonLink Method Description:Deletes row from
	 * STAGE_PERSON_LINK table
	 * 
	 * @param stagePersonValueBean
	 * @return long
	 */
	@Override
	public long deleteStagePersonLink(StagePersonValueDto stagePersonValueBean) {
		Criteria criteria = sessionFactory.getCurrentSession().createCriteria(StagePersonLink.class);

		// where condition in queries mapped
		criteria.add(Restrictions.eq(ServiceConstants.STAGE_PERSONLINK, stagePersonValueBean.getIdStagePersonLink()));
		criteria.add(Restrictions.le("dtLastUpdate", stagePersonValueBean.getDtLastUpdate()));

		StagePersonLink stage = (StagePersonLink) criteria.uniqueResult();
		if (!ObjectUtils.isEmpty(stage))
			sessionFactory.getCurrentSession().delete(stage);
		return criteria.list().size();
	}
	
	/**
	 * Method Name: updateRciIndicator
	 * @param ldIdTodo
	 * artf129782: Licensing Investigation Conclusion
	 */
	@Override
	public void updateRciIndicator( List<TodoDto> todoDtoList){
		
		TodoDto todoDto = null;
		List<Long> stageIdList = new ArrayList();
		
		for (int i = 0; i < todoDtoList.size(); i++) {
			
			todoDto = todoDtoList.get(i);
			
			String cdTask = todoDto.getCdTodoTask();
			log.info("ldTodo from todo table:"+ todoDto.getIdTodo());
		    Long idStage =	todoDto.getIdTodoStage();
		    if(cdTask != null && cdTask.equalsIgnoreCase(ServiceConstants.RCL_ALERT_TASK_CODE )){
		    	if(!stageIdList.contains(idStage)){
		    			stageIdList.add(idStage);
		    	}
		      }
		    }
		   
		for (int i = 0; i < stageIdList.size(); i++) {
		
			Stage stage = (Stage) sessionFactory.getCurrentSession().get(Stage.class, stageIdList.get(i));
			if(!getRCIAlertExists(stage.getIdStage())){
				stage.setIndVictimNotifStatus(ServiceConstants.STRING_IND_N);
				updateStageIndVictimStatus(stage);
			}
		}
		    
		    
	}
	
	/**
	 * 
	 */
	@Override 
	public Boolean getRCIAlertExists(Long idStage ) {
		
		Boolean value = Boolean.FALSE;
		BigDecimal recExists = (BigDecimal) sessionFactory.getCurrentSession().createSQLQuery(getRCIAlertSql)
				.setParameter("idStage", idStage).uniqueResult();
		if (!TypeConvUtil.isNullOrEmptyBdDecm(recExists)) {
			value = (recExists.longValue() > 0) ? true : false;
		}
		return value;
	}

	@Override
	public IntakeNotfChildDto getStagePersonFilterByAdoStage(Long idPerson) {
		SQLQuery sqlQuery = (SQLQuery) sessionFactory.getCurrentSession().createSQLQuery(getStagePersonFilterByAdoStage)
				.addScalar("idStage", StandardBasicTypes.LONG).addScalar("idCase", StandardBasicTypes.LONG)
				.addScalar("idPerson", StandardBasicTypes.LONG).addScalar("nmPersonFull", StandardBasicTypes.STRING)
				.addScalar("cdLegalStatStatus", StandardBasicTypes.STRING)
				.setParameter("idPerson", idPerson).setResultTransformer(Transformers.aliasToBean(IntakeNotfChildDto.class));
		List<IntakeNotfChildDto> stagePersonLinkDtos = sqlQuery.list();
		return stagePersonLinkDtos.size() > 0 ? stagePersonLinkDtos.get(0) : null;
	}

	@Override
	public List<IntakeNotfChildDto> getStagesForPCSelfByStageType(Long idPerson,String cdStage) {
		SQLQuery sqlQuery = (SQLQuery) sessionFactory.getCurrentSession().createSQLQuery(getStagesForPCSelfByStageType)
				.addScalar("idStage", StandardBasicTypes.LONG).addScalar("idCase", StandardBasicTypes.LONG)
				.addScalar("idPerson", StandardBasicTypes.LONG).addScalar("nmPersonFull", StandardBasicTypes.STRING)
				.addScalar("cdLegalStatStatus", StandardBasicTypes.STRING)
				.setParameter("cdStage",cdStage).setParameter("idPerson", idPerson)
				.setResultTransformer(Transformers.aliasToBean(IntakeNotfChildDto.class));
		List<IntakeNotfChildDto> intakeNotfChildDtos = sqlQuery.list();
		return intakeNotfChildDtos;
	}

	@Override
	public IntakeNotfChildDto getStagePersonFilterByPersTypeAndRole(Long idStage) {
		SQLQuery sqlQuery = (SQLQuery) sessionFactory.getCurrentSession().createSQLQuery(getStagePersonFilterByPersTypeAndRole)
				.addScalar("idWorkerPerson", StandardBasicTypes.LONG).addScalar("nmWorkerPerson", StandardBasicTypes.STRING)
				.addScalar("idSupervisor", StandardBasicTypes.LONG).addScalar("nmSupervisor", StandardBasicTypes.STRING)
				.setParameter("idStage", idStage)
				.setResultTransformer(Transformers.aliasToBean(IntakeNotfChildDto.class));
		List<IntakeNotfChildDto> stagePersonLinkDtos = sqlQuery.list();
		return stagePersonLinkDtos.size() > 0 ? stagePersonLinkDtos.get(0) : null;
	}
}
