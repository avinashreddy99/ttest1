package us.tx.state.dfps.service.pca.daoimpl;

import java.util.List;

import org.apache.log4j.Logger;
import org.hibernate.SQLQuery;
import org.hibernate.SessionFactory;
import org.hibernate.transform.Transformers;
import org.hibernate.type.StandardBasicTypes;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;

import us.tx.state.dfps.service.pca.dao.PcaDao;
import us.tx.state.dfps.service.pca.dto.PcaEligApplicationDto;
import us.tx.state.dfps.service.pca.dto.PlacementDtlDto;
import us.tx.state.dfps.service.pca.dto.ResourcePlcmntDto;
import us.tx.state.dfps.service.pca.dto.StageCaseDtlDto;
import us.tx.state.dfps.service.person.dto.SupervisorDto;

/**
 * service-business- IMPACT PHASE 2 MODERNIZATION Class Description:PcaDaoImpl
 * will implemented all operation defined in PcaDao Interface related PCA
 * module.. Feb 9, 2018- 2:02:51 PM © 2017 Texas Department of Family and
 * Protective Services
 */
@Repository
public class PcaDaoImpl implements PcaDao {

	@Value("${PcaDaoImpl.getStageCaseDtls}")
	String getStageCaseDtlsSql;

	@Value("${PcaDaoImpl.getResourcePlcmntDtls}")
	String getResourcePlcmntDtlsSql;

	@Value("${PcaDaoImpl.getSupervisorIdPerson}")
	String getSupervisorIdPersonSql;

	@Value("${PcaDaoImpl.getPlcmntEvent}")
	String getPlcmntEventSql;

	@Value("${PcaDaoImpl.getPlcmntDtls}")
	String getPlcmntDtlsSql;

	@Autowired
	private SessionFactory sessionFactory;

	public static final Logger logger = Logger.getLogger(PcaDaoImpl.class);

	/**
	 * Method Description: This method is used to retrieve the information for
	 * stage and caps case table by passing idStage as input request. Dam Name:
	 * CSEC02D
	 * 
	 * @param pcaApplicationReq
	 * @return PcaApplicationRes
	 */
	@Override
	public StageCaseDtlDto getStageAndCaseDtls(Long idStage) {

		StageCaseDtlDto stageCaseDtlDto = new StageCaseDtlDto();
		stageCaseDtlDto = (StageCaseDtlDto) ((SQLQuery) sessionFactory.getCurrentSession()
				.createSQLQuery(getStageCaseDtlsSql).setParameter("idStage", idStage))
						.addScalar("idStage", StandardBasicTypes.LONG)
						.addScalar("dtLastUpdate", StandardBasicTypes.DATE)
						.addScalar("currentDate", StandardBasicTypes.DATE)
						.addScalar("cdStageType", StandardBasicTypes.STRING)
						.addScalar("idUnit", StandardBasicTypes.LONG).addScalar("idCase", StandardBasicTypes.LONG)
						.addScalar("dtStageClose", StandardBasicTypes.DATE)
						.addScalar("cdStageClassification", StandardBasicTypes.STRING)
						.addScalar("cdStageCurrPriority", StandardBasicTypes.STRING)
						.addScalar("cdStageInitialPriority", StandardBasicTypes.STRING)
						.addScalar("cdStageRsnPriorityChgd", StandardBasicTypes.STRING)
						.addScalar("cdStageReasonClosed", StandardBasicTypes.STRING)
						.addScalar("indStageClose", StandardBasicTypes.STRING)
						.addScalar("txtStagePriorityCmnts", StandardBasicTypes.STRING)
						.addScalar("cdStageCnty", StandardBasicTypes.STRING)
						.addScalar("nmStage", StandardBasicTypes.STRING)
						.addScalar("cdStageRegion", StandardBasicTypes.STRING)
						.addScalar("dtStageStart", StandardBasicTypes.DATE)
						.addScalar("idSituation", StandardBasicTypes.LONG)
						.addScalar("cdStageProgram", StandardBasicTypes.STRING)
						.addScalar("cdStage", StandardBasicTypes.STRING)
						.addScalar("txtStageClosureCmnts", StandardBasicTypes.STRING)
						.addScalar("cdCaseProgram", StandardBasicTypes.STRING)
						.addScalar("cdCaseCounty", StandardBasicTypes.STRING)
						.addScalar("cdCaseSpecialHandling", StandardBasicTypes.STRING)
						.addScalar("indCaseWorkerSafety", StandardBasicTypes.STRING)
						.addScalar("txtCaseWorkerSafety", StandardBasicTypes.STRING)
						.addScalar("txtCaseSensitiveCmnts", StandardBasicTypes.STRING)
						.addScalar("indCaseSensitive", StandardBasicTypes.STRING)
						.addScalar("indCaseArchived", StandardBasicTypes.STRING)
						.addScalar("dtCaseClosed", StandardBasicTypes.DATE)
						.addScalar("cdCaseRegion", StandardBasicTypes.STRING)
						.addScalar("dtCaseOpened", StandardBasicTypes.DATE)
						.addScalar("nmCase", StandardBasicTypes.STRING)
						.setResultTransformer(Transformers.aliasToBean(StageCaseDtlDto.class)).uniqueResult();
		return stageCaseDtlDto;
	}

