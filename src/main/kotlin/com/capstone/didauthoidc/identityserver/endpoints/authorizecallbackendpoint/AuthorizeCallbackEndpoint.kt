package com.capstone.didauthoidc.identityserver.endpoints.authorizecallbackendpoint

import com.capstone.didauthoidc.Authsession_constant
import com.capstone.didauthoidc.identityserver.IdentityConstants
import com.capstone.didauthoidc.models.AttributeFilter
import com.capstone.didauthoidc.models.AuthSession
import com.capstone.didauthoidc.models.PresentationRequestConfiguration
import com.capstone.didauthoidc.models.RequestedAttribute
import com.capstone.didauthoidc.utils.OurJacksonObjectMapper
import com.capstone.didauthoidc.utils.PresentationRequestUtils
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.util.MultiValueMap
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestMethod
import org.springframework.web.bind.annotation.RequestParam

@Controller
class AuthorizeCallbackEndpoint {

    companion object {
        const val Name = "VCAuthorizeCallback"
    }

    @RequestMapping("/vc/connect/callback", method = arrayOf(RequestMethod.GET))
    fun processAsync(@RequestParam param: MultiValueMap<String, String>, model: Model): String {

        // generatePresentationRequest 임시 테스트 코드 시작
        val attributeFilter =
            AttributeFilter(schemaName = "verified-email", issuerDid = "MTYqmTBoLT7KLP5RNfgK3b")

        val requestedAttribute = RequestedAttribute(name = "email")
        requestedAttribute.restrictions?.add(attributeFilter)

        val presentationRequestConfiguration = PresentationRequestConfiguration("verified-email", "1.0")
        presentationRequestConfiguration.RequestedAttributes.add(requestedAttribute)

        val configuration: String =
            OurJacksonObjectMapper.getMapper().writeValueAsString(presentationRequestConfiguration)

        // 위에서 만든 presentationRequestConfiguration을 파라미터로 전달하며 generatePresentationRequest를 호출한다.
        val jsonRequestBody: String =
            PresentationRequestUtils.generatePresentationRequest(presentationRequestConfiguration)

        // generatePresentationRequest 임시 테스트 코드 끝

        // 원본 코드 시작
        // 원래 코드는 이 코드이지만, 지금은 하드코딩 하겠다.
        val sessionId: String = param.getValue(IdentityConstants.ChallengeIdQueryParameterName).toString()
        val presentationRecordId = "test-request-config"
        val presentationRequest = "{\n" +
            "    \"name\":\"Basic Proof\",\n" +
            "    \"version\":\"1.0\",\n" +
            "    \"nonce\":\"1010780604715472902183542\",\n" +
            "    \"requested_attributes\":{\n" +
            "        \"d3774d55-c815-4dca-b494-c33ff102dda2\":{\n" +
            "            \"name\":\"email\",\n" +
            "            \"restrictions\":[\n" +
            "                \n" +
            "            ]\n" +
            "        },\n" +
            "        \"7beb897a-172e-4574-b76a-10c949dbea8e\":{\n" +
            "            \"name\":\"first_name\",\n" +
            "            \"restrictions\":[\n" +
            "                \n" +
            "            ]\n" +
            "        },\n" +
            "        \"b086ae43-ca97-45fa-9ae8-9f5539546dad\":{\n" +
            "            \"name\":\"last_name\",\n" +
            "            \"restrictions\":[\n" +
            "                \n" +
            "            ]\n" +
            "        }\n" +
            "    },\n" +
            "    \"requested_predicates\":{\n" +
            "        \n" +
            "    }\n" +
            "}"

        var requestParameters: MutableMap<String, String> = LinkedHashMap()
        requestParameters["scope"] = "openid vc_authn"
        requestParameters["state"] = "qIgqnlLoWZxaMlEJmmX8KRFAHgZ72eGJ"
        requestParameters["response_type"] = "code"
        requestParameters["client_id"] = "keycloak"
        requestParameters["redirect_uri"] = "http://localhost:8080/oidc/auth/cb/"
        requestParameters["nonce"] = "eEJ7joxB5CC8j_LaOaw3Dg"
        requestParameters["pres_req_conf_id"] = "test-request-config"

        // 원래는 Session DB에서 가져와야 하지만, 지금은 하드코딩 하겠다.
//        var session: AuthSession? = sessionStorageService.FindByPresentationIdAsync(sessionId)
        var session: AuthSession = AuthSession(
            presentationRequestId = sessionId,
            presentationRecordId = presentationRecordId,
            presentationRequest = presentationRequest,
            requestParameters = requestParameters
        )

        // response_type이 code이면 url에 code를 파라미터로 붙여서 넘겨준다. state도 있다면 state도 url에 붙인다.
        if (session.requestParameters[IdentityConstants.ResponseTypeUriParameterName] == "code") {
            var url: String =
                "${session.requestParameters[IdentityConstants.RedirectUriParameterName]}?code=${Authsession_constant.session_Id}"

            if (session.requestParameters.contains(IdentityConstants.StateParameterName))
                url += "&state=${Authsession_constant.state}"
            Authsession_constant.redirect_url = url
            // url을 다 만들었으므로, 이 url로 redirect 시킨다.
            return "redirect:$url"
        }
        return "[ERROR] : Unknown response type"
    }
}
