package us.tx.state.dfps.service.disasterplan.daoimpl;

import java.math.BigDecimal;
import java.util.Date;

import org.apache.log4j.Logger;
import org.hibernate.Query;
import org.hibernate.SessionFactory;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;
import org.hibernate.transform.Transformers;
import org.hibernate.type.StandardBasicTypes;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;

import org.springframework.util.ObjectUtils;
import us.tx.state.dfps.common.domain.Stage;
import us.tx.state.dfps.common.dto.GenericCaseInfoDto;
import us.tx.state.dfps.common.dto.PersonAddressDto;
import us.tx.state.dfps.common.dto.WorkerDetailDto;
import us.tx.state.dfps.service.common.ServiceConstants;
import us.tx.state.dfps.service.disasterplan.dao.DisasterPlanDao;
import us.tx.state.dfps.service.exception.DataLayerException;
import us.tx.state.dfps.service.exception.DataNotFoundException;
import us.tx.state.dfps.service.lookup.dao.LookupDao;
import us.tx.state.dfps.service.subcare.dto.ResourceAddressDto;

/**
 * service-business- IMPACT PHASE 2 MODERNIZATION Class
 * Description:DisasterPlanDaoImpl will implemented all operation defined in
 * DisasterPlanDao Interface related DisasterPlan module.. Feb 9, 2018- 2:02:51
 * PM © 2017 Texas Department of Family and Protective Services
 */
@Repository
public class DisasterPlanDaoImpl implements DisasterPlanDao {

	@Autowired
	private SessionFactory sessionFactory;

	@Autowired
	private LookupDao lookUpDao;

	@Value("${DisasterPlanDaoImpl.getWorkerOrSupervisor}")
	private String getWorkerOrSupervisorSql;

	@Value("${DisasterPlanDaoImpl.getWorkerInfoById}")
	private String getWorkerInfoByIdSql;

	@Value("${DisasterPlanDaoImpl.getAddressList}")
	private String getAddressListSql;

	@Value("${DisasterPlanDaoImpl.getPersonAddress}")
	private String getPersonAddressSql;

	@Value("${DisasterPlanDaoImpl.getMostRecentLegalStatus}")
	private String getMostRecentLegalStatusSql;

	@Value("${DisasterPlanDaoImpl.getPrimaryWorkerId}")
	private String getPrimaryWorkerIdSql;

	@Value("${DisasterPlanDaoImpl.getPersonsByIdRsrcFacil}")
	private String getPersonsByIdRsrcFacilSql;

	@Value("${DisasterPlanDaoImpl.deletePersonLinks}")
	private String deletePersonLinksSql;

	@Value("${DisasterPlanDaoImpl.getPersonsByIdChildPlmt}")
	private String getPersonsByIdChildPlmtSql;

	@Value("${DisasterPlanDaoImpl.plcmtSelect}")
	private String plcmtSelectSql;

	private static final Logger LOG = Logger.getLogger(DisasterPlanDaoImpl.class);