	/**
	 * Method Description: This method is used to retrieve the information for
	 * Caps_Resource and Placement table by passing idPersonPlcmtChild as input
	 * request. Dam Name: CSES28D
	 * 
	 * @param idPersonPlcmtChild
	 * @return ResourcePlcmntDto
	 */
	@Override
	public ResourcePlcmntDto getResourcePlcmntDtls(Long idPersonPlcmtChild) {
		ResourcePlcmntDto resourcePlcmntDto = new ResourcePlcmntDto();
		resourcePlcmntDto = (ResourcePlcmntDto) ((SQLQuery) sessionFactory.getCurrentSession()
				.createSQLQuery(getResourcePlcmntDtlsSql).setParameter("idPersonPlcmtChild", idPersonPlcmtChild))
						.addScalar("idPlcmtEvent", StandardBasicTypes.LONG)
						.addScalar("dtLastUpdate", StandardBasicTypes.DATE)
						.addScalar("idPersonPlcmtAdult", StandardBasicTypes.LONG)
						.addScalar("idPersonPlcmtChild", StandardBasicTypes.LONG)
						.addScalar("idRsrcAgency", StandardBasicTypes.LONG)
						.addScalar("idRsrcFacil", StandardBasicTypes.LONG)
						.addScalar("addrPlcmtCity", StandardBasicTypes.STRING)
						.addScalar("addrPlcmtCnty", StandardBasicTypes.STRING)
						.addScalar("addrPlcmtLn1", StandardBasicTypes.STRING)
						.addScalar("addrPlcmtLn2", StandardBasicTypes.STRING)
						.addScalar("addrPlcmtSt", StandardBasicTypes.STRING)
						.addScalar("addrPlcmtZip", StandardBasicTypes.STRING)
						.addScalar("cdPlcmtInfo1", StandardBasicTypes.STRING)
						.addScalar("cdPlcmtInfo2", StandardBasicTypes.STRING)
						.addScalar("cdPlcmtInfo3", StandardBasicTypes.STRING)
						.addScalar("cdPlcmtInfo4", StandardBasicTypes.STRING)
						.addScalar("cdPlcmtInfo5", StandardBasicTypes.STRING)
						.addScalar("cdPlcmtInfo6", StandardBasicTypes.STRING)
						.addScalar("cdPlcmtInfo7", StandardBasicTypes.STRING)
						.addScalar("cdPlcmtLivArr", StandardBasicTypes.STRING)
						.addScalar("cdPlcmtRemovalRsn", StandardBasicTypes.STRING)
						.addScalar("cdPlcmtActPlanned", StandardBasicTypes.STRING)
						.addScalar("cdPlcmtType", StandardBasicTypes.STRING)
						.addScalar("dtPlcmtCaregvrDiscuss", StandardBasicTypes.DATE)
						.addScalar("dtPlcmtChildDiscuss", StandardBasicTypes.DATE)
						.addScalar("dtPlcmtChildPlan", StandardBasicTypes.DATE)
						.addScalar("dtPlcmtEducLog", StandardBasicTypes.DATE)
						.addScalar("dtPlcmtEnd", StandardBasicTypes.DATE)
						.addScalar("dtPlcmtMeddevHistory", StandardBasicTypes.DATE)
						.addScalar("dtPlcmtParentsNotif", StandardBasicTypes.DATE)
						.addScalar("dtPlcmtPreplaceVisit", StandardBasicTypes.DATE)
						.addScalar("dtPlcmtSchoolRecords", StandardBasicTypes.DATE)
						.addScalar("dtPlcmtStart", StandardBasicTypes.DATE)
						.addScalar("indPlcmtContCntct", StandardBasicTypes.CHARACTER)
						.addScalar("indPlcmtEducLog", StandardBasicTypes.CHARACTER)
						.addScalar("indPlcmtEmerg", StandardBasicTypes.CHARACTER)
						.addScalar("indPlcmtNotApplic", StandardBasicTypes.CHARACTER)
						.addScalar("indPlcmtSchoolDoc", StandardBasicTypes.CHARACTER)
						.addScalar("indPlcmtWriteHistory", StandardBasicTypes.CHARACTER)
						.addScalar("nbrPlcmtPhoneExt", StandardBasicTypes.STRING)
						.addScalar("nbrPlcmtTelephone", StandardBasicTypes.STRING)
						.addScalar("nmPlcmtAgency", StandardBasicTypes.STRING)
						.addScalar("nmPlcmtContact", StandardBasicTypes.STRING)
						.addScalar("nmPlcmtFacil", StandardBasicTypes.STRING)
						.addScalar("nmPlcmtPersonFull", StandardBasicTypes.STRING)
						.addScalar("txtPlcmtAddrComment", StandardBasicTypes.STRING)
						.addScalar("txtPlcmtDiscussion", StandardBasicTypes.STRING)
						.addScalar("txtPlcmtDocuments", StandardBasicTypes.STRING)
						.addScalar("txtPlcmtRemovalRsn", StandardBasicTypes.STRING)
						.addScalar("idResource", StandardBasicTypes.LONG)
						.addScalar("addrRsrcStLn1", StandardBasicTypes.STRING)
						.addScalar("addrRsrcStLn2", StandardBasicTypes.STRING)
						.addScalar("addrRsrcCity", StandardBasicTypes.STRING)
						.addScalar("cdRsrcState", StandardBasicTypes.STRING)
						.addScalar("addrRsrcZip", StandardBasicTypes.STRING)
						.addScalar("addrRsrcAttn", StandardBasicTypes.STRING)
						.addScalar("cdRsrcCnty", StandardBasicTypes.STRING)
						.addScalar("cdRsrcInvolClosure", StandardBasicTypes.STRING)
						.addScalar("cdRsrcClosureRsn", StandardBasicTypes.STRING)
						.addScalar("cdRsrcCampusType", StandardBasicTypes.STRING)
						.addScalar("cdRsrcCategory", StandardBasicTypes.STRING)
						.addScalar("cdRsrcCertBy", StandardBasicTypes.STRING)
						.addScalar("cdRsrcInvolClosure", StandardBasicTypes.STRING)
						.addScalar("cdRsrcClosureRsn", StandardBasicTypes.STRING)
						.addScalar("cdRsrcCampusType", StandardBasicTypes.STRING)
						.addScalar("cdRsrcCategory", StandardBasicTypes.STRING)
						.addScalar("cdRsrcCertBy", StandardBasicTypes.STRING)
						.addScalar("cdRsrcEthnicity", StandardBasicTypes.STRING)
						.addScalar("cdRsrcFaHomeStatus", StandardBasicTypes.STRING)
						.addScalar("cdRsrcFaHomeType1", StandardBasicTypes.CHARACTER)
						.addScalar("cdRsrcFaHomeType2", StandardBasicTypes.CHARACTER)
						.addScalar("cdRsrcFaHomeType3", StandardBasicTypes.CHARACTER)
						.addScalar("cdRsrcFaHomeType4", StandardBasicTypes.CHARACTER)
						.addScalar("cdRsrcFaHomeType5", StandardBasicTypes.CHARACTER)
						.addScalar("cdRsrcFaHomeType6", StandardBasicTypes.CHARACTER)
						.addScalar("cdRsrcFaHomeType7", StandardBasicTypes.CHARACTER)
						.addScalar("cdRsrcFacilType", StandardBasicTypes.STRING)
						.addScalar("cdRsrcLanguage", StandardBasicTypes.STRING)
						.addScalar("cdRsrcMaintainer", StandardBasicTypes.STRING)
						.addScalar("cdRsrcMaritalStatus", StandardBasicTypes.STRING)
						.addScalar("cdRsrcOperBy", StandardBasicTypes.STRING)
						.addScalar("cdRsrcOwnership", StandardBasicTypes.STRING)
						.addScalar("cdRsrcPayment", StandardBasicTypes.STRING)
						.addScalar("cdRsrcRecmndReopen", StandardBasicTypes.STRING)
						.addScalar("cdRsrcRegion", StandardBasicTypes.STRING)
						.addScalar("cdRsrcReligion", StandardBasicTypes.STRING)
						.addScalar("cdRsrcRespite", StandardBasicTypes.STRING)
						.addScalar("cdRsrcSchDist", StandardBasicTypes.STRING)
						.addScalar("cdRsrcSetting", StandardBasicTypes.STRING)
						.addScalar("cdRsrcSourceInquiry", StandardBasicTypes.STRING)
						.addScalar("cdRsrcStatus", StandardBasicTypes.STRING)
						.addScalar("cdRsrcType", StandardBasicTypes.STRING)
						.addScalar("dtRsrcMarriage", StandardBasicTypes.DATE)
						.addScalar("dtRsrcCert", StandardBasicTypes.DATE)
						.addScalar("dtRsrcClose", StandardBasicTypes.DATE)
						.addScalar("idRsrcFaHomeEvent", StandardBasicTypes.LONG)
						.addScalar("idRsrcFaHomeStage", StandardBasicTypes.LONG)
						.addScalar("indRsrcCareProv", StandardBasicTypes.CHARACTER)
						.addScalar("indRsrcInactive", StandardBasicTypes.CHARACTER)
						.addScalar("indRsrcIndivStudy", StandardBasicTypes.CHARACTER)
						.addScalar("indRsrcNonprs", StandardBasicTypes.CHARACTER)
						.addScalar("indRsrcTransport", StandardBasicTypes.CHARACTER)
						.addScalar("indRsrcWriteHist", StandardBasicTypes.CHARACTER)
						.addScalar("nmRsrcLastUpdate", StandardBasicTypes.STRING)
						.addScalar("nmResource", StandardBasicTypes.STRING)
						.addScalar("nmRsrcContact", StandardBasicTypes.STRING)
						.addScalar("nbrRsrcAnnualIncome", StandardBasicTypes.LONG)
						.addScalar("nbrRsrcCampusNbr", StandardBasicTypes.INTEGER)
						.addScalar("nbrRsrcFacilAcclaim", StandardBasicTypes.INTEGER)
						.addScalar("nbrRsrcFacilCapacity", StandardBasicTypes.SHORT)
						.addScalar("nbrRsrcFmAgeMax", StandardBasicTypes.SHORT)
						.addScalar("nbrRsrcFmAgeMin", StandardBasicTypes.SHORT)
						.addScalar("nbrRsrcMaAgeMax", StandardBasicTypes.SHORT)
						.addScalar("nbrRsrcMaAgeMin", StandardBasicTypes.SHORT)
						.addScalar("nbrRsrcIntChildren", StandardBasicTypes.SHORT)
						.addScalar("nbrRsrcIntFeAgeMax", StandardBasicTypes.SHORT)
						.addScalar("nbrRsrcIntFeAgeMin", StandardBasicTypes.SHORT)
						.addScalar("nbrRsrcIntMaAgeMax", StandardBasicTypes.SHORT)
						.addScalar("nbrRsrcIntMaAgeMin", StandardBasicTypes.SHORT)
						.addScalar("nbrRsrcOpenSlots", StandardBasicTypes.BYTE)
						.addScalar("nbrRsrcPhn", StandardBasicTypes.STRING)
						.addScalar("nbrRsrcPhoneExt", StandardBasicTypes.STRING)
						.addScalar("nbrRsrcVid", StandardBasicTypes.STRING)
						.addScalar("txtRsrcAddrCmnts", StandardBasicTypes.STRING)
						.addScalar("txtRsrcComments", StandardBasicTypes.STRING)
						.setResultTransformer(Transformers.aliasToBean(ResourcePlcmntDto.class)).uniqueResult();
		return resourcePlcmntDto;
	}

