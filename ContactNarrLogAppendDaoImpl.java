package us.tx.state.dfps.service.contactnarrlog.daoimpl;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.hibernate.SessionFactory;
import org.hibernate.transform.Transformers;
import org.hibernate.type.StandardBasicTypes;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;

import us.tx.state.dfps.service.contact.dto.ContactNarrGuideDto;
import us.tx.state.dfps.service.contact.dto.ContactNarrLogPerDateDto;
import us.tx.state.dfps.service.contactnarrlog.dao.ContactNarrLogAppendDao;

/**
 * service-business- IMPACT PHASE 2 MODERNIZATION Class
 * Description:ContactNarrLogAppendDaoImpl Feb 14, 2018- 3:11:21 PM © 2017 Texas
 * Department of Family and Protective Services
 */
@Repository
public class ContactNarrLogAppendDaoImpl implements ContactNarrLogAppendDao {

	@Autowired
	SessionFactory sessionFactory;

	@Value("${ContactNarrLogAppendDaoImpl.getContactInfoPerDate}")
	private String getContactInfoPerDate;

	@Value("${ContactNarrLogAppendDaoImpl.getNameContact}")
	private String getNameContactSql;

	@Value("${ContactNarrLogAppendDaoImpl.getContactGuide}")
	private String getContactGuideSql;

	/**
	 * 
	 * Method Name: getContactInfo Method Description: This retrieves rows from
	 * the contact table based upon a date range and an id_stage passed in. Dam
	 * :CLSCD5D
	 * 
	 * @param idStage
	 * @param dtMonthlySummBegin
	 * @param dtMonthlySummEnd
	 * @return ContactNarrLogPerDateDto
	 */

	@SuppressWarnings("unchecked")
	@Override
	public List<ContactNarrLogPerDateDto> getContactInfo(Long idStage, Date dtMonthlySummBegin, Date dtMonthlySummEnd) {

		List<ContactNarrLogPerDateDto> contactNarrLogPerDateDto = null;
		// written in ContactSql.properties file.
		/* if(null == dtMonthlySummEnd) { */
		String pattern = "MM/dd/yyyy";
		SimpleDateFormat simpleDateFormat = new SimpleDateFormat(pattern);

		contactNarrLogPerDateDto = (List<ContactNarrLogPerDateDto>) sessionFactory.getCurrentSession()
				.createSQLQuery(getContactInfoPerDate).addScalar("idEvent", StandardBasicTypes.LONG)
				.addScalar("indNarr", StandardBasicTypes.STRING).addScalar("indAtt", StandardBasicTypes.STRING)
				.addScalar("dtLastUpdate", StandardBasicTypes.DATE)
				.addScalar("idContactWorker", StandardBasicTypes.LONG)
				.addScalar("idContactStage", StandardBasicTypes.LONG)
				.addScalar("dtContactOccurred", StandardBasicTypes.TIMESTAMP)
				.addScalar("dtNextSummaryDue", StandardBasicTypes.DATE)
				.addScalar("cdContactType", StandardBasicTypes.STRING)
				.addScalar("cdContactPurpose", StandardBasicTypes.STRING)
				.addScalar("cdContactLocation", StandardBasicTypes.STRING)
				.addScalar("cdContactMethod", StandardBasicTypes.STRING)
				.addScalar("cdContactOthers", StandardBasicTypes.STRING)
				.addScalar("dtMonthlySummBegin", StandardBasicTypes.DATE)
				.addScalar("dtMonthlySummEnd", StandardBasicTypes.DATE)
				.addScalar("dtContactApprv", StandardBasicTypes.DATE).addScalar("idPerson", StandardBasicTypes.LONG)
				.addScalar("nmNameFirst", StandardBasicTypes.STRING)
				.addScalar("nmNameMiddle", StandardBasicTypes.STRING).addScalar("nmNameLast", StandardBasicTypes.STRING)
				.addScalar("cdNameSuffix", StandardBasicTypes.STRING)
				.addScalar("dtEventCreated", StandardBasicTypes.DATE)
				.addScalar("cdRsnScrout", StandardBasicTypes.STRING).addScalar("indRecCons", StandardBasicTypes.STRING)
				.addScalar("txtKinCaregiver", StandardBasicTypes.STRING)
				.addScalar("cdRsnAmtNE", StandardBasicTypes.STRING).addScalar("idAmtNeeded", StandardBasicTypes.LONG)
				.addScalar("indSiblingVisit", StandardBasicTypes.STRING)
				.addScalar("indAnnounced", StandardBasicTypes.STRING)
				.addScalar("indSafPlanComp", StandardBasicTypes.STRING)
				.addScalar("indFamPlanComp", StandardBasicTypes.STRING)
				.addScalar("indClientTime", StandardBasicTypes.STRING).addScalar("nbrHours", StandardBasicTypes.STRING)
				.addScalar("nbrMins", StandardBasicTypes.STRING).setParameter("idStage", idStage)
				.setParameter("dtFrom", simpleDateFormat.format(dtMonthlySummBegin))
				.setParameter("dtTo", simpleDateFormat.format(dtMonthlySummEnd))
				.setResultTransformer(Transformers.aliasToBean(ContactNarrLogPerDateDto.class)).list();

		return contactNarrLogPerDateDto;

	}

