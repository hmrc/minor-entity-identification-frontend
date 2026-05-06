package uk.gov.hmrc.minorentityidentificationfrontend.controllers


import play.api.mvc.ControllerComponents
import play.filters.csp._
import play.api.mvc._
import javax.inject.Inject



class CSPReportController @Inject() (cc: ControllerComponents, cspReportAction: CSPReportActionBuilder)
  extends AbstractController(cc) {
  private val logger = org.slf4j.LoggerFactory.getLogger(getClass)

  val report: Action[ScalaCSPReport] = cspReportAction { request =>
    val report = request.body
    logger.warn(
      s"CSP violation: violated-directive = ${report.violatedDirective}, " +
        s"blocked = ${report.blockedUri}, " +
        s"policy = ${report.originalPolicy}"
    )
    Ok("{}").as(JSON)
  }
}