	/**
	 * Method Description: This method is used to retrieve placement eventid
	 * from PCA_ELG_APPLICATION table by passing idPerson as input. Dam Name:
	 * CSECE0D
	 * 
	 * @param idPerson
	 * @return List<PcaEligApplicationDto>
	 */
	@SuppressWarnings("unchecked")
	@Override
	public List<PcaEligApplicationDto> getPlcmntEvent(Long idPerson) {

		List<PcaEligApplicationDto> pcaEligApplicationDtoList = (List<PcaEligApplicationDto>) sessionFactory
				.getCurrentSession().createSQLQuery(getPlcmntEventSql)
				.addScalar("dtApplSubmitted", StandardBasicTypes.DATE)
				.addScalar("idPlcmntEvent", StandardBasicTypes.LONG).setParameter("idPerson", idPerson)
				.setResultTransformer(Transformers.aliasToBean(PcaEligApplicationDto.class)).list();
		return pcaEligApplicationDtoList;
	}

	/**
	 * Method Description: This method is used to retrieve placement details
	 * from Placement table by passing input as idPlcmntevent Dam Name:CSES37D
	 * 
	 * @param idPlcmntEvent
	 * @return PlacementDtlDto
	 */
	@Override
	public PlacementDtlDto getPlcmntDtls(Long idPlcmntEvent) {

		PlacementDtlDto placementDtlDto = (PlacementDtlDto) sessionFactory.getCurrentSession()
				.createSQLQuery(getPlcmntDtlsSql).addScalar("idPlcmtEvent", StandardBasicTypes.LONG)
				.addScalar("dtLastUpdate", StandardBasicTypes.DATE)
				.addScalar("idPersonPlcmtAdult", StandardBasicTypes.LONG)
				.addScalar("idPersonPlcmtChild", StandardBasicTypes.LONG)
				.addScalar("idPlcmtContract", StandardBasicTypes.LONG)
				.addScalar("idRsrcAgency", StandardBasicTypes.LONG).addScalar("idRsrcFacil", StandardBasicTypes.LONG)
				.addScalar("addrPlcmtCity", StandardBasicTypes.STRING)
				.addScalar("addrPlcmtCnty", StandardBasicTypes.STRING)
				.addScalar("addrPlcmtLn1", StandardBasicTypes.STRING)
				.addScalar("addrPlcmtLn2", StandardBasicTypes.STRING)
				.addScalar("addrPlcmtSt", StandardBasicTypes.STRING)
				.addScalar("addrPlcmtZip", StandardBasicTypes.STRING)
				.addScalar("cdPlcmtInfo1", StandardBasicTypes.STRING)
				.addScalar("cdPlcmtInfo2", StandardBasicTypes.STRING)
				.addScalar("cdPlcmtInfo3", StandardBasicTypes.STRING)
				.addScalar("cdPlcmtInfo4", StandardBasicTypes.STRING)
				.addScalar("cdPlcmtInfo5", StandardBasicTypes.STRING)
				.addScalar("cdPlcmtInfo6", StandardBasicTypes.STRING)
				.addScalar("cdPlcmtInfo7", StandardBasicTypes.STRING)
				.addScalar("cdPlcmtInfo8", StandardBasicTypes.STRING)
				.addScalar("cdPlcmtInfo9", StandardBasicTypes.STRING)
				.addScalar("cdPlcmtInfo10", StandardBasicTypes.STRING)
				.addScalar("cdPlcmtInfo11", StandardBasicTypes.STRING)
				.addScalar("cdPlcmtInfo12", StandardBasicTypes.STRING)
				.addScalar("cdPlcmtInfo13", StandardBasicTypes.STRING)
				.addScalar("cdPlcmtInfo14", StandardBasicTypes.STRING)
				.addScalar("cdPlcmtInfo15", StandardBasicTypes.STRING)
				.addScalar("cdPlcmtInfo16", StandardBasicTypes.STRING)
				.addScalar("cdPlcmtInfo17", StandardBasicTypes.STRING)
				.addScalar("cdPlcmtInfo18", StandardBasicTypes.STRING)
				.addScalar("cdPlcmtInfo19", StandardBasicTypes.STRING)
				.addScalar("cdPlcmtInfo20", StandardBasicTypes.STRING)
				.addScalar("cdPlcmtLivArr", StandardBasicTypes.STRING)
				.addScalar("cdPlcmtRemovalRsn", StandardBasicTypes.STRING)
				.addScalar("cdPlcmtActPlanned", StandardBasicTypes.STRING)
				.addScalar("cdPlcmtType", StandardBasicTypes.STRING)
				.addScalar("cdPlcmtService", StandardBasicTypes.STRING)
				.addScalar("dtPlcmtCaregvrDiscuss", StandardBasicTypes.DATE)
				.addScalar("dtPlcmtPermEff", StandardBasicTypes.DATE)
				.addScalar("dtPlcmtChildDiscuss", StandardBasicTypes.DATE)
				.addScalar("dtPlcmtChildPlan", StandardBasicTypes.DATE)
				.addScalar("dtPlcmtEducLog", StandardBasicTypes.DATE).addScalar("dtPlcmtEnd", StandardBasicTypes.DATE)
				.addScalar("dtPlcmtMeddevHistory", StandardBasicTypes.DATE)
				.addScalar("dtPlcmtParentsNotif", StandardBasicTypes.DATE)
				.addScalar("dtPlcmtPreplaceVisit", StandardBasicTypes.DATE)
				.addScalar("dtPlcmtSchoolRecords", StandardBasicTypes.DATE)
				.addScalar("dtPlcmtStart", StandardBasicTypes.DATE)
				.addScalar("indPlcmtContCntct", StandardBasicTypes.CHARACTER)
				.addScalar("indPlcmtEducLog", StandardBasicTypes.CHARACTER)
				.addScalar("indPlcmtEmerg", StandardBasicTypes.CHARACTER)
				.addScalar("indPlcmtNotApplic", StandardBasicTypes.CHARACTER)
				.addScalar("indPlcmtSchoolDoc", StandardBasicTypes.CHARACTER)
				.addScalar("indPlcmtWriteHistory", StandardBasicTypes.CHARACTER)
				.addScalar("nbrPlcmtPhoneExt", StandardBasicTypes.STRING)
				.addScalar("nbrPlcmtTelephone", StandardBasicTypes.STRING)
				.addScalar("nmPlcmtAgency", StandardBasicTypes.STRING)
				.addScalar("nmPlcmtContact", StandardBasicTypes.STRING)
				.addScalar("nmPlcmtFacil", StandardBasicTypes.STRING)
				.addScalar("nmPlcmtPersonFull", StandardBasicTypes.STRING)
				.addScalar("txtPlcmtAddrComment", StandardBasicTypes.STRING)
				.addScalar("txtPlcmtDiscussion", StandardBasicTypes.STRING)
				.addScalar("txtPlcmtDocuments", StandardBasicTypes.STRING)
				.addScalar("txtPlcmtRemovalRsn", StandardBasicTypes.STRING)
				.addScalar("cdRmvlRsnSubtype", StandardBasicTypes.STRING)
				.addScalar("idRsrcSscc", StandardBasicTypes.LONG).addScalar("nmPlcmtSscc", StandardBasicTypes.STRING)
				.addScalar("indPlcmtLessThan24Hrs", StandardBasicTypes.CHARACTER)
				.setParameter("idPlcmtEvent", idPlcmntEvent)
				.setResultTransformer(Transformers.aliasToBean(PlacementDtlDto.class)).uniqueResult();
		return placementDtlDto;
	}

	/**
	 * Method Description: This method is used to retrieve an employee's
	 * supervisor name and ID from Person, Unit, Unit_Emp_Link tables by passing
	 * idPerson as input. Dam Name: CCMN60D
	 * 
	 * @param idPerson
	 * @return SupervisorDto
	 */
	@Override
	public SupervisorDto getSupervisorPersonId(Long idPerson) {
		return (SupervisorDto) ((SQLQuery) sessionFactory.getCurrentSession().createSQLQuery(getSupervisorIdPersonSql)
				.setParameter("idPerson", idPerson)).addScalar("idPerson", StandardBasicTypes.LONG)
						.addScalar("nmPersonFull", StandardBasicTypes.STRING)
						.setResultTransformer(Transformers.aliasToBean(SupervisorDto.class)).uniqueResult();
	}
}