	/**
	 * 
	 * Method Name: getNameContact Method Description: DAM that retrieves the
	 * Names and Others Contacted for all the events in a particular stage Dam:
	 * CLSCD6D
	 * 
	 * @param idStage
	 * @param dtDtMonthlySummBegin
	 * @param dtDtMonthlySummEnd
	 * @return List<ContactNarrLogPerDateDto>
	 */
	@SuppressWarnings("unchecked")
	@Override
	public List<ContactNarrLogPerDateDto> getNameContact(Long idStage, Date dtMonthlySummBegin, Date dtMonthlySummEnd) {
		// written in ContactSql.properties file.
		List<ContactNarrLogPerDateDto> contactNarrLogNameDtoList = new ArrayList<ContactNarrLogPerDateDto>();
		String pattern = "MM/dd/yyyy";
		SimpleDateFormat simpleDateFormat = new SimpleDateFormat(pattern);

		List<ContactNarrLogPerDateDto> contactNarrLogNameDto = sessionFactory.getCurrentSession()
				.createSQLQuery(getNameContactSql).addScalar("idEvent", StandardBasicTypes.LONG)
				.addScalar("nmNameFirst", StandardBasicTypes.STRING).setParameter("idStage", idStage)
				.setParameter("dtFrom", simpleDateFormat.format(dtMonthlySummBegin))
				.setParameter("dtTo", simpleDateFormat.format(dtMonthlySummEnd))
				.setResultTransformer(Transformers.aliasToBean(ContactNarrLogPerDateDto.class)).list();

		if (null != contactNarrLogNameDto) {
			contactNarrLogNameDtoList = contactNarrLogNameDto;
		}

		return contactNarrLogNameDtoList;
	}

	/**
	 * 
	 * Method Name: getContactGuide Method Description:Retrieve contact guide
	 * information for a given case Dam Name: CLSCG7D
	 * 
	 * @param idCase
	 * @param dtDtMonthlySummBegin
	 * @param dtDtMonthlySummEnd
	 * @return List<ContactNarrGuideDto>
	 */

	@SuppressWarnings("unchecked")
	@Override
	public List<ContactNarrGuideDto> getContactGuide(Long idCase, Date dtDtMonthlySummBegin, Date dtDtMonthlySummEnd) {
		List<ContactNarrGuideDto> contactNarrGuideDto = new ArrayList<ContactNarrGuideDto>();
		// written in ContactSql.properties file.
		String pattern = "MM/dd/yyyy";
		SimpleDateFormat simpleDateFormat = new SimpleDateFormat(pattern);

		List<ContactNarrGuideDto> contactNarrGuideDtoList = sessionFactory.getCurrentSession()
				.createSQLQuery(getContactGuideSql).addScalar("idContactGuideNarr", StandardBasicTypes.LONG)
				.addScalar("dtLastUpdate", StandardBasicTypes.DATE).addScalar("idEvent", StandardBasicTypes.LONG)
				.addScalar("idPerson", StandardBasicTypes.LONG).addScalar("idCase", StandardBasicTypes.LONG)
				.addScalar("idStage", StandardBasicTypes.LONG).addScalar("cdType", StandardBasicTypes.STRING)
				.addScalar("cdGuideRole", StandardBasicTypes.STRING).addScalar("nmNameFirst", StandardBasicTypes.STRING)
				.addScalar("nmNameLast", StandardBasicTypes.STRING).addScalar("nmNameMiddle", StandardBasicTypes.STRING)
				.addScalar("cdNameSuffix", StandardBasicTypes.STRING).setParameter("idCase", idCase)
				.setParameter("dtFrom", simpleDateFormat.format(dtDtMonthlySummBegin))
				.setParameter("dtTo", simpleDateFormat.format(dtDtMonthlySummEnd))
				.setResultTransformer(Transformers.aliasToBean(ContactNarrGuideDto.class)).list();

		if (null != contactNarrGuideDtoList) {
			contactNarrGuideDto = contactNarrGuideDtoList;
		}

		return contactNarrGuideDto;
	}

}
