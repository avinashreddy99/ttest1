<%@  taglib prefix="form" uri="http://www.springframework.org/tags/form"%>
<%@  taglib prefix="spring" uri="http://www.springframework.org/tags"%>
<%@  taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt"%>
<%@ taglib uri="http://www.springframework.org/tags" prefix="spring"%>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions"%>
<%@ page import="us.tx.state.dfps.common.web.MessagesConstants"%>
<%@ page import="us.tx.state.dfps.common.web.WebConstants"%>
<%@ page import="us.tx.state.dfps.common.web.CodesConstant"%>
<%@ page import="us.tx.state.dfps.service.person.dto.RecordsCheckDto" %>
<%@ page import="org.apache.bcel.util.InstructionFinder" %>


<%-- ***** Change History ****** --%>
<%-- 04/12/2021 nairl  artf179492 : The "Criminal History Results and Access section" questions are not in same order for Legacy IMPACT and IMPACT 2.0 --%>
<%-- 05/09/2022 thompswa artf205247 : Records Check Notifications : add sendSecureNotif checkbox --%>
<%-- 05/26/2023 rayanv artf245970 :New Subscription to Rap Back section --%>
<head>
	<title><spring:message code="recordsCheckDetail.title.label" /></title>
</head>
<main id="mainContent" tabindex="0">
	<div id="mainDiv" class="container-fluid bodycolor">
		<div class="row leftPadFirstLevel">
			<h1 aria-level="1">
				<spring:message code="recordsCheckDetail.page.label" />
			</h1>
			<hr>
		</div>
		<%--@elvariable id="recordsCheckDto" type=""--%>
		<form:form
				action="${pageContext.request.contextPath}/case/person/record/recordAction"
				id="recordsCheckDetail" method="post" modelAttribute="recordsCheckDto">

			<%--Initial Constant Variable --%>
			<c:set var="webView" value="<%=WebConstants.VIEW%>"></c:set>
			<c:set var="webModify" value="<%=WebConstants.MODIFY_PAGE_MODE%>"></c:set>
			<c:set var="webNew" value="<%=WebConstants.STATUS_NEW%>"></c:set>
			<c:set var="webYes" value="<%=WebConstants.YES%>"></c:set>

			<c:set var="codeCheckType" value="<%=CodesConstant.CCHKTYPE%>"></c:set>
			<c:set var="codeCheckType10" value="<%=CodesConstant.CCHKTYPE_10%>"></c:set>
			<c:set var="codeCheckType80" value="<%=CodesConstant.CCHKTYPE_80%>"></c:set>
			<c:set var="codeCheckType75" value="<%=CodesConstant.CCHKTYPE_75%>"></c:set>
			<c:set var="codeResent" value="<%=CodesConstant.CNOTSTAT_RESENT%>"></c:set>
			<c:set var="codeDrft" value="<%=CodesConstant.CNOTSTAT_DRFT%>"></c:set>
			<c:set var="codeDetermCler" value="<%=CodesConstant.CDETERM_CLER%>"></c:set>
			<c:set var="codeDeterm" value="<%=CodesConstant.CDETERM%>"></c:set>
			<c:set var="codeNoticeStatus" value="<%=CodesConstant.CNOTSTAT%>"></c:set>
			<c:set var="codeNotifyType" value="<%=CodesConstant.NOTIFTYP%>"></c:set>
			<c:set var="codeCheckType81" value="<%=CodesConstant.CCHKTYPE_81%>"></c:set>
			<c:set var="rapSub" value="<%=WebConstants.SUBSCRIBED%>"></c:set>
			<c:set var="rapUnSub" value="<%=WebConstants.UN_SUBSCRIBED%>"></c:set>
			<c:set var="PG" value="<%=CodesConstant.CCRIMSTA_PG%>"></c:set>



			<form:input type="hidden" id="buttonClicked" path="buttonClicked"
						value="" />
			<input type="hidden" id="flagClearTypeId" name="flagClearType"
				   value="${flagClearType}">
			<input type="hidden" aria-label="requestContext" id="requestContext"
				   value="${pageContext.request.contextPath}">
			<input type="hidden" id="txtDpsSIDForOrignFpChck"
				   value="${recordsCheckDto.txtDpsSIDForOrignFpChck}">
		    <input type="hidden" id="showEligibleEmailButton"
            				   value="${recordsCheckDto.showEligibleEmailButton}">
            <input type="hidden" id="showIneligibleEmailButton"
                               value="${recordsCheckDto.showIneligibleEmailButton}">
            <input type="hidden" id="showEligibleEmailButton"
                        	    value="${recordsCheckDto.showResendEligibleEmailButton}">
            <input type="hidden" id="showIneligibleEmailButton"
                                value="${recordsCheckDto.showResendIneligibleEmailButton}">



			<c:set var="error">
				<form:errors path="*" />
			</c:set>
			<c:set var="index" value="${index}"></c:set>
			<input hidden="true" value="${index}" id="recordsCheckIndex" aria-label="record check index"/>
			<c:if
					test="${fn:length(errorList) eq 0 and  empty error and (not empty fn:trim(recordsCheckDto.informationMessage) or not empty fn:trim(errorOpeningDocument)) }">
				<div class="row alert alert-info alert-dismissible" id="infoDiv"
					 role="alert" aria-hidden="false">
					<div class="col-xs-12">
						<h2 id="infoMessageHeader">
							<spring:message code="common.label.attention" />
						</h2>
					</div>
					<div class="col-xs-12 leftPadSecondLevel">
						<ul>
							<c:if
									test="${not empty fn:trim(recordsCheckDto.informationMessage)}">
								<li><Strong>${recordsCheckDto.informationMessage}</Strong></li>
							</c:if>
							<c:if test="${not empty fn:trim(errorOpeningDocument) }">
								<li><Strong>${errorOpeningDocument}</Strong></li>
							</c:if>
						</ul>
					</div>
				</div>
			</c:if>


			<c:set var="sidmsgnumber" value="<%=MessagesConstants.SID_MISMATCH_NOTIF%>"></c:set>
			<input type="hidden" name="sidConfirmMsg" id="sidConfirmMsg" value="${cacheAdapter.getMessage(sidmsgnumber)}"/>

			<%-- Display error messsage start --%>

			<%--Rec Check Type Error variable  --%>
			<c:set var="msgnumber" value="<%=MessagesConstants.MSG_CONFIRM_ON_DELETE%>"></c:set>

			<input hidden="true" id="confirmMsg"
				   data-message="${cacheAdapter.getMessage(msgnumber)}"
				   aria-label="Confirm Msg" />
			<div class="row leftPadSecondLevel rowPadCustom01">
				<div class="col-sm-6 noLeftPad">
					<div class="row">
						<p
								class="boldBody col-xs-5 col-sm-5 col-md-4 col-lg-3 noMarginBottom">
							<spring:message code="recordsCheckDetail.name.label" />
						</p>
						<p
								class="bodyText col-xs-7 col-sm-7 col-md-8 col-lg-9 noMarginBottom">${nmPersonFull}</p>
					</div>
					<div class="row rowPadCustom01">
						<p
								class="boldBody col-xs-5 col-md-4 col-lg-3 col-sm-5 noRightPad noMarginBottom">
							<spring:message code="recordsCheckDetail.personId.label" />
						</p>
						<p
								class="bodyText col-xs-7 col-md-8 col-lg-9 col-sm-7 noMarginBottom">${personId}</p>
					</div>
				</div>
				<div class="col-sm-6 alignRight noRightPad rowPadCustom01767">
					<p class="bodyText reqFieldDisplay noMarginBottom">required
						field</p>
					<p class="bodyText conReqDisplay rowPadCustom01 noMarginBottom"><spring:message code="common.label.conditionally.required"/></p>
				</div>
			</div>

			<c:set var="recCheckCheckTypeError">
				<form:errors path="recCheckCheckType" />
			</c:set>

			<%-- Complete Date Error variable --%>
			<c:set var="recCheckCompletedDateError">
				<form:errors path="dtRecCheckCompleted" />
			</c:set>
			<%-- Request Date Error variable  --%>
			<c:set var="recCheckRequestDateError">
				<form:errors path="dtRecCheckRequest" />
			</c:set>
			<%-- Rec Check Determination Error variable --%>
			<c:set var="recChkDetermError">
				<form:errors path="recChkDeterm" />
			</c:set>
			<div class="row alert alert-danger" id="errordiv" role="alert"
				 hidden="true" aria-hidden="true" tabindex="0">
				<div class="col-xs-12">
					<h2 class="errorText" id="errorMessageHeader">Error Message Header</h2>
				</div>
				<div class="col-xs-12 noLeftPad leftPadFirstLevel" hidden="true"
					 aria-hidden="true">
					<p class="sensitiveInd noMarginBottom" id="errorMessageInfo"></p>
				</div>
				<div class="col-xs-12 leftPadSecondLevel">
					<ul id="errorMessageList">
						<li><form:errors path="*" /></li>
					</ul>
				</div>
			</div>
			<c:if test="${fn:length(errorList) gt 0 || not empty fn:trim(error)}">
				<div class="row alert alert-danger" id="infoDiv" role="alert"
					 tabindex="0">
					<div class="col-xs-12">
						<h2 class="errorText" id="errorMessageHeader">Your information
							contains ${errorCount} error(s)</h2>
					</div>
					<div class="col-xs-12 noLeftPad leftPadFirstLevel" hidden="true"
						 aria-hidden="true">
						<p class="sensitiveInd noMarginBottom" id="errorMessageInfo"></p>
					</div>
					<div class="col-xs-12 leftPadSecondLevel">
						<ul id="errorMessageList">
							<c:forEach var="errorMsg" items="${errorList}">
								<li><span>&nbsp;&nbsp;${errorMsg}</span></li>
							</c:forEach>
							<c:if test="${not empty recCheckCheckTypeError}">
								<li><span><strong>&nbsp;&nbsp;<a
										href="#searchType">Search Type: </a></strong> -
										${recCheckCheckTypeError}</span></li>
							</c:if>
							<c:if test="${not empty recCheckRequestDateError}">
								<li><span><strong>&nbsp;&nbsp;<a
										href="#dateReq">Date of Request: </a></strong> -
										${recCheckRequestDateError}</span></li>
							</c:if>
							<c:if test="${not empty recCheckCompletedDateError}">
								<li><span><strong>&nbsp;&nbsp;<a
										href="#dateComp">Date Completed: </a></strong> -
										${recCheckCompletedDateError}</span></li>
							</c:if>
							<c:if test="${not empty recChkDetermError }">
								<li><span><strong>&nbsp;&nbsp;<a
										href="#determination">Determination: </a></strong> ${recChkDetermError }</span></li>
							</c:if>
						</ul>
					</div>
				</div>
			</c:if>
			<div class="row leftPadSecondLevel">
				<h2 aria-level="2">
					<spring:message code="recordsCheckDetail.section.label" />
				</h2>

			</div>

			<%-- Hidden value --%>

			<form:input hidden="true"
						aria-label="recordsCheckDeterminationListStr"
						path="recordsCheckDeterminationListStr" />
			<form:input hidden="true" aria-label="determinationListStr"
						path="determinationListStr" />
			<form:input hidden="true" aria-label="cancelReasonListStr"
						path="cancelReasonListStr" />
			<form:input hidden="true" aria-label="checkTypeListStr"
						path="checkTypeListStr" />
			<form:input hidden="true" aria-label="phoneListStr"
						path="phoneListStr" />
			<form:input hidden="true" aria-label="emailListStr"
						path="emailListStr" />
			<form:input hidden="true" aria-label="showResultsButton"
						path="showResultsButton" />
			<form:input hidden="true" aria-label="showRadioButton"
						path="showRadioButton" />
			<form:input hidden="true" aria-label="lastUpdate" path="dtLastUpdate" />
			<form:input hidden="true" aria-label="lastUpdate"
						path="dtLastUpdateStr" />
			<form:input hidden="true" aria-label="cdAbcsExtrnlType"
						path="cdAbcsExtrnlType" />
			<form:input hidden="true" aria-label="cdAbcsExtrnlType"
						path="cdAbcsExtrnlType" />
			<form:input hidden="true" aria-label="caseId" path="idCase" />
			<form:input id="recCheckId" hidden="true" aria-label="idRecCheck"
						path="idRecCheck" />
			<form:input hidden="true" aria-label="idStage" path="idStage" />
			<form:input hidden="true" aria-label="idRecCheckRequestor"
						path="idRecCheckRequestor" />
			<form:input hidden="true" aria-label="recCheckEmpType"
						path="recCheckEmpType" />
			<form:input hidden="true" aria-label="recCheckStatus"
						path="recCheckStatus" />
			<form:input hidden="true" aria-label="indClearedEmail"
						path="indClearedEmail" />
			<form:input hidden="true" aria-label="clearedEmailSentDate"
						path="dtClearedEmailSent" />
			<form:input hidden="true" aria-label="indOutstandingCHAcpRej"
						path="indOutstandingCHAcpRej" />
			<form:input hidden="true" aria-label="setbShowUploadedDocuments"
						path="showUploadedDocuments" />
			<form:input hidden="true" aria-label="bShowUploadedDocuments"
						path="showNotifications" />
			<form:input hidden="true" aria-label="determFinalDate"
						path="dtDetermFinal" />
			<form:input hidden="true" aria-label="hasEmailSentInd"
						path="indEmailSent" />
			<%-- Update determination field --%>
			<c:if test="${recordsCheckDto.selectDetermin}">
            			<form:input hidden="true" aria-label="recChkDeterm"
            				path="recChkDeterm" />
            		</c:if>
			<form:input hidden="true" aria-label="indAccptRej" path="indAccptRej" />
			<form:input hidden="true" aria-label="indManualCheck"
						path="indManualCheck" />
			<form:input hidden="true" aria-label="rowCanBeDeleted"
						path="rowCanBeDeleted" />
			<form:input hidden="true" aria-label="dispalyResults"
						path="dispalyResults" />
			<form:input hidden="true"
						aria-label="displayTickMarkBIndOutsndCHAcpRej"
						path="displayTickMarkBIndOutsndCHAcpRej" />
			<form:input hidden="true" aria-label="pdbBackgroundCheckId"
						path="idPdbBackgroundCheck" />
			<form:input hidden="true" aria-label="bDisableDate" path="disableDate" />
			<form:input hidden="true" aria-label="bDeletable" path="deletable" />
			<form:input hidden="true" aria-label="bResultsDisabled"
						path="resultsDisabled" />
			<form:input hidden="true" aria-label="bDisableType"
						path="indDisableType" />
			<form:input hidden="true" aria-label="bEBCNarrativeDisabled"
						path="indEBCNarrativeDisabled" />
			<form:input hidden="true" aria-label="narrModeView"
						path="narrModeView" />
			<form:input hidden="true" aria-label="bDisableCompletedDate"
						path="disableCompletedDate" />
			<form:input hidden="true" aria-label="bDisableCompletedCkbox"
						path="disableCompletedCkbox" />
			<form:input hidden="true" aria-label="bShowEmailButton"
						path="showEmailButton" />
			<form:input hidden="true" aria-label="bShowCompletedCkbox"
						path="showCompletedCkbox" />
			<form:input hidden="true" aria-label="bCompletedCkboxChkd"
						path="completedCkboxChkd" />
			<form:input hidden="true" aria-label="bIncompleteBatchCk"
						path="incompleteBatchCk" />
			<form:input hidden="true" aria-label="bShowEmailDate"
						path="showEmailDate" />
			<form:input hidden="true" aria-label="nmRequestedBy"
						path="nmRequestedBy" />
			<form:input hidden="true" aria-label="bSelectDetermin"
						path="selectDetermin" />

			<form:input hidden="true" aria-label="bHistorySection"
						path="historySection" />
			<form:input hidden="true" aria-label="bReviewNowLater"
						path="reviewNowLater" />
			<form:input hidden="true" aria-label="excludePossibleMatchFromDeterm"
						path="excludePossibleMatchFromDeterm" />
			<form:input hidden="true" aria-label="bShowSendResenButton"
						path="showSendResenButton" />
			<form:input hidden="true" aria-label="bDisableemailPhone"
						path="disableemailPhone" />
			<form:input hidden="true" aria-label="informationMessage"
						path="informationMessage" />
			<form:input hidden="true" aria-label="indicator" path="indicator" />
			<form:input hidden="true" aria-label="recCheckDpsIncomplete"
						path="recCheckDpsIncomplete" />
			<form:input hidden="true" aria-label="personType" path="personType" />
			<form:input hidden="true" aria-label="isABCSCheck" path="abcsCheck" />
			<form:input hidden="true" aria-label="contractId" path="idContract" />
			<form:input hidden="true" aria-label="contractNumber"
						path="contractNumber" />
			<form:input hidden="true" aria-label="historyCount"
						path="historyCount" />

			<form:input hidden="true" aria-label="personFullName"
						path="personFullName" />
			<form:input hidden="true" aria-label="personFirstName"
						path="personFirstName" />
			<form:input hidden="true" aria-label="personLastName"
						path="personLastName" />
			<form:input hidden="true" aria-label="personSex" path="personSex" />
			<form:input hidden="true" aria-label="ethnicity" path="ethnicity" />
			<form:input hidden="true" aria-label="personDateOfBirth"
						path="personDateOfBirth" />
			<form:input hidden="true" aria-label="personDobApprxInd"
						path="indPersonDobApprx" />
			<form:input hidden="true" aria-label="personAge" path="personAge" />
			<form:input hidden="true" aria-label="pageModeStr" path="pageModeStr" />
			<input id="pageMode" hidden="true" aria-label="pageMode"
				   value="${recordsCheckDto.pageModeStr}" />
			<form:input hidden="true" aria-label="hiddentPersonPhone"
						path="hiddenPersonPhone" value="${nbrPersonPhone}" />
			<form:input hidden="true" aria-label="hiddentPersonPhoneId"
						path="idHiddenPersonPhone" value="${idPersonPhone}" />
			<form:input hidden="true" aria-label="recChkDeterm"
						path="indHiddenPhonePrimary" value="${phonePrimary}" />

			<form:input hidden="true" aria-label="hiddentPersonEmail"
						path="hiddenPersonEmail" value="${fPrintEmail}" />
			<form:input hidden="true" aria-label="hiddentPersonEmailId"
						path="idHiddenPersonEmail" value="${idEmailPerson}" />
			<form:input hidden="true" aria-label="hiddenIndEmailPrimary"
						path="indHiddenEmailPrimary" value="${emailPrimary}" />
			<!-- Added for warranty defect 11804 -->
			<form:input hidden="true" aria-label="lastUpdate"
						path="dtRecCheckCompletedStr" />
			<form:input hidden="true" aria-label="dtRapBackUnSubrReq"
						path="dtRapBackUnSubrReq" />
			<form:input hidden="true" aria-label="indDisableRapBackReview"
						path="indDisableRapBackReview" />
			<form:input hidden="true" aria-label="txtDpsSIDForOrignFpChck"
						path="txtDpsSIDForOrignFpChck" />
			<form:input hidden="true" aria-label="showEligibleEmailButton"
            			path="showEligibleEmailButton" />
            <form:input hidden="true" aria-label="showIneligibleEmailButton"
                        path="showIneligibleEmailButton" />
            <form:input hidden="true" aria-label="showResendIneligibleEmailButton"
                        			path="showResendIneligibleEmailButton" />
                        <form:input hidden="true" aria-label="showResendEligibleEmailButton"
                                    path="showResendEligibleEmailButton" />
			<input id="idUser" hidden="true" aria-label="pageMode" value="${idUser}" />


			<div class="row leftPadSecondLevel">
				<div class="col-xs-12 col-sm-6 col-md-5 col-lg-4">
					<label for="searchType" id="searchTypeLabel"
						   class="col-xs-5 col-sm-4 col-md-5 col-lg-5 noLeftPad reqField navCheckFalse">
						<spring:message code="recordsCheckDetail.searchType.label" />
					</label> <input id="checkTypeId" hidden="true" name="checkType" class="navCheckFalse"
									aria-label="check Type Id"
									value="${recordsCheckDto.recCheckCheckType}">
					<c:if test="${recordsCheckDto.indDisableType}">
						<form:input path="recCheckCheckType" aria-label="recCheckCheckType"
									hidden="true" />
					</c:if>
					<form:select disabled="${recordsCheckDto.indDisableType}"
								 id="searchType" path="recCheckCheckType" aria-required="true"
								 required="required" class="minWidthSelect navCheckFalse">
						<option value=""></option>

						<form:options path="checkTypeList"
									  items="${recordsCheckDto.checkTypeList}" />

					</form:select>

					<c:if test="${not empty recCheckCheckTypeError}">
						<script>
							$("#searchType").addClass("errorFieldborder");
						</script>
					</c:if>
				</div>
				<c:if test="${recordsCheckDto.showCompletedCkbox }">
					<div class="col-xs-12 col-sm-6 col-md-1 noLeftPad"></div>
				</c:if>

				<div class="col-xs-12 col-sm-6 col-md-4 col-lg-4 rowPadding767 ">
					<label id="dateRequestLabel" for="dateReq"
						   class="col-xs-5 col-sm-6 col-md-6 col-lg-6 noLeftPad767 reqField">
						<spring:message code="recordsCheckDetail.dateOfRequest.label" />
					</label>
					<c:choose>
						<c:when test="${recordsCheckDto.disableDate}">
							<form:input hidden="true" aria-label="recCheckRequestDate"
										path="dtRecCheckRequest" />
							<form:input path="dtRecCheckRequest"
										disabled="${recordsCheckDto.disableDate}" id="dateReq"
										name="Date of Request" size="10" maxlength="10"
										aria-required="true" required="required" />
						</c:when>
						<c:otherwise>
							<form:input path="dtRecCheckRequest"
										disabled="${recordsCheckDto.disableDate}" id="dateReq"
										name="Date of Request" class="dateField" size="10"
										aria-required="true" maxlength="10" required="required" />
						</c:otherwise>
					</c:choose>
					<c:if test="${not empty recCheckRequestDateError}">
						<script>
							$("#dateReq").addClass("errorFieldborder");
						</script>
					</c:if>
				</div>

				<div
						class="col-xs-12 col-sm-12 col-md-3 col-lg-3 rowPadding767 rowPadding991 ">
					<p
							class="col-xs-5 col-sm-2 col-md-3 col-lg-5 noLeftPad767 noLeftPad991 noMarginBottom boldBody">
						<spring:message code="recordsCheckDetail.ueid.label" />
					</p>
					<p class="col-xs-5 col-sm-4 col-md-5 noLeftPad">${recordsCheckDto.idRecCheckUe}</p>
				</div>
			</div>

			<div class="row leftPadSecondLevel rowPadding">
				<div class="col-xs-12 col-sm-6 col-md-4 col-lg-4">
					<p
							class="col-xs-5 col-sm-5 col-md-5 col-lg-5 noLeftPad noMarginBottom boldBody">
						<spring:message code="recordsCheckDetail.requestedBy.label" />
					</p>
					<p class="col-xs-6 col-sm-5 col-md-7 noLeftPad noMarginBottom">${recordsCheckDto.nmRequestedBy}</p>
				</div>

				<c:if test="${recordsCheckDto.showCompletedCkbox}">
					<div class="col-xs-12 col-sm-6 col-md-1 noLeftPad">
						<!-- Checkbox display condition logic -->
						<label for="completed" class="mdl-checkbox mdl-js-checkbox">
							<c:choose>
								<c:when test="${recordsCheckDto.recChkDeterm eq 'ELGB' or
                                recordsCheckDto.recChkDeterm eq 'INEG' or
                                recordsCheckDto.recChkDeterm eq 'CLER' or
                                recordsCheckDto.recChkDeterm eq 'BAR' or
                        		recordsCheckDto.recChkDeterm eq 'NOTA'}">
									<form:checkbox id="completed"
												   checked="true"
												   disabled="true"
												   class="mdl-checkbox__input" path="completedCkboxChkd"/>
								</c:when>
								<c:otherwise>
									<form:checkbox id="completed"
												   disabled="${recordsCheckDto.disableCompletedCkbox}"
												   class="mdl-checkbox__input" path="completedCkboxChkd"/>
								</c:otherwise>
							</c:choose>
							<spring:message code="recordsCheckDetail.completed.label" />
						</label>
					</div>
				</c:if>


				<div class="col-xs-12 col-sm-6 col-md-4">
					<label id="dateCompletionLabel" for="dateComp"
						   class="col-xs-5 col-sm-6 col-md-6 col-lg-6 noLeftPad767 "><spring:message
							code="recordsCheckDetail.dateOfCompletion.label" /></label>
					<c:choose>
						<c:when test="${recordsCheckDto.disableCompletedDate}">
							<form:input hidden="true" aria-label="recCheckCompletedDate"
										path="dtRecCheckCompleted" />
							<form:input path="dtRecCheckCompleted"
										disabled="${recordsCheckDto.disableCompletedDate }" id="dateComp"
										name="Date Completed" maxlength="10" size="10" />
						</c:when>
						<c:otherwise>
							<form:input path="dtRecCheckCompleted"
										disabled="${recordsCheckDto.disableCompletedDate }" id="dateComp"
										name="Date Completed" class="dateField" maxlength="10" size="10" />
						</c:otherwise>
					</c:choose>
					<c:if test="${not empty recCheckCompletedDateError}">
						<script>
							$("#dateComp").addClass("errorFieldborder");
						</script>
					</c:if>
				</div>

				<div class="col-xs-12 col-sm-6 col-md-3">
					<p
							class="col-xs-5 col-sm-6 col-md-5 col-lg-5 noLeftPad767 noLeftPad991 noMarginBottom boldBody">
						<spring:message code="recordsCheckDetail.serviceCode.label" />
					</p>
					<p class="col-xs-5 col-sm-4 col-md-7 noLeftPad">${recordsCheckDto.cdRecCheckServ}</p>
				</div>
			</div>
			<c:if test="${recordsCheckDto.recCheckCheckType eq codeCheckType80
			or recordsCheckDto.recCheckCheckType eq codeCheckType81
				or recordsCheckDto.recCheckCheckType eq codeCheckType10 }">

				<div class="row leftPadSecondLevel rowPadding alignCenter">
					<div class = "col-xs-12 col-sm-12 col-md-10 col-lg-10">
						<Strong>
							<spring:message code="recordsCheckDetail.remainder.label" />
						</Strong>
					</div>
				</div>
			</c:if>
			<div class="row leftPadSecondLevel rowPadding">
				<div class="col-xs-12 col-sm-12 col-md-10 col-lg-10">
					<label for="commentsRecordsCheck"
						   class="col-xs-12 col-sm-2 col-md-2 col-lg-2 noLeftPad conReq"><spring:message
							code="recordsCheckDetail.comments.label" /><span class="sr-only">&nbsp;conditionally required field</span></label>
						<%-- Changed rows to 6 for artifact artf172944 --%>
					<form:textarea id="commentsRecordsCheck" path="recCheckComments"
								   name="Comments" class="col-xs-12 col-sm-9 col-md-10 stringSafeClass"
								   maxlength="4000" rows="6"></form:textarea>
						<%--End of code change for artifact artf172944 --%>
				</div>
			</div>
			<c:if test="${recordsCheckDto.recCheckCheckType eq codeCheckType80 }">
				<div class="row leftPadSecondLevel rowPadding rowPadding">
					<hr>
				</div>
				<div class="row leftPadSecondLevel">
					<h2>
						<spring:message
								code="recordsCheckDetail.fingerPrintSectionHeader.label" />
					</h2>
				</div>

				<div class="row leftPadSecondLevel rowPadding">
					<div class="col-xs-12 col-sm-6 col-md-4  ">
						<c:if test="${recordsCheckDto.disableemailPhone }">
							<form:input path="email" aria-label="txtEmail" hidden="true" />
						</c:if>

						<label for="email"
							   class="col-xs-4 col-sm-6 col-md-5 noLeftPad conReq"><spring:message
								code="recordsCheckDetail.email.label" /><span class="sr-only">&nbsp;conditionally required field</span></label>
						<form:select id="email"
									 disabled="${recordsCheckDto.disableemailPhone}" path="email"
									 name="Email" class="minWidthSelect">
							<%--  <c:if test="${ not empty fn:trim(recordsCheckDto.txtEmail) }"> --%>
							<form:option value=""></form:option>
							<form:options items="${emailPersonList}" />
						</form:select>

					</div>
					<div class="col-xs-12 col-sm-6 col-md-4 rowPadding767 ">
						<c:if test="${recordsCheckDto.disableemailPhone }">
							<form:input path="phoneNumber" aria-label="phoneNumber"
										hidden="true" />
						</c:if>
						<label for="phone"
							   class="col-xs-4 col-sm-6 col-md-3 col-lg-3 noLeftPad767"><spring:message
								code="recordsCheckDetail.phone.label" /></label>
						<form:select id="phone"
									 disabled="${recordsCheckDto.disableemailPhone}" path="phoneNumber"
									 name="Phone" class="minWidthSelect">
							<%-- <c:if test="${ not empty fn:trim(recordsCheckDto.phoneNumber) }"> --%>
							<form:option value=""></form:option>
							<form:options items="${phoneList}" />
						</form:select>
					</div>
				</div>

				<div class="row leftPadSecondLevel rowPadding">
					<div class="col-xs-12 col-sm-6 col-md-4 ">
						<p
								class="col-xs-4 col-sm-6 col-md-5 noLeftPad conReq noMarginBottom boldBody"
								id="cntMethod">
							<spring:message code="recordsCheckDetail.contactMethod.label" />
							<span class="sr-only">&nbsp;conditionally required field</span>
						</p>
						<c:if test="${recordsCheckDto.disableemailPhone }">
							<form:input path="recCheckContMethod"
										aria-label="recCheckContMethod" hidden="true" />
						</c:if>
						<fieldset>
							<legend class="legendDisplayNone">
								<span class="visuallyhidden">Radio Group</span>
							</legend>
							<label for="rbEmailphoneType_Id1"
								   class="mdl-radio mdl-js-radio autoHeight"> <form:radiobutton
									disabled="${recordsCheckDto.disableemailPhone }"
									path="recCheckContMethod" class="mdl-radio__button"
									id="rbEmailphoneType_Id1" name="contactMeth" value="EML" />
								<spring:message
										code="recordsCheckDetail.contactMethodEmail.label" />
							</label>&nbsp;&nbsp;&nbsp; <label for="rbEmailphoneType_Id2"
															  class="mdl-radio mdl-js-radio autoHeight"> <form:radiobutton
								disabled="${recordsCheckDto.disableemailPhone }"
								path="recCheckContMethod" class="mdl-radio__button"
								id="rbEmailphoneType_Id2" name="contactMeth" value="PHN" />
							<spring:message
									code="recordsCheckDetail.contactMethodPhone.label" />
						</label>
						</fieldset>
					</div>
				</div>




				<c:if
						test="${recordsCheckDto.pageModeStr eq webModify and empty fn:trim(recordsCheckDto.informationMessage) and fn:length(recordsCheckDto.cancelReasonList) gt 0 }">
					<div class="row leftPadSecondLevel rowPadding">
						<div class="col-xs-12 col-sm-6 col-md-4 ">
							<label for="cancelReasonId"
								   class="col-xs-5 col-sm-4 col-md-5 col-lg-5 noLeftPad "> <spring:message
									code="recordsCheckDetail.cancelReason.label" /></label>
							<div class="col-xs-12 col-sm-6 col-md-6 noLeftPad">
								<form:select id="cancelReasonId" path="cancelReason"
											 name="selCancelReason" class="minWidthSelect">
									<form:option value=""></form:option>
									<form:options items="${recordsCheckDto.cancelReasonList}" />
								</form:select>
							</div>
						</div>
					</div>

				</c:if>
				<%-- Added to implement artf172945  --%>
				<c:if test="${recordsCheckDto.pageModeStr eq webModify and not empty fn:trim(recordsCheckDto.recCheckCheckType) and recordsCheckDto.recCheckCheckType eq codeCheckType80
						and not empty fn:trim(recordsCheckDto.fbiCancelReason)}">
					<div class="row leftPadThirdLevel rowPadding">
						<div class="table-responsive tableDivClass">
							<table class="table-striped tablewidth">
								<thead>
								<tr>
									<th scope="colgroup"><spring:message
											code="recordsCheckDetail.cancellationDate.label" /></th>
									<th scope="colgroup"><spring:message
											code="recordsCheckDetail.cancellationReason.label" /></th>
									<th scope="colgroup"><spring:message
											code="recordsCheckDetail.cancelledBy.label" /></th>
								</tr>
								</thead>
								<tbody>
								<tr>
									<fmt:formatDate pattern="MM/dd/yyyy" var="dateEntered"
													value="${recordsCheckDto.dtCancelled}" />
									<td>${dateEntered}</td>
									<td>${recordsCheckDto.fbiCancelReason}</td>
									<td>${recordsCheckDto.fbiCancelledPersonName}</td>
								</tr>
								</tbody>
							</table>
						</div>
					</div>
				</c:if>
				<%-- End of artf172945  --%>
			</c:if>
			<%-- Added to implement artifact artf172936 --%>
			<c:if test="${(recordsCheckDto.pageModeStr eq webModify  or recordsCheckDto.stageClosed eq true) and not empty fn:trim(recordsCheckDto.recCheckCheckType) and
												(recordsCheckDto.recCheckCheckType eq codeCheckType80 or recordsCheckDto.recCheckCheckType eq codeCheckType81)}">
				<div class="row leftPadSecondLevel rowPadding rowPadding">
					<hr>
				</div>
				<div class="row leftPadSecondLevel">
					<h2>
						<spring:message
								code="recordsCheckDetail.criminalhistorySection.label" />
					</h2>
				</div>

				<%-- Added for artf172946  --%>
				<div class="row leftPadThirdLevel rowPadding">
					<div class="col-xs-12 col-sm-6 col-md-4 col-lg-4 noLeftPad">
						<c:if test="${recordsCheckDto.recCheckCheckType eq codeCheckType80}">
							<p class="col-xs-12 col-sm-4  col-md-6 noRightPad noLeftPad  noMarginBottom boldBody" id="crimAccess">
								<spring:message code="recordsCheckDetail.criminalhistoryResultCopied.label" />
							</p>
						</c:if>
						<c:if test="${recordsCheckDto.recCheckCheckType eq codeCheckType81}">
							<p class="col-xs-12 col-sm-4  col-md-6 noRightPad noLeftPad  noMarginBottom boldBody"
							   id="crimAccess">
								<spring:message code="recordsCheckDetail.rapback.resultcopied.label" />
							</p>
						</c:if>
						<c:if test="${recordsCheckDto.disableCrimResult }">
							<form:input path="indCrimHistoryResultCopied"
										aria-label="recCheckContMethod" hidden="true" />
						</c:if>
						<fieldset>
							<legend class="legendDisplayNone">
								<span class="visuallyhidden">Radio Group</span>
							</legend>
							<label for="rbCrimHistResultYes"
								   class="mdl-radio mdl-js-radio autoHeight">
								<form:radiobutton
										disabled="${recordsCheckDto.disableCrimResult}"
										path="indCrimHistoryResultCopied" class="mdl-radio__button"
										onclick="showChrimHistSIDTextField()"
										id="rbCrimHistResultYes" name="crimHistoryResult" value="Y" />
								<spring:message
										code="recordsCheckDetail.criminalhistory.yes" />
							</label>&nbsp;&nbsp;&nbsp;
							<label for="rbCrimHistResultNo"
								   class="mdl-radio mdl-js-radio autoHeight"> <form:radiobutton
									disabled="${recordsCheckDto.disableCrimResult}"
									path="indCrimHistoryResultCopied" class="mdl-radio__button"
									onclick="showNoCondChrimHistSIDTextField()"
									id="rbCrimHistResultNo" name="crimHistoryResult" value="N" />
								<spring:message
										code="recordsCheckDetail.criminalhistory.no" />
							</label>
							<input value="${recordsCheckDto.indCrimHistoryResultCopied}" id="displaySID" name="displaySID" type = "hidden"/>
						</fieldset>
					</div>
					<input value="${recordsCheckDto.disabletxtDpsSID}" id="distxtDpsSID" name="distxtDpsSID" type = "hidden"/>
					<div id="crimHistSidTextField"  class="col-xs-12 col-sm-6 col-md-4 rowPadding767" style="display: none;">



                                <label for="crimHistSID" class="col-xs-4 col-sm-6 col-md-3 col-lg-3 noLeftPad767 reqField" id="labelWithAsterRisk"><spring:message
                                								code="recordsCheckDetail.label.sid"/></label>



                                <label for="crimHistSID" class="col-xs-4 col-sm-6 col-md-3 col-lg-3 noLeftPad767" id="labelWithoutAsterRisk"><spring:message code="recordsCheckDetail.label.sid"/></label>


						<form:input value="${recordsCheckDto.txtDpsSID}" type="text" id="crimHistSID" path="txtDpsSID" pattern="[0-9]+" aria-label="SID number" class="col-xs-12 col-sm-8 col-md-2" required="required" size="8" maxlength="8" aria-required="true"
									oninput="this.value = this.value.replace(/[^0-9.]/g, '').replace(/(\..*)\./g, '$1');" />
						<form:input value="${recordsCheckDto.txtDpsSID}" id="crimHistSID2" path="txtDpsSID" type = "hidden"/>
					</div>
				</div>

				<%-- End of artifact artf172946  --%>

				<c:if test="${recordsCheckDto.abcsCheck and recordsCheckDto.recCheckCheckType ne codeCheckType81}">
					<div class="row leftPadThirdLevel rowPadding">
						<div class="col-xs-12 col-sm-6 col-md-4 col-lg-4 noLeftPad">
							<p
									class="col-xs-12 col-sm-4  col-md-6 noRightPad noLeftPad  noMarginBottom boldBody"
									id="crimAccess">
								<spring:message code="recordsCheckDetail.criminalhistoryAccess.label" />

							</p>

							<fieldset>
								<legend class="legendDisplayNone">
									<span class="visuallyhidden">Radio Group</span>
								</legend>
								<label for="rbcrimAcessYes"
									   class="mdl-radio mdl-js-radio autoHeight">
									<form:radiobutton
											disabled="true"
											path="chriAccess" class="mdl-radio__button"
											id="rbcrimAcessYes" name="crimHistoryAccess" value="true" />
									<spring:message
											code="recordsCheckDetail.criminalhistory.yes" />
								</label>&nbsp;&nbsp;&nbsp;
								<label for="rbcrimAcessNo" class="mdl-radio mdl-js-radio autoHeight">
									<form:radiobutton
											disabled="true"
											path="chriAccess" class="mdl-radio__button"
											id="rbcrimAcessNo" name="crimHistoryAccess" value="false" />
									<spring:message
											code="recordsCheckDetail.criminalhistory.no" />
								</label>
							</fieldset>
						</div>
					</div>
				</c:if>

			</c:if>
			<%-- End of artifact artf172936 --%>
			<%-- Rap Back Review Section --%>
			<c:if test="${recordsCheckDto.abcsCheck and recordsCheckDto.recCheckCheckType eq codeCheckType81}">
				<div class="row leftPadSecondLevel rowPadding rowPadding">
					<hr>
				</div>
				<div class="row leftPadSecondLevel">
					<h2 aria-level="2">
						<spring:message code="recordsCheckDetail.rapback.review.section" />
					</h2>
				</div>
				<div class="row leftPadSecondLevel">
					<div class="col-xs-12 col-sm-6 col-md-5 col-lg-4">
						<label for="rapbackReview" id="rapbackReviewLabel"
							   class="col-xs-5 col-sm-4 col-md-5 col-lg-5 noLeftPad reqField navCheckFalse">
							<spring:message code="recordsCheckDetail.rapback.review.label" />
						</label>
						<form:select disabled="${recordsCheckDto.indDisableRapBackReview}" aria-required="true"
									 required="required"  id="rapbackReview" path="cdRapBackReview"  class="minWidthSelect">
							<option value=""></option>
							<form:options items="${recordsCheckDto.rapBackReviewList}" />
						</form:select>
					</div>
					<div class="col-xs-12 col-sm-4 col-md-4 col-lg-4 rowPadding767">
						<label class="col-xs-12 col-sm-4  col-md-5 col-lg-6"
							   for="dtRapBackReviewed"><spring:message
								code="recordsCheckDetail.rapback.date.reviewed" /></label>
						<fmt:formatDate pattern="MM/dd/yyyy"
										value="${recordsCheckDto.dtRapBackRecordReviewed}" var="dtRapBackReviewed" />
						<p class="noMarginBottom">${dtRapBackReviewed}</p>
					</div>
					<input hidden="true" aria-label="rapBackReviewDate"
						   id="rapBackReviewDate" value="${recordsCheckDto.dtRapBackRecordReviewed}">
				</div>
			</c:if>
			<%-- End Rap Back Review Section --%>
			<c:if test="${recordsCheckDto.historySection}">
				<div class="row leftPadSecondLevel rowPadding rowPadding">
					<hr>
				</div>

				<div>
					<div class="row leftPadSecondLevel">
						<h2 aria-level="2">${sectionHeading}</h2>
					</div>
					<div class="row leftPadThirdLevel">
						<div class="col-xs-12 col-sm-6 col-md-4 col-lg-4 noLeftPad">
							<label for="determination" id="detrm"
								   class="col-xs-5 col-sm-4 col-md-4 col-lg-5 noLeftPad conReq"><spring:message
									code="recordsCheckDetail.determination.label" /><span class="sr-only">&nbsp;conditionally required field</span></label>
							<c:choose>
								<c:when test="${recordsCheckDto.recChkDeterm eq 'ELGB' or
                   							recordsCheckDto.recChkDeterm eq 'INEG' or
                   							recordsCheckDto.recChkDeterm eq 'CLER' or
                                			recordsCheckDto.recChkDeterm eq 'BAR' or
                   							recordsCheckDto.recChkDeterm eq 'NOTA'or
                   							recordsCheckDto.recChkDeterm eq 'BRDN'or
                   							recordsCheckDto.recChkDeterm eq 'BRNR'or
                   							recordsCheckDto.recChkDeterm eq 'CLAP'}">
									<form:select id="determination" name="Determination"
												 disabled="${recordsCheckDto.selectDetermin}" path="recChkDeterm"
												 class="minWidthSelect">
										<form:option value="" />
										<form:options items="${recordsCheckDto.determinationList}" />
									</form:select>
								</c:when>
								<c:otherwise>
									<form:select id="determination" name="Determination"
												 disabled="${recordsCheckDto.selectDetermin}" path="recChkDeterm"
												 class="minWidthSelect">
										<form:option value="" />
										<form:options items="${recordsCheckDto.determinationList}" />
									</form:select>
								</c:otherwise>
							</c:choose>
							<c:if test="${not empty recChkDetermError}">
								<script>
									$("#determination").addClass("errorFieldborder");
								</script>
							</c:if>
						</div>
						<c:choose>
							<c:when
									test="${not empty fn:trim(recordsCheckDto.recCheckCheckType) and recordsCheckDto.recCheckCheckType eq codeCheckType75   }">

								<div class="col-xs-12 col-sm-6 col-md-4 col-lg-4 rowPadding767 ">
									<label class="col-xs-12 col-sm-4  col-md-5 col-lg-6"
										   for="contractNumber"><spring:message
											code="recordsCheckDetail.contractId.label" /></label>
									<p class="col-xs-5 col-sm-4 col-md-5 noLeftPad">${recordsCheckDto.idContract}</p>
								</div>
							</c:when>
							<c:when
									test="${not empty fn:trim(recordsCheckDto.recCheckCheckType)  and
							(recordsCheckDto.recCheckCheckType eq codeCheckType10 ||
							recordsCheckDto.recCheckCheckType eq codeCheckType80 ||
							recordsCheckDto.recCheckCheckType eq codeCheckType75)}">

								<div class="col-xs-12 col-sm-4 col-md-4 col-lg-4 rowPadding767">
									<label class="col-xs-12 col-sm-4  col-md-5 col-lg-6"
										   for="DateFinalized"><spring:message
											code="recordsCheckDetail.dateFinalized.label" /></label>
									<c:if test="${recordsCheckDto.recChkDeterm eq 'ELGB' or
                   							recordsCheckDto.recChkDeterm eq 'INEG' or
                   							recordsCheckDto.recChkDeterm eq 'CLER' or
                                			recordsCheckDto.recChkDeterm eq 'BAR' or
                   							recordsCheckDto.recChkDeterm eq 'BRDN'or
                   							recordsCheckDto.recChkDeterm eq 'BRNR'or
                   							recordsCheckDto.recChkDeterm eq 'CLAP' or
                              				recordsCheckDto.recChkDeterm eq 'NOTA'}">
										<fmt:formatDate pattern="MM/dd/yyyy"
														value="${recordsCheckDto.dtDetermFinal}" var="dateFinalized" />
										<p class="noMarginBottom">${dateFinalized}</p>
									</c:if>
								</div>
								<input hidden="true" aria-label="determinationDate"
									   id="determinationDate" value="${recordsCheckDto.dtDetermFinal}">
							</c:when>
						</c:choose>
						<c:choose>
							<c:when
									test="${not empty fn:trim(recordsCheckDto.recCheckCheckType) and recordsCheckDto.recCheckCheckType eq codeCheckType10 and recordsCheckDto.abcsCheck and recordsCheckDto.idContract>0}">
								<p
										class="col-xs-12 col-sm-4  col-md-3 noLeftPad  noMarginBottom boldBody">
									<spring:message code="recordsCheckDetail.contractId.label" />
								</p>
								<p class="noMarginBottom">${recordsCheckDto.idContract}</p>
							</c:when>
						</c:choose>


					</div>
					<div class="row leftPadThirdLevel rowPadding">
						<c:choose>
							<c:when
									test="${(recordsCheckDto.showEmailDate || recordsCheckDto.showSendResenButton)}">
								<div class="col-xs-12 col-sm-6 col-md-4 col-lg-4 noLeftPad">
									<p
											class="col-xs-12 col-sm-4  col-md-6 noRightPad noLeftPad  noMarginBottom boldBody">
										Date
										<spring:message
												code="recordsCheckDetail.emailClrdRequestDate.label" />
									</p>
									<form:input hidden="true" aria-label="clearEmailReqDate"
												id="clearEmailReqDate" path="dtClrdEmailRequested" />
									<fmt:formatDate pattern="MM/dd/yyyy" var="dateEntered"
													value="${recordsCheckDto.dtClrdEmailRequested}" />
									<p id="clearedEmailReqDate" class="noMarginBottom">${dateEntered}</p>
								</div>
							</c:when>
						</c:choose>
						<c:choose>
							<c:when
									test="${recordsCheckDto.showEligibleEmailReqDate}">
								<div class="col-xs-12 col-sm-6 col-md-4 col-lg-4 noLeftPad">
									<p
											class="col-xs-12 col-sm-4  col-md-6 noRightPad noLeftPad  noMarginBottom boldBody">
										Date
										<spring:message
												code="recordsCheckDetail.emailEligibleRequestDate.label" />
									</p>
									<form:input hidden="true" aria-label="eligibleEmailReqDate"
												id="eligibleEmailReqDate" path="dtClrdEmailRequested" />
									<fmt:formatDate pattern="MM/dd/yyyy" var="dateEntered"
													value="${recordsCheckDto.dtClrdEmailRequested}" />
									<p id="eligibledEmailReqDate" class="noMarginBottom">${dateEntered}</p>
								</div>
							</c:when>
						</c:choose>
						<c:choose>
							<c:when
									test="${recordsCheckDto.showIneligibleEmailReqDate}">
								<div class="col-xs-12 col-sm-6 col-md-4 col-lg-4 noLeftPad">
									<p
											class="col-xs-12 col-sm-4  col-md-6 noRightPad noLeftPad  noMarginBottom boldBody">
										Date
										<spring:message
												code="recordsCheckDetail.emailIneligibleRequestDate.label" />
									</p>
									<form:input hidden="true" aria-label="ineligibleEmailReqDate"
												id="ineligibleEmailReqDate" path="dtClrdEmailRequested" />
									<fmt:formatDate pattern="MM/dd/yyyy" var="dateEntered"
													value="${recordsCheckDto.dtClrdEmailRequested}" />
									<p id="inelibledEmailReqDate" class="noMarginBottom">${dateEntered}</p>
								</div>
							</c:when>
						</c:choose>
							<%-- Added to implement artifact artf171305 --%>
						<c:choose>
							<c:when
									test="${not empty fn:trim(recordsCheckDto.recCheckCheckType) and recordsCheckDto.recCheckCheckType eq codeCheckType80 and recordsCheckDto.abcsCheck and recordsCheckDto.idContract>0}">
								<div class="col-xs-12 col-sm-6 col-md-4 col-lg-4 noLeftPad">
									<p
											class="col-xs-12 col-sm-4  col-md-6 noRightPad noLeftPad  noMarginBottom boldBody">

										<spring:message code="recordsCheckDetail.abcsAccountType.label" />
									</p>
									<p class="noMarginBottom">${recordsCheckDto.contractType}</p>
								</div>
								<div class="col-xs-12 col-sm-6 col-md-4 col-lg-4 noLeftPad">
									<p
											class="col-xs-12 col-sm-4  col-md-6 noRightPad noLeftPad  noMarginBottom boldBody">

										<spring:message code="recordsCheckDetail.agencyAcctId.label" />
									</p>
									<p class="noMarginBottom">${recordsCheckDto.idContract}</p>
								</div>
							</c:when>
						</c:choose>
							<%-- End artf171305 --%>
					</div>

					<input id="historyCount" aria-label="historyCount"
						   value="${recordsCheckDto.historyCount}" hidden="true">
					<c:if test="${recordsCheckDto.historyCount >0 }">
						<div class="row leftPadThirdLevel rowPadding">
							<div class="table-responsive tableDivClass">
								<table class="table-striped tablewidth">
									<thead>
									<tr>
										<th scope="colgroup"><spring:message
												code="recordsCheckDetail.determinationDate.label" /></th>
										<th scope="colgroup"><spring:message
												code="recordsCheckDetail.determinationCode.label" /></th>
										<th scope="colgroup"><spring:message
												code="recordsCheckDetail.determinationEnteredBy.label" /></th>
									</tr>
									</thead>
									<tbody>
									<c:forEach items="${recordsCheckDto.recordsCheckDetermination}"
											   var="historyDecision" varStatus="loop">
										<tr>
											<fmt:formatDate pattern="MM/dd/yyyy" var="dateEntered"
															value="${historyDecision.dtCreated}" />
											<td>${dateEntered}</td>
											<td>${cacheAdapter.getDecode(codeDeterm,historyDecision.recChkDeterm) }</td>
											<td>${historyDecision.nmPersonFull }</td>
										</tr>
									</c:forEach>
									</tbody>
								</table>
							</div>
						</div>

					</c:if>
				</div>
			</c:if>
			<c:if test="${(recordsCheckDto.pageModeStr eq webModify  or recordsCheckDto.stageClosed eq true) and not empty fn:trim(recordsCheckDto.recCheckCheckType) and recordsCheckDto.recCheckCheckType eq codeCheckType80}">
				<div class="row leftPadSecondLevel rowPadding">
					<hr>
				</div>
				<div class="row leftPadSecondLevel">
					<h2 aria-level="2"><spring:message code="recordsCheckDetail.label.subscribedRapBackSection"/></h2>
				</div>
				<div class="row leftPadThirdLevel rowPadding">
					<div class="col-xs-12 col-sm-6 col-md-7 col-lg-6 noLeftPad ">
						<label for="rapbackExpDate" id="rapbackExpDateLable"
							   class="col-xs-4 col-sm-5 col-md-7 col-lg-6 noLeftPad reqField">
							<spring:message code="recordsCheckDetail.label.rapbackExpDate"/>
						</label>

						<c:choose>
							<c:when test="${recordsCheckDto.disableDtRapBackExp}">
								<form:input hidden="true" aria-label="recCheckSubExpDate"
											path="dtRapBackExp"/>
								<form:input path="dtRapBackExp"
											disabled="${recordsCheckDto.disableFRBSlectionOptions}" id="rapbackExpDate"
											name="Date of SubExp" size="10" maxlength="10"
											aria-required="true" required="required"/>
							</c:when>
							<c:otherwise>
								<form:input path="dtRapBackExp"
											id="rapbackExpDate"
											name="Date of SubExp" class="dateFieldFuture-sub" size="10"
											aria-required="true" maxlength="10" required="required"/>
							</c:otherwise>
						</c:choose>
						<c:if test="${not empty recCheckRequestDateError}">
							<script>
								$("#rapbackExpDate").addClass("errorFieldborder");
							</script>
						</c:if>
					</div>
				</div>
				<div class="row leftPadThirdLevel rowPadding">
					<div class="col-xs-12 col-sm-6 col-md-4 col-lg-6 noLeftPad ">
						<label
								class="col-xs-5 col-sm-6 col-md-6 col-lg-6 noLeftPad">
							<spring:message code="recordsCheckDetail.label.rapbackStatus"/>
						</label>
						<c:out value="${cdFbiSubscriptionDStatus}"></c:out>
					</div>
				</div>
				<div class="row leftPadThirdLevel rowPadding">
					<div class="col-xs-12 col-sm-6 col-md-4 col-lg-6 noLeftPad ">

						<label for="oriAccountCdId" id="rapbackOriAccount"
							   class="col-xs-5 col-sm-6 col-md-6 col-lg-6 noLeftPad">
							<spring:message code="recordsCheckDetail.label.rapbackOriAccount"/>
						</label>

						<form:select id="oriAccountCdId" name="oriAccountCdName"
									 path="cdORIAccntNum"
									 disabled="${recordsCheckDto.disableORIAccount}"
									 class="minWidthSelect">
							<form:option value=""/>
							<form:options  items="${recordsCheckDto.rapBackORIAccountList}"/>
						</form:select>
						<c:if test="${not empty recChkDetermError}">
							<script>
								$("#cdORIAccntNum").addClass("errorFieldborder");
							</script>
						</c:if>

					</div>
				</div>
				<div class="row leftPadThirdLevel rowPadding">

					<div class="col-xs-12 col-sm-6 col-md-4 col-lg-6 noLeftPad ">

						<label for="resultsReceived" id="resultsReceived"
							   class="col-xs-5 col-sm-6 col-md-6 col-lg-6 noLeftPad">
							<spring:message code="recordsCheckDetail.label.resultsReceived"/>
						</label>
						<c:choose>
							<c:when test="${recordsCheckDto.disableDate}">
								<form:input hidden="true" aria-label="recCheckRequestDate"
											path="dtRecCheckReceived" />
								<form:input path="dtRecCheckReceived"
											disabled="${recordsCheckDto.disableDate}" id="dtRecCheckReceived"
											name="ResultsReceived" size="10" maxlength="10"
											aria-required="true" required="required" />
							</c:when>
							<c:otherwise>
								<form:input path="dtRecCheckReceived"
											disabled="${recordsCheckDto.disableDate}" id="dtRecCheckReceived"
											name="ResultsReceived" class="dateField" size="10"
											aria-required="true" maxlength="10" required="required" />
							</c:otherwise>
						</c:choose>
					</div>
				</div>
				<div class="row leftPadThirdLevel rowPadding">
					<div class="col-xs-12 col-sm-6 col-md-6 col-lg-6 noLeftPad">
						<p class="col-xs-12 col-sm-4  col-md-6 noRightPad noLeftPad  noMarginBottom boldBody"
						   id="rapBackCrimAccess">
							<spring:message code="recordsCheckDetail.rapBackResultCopied.label"/>
						</p>
						<c:if test="${recordsCheckDto.disableRapBackSubsCopied}">
							<form:input path="indRapBackSubscriptionCopied"
										aria-label="recCheckContMethod" hidden="true" />
						</c:if>
						<fieldset>
							<legend class="legendDisplayNone">
								<span class="visuallyhidden">Radio Group</span>
							</legend>
							<label for="rapBackSubscriptionCopiedYes"
								   class="mdl-radio mdl-js-radio autoHeight">
								<form:radiobutton
										disabled="${recordsCheckDto.disableRapBackSubsCopied}"
										path="indRapBackSubscriptionCopied" class="mdl-radio__button"
										id="rapBackSubscriptionCopiedYes" name="frbcrimHistoryResult" value="Y"/>
								<spring:message
										code="recordsCheckDetail.criminalhistory.yes"/>
							</label>&nbsp;&nbsp;&nbsp;
							<label for="rapBackSubscriptionCopiedNo"
								   class="mdl-radio mdl-js-radio autoHeight">
								<form:radiobutton
										disabled="${recordsCheckDto.disableRapBackSubsCopied}"
										path="indRapBackSubscriptionCopied" class="mdl-radio__button"
										id="rapBackSubscriptionCopiedNo" name="frbcrimHistoryResult" value="N"/>
								<spring:message
										code="recordsCheckDetail.criminalhistory.no"/>
							</label>
						</fieldset>
					</div>
				</div>
				<%-- rabback unsubscribe code --%>
				<c:if test="${recordsCheckDto.showRapBackUnsubscribeSection}">

					<div class="row leftPadSecondLevel rowPadding rowPadding">
						<hr>
					</div>
					<div class="row leftPadSecondLevel">
						<h2 aria-level="2">
							<spring:message code="recordsCheckDetail.unSubscribe.requestToUnsubscribe.heading" />
						</h2>
					</div>
					<div class="row leftPadThirdLevel rowPadding">
						<div id="unsubReqDivID" class="col-xs-12 col-sm-6 col-md-6 col-lg-8 noLeftPad">
							<label for="rapBackUnSubscrReqTypeID" style="margin-right: 10px; display: inline-block">
								<spring:message code="recordsCheckDetail.unSubscribe.requestToUnsubscribe"/>
							</label>
							<form:radiobutton path="indRapBackUnSubScrType" style="margin-right: 100px" id="rapBackUnSubscrReqTypeID"  name="indRapBackUnSubScrType"
											  onclick="updateReqDt();" value="I" aria-label="Select option to request Unsubscribe RapBack, move up or down arrow to change selection"
											  disabled="${recordsCheckDto.disableUnsubcribeOptions}"/>
							<label for="dtRapBackUnSubrReqDateLabelID" style="margin-right: 10px; display: inline-block" >
								<spring:message code="recordsCheckDetail.unSubscribe.RequestDate"/>
							</label>
							<c:choose>
								<c:when test="${not empty recordsCheckDto.dtRapBackUnSubrReq or not empty recordsCheckDto.dtRapBackUnSubscribed}">
									<form:input path="dtRapBackUnSubrReq" hidden="true" aria-label=" Select Date to Request Unsubscribe RapBack"  name="dtRapBackUnSubrReqDateLabelID" />

									<form:input path="dtRapBackUnSubrReq"  id="dtRapBackUnSubrReqDateID" aria-label=" Select Date to Request Unsubscribe RapBackk"
												name="dtRapBackUnSubrReqDateLabelID"  size="10" disabled="${recordsCheckDto.disableUnsubcribeOptions}"/>
								</c:when>
								<c:otherwise>
									<form:input path="dtRapBackUnSubrReq"  id="dtRapBackUnSubrReqDateID"  aria-label="Select Date to Request Unsubscribe RapBack"
												name="dtRapBackUnSubrReqDateLabelID"  class="dateField"  size="10"/>

								</c:otherwise>
							</c:choose>
							<label for="rapBackUnSubscrReqComp" style="margin-left: 35px; display: inline-block" >
								<spring:message code="recordsCheckDetail.unSubscribe.UnsubscribeCompletionDate"/>
							</label>
							<form:input path="dtRapBackUnSubrReqComp" style="margin-left: 10px; "  id="rapBackUnSubscrReqComp"  name="dtRapBackUnSubrReqComp"  size="10" disabled="true"/>

						</div>
					</div>
					<div class="row leftPadThirdLevel rowPadding">
						<div  id="unsubDPSReqDivID" class="col-xs-12 col-sm-6 col-md-6 col-lg-6 noLeftPad">
							<label for="rapBackUnSubscrReqTypeDpsID" style="margin-right: 10px; display: inline-block">
								<spring:message code="recordsCheckDetail.unSubscribe.unSubscribedInDPSSecureSite"/>
							</label>
							<form:radiobutton path="indRapBackUnSubScrType" style="margin-right: 48px;" id="rapBackUnSubscrReqTypeDpsID" name="indRapBackUnSubScrType"
											  value="D" onclick="updateReqDt(); " aria-label="Select option for Unsubscribed RapBack on DPS secure site, move up or down arrow to change selection"
											  disabled="${recordsCheckDto.disableUnsubcribeOptions}"
							/>

							<label for="dtRapBackUnSubrReqDateDpsID" style="margin-right: 10px; display: inline-block" >
								<spring:message code="recordsCheckDetail.unSubscribe.UnsubscribedDate"/>
							</label>
							<c:choose>
								<c:when test="${not empty recordsCheckDto.dtRapBackUnSubscribed or not empty recordsCheckDto.dtRapBackUnSubrReq }">
									<form:input path="dtRapBackUnSubscribed" hidden="true" aria-label="RapBack Unsubscribed Date"  name="dtRapBackUnSubscribed" />

									<form:input path="dtRapBackUnSubscribed"  id="dtRapBackUnSubrReqDateDpsID" aria-label="RapBack Unsubscribed Date"
												name="dtRapBackUnSubscribed"  size="10" disabled="${recordsCheckDto.disableUnsubcribeOptions}"/>
								</c:when>
								<c:otherwise>
									<form:input path="dtRapBackUnSubscribed"  id="dtRapBackUnSubrReqDateDpsID"  aria-label="RapBack Unsubscribed Date"
												name="dtRapBackUnSubscribed"  class="dateField"  size="10" />
								</c:otherwise>
							</c:choose>
						</div>
					</div>
				</c:if>
				<%-- end of  rabback unsubscribe code --%>
			</c:if>
			<div class="row leftPadThirdLevel rowPadding">
				<c:if test="${not empty fn:trim(recordsCheckDto.cdCheckType)  }">
					<form:input path="cdCheckType" aria-label="hdnCdCheckType"
								hidden="true" />
				</c:if>

				<div class="col-xs-3 noLeftPad">
					<form:input hidden="true" aria-label="checkedBoxesForDelete"
								path="checkedBoxesForDelete"></form:input>
					<c:if
							test="${ recordsCheckDto.pageModeStr ne webView and recordsCheckDto.rowCanBeDeleted and (not empty fn:trim(recordsCheckDto.recCheckCheckType) and
												recordsCheckDto.recCheckCheckType ne codeCheckType81)}">
						<button type="button" id="deleteRecordCheck" name="DeleteRecordCheckDetail"
								class="btn btn-default">
							&nbsp;&nbsp;<spring:message
								code="recordsCheckDetail.delete.label" />&nbsp;&nbsp;
						</button>
					</c:if>
				</div>

				<div class="col-xs-9 noLeftPad noRightPad alignRight">
					<c:if test="${recordsCheckDto.showRadioButton }">
						<c:if test="${recordsCheckDto.indDisableType}">
							<form:input path="indReviewNow" aria-label="indReviewNow"
										hidden="true" />
						</c:if>
						<fieldset class="col-xs-5">
							<legend class="legendDisplayNone">
								<span class="visuallyhidden">Radio Group</span>
							</legend>
							<div class="col-xs-12 col-sm-4 col-md-5 noLeftPad">
								<label for="reviewNow"
									   class=" noLeftPad mdl-radio mdl-js-radio autoHeight"><spring:message
										code="recordsCheckDetail.reviewNow.label" /> <form:radiobutton
										path="indReviewNow" class="mdl-radio__button" id="reviewNow"
										name="Review Now" disabled="${recordsCheckDto.indDisableType}"
										value="reviewNow" /> </label>
							</div>
							<div class="col-xs-12 col-sm-5 col-md-5 noLeftPad rowPadding767">
								<label for="reviewLater"
									   class=" noLeftPad mdl-radio mdl-js-radio autoHeight"><spring:message
										code="recordsCheckDetail.reviewLater.label" /> <form:radiobutton
										path="indReviewNow" class="mdl-radio__button" id="reviewLater"
										name="Review Now" disabled="${recordsCheckDto.indDisableType}"
										value="reviewLater" /> </label>
							</div>
						</fieldset>
					</c:if>
						<%-- Added for artf172946  --%>
					<c:if
							test="${recordsCheckDto.pageModeStr ne webView and recordsCheckDto.showAddResultsButton}">
						<button id="addResultRecordsCheckDetail" type="button"
								name="AddResultRecordsCheckDetail"
								data-url="<c:url value='/case/person/record/redirectCriminalHistory?recordsCheckDetailIndex=${recordsCheckDto.idRecCheck}'/>"
								class="btn btn-primary buttonSubmission">
							&nbsp;&nbsp;
							<spring:message code="recordsCheckDetail.addresults.label" />
							&nbsp;&nbsp;
						</button>
					</c:if>
						<%-- End of artifact artf172946  --%>
					<c:if
							test="${recordsCheckDto.pageModeStr ne webView and recordsCheckDto.showResultsButton and recordsCheckDto.recCheckStatus.toUpperCase() ne PG}">
						<button id="displayCriminalHistoryResults" type="button"
								name="Display Criminal History Results"
								data-url="<c:url value='/case/person/record/redirectCriminalHistory?recordsCheckDetailIndex=${recordsCheckDto.idRecCheck}'/>"
								class="btn btn-primary buttonSubmission">
							&nbsp;&nbsp;
							<spring:message code="recordsCheckDetail.results.label" />
							&nbsp;&nbsp;
						</button>
					</c:if>
					<c:choose>
						<c:when
								test="${recordsCheckDto.pageModeStr ne webView and (recordsCheckDto.showEmailButton || recordsCheckDto.showSendResenButton)}">
							<c:choose>
								<c:when
										test="${recordsCheckDto.showSendResenButton and !recordsCheckDto.indEmailSent }">
									<%--artf257140 :CASA users should see SendCearEmail Button after selecting Clear in determination dropdown--
                                    Resend Email button should not display for CASA users after sending the cleared email - Resend button code has been removed.--%>
									<button type="button" name="btnEmail"
											class="btn btn-primary marginLeft15"
											onClick="setDateClearedEmail();">
										&nbsp;&nbsp;
										<spring:message code="recordsCheckDetail.sendClearedEmail.label"/>
										&nbsp;&nbsp;
									</button>
								</c:when>
							</c:choose>
							<c:choose>
								<c:when test="${recordsCheckDto.showEmailButton }">

									<button type="button" onClick="setDateClearedEmail();"
											name="btnEmail" class="btn btn-primary marginLeft15">
										&nbsp;&nbsp;
										<spring:message
												code="recordsCheckDetail.sendClearedEmail.label"/>
										&nbsp;&nbsp;
									</button>
								</c:when>
							</c:choose>
						</c:when>
					</c:choose>
					<c:choose>
						<c:when test="${recordsCheckDto.showEligibleEmailButton}">
						    <c:choose>
                        	    <c:when test="${recordsCheckDto.disableEmailButton}">
                                    <button type="button" disabled onClick="setDateClearedEmail();"
                                        name="btnEmail" class="btn btn-primary marginLeft15">
                                        &nbsp;&nbsp;
                                        <spring:message
                                            code="recordsCheckDetail.sendEligibleEmail.label"/>
                                        &nbsp;&nbsp;
                                    </button>
                        		</c:when>
                        		<c:otherwise>
                        		    <button type="button" onClick="setDateClearedEmail();"
                                            name="btnEmail" class="btn btn-primary marginLeft15">
                                            &nbsp;&nbsp;
                                            <spring:message
                                                code="recordsCheckDetail.sendEligibleEmail.label"/>
                                            &nbsp;&nbsp;
                                    </button>
                        		</c:otherwise>
                        	</c:choose>
						</c:when>
					</c:choose>
					<c:choose>
						<c:when test="${recordsCheckDto.showResendEligibleEmailButton}">
						    <c:choose>
						        <c:when test="${recordsCheckDto.disableEmailButton}">
                                    <button type="button" disabled onClick="setDateClearedEmail();"
                                     		name="btnEmail" class="btn btn-primary marginLeft15">
                                     		&nbsp;&nbsp;
                                     		<spring:message
                                     		code="recordsCheckDetail.resendEligibleEmail.label"/>
                                     		&nbsp;&nbsp;
                                     </button>
						        </c:when>
						        <c:otherwise>
						            <button type="button" onClick="setDateClearedEmail();"
                                        name="btnEmail" class="btn btn-primary marginLeft15">
                                        &nbsp;&nbsp;
                                        <spring:message
                                        code="recordsCheckDetail.resendEligibleEmail.label"/>
                                        &nbsp;&nbsp;
                                        </button>
						        </c:otherwise>
						    </c:choose>
						</c:when>
					</c:choose>
					<c:choose>
						<c:when test="${recordsCheckDto.showIneligibleEmailButton}">
						    <c:choose>
                                <c:when test="${recordsCheckDto.disableEmailButton}">
                                    <button type="button" disabled onClick="setDateClearedEmail();"
                                        name="btnEmail" class="btn btn-primary marginLeft15">
                                        &nbsp;&nbsp;
                                        <spring:message
                                        code="recordsCheckDetail.sendIneligibleEmail.label"/>
                                        &nbsp;&nbsp;
                                    </button>
                                </c:when>
                                <c:otherwise>
                                    <button type="button" onClick="setDateClearedEmail();"
                                        name="btnEmail" class="btn btn-primary marginLeft15">
                                        &nbsp;&nbsp;
                                        <spring:message
                                            code="recordsCheckDetail.sendIneligibleEmail.label"/>
                                        &nbsp;&nbsp;
                                    </button>
                                </c:otherwise>
                            </c:choose>
					    </c:when>
					</c:choose>
					<c:choose>
						<c:when test="${recordsCheckDto.showResendIneligibleEmailButton}">
						    <c:choose>
                                <c:when test="${recordsCheckDto.disableEmailButton}">
                                    <button type="button" disabled onClick="setDateClearedEmail();"
                                    name="btnEmail" class="btn btn-primary marginLeft15">
                                    &nbsp;&nbsp;
                                    <spring:message
                                    code="recordsCheckDetail.resendEligibleEmail.label"/>
                                    &nbsp;&nbsp;
                                    </button>
                            	</c:when>
                            	<c:otherwise>
							        <button type="button" onClick="setDateClearedEmail();"
									name="btnEmail" class="btn btn-primary marginLeft15">
								    &nbsp;&nbsp;
								    <spring:message
									code="recordsCheckDetail.resendIneligibleEmail.label"/>
								    &nbsp;&nbsp;
							        </button>
							    </c:otherwise>
                            </c:choose>
						</c:when>
					</c:choose>
						<%-- Artifact: artf204870 - ALMID: 18303: FPS check batch comments not saving : Irrespective of the completed checkobox, save button now points to the same code (just like in Legacy) --%>
					<button type="button" id="saveRecordsCheckDetail"
							name="SaveRecordCheckDetail"
							class="btn btn-primary marginLeft15 navCheckFalse">&nbsp;&nbsp;<spring:message
							code="recordsCheckDetail.save.label" />&nbsp;&nbsp;
					</button>
				</div>
			</div>
			<div style="display: none;">
				<select id="narrativeForms" name="narrativeForms" aria-label="Narrative Forms"
						class="navCheckFalse">
					<c:if
							test="${!recordsCheckDto.indEBCNarrativeDisabled}">
						<option value="EBC" data-formAttribute="${formTagDtoList['EBC']}"></option>
					</c:if>
				</select>
			</div>
			<div class="row leftPadThirdLevel">
				<div id="processingCol1Id" style="color: red"
					 class="noLeftPad alignRight">&nbsp;</div>
			</div>

			<c:if test="${recordsCheckDto.showUploadedDocuments }">
				<div class="row leftPadSecondLevel rowPadding">
					<hr>
				</div>

				<div class="row leftPadSecondLevel">
					<h2 class="mb-0">
						<button type="button" class="btn btn-block accordionBtn noLeftPad "
								aria-expanded="true" data-toggle="collapse"
								data-target="#uploadDocuments" name="Upload Documents">
							<spring:message code="recordsCheckDetail.uploadDocuments.label" />
						</button>
					</h2>
				</div>

				<div class="row leftPadThirdLevel rowPadding collapse in"
					 aria-expanded="true" id="uploadDocuments">
					<input hidden="true" id="documentId" name="documentIndex" value="">
					<div class="table-responsive tableDivClass">
						<table class="table-striped tablewidth">
							<thead>
							<tr>
								<th scope="colgroup" id="event_List_Column01"><span
										class="visuallyhidden">Empty Table header for Radio Data</span></th>
								<th align="left" scope="colgroup"><spring:message
										code="recordsCheckDetail.documentName.label" /></th>
							</tr>
							</thead>
							<tbody>
							<c:choose>
								<c:when
										test="${fn:length(recordsCheckDto.recordsCheckDocList) eq 0 }">
									<td colspan="2"><spring:message
											code="recordsCheckDetail.noRecordsExists.label" /></td>
								</c:when>
								<c:otherwise>
									<c:forEach items="${recordsCheckDto.recordsCheckDocList}"
											   var="document" varStatus="loop">
										<tr>
											<td colspan="2"><fieldset>
												<legend class="legendDisplayNone">
													<span class="visuallyhidden">Radio Group</span>
												</legend>
												<label for="selectDocumentRadio${loop.index}"
													   class="mdl-radio mdl-js-radio"> <input
														type="radio" name="rowIndex"
														id="selectDocumentRadio${loop.index}"
														class="mdl-radio__button"
														onclick="setDocumentId(${document.idDocRepository},${loop.index})"
														value="${document.idDocumentPdb}">${document.documentName}
													<span class="accessibilityText" id="documentRecord">Document
															For View Or Delete</span>
												</label>
											</fieldset></td>
										</tr>
									</c:forEach>
								</c:otherwise>
							</c:choose>
							</tbody>
						</table>
					</div>

					<div class="row rowPadding">
						<div class="col-xs-6">

							<c:if test="${recordsCheckDto.pageModeStr ne webView}">
								<button type="button" id="deleteDocument"
										name="DeleteUploadedDocument" class="btn btn-default"
										onClick="return confirmDocumentDeleteOrView('delete');location.href='${pageContext.request.contextPath}/case/person/record/deleteUploadedDocument'">
									&nbsp;&nbsp;
									<spring:message code="recordsCheckDetail.delete.label" />
									&nbsp;&nbsp;
								</button>
							</c:if>
						</div>
						<div class="col-xs-6 alignRight">
							<c:if test="${recordsCheckDto.pageModeStr ne webView}">
								<button id="viewDocument" type="button" name="View Document"
										class="btn btn-primary"
										onClick="return confirmDocumentDeleteOrView('view');">
									&nbsp;&nbsp;
									<spring:message code="recordsCheckDetail.viewDocument.label" />
									&nbsp;&nbsp;
								</button>
							</c:if>
						</div>
					</div>
				</div>

				<div class="row leftPadSecondLevel rowPadding">
					<hr>
				</div>
			</c:if>

			<c:if test="${recordsCheckDto.showNotifications }">
				<div class="row leftPadSecondLevel">
					<input hidden="true" id="notificationId" name="notificationIndex"
						   value="">
					<h2 class="mb-0">
						<button type="button" class="btn btn-block accordionBtn noLeftPad "
								aria-expanded="true" data-toggle="collapse"
								data-target="#notifications" name="Notifications">
							<spring:message code="recordsCheckDetail.notificationSectionHeader.label" />
						</button>
					</h2>
				</div>

				<div class="row leftPadThirdLevel rowPadding collapse in"
					 aria-expanded="true" id="notifications">

					<div class="table-responsive tableDivClass">
						<table class="table-striped tablewidth">
							<thead>
							<tr>
								<th scope="colgroup" id="event_List_Column01"><span
										class="visuallyhidden">Empty Table header for Radio Data</span></th>
								<th scope="colgroup"><spring:message
										code="recordsCheckDetail.notificationStatusDate.label" /></th>
								<th scope="colgroup"><spring:message
										code="recordsCheckDetail.notificationStatus.label" /></th>
								<th scope="colgroup"><spring:message
										code="recordsCheckDetail.notificationType.label" /></th>
								<th scope="colgroup"><spring:message
										code="recordsCheckDetail.notificationSentTo.label" /></th>
								<th scope="colgroup"><spring:message
										code="recordsCheckDetail.notificationSentFrom.label" /></th>
								<th scope="colgroup"><spring:message
										code="recordsCheckDetail.notificationUpdatedBy.label" /></th>
								<th scope="colgroup"><spring:message
										code="recordsCheckDetail.notificationStaffId.label" /></th>
								<th scope="colgroup"><spring:message
										code="recordsCheckDetail.notificationId.label" /></th>
							</tr>
							</thead>
							<tbody>
							<c:choose>
								<c:when
										test="${fn:length(recordsCheckDto.recordsCheckNotificationList) eq 0 }">
									<td colspan="9"><spring:message
											code="recordsCheckDetail.noRecordsExists.label" /></td>
								</c:when>
								<c:otherwise>
									<c:forEach
											items="${recordsCheckDto.recordsCheckNotificationList}"
											var="notification" varStatus="loop">
										<c:if
												test="${!(notification.notificationStatus == codeResent &&  empty fn:trim(notification.recipientEmail)) }">
											<c:set var="renderFormat" value=""></c:set>
											<c:choose>
												<c:when
														test="${notification.notificationStatus == webNew || notification.notificationStatus == codeDrft}">
													<c:set var="renderFormat" value="HTML_WITH_SHELL"></c:set>
												</c:when>
												<c:otherwise>
													<c:set var="renderFormat" value="HTML_WITHOUT_SHELL"></c:set>
												</c:otherwise>
											</c:choose>
											<tr>
												<td><c:if
														test="${'SENT' eq notification.notificationStatus}">
													<fieldset>
														<legend class="legendDisplayNone">
															<span class="visuallyhidden">Radio Group</span>
														</legend>
														<label for="selectNotifRadio${loop.index}"
															   class="mdl-radio mdl-js-radio"> <input
																type="radio" name="rowIndex"
																id="selectNotifRadio${loop.index}"
																class="mdl-radio__button"
																onclick="setNotificationId('${notification.notificationType}','${notification.idRecordsCheckNotif}','${notification.notificationStatus}','${notification.getDtLastUpdate()}')"
																value="${notification.idRecordsCheckNotif}"> <span
																class="accessibilityText" id="notifRecord">Notification
																	For View Or Delete</span>
														</label>
													</fieldset>
												</c:if></td>
												<fmt:formatDate pattern="MM/dd/yyyy"
																value="${notification.getDtLastUpdate()}" var="dateUpdated" />
												<td>${dateUpdated}</td>



												<td>${cacheAdapter.getDecode(codeNoticeStatus,notification.notificationStatus)}</td>
												<td><a href="#"
													   onclick="openNotification('${notification.notificationType}','${renderFormat}','${notification.idRecordsCheckNotif}','${notification.notificationStatus}','false','${notification.getDtLastUpdate()}','true');">${cacheAdapter.getDecode(codeNotifyType,notification.notificationType) }</a></td>
												<td>${notification.recipientEmail}</td>
												<td>${notification.senderEmail }</td>
												<td>${notification.senderFullName}</td>
												<td>${notification.idSenderPerson}</td>
												<td>${notification.idRecordsCheckNotif}</td>
											</tr>
										</c:if>
									</c:forEach>
								</c:otherwise>
							</c:choose>
							</tbody>
						</table>
					</div>

					<div class="col-xs-12 noLeftPad rowPadding noRightPad alignRight">
						<button id="displayCriminalHistoryResults" type="button"
							name="Display Criminal History Results"
							onClick="refreshNotification()"
							class="btn btn-primary marginLeft15">
						<spring:message code="recordsCheckDetail.refresh.label"/>
					</button>
					<button id="resendNotif" type="button"
							name="Resend Notification" class="btn btn-primary"
							onClick="resendNotification()">
						<spring:message code="recordsCheckDetail.resendEmail.label"/>
					</button>
				</div>
			</c:if>
			<input hidden="true" id="helpPageName" aria-label="helpPageName"
				   value="/Person/Record_Check_Detail">
			<c:if
					test="${ recordsCheckDto.pageModeStr ne webView and !recordsCheckDto.indEBCNarrativeDisabled}">
				<div class="row leftPadThirdLevel rowPadding">
					<div class="col-xs-3 noLeftPad">
						<button id="narrativeButton" name="EBC Narrative"
								class="btn btn-primary tertiaryButton hasP2NarrativeForm">
							&nbsp;&nbsp;
							<spring:message code="recordsCheckDetail.narrative.label" />
							&nbsp;&nbsp;
						</button>
						<c:if test="${indicator}">
							<span class="glyphicon glyphicon-ok"></span>
						</c:if>
					</div>
				</div>
			</div>
		</c:if>
		<c:if test="${recordsCheckDto.showNotifications}">
			<div class="row rowPadding leftPadSecondLevel">
				<div class="col-sm-12 noLeftPad noRightPad">
					<div class="panel formPanel">
						<div class="panel-heading formHeader" aria-level="2">
							<spring:message code="recordsCheckDetail.forms.label" />
						</div>
						<div class="panel-body">
							<div class="row">
								<div class="col-xs-12 col-md-6">
									<div class="row">
										<div class="col-sm-2 col-md-3 formLabelPad">
											<label class="control-label" for="forms"><spring:message
													code="recordsCheckDetail.notification.label" /></label>
										</div>
										<div class="col-sm-10 col-md-9">
											<table>
												<tr class="noBorder">
													<td class="noPad"><select id="forms" name="forms">
															<option value=""></option>
													        <option value="ACTRNOT" data-formAttribute="${formTagDtoMap['ACTRNOT']}">Action Required Notification</option>
															<option value="BARRNOT" data-formAttribute="${formTagDtoMap['BARRNOT']}">Barred Notification</option>
															<option value="CREGNOT" data-formAttribute="${formTagDtoMap['CREGNOT']}">Child Abuse Neglect Central Registry Check Notification</option>
															<option value="LTTRNOT" data-formAttribute="${formTagDtoMap['LTTRNOT']}">DFPS Letterhead Notification</option>
														<c:if test="${recordsCheckDto.showFbiNotifications}">
															<option value="FBIENOT" data-formAttribute="${formTagDtoMap['FBIENOT']}">FBI Eligible Notification</option>
															<option value="FBIINOT" data-formAttribute="${formTagDtoMap['FBIINOT']}">FBI Ineligible Notification</option>
															<option value="FBIVIEL" data-formAttribute="${formTagDtoMap['FBIVIEL']}">FBI Volunteer Ineligible</option>
															<option value="FBIHVEL" data-formAttribute="${formTagDtoMap['FBIHVEL']}">FBI Volunteer Eligible With History</option>
														</c:if>
															<option value="NLCMNOT" data-formAttribute="${formTagDtoMap['NLCMNOT']}">Non-Licensing Match Summary Notification</option>
															<option value="PCSRNOT" data-formAttribute="${formTagDtoMap['PCSRNOT']}">PCS Risk Evaluation Decision Notification</option>


														<c:if test="${recordsCheckDto.contractType.equals('PCS')}">
															<option value="PCSINEN" data-formAttribute="${formTagDtoMap['PCSINEN']}">PCS Ineligible Notification</option>
														</c:if>

													</select></td>												
														<td class="reportButtonLeftPad">
															<button type="button" class="btn btn-sm btnSectionLevel buttonFormSubmission"
																	name="launch Form" id="launchForm">Launch<span class="sr-only">&nbsp;Form</span></button>
														</td>
														<td>
																<%-- artf205247 email encrypt artf148743 --%>
															<label for="sendSecureNotif" class="mdl-checkbox mdl-js-checkbox"><form:checkbox
																	id="sendSecureNotif" path="*"
																	onclick="showNonSecureAlert(this);"
																	class="mdl-checkbox__input" value="Y" checked="checked"></form:checkbox>
																<spring:message code="recordsCheckDetail.sendSecure.label" /></label>
														</td>
													</tr>
												</table>
											</div>
										</div>
									</div>
								</div>
							</div>
						</div>
					</div>
				</div>
			</c:if>
		</form:form>
	</div>
