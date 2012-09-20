package com.octo.android.robospice.sample;

/**
 * It also provides a list of urls to initate the various REST requests.
 * 
 * @author mwa
 * 
 */
public interface UrlConstants {
	public static final String PRODUCT_CATALOG_CREDIT = "catalog/credit";
	public static final String PRODUCT_CATALOG_SAVING = "catalog/epargne";
	public static final String PRODUCT_CATALOG_INSURANCE = "catalog/assurance";
	public static final String PRODUCT_CATALOG_OFFER_DETAIL = "catalog/produit?code=%s";
	public static final String HOMEPAGE_BANNER = "catalog/homepage";

	public static final String REQUEST_STATUS = "prospect/creditRequestStatus?ref=%s&birthDate=%s&appCode=%s";

	public static final String CREDIT_PROJECT_SIMULATOR = "simulator/credits/projectlist";

	public static final String CALL_CENTER_TIMES = "contact/horaires";

	public static final String CREDIT_DISCLAIMER = "infos/creditdisclaimer";
	public static final String CNIL_LEGAL_MENTIONS = "infos/cnil";

	public static final String SAVING_CALCULATOR_CAPACITY = "calc/savingCapacity";
	public static final String SAVING_CALCULATOR_CAPACITY_SEND_MAIL = "calc/savingCapacity/mail";
	public static final String SAVING_CALCULATOR = "calc/savings";

	public static final String SAVING_PRODUCT_SIMULATOR = "simulator/savings/products";
	public static final String SAVING_SIMULATOR = "simulator/savings/json";
	public static final String SAVING_SIMULATOR_SEND_MAIL = "simulator/savings/json/mail";

	public static final String GEOLOC_TYPES = "geoloc/types";
	public static final String GEOLOC_FIND_CITY = "geoloc/findCities?nameAndCode=%s";
	public static final String GEOLOC_SEARCH = "geoloc/byPostalCodeCity";
	public static final String GEOLOC_POSITION_SEARCH = "geoloc/byPosition";

	public static final String NOTIFS_REGISTER = "notifications/register";
	public static final String NOTIFS_UPDATE = "notifications/update";
	public static final String NOTIFS_UNREGISTER = "notifications/unregister";

	public static final String MAIL_SENDER = "mail/generic";
	// TODO ask front services to rename service
	public static final String PROJECT_SUBSCRIPTION = "prospect/creditSubscription";
	public static final String OPTIN_SUBSCRIPTION = "prospect/optinSubscription";
}