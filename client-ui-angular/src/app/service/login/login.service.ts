import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { environment } from 'src/environments/environment';

@Injectable({
  providedIn: 'root'
})
export class LoginService {

  getGoogleConsentPageUrlEndpoint = "/social/getConsentPageUrl";
  socialGoogleLoginEndpoint = "/social/socialGoogleLogin";
  verifyGoogleKeyEndpoint = "/login/googleCustomerKeyAuth";
  autheticateCustomerTokenEndpoint = "/secure/customer/customerTokenAuthentication";
  registerCustomerEndpoint = "/login/registerCustomer";
  customerLoginEndpoint = "/login/customerAuthentication";
  sendRegisterOTPEndpoint = "/login/sendRegisterEmailOtp";
  forgotPasswordEndPoint = "/login/login/customerForgotPassword";

  constructor(private http: HttpClient) { }

  getGoogleConsentPageUrl(): Observable<any>{
    return this.http.get(environment.backendBaseUrl+this.getGoogleConsentPageUrlEndpoint);
  }

  verifyGoogleKey(key): Observable<any>{
    const uploadData = new FormData();
    uploadData.append('key', key);
    return this.http.post(environment.backendBaseUrl+this.verifyGoogleKeyEndpoint, uploadData);
  }

  autheticateCustomerToken(){
    return this.http.post(environment.backendBaseUrl+this.autheticateCustomerTokenEndpoint, null);
  }

  createCustomer(firstName, lastName, email, password, otp) : Observable<any>{
    const body = {
        customerInfo :{
          emailId : email,
            firstName : firstName,
            lastName : lastName,
            password : password
        },
        otp: otp
    };
    const httpOptions = {
    headers: new HttpHeaders({
      'Content-Type': 'application/json',
      }),
    };
    return this.http.post(environment.backendBaseUrl+this.registerCustomerEndpoint, body, httpOptions);
  }

  loginCustomer(email, password, rememberMe) : Observable<any>{
    const body = {
          customerInfo : {
            emailId : email,
            password : password
          },
          rememberMe: rememberMe
    };
    const httpOptions = {
    headers: new HttpHeaders({
      'Content-Type': 'application/json',
      }),
    };
    return this.http.post(environment.backendBaseUrl+this.customerLoginEndpoint, body, httpOptions);
  }

  sendRegisterOtp(emailId) : Observable<any>{
    const body = {
      customerInfo : {
           emailId : emailId
        }
    };
    const httpOptions = {
    headers: new HttpHeaders({
      'Content-Type': 'application/json',
      }),
    };
    return this.http.post(environment.backendBaseUrl+this.sendRegisterOTPEndpoint, body, httpOptions);
  }

  forgotPassWord(emailId) : Observable<any>{
    const body = {
      customerInfo : {
           emailId : emailId
        }
    };
    const httpOptions = {
    headers: new HttpHeaders({
      'Content-Type': 'application/json',
      }),
    };
    return this.http.post(environment.backendBaseUrl+this.forgotPasswordEndPoint, body, httpOptions);
  }

  googleSocialLogin(body){
    const httpOptions = {
      headers: new HttpHeaders({
        'Content-Type': 'application/json',
        }),
      };
      return this.http.post(environment.backendBaseUrl+this.socialGoogleLoginEndpoint, body, httpOptions);
  }

}