	/**
	 * 
	 * Method Name: getGenericCaseInfo (DAm Name : CallCSEC02D) Method
	 * Description:This dam will return the generic case information needed for
	 * all forms.
	 * 
	 * @param idStage
	 * @return
	 */
	@Override
	public GenericCaseInfoDto getGenericCaseInfo(Long idStage) {

		GenericCaseInfoDto genericCaseInfoDto = (GenericCaseInfoDto) sessionFactory.getCurrentSession()
				.createCriteria(Stage.class).createAlias("capsCase", "capsCase").createAlias("situation", "situation")
				.setProjection(Projections.projectionList()
						.add(Projections.property(ServiceConstants.STAGE_STAGEID), ServiceConstants.STAGE_STAGEID)
						.add(Projections.property(ServiceConstants.STAGE_LASTUPDATE), ServiceConstants.STAGE_LASTUPDATE)
						.add(Projections.property(ServiceConstants.STAGE_TYPE), ServiceConstants.STAGE_TYPE)
						.add(Projections.property(ServiceConstants.STAGE_IDUNIT), ServiceConstants.STAGE_IDUNIT)
						.add(Projections.property("capsCase.idCase"), ServiceConstants.STAGE_IDCASE)
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
						.add(Projections.property("txtStagePriorityCmnts"), "txtStagePriorityCmnts")
						.add(Projections.property(ServiceConstants.STAGE_CNTY), ServiceConstants.STAGE_CNTY)
						.add(Projections.property(ServiceConstants.STAGE_NMSTAGE), ServiceConstants.STAGE_NMSTAGE)
						.add(Projections.property(ServiceConstants.STAGE_REGION), ServiceConstants.STAGE_REGION)
						.add(Projections.property(ServiceConstants.STAGE_START), ServiceConstants.STAGE_START)
						.add(Projections.property("situation.idSituation"), "idSituation")
						.add(Projections.property(ServiceConstants.STAGE_CDPROGRAM), ServiceConstants.STAGE_CDPROGRAM)
						.add(Projections.property(ServiceConstants.STAGE_CD), ServiceConstants.STAGE_CD)
						.add(Projections.property("txtStageClosureCmnts"), "txtStageClosureCmnts")
						.add(Projections.property("capsCase.cdCaseProgram"), "cdCaseProgram")
						.add(Projections.property("capsCase.cdCaseCounty"), "cdCaseCounty")
						.add(Projections.property("capsCase.cdCaseSpecialHandling"), "cdCaseSpecialHandling")
						.add(Projections.property("capsCase.indCaseWorkerSafety"), "indCaseWorkerSafety")
						.add(Projections.property("capsCase.txtCaseWorkerSafety"), "txtCaseWorkerSafety")
						.add(Projections.property("capsCase.txtCaseSensitiveCmnts"), "txtCaseSensitiveCmnts")
						.add(Projections.property("capsCase.indCaseSensitive"), "indCaseSensitive")
						.add(Projections.property("capsCase.indCaseArchived"), "indCaseArchived")
						.add(Projections.property("capsCase.dtCaseClosed"), "dtCaseClosed")
						.add(Projections.property("capsCase.cdCaseRegion"), "cdCaseRegion")
						.add(Projections.property("capsCase.dtCaseOpened"), "dtCaseOpened")
						.add(Projections.property("capsCase.nmCase"), "nmCase")
						.add(Projections.property("capsCase.nmCase"), "nmCase"))
				.add(Restrictions.eq(ServiceConstants.STAGE_STAGEID, idStage))
				.setResultTransformer(Transformers.aliasToBean(GenericCaseInfoDto.class)).uniqueResult();
		genericCaseInfoDto.setDtSysDtGenericSysdate(lookUpDao.getCurrentDate());
		return genericCaseInfoDto;
	}

	/**
	 * 
	 * Method Name: getPrimaryWorkerOrSupervisor (DAm Name : CallCLSCB1D) Method
	 * Description:This dam will retrieve the primary worker supervisor
	 * associated with the stage
	 * 
	 * @param idCase
	 * @return Long
	 */
	@Override
	public Long getPrimaryWorkerOrSupervisor(Long idCase) {
		Query query = sessionFactory.getCurrentSession().createSQLQuery(getWorkerOrSupervisorSql)
				.setParameter("idCase", idCase).setParameter("cdStagePersRole", ServiceConstants.PRIMARY_WORKER);

		BigDecimal result = (BigDecimal) query.uniqueResult();

		return ObjectUtils.isEmpty(result) ? null : Long.valueOf(result.longValue());
	}

	/**
	 * 
	 * Method Name: getWorkerInfoById (DAm Name : CallCSEC01D) Method
	 * Description:This dam will retrieve all worker info based upon an Id
	 * Person
	 * 
	 * @param idPerson
	 * @return WorkerDetailDto
	 */

