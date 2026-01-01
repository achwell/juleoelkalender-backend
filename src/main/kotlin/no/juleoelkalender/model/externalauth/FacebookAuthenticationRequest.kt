package no.juleoelkalender.model.externalauth

data class FacebookAuthenticationRequest(var id: String, var firstName: String, var middleName: String?, var lastName: String, var email: String, var picture: FacebookPicture?)