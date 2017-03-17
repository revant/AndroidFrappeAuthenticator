# Add an OAuth Client in Frappe Server. 
 
 - refer: https://frappe.github.io/frappe/user/en/guides/integration/how_to_setup_oauth
 - This is the mobile app that will be communicating with the Frappe OAuth Server
 - **Note :** While adding the Client keep the Redirect URI as http://localhost (or any non existing hostname)
 - Keep a note of the Client ID, Redirect URI and Frappe Server as they are requried later.
 - Frappe server is the server url where your account is hosted

# Add Account on Android

#### Go to Settings > Accounts and click on Add Account
 <img src="https://raw.githubusercontent.com/revant/AndroidFrappeAuthenticator/sync/docs/images/01_add_account.jpeg" width="320">

#### Select Frappe to Frappe Server account.

#### Enter the Frappe server, Client ID and Redirect URI here.
 <img src="https://raw.githubusercontent.com/revant/AndroidFrappeAuthenticator/sync/docs/images/02_enter_server_details.jpeg" width="320">

#### Click Sign In, Enter your account the credentials and login.
 <img src="https://raw.githubusercontent.com/revant/AndroidFrappeAuthenticator/sync/docs/images/03_login_to_frappe_server.jpeg" width="320">

#### Added account is seen under Settings > Accounts > Frappe
 <img src="https://raw.githubusercontent.com/revant/AndroidFrappeAuthenticator/sync/docs/images/05_account_added.jpeg" width="320">

# Allow Contact Permissions

#### In Newer version of Android go to Settings > Apps
 <img src="https://raw.githubusercontent.com/revant/AndroidFrappeAuthenticator/sync/docs/images/06_app_info.jpg" width="320">

#### Select Permissions and enable Contacts permission
 <img src="https://raw.githubusercontent.com/revant/AndroidFrappeAuthenticator/sync/docs/images/07_allow_contact_permission.jpg" width="320">
##### Under All app, search Frappe Authenticator
<img src="https://raw.githubusercontent.com/revant/AndroidFrappeAuthenticator/sync/docs/images/08_sync_contacts.jpeg" width="320">

# Set Contacts to Display in Contacts Manager

#### Go to the Phone's Contacts app,

##### Click the menu and select Contacts to display

<img src="https://raw.githubusercontent.com/revant/AndroidFrappeAuthenticator/sync/docs/images/09_contacts_to_display.jpeg" width="320">

#### Select the Frappe account contacts to display under contact list.

<img src="https://raw.githubusercontent.com/revant/AndroidFrappeAuthenticator/sync/docs/images/10_erpnext_contact.jpeg" width="320">

##### Click on the Frappe icon on phone book contact to view ERPNext Contact

# For Developer

<a href="https://github.com/revant/AndroidFrappeAuthenticator/blob/sync/docs/developer.md">
Develop integrations with Android apps using Frappe Authenticator
</a>