	@Override
	public WorkerDetailDto getWorkerInfoById(Long idPerson) {
		Query query = sessionFactory.getCurrentSession().createSQLQuery(getWorkerInfoByIdSql)
				.addScalar("idPerson", StandardBasicTypes.LONG).addScalar("nbrEmpActivePct", StandardBasicTypes.SHORT)
				.addScalar("dtEmpHire", StandardBasicTypes.DATE).addScalar("idEmpJobHistory", StandardBasicTypes.LONG)
				.addScalar("idEmployeeLogon", StandardBasicTypes.STRING)
				.addScalar("cdEmployeeClass", StandardBasicTypes.STRING)
				.addScalar("txtEmployeeClass", StandardBasicTypes.STRING)
				.addScalar("cdEmpSecurityClassNm", StandardBasicTypes.STRING)
				.addScalar("idEmpOffice", StandardBasicTypes.LONG)
				.addScalar("dtEmpLastAssigned", StandardBasicTypes.DATE)
				.addScalar("cdEmpProgram", StandardBasicTypes.STRING)
				.addScalar("indEmpConfirmedHrmis", StandardBasicTypes.STRING)
				.addScalar("indEmpPendingHrmis", StandardBasicTypes.STRING)
				.addScalar("indEmpActiveStatus", StandardBasicTypes.STRING)
				.addScalar("dtEmpTermination", StandardBasicTypes.DATE)
				.addScalar("idJobPersSupv", StandardBasicTypes.LONG).addScalar("cdJobClass", StandardBasicTypes.STRING)
				.addScalar("txtJobDescr", StandardBasicTypes.STRING)
				.addScalar("indJobAssignable", StandardBasicTypes.STRING)
				.addScalar("cdJobFunction", StandardBasicTypes.STRING).addScalar("cdJobBjn", StandardBasicTypes.STRING)
				.addScalar("dtJobEnd", StandardBasicTypes.DATE).addScalar("dtJobStart", StandardBasicTypes.DATE)
				.addScalar("cdOfficeMail", StandardBasicTypes.STRING)
				.addScalar("cdOfficeProgram", StandardBasicTypes.STRING)
				.addScalar("cdOfficeRegion", StandardBasicTypes.STRING)
				.addScalar("nmOfficeName", StandardBasicTypes.STRING)
				.addScalar("nbrMailCodePhone", StandardBasicTypes.STRING)
				.addScalar("nbrMailCodePhoneExt", StandardBasicTypes.STRING)
				.addScalar("addrMailCodeStLn1", StandardBasicTypes.STRING)
				.addScalar("addrMailCodeStLn2", StandardBasicTypes.STRING)
				.addScalar("addrMailCodeCity", StandardBasicTypes.STRING)
				.addScalar("addrMailCodeZip", StandardBasicTypes.STRING)
				.addScalar("addrMailCodeCounty", StandardBasicTypes.STRING)
				.addScalar("indMailCodeInvalid", StandardBasicTypes.STRING)
				.addScalar("idPersonPhone", StandardBasicTypes.LONG)
				.addScalar("txtPersonPhoneComments", StandardBasicTypes.STRING)
				.addScalar("nbrPersonPhoneExtension", StandardBasicTypes.STRING)
				.addScalar("nbrPersonPhone", StandardBasicTypes.STRING)
				.addScalar("dtPersonPhoneStart", StandardBasicTypes.DATE)
				.addScalar("dtPersonPhoneEnd", StandardBasicTypes.DATE)
				.addScalar("indPersonPhoneInvalid", StandardBasicTypes.STRING)
				.addScalar("indPersonPhonePrimary", StandardBasicTypes.STRING)
				.addScalar("cdPersonPhoneType", StandardBasicTypes.STRING).addScalar("idName", StandardBasicTypes.LONG)
				.addScalar("indNameInvalid", StandardBasicTypes.STRING)
				.addScalar("nmNameFirst", StandardBasicTypes.STRING)
				.addScalar("nmNameMiddle", StandardBasicTypes.STRING).addScalar("nmNameLast", StandardBasicTypes.STRING)
				.addScalar("indNamePrimary", StandardBasicTypes.STRING)
				.addScalar("cdNameSuffix", StandardBasicTypes.STRING)
				.addScalar("dtNameStartDate", StandardBasicTypes.DATE)
				.addScalar("dtNameEndDate", StandardBasicTypes.DATE).setParameter("idPerson", idPerson)
				.setResultTransformer(Transformers.aliasToBean(WorkerDetailDto.class));
		WorkerDetailDto workerDetailDto = (WorkerDetailDto) query.uniqueResult();

		return workerDetailDto;
	}

