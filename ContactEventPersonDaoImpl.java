package us.tx.state.dfps.service.contacts.daoimpl;

import java.util.List;

import org.apache.commons.collections.CollectionUtils;
import org.hibernate.SessionFactory;
import org.hibernate.transform.Transformers;
import org.hibernate.type.StandardBasicTypes;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Repository;

import us.tx.state.dfps.service.common.ServiceConstants;
import us.tx.state.dfps.service.contacts.dao.ContactEventPersonDao;
import us.tx.state.dfps.xmlstructs.inputstructs.ContactDetailsOutDto;
import us.tx.state.dfps.xmlstructs.outputstructs.StageProgramDto;

/**
 * 
 * service-business- IMPACT PHASE 2 MODERNIZATION Class
 * Description:ContactEventPersonDaoImpl Aug 2, 2018- 6:40:04 PM © 2017 Texas
 * Department of Family and Protective Services
 */
@Repository
public class ContactEventPersonDaoImpl implements ContactEventPersonDao {

	@Autowired
	private SessionFactory sessionFactory;

	@Autowired
	MessageSource messageSource;

	@Value("${Csys11dDaoImpl.getContactDetails}")
	private transient String getContactDetailsSql;

	/**
	 * 
	 * Method Name: getContactDetails Method Description: This DAM is a QUERY
	 * join for the CONTACT, EVENT and PERSON tables. See the SELECT statement
	 * for details. ( It is functionally specific to the Contact Detail window.)
	 * 
	 * @param contactDetailsOutDto
	 * @return StageProgramDto
	 */
	@SuppressWarnings("unchecked")
	@Override
	public StageProgramDto getContactDetails(ContactDetailsOutDto contactDetailsOutDto) {

		List<StageProgramDto> stageProgramDtoList = (List<StageProgramDto>) sessionFactory.getCurrentSession()
				.createSQLQuery(getContactDetailsSql).addScalar("idContact", StandardBasicTypes.LONG)
				.addScalar("nmPerson", StandardBasicTypes.STRING).addScalar("cdContactLoc", StandardBasicTypes.STRING)
				.addScalar("cdContMeth", StandardBasicTypes.STRING).addScalar("cdCntOthr", StandardBasicTypes.STRING)
				.addScalar("cdCntPurp", StandardBasicTypes.STRING).addScalar("cdCntTyp", StandardBasicTypes.STRING)
				.addScalar("dtCntOccr", StandardBasicTypes.DATE).addScalar("tmScrTmCntct", StandardBasicTypes.STRING)
				.addScalar("indCntAttmt", StandardBasicTypes.STRING).addScalar("dtMntlsumBg", StandardBasicTypes.DATE)
				.addScalar("dtCntMntSumEnd", StandardBasicTypes.DATE).addScalar("dtLstUpdate", StandardBasicTypes.DATE)
				.addScalar("dtLstUpdt", StandardBasicTypes.DATE).addScalar("cdRsnScrOut", StandardBasicTypes.STRING)
				.addScalar("indRecCons", StandardBasicTypes.STRING)
				.addScalar("txtKinCareGvr", StandardBasicTypes.STRING)
				.addScalar("cdRsnAmtne", StandardBasicTypes.STRING).addScalar("AmtNeeded", StandardBasicTypes.LONG)
				.addScalar("indSibVst", StandardBasicTypes.STRING).addScalar("cdChildSafty", StandardBasicTypes.STRING)
				.addScalar("cdPendLegActn", StandardBasicTypes.STRING)
				.addScalar("indPrncplIntrview", StandardBasicTypes.STRING)
				.addScalar("cdProfColatrl", StandardBasicTypes.STRING)
				.addScalar("cdAdminstrat", StandardBasicTypes.STRING).addScalar("txtCmmnts", StandardBasicTypes.STRING)
				.addScalar("indAnnced", StandardBasicTypes.STRING).addScalar("indSafPln", StandardBasicTypes.STRING)
				.addScalar("indFamPlnComp", StandardBasicTypes.STRING)
				.addScalar("indSafConResolv", StandardBasicTypes.STRING).addScalar("estCntHr", StandardBasicTypes.LONG)
				.addScalar("estCntMin", StandardBasicTypes.LONG)
				.setParameter("idEvent", contactDetailsOutDto.getUlIdEvent())
				.setResultTransformer(Transformers.aliasToBean(StageProgramDto.class)).list();

		return CollectionUtils.isNotEmpty(stageProgramDtoList) ? stageProgramDtoList.get(ServiceConstants.Zero) : null;

	}
}