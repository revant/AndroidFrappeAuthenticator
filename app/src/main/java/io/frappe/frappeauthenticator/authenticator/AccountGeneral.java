package io.frappe.frappeauthenticator.authenticator;

public class AccountGeneral {

    /**
     * Account type id
     */
    public static final String ACCOUNT_TYPE = "io.frappe.frappeauthenticator";

    /**
     * Account name
     */
    public static final String ACCOUNT_NAME = "Frappe";

    /**
     * Auth token types
     */
    public static final String AUTHTOKEN_TYPE_READ_ONLY = "Read only";
    public static final String AUTHTOKEN_TYPE_READ_ONLY_LABEL = "Read only access to an Frappe account";

    public static final String AUTHTOKEN_TYPE_FULL_ACCESS = "Full access";
    public static final String AUTHTOKEN_TYPE_FULL_ACCESS_LABEL = "Full access to an Frappe account";
    public static final String AUTH_ENDPOINT = "/api/method/frappe.integration_broker.oauth2.authorize";
    public static final String TOKEN_ENDPOINT = "/api/method/frappe.integration_broker.oauth2.get_token";
    public static final String OPENID_PROFILE_ENDPOINT = "/api/method/frappe.integration_broker.oauth2.openid_profile";
    public static final ServerAuthenticate sServerAuthenticate = new FrappeServerAuthenticate();
}