	/**
	 * 
	 * Method Name: getResourceAddress (DAm Name : CallCRES0AD) Method
	 * Description:This DAM retrieves all of the address data given a resource
	 * id and address type
	 * 
	 * @param idResource
	 * @return ResourceAddressDto
	 */
	@Override
	public ResourceAddressDto getResourceAddress(Long idResource) {

		String cdRsrcAddrType = ServiceConstants.PRIMARY_ADDRESS_TYPE;
		Query query = sessionFactory.getCurrentSession().createSQLQuery(getAddressListSql)
				.addScalar("idRsrcAddress", StandardBasicTypes.LONG).addScalar("idResource", StandardBasicTypes.LONG)
				.addScalar("dtLastUpdate", StandardBasicTypes.DATE)
				.addScalar("addrRsrcAddrZip", StandardBasicTypes.STRING)
				.addScalar("cdRsrcAddrCounty", StandardBasicTypes.STRING)
				.addScalar("addrRsrcAddrAttn", StandardBasicTypes.STRING)
				.addScalar("cdRsrcAddrState", StandardBasicTypes.STRING)
				.addScalar("addrRsrcAddrStLn1", StandardBasicTypes.STRING)
				.addScalar("addrRsrcAddrStLn2", StandardBasicTypes.STRING)
				.addScalar("cdRsrcAddrSchDist", StandardBasicTypes.STRING)
				.addScalar("cdRsrcAddrType", StandardBasicTypes.STRING)
				.addScalar("txtRsrcAddrComments", StandardBasicTypes.STRING)
				.addScalar("addrRsrcAddrCity", StandardBasicTypes.STRING)
				.addScalar("nbrRsrcAddrVid", StandardBasicTypes.STRING)
				.addScalar("nmResource", StandardBasicTypes.STRING).setParameter("idResource", idResource)
				.setParameter("cdRsrcAddrType", cdRsrcAddrType)
				.setResultTransformer(Transformers.aliasToBean(ResourceAddressDto.class));

		return (ResourceAddressDto) query.uniqueResult();

	}

	/**
	 * 
	 * Method Name: getWorkerInfoById (DAm Name : CallCSEC34D) Method
	 * Description: Dam will retrieve an address of a specified type(CD PERS
	 * ADDR LINK TYPE) from the Person Address table
	 * 
	 * @param idPerson
	 * @return
	 */
	@Override
	public PersonAddressDto getPersonAddress(Long idPerson, String cdPersAddrLinkType, Date dtScrDtCurrentDate) {

		PersonAddressDto personDto = null;

		Query query = sessionFactory.getCurrentSession().createSQLQuery(getPersonAddressSql)
				.addScalar("idPersonAddr", StandardBasicTypes.LONG).addScalar("dtLastUpdate", StandardBasicTypes.DATE)
				.addScalar("addrPersonAddrZip", StandardBasicTypes.STRING)
				.addScalar("cdPersonAddrState", StandardBasicTypes.STRING)
				.addScalar("nbrPersonAddrHash", StandardBasicTypes.INTEGER)
				.addScalar("addrPersonAddrCity", StandardBasicTypes.STRING)
				.addScalar("addrPersonAddrAttn", StandardBasicTypes.STRING)
				.addScalar("addrPersAddrStLn1", StandardBasicTypes.STRING)
				.addScalar("addrPersAddrStLn2", StandardBasicTypes.STRING)
				.addScalar("cdPersonAddrCounty", StandardBasicTypes.STRING)
				.addScalar("idAddrPersonLink", StandardBasicTypes.LONG).addScalar("idPerson", StandardBasicTypes.LONG)
				.addScalar("cdPersAddrLinkType", StandardBasicTypes.STRING)
				.addScalar("indPersAddrLinkInvalid", StandardBasicTypes.STRING)
				.addScalar("indPersAddrLinkPrimary", StandardBasicTypes.STRING)
				.addScalar("txtPersAddrCmnts", StandardBasicTypes.STRING)
				.addScalar("dtPersAddrLinkStart", StandardBasicTypes.DATE)
				.setParameter("cdPersAddrLinkType", cdPersAddrLinkType).setParameter("idPerson", idPerson)
				.setParameter("indPersAddrLinkInvalid", ServiceConstants.Character_IND_N)
				.setParameter("dtScrDtCurrentDate", dtScrDtCurrentDate)
				.setResultTransformer(Transformers.aliasToBean(PersonAddressDto.class));

		try {
			personDto = (PersonAddressDto) query.uniqueResult();
		} catch (DataNotFoundException | DataLayerException e) {
			LOG.error("PersonAddress data not found");
		}

		return personDto;
	}

}