</main>
<script src="${pageContext.request.contextPath}/resources/js/recordsCheckList.js?v=2.20"></script>
<script src="${pageContext.request.contextPath}/resources/js/event.js?v=1"></script>
<script	src="${pageContext.request.contextPath}/resources/js/formChange.js"></script>
<script>
	if($("#distxtDpsSID").val()== 'true'){
		$("#crimHistSID").attr("disabled", "true");
	}
	if($("#displaySID").val()== 'Y'){
		var textField = document.getElementById("crimHistSidTextField");
		textField.style.display = "block";

		var labelWithAsterRisk = document.getElementById("labelWithAsterRisk");
        labelWithAsterRisk.style.display = "block";

        var labelWithoutAsterRisk = document.getElementById("labelWithoutAsterRisk");
        labelWithoutAsterRisk.style.display = "none";
	}
	if($("#displaySID").val()== 'N' || $("#displaySID").val()==undefined || $("#displaySID").val()== null){
		var textField = document.getElementById("crimHistSidTextField");
		textField.style.display = "block";

		var labelWithAsterRisk = document.getElementById("labelWithAsterRisk");
        labelWithAsterRisk.style.display = "none";

        var labelWithoutAsterRisk = document.getElementById("labelWithoutAsterRisk");
        labelWithoutAsterRisk.style.display = "block";
	}
</script>



