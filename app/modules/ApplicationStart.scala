package modules

import services.AuthenticationService

import javax.inject.{Inject, Singleton}

@Singleton
class ApplicationStart @Inject()(authenticationService: AuthenticationService) {

//    authenticationService.authenticate()

}