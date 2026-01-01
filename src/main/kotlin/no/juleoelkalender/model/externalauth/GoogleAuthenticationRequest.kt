package no.juleoelkalender.model.externalauth

data class GoogleAuthenticationRequest(var credential: String,
                                       var email: String,
                                       var familyName: String,
                                       var givenName: String,
                                       var picture: String?)